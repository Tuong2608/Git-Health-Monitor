import copy
import unittest
from evaluate_rankings import percentiles, metrics, evaluate


class EvaluationTests(unittest.TestCase):
    def fixture(self):
        return {"kind": "fixture", "cases": [{"repository": "fixture/demo", "observationStart": "2026-01-01T00:00:00Z", "cutoff": "2026-07-01T00:00:00Z", "predictionEnd": "2026-08-01T00:00:00Z", "files": [
            {"fileId": 2, "frequency": 2, "maxCcn": 3, "label": "positive"},
            {"fileId": 1, "frequency": 2, "maxCcn": 3, "label": "negative"}]}]}

    def test_midrank_ties_and_singleton(self):
        self.assertEqual(percentiles([1, 2, 2, 4]), [0, .5, .5, 1])
        self.assertEqual(percentiles([3, 3]), [.5, .5])
        self.assertEqual(percentiles([7]), [.5])
        self.assertEqual(percentiles([]), [])

    def test_ap_hand_calculated_and_k_cap(self):
        m = metrics([1, 2, 3, 4, 5], {1, 3}, (3, 20))
        self.assertAlmostEqual(m["averagePrecision"], 5/6)
        self.assertEqual(m["atK"]["3"]["recall"], 1)
        self.assertEqual(m["atK"]["20"]["kActual"], 5)

    def test_empty_and_no_positive_are_not_nan(self):
        self.assertIsNone(metrics([], set())["averagePrecision"])
        m = metrics([1], set())["atK"]["5"]
        self.assertEqual(m["precision"], 0)
        self.assertIsNone(m["recall"])

    def test_tie_policy_is_id_and_shared_universe(self):
        result = evaluate(self.fixture())
        for method in result["cases"][0]["methods"].values():
            self.assertEqual(method["ranking"], [1, 2])
            self.assertEqual(method["averagePrecision"], .5)

    def test_rejects_unresolved_missing_and_duplicate(self):
        for key, value in [("label", "uncertain"), ("maxCcn", None), ("frequency", -1), ("frequency", True), ("fileId", 1)]:
            doc = self.fixture(); doc["cases"][0]["files"][0][key] = value
            with self.assertRaises(ValueError): evaluate(doc)

    def test_reviewed_data_requires_provenance(self):
        doc = self.fixture(); doc["kind"] = "reviewed-dataset"
        with self.assertRaises(ValueError): evaluate(doc)

    def test_time_and_duplicate_case_validation(self):
        doc = self.fixture(); doc["cases"][0]["cutoff"] = "2027-01-01T00:00:00Z"
        with self.assertRaises(ValueError): evaluate(doc)
        doc = self.fixture(); doc["cases"].append(copy.deepcopy(doc["cases"][0]))
        with self.assertRaises(ValueError): evaluate(doc)

    def test_macro_counts_exclude_undefined_ap(self):
        doc = self.fixture(); empty = copy.deepcopy(doc["cases"][0]);empty["repository"] = "fixture/empty";empty["files"] = []
        doc["cases"].append(empty)
        self.assertEqual(evaluate(doc)["macro"]["hotspot"]["MAP"], {"value": .5, "validCases": 1})


if __name__ == "__main__": unittest.main()
