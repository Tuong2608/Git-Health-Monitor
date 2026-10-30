# Git Health Monitor

Bản thực hành tuần 3 của Toản: đăng ký URL repository, lưu PostgreSQL, xem danh sách/chi tiết qua REST API và React; biểu đồ Recharts dùng **dữ liệu minh họa** có nhãn riêng. Backend hiện tại **có module đọc Git đơn lập chưa nối adapter, đã có job/snapshot/cảnh báo trên PostgreSQL nhưng mặc định tắt; chưa tính metric và chưa gọi LLM**.

Nhóm đã có PoC/feasibility riêng để xác minh hướng dùng JGit và Lizard; PoC này **chưa được tích hợp vào backend hiện tại**. Baseline metric chuẩn bị cho giai đoạn cài đặt nằm tại [Metric Specification v1](docs/metric-spec-v1.md).

Tài liệu bắt đầu: [Bộ bàn giao tuần 3](docs/week-03/README.md).

## Công nghệ thực tế

- Java 21, Spring Boot **4.1.1** (kế thừa repo; đề cương v3 ghi Boot 3, cần nhóm xác nhận khác biệt).
- PostgreSQL 18, Spring Data JPA, Flyway; V1 tạo bảng `tracked_repository`, V2 thêm 10 bảng job/snapshot/metric/cảnh báo.
- React 19, TypeScript, Vite 8, Recharts; dùng Node 24 như CI.

## Chạy local bằng Docker và Vite

1. Cần Docker Engine đang chạy, Node 24 và npm.
2. Sao chép `.env.example` thành `.env`, đặt mật khẩu local riêng.
3. Từ thư mục gốc: `docker compose up --build -d`.
4. Trong `frontend`: `npm ci`, rồi `npm run dev`.
5. Mở `http://localhost:5173`. Backend ở `http://localhost:8080/api/health`.

Compose chỉ bind cổng database/backend vào localhost. Cờ cho phép đăng ký được bật trong Compose cho thử nghiệm local. `.env` không được commit. Không chạy `docker compose down -v` nếu muốn giữ dữ liệu.

## Chạy với PostgreSQL và Java có sẵn

Tạo database phát triển riêng, ví dụ `git_health_monitor`, không dùng database quan trọng. Trong PowerShell:

```powershell
$env:JAVA_HOME='DUONG_DAN_JDK_21'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:DB_URL='jdbc:postgresql://localhost:5432/git_health_monitor'
$env:DB_USERNAME='TEN_USER_DATABASE'
$env:DB_PASSWORD='MAT_KHAU_LOCAL_CUA_BAN'
$env:APP_REGISTRATION_ENABLED='true'
cd backend
.\mvnw.cmd spring-boot:run
```

Mở terminal thứ hai, vào `frontend`, chạy `npm ci` và `npm run dev`. Vite chuyển tiếp `/api` tới backend local. Backend **không tự đọc `.env`** khi chạy trực tiếp bằng Maven; `.env` ở root dành cho Compose.

## Kiểm thử

Dùng database thử nghiệm riêng vì bộ test ghi dữ liệu.

- Backend: cấu hình `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` rồi chạy `backend/mvnw -B -ntp verify` (Windows: `mvnw.cmd`). Flyway chạy trên PostgreSQL thật.
- Frontend: `npm run lint`, `npm run build`, `npx playwright install chromium`, `npm run test:e2e`.
- Mặc định test browser mock API, test live được skip có lý do. Muốn chạy live, khởi động backend ở port 8080 trên database test và đặt `GHM_LIVE_BACKEND=1` trước `npm run test:e2e`.
- Báo cáo backend: `backend/target/surefire-reports/`; browser: `frontend/playwright-report/`.
- Module đọc Git (tuần 6) không cần database: `mvnw.cmd -B -ntp test "-Dtest=GitIngestion*Tests"` chạy được cả bộ ngay cả khi Docker/PostgreSQL đang hỏng.

## Phạm vi và triển khai

Bộ đặc tả tuần 4 của Toản: [yêu cầu, rule/NFR, API và ADR](docs/week-04/README.md). Đây là thiết kế để review, không đồng nghĩa các endpoint tương lai đã triển khai.

Bộ thiết kế tuần 5 của Toản: [database 10 bảng, wireframe, thực nghiệm và minh chứng](docs/week-05/README.md).

Bộ cài đặt tuần 6 của Toản: [snapshot, PostgreSQL job/scheduler, cảnh báo, API và kiểm thử](docs/week-06/README.md). Cần adapter `AnalysisPipeline` của module Git/metric để phân tích repository thật. `APP_ANALYSIS_ENABLED` và `APP_ANALYSIS_WRITES_ENABLED` mặc định `false`; chỉ bật trong môi trường nhóm kiểm soát sau khi có adapter. Xem [hướng dẫn tích hợp](docs/week-06/ban-giao-tuong.md).

Tuần 7: [API file/evidence/coupling/trend/comparison, retry job và metric nullable](docs/week-07/README.md). V3 giữ dữ liệu chưa đo được là null; xem [hợp đồng tích hợp mới](docs/week-07/integration-contract.md) trước khi nối adapter.

- URL hợp cú pháp chưa chứng minh repo tồn tại/công khai. Module đọc Git của Tưởng chỉ phát hiện điều đó khi clone/fetch thất bại, tức vẫn **không** có xác nhận sớm tại thời điểm đăng ký.
- Có chuẩn hóa URL, chống trùng tại database, giới hạn `size` của danh sách 1–100, CORS theo danh sách origin.
- Chưa có authentication/quota. Đăng ký mặc định **tắt** (`APP_REGISTRATION_ENABLED=false`); chỉ bật cho phiên thử nghiệm được kiểm soát. CORS không thay thế xác thực/phân quyền.
- **Trước khi triển khai bản này lên Render phải cấu hình PostgreSQL và DB_*:** skeleton cũ không cần DB nhưng bản mới có Flyway/JPA nên sẽ không khởi động nếu thiếu kết nối.
- Hướng dẫn và giới hạn staging: [staging-week3.md](docs/week-03/staging-week3.md).

Bản review tích hợp tuần 4: [kiến trúc](docs/architecture.md), [đối chiếu Tưởng và Toản](docs/week-04/review-tich-hop-tuong-toan.md). Quyết định mới chờ nhóm review; không phải tính năng runtime đã triển khai.

Phần tuần 5 của Tưởng: [bàn giao](docs/week-05/README.md), [thuật toán](docs/algorithm-design.md), [giám sát](docs/monitoring-design.md), [đề xuất thực nghiệm chung](docs/experiment-design.md). Interface/prompt AI được bổ sung trong AI scope; ERD và wireframe chờ phần Toản.

Phần tuần 6 của Tưởng: [module thu thập Git](docs/week-06/git-ingestion.md) — hợp đồng `Fact`/lineage cho lớp lưu trữ, lệnh chạy mẫu không cần database, minh chứng test và ba điểm còn mở cần hai người chốt.

Phần tuần 7 của Tưởng: [bộ tính chỉ số, Lizard XML, adapter AnalysisPipeline và LlmReviewService](docs/week-07/metric-and-pipeline-tuong.md) — cài đặt công thức Metric Spec v1, lệnh chạy mẫu full pipeline và 41/41 test không cần database.
