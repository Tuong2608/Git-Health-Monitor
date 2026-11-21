## Vấn đề và thay đổi

Bổ sung phần thực nghiệm, kiểm thử tích hợp PostgreSQL 18.3, báo cáo độ phủ full-stack và bộ hồ sơ Tuần 10 của Trần Quang Toản (`23110158`) trên nhánh `feat/toan-week-10`:

1. **Phân rã định lượng độ phủ & quần thể RQ2 (`STT 31`):** Thêm `tools/experiments/audit_rq2_coverage.py` và `test_rq2_coverage_audit.py` (6 unit test mới), kiểm toán toàn bộ `369` lượt file Java trên 9 snapshot OSS và 12 snapshot kịch bản (`S1..S5`), tách bạch `NO_FUNCTIONS` (`37` file phả hệ rõ, chỉ giảm `hotspotPopulation`) và `LINEAGE_AMBIGUOUS` (`177` file, gây `coverageComplete = false`).
2. **Benchmark lưu trữ & đồng nhất bất biến RQ2 trên PostgreSQL 18.3 (`STT 31`, `STT 32`):** Thêm `Week10PersistenceBenchmarkTests.java` (3 JUnit test mới) đo chu trình `SnapshotPublisher.publish` + `AlertService.evaluateNext` + `SnapshotQueries.comparison` ở 3 mốc `1.000 / 5.000 / 10.000` commit (`50.08 ms / 63.34 ms / 59.90 ms`) và kiểm chứng biên `BigDecimal +0.15` (`S4`) cùng cơ chế chặn `INCOMPLETE_COVERAGE` (`S5`) trực tiếp trong PostgreSQL.
3. **Báo cáo độ phủ JaCoCo toàn hệ thống có PostgreSQL (`NFR08`, `TC2.5`):** Chạy đầy đủ `122/122` JUnit test với PostgreSQL 18.3, xuất `jacoco-fullstack-coverage.json` đạt **89.07% line coverage** toàn cục (`repository` 100%, `api` 100%, `monitoring` 94.60%, `gitanalyzer` 88.12%, `aireview` 85.88%).
4. **Chuẩn bị khảo sát người dùng RQ3 (`STT 33`) & Hồ sơ Tuần 10:** Bổ sung phiếu khảo sát `mau-khao-sat-rq3-gui-nguoi-tham-gia.txt`, mẫu `rq3-response-template.csv`, báo cáo cá nhân/nhóm, nhật ký AI học thuật, tài liệu lý thuyết áp dụng, runbook và biên bản kiểm chứng.

## Kết quả kiểm chứng

- **Backend JUnit + PostgreSQL 18.3:** `122/122` ca kiểm thử PASS (`0` lỗi, `0` skip); JaCoCo line coverage đạt `89.07%` (`1.630/1.830` dòng).
- **Python Experiment Tests:** `37/37` ca kiểm thử PASS (`0` lỗi); `21/21` snapshot (`9` OSS + `12` kịch bản) khớp toàn bộ bất biến kiểm toán.
- **Frontend:** `npm run lint` và `npm run build` PASS; `9/9` ca Playwright E2E (chế độ mock) PASS tại local.

## Cần review

- Giữ nguyên toàn bộ file dữ liệu thực nghiệm đã đóng băng trước đó; các minh chứng bổ sung trên PostgreSQL 18.3 được lưu ở file riêng (`jacoco-fullstack-coverage.json`, `postgres-persistence-benchmark.json`, `rq2-coverage-audit.json`).
- Chưa có dữ liệu khảo sát người dùng thật cho `RQ3` (`STT 33`, `STT 34` giữ trạng thái *Chưa bắt đầu*, thực hiện trong Tuần 11).
