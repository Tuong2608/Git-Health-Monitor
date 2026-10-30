# Báo cáo tiến độ nhóm — Tuần 7 (25/10–31/10/2026)

Đề tài: **Git Health Monitor**. Thành viên: Trần Văn Tưởng (`23110170`, nhóm trưởng) & Trần Quang Toản (`23110158`).
Nền đối chiếu: `main` `32eb6a7` (đã merge PR #6 tuần 7 của Toản) gộp cùng nhánh làm việc của Tưởng (`codex/tuong-week6` → `codex/tuong-week7`).
Tổng hợp trạng thái 39 đầu việc trong workbook `BaoCaoTienDo_TLCN.xlsx`: **13 việc Hoàn thành, 13 việc Đang làm, 13 việc Chưa bắt đầu**.

## 1. Kết quả theo từng đầu việc trong tuần 7

- **STT 21 — Cài đặt mô-đun thu thập dữ liệu Git (Tưởng, tuần 6–7) — Đang làm.**
  - Tiếp nối `GitRepositoryClient` và `GitIngestion` của tuần 6, đã cài `GitAnalysisPipeline` (implement `AnalysisPipeline`) và `GitMetricAnalysisSample` để nối luồng thu thập JGit với bộ đo độ phức tạp và tính chỉ số, kiểm tra `Lease.checkpoint()` ở từng chặng và sinh cấu hình JSON chuẩn hóa cho `effectiveConfig()`.
  - Giữ trạng thái *Đang làm* sang tuần 8 vì chưa chạy lưu snapshot từ repo công khai thẳng vào PostgreSQL thật trên máy nhóm trưởng.
- **STT 22 — Cài đặt mô-đun tính chỉ số (Tưởng, tuần 7–8) — Đang làm.**
  - Đã cài `LizardComplexityAnalyzer` (đọc trực tiếp blob tại `headSha` từ JGit object database, deduplicate theo `ObjectId`, gọi `python -m lizard --xml` qua tệp tạm không bị nghẽn pipe buffer, trích `max_ccn`, `avg_ccn`, `function_count`, `nloc`) và `RepositoryMetricCalculator` (tính `change_frequency`, `churn`, `HHI`, `hotspot_score` theo midrank percentile `sqrt(P_change * P_complexity)`, và `temporal_coupling` `shared / min(countA, countB)`).
  - Tuân thủ đầy đủ hợp đồng V3: `commitCount = selectedCommits`, tách `firstSeenSha`/`initialPath` tại dấu `:` đầu tiên, giữ `null` + `unavailableReason` cho `LINEAGE_AMBIGUOUS` / `BINARY` / `NO_FUNCTIONS`, không sinh HHI khi `churn = 0`, và ẩn phần `@domain` trong `displayName`.
  - Đã chạy thực tế trên 3 repository công khai (`gs-rest-service`, `spring-petclinic`, `Hello-World`): xác định đúng `#1 hotspot` trên `spring-petclinic` (365 ngày) là `OwnerController.java` (`hotspot = 0.9583, freq = 5, churn = 21, maxCcn = 7, fn = 12, nloc = 129, hhi = 0.4603`).
- **STT 23 — Cài đặt mô-đun lưu trữ và giám sát (Toản, tuần 6–7) — Đang làm.**
  - Bổ sung migration `V3__unavailable_metrics_and_retry_provenance.sql` cho phép lưu `NULL` có lý do (`unavailable_reason`) trên `file_metric`, thêm `retry_of` có khóa ngoại cùng repository trên `analysis_job`, và kiểm tra tính toàn vẹn của `Claim` khi publish.
- **STT 24 — Cài đặt tầng API (Toản, tuần 7–8) — Đang làm.**
  - Cài `SnapshotQueries` và `SnapshotQueryController`: API đọc chi tiết file, danh sách đóng góp (ẩn `identity_key`/`aliases`), phân trang bằng chứng (`evidence`), coupling hai chiều, xu hướng (`trend`) giữ danh tính qua rename, so sánh hai snapshot (`comparison` phân biệt `BASE_ZERO` và `UNAVAILABLE`), lịch sử job và `POST /api/jobs/{id}/retry`.
- **STT 19 & STT 25 — Thiết kế (Hoàn thành) & Cài đặt (Đang làm) module trợ lý rà soát bằng LLM (Tưởng, tuần 5–8).**
  - Chốt thiết kế cơ chế `ConsentReceipt` gắn `contextHash` SHA-256 bất biến (đóng STT 19 sang *Hoàn thành*).
  - Cài đặt `LlmReviewService` (STT 25): kiểm tra `ConsentReceipt`, ghim đúng `headSha` của snapshot trên mọi đoạn mã/diff, quét secret trước khi gọi provider, kiểm soát giới hạn kích thước (tổng $\le 32$ KiB, diff $\le 16$ KiB / 200 dòng, mã nguồn $\le 8$ KiB / 200 dòng), giới hạn đồng thời/rate limit, render prompt `ai-review/v1` và xác thực JSON schema nghiêm ngặt + đối chiếu allowlist `evidenceId`. Đạt **8/8 ca kiểm thử `AI-T01`..`AI-T08`**.

## 2. Tổng hợp kiểm chứng và giới hạn môi trường

- **Tưởng tự chạy trên máy phát triển ngày 30/10 (`VERIFIED-HERE`)**:
  - `mvnw test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests"`: **41/41 ca kiểm thử đạt (`BUILD SUCCESS`)** (tăng từ 28 ca ở tuần 6 lên 41 ca nhờ thêm 5 ca metric/pipeline và 8 ca LLM review).
  - Toàn bộ 25 file mã nguồn chính và 15 file test của hai thành viên biên dịch chung không lỗi trên JDK 21.
  - Khi chạy `mvnw verify` toàn bộ dự án trên máy Tưởng: **100 ca chạy / 41 ca đạt / 59 ca lỗi** — cả 59 ca lỗi đều do thiếu kết nối PostgreSQL ở `localhost:5432` (Docker Engine trên máy Tưởng vẫn hỏng).
- **Toản bàn giao trên môi trường có PostgreSQL 18.3 (`HANDOFF-UNVERIFIED` trên máy Tưởng)**:
  - **58/58 test backend đạt** trên nhánh Toản; **88/88 test đạt** khi ghép phần backend tuần 7 của Toản với nhánh tuần 6 của Tưởng (`15f291b`).
  - Frontend sau khi vá `zod 4.6.5 → 4.6.7`: `npm audit` 0 cảnh báo, lint/build đạt, 3 UI test đạt (1 live test skip).

## 3. Kế hoạch tuần 8

- Dựng lại PostgreSQL (hoặc chạy trên môi trường có DB thật) để chạy tích hợp end-to-end từ `POST /api/repositories/{id}/analyses` → `AnalysisWorker` → `GitAnalysisPipeline` → `SnapshotPublisher` → `AlertService` → `SnapshotQueries` (STT 28).
- Tiếp tục cài đặt giao diện người dùng kết nối API tuần 7 (STT 26) và nối endpoint/provider cho module AI review (STT 25).
- Hai thành viên họp chốt 3 điểm thiết kế thực nghiệm còn mở từ tuần 5 (temporal split, quy tắc đồng hạng, phạm vi metric báo cáo) để đóng STT 20 trước khi sang giai đoạn thực nghiệm tuần 9.
