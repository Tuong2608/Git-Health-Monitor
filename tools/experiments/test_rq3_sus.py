"""Unit tests for RQ3 User Study & System Usability Scale (SUS) evaluator."""
import tempfile
import unittest
from pathlib import Path
from evaluate_rq3_sus import compute_sus_score, evaluate_participants, load_csv_responses


class Rq3SusTests(unittest.TestCase):
    def test_sus_formula_bounds_and_exact_hand_calculated_score(self):
        # All 5s on odd items (5-1=4 each) and all 1s on even items (5-1=4 each) -> 40 * 2.5 = 100.0
        self.assertEqual(compute_sus_score([5, 1, 5, 1, 5, 1, 5, 1, 5, 1]), 100.0)
        # Worst possible: 1 on odd, 5 on even -> 0 * 2.5 = 0.0
        self.assertEqual(compute_sus_score([1, 5, 1, 5, 1, 5, 1, 5, 1, 5]), 0.0)
        # Neutral all 3s -> 20 * 2.5 = 50.0
        self.assertEqual(compute_sus_score([3] * 10), 50.0)
        # Realistic mixed case: odd=[4,5,4,4,5] -> (3+4+3+3+4=17); even=[2,2,1,2,2] -> (3+3+4+3+3=16) => 33*2.5 = 82.5
        self.assertEqual(compute_sus_score([4, 2, 5, 2, 4, 1, 4, 2, 5, 2]), 82.5)

    def test_sus_rejects_wrong_length_or_out_of_range(self):
        with self.assertRaises(ValueError):
            compute_sus_score([3] * 9)
        with self.assertRaises(ValueError):
            compute_sus_score([6, 1, 1, 1, 1, 1, 1, 1, 1, 1])
        with self.assertRaises(ValueError):
            compute_sus_score([True, 1, 1, 1, 1, 1, 1, 1, 1, 1])

    def test_evaluate_participants_computes_task_rates_thresholds_and_medians(self):
        sample = [
            {
                "participantId": f"P{i}",
                "role": "SV năm 4",
                "tasks": {
                    "T1": {"passed": True, "seconds": 30},
                    "T2": {"passed": True, "seconds": 25},
                    "T3": {"passed": True, "seconds": 40},
                    "T4": {"passed": i != 5, "seconds": 55},
                    "T5": {"passed": True, "seconds": 35},
                    "T6": {"passed": True, "seconds": 20},
                    "T7": {"passed": True, "seconds": 45},
                },
                "sus": [4, 2, 5, 2, 4, 2, 4, 2, 4, 2],  # 16 + 15 = 31 => 77.5
                "feedback": "Clear hotspot table.",
            }
            for i in range(1, 6)
        ]
        report = evaluate_participants(sample)
        self.assertEqual(report["participantCount"], 5)
        self.assertEqual(report["overallTaskPassed"], 34)
        self.assertEqual(report["overallTaskAttempts"], 35)
        self.assertEqual(report["overallTaskSuccessRatePct"], 97.14)
        self.assertEqual(report["sus"]["mean"], 77.5)
        self.assertTrue(report["thresholds"]["proposalBaselineTaskSuccessMet"])
        self.assertTrue(report["thresholds"]["proposalBaselineSusMet"])
        self.assertFalse(report["thresholds"]["rubricStretchSusMet"])
        self.assertEqual(report["perTask"]["T4"]["passed"], 4)

    def test_load_csv_responses_parses_google_form_export_and_rejects_duplicates(self):
        csv_text = (
            "participantId,role,T1,T1_sec,T2,T2_sec,T3,T3_sec,T4,T4_sec,T5,T5_sec,T6,T6_sec,T7,T7_sec,"
            "Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8,Q9,Q10,feedback\n"
            "P01,Dev,pass,20,1,30,true,40,đạt,50,yes,25,1,15,1,60,5,1,5,1,5,1,5,1,5,1,Rất rõ ràng\n"
        )
        with tempfile.TemporaryDirectory() as td:
            p = Path(td) / "responses.csv"
            p.write_text(csv_text, encoding="utf-8")
            rows = load_csv_responses(p)
            self.assertEqual(len(rows), 1)
            report = evaluate_participants(rows)
            self.assertEqual(report["overallTaskSuccessRatePct"], 100.0)
            self.assertEqual(report["sus"]["mean"], 100.0)

            with self.assertRaises(ValueError):
                evaluate_participants([rows[0], rows[0]])


if __name__ == "__main__":
    unittest.main()
