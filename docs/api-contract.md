# API contract sơ bộ — tuần 4

Ngày 08/10/2026. Toản phụ trách. **Contract để review, chưa triển khai các route dự kiến.** Base `/api`, JSON UTF-8, ID nguyên dương, timestamp UTC ISO-8601. Không đổi ngầm response shape của API tuần 3.

## Quy ước

Route phải kiểm tra quan hệ repo/snapshot/file. Collection dự kiến nhận `page>=0`, `size=1..100` mặc định 20 và trả `{items,page,size,hasNext}`; ngoại lệ GET repositories hiện trả mảng trực tiếp. Lỗi dự kiến theo Problem Detail `{type,title,status,detail,code,instance}`; code ổn định, detail không chứa stack trace/secret. Prototype chưa bảo đảm code/instance.

400 input sai; 403 không được phép/thao tác tắt; 404 không tồn tại; 409 xung đột; 413 context quá lớn; 429 quota/queue đầy; 502 upstream/output sai; 503 chưa sẵn sàng; 504 timeout. Endpoint ghi/gọi provider cần được bảo vệ trước public release; bản hiện tại chưa có tài khoản, CORS không thay authorization.

## Danh mục endpoint

| Method và path sau /api | Input chính | Output/ý nghĩa | UC | Trạng thái |
|---|---|---|---|---|
| POST /repositories | url | 201 repository DTO + Location | UC01 | Có, mới syntax validation |
| GET /repositories | page,size | 200 mảng DTO | UC01 | Có |
| GET /repositories/{repositoryId} | id | 200 DTO /404 | UC01 | Có |
| GET /health | — | 200 chuỗi OK, chỉ liveness | Vận hành | Có |
| POST /repositories/{repositoryId}/analyses | observationDays,configVersion | 202 jobId/status + Location | UC02 | Dự kiến |
| GET /jobs/{jobId} | id | status/phase/progress/outcome/snapshotId/errorCode | UC03 | Dự kiến |
| GET /repositories/{repositoryId}/snapshots | page,size | Snapshot thành công mới nhất trước | UC04/07 | Dự kiến |
| GET /snapshots/{snapshotId} | id | HEAD/window/config/summary/truncated | UC04 | Dự kiến |
| GET /snapshots/{snapshotId}/hotspots | page,size | Score giảm dần, path tăng dần khi tie | UC04 | Dự kiến |
| GET /snapshots/{snapshotId}/files/{fileId} | id | Metric/ownership/commit evidence | UC05 | Dự kiến |
| GET /snapshots/{snapshotId}/files/{fileId}/couplings | page,size | Cặp file/shared/mẫu số/evidence | UC06 | Dự kiến |
| GET /repositories/{repositoryId}/files/{fileId}/trend | page,size | Chuỗi snapshot tăng theo thời gian | UC07 | Dự kiến |
| GET /repositories/{repositoryId}/monitoring | id | enabled/intervalMinutes/version | UC08 | Dự kiến |
| PUT /repositories/{repositoryId}/monitoring | enabled,intervalMinutes | 200 cấu hình, interval>=60 đề xuất | UC08 | Dự kiến |
| GET /repositories/{repositoryId}/alert-rules | page,size | Rule/version/threshold/enabled | UC08 | Dự kiến |
| POST /repositories/{repositoryId}/alert-rules | type,threshold,enabled | 201 rule thuộc type hỗ trợ | UC08 | Dự kiến |
| PUT /alert-rules/{ruleId} | type,threshold,enabled | 200 version mới, giữ evidence cũ | UC08 | Dự kiến |
| GET /repositories/{repositoryId}/alerts | status,page,size | Cảnh báo, lý do và evidence | UC09 | Dự kiến |
| PATCH /alerts/{alertId} | status | ACKNOWLEDGED/CLOSED, idempotent | UC09 | Dự kiến |
| GET /repositories/{repositoryId}/snapshot-comparison | fromSnapshotId,toSnapshotId,page,size | Before/after/delta hoặc 409 | UC10 | Dự kiến |
| POST /snapshots/{snapshotId}/files/{fileId}/ai-reviews | lineRanges,consent | 200 reviewId/snapshotId/fileId/generatedAt/aiGenerated/result | UC11 | Dự kiến |

Đường dẫn lồng dưới repository thay ví dụ phẳng POST /analysis trong phân công để rõ phạm vi; không thêm use case. Evidence dài cần phân trang khi thiết kế chi tiết tuần 5, không trả toàn bộ lịch sử trong file detail.

## Analysis job

Request đề xuất: `{"observationDays":180,"configVersion":"metric-v1"}`. observationDays 1..3650 đề xuất; configVersion thuộc allowlist server, không nhận công thức thực thi tùy ý.

Response minh họa, không phải dữ liệu thật: `{"jobId":101,"status":"QUEUED"}`, Location `/api/jobs/101`.

```json
{"id":101,"repositoryId":1,"status":"RUNNING","phase":"EXTRACTING","progress":null,"snapshotId":null,"outcome":null,"errorCode":null}
```

QUEUED → RUNNING → SUCCEEDED/FAILED. Phase: FETCHING/EXTRACTING/COMPLEXITY/METRICS/PERSISTING. progress=null khi chưa có mẫu số thật. outcome cuối CREATED hoặc NO_CHANGE; NO_CHANGE trỏ snapshot cũ. FAILED không công bố snapshot mới dở dang.

Chống trùng bằng transaction/constraint PostgreSQL, không chỉ Java check. Ghi QUEUED trước trả 202; dispatcher đọc queue bền vững. Restart giữ job QUEUED; job RUNNING hết lease → FAILED; lần thử mới có id mới. [ADR 002](adr/002-no-redis.md) đề xuất lease/fencing.

## Snapshot và tương thích

Metadata: repositoryId/headSha/createdAt/observationStart/End/configVersion/configHash/toolVersions/analyzedCommitCount/truncated. Window bounds cố định cho lần chạy lại; không dùng now khác nhau rồi nhận cùng input. Hotspot trả raw frequency/churn/max_ccn cùng percentile/score; null kèm unavailableReason, không giả 0. Quy tắc percentile/empty file cần algorithm design.

So sánh khác repo →400; khác công thức/config không tương thích →409 INCOMPATIBLE_SNAPSHOTS. File thiếu một phía trả null và added/removed, không coi là delta từ 0. UI có thể xem riêng nhưng không nối biểu đồ/delta gây hiểu nhầm.

## AI review

Request mẫu `{"lineRanges":[{"start":10,"end":40}],"consent":true}`. Server đọc code tại HEAD snapshot, kiểm tra file/line bounds, không nhận filesystem path tùy ý. UI cho xem phần mã chọn và thông báo dữ liệu gửi trước consent. result theo [schema](schemas/ai-review-output.schema.json) và [AI scope](ai-review-scope.md). AI gọi ở luồng riêng, timeout 30 giây đề xuất; NFR01 2 giây chỉ áp API tạo analysis job. Lỗi provider/schema/evidence không trả review thành công rỗng.

Tuần 4 bàn giao contract, tuần 5 chốt DTO/ERD/security, giai đoạn cài đặt mới thêm handler. API/DB/UI: Toản; worker/metric: Tưởng. Nguồn định dạng lỗi: [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457.html); mã lỗi dự án là đề xuất riêng.
