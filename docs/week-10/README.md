# Tuần 10 — Trần Văn Tưởng (23110170) & Trần Quang Toản (23110158)

Tuần 10 (`10/11–18/11/2026`). Đối chiếu `docs/BaoCaoTienDo_TLCN.xlsx`: hoàn thành thêm **7 đầu việc (`STT 9, 10, 12, 14, 31, 32, 35`)**, nâng tổng tiến độ lên **29 Hoàn thành / 4 Đang làm / 6 Chưa bắt đầu**. Riêng `STT 33` (Thực nghiệm người dùng RQ3) và `STT 34` (Cải tiến sau phản hồi) đã chuẩn bị xong bộ đề 7 tác vụ chuẩn, bảng hỏi SUS, phiếu khảo sát và công cụ chấm điểm tự động, giữ trạng thái *Chưa bắt đầu* để thu thập người dùng thật trong Tuần 11.

---

## Danh mục tài liệu và minh chứng Tuần 10

| Tài liệu / Minh chứng | Mục đích & Nội dung chính |
|---|---|
| [Báo cáo cá nhân Tuần 10 (Toản)](bao-cao-tuan-10-toan.md) | Báo cáo chi tiết phần việc của Trần Quang Toản (`23110158`): hoàn tất RQ2 (`STT 31`), Benchmark hiệu năng & PostgreSQL (`STT 32`), độ phủ JaCoCo full-stack có PostgreSQL 18.3 (`NFR08`) và chuẩn bị phiếu khảo sát RQ3 (`STT 33`) |
| [Báo cáo nhóm Tuần 10](bao-cao-tuan-10-nhom.md) | Tổng hợp kết quả Tuần 10 của cả hai thành viên (29 Hoàn thành / 4 Đang làm / 6 Chưa bắt đầu) |
| [Nội dung nộp web Tuần 10](noi-dung-nop-web-tuan-10.md) | Bản tóm tắt kết quả nổi bật để nộp lên cổng quản lý của Khoa |
| [Đánh giá hoàn chỉnh RQ2 (`STT 31`)](rq2-complete-evaluation.md) | Kết quả 5 kịch bản tiến hóa chuẩn (`S1..S5`), kiểm toán `91/91` file chung OSS, phân rã `369` thực thể file Java (`COMPLETE` / `NO_FUNCTIONS` / `LINEAGE_AMBIGUOUS`) và kiểm chứng đồng nhất trên PostgreSQL 18.3 |
| [Báo cáo Benchmark hiệu năng (`STT 32` & `NFR03`)](benchmark-report.md) | Kết quả đo 3 lượt/mốc ở quy mô `1.000 / 5.000 / 10.000` commit (`48.96 s` TB / `73.25 s` CLI <= `600 s`) kèm thời gian lưu trữ giao dịch, đánh giá cảnh báo và so sánh trên PostgreSQL 18.3 (`50.08..63.34 ms`) |
| [Giao thức khảo sát người dùng RQ3 (`STT 33`)](rq3-user-study-protocol.md) | Đề bài 7 tác vụ chuẩn (`T1..T7`), thang đo SUS 10 câu (Brooke, 1996) và hướng dẫn chấm điểm bằng `evaluate_rq3_sus.py` |
| [Phiếu khảo sát RQ3 gửi người tham gia](mau-khao-sat-rq3-gui-nguoi-tham-gia.txt) | Bản văn bản phẳng để in hoặc gửi cho người tham gia thực nghiệm kèm [mẫu CSV trống](rq3-response-template.csv) |
| [Tổng hợp kết quả, So sánh công cụ & Threats to Validity (`STT 35`)](synthesis-and-threats.md) | Tổng hợp định lượng `RQ1`, `RQ2`, `NFR03`, `NFR08`; bảng đối chiếu với SonarQube, CodeScene, Code Maat, CodeCharta và phân tích 4 nhóm *Threats to Validity* |
| [Tài liệu lý thuyết và áp dụng (Toản)](ly-thuyet-va-ap-dung.md) | 5 nguồn lý thuyết chính thức (JaCoCo bytecode counters, Decimal vs IEEE-754, Git fast-import/JGit DAG, Brooke 1996 SUS, Wohlin et al. Threats to Validity) và câu hỏi tự kiểm tra vấn đáp |
| [Nhật ký sử dụng AI Tuần 10 (Toản)](ai-usage-toan-week10.md) | Nhật ký học thuật theo từng hoạt động, ghi rõ 3 lỗi AI thực tế phát hiện qua kiểm thử và cách khắc phục |
| [Hướng dẫn chạy lại (Runbook)](runbook.md) | Lệnh tái lập 37 unit test Python, kịch bản RQ2, benchmark 10.000 commit, 122 JUnit test + JaCoCo trên PostgreSQL 18.3 và Playwright E2E |
| [Biên bản kiểm chứng (`verification.md`)](evidence/verification.md) | Tổng hợp số liệu kiểm chứng: [122 backend tests](evidence/backend-tests.json), [JaCoCo full-stack 89.07%](evidence/jacoco-fullstack-coverage.json), [JaCoCo non-DB 64.06%](evidence/jacoco-coverage-summary.json), [benchmark lõi](evidence/benchmark-results.json), [benchmark PostgreSQL](evidence/postgres-persistence-benchmark.json), [kịch bản RQ2](evidence/rq2-scenarios/rq2-scenarios-summary.json), [phân rã độ phủ RQ2](evidence/rq2-coverage-audit.json) |
| [Bàn giao cho Tưởng](ban-giao-tuong.md) & [Nội dung PR](noi-dung-pr.md) | Tóm tắt bàn giao nhánh `feat/toan-week-10` và mô tả Pull Request |
