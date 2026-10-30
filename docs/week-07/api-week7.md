# API tuần 7 — đã có mã và kiểm thử

Tiền tố `/api`; JSON camelCase, timestamp UTC. Collections `{items,page,size,hasNext}`, page≥0, size 1..100. ID là ID trong database, không nhận path file để truy cập filesystem. Route lồng snapshot/file hoặc repo/file luôn kiểm tra quan hệ trước truy vấn.

| Method / route | Ý nghĩa / response |
|---|---|
| GET /analysis-capabilities | writesEnabled, workerEnabled, pipelineAvailable, canAnalyze, maxObservationDays=365 |
| GET /repositories/{repo}/jobs | Lịch sử job, mới nhất trước; không trả claim token hoặc cấu hình nội bộ |
| POST /jobs/{id}/retry | Chỉ FAILED; 202 jobId/status/retryOf và Location; giữ window/config, job mới |
| GET /snapshots/{snapshot}/files/{file} | Metric và unavailableReason; evidenceCount/evidenceAvailable; không đính kèm toàn bộ commit |
| GET /snapshots/{snapshot}/files/{file}/contributions | contributorId/displayName/additions/deletions/commitCount/share, phân trang; không trả identityKey/aliases |
| GET /snapshots/{snapshot}/files/{file}/evidence | `{evidenceIndex,evidence}` theo thứ tự trong snapshot, phân trang |
| GET /snapshots/{snapshot}/files/{file}/couplings | relatedFileId/path, sharedCommits, fileCommitCount, relatedCommitCount, couplingDegree; shared≥5, degree≥0.5 |
| GET /snapshots/{snapshot}/files/{file}/couplings/{related}/evidence | Evidence của đúng cặp canonical, đọc được từ cả hai chiều |
| GET /repositories/{repo}/files/{file}/trend | snapshotId/SHA/window/config/truncated/coverageComplete + metric; tăng dần theo windowEnd/createdAt/id |
| GET /repositories/{repo}/snapshot-comparison | fromSnapshotId/toSnapshotId bắt buộc; before/after/delta từng metric, state từng file, phân trang |

Các API tuần 6 (enqueue, schedule, snapshot list/hotspots, alert rule/alerts) giữ nguyên. [Contract tuần 6](../week-06/api-week6.md). Các route AI review vẫn chưa cài, không trả output giả.

## Null, evidence và contribution

Ví dụ file có lineage không rõ: `changeFrequency:null, churn:null, hotspotScore:null, unavailableReason:"LINEAGE_AMBIGUOUS"`. Không dùng 0 thay cho thiếu dữ liệu. Số 0 vẫn được giữ nếu đã đo ra 0. Share contributor là (additions+deletions)/churn; khi không có mẫu số dương thì null. API không suy diễn HHI thành mức hiểu code.

Hợp đồng evidence mới dùng `evidence.commits` là array. Array rỗng nghĩa đã cung cấp nhưng không có commit; thiếu trường này → evidenceAvailable=false. Metadata cũ dùng commitShas hoặc khóa khác không bị tự diễn giải thành evidence chuẩn. File detail không gửi cả JSONB; đọc riêng endpoint phân trang. Adapter chịu trách nhiệm xác minh SHA, chỉ cung cấp provenance cần thiết, không raw source/secret. Không tạo link GitHub từ nội dung chưa kiểm tra.

Coupling A/B canonical trong DB nhưng response luôn định hướng theo file được hỏi: mẫu số của file hiện tại là fileCommitCount. Công thức không đổi: shared/min(counts). Danh sách chỉ lấy cặp đạt ngưỡng mặc định; endpoint evidence vẫn yêu cầu cặp thực sự tồn tại.

## So sánh snapshot

Yêu cầu cùng repo, config hash/version và độ dài cửa sổ; coverageComplete=true, truncated=false ở cả hai. Snapshot from phải đứng trước to theo windowEnd, rồi createdAt/id khi bằng nhau. Sai repo trả 404; sai thứ tự hoặc trùng snapshot trả 400; không tương thích trả 409 cùng code CONFIG hoặc COVERAGE.

Join bằng logical file ID giữ đúng rename. States ADDED, REMOVED, CHANGED, UNCHANGED, UNAVAILABLE; nếu một metric không đo được thì state=UNAVAILABLE, các metric đủ vẫn có delta. REMOVED chỉ nghĩa rời universe, không tự kết luận rủi ro giảm.

Mỗi metric có `{before,after,absoluteDelta,relativeDelta,reason}`. Absolute=new−old; relative=(new−old)/abs(old). Thiếu một phía → delta=null/reason=UNAVAILABLE. Old=0 → relative=null/reason=BASE_ZERO, absolute vẫn hợp lệ. So sánh giữ raw values, không làm tròn trước khi xét ngưỡng. Trend trả provenance từng snapshot để giao diện không nối các cấu hình khác nhau thành một xu hướng đồng nhất.

## Retry và readiness

POST retry dùng cùng cờ ghi và availability gate như POST analyses. 403 nếu ghi bị tắt; 503 nếu worker/adapter chưa sẵn sàng; 404 job không có; 409 nếu job chưa FAILED, repo có active job hoặc config adapter đã đổi; 429 nếu backlog đầy. Retry tạo id khác và retryOf, không sửa trạng thái job cũ. Window/config được giữ; **HEAD chưa được chốt ở job thất bại thì retry vẫn có thể resolve HEAD mới**, nên không hứa replay byte-for-byte lịch sử Git. Muốn phân tích theo cấu hình mới dùng POST analyses.

`canAnalyze` chỉ phản ánh cờ/adapter được đăng ký, không cam kết mạng, tài nguyên hay quyền repo sẽ thành công. Lịch sử job không lộ claim token; quyền người dùng thực tế vẫn chưa có auth/quota trong prototype, nên chỉ bật ghi trong phiên nội bộ được kiểm soát.

## Minh chứng

[HTTP exchanges](evidence/http-exchanges.jsonl) là request/response thật từ test server và PostgreSQL, **adapter metric là fixture**, không phải chạy repository public. [requests.http](requests.http) dùng ID cần thay bằng dữ liệu môi trường của bạn.
