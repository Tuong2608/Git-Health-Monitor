# Nhật ký AI hỗ trợ tài liệu tuần 4 của Tưởng

- Sinh viên phụ trách: Trần Văn Tưởng, MSSV 23110170.
- Ngày phiên hỗ trợ: 08/10/2026; tuần 04–10/10/2026.
- Công cụ: Codex desktop; không ghi phiên bản mô hình cụ thể chưa xác nhận.
- Source cơ sở: 6560cb1; tài liệu local, chưa có commit/PR mới trong đợt này.

## Đầu vào và yêu cầu

Sinh viên yêu cầu làm phần tuần 4: kiến trúc do Tưởng dẫn dắt, cùng Toản chuẩn bị ADR/AI scope. AI đọc phân công được cung cấp, README/spec/AI scope/use cases/contract tuần 3, controller/service/UI/API client, cấu hình application/Compose/CI. Đối chiếu nguồn chính thức Spring task scheduling, PostgreSQL locking, Git clone.

## AI thực hiện

- Soạn [architecture](../architecture.md), tách hiện trạng/mục tiêu, Mermaid và DTO dự kiến.
- Soạn [ADR 001](../adr/001-full-clone.md), [ADR 002](../adr/002-no-redis.md), alternatives/lý do/hệ quả/kiểm chứng.
- Mở rộng [AI scope](../ai-review-scope.md), limits/schema/lỗi; không chọn provider/chạy PoC.
- Tạo [bàn giao](../week-04/README.md), chỉ phần cần tự review và việc Toản dẫn dắt.
- Kiểm tra link nội bộ/whitespace; không chạy test ứng dụng vì chỉ sửa tài liệu.

## Giới hạn ghi nhận

Không xác nhận Tưởng đã tự hiểu thiết kế, Toản đồng ý, nhóm họp, GVHD duyệt hoặc ADR Accepted. Tham số đề xuất không phải benchmark; không ghi thành cài pipeline/deploy/test thành công. Excel chưa cập nhật hoàn thành.

## Sinh viên tự thực hiện sau phiên

Chưa có bằng chứng review độc lập cung cấp trong phiên. Tưởng cần tự giải thích sơ đồ/job, đối chiếu spec, nhận xét AI limits và ghi thay đổi do mình quyết định. Sau review Toản, bổ sung ngày thật, kết luận và commit/PR thật. Không điền dữ liệu giả.

## Phiên đối chiếu ngày 09/10/2026

Người dùng cung cấp ZIP Toản và yêu cầu kiểm tra/sửa chồng chéo. AI đọc hai Word trong ZIP, đối chiếu origin/docs/toan-week-4 tại 33fefdd, mô phỏng merge README/AI scope, tạo checkout tích hợp riêng. AI căn chỉnh schema/API/budget theo Toản, bổ sung kiến trúc Tưởng, sửa identity NO_CHANGE và diễn đạt lease/heartbeat. Schema/fixture/link là kiểm tra tĩnh, không phải runtime pipeline.

Không gửi tin/comment/approval PR, push hoặc merge remote. Không xác nhận hai thành viên đồng thuận thay họ; Word/ZIP gốc giữ nguyên. Xem review tích hợp để biết phạm vi kiểm tra thực tế.


## Bàn giao nhánh theo yêu cầu ngày 09/10/2026

Sau phiên đối chiếu, người dùng yêu cầu push bản tích hợp. Chuẩn bị commit 12 tài liệu trên nhánh codex/week4-reconcile dựa trên 33fefdd; kiểm tra lại remote và whitespace trước khi push. Đây là yêu cầu xuất bản nhánh để review, không phải bằng chứng nhóm duyệt thiết kế hoặc CI đạt.
