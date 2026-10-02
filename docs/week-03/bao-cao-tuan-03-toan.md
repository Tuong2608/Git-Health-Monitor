# Báo cáo tiến độ tuần 3 của Trần Quang Toản

**Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git.

**Sinh viên:** Trần Quang Toản — MSSV 23110158. **Nhóm:** Toản và Trần Văn Tưởng.

**Tuần báo cáo:** 27/09–03/10/2026. **Thời điểm cập nhật:** 02/10/2026.

**Trạng thái:** Bản báo cáo theo việc đã có minh chứng đến ngày cập nhật, chưa nộp/chưa có xác nhận GVHD. Ngày 02/10, Toản xác nhận đã đọc phần lý thuyết và trao đổi xong với Tưởng; phần Tưởng được Toản xác nhận đã làm xong. Kiểm thử kỹ thuật dưới đây do AI thực thi theo yêu cầu, không quy thành Toản tự chạy. Trạng thái Git/CI mới nhất xem [biên bản hoàn tất](bien-ban-hoan-tat-02-10.md).

## 1. Mục tiêu tuần

Tìm hiểu Spring Boot REST, PostgreSQL/JPA/Flyway, React/TypeScript và Recharts qua một luồng thực hành nhỏ của dự án; chuẩn bị khảo sát stakeholder; bổ sung contract, kiểm thử và minh chứng CI/staging; ghi AI Usage Log có thể đối chiếu mã nguồn.

Đã có skeleton backend/frontend, CI và Dockerfile từ tuần 2 ở repo base `e99a27b`. Công việc tuần này mở rộng phần có sẵn, không nhận là tạo mới toàn bộ skeleton.

## 2. Kết quả đã thực hiện với hỗ trợ AI

| Nội dung | Kết quả/tài liệu | Giới hạn |
|---|---|---|
| Tìm nguồn và tổng hợp lý thuyết | Tài liệu text gồm 14 nguồn chính thức, liên hệ từng công nghệ với mã dự án, bài thực hành và câu hỏi | Toản xác nhận đã đọc ngày 02/10; chưa có bản diễn giải độc lập từng chủ đề |
| Backend REST và PostgreSQL | API đăng ký/danh sách/chi tiết repository; DTO, service, JPA; migration Flyway V1; validation/chống trùng/phân trang/CORS | Chỉ lưu URL, chưa clone và chưa xác nhận repo công khai |
| Frontend | Màn thêm/xem repo; trạng thái tải/rỗng/lỗi, thông báo; layout desktop/mobile | Chưa có các màn phân tích thật |
| Recharts | Biểu đồ đường 4 điểm mẫu, trục 0–1, bảng số liệu, nhãn minh họa | Không sử dụng làm kết quả RQ2 |
| Test và kiểm soát mã | Backend verify 22 test pass; browser 4 pass gồm live PostgreSQL; lint/build/npm audit đạt; đã có CI remote đạt | Chưa có coverage/static report Java; test browser live chạy local, CI chạy 3 UI test mock và bỏ qua ca live theo cấu hình |
| Phỏng vấn | Đã tích hợp 5 trường hợp S01–S05, gồm hai trường hợp từ Tưởng tại `3a3fd30`, lập [báo cáo tổng hợp](../interview-notes.md), nhóm nhu cầu và đề xuất yêu cầu | S02 thiếu câu 10–12; ngày/hình thức thu thập và số liệu trong ngoặc vuông của S03 cần xác nhận |
| Yêu cầu/API/AI | Contract tuần 3 có 8 AC; nháp job contract; nháp phạm vi AI review | Chưa chốt SRS/provider/thiết kế chung |
| CI/hạ tầng | Workflow bổ sung PostgreSQL service, npm ci/lint/audit/browser, secret scan, Docker build; Compose local | CI `36979524926` đạt backend/frontend/secret-scan/Docker build; staging backend mới chưa deploy |
| Báo cáo và AI log | Log nêu rõ phạm vi AI sinh, lỗi, cách kiểm chứng và phần chờ sinh viên | Đã commit mã; PR/CI được cập nhật trong biên bản hoàn tất; review độc lập chưa xác nhận |

## 3. Nội dung kiến thức đã được tổng hợp để học

1. REST và phân lớp: controller xử lý HTTP, service điều phối, repository truy cập dữ liệu. HTTP 201 khác 202; tạo repository không đồng nghĩa phân tích xong.
2. PostgreSQL: khóa chính/UNIQUE/NOT NULL bảo vệ dữ liệu; transaction giữ nhất quán. Chuẩn hóa URL trước UNIQUE và vẫn cần constraint để chống request đồng thời.
3. Flyway: schema có phiên bản, dùng migration và Hibernate validate; tránh tự sửa bảng không có lịch sử.
4. React: quản lý trạng thái UI riêng với dữ liệu server; cleanup request, timeout và thông báo đúng loại lỗi. Cần phân biệt dữ liệu rỗng với mất kết nối.
5. Recharts: chọn trục và nhãn đúng nghĩa; dữ liệu mẫu phải tách khỏi kết quả phân tích thật.
6. Môi trường triển khai: biến VITE_* được đưa ra client; không chứa secret. Origin localhost khác 127.0.0.1; CORS không phải xác thực.
7. Kiểm thử và Git: test mock khác test tích hợp thật, build khác test; PR cần review của người khác và log phải trỏ tới SHA thật.

Chi tiết, nguồn, câu hỏi tự kiểm tra và chỗ áp dụng: [ly-thuyet-va-ap-dung-toan.txt](ly-thuyet-va-ap-dung-toan.txt).

**Ghi nhận việc học của Toản:**

- Toản xác nhận ngày 02/10 đã đọc phần lý thuyết được bàn giao. Phạm vi tài liệu gồm REST, JPA/PostgreSQL/Flyway, React/TypeScript, Recharts, CORS và kiểm thử/Git.
- Kiểm thử ngày 02/10 do AI thực hiện; chưa ghi nhận Toản tự chạy lại.
- Chưa có ghi nhận kiểm tra khả năng giải thích độc lập; không suy diễn việc đã đọc thành đã làm chủ.
- Các câu hỏi còn vướng của Toản: chưa cung cấp.

Không đổi tiêu đề phần này thành “đã làm chủ toàn bộ” nếu mới tiếp nhận tài liệu AI.

## 4. AI sử dụng và kiểm soát

Công cụ: Codex. AI hỗ trợ đọc repo/tài liệu, tìm nguồn, viết phần code và tài liệu mới, chạy kiểm thử, sửa lỗi phát hiện trong quá trình chạy và soạn báo cáo.

Các vấn đề thực tế đã ghi nhận: mất error cause bị ESLint bắt; CORS không khớp origin làm test live thất bại; thông báo 403 suy diễn nguyên nhân sai. AI đã sửa và chạy lại. Đây **chưa phải** minh chứng Toản tự phát hiện lỗi AI; Toản cần thực hiện review độc lập và bổ sung phần tự sửa/kiểm chứng của mình.

Báo cáo phỏng vấn được tổng hợp từ câu trả lời Toản cung cấp; câu thiếu được ghi rõ. Không tạo số liệu SUS, commit hoặc review giả. Nguồn tham khảo chính thức được liên kết, secret không đưa vào frontend/repo. Log đầy đủ: [toan-week03.md](../ai-usage/toan-week03.md).

## 5. Minh chứng kỹ thuật và staging

- Nhánh: `feat/toan-week-3`; đã tích hợp cập nhật Tưởng `43d614f`; commit chức năng: `165df3376182602d84607984b267d41cec15156e`. Xem biên bản hoàn tất để lấy PR/CI.
- Backend: `mvnw verify` trên PostgreSQL 18.3 thật, 22 test pass, 0 lỗi/skip; Flyway V1 success.
- Frontend: lint/build pass, npm audit báo 0 vulnerabilities tại lần kiểm tra; Playwright 4 pass, trong đó 1 live luồng browser→API→database và reload.
- Ảnh desktop/mobile và tổng hợp: [kiem-thu-minh-chung.md](kiem-thu-minh-chung.md), thư mục `evidence/`.
- Render https://git-health-monitor.onrender.com/api/health trả HTTP 200 và OK khi kiểm tra ngày 30/09.
- Ngày 02/10, URL Vercel vẫn chuyển sang đăng nhập; backend `/api/repositories` trả 404. Chưa xác nhận frontend công khai hoặc bản mới trên staging. Mã mới chưa được deploy. Xem [staging-week3.md](staging-week3.md).

## 6. Vướng mắc và điều chỉnh có lý do

- Chuyển từ học tutorial rời rạc sang luồng nhỏ trên chính repo để có thể hiểu và kiểm thử sự liên kết frontend/API/database.
- Giữ Boot 4.1.1 đang dùng trong repo; đề cương ghi Boot 3 nên cần nhóm xác nhận khác biệt, không đổi major version âm thầm.
- Đã tiếp nhận 3 trường hợp từ Toản và 2 trường hợp từ bản tổng hợp của Tưởng. Cần bổ sung câu 10–12 của S02, xác nhận thông tin còn thiếu và review diễn giải. Nhu cầu cảnh báo file bị bỏ sót trong PR vượt phạm vi snapshot hiện tại, chưa đưa vào cam kết.
- Staging frontend chưa truy cập được từ bên ngoài không đăng nhập; cần chủ account cung cấp domain phù hợp. Backend mới cần DB/env trước khi merge nếu auto-deploy đang bật.
- Docker Engine chưa chạy trong môi trường kiểm tra. Đã dùng PostgreSQL cài sẵn với database riêng để kiểm chứng; Docker build và Gitleaks đã đạt trên GitHub Actions, không chạy local.
- Chưa có pipeline ingestion, nên không đo NFR03 10.000 commit/10 phút bằng luồng CRUD. Phối hợp Tưởng cho benchmark đúng phạm vi.

## 7. Phần cần hoàn tất đến cuối tuần 3

- [x] Toản xác nhận đã đọc phần lý thuyết ngày 02/10.
- [x] AI chạy lại kiểm thử local: 22 backend và 4 browser test đạt. Không nhận là Toản tự chạy.
- [x] Hợp nhất 5 trường hợp stakeholder, cập nhật chủ đề N08 và giữ quan điểm hoài nghi.
- [x] Tiếp nhận bản tổng hợp hai người S04–S05 từ Tưởng tại `3a3fd30`.
- [ ] Metadata và những câu trả lời còn thiếu giữ nguyên trạng thái, cần bổ sung khi có nguồn.
- [x] Toản xác nhận đã trao đổi với Tưởng; đã tích hợp ghi chú nghiên cứu tuần 3 của Tưởng.
- [ ] Review PR độc lập và quyết định chi tiết còn mở được lưu thành minh chứng.
- [x] Commit và push nhánh; [PR #1](https://github.com/Tuong2608/Git-Health-Monitor/pull/1); [CI đạt](https://github.com/Tuong2608/Git-Health-Monitor/actions/runs/36979524926).
- [ ] Người khác review PR trên GitHub; chưa có approval.
- [ ] Cấu hình database/env cho staging, frontend domain phù hợp; kiểm tra và lưu deploy evidence của bản mới.
- [ ] Báo cáo tuần cho GVHD/web khoa; lưu xác nhận và góp ý.

## 8. Kế hoạch tuần 4

Hoàn thiện SRS/acceptance criteria từ phản hồi thật; chốt API contract với request/response/lỗi; thiết kế ERD ban đầu và từ điển dữ liệu; cụ thể hóa NFR/cách đo; review kiến trúc/job contract và AI scope với Tưởng. Tiếp tục test/PR/deploy theo phần đã xong, cập nhật log AI. Đặt lịch chuẩn bị bản cam kết sản phẩm/metric để hoàn tất trước khi qua nửa kỳ.

## 9. Xác nhận trước khi nộp

Ngày Toản rà soát: __________
Phần Toản tự sửa/bổ sung: __________
PR/SHA và người review: __________
Ngày báo cáo, góp ý/xác nhận GVHD: __________
