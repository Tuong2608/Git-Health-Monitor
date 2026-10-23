"""Evaluate exported observation features against independently reviewed prediction labels.

This tool does not clone repos, generate labels, tune thresholds, or call an LLM.
Usage: python tools/experiments/evaluate_rankings.py input.json output.json
"""
import argparse
import json
import math
from datetime import datetime
from pathlib import Path


def percentiles(values):
    """Normalized midrank, O(N log N); singleton=0.5, all ties=0.5."""
    n = len(values)
    if n == 0:
        return []
    if n == 1:
        return [0.5]
    ranks = {}
    ordered = sorted(values)
    lo = 0
    while lo < n:
        hi = lo + 1
        while hi < n and ordered[hi] == ordered[lo]:
            hi += 1
        ranks[ordered[lo]] = (lo + (hi - lo - 1) / 2) / (n - 1)
        lo = hi
    return [ranks[v] for v in values]


def metrics(ranking, positives, ks=(5, 10, 20)):
    total = len(positives)
    hits = 0
    ap = 0.0
    for i, file_id in enumerate(ranking, 1):
        if file_id in positives:
            hits += 1
            ap += hits / i
    at_k = {}
    for K in ks:
        k = min(K, len(ranking))
        tp = sum(f in positives for f in ranking[:k])
        at_k[str(K)] = {"kActual": k, "truePositives": tp,
                        "precision": tp / k if k else None,
                        "recall": tp / total if total else None}
    return {"averagePrecision": ap / total if total else None, "atK": at_k}


def timestamp(value):
    parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    if parsed.tzinfo is None:
        raise ValueError("Use timezone-aware timestamps")
    return parsed


def evaluate(document):
    if document.get("kind") not in ("fixture", "reviewed-dataset"):
        raise ValueError("kind must distinguish fixture from reviewed-dataset")
    outputs = []
    seen_cases = set()
    for case in document["cases"]:
        key = (case["repository"], case["cutoff"])
        if key in seen_cases:
            raise ValueError("Duplicate repository/cutoff would bias macro-average")
        seen_cases.add(key)
        start, cutoff, end = map(timestamp, (case["observationStart"], case["cutoff"], case["predictionEnd"]))
        if not start < cutoff < end:
            raise ValueError("Require observationStart < cutoff < predictionEnd")
        rows = case["files"]
        ids = [r["fileId"] for r in rows]
        if any(type(i) is not int or i < 1 for i in ids) or len(ids) != len(set(ids)):
            raise ValueError("fileId must be a unique positive integer")
        for row in rows:
            for field in ("frequency", "maxCcn"):
                v = row[field]
                if type(v) not in (int, float) or not math.isfinite(v) or v < 0:
                    raise ValueError("Eligible features must be finite and nonnegative; missing files need an exclusion log")
            if type(row["frequency"]) is not int:
                raise ValueError("Frequency is an integer count")
            if row["label"] not in ("positive", "negative"):
                raise ValueError("Unresolved labels must be reviewed before evaluation")
            if document["kind"] == "reviewed-dataset" and not row.get("labelEvidence"):
                raise ValueError("Reviewed data requires label evidence for each row, including negative review/coverage")
        frequency = percentiles([r["frequency"] for r in rows])
        complexity = percentiles([r["maxCcn"] for r in rows])
        scores = {"frequency": [r["frequency"] for r in rows],
                  "complexity": [r["maxCcn"] for r in rows],
                  "hotspot": [math.sqrt(a * b) for a, b in zip(frequency, complexity)]}
        positives = {r["fileId"] for r in rows if r["label"] == "positive"}
        methods = {}
        for name, values in scores.items():
            ranking = [ids[i] for i in sorted(range(len(ids)), key=lambda i: (-values[i], ids[i]))]
            methods[name] = {"ranking": ranking, **metrics(ranking, positives)}
        outputs.append({"repository": case["repository"], "cutoff": case["cutoff"], "eligibleFiles": len(ids), "positives": len(positives), "methods": methods})
    macro = {}
    def average(values):
        valid = [v for v in values if v is not None]
        return {"value": sum(valid) / len(valid) if valid else None, "validCases": len(valid)}
    for method in ("frequency", "complexity", "hotspot"):
        macro[method] = {"MAP": average([c["methods"][method]["averagePrecision"] for c in outputs]),
                         "atK": {str(k): {m: average([c["methods"][method]["atK"][str(k)][m] for c in outputs]) for m in ("precision", "recall")} for k in (5, 10, 20)}}
    return {"kind": document["kind"], "policy": "week06-proposed-v1-midrank-id-tie", "cases": outputs, "macro": macro,
            "limitation": "Export validation cannot prove absence of temporal leakage; audit manifest and source extraction separately."}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    result = evaluate(json.loads(args.input.read_text(encoding="utf-8")))
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2, allow_nan=False) + "\n", encoding="utf-8")
