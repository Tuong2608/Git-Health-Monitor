"""Unit tests for RQ2 scenario traceability and Week 9 OSS pilot audit (pure Python, CI-safe)."""
import copy
import json
import unittest
from pathlib import Path
from evaluate_rq2_scenarios import (
    java_source_with_ccn,
    summarize_pilot_evidence,
    verify_comparison_traceability,
)
from rq2_trends import compare


class Rq2ScenarioTests(unittest.TestCase):
    def _snap(self, month: int, files=None, facts=None, coverage=True, window_start=None):
        return {
            "repositoryUrl": "https://github.com/fixture/demo",
            "headSha": f"{month:02x}" * 20,
            "windowStart": window_start or f"2025-{month:02d}-01T00:00:00Z",
            "windowEnd": f"2025-{month + 6:02d}-01T00:00:00Z",
            "config": '{"windowDays":180,"metricVersion":"metric-v1"}',
            "metricVersion": "metric-v1",
            "truncated": False,
            "summary": {"coverageComplete": coverage},
            "files": files if files is not None else [],
            "facts": facts if facts is not None else [],
        }

    def test_java_source_generator_emits_expected_branch_count(self):
        src1 = java_source_with_ccn("Alpha", 1)
        src5 = java_source_with_ccn("Beta", 5)
        self.assertIn("public class Alpha", src1)
        self.assertEqual(src1.count("if ("), 0)
        self.assertEqual(src5.count("if ("), 4)

    def test_traceability_accepts_exact_decimal_boundary_and_rename(self):
        before = self._snap(
            1,
            files=[{"lineageKey": "init:Old.java", "path": "Old.java", "frequency": 1, "maxCcn": 1, "hotspot": 0.40, "unavailableReason": None}],
        )
        sha1 = "a" * 40
        after_exact = self._snap(
            2,
            files=[{"lineageKey": "init:Old.java", "path": "New.java", "frequency": 2, "maxCcn": 2, "hotspot": 0.55, "unavailableReason": None}],
            facts=[{"commitSha": sha1, "committerTime": "2025-07-10T00:00:00Z", "lineageKey": "init:Old.java"}],
        )
        comp_exact = compare(before, after_exact, 0.15)
        res = verify_comparison_traceability(before, after_exact, comp_exact)
        self.assertEqual(res["status"], "COMPARABLE")
        self.assertEqual(comp_exact["alertCandidates"], 0)
        self.assertFalse(comp_exact["changes"][0]["candidateIncrease"])
        self.assertEqual(comp_exact["changes"][0]["afterPath"], "New.java")

        # 0.56 - 0.40 = 0.16 > 0.15 triggers alert candidate
        after_over = copy.deepcopy(after_exact)
        after_over["files"][0]["hotspot"] = 0.56
        comp_over = compare(before, after_over, 0.15)
        verify_comparison_traceability(before, after_over, comp_over)
        self.assertEqual(comp_over["alertCandidates"], 1)

    def test_traceability_rejects_fabricated_or_missing_evidence_commits(self):
        before = self._snap(
            1,
            files=[{"lineageKey": "k1", "path": "A.java", "frequency": 1, "maxCcn": 2, "hotspot": 0.2, "unavailableReason": None}],
        )
        sha1 = "b" * 40
        after = self._snap(
            2,
            files=[{"lineageKey": "k1", "path": "A.java", "frequency": 2, "maxCcn": 3, "hotspot": 0.7, "unavailableReason": None}],
            facts=[{"commitSha": sha1, "committerTime": "2025-07-15T00:00:00Z", "lineageKey": "k1"}],
        )
        comp = compare(before, after, 0.15)
        verify_comparison_traceability(before, after, comp)

        tampered = copy.deepcopy(comp)
        tampered["changes"][0]["evidenceCommits"] = []
        with self.assertRaises(ValueError):
            verify_comparison_traceability(before, after, tampered)

        tampered2 = copy.deepcopy(comp)
        tampered2["changes"][0]["evidenceCommits"].append("c" * 40)
        with self.assertRaises(ValueError):
            verify_comparison_traceability(before, after, tampered2)

    def test_blocked_comparison_rejects_non_null_alert_candidates(self):
        before = self._snap(
            1,
            files=[{"lineageKey": "k1", "path": "A.java", "frequency": 1, "maxCcn": 2, "hotspot": 0.1, "unavailableReason": None}],
            coverage=False,
        )
        after = self._snap(
            2,
            files=[{"lineageKey": "k1", "path": "A.java", "frequency": 5, "maxCcn": 8, "hotspot": 0.9, "unavailableReason": None}],
            facts=[{"commitSha": "d" * 40, "committerTime": "2025-07-15T00:00:00Z", "lineageKey": "k1"}],
            coverage=False,
        )
        comp = compare(before, after, 0.15)
        self.assertEqual(comp["status"], "BLOCKED")
        self.assertIsNone(comp["alertCandidates"])
        verify_comparison_traceability(before, after, comp)

        bad_comp = copy.deepcopy(comp)
        bad_comp["alertCandidates"] = 1
        with self.assertRaises(ValueError):
            verify_comparison_traceability(before, after, bad_comp)

    def test_week9_pilot_evidence_audits_cleanly_when_present(self):
        pilot_dir = Path(__file__).resolve().parents[2] / "docs" / "week-09" / "evidence" / "pilot"
        audit = summarize_pilot_evidence(pilot_dir)
        self.assertEqual(audit["totalSnapshots"], 9)
        self.assertEqual(audit["succeededSnapshots"], 9)
        self.assertEqual(audit["totalComparisons"], 6)
        self.assertEqual(audit["blockedPairs"], 6)


if __name__ == "__main__":
    unittest.main()
