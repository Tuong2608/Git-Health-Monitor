# Bàn giao tuần 6 — Trần Quang Toản

MSSV 23110158. Tuần 18–24/10/2026, thực hiện 22/10/2026. Base main `188968c` (PR #4 đã merge). Nhánh `feat/toan-week-6`.

Đã cài phần lưu trữ/giám sát của task 23 (lịch tuần 6–7), kèm kiểm thử PostgreSQL thật. Phần thực nghiệm chung task 20 được bổ sung bản đối chiếu và công cụ tính chỉ số. Chưa có adapter ingestion/metrics của Tưởng trên main, vì vậy phân tích repository thật chưa hoạt động; runtime không cài mock thay thế và trả 503 khi thiếu adapter.

| Cần xem | Tài liệu |
|---|---|
| Báo cáo tuần | [Báo cáo](bao-cao-tuan-06-toan.md) |
| Gửi Tưởng và tích hợp | [Hướng dẫn bàn giao](ban-giao-tuong.md), [tin nhắn soạn sẵn](tin-nhan-gui-tuong.txt) |
| Phân công và phạm vi | [Đối chiếu](assignment-check.md) |
| Kiến trúc và API đã có | [Implementation](implementation.md), [API tuần 6](api-week6.md) |
| Thực nghiệm chung | [Protocol và điểm cần chốt](experiment-protocol.md), thư mục experiment/ |
| Minh chứng | [Kiểm thử](evidence/verification.md), JSON kết quả và log |
| AI log | [Nhật ký học thuật](ai-usage-toan-week06.md) |

Mã nguồn: [module monitoring](../../backend/src/main/java/com/example/demo/monitoring), [migration V2](../../backend/src/main/resources/db/migration/V2__snapshot_jobs_and_monitoring.sql), [tests PostgreSQL](../../backend/src/test/java/com/example/demo/monitoring), [evaluation tool](../../tools/experiments).

Các cờ ghi/worker mặc định tắt. Chỉ bật trong môi trường nhóm kiểm soát sau khi gắn adapter thật; chưa có auth/quota cho người dùng công khai. Giao diện không thay đổi; Vite được cập nhật bản vá 8.3.5 để xử lý audit chặn CI. Không tự nhận đã merge PR tuần 6, deploy staging, có nhãn nghiên cứu hoặc được Tưởng/GVHD nghiệm thu.
