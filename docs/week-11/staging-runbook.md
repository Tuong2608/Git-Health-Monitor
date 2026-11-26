# Hướng dẫn triển khai Staging sống (Cách 1 — Docker Compose + HTTPS) và nạp dữ liệu RQ3 (STT 06, 27, 28)

Tài liệu này hướng dẫn dựng môi trường Staging sống bằng Docker Compose trên máy chủ Ubuntu (Azure for Students `Standard_B2s` 2 vCPU / 4 GB RAM hoặc VPS tương đương) và nạp sẵn dữ liệu mẫu phục vụ khảo sát người dùng `RQ3` (`STT 33`).

---

## 1. Những điểm kỹ thuật đã hoàn thiện trong mã nguồn cho Staging

1. **Tham số hóa `APP_ALLOWED_ORIGINS` và cổng bind trong [compose.yaml](../../compose.yaml):**
   - `APP_ALLOWED_ORIGINS: ${APP_ALLOWED_ORIGINS:-http://localhost:5173,http://127.0.0.1:5173}`: Mặc định giữ nguyên cấu hình local và CI (`localhost:5173`, `127.0.0.1:5173`), đồng thời cho phép truyền domain HTTPS của máy chủ staging từ file `.env`.
   - `ports: - "${FRONTEND_BIND_HOST:-127.0.0.1}:${FRONTEND_PORT:-5173}:80"`: Giữ an toàn chỉ lắng nghe loopback mặc định khi đứng sau reverse proxy trên cùng host, hoặc đổi qua biến môi trường khi cần.
2. **Bổ sung [compose.staging.yaml](../../compose.staging.yaml) và [deploy/Caddyfile](../../deploy/Caddyfile):**
   - Tích hợp dịch vụ `caddy:2.9-alpine` tự động cấp phát chứng chỉ TLS/HTTPS (Let's Encrypt) cho biến `STAGING_DOMAIN` và reverse-proxy toàn bộ lưu lượng `/` cùng `/api/*` vào container `frontend:80` (Nginx trong `frontend` tiếp tục chuyển tiếp `/api/*` sang `backend:8080`).
3. **Bổ sung công cụ nạp dữ liệu mẫu tự động [deploy/seed_staging.py](../../deploy/seed_staging.py):**
   - Tự động kiểm tra `/api/health`, `/api/analysis-capabilities`, đăng ký kho mẫu `spring-projects/spring-petclinic` và `apache/commons-csv`, tạo quy tắc cảnh báo `STRONG_COUPLING` (`threshold = 0.5, minShared = 5`) cùng `HOTSPOT_INCREASE` (`threshold = 0.15`), kích hoạt phân tích và chờ job hoàn tất (`SUCCEEDED`).
   - Giữ trống kho `spring-guides/gs-rest-service` để người tham gia khảo sát `RQ3` tự tay đăng ký và bấm phân tích ở **Tác vụ T1**.

---

## 2. Quy trình 4 bước dựng Staging trên Ubuntu VM

### Bước 1: Clone mã nguồn và tạo tệp `.env` trên server

```bash
git clone https://github.com/Tuong2608/Git-Health-Monitor.git
cd Git-Health-Monitor
cp .env.example .env
```

Chỉnh sửa `.env` trên server (tuyệt đối không commit `.env` lên Git):

```dotenv
DB_NAME=git_health_monitor
DB_USERNAME=ghm
DB_PASSWORD=MatKhau_PostgreSQL_Manh_Rieng_2026
APP_REGISTRATION_ENABLED=true
APP_ANALYSIS_ENABLED=true
APP_ANALYSIS_WRITES_ENABLED=true
APP_ANALYSIS_PIPELINE_ENABLED=true
APP_ANALYSIS_CACHE_DIR=/app/cache
APP_AI_REVIEW_ENABLED=true
APP_AI_REVIEW_PROVIDER_ENABLED=true
APP_AI_REVIEW_PROVIDER_NAME=mistral
APP_AI_REVIEW_PROVIDER_ENDPOINT=https://api.mistral.ai/v1/chat/completions
APP_AI_REVIEW_PROVIDER_MODEL=codestral-latest
APP_AI_REVIEW_PROVIDER_API_KEY=YOUR_MISTRAL_API_KEY
STAGING_DOMAIN=ghm-staging.example.com
APP_ALLOWED_ORIGINS=https://ghm-staging.example.com,http://localhost:5173,http://127.0.0.1:5173
```

### Bước 2: Khởi chạy cụm dịch vụ bằng Docker Compose

```bash
# Chạy kèm Caddy HTTPS reverse proxy trên máy chủ staging:
docker compose -f compose.yaml -f compose.staging.yaml up -d --build

# Hoặc chạy thử nghiệm trên máy local tại cổng 5173:
docker compose up -d --build
```

### Bước 3: Nạp dữ liệu mẫu phục vụ tác vụ `T2..T7`

```bash
python3 deploy/seed_staging.py \
    --base-url https://ghm-staging.example.com \
    --out-json docs/week-11/evidence/staging-seed-summary.json
```

### Bước 4: Kiểm tra thông suốt 7 tác vụ chuẩn (`T1..T7`)

1. Mở trình duyệt truy cập URL staging (hoặc `http://localhost:5173` khi kiểm thử local).
2. Kiểm tra kho `spring-projects/spring-petclinic` đã có sẵn snapshot hoàn chỉnh để người tham gia thực hiện ngay `T2..T7`.
3. Dùng URL `https://github.com/spring-guides/gs-rest-service` để kiểm tra tác vụ đăng ký và phân tích mới (`T1`).
