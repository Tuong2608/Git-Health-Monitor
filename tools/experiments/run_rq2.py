"""Freeze Git inputs BEFORE measurement, then run the production JGit/Lizard calculator.

No repository code is executed. Requires git, Java 21, Maven-built runtime classpath,
and Python with lizard==1.24.3 on PATH. Output directories must be new.
"""
import argparse
import hashlib
import json
import re
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path
from rq2_trends import compare, instant


def command(args, timeout=600):
    return subprocess.check_output(args, text=True, encoding="utf-8", errors="replace",
                                   stderr=subprocess.STDOUT, timeout=timeout).strip()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2, allow_nan=False) + "\n", encoding="utf-8")


def git(cache, *args):
    return command(["git", "-C", str(cache), *args])


def freeze(proposal_path, cache_root, output):
    if output.exists():
        raise ValueError("Refusing to replace frozen manifest")
    proposal = json.loads(proposal_path.read_text(encoding="utf-8"))
    ends = [instant(c) for c in proposal["cutoffs"]]
    if len(ends) < 2 or any(a >= b for a, b in zip(ends, ends[1:])) or proposal["windowDays"] < 1:
        raise ValueError("Invalid schedule")
    cache_root.mkdir(parents=True, exist_ok=True)
    manifest = {"schemaVersion": 1, "frozenAt": datetime.now(timezone.utc).isoformat(),
                "proposalSha256": hashlib.sha256(proposal_path.read_bytes()).hexdigest(),
                "proposal": proposal, "repositories": []}
    ids = set()
    for item in proposal["repositories"]:
        name, url = item["id"], item["url"]
        if not re.fullmatch(r"[a-zA-Z0-9_-]+", name) or name in ids:
            raise ValueError("Unsafe/duplicate repository ID")
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
        # Traverse the frozen first-parent chain in topology order, not timestamp sort.
        chain = [line.split() for line in git(cache, "log", "--first-parent", "--format=%H %ct", head).splitlines()]
        repo = {"id": name, "url": url, "endSha": head, "snapshots": []}
        for cutoff, end in zip(proposal["cutoffs"], ends):
            sha = next((sha for sha, timestamp in chain if int(timestamp) < end.timestamp()), None)
            row = {"windowEnd": cutoff, "windowStart": (end - timedelta(days=proposal["windowDays"])).isoformat(), "headSha": sha}
            if sha is None:
                row["selectionStatus"] = "NO_HISTORICAL_HEAD"
            else:
                count = int(git(cache, "rev-list", "--count", sha))
                row["reachableCommits"] = count
                if count > 10000:
                    row["selectionStatus"] = "RESOURCE_LIMIT_COMMITS"
                else:
                    times = [int(x) for x in git(cache, "log", "--format=%ct", sha).splitlines()]
                    row["selectionStatus"] = "READY" if max(times) < end.timestamp() else "FUTURE_ANCESTOR"
            repo["snapshots"].append(row)
        # Capture license file names and Git blob IDs; do not infer permission from hosting alone.
        tree = git(cache, "ls-tree", head).splitlines()
        repo["licenseFiles"] = [line for line in tree if re.search(r"\t(?:LICENSE|COPYING|NOTICE)", line, re.I)]
        manifest["repositories"].append(repo)
    output.parent.mkdir(parents=True, exist_ok=True)
    save(output, manifest)


def run(manifest_path, cache_root, output, java, classpath):
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    output.mkdir(parents=True, exist_ok=False)
    report = {"manifestSha256": hashlib.sha256(manifest_path.read_bytes()).hexdigest(),
              "kind": "PILOT_DESCRIPTIVE_NOT_BUG_PREDICTION",
              "javaVersion": command([java, "-version"]), "gitVersion": command(["git", "--version"]),
              "toolSha256": {p.name: hashlib.sha256(p.read_bytes()).hexdigest()
                             for p in [Path(__file__), Path(__file__).with_name("rq2_trends.py")]},
              "repositories": []}
    for repo in manifest["repositories"]:
        cache = cache_root / (repo["id"] + ".git")
        rows, previous = [], None
        for index, spec in enumerate(repo["snapshots"]):
            row = dict(spec)
            row["index"] = index
            if spec["selectionStatus"] != "READY":
                rows.append(row)
                previous = None
                continue
            target = output / f"{repo['id']}-{index}.json"
            try:
                command([java, "-Xmx1g", "-cp", classpath,
                         "com.example.demo.gitanalyzer.HistoricalSnapshotExport", str(cache),
                         spec["headSha"], spec["windowStart"], spec["windowEnd"], str(target)], timeout=360)
                snapshot = json.loads(target.read_text(encoding="utf-8"))
                row.update(status="SUCCESS", output=target.name,
                           sha256=hashlib.sha256(target.read_bytes()).hexdigest(),
                           commitCount=snapshot["commitCount"], javaFiles=len(snapshot["files"]),
                           coverageComplete=snapshot["summary"].get("coverageComplete"))
                if previous is not None:
                    # Identity comparisons are scoped to the same repository and ancestry.
                    git(cache, "merge-base", "--is-ancestor", previous["headSha"], snapshot["headSha"])
                    comparison = compare(previous, snapshot, manifest["proposal"]["hotspotIncreaseThreshold"])
                    comparison_path = output / f"{repo['id']}-{index-1}-to-{index}.json"
                    save(comparison_path, comparison)
                    row["comparison"] = comparison_path.name
                    row["comparisonStatus"] = comparison["status"]
                previous = snapshot
            except (subprocess.SubprocessError, OSError, ValueError) as exc:
                row.update(status="FAILED", errorType=type(exc).__name__, error=str(exc)[-2000:])
                if isinstance(exc, subprocess.CalledProcessError):
                    row["diagnostic"] = (exc.output or "")[-4000:]
                previous = None
            rows.append(row)
        report["repositories"].append({"id": repo["id"], "url": repo["url"], "snapshots": rows})
        save(output / "run-summary.json", report)
    return report


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    sub = parser.add_subparsers(dest="mode", required=True)
    for mode in ("freeze", "run"):
        p = sub.add_parser(mode)
        p.add_argument("input", type=Path)
        p.add_argument("cache", type=Path)
        p.add_argument("output", type=Path)
        if mode == "run":
            p.add_argument("--java", default="java")
            p.add_argument("--classpath", required=True)
    args = parser.parse_args()
    if args.mode == "freeze":
        freeze(args.input, args.cache, args.output)
    else:
        print(json.dumps(run(args.input, args.cache, args.output, args.java, args.classpath), indent=2))


if __name__ == "__main__":
    main()
