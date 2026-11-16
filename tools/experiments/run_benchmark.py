"""NFR03 Performance benchmark harness up to 10,000 commits (STT 32 & STT 10)."""
import argparse
import json
import os
import platform
import shutil
import statistics
import subprocess
import tempfile
import time
from datetime import datetime, timedelta, timezone
from pathlib import Path

BENCHMARK_JAVA_DRIVER = r"""
import com.example.demo.gitanalyzer.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import tools.jackson.databind.json.JsonMapper;

public final class BenchmarkStageRunner {
    public static void main(String[] args) throws Exception {
        if (args.length != 5) {
            throw new IllegalArgumentException("Usage: BenchmarkStageRunner <repoDir> <headSha> <start> <end> <iterations>");
        }
        Path repo = Path.of(args[0]);
        String sha = args[1];
        Instant start = Instant.parse(args[2]);
        Instant end = Instant.parse(args[3]);
        int iterations = Integer.parseInt(args[4]);

        var mapper = JsonMapper.builder().build();
        var runs = new ArrayList<Map<String, Object>>();
        int reachable = 0, selected = 0, filesCount = 0, factsCount = 0;

        for (int i = 1; i <= iterations; i++) {
            long t0 = System.nanoTime();
            var options = GitIngestion.Options.defaults(start, end);
            var extracted = new GitIngestion().extract(repo, sha, options);
            long t1 = System.nanoTime();
            var complexities = new LizardComplexityAnalyzer().analyzeAtHead(
                    repo, sha, extracted.currentFiles().keySet(), options.maxBlobBytes(), options.deadline());
            long t2 = System.nanoTime();
            var snapshot = new RepositoryMetricCalculator().calculate(
                    extracted, complexities, RepositoryMetricCalculator.Policy.defaults());
            long t3 = System.nanoTime();
            String jsonOut = mapper.writeValueAsString(snapshot.summary()) + mapper.writeValueAsString(snapshot.files());
            long t4 = System.nanoTime();

            reachable = extracted.reachableCommits();
            selected = extracted.selectedCommits();
            filesCount = snapshot.files().size();
            factsCount = extracted.facts().size();

            var row = new LinkedHashMap<String, Object>();
            row.put("iteration", i);
            row.put("runType", i == 1 ? "cold_jvm" : "warm_jvm");
            row.put("jgitMs", (t1 - t0) / 1_000_000.0);
            row.put("lizardMs", (t2 - t1) / 1_000_000.0);
            row.put("metricMs", (t3 - t2) / 1_000_000.0);
            row.put("serializeMs", (t4 - t3) / 1_000_000.0);
            row.put("totalMs", (t4 - t0) / 1_000_000.0);
            row.put("payloadBytes", jsonOut.length());
            runs.add(row);
        }

        var out = new LinkedHashMap<String, Object>();
        out.put("headSha", sha);
        out.put("reachableCommits", reachable);
        out.put("selectedCommits", selected);
        out.put("javaFiles", filesCount);
        out.put("factsCount", factsCount);
        out.put("runs", runs);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(out));
    }
}
"""


def _java_class_source(class_name: str, seed_idx: int, branch_count: int) -> str:
    lines = [
        "package bench.sample;",
        f"public class {class_name} {{",
        f"    private final int base = {seed_idx};",
        "    public int compute(int input) {",
        "        int total = base;",
    ]
    for b in range(branch_count):
        lines.append(f"        if (input == {b + 1}) total += {b + seed_idx};")
    lines.append("        return total;")
    lines.append("    }")
    lines.append("}")
    return "\n".join(lines) + "\n"


def build_synthetic_repo(repo_dir: Path, commit_count: int, file_count: int) -> dict:
    """Create a bare Git repository with exact `commit_count` non-merge commits inside a 180d window."""
    t_gen_start = time.perf_counter()
    if repo_dir.exists():
        shutil.rmtree(repo_dir)
    repo_dir.mkdir(parents=True, exist_ok=True)
    subprocess.run(["git", "init", "--bare", "-b", "main", str(repo_dir)], check=True, capture_output=True)

    authors = [
        ("Dev Alpha", "alpha@bench.test"),
        ("Dev Beta", "beta@bench.test"),
        ("Dev Gamma", "gamma@bench.test"),
    ]
    # Keep all commits strictly within [2026-01-10, 2026-06-25) inside 180-day window ending 2026-07-01T00:00:00Z
    base_dt = datetime(2026, 1, 10, 0, 0, 0, tzinfo=timezone.utc)
    step_seconds = max(1, (160 * 86400) // max(1, commit_count))

    stream = bytearray()
    # Pre-seed initial file contents on commit 1
    mark = 1
    first_ts = int(base_dt.timestamp())
    msg0 = b"bench seed all files"
    hdr0 = (
        f"commit refs/heads/main\nmark :{mark}\n"
        f"author {authors[0][0]} <{authors[0][1]}> {first_ts} +0000\n"
        f"committer {authors[0][0]} <{authors[0][1]}> {first_ts} +0000\n"
        f"data {len(msg0)}\n"
    ).encode("utf-8")
    stream.extend(hdr0 + msg0 + b"\n")
    for fi in range(file_count):
        cls = f"Module{fi:02d}"
        src = _java_class_source(cls, fi, (fi % 8) + 1).encode("utf-8")
        stream.extend(f"M 100644 inline src/main/java/bench/sample/{cls}.java\ndata {len(src)}\n".encode("utf-8"))
        stream.extend(src + b"\n")
    mark += 1

    # Remaining commit_count - 1 commits touch 1 or 2 files each cleanly
    for ci in range(1, commit_count):
        ts = int((base_dt + timedelta(seconds=ci * step_seconds)).timestamp())
        aname, aemail = authors[ci % len(authors)]
        msg = f"bench update #{ci}".encode("utf-8")
        hdr = (
            f"commit refs/heads/main\nmark :{mark}\n"
            f"author {aname} <{aemail}> {ts} +0000\n"
            f"committer {aname} <{aemail}> {ts} +0000\n"
            f"data {len(msg)}\n"
        ).encode("utf-8")
        stream.extend(hdr + msg + f"\nfrom :{mark - 1}\n".encode("utf-8"))

        touched = [ci % file_count]
        if ci % 5 == 0:
            touched.append((ci + 1) % file_count)
        for fi in touched:
            cls = f"Module{fi:02d}"
            ccn = ((fi + ci) % 10) + 1
            src = _java_class_source(cls, ci, ccn).encode("utf-8")
            stream.extend(f"M 100644 inline src/main/java/bench/sample/{cls}.java\ndata {len(src)}\n".encode("utf-8"))
            stream.extend(src + b"\n")
        mark += 1

    marks_file = repo_dir / "bench-marks.txt"
    subprocess.run(
        ["git", "-C", str(repo_dir), "fast-import", f"--export-marks={marks_file}"],
        input=bytes(stream),
        check=True,
        capture_output=True,
    )
    last_sha = None
    for line in marks_file.read_text(encoding="utf-8").splitlines():
        if line.startswith(f":{commit_count} "):
            last_sha = line.split()[1]
    marks_file.unlink()
    if not last_sha:
        raise RuntimeError("Failed to find final commit SHA from fast-import")
    gen_ms = (time.perf_counter() - t_gen_start) * 1000.0
    return {
        "repoGenMs": round(gen_ms, 2),
        "headSha": last_sha,
        "windowStart": "2026-01-02T00:00:00Z",
        "windowEnd": "2026-07-01T00:00:00Z",
    }


def _hardware_info(java_cmd: str) -> dict:
    java_ver = subprocess.run([java_cmd, "-version"], capture_output=True, text=True).stderr.strip()
    git_ver = subprocess.check_output(["git", "--version"], text=True).strip()
    lizard_ver = subprocess.check_output(["lizard", "--version"], text=True).strip()
    cpu_name = platform.processor()
    ram_gb = None
    try:
        out = subprocess.check_output(
            ["powershell", "-NoProfile", "-Command", "(Get-CimInstance Win32_Processor).Name; (Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory"],
            text=True,
            timeout=10,
        ).strip().splitlines()
        if len(out) >= 2:
            cpu_name = out[0].strip()
            ram_gb = round(int(out[1].strip()) / (1024 ** 3), 2)
    except Exception:
        pass
    return {
        "os": platform.platform(),
        "cpu": cpu_name,
        "logicalCores": os.cpu_count(),
        "ramGiB": ram_gb,
        "javaVersion": java_ver,
        "gitVersion": git_ver,
        "lizardVersion": lizard_ver,
        "jgitVersion": "7.8.0.202609011348-r",
    }


def compile_driver(driver_dir: Path, javac_cmd: str, classpath: str) -> str:
    driver_dir.mkdir(parents=True, exist_ok=True)
    src = driver_dir / "BenchmarkStageRunner.java"
    src.write_text(BENCHMARK_JAVA_DRIVER.strip() + "\n", encoding="utf-8")
    subprocess.run([javac_cmd, "-cp", classpath, str(src)], check=True, capture_output=True, text=True)
    return f"{driver_dir}{os.pathsep}{classpath}"


def run_benchmark(out_json: Path, java_cmd: str, javac_cmd: str, classpath: str, iterations: int) -> dict:
    scales = [
        {"id": "scale_1k", "commits": 1000, "files": 25},
        {"id": "scale_5k", "commits": 5000, "files": 40},
        {"id": "scale_10k", "commits": 10000, "files": 50},
    ]
    env_info = _hardware_info(java_cmd)
    results = []

    with tempfile.TemporaryDirectory() as tmp:
        tmp_path = Path(tmp)
        bench_cp = compile_driver(tmp_path / "driver", javac_cmd, classpath)

        for spec in scales:
            repo_dir = tmp_path / f"{spec['id']}.git"
            meta = build_synthetic_repo(repo_dir, spec["commits"], spec["files"])

            proc = subprocess.run(
                [
                    java_cmd,
                    "-Xmx1g",
                    "-cp",
                    bench_cp,
                    "BenchmarkStageRunner",
                    str(repo_dir),
                    meta["headSha"],
                    meta["windowStart"],
                    meta["windowEnd"],
                    str(iterations),
                ],
                check=True,
                capture_output=True,
                text=True,
                timeout=600,
            )
            stage_data = json.loads(proc.stdout)

            # Also time 1 end-to-end HistoricalSnapshotExport process call for realistic CLI overhead
            cli_out = tmp_path / f"{spec['id']}-cli.json"
            t_cli0 = time.perf_counter()
            subprocess.run(
                [
                    java_cmd,
                    "-Xmx1g",
                    "-cp",
                    classpath,
                    "com.example.demo.gitanalyzer.HistoricalSnapshotExport",
                    str(repo_dir),
                    meta["headSha"],
                    meta["windowStart"],
                    meta["windowEnd"],
                    str(cli_out),
                ],
                check=True,
                capture_output=True,
                text=True,
                timeout=600,
            )
            cli_total_ms = round((time.perf_counter() - t_cli0) * 1000.0, 2)

            runs = stage_data["runs"]
            warm_runs = [r for r in runs if r["iteration"] > 1] or runs
            summary_stats = {
                "coldTotalMs": round(runs[0]["totalMs"], 2),
                "meanWarmTotalMs": round(statistics.mean(r["totalMs"] for r in warm_runs), 2),
                "meanAllTotalMs": round(statistics.mean(r["totalMs"] for r in runs), 2),
                "maxTotalMs": round(max(r["totalMs"] for r in runs), 2),
                "meanJgitMs": round(statistics.mean(r["jgitMs"] for r in runs), 2),
                "meanLizardMs": round(statistics.mean(r["lizardMs"] for r in runs), 2),
                "meanMetricMs": round(statistics.mean(r["metricMs"] for r in runs), 2),
                "meanSerializeMs": round(statistics.mean(r["serializeMs"] for r in runs), 2),
                "endToEndCliWallMs": cli_total_ms,
                "passesNfr03Within600s": max(max(r["totalMs"] for r in runs), cli_total_ms) <= 600_000.0,
            }
            results.append(
                {
                    "id": spec["id"],
                    "targetCommits": spec["commits"],
                    "targetFiles": spec["files"],
                    "repoGenMs": meta["repoGenMs"],
                    "reachableCommits": stage_data["reachableCommits"],
                    "selectedCommits": stage_data["selectedCommits"],
                    "javaFiles": stage_data["javaFiles"],
                    "factsCount": stage_data["factsCount"],
                    "runs": runs,
                    "summary": summary_stats,
                }
            )
            print(
                f"{spec['id']} ({stage_data['selectedCommits']} commits, {stage_data['javaFiles']} files, {stage_data['factsCount']} facts): "
                f"mean={summary_stats['meanAllTotalMs']} ms (JGit={summary_stats['meanJgitMs']} ms, Lizard={summary_stats['meanLizardMs']} ms, Metric={summary_stats['meanMetricMs']} ms), "
                f"CLI={cli_total_ms} ms, NFR03_PASS={summary_stats['passesNfr03Within600s']}"
            )

    report = {
        "schemaVersion": 1,
        "kind": "NFR03_CORE_PIPELINE_SCALE_BENCHMARK",
        "measuredAtUtc": datetime.now(timezone.utc).isoformat(),
        "nfr03TargetMaxMs": 600_000,
        "iterationsPerScale": iterations,
        "scopeNote": "Measures offline core analysis pipeline (GitIngestion + LizardComplexityAnalyzer + RepositoryMetricCalculator) on local bare repositories without remote network GitHub clone latency or PostgreSQL JDBC persistence.",
        "environment": env_info,
        "scales": results,
    }
    out_json.parent.mkdir(parents=True, exist_ok=True)
    out_json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return report


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--out-json", type=Path, required=True)
    parser.add_argument("--java", default="java")
    parser.add_argument("--javac", default="javac")
    parser.add_argument("--classpath", required=True)
    parser.add_argument("--iterations", type=int, default=3)
    args = parser.parse_args()
    run_benchmark(args.out_json, args.java, args.javac, args.classpath, args.iterations)


if __name__ == "__main__":
    main()
