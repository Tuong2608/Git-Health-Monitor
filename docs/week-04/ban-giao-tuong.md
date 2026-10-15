# Bàn giao tài liệu tuần 4 của Tưởng

Tuần 04–10/10/2026. Soạn 08/10/2026 trên `6560cb1`.

| Tài liệu | Nội dung đã soạn | Trạng thái |
|---|---|---|
| [Kiến trúc](../architecture.md) | Hiện trạng/mục tiêu, sơ đồ, module/job/DTO/snapshot | Đề xuất chờ review |
| [ADR 001](../adr/001-full-clone.md) | Full history và trade-off | Proposed |
| [ADR 002](../adr/002-no-redis.md) | TaskExecutor + PostgreSQL và recovery | Proposed |
| [AI scope](../ai-review-scope.md) | Input/output, limits, lỗi, API/UI, nghiệm thu | Nháp, chưa chọn provider |
| [AI Usage Log](../ai-usage/tuong-week04.md) | AI hỗ trợ và phần sinh viên tự review | Ghi nhận phiên này |

Use cases/business rules/NFR/API contract do Toản dẫn dắt đã có trên nhánh Toản; bản tích hợp 09/10 giữ tài liệu này và căn chỉnh phần giao nhau. Excel chưa đổi trạng thái để tránh đồng nhất “đã soạn nháp” với “nhóm đã review và hoàn thành”.

## Tưởng cần review và trình bày

1. Đọc architecture mục 1: bản mới có API/UI/DB, khác bản ôn tại 3a3fd30.
2. Tự giải thích 202 → QUEUED → worker → snapshot; lưu DB khác chạy thread.
3. Review full clone: phạm vi tính khác phạm vi tải, có trade-off tài nguyên.
4. Review không Redis: ít hạ tầng nhưng tự làm recovery/idempotency, chưa cài.
5. Cùng Toản thống nhất DTO/identity/job/schema/UI/AI limits. Ghi kết luận thật trước đổi ADR Accepted.

Lời trình bày sau tự review: “Tuần 4 em đã chuẩn bị thiết kế modular monolith, luồng bất đồng bộ và hợp đồng dữ liệu giữa Git/metric với API/lưu trữ. Em cũng chuẩn bị hai ADR và phạm vi AI cụ thể hơn. Đây là tài liệu thiết kế chờ nhóm review; pipeline chưa được cài đặt.”

## Các mục cần kết luận

- Identity theo SHA + window + configVersion; quy tắc cũ chỉ nhìn HEAD.
- Một job hoạt động/repo; recovery FAILED và retry job mới.
- Admission/poll/heartbeat/lease/timeout; chưa có thông số đo.
- Giới hạn repo/full clone, metric thiếu dữ liệu tuần 5.
- AI provider/tokenizer/ngân sách/retention/TTL/diff base/quyền truy cập.

## Ghi tiến độ sau review

Gợi ý: “08/10: đã có nháp architecture, ADR full clone/no Redis và AI scope; cần review với Toản; chưa cài pipeline.” Chỉ hoàn thành sau review thật. Đổi lịch/phân công thì cập nhật Excel và bản phân công cùng lý do. Không thêm ngày họp/lời duyệt chưa xảy ra.

## Phạm vi kiểm tra

Đối chiếu controller/service/React, DB/Compose/CI và metric spec. Kiểm tra link Markdown local/whitespace trước bàn giao. Không chạy lại ứng dụng, benchmark hoặc gọi provider; acceptance criteria là kiểm thử tương lai.

Bản riêng Tưởng 08/10 đã được bảo toàn khi đối chiếu. AI hiện theo schema/contract chung thay đề xuất async và budget cũ; xem [review tích hợp](review-tich-hop-tuong-toan.md). Chưa ghi nhận thành viên đã duyệt.
