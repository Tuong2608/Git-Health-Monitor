# Nội dung PR tuần 7 — phần Tưởng

Tạo PR: `codex/tuong-week7` -> `main`. Tiêu đề gợi ý:

```text
feat: implement metric calculator, Lizard XML analyzer, AnalysisPipeline adapter, and LLM review service
```

## Nội dung PR

```markdown
## Summary

- **STT 22 — Bộ đo độ phức tạp & tính chỉ số (`com.example.demo.gitanalyzer`)**:
  - `LizardComplexityAnalyzer`: đọc trực tiếp blob Java tại `headSha` từ JGit object database, deduplicate theo `ObjectId` để tránh Lizard tự bỏ qua file trùng nội dung, gọi `python -m lizard --xml` qua file tạm tránh nghẽn OS stdout pipe buffer, trích `max_ccn`, `avg_ccn`, `function_count`, `nloc`.
  - `RepositoryMetricCalculator`: tính `change_frequency`, `churn`, `HHI`, `hotspot_score` (midrank percentile `sqrt(P_change * P_complexity)`), và `temporal_coupling` (`shared / min(countA, countB)`), tuân thủ đầy đủ hợp đồng V3 (`docs/week-07/integration-contract.md`).
- **STT 21 — Adapter nối `AnalysisPipeline` & CLI chạy mẫu**:
  - `GitAnalysisPipeline`: nối `GitRepositoryClient` + `GitIngestion` + `LizardComplexityAnalyzer` + `RepositoryMetricCalculator` vào `AnalysisPipeline` (chỉ đăng ký `@Bean` khi `app.analysis.pipeline.enabled=true` và `@ConditionalOnMissingBean` để giữ nguyên hành vi mặc định cho bộ test của Toản).
  - `GitMetricAnalysisSample`: CLI chạy toàn bộ pipeline trên repository công khai không cần PostgreSQL.
- **STT 19 & 25 — Module trợ lý rà soát bằng LLM (`com.example.demo.aireview`)**:
  - `LlmReviewService`: kiểm tra `ConsentReceipt` gắn `contextHash` SHA-256, ghim `headSha`, quét secret, giới hạn byte/dòng (32 KiB / 16 KiB diff / 8 KiB source / 200 dòng), concurrency & rate quota, render prompt `ai-review/v1` và validate JSON schema + allowlist `evidenceId` (`AI-T01`..`AI-T08`).
- **Tài liệu & Báo cáo tuần 7**:
  - `docs/week-07/metric-and-pipeline-tuong.md`, `docs/week-07/bao-cao-tuan-07-nhom.md`, `docs/week-07/noi-dung-nop-web-tuan-07.md`, `docs/ai-usage/tuong-week07.md`.

## Kiểm chứng (`VERIFIED-HERE`, 30/10/2026)

| Lệnh | Kết quả |
|---|---|
| `mvnw test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests"` | **41/41 đạt** (10 ingestion + 2 sample + 5 metric/pipeline + 8 AI review + 16 URL) |
| `GitMetricAnalysisSample` trên `gs-rest-service` (20000d) | 421 reachable / 396 selected, 6 file Java (`R=4`), top 1 hotspot `GreetingControllerTests.java` (`0.7071`) |
| `GitMetricAnalysisSample` trên `spring-petclinic` (365d) | 1047 reachable / 49 selected, 50 file Java (`R=25`), top 1 hotspot `OwnerController.java` (`0.9583`, `maxCcn=7`, `freq=5`) |
| `GitMetricAnalysisSample` trên `octocat/Hello-World` (20000d) | 3 reachable / 2 selected, 0 file Java, `coverageComplete=true` |
```
