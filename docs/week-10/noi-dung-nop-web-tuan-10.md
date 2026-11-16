# Báo cáo tiến độ Tuần 10 - Nhóm Git Health Monitor (23110170 & 23110158)

**1. Các phần mới làm được trong Tuần 10 (Highlight):**
- **Hoàn thành thêm 7 đầu việc (STT 9, 10, 12, 14, 31, 32, 35):** Nâng tổng tiến độ lên **29 Hoàn thành / 4 Đang làm / 6 Chưa bắt đầu** (chi tiết được tô nền vàng trong file Excel `BaoCaoTIenDo_TLCN_Tuan10.xlsx`).
- **Hoàn tất thực nghiệm RQ2 (STT 31):** Viết script `evaluate_rq2_scenarios.py` và 5 unit test mới (`test_rq2_scenarios.py`), chạy kiểm chứng qua chính `HistoricalSnapshotExport` + `rq2_trends` đạt **5/5 kịch bản chuẩn (`S1..S5`)**, đồng thời kiểm toán truy vết SHA commit cho **91/91 file chung** trong 9 snapshot OSS ở Tuần 9.
- **Hoàn tất Benchmark hiệu năng 10.000 commit (STT 32 & NFR03):** Viết `run_benchmark.py` đo 3 lần lặp ở các mốc **1.000 / 5.000 / 10.000 commit** trên laptop i3-7020U @ 2.30 GHz. Mốc 10.000 commit (`12.045` facts) chạy lõi mất trung bình **48.96 giây** (tiến trình CLI đầu cuối **73.25 giây = 1.22 phút**), nhanh hơn rất nhiều so với ngưỡng **<= 10 phút** của `NFR03`.
- **Tích hợp JaCoCo `0.8.12` (NFR08) và tổng hợp công cụ (STT 35):** Chạy bộ 49 unit test không cần DB đạt **87.92%** độ phủ dòng lệnh trên package cốt lõi `gitanalyzer` (`GitIngestion` đạt 100%) và **70.39%** trên `aireview`; hoàn thành bảng đối chiếu công cụ và phân tích *Threats to Validity*.
- **Chuẩn bị sẵn bộ khảo sát RQ3 (STT 33) & xuất đủ 12 tệp Word `.docx`:** Hoàn thiện bộ đề 7 tác vụ chuẩn + bảng hỏi SUS (Brooke, 1996) + công cụ chấm điểm `evaluate_rq3_sus.py` (tổng toàn bộ bộ test Python thực nghiệm đạt **31/31 PASS**), cùng trọn bộ 12 tài liệu `.docx` nộp cổng Khoa.

**2. Chi tiết công việc từng thành viên trong Tuần 10:**
- **Phối hợp chốt bộ hồ sơ & kiểm thử hệ thống:** Tích hợp `jacoco-maven-plugin 0.8.12` vào `backend/pom.xml`, sửa lỗi biên thời gian giả lập mạng ở test `aiT11` khi chạy dưới instrumentation trên máy i3 (tất cả `12/12` `LlmReviewServiceTests` pass sau chỉnh sửa; trước đó `49/49` unit test không DB pass và `31/31` test Python pass).
- **Thực nghiệm RQ2 & Benchmark (Toản & Tưởng phối hợp):**
  - Kịch bản `S1` (tăng thật), `S2` (giữ `lineageKey` qua `git mv`), `S3` (trượt cửa sổ không có commit mới -> giảm hotspot dù `max_ccn = 5` không đổi), `S4` (kiểm chứng `Decimal` tại biên `+0.15` không báo động giả như `float`), `S5` (chặn cảnh báo an toàn khi `LINEAGE_AMBIGUOUS` hoặc lệch cửa sổ) đều chạy bằng code thật qua `HistoricalSnapshotExport` và đạt 5/5.
  - Benchmark 10.000 commit chứng minh chi phí chính nằm ở `JGit ingestion` (`45.16 s`, chiếm 92.2%), trong khi `Lizard` chỉ mất `2.44 s` và tính metric chỉ mất `0.71 s`.
- **Kế hoạch Tuần 11:** Tiến hành thu thập khảo sát người dùng thực tế RQ3 (`STT 33`, `STT 34`) trên tối thiểu 5-9 người (phấn đấu >= 10 người) với 7 tác vụ chuẩn + thang đo SUS, sửa một vòng UI theo góp ý rồi ghép thành cuốn Tiểu luận chuyên ngành hoàn chỉnh (`STT 36..39`).

**3. Minh chứng mới trong Tuần 10:**
- Code & Test: `backend/pom.xml` (JaCoCo `0.8.12`), `tools/experiments/evaluate_rq2_scenarios.py`, `test_rq2_scenarios.py`, `run_benchmark.py`, `evaluate_rq3_sus.py`, `test_rq3_sus.py` (31/31 test Python pass).
- Báo cáo số liệu: `docs/week-10/rq2-complete-evaluation.md`, `docs/week-10/benchmark-report.md`, `docs/week-10/synthesis-and-threats.md`, `docs/week-10/rq3-user-study-protocol.md`, `docs/week-10/evidence/jacoco-coverage-summary.json`, `docs/week-10/evidence/benchmark-results.json`.
