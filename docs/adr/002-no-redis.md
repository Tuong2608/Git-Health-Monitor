# ADR 002 — TaskExecutor và PostgreSQL, chưa dùng Redis

Ngày 08/10/2026. Đề xuất chi tiết hóa định hướng đề cương, chờ Tưởng review kiến trúc. Toản soạn yêu cầu API/lưu trữ; chưa có worker implementation.

## Context

API phải trả jobId nhanh, worker chạy lâu và có thể restart. Chỉ @Async không tạo queue bền vững. Nhóm cần giảm dịch vụ vận hành mà vẫn giữ trạng thái công việc.

## Decision

PostgreSQL là nguồn trạng thái; TaskExecutor chỉ thực thi trong process. Commit QUEUED trước trả 202; dispatcher polling job đã commit để tránh mất việc khi chết trước enqueue. Pool/queue hữu hạn.

Đề xuất claim bằng transaction ngắn SELECT FOR UPDATE SKIP LOCKED → RUNNING với workerId/claimToken/leaseUntil → commit → tính toán ngoài transaction. Unique partial index cho QUEUED/RUNNING chống trùng repo.

Heartbeat chỉ cập nhật khi claimToken hợp lệ. Watchdog chuyển job hết lease sang FAILED; worker cũ phải kiểm tra token/status/lease trong transaction finalize trước công bố snapshot. Snapshot unique theo repo/HEAD/effectiveConfigHash/window bounds. Job thành công và snapshot công bố nhất quán; alert retry idempotent. Chi tiết schema/fencing phải có test restart/race ở giai đoạn cài đặt.

## Alternatives

Redis/broker giúp mở rộng nhưng thêm vận hành và nhất quán queue/DB; queue in-memory mất khi restart; HTTP đồng bộ không đáp ứng NFR01.

## Why chosen

Một backend và PostgreSQL phù hợp phạm vi prototype, không tuyên bố queue DB luôn tốt hơn broker. [Spring scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html) cung cấp executor/scheduler; durability/recovery do dự án thiết kế. [PostgreSQL SELECT](https://www.postgresql.org/docs/18/sql-select.html) hỗ trợ SKIP LOCKED, cần kết hợp transaction/claim; SELECT riêng không đủ chống chạy trùng.

## Consequences

Polling/heartbeat tăng tải DB, cần cleanup và giới hạn queue. Không giữ row lock suốt clone/Lizard. Có thể tính toán lại sau crash; không hứa exactly-once execution, chỉ ngăn công bố lặp hoặc ghi từ worker mất quyền. Mở rộng nhiều instance phải kiểm chứng fencing/recovery. Xem [BR15/NFR04](../business-rules-nfr.md).
