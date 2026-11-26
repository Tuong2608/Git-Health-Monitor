# Báo cáo tiến độ Tuần 11 — Nhóm Git Health Monitor (23110170 & 23110158)

- **Kho mã nguồn:** https://github.com/Tuong2608/Git-Health-Monitor (nhánh `main`, hợp nhất **PR #15** của Toản và **PR #16** của Tưởng)
- **Tổng tiến độ hiện tại:** **30 Hoàn thành / 8 Đang làm / 1 Chưa bắt đầu** (chốt xong `STT 06`, đưa `STT 27, 28, 33, 34, 36, 37, 38` sang giai đoạn hoàn thiện cuối cùng để chuẩn bị ghép cuốn Tiểu luận và thu phiếu người dùng thật trên staging).

---

## 1. Kết quả nổi bật đạt được trong Tuần 11

1. **Hoàn tất cấu hình triển khai Staging sống Cách 1 (`STT 06, 27, 28 — PR #15`):**
   - Tham số hóa `APP_ALLOWED_ORIGINS`, `APP_REGISTRATION_ENABLED`, `APP_ANALYSIS_CACHE_DIR` và cổng bind frontend trong `compose.yaml` + `.env.example` (tương thích ngược 100% với CI); bổ sung `compose.staging.yaml`, `deploy/Caddyfile` (HTTPS tự động qua Caddy 2.9), `deploy/seed_staging.py` (tự động nạp `spring-petclinic`, `commons-csv` và rule cảnh báo) cùng `docs/week-11/staging-runbook.md`.
   - Đã đo độ trễ API thực tế đối chiếu `NFR01/NFR02` trên backend + PostgreSQL 18 (`measure_api_latency.py` -> `nfr01-nfr02-api-latency.json`): `POST /analyses` trả `202` trong tối đa `46,00 ms` (ngưỡng `<= 2.000 ms`), `GET /hotspots?size=100` `p95 = 28,42 ms`, `GET /files/{id}` `p95 = 28,05 ms` (ngưỡng `<= 1.000 ms`).
2. **Hoàn thành vòng cải tiến giao diện trước RQ3 & nâng cấp kiểm thử UI (`STT 34 — PR #15`):**
   - Bổ sung 4 cải tiến trực quan trên `AnalysisDashboard.tsx` & `App.css`: hướng dẫn Top 5 Hotspot (`T2`), hộp công thức & tooltip `Hotspot`, `Max CCN`, `HHI` (`T3`), giải thích phân biệt `LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS` khi `coverageComplete = false` + điều kiện vượt ngưỡng cảnh báo (`T5, T6`), thanh quy trình 3 bước & hiển thị `contextHash` SHA-256 trong AI Review (`T7`).
   - Tự phát hiện và sửa lỗi CSS tràn ngang trên màn hình Mobile `375 x 812` khi thu gọn viewport với `<details className="metric-guide">`; chụp đủ 4 ảnh Before/After (`1440px` & `375px`) và thêm ca Playwright E2E thứ 12 (`10/10` ca mock PASS).
3. **Hoàn thiện bộ công cụ & hướng dẫn thu thập Google Form RQ3 (`STT 33 — PR #16`):**
   - Sửa lỗi trong `evaluate_rq3_sus.py` để hỗ trợ đầy đủ các nhãn tiếng Việt từ Google Form (`Đạt (1)`, `Không đạt (0)`, `Không đạt`), chuẩn hóa khoảng trắng tiêu đề và bỏ qua dòng trống; thêm unit test mới nâng bộ test Python lên **38/38 PASS**.
   - Tạo `rq3-responses-template.csv` (chỉ gồm tiêu đề 27 cột, giữ nguyên nguyên tắc trung thực không tạo dữ liệu giả) và `rq3-google-form-setup-and-csv-guide.md`.
4. **Hoàn thành gần như toàn bộ bản thảo Tiểu luận, Slide 18 trang & Cẩm nang ôn code chéo 3 buổi (`STT 36, 37, 38 — PR #15 & PR #16`):**
   - Bản thảo Tiểu luận của hai thành viên khớp nối liền mạch từ Chương 1 đến Chương 5, Tài liệu tham khảo và Phụ lục (`tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md` + `tieu-luan-phan-tuong-chuong-1-3-4-ket-luan-tltk.md`).
   - Bộ Slide 18 trang chia đều 9-9 giữa Tưởng và Toản (`slide-phan-tuong-va-de-cuong-buoi-1.md` + `slide-demo-va-on-bao-ve-toan.md`), kèm kịch bản Demo 5 phút, bản đồ code 100% lớp/hàm (`ban-do-code-tung-ham-va-stack.md`), VS Code CodeTour `.tours/on-code-git-health-monitor.tour` và đáp án chuẩn 5 câu hỏi phản biện Hội đồng.

---

## 2. Tổng hợp kiểm thử hiện tại

- **122/122** test JUnit backend trên PostgreSQL 18.3 (`89,07%` JaCoCo line coverage — lượt chạy trước code freeze; tuần 11 không sửa mã backend).
- **12** ca Playwright E2E frontend (`10/10` ca mock PASS sau cải tiến UI tuần 11; 2 ca live chạy khi bật backend live).
- **38/38** unit test Python (`tools/experiments/`, chạy sau bản sửa `evaluate_rq3_sus.py` trong PR #16).
- Tổng cộng: **172 ca kiểm thử tự động** + **5/5** job GitHub Actions CI xanh.

---

## 3. Việc còn lại cho Chặng Về Đích

1. Dựng máy chủ Ubuntu (Azure for Students / VPS) chạy `compose.staging.yaml` + `deploy/seed_staging.py` và mời 8–10 người thực hiện 7 tác vụ RQ3 trên Google Form, chạy `evaluate_rq3_sus.py` sinh `rq3-results.json` (và bổ sung chỉnh UI theo góp ý thực tế nếu có).
2. Ghép bản thảo hai thành viên thành file Word Tiểu luận chính thức, xuất bộ Slide `.pptx`, quay 1 video demo dự phòng và chạy 3 buổi ôn code chéo trước khi bảo vệ.
