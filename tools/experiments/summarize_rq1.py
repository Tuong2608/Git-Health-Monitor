"""Generate an auditable markdown report and CSV from a completed RQ1 run."""
import argparse
import csv
import hashlib
import json
from pathlib import Path


def fmt(val, digits=4):
    return "N/A" if val is None else f"{val:.{digits}f}"


def summarize(directory):
    summary = json.loads((directory / "rq1-run-summary.json").read_text(encoding="utf-8"))
    dataset_file = directory / "rq1-reviewed-dataset.json"
    eval_file = directory / "rq1-evaluation-output.json"
    if hashlib.sha256(dataset_file.read_bytes()).hexdigest() != summary["datasetSha256"]:
        raise ValueError("Dataset SHA-256 mismatch")
    if hashlib.sha256(eval_file.read_bytes()).hexdigest() != summary["evaluationSha256"]:
        raise ValueError("Evaluation SHA-256 mismatch")

    eval_doc = json.loads(eval_file.read_text(encoding="utf-8"))
    primary = eval_doc["primaryEvaluation"]
    case_by_repo = {c["repository"]: c for c in primary["cases"]}

    lines = [
        "# Kết quả thực nghiệm RQ1 — Đánh giá xếp hạng rủi ro bảo trì theo chia cắt thời gian 80/20",
        "",
        "Dữ liệu đầu vào được cố định trong `rq1-frozen-manifest.json` trước khi chạy xếp hạng; nhãn `positive`/`negative` được kiểm tra thủ công từ tập commit ứng viên (Jira/keyword) và mẫu âm ngẫu nhiên (`seed=23110170`). Dự án có `eligibleFiles = 0` do quy tắc lineage qua merge ở `v1` (`javapoet`) được giữ nguyên trong bảng kiểm toán, không loại bỏ để làm đẹp số liệu.",
        "",
        "## 1. Thống kê tập dữ liệu và rà soát nhãn theo dự án",
        "",
        "| Dự án | Mốc cắt 80/20 (UTC) | Tỷ lệ commit thực | File Java | File đủ điều kiện ($N$) | Lý do loại (`v1`) | Ứng viên / Mẫu âm đã duyệt | Commit bug-fix xác nhận | File dương ($P$) | Nhãn từ khóa thô (chưa lọc) |",
        "|---|---|---:|---:|---:|---|---:|---:|---:|---:|",
    ]

    csv_rows = []
    for repo in summary["repositories"]:
        repo_id = repo["repository"]
        if repo["status"] != "EVALUATED":
            obs_file = directory / repo["observationSnapshot"]
            if hashlib.sha256(obs_file.read_bytes()).hexdigest() != repo["observationSha256"]:
                raise ValueError(f"Observation hash mismatch: {obs_file}")
            reasons = ", ".join(f"{k}:{v}" for k, v in sorted(repo.get("excludedByReason", {}).items())) or "—"
            lines.append(
                f"| {repo_id} | {repo['cutoff']} | {repo['realizedCommitRatio']:.4f} | {repo['totalJavaFiles']} | 0 | {reasons} | — | — | — | — |"
            )
            csv_rows.append([
                repo_id, repo["cutoff"], repo["realizedCommitRatio"], repo["totalJavaFiles"], 0, 0,
                "EXCLUDED_ZERO_ELIGIBLE_FILES", None, None, None, None, None, None, None, None
            ])
            continue

        for key_file, key_hash in (("observationSnapshot", "observationSha256"), ("predictionSnapshot", "predictionSha256")):
            path = directory / repo[key_file]
            if hashlib.sha256(path.read_bytes()).hexdigest() != repo[key_hash]:
                raise ValueError(f"Snapshot hash mismatch: {path}")

        reasons = ", ".join(f"{k}:{v}" for k, v in sorted(repo.get("excludedByReason", {}).items())) or "0"
        audited_str = f"{repo['candidateCommitsAudited']} + {repo['negativeSampleCommitsAudited']}"
        lines.append(
            f"| {repo_id} | {repo['cutoff']} | {repo['realizedCommitRatio']:.4f} | {repo['totalJavaFiles']} | {repo['eligibleFiles']} | {reasons} | {audited_str} | {repo['confirmedBugFixCommits']} (mẫu âm: {repo['confirmedViaNegativeSample']}) | {repo['positives']} | {repo['naiveKeywordPositives']} |"
        )

    lines.extend([
        "",
        "## 2. Kết quả xếp hạng trên từng dự án hợp lệ ($N > 0, P > 0$)",
        "",
        "| Dự án | Phương pháp | AP | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 | Spearman $\\rho$ |",
        "|---|---|---:|---:|---:|---:|---:|---:|---:|---:|",
    ])

    for repo in summary["repositories"]:
        if repo["status"] != "EVALUATED":
            continue
        repo_id = repo["repository"]
        case = case_by_repo[repo_id]
        rhos = repo["spearmanRhoWithBugFixCount"]
        for method in ("frequency", "complexity", "hotspot"):
            m = case["methods"][method]
            k5, k10, k20 = m["atK"]["5"], m["atK"]["10"], m["atK"]["20"]
            rho = rhos.get(method)
            lines.append(
                f"| {repo_id} | `{method}` | {fmt(m['averagePrecision'])} | {fmt(k5['precision'])} | {fmt(k5['recall'])} | {fmt(k10['precision'])} | {fmt(k10['recall'])} | {fmt(k20['precision'])} | {fmt(k20['recall'])} | {fmt(rho)} |"
            )
            csv_rows.append([
                repo_id, repo["cutoff"], repo["realizedCommitRatio"], repo["totalJavaFiles"],
                repo["eligibleFiles"], repo["positives"], method,
                m["averagePrecision"], k5["precision"], k5["recall"],
                k10["precision"], k10["recall"], k20["precision"], k20["recall"], rho
            ])

    lines.extend([
        "",
        "## 3. Trung bình vĩ mô (Macro-Average) trên các dự án đủ điều kiện (`validCases = 4 / 5`)",
        "",
        "| Phương pháp | MAP (`validCases=4`) | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 |",
        "|---|---:|---:|---:|---:|---:|---:|---:|",
    ])

    for method in ("frequency", "complexity", "hotspot"):
        mm = primary["macro"][method]
        k5, k10, k20 = mm["atK"]["5"], mm["atK"]["10"], mm["atK"]["20"]
        lines.append(
            f"| `{method}` | {fmt(mm['MAP']['value'])} | {fmt(k5['precision']['value'])} | {fmt(k5['recall']['value'])} | {fmt(k10['precision']['value'])} | {fmt(k10['recall']['value'])} | {fmt(k20['precision']['value'])} | {fmt(k20['recall']['value'])} |"
        )

    sens = eval_doc.get("sensitivity", {})
    if sens.get("productionMainJavaOnly") and sens.get("unfilteredKeywordHeuristic"):
        lines.extend([
            "",
            "## 4. Phân tích độ nhạy (Sensitivity Analysis)",
            "",
            "| Cấu hình đánh giá | Phương pháp | MAP (`validCases=4`) | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 |",
            "|---|---|---:|---:|---:|---:|---:|---:|---:|",
        ])
        for label, block in (
            ("Chỉ file nguồn `src/main/java`", sens["productionMainJavaOnly"]),
            ("Nhãn từ khóa thô chưa lọc (`fix/bug`)", sens["unfilteredKeywordHeuristic"]),
        ):
            for method in ("frequency", "complexity", "hotspot"):
                mm = block[method]
                k5, k10, k20 = mm["atK"]["5"], mm["atK"]["10"], mm["atK"]["20"]
                lines.append(
                    f"| {label} | `{method}` | {fmt(mm['MAP']['value'])} | {fmt(k5['precision']['value'])} | {fmt(k5['recall']['value'])} | {fmt(k10['precision']['value'])} | {fmt(k10['recall']['value'])} | {fmt(k20['precision']['value'])} | {fmt(k20['recall']['value'])} |"
                )

    with (directory / "rq1-summary.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.writer(stream)
        writer.writerow([
            "repository", "cutoff", "realizedCommitRatio", "totalJavaFiles",
            "eligibleFiles", "positives", "method", "AP", "P@5", "R@5", "P@10", "R@10", "P@20", "R@20", "spearmanRho"
        ])
        writer.writerows(csv_rows)

    (directory / "results.md").write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    summarize(parser.parse_args().directory)
