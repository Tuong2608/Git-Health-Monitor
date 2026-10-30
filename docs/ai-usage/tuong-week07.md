# Nhật ký AI hỗ trợ — Tưởng tuần 7

Ngày 30/10/2026. Công cụ: Antigravity (Gemini 4 Argon) và Qoder (Qwen 3.8 Flash). Người dùng yêu cầu hoàn tất phần việc tuần 6, kéo phần tuần 7 của Toản từ `main` (`32eb6a7`), và triển khai phần việc tuần 7 của Tưởng theo phân công.

Nguồn đối chiếu: `main` `32eb6a7` (PR #6 của Toản), nhánh `codex/tuong-week6` (`6684b74`), [Metric Spec v1](../metric-spec-v1.md), [algorithm-design](../algorithm-design.md), [ai-review-scope](../ai-review-scope.md) và [integration-contract](../week-07/integration-contract.md). AI hỗ trợ viết mã `LizardComplexityAnalyzer`, `RepositoryMetricCalculator`, `GitAnalysisPipeline`, `GitMetricAnalysisSample`, `LlmReviewService`, hai bộ test `RepositoryMetricAndPipelineTests` (5 ca) và `LlmReviewServiceTests` (8 ca `AI-T01`..`AI-T08`), cùng tài liệu báo cáo và bảng Excel tuần 7.

Hai lỗi kỹ thuật thực tế phát hiện khi chạy kiểm chứng và đã sửa:
1. Chạy `python -m lizard --xml` qua `ProcessBuilder` trên `spring-petclinic` (50 file Java) làm đầy bộ đệm pipe stdout của hệ điều hành khiến `process.waitFor(...)` bị treo tới hết deadline (`DEADLINE_EXCEEDED`). Đã sửa bằng cách chuyển hướng stdout ra tệp tạm (`redirectOutput(outFile)`), giảm thời gian chạy toàn bộ `spring-petclinic` xuống ~8 giây.
2. Trên `gs-rest-service`, hai file `initial/.../RestServiceApplication.java` và `complete/.../RestServiceApplication.java` có nội dung giống hệt nhau nên Lizard mặc định bỏ qua file thứ hai trong `<measure type="File">`, làm báo nhầm `CCN_PARSE_ERROR`. Đã sửa bằng cách deduplicate theo JGit blob `ObjectId` trước khi gọi Lizard rồi ánh xạ lại cho mọi đường dẫn cùng `ObjectId`.

Kiểm chứng AI tự chạy trên máy ngày 30/10: `mvnw test` cho bộ không cần database đạt **41/41 ca (`BUILD SUCCESS`)**; chạy `GitMetricAnalysisSample` đo chỉ số thật trên `gs-rest-service`, `spring-petclinic` và `Hello-World` (log lưu tại `outputs/week7-progress-20261030/`). Các ca kiểm thử cần PostgreSQL (58/88 ca do Toản báo cáo) vẫn được ghi rõ là bàn giao của thành viên cùng nhóm vì Docker Engine trên máy phát triển của Tưởng chưa hoạt động. Sinh viên kiểm tra lại mã nguồn, công thức và chịu trách nhiệm giải trình trước GVHD.
