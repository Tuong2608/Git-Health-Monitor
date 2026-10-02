## Thay đổi

Skeleton tuần 2 chưa có luồng lưu repository. PR bổ sung API đăng ký/danh sách/chi tiết với PostgreSQL, JPA và Flyway; UI React có trạng thái tải/rỗng/lỗi, chống trùng URL và biểu đồ Recharts gắn nhãn dữ liệu mẫu. URL chỉ được kiểm tra cú pháp, chưa clone hoặc phân tích Git.

Kèm contract tuần 3, 8 acceptance criteria, tài liệu lý thuyết, báo cáo phỏng vấn 3 người, báo cáo cá nhân, nhật ký AI theo hoạt động và minh chứng. Đã tích hợp nghiên cứu tuần 3 của Tưởng tại `43d614f`.

## Kiểm chứng ngày 02/10/2026

- Maven verify: 22 tests, 0 failures/errors/skipped, PostgreSQL thật.
- Playwright: 4 passed, gồm lưu dữ liệu và tải lại qua backend thật; kiểm tra mobile 375px.
- Frontend lint/build đạt; npm audit: 0 vulnerabilities.
- Kiểm thử local do AI thực thi theo yêu cầu Toản; Toản xác nhận đã đọc lý thuyết và trao đổi với Tưởng. Không ghi kiểm thử là sinh viên tự thực hiện.
- CI trên GitHub kiểm tra backend, frontend, secret scan và Docker build; xem trạng thái checks của PR.

## Điều kiện trước merge

- Người khác review mã và contract; chưa có approval tại thời điểm tạo PR.
- Chủ staging xác nhận PostgreSQL và DB/env đã cấu hình; mặc định đăng ký tắt, chỉ bật cho phiên thử được kiểm soát.
- Backend staging hiện `/api/health` 200 nhưng `/api/repositories` 404; frontend URL vẫn chuyển sang Vercel login. Chưa tuyên bố triển khai bản mới.

## Giới hạn và bàn giao

Không triển khai ingestion, tính metric hay LLM trong PR này. Công thức coupling trong ghi chú mới khác đề cương; cần thống nhất trước cài đặt metric. Tưởng phụ trách hai phỏng vấn bổ sung và tổng hợp sau; dữ liệu chưa nhận không được thêm vào báo cáo. Xem `docs/week-03/bien-ban-hoan-tat-02-10.md` và `docs/ai-usage/toan-week03.md`.
