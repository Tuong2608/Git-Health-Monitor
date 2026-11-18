"""RQ2 population & coverage blocker decomposition auditor (STT 31)."""
import argparse
import json
from pathlib import Path

PILOT_SNAPSHOT_NAMES = (
    "gs-rest-service-0.json",
    "gs-rest-service-1.json",
    "gs-rest-service-2.json",
    "spring-petclinic-0.json",
    "spring-petclinic-1.json",
    "spring-petclinic-2.json",
    "commons-cli-0.json",
    "commons-cli-1.json",
    "commons-cli-2.json",
)

SCENARIO_SNAPSHOT_NAMES = (
    "s1-before.json",
    "s1-after.json",
    "s1-before-90d.json",
    "s2-before.json",
    "s2-after.json",
    "s3-before.json",
    "s3-after.json",
    "s4-t0.json",
    "s4-t1.json",
    "s4-t2.json",
    "s5-before.json",
    "s5-after.json",
)


def audit_snapshot_payload(snapshot: dict, label: str = "") -> dict:
    """Decompose a snapshot's Java files into hotspotPopulation, LINEAGE_AMBIGUOUS, NO_FUNCTIONS, and other."""
    if not isinstance(snapshot, dict) or "summary" not in snapshot or "files" not in snapshot:
        raise ValueError(f"Invalid snapshot structure for {label}")

    summary = snapshot["summary"]
    files = snapshot["files"]
    if not isinstance(files, list):
        raise ValueError(f"files must be a list for {label}")

    hotspot_files = []
    ambiguous_with_ccn = []
    ambiguous_without_ccn = []
    unambiguous_no_functions = []
    other_unmeasured = []

    for row in files:
        path = row.get("path")
        reason = row.get("unavailableReason")
        freq = row.get("frequency")
        max_ccn = row.get("maxCcn")
        hotspot = row.get("hotspot")

        if freq is not None and max_ccn is not None:
            if hotspot is None or reason is not None:
                raise ValueError(f"Measured file {path} in {label} must have hotspot and null unavailableReason")
            hotspot_files.append(path)
        elif reason == "LINEAGE_AMBIGUOUS":
            if freq is not None or hotspot is not None:
                raise ValueError(f"LINEAGE_AMBIGUOUS file {path} in {label} must keep frequency and hotspot null")
            if max_ccn is None:
                ambiguous_without_ccn.append(path)
            else:
                ambiguous_with_ccn.append(path)
        elif reason == "NO_FUNCTIONS":
            if freq is None or max_ccn is not None or hotspot is not None:
                raise ValueError(f"NO_FUNCTIONS file {path} in {label} must have frequency and null maxCcn/hotspot")
            unambiguous_no_functions.append(path)
        else:
            if hotspot is not None:
                raise ValueError(f"Unmeasured file {path} in {label} must not have hotspot")
            other_unmeasured.append({"path": path, "reason": reason})

    total = len(files)
    total_ambiguous = len(ambiguous_with_ccn) + len(ambiguous_without_ccn)
    if total != summary.get("currentJavaFiles"):
        raise ValueError(f"currentJavaFiles mismatch in {label}: {total} != {summary.get('currentJavaFiles')}")
    if len(hotspot_files) != summary.get("hotspotPopulation"):
        raise ValueError(
            f"hotspotPopulation mismatch in {label}: {len(hotspot_files)} != {summary.get('hotspotPopulation')}"
        )
    if total_ambiguous != summary.get("ambiguousFiles"):
        raise ValueError(
            f"ambiguousFiles mismatch in {label}: {total_ambiguous} != {summary.get('ambiguousFiles')}"
        )
    if len(hotspot_files) + total_ambiguous + len(unambiguous_no_functions) + len(other_unmeasured) != total:
        raise ValueError(f"Partition sum mismatch in {label}")

    all_no_func_count = len(unambiguous_no_functions) + len(ambiguous_without_ccn)
    if all_no_func_count != summary.get("noFunctionFiles"):
        raise ValueError(
            f"noFunctionFiles mismatch in {label}: {all_no_func_count} != {summary.get('noFunctionFiles')}"
        )

    coverage_complete = bool(summary.get("coverageComplete"))
    if total_ambiguous > 0 and coverage_complete:
        raise ValueError(f"coverageComplete must be False when ambiguousFiles > 0 in {label}")
    if total_ambiguous == 0 and len(other_unmeasured) == 0 and not coverage_complete:
        raise ValueError(f"Unexpected coverageComplete=False without lineage or measurement blockers in {label}")

    return {
        "label": label,
        "headSha": snapshot.get("headSha"),
        "windowStart": snapshot.get("windowStart"),
        "windowEnd": snapshot.get("windowEnd"),
        "selectedCommits": summary.get("selectedCommits"),
        "currentJavaFiles": total,
        "hotspotPopulation": len(hotspot_files),
        "lineageAmbiguousFiles": total_ambiguous,
        "lineageAmbiguousWithCcn": len(ambiguous_with_ccn),
        "lineageAmbiguousAndNoFunctions": len(ambiguous_without_ccn),
        "unambiguousNoFunctionFiles": len(unambiguous_no_functions),
        "totalNoFunctionFiles": all_no_func_count,
        "otherUnmeasuredFiles": len(other_unmeasured),
        "coverageComplete": coverage_complete,
        "hotspotPopulationRatioPct": round(100.0 * len(hotspot_files) / total, 2) if total > 0 else None,
        "ifMergeLineageResolvedHotspotPopulation": len(hotspot_files) + len(ambiguous_with_ccn),
        "sampleNoFunctionPaths": unambiguous_no_functions[:5],
        "sampleAmbiguousPaths": (ambiguous_with_ccn + ambiguous_without_ccn)[:5],
    }


def run_coverage_audit(pilot_dir: Path, scenarios_dir: Path) -> dict:
    """Audit all OSS pilot snapshots and controlled scenario snapshots."""
    pilot_records = []
    for name in PILOT_SNAPSHOT_NAMES:
        path = pilot_dir / name
        snap = json.loads(path.read_text(encoding="utf-8"))
        pilot_records.append(audit_snapshot_payload(snap, name))

    scenario_records = []
    for name in SCENARIO_SNAPSHOT_NAMES:
        path = scenarios_dir / name
        snap = json.loads(path.read_text(encoding="utf-8"))
        scenario_records.append(audit_snapshot_payload(snap, name))

    oss_totals = {
        "snapshotsAudited": len(pilot_records),
        "totalJavaFileInstances": sum(r["currentJavaFiles"] for r in pilot_records),
        "totalHotspotPopulationInstances": sum(r["hotspotPopulation"] for r in pilot_records),
        "totalLineageAmbiguousInstances": sum(r["lineageAmbiguousFiles"] for r in pilot_records),
        "totalLineageAmbiguousWithCcnInstances": sum(r["lineageAmbiguousWithCcn"] for r in pilot_records),
        "totalLineageAmbiguousAndNoFunctionsInstances": sum(
            r["lineageAmbiguousAndNoFunctions"] for r in pilot_records
        ),
        "totalUnambiguousNoFunctionInstances": sum(r["unambiguousNoFunctionFiles"] for r in pilot_records),
        "totalNoFunctionInstances": sum(r["totalNoFunctionFiles"] for r in pilot_records),
        "totalOtherUnmeasuredInstances": sum(r["otherUnmeasuredFiles"] for r in pilot_records),
        "coverageCompleteSnapshots": sum(1 for r in pilot_records if r["coverageComplete"]),
    }

    scenario_totals = {
        "snapshotsAudited": len(scenario_records),
        "coverageCompleteSnapshots": sum(1 for r in scenario_records if r["coverageComplete"]),
        "blockedByLineageSnapshots": sum(1 for r in scenario_records if r["lineageAmbiguousFiles"] > 0),
    }

    return {
        "schemaVersion": 1,
        "kind": "RQ2_POPULATION_AND_COVERAGE_DECOMPOSITION_AUDIT",
        "ossSummary": oss_totals,
        "scenarioSummary": scenario_totals,
        "ossSnapshots": pilot_records,
        "scenarioSnapshots": scenario_records,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pilot-dir", type=Path, default=Path("docs/week-09/evidence/pilot"))
    parser.add_argument("--scenarios-dir", type=Path, default=Path("docs/week-10/evidence/rq2-scenarios"))
    parser.add_argument("--out-json", type=Path, required=True)
    args = parser.parse_args()

    report = run_coverage_audit(args.pilot_dir, args.scenarios_dir)
    args.out_json.parent.mkdir(parents=True, exist_ok=True)
    args.out_json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(
        f"Audited {report['ossSummary']['snapshotsAudited']} OSS snapshots and "
        f"{report['scenarioSummary']['snapshotsAudited']} scenario snapshots."
    )


if __name__ == "__main__":
    main()
