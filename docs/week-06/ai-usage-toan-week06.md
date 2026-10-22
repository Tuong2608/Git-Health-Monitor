# Nhật ký AI tuần 6 — Trần Quang Toản

Ngày 22/10/2026. Công cụ: Codex. Log theo mục tiêu học thuật, không sao chép prompt gốc hoặc dựng hội thoại. Không ghi model/version, thời lượng tự học hay giờ công chưa xác minh.

## AI-06-01 — Đối chiếu phân công và thiết kế

**Mục tiêu:** xác định task 23 của Toản và task 20 chung. **Đầu vào:** workbook main 188968c, bộ tuần 5, nhánh Tưởng 0518e52. **Hỗ trợ:** đọc Git và trích Excel, đối chiếu monitoring/experiment design. **Đầu ra:** assignment-check và evidence JSON. **Kiểm chứng:** giữ source commit, chỉ đọc workbook. **Giới hạn:** nhánh Tưởng chưa merge không được coi là phê duyệt.

## AI-06-02 — Cài lưu trữ và job

**Mục tiêu:** chuyển schema tuần 5 thành migration và luồng thực thi bền vững. **Hỗ trợ:** viết V2, MonitoringStore, SnapshotPublisher, AnalysisWorker, port AnalysisPipeline. **Kiểm chứng:** PostgreSQL thật, concurrency/lease/rollback/reuse/scheduling/backlog tests; thử nâng cấp row V1. **Chỉnh sửa sau kiểm tra:** thêm kiểm tra tổng churn/lineage origin và bảo vệ timer không interrupt nhầm job kế tiếp. **Giới hạn:** chưa có ingestion thực và chưa chứng minh mọi thư viện/process đều tuân thủ interrupt.

## AI-06-03 — Cảnh báo và API

**Mục tiêu:** rule version, dedupe/retry và giao diện API vận hành. **Hỗ trợ:** AlertService/Controller, validation, pagination và cờ mặc định tắt. **Kiểm chứng:** test rule pin, retry giữ CLOSED, metadata PENDING/FAILED, null/coverage, code 403/503. **Chỉnh sửa:** fixture test cờ tắt ban đầu thiếu trường boolean hợp lệ nên nhận 400 trước guard; sửa test gửi request hợp lệ và chạy lại toàn bộ đạt. **Giới hạn:** chưa có auth/quota, UI tích hợp, email/push hay rule engine tổng quát.

## AI-06-04 — Thiết kế đánh giá và công cụ

**Mục tiêu:** làm rõ khác biệt split/tie giữa hai bản và bổ sung MAP. **Hỗ trợ:** protocol đề xuất, manifest/label templates và Python evaluator. **Kiểm chứng:** 8 unit tests AP/ties/empty/K/missing/provenance/macro; fixture output ghi kind=fixture. **Giới hạn:** chưa chọn repo/nhãn thật, parser không tự chứng minh data leakage; quyết định nhóm còn cần Tưởng và Toản chốt.

## AI-06-05 — Báo cáo và bàn giao

**Mục tiêu:** các phát biểu hoàn thành truy vết được về mã và kiểm tra. **Hỗ trợ:** log đã rút gọn, JSON tên test, hướng dẫn tích hợp, báo cáo, nội dung PR và ZIP. **Kiểm chứng:** lấy số test từ Surefire XML, kiểm tra liên kết/file và CI GitHub khi push. **Trách nhiệm:** Toản tiếp nhận/giải thích, Tưởng review; không tự ghi đã nộp, đã duyệt hay tự tay làm mọi thao tác.
