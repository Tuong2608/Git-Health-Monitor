# Nhật ký sử dụng AI — Tuần 11 (Trần Văn Tưởng - 23110170)

- **Ngày thực hiện:** 26/11/2026
- **Nhánh:** `codex/tuong-week11` (hợp nhất PR #15 và triển khai PR #16)
- **Mục tiêu:** Hoàn thiện phần công việc Tuần 11 và Chặng Về Đích của Tưởng: review/merge PR #15, kiểm tra tính tương thích của script chấm điểm khảo sát người dùng RQ3 (`evaluate_rq3_sus.py`), dựng mẫu CSV + hướng dẫn Google Form, và soạn bản thảo Tiểu luận + Slide phần Tưởng khớp 1-1 với Toản.

---

## 1. Hoạt động 1 — Kiểm tra và sửa lỗi parser tiếng Việt trong `evaluate_rq3_sus.py` (`STT 33`)

- **Phạm vi AI hỗ trợ:** Gợi ý danh sách chuỗi cần chấp nhận khi nhập/xuất kết quả từ Google Form tiếng Việt và kiểm tra xử lý khoảng trắng tiêu đề CSV.
- **Lỗi thực tế tự phát hiện và khắc phục:**
  - *Biểu hiện lỗi:* Khi đối chiếu phiếu khảo sát [mau-khao-sat-rq3-gui-nguoi-tham-gia.txt](../week-10/mau-khao-sat-rq3-gui-nguoi-tham-gia.txt) (sử dụng nhãn lựa chọn `"Đạt (1)"` và `"Không đạt (0)"`) với hàm `load_csv_responses` viết ở Tuần 10 (`evaluate_rq3_sus.py`), hàm chỉ nhận đúng 2 chuỗi `"đạt"` / `"dat"` và `"không"` / `"khong"`, đồng thời đọc khóa cột thô `row[tid]` không `.strip()`. Nếu chạy trực tiếp trên file CSV tải từ Google Sheets có lựa chọn `"Đạt (1)"` / `"Không đạt (0)"`, khoảng trắng quanh tên cột hoặc dòng trống ở cuối bảng tính, công cụ ném ngay lỗi `Unrecognized boolean value` hoặc `KeyError`.
  - *Cách khắc phục:* Cập nhật `load_csv_responses` chuẩn hóa khoảng trắng khóa tiêu đề, bỏ qua dòng trống hoàn toàn và mở rộng tập nhãn hợp lệ gồm `"đạt (1)"`, `"dat (1)"`, `"không đạt"`, `"khong dat"`, `"không đạt (0)"`, `"khong dat (0)"`. Bổ sung ca test `test_load_csv_responses_supports_vietnamese_labels_whitespace_and_template` trong `test_rq3_sus.py` và tạo tệp tiêu đề chuẩn không dữ liệu giả `rq3-responses-template.csv` (**38/38 ca Python PASS**).

---

## 2. Hoạt động 2 — Biên soạn bản thảo Tiểu luận, Slide & Hồ sơ Tuần 11 (`STT 36, 37, 38, 02`)

- **Phạm vi AI hỗ trợ:** Hỗ trợ tổng hợp cấu trúc chương mục và bố cục bảng biểu cho các file `tieu-luan-phan-tuong-chuong-1-3-4-ket-luan-tltk.md`, `slide-phan-tuong-va-de-cuong-buoi-1.md`, `bao-cao-tuan-11-nhom.md` và `noi-dung-nop-web-tuan-11.md` sao cho nối khớp số mục (`3.4..3.6`, `4.5..4.8`, Slide `1..5, 10..11, 17..18`) với bản thảo Toản đã nộp ở PR #15.
- **Kiểm chứng & chỉnh sửa bởi sinh viên:**
  - Tự rà soát đối chiếu mọi hằng số kỹ thuật (`MAX_TOTAL_CONTEXT_BYTES = 32 KiB`, `MAX_LINES_PER_SECTION = 200`, `MAX_PROVIDER_TIMEOUT = 30 giây`, `renameScore = 60%`, số dòng trong `GitIngestion.java`, `LizardComplexityAnalyzer.java`, `LlmReviewService.java`) khớp trực tiếp với mã nguồn thật.
  - Tự đối chiếu lại toàn bộ bảng số liệu RQ1 (`401` file, `67` dương, `141` commit duyệt tay, `MAP = 0.3918`, `javapoet = 36/0`), Benchmark `NFR03` (`48,96 s` lõi / `73,25 s` CLI / `59,90 ms` DB) và `NFR01/NFR02` (`46,00 ms / 28,42 ms / 28,05 ms`) từ các tệp JSON bằng chứng, tuyệt đối không sinh trước dữ liệu khảo sát RQ3 giả.
  - Tự viết lại báo cáo nộp web Tuần 11 theo đúng văn phong ngắn gọn, bám sát việc thật và số liệu thật của nhóm.
