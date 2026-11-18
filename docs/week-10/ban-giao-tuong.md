# Bàn giao tuần 10 cho Tưởng

Người gửi: Trần Quang Toản (`23110158`). Ngày: 18/11/2026. Nhánh: `feat/toan-week-10` (phát triển từ `main` `8e437fd`).

Mở [README tuần 10](README.md), [báo cáo cá nhân](bao-cao-tuan-10-toan.md), [báo cáo nhóm](bao-cao-tuan-10-nhom.md), [đánh giá RQ2 đầy đủ](rq2-complete-evaluation.md), [báo cáo benchmark](benchmark-report.md), [tổng hợp Threats to Validity](synthesis-and-threats.md) và [kiểm chứng](evidence/verification.md) để review.

---

## 1. Các bổ sung của Toản trong nhánh `feat/toan-week-10`

| Hạng mục | Nội dung bổ sung | Minh chứng |
|---|---|---|
| **Phân rã độ phủ & quần thể RQ2 (`STT 31`)** | Thêm `tools/experiments/audit_rq2_coverage.py` và 6 unit test (`test_rq2_coverage_audit.py`), kiểm toán toàn bộ `369` thực thể file Java trên 9 snapshot OSS và 12 snapshot kịch bản (`S1..S5`), chứng minh `NO_FUNCTIONS` (`37` file phả hệ rõ) chỉ giảm `hotspotPopulation` chứ không làm `coverageComplete = false`, còn `LINEAGE_AMBIGUOUS` (`177` file) mới kích hoạt `BLOCKED`. | [rq2-coverage-audit.json](evidence/rq2-coverage-audit.json) |
| **Benchmark lưu trữ & đồng nhất RQ2 trên PostgreSQL 18.3 (`STT 31`, `STT 32`)** | Thêm `Week10PersistenceBenchmarkTests.java` (3 JUnit test mới): đo trực tiếp thời gian `SnapshotPublisher.publish` + `AlertService.evaluateNext` + `SnapshotQueries.comparison` ở đúng 3 mốc quy mô `1k / 5k / 10k` commit (`50.08 ms / 63.34 ms / 59.90 ms`), đồng thời kiểm chứng biên `BigDecimal +0.15` (`S4`) và cơ chế chặn `INCOMPLETE_COVERAGE` (`S5`) trong PostgreSQL. | [postgres-persistence-benchmark.json](evidence/postgres-persistence-benchmark.json) |
| **Độ phủ JaCoCo toàn hệ thống có PostgreSQL 18.3 (`NFR08`, `TC2.5`)** | Chạy full `mvnw clean verify` với PostgreSQL 18.3 (`122/122` JUnit pass), xuất báo cáo độ phủ đầy đủ 6 package đạt **89.07% line coverage** (`repository` 100%, `api` 100%, `monitoring` 94.60%, `gitanalyzer` 88.12%, `aireview` 85.88%), bổ sung cho báo cáo 49 unit test không DB (`64.06%`) của bạn. | [jacoco-fullstack-coverage.json](evidence/jacoco-fullstack-coverage.json), [backend-tests.json](evidence/backend-tests.json) |
| **Bộ phiếu khảo sát RQ3 gửi người tham gia (`STT 33`)** | Soạn [mau-khao-sat-rq3-gui-nguoi-tham-gia.txt](mau-khao-sat-rq3-gui-nguoi-tham-gia.txt) và mẫu CSV [rq3-response-template.csv](rq3-response-template.csv) khớp cột của `evaluate_rq3_sus.py` để hai thành viên dùng thu thập dữ liệu người dùng thật trong Tuần 11. | [mau-khao-sat-rq3-gui-nguoi-tham-gia.txt](mau-khao-sat-rq3-gui-nguoi-tham-gia.txt) |
| **Hồ sơ cá nhân & nhóm Tuần 10** | Bổ sung `README.md`, `bao-cao-tuan-10-toan.md`, `bao-cao-tuan-10-nhom.md`, `ai-usage-toan-week10.md`, `ly-thuyet-va-ap-dung.md`, `runbook.md`, `evidence/verification.md` và cập nhật số liệu đồng bộ trong các báo cáo Tuần 10. | Thư mục `docs/week-10/` |

---

## 2. Những điểm cần phối hợp tiếp sang Tuần 11

1. **Tổ chức khảo sát người dùng thật RQ3 (`STT 33`) và cải tiến giao diện (`STT 34`):** Dùng phiếu `mau-khao-sat-rq3-gui-nguoi-tham-gia.txt` để mời tối thiểu 5–9 người (phấn đấu `>= 10` người theo Rubric Mức 5), ghi nhận kết quả thật vào CSV rồi chạy `evaluate_rq3_sus.py` và sửa ít nhất 1 vòng UI theo góp ý.
2. **Giữ nguyên dữ liệu thực nghiệm đã đóng băng:** Mọi file JSON của RQ1, RQ2 pilot (`15/15`), RQ2 scenarios (`S1..S5`), `benchmark-results.json` và `jacoco-coverage-summary.json` (bản 49 test không DB) đều được giữ nguyên tuyệt đối; các file kiểm chứng mới có PostgreSQL của Toản được đặt tên tách biệt (`jacoco-fullstack-coverage.json`, `postgres-persistence-benchmark.json`, `rq2-coverage-audit.json`).
3. **Review và Merge PR:** Nhờ Tưởng review PR nhánh `feat/toan-week-10` để lịch sử Git của cả hai thành viên trong Tuần 10 đều có PR và review chéo đầy đủ theo tiêu chí `TC2.4`.
