# Tuần 9 — Nhóm Git Health Monitor (Trần Văn Tưởng 23110170 & Trần Quang Toản 23110158)

Đối chiếu `docs/BaoCaoTienDo_TLCN.xlsx` ngày 12/11/2026:
- **STT 20 (Thiết kế thực nghiệm — Cả 2):** Hoàn thành — đã chốt chính thức giao thức thực nghiệm v1 (`week09-rq1-approved-v1` & `rq2-protocol.md`).
- **STT 29 (Chọn tập dữ liệu thực nghiệm — Cả 2):** Hoàn thành — đã cố định manifest cho **5 dự án Java OSS chọn mù của RQ1** (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`) và **3 dự án lịch sử của RQ2** (`gs-rest-service`, `spring-petclinic`, `commons-cli`).
- **STT 30 (Thực nghiệm RQ1: xếp hạng rủi ro — Tưởng 23110170):** Hoàn thành — đã triển khai `run_rq1.py`, `summarize_rq1.py`, duyệt thủ công `141` commit trong `rq1-commit-reviews.json`, chạy đánh giá xếp hạng trên `401` file Java đủ điều kiện và xác nhận tái lập khớp SHA-256 `13/13` file JSON.
- **STT 31 (Thực nghiệm RQ2: theo dõi xu hướng qua snapshot — Toản 23110158):** Đang làm — hoàn thành CLI `HistoricalSnapshotExport`, công cụ RQ2, chạy `9/9` snapshot và `6` cặp đối chiếu, tái lập khớp SHA-256 `15/15` file JSON; tiếp tục đánh giá giải thích tuần 10.
- **STT 32 (Hiệu năng) & STT 33 (Người dùng RQ3):** Thuộc tuần 10.

## Đọc theo thứ tự

1. **Tổng hợp nhóm & nộp web:** [Báo cáo nhóm tuần 9](bao-cao-tuan-09-nhom.md), [Nội dung nộp web tuần 9](noi-dung-nop-web-tuan-09.md).
2. **Phần RQ1 (Trần Văn Tưởng — 23110170):** [Báo cáo cá nhân Tưởng](bao-cao-tuan-09-tuong.md), [Giao thức RQ1 & chốt STT 20](rq1-protocol.md), [Đề xuất dữ liệu RQ1](rq1-dataset-proposal.json), [Manifest RQ1 đã cố định](evidence/rq1-frozen-manifest.json), [Sổ duyệt 141 commit](rq1-commit-reviews.json), [Bảng kết quả RQ1](evidence/rq1/results.md), [CSV số liệu RQ1](evidence/rq1/rq1-summary.csv), [Kiểm chứng tái lập RQ1](evidence/rq1-replay-verification.json), [Nhật ký AI Tưởng](ai-usage-tuong-week09.md).
3. **Phần RQ2 (Trần Quang Toản — 23110158):** [Báo cáo cá nhân Toản](bao-cao-tuan-09-toan.md), [Giao thức RQ2](rq2-protocol.md), [Đề xuất dữ liệu RQ2](dataset-proposal.json), [Manifest RQ2 đã cố định](evidence/frozen-manifest.json), [Bảng kết quả RQ2](evidence/pilot/results.md), [CSV số liệu RQ2](evidence/pilot/snapshot-summary.csv), [Kiểm chứng tái lập RQ2](evidence/replay-verification.json), [Nhật ký AI Toản](ai-usage-toan-week09.md).
4. **Hướng dẫn chạy lại & kiểm chứng:** [Runbook chạy lại RQ1 & RQ2](runbook.md), [Kiểm chứng](evidence/verification.md).
