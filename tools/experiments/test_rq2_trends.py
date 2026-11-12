import copy
import unittest
from rq2_trends import compare


class Rq2Tests(unittest.TestCase):
    def snapshot(self, month, files=None):
        return {"repositoryUrl": "https://github.com/fixture/demo", "headSha": str(month) * 40, "windowStart": f"2026-{month:02d}-01T00:00:00Z",
                "windowEnd": f"2026-{month:02d}-11T00:00:00Z", "config": '{"a":1,"b":2}',
                "metricVersion": "test", "truncated": False, "summary": {"coverageComplete": True},
                "files": files if files is not None else [self.file("a", .2)], "facts": []}

    def file(self, key, score, path=None):
        return {"lineageKey": key, "path": path or key + ".java", "frequency": 2, "maxCcn": 3,
                "hotspot": score, "unavailableReason": "NO_FUNCTIONS" if score is None else None}

    def test_delta_threshold_is_strict_and_decimal(self):
        result = compare(self.snapshot(1), self.snapshot(2, [self.file("a", .35)]))
        self.assertEqual(result["alertCandidates"], 0)
        self.assertEqual(result["changes"][0]["hotspotDelta"], .15)
        self.assertEqual(compare(self.snapshot(1), self.snapshot(2, [self.file("a", .36)]))["alertCandidates"], 1)

    def test_rename_matches_lineage_but_path_reuse_does_not(self):
        result = compare(self.snapshot(1, [self.file("a", .2), self.file("old", .8, "Same.java")]),
                         self.snapshot(2, [self.file("a", .4, "Renamed.java"), self.file("new", .8, "Same.java")]))
        self.assertEqual(result["added"], ["new"])
        self.assertEqual(result["removed"], ["old"])
        self.assertEqual(result["changes"][0]["afterPath"], "Renamed.java")

    def test_null_and_zero_have_distinct_meaning(self):
        result = compare(self.snapshot(1, [self.file("a", None), self.file("b", 0)]),
                         self.snapshot(2, [self.file("a", .8), self.file("b", .5)]))
        self.assertEqual(result["unavailableCommonFiles"], 1)
        self.assertIsNone(result["changes"][0]["relativeDelta"])
        self.assertEqual(result["changes"][0]["relativeStatus"], "BASE_ZERO")

    def test_coverage_blocks_alert_even_when_diagnostic_delta_is_large(self):
        before, after = self.snapshot(1), self.snapshot(2, [self.file("a", .9)])
        before["summary"]["coverageComplete"] = False
        result = compare(before, after)
        self.assertEqual(result["status"], "BLOCKED")
        self.assertIsNone(result["alertCandidates"])
        self.assertTrue(result["diagnosticChangesOnly"])

    def test_config_version_duration_and_truncation_block_comparison(self):
        for mutate, reason in [
            (lambda s: s.update(config='{"a":2}'), "CONFIG_CHANGED"),
            (lambda s: s.update(metricVersion="other"), "METRIC_VERSION_CHANGED"),
            (lambda s: s.update(windowStart="2026-02-02T00:00:00Z"), "WINDOW_DURATION_CHANGED"),
            (lambda s: s.update(truncated=True), "INCOMPLETE_COVERAGE")]:
            with self.subTest(reason=reason):
                after = self.snapshot(2)
                mutate(after)
                self.assertIn(reason, compare(self.snapshot(1), after)["blockers"])

    def test_config_key_order_does_not_change_compatibility(self):
        after = self.snapshot(2)
        after["config"] = '{"b":2,"a":1}'
        self.assertEqual(compare(self.snapshot(1), after)["status"], "COMPARABLE")

    def test_invalid_values_duplicates_and_outside_window_evidence_rejected(self):
        mutations = [lambda s: s["files"][0].update(hotspot=float("nan")),
                     lambda s: s["files"][0].update(frequency=-1),
                     lambda s: s["files"].append(copy.deepcopy(s["files"][0])),
                     lambda s: s.update(facts=[{"committerTime": "2026-02-11T00:00:00Z"}])]
        for mutate in mutations:
            after = self.snapshot(2)
            mutate(after)
            with self.assertRaises(ValueError):
                compare(self.snapshot(1), after)

    def test_reverse_chronology_rejected_and_empty_universe_not_positive(self):
        with self.assertRaises(ValueError):
            compare(self.snapshot(2), self.snapshot(1))
        result = compare(self.snapshot(1, []), self.snapshot(2, []))
        self.assertEqual(result["comparableCommonFiles"], 0)
        self.assertEqual(result["alertCandidates"], 0)

    def test_cross_repository_comparison_rejected(self):
        after = self.snapshot(2)
        after["repositoryUrl"] = "https://github.com/fixture/other"
        with self.assertRaises(ValueError):
            compare(self.snapshot(1), after)


if __name__ == "__main__":
    unittest.main()
