# Bàn giao tuần 7 cho Tưởng

## Phần mới của Toản

V3 hỗ trợ unknown metrics; API file/contribution/evidence/coupling/trend/comparison; job history/capabilities/retry. Cờ ghi/worker giữ mặc định false. Không thay giao diện hoặc thêm provider LLM giả. Xem [API](api-week7.md) và [hợp đồng adapter](integration-contract.md).

Nhánh Toản có58 tests đạt; khi áp phần backend này lên nhánh bạn15f291b trong checkout thử riêng có88 tests đạt trên PostgreSQL 18.3. Kết quả giúp xác nhận code hai bên cùng chạy test được; không nhận adapter/metrics đã được nối thật. Nhánh bạn không bị sửa hoặc merge vào main trong phiên này.

## Nối adapter và kiểm tra tiếp

1. Dùng `selectedCommits()` cho SnapshotData.commitCount, không dùng reachableCommits. Reachable có thể lưu summary riêng.
2. Map lineage key ổn định; ambiguous → null/reason, không0. Nullable Long cần literal hậu tố L nếu khởi tạo trực tiếp trong Java. Không gửi contribution/HHI đo được khi churn còn unknown.
3. File/pair evidence dùng `commits` array; API phân trang. Giữ raw email ở identity nội bộ, displayName thích hợp; không đẩy raw source/secrets vào evidence.
4. Resource limit ở ingestion vẫn fail job. Truncated chỉ là metadata được API xử lý bảo thủ, không yêu cầu bạn đổi sang phân tích thiếu.
5. Giữ checkpoint/interrupt/workspace theo claim và trả kết quả qua publisher. Không mutate window/config trên Claim. Retry giữ window/config nhưng không hứa cùng HEAD nếu lỗi trước lúc resolve.
6. Sau khi có metric/CCN/adapter, chạy repository public thật, kiểm tra 0/unknown/rename/empty, so sánh snapshot và cảnh báo. Không dùng fixture HTTP làm minh chứng dữ liệu thật.

## Cách xem và chạy

- Đọc `README.md`, báo cáo và API guide trong thư mục tuần 7.
- Java 21 + PostgreSQL 18, DB_* trỏ database test. Chạy `backend/mvnw -B -ntp verify` (Windows mvnw.cmd). Tests có TRUNCATE, không dùng DB thật.
- Muốn ghi HTTP evidence, thêm `-Dweek7.evidenceDir=<thư mục mới>`; script append JSONL nên dùng thư mục trống cho mỗi lần. Không bật ghi evidence test với dữ liệu production.
- Chạy migration qua Flyway; V1/V2 giữ nguyên. Backup DB thật trước deploy và kiểm tra dữ liệu cũ thỏa ràng buộc mới. Test migration hiện chỉ minh chứng fixture có kiểm soát, không chứng minh mọi DB đã từng được sửa bằng tay đều sạch.
- Với API thật dùng request mẫu và ID thực. `canAnalyze=false` là trạng thái đúng khi chưa có adapter; không sửa thành true chỉ để demo.

## Nhờ review

Review V3/nullable contract, schema evidence, semantics comparison và retry. Task 24 tiếp tục tuần 8 với tích hợp frontend/adapter, không tự nhận đã hết việc cần phối hợp. Chốt protocol thực nghiệm còn mở cùng Toản. Sau review nhóm quyết định merge/deploy; chưa có bằng chứng approval trong bộ này.
