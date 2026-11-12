"""Offline RQ2 descriptive evaluator. No bug labels, model calls or causal claims."""
import argparse
import json
import math
from datetime import datetime
from pathlib import Path
from decimal import Decimal


def instant(value):
    result = datetime.fromisoformat(value.replace("Z", "+00:00"))
    if result.tzinfo is None:
        raise ValueError("Timestamp must include timezone")
    return result


def validate(snapshot):
    start, end = instant(snapshot["windowStart"]), instant(snapshot["windowEnd"])
    if start >= end:
        raise ValueError("Empty/reversed observation window")
    files = {}
    for row in snapshot["files"]:
        key = row["lineageKey"]
        if key in files:
            raise ValueError("Duplicate lineage")
        for metric in ("frequency", "maxCcn", "hotspot"):
            value = row[metric]
            if value is not None and (isinstance(value, bool) or not isinstance(value, (float, int))
                                      or not math.isfinite(value) or value < 0):
                raise ValueError("Invalid metric")
        if row["hotspot"] is not None and row["hotspot"] > 1:
            raise ValueError("Hotspot outside [0,1]")
        files[key] = row
    for fact in snapshot.get("facts", []):
        if not start <= instant(fact["committerTime"]) < end:
            raise ValueError("Fact outside observation window")
    return files, end - start


def compare(before, after, threshold=0.15):
    if not before.get("repositoryUrl") or before["repositoryUrl"] != after.get("repositoryUrl"):
        raise ValueError("Snapshots must belong to the same repository")
    if not math.isfinite(threshold) or not 0 <= threshold <= 1:
        raise ValueError("Invalid threshold")
    old, old_duration = validate(before)
    new, new_duration = validate(after)
    if instant(before["windowEnd"]) >= instant(after["windowEnd"]):
        raise ValueError("Snapshots must be chronological")
    blockers = []
    if json.loads(before["config"]) != json.loads(after["config"]):
        blockers.append("CONFIG_CHANGED")
    if before["metricVersion"] != after["metricVersion"]:
        blockers.append("METRIC_VERSION_CHANGED")
    if old_duration != new_duration:
        blockers.append("WINDOW_DURATION_CHANGED")
    if any(s["summary"].get("coverageComplete") is not True or s["truncated"] for s in (before, after)):
        blockers.append("INCOMPLETE_COVERAGE")
    changes, unavailable = [], 0
    for key in sorted(old.keys() & new.keys()):
        a, b = old[key], new[key]
        if a["hotspot"] is None or b["hotspot"] is None:
            unavailable += 1
            continue
        exact_delta = Decimal(str(b["hotspot"])) - Decimal(str(a["hotspot"]))
        delta = float(exact_delta)
        # Absolute delta matches HOTSPOT_INCREASE. Never divide by zero or treat null as zero.
        changes.append({"lineageKey": key, "beforePath": a["path"], "afterPath": b["path"],
                        "before": a, "after": b, "hotspotDelta": delta,
                        "relativeDelta": delta / a["hotspot"] if a["hotspot"] else None,
                        "relativeStatus": "AVAILABLE" if a["hotspot"] else "BASE_ZERO",
                        "candidateIncrease": exact_delta > Decimal(str(threshold)),
                        "evidenceCommits": sorted({f["commitSha"] for f in after.get("facts", [])
                                                   if f["lineageKey"] == key})})
    return {"beforeSha": before["headSha"], "afterSha": after["headSha"],
            "status": "BLOCKED" if blockers else "COMPARABLE", "blockers": blockers,
            "threshold": threshold, "added": sorted(new.keys() - old.keys()),
            "removed": sorted(old.keys() - new.keys()), "unavailableCommonFiles": unavailable,
            "comparableCommonFiles": len(changes),
            "alertCandidates": sum(c["candidateIncrease"] for c in changes) if not blockers else None,
            "diagnosticChangesOnly": bool(blockers), "changes": changes}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("before", type=Path)
    parser.add_argument("after", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    result = compare(json.loads(args.before.read_text(encoding="utf-8")),
                     json.loads(args.after.read_text(encoding="utf-8")))
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2, allow_nan=False) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
