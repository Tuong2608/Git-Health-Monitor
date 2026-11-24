# Kiểm chứng tuần 11 & Chặng Về Đích

Ngày thực hiện: 24/11/2026. Môi trường kiểm chứng của Toản: Windows 11, JDK 21 (`ms-21.0.10`), PostgreSQL 18.3 local port `55432`, Python 3.12.14, Lizard 1.24.3, Node 24.11.1, Playwright Chromium 153.

---

## 1. Bảng tổng hợp kết quả kiểm thử trước mốc CODE FREEZE

| Hạng mục kiểm chứng | Kết quả đạt được | Nguồn minh chứng |
|---|---|---|
| **Frontend Lint & Production Build** | `npm run lint` PASS (`0` lỗi/cảnh báo); `npm run build` PASS (`tsc -b && vite build`) | `frontend/` |
| **Playwright UI E2E Tests (12 ca)** | **10 / 10 ca chế độ mock PASS** (gồm ca thứ 12 kiểm chứng 4 cải tiến UI Tuần 11 và chống tràn ngang Mobile `375 × 812`); 2 ca live chạy khi bật backend live | `frontend/e2e/week8.spec.ts` |
| **Minh chứng hình ảnh UI Before / After (`STT 34`)** | Đủ 4 ảnh độ phân giải chuẩn Desktop (`1440 × 1000`) và Mobile (`375 × 812`) trước và sau cải tiến | [ui-before-desktop.png](ui-before-desktop.png), [ui-after-desktop.png](ui-after-desktop.png), [ui-before-mobile.png](ui-before-mobile.png), [ui-after-mobile.png](ui-after-mobile.png) |
| **Python Experiment Unit Tests** | **37 / 37 ca PASS** (`0` lỗi) | `python -m unittest discover -s tools/experiments -v` |
| **Backend JUnit + PostgreSQL 18.3 + JaCoCo `0.8.12`** | **122 / 122 ca PASS** (`0` lỗi, `0` skip); độ phủ dòng lệnh toàn hệ thống **89.07%** (`1.630/1.830` dòng) | [jacoco-fullstack-coverage.json](../../week-10/evidence/jacoco-fullstack-coverage.json) |
| **Cấu hình Docker Compose & Staging Overlay** | Tham số hóa `APP_ALLOWED_ORIGINS` & cổng frontend tương thích ngược 100% với CI; có `compose.staging.yaml`, `deploy/Caddyfile` và `deploy/seed_staging.py` | [compose.yaml](../../../compose.yaml), [compose.staging.yaml](../../../compose.staging.yaml), [deploy/seed_staging.py](../../../deploy/seed_staging.py) |

---

## 2. Nguyên tắc trung thực thực nghiệm

- Toàn bộ 4 ảnh `ui-before-*.png` và `ui-after-*.png` được chụp tự động bằng trình duyệt Chromium thật qua Playwright ở đúng kích thước `1440 × 1000` và `375 × 812`.
- Kết quả khảo sát người dùng `RQ3` (`STT 33`) được để trống phần số liệu người thật cho tới khi nhóm thu đủ 8–10 phiếu từ Google Form và chạy `evaluate_rq3_sus.py`.
