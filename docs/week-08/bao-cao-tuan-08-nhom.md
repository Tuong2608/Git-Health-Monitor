# Báo cáo tiến độ nhóm — Tuần 8 (01/11–07/11/2026)

Đề tài: **Git Health Monitor**. Thành viên: Trần Văn Tưởng (`23110170`, nhóm trưởng) & Trần Quang Toản (`23110158`).
Nền đối chiếu: `main` `a9fdfc7` (đã merge PR #9 tuần 8 của Toản) gộp cùng nhánh làm việc tuần 8 của Tưởng (`codex/tuong-week8`).
Tổng hợp trạng thái 39 đầu việc trong workbook `BaoCaoTienDo_TLCN.xlsx`: **19 việc Hoàn thành, 9 việc Đang làm, 11 việc Chưa bắt đầu**.

---

## 1. Kết quả theo từng đầu việc trong tuần 8

- **STT 21 (Hoàn thành) & STT 22 (Hoàn thành) — Mô-đun thu thập dữ liệu Git và tính toán chỉ số (Tưởng, tuần 6–8)**:
  - Tuần này `GitAnalysisPipeline` (gồm `GitRepositoryClient`, `GitIngestion`, `LizardComplexityAnalyzer`, `RepositoryMetricCalculator`) đã được chạy tích hợp end-to-end qua hàng đợi PostgreSQL 18.3 thật trên 3 repository công khai (`spring-guides/gs-rest-service`, `spring-projects/spring-petclinic`, `octocat/Hello-World`, cửa sổ 365 ngày) và kiểm chứng qua `Week8IntegrationTests` (8/8 ca đạt).
  - Bổ sung `pipelineVersion = "git-metrics-v2"`, `lizardVersion = "1.24.3"`, `jgitVersion` và `authorAliases` vào `GitAnalysisPipeline.effectiveConfig()` để `config_hash` của snapshot phản ánh đầy đủ phiên bản công cụ phân tích, tránh nối nhầm đường xu hướng với cấu hình cũ.
- **STT 23 (Hoàn thành) & STT 24 (Hoàn thành) — Mô-đun lưu trữ, giám sát và tầng REST API (Toản, tuần 6–8)**:
  - Bổ sung migration `V4__ai_review_audit.sql` lưu siêu dữ liệu truy vết AI review (`review_id`, `snapshot_id`, `file_id`, `created_at`, `metadata` JSONB) có khóa ngoại tới `file_metric(snapshot_id, file_id)`, không lưu mã nguồn, prompt hay văn bản đầu ra.
  - Cài đặt `ReviewContextBuilder` (nạp metric từ snapshot, kiểm tra repository còn công khai qua HTTP `HEAD` không redirect/credential, đọc trực tiếp blob từ Git cache tại đúng `headSha` của snapshot với giới hạn $\le 200$ dòng và $\le 8$ KiB) và `AiReviewController` (`POST /api/snapshots/{snapshot}/files/{file}/ai-review-preview`, `POST /api/snapshots/{snapshot}/files/{file}/ai-reviews` với `ThreadPoolExecutor` giới hạn 2 tác vụ không hàng đợi và deadline 35 giây).
- **STT 25 (Hoàn thành) — Module trợ lý rà soát bằng LLM (Tưởng, tuần 7–8)**:
  - Trên nền `LlmReviewService` (tuần 7) và `ReviewContextBuilder`/`AiReviewController` (Toản cài tuần 8), Tưởng cài đặt adapter nhà cung cấp thực tế [`HttpLlmProviderAdapter.java`](../../backend/src/main/java/com/example/demo/aireview/HttpLlmProviderAdapter.java) kết nối chuẩn OpenAI-compatible Chat Completions qua HTTPS (mặc định cấu hình cho Mistral `codestral-latest`, hỗ trợ đổi sang Gemini/Cerebras/OpenAI qua biến môi trường `APP_AI_REVIEW_PROVIDER_*`).
  - Thực thi đầy đủ các ràng buộc an toàn: kiểm tra thời gian còn lại theo `budget.deadline()` trừ phần dự trữ validation (`300 ms`) và giới hạn tối đa `30 giây`; kiểm tra `CancellationToken` và trạng thái interrupt; đọc phản hồi bằng `HttpResponse.BodyHandlers.ofInputStream()` theo từng khối `4 KiB` và ngắt ngay lập tức nếu tổng byte vượt `maxOutputBytes + 4 KiB`; yêu cầu `response_format = json_object`, `temperature = 0.0`, không cấp `tools`, ẩn API key trong `toString()`.
  - Bean chỉ đăng ký khi bật `app.ai-review.provider.enabled=true` (mặc định `false`), bảo đảm môi trường chưa cấu hình khóa trả đúng `503 AI_PROVIDER_UNAVAILABLE`.
  - Bổ sung 4 ca kiểm thử HTTP nội bộ mới (`AI-T09`..`AI-T12`) trong [`LlmReviewServiceTests.java`](../../backend/src/test/java/com/example/demo/aireview/LlmReviewServiceTests.java), nâng tổng số ca kiểm thử đơn vị của `LlmReviewServiceTests` lên **12/12 ca đạt**.
- **STT 26 (Hoàn thành) — Giao diện người dùng React/TypeScript (Toản, tuần 8)**:
  - Hoàn thiện `AnalysisDashboard.tsx` kết nối dữ liệu thật từ REST API: chọn snapshot, bản đồ điểm nóng (hotspot), bảng chỉ số phân trang, chi tiết file, đóng góp tác giả, bằng chứng commit và temporal coupling, biểu đồ xu hướng (ngắt nối khi khác `configHash`/cửa sổ hoặc `coverageComplete = false`), so sánh hai snapshot, điều khiển job/retry, lịch quét, quy tắc/ACK/CLOSE cảnh báo, và màn hình xem trước/xác nhận consent/hiển thị kết quả AI review.
  - Sửa lỗi tràn ngang trên màn hình di động khi gặp chuỗi SHA và đường dẫn dài; đạt `npm run lint`, `npm run build` và **11/11 ca kiểm thử giao diện Playwright** (gồm 2 ca live chạy với backend thật và snapshot `spring-petclinic`).
- **STT 27 (Đang làm) & STT 28 (Đang làm) — Đóng gói Docker và Tích hợp toàn hệ thống (Cả 2, tuần 8)**:
  - Cập nhật `backend/Dockerfile` cài đặt Python 3 venv + `lizard==1.24.3` chạy dưới tài khoản không đặc quyền `ghm`, bổ sung `frontend/Dockerfile` multi-stage với Nginx proxy `/api` (timeout 40s), cập nhật `compose.yaml` gắn volume bền vững `pgdata` và `gitcache`, và mở rộng `.github/workflows/ci.yaml`.
  - Trên GitHub Actions CI (`main` `a9fdfc7`, run ID `42207922177`), cả 5 job (`experiments`, `backend`, `frontend`, `secret-scan`, `package` gồm `docker compose up -d --build` smoke test) đều **PASS (`success`)**.
  - Giữ **STT 27** và **STT 28** ở trạng thái *Đang làm* vì môi trường cloud staging cũ từ tuần 3 (Vercel trả `410`, Render `/api/analysis-capabilities` trả `404`) chưa được triển khai lại với cấu hình PostgreSQL V4 + Lizard + persistent Git cache mới, và chưa chạy thử nghiệm trực tiếp với khóa LLM thương mại trên môi trường staging.

---

## 2. Tổng hợp kiểm chứng và phân tách nguồn số liệu

- **Tưởng tự chạy trên máy phát triển ngày 06/11/2026 (`VERIFIED-HERE`)**:
  - `mvnw.cmd -B -ntp test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests" -DfailIfNoTests=false`: **45/45 ca kiểm thử không cần PostgreSQL đạt (`BUILD SUCCESS`)** (10 `GitIngestionTests` + 2 `GitIngestionSampleTests` + 5 `RepositoryMetricAndPipelineTests` + 12 `LlmReviewServiceTests` `AI-T01`..`AI-T12` + 16 `GitHubRepositoryUrlTests`).
  - `python -m unittest discover -s tools/experiments -v`: **8/8 ca đạt (`OK`)**.
  - Toàn bộ 28 lớp chính và 17 lớp test biên dịch chung không lỗi trên JDK 21.
- **Toản bàn giao trên máy có PostgreSQL 18.3 & xác nhận trên GitHub Actions CI (`HANDOFF + CI VERIFIED`)**:
  - **111/111 ca kiểm thử backend duy nhất đạt** trên PostgreSQL 18.3 (`109` ca trong `mvnw verify` + `2` ca `AiReviewAvailabilityTests`). Khi cộng thêm 4 ca `HttpLlmProviderAdapter` mới của Tưởng (`AI-T09`..`AI-T12`), toàn dự án có **115 ca kiểm thử backend duy nhất**.
  - **11/11 ca kiểm thử Playwright UI đạt** trên máy local của Toản (gồm 2 ca live với `spring-petclinic`); **9/9 ca UI fixture đạt** trên GitHub Actions CI.
  - Phân tích và lưu PostgreSQL thành công 3 repository công khai (cửa sổ 365 ngày): `spring-guides/gs-rest-service` (6 file Java, 29 commit trong cửa sổ, `coverageComplete = false` do 2 file lineage bất định), `spring-projects/spring-petclinic` (50 file Java, 49 commit, `coverageComplete = false` do 13 file lineage bất định), `octocat/Hello-World` (0 file Java, `coverageComplete = true`).

---

## 3. Kế hoạch tuần 9

- Chốt dứt điểm các tham số thiết kế thực nghiệm (STT 20) và chọn tập dữ liệu thực nghiệm chính thức (STT 29).
- Thực hiện thực nghiệm **RQ1: đánh giá độ chính xác xếp hạng rủi ro** của `hotspot_score` so với 2 baseline (`change_frequency` đơn thuần và `max_ccn` đơn thuần) bằng `Precision@K`, `Recall@K`, `MAP` (STT 30 — Tưởng phụ trách).
- Bắt đầu thực nghiệm **RQ2: theo dõi xu hướng qua nhiều mốc snapshot** (STT 31 — Toản phụ trách) và cập nhật lại môi trường staging (STT 27–28).
