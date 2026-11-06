# Chạy và kiểm tra tuần 8

## Local

Java 21, PostgreSQL 18, Node 24 và Python có `lizard==1.24.3`. Dùng database riêng cho tests; nhiều test dùng TRUNCATE CASCADE. Không trỏ tests vào staging hoặc database đang dùng.

```powershell
python -m venv .local/lizard-env
.local/lizard-env/Scripts/python.exe -m pip install lizard==1.24.3
$env:PATH="$PWD/.local/lizard-env/Scripts;$env:PATH"
$env:DB_URL='jdbc:postgresql://127.0.0.1:5432/ghm_test'
$env:DB_USERNAME='ghm'
$env:DB_PASSWORD='<mật khẩu database test>'
Set-Location backend
./mvnw.cmd -B -ntp verify
```

Để chạy ứng dụng với **database demo riêng**, đổi DB_URL rồi bật `APP_REGISTRATION_ENABLED`, `APP_ANALYSIS_ENABLED`, `APP_ANALYSIS_WRITES_ENABLED`, `APP_ANALYSIS_PIPELINE_ENABLED` thành true. `APP_ANALYSIS_CACHE_DIR` trỏ thư mục cache giữ được qua restart. `APP_AI_REVIEW_ENABLED=true` cho preview; POST review vẫn 503 nếu chưa có provider thật. Mặc định application.properties tắt các thao tác này.

Build `backend/mvnw package -DskipTests` sau khi tests đạt, chạy jar, rồi `npm ci`, `npm run dev` trong frontend. Vite proxy `/api` tới localhost:8080. Trên UI thêm repo công khai → Mở phân tích → Phân tích repository → xem Lịch sử phân tích → mở snapshot. Có thể chờ snapshot xuất hiện trong danh sách tự cập nhật.

Kiểm thử UI: `npm run lint`, `npm run build`, `npm run test:e2e`. Mặc định CI bỏ qua 2 live cases. Muốn chạy đủ local, backend demo phải sẵn sàng và `spring-petclinic` đã có snapshot:

```powershell
$env:GHM_LIVE_BACKEND='1'
$env:GHM_WEEK8_LIVE='spring-petclinic'
npm run test:e2e
```

## Docker Compose

Tạo `.env` từ `.env.example`, đặt mật khẩu DB local và bật **cả ba** APP_ANALYSIS_* flags. Compose mặc định bật pipeline nếu biến không đặt; file `.env.example` chủ động để false, vì vậy cần sửa ba cờ khi làm demo. AI giữ tắt trừ môi trường kiểm soát đã hiểu phần provider.

```text
docker compose config --quiet
docker compose up -d --build
```

Mở http://localhost:5173; nginx phục vụ React và proxy API sang backend. Backend có Java/Python/Lizard, chạy user ghm; `pgdata` giữ database và `gitcache` giữ object Git theo repo ID. Cổng host chỉ bind 127.0.0.1. Không xoá volume khi cần giữ dữ liệu. `docker compose down` dừng dịch vụ và giữ volume.

CI package build cả image rồi chạy Compose smoke qua nginx: trang chủ, health, capabilities. Test pipeline thật chạy ở job backend (Git fixture) và local (repo công khai); Compose smoke không được mô tả thành đã chạy đầy đủ AI provider thương mại.

## Staging

Review PR, backup DB/cache, merge theo quy trình nhóm rồi để platform deploy. Backend mới cần Python/Lizard trong image, database V4 và cache lưu bền; frontend mới cần API base URL hoặc nginx proxy đúng. Kiểm tra health, capabilities, đăng ký/analysis job, snapshot/file/evidence và lỗi AI khi thiếu provider. Chụp ảnh URL thật, lưu commit deploy và thời gian. Chưa có quyền/config platform được xác minh trong bộ này; không ghi đã deploy.

## Migration

V1–V3 không sửa checksum. V4 thêm bảng ai_review_audit và FK `(snapshot_id,file_id)`; không đổi dữ liệu metric cũ. Full backend tests chạy PostgreSQL thật. Nâng cấp database có snapshot được kiểm tra riêng trong minh chứng trước bàn giao; backup vẫn cần khi triển khai DB thật.
