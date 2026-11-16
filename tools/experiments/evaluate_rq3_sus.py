"""RQ3 User Study & System Usability Scale (SUS, Brooke 1996) evaluator."""
import argparse
import csv
import json
import math
import statistics
from pathlib import Path

REQUIRED_TASKS = ("T1", "T2", "T3", "T4", "T5", "T6", "T7")

TASK_LABELS = {
    "T1": "Đăng ký repository và kích hoạt phân tích",
    "T2": "Tìm Top 5 tập tin ưu tiên rà soát (Hotspot)",
    "T3": "Giải thích lý do rủi ro của tập tin (Frequency, CCN, HHI)",
    "T4": "Xác định cặp tập tin liên kết thời gian (Temporal Coupling)",
    "T5": "Xem lịch sử snapshot và biểu đồ xu hướng",
    "T6": "Kiểm tra cảnh báo và lý do kích hoạt ngưỡng",
    "T7": "Xem trước ngữ cảnh, xác nhận consent và gọi AI Review",
}

SUS_QUESTIONS = [
    "Tôi nghĩ rằng mình sẽ muốn sử dụng hệ thống này thường xuyên.",
    "Tôi thấy hệ thống này phức tạp một cách không cần thiết.",
    "Tôi nghĩ rằng hệ thống này dễ sử dụng.",
    "Tôi nghĩ rằng tôi sẽ cần sự hỗ trợ của nhân viên kỹ thuật để có thể sử dụng hệ thống này.",
    "Tôi thấy các chức năng khác nhau trong hệ thống này được tích hợp tốt.",
    "Tôi nghĩ rằng có quá nhiều sự không nhất quán trong hệ thống này.",
    "Tôi hình dung rằng hầu hết mọi người sẽ học cách sử dụng hệ thống này rất nhanh chóng.",
    "Tôi thấy hệ thống này rất rườm rà/khó thao tác khi sử dụng.",
    "Tôi cảm thấy rất tự tin khi sử dụng hệ thống này.",
    "Tôi cần phải học rất nhiều thứ trước khi có thể bắt đầu sử dụng hệ thống này.",
]


def compute_sus_score(responses: list[int]) -> float:
    """Compute Brooke (1996) SUS score in [0, 100] from 10 Likert items (1..5)."""
    if len(responses) != 10:
        raise ValueError(f"Expected 10 SUS items, got {len(responses)}")
    contributions = []
    for idx, raw in enumerate(responses, start=1):
        if isinstance(raw, bool) or not isinstance(raw, int) or not 1 <= raw <= 5:
            raise ValueError(f"SUS item Q{idx} must be integer in 1..5, got {raw!r}")
        contributions.append((raw - 1) if idx % 2 == 1 else (5 - raw))
    return sum(contributions) * 2.5


def evaluate_participants(participants: list[dict]) -> dict:
    """Evaluate task completion rates and SUS statistics across study participants."""
    if not participants:
        raise ValueError("Participant list must not be empty")

    seen_ids: set[str] = set()
    sus_scores: list[float] = []
    task_passes = {t: 0 for t in REQUIRED_TASKS}
    task_durations: dict[str, list[float]] = {t: [] for t in REQUIRED_TASKS}
    participant_summaries = []

    for row in participants:
        pid = str(row.get("participantId", "")).strip()
        if not pid:
            raise ValueError("Missing participantId")
        if pid in seen_ids:
            raise ValueError(f"Duplicate participantId: {pid}")
        seen_ids.add(pid)

        tasks = row.get("tasks")
        if not isinstance(tasks, dict):
            raise ValueError(f"Participant {pid} missing tasks object")
        passed_count = 0
        duration_sum = 0.0
        has_duration = False
        for tid in REQUIRED_TASKS:
            if tid not in tasks:
                raise ValueError(f"Participant {pid} missing task {tid}")
            t_entry = tasks[tid]
            if isinstance(t_entry, bool):
                ok = t_entry
                sec = None
            elif isinstance(t_entry, dict):
                if "passed" not in t_entry or not isinstance(t_entry["passed"], bool):
                    raise ValueError(f"Participant {pid} task {tid} requires boolean passed")
                ok = t_entry["passed"]
                sec = t_entry.get("seconds")
            else:
                raise ValueError(f"Invalid task entry for {pid}/{tid}")
            if sec is not None:
                if isinstance(sec, bool) or not isinstance(sec, (int, float)) or not math.isfinite(sec) or sec < 0:
                    raise ValueError(f"Invalid duration seconds for {pid}/{tid}: {sec!r}")
                task_durations[tid].append(float(sec))
                duration_sum += float(sec)
                has_duration = True
            if ok:
                task_passes[tid] += 1
                passed_count += 1

        sus_items = row.get("sus")
        if not isinstance(sus_items, list):
            raise ValueError(f"Participant {pid} missing 10-item sus list")
        sus = compute_sus_score(sus_items)
        sus_scores.append(sus)

        participant_summaries.append({
            "participantId": pid,
            "role": row.get("role", ""),
            "tasksPassed": passed_count,
            "tasksTotal": len(REQUIRED_TASKS),
            "taskSuccessRatePct": round(100.0 * passed_count / len(REQUIRED_TASKS), 2),
            "totalDurationSeconds": round(duration_sum, 2) if has_duration else None,
            "susScore": sus,
            "feedback": row.get("feedback", ""),
        })

    n = len(participants)
    total_attempts = n * len(REQUIRED_TASKS)
    total_passed = sum(task_passes.values())
    overall_task_rate = round(100.0 * total_passed / total_attempts, 2)
    mean_sus = round(statistics.mean(sus_scores), 2)
    median_sus = round(statistics.median(sus_scores), 2)
    stdev_sus = round(statistics.stdev(sus_scores), 2) if n >= 2 else 0.0

    per_task = {}
    for tid in REQUIRED_TASKS:
        durs = task_durations[tid]
        per_task[tid] = {
            "label": TASK_LABELS[tid],
            "passed": task_passes[tid],
            "total": n,
            "successRatePct": round(100.0 * task_passes[tid] / n, 2),
            "medianSeconds": round(statistics.median(durs), 2) if durs else None,
        }

    return {
        "schemaVersion": 1,
        "kind": "RQ3_USER_STUDY_EVALUATION",
        "participantCount": n,
        "overallTaskPassed": total_passed,
        "overallTaskAttempts": total_attempts,
        "overallTaskSuccessRatePct": overall_task_rate,
        "sus": {
            "mean": mean_sus,
            "median": median_sus,
            "min": min(sus_scores),
            "max": max(sus_scores),
            "stdev": stdev_sus,
        },
        "thresholds": {
            "proposalBaselineTaskSuccessMet": n >= 5 and overall_task_rate >= 80.0,
            "proposalBaselineSusMet": n >= 5 and mean_sus >= 68.0,
            "rubricStretchParticipantsMet": n >= 10,
            "rubricStretchTaskSuccessMet": overall_task_rate >= 90.0,
            "rubricStretchSusMet": mean_sus >= 80.0,
        },
        "perTask": per_task,
        "participants": participant_summaries,
    }


def load_csv_responses(csv_path: Path) -> list[dict]:
    """Load Google Form CSV export cleanly (columns: participantId,role,T1..T7,Q1..Q10,feedback)."""
    participants = []
    with csv_path.open(encoding="utf-8-sig", newline="") as stream:
        for row in csv.DictReader(stream):
            tasks = {}
            for tid in REQUIRED_TASKS:
                raw_ok = str(row[tid]).strip().lower()
                if raw_ok in ("1", "true", "pass", "passed", "yes", "dat", "đạt"):
                    ok = True
                elif raw_ok in ("0", "false", "fail", "failed", "no", "khong", "không"):
                    ok = False
                else:
                    raise ValueError(f"Unrecognized boolean value for {tid}: {row[tid]!r}")
                dur_key = f"{tid}_sec"
                sec = float(row[dur_key]) if row.get(dur_key) not in (None, "") else None
                tasks[tid] = {"passed": ok, "seconds": sec}
            sus = [int(row[f"Q{i}"]) for i in range(1, 11)]
            participants.append({
                "participantId": row["participantId"].strip(),
                "role": row.get("role", "").strip(),
                "tasks": tasks,
                "sus": sus,
                "feedback": row.get("feedback", "").strip(),
            })
    return participants


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("input_file", type=Path, help="Input JSON array/object or Google Form CSV export")
    parser.add_argument("--out-json", type=Path, required=True)
    args = parser.parse_args()

    if args.input_file.suffix.lower() == ".csv":
        participants = load_csv_responses(args.input_file)
    else:
        payload = json.loads(args.input_file.read_text(encoding="utf-8"))
        participants = payload["participants"] if isinstance(payload, dict) else payload

    report = evaluate_participants(participants)
    args.out_json.parent.mkdir(parents=True, exist_ok=True)
    args.out_json.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"RQ3 Evaluated N={report['participantCount']}: TaskSuccess={report['overallTaskSuccessRatePct']}%, MeanSUS={report['sus']['mean']}")


if __name__ == "__main__":
    main()
