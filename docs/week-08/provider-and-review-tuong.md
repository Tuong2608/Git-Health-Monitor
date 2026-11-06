# Tuần 8 — Adapter LLM Provider thật & Rà soát tích hợp hệ thống (Trần Văn Tưởng — 23110170)

Ngày thực hiện: **06/11/2026**. Nhánh làm việc: `codex/tuong-week8` trên nền `main` `a9fdfc7` (đã merge PR #9 tuần 8 của Trần Quang Toản).

---

## 1. Cài đặt `HttpLlmProviderAdapter` (STT 25 — Module trợ lý rà soát bằng LLM)

Tiếp nối `LlmReviewService` (tuần 7) cùng `ReviewContextBuilder` và `AiReviewController` (Toản cài ở tuần 8), phần tuần 8 của Tưởng hoàn thiện tầng kết nối nhà cung cấp LLM thực tế tại [`HttpLlmProviderAdapter.java`](../../backend/src/main/java/com/example/demo/aireview/HttpLlmProviderAdapter.java):

### 1.1. Cấu hình Provider, Model Budget và Chính sách dữ liệu
- **Giao thức**: Chuẩn OpenAI-compatible Chat Completions (`POST /v1/chat/completions`) qua HTTPS, không phụ thuộc SDK nặng. Mặc định cấu hình hướng tới **Mistral AI (`codestral-latest`, `https://api.mistral.ai/v1/chat/completions`)** hoặc có thể trỏ sang bất kỳ endpoint tương thích (Gemini OpenAI-compat, Cerebras, OpenAI) qua biến môi trường mà không cần sửa mã.
- **Cơ chế kích hoạt an toàn (Fail-closed & Opt-in)**:
  - Bean `LlmReviewService.LLMProviderAdapter` chỉ được đăng ký khi bật đồng thời `@ConditionalOnProperty(name = "app.ai-review.provider.enabled", havingValue = "true")` và `@ConditionalOnMissingBean(LlmReviewService.LLMProviderAdapter.class)`.
  - Khi `app.ai-review.provider.enabled=false` (mặc định trong `application.properties`, `.env.example`, `compose.yaml`), hệ thống không tạo fake provider trong runtime; nếu bật `app.ai-review.enabled=true` mà chưa bật provider, `POST /api/snapshots/{snapshot}/files/{file}/ai-reviews` trả đúng `503 AI_PROVIDER_UNAVAILABLE` (tương thích 100% với `AiReviewProviderUnavailableTests`).
  - Khi bật `app.ai-review.provider.enabled=true` nhưng thiếu `APP_AI_REVIEW_PROVIDER_API_KEY` hoặc dùng URL `http://` không phải loopback, `HttpLlmProviderAdapter.Config` ném ngoại lệ ngay lúc khởi tạo.
- **Ngân sách token & byte (`ReviewBudget` & `Config`)**:
  - `maxInputTokens = 16384`, `maxOutputTokens = 2048`, `maxOutputBytes = 16384` bytes.
  - Kiểm tra `prompt.estimatedInputTokens() + requestedOutputTokens <= config.maxModelInputTokens()` trước khi gửi HTTP request.
- **Chính sách dữ liệu & Chống lạm dụng công cụ**:
  - Request chỉ gửi `messages` (`system` + `user` từ `RenderedPrompt`), `temperature: 0.0`, `max_tokens`, và `response_format: {"type": "json_object"}`.
  - **Không** khai báo `tools` hay `functions`, không cho phép redirect (`HttpClient.Redirect.NEVER`), không ghi log API key (`Config.toString()` che bằng `***REDACTED***`), không ghi log mã nguồn, prompt hay raw response.

### 1.2. Thực thi Deadline, Cancellation và Giới hạn Byte khi đọc Stream
- **Tính thời gian còn lại theo `budget.deadline()`**:
  - Trước khi mở kết nối HTTP, adapter tính `remaining = Duration.between(Instant.now(), budget.deadline()).minus(VALIDATION_RESERVE)` (dự trữ `300 ms` cho bước kiểm tra JSON schema/allowlist sau khi nhận phản hồi) và lấy `effectiveTimeout = min(remaining, maxRequestTimeout)` (tối đa `30 giây`).
  - Nếu `remaining <= 0`, từ chối ngay với mã `DEADLINE_EXCEEDED` (ánh xạ sang HTTP `504` tại `AiReviewController`) mà không gọi mạng.
- **Kiểm tra hủy tác vụ (`CancellationToken` & Thread Interrupt)**:
  - Kiểm tra `budget.cancellation().isCancelled()` và `Thread.currentThread().isInterrupted()` trước khi gửi request và trong từng vòng lặp đọc dữ liệu để trả về `CANCELLED` (`503`).
- **Giới hạn byte ngay trong lúc đọc (`HttpResponse.BodyHandlers.ofInputStream()`)**:
  - Không dùng `BodyHandlers.ofString()` (tránh việc upstream trả phản hồi khổng lồ làm tràn bộ nhớ trước khi bước kiểm tra kích thước chạy).
  - Đọc `InputStream` theo từng khối `4 KiB`; ở mỗi khối đều kiểm tra `deadline`, `cancellation` và tổng số byte đã nhận (`maxEnvelopeBytes = maxOutputBytes + 4 KiB`). Nếu vượt ngưỡng, đóng stream ngay lập tức và báo `PROVIDER_RESPONSE_TOO_LARGE` → `ReviewError.PROVIDER_FAILED` (`502`).

---

## 2. Rà soát & Phản hồi 7 điểm bàn giao tuần 8 của Toản

Đối chiếu [ban-giao-tuong.md](ban-giao-tuong.md) và [api-and-ai-contract.md](api-and-ai-contract.md):

1. **`ReviewContextBuilder` và `AiReviewController`**:
   - Đã rà soát: luồng `POST /ai-review-preview` trả đúng `contextHash` của chuỗi JSON chuẩn hóa (`LlmReviewService.contextJson(c)`) và `POST /ai-reviews` dựng lại `VerifiedReviewContext` từ DB + Git blob tại `headSha` của snapshot, đối chiếu `contextHash` (`409 CONTEXT_CHANGED_PREVIEW_AGAIN` nếu lệch) rồi gắn `ConsentReceipt` do server đóng dấu thời gian. Việc để `diff = null` ở v1 và dùng cặp bằng chứng `E1` (`SNAPSHOT_METRICS`), `E2` (`SOURCE_EXCERPT` dòng `startLine..endLine`) hoàn toàn khớp với `LlmReviewService`.
2. **Đăng ký bean `LLMProviderAdapter` thật**:
   - Đã cài đặt tại [`HttpLlmProviderAdapter.java`](../../backend/src/main/java/com/example/demo/aireview/HttpLlmProviderAdapter.java) cùng 4 ca kiểm thử HTTP cục bộ mới (`AI-T09`..`AI-T12` trong [`LlmReviewServiceTests.java`](../../backend/src/test/java/com/example/demo/aireview/LlmReviewServiceTests.java)). Giữ nguyên nguyên tắc tắt mặc định khi chưa có xác thực người dùng (authorization).
3. **Bổ sung `pipelineVersion`, `lizardVersion`, `jgitVersion`, `authorAliases` vào `GitAnalysisPipeline.effectiveConfig()`**:
   - Nhất trí 100%: giúp `config_hash` phản ánh đúng phiên bản công cụ và cấu hình alias, ngăn việc nối chung đường xu hướng (trend) giữa snapshot cũ và snapshot dùng pipeline mới.
4. **Migration `V4__ai_review_audit.sql` và Git cache volume**:
   - Bảng `ai_review_audit` chỉ lưu `review_id`, `snapshot_id`, `file_id`, `created_at` và `metadata` (`jsonb`), tuyệt đối không lưu mã nguồn, prompt hay nội dung đầu ra. Volume `gitcache:/app/cache` trong `compose.yaml` bảo đảm `ReviewContextBuilder` luôn đọc được blob ở đúng `headSha` của snapshot ngay cả khi nhánh từ xa đã tiến lên commit mới.
5. **Docker hóa Backend (kèm Python + Lizard 1.24.3), Frontend (Nginx proxy) và CI**:
   - Đã kiểm tra kết quả GitHub Actions CI trên `main` (`a9fdfc7`, run `42207922177`): cả 5 job (`experiments`, `backend`, `frontend`, `secret-scan`, `package`) đều **PASS (`success`)**, bao gồm đóng gói 2 Docker image và chạy `docker compose up -d --build` kiểm tra `/api/health`, trang chủ UI và `/api/analysis-capabilities` (`canAnalyze = true`).
6. **Hiện trạng Staging (STT 27 & 28)**:
   - Đồng ý không tính URL staging cũ từ tuần 3 (Vercel `410`, Render `/api/analysis-capabilities` `404`) làm minh chứng triển khai tuần 8. Trong bảng tiến độ tuần 8, hai đầu việc dùng chung **STT 27** (Đóng gói & triển khai Docker) và **STT 28** (Tích hợp & kiểm thử toàn hệ thống) được chuyển từ *Chưa bắt đầu* sang **Đang làm** (đã hoàn tất mã Docker/Compose, CI Compose smoke và tích hợp local PostgreSQL/UI, còn chờ cấu hình lại môi trường staging mới và chạy thử nghiệm với khóa API thương mại).
7. **Cảnh báo `npm audit` ở mức moderate**:
   - Nhất trí giữ nguyên khóa phụ thuộc hiện tại (`0 high/critical`, `10 moderate` thuộc chuỗi `minimatch` của bộ công cụ lint phát triển), không ép hạ cấp major ESLint ngoài phạm vi.

---

## 3. Minh chứng kiểm thử bổ sung của Tưởng (Tuần 8)

- **Bộ kiểm thử backend không cần PostgreSQL (chạy trực tiếp trên máy Tưởng ngày 06/11/2026)**:
  - Lệnh chạy:
    ```powershell
    .\mvnw.cmd -B -ntp test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests" -DfailIfNoTests=false
    ```
  - Kết quả: **45/45 ca kiểm thử đạt (`BUILD SUCCESS`)** (tăng từ 41 ca ở tuần 7 lên 45 ca nhờ bổ sung 4 ca `AI-T09`..`AI-T12` kiểm tra `HttpLlmProviderAdapter` với HTTP server nội bộ: parse OpenAI-compatible JSON + token usage, ngắt stream ngay khi vượt giới hạn byte, hết hạn deadline trước và trong khi gọi HTTP, từ chối cấu hình thiếu API key / dùng HTTP từ xa và ẩn secret trong `toString()`).
  - Log lưu tại: `outputs/week8-progress-20261106/db-free-tests.log`.
- **Bộ kiểm thử công cụ đánh giá xếp hạng thực nghiệm (`tools/experiments`)**:
  - Lệnh chạy: `python -m unittest discover -s tools/experiments -v`
  - Kết quả: **8/8 ca đạt (`OK`)**.
- **Tổng số ca kiểm thử backend toàn dự án**:
  - Kết hợp 111 ca kiểm thử có PostgreSQL của Toản (đã xác nhận lại trên GitHub Actions CI run `42207922177`) và 4 ca kiểm thử `HttpLlmProviderAdapter` mới của Tưởng: **115 ca kiểm thử backend duy nhất**.
