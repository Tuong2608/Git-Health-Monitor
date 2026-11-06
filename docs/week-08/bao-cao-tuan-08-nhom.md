# Báo cáo tiến độ nhóm — Tuần 8 (01/11–07/11/2026)

Đề tài: **Git Health Monitor**. Thành viên: Trần Văn Tưởng (`23110170`, nhóm trưởng) & Trần Quang Toản (`23110158`).
Nền đối chiếu: `main` `a9fdfc7` (PR #9 của Toản) gộp cùng nhánh `codex/tuong-week8` (PR #10 của Tưởng).
Tổng hợp trạng thái 39 đầu việc trong workbook `BaoCaoTienDo_TLCN.xlsx`: **19 việc Hoàn thành (+6 so với tuần 7), 9 việc Đang làm, 11 việc Chưa bắt đầu**.

---

## 1. Điểm nổi bật làm mới trong Tuần 8 (Highlight tuần 8)

1. **[MỚI TUẦN 8 — Hoàn thành Giao diện Dashboard dữ liệu thật (STT 26 — Toản)]**:
   - Xây dựng `AnalysisDashboard.tsx` thay thế hoàn toàn biểu đồ minh họa tuần 3 bằng dữ liệu thật từ PostgreSQL: chọn snapshot, bản đồ điểm nóng (hotspot), bảng chỉ số phân trang (`size = 20`, phân biệt rõ `null` và `0`), chi tiết file, đóng góp tác giả, bằng chứng commit và temporal coupling, xu hướng qua các snapshot, so sánh 2 snapshot, điều khiển job/retry, lịch quét, cảnh báo và hộp thoại AI review.
   - Xử lý lỗi tràn ngang trên màn hình mobile khi hiển thị chuỗi SHA và đường dẫn dài; đạt **11/11 ca kiểm thử giao diện Playwright** (gồm 2 ca live chạy trên snapshot `spring-petclinic` thật).
2. **[MỚI TUẦN 8 — Hoàn thành API AI Review, Audit V4 & Adapter LLM Provider thật (STT 24 & STT 25 — Toản & Tưởng)]**:
   - **Toản (STT 24)**: Thêm migration `V4__ai_review_audit.sql` (chỉ lưu metadata truy vết) và cài `ReviewContextBuilder`, `AiReviewController` (`POST /ai-review-preview`, `POST /ai-reviews`) đọc mã nguồn trực tiếp từ Git blob tại đúng `headSha` của snapshot, kiểm tra repository công khai và ràng buộc `contextHash` với xác nhận consent.
   - **Tưởng (STT 25)**: Cài đặt [`HttpLlmProviderAdapter.java`](../../backend/src/main/java/com/example/demo/aireview/HttpLlmProviderAdapter.java) kết nối chuẩn OpenAI-compatible Chat Completions qua HTTPS (mặc định cấu hình cho Mistral `codestral-latest`), thực thi thời gian chờ theo deadline còn lại, kiểm tra hủy tác vụ, giới hạn byte ngay trong lúc đọc `InputStream` (khối 4 KiB) và thêm 4 ca kiểm thử HTTP nội bộ (`AI-T09`..`AI-T12`), nâng `LlmReviewServiceTests` lên **12/12 ca đạt**.
3. **[MỚI TUẦN 8 — Hoàn thành tích hợp Pipeline Git/Lizard lưu thẳng vào PostgreSQL (STT 21, 22, 23 — Tưởng & Toản)]**:
   - Nối `GitAnalysisPipeline` vào hàng đợi PostgreSQL 18.3, bổ sung `pipelineVersion = "git-metrics-v2"`, `lizardVersion = "1.24.3"`, `jgitVersion`, `authorAliases` vào `effectiveConfig()`.
   - Phân tích qua HTTP và lưu PostgreSQL thành công trên 3 repository công khai (cửa sổ 365 ngày): `spring-guides/gs-rest-service` (6 file Java, 29 commit), `spring-projects/spring-petclinic` (50 file Java, 49 commit), `octocat/Hello-World` (0 file Java, xử lý rỗng hợp lệ).
4. **[MỚI TUẦN 8 — Đóng gói Docker kèm Lizard & CI Compose Smoke (STT 27 & STT 28 — Cả 2, Đang làm)]**:
   - Cập nhật `backend/Dockerfile` cài sẵn Python 3 + `lizard==1.24.3`, thêm `frontend/Dockerfile` dùng Nginx proxy `/api`, cập nhật `compose.yaml` gắn volume `pgdata` + `gitcache` và viết `Week8IntegrationTests` (8 ca).
   - Trên GitHub Actions CI (`main` `a9fdfc7`, run `42207922177`), cả 5/5 job (`experiments`, `backend`, `frontend`, `secret-scan`, `package` chạy `docker compose up` smoke test) đều đạt.
   - Giữ STT 27 và STT 28 ở trạng thái *Đang làm* vì môi trường cloud staging cũ từ tuần 3 (`410`/`404`) cần được triển khai lại với cấu hình mới.

---

## 2. Tổng hợp kiểm chứng

- **Trên máy Tưởng (không cần PostgreSQL)**: **45/45 ca kiểm thử backend đạt (`BUILD SUCCESS`)** + **8/8 ca kiểm thử công cụ thực nghiệm đạt**.
- **Trên máy Toản (PostgreSQL 18.3) & GitHub Actions CI**: **111/111 ca kiểm thử backend có PostgreSQL đạt** (tổng cộng toàn dự án **115 ca kiểm thử backend duy nhất**) + **11/11 ca kiểm thử Playwright UI đạt**.

---

## 3. Kế hoạch tuần 9

- Chốt tập dữ liệu thực nghiệm (STT 20, 29).
- Triển khai thực nghiệm **RQ1: đánh giá xếp hạng rủi ro hotspot** so với 2 baseline bằng `Precision@K`, `Recall@K`, `MAP` (STT 30 — Tưởng phụ trách).
- Bắt đầu thực nghiệm **RQ2: theo dõi xu hướng qua nhiều mốc snapshot** (STT 31 — Toản phụ trách) và cập nhật lại môi trường staging (STT 27–28).
