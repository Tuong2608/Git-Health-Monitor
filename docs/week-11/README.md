# Tuần 11 & Chặng Về Đích — Trần Quang Toản (23110158)

Thực hiện ngày 24/11/2026 trên nhánh `feat/toan-week-11` (phát triển từ `main` `01f9ed1`). Bộ tài liệu và mã nguồn này hoàn tất toàn bộ phần việc của Trần Quang Toản từ **Tuần 11 đến ngày Bảo vệ Tiểu luận chuyên ngành** theo đúng lộ trình `KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx`:

1. **Chặng 1 (`STT 06, 27, 28, 33, 34` — Chốt kỹ thuật trước CODE FREEZE):**
   - Tham số hóa `compose.yaml` + `.env.example`, bổ sung `compose.staging.yaml`, `deploy/Caddyfile` và script nạp dữ liệu mẫu `deploy/seed_staging.py` cho máy chủ Staging sống (Cách 1).
   - Thực hiện 4 cải tiến giao diện React trên `AnalysisDashboard.tsx` & `App.css` phục vụ 7 tác vụ khảo sát `RQ3`, bổ sung ca kiểm thử Playwright E2E thứ 12 (`10/10` ca mock PASS) và chụp trọn bộ ảnh **Before / After** trên Desktop (`1440px`) và Mobile (`375px`).
2. **Chặng 2 (`STT 36, 37, 38` — Bản thảo Tiểu luận, Slide & Kịch bản Demo 5 phút):**
   - Soạn đầy đủ văn bản học thuật **Chương 2, Chương 3 (phần hệ thống), Chương 4 (phần RQ2, Benchmark PostgreSQL, Kiểm thử 171 test / JaCoCo 89.07%, RQ3 & UI Before/After) và Phụ lục cài đặt** để ghép trực tiếp vào cuốn Tiểu luận.
   - Soạn chi tiết nội dung từng trang **Slide thuyết trình** và **Kịch bản bấm Demo 5 phút**.
3. **Chặng 3 (`STT 02, 39` — Cẩm nang Ôn code chéo 3 buổi & Hỏi-đáp Hội đồng):**
   - Soạn tài liệu ôn code chéo 3 buổi (Buổi 1 Tưởng giảng cho Toản, Buổi 2 Toản giảng cho Tưởng, Buổi 3 trả lời 5 câu hỏi phản biện cốt lõi của Hội đồng kèm chỉ mục từng file/hàm trong repository).

---

## Danh mục tài liệu Tuần 11 & Chặng Về Đích

| Tài liệu / Minh chứng | Nội dung chính |
|---|---|
| [Báo cáo Tuần 11 & Về Đích (Toản)](bao-cao-tuan-11-toan.md) | Tổng hợp toàn bộ sản phẩm kỹ thuật, kiểm thử và tài liệu của Toản trong Chặng 1–3 |
| [Cải tiến giao diện Before / After (`STT 34`)](ui-improvements-before-after.md) | Phân tích 4 cải tiến UI theo tác vụ `T2, T3, T5, T6, T7` kèm bộ ảnh [Before Desktop](evidence/ui-before-desktop.png), [After Desktop](evidence/ui-after-desktop.png), [Before Mobile](evidence/ui-before-mobile.png), [After Mobile](evidence/ui-after-mobile.png) |
| [Hướng dẫn triển khai Staging Cách 1 (`STT 27, 28`)](staging-runbook.md) | Hướng dẫn chạy `compose.staging.yaml` + Caddy HTTPS và lệnh `deploy/seed_staging.py` nạp kho mẫu cho RQ3 |
| [Bản thảo Tiểu luận phần Toản (Chương 2, 3, 4 & Phụ lục) (`STT 36, 37`)](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) | Toàn văn Chương 2, Chương 3 (hệ thống), Chương 4 (RQ2, kiểm thử, JaCoCo, RQ3/UI) và Phụ lục chuẩn format HCMUTE |
| [Slide, Kịch bản Demo 5 phút & Cẩm nang Ôn bảo vệ 3 buổi (`STT 38, 39`)](slide-demo-va-on-bao-ve-toan.md) | Nội dung Slide từng trang, kịch bản Demo 5 phút và tài liệu giảng code chéo + trả lời 5 câu hỏi Hội đồng |
| [Bản đồ Code từng hàm, Tech Stack & VS Code Extensions](ban-do-code-tung-ham-va-stack.md) | Giải thích 100% lớp/hàm trong dự án, lý do chọn từng công nghệ trong Tech Stack và hướng dẫn dùng VS Code CodeTour (`.tours/on-code-git-health-monitor.tour`) |
| [Nhật ký sử dụng AI Tuần 11 (Toản)](ai-usage-toan-week11.md) | Nhật ký AI học thuật theo từng hoạt động, ghi rõ lỗi tràn ngang Mobile `375px` phát hiện qua Playwright và cách khắc phục |
| [Biên bản kiểm chứng (`verification.md`)](evidence/verification.md) | Tổng hợp kết quả kiểm thử: 122 JUnit (`89.07%` JaCoCo), 37 Python test, 12 Playwright E2E test |
| [Bàn giao cho Tưởng](ban-giao-tuong.md) & [Nội dung PR](noi-dung-pr.md) | Tóm tắt bàn giao nhánh `feat/toan-week-11` và mô tả Pull Request |
