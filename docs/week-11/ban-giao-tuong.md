# Bàn giao tuần 11 & Chặng Về Đích cho Tưởng

Người gửi: Trần Quang Toản (`23110158`). Ngày: 24/11/2026. Nhánh: `feat/toan-week-11` (phát triển từ `main` `01f9ed1`).

Mở [README tuần 11](README.md), [báo cáo cá nhân tuần 11](bao-cao-tuan-11-toan.md), [cải tiến giao diện Before/After](ui-improvements-before-after.md), [hướng dẫn dựng Staging Cách 1](staging-runbook.md), [bản thảo Tiểu luận phần của Toản](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) và [Slide, Demo & Cẩm nang ôn bảo vệ](slide-demo-va-on-bao-ve-toan.md) để review.

---

## 1. Các hạng mục Toản đã hoàn tất theo Kế hoạch Về Đích (`KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx`)

| Hạng mục trong Kế hoạch Về Đích | Nội dung đã hoàn tất | Minh chứng |
|---|---|---|
| **Mục 3.1 — Chuẩn bị Docker Compose Staging sống & Nạp dữ liệu mẫu (`STT 06, 27, 28`)** | Tham số hóa `APP_ALLOWED_ORIGINS` và cổng bind frontend trong `compose.yaml` (giữ mặc định tương thích 100% với CI); thêm `compose.staging.yaml` + `deploy/Caddyfile` (HTTPS tự động) và script `deploy/seed_staging.py` tự động nạp `spring-petclinic` + `commons-csv` cùng quy tắc cảnh báo cho `T2..T7`. | [compose.yaml](../../compose.yaml), [compose.staging.yaml](../../compose.staging.yaml), [deploy/seed_staging.py](../../deploy/seed_staging.py), [staging-runbook.md](staging-runbook.md) |
| **Mục 3.3 — Cải tiến giao diện React cho RQ3 + Bộ ảnh Before/After + Playwright E2E (`STT 34`)** | Bổ sung 4 cải tiến trực quan trên `AnalysisDashboard.tsx` & `App.css`: chỉ dẫn Top 5 Hotspot (`T2`), tooltip & hộp công thức `Hotspot`/`Max CCN`/`HHI` (`T3`), giải thích rõ `LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS` khi `coverageComplete = false` và điều kiện vượt ngưỡng cảnh báo (`T5, T6`), thanh quy trình 3 bước & hiển thị `contextHash` SHA-256 trong AI Review (`T7`). Chụp đủ 4 ảnh Before/After (Desktop `1440px` & Mobile `375px`) và bổ sung ca Playwright E2E thứ 12 (`10/10` ca mock PASS). | [ui-improvements-before-after.md](ui-improvements-before-after.md), [ui-before-desktop.png](evidence/ui-before-desktop.png), [ui-after-desktop.png](evidence/ui-after-desktop.png), [ui-before-mobile.png](evidence/ui-before-mobile.png), [ui-after-mobile.png](evidence/ui-after-mobile.png) |
| **Mục 4 — Bản thảo Tiểu luận phần của Toản (`STT 36, 37`)** | Soạn hoàn chỉnh văn bản học thuật cho **Chương 2** (SRS `UC01..UC11`, `BR01..BR18`, `NFR01..NFR08`, Kiến trúc Modular Monolith, ERD 11 bảng `V1..V4`, `ADR-001/002`), **Chương 3 — Phần hệ thống** (Flyway `V1..V4`, Queue PostgreSQL không Redis, `SnapshotPublisher`, `AlertService`, REST API, React UI, Docker/CI), **Chương 4 — Phần của Toản** (Thực nghiệm RQ2, Benchmark PostgreSQL, Kiểm thử 171 test & JaCoCo `89.07%`, RQ3 & cải tiến UI Before/After) và **Phụ lục cài đặt**. | [tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) |
| **Mục 4 & 5 — Nội dung Slide, Kịch bản Demo 5 phút & Tài liệu Ôn code chéo 3 buổi (`STT 38, 39`)** | Soạn chi tiết nội dung từng trang Slide phần Toản, kịch bản bấm Demo 5 phút và cẩm nang ôn code chéo (Buổi 1, Buổi 2 Toản giảng cho Tưởng, Buổi 3 trả lời 5 câu hỏi phản biện Hội đồng kèm chỉ dẫn từng file/dòng code). | [slide-demo-va-on-bao-ve-toan.md](slide-demo-va-on-bao-ve-toan.md) |

---

## 2. Phối hợp đóng băng mã nguồn (`CODE FREEZE`) và thu phiếu RQ3

1. **Khi VM Staging chạy lên:** Chạy `python3 deploy/seed_staging.py --base-url https://<domain-staging>` để nạp sẵn `spring-petclinic` và `commons-csv`.
2. **Khi thu đủ 8–10 phiếu Google Form từ bạn bè:** Xuất CSV và chạy `python tools/experiments/evaluate_rq3_sus.py rq3-responses.csv --out-json docs/week-11/evidence/rq3-results.json`, sau đó điền các con số `Task Success Rate (%)` và `Mean SUS` thực tế vào mục `[CHỜ SỐ LIỆU KHẢO SÁT THỰC TẾ]` trong Chương 4 của cuốn Tiểu luận.
3. **Review & Merge PR cuối:** Nhờ Tưởng review PR nhánh `feat/toan-week-11` để chốt `CODE FREEZE` đúng lộ trình.
