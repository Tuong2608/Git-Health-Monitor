# Bộ bàn giao tuần 3 của Toản

Tuần 27/09–03/10/2026, cập nhật ngày 02/10. Nhánh `feat/toan-week-3` đã tích hợp `43d614f` của Tưởng. Xem [biên bản hoàn tất](bien-ban-hoan-tat-02-10.md) để lấy trạng thái Git/CI và những phần còn phụ thuộc staging/review.


Bắt đầu phần còn lại tại [kế hoạch và hướng dẫn từng bước](ke-hoach-hoan-tat-toan.md); ghi kết quả thật vào [phiếu tự thực hiện](nhat-ky-tu-thuc-hien-toan.md).

## Đọc và sử dụng theo thứ tự

1. [Tài liệu text lý thuyết và áp dụng](ly-thuyet-va-ap-dung-toan.txt): 14 nguồn chính thức, giải thích vai trò từng công nghệ/file, lý do chọn cách làm, bài thực hành và câu hỏi tự kiểm tra.
2. [Mẫu text gửi người phỏng vấn](mau-phong-van-gui-nguoi-tham-gia.txt): lời giới thiệu, đồng ý tham gia, câu hỏi tình huống, ưu tiên và phản biện. [Báo cáo phỏng vấn](../interview-notes.md) đã tổng hợp 5 trường hợp, có chỉ rõ thông tin còn thiếu.
3. [README chạy ứng dụng](../../README.md): thực hành luồng React → API → PostgreSQL và biểu đồ mẫu.
4. [Contract và acceptance criteria tuần 3](api-contract-week3.md): phân biệt endpoint có thật với job API dự kiến.
5. [Nháp phạm vi AI review](../ai-review-scope.md): đầu vào, đầu ra, kiểm soát lỗi và những quyết định chờ nhóm.
6. [Kiểm thử và minh chứng](kiem-thu-minh-chung.md): kết quả chạy thực tế, phạm vi và hạn chế.
7. [Báo cáo tuần](bao-cao-tuan-03-toan.md) và [AI Usage Log](../ai-usage/toan-week03.md): đã cập nhật xác nhận đọc lý thuyết; phân biệt kiểm thử do AI thực thi.
8. [Bàn giao staging](staging-week3.md), [Git và minh chứng](git-va-minh-chung.md): việc cần nhóm/account owner hoàn tất.

## Tiến độ theo từng đầu việc

| Việc | Đầu ra | Trạng thái chính xác |
|---|---|---|
| Tìm tài liệu Spring/PostgreSQL/React/Recharts | File text, nguồn, ví dụ trong repo | Toản xác nhận đã đọc ngày 02/10 |
| Bản thực hành tích hợp | API, migration, UI và tests | Đã cài local, xem báo cáo kiểm thử |
| Khảo sát stakeholder | Bộ câu hỏi + báo cáo 5 trường hợp S01–S05 | Đã có phản hồi; S02 thiếu 10–12, metadata thu thập và số liệu S03 cần xác nhận |
| Skeleton/CI/staging | Kế thừa skeleton; cải tiến workflow | CI backend/frontend/secret scan/Docker build đã đạt; frontend URL cũ cần đăng nhập |
| UC/contract | W3-AC01–08 và nháp job contract | Đã soạn, cần Tưởng review; chưa khóa SRS |
| Phạm vi AI review | Tài liệu nháp | Chờ nhóm chốt tuần 4 |
| NFR03 benchmark Git ≤10.000 commits | Ghi yêu cầu bàn giao | Chưa có ingestion/metric nên chưa đo; cần Tưởng phụ trách PoC |
| AI log/báo cáo | File hoàn chỉnh theo dữ liệu có thật | Chưa nộp web khoa hoặc được GVHD xác nhận |

## Toản còn phải làm bằng hành động thực tế

- Đã xác nhận đọc lý thuyết; phần giải thích độc lập/tự thực hành ghi theo hoạt động thực tế, không lấy test AI chạy làm minh chứng tự chạy.
- Hoàn thiện metadata của 5 phản hồi, bổ sung câu thiếu của S02, xác nhận số liệu S03 và cùng nhóm duyệt đề xuất cập nhật UC; giữ thông tin liên hệ ngoài repo.
- Cùng Tưởng review nhánh, commit/PR thật, thêm SHA vào log. Không lấy lịch sử của Tưởng làm đóng góp cá nhân của Toản.
- Chủ account chuẩn bị DB/env trước khi deploy backend mới; lấy frontend domain cho GVHD truy cập được; chạy CI/deploy và ghi minh chứng.
- Thống nhất lịch Excel/đề cương; bổ sung xác nhận báo cáo tuần và kế hoạch tuần 4.

Đã làm được phần tài liệu/mã và kiểm chứng local không có nghĩa mọi nghĩa vụ tuần 3 đã hoàn tất. Những mục phụ thuộc người thật/account được đánh dấu để bàn giao, không ghi thành kết quả giả.
