# Nhật ký AI hỗ trợ — Tưởng tuần 8

Ngày 06/11/2026. Tiếp thu nhận xét tuần 7 của GVHD ("Hạn chế dùng AI tạo"), trong tuần 8 sinh viên chủ động thiết kế và viết mã cho `HttpLlmProviderAdapter`, chỉ sử dụng AI ở mức hỗ trợ tra cứu API thư viện chuẩn và rà soát kiểm thử.

1. **Phạm vi hỗ trợ**:
   - Tra cứu cách sử dụng `HttpResponse.BodyHandlers.ofInputStream()` trong Java 21 `HttpClient` và cách dựng `com.sun.net.httpserver.HttpServer` lắng nghe cổng động cục bộ (`127.0.0.1:0`) để kiểm thử các tình huống mạng (timeout, phản hồi vượt giới hạn byte).
   - Rà soát chính tả và trình bày ngắn gọn phần điểm mới tuần 8 trong báo cáo.
2. **Phần sinh viên trực tiếp thực hiện và kiểm chứng**:
   - Thiết kế cấu hình `HttpLlmProviderAdapter.Config` (bắt buộc HTTPS hoặc loopback HTTP, kiểm tra khóa API khác rỗng, ẩn khóa trong `toString()`, đăng ký `@Bean` có điều kiện `app.ai-review.provider.enabled=true`).
   - Cài đặt logic trừ thời gian dự trữ validation (`300 ms`) khỏi `budget.deadline()`, đọc luồng phản hồi theo khối `4 KiB` và ngắt ngay khi vượt `maxOutputBytes + 4 KiB`.
   - Chạy kiểm chứng bộ 45/45 ca kiểm thử backend không cần DB và 8/8 ca kiểm thử công cụ thực nghiệm trên máy cá nhân, đối chiếu với kết quả CI của nhóm.
