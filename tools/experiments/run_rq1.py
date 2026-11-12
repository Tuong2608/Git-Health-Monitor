"""Freeze RQ1 temporal-split inputs, run JGit/Lizard snapshots, verify human-reviewed commit labels, and evaluate rankings.

No repository code is executed and no LLM is called. Requires git, Java 21, Maven-built runtime classpath,
and Python with lizard==1.24.3 on PATH. Output directories must be new.
"""
import argparse
import hashlib
import json
import math
import random
import re
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path
from evaluate_rankings import evaluate, percentiles, timestamp


def command(args, timeout=600):
    return subprocess.check_output(args, text=True, encoding="utf-8", errors="replace",
                                   stderr=subprocess.STDOUT, timeout=timeout).strip()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2, allow_nan=False) + "\n", encoding="utf-8")


def git(cache, *args):
    return command(["git", "-C", str(cache), *args])


def choose_temporal_split(non_merge_commits, first_parent_commits, ancestor_max_time_fn, target_ratio=0.8):
    """Compute grouped-timestamp 80/20 split on non-merge commits and select ancestor-safe observationSha."""
    if len(non_merge_commits) < 5:
        raise ValueError("Insufficient non-merge commits for 80/20 temporal split")
    ordered = sorted(((sha, int(ts)) for sha, ts in non_merge_commits), key=lambda item: (item[1], item[0]))
    target_index = max(1, min(len(ordered) - 1, int(round(len(ordered) * target_ratio))))
    cutoff_ts = ordered[target_index][1]
    # Keep identical timestamps on the same side of the cutoff boundary.
    while target_index < len(ordered) and ordered[target_index - 1][1] == cutoff_ts:
        target_index += 1
        if target_index < len(ordered):
            cutoff_ts = ordered[target_index][1]
    obs_nm = sum(1 for _, ts in ordered if ts < cutoff_ts)
    pred_nm = len(ordered) - obs_nm
    if obs_nm == 0 or pred_nm == 0:
        raise ValueError("Degenerate temporal split produced an empty observation or prediction partition")
    obs_sha = None
    for sha, ts in first_parent_commits:
        if int(ts) < cutoff_ts and ancestor_max_time_fn(sha) < cutoff_ts:
            obs_sha = sha
            break
    return {
        "cutoffEpochSeconds": cutoff_ts,
        "totalNonMergeCommits": len(ordered),
        "observationNonMergeCommits": obs_nm,
        "predictionNonMergeCommits": pred_nm,
        "realizedCommitRatio": round(obs_nm / len(ordered), 6),
        "observationSha": obs_sha,
    }


def spearman_rho(xs, ys):
    """Exact Spearman rank correlation using midrank percentiles."""
    if len(xs) != len(ys) or len(xs) < 2:
        return None
    rx = percentiles(xs)
    ry = percentiles(ys)
    mx = sum(rx) / len(rx)
    my = sum(ry) / len(ry)
    num = sum((a - mx) * (b - my) for a, b in zip(rx, ry))
    den_x = sum((a - mx) ** 2 for a in rx)
    den_y = sum((b - my) ** 2 for b in ry)
    if den_x <= 0.0 or den_y <= 0.0:
        return None
    return num / math.sqrt(den_x * den_y)


def freeze(proposal_path, cache_root, output):
    if output.exists():
        raise ValueError("Refusing to replace frozen RQ1 manifest")
    proposal = json.loads(proposal_path.read_text(encoding="utf-8"))
    ratio = float(proposal.get("targetObservationRatio", 0.8))
    obs_days = int(proposal.get("observationDays", 180))
    if not (0.5 <= ratio <= 0.95) or obs_days < 1:
        raise ValueError("Invalid split parameters in RQ1 proposal")
    cache_root.mkdir(parents=True, exist_ok=True)
    manifest = {
        "schemaVersion": 1,
        "frozenAt": datetime.now(timezone.utc).isoformat(),
        "proposalSha256": hashlib.sha256(proposal_path.read_bytes()).hexdigest(),
        "proposal": proposal,
        "repositories": [],
    }
    ids = set()
    for item in proposal["repositories"]:
        name, url = item["id"], item["url"]
        if not re.fullmatch(r"[a-zA-Z0-9_-]+", name) or name in ids:
            raise ValueError("Unsafe or duplicate repository ID")
        ids.add(name)
        if not re.fullmatch(r"https://github.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", url):
            raise ValueError("Expected public GitHub URL")
        cache = cache_root / (name + ".git")
        if not cache.exists():
            command(["git", "clone", "--bare", url, str(cache)])
        elif git(cache, "remote", "get-url", "origin").removesuffix(".git") != url:
            raise ValueError("Cache remote does not match proposal")
        if git(cache, "rev-parse", "--is-shallow-repository") != "false":
            raise ValueError("Full history required")
        head = git(cache, "rev-parse", "HEAD")
        total_commits = int(git(cache, "rev-list", "--count", head))
        if total_commits > 10000:
            manifest["repositories"].append({
                "id": name, "url": url, "endSha": head,
                "reachableCommitsAtEnd": total_commits, "selectionStatus": "RESOURCE_LIMIT_COMMITS",
            })
            continue
        non_merge = [line.split() for line in git(cache, "log", "--no-merges", "--format=%H %ct", head).splitlines()]
        first_parent = [line.split() for line in git(cache, "log", "--first-parent", "--format=%H %ct", head).splitlines()]
        all_times_head = [int(x) for x in git(cache, "log", "--format=%ct", head).splitlines()]

        def anc_max(sha):
            return max(int(x) for x in git(cache, "log", "--format=%ct", sha).splitlines())

        split = choose_temporal_split(non_merge, first_parent, anc_max, ratio)
        cutoff_dt = datetime.fromtimestamp(split["cutoffEpochSeconds"], timezone.utc)
        obs_start_dt = cutoff_dt - timedelta(days=obs_days)
        pred_end_dt = datetime.fromtimestamp(max(all_times_head) + 1, timezone.utc)
        obs_sha = split["observationSha"]
        obs_reachable = int(git(cache, "rev-list", "--count", obs_sha)) if obs_sha else 0
        tree = git(cache, "ls-tree", head).splitlines()
        licenses = [line for line in tree if re.search(r"\t(?:LICENSE|COPYING|NOTICE)", line, re.I)]
        manifest["repositories"].append({
            "id": name,
            "url": url,
            "reason": item.get("reason", ""),
            "endSha": head,
            "reachableCommitsAtEnd": total_commits,
            "observationSha": obs_sha,
            "reachableCommitsAtObservation": obs_reachable,
            "observationStart": obs_start_dt.isoformat().replace("+00:00", "Z"),
            "cutoff": cutoff_dt.isoformat().replace("+00:00", "Z"),
            "predictionEnd": pred_end_dt.isoformat().replace("+00:00", "Z"),
            "totalNonMergeCommits": split["totalNonMergeCommits"],
            "observationNonMergeCommits": split["observationNonMergeCommits"],
            "predictionNonMergeCommits": split["predictionNonMergeCommits"],
            "realizedCommitRatio": split["realizedCommitRatio"],
            "selectionStatus": "READY" if obs_sha else "NO_HISTORICAL_HEAD",
            "licenseFiles": licenses,
        })
    save(output, manifest)
    return manifest


def build_case_from_snapshots(repo_id, obs_snapshot, pred_snapshot, review_ledger, candidate_re, neg_sample_size, seed,
                              commit_subject_fn, main_java_only=False):
    eligible = [
        f for f in obs_snapshot["files"]
        if f["hotspot"] is not None and (not main_java_only or f["path"].startswith("src/main/java/"))
    ]
    eligible.sort(key=lambda f: f["lineageKey"])
    excluded_counts = {}
    for f in obs_snapshot["files"]:
        if main_java_only and not f["path"].startswith("src/main/java/"):
            continue
        if f["hotspot"] is None:
            reason = f.get("unavailableReason") or "UNAVAILABLE"
            excluded_counts[reason] = excluded_counts.get(reason, 0) + 1

    if not eligible:
        return None, {
            "repository": repo_id,
            "status": "EXCLUDED_ZERO_ELIGIBLE_FILES",
            "totalJavaFiles": len(obs_snapshot["files"]),
            "eligibleFiles": 0,
            "excludedByReason": excluded_counts,
        }

    all_eligible_keys = {f["lineageKey"]: f["path"] for f in obs_snapshot["files"] if f["hotspot"] is not None}
    eligible_keys = {f["lineageKey"]: f["path"] for f in eligible}
    all_commits_touching = {}
    commits_touching_eligible = {}
    for fact in pred_snapshot.get("facts", []):
        k = fact["lineageKey"]
        if k in all_eligible_keys:
            all_commits_touching.setdefault(fact["commitSha"], set()).add(k)
        if k in eligible_keys:
            commits_touching_eligible.setdefault(fact["commitSha"], set()).add(k)

    cands, non_cands = [], []
    for sha in sorted(all_commits_touching):
        subj = commit_subject_fn(sha)
        if candidate_re.search(subj):
            cands.append((sha, subj))
        else:
            non_cands.append((sha, subj))

    rng = random.Random(seed)
    neg_sample = rng.sample(non_cands, min(neg_sample_size, len(non_cands)))
    required_reviews = {sha: ("issue-or-keyword-candidate", subj) for sha, subj in cands}
    for sha, subj in neg_sample:
        required_reviews[sha] = ("negative-sample-seed-" + str(seed), subj)

    repo_reviews = review_ledger.get(repo_id, {})
    confirmed_by_key = {}
    naive_by_key = {}
    for sha, _ in cands:
        for k in commits_touching_eligible.get(sha, set()):
            naive_by_key.setdefault(k, []).append(sha)

    for sha, (source_kind, subj) in sorted(required_reviews.items()):
        entry = repo_reviews.get(sha)
        if not entry:
            raise ValueError(f"Missing human review entry for {repo_id} commit {sha} ({source_kind}: {subj})")
        if entry.get("finalDecision") not in ("confirmed", "rejected"):
            raise ValueError(f"Unresolved commit review decision for {repo_id} commit {sha}")
        if not entry.get("reason") or not entry.get("reviewer1") or not entry.get("reviewer2"):
            raise ValueError(f"Incomplete provenance for {repo_id} commit {sha}")
        if entry["finalDecision"] == "confirmed":
            for k in commits_touching_eligible.get(sha, set()):
                confirmed_by_key.setdefault(k, []).append(sha)

    files = []
    bugfix_counts = []
    naive_files = []
    for idx, f in enumerate(eligible, 1):
        k = f["lineageKey"]
        pos_shas = sorted(set(confirmed_by_key.get(k, [])))
        is_pos = len(pos_shas) > 0
        bugfix_counts.append(len(pos_shas))
        files.append({
            "fileId": idx,
            "lineageKey": k,
            "path": f["path"],
            "frequency": f["frequency"],
            "maxCcn": f["maxCcn"],
            "label": "positive" if is_pos else "negative",
            "labelEvidence": (
                f"confirmed-bugfix-commits:{','.join(s[:10] for s in pos_shas)}"
                if is_pos else f"reviewed-negative:0-confirmed-of-{len(required_reviews)}-audited-commits"
            ),
        })
        naive_shas = sorted(set(naive_by_key.get(k, [])))
        naive_pos = len(naive_shas) > 0
        naive_files.append({
            "fileId": idx,
            "lineageKey": k,
            "path": f["path"],
            "frequency": f["frequency"],
            "maxCcn": f["maxCcn"],
            "label": "positive" if naive_pos else "negative",
            "labelEvidence": (
                f"unfiltered-keyword-commits:{','.join(s[:10] for s in naive_shas)}"
                if naive_pos else "unfiltered-keyword-negative"
            ),
        })

    freqs = [r["frequency"] for r in files]
    ccns = [r["maxCcn"] for r in files]
    p_freq = percentiles(freqs)
    p_ccn = percentiles(ccns)
    hotspots = [math.sqrt(a * b) for a, b in zip(p_freq, p_ccn)]
    correlations = {
        "frequency": spearman_rho(freqs, bugfix_counts),
        "complexity": spearman_rho(ccns, bugfix_counts),
        "hotspot": spearman_rho(hotspots, bugfix_counts),
    }

    case_doc = {
        "repository": repo_id,
        "observationStart": obs_snapshot["windowStart"],
        "cutoff": obs_snapshot["windowEnd"],
        "predictionEnd": pred_snapshot["windowEnd"],
        "files": files,
    }
    naive_case_doc = {
        "repository": repo_id,
        "observationStart": obs_snapshot["windowStart"],
        "cutoff": obs_snapshot["windowEnd"],
        "predictionEnd": pred_snapshot["windowEnd"],
        "files": naive_files,
    }
    meta = {
        "repository": repo_id,
        "status": "EVALUATED",
        "totalJavaFiles": len(obs_snapshot["files"]),
        "eligibleFiles": len(files),
        "excludedByReason": excluded_counts,
        "predictionCommitsTouchingEligible": len(commits_touching_eligible),
        "candidateCommitsAudited": len(cands),
        "negativeSampleCommitsAudited": len(neg_sample),
        "confirmedBugFixCommits": sum(
            1 for sha in required_reviews if repo_reviews[sha]["finalDecision"] == "confirmed"
        ),
        "rejectedCandidateCommits": sum(
            1 for sha, _ in cands if repo_reviews[sha]["finalDecision"] == "rejected"
        ),
        "confirmedViaNegativeSample": sum(
            1 for sha, _ in neg_sample if repo_reviews[sha]["finalDecision"] == "confirmed"
        ),
        "positives": sum(1 for r in files if r["label"] == "positive"),
        "naiveKeywordPositives": sum(1 for r in naive_files if r["label"] == "positive"),
        "spearmanRhoWithBugFixCount": correlations,
    }
    return (case_doc, naive_case_doc), meta


def run(manifest_path, cache_root, reviews_path, output, java, classpath):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    reviews_doc = json.loads(reviews_path.read_text(encoding="utf-8"))
    review_ledger = reviews_doc["repositories"]
    proposal = manifest["proposal"]
    candidate_re = re.compile(proposal["candidateRegex"], re.I)
    neg_sample_size = int(proposal.get("negativeSamplePerRepo", 10))
    seed = int(proposal.get("seed", 23110170))
    output.mkdir(parents=True, exist_ok=False)

    primary_cases, prod_cases, naive_cases = [], [], []
    repo_summaries = []

    for repo in manifest["repositories"]:
        repo_id = repo["id"]
        if repo.get("selectionStatus") != "READY":
            repo_summaries.append({"id": repo_id, "status": repo.get("selectionStatus", "NOT_READY")})
            continue
        cache = cache_root / (repo_id + ".git")
        obs_path = output / f"{repo_id}-obs.json"
        pred_path = output / f"{repo_id}-pred.json"
        command([
            java, "-Xmx1g", "-cp", classpath,
            "com.example.demo.gitanalyzer.HistoricalSnapshotExport",
            str(cache), repo["observationSha"], repo["observationStart"], repo["cutoff"], str(obs_path)
        ], timeout=360)
        command([
            java, "-Xmx1g", "-cp", classpath,
            "com.example.demo.gitanalyzer.HistoricalSnapshotExport",
            str(cache), repo["endSha"], repo["cutoff"], repo["predictionEnd"], str(pred_path)
        ], timeout=360)

        obs_snapshot = json.loads(obs_path.read_text(encoding="utf-8"))
        pred_snapshot = json.loads(pred_path.read_text(encoding="utf-8"))

        def subject_fn(sha):
            return git(cache, "log", "-1", "--format=%s", sha)

        pair, meta = build_case_from_snapshots(
            repo_id, obs_snapshot, pred_snapshot, review_ledger,
            candidate_re, neg_sample_size, seed, subject_fn, main_java_only=False
        )
        meta["observationSnapshot"] = obs_path.name
        meta["observationSha256"] = hashlib.sha256(obs_path.read_bytes()).hexdigest()
        meta["predictionSnapshot"] = pred_path.name
        meta["predictionSha256"] = hashlib.sha256(pred_path.read_bytes()).hexdigest()
        meta["observationSha"] = repo["observationSha"]
        meta["endSha"] = repo["endSha"]
        meta["cutoff"] = repo["cutoff"]
        meta["realizedCommitRatio"] = repo["realizedCommitRatio"]

        if pair is not None:
            case_doc, naive_case_doc = pair
            primary_cases.append(case_doc)
            naive_cases.append(naive_case_doc)
            prod_pair, prod_meta = build_case_from_snapshots(
                repo_id, obs_snapshot, pred_snapshot, review_ledger,
                candidate_re, neg_sample_size, seed, subject_fn, main_java_only=True
            )
            if prod_pair is not None:
                prod_cases.append(prod_pair[0])
                meta["productionMainJavaEligible"] = prod_meta["eligibleFiles"]
                meta["productionMainJavaPositives"] = prod_meta["positives"]

        repo_summaries.append(meta)

    dataset_doc = {
        "kind": "reviewed-dataset",
        "protocolVersion": proposal.get("protocolVersion", "week09-rq1-approved-v1"),
        "manifestSha256": hashlib.sha256(manifest_path.read_bytes()).hexdigest(),
        "reviewsSha256": hashlib.sha256(reviews_path.read_bytes()).hexdigest(),
        "cases": primary_cases,
    }
    dataset_path = output / "rq1-reviewed-dataset.json"
    save(dataset_path, dataset_doc)

    eval_primary = evaluate(dataset_doc)
    eval_prod = evaluate({"kind": "reviewed-dataset", "cases": prod_cases}) if prod_cases else None
    eval_naive = evaluate({"kind": "reviewed-dataset", "cases": naive_cases}) if naive_cases else None

    eval_output = {
        "protocolVersion": proposal.get("protocolVersion", "week09-rq1-approved-v1"),
        "primaryEvaluation": eval_primary,
        "sensitivity": {
            "productionMainJavaOnly": eval_prod["macro"] if eval_prod else None,
            "unfilteredKeywordHeuristic": eval_naive["macro"] if eval_naive else None,
        },
    }
    eval_path = output / "rq1-evaluation-output.json"
    save(eval_path, eval_output)

    summary_doc = {
        "schemaVersion": 1,
        "kind": "RQ1_TEMPORAL_SPLIT_RANKING_EVALUATION",
        "manifestSha256": hashlib.sha256(manifest_path.read_bytes()).hexdigest(),
        "reviewsSha256": hashlib.sha256(reviews_path.read_bytes()).hexdigest(),
        "datasetSha256": hashlib.sha256(dataset_path.read_bytes()).hexdigest(),
        "evaluationSha256": hashlib.sha256(eval_path.read_bytes()).hexdigest(),
        "javaVersion": command([java, "-version"]),
        "gitVersion": command(["git", "--version"]),
        "toolSha256": {
            p.name: hashlib.sha256(p.read_bytes()).hexdigest()
            for p in [Path(__file__), Path(__file__).with_name("evaluate_rankings.py")]
        },
        "repositories": repo_summaries,
        "macroPrimary": eval_primary["macro"],
        "sensitivityMacro": eval_output["sensitivity"],
    }
    save(output / "rq1-run-summary.json", summary_doc)
    return summary_doc


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="mode", required=True)

    p_freeze = sub.add_parser("freeze")
    p_freeze.add_argument("proposal", type=Path)
    p_freeze.add_argument("cache", type=Path)
    p_freeze.add_argument("output", type=Path)

    p_run = sub.add_parser("run")
    p_run.add_argument("manifest", type=Path)
    p_run.add_argument("cache", type=Path)
    p_run.add_argument("reviews", type=Path)
    p_run.add_argument("output", type=Path)
    p_run.add_argument("--java", default="java")
    p_run.add_argument("--classpath", required=True)

    args = parser.parse_args()
    if args.mode == "freeze":
        freeze(args.proposal, args.cache, args.output)
    else:
        print(json.dumps(run(args.manifest, args.cache, args.reviews, args.output, args.java, args.classpath), indent=2))


if __name__ == "__main__":
    main()
