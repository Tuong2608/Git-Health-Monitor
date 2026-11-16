"""RQ2 controlled scenario evaluator & pilot evidence auditor (STT 31)."""
import argparse
import hashlib
import json
import os
import shutil
import subprocess
import tempfile
from datetime import datetime, timedelta, timezone
from decimal import Decimal
from pathlib import Path
from rq2_trends import compare


def java_source_with_ccn(class_name: str, target_ccn: int, extra_lines: int = 0) -> str:
    """Generate valid Java source whose single method has cyclomatic complexity == target_ccn."""
    if target_ccn < 1:
        raise ValueError("target_ccn must be >= 1")
    lines = [f"public class {class_name} {{"]
    for idx in range(extra_lines):
        lines.append(f"    private final int field{idx} = {idx};")
    lines.append("    public int evaluate(int x) {")
    lines.append("        int acc = 0;")
    for idx in range(target_ccn - 1):
        lines.append(f"        if (x == {idx + 1}) acc += {idx + 1};")
    lines.append("        return acc + x;")
    lines.append("    }")
    lines.append("}")
    return "\n".join(lines) + "\n"


def _git(repo_dir: Path, *args: str, env_extra: dict | None = None, input_bytes: bytes | None = None) -> str:
    env = os.environ.copy()
    if env_extra:
        env.update(env_extra)
    res = subprocess.run(
        ["git", "-C", str(repo_dir), *args],
        input=input_bytes,
        capture_output=True,
        env=env,
        check=True,
    )
    return res.stdout.decode("utf-8", errors="replace").strip()


def _init_repo(repo_dir: Path, repo_url: str) -> None:
    repo_dir.mkdir(parents=True, exist_ok=True)
    subprocess.run(["git", "init", "-b", "main", str(repo_dir)], check=True, capture_output=True)
    _git(repo_dir, "config", "user.name", "Scenario Fixture")
    _git(repo_dir, "config", "user.email", "fixture@example.test")
    _git(repo_dir, "remote", "add", "origin", repo_url)


def _commit_files(repo_dir: Path, files: dict[str, str], message: str, iso_utc: str) -> str:
    for rel, content in files.items():
        target = repo_dir / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(content, encoding="utf-8", newline="\n")
        _git(repo_dir, "add", rel)
    env = {"GIT_AUTHOR_DATE": iso_utc, "GIT_COMMITTER_DATE": iso_utc}
    _git(repo_dir, "commit", "-m", message, env_extra=env)
    return _git(repo_dir, "rev-parse", "HEAD")


def _export_snapshot(
    repo_dir: Path, sha: str, window_start: str, window_end: str, out_json: Path, java_cmd: str, classpath: str
) -> dict:
    out_json.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(
        [
            java_cmd,
            "-Xmx1g",
            "-cp",
            classpath,
            "com.example.demo.gitanalyzer.HistoricalSnapshotExport",
            str(repo_dir),
            sha,
            window_start,
            window_end,
            str(out_json),
        ],
        check=True,
        capture_output=True,
        text=True,
    )
    return json.loads(out_json.read_text(encoding="utf-8"))


def verify_comparison_traceability(before_snap: dict, after_snap: dict, comparison: dict) -> dict:
    """Verify invariants: evidenceCommits belong strictly to after window facts and match frequency/delta rules."""
    fact_shas_by_lineage: dict[str, set[str]] = {}
    for fact in after_snap.get("facts", []):
        fact_shas_by_lineage.setdefault(fact["lineageKey"], set()).add(fact["commitSha"])

    after_files = {row["lineageKey"]: row for row in after_snap["files"]}
    verified_changes = 0
    for ch in comparison["changes"]:
        key = ch["lineageKey"]
        expected_ev = sorted(fact_shas_by_lineage.get(key, set()))
        if ch["evidenceCommits"] != expected_ev:
            raise ValueError(f"Evidence commit mismatch for {key}: {ch['evidenceCommits']} != {expected_ev}")
        freq_after = after_files[key]["frequency"]
        if freq_after == 0 and ch["evidenceCommits"]:
            raise ValueError(f"Unexpected evidenceCommits when frequency == 0 for {key}")
        if freq_after and freq_after > 0 and not ch["evidenceCommits"]:
            raise ValueError(f"Missing evidenceCommits when frequency > 0 for {key}")
        verified_changes += 1

    # Verify strict Decimal delta threshold integrity
    thresh = Decimal(str(comparison["threshold"]))
    for ch in comparison["changes"]:
        expected_flag = Decimal(str(ch["after"]["hotspot"])) - Decimal(str(ch["before"]["hotspot"])) > thresh
        if ch["candidateIncrease"] != expected_flag:
            raise ValueError(f"Decimal threshold mismatch for {ch['lineageKey']}")

    if comparison["status"] == "BLOCKED" and comparison["alertCandidates"] is not None:
        raise ValueError("Blocked comparison must keep alertCandidates as null")
    return {"verifiedChanges": verified_changes, "status": comparison["status"], "blockers": comparison["blockers"]}


def summarize_pilot_evidence(pilot_dir: Path) -> dict:
    """Audit existing Week 9 OSS pilot snapshots and pairwise comparisons in pure Python."""
    summary_path = pilot_dir / "run-summary.json"
    run_summary = json.loads(summary_path.read_text(encoding="utf-8"))
    total_snapshots = 0
    succeeded_snapshots = 0
    total_comparisons = 0
    comparable_pairs = 0
    blocked_pairs = 0
    audit_cases = []

    for repo in run_summary["repositories"]:
        prev_snap = None
        for row in repo["snapshots"]:
            total_snapshots += 1
            if row.get("status") != "SUCCESS":
                prev_snap = None
                continue
            succeeded_snapshots += 1
            snap_file = pilot_dir / row["output"]
            if hashlib.sha256(snap_file.read_bytes()).hexdigest() != row["sha256"]:
                raise ValueError(f"SHA-256 mismatch for {snap_file}")
            snap = json.loads(snap_file.read_text(encoding="utf-8"))
            if prev_snap is not None and "comparison" in row:
                total_comparisons += 1
                comp = json.loads((pilot_dir / row["comparison"]).read_text(encoding="utf-8"))
                trace = verify_comparison_traceability(prev_snap, snap, comp)
                if comp["status"] == "COMPARABLE":
                    comparable_pairs += 1
                else:
                    blocked_pairs += 1
                top_example = None
                if comp["changes"]:
                    top = sorted(comp["changes"], key=lambda c: (-abs(c["hotspotDelta"]), c["lineageKey"]))[0]
                    top_example = {
                        "path": top["afterPath"],
                        "freqBefore": top["before"]["frequency"],
                        "freqAfter": top["after"]["frequency"],
                        "ccnBefore": top["before"]["maxCcn"],
                        "ccnAfter": top["after"]["maxCcn"],
                        "hotspotBefore": top["before"]["hotspot"],
                        "hotspotAfter": top["after"]["hotspot"],
                        "hotspotDelta": top["hotspotDelta"],
                        "evidenceCommitCount": len(top["evidenceCommits"]),
                    }
                audit_cases.append(
                    {
                        "repository": repo["id"],
                        "comparisonFile": row["comparison"],
                        "status": comp["status"],
                        "blockers": comp["blockers"],
                        "comparableCommonFiles": comp["comparableCommonFiles"],
                        "verifiedTraceabilityCount": trace["verifiedChanges"],
                        "topDiagnosticChange": top_example,
                    }
                )
            prev_snap = snap

    return {
        "totalSnapshots": total_snapshots,
        "succeededSnapshots": succeeded_snapshots,
        "totalComparisons": total_comparisons,
        "comparablePairs": comparable_pairs,
        "blockedPairs": blocked_pairs,
        "comparisons": audit_cases,
    }


def _build_fast_import_boundary_repo(repo_dir: Path, repo_url: str) -> tuple[str, str, str]:
    """Create a 21-file repo so F08.java shifts hotspot from exact 0.40 to 0.55 (t1), then 0.75 (t2)."""
    _init_repo(repo_dir, repo_url)
    # Ranks at t0 (all within [2025-05-01, 2025-07-01)):
    # F00..F07: i+1 commits & ccn=i+1 (values 1..8)
    # F08: 9 commits & ccn=9
    # F09..F11: 10, 11, 12 commits & ccn=10, 11, 12
    # F12..F20: 20..28 commits & ccn=20..28
    targets: dict[str, int] = {}
    for i in range(8):
        targets[f"F{i:02d}"] = i + 1
    targets["F08"] = 9
    targets["F09"] = 10
    targets["F10"] = 11
    targets["F11"] = 12
    for i in range(12, 21):
        targets[f"F{i:02d}"] = i + 8  # 20..28

    base_t0 = datetime(2025, 5, 1, 12, 0, 0, tzinfo=timezone.utc)
    stream_parts: list[bytes] = []
    mark = 1
    step = 0
    for cls, count in sorted(targets.items()):
        for c_idx in range(1, count + 1):
            ts = int((base_t0 + timedelta(minutes=step)).timestamp())
            step += 1
            # On final commit for cls, reach ccn == count; prior commits stay ccn == 1
            ccn = count if c_idx == count else 1
            src = java_source_with_ccn(cls, ccn, extra_lines=c_idx).encode("utf-8")
            commit_msg = f"t0 {cls} rev {c_idx}".encode("utf-8")
            parent_line = f"from :{mark - 1}\n" if mark > 1 else ""
            header = (
                f"commit refs/heads/main\n"
                f"mark :{mark}\n"
                f"author Scenario Fixture <fixture@example.test> {ts} +0000\n"
                f"committer Scenario Fixture <fixture@example.test> {ts} +0000\n"
                f"data {len(commit_msg)}\n"
            ).encode("utf-8")
            file_op = f"M 100644 inline {cls}.java\ndata {len(src)}\n".encode("utf-8")
            stream_parts.append(header + commit_msg + b"\n" + parent_line.encode("utf-8") + file_op + src + b"\n")
            mark += 1

    t0_mark = mark - 1
    # Ensure window [2025-05-01, 2025-11-01) keeps t0 commits in window at t1 and t2!
    # Wait: 180 days from 2025-05-01 is 2025-10-28; so use t0_end=2025-07-01 (start 2025-01-02),
    # t1_end=2025-08-01 (start 2025-02-02), t2_end=2025-09-01 (start 2025-03-05) -> all May commits remain inside all 3 windows!
    base_t1 = datetime(2025, 7, 15, 12, 0, 0, tzinfo=timezone.utc)
    for idx in range(1, 6):  # F08 freq goes 9 -> 14, ccn -> 14 (rank passes F09..F11 -> 11/20 = 0.55)
        ts = int((base_t1 + timedelta(minutes=idx)).timestamp())
        ccn = 14 if idx == 5 else 9 + idx
        src = java_source_with_ccn("F08", ccn, extra_lines=10 + idx).encode("utf-8")
        msg = f"t1 F08 edit {idx}".encode("utf-8")
        header = (
            f"commit refs/heads/main\nmark :{mark}\n"
            f"author Scenario Fixture <fixture@example.test> {ts} +0000\n"
            f"committer Scenario Fixture <fixture@example.test> {ts} +0000\n"
            f"data {len(msg)}\n"
        ).encode("utf-8")
        parent_line = f"from :{mark - 1}\n".encode("utf-8")
        file_op = f"M 100644 inline F08.java\ndata {len(src)}\n".encode("utf-8")
        stream_parts.append(header + msg + b"\n" + parent_line + file_op + src + b"\n")
        mark += 1
    t1_mark = mark - 1

    # Bump F12..F15 threshold? No, bump F08 from 14 -> 24 (passes F12..F15 values 20..23 -> rank 15/20 = 0.75!)
    base_t2 = datetime(2025, 8, 15, 12, 0, 0, tzinfo=timezone.utc)
    for idx in range(1, 11):  # 14 + 10 = 24
        ts = int((base_t2 + timedelta(minutes=idx)).timestamp())
        ccn = 24 if idx == 10 else 14 + idx
        src = java_source_with_ccn("F08", ccn, extra_lines=20 + idx).encode("utf-8")
        msg = f"t2 F08 escalation {idx}".encode("utf-8")
        header = (
            f"commit refs/heads/main\nmark :{mark}\n"
            f"author Scenario Fixture <fixture@example.test> {ts} +0000\n"
            f"committer Scenario Fixture <fixture@example.test> {ts} +0000\n"
            f"data {len(msg)}\n"
        ).encode("utf-8")
        parent_line = f"from :{mark - 1}\n".encode("utf-8")
        file_op = f"M 100644 inline F08.java\ndata {len(src)}\n".encode("utf-8")
        stream_parts.append(header + msg + b"\n" + parent_line + file_op + src + b"\n")
        mark += 1
    t2_mark = mark - 1

    marks_file = repo_dir / "marks.txt"
    subprocess.run(
        ["git", "-C", str(repo_dir), "fast-import", f"--export-marks={marks_file}"],
        input=b"".join(stream_parts),
        check=True,
        capture_output=True,
    )
    mark_map = {}
    for line in marks_file.read_text(encoding="utf-8").splitlines():
        m_id, sha = line.split()
        mark_map[int(m_id[1:])] = sha
    marks_file.unlink()
    return mark_map[t0_mark], mark_map[t1_mark], mark_map[t2_mark]


def run_scenarios(out_dir: Path, java_cmd: str, classpath: str) -> dict:
    out_dir.mkdir(parents=True, exist_ok=True)
    pilot_dir = Path(__file__).resolve().parents[2] / "docs" / "week-09" / "evidence" / "pilot"
    scenarios_results = []

    with tempfile.TemporaryDirectory() as tmp:
        root = Path(tmp)

        # --- S1: Real hotspot regression (frequency + complexity climb together) ---
        s1_repo = root / "s1"
        _init_repo(s1_repo, "https://github.com/fixture/s1-regression")
        _commit_files(
            s1_repo,
            {
                "A.java": java_source_with_ccn("A", 1),
                "B.java": java_source_with_ccn("B", 1),
                "C.java": java_source_with_ccn("C", 3),
            },
            "initial seed",
            "2025-02-01T10:00:00Z",
        )
        t0_s1 = _commit_files(
            s1_repo,
            {
                "B.java": java_source_with_ccn("B", 2, 1),
                "C.java": java_source_with_ccn("C", 4, 1),
            },
            "update B and C",
            "2025-02-10T10:00:00Z",
        )
        ev_s1 = [
            _commit_files(s1_repo, {"A.java": java_source_with_ccn("A", 2, 1)}, "s1 edit A 1", "2025-07-10T10:00:00Z"),
            _commit_files(s1_repo, {"A.java": java_source_with_ccn("A", 4, 2)}, "s1 edit A 2", "2025-07-20T10:00:00Z"),
            _commit_files(s1_repo, {"A.java": java_source_with_ccn("A", 6, 3)}, "s1 edit A 3", "2025-08-10T10:00:00Z"),
        ]
        t1_s1 = ev_s1[-1]
        s1_before = _export_snapshot(s1_repo, t0_s1, "2025-01-02T00:00:00Z", "2025-07-01T00:00:00Z", out_dir / "s1-before.json", java_cmd, classpath)
        s1_after = _export_snapshot(s1_repo, t1_s1, "2025-03-05T00:00:00Z", "2025-09-01T00:00:00Z", out_dir / "s1-after.json", java_cmd, classpath)
        # Verify same duration (180 days): 2025-01-02 -> 2025-07-01 is 180d; 2025-03-05 -> 2025-09-01 is 180d.
        s1_comp = compare(s1_before, s1_after, 0.15)
        verify_comparison_traceability(s1_before, s1_after, s1_comp)
        assert s1_comp["status"] == "COMPARABLE" and s1_comp["alertCandidates"] == 1
        s1_a = next(c for c in s1_comp["changes"] if c["afterPath"] == "A.java")
        assert s1_a["evidenceCommits"] == sorted(ev_s1) and s1_a["candidateIncrease"] is True
        scenarios_results.append({
            "id": "S1_REAL_HOTSPOT_REGRESSION",
            "title": "Tăng rủi ro thực sự khi tần suất và độ phức tạp cùng tăng",
            "status": s1_comp["status"],
            "alertCandidates": s1_comp["alertCandidates"],
            "targetFile": "A.java",
            "hotspotBefore": s1_a["before"]["hotspot"],
            "hotspotAfter": s1_a["after"]["hotspot"],
            "hotspotDelta": s1_a["hotspotDelta"],
            "evidenceCommits": s1_a["evidenceCommits"],
            "passed": True,
        })

        # --- S2: File rename preserves lineageKey across snapshots ---
        s2_repo = root / "s2"
        _init_repo(s2_repo, "https://github.com/fixture/s2-rename")
        _commit_files(
            s2_repo,
            {
                "src/LegacyService.java": java_source_with_ccn("LegacyService", 1, extra_lines=10),
                "src/Mid.java": java_source_with_ccn("Mid", 2),
                "src/High.java": java_source_with_ccn("High", 3),
            },
            "initial seed",
            "2025-02-01T10:00:00Z",
        )
        t0_s2 = _commit_files(
            s2_repo,
            {
                "src/Mid.java": java_source_with_ccn("Mid", 3, 1),
                "src/High.java": java_source_with_ccn("High", 4, 1),
            },
            "mid/high activity",
            "2025-02-10T10:00:00Z",
        )
        # Pure rename first (keeps high similarity for JGit RenameDetector >= 60%), then modify new path
        _git(s2_repo, "mv", "src/LegacyService.java", "src/RenamedService.java")
        r_ev1 = _git(s2_repo, "commit", "-m", "rename LegacyService -> RenamedService", env_extra={"GIT_AUTHOR_DATE": "2025-07-05T10:00:00Z", "GIT_COMMITTER_DATE": "2025-07-05T10:00:00Z"})
        r_ev1 = _git(s2_repo, "rev-parse", "HEAD")
        r_ev2 = _commit_files(s2_repo, {"src/RenamedService.java": java_source_with_ccn("RenamedService", 5, extra_lines=10)}, "add branches 1", "2025-07-15T10:00:00Z")
        r_ev3 = _commit_files(s2_repo, {"src/RenamedService.java": java_source_with_ccn("RenamedService", 7, extra_lines=10)}, "add branches 2", "2025-08-01T10:00:00Z")
        t1_s2 = r_ev3
        s2_before = _export_snapshot(s2_repo, t0_s2, "2025-01-02T00:00:00Z", "2025-07-01T00:00:00Z", out_dir / "s2-before.json", java_cmd, classpath)
        s2_after = _export_snapshot(s2_repo, t1_s2, "2025-03-05T00:00:00Z", "2025-09-01T00:00:00Z", out_dir / "s2-after.json", java_cmd, classpath)
        s2_comp = compare(s2_before, s2_after, 0.15)
        verify_comparison_traceability(s2_before, s2_after, s2_comp)
        assert s2_comp["status"] == "COMPARABLE" and s2_comp["added"] == [] and s2_comp["removed"] == []
        s2_r = next(c for c in s2_comp["changes"] if c["afterPath"] == "src/RenamedService.java")
        assert s2_r["beforePath"] == "src/LegacyService.java" and s2_r["candidateIncrease"] is True
        scenarios_results.append({
            "id": "S2_RENAME_LINEAGE_PRESERVED",
            "title": "Đổi tên tập tin vẫn giữ chung lineageKey và phát hiện tăng rủi ro",
            "status": s2_comp["status"],
            "alertCandidates": s2_comp["alertCandidates"],
            "beforePath": s2_r["beforePath"],
            "afterPath": s2_r["afterPath"],
            "lineageKey": s2_r["lineageKey"],
            "hotspotBefore": s2_r["before"]["hotspot"],
            "hotspotAfter": s2_r["after"]["hotspot"],
            "hotspotDelta": s2_r["hotspotDelta"],
            "added": s2_comp["added"],
            "removed": s2_comp["removed"],
            "evidenceCommits": s2_r["evidenceCommits"],
            "passed": True,
        })

        # --- S3: Window slide drops frequency without touching code (5-file anchor controls population shift) ---
        s3_repo = root / "s3"
        _init_repo(s3_repo, "https://github.com/fixture/s3-window-slide")
        _commit_files(
            s3_repo,
            {
                "StableComplex.java": java_source_with_ccn("StableComplex", 5),
                "Anchor.java": java_source_with_ccn("Anchor", 4),
                "Peer1.java": java_source_with_ccn("Peer1", 1),
                "Peer2.java": java_source_with_ccn("Peer2", 1),
                "Peer3.java": java_source_with_ccn("Peer3", 2),
            },
            "early commit 1",
            "2025-01-10T10:00:00Z",
        )
        _commit_files(s3_repo, {"StableComplex.java": java_source_with_ccn("StableComplex", 5, 1)}, "early commit 2", "2025-01-15T10:00:00Z")
        _commit_files(s3_repo, {"StableComplex.java": java_source_with_ccn("StableComplex", 5, 2)}, "early commit 3", "2025-01-20T10:00:00Z")
        for idx in range(4):
            _commit_files(s3_repo, {"Anchor.java": java_source_with_ccn("Anchor", 4, idx + 1)}, f"anchor {idx}", f"2025-05-0{idx + 1}T10:00:00Z")
        t0_s3 = _commit_files(s3_repo, {"Anchor.java": java_source_with_ccn("Anchor", 4, 5), "Peer3.java": java_source_with_ccn("Peer3", 2, 1)}, "later anchor+peer3", "2025-06-15T10:00:00Z")
        t1_s3 = _commit_files(s3_repo, {"README.md": "window slide check\n"}, "non-java commit at t1", "2025-08-15T10:00:00Z")
        s3_before = _export_snapshot(s3_repo, t0_s3, "2025-01-02T00:00:00Z", "2025-07-01T00:00:00Z", out_dir / "s3-before.json", java_cmd, classpath)
        s3_after = _export_snapshot(s3_repo, t1_s3, "2025-03-05T00:00:00Z", "2025-09-01T00:00:00Z", out_dir / "s3-after.json", java_cmd, classpath)
        s3_comp = compare(s3_before, s3_after, 0.15)
        verify_comparison_traceability(s3_before, s3_after, s3_comp)
        s3_sc = next(c for c in s3_comp["changes"] if c["afterPath"] == "StableComplex.java")
        assert s3_comp["status"] == "COMPARABLE" and s3_comp["alertCandidates"] == 0
        assert s3_sc["before"]["maxCcn"] == 5 and s3_sc["after"]["maxCcn"] == 5
        assert s3_sc["before"]["frequency"] == 3 and s3_sc["after"]["frequency"] == 0
        assert s3_sc["evidenceCommits"] == [] and s3_sc["hotspotDelta"] < 0
        scenarios_results.append({
            "id": "S3_WINDOW_SLIDE_NO_NEW_COMMITS",
            "title": "Trượt cửa sổ thời gian làm giảm hotspot dù maxCcn giữ nguyên (tránh kết luận nhầm đã sửa code)",
            "status": s3_comp["status"],
            "alertCandidates": s3_comp["alertCandidates"],
            "targetFile": "StableComplex.java",
            "freqBefore": s3_sc["before"]["frequency"],
            "freqAfter": s3_sc["after"]["frequency"],
            "ccnBefore": s3_sc["before"]["maxCcn"],
            "ccnAfter": s3_sc["after"]["maxCcn"],
            "hotspotBefore": s3_sc["before"]["hotspot"],
            "hotspotAfter": s3_sc["after"]["hotspot"],
            "hotspotDelta": s3_sc["hotspotDelta"],
            "evidenceCommits": s3_sc["evidenceCommits"],
            "passed": True,
        })

        # --- S4: Exact Decimal boundary +0.15 vs +0.35 on 21-file controlled repo ---
        s4_repo = root / "s4"
        t0_s4, t1_s4, t2_s4 = _build_fast_import_boundary_repo(s4_repo, "https://github.com/fixture/s4-boundary")
        s4_t0 = _export_snapshot(s4_repo, t0_s4, "2025-01-02T00:00:00Z", "2025-07-01T00:00:00Z", out_dir / "s4-t0.json", java_cmd, classpath)
        s4_t1 = _export_snapshot(s4_repo, t1_s4, "2025-02-02T00:00:00Z", "2025-08-01T00:00:00Z", out_dir / "s4-t1.json", java_cmd, classpath)
        s4_t2 = _export_snapshot(s4_repo, t2_s4, "2025-03-05T00:00:00Z", "2025-09-01T00:00:00Z", out_dir / "s4-t2.json", java_cmd, classpath)
        s4_c01 = compare(s4_t0, s4_t1, 0.15)
        s4_c12 = compare(s4_t1, s4_t2, 0.15)
        verify_comparison_traceability(s4_t0, s4_t1, s4_c01)
        verify_comparison_traceability(s4_t1, s4_t2, s4_c12)
        f08_01 = next(c for c in s4_c01["changes"] if c["afterPath"] == "F08.java")
        f08_12 = next(c for c in s4_c12["changes"] if c["afterPath"] == "F08.java")
        assert s4_c01["alertCandidates"] == 0 and f08_01["hotspotDelta"] == 0.15 and f08_01["candidateIncrease"] is False
        assert s4_c12["alertCandidates"] == 1 and f08_12["candidateIncrease"] is True and f08_12["hotspotDelta"] > 0.15
        scenarios_results.append({
            "id": "S4_THRESHOLD_BOUNDARY_DECIMAL",
            "title": "Kiểm chứng biên ngưỡng chính xác +0.15 bằng Decimal (0.40 -> 0.55 không báo giả, vượt >0.15 mới báo)",
            "exactBoundaryStep": {
                "status": s4_c01["status"],
                "alertCandidates": s4_c01["alertCandidates"],
                "targetFile": "F08.java",
                "hotspotBefore": f08_01["before"]["hotspot"],
                "hotspotAfter": f08_01["after"]["hotspot"],
                "hotspotDelta": f08_01["hotspotDelta"],
                "candidateIncrease": f08_01["candidateIncrease"],
            },
            "aboveBoundaryStep": {
                "status": s4_c12["status"],
                "alertCandidates": s4_c12["alertCandidates"],
                "targetFile": "F08.java",
                "hotspotBefore": f08_12["before"]["hotspot"],
                "hotspotAfter": f08_12["after"]["hotspot"],
                "hotspotDelta": f08_12["hotspotDelta"],
                "candidateIncrease": f08_12["candidateIncrease"],
            },
            "passed": True,
        })

        # --- S5: Safety blockers (merge lineage ambiguity + window duration mismatch) ---
        s5_repo = root / "s5"
        _init_repo(s5_repo, "https://github.com/fixture/s5-blockers")
        t0_s5 = _commit_files(
            s5_repo,
            {"Main.java": java_source_with_ccn("Main", 1), "Peer.java": java_source_with_ccn("Peer", 2)},
            "base commit",
            "2025-02-01T10:00:00Z",
        )
        _git(s5_repo, "checkout", "-b", "feature")
        _commit_files(s5_repo, {"BranchOnly.java": java_source_with_ccn("BranchOnly", 2)}, "add file on branch", "2025-07-10T10:00:00Z")
        _git(s5_repo, "checkout", "main")
        _commit_files(s5_repo, {"Main.java": java_source_with_ccn("Main", 6, 3)}, "escalate Main on main", "2025-07-15T10:00:00Z")
        _git(s5_repo, "merge", "--no-ff", "feature", "-m", "merge feature", env_extra={"GIT_AUTHOR_DATE": "2025-08-01T10:00:00Z", "GIT_COMMITTER_DATE": "2025-08-01T10:00:00Z"})
        t1_s5 = _git(s5_repo, "rev-parse", "HEAD")
        s5_before = _export_snapshot(s5_repo, t0_s5, "2025-01-02T00:00:00Z", "2025-07-01T00:00:00Z", out_dir / "s5-before.json", java_cmd, classpath)
        s5_after = _export_snapshot(s5_repo, t1_s5, "2025-03-05T00:00:00Z", "2025-09-01T00:00:00Z", out_dir / "s5-after.json", java_cmd, classpath)
        s5_comp_merge = compare(s5_before, s5_after, 0.15)
        verify_comparison_traceability(s5_before, s5_after, s5_comp_merge)
        assert s5_comp_merge["status"] == "BLOCKED" and "INCOMPLETE_COVERAGE" in s5_comp_merge["blockers"]
        assert s5_comp_merge["alertCandidates"] is None and s5_comp_merge["diagnosticChangesOnly"] is True

        # Also verify window mismatch blocker on S1
        s1_before_90d = _export_snapshot(s1_repo, t0_s1, "2025-04-02T00:00:00Z", "2025-07-01T00:00:00Z", out_dir / "s1-before-90d.json", java_cmd, classpath)
        s5_comp_window = compare(s1_before_90d, s1_after, 0.15)
        assert s5_comp_window["status"] == "BLOCKED" and "WINDOW_DURATION_CHANGED" in s5_comp_window["blockers"]
        assert s5_comp_window["alertCandidates"] is None

        scenarios_results.append({
            "id": "S5_SAFETY_BLOCKERS",
            "title": "Chặn cảnh báo an toàn khi vướng LINEAGE_AMBIGUOUS qua merge hoặc lệch độ dài cửa sổ",
            "mergeCoverageCheck": {
                "status": s5_comp_merge["status"],
                "blockers": s5_comp_merge["blockers"],
                "alertCandidates": s5_comp_merge["alertCandidates"],
                "diagnosticChangesOnly": s5_comp_merge["diagnosticChangesOnly"],
                "ambiguousFilesAfter": s5_after["summary"]["ambiguousFiles"],
            },
            "windowMismatchCheck": {
                "status": s5_comp_window["status"],
                "blockers": s5_comp_window["blockers"],
                "alertCandidates": s5_comp_window["alertCandidates"],
            },
            "passed": True,
        })

    pilot_audit = summarize_pilot_evidence(pilot_dir) if pilot_dir.exists() else None
    summary = {
        "schemaVersion": 1,
        "kind": "RQ2_COMPLETE_SCENARIO_AND_PILOT_AUDIT",
        "scenariosPassed": sum(1 for s in scenarios_results if s["passed"]),
        "scenariosTotal": len(scenarios_results),
        "scenarios": scenarios_results,
        "pilotAudit": pilot_audit,
    }
    summary_file = out_dir / "rq2-scenarios-summary.json"
    summary_file.write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return summary


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out-dir", type=Path, required=True)
    parser.add_argument("--java", default="java")
    parser.add_argument("--classpath", required=True)
    args = parser.parse_args()
    summary = run_scenarios(args.out_dir, args.java, args.classpath)
    print(f"RQ2 Scenarios PASSED: {summary['scenariosPassed']}/{summary['scenariosTotal']}")
    if summary["pilotAudit"]:
        pa = summary["pilotAudit"]
        print(f"Pilot OSS Audit: {pa['succeededSnapshots']}/{pa['totalSnapshots']} snapshots, {pa['totalComparisons']} comparisons verified")


if __name__ == "__main__":
    main()
