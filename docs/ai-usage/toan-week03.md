# Nhật ký sử dụng trí tuệ nhân tạo trong tuần 3

## 1. Thông tin chung

| Nội dung | Thông tin |
|---|---|
| Đề tài | Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git |
| Sinh viên | Trần Quang Toản — MSSV 23110158 |
| Thời gian báo cáo | 27/09–03/10/2026 |
| Ngày thực hiện các hoạt động được ghi nhận | 30/09 và 02/10/2026 |
| Công cụ hỗ trợ | Codex trên ứng dụng desktop; phiên bản mô hình chưa được ghi nhận |
| Kho mã nguồn | https://github.com/Tuong2608/Git-Health-Monitor |
| Phiên bản cơ sở | Ban đầu `e99a27b`; tích hợp nghiên cứu của Tưởng `43d614f` ngày 02/10 |
| Nhánh triển khai cục bộ | `feat/toan-week-3` |
| Trạng thái truy vết | Mã chức năng `165df3376182602d84607984b267d41cec15156e`; PR/CI xem [biên bản](../week-03/bien-ban-hoan-tat-02-10.md) |

Nhật ký được tổ chức theo từng hoạt động chuyên môn. Nội dung yêu cầu đối với AI được diễn giải thông qua mục tiêu và phạm vi hỗ trợ, không trích lại hội thoại gốc. Các hoạt động được ghi nhận theo hai ngày làm việc; mã hoạt động dùng để truy vết nội dung, không đại diện cho những phiên sử dụng độc lập.

Trong phiên được ghi nhận, AI hỗ trợ tổng hợp tài liệu, tạo mã nguồn, thực thi kiểm tra bằng công cụ và hiệu chỉnh kết quả. Hoạt động kiểm chứng kỹ thuật của AI và hoạt động rà soát độc lập của sinh viên được ghi nhận riêng. Phần sinh viên tự thực hiện được bổ sung tại Mục 5 sau khi có kết quả.

## 2. Nhật ký theo hoạt động

### W3-AI-01. Đối chiếu yêu cầu và hiện trạng dự án

**Mục tiêu:** Xác định phạm vi công việc tuần 3 trên cơ sở kế hoạch, rubric và hiện trạng repository.

**Dữ liệu đầu vào:** Đề cương, bảng tiến độ, bản phân công, rubric; cấu trúc repository và lịch sử Git tại phiên bản cơ sở.

**Phạm vi AI hỗ trợ:** Phân tích các tài liệu và cấu hình; xác định thành phần đã tồn tại, phần cần bổ sung và những nội dung chưa thống nhất.

**Kết quả:** Ghi nhận skeleton backend/frontend, CI và Dockerfile đã có từ tuần 2. Phương án triển khai mở rộng cấu trúc hiện có. Phát hiện Spring Boot 4.1.1 trong mã nguồn khác với Boot 3 trong đề cương; giữ phiên bản của repository để tránh thay đổi nền tảng ngoài phạm vi.

**Kiểm chứng:** Đối chiếu `backend/pom.xml`, `frontend/package.json`, `.github/workflows/ci.yaml` và lịch sử Git. Phân biệt công việc dự kiến trong kế hoạch với sản phẩm đã có.

**Minh chứng và trạng thái:** [Bộ bàn giao tuần 3](../week-03/README.md). Kết quả đối chiếu đã được lập; việc điều chỉnh kế hoạch chung cần nhóm xác nhận.

### W3-AI-02. Tổng hợp tài liệu học tập

**Mục tiêu:** Xây dựng tài liệu tự học gắn với nhiệm vụ kỹ thuật của Toản.

**Dữ liệu đầu vào:** Các công nghệ của dự án và tài liệu chính thức của Spring, PostgreSQL, React, Recharts, Vite, Docker và GitHub.

**Phạm vi AI hỗ trợ:** Tra cứu nguồn, tổng hợp khái niệm, giải thích lý do áp dụng vào dự án; xây dựng bài thực hành và câu hỏi tự kiểm tra.

**Kết quả:** Tài liệu gồm 14 nguồn tham khảo, bao quát REST, JPA, ràng buộc dữ liệu, transaction, migration, component/state/effect, biểu đồ, cấu hình môi trường và quy trình Git/CI. Các nội dung được liên hệ với file triển khai tương ứng.

**Kiểm chứng:** Đối chiếu nội dung với tài liệu chính thức và phiên bản thực tế trong repository. Các nguồn và bài thực hành được ghi trong tài liệu để sinh viên kiểm tra lại.

**Minh chứng và trạng thái:** [Lý thuyết và áp dụng](../week-03/ly-thuyet-va-ap-dung-toan.txt). Tài liệu đã được soạn; mức độ tiếp thu của sinh viên chờ tự đánh giá.

### W3-AI-03. Xây dựng lớp REST API

**Mục tiêu:** Triển khai chức năng đăng ký và tra cứu repository phục vụ luồng thực hành tuần 3.

**Dữ liệu đầu vào:** Skeleton Spring Boot, phạm vi repository GitHub và yêu cầu kiểm tra dữ liệu đầu vào.

**Phạm vi AI hỗ trợ:** Tạo controller, service, DTO, bộ chuẩn hóa URL và xử lý ngoại lệ; bổ sung chức năng đọc danh sách và chi tiết.

**Kết quả:** Cài đặt `POST /api/repositories`, `GET /api/repositories` và `GET /api/repositories/{id}`. Bổ sung kiểm tra URL, phân trang, mã trạng thái HTTP và cờ kiểm soát đăng ký.

**Kiểm chứng:** Kiểm thử URL, request không hợp lệ, bản ghi trùng, bản ghi không tồn tại, giới hạn phân trang và trạng thái đăng ký bị tắt. Kết quả kiểm thử cuối được tổng hợp tại Mục 3.

**Minh chứng và trạng thái:** Các lớp trong `backend/src/main/java/com/example/demo/repository/` và `api/`; [contract tuần 3](../week-03/api-contract-week3.md). API đã được kiểm tra cục bộ. Chức năng hiện chỉ lưu URL, chưa xác minh repository tồn tại/công khai hoặc thực hiện phân tích Git.

### W3-AI-04. Thiết lập lưu trữ PostgreSQL và migration

**Mục tiêu:** Lưu trữ dữ liệu repository và quản lý thay đổi schema có phiên bản.

**Dữ liệu đầu vào:** Cấu trúc dữ liệu repository, Spring Data JPA, PostgreSQL và định hướng sử dụng Flyway trong đề cương.

**Phạm vi AI hỗ trợ:** Tạo entity, repository truy cập dữ liệu, migration V1; cấu hình datasource, transaction và cơ chế Hibernate kiểm tra schema.

**Kết quả:** Bảng `tracked_repository` gồm các trường `id`, `url`, `owner`, `name`, `created_at`; áp dụng ràng buộc duy nhất cho URL đã chuẩn hóa. Migration được quản lý qua Flyway, `ddl-auto=validate` được sử dụng để kiểm tra sự phù hợp của schema.

**Kiểm chứng:** Chạy trên PostgreSQL 18.3 với database thử nghiệm riêng; kiểm tra Flyway V1 thành công, lưu/đọc dữ liệu và phản hồi khi vi phạm ràng buộc duy nhất.

**Minh chứng và trạng thái:** `backend/src/main/resources/db/migration/V1__create_tracked_repository.sql`, các lớp JPA và [kết quả kiểm thử](../week-03/kiem-thu-minh-chung.md). Chưa triển khai đầy đủ schema job, snapshot và metric của hệ thống.

### W3-AI-05. Xây dựng giao diện React và kết nối API

**Mục tiêu:** Thực hiện luồng thêm và xem repository từ giao diện người dùng.

**Dữ liệu đầu vào:** Skeleton React/TypeScript, contract API và các trạng thái tương tác cần thể hiện.

**Phạm vi AI hỗ trợ:** Viết giao diện, API client, quản lý state, cleanup yêu cầu đọc, timeout và thông báo; điều chỉnh bố cục cho desktop và mobile.

**Kết quả:** Giao diện có form đăng ký, danh sách repository, phân trang, thông báo thành công và trạng thái tải/rỗng/lỗi. Lỗi đăng ký được tách khỏi lỗi tải danh sách để tránh che dữ liệu hiện có khi thao tác đăng ký thất bại.

**Kiểm chứng:** Chạy lint, TypeScript/Vite build và kiểm thử trình duyệt; kiểm tra luồng ghi dữ liệu thật rồi tải lại trang. Xem ảnh desktop/mobile và kiểm tra không tràn ngang tại viewport 375 px.

**Minh chứng và trạng thái:** `frontend/src/App.tsx`, `frontend/src/api.ts`, các file CSS và [ảnh minh chứng](../week-03/evidence/week3-mobile.png). Luồng thực hành đã được kiểm tra cục bộ; chưa được triển khai lên frontend staging.

### W3-AI-06. Thực hành trực quan hóa bằng Recharts

**Mục tiêu:** Khảo sát cách biểu diễn xu hướng chỉ số theo snapshot để chuẩn bị cho dashboard.

**Dữ liệu đầu vào:** Tài liệu LineChart của Recharts và bốn điểm dữ liệu minh họa.

**Phạm vi AI hỗ trợ:** Tạo biểu đồ đường, cấu hình trục, tooltip, vùng hiển thị responsive và bảng dữ liệu thay thế; tách phần biểu đồ bằng lazy import.

**Kết quả:** Component `DemoTrend` hiển thị điểm mẫu trong miền 0–1, có nhãn “Dữ liệu minh họa”. Phần biểu đồ được tách khỏi bundle chính của ứng dụng.

**Kiểm chứng:** Kiểm tra nhãn bằng browser test, đối chiếu số liệu hiển thị và xem kết quả build. Bundle chính sau điều chỉnh khoảng 226,22 kB; chunk biểu đồ khoảng 352,86 kB, trước nén gzip.

**Minh chứng và trạng thái:** `frontend/src/components/DemoTrend.tsx` và [ảnh desktop](../week-03/evidence/week3-desktop.png). Đây là bài thực hành giao diện; chưa phải dữ liệu snapshot hoặc kết quả thực nghiệm RQ2.

### W3-AI-07. Chuẩn bị công cụ phỏng vấn stakeholder

**Mục tiêu:** Chuẩn bị thu thập nhu cầu thực tế trước khi hoàn thiện đặc tả yêu cầu.

**Dữ liệu đầu vào:** Nhóm người dùng mục tiêu, phạm vi dự án và mục tiêu phỏng vấn ít nhất ba stakeholder phù hợp.

**Phạm vi AI hỗ trợ:** Soạn phần giới thiệu, nội dung đồng ý tham gia, câu hỏi về tình huống sử dụng Git, khó khăn, công cụ hiện có, nhu cầu cảnh báo và kỳ vọng đối với AI; tạo mẫu biên bản và bảng tổng hợp nhu cầu.

**Kết quả:** Bộ câu hỏi và biểu mẫu ghi nhận phản hồi theo mã ẩn danh, có nội dung ghi nhận ý kiến phản biện và ưu tiên của người tham gia.

**Kiểm chứng:** Rà soát phạm vi câu hỏi so với mục tiêu nghiên cứu; giữ các trường kết quả ở trạng thái chưa có dữ liệu. Chưa có kiểm chứng thực địa về chất lượng bộ câu hỏi.

**Minh chứng và trạng thái tại bước chuẩn bị:** [Mẫu phỏng vấn](../week-03/mau-phong-van-gui-nguoi-tham-gia.txt) đã được soạn khi chưa nhận phản hồi. Sau đó Toản cung cấp ba bộ trả lời; hoạt động tổng hợp bổ sung được ghi riêng tại W3-AI-14 và [báo cáo phỏng vấn](../interview-notes.md).

### W3-AI-08. Soạn contract và tiêu chí nghiệm thu

**Mục tiêu:** Làm rõ đầu vào, đầu ra, trường hợp lỗi và điều kiện hoàn thành của phần thực hành.

**Dữ liệu đầu vào:** API đã cài đặt, danh sách use case hiện có và đề cương v3.

**Phạm vi AI hỗ trợ:** Mô tả endpoint, request/response, mã HTTP và tám acceptance criteria W3-AC01–W3-AC08; xây dựng đề xuất contract cho analysis job.

**Kết quả:** Tách rõ contract của API đã triển khai với đề xuất job/snapshot/alert cho giai đoạn tiếp theo. Ghi nhận sự khác biệt về số thứ tự use case giữa tài liệu hiện có để nhóm đối chiếu.

**Kiểm chứng:** Đối chiếu mô tả API với controller, service và các ca kiểm thử. Phần đề xuất job chưa được đánh giá bằng implementation hoặc benchmark.

**Minh chứng và trạng thái:** [Contract tuần 3](../week-03/api-contract-week3.md). Phần hiện có đã được đối chiếu với mã; đặc tả chung chờ review của nhóm.

### W3-AI-09. Xác định phạm vi đề xuất cho trợ lý AI review

**Mục tiêu:** Chuẩn bị cơ sở thảo luận về chức năng LLM trong sản phẩm.

**Dữ liệu đầu vào:** Yêu cầu AI review trong đề cương và nguyên tắc tách AI khỏi metric engine.

**Phạm vi AI hỗ trợ:** Đề xuất context, cấu trúc output, provider abstraction, version của prompt, kiểm soát kích thước, timeout và xử lý lỗi.

**Kết quả:** Bản nháp giới hạn AI ở chức năng tư vấn sau khi có snapshot; không tự sửa mã hoặc thay đổi hotspot score. Provider/model và các ngưỡng vận hành được để mở cho PoC.

**Kiểm chứng:** Đối chiếu phạm vi đề xuất với đề cương; chưa thực hiện gọi provider thật hoặc đánh giá chất lượng đầu ra LLM.

**Minh chứng và trạng thái:** [Phạm vi AI review](../ai-review-scope.md). Tài liệu ở mức đề xuất, chờ nhóm thống nhất.

### W3-AI-10. Thiết kế, thực thi kiểm thử và hiệu chỉnh

**Mục tiêu:** Kiểm tra tính đúng đắn của phần triển khai và ghi nhận các vấn đề trong đầu ra AI.

**Dữ liệu đầu vào:** Mã backend/frontend được bổ sung, contract và các tình huống đầu vào hợp lệ/không hợp lệ.

**Phạm vi AI hỗ trợ:** Viết test URL, API trên PostgreSQL thật, cờ đăng ký, CORS và browser test; chạy các công cụ kiểm tra, phân tích lỗi và sửa mã.

**Kết quả:** Lần kiểm tra cuối có 22 test backend và 4 test trình duyệt đạt; lint/build frontend thành công. Các lỗi được phân tích riêng tại Mục 4.

**Kiểm chứng:** Đối chiếu kết quả test với báo cáo thực thi; phân biệt ba test trình duyệt dùng API giả lập với một test đi qua backend và PostgreSQL thật.

**Minh chứng và trạng thái:** [Báo cáo kiểm thử](../week-03/kiem-thu-minh-chung.md), [trích xuất kết quả chạy](../week-03/evidence/verification-summary.txt). Hoạt động kiểm tra đã được AI thực thi; phần sinh viên chạy lại được ghi riêng tại Mục 5.

### W3-AI-11. Chuẩn bị CI và cấu hình môi trường

**Mục tiêu:** Tạo điều kiện kiểm tra lặp lại và chuẩn bị chạy ứng dụng với database.

**Dữ liệu đầu vào:** Workflow và Dockerfile hiện có; yêu cầu Java 21, Node 24, PostgreSQL và kiểm thử trình duyệt.

**Phạm vi AI hỗ trợ:** Bổ sung PostgreSQL service cho CI, npm ci, lint/build, browser test, dependency audit, secret scan và Docker build; tạo Compose, mẫu biến môi trường và hướng dẫn chạy.

**Kết quả:** Cấu hình CI được cập nhật; Compose mô tả database/backend local, có kiểm tra trạng thái PostgreSQL trước khi khởi động backend.

**Kiểm chứng:** `docker compose config --quiet` chấp nhận cấu hình; backend/frontend được kiểm tra trực tiếp tại máy. Docker Engine chưa chạy nên chưa thực thi container, Gitleaks hoặc Docker build. Chưa có run GitHub Actions cho bản sửa.

**Minh chứng và trạng thái:** `.github/workflows/ci.yaml`, `compose.yaml`, `.env.example` và [README](../../README.md). Hoàn thành phần cấu hình; hiệu lực trên hạ tầng CI/CD cần được kiểm chứng sau khi đưa thay đổi lên repository.

### W3-AI-12. Kiểm tra staging

**Mục tiêu:** Xác định khả năng truy cập của các môi trường triển khai hiện có.

**Dữ liệu đầu vào:** Hai URL backend/frontend do sinh viên cung cấp.

**Phạm vi AI hỗ trợ:** Gửi yêu cầu chỉ đọc, ghi nhận response và chuyển hướng; đề xuất các bước cấu hình trước khi triển khai mã mới.

**Kết quả:** Backend Render tại `/api/health` trả HTTP 200, nội dung `OK`. URL frontend Vercel chuyển đến trang đăng nhập, chưa xác nhận truy cập công khai.

**Kiểm chứng:** Xem nội dung response và URL sau chuyển hướng thay vì chỉ dựa vào mã HTTP cuối cùng. Không suy ra trạng thái database hoặc toàn bộ sản phẩm từ health endpoint.

**Minh chứng và trạng thái:** [Bàn giao staging](../week-03/staging-week3.md). Đã kiểm tra phiên bản đang tồn tại tại thời điểm 30/09; mã mới chưa được triển khai.

### W3-AI-13. Tổng hợp báo cáo và hồ sơ truy vết

**Mục tiêu:** Hệ thống hóa kết quả tuần 3, phần chưa hoàn thành và minh chứng liên quan.

**Dữ liệu đầu vào:** File đã tạo/sửa, kết quả kiểm thử, quan sát staging và trạng thái công việc.

**Phạm vi AI hỗ trợ:** Soạn báo cáo tiến độ, nhật ký AI, mục lục tài liệu và hướng dẫn bổ sung minh chứng Git/PR.

**Kết quả:** Bộ báo cáo phân biệt sản phẩm đã kiểm tra, tài liệu đề xuất và hoạt động còn chờ thực hiện. Nhật ký được trình bày theo từng phần chuyên môn, không sử dụng nguyên văn yêu cầu hội thoại.

**Kiểm chứng:** Đối chiếu số lượng test, tình trạng phỏng vấn, deployment và commit với kết quả thực tế; kiểm tra liên kết nội bộ của bộ tài liệu.

**Minh chứng và trạng thái:** [Báo cáo tuần](../week-03/bao-cao-tuan-03-toan.md), [hướng dẫn Git và minh chứng](../week-03/git-va-minh-chung.md). Hồ sơ đã được soạn; chờ sinh viên rà soát, bổ sung và nộp theo quy trình của môn học.

### W3-AI-14. Tổng hợp và phân tích phản hồi stakeholder

**Mục tiêu:** Lập báo cáo khảo sát nhu cầu từ ba bộ câu trả lời do sinh viên cung cấp sau bước chuẩn bị câu hỏi.

**Dữ liệu đầu vào:** Một tệp văn bản được quy ước là S01 và hai bộ trả lời S02–S03 trong trao đổi bổ sung. Cả ba có câu đồng ý sử dụng nội dung ẩn danh; S02 chưa có câu 10–12. S03 không đồng ý ghi âm.

**Phạm vi AI hỗ trợ:** Biên tập tóm tắt theo người/câu hỏi, nhóm chủ đề, phân tích khác biệt và liên hệ với yêu cầu dự án. Loại thông tin liên hệ khỏi bản công khai; ghi rõ các trường metadata chưa được cung cấp.

**Kết quả:** Báo cáo gồm hồ sơ ba trường hợp, bảy nhóm nhu cầu N01–N07 và bảy đề xuất yêu cầu PV-R01–PV-R07. Phân biệt nhu cầu hiển thị coupling lịch sử với phát hiện file bị bỏ sót trong PR; giữ nhu cầu vượt phạm vi ở trạng thái đề xuất. Cập nhật phần khảo sát trong báo cáo tuần và mục lục bàn giao.

**Kiểm chứng:** Đối chiếu từng nội dung với câu trả lời gốc; không điền thay câu 10–12 của S02; đánh dấu số liệu S03 đặt trong ngoặc vuông cần xác nhận. Các khoảng thời gian tự ước tính không được gộp thành trung bình hoặc kết quả hiệu quả sản phẩm. Chưa có xác nhận lại của người tham gia hoặc kiểm chứng thực địa độc lập.

**Minh chứng và trạng thái:** [Báo cáo phỏng vấn](../interview-notes.md). Đã lập từ nguồn Toản cung cấp; chờ sinh viên xác nhận metadata, phản hồi còn thiếu và nhóm duyệt diễn giải/đề xuất. Chưa tạo commit/PR cho cập nhật này.

## 3. Tổng hợp kết quả kiểm chứng

| Hạng mục | Kết quả ghi nhận | Giới hạn diễn giải |
|---|---|---|
| Backend trên PostgreSQL 18.3 | 22 test đạt, 0 lỗi, 0 bỏ qua; Maven verify thành công | Phạm vi chức năng tuần 3 |
| Migration | Flyway V1 thành công | Một bảng repository, chưa phải toàn bộ schema |
| Giao diện | 4 browser test đạt, gồm một test live | Chưa đại diện toàn bộ luồng sản phẩm |
| Kiểm tra frontend | Lint và build đạt; npm audit báo 0 vulnerabilities tại lần chạy | Không thay thế kiểm tra secret, giấy phép hoặc dependency Java |
| Compose | Cấu hình được chấp nhận | Chưa chạy container |
| Staging hiện có | Backend health truy cập được; frontend chuyển đến đăng nhập | Chưa triển khai mã mới |
| Phỏng vấn | Đã tổng hợp 3 bộ trả lời do Toản cung cấp | S02 thiếu câu 10–12; metadata thu thập và số liệu S03 cần xác nhận |

Minh chứng kỹ thuật chứng minh trạng thái của sản phẩm tại lần kiểm tra. Mức độ hiểu và khả năng giải thích của sinh viên được đánh giá qua hoạt động tự thực hành và review riêng.

## 4. Phân tích và hiệu chỉnh các vấn đề trong đầu ra AI

### E01. Không bảo toàn nguyên nhân của ngoại lệ

- **Vị trí:** `frontend/src/api.ts`.
- **Biểu hiện:** ESLint báo lỗi `preserve-caught-error`.
- **Nguyên nhân:** Tạo đối tượng `Error` mới trong khối catch nhưng không đính kèm ngoại lệ gốc.
- **Hiệu chỉnh:** Bổ sung `{ cause: error }` để giữ thông tin nguyên nhân.
- **Kiểm chứng sau sửa:** Lint frontend không còn lỗi.
- **Nguồn phát hiện và xử lý:** AI thông qua công cụ phân tích tĩnh; chưa ghi nhận sinh viên phát hiện độc lập.

### E02. Cấu hình CORS chưa bao phủ origin dùng khi kiểm thử

- **Vị trí:** `backend/src/main/resources/application.properties` và `compose.yaml`.
- **Biểu hiện:** POST từ bài kiểm thử trình duyệt nhận HTTP 403; kiểm tra trực tiếp cho nội dung `Invalid CORS request`.
- **Nguyên nhân:** Cấu hình chỉ cho phép `http://localhost:5173`, trong khi trình duyệt thử nghiệm dùng `http://127.0.0.1:5173`.
- **Hiệu chỉnh:** Khai báo rõ hai origin local, bổ sung test preflight cho origin hợp lệ và origin không được phép.
- **Kiểm chứng sau sửa:** Test CORS và luồng browser→API→PostgreSQL đạt.
- **Nguồn phát hiện và xử lý:** AI qua browser test và đối chiếu HTTP; chưa ghi nhận sinh viên phát hiện độc lập.

### E03. Diễn giải chưa chính xác phản hồi HTTP 403

- **Vị trí:** `frontend/src/api.ts`.
- **Biểu hiện:** Giao diện thông báo đăng ký bị tắt trong trường hợp nguyên nhân thực tế là CORS.
- **Nguyên nhân:** Gán một nguyên nhân duy nhất cho mọi response 403.
- **Hiệu chỉnh:** Sử dụng thông báo từ chối chung khi không có chi tiết; ưu tiên thông tin lỗi có cấu trúc từ server khi có.
- **Kiểm chứng sau sửa:** Đối chiếu nhánh xử lý lỗi trong mã; lint/build và bộ browser test cuối đạt. Chưa có ca browser tự động riêng khẳng định chính xác nội dung fallback 403.
- **Nguồn phát hiện và xử lý:** AI trong quá trình phân tích E02; chưa ghi nhận sinh viên phát hiện độc lập.

### Ghi nhận bổ sung về tối ưu và môi trường

| Vấn đề | Điều chỉnh | Phân loại |
|---|---|---|
| Bundle ban đầu khoảng 577,96 kB, có cảnh báo kích thước | Tách phần biểu đồ bằng lazy import; bundle chính còn khoảng 226,22 kB | Tối ưu đóng gói; chưa có đo tác động đến thời gian tải thực tế |
| Repackage JAR thất bại trên Windows do file đang được backend thử nghiệm sử dụng | Dừng đúng tiến trình thử nghiệm rồi chạy verify lại | Vấn đề môi trường thực thi |

Các ghi nhận trên không được quy đổi thành số lỗi AI do sinh viên tự phát hiện khi chưa có minh chứng tương ứng. Commit sửa lỗi sẽ được liên kết sau khi thay đổi được ghi nhận trong Git.

## 5. Rà soát và xác nhận của sinh viên

Bảng này dành cho hoạt động sinh viên thực hiện sau khi tiếp nhận kết quả hỗ trợ. Trạng thái hiện tại được giữ là “chờ bổ sung” đối với nội dung chưa có ghi nhận.

| Hoạt động | Nội dung cần xác nhận | Kết quả tự thực hiện | Ngày và minh chứng |
|---|---|---|---|
| W3-AI-01–02 | Nguồn đã đọc; đánh giá sự phù hợp của nội dung tổng hợp | Toản xác nhận đã đọc lý thuyết; chưa có đánh giá độc lập từng nguồn | Xác nhận trong hội thoại 02/10/2026 |
| W3-AI-03–04 | Giải thích luồng API, chuẩn hóa URL, constraint và migration | Chờ bổ sung | |
| W3-AI-05–06 | Giải thích state, timeout, CORS và ý nghĩa dữ liệu minh họa | Chờ bổ sung | |
| W3-AI-07, W3-AI-14 | Đối chiếu báo cáo với nguồn; bổ sung metadata, câu trả lời thiếu và xác nhận của nhóm | Đã cung cấp 3 bộ trả lời; chờ rà soát bản tổng hợp | |
| W3-AI-08–09 | Review contract và phạm vi AI với thành viên còn lại | Toản xác nhận trao đổi xong với Tưởng; chưa có biên bản từng quyết định | Xác nhận 02/10/2026; không thay thế GitHub PR review |
| W3-AI-10–12 | Chạy lại kiểm thử, kiểm tra CI và môi trường triển khai | Chờ bổ sung | |
| W3-AI-13 | Rà soát báo cáo và bổ sung minh chứng nộp | Chờ bổ sung | |

### Ghi nhận nội dung sinh viên giữ, sửa hoặc loại bỏ

| Mã hoạt động/file | Quyết định | Lý do | Kiểm chứng của sinh viên | Commit/PR |
|---|---|---|---|---|
| Chờ bổ sung | | | | |

### Liên kết với lịch sử Git

| Nhóm thay đổi | Commit triển khai | Commit hiệu chỉnh | Pull Request và người review |
|---|---|---|---|
| Backend và database | `165df3376182602d84607984b267d41cec15156e` | Trong commit triển khai | Xem biên bản hoàn tất |
| Frontend và biểu đồ | `165df3376182602d84607984b267d41cec15156e` | Trong commit triển khai | Xem biên bản hoàn tất |
| Kiểm thử và cấu hình CI | `165df3376182602d84607984b267d41cec15156e` | Trong commit triển khai | Xem biên bản hoàn tất |
| Tài liệu học, khảo sát và báo cáo | Xem lịch sử Git của file | Xem biên bản hoàn tất | Xem biên bản hoàn tất |

## 6. Nguyên tắc kiểm soát và giới hạn sử dụng

1. Đối chiếu đề xuất AI với phạm vi đề tài, cấu hình repository và nguồn chính thức trước khi tiếp nhận.
2. Phân biệt nội dung kế thừa, nội dung do AI tạo và phần sinh viên hiệu chỉnh; lưu liên kết đến file, test và commit tương ứng.
3. Kiểm tra đầu ra bằng phương pháp phù hợp; ghi rõ dữ liệu giả lập, dữ liệu thật và phần chưa được kiểm chứng.
4. Không đưa secret hoặc thông tin nhận diện người tham gia vào nhật ký công khai. Kiểm tra nguồn và giấy phép khi tái sử dụng mã; kiểm tra dependency license đầy đủ chưa được thực hiện trong phiên này.
5. Sinh viên chịu trách nhiệm rà soát nội dung được tiếp nhận, giải thích các quyết định kỹ thuật và cập nhật nhật ký theo hoạt động thực tế.

Người rà soát: ____________________

Ngày xác nhận: ____________________

## 7. Hoạt động bổ sung ngày 02/10/2026 — W3-AI-15

**Mục tiêu:** Hoàn tất phần kỹ thuật và hồ sơ tuần 3 sau khi sinh viên xác nhận đã đọc lý thuyết, trao đổi với thành viên còn lại.

**Đầu vào:** Nhánh tuần 3, cập nhật `43d614f` của Tưởng, xác nhận của Toản về việc học và phân công phỏng vấn bổ sung, hai URL staging được cung cấp lại.

**Phạm vi AI hỗ trợ:** Tích hợp cập nhật không xung đột; rà soát thay đổi; chạy Maven trên PostgreSQL thật, lint/build/audit và browser test; kiểm tra chỉ đọc staging; tạo commit và chuẩn bị PR/CI; cập nhật hồ sơ theo minh chứng.

**Đầu ra và kiểm chứng:** 22 backend test và 4 browser test đạt; frontend lint/build đạt; npm audit 0 vulnerabilities. Backend staging health 200 nhưng API mới 404; frontend chuyển sang Vercel login. Phiên đầu bị hạn chế truy cập cache/mạng trong sandbox; chạy lại với quyền công cụ phù hợp, không sửa kết quả kiểm thử để bỏ qua lỗi. Một lần kết nối thử dùng role postgres không tồn tại; dùng đúng role ghm của database thử nghiệm và kiểm tra thành công.

**Phần do sinh viên xác nhận:** Đã đọc lý thuyết và trao đổi với Tưởng. Tưởng phụ trách hỏi thêm hai người và tổng hợp sau. Không tự tạo câu trả lời, xác nhận review, thời gian học hay kết quả triển khai.

**Giới hạn:** Review độc lập, quyền quản trị staging và xác nhận nộp báo cáo cần bằng chứng bên ngoài phiên kiểm thử. Khác biệt công thức coupling giữa đề cương và ghi chú mới được ghi thành mục cần thống nhất trước khi cài đặt metric.

**Truy vết:** [Biên bản hoàn tất và liên kết Git/CI](../week-03/bien-ban-hoan-tat-02-10.md).
