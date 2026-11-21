# Kết quả hoàn thiện Thực nghiệm RQ2 - Theo dõi xu hướng và giải thích biến động (STT 31)

- **Công cụ thực thi:** `HistoricalSnapshotExport.java` (JGit 7.8.0 + Lizard 1.24.3 + RepositoryMetricCalculator v1) + `tools/experiments/evaluate_rq2_scenarios.py` + `tools/experiments/audit_rq2_coverage.py` + `tools/experiments/rq2_trends.py`
- **Dữ liệu kiểm chứng:** [rq2-scenarios-summary.json](evidence/rq2-scenarios/rq2-scenarios-summary.json), [rq2-coverage-audit.json](evidence/rq2-coverage-audit.json) và [pilot/results.md](../week-09/evidence/pilot/results.md)

---

## 1. Kết quả kiểm chứng 5 kịch bản tiến hóa mã nguồn có kiểm soát (`S1..S5`)

Nhóm xây dựng 5 kho Git thực nghiệm có kiểm soát (không giả lập kết quả JSON mà chạy trực tiếp qua pipeline Java/JGit/Lizard tại từng commit SHA quá khứ) để đánh giá đầy đủ 5 tình huống trọng tâm của RQ2:

| Kịch bản | Tình huống mô phỏng | Trạng thái so sánh | Tập tin tiêu biểu | Giá trị hotspot trước -> sau (delta) | Kết quả giải thích & kiểm toán |
|---|---|---|---|---|---|
| **S1** | Tần suất sửa và độ phức tạp (`max_ccn`) cùng tăng trên `A.java` | `COMPARABLE` | `A.java` | `0.0000 -> 1.0000 (+1.0000)` | Sinh đúng `1` ứng viên cảnh báo; `evidenceCommits` khớp đủ `3/3` SHA commit trong cửa sổ sau. |
| **S2** | Đổi tên file `src/LegacyService.java` -> `src/RenamedService.java` rồi tăng nhánh | `COMPARABLE` | `RenamedService.java` | `0.0000 -> 1.0000 (+1.0000)` | Bảo toàn cùng `lineageKey`; `added = 0, removed = 0`; phát hiện đúng `1` cảnh báo qua rename. |
| **S3** | Trượt cửa sổ 180 ngày làm commit cũ rời cửa sổ (`max_ccn = 5` không đổi) | `COMPARABLE` | `StableComplex.java` | `0.8660 -> 0.5000 (-0.3660)` | `frequency: 3 -> 0`, `evidenceCommits = []`, `alertCandidates = 0`. Hệ thống minh bạch lý do trượt cửa sổ, không kết luận nhầm đã sửa code. |
| **S4** | Kiểm chứng đúng biên ngưỡng `+0.15` bằng kiểu `Decimal` trên kho 21 file | `COMPARABLE` | `F08.java` | Bước 1: `0.40 -> 0.55 (+0.15)`<br>Bước 2: `0.55 -> 0.775 (+0.225)` | Tại `+0.15`, `float` IEEE-754 lệch thành `0.15000000000000002`, nhưng so sánh `Decimal` chặn đúng (`alertCandidates = 0`); vượt lên `+0.225` mới báo `alertCandidates = 1`. |
| **S5** | Nhánh merge gây `LINEAGE_AMBIGUOUS` hoặc đổi độ dài cửa sổ (90d vs 180d) | `BLOCKED` | `Main.java` / `A.java` | Cảnh báo chính thức = `null` | Hệ thống chặn phát cảnh báo giả (`INCOMPLETE_COVERAGE` / `WINDOW_DURATION_CHANGED`), giữ phần delta ở mức chẩn đoán (`diagnosticChangesOnly = true`). |

**Kết luận phần kịch bản kiểm soát:** Cả `5/5` kịch bản (`S1..S5`) đều đạt toàn bộ điều kiện kiểm toán dữ liệu, giải thích chính xác nguyên nhân thay đổi và truy vết 100% SHA commit nguồn.

---

## 2. Tổng hợp kiểm toán truy vết trên tập OSS thực tế (Tuần 9 - Tuần 10)

Kiểm tra tự động qua hàm `summarize_pilot_evidence` trên toàn bộ dữ liệu thực tế ở Tuần 9 (`docs/week-09/evidence/pilot/`) cho thấy:

- **9/9 snapshot lịch sử** trên `gs-rest-service`, `spring-petclinic` và `commons-cli` tại 3 mốc (`2024-01-01`, `2025-01-01`, `2026-01-01`) hoàn thành và khớp SHA-256 `15/15` file.
- **6/6 cặp đối chiếu liền kề** đều kích hoạt cơ chế an toàn `BLOCKED` (`INCOMPLETE_COVERAGE`) vì quy tắc nối phả hệ `v1` trong `GitIngestion.java` (dòng 103-110) đánh dấu `LINEAGE_AMBIGUOUS` khi gặp merge commit chỉ có một nhánh cha chạm vào tập tin.
- Trên **91 cặp file chung có điểm chẩn đoán** (`4 + 4` ở `gs-rest-service`, `20 + 21` ở `spring-petclinic`, `6 + 36` ở `commons-cli`), hệ thống truy vết `verify_comparison_traceability` xác nhận `91/91` file (`100%`) có danh sách `evidenceCommits` khớp chính xác với tập `facts` thuộc cửa sổ `[windowStart, windowEnd)`.

| Cặp đối chiếu OSS | Trạng thái | File chung kiểm toán | File có `|delta|` lớn nhất | `freqBefore -> freqAfter` | `hotspotDelta` | `evidenceCommits` |
|---|---|---|---|---:|---:|---:|
| `gs-rest-service` (2024 -> 2025) | `BLOCKED` | `4/4` khớp | `GreetingControllerTests.java` | `0 -> 0` | `0.0000` | `0` |
| `gs-rest-service` (2025 -> 2026) | `BLOCKED` | `4/4` khớp | `GreetingControllerTests.java` | `0 -> 2` | `+0.2071` | `2` |
| `spring-petclinic` (2024 -> 2025) | `BLOCKED` | `20/20` khớp | `PetValidator.java` | `1 -> 0` | `-0.3957` | `0` |
| `spring-petclinic` (2025 -> 2026) | `BLOCKED` | `21/21` khớp | `VetTests.java` | `2 -> 0` | `-0.3079` | `0` |
| `commons-cli` (2024 -> 2025) | `BLOCKED` | `6/6` khớp | `BugCLI252Test.java` | `1 -> 0` | `-0.1923` | `0` |
| `commons-cli` (2025 -> 2026) | `BLOCKED` | `36/36` khớp | `XhtmlHelpAppendable.java` | `9 -> 0` | `-0.4446` | `0` |

### 2.1. Phân rã nguyên nhân loại tập tin (`LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS`) trên 9 snapshot OSS

Để giải quyết câu hỏi tồn đọng từ Tuần 9 về việc tách bạch hai nguyên nhân làm giảm quần thể `hotspotPopulation` và chặn `coverageComplete`, công cụ kiểm toán `tools/experiments/audit_rq2_coverage.py` (kèm 6 unit test trong `test_rq2_coverage_audit.py`) đã phân rã toàn bộ **369 lượt tập tin Java** trên 9 snapshot OSS ([rq2-coverage-audit.json](evidence/rq2-coverage-audit.json)):

| Snapshot lịch sử | Tổng file Java | Quần thể Hotspot (`COMPLETE`) | `NO_FUNCTIONS` (phả hệ rõ, `functions = 0`) | `LINEAGE_AMBIGUOUS` (có `maxCcn`) | `LINEAGE_AMBIGUOUS` kiêm `NO_FUNCTIONS` | Nếu gỡ merge `v1` -> Quần thể Hotspot |
|---|---:|---:|---:|---:|---:|---:|
| `gs-rest-service-0` (2024) | 6 | 4 (66.67%) | 0 | 1 | 1 (`Greeting.java`) | 5 / 6 |
| `gs-rest-service-1` (2025) | 6 | 4 (66.67%) | 0 | 1 | 1 (`Greeting.java`) | 5 / 6 |
| `gs-rest-service-2` (2026) | 6 | 4 (66.67%) | 0 | 1 | 1 (`Greeting.java`) | 5 / 6 |
| `spring-petclinic-0` (2024) | 40 | 20 (50.00%) | 10 | 9 | 1 | 29 / 40 |
| `spring-petclinic-1` (2025) | 40 | 21 (52.50%) | 10 | 7 | 2 | 28 / 40 |
| `spring-petclinic-2` (2026) | 47 | 22 (46.81%) | 12 | 7 | 6 | 29 / 47 |
| `commons-cli-0` (2024) | 52 | 6 (11.54%) | 1 (`package-info.java`) | 44 | 1 | 50 / 52 |
| `commons-cli-1` (2025) | 85 | 36 (42.35%) | 2 (`package-info.java`) | 45 | 2 | 81 / 85 |
| `commons-cli-2` (2026) | 87 | 38 (43.68%) | 2 (`package-info.java`) | 45 | 2 | 83 / 87 |
| **Tổng 9 snapshot OSS** | **369** | **155 (42.01%)** | **37 (10.03%)** | **160 (43.36%)** | **17 (4.61%)** | **315 / 369 (85.37%)** |

**Phát hiện kỹ thuật từ phân rã:**
- Tập tin chỉ có `NO_FUNCTIONS` (`37` trường hợp phả hệ rõ, ví dụ các `interface` Spring Data Repository hoặc `package-info.java`) vẫn có `frequency != null` và `functions = 0`, nên **không** làm `coverageComplete = false`; chúng chỉ được tách khỏi mẫu số tính phân vị `hotspotPopulation`.
- Ngược lại, `177` trường hợp `LINEAGE_AMBIGUOUS` (`160` file có hàm + `17` file không hàm) có `frequency = null` nên trực tiếp làm `coverageComplete = false`. Nếu quy tắc nối phả hệ qua merge được nâng cấp ở `v2`, tỷ lệ tập tin có điểm hotspot trên 9 snapshot sẽ tăng từ **42.01% (`155/369`)** lên **85.37% (`315/369`)** (54 thực thể còn lại là interface/record/package-info không chứa thân phương thức).

### 2.2. Kiểm chứng đồng nhất bất biến RQ2 trên tầng lưu trữ PostgreSQL 18.3

Bộ kiểm thử tích hợp `Week10PersistenceBenchmarkTests.java` xác nhận tầng backend sản phẩm (`SnapshotPublisher`, `AlertService`, `SnapshotQueries`) trên PostgreSQL 18.3 thực thi đồng nhất tuyệt đối với công cụ Python RQ2:
- Ca `hotspotIncreaseAlertRespectsExactBigDecimalBoundaryInPostgres`: bước tăng `0.400000 -> 0.550000` (đúng bằng biên `+0.150000`) không phát sinh cảnh báo giả; bước tăng `0.550000 -> 0.775000` (`+0.225000 > 0.15`) sinh đúng `1` cảnh báo `OPEN`.
- Ca `incompleteCoverageSnapshotBlocksComparisonAndAlertGenerationInPostgres`: khi snapshot đích có `coverageComplete = false`, `AlertService` ghi nhận `INCOMPLETE_COVERAGE` vào `evaluation_notes` và không tạo cảnh báo giả, đồng thời `SnapshotQueries.comparison` trả mã `409 CONFLICT`.

## 3. Nhận xét đánh giá cho RQ2

1. **Khi phả hệ đầy đủ (`coverageComplete = true`):** Hệ thống phát hiện chính xác file tăng rủi ro thật kể cả sau khi đổi tên (`S1`, `S2`), loại bỏ sai số làm tròn nhị phân tại biên ngưỡng `+0.15` (`S4`) và giải thích rành mạch trường hợp giảm điểm do trượt cửa sổ thay vì hiểu nhầm là đã cải thiện code (`S3`).
2. **Giới hạn thực tế trên kho OSS nhiều merge (`v1`):** Quy tắc bảo thủ ở `GitIngestion.java` giúp tránh ghép sai phả hệ (không báo động giả), nhưng làm giảm tỷ lệ snapshot đủ `coverageComplete = true` trên các dự án dùng GitHub Merge PR. Đây là cơ sở định lượng rõ ràng để nhóm đề xuất bản nâng cấp quy tắc phả hệ `v2` (phân tách nhánh thực sự xung đột và merge giữ nguyên nội dung từ một nhánh cha) ở hướng phát triển tiếp theo.
