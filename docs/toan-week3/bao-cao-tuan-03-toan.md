> Bản bàn giao trước tích hợp, được giữ để truy vết. Trạng thái cập nhật ngày 02/10 và PR/CI xem [hồ sơ hiện hành](../week-03/README.md). Các dòng “chưa commit/chưa học” bên dưới phản ánh thời điểm bản cũ.

# Báo cáo tiến độ tuần 3 của Trần Quang Toản

**Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git.

**Sinh viên:** Trần Quang Toản — MSSV 23110158. **Nhóm:** Toản và Trần Văn Tưởng.

**Tuần báo cáo:** 27/09–03/10/2026. **Thời điểm cập nhật:** 30/09/2026.

**Trạng thái:** Bản báo cáo theo việc đã có minh chứng đến ngày cập nhật, chưa nộp/chưa có xác nhận GVHD. Phần sau 30/09 cần bổ sung trước nộp cuối tuần. Nội dung và mã mới được AI hỗ trợ trực tiếp; chưa xác nhận sinh viên đã tự đọc và kiểm chứng toàn bộ.

## 1. Mục tiêu tuần

Tìm hiểu Spring Boot REST, PostgreSQL/JPA/Flyway, React/TypeScript và Recharts qua một luồng thực hành nhỏ của dự án; chuẩn bị khảo sát stakeholder; bổ sung contract, kiểm thử và minh chứng CI/staging; ghi AI Usage Log có thể đối chiếu mã nguồn.

Đã có skeleton backend/frontend, CI và Dockerfile từ tuần 2 ở repo base `e99a27b`. Công việc tuần này mở rộng phần có sẵn, không nhận là tạo mới toàn bộ skeleton.

## 2. Kết quả đã thực hiện với hỗ trợ AI

| Nội dung | Kết quả/tài liệu | Giới hạn |
|---|---|---|
| Tìm nguồn và tổng hợp lý thuyết | Tài liệu text gồm 14 nguồn chính thức, liên hệ từng công nghệ với mã dự án, bài thực hành và câu hỏi | Chờ Toản xác nhận phần đã tự học |
| Backend REST và PostgreSQL | API đăng ký/danh sách/chi tiết repository; DTO, service, JPA; migration Flyway V1; validation/chống trùng/phân trang/CORS | Chỉ lưu URL, chưa clone và chưa xác nhận repo công khai |
| Frontend | Màn thêm/xem repo; trạng thái tải/rỗng/lỗi, thông báo; layout desktop/mobile | Chưa có các màn phân tích thật |
| Recharts | Biểu đồ đường 4 điểm mẫu, trục 0–1, bảng số liệu, nhãn minh họa | Không sử dụng làm kết quả RQ2 |
| Test và kiểm soát mã | Backend verify 22 test pass; browser 4 pass gồm live PostgreSQL; lint/build/npm audit đạt | Chưa có coverage/static report Java và CI run remote |
| Phỏng vấn | Đã tiếp nhận 3 bộ trả lời S01–S03, lập [báo cáo tổng hợp](../interview-notes.md), nhóm nhu cầu và đề xuất yêu cầu | S02 thiếu câu 10–12; ngày/hình thức thu thập và số liệu trong ngoặc vuông của S03 cần xác nhận |
| Yêu cầu/API/AI | Contract tuần 3 có 8 AC; nháp job contract; nháp phạm vi AI review | Chưa chốt SRS/provider/thiết kế chung |
| CI/hạ tầng | Workflow bổ sung PostgreSQL service, npm ci/lint/audit/browser, secret scan, Docker build; Compose local | Cấu hình mới chưa chạy trên GitHub/Docker tại đây |
| Báo cáo và AI log | Log nêu rõ phạm vi AI sinh, lỗi, cách kiểm chứng và phần chờ sinh viên | Chưa có commit/PR/review thật của bản sửa |

## 3. Nội dung kiến thức đã được tổng hợp để học

1. REST và phân lớp: controller xử lý HTTP, service điều phối, repository truy cập dữ liệu. HTTP 201 khác 202; tạo repository không đồng nghĩa phân tích xong.
2. PostgreSQL: khóa chính/UNIQUE/NOT NULL bảo vệ dữ liệu; transaction giữ nhất quán. Chuẩn hóa URL trước UNIQUE và vẫn cần constraint để chống request đồng thời.
3. Flyway: schema có phiên bản, dùng migration và Hibernate validate; tránh tự sửa bảng không có lịch sử.
4. React: quản lý trạng thái UI riêng với dữ liệu server; cleanup request, timeout và thông báo đúng loại lỗi. Cần phân biệt dữ liệu rỗng với mất kết nối.
5. Recharts: chọn trục và nhãn đúng nghĩa; dữ liệu mẫu phải tách khỏi kết quả phân tích thật.
6. Môi trường triển khai: biến VITE_* được đưa ra client; không chứa secret. Origin localhost khác 127.0.0.1; CORS không phải xác thực.
7. Kiểm thử và Git: test mock khác test tích hợp thật, build khác test; PR cần review của người khác và log phải trỏ tới SHA thật.

Chi tiết, nguồn, câu hỏi tự kiểm tra và chỗ áp dụng: [ly-thuyet-va-ap-dung-toan.txt](ly-thuyet-va-ap-dung-toan.txt).

**Phần “Toản đã học được gì” cần tự xác nhận trước nộp:**

- Tài liệu thực sự đã đọc: chưa điền.
- Bài thực hành tự chạy và kết quả: chưa điền.
- Nội dung đã giải thích được không cần đọc lại: chưa điền.
- Nội dung còn chưa hiểu: chưa điền.

Không đổi tiêu đề phần này thành “đã làm chủ toàn bộ” nếu mới tiếp nhận tài liệu AI.

## 4. AI sử dụng và kiểm soát

Công cụ: Codex. AI hỗ trợ đọc repo/tài liệu, tìm nguồn, viết phần code và tài liệu mới, chạy kiểm thử, sửa lỗi phát hiện trong quá trình chạy và soạn báo cáo.

Các vấn đề thực tế đã ghi nhận: mất error cause bị ESLint bắt; CORS không khớp origin làm test live thất bại; thông báo 403 suy diễn nguyên nhân sai. AI đã sửa và chạy lại. Đây **chưa phải** minh chứng Toản tự phát hiện lỗi AI; Toản cần thực hiện review độc lập và bổ sung phần tự sửa/kiểm chứng của mình.

Báo cáo phỏng vấn được tổng hợp từ câu trả lời Toản cung cấp; câu thiếu được ghi rõ. Không tạo số liệu SUS, commit hoặc review giả. Nguồn tham khảo chính thức được liên kết, secret không đưa vào frontend/repo. Log đầy đủ: [toan-week03.md](../ai-usage/toan-week03.md).

## 5. Minh chứng kỹ thuật và staging

- Nhánh local: `feat/toan-week-3`; base commit: `e99a27b`. Chưa commit/push/PR.
- Backend: `mvnw verify` trên PostgreSQL 18.3 thật, 22 test pass, 0 lỗi/skip; Flyway V1 success.
- Frontend: lint/build pass, npm audit báo 0 vulnerabilities tại lần kiểm tra; Playwright 4 pass, trong đó 1 live luồng browser→API→database và reload.
- Ảnh desktop/mobile và tổng hợp: [kiem-thu-minh-chung.md](kiem-thu-minh-chung.md), thư mục `evidence/`.
- Render https://git-health-monitor.onrender.com/api/health trả HTTP 200 và OK khi kiểm tra ngày 30/09.
- URL Vercel do nhóm cung cấp chuyển sang đăng nhập; chưa xác nhận frontend công khai. Mã mới chưa được deploy. Xem [staging-week3.md](staging-week3.md).

## 6. Vướng mắc và điều chỉnh có lý do

- Chuyển từ học tutorial rời rạc sang luồng nhỏ trên chính repo để có thể hiểu và kiểm thử sự liên kết frontend/API/database.
- Giữ Boot 4.1.1 đang dùng trong repo; đề cương ghi Boot 3 nên cần nhóm xác nhận khác biệt, không đổi major version âm thầm.
- Đã tiếp nhận phản hồi của 3 stakeholder do Toản cung cấp. Cần bổ sung câu 10–12 của S02, xác nhận thông tin còn thiếu và review diễn giải. Nhu cầu cảnh báo file bị bỏ sót trong PR vượt phạm vi snapshot hiện tại, chưa đưa vào cam kết.
- Staging frontend chưa truy cập được từ bên ngoài không đăng nhập; cần chủ account cung cấp domain phù hợp. Backend mới cần DB/env trước khi merge nếu auto-deploy đang bật.
- Docker Engine chưa chạy trong môi trường kiểm tra. Đã dùng PostgreSQL cài sẵn với database riêng để kiểm chứng; chưa ghi Docker/Gitleaks đã đạt.
- Chưa có pipeline ingestion, nên không đo NFR03 10.000 commit/10 phút bằng luồng CRUD. Phối hợp Tưởng cho benchmark đúng phạm vi.

## 7. Phần cần hoàn tất đến cuối tuần 3

- [ ] Toản tự đọc tài liệu, chạy lại và ghi phần đã hiểu/chưa hiểu.
- [x] Tiếp nhận 3 bộ trả lời stakeholder và lập báo cáo tổng hợp nhu cầu.
- [ ] Bổ sung thông tin thu thập, câu 10–12 của S02, xác nhận số liệu S03; nhóm duyệt các yêu cầu đề xuất sau phỏng vấn.
- [ ] Tưởng review mã/contract/phạm vi AI; thống nhất số UC và ranh giới module.
- [ ] Commit, PR và review thật; thêm SHA/run URL vào AI log/báo cáo.
- [ ] Cấu hình database/env cho staging, frontend domain phù hợp; kiểm tra và lưu deploy evidence của bản mới.
- [ ] Báo cáo tuần cho GVHD/web khoa; lưu xác nhận và góp ý.

## 8. Kế hoạch tuần 4

Hoàn thiện SRS/acceptance criteria từ phản hồi thật; chốt API contract với request/response/lỗi; thiết kế ERD ban đầu và từ điển dữ liệu; cụ thể hóa NFR/cách đo; review kiến trúc/job contract và AI scope với Tưởng. Tiếp tục test/PR/deploy theo phần đã xong, cập nhật log AI. Đặt lịch chuẩn bị bản cam kết sản phẩm/metric để hoàn tất trước khi qua nửa kỳ.

## 9. Xác nhận trước khi nộp

Ngày Toản rà soát: __________
Phần Toản tự sửa/bổ sung: __________
PR/SHA và người review: __________
Ngày báo cáo, góp ý/xác nhận GVHD: __________
