# Draft danh sách Use Case chính — Tuần 2

> Trạng thái: bản nháp đầu tiên, chưa đặc tả chi tiết (precondition/main flow/exception). Sẽ hoàn thiện ở M2 (tuần 4) theo kế hoạch tiến độ, cùng với acceptance criteria.
> Cơ sở: rút ra từ kết luận khảo sát công cụ hiện có (`competitor-matrix.md`).

## Nhóm 1 — Phân tích & giám sát (chức năng lõi)

| # | Use case | Mô tả ngắn |
|---|---|---|
| UC1 | Thêm/đăng ký repository | Người dùng nhập URL kho mã nguồn Git để hệ thống bắt đầu theo dõi |
| UC2 | Phân tích repository | Hệ thống clone/fetch, trích xuất lịch sử commit và tính các chỉ số (change frequency, complexity, hotspot score, temporal coupling, ownership concentration) |
| UC3 | Xem bản đồ hotspot | Người dùng xem danh sách/bản đồ trực quan các file rủi ro cao nhất |
| UC4 | Xem xu hướng theo thời gian | Người dùng so sánh chỉ số của 1 file/module giữa nhiều lần quét (snapshot) |
| UC5 | Xem temporal coupling | Người dùng xem các cặp file thường bị sửa cùng nhau dù không liên quan rõ ràng trong code |
| UC6 | Xem ownership concentration | Người dùng xem mức độ tập trung "sở hữu" của từng file vào 1-2 người |

## Nhóm 2 — Giám sát định kỳ & cảnh báo

| # | Use case | Mô tả ngắn |
|---|---|---|
| UC7 | Cấu hình lịch quét định kỳ | Người dùng đặt tần suất hệ thống tự động quét lại repository |
| UC8 | Cấu hình ngưỡng cảnh báo | Người dùng đặt ngưỡng chỉ số để hệ thống sinh cảnh báo khi vượt |
| UC9 | Nhận và xem cảnh báo | Người dùng xem danh sách cảnh báo đã phát sinh, kèm lý do |

## Nhóm 3 — Trợ lý AI review (khác biệt so với các công cụ đã khảo sát)

| # | Use case | Mô tả ngắn |
|---|---|---|
| UC10 | Yêu cầu AI giải thích rủi ro của 1 file | Người dùng chọn 1 file, hệ thống gọi LLM để giải thích lý do file này rủi ro, dựa trên chỉ số + thay đổi gần đây |
| UC11 | Nhận gợi ý checklist review từ AI | Hệ thống gợi ý các điểm cần chú ý khi review file/thay đổi đó (không tự động sửa code) |

## Ghi chú khi hoàn thiện ở M2
- Mỗi use case cần thêm: actor, precondition, main flow, alternate/exception flow, acceptance criteria (theo yêu cầu rubric TC2.1 — 100% use case cốt lõi phải có acceptance criteria để đạt Mức 5).
- Cần rà lại xem UC nào thuộc "chức năng cam kết bắt buộc" và UC nào là "mở rộng nếu còn thời gian", tránh cam kết quá tải so với 12 tuần thực hiện.
