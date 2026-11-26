# Báo cáo Tuần 11 & Chặng Về Đích — Trần Văn Tưởng (23110170)

- **Người thực hiện:** Trần Văn Tưởng (`23110170` - Trưởng nhóm)
- **Ngày thực hiện:** 26/11/2026
- **Nhánh làm việc:** `codex/tuong-week11` (phát triển từ `main @ a33acb5` sau khi review và merge PR #15 của Toản)
- **Tài liệu đối chiếu:** `KeHoach_VeDich_Tuan11_Den_BaoVe_GitHealthMonitor.docx`

---

## 1. Các hạng mục Tưởng đã hoàn tất trong Tuần 11

| Hạng mục | Nội dung hoàn thành | Minh chứng trong repository |
|---|---|---|
| **Review & Merge PR #15 của Toản (`STT 06, 27, 28, 34`)** | Rà soát cấu hình Staging Cách 1 (`compose.yaml`, `compose.staging.yaml`, `deploy/Caddyfile`, `deploy/seed_staging.py`), 4 cải tiến UI RQ3 + ảnh Before/After (`1440px` / `375px`), kết quả đo độ trễ API `NFR01/NFR02` (`46,00 ms / 28,42 ms / 28,05 ms`) và hợp nhất thành công vào `main @ a33acb5` (`5/5` job CI xanh). | [ban-giao-tuong.md](ban-giao-tuong.md), [verification.md](evidence/verification.md) |
| **Chốt STT 06 & Cải tiến công cụ chấm RQ3 (`STT 33`)** | Sửa lỗi phân tích CSV thực tế trong `evaluate_rq3_sus.py` khi Google Form/Sheets xuất nhãn tiếng Việt `"Đạt (1)"`, `"Không đạt (0)"`, `"Không đạt"` kèm khoảng trắng tiêu đề hoặc dòng trống cuối tệp; bổ sung unit test mới trong `test_rq3_sus.py` (nâng tổng test Python lên **38/38 PASS**). Tạo tệp CSV mẫu không dữ liệu giả `rq3-responses-template.csv` và tài liệu hướng dẫn dựng Google Form khớp 27 cột. | [evaluate_rq3_sus.py](../../tools/experiments/evaluate_rq3_sus.py), [test_rq3_sus.py](../../tools/experiments/test_rq3_sus.py), [rq3-responses-template.csv](evidence/rq3-responses-template.csv), [rq3-google-form-setup-and-csv-guide.md](rq3-google-form-setup-and-csv-guide.md) |
| **Bản thảo Tiểu luận phần của Tưởng (`STT 36, 37`)** | Soạn hoàn chỉnh văn bản học thuật chuẩn cấu trúc HCMUTE ghép nối tiếp bản thảo của Toản: **Chương 1** (Tổng quan, lý thuyết MSR, đối chiếu 5 công cụ, khảo sát `S01..S05`), **Chương 3 (Mục 3.4–3.6)** (Thuật toán `GitIngestion`, `LizardComplexityAnalyzer`, `RepositoryMetricCalculator`, `LlmReviewService` & `HttpLlmProviderAdapter`), **Chương 4 (Mục 4.5–4.8)** (RQ1, Benchmark `NFR03` & `NFR01/NFR02`, Đối chiếu công cụ, *Threats to Validity*), **Chương 5 (Kết luận & Hướng phát triển)** và **Tài liệu tham khảo**. | [tieu-luan-phan-tuong-chuong-1-3-4-ket-luan-tltk.md](tieu-luan-phan-tuong-chuong-1-3-4-ket-luan-tltk.md) |
| **Slide phần Tưởng & Đề cương Buổi 1 (`STT 38, 39`)** | Hoàn thiện nội dung 9 trang Slide phần Tưởng (`Slide 1–5, 10–11, 17–18`) khớp 1-1 với 9 trang Slide của Toản thành bộ Slide 18 trang hoàn chỉnh; soạn đề cương chi tiết Buổi 1 giảng chéo `gitanalyzer` & `aireview`. | [slide-phan-tuong-va-de-cuong-buoi-1.md](slide-phan-tuong-va-de-cuong-buoi-1.md) |
| **Tổng hợp báo cáo nhóm, AI Usage Log & đồng bộ hồ sơ (`STT 02`)** | Cập nhật báo cáo nhóm Tuần 11, nhật ký AI Tuần 11 và nội dung nộp web Khoa theo phong cách súc tích, không mang văn phong AI. | [bao-cao-tuan-11-nhom.md](bao-cao-tuan-11-nhom.md), [ai-usage-tuong-week11.md](ai-usage-tuong-week11.md), [noi-dung-nop-web-tuan-11.md](noi-dung-nop-web-tuan-11.md) |

---

## 2. Kiểm chứng đã chạy trên nhánh `codex/tuong-week11`

- `python -m unittest discover -s tools/experiments -v`: **38/38 ca PASS** (`0` lỗi, `0` bỏ qua), xác nhận luôn `rq3-responses-template.csv` rỗng dữ liệu tải lên hợp lệ.
- **Tổng số kiểm thử toàn hệ thống hiện tại:** **172 ca tự động** (`122` JUnit backend trên PostgreSQL 18.3 với `89,07%` JaCoCo + `12` Playwright UI E2E + `38` Python unittest).
