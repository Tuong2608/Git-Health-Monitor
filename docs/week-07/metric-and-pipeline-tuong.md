# Bàn giao tuần 7 — Phần Tưởng: Bộ tính chỉ số, Lizard XML, Adapter `AnalysisPipeline` và `LlmReviewService`

Sinh viên: **Trần Văn Tưởng — 23110170**. Tuần 7 (25/10–31/10/2026), thực hiện ngày 30/10/2026 trên nền `main` `32eb6a7` (PR #6 của Toản) và nhánh tuần 6 `codex/tuong-week6` (`6684b74`).

## 1. Phạm vi triển khai

| STT | Công việc | Lớp triển khai | Trạng thái |
|---|---|---|---|
| 21 | Cài mô-đun thu thập dữ liệu Git & nối adapter | `GitAnalysisPipeline`, `GitMetricAnalysisSample` | Đã nối xong hợp đồng `AnalysisPipeline`; chờ chạy persistence thật với PostgreSQL |
| 22 | Cài mô-đun tính chỉ số & đo độ phức tạp | `LizardComplexityAnalyzer`, `RepositoryMetricCalculator` | Đã cài đầy đủ công thức v1 + đo trên 3 repository công khai |
| 19 & 25 | Thiết kế & cài đặt module trợ lý rà soát bằng LLM | `LlmReviewService` (`com.example.demo.aireview`) | Đã cài engine, kiểm tra consent/secret/budget/quota/schema + 8/8 ca `AI-T01`..`AI-T08` |

## 2. Tuân thủ hợp đồng tích hợp tuần 7 (`integration-contract.md`)

1. **`commitCount` = `selectedCommits()`**: `RepositoryMetricCalculator` gán `SnapshotData.commitCount = ingestion.selectedCommits()`, lưu `reachableCommits` riêng trong `summary.reachableCommits`. Ràng buộc `frequency <= commitCount` luôn thỏa.
2. **Định danh `lineageKey` và tách nguồn gốc**: giữ nguyên `birthSha:birthPath` từ `GitIngestion`; tách `firstSeenSha` và `initialPath` tại dấu `:` đầu tiên.
3. **Phân biệt `null` (không đo được) với `0` (đo được bằng 0)**:
   - File có cờ `LINEAGE_AMBIGUOUS`: `frequency = null`, `churn = null`, `hhi = null`, `hotspot = null`, `unavailableReason = "LINEAGE_AMBIGUOUS"`, `contributions = []`, và `evidence` **không** chứa mảng `"commits"` (để `SnapshotQueries` trả `evidenceAvailable = false` đúng chuẩn V3).
   - File có diff nhị phân (`BINARY`) hoặc không phải file thường (`NON_REGULAR_FILE`): `churn = null`, `contributions = []`, `hhi = null`, kèm `unavailableReason`.
   - File không có hàm (`functionCount = 0`, ví dụ `interface` hoặc `record` rỗng): giữ `nloc` đã đo, `maxCcn = null`, `avgCcn = null`, `hotspot = null`, `unavailableReason = "NO_FUNCTIONS"`.
   - File chỉ có rename thuần (`churn = 0`): `contributions = []`, `hhi = null`, gắn cờ `NO_LINE_CONTRIBUTION` trong `qualityFlags`.
4. **Bảo vệ thông tin tác giả và định dạng `evidence`**:
   - `Contribution.displayName` tự động cắt bỏ phần `@domain` nếu `authorKey` là email, tránh lộ email thô qua endpoint `/contributions`.
   - `FileData.evidence` và `PairData.evidence` dùng key chuẩn `"commits"` để tương thích trực tiếp với truy vấn phân trang `jsonb_array_elements` trong `SnapshotQueries`.
5. **Không kích hoạt bean ngầm làm hỏng test readiness**: `GitAnalysisPipeline.PipelineConfiguration` chỉ tạo bean `AnalysisPipeline` khi bật tường minh `app.analysis.pipeline.enabled=true` và `@ConditionalOnMissingBean(AnalysisPipeline.class)`, giữ nguyên hành vi mặc định `pipelineAvailable = false` cho các ca kiểm thử readiness tuần 6–7 của Toản.

## 3. Hai lỗi thực tế phát hiện và đã sửa khi chạy Lizard trên repository công khai

1. **Treo tiến trình con (`DEADLINE_EXCEEDED`) trên `spring-petclinic` (50 file Java)**:
   - Nguyên nhân: `python -m lizard --xml` xuất XML lớn làm đầy bộ đệm pipe stdout của hệ điều hành khi `process.waitFor(...)` được gọi trước lúc đọc hết `InputStream`.
   - Cách sửa: dùng `ProcessBuilder.redirectOutput(outFile)` ghi thẳng XML ra tệp tạm và xóa trong khối `finally`. Thời gian phân tích toàn bộ `spring-petclinic` giảm xuống ~8 giây.
2. **Lizard tự bỏ qua file trùng nội dung trong cùng lần quét trên `gs-rest-service`**:
   - Nguyên nhân: `initial/.../RestServiceApplication.java` và `complete/.../RestServiceApplication.java` có cùng nội dung byte nên Lizard mặc định coi file thứ hai là bản sao và không xuất vào `<measure type="File">`, dẫn tới báo nhầm `CCN_PARSE_ERROR`.
   - Cách sửa: deduplicate các file tại `headSha` theo JGit blob `ObjectId` trước khi ghi ra thư mục tạm cho Lizard, sau đó ánh xạ kết quả về mọi đường dẫn cùng `ObjectId`.

## 4. Kết quả kiểm thử và đo thực tế (30/10/2026)

### 4.1. Kiểm thử tự động không cần database (`VERIFIED-HERE`)

Lệnh chạy:

```powershell
$env:JAVA_HOME='C:\Users\Acer\.jdks\jbr-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
cd backend
.\mvnw.cmd -B -ntp test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests" -DfailIfNoTests=false
```

Kết quả: **41/41 ca kiểm thử đạt (`BUILD SUCCESS`)**:
- `GitIngestionTests`: 10/10 đạt
- `GitIngestionSampleTests`: 2/2 đạt
- `RepositoryMetricAndPipelineTests`: 5/5 đạt (gồm gọi Lizard 1.24.3 thật, deduplicate blob trùng, midrank percentile/hotspot, HHI 75/25 & 50/50, che email, `LINEAGE_AMBIGUOUS`, lọc `largeChangesetFilter`, và chạy `GitAnalysisPipeline` qua `Claim`/`Lease`)
- `LlmReviewServiceTests`: 8/8 đạt (đủ 8 ca nghiệm thu thiết kế `AI-T01` → `AI-T08`)
- `GitHubRepositoryUrlTests`: 16/16 đạt

### 4.2. Số liệu đo bằng `GitMetricAnalysisSample` trên 3 repository công khai (`VERIFIED-HERE`)

Lệnh chạy:

```powershell
java -cp "target/classes;$(Get-Content target/cp.txt)" `
  com.example.demo.gitanalyzer.GitMetricAnalysisSample `
  <repo-url> <cache-dir> <window-days>
```

| Repository (window) | reachable / selected | File Java (`U`) | Tập mẫu Hotspot (`R`) | File bất định | File `fn=0` | Cặp coupling (`shared>=1`) | Top 1 Hotspot (`score`, `freq`, `churn`, `maxCcn`, `hhi`) |
|---|---|---|---|---|---|---|---|
| `spring-guides/gs-rest-service` (20000 ngày) | 421 / 396 | 6 | 4 | 2 | 1 (`Greeting.java`) | 6 (0 cặp `>=5`) | `GreetingControllerTests.java` (`0.7071`, freq=8, churn=246, maxCcn=1, hhi=0.2734) |
| `spring-projects/spring-petclinic` (365 ngày) | 1047 / 49 | 50 | 25 | 13 | 18 | 133 (0 cặp `>=5`) | `OwnerController.java` (`0.9583`, freq=5, churn=21, maxCcn=7, hhi=0.4603) |
| `octocat/Hello-World` (20000 ngày) | 3 / 2 | 0 | 0 | 0 | 0 | 0 | Không có file Java (`coverageComplete=true`) |

Log đầy đủ được lưu tại `outputs/week7-progress-20261030/{gs-rest-service,spring-petclinic,hello-world}-metrics.log`.

## 5. Giới hạn hiện tại (không tuyên bố quá phạm vi)

- Máy phát triển của Tưởng hiện vẫn hỏng Docker Engine và chưa dựng lại PostgreSQL local, nên **46 ca kiểm thử cần PostgreSQL** (`RepositoryContractTests` 18, `MonitoringIntegrationTests` 15, `Week7ApiTests` 14, `RepositoryApiTests` 4, `Week7WorkerHttpTests` 3, `MonitoringApiTests` 3, `MonitoringDisabledTests` 1, `RegistrationDisabledTests` 1, `DemoApplicationTests` 1) chưa tự chạy lại trên máy này; kết quả 58/88 test của Toản trên PostgreSQL 18.3 được ghi nhận rõ là bàn giao của thành viên cùng nhóm.
- `LlmReviewService` mới kiểm thử với provider adapter giả lập theo hợp đồng (`AI-T01`..`AI-T08`), chưa nối HTTP controller và chưa gọi LLM provider thương mại.
- Chưa chốt 3 quyết định thực nghiệm RQ1 từ tuần 5 (split 80/20 hay 180+30 ngày, tie-break chung, phạm vi metric báo cáo), chưa có nhãn thực nghiệm hay benchmark 10.000 commit.
