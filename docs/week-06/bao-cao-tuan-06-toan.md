# Báo cáo tuần 6 — Trần Quang Toản

MSSV 23110158. Đề tài Git Health Monitor. Tuần 18–24/10/2026, ngày thực hiện 22/10/2026.

## Mục tiêu

Theo workbook task 23 (Toản, tuần 6–7), cài lưu trữ snapshot và giám sát; task 20 chung (tuần 5–6), hoàn thiện thiết kế đánh giá. Đã cập nhật main tới 188968c và đối chiếu thiết kế tuần 5 của Tưởng trên nhánh 0518e52 chưa merge.

## Kết quả

| Nhóm việc | Sản phẩm đã làm | Minh chứng |
|---|---|---|
| Backend/database | Migration V2, 10 bảng, snapshot + file/contributor/coupling, nâng cấp giữ V1 | SQL migration, log upgrade |
| Backend/job | Durable queue, scheduler, 2 workers/queue20, claim token, heartbeat, lease, watchdog và timeout | MonitoringStore, AnalysisWorker, PostgreSQL integration tests |
| Backend/alert | Hai rule có version, pinned rules, PENDING/FAILED/COMPLETE, retry và dedupe, xử lý CLOSED | AlertService và tests |
| API | Job/schedule/snapshot/hotspot/rules/alerts, pagination, validation, cờ ghi mặc định tắt | MonitoringController, API tests |
| Frontend | Giữ giao diện; cập nhật Vite 8.3.0→8.3.5 để xử lý audit bảo mật chặn CI | package/lockfile, npm-audit.json; không nhận wireframe thành chức năng tích hợp mới |
| Nghiệp vụ | Làm rõ NO_CHANGE, thiếu metric/coverage, baseline tương thích và cảnh báo có evidence | Implementation/API docs |
| Nghiên cứu chung | Đối chiếu split/tie, protocol đề xuất, manifest/label templates, evaluator baseline và P/R/MAP | experiment-protocol.md, tools/experiments |
| Báo cáo/AI/Git | Tài liệu bàn giao, AI log từng hạng mục, nhánh tuần 6, PR và gói ZIP | README, ban-giao-tuong.md, AI log, PR |

## Kết quả kiểm chứng

41 backend tests đạt, không failure/error/skip; trong đó 19 test mới tuần 6. Các ca quan trọng: enqueue race, claim đồng thời, backlog20, executor quá tải, lease hết/token sai, rollback snapshot, cùng identity NO_CHANGE, lịch bù/coalesce, rule version pin, alert retry không mở lại CLOSED, baseline/null/coverage. 8 test evaluator đạt. Kiểm tra V1→V2 với row cũ thành công và rollback schema thử.

Minh chứng chi tiết: [verification](evidence/verification.md). Test adapter có dữ liệu fixture gắn rõ trong mã test; không dùng làm số liệu nghiên cứu hoặc kết quả phân tích repository thật. Test tái tạo worker chứng minh đọc job từ DB, không được gọi là thử kill máy chủ/staging đầy đủ.

## Kiến thức áp dụng

Transaction giữ snapshot/detail/job nhất quán; khóa DB và partial unique index xử lý race; lease và fencing ngăn worker cũ publish; bounded executor giới hạn tài nguyên trong tiến trình. Durable pending marker giúp retry cảnh báo sau publication mà không rollback metric. Thực nghiệm cần cùng universe, tách thời gian và xử lý mẫu số bằng 0 thay vì báo điểm sai.

## Giới hạn và phối hợp

Main chưa có AnalysisPipeline của Tưởng, do đó runtime không clone/tính metric thật và trả 503 khi thiếu adapter. Task 23 còn phần tích hợp/đo tải tuần 7. Task 20 có protocol/công cụ nhưng nhóm chưa xác nhận split/dataset/nhãn; không tự tạo nhãn người dùng hoặc số liệu benchmark/SUS. Chưa deploy staging hoặc nghiệm thu. Cần Tưởng review điểm nối module, config và quyết định thực nghiệm.

## Sử dụng AI

AI hỗ trợ đọc phân công, thiết kế/cài mã, tạo và chạy tests, tổng hợp bằng chứng/tài liệu. [AI log](ai-usage-toan-week06.md) phân biệt từng mục tiêu và kiểm chứng; sinh viên cần đọc và giải thích mã trước khi tiếp nhận/nộp. Không khai toàn bộ thao tác là tự thực hiện thủ công, không tạo giờ công hay approval giả.
