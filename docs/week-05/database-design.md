# Thiết kế cơ sở dữ liệu tuần 5

Ngày 15/10/2026. Toản phụ trách. Bản thiết kế để review, không phải migration đã triển khai. Base main 8d55040; tham khảo nhánh review 1111a2e (chưa merge). Giữ 10 bảng theo workbook GitHub task 17.

## Quy ước và nhất quán

PostgreSQL; bigint identity; timestamp UTC; JSONB phải có schema/version kiểm tra ở service. FK mặc định RESTRICT, không cascade xóa lịch sử. Thiết kế ưu tiên giữ snapshot bất biến, file path theo snapshot và identity qua rename. Khóa tenant tại prototype là repository_id, chưa thay thế authorization.

Snapshot identity = repo + HEAD SHA + window_start + window_end + effective config hash (gồm tool versions/filters/commit selection). Window trượt dù HEAD không đổi vẫn phân tích lại. configVersion chỉ là nhãn. NO_CHANGE là outcome của job thành công, không thêm trạng thái. Thiết kế này tiếp nhận điểm làm rõ trên nhánh review, không tuyên bố nhánh đã được duyệt.

Tất cả bảng metric/contribution/coupling và snapshot phải được công bố trong cùng transaction sau finalize kiểm tra claim_token, status RUNNING và lease còn hiệu lực. Tính toán dài thực hiện ngoài transaction; không giữ khóa trong clone. Sau commit SUCCEEDED và snapshot cùng tồn tại; lỗi rollback không có snapshot nửa chừng. Job NO_CHANGE tham chiếu snapshot thành công đã có, không nhận ownership của snapshot đó.

## Từ điển 10 bảng

### tracked_repository

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Mã repository, giữ id V1 |
| `url` | varchar(512) UNIQUE NOT NULL | URL chuẩn hóa, giữ V1 |
| `owner` | varchar(100) NOT NULL | GitHub owner, giữ V1 |
| `name` | varchar(100) NOT NULL | Tên kho, giữ V1 |
| `created_at` | timestamptz NOT NULL | Giữ V1 |
| `verified_at` | timestamptz NULL | Lần xác minh public, null chưa xác minh |
| `default_branch` | text NULL | Branch lấy từ GitHub |
| `monitoring_enabled` | boolean NOT NULL default false | Bật lịch |
| `interval_minutes` | integer NOT NULL default 1440 | Đề xuất >=60 |
| `next_scan_at` | timestamptz NULL | Lịch kế tiếp UTC |
| `monitoring_version` | integer NOT NULL default 1 | Optimistic locking lịch |

### analysis_job

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Mỗi lần thử có id mới |
| `repository_id` | bigint FK NOT NULL | Repository |
| `status` | text NOT NULL | QUEUED RUNNING SUCCEEDED FAILED |
| `outcome` | text NULL | CREATED hoặc NO_CHANGE khi SUCCEEDED |
| `requested_at` | timestamptz NOT NULL | Thời điểm xếp hàng |
| `started_at` | timestamptz NULL | Lúc worker nhận |
| `finished_at` | timestamptz NULL | Lúc terminal |
| `head_sha` | varchar(64) NULL | Cố định trước tính metric |
| `window_start` | timestamptz NOT NULL | Đầu window đã cố định |
| `window_end` | timestamptz NOT NULL | Cuối window đã cố định |
| `config_hash` | varchar(64) NOT NULL | SHA256 effective config gồm tool versions |
| `config` | jsonb NOT NULL | Cấu hình đã resolve để tái lập |
| `phase` | text NULL | Giai đoạn thực tế |
| `progress` | numeric NULL | 0..100, null khi chưa biết |
| `claim_token` | uuid NULL | Fencing token do worker claim |
| `lease_until` | timestamptz NULL | Hạn quyền chạy |
| `heartbeat_at` | timestamptz NULL | Gia hạn lease |
| `result_snapshot_id` | bigint NULL FK | Có thể trỏ snapshot cũ NO_CHANGE |
| `error_code` | text NULL | Mã lỗi an toàn, không raw exception |

### analysis_snapshot

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Snapshot đã công bố |
| `repository_id` | bigint FK NOT NULL | Repository |
| `created_by_job_id` | bigint UNIQUE FK NOT NULL | Job tạo mới, không phải job NO_CHANGE |
| `head_sha` | varchar(64) NOT NULL | SHA cố định |
| `window_start` | timestamptz NOT NULL | Đầu window |
| `window_end` | timestamptz NOT NULL | Cuối window |
| `config_hash` | varchar(64) NOT NULL | Identity cùng config/window/SHA |
| `config_version` | text NOT NULL | Nhãn phiên bản |
| `config` | jsonb NOT NULL | Toàn cấu hình hiệu lực và tool versions |
| `commit_count` | integer NOT NULL | Số commit thực sự phân tích |
| `truncated` | boolean NOT NULL | Có cắt giới hạn hay không |
| `created_at` | timestamptz NOT NULL | Thời điểm publication |
| `summary` | jsonb NOT NULL | Số file/metric thiếu, không raw source |

### logical_file

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Định danh ổn định qua rename nếu đủ chứng cứ |
| `repository_id` | bigint FK NOT NULL | Repository |
| `first_seen_sha` | varchar(64) NOT NULL | SHA đầu tiên nhận diện |
| `initial_path` | text NOT NULL | Tên tại lần nhận diện, không unique theo path |
| `identity_evidence` | jsonb NOT NULL | Bằng chứng rename/identity; không giả confidence |

### file_metric

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `snapshot_id` | bigint composite PK FK | Snapshot |
| `file_id` | bigint composite PK FK | Logical file |
| `repository_id` | bigint NOT NULL | Khóa bảo đảm cùng repository |
| `path_at_snapshot` | text NOT NULL | Tên file tại snapshot; lịch sử rename không ghi đè |
| `change_frequency` | integer NOT NULL | >=0 |
| `churn` | bigint NOT NULL | Additions + deletions >=0 |
| `max_ccn` | numeric NULL | Max function CCN theo metric v1 |
| `avg_ccn` | numeric NULL | Thông tin bổ sung |
| `function_count` | integer NOT NULL | >=0 |
| `nloc` | bigint NOT NULL | >=0 |
| `hotspot_score` | numeric NULL | 0..1, không giả 0 nếu thiếu |
| `hhi` | numeric NULL | 0..1; tổng contribution=0 thì null |
| `unavailable_reason` | text NULL | Lý do thiếu score/complexity/HHI |
| `evidence` | jsonb NOT NULL | Commit SHAs, counts và provenance, không raw code |

### contributor

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Contributor scoped theo repo |
| `repository_id` | bigint FK NOT NULL | Repository |
| `identity_key` | text NOT NULL | Khóa chuẩn hóa hoặc alias mapping có version |
| `display_name` | text NOT NULL | Tên hiển thị; không đánh giá năng suất |
| `aliases` | jsonb NOT NULL | Mapping danh tính, hạn chế truy cập, không API raw email |

### file_contribution

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `snapshot_id` | bigint composite PK FK | Snapshot/file metric |
| `file_id` | bigint composite PK FK | File |
| `contributor_id` | bigint composite PK FK | Contributor |
| `repository_id` | bigint NOT NULL | Cùng repo |
| `additions` | bigint NOT NULL | >=0 |
| `deletions` | bigint NOT NULL | >=0 |
| `commit_count` | integer NOT NULL | >=0; share tính từ additions+deletions |

### temporal_coupling

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `snapshot_id` | bigint composite PK FK | Snapshot |
| `source_file_id` | bigint composite PK FK | A, luôn A < B |
| `target_file_id` | bigint composite PK FK | B |
| `shared_commits` | integer NOT NULL | >=0, không vượt min(countA,countB) |
| `commit_count_a` | integer NOT NULL | Mẫu số tập eligible >=1 |
| `commit_count_b` | integer NOT NULL | Mẫu số tập eligible >=1 |
| `coupling_degree` | numeric GENERATED | shared / least(countA,countB) |
| `evidence` | jsonb NOT NULL | Commit chung, filter/count, phân trang tại API |

### alert_rule

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Rule logical id |
| `repository_id` | bigint FK NOT NULL | Repository |
| `type` | text NOT NULL | HOTSPOT_INCREASE hoặc STRONG_COUPLING đề xuất |
| `version` | integer NOT NULL | Tăng mỗi lần sửa |
| `enabled` | boolean NOT NULL | Không xóa rule đã có alert |
| `parameters` | jsonb NOT NULL | Threshold và comparator có schema theo type |
| `updated_at` | timestamptz NOT NULL | UTC |

### alert

| Cột | Kiểu và ràng buộc | Ý nghĩa |
|---|---|---|
| `id` | bigint identity PK | Cảnh báo |
| `repository_id` | bigint NOT NULL | Repo guard |
| `rule_id` | bigint FK NOT NULL | Rule |
| `rule_version` | integer NOT NULL | Phiên bản khi sinh |
| `rule_config` | jsonb NOT NULL | Bản chụp parameters cũ |
| `snapshot_id` | bigint FK NOT NULL | Snapshot đích |
| `baseline_snapshot_id` | bigint NULL FK | Snapshot trước nếu rule cần |
| `file_id` | bigint NOT NULL FK | File metric của snapshot đích |
| `related_file_id` | bigint NULL FK | Cặp file nếu có; file_id < related_file_id |
| `status` | text NOT NULL | OPEN ACKNOWLEDGED CLOSED |
| `observed_value` | numeric NOT NULL | Giá trị thực |
| `threshold` | numeric NOT NULL | Ngưỡng áp dụng |
| `evidence` | jsonb NOT NULL | Lý do/số liệu/commit, không tự khẳng định bug |
| `created_at` | timestamptz NOT NULL | UTC |
| `acknowledged_at` | timestamptz NULL | Lúc acknowledge |
| `closed_at` | timestamptz NULL | Lúc đóng |

## Index và quan hệ

- Unique URL tracked_repository kế thừa V1; partial index next_scan_at cho monitoring_enabled.
- Partial unique analysis_job(repository_id) WHERE status IN (QUEUED,RUNNING); queue index requested_at,id WHERE QUEUED; lease index WHERE RUNNING.
- Unique snapshot identity đầy đủ và created_by_job_id; index repository_id,created_at DESC,id DESC cho danh sách.
- Unique file_metric(snapshot_id,path_at_snapshot); PK(snapshot_id,file_id); index snapshot_id,hotspot_score DESC NULLS LAST,path_at_snapshot; trend index file_id,snapshot_id.
- Composite FK theo (id,repository_id) chống gắn file/contributor/job/snapshot từ repo khác. Contribution tham chiếu file_metric và contributor cùng repo.
- Coupling PK(snapshot_id,A,B), CHECK A<B; FK hai phía tới file_metric cùng snapshot. Degree generated giữ đúng shared/min; không dùng công thức hợp commit. Lưu các cặp đã được tính, API lọc shared>=5 và degree>=0.5 mặc định.
- Alert dedupe unique rule_id,rule_version,snapshot_id,file_id,related_file_id NULLS NOT DISTINCT; không để NULL vượt kiểm tra trùng. Rule_config/evidence chụp lại để sửa rule không làm sai lịch sử.

## Evidence và giới hạn 10 bảng

Đề xuất dùng JSONB cho evidence trong prototype: commit SHA, counts, config và provenance; không lưu raw code/diff. Khi response dài, API phân trang evidence; backend vẫn phải giới hạn payload và kiểm tra cấu trúc. Facts commit/diff chi tiết chưa có bảng chuẩn hóa riêng trong danh sách 10 bảng. Tưởng cần xác nhận đủ cho pipeline/reproducibility; nếu cần lưu facts lâu dài thì ADR mở rộng bảng trước cài đặt, không giả định 10 bảng giải quyết mọi truy vấn lớn.

Share contributor tính từ additions+deletions; tổng bằng 0 → HHI null và lý do. Tổng share/HHI, percentile ties và missing function do metric service kiểm chứng, không giả mọi bất biến là CHECK SQL. HHI không đo năng lực cá nhân. Xóa và tái tạo file cùng path không tự tái dùng logicalFileId; rename chỉ khi Git đủ bằng chứng.

Alert_rule chỉ lưu bản hiện hành, version tăng dưới optimistic locking. Alert giữ rule_config bất biến. Chỉ hai type rule đề xuất, parameters phải validate riêng; không áp hard threshold HHI. Thay trạng thái hợp lệ OPEN→ACKNOWLEDGED→CLOSED hoặc OPEN→CLOSED; lặp cùng hành động idempotent, không tự mở CLOSED. SQL CHECK tập giá trị không thực thi state transition, cần service/transaction test.

## Nâng cấp từ V1 và rollback

Không thay migration V1. Thiết kế tham chiếu schema-reference.sql gồm V1 gốc + ALTER additive + 9 bảng mới. Khi cài đặt, tách thành V2 đã review, kiểm tra database có dữ liệu cũ và backfill; verified_at null, monitoring false để không tự phát sinh công việc. Thêm auth/quota trước mở thao tác ghi công khai. Bảng không có bảng tài khoản; prototype chỉ phiên vận hành kiểm soát.

Không tự chạy DDL vào staging. Backup trước deploy; rollback app phải xét khả năng đọc schema mở rộng, không DROP bảng có snapshot bằng tay. Thiết kế PostgreSQL >=15 vì UNIQUE NULLS NOT DISTINCT; môi trường đang dùng PostgreSQL 18. Khi cần version thấp hơn, đổi index theo ADR có test tương đương.

## API và giao diện

Repository/job → UC01–03; snapshot/file_metric → dashboard/hotspot/trend/compare; contribution → file detail; coupling → quan hệ file; monitoring trên repository + alert_rule/alert → cấu hình/cảnh báo. Không thêm bảng AI review: output v1 không persist; metadata log theo AI scope, không raw source. AI timeout client 40s/API35s/provider30s tham khảo nhánh review, chưa thay runtime 15s hiện tại.

Xem [ERD](erd.png), [SQL tham chiếu](schema-reference.sql), [wireframe](wireframes/index.html) và [kiểm chứng](evidence/verification.md).
