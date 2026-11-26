# Nhật ký sử dụng trí tuệ nhân tạo — Tuần 11 & Chặng Về Đích

- **Người phụ trách:** Trần Quang Toản (`23110158`)
- **Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git (`Git Health Monitor`)
- **Ngày thực hiện:** 24/11/2026
- **Nhánh phát triển:** `feat/toan-week-11` (phát triển từ `main` `01f9ed1`)
- **Nguyên tắc ghi nhận:** Trình bày học thuật theo từng hoạt động độc lập (Mục tiêu, Phạm vi AI hỗ trợ, Kiểm chứng & Kết quả, Lỗi AI thực tế phát hiện & Cách khắc phục); không sao chép nguyên văn câu lệnh ban đầu của người dùng.

---

## 1. Hoạt động 1 — Phân tích Kế hoạch Về Đích và thiết kế cấu hình Docker Compose Staging (`STT 06, 27, 28`)

- **Mục tiêu:** Đọc tài liệu `KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx`, đối chiếu cấu hình `compose.yaml` hiện tại và khắc phục điểm gắn cứng `APP_ALLOWED_ORIGINS` cùng cổng bind frontend để sẵn sàng triển khai lên máy chủ Staging chạy HTTPS (Cách 1).
- **Phạm vi AI hỗ trợ:**
  - Cập nhật `compose.yaml` và `.env.example` để tham số hóa `APP_ALLOWED_ORIGINS`, `APP_REGISTRATION_ENABLED`, `APP_ANALYSIS_CACHE_DIR`, `FRONTEND_BIND_HOST` và `FRONTEND_PORT` với giá trị mặc định tương thích 100% với GitHub Actions CI.
  - Soạn thảo `compose.staging.yaml`, `deploy/Caddyfile` (reverse proxy HTTPS tự động qua Caddy 2.9) và script `deploy/seed_staging.py` tự động nạp kho mẫu `spring-petclinic`, `commons-csv` cùng quy tắc cảnh báo cho các tác vụ `T2..T7`.
- **Cách kiểm chứng:** Đối chiếu với bước `Compose integration smoke` trong `.github/workflows/ci.yaml` để đảm bảo khi không truyền biến môi trường mới, container `frontend` vẫn bind `127.0.0.1:5173:80` và `backend` vẫn cho phép origin `http://localhost:5173,http://127.0.0.1:5173`.

---

## 2. Hoạt động 2 — Cải tiến giao diện React phục vụ RQ3 và kiểm thử Playwright Before/After (`STT 34`)

- **Mục tiêu:** Thực hiện vòng cải tiến giao diện (UI/UX) trên `AnalysisDashboard.tsx` và `App.css` nhằm giải quyết các điểm dễ gây hiểu nhầm trong 7 tác vụ chuẩn (`T1..T7`), chụp bộ ảnh Before/After ở cả chế độ Desktop (`1440 × 1000`) và Mobile (`375 × 812`), đồng thời bổ sung kiểm thử tự động Playwright E2E.
- **Phạm vi AI hỗ trợ:**
  - Bổ sung hướng dẫn xác định nhanh Top 5 Hotspot (`T2`), tooltip và hộp mở rộng `<details className="metric-guide">` giải thích công thức `Hotspot`, `Max CCN`, `HHI` (`T3`), thông báo phân biệt `LINEAGE_AMBIGUOUS` với `NO_FUNCTIONS` khi `coverageComplete = false` (`T5`), chú thích điều kiện kích hoạt cảnh báo (`T6`) và thanh quy trình 3 bước kèm mã băm `contextHash` SHA-256 trong `AiReview` (`T7`).
  - Viết thêm ca kiểm thử Playwright thứ 12 (`week 11 RQ3 UI guidance exposes metric formulas, AI 3-step contextHash and coverage explanation`) trong `frontend/e2e/week8.spec.ts`.
- **Lỗi AI phát hiện và khắc phục (Lỗi thực tế trong Tuần 11):**
  - *Biểu hiện lỗi:* Ở lần chạy Playwright đầu tiên sau khi thêm hộp `.metric-guide` và `.context-hash`, ca kiểm thử thứ 12 thất bại tại dòng kiểm tra bố cục di động `expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)` khi thu nhỏ viewport từ `1440 × 1000` xuống `375 × 812` (`Received: false`).
  - *Nguyên nhân gốc:* Khi người dùng mở hộp `<details className="metric-guide">` đồng thời biểu đồ Recharts `<ResponsiveContainer>` trong `.chart` vừa render ở kích thước `1440px`, việc thu nhỏ viewport đột ngột xuống `375px` khiến thẻ `<svg>` bên trong `.chart` và các khối con trong `.subsection` / `.file-detail` chưa có thuộc tính `min-width: 0; max-width: 100%; overflow: hidden` giữ nguyên chiều rộng cũ trong DOM, gây tràn ngang (`scrollWidth > 375`).
  - *Cách sửa:* Bổ sung quy tắc CSS `.subsection, .file-detail, .chart, .metric-guide, .context-hash { min-width: 0; max-width: 100%; overflow-wrap: anywhere; } .chart { overflow: hidden; }` vào `frontend/src/App.css`. Sau khi sửa, chạy lại `npm run lint`, `npm run build` và `npm run test:e2e` đạt **10/10 ca mock PASS** (2 ca live skip khi không bật cờ live) và xuất thành công bộ ảnh Before/After không tràn ngang trên cả Desktop lẫn Mobile.

---

## 3. Hoạt động 3 — Biên soạn bản thảo Tiểu luận (Chương 2, 3, 4, Phụ lục), Slide, Kịch bản Demo và Cẩm nang Ôn bảo vệ (`STT 36, 37, 38, 39`)

- **Mục tiêu:** Chuẩn bị đầy đủ nội dung học thuật thuộc phần phân công của Toản trong Chặng 2 và Chặng 3 để hai thành viên ghép trực tiếp vào cuốn Tiểu luận chuyên ngành, làm Slide thuyết trình và ôn code chéo 3 buổi.
- **Phạm vi AI hỗ trợ:**
  - Tổng hợp số liệu thực tế từ mã nguồn (`V1..V4`, `MonitoringStore`, `AnalysisWorker`, `SnapshotPublisher`, `AlertService`, `SnapshotQueries`, `ReviewContextBuilder`, `AiReviewController`, `AnalysisDashboard.tsx`) và các báo cáo thực nghiệm (`RQ2`, `Benchmark PostgreSQL`, `JaCoCo 89.07%`, `UI Before/After`) thành bản thảo [tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md).
  - Soạn cấu trúc Slide phần của Toản, kịch bản bấm Demo 5 phút và tài liệu ôn code chéo 3 buổi trong [slide-demo-va-on-bao-ve-toan.md](slide-demo-va-on-bao-ve-toan.md).
- **Cách kiểm chứng & Trách nhiệm sinh viên:** Đối chiếu toàn bộ tên bảng, tên cột, mã lỗi HTTP (`202`, `403`, `404`, `409`, `429`, `502`, `503`), công thức metric và con số kiểm thử (`122` JUnit, `37` Python, `12` Playwright E2E, `89.07%` JaCoCo line coverage) khớp 100% với repository. Toản cần tự đọc lại, thực hành mở từng file trong IDE để giải thích lưu loát khi ôn chéo với Tưởng và khi bảo vệ trước Hội đồng.
