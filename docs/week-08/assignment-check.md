# Đối chiếu kế hoạch tuần 8

Nguồn: `docs/BaoCaoTienDo_TLCN.xlsx`, sheet **Kế hoạch chi tiết**, phiên bản `main` `1878d2b` fetch ngày 05/11/2026. [Trích các dòng nguồn](evidence/assignment-source.json). Giữ workbook nguyên trạng, không tự đánh dấu nhóm đã nghiệm thu.

| Dòng / task | Phân công | Sản phẩm tuần 8 của Toản |
|---|---|---|
| 6 / 2 | Cả hai, AI log tuần 1–12 | Log tách mục tiêu, đầu vào, hỗ trợ, sửa lỗi và kiểm chứng |
| 28 / 24 | Toản, tầng API tuần 7–8 | Hoàn thiện kết nối UI; API AI context preview/review và audit |
| 30 / 26 | Toản, React tuần 8 | Snapshot, hotspot, file, evidence/coupling, trend/comparison, job, monitoring/alerts, AI review |
| 31 / 27 | Cả hai, Docker/staging tuần 8 | Frontend Docker/nginx, backend có Python/Lizard, Compose và CI smoke; staging còn mở |
| 32 / 28 | Cả hai, tích hợp tuần 8 | HTTP→worker→JGit/Lizard→PostgreSQL, React live, AI success/error/timeout bằng test adapter |
| 25–26 / 21–22 | Tưởng, Git/metric | Tái sử dụng mã đã merge; chạy kiểm chứng persistence thật để nhóm review hoàn tất |
| 29 / 25 | Tưởng, LLM | Toản nối HTTP/UI vào engine có sẵn; provider thật còn thuộc phần Tưởng |
| 24 / 20 | Cả hai, thực nghiệm | Split/tie/dataset/nhãn chưa có quyết định mới, không tự chốt |

Tiến độ tuần 7 ghi rõ chưa có dashboard metric thật và chờ persistence Git→PostgreSQL. Tuần 8 đã xử lý hai phần này. Việc ba repo chạy thành công không phải quyết định chọn dataset nghiên cứu hoặc bằng chứng các giả thuyết nghiên cứu đúng.
