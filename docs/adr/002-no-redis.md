# ADR 002 — TaskExecutor và PostgreSQL, chưa dùng Redis

Ngày 08/10/2026. Đề xuất chi tiết hóa định hướng đề cương, chờ Tưởng review kiến trúc. Toản soạn yêu cầu API/lưu trữ; chưa có worker implementation.

## Context

API phải trả jobId nhanh, worker chạy lâu và có thể restart. Chỉ @Async không tạo queue bền vững. Nhóm cần giảm dịch vụ vận hành mà vẫn giữ trạng thái công việc.

## Decision

PostgreSQL là nguồn trạng thái; TaskExecutor chỉ thực thi trong process. Commit QUEUED trước trả 202; dispatcher polling job đã commit để tránh mất việc khi chết trước enqueue. Pool/queue hữu hạn.

Đề xuất claim bằng transaction ngắn SELECT FOR UPDATE SKIP LOCKED → RUNNING với workerId/claimToken/leaseUntil → commit → tính toán ngoài transaction. Unique partial index cho QUEUED/RUNNING chống trùng repo.

Heartbeat chỉ cập nhật khi claimToken hợp lệ. Watchdog chuyển job hết lease sang FAILED; worker cũ phải kiểm tra token/status/lease trong transaction finalize trước công bố snapshot. Snapshot unique theo repo/HEAD/effectiveConfigHash/window bounds; hash gồm tool versions và mọi tham số ảnh hưởng metric, không chỉ nhãn configVersion. Job thành công và snapshot công bố nhất quán; alert retry idempotent. Chi tiết schema/fencing phải có test restart/race ở giai đoạn cài đặt.

## Alternatives

Redis/broker giúp mở rộng nhưng thêm vận hành và nhất quán queue/DB; queue in-memory mất khi restart; HTTP đồng bộ không đáp ứng NFR01.

## Why chosen

Một backend và PostgreSQL phù hợp phạm vi prototype, không tuyên bố queue DB luôn tốt hơn broker. [Spring scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html) cung cấp executor/scheduler; durability/recovery do dự án thiết kế. [PostgreSQL SELECT](https://www.postgresql.org/docs/18/sql-select.html) hỗ trợ SKIP LOCKED, cần kết hợp transaction/claim; SELECT riêng không đủ chống chạy trùng.

## Consequences

Polling/heartbeat tăng tải DB, cần cleanup và giới hạn queue. Không giữ row lock suốt clone/Lizard. Có thể tính toán lại sau crash; không hứa exactly-once execution, chỉ ngăn công bố lặp hoặc ghi từ worker mất quyền. Mở rộng nhiều instance phải kiểm chứng fencing/recovery. Xem [BR15/NFR04](../business-rules-nfr.md).

## Bổ sung bản review chung ngày 09/10/2026

Bắt đầu một instance backend. Executor từ chối sau claim thì trả QUEUED có điều kiện claimToken; crash trước đó do watchdog xử lý khi hết lease. QUEUED tìm lại sau restart. Giới hạn cả backlog DB lẫn executor; pool 2/queue 20 theo BR15 là đề xuất, chưa nghiệm thu.

Finalize kiểm tra status/claimToken/leaseUntil còn hạn theo thời gian DB trong transaction khóa cùng job với publication, không kiểm tra sớm rồi ghi muộn. Heartbeat sau hết lease không hồi sinh claim. Worker mất quyền không ghi staging dùng chung hoặc cache/checkout của lần chạy mới; cần workspace theo claim, khóa cache và cơ chế dừng process.

NO_CHANGE chỉ khi snapshot cũ thành công cùng repo/SHA/window/effectiveConfigHash; thành công mới là CREATED. Window trượt/tool/config đổi thì không NO_CHANGE. Alert retry riêng theo key BR18, không đảo snapshot thành công.

Kill worker, executor rejection, hết lease trước watchdog, worker cũ ghi muộn và DB lỗi publication là ca kiểm thử tương lai. Xem [architecture](../architecture.md); ADR vẫn đề xuất.
