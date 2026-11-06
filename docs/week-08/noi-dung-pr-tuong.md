# Mô tả Pull Request — Tuần 8 (Tưởng: `codex/tuong-week8` → `main`)

## Tiêu đề PR
`feat: add production HttpLlmProviderAdapter, week 8 integration review, and weekly progress deliverables`

## Nội dung PR

### 1. Tóm tắt thay đổi
- **STT 25 — Production `LLMProviderAdapter` ([`HttpLlmProviderAdapter.java`](../../backend/src/main/java/com/example/demo/aireview/HttpLlmProviderAdapter.java))**:
  - Cài đặt adapter HTTP gọi chuẩn OpenAI-compatible Chat Completions (`POST /v1/chat/completions`, mặc định cấu hình cho Mistral `codestral-latest`, hỗ trợ cấu hình qua `app.ai-review.provider.*`).
  - Thực thi thời gian chờ theo phần ngân sách còn lại của `budget.deadline()` trừ `300 ms` dự trữ validation (tối đa `30s`), kiểm tra `CancellationToken` và trạng thái interrupt ở mọi bước.
  - Đọc phản hồi qua `HttpResponse.BodyHandlers.ofInputStream()` theo từng khối `4 KiB` và ngắt ngay lập tức nếu tổng byte vượt `maxOutputBytes + 4 KiB`, không nạp phản hồi không giới hạn vào bộ nhớ.
  - Chỉ gửi `messages` (`system` + `user`), `temperature: 0.0`, `max_tokens`, `response_format: {"type": "json_object"}`, không cấp `tools`/`functions`, che khóa API bằng `***REDACTED***` trong `toString()`.
  - Đăng ký `@Bean` có điều kiện `@ConditionalOnProperty(name = "app.ai-review.provider.enabled", havingValue = "true")` và `@ConditionalOnMissingBean`, bảo đảm mặc định tắt và giữ nguyên hành vi trả `503 AI_PROVIDER_UNAVAILABLE` khi chưa bật provider.
- **Kiểm thử ([`LlmReviewServiceTests.java`](../../backend/src/test/java/com/example/demo/aireview/LlmReviewServiceTests.java))**:
  - Bổ sung 4 ca kiểm thử HTTP cục bộ (`AI-T09`..`AI-T12`): parse phản hồi OpenAI-compatible + token usage, ngắt stream quá cỡ ngay khi đang đọc, hết hạn deadline trước và trong khi gọi HTTP, từ chối cấu hình thiếu API key hoặc dùng HTTP ngoài loopback.
- **Tài liệu, Báo cáo & Tiến độ tuần 8**:
  - [`docs/week-08/provider-and-review-tuong.md`](provider-and-review-tuong.md): Chi tiết thiết kế `HttpLlmProviderAdapter` và phản hồi 7 điểm bàn giao tuần 8 của Toản.
  - [`docs/week-08/bao-cao-tuan-08-nhom.md`](bao-cao-tuan-08-nhom.md) & [`docs/week-08/noi-dung-nop-web-tuan-08.md`](noi-dung-nop-web-tuan-08.md): Báo cáo tiến độ nhóm tuần 8 và văn bản nộp form web khoa.
  - [`docs/ai-usage/tuong-week08.md`](../ai-usage/tuong-week08.md): Nhật ký sử dụng AI tuần 8 của Tưởng.
  - Cập nhật [`docs/BaoCaoTienDo_TLCN.xlsx`](../BaoCaoTienDo_TLCN.xlsx) sang Tuần 08 (`19 Hoàn thành / 9 Đang làm / 11 Chưa bắt đầu`).

### 2. Kiểm chứng
- Trên máy Tưởng (JDK 21, không cần PostgreSQL):
  - `mvnw.cmd -B -ntp test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests" -DfailIfNoTests=false`: **45/45 PASS (`BUILD SUCCESS`)**.
  - `python -m unittest discover -s tools/experiments -v`: **8/8 PASS**.
- Kết hợp với bộ kiểm thử PostgreSQL/UI/CI tuần 8 của Toản (`main` `a9fdfc7`, CI run `42207922177`): **115 ca kiểm thử backend duy nhất** và **11 ca kiểm thử Playwright UI**.
