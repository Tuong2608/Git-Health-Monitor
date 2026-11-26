# Tuần 11 & Chặng Về Đích — Nhóm Git Health Monitor (PR #15 & PR #16)

Thư mục này tổng hợp toàn bộ kết quả Tuần 11 và chuẩn bị Chặng Về Đích của cả hai thành viên theo đúng lộ trình `KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx`:

- **Phần của Trần Quang Toản (`23110158` — PR #15, đã merge `a33acb5`):** Cấu hình Docker Compose Staging sống Cách 1 (`compose.staging.yaml`, `deploy/Caddyfile`, `deploy/seed_staging.py`), 4 cải tiến UI RQ3 + ảnh Before/After (`1440px` / `375px`) + ca Playwright E2E thứ 12, đo độ trễ API thật `NFR01/NFR02`, bản đồ 100% lớp/hàm + VS Code CodeTour (`.tours/on-code-git-health-monitor.tour`), bản thảo Tiểu luận phần hệ thống/RQ2/RQ3 và 9 trang Slide phần Toản + Kịch bản Demo 5 phút.
- **Phần của Trần Văn Tưởng (`23110170` — PR #16):** Review/merge PR #15, sửa parser CSV tiếng Việt trong `evaluate_rq3_sus.py` + test (`38/38` Python PASS), tạo `rq3-responses-template.csv` + hướng dẫn Google Form RQ3, soạn bản thảo Tiểu luận phần Tổng quan / Thuật toán / RQ1 / Benchmark / Threats / Kết luận & Tài liệu tham khảo, 9 trang Slide bổ sung thành bộ 18 trang hoàn chỉnh, đề cương ôn code Buổi 1 và tổng hợp báo cáo nhóm + nội dung nộp web Tuần 11.

---

## Danh mục tài liệu Tuần 11 & Chặng Về Đích

| Tài liệu / Minh chứng | Phụ trách | Nội dung chính |
|---|---|---|
| [Báo cáo tổng hợp Tuần 11 (Nhóm)](bao-cao-tuan-11-nhom.md) | Cả hai | Tổng hợp tiến độ `30 Hoàn thành / 8 Đang làm / 1 Chưa bắt đầu` và kiểm thử `172` ca tự động |
| [Báo cáo Tuần 11 (Toản)](bao-cao-tuan-11-toan.md) & [Tưởng](bao-cao-tuan-11-tuong.md) | Riêng | Chi tiết từng đầu việc, PR và kiểm chứng của từng thành viên |
| [Nội dung nộp web Khoa Tuần 11](noi-dung-nop-web-tuan-11.md) | Tưởng | Bản tóm tắt súc tích chuẩn dán lên `projects-fit.hcmute.edu.vn` kèm 3 ô AI Hỗ trợ |
| [Hướng dẫn Staging Cách 1 (`STT 27, 28`)](staging-runbook.md) | Toản | Hướng dẫn chạy `compose.staging.yaml` + Caddy HTTPS và `deploy/seed_staging.py` nạp kho mẫu cho RQ3 |
| [Cải tiến giao diện Before / After (`STT 34`)](ui-improvements-before-after.md) | Toản | 4 cải tiến UI theo `T2, T3, T5, T6, T7` kèm 4 ảnh Before/After Desktop (`1440px`) & Mobile (`375px`) |
| [Hướng dẫn Google Form RQ3 & Mẫu CSV (`STT 33`)](rq3-google-form-setup-and-csv-guide.md) | Tưởng | Cấu trúc Google Form 4 phần + [rq3-responses-template.csv](evidence/rq3-responses-template.csv) khớp `evaluate_rq3_sus.py` |
| [Bản thảo Tiểu luận (Phần Tưởng: Ch.1, 3.4–3.6, 4.5–4.8, Ch.5, TLTK)](tieu-luan-phan-tuong-chuong-1-3-4-ket-luan-tltk.md) | Tưởng | Toàn văn Chương 1, phần thuật toán Chương 3, RQ1/Benchmark/Đối chiếu/Threats Chương 4, Kết luận & TLTK |
| [Bản thảo Tiểu luận (Phần Toản: Ch.2, 3.1–3.3, 4.1–4.4, Phụ lục)](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) | Toản | Toàn văn Chương 2, phần hệ thống Chương 3, RQ2/Kiểm thử/JaCoCo/RQ3 Chương 4 và Phụ lục cài đặt |
| [Slide 1–5, 10–11, 17–18 & Ôn code Buổi 1](slide-phan-tuong-va-de-cuong-buoi-1.md) | Tưởng | 9 trang Slide bổ sung + Đề cương Buổi 1 giảng chéo `gitanalyzer` & `aireview` |
| [Slide 6–9, 12–16, Demo 5 phút & Cẩm nang Ôn bảo vệ](slide-demo-va-on-bao-ve-toan.md) | Toản | 9 trang Slide phần Toản, kịch bản Demo 5 phút, Buổi 2 và đáp án 5 câu hỏi Hội đồng |
| [Bản đồ Code từng hàm, Tech Stack & VS Code Extensions](ban-do-code-tung-ham-va-stack.md) | Toản | Giải thích 100% lớp/hàm, lý do chọn công nghệ và hướng dẫn CodeTour (`.tours/on-code-git-health-monitor.tour`) |
| [Nhật ký AI Tuần 11 (Toản)](ai-usage-toan-week11.md) & [(Tưởng)](ai-usage-tuong-week11.md) | Cả hai | Nhật ký học thuật ghi rõ lỗi CSS 375px và lỗi parser CSV Google Form tiếng Việt đã phát hiện/sửa |
| [Biên bản kiểm chứng (`verification.md`)](evidence/verification.md) & [NFR01/NFR02 JSON](evidence/nfr01-nfr02-api-latency.json) | Toản | Kết quả 122 JUnit (`89,07%` JaCoCo), 38 Python, 12 Playwright E2E và phép đo độ trễ API thật |
| [Bàn giao cho Tưởng](ban-giao-tuong.md) & [Nội dung PR](noi-dung-pr.md) | Toản | Tài liệu bàn giao nhánh `feat/toan-week-11` trước khi merge PR #15 |
