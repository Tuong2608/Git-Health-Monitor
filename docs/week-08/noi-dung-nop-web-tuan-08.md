# Nội dung báo cáo tuần 8 — bản để Toản kiểm tra trước nộp

**Thời gian:** 01–07/11/2026. **Sinh viên:** Trần Quang Toản — 23110158.

**Công việc:** Hoàn thiện tầng API và giao diện React theo task 24/26; tham gia đóng gói/tích hợp theo task 27/28. Giao diện đã đọc dữ liệu snapshot thật, thể hiện hotspot, chi tiết file/coupling/evidence, xu hướng, so sánh, job, lịch giám sát và cảnh báo. Bổ sung API AI preview có consent gắn contextHash, đọc source theo SHA snapshot, audit metadata và xử lý timeout/output không hợp lệ. Tái sử dụng engine Git/metric/LLM của thành viên Tưởng.

**Kết quả:** 111 test backend duy nhất đạt (109 full verify + 2 availability), 11 UI tests đạt gồm 2 live cases. Phân tích qua HTTP và lưu PostgreSQL thành công trên gs-rest-service, spring-petclinic và Hello-World. Docker frontend/nginx và backend có Lizard được bổ sung; trạng thái build/Compose xem CI của PR tuần 8.

**Khó khăn và xử lý:** Sửa trailing JSON validation, thiếu Lizard trong môi trường đóng gói, test selector khi nhiều repository và bố cục bảng trên mobile. Giữ null/coverage ambiguity đúng dữ liệu, không dùng số 0 thay cho chưa đo.

**Còn lại:** Tưởng cấu hình provider thật; nhóm review/merge và cập nhật staging do URL cũ không còn đủ dùng. Docker local timeout; cần dựa vào CI/container evidence. Còn audit moderate ở tooling và các quyết định thiết kế thực nghiệm. Chưa gọi AI thương mại hoặc thực nghiệm người dùng.

**AI hỗ trợ:** phân tích mã, soạn/điều chỉnh implementation, tests và tài liệu; phạm vi, lỗi và kiểm chứng được ghi riêng trong AI usage log tuần 8. Sinh viên cần đọc và giải thích trước khi nộp. Tệp này chưa được tự gửi lên web khoa.
