import re
import tempfile
import unittest
from pathlib import Path
from run_rq1 import build_case_from_snapshots, choose_temporal_split, save, spearman_rho
from summarize_rq1 import summarize


class Rq1RunnerTests(unittest.TestCase):
    def test_choose_temporal_split_groups_equal_timestamps_and_checks_ancestors(self):
        non_merge = [
            ("z1", "100"),
            ("y2", "200"),
            ("x3", "300"),
            ("b4", "400"),
            ("a5", "400"),
        ]
        first_parent = [("a5", "400"), ("x3", "300"), ("y2", "200"), ("z1", "100")]
        # Suppose x3 has a future-dated ancestor (450 >= 400 so x3 must be skipped)
        anc_map = {"a5": 400, "x3": 450, "y2": 200, "z1": 100}
        split = choose_temporal_split(non_merge, first_parent, lambda sha: anc_map[sha], 0.8)
        self.assertEqual(split["cutoffEpochSeconds"], 400)
        self.assertEqual(split["observationNonMergeCommits"], 3)
        self.assertEqual(split["predictionNonMergeCommits"], 2)
        self.assertEqual(split["observationSha"], "y2")

    def test_spearman_rho_handles_ties_and_constant_vectors(self):
        self.assertAlmostEqual(spearman_rho([1, 2, 3, 4], [10, 20, 30, 40]), 1.0)
        self.assertAlmostEqual(spearman_rho([1, 2, 3, 4], [40, 30, 20, 10]), -1.0)
        self.assertIsNone(spearman_rho([2, 2, 2], [1, 2, 3]))
        self.assertIsNone(spearman_rho([1], [1]))

    def test_zero_eligible_files_recorded_as_excluded_without_dropping(self):
        obs = {
            "windowStart": "2025-01-01T00:00:00Z",
            "windowEnd": "2025-07-01T00:00:00Z",
            "files": [
                {"lineageKey": "k1", "path": "src/main/java/A.java", "frequency": 0, "maxCcn": 2, "hotspot": None, "unavailableReason": "LINEAGE_AMBIGUOUS"}
            ],
        }
        pred = {"windowStart": "2025-07-01T00:00:00Z", "windowEnd": "2025-12-01T00:00:00Z", "facts": []}
        pair, meta = build_case_from_snapshots(
            "javapoet", obs, pred, {}, re.compile("fix", re.I), 5, 23110170, lambda sha: "msg"
        )
        self.assertIsNone(pair)
        self.assertEqual(meta["status"], "EXCLUDED_ZERO_ELIGIBLE_FILES")
        self.assertEqual(meta["excludedByReason"], {"LINEAGE_AMBIGUOUS": 1})

    def test_build_case_requires_reviewed_commits_and_maps_lineage_labels(self):
        obs = {
            "windowStart": "2025-01-01T00:00:00Z",
            "windowEnd": "2025-07-01T00:00:00Z",
            "files": [
                {"lineageKey": "k1", "path": "src/main/java/A.java", "frequency": 4, "maxCcn": 8, "hotspot": 1.0, "unavailableReason": None},
                {"lineageKey": "k2", "path": "src/main/java/B.java", "frequency": 1, "maxCcn": 1, "hotspot": 0.0, "unavailableReason": None},
            ],
        }
        pred = {
            "windowStart": "2025-07-01T00:00:00Z",
            "windowEnd": "2025-12-01T00:00:00Z",
            "facts": [
                {"commitSha": "a" * 40, "lineageKey": "k1"},
                {"commitSha": "b" * 40, "lineageKey": "k2"},
            ],
        }
        subjects = {"a" * 40: "[CSV-1] Fix parser NPE", "b" * 40: "Fix Javadoc typo"}
        reviews = {
            "demo": {
                "a" * 40: {"reviewer1": "23110170", "reviewer2": "23110158", "finalDecision": "confirmed", "reason": "Fixes runtime NPE"},
                "b" * 40: {"reviewer1": "23110170", "reviewer2": "23110158", "finalDecision": "rejected", "reason": "Javadoc comment only"},
            }
        }
        pair, meta = build_case_from_snapshots(
            "demo", obs, pred, reviews, re.compile("fix", re.I), 5, 23110170, lambda s: subjects[s]
        )
        self.assertIsNotNone(pair)
        case_doc, naive_doc = pair
        self.assertEqual(meta["positives"], 1)
        self.assertEqual(meta["naiveKeywordPositives"], 2)
        self.assertEqual(case_doc["files"][0]["label"], "positive")
        self.assertEqual(case_doc["files"][1]["label"], "negative")
        self.assertEqual(naive_doc["files"][1]["label"], "positive")

        # Missing review entry must raise ValueError
        with self.assertRaises(ValueError):
            build_case_from_snapshots("demo", obs, pred, {"demo": {}}, re.compile("fix", re.I), 5, 23110170, lambda s: subjects[s])

    def test_summarize_verifies_hashes_and_renders_markdown(self):
        with tempfile.TemporaryDirectory() as tmp:
            d = Path(tmp)
            dataset = {"kind": "reviewed-dataset", "cases": []}
            eval_doc = {
                "protocolVersion": "v1",
                "primaryEvaluation": {
                    "cases": [],
                    "macro": {
                        m: {
                            "MAP": {"value": 0.5, "validCases": 1},
                            "atK": {k: {"precision": {"value": 0.4, "validCases": 1}, "recall": {"value": 0.5, "validCases": 1}} for k in ("5", "10", "20")}
                        }
                        for m in ("frequency", "complexity", "hotspot")
                    },
                },
                "sensitivity": {},
            }
            save(d / "rq1-reviewed-dataset.json", dataset)
            save(d / "rq1-evaluation-output.json", eval_doc)
            import hashlib
            summary = {
                "datasetSha256": hashlib.sha256((d / "rq1-reviewed-dataset.json").read_bytes()).hexdigest(),
                "evaluationSha256": hashlib.sha256((d / "rq1-evaluation-output.json").read_bytes()).hexdigest(),
                "repositories": [],
            }
            save(d / "rq1-run-summary.json", summary)
            summarize(d)
            self.assertTrue((d / "results.md").exists())
            self.assertTrue((d / "rq1-summary.csv").exists())


if __name__ == "__main__":
    unittest.main()
