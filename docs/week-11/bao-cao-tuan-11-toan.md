# Báo cáo tuần 11 & Chặng Về Đích — Trần Quang Toản

- **Họ tên & MSSV:** Trần Quang Toản (`23110158`)
- **Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git (`Git Health Monitor`)
- **Ngày thực hiện:** 24/11/2026
- **Nhánh phát triển:** `feat/toan-week-11` (phát triển từ `main` `01f9ed1` sau PR #14)
- **Tài liệu đối chiếu:** `KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx` (Lộ trình 3 chặng từ Tuần 11 đến Bảo vệ)

---

## 1. Tổng quan các hạng mục đã hoàn tất trong đợt triển khai Về Đích

Bám sát phân công của Toản trong **Kế hoạch Về Đích (Chặng 1, Chặng 2 và Chặng 3)**, nhánh `feat/toan-week-11` đã hoàn thành trọn bộ phần kỹ thuật trước mốc `CODE FREEZE` và soạn sẵn toàn bộ bản thảo Tiểu luận, Slide, Kịch bản Demo 5 phút cùng Cẩm nang Ôn code chéo 3 buổi:

| Chặng theo kế hoạch | Đầu việc của Toản | Sản phẩm & Minh chứng đã hoàn thành |
|---|---|---|
| **Chặng 1A — Cấu hình Docker Compose Staging sống & Script nạp dữ liệu mẫu (`STT 06, 27, 28`)** | Tham số hóa `APP_ALLOWED_ORIGINS`, `FRONTEND_BIND_HOST`, `FRONTEND_PORT` trong `compose.yaml`; tạo `compose.staging.yaml` kèm `deploy/Caddyfile` (HTTPS tự động) và script `deploy/seed_staging.py` tự động nạp `spring-petclinic` + `commons-csv` và quy tắc cảnh báo cho `T2..T7`. | [compose.yaml](../../compose.yaml), [compose.staging.yaml](../../compose.staging.yaml), [deploy/Caddyfile](../../deploy/Caddyfile), [deploy/seed_staging.py](../../deploy/seed_staging.py), [staging-runbook.md](staging-runbook.md) |
| **Chặng 1B — Cải tiến giao diện React cho RQ3 + Bộ ảnh Before/After + Playwright E2E (`STT 34`)** | Bổ sung 4 cải tiến UI trong `AnalysisDashboard.tsx` & `App.css`: (1) chỉ dẫn Top 5 Hotspot (`T2`), (2) hộp công thức & tooltip `Hotspot`, `Max CCN`, `HHI` (`T3`), (3) giải thích rõ `LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS` khi `coverageComplete = false` và điều kiện kích hoạt cảnh báo (`T5, T6`), (4) thanh quy trình 3 bước & hiển thị `contextHash` SHA-256 trong AI Review (`T7`). Chụp đủ bộ ảnh Before/After Desktop & Mobile và thêm ca Playwright E2E thứ 12. | [AnalysisDashboard.tsx](../../frontend/src/components/AnalysisDashboard.tsx), [ui-improvements-before-after.md](ui-improvements-before-after.md), [ui-before-desktop.png](evidence/ui-before-desktop.png), [ui-after-desktop.png](evidence/ui-after-desktop.png), [ui-before-mobile.png](evidence/ui-before-mobile.png), [ui-after-mobile.png](evidence/ui-after-mobile.png) |
| **Chặng 1C — Điều phối khảo sát người dùng RQ3 (`STT 33`)** | Chuẩn bị sẵn môi trường, đề bài 7 tác vụ chuẩn (`T1..T7`), bảng hỏi SUS 10 câu và lệnh chấm tự động `evaluate_rq3_sus.py`. **Giữ nguyên tắc trung thực học thuật:** không tự điền phiếu khảo sát giả; chờ thu thập 8–10 phiếu từ bạn bè thực tế trên Google Form rồi chạy lệnh xuất `docs/week-11/evidence/rq3-results.json`. | [rq3-user-study-protocol.md](../week-10/rq3-user-study-protocol.md), [mau-khao-sat-rq3-gui-nguoi-tham-gia.txt](../week-10/mau-khao-sat-rq3-gui-nguoi-tham-gia.txt) |
| **Chặng 2A — Bản thảo Tiểu luận phần của Toản (`STT 36, 37`)** | Soạn hoàn chỉnh văn bản học thuật chuẩn cấu trúc Khoa CNTT HCMUTE cho toàn bộ phần Toản phụ trách: **Chương 2** (SRS `UC01..UC11`, `BR01..BR18`, `NFR01..NFR08`, Kiến trúc Modular Monolith, ERD 11 bảng `V1..V4`, `ADR-001/002`), **Chương 3 — Phần hệ thống** (Flyway `V1..V4`, Queue PostgreSQL không Redis, `SnapshotPublisher`, `AlertService`, REST API, React UI, Docker/CI), **Chương 4 — Phần của Toản** (Thực nghiệm RQ2 `S1..S5` & phân rã 369 file, Benchmark PostgreSQL, Kiểm thử 171 test & JaCoCo 89.07%, RQ3 & cải tiến UI Before/After) và **Phụ lục cài đặt**. | [tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) |
| **Chặng 2B & Chặng 3 — Nội dung Slide, Kịch bản Demo 5 phút & Cẩm nang Ôn code chéo 3 buổi (`STT 38, 39`)** | Soạn chi tiết nội dung từng trang Slide phần Toản, kịch bản bấm Demo 5 phút (kèm kịch bản quay video dự phòng) và bộ tài liệu giảng code chéo (Buổi 1, Buổi 2 Toản giảng cho Tưởng, Buổi 3 trả lời 5 câu hỏi phản biện Hội đồng kèm chỉ mục dòng code). | [slide-demo-va-on-bao-ve-toan.md](slide-demo-va-on-bao-ve-toan.md) |

---

## 2. Kết quả kiểm thử kỹ thuật trước mốc CODE FREEZE

- **Backend JUnit Tests + PostgreSQL 18.3 (lượt chạy 18/11; tuần 11 không sửa mã backend):** **122 / 122 ca PASS** (`0` lỗi, `0` skip); độ phủ JaCoCo `0.8.12` full-stack đạt **89.07% line coverage** (`repository` 100%, `api` 100%, `monitoring` 94.60%, `gitanalyzer` 88.12%, `aireview` 85.88%).
- **NFR01/NFR02 đo thật (25/11):** `POST /analyses` trả `202` tối đa 46,00 ms ($n=2$); `GET /hotspots?size=100` p95 28,42 ms; `GET /files/{id}` p95 28,05 ms — xem [nfr01-nfr02-api-latency.json](evidence/nfr01-nfr02-api-latency.json). Script: `tools/experiments/measure_api_latency.py`.
- **Python Experiment Unit Tests:** **37 / 37 ca PASS** (`0` lỗi).
- **Frontend Lint, Build & Playwright E2E:**
  - `npm run lint` và `npm run build` PASS.
  - Bộ kiểm thử Playwright E2E tăng từ 11 lên **12 ca** (bổ sung ca kiểm thử cải tiến giao diện Tuần 11): **10 / 10 ca chế độ mock PASS** (2 ca live chạy khi bật backend live), kiểm chứng cả `scrollWidth <= window.innerWidth` trên màn hình di động `375 × 812`.
- **Docker Compose Config Validation:** `docker compose config` và `docker compose -f compose.yaml -f compose.staging.yaml config` hợp lệ.

---

## 3. Hai thao tác thực tế Toản và Tưởng cần thực hiện ngoài đời thực

1. **Dựng VM Staging & Thu phiếu khảo sát RQ3 thật (`STT 27, 28, 33`):**
   - Chạy `docker compose -f compose.yaml -f compose.staging.yaml up -d --build` và `python3 deploy/seed_staging.py` trên VM Ubuntu (Azure for Students / VPS).
   - Mời 8–10 bạn thực hiện 7 tác vụ và điền Google Form, tải file CSV về và chạy:
     ```powershell
     & .local/week8-python/Scripts/python.exe tools/experiments/evaluate_rq3_sus.py rq3-responses.csv --out-json docs/week-11/evidence/rq3-results.json
     ```
2. **Ghép file Word Tiểu luận & Luyện 3 buổi ôn chéo (`STT 36..39`):**
   - Chép nội dung từ [tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) ghép với Chương 1, phần thuật toán Chương 3, phần RQ1/Benchmark Chương 4 và Kết luận của Tưởng.
   - Dùng [slide-demo-va-on-bao-ve-toan.md](slide-demo-va-on-bao-ve-toan.md) để làm Slide, quay video demo 5 phút và ôn code chéo 3 buổi.
