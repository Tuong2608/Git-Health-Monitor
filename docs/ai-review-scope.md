# Nháp phạm vi AI review cho thảo luận tuần 3

Ngày 30/09/2026. **Đề xuất để Toản và Tưởng review; chưa được nhóm/GVHD chốt, chưa cài đặt.**

- Chỉ gọi theo yêu cầu sau khi có snapshot thành công. Không nằm trong đường tính metric/hotspot; không dùng output LLM trong nhãn hoặc score thực nghiệm RQ1.
- Đầu vào: metric có nguồn, commit/snapshot id, tóm tắt thay đổi và phần source công khai người dùng chọn. Chặn secret và giới hạn kích thước context qua cấu hình; giá trị cụ thể chọn sau PoC.
- Đầu ra JSON dự kiến: `riskSummary`, `evidence[]`, `reviewChecklist[]`, `refactorSuggestions[]`. Evidence phải tham chiếu metric/đoạn code thật; không tin chỉ vì JSON parse được.
- Giao diện gắn nhãn AI-generated, cho biết giới hạn, không tự áp dụng refactor, tạo commit hay sửa source.
- Dùng abstraction `LLMReviewService`, adapter provider riêng, prompt có version, timeout/rate limit; CI dùng fake/mock. Provider/model chưa được chọn, không mặc định Claude.
- Provider lỗi, hết quota, response sai schema hoặc timeout: báo không thể review và cho thử lại có kiểm soát; dashboard/metric vẫn dùng được.
- Không lưu API key vào repo, frontend, AI log hoặc log server. Không gửi toàn repository.

## Toản cần thiết kế ở tuần 4

Trạng thái UI: chưa yêu cầu, đang xử lý, thành công, lỗi/timeout, context vượt giới hạn. API request/response và cách trình bày evidence do hai người thống nhất. Không biến gợi ý AI thành khẳng định file chắc chắn có bug.

## Quyết định còn mở

Provider/model, giới hạn token/byte, thời gian timeout, giới hạn lượt gọi, lưu hay không lưu nội dung review, cách chọn source tại đúng snapshot. Cần kiểm tra khả năng giải thích, chi phí và độ trễ bằng PoC của nhóm.
