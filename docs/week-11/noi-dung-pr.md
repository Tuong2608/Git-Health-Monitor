## Vấn đề và thay đổi

Hoàn thiện phần việc Tuần 11 và Chặng Về Đích của Trần Quang Toản (`23110158`) trên nhánh `feat/toan-week-11` theo đúng lộ trình `KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx`:

1. **Cấu hình Docker Compose Staging sống & Script nạp dữ liệu mẫu (`STT 06, 27, 28`):**
   - Tham số hóa `APP_ALLOWED_ORIGINS`, `APP_REGISTRATION_ENABLED`, `APP_ANALYSIS_CACHE_DIR`, `FRONTEND_BIND_HOST` và `FRONTEND_PORT` trong `compose.yaml` và `.env.example` (giữ mặc định tương thích 100% với GitHub Actions CI).
   - Bổ sung `compose.staging.yaml`, `deploy/Caddyfile` (HTTPS reverse proxy tự động) và `deploy/seed_staging.py` (tự động nạp `spring-petclinic`, `commons-csv` và quy tắc cảnh báo cho `T2..T7`).
2. **Cải tiến giao diện React phục vụ RQ3 + Bộ ảnh Before/After + Playwright E2E (`STT 34`):**
   - Bổ sung 4 cải tiến trực quan trên `AnalysisDashboard.tsx` & `App.css`: hướng dẫn xác định Top 5 Hotspot (`T2`), tooltip & hộp công thức `Hotspot`/`Max CCN`/`HHI` (`T3`), giải thích rõ `LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS` khi `coverageComplete = false` và điều kiện kích hoạt cảnh báo (`T5, T6`), thanh quy trình 3 bước & hiển thị `contextHash` SHA-256 trong AI Review (`T7`).
   - Chụp đủ bộ ảnh Before/After (`ui-before-desktop.png`, `ui-before-mobile.png`, `ui-after-desktop.png`, `ui-after-mobile.png`) và thêm ca kiểm thử Playwright E2E thứ 12 trong `frontend/e2e/week8.spec.ts`.
3. **Bản thảo Tiểu luận (Chương 2, 3, 4, Phụ lục), Slide, Kịch bản Demo 5 phút & Cẩm nang Ôn bảo vệ (`STT 36, 37, 38, 39`):**
   - Soạn đầy đủ nội dung học thuật phần của Toản trong `docs/week-11/tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md` và `docs/week-11/slide-demo-va-on-bao-ve-toan.md`.

## Kết quả kiểm chứng

- **Backend JUnit + PostgreSQL 18.3:** `122/122` ca kiểm thử PASS (`89.07%` JaCoCo full-stack line coverage).
- **Python Experiment Unit Tests:** `37/37` ca kiểm thử PASS.
- **Frontend Lint, Build & Playwright E2E:** `npm run lint` PASS; `npm run build` PASS; Playwright E2E **10/10 ca chế độ mock PASS** (tổng 12 ca gồm 2 ca live), kiểm chứng không tràn ngang trên màn hình di động `375 × 812`.

## Cần review

- Không tự tạo dữ liệu khảo sát người dùng giả cho `RQ3` (`STT 33`); sau khi thu đủ 8–10 phiếu thực tế từ Google Form, chạy `evaluate_rq3_sus.py` để xuất `docs/week-11/evidence/rq3-results.json`.
