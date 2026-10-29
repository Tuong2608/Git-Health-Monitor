# Nhật ký sử dụng AI — tuần 7

Sinh viên Trần Quang Toản, 23110158. Ngày 29/10/2026. Công cụ Codex. Tóm tắt yêu cầu theo mục tiêu, không chép prompt gốc hoặc tạo lại hội thoại. Không khai model/giờ công chưa xác minh.

## AI-07-01 — Đối chiếu kế hoạch và mã nhóm

**Mục tiêu:** làm đúng task 23/24, tránh nhận thay Git/metric/LLM của Tưởng. **Đầu vào:** workbook main0b664d9, bộ tuần 6, nhánh Tưởng 15f291b. **Hỗ trợ:** fetch, đọc code/tài liệu, trích các dòng phân công. **Đầu ra:** assignment-check và source JSON. **Kiểm chứng:** ghi SHA và dòng Excel, phân biệt nhánh chưa merge. **Giới hạn:** chưa có quyết định nhóm mới được xác nhận.

## AI-07-02 — Hoàn thiện lưu trữ

**Mục tiêu:** hỗ trợ metric không đo được khi ingestion báo binary/ambiguous lineage. **Hỗ trợ:** V3, nullable FileData, invariants contribution/hotspot/HHI, retryOf và claim-context checks. **Kiểm chứng:** tests null/zero/reason/negative/cross-repo/rollback, upgrade V1/V2. **Điều chỉnh:** nhận ra PostgreSQL CHECK chấp nhận UNKNOWN nên HHI cần kiểm tra churn IS NOT NULL tường minh. **Giới hạn:** không tự đo metric hoặc thay chính sách lineage của Tưởng.

## AI-07-03 — Tầng API cho dữ liệu snapshot

**Mục tiêu:** cung cấp dữ liệu đủ cho màn hình file/coupling/trend/comparison. **Hỗ trợ:** read-only queries/controllers, pagination, oriented pair counts, state/delta và readiness. **Kiểm chứng:** HTTP tests trên PostgreSQL thật, sai scope trả404, cấu hình/coverage khác trả409; null không đổi thành 0. **Giới hạn:** chưa frontend hoặc AI runtime; author identity key không được expose qua contribution endpoint.

## AI-07-04 — Luồng worker và kiểm tra tương thích

**Mục tiêu:** kiểm tra phối hợp API/job/scheduler/storage và khả năng cùng biên dịch với nhánh Tưởng. **Hỗ trợ:** test-only adapter, async HTTP cases, checkout compatibility riêng. **Kiểm chứng:** 58 tests nhánh Toản và88 tests checkout kết hợp đạt; HTTP request/response được ghi từ lần chạy. **Lỗi phát hiện:** fixture SQL thiếu timezone gây lệch 7 giờ, sửa literal UTC rồi chạy lại. **Giới hạn:** adapter fixture không phải phân tích Git thật; không gọi kết quả88 test là đã hoàn tất pipeline hoặc merge nhánh Tưởng.

## AI-07-05 — Báo cáo và bàn giao

**Mục tiêu:** báo cáo truy vết được đến sản phẩm/kiểm tra. **Hỗ trợ:** tổng hợp Surefire XML, log sạch, HTTP evidence, báo cáo/API guide/handoff/PR và ZIP. **Kiểm chứng:** số test lấy từ XML, liên kết nội bộ và trạng thái Git/CI khi push. **Trách nhiệm:** Toản tiếp nhận/giải thích, Tưởng review các điểm nối; không tự ghi đã học xong, nộp, có approval, phỏng vấn hoặc kết quả thực nghiệm.

## AI-07-06 — Xử lý lỗi kiểm tra dependency trên CI

**Mục tiêu:** khắc phục cảnh báo bảo mật chặn CI. **Đầu vào:** kết quả GitHub Actions và npm audit cho zod 4.6.5. **Hỗ trợ:** cập nhật riêng lockfile lên 4.6.7, cài lại và kiểm tra hồi quy. **Kiểm chứng:** audit 0 cảnh báo, lint/build đạt; 3 UI tests đạt, 1 live test bỏ qua. **Giới hạn:** không phát triển màn hình mới, không nhận ca live chưa chạy là đã đạt. Minh chứng tại evidence/frontend-verification.md.
