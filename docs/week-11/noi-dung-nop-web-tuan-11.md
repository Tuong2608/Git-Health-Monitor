# Nội dung nộp cổng Khoa Tuần 11 (Nhóm Git Health Monitor - 23110170 & 23110158)

*(Bản trình bày súc tích bám sát tiến độ và số liệu thực tế trong PR #15 và PR #16 để dán vào mục NỘI DUNG BÁO CÁO và AI HỖ TRỢ trên web Khoa.)*

---

## NỘI DUNG BÁO CÁO

```markdown
1. Các phần hoàn thành và đang triển khai trong tuần 11:
- Hoàn thành STT 06 và xong cấu hình triển khai Staging sống Cách 1 (STT 27, 28): tham số hóa APP_ALLOWED_ORIGINS và cổng bind frontend trong compose.yaml (tương thích ngược với CI), bổ sung compose.staging.yaml, deploy/Caddyfile (HTTPS tự động qua Caddy 2.9), script nạp dữ liệu mẫu deploy/seed_staging.py và docs/week-11/staging-runbook.md.
- Đo độ trễ API thật trên backend kết nối PostgreSQL 18 (measure_api_latency.py): POST /analyses trả mã 202 trong tối đa 46.00 ms (ngưỡng NFR01 <= 2.000 ms), GET /hotspots p95 = 28.42 ms và GET /files/{id} p95 = 28.05 ms (ngưỡng NFR02 <= 1.000 ms).
- Đang làm STT 34 (xong vòng cải tiến UI trước khảo sát): bổ sung hướng dẫn Top 5 Hotspot (T2), hộp tra cứu công thức Hotspot/Max CCN/HHI (T3), giải thích LINEAGE_AMBIGUOUS vs NO_FUNCTIONS khi coverageComplete = false và ngưỡng cảnh báo (T5, T6), thanh quy trình 3 bước kèm mã băm contextHash SHA-256 trong AI Review (T7); chụp đủ 4 ảnh Before/After (1440px và 375px) và nâng Playwright E2E lên 12 ca.
- Đang làm STT 33 (chuẩn bị đầy đủ bộ khảo sát RQ3): sửa evaluate_rq3_sus.py hỗ trợ nhãn tiếng Việt Google Form (nâng bộ test Python lên 38/38 pass), tạo file mẫu rq3-responses-template.csv và hướng dẫn dựng Google Form (chờ chạy staging và thu 8-10 phiếu người dùng thật).
- Đang làm STT 36, 37, 38 (hoàn thiện trọn bản thảo nội dung): viết đầy đủ bản thảo Chương 1-5, Tài liệu tham khảo, Phụ lục, Slide 18 trang chia đều hai thành viên, kịch bản Demo 5 phút và tài liệu ôn code chéo 3 buổi kèm CodeTour trong .tours/on-code-git-health-monitor.tour.
- Tiến độ chung: 30 việc Hoàn thành / 8 Đang làm / 1 Chưa bắt đầu (chi tiết tô nền vàng trong file Excel BaoCaoTIenDo_TLCN_Tuan11.xlsx đính kèm).

2. Chi tiết công việc theo thành viên trong tuần 11:
- **Trần Quang Toản (23110158) - Cấu hình Staging, Cải tiến UI RQ3, Đo NFR01/NFR02 & Bản thảo phần hệ thống (PR #15):**
  - Cập nhật compose.yaml, .env.example, thêm compose.staging.yaml, deploy/Caddyfile, deploy/seed_staging.py và measure_api_latency.py (đo thật trên 2 repo gs-rest-service và spring-petclinic).
  - Cập nhật AnalysisDashboard.tsx, App.css và thêm ca kiểm thử thứ 12 trong frontend/e2e/week8.spec.ts (10/10 ca mock PASS, sửa lỗi CSS tràn ngang trên màn hình mobile 375x812 khi mở hộp công thức cùng biểu đồ Recharts).
  - Soạn bản thảo Chương 2, Chương 3 (phần CSDL, Queue PostgreSQL, REST API, React UI, Docker/CI), Chương 4 (phần RQ2, Benchmark PostgreSQL, JaCoCo 89.07%, UI Before/After), Phụ lục cài đặt, nội dung 9 trang Slide phần Toản, kịch bản Demo 5 phút và bản đồ tra cứu 100% lớp/hàm trong ban-do-code-tung-ham-va-stack.md.
- **Trần Văn Tưởng (23110170) - Review/Merge PR #15, Hoàn thiện công cụ Google Form RQ3 & Bản thảo phần thuật toán/RQ1 (PR #16):**
  - Rà soát và merge PR #15 vào main; kiểm tra và sửa lỗi parser CSV trong evaluate_rq3_sus.py để đọc được nhãn tiếng Việt ("Đạt (1)", "Không đạt (0)") và header có khoảng trắng thừa từ Google Form, bổ sung test trong test_rq3_sus.py (38/38 test Python PASS) và tạo rq3-responses-template.csv cùng rq3-google-form-setup-and-csv-guide.md.
  - Soạn hoàn chỉnh bản thảo Chương 1, Chương 3 (phần thuật toán GitIngestion, LizardComplexityAnalyzer, RepositoryMetricCalculator, LlmReviewService, HttpLlmProviderAdapter), Chương 4 (RQ1, Benchmark NFR03, đối chiếu 5 công cụ, Threats to Validity), Chương 5 Kết luận, Tài liệu tham khảo và nội dung 9 trang Slide bổ sung (Slide 1-5, 10-11, 17-18) kèm đề cương giảng chéo Buổi 1.

3. Link kết quả:
- Link Github: https://github.com/Tuong2608/Git-Health-Monitor (nhánh main, đã merge PR #15 và PR #16, CI xanh 5/5 job)
- Hồ sơ và bằng chứng tuần 11: xem trong thư mục docs/week-11/ (bao-cao-tuan-11-nhom.md, staging-runbook.md, ui-improvements-before-after.md, tieu-luan-phan-tuong-chuong-1-3-4-ket-luan-tltk.md, tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md, slide-phan-tuong-va-de-cuong-buoi-1.md, slide-demo-va-on-bao-ve-toan.md) và docs/week-11/evidence/.
- Link đề cương: <https://drive.google.com/drive/folders/1Nx1qMCjnKJr293tqZKtUZKhuFzJa9gZT?usp=sharing>
```

---

## AI HỖ TRỢ

### PROMPT
```text
Hỏi cách tham số hóa CORS origin và cổng bind trong Docker Compose kèm cấu hình Caddy reverse proxy HTTPS, cách viết Playwright kiểm thử chống tràn ngang 375px với phần tử details/Recharts SVG, và cách xử lý cột CSV tiếng Việt xuất từ Google Forms trong Python.
```

### AI TẠO RA
```text
Mẫu compose.staging.yaml với Caddy 2.9, gợi ý đoạn script Playwright đo scrollWidth trên viewport 375x812, khung ánh xạ giá trị CSV trong Python và gợi ý cấu trúc mục lục bản thảo Tiểu luận theo đề cương.
```

### SV ĐÃ SỬA
```text
Tự phát hiện và sửa lỗi CSS tràn ngang mobile 375x812 trong App.css bằng quy tắc min-width: 0 và overflow: hidden cho biểu đồ Recharts khi mở hộp công thức metric-guide; tự viết script measure_api_latency.py đo độ trễ thật NFR01/NFR02 trên backend và PostgreSQL 18; tự phát hiện và sửa lỗi evaluate_rq3_sus.py không đọc được nhãn tiếng Việt "Đạt (1)"/"Không đạt (0)" và dòng trống từ Google Form; tự đối chiếu toàn bộ số liệu thực nghiệm, tên lớp/hàm trong mã nguồn thật để viết bản thảo Tiểu luận, Slide và giữ trống dữ liệu RQ3 chờ thu phiếu người dùng thật.
```
