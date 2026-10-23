# Bàn giao tuần 6 cho Tưởng

## Phần Toản đã cài

Migration V2 (10 bảng), job queue PostgreSQL, lịch Spring Scheduling, bounded executor, claim/heartbeat/lease/recovery, publish snapshot nguyên tử, hai rule cảnh báo, evaluation retry và API vận hành. 41 backend tests đạt, 8 evaluation-tool tests đạt. Chi tiết tại [minh chứng](evidence/verification.md).

## Điểm nối module Git/metric

Tưởng tạo Spring bean triển khai `AnalysisPipeline` trong package monitoring hoặc module riêng. Không merge bản fake test vào main runtime.

1. `effectiveConfig()` trả JSON ổn định gồm metric spec, Git/Lizard version, exclusions, commit selection, percentile, lineage/author mapping. Hash thay đổi khi bất kỳ chính sách có ảnh hưởng metric thay đổi. Không chứa token/secret. Job giữ config/window lúc enqueue.
2. `analyze(Claim, Lease)` xác minh repository thực sự public, clone/fetch an toàn, chọn branch/SHA và tính metric đúng window đã nhận. Gọi `lease.checkpoint()` tại các bước IO/loop; dừng process và giải phóng workspace khi interrupt hoặc mất lease.
3. Workspace riêng theo repository/jobId/claimToken. Khóa cache Git nếu dùng chung. Không chạy build/script từ repo đầu vào. Port không tự thực hiện các bảo vệ Git này thay adapter.
4. Trả `SnapshotData` với full universe, metadata và `summary.coverageComplete`. Chỉ đặt true khi extraction đủ; false/truncated khiến delta và coupling alert skip. FileData có lineageKey ổn định qua rename, firstSeenSha/initialPath không đổi; path là path ở snapshot. Contribution churn phải cộng đúng file churn. Pair dùng lineage keys của hai file trong payload, countA/B cùng tập lọc.
5. Backend tự ghi snapshot và chuyển job; adapter không ghi trạng thái job/metric trực tiếp. Với cùng SHA/window/config, publisher trả snapshot cũ/NO_CHANGE.

Đầu ra không giữ raw source/diff; evidence chỉ các SHA/count/provenance cần thiết. Cần định nghĩa schema/giới hạn evidence phù hợp tập dữ liệu lớn khi tích hợp.

## Chạy và kiểm tra

- Dùng Java 21, PostgreSQL 18, cấu hình DB_URL/DB_USERNAME/DB_PASSWORD. Maven verify yêu cầu database thử nghiệm riêng vì tests xóa dữ liệu fixture.
- Migration V2 tự chạy qua Flyway. Đã kiểm tra V1 có dữ liệu → V2 và giữ repository cũ; cần backup DB thật trước deploy.
- Sau khi có adapter, bật APP_ANALYSIS_ENABLED=true và APP_ANALYSIS_WRITES_ENABLED=true trong phiên nội bộ. Hai cờ mặc định false, Compose hỗ trợ truyền từ .env. Chưa mở endpoint ghi công khai khi chưa có auth/quota.
- Gọi POST /api/repositories/{id}/analyses với observationDays, theo Location đọc job. Khi thành công đọc snapshots/hotspots/alerts và evaluationStatus. Cấu hình lịch dùng version từ GET; rule update cũng cần version.
- Trong tuần 7: chạy repo Java public thật; kiểm chứng rename/empty/no-function/truncation; ngắt worker ở nhiều giai đoạn; chạy UI/live staging; đo tài nguyên/hiệu năng. Tests tuần 6 dùng adapter fixture nội bộ, chưa thay việc này.

## Các điểm nhờ review

1. Chốt AnalysisPipeline DTO, lineageKey và bộ config hash với algorithm design của Tưởng.
2. Review V2, các giá trị mặc định worker/lease/backlog/deadline và JSONB evidence.
3. Chốt task 20: 80/20 hay 30 ngày prediction, tie ID/path, dataset/nhãn và MAP; [bản đối chiếu](experiment-protocol.md).
4. Review PR, sau đó nhóm mới quyết định merge/deploy. Chưa ghi nhận approval của Tưởng trong bộ này.
