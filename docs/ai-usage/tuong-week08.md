# Nhật ký AI hỗ trợ — Tưởng tuần 8

Ngày 06/11/2026. Công cụ: Antigravity (Gemini 4 Argon). Người dùng yêu cầu kéo bản merge tuần 8 mới nhất từ `main` (`a9fdfc7` — PR #9 của Toản), đọc toàn bộ phần việc tuần 8 của Toản và triển khai các đầu việc tuần 8 của Tưởng.

Nguồn đối chiếu: `main` `a9fdfc7`, [ban-giao-tuong.md](../week-08/ban-giao-tuong.md), [api-and-ai-contract.md](../week-08/api-and-ai-contract.md), [ai-review-scope.md](../ai-review-scope.md) và `BaoCaoTienDo_TLCN.xlsx`. AI hỗ trợ viết mã `HttpLlmProviderAdapter.java` (adapter HTTP kết nối OpenAI-compatible Chat Completions cho `LlmReviewService`), bổ sung cấu hình opt-in `app.ai-review.provider.*` vào `application.properties`, `.env.example`, `compose.yaml`, viết 4 ca kiểm thử HTTP mới (`AI-T09`..`AI-T12`) trong `LlmReviewServiceTests.java`, và soạn bộ báo cáo + bảng Excel tiến độ tuần 8.

Một lỗi biên dịch phát hiện khi chạy kiểm chứng và đã sửa ngay:
1. `HttpLlmProviderAdapter` ban đầu gọi constructor 2 tham số của `GitIngestion.ExtractionException(code, message)` và thiếu khai báo `throws GitIngestion.ExtractionException` trên phương thức tĩnh `checkNotCancelled(...)`. Đã sửa lại đúng chữ ký 1 tham số `new GitIngestion.ExtractionException(code)` và khai báo `throws` đầy đủ.

Kiểm chứng AI tự chạy trên máy ngày 06/11/2026:
- `mvnw.cmd -B -ntp test "-Dtest=GitIngestion*Tests,RepositoryMetricAndPipelineTests,LlmReviewServiceTests,GitHubRepositoryUrlTests" -DfailIfNoTests=false`: **45/45 ca đạt (`BUILD SUCCESS`)** (log lưu tại `outputs/week8-progress-20261106/db-free-tests.log`).
- `python -m unittest discover -s tools/experiments -v`: **8/8 ca đạt (`OK`)**.
- Đối chiếu GitHub Actions CI run `42207922177` trên `main` (`a9fdfc7`): cả 5 job (`experiments`, `backend`, `frontend`, `secret-scan`, `package`) đều đạt. Các số liệu cần PostgreSQL local (111 ca backend, 11 ca UI, 3 repo công khai lưu DB) được ghi rõ là kết quả bàn giao của Toản và xác nhận qua CI. Sinh viên kiểm tra lại mã nguồn, số liệu và chịu trách nhiệm giải trình trước GVHD.
