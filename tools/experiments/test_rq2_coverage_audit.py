"""Unit tests for RQ2 population & coverage decomposition auditor."""
import unittest
from pathlib import Path
from audit_rq2_coverage import audit_snapshot_payload, run_coverage_audit


class Rq2CoverageAuditTests(unittest.TestCase):
    def _sample_snapshot(self) -> dict:
        return {
            "headSha": "a" * 40,
            "windowStart": "2025-01-01T00:00:00Z",
            "windowEnd": "2025-06-30T00:00:00Z",
            "summary": {
                "coverageComplete": True,
                "reachableCommits": 20,
                "selectedCommits": 10,
                "currentJavaFiles": 3,
                "hotspotPopulation": 2,
                "ambiguousFiles": 0,
                "noFunctionFiles": 1,
            },
            "files": [
                {
                    "path": "src/ServiceA.java",
                    "frequency": 4,
                    "maxCcn": 5,
                    "hotspot": 0.9,
                    "unavailableReason": None,
                },
                {
                    "path": "src/ServiceB.java",
                    "frequency": 1,
                    "maxCcn": 1,
                    "hotspot": 0.25,
                    "unavailableReason": None,
                },
                {
                    "path": "src/MarkerInterface.java",
                    "frequency": 1,
                    "maxCcn": None,
                    "hotspot": None,
                    "unavailableReason": "NO_FUNCTIONS",
                },
            ],
        }

    def test_no_functions_file_reduces_hotspot_population_while_preserving_coverage_complete(self) -> None:
        res = audit_snapshot_payload(self._sample_snapshot(), "sample")
        self.assertEqual(res["currentJavaFiles"], 3)
        self.assertEqual(res["hotspotPopulation"], 2)
        self.assertEqual(res["unambiguousNoFunctionFiles"], 1)
        self.assertEqual(res["lineageAmbiguousFiles"], 0)
        self.assertTrue(res["coverageComplete"])

    def test_lineage_ambiguous_file_requires_coverage_incomplete_and_tracks_overlap_with_no_functions(self) -> None:
        snap = self._sample_snapshot()
        snap["files"].append(
            {
                "path": "src/MergedRecord.java",
                "frequency": None,
                "maxCcn": None,
                "hotspot": None,
                "unavailableReason": "LINEAGE_AMBIGUOUS",
            }
        )
        snap["summary"]["currentJavaFiles"] = 4
        snap["summary"]["ambiguousFiles"] = 1
        snap["summary"]["noFunctionFiles"] = 2
        with self.assertRaises(ValueError):
            audit_snapshot_payload(snap, "bad-coverage")
        snap["summary"]["coverageComplete"] = False
        res = audit_snapshot_payload(snap, "valid-incomplete")
        self.assertFalse(res["coverageComplete"])
        self.assertEqual(res["lineageAmbiguousFiles"], 1)
        self.assertEqual(res["lineageAmbiguousAndNoFunctions"], 1)
        self.assertEqual(res["totalNoFunctionFiles"], 2)

    def test_rejects_summary_count_mismatch(self) -> None:
        snap = self._sample_snapshot()
        snap["summary"]["hotspotPopulation"] = 3
        with self.assertRaises(ValueError):
            audit_snapshot_payload(snap, "mismatch")

    def test_rejects_ambiguous_file_with_non_null_hotspot(self) -> None:
        snap = self._sample_snapshot()
        snap["files"][2] = {
            "path": "src/BadAmbiguous.java",
            "frequency": None,
            "maxCcn": 2,
            "hotspot": 0.5,
            "unavailableReason": "LINEAGE_AMBIGUOUS",
        }
        snap["summary"]["noFunctionFiles"] = 0
        snap["summary"]["ambiguousFiles"] = 1
        snap["summary"]["coverageComplete"] = False
        with self.assertRaises(ValueError):
            audit_snapshot_payload(snap, "bad-ambig-hotspot")

    def test_rejects_no_functions_file_with_non_null_max_ccn(self) -> None:
        snap = self._sample_snapshot()
        snap["files"][2]["maxCcn"] = 2
        with self.assertRaises(ValueError):
            audit_snapshot_payload(snap, "bad-no-func")

    def test_committed_pilot_and_scenario_snapshots_pass_full_audit(self) -> None:
        repo_root = Path(__file__).resolve().parents[2]
        pilot_dir = repo_root / "docs" / "week-09" / "evidence" / "pilot"
        scenarios_dir = repo_root / "docs" / "week-10" / "evidence" / "rq2-scenarios"
        report = run_coverage_audit(pilot_dir, scenarios_dir)
        self.assertEqual(report["ossSummary"]["snapshotsAudited"], 9)
        self.assertEqual(report["ossSummary"]["coverageCompleteSnapshots"], 0)
        self.assertEqual(report["ossSummary"]["totalUnambiguousNoFunctionInstances"], 37)
        self.assertEqual(report["ossSummary"]["totalLineageAmbiguousAndNoFunctionsInstances"], 17)
        self.assertEqual(report["ossSummary"]["totalNoFunctionInstances"], 54)
        self.assertEqual(report["scenarioSummary"]["snapshotsAudited"], 12)
        self.assertEqual(report["scenarioSummary"]["blockedByLineageSnapshots"], 1)


if __name__ == "__main__":
    unittest.main()
