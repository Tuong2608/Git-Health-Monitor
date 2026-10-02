> Bản bàn giao trước tích hợp, được giữ để truy vết. Trạng thái cập nhật ngày 02/10 và PR/CI xem [hồ sơ hiện hành](../week-03/README.md). Các dòng “chưa commit/chưa học” bên dưới phản ánh thời điểm bản cũ.

# Git, AI log và minh chứng tuần 3

## Điều đã kiểm tra

Nhánh local `feat/toan-week-3`, base `e99a27b`. Lịch sử lấy từ repo có commit tuần 1 và tuần 2; bảy commit hiện thấy đều mang tên tác giả `Tuong2608`. Điều này chỉ nói về metadata Git, chưa chứng minh Toản không tham gia ngoài Git.

Bản bàn giao chưa tạo commit/push/PR, không giả SHA, thời điểm hay tác giả để hợp thức hóa điểm. Thay đổi AI tạo được ghi trong AI log; Toản cần review và commit thật bằng tài khoản của mình nếu tiếp nhận.

## Cách đưa vào quy trình nhóm

1. Xem `git diff` và file mới; đọc README, tự chạy phần có liên quan.
2. Kiểm tra `git config user.name` và `git config user.email` trên máy. Chỉ dùng danh tính/email GitHub của chính mình, không sao chép danh tính Tưởng.
3. Chia commit theo thay đổi có ý nghĩa, ví dụ backend/API, frontend, CI, tài liệu. Không cố tạo số lượng commit.
4. Cập nhật AI log với SHA thật sau khi commit; có thể thêm mapping trong commit tài liệu tiếp theo để tránh tự tham chiếu hash của chính commit.
5. Push nhánh lên remote khi có quyền, mở PR theo `.github/pull_request_template.md`, nhờ Tưởng review; không tự ghi review đã xảy ra.
6. Merge khi kiểm tra đạt và cấu hình DB staging đã chuẩn bị. Thu minh chứng CI/deploy thực tế.

Gợi ý commit message (không phải lịch sử đã tạo):

- `feat(repository): add PostgreSQL registration API and validation`
- `feat(frontend): add repository screen and labelled chart exercise`
- `ci: verify backend with PostgreSQL and frontend browser checks`
- `docs: add Toan week 3 study notes, interview kit and AI log`

## Checklist cuối tuần cá nhân

- [ ] Có đóng góp thật, có giải thích; PR được người còn lại review.
- [ ] Liên kết việc → file/PR → test → báo cáo.
- [ ] AI log ghi prompt/phạm vi/output/kiểm chứng/phần tự sửa, không ghi hộ việc chưa làm.
- [ ] Nếu có lỗi AI, giữ test hoặc mô tả tái hiện + bản sửa; phân biệt AI tự sửa với lỗi Toản tự phát hiện.
- [ ] Báo cáo tuần gửi GVHD/web khoa, lưu xác nhận; file viết sẵn chưa phải bằng chứng đã nộp.
- [ ] Lý do thay đổi thiết kế/lịch được nhóm và GVHD xem xét khi cần.

Rubric tham chiếu: ≥90% tuần có commit (12 tuần cần ít nhất 11); ≥90% thay đổi qua PR có review; không có số commit tối thiểu mỗi ngày. Lịch sử của một người không thay thế minh chứng đóng góp cá nhân người khác. Chốt cách tính mẫu số với GVHD.

## Rà soát với Excel

Đề xuất cập nhật các việc STT 6 (tìm hiểu công nghệ), 7 (phỏng vấn), 13 (skeleton/CI/staging), 14 (phạm vi AI), 2 (AI Usage Log). Chỉ đánh dấu tương ứng với phần đã nghiệm thu; khảo sát đã có 3 bộ trả lời nhưng còn thiếu metadata/câu trả lời, staging frontend còn bảo vệ, phạm vi AI mới là nháp. Không tự đánh dấu các việc chung hoàn thành vì riêng mã Toản đã có.

Không sửa hai workbook/đề cương trong phiên này để tránh ghi đè lịch và quyết định chưa được nhóm duyệt. Bản Markdown phân công là đề xuất công việc, không phải lệnh tự động thực hiện mọi phần của Tưởng.
