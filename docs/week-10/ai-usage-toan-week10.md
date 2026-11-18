# Nhật ký sử dụng trí tuệ nhân tạo — Tuần 10

- **Người phụ trách:** Trần Quang Toản (`23110158`)
- **Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git (`Git Health Monitor`)
- **Ngày thực hiện:** 18/11/2026
- **Nhánh phát triển:** `feat/toan-week-10` (phát triển từ `main` `8e437fd`)
- **Nguyên tắc ghi nhận:** Trình bày tách biệt từng hoạt động theo cấu trúc học thuật (Mục tiêu, Phạm vi AI hỗ trợ, Kiểm chứng & Kết quả, Lỗi phát hiện & Cách khắc phục); không sao chép nguyên văn câu lệnh giao tiếp ban đầu; không dùng nhật ký này thay thế việc sinh viên tự đọc hiểu và giải thích mã nguồn.

---

## 1. Hoạt động 1 — Đối chiếu tiến độ nhánh `main` và xác định phạm vi đóng góp Tuần 10 của Toản

- **Mục tiêu:** Đồng bộ trạng thái mới nhất từ `origin/main` (`8e437fd` sau PR #11, #12, #13), kiểm tra bảng tiến độ `docs/BaoCaoTienDo_TLCN.xlsx` và xác định các khoảng trống kỹ thuật thuộc trách nhiệm của Toản trong Tuần 10 (`STT 31`, `STT 32`, `STT 28/NFR08`, `STT 33`, `STT 35`).
- **Phạm vi AI hỗ trợ:** Phân tích lịch sử commit, đối chiếu báo cáo `jacoco-coverage-summary.json` (chỉ chạy 49 unit test không DB nên độ phủ `monitoring` mới đạt `1.42%` và tổng đạt `64.06%`), `benchmark-report.md` (chưa đo thời gian ghi PostgreSQL) và `rq2-complete-evaluation.md` (cần phân rã định lượng `LINEAGE_AMBIGUOUS` và `NO_FUNCTIONS` trên 9 snapshot OSS).
- **Cách kiểm chứng:** Đọc trực tiếp workbook `docs/BaoCaoTienDo_TLCN.xlsx`, `RepositoryMetricCalculator.java` và `SnapshotPublisher.java`.
- **Kết quả:** Thiết lập nhánh `feat/toan-week-10` bổ sung 3 mảnh ghép kỹ thuật trực tiếp vào kho mã mà không ghi đè hay làm hỏng dữ liệu thực nghiệm đã đóng băng trước đó.

---

## 2. Hoạt động 2 — Cài đặt công cụ kiểm toán phân rã độ phủ và quần thể RQ2 (`STT 31`)

- **Mục tiêu:** Phân rã chính xác toàn bộ `369` lượt tập tin Java trong 9 snapshot OSS (`gs-rest-service`, `spring-petclinic`, `commons-cli`) và 12 snapshot kịch bản (`S1..S5`) để chứng minh sự khác biệt giữa `NO_FUNCTIONS` và `LINEAGE_AMBIGUOUS`.
- **Phạm vi AI hỗ trợ:** Soạn thảo module `tools/experiments/audit_rq2_coverage.py` và bộ 6 ca kiểm thử `tools/experiments/test_rq2_coverage_audit.py`; xuất minh chứng `docs/week-10/evidence/rq2-coverage-audit.json`.
- **Lỗi AI phát hiện và khắc phục (Lỗi thực tế #1):**
  - *Biểu hiện lỗi:* Phiên bản đầu của `audit_rq2_coverage.py` do AI sinh giả định mỗi phần tử trong mảng `files` của file JSON xuất từ `HistoricalSnapshotExport` có chứa trường `functions`, `churn`, `nloc` giống record `FileData` nội bộ, đồng thời tính `all_no_func_count` chỉ dựa trên `unavailableReason == "NO_FUNCTIONS"`. Khi chạy `test_committed_pilot_and_scenario_snapshots_pass_full_audit`, kiểm thử báo lỗi `ValueError: noFunctionFiles mismatch in gs-rest-service-0.json: 0 != 1`.
  - *Nguyên nhân gốc:* Trong `HistoricalSnapshotExport.java`, mỗi file chỉ xuất 6 trường (`lineageKey`, `path`, `frequency`, `maxCcn`, `hotspot`, `unavailableReason`). Đặc biệt, khi một tập tin như `Greeting.java` (Java `record` không có phương thức tính CCN nên `maxCcn = null`) đồng thời vướng `LINEAGE_AMBIGUOUS` (`frequency = null`), hàm `determineUnavailableReason` ưu tiên gán `unavailableReason = "LINEAGE_AMBIGUOUS"`, trong khi `summary.noFunctionFiles` đếm mọi file có `functions == 0` (tức bao gồm cả các file `LINEAGE_AMBIGUOUS` có `maxCcn == null`).
  - *Cách sửa:* Tách rõ hai tập con `ambiguous_with_ccn` (`reason == "LINEAGE_AMBIGUOUS" and maxCcn is not None`) và `ambiguous_without_ccn` (`reason == "LINEAGE_AMBIGUOUS" and maxCcn is None`), kiểm chứng bất biến `totalNoFunctionFiles == len(unambiguous_no_functions) + len(ambiguous_without_ccn)`. Sau khi sửa, cả 21/21 snapshot (`9` OSS + `12` kịch bản) và `37/37` unit test Python đều PASS tuyệt đối.

---

## 3. Hoạt động 3 — Cài đặt kiểm thử Benchmark lưu trữ PostgreSQL 18.3 và đồng nhất bất biến RQ2 (`STT 31`, `STT 32`)

- **Mục tiêu:** Đo thời gian thực tế của tầng lưu trữ giao dịch PostgreSQL (`SnapshotPublisher.publish`, `AlertService.evaluateNext`, `SnapshotQueries.comparison`) ở 3 mốc quy mô `1.000 / 5.000 / 10.000` commit (`25 / 40 / 50` file Java) và kiểm chứng biên ngưỡng `+0.15` (`BigDecimal`) cùng cơ chế chặn `INCOMPLETE_COVERAGE` ngay trong cơ sở dữ liệu.
- **Phạm vi AI hỗ trợ:** Viết lớp kiểm thử tích hợp `backend/src/test/java/com/example/demo/monitoring/Week10PersistenceBenchmarkTests.java` và cấu hình xuất `docs/week-10/evidence/postgres-persistence-benchmark.json`.
- **Lỗi AI phát hiện và khắc phục (Lỗi thực tế #2 và #3):**
  - *Lỗi thực tế #2 (Import sai kiểu nested record):* Bản nháp đầu của `Week10PersistenceBenchmarkTests.java` viết `import static com.example.demo.monitoring.MonitoringStore.Claim;`, gây lỗi biên dịch `cannot find symbol: class Claim in MonitoringStore`. Nguyên nhân là `Claim` được định nghĩa trong interface `AnalysisPipeline` (đã được import qua `import static com.example.demo.monitoring.AnalysisPipeline.*;`) chứ không nằm trong `MonitoringStore`. Đã xóa dòng import thừa và biên dịch thành công.
  - *Lỗi thực tế #3 (Lệch quy ước chỉ số trang 0-based trong `SnapshotQueries.comparison`):* Bản nháp đầu gọi `queries.comparison(repo, s1, s2, 1, 100)` với giả định trang đầu tiên là `page = 1`. Khi chạy `mvnw verify` trên PostgreSQL 18.3, JUnit bắt được lỗi `AssertionError: Expected size: 25 but was: 0 in: []`. Đối chiếu `MonitoringController.java` (dòng 43) và `SnapshotQueries.java` (dòng 51) xác nhận tham số `page` là `0-based` (`offset = page * size`), nên `page = 1, size = 100` đẩy `offset = 100` vượt qua 25 tập tin. Sau khi sửa thành `page = 0`, toàn bộ `122/122` ca JUnit test trên PostgreSQL 18.3 PASS và ghi nhận chính xác thời gian chu trình DB (`50.08 ms / 63.34 ms / 59.90 ms`).

---

## 4. Hoạt động 4 — Đo độ phủ JaCoCo toàn hệ thống với PostgreSQL 18.3 và chuẩn bị bộ công cụ khảo sát RQ3 (`NFR08`, `STT 33`)

- **Mục tiêu:** Chạy toàn bộ 122 ca JUnit test dưới sự giám sát của `jacoco-maven-plugin 0.8.12` trên PostgreSQL 18.3 để thu thập độ phủ thực tế của cả 6 package; chuẩn bị phiếu khảo sát gửi người tham gia cho `RQ3`.
- **Phạm vi AI hỗ trợ:** Viết script trích xuất dữ liệu từ `backend/target/site/jacoco/jacoco.xml` và `backend/target/surefire-reports/*.xml` sang `docs/week-10/evidence/jacoco-fullstack-coverage.json` và `docs/week-10/evidence/backend-tests.json`; soạn `docs/week-10/mau-khao-sat-rq3-gui-nguoi-tham-gia.txt` và `docs/week-10/rq3-response-template.csv`.
- **Cách kiểm chứng:** Đối chiếu tổng số dòng lệnh `1.630 / 1.830` (`89.07%`) và từng package (`repository` `100%`, `api` `100%`, `monitoring` `94.60%`, `gitanalyzer` `88.12%`, `aireview` `85.88%`) với báo cáo XML gốc của JaCoCo; chạy kiểm thử Playwright (`9` passed, `2` skipped live) và `npm run lint` / `npm run build`. Không tự tạo bất kỳ câu trả lời khảo sát giả nào cho `RQ3`.

---

## 5. Hoạt động 5 — Biên soạn tài liệu lý thuyết, báo cáo tuần và đóng gói bàn giao

- **Mục tiêu:** Hoàn thiện bộ tài liệu `docs/week-10/`, đảm bảo mọi liên kết nội bộ hợp lệ và đóng gói `outputs/week-10/Tuan10_Toan_Gui_Tuong.zip`.
- **Phạm vi AI hỗ trợ:** Soạn `bao-cao-tuan-10-toan.md`, `bao-cao-tuan-10-nhom.md`, `ly-thuyet-va-ap-dung.md`, `runbook.md`, `ban-giao-tuong.md`, `noi-dung-pr.md`, `evidence/verification.md` và cập nhật các bảng tổng hợp.
- **Trách nhiệm xác nhận của sinh viên:** Toản cần tự đọc lại mã nguồn `Week10PersistenceBenchmarkTests.java`, `audit_rq2_coverage.py`, hiểu rõ cơ chế tách `NO_FUNCTIONS` khỏi `LINEAGE_AMBIGUOUS`, sự khác biệt giữa độ phủ unit test không DB (`64.06%`) và độ phủ full-stack có PostgreSQL (`89.07%`), cũng như chi phí từng chặng trong benchmark 10.000 commit trước khi nộp báo cáo và bảo vệ.
