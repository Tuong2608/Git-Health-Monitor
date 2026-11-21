# Báo cáo tuần 10 — Trần Quang Toản

- **Họ tên & MSSV:** Trần Quang Toản (`23110158`)
- **Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git (`Git Health Monitor`)
- **Ngày thực hiện:** 18/11/2026
- **Nhánh phát triển:** `feat/toan-week-10` (phát triển từ `main` `8e437fd`)
- **Lưu ý xác nhận:** Báo cáo được biên soạn có AI hỗ trợ dựa trên dữ liệu chạy thực tế trên máy Toản; Toản cần đọc lại, chạy kiểm chứng các bước chính và xác nhận trước khi nộp.

---

## 1. Phạm vi công việc theo kế hoạch

Theo bảng kế hoạch chi tiết (`docs/BaoCaoTienDo_TLCN.xlsx` trên `main` `8e437fd`):
- **STT 31 (Thực nghiệm RQ2: theo dõi xu hướng qua snapshot — Toản phụ trách, Tuần 9–10):** Hoàn tất đánh giá 5 kịch bản tiến hóa mã nguồn có kiểm soát (`S1..S5`), kiểm toán truy vết `91/91` tập tin chung trên 9 snapshot OSS, phân rã định lượng nguyên nhân loại tập tin (`LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS`) trên toàn bộ `369` thực thể file Java, và kiểm chứng đồng nhất bất biến RQ2 trên tầng lưu trữ PostgreSQL 18.3.
- **STT 32 (Thực nghiệm hiệu năng — Toản phụ trách, Tuần 10):** Phối hợp kết quả benchmark lõi (`1.000 / 5.000 / 10.000` commit) và bổ sung phép đo trực tiếp thời gian lưu trữ giao dịch (`SnapshotPublisher`), đánh giá cảnh báo (`AlertService`) và truy vấn so sánh (`SnapshotQueries.comparison`) trên PostgreSQL 18.3 ở đúng 3 mốc quy mô để khép kín kiểm chứng `NFR03 <= 600s`.
- **STT 28 & NFR08 (Kiểm thử tích hợp & độ phủ mã nguồn):** Chạy đầy đủ `mvnw clean verify` với PostgreSQL 18.3 và JaCoCo `0.8.12` trên toàn bộ **122 ca JUnit test**, bổ sung độ phủ thực tế của các package có cơ sở dữ liệu (`monitoring`, `repository`, `api`, `aireview`) mà lượt chạy offline không DB (`49` test) chưa bao phủ.
- **STT 33 & STT 35 (Chuẩn bị thực nghiệm người dùng RQ3 & Tổng hợp Threats to Validity — Cả 2 cùng làm):** Kiểm thử lại giao diện React E2E với Playwright, soạn phiếu khảo sát/nhiệm vụ gửi người tham gia (`mau-khao-sat-rq3-gui-nguoi-tham-gia.txt`) và mẫu CSV trống (`rq3-response-template.csv`) khớp công cụ `evaluate_rq3_sus.py`. **Không tự điền hay giả lập dữ liệu người dùng thật.**

---

## 2. Sản phẩm kỹ thuật và thực nghiệm trong Tuần 10

| Hạng mục | Đã thực hiện | Minh chứng trong repository |
|---|---|---|
| **Kiểm toán phân rã độ phủ & quần thể RQ2 (`STT 31`)** | Viết `audit_rq2_coverage.py` và 6 unit test (`test_rq2_coverage_audit.py`); kiểm toán 9 snapshot OSS (`369` lượt file) và 12 snapshot kịch bản (`S1..S5`), tách bạch ảnh hưởng của `NO_FUNCTIONS` (chỉ giảm `hotspotPopulation`) và `LINEAGE_AMBIGUOUS` (gây `coverageComplete = false`) | [audit_rq2_coverage.py](../../tools/experiments/audit_rq2_coverage.py), [test_rq2_coverage_audit.py](../../tools/experiments/test_rq2_coverage_audit.py), [rq2-coverage-audit.json](evidence/rq2-coverage-audit.json) |
| **Benchmark lưu trữ & đồng nhất RQ2 trên PostgreSQL (`STT 31`, `STT 32`)** | Viết `Week10PersistenceBenchmarkTests.java` (3 JUnit test mới trên PostgreSQL 18.3): đo thời gian `publish` + `evaluateNext` + `comparison` ở quy mô `1k / 5k / 10k` commit (`25 / 40 / 50` file Java, `75..150` dòng đóng góp, `12..25` cặp coupling) và kiểm chứng biên `BigDecimal +0.15` (`S4`) cùng cơ chế chặn `INCOMPLETE_COVERAGE` (`S5`) ngay trong DB | [Week10PersistenceBenchmarkTests.java](../../backend/src/test/java/com/example/demo/monitoring/Week10PersistenceBenchmarkTests.java), [postgres-persistence-benchmark.json](evidence/postgres-persistence-benchmark.json) |
| **Độ phủ JaCoCo toàn hệ thống có PostgreSQL (`NFR08`, `TC2.5`)** | Chạy `mvnw clean verify` trên PostgreSQL 18.3 (`122/122` JUnit pass), trích xuất báo cáo độ phủ đầy đủ 6 package (`89.07%` line coverage toàn cục) để đối chiếu với báo cáo 49 unit test không DB (`64.06%`) | [jacoco-fullstack-coverage.json](evidence/jacoco-fullstack-coverage.json), [backend-tests.json](evidence/backend-tests.json) |
| **Chuẩn bị bộ công cụ khảo sát RQ3 (`STT 33`)** | Soạn phiếu hướng dẫn 7 tác vụ chuẩn + 10 câu hỏi SUS + form đồng thuận để gửi người tham gia thực tế cùng file CSV mẫu; chạy kiểm chứng 9 ca UI Playwright (2 ca live skip khi không bật cờ live) | [mau-khao-sat-rq3-gui-nguoi-tham-gia.txt](mau-khao-sat-rq3-gui-nguoi-tham-gia.txt), [rq3-response-template.csv](rq3-response-template.csv), [rq3-user-study-protocol.md](rq3-user-study-protocol.md) |
| **Báo cáo, lý thuyết, AI log & bàn giao** | Cập nhật báo cáo RQ2, Benchmark, Tổng hợp Threats to Validity; viết báo cáo cá nhân, báo cáo nhóm, nhật ký AI học thuật, tài liệu lý thuyết áp dụng, runbook và biên bản kiểm chứng | [README.md](README.md), [ly-thuyet-va-ap-dung.md](ly-thuyet-va-ap-dung.md), [ai-usage-toan-week10.md](ai-usage-toan-week10.md), [verification.md](evidence/verification.md) |

---

## 3. Kết quả định lượng chính và ý nghĩa kỹ thuật

### 3.1. Giải thích định lượng hiện tượng thiếu coverage và giảm quần thể ở RQ2 (`STT 31`)
- Trên **5 kịch bản tiến hóa có kiểm soát (`S1..S5`)**: Cả 5/5 kịch bản chạy qua `HistoricalSnapshotExport` và `rq2_trends.py` đều khớp tuyệt đối kỳ vọng (`S1` phát hiện tăng thật `+1.0`; `S2` giữ nguyên `lineageKey` qua `git mv`; `S3` giải thích giảm điểm `-0.3660` do trượt cửa sổ 180 ngày khi `maxCcn = 5` giữ nguyên; `S4` chặn cảnh báo giả tại đúng biên `+0.15` bằng `Decimal`/`BigDecimal`; `S5` chặn cảnh báo khi vướng `LINEAGE_AMBIGUOUS` hoặc lệch cửa sổ 90d vs 180d).
- Trên **9 snapshot lịch sử của 3 dự án OSS (`369` thực thể file Java)**:
  - `155 / 369` (`42.01%`) có đủ `frequency` và `maxCcn` để vào `hotspotPopulation`.
  - `37 / 369` (`10.03%`) có phả hệ rõ ràng (`frequency != null`) nhưng `unavailableReason = "NO_FUNCTIONS"` (các `interface` JPA, lớp thực thể không phương thức hoặc `package-info.java` trong `spring-petclinic` và `commons-cli`). Các file này **không** làm `coverageComplete = false` mà chỉ không tham gia mẫu số phân vị `hotspotPopulation`.
  - `177 / 369` (`47.97%`) bị đánh dấu `LINEAGE_AMBIGUOUS` (gồm `160` file có `maxCcn` hợp lệ và `17` file đồng thời không có hàm như `Greeting.java` record). Chính nhóm `177` thực thể có `frequency = null` này kích hoạt `coverageComplete = false` và chặn `6/6` cặp OSS. Nếu quy tắc phả hệ qua merge được mở rộng ở `v2`, quần thể hotspot trên 9 snapshot sẽ tăng từ **42.01% (`155/369`)** lên **85.37% (`315/369`)**.

### 3.2. Hiệu năng phân tích 10.000 commit và chi phí giao dịch PostgreSQL 18.3 (`STT 32` & `NFR03`)
- Lõi phân tích (`JGit + Lizard + Metric`) ở mốc `10.000` commit (`50` file Java, `12.045` facts) đạt trung bình **48.96 giây** (lượt cold `59.96 s`, CLI độc lập `73.25 s`), trong đó `JGitIngestion` chiếm **92.2%** (`45.16 s`).
- Phép đo bổ sung trên PostgreSQL 18.3 (`Week10PersistenceBenchmarkTests`) cho thấy toàn bộ chu trình ghi snapshot đích (`50` dòng `file_metric`, `150` dòng `file_contribution`, `25` cặp `temporal_coupling`), chạy `AlertService.evaluateNext()` (`55` cảnh báo) và truy vấn `SnapshotQueries.comparison()` chỉ mất tổng cộng **59.90 ms (`~0.06 giây`)** (ở mốc `1k` là `50.08 ms`, mốc `5k` là `63.34 ms`).
- Như vậy, tổng thời gian xử lý bao gồm cả PostgreSQL vẫn đạt **`~49.02 giây`** (nhanh gấp hơn **8 lần** so với giới hạn `600 giây` của `NFR03`).

### 3.3. Độ phủ mã nguồn JaCoCo `0.8.12` khi có PostgreSQL 18.3 (`NFR08` & `TC2.5`)
- Khi chỉ chạy 49 unit test không cần DB, tổng độ phủ dòng lệnh là `64.06%` vì package `monitoring` (`1.42%`), `repository` (`33.33%`) và `api` (`0.0%`) chưa chạy các `@SpringBootTest`.
- Khi chạy đầy đủ **122 ca JUnit test trên PostgreSQL 18.3**, độ phủ dòng lệnh toàn backend đạt **89.07% (`1.630 / 1.830` dòng)**, độ phủ lệnh đạt **89.52%** và độ phủ nhánh đạt **67.80%**:
  - `com.example.demo.repository`: **100.00%** (`48/48` dòng)
  - `com.example.demo.api`: **100.00%** (`14/14` dòng)
  - `com.example.demo.monitoring`: **94.60%** (`333/352` dòng)
  - `com.example.demo.gitanalyzer`: **88.12%** (`794/901` dòng)
  - `com.example.demo.aireview`: **85.88%** (`438/510` dòng)

---

## 4. Những giới hạn còn mở và kế hoạch Tuần 11

1. **Thực nghiệm người dùng RQ3 (`STT 33`) và cải tiến giao diện (`STT 34`):** Bộ đề 7 tác vụ chuẩn, bảng hỏi SUS, phiếu khảo sát (`mau-khao-sat-rq3-gui-nguoi-tham-gia.txt`) và script chấm điểm (`evaluate_rq3_sus.py`) đã sẵn sàng, nhưng **chưa có dữ liệu khảo sát người dùng thật**. Hai mục `STT 33` và `STT 34` giữ trạng thái **Chưa bắt đầu** trên bảng tiến độ cho đến khi thu thập xong ít nhất 5–9 người tham gia thực tế (phấn đấu >= 10 người) trong Tuần 11.
2. **Môi trường Staging (`STT 27`, `STT 28`):** Chưa có URL cloud staging mới có cấu hình PostgreSQL V4 + Lizard + persistent Git cache; giữ trạng thái **Đang làm** và không ghi nhận là đã triển khai cloud mới trong Tuần 10.
3. **Kế hoạch Tuần 11:** Tổ chức phiên thực nghiệm người dùng RQ3, chạy `evaluate_rq3_sus.py` trên dữ liệu thật, thực hiện tối thiểu 1 vòng cải tiến giao diện theo phản hồi (`STT 34`), và hoàn thiện Chương 3, Chương 4 cùng Kết luận của cuốn Tiểu luận chuyên ngành (`STT 36`, `STT 37`).
