# Báo cáo tiến độ tuần 7

Sinh viên: **Trần Quang Toản — 23110158**. Đề tài: Git Health Monitor. Tuần 25–31/10/2026. Ngày thực hiện 29/10/2026.

## 1. Mục tiêu và cơ sở

Hoàn thiện task 23 lưu trữ/giám sát và triển khai task 24 REST API theo workbook. Đã cập nhật main tới 0b664d9 (PR tuần 6 đã merge), đọc nhánh Tưởng 15f291b để xử lý các lệch hợp đồng. Task 24 kéo dài tuần 7–8; chưa nhận đã hoàn tất toàn hệ thống.

## 2. Kết quả công việc

| Mảng | Nội dung mới tuần 7 | Minh chứng |
|---|---|---|
| Database | V3 nullable metric có lý do, giữ measured zero, retryOf và FK cùng repository; vẫn 10 bảng | Migration và verify-upgrade.sql/log |
| Backend API | Chi tiết file, contribution, evidence phân trang, coupling hai chiều, trend, so sánh snapshot | SnapshotQueries/Controller và tests |
| Vận hành | Capabilities/readiness, job history, retry FAILED giữ window/config; từ chối claim bị đổi context | MonitoringStore/Controller/Publisher |
| Nghiệp vụ | Chặn so sánh khác config/coverage, không biến null thành 0, giữ rename identity, BASE_ZERO/UNAVAILABLE rõ ràng | API contract, test cases |
| Tích hợp nhóm | Đối chiếu 4 điểm Tưởng nêu; test mã Toản cùng JGit branch trong checkout riêng | integration-contract, compatibility evidence |
| Frontend/AI runtime | Không cài frontend hoặc provider LLM trong tuần này | Đúng phần việc, không nhận output giả thành chức năng |
| Báo cáo/Git | Tài liệu bàn giao, AI log theo mục, minh chứng HTTP và bộ ZIP | docs/week-07 và nhánh feat/toan-week-7 |

## 3. Kết quả kiểm chứng

- Nhánh Toản: **58 tests đạt**, 0 failure/error/skip, gồm 17 test mới (14 API/storage và 3 worker HTTP).
- Checkout kết hợp nhánh Tưởng với backend tuần 7: **88 tests đạt**, 0 failure/error/skip. Có cả tests Git của Tưởng và tests API/DB. Đây là tập lớn hơn chứa nhiều test đã tính ở 58, không cộng hai số.
- Test nâng cấp V1/V2 có repository và số đo 0 sang V3 giữ dữ liệu, cho phép null có reason và rollback schema thử nghiệm.
- HTTP → queue → scheduler/worker → PostgreSQL → snapshot → alert chạy thành công với adapter test-only; evidence lưu nhãn TEST_FIXTURE_NOT_REAL_GIT.

Lần chạy đầu một fixture timestamp không ghi timezone nên lệch 7 giờ trên môi trường UTC+7; đã sửa literal thành UTC rõ ràng và chạy lại thành công. Không sửa số liệu kỳ vọng để che lỗi. Log tóm tắt, tên từng test và HTTP exchanges tại [minh chứng](evidence/verification.md).

## 4. Kiến thức áp dụng

Phân biệt dữ liệu chưa đo với giá trị 0; migration additive không sửa checksum V1/V2; khóa ngoại ghép bảo vệ phạm vi repository. Dùng snapshot identity/provenance và file ID ổn định để so sánh qua rename. Retry tạo lần chạy mới giữ lịch sử lỗi; dữ liệu context phải khớp DB để worker không publish dưới cấu hình sai. Phân trang evidence giúp API không gửi toàn bộ lịch sử trong file detail.

## 5. Giới hạn và bước tiếp theo

Tưởng chưa có metric/Lizard/AnalysisPipeline thật trong nhánh đã kiểm tra; chỉ có JGit fact. Bộ này không tạo pipeline giả trong runtime, không claim benchmark hoặc chạy repo public thành công. Cần nối adapter, xác nhận mapping null/selectedCommitCount/evidence, kiểm tra end-to-end với repo Java thật, sau đó kiểm tra staging và API cho frontend trong tuần 8. Cần nhóm chốt split/tie/dataset/nhãn nghiên cứu còn mở; không tạo đồng thuận hoặc số liệu nghiên cứu giả.

Chưa tự merge PR tuần 7, deploy hoặc ghi đã được GVHD nghiệm thu. Toản cần đọc và giải thích phần mã được AI hỗ trợ trước khi nộp. [AI usage log](ai-usage-toan-week07.md) ghi phạm vi và kiểm chứng.
