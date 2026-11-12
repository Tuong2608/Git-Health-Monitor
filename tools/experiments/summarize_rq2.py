"""Generate an auditable table from a completed RQ2 run, including blocked comparisons."""
import argparse
import csv
import hashlib
import json
from pathlib import Path


def summarize(directory):
    summary = json.loads((directory / "run-summary.json").read_text(encoding="utf-8"))
    lines = ["# Kết quả RQ2 — dữ liệu chạy thử", "",
             "Các thay đổi ở cặp BLOCKED chỉ là dữ liệu chẩn đoán, không phải cảnh báo hợp lệ hay bằng chứng dự đoán lỗi.", "",
             "| Dự án | Mốc cuối (UTC) | Commit trong cửa sổ | File Java | File có hotspot | Lineage chưa rõ | Coverage |",
             "|---|---|---:|---:|---:|---:|---|"]
    records, comparisons = [], []
    for repo in summary["repositories"]:
        for row in repo["snapshots"]:
            if row.get("status") != "SUCCESS":
                lines.append(f"| {repo['id']} | {row['windowEnd']} | — | — | — | — | {row.get('status', row['selectionStatus'])} |")
                continue
            file = directory / row["output"]
            if hashlib.sha256(file.read_bytes()).hexdigest() != row["sha256"]:
                raise ValueError(f"Evidence hash mismatch: {file}")
            data = json.loads(file.read_text(encoding="utf-8"))
            s = data["summary"]
            record = [repo["id"], row["windowEnd"], data["commitCount"], len(data["files"]), s["hotspotPopulation"], s["ambiguousFiles"], s["coverageComplete"]]
            records.append(record)
            lines.append("| " + " | ".join(map(str, record)) + " |")
            if "comparison" in row:
                result = json.loads((directory / row["comparison"]).read_text(encoding="utf-8"))
                comparisons.append((repo, row, result))
    lines.extend(["", "| Cặp snapshot | Trạng thái | File chung có điểm (chẩn đoán) | Thêm / mất lineage | Cảnh báo hợp lệ |",
                  "|---|---|---:|---|---|"])
    for repo, row, result in comparisons:
        lines.append(f"| [{row['comparison']}]({row['comparison']}) | {result['status']} | {result['comparableCommonFiles']} | {len(result['added'])} / {len(result['removed'])} | {'N/A' if result['alertCandidates'] is None else result['alertCandidates']} |")
    lines.extend(["", "## Ví dụ chẩn đoán có thể đối chiếu", "",
                  "Mỗi cặp lấy đúng một file có |delta hotspot| lớn nhất, hòa thì theo lineageKey; quy tắc này chỉ chọn ví dụ trình bày sau đo, không chọn/bỏ dự án thực nghiệm.", ""])
    for repo, row, result in comparisons:
        if not result["changes"]:
            continue
        c = sorted(result["changes"], key=lambda c: (-abs(c["hotspotDelta"]), c["lineageKey"]))[0]
        a, b = c["before"], c["after"]
        lines.extend([f"### {repo['id']} — cặp {row['index']-1} → {row['index']}", "",
                      f"File: `{c['afterPath']}`. Frequency {a['frequency']} → {b['frequency']}; max CCN {a['maxCcn']} → {b['maxCcn']}; hotspot {a['hotspot']:.6f} → {b['hotspot']:.6f}; delta {c['hotspotDelta']:+.6f}.", "",
                      "Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.", ""])
        if c["evidenceCommits"]:
            lines.append("Commit trong cửa sổ sau: " + ", ".join(f"[{sha[:8]}]({repo['url']}/commit/{sha})" for sha in c["evidenceCommits"]) + ".")
        else:
            lines.append("Không có commit của file trong cửa sổ sau. Frequency giảm khi các commit trước rời cửa sổ không có nghĩa code đã được sửa cho tốt hơn.")
        lines.append("")
    with (directory / "snapshot-summary.csv").open("w", newline="", encoding="utf-8") as stream:
        writer = csv.writer(stream)
        writer.writerow(["repository", "windowEnd", "selectedCommits", "javaFiles", "hotspotPopulation", "ambiguousFiles", "coverageComplete"])
        writer.writerows(records)
    (directory / "results.md").write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("directory", type=Path)
    summarize(parser.parse_args().directory)
