# Bàn giao tuần 7 — Trần Quang Toản

MSSV 23110158. Tuần 25–31/10/2026, thực hiện ngày 29/10/2026. Base main `0b664d9` đã merge PR #5 tuần 6. Nhánh làm việc `feat/toan-week-7`.

Theo workbook: Toản hoàn thiện lưu trữ/giám sát (task 23, tuần 6–7) và bắt đầu tầng API (task 24, tuần 7–8). Bộ này bổ sung API đọc dữ liệu, retry job, capability, migration V3 để lưu metric chưa đo được và kiểm thử HTTP/worker/PostgreSQL.

| Sản phẩm | Tệp |
|---|---|
| Báo cáo tuần 7 | [Báo cáo](bao-cao-tuan-07-toan.md) |
| Phân công và phạm vi | [Đối chiếu](assignment-check.md) |
| API cho frontend | [API và ví dụ](api-week7.md), [request mẫu](requests.http) |
| Nối module của Tưởng | [Hợp đồng và điểm đã xử lý](integration-contract.md) |
| Minh chứng thực thi | [Kiểm thử](evidence/verification.md), HTTP exchanges, migration và test summaries |
| AI usage log | [Log theo hạng mục](ai-usage-toan-week07.md) |
| Gửi nhóm trưởng | [Bàn giao](ban-giao-tuong.md), [tin nhắn](tin-nhan-gui-tuong.txt) |

**Đã kiểm tra:** 58 tests trên nhánh Toản; 88 tests trên checkout thử gồm nhánh Tưởng `15f291b` và phần backend tuần 7 của Toản. Không cộng hai con số để thành 146 test độc lập. Migration V3 giữ dữ liệu V1/V2, 17 test mới tuần 7, HTTP evidence có nhãn TEST_FIXTURE_NOT_REAL_GIT.

**Giới hạn:** chưa có adapter Git/metric thật trên main, không triển khai LLM/CCN thay Tưởng, không thêm chức năng frontend, chưa deploy staging. Các ca HTTP dùng scheduler/worker/database thật với adapter chỉ nằm trong test. Việc chốt thực nghiệm từ tuần 6 vẫn cần hai thành viên; không tự điền nhãn hoặc kết quả nghiên cứu.

**Bảo trì CI:** cập nhật riêng dependency phát triển zod 4.6.5 → 4.6.7 trong lockfile sau cảnh báo mức high. Audit còn 0 cảnh báo; lint/build đạt, 3 UI tests đạt và 1 live test bị bỏ qua do không bật môi trường live. Xem [minh chứng](evidence/frontend-verification.md).

Mã mới: [SnapshotQueries](../../backend/src/main/java/com/example/demo/monitoring/SnapshotQueries.java), [Controller](../../backend/src/main/java/com/example/demo/monitoring/SnapshotQueryController.java), [V3](../../backend/src/main/resources/db/migration/V3__unavailable_metrics_and_retry_provenance.sql), [tests API](../../backend/src/test/java/com/example/demo/monitoring/Week7ApiTests.java), [tests worker HTTP](../../backend/src/test/java/com/example/demo/monitoring/Week7WorkerHttpTests.java).
