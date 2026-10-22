# API đã cài tuần 6

Tiền tố `/api`. Các route dưới đây có mã nguồn và test, nhưng chạy phân tích cần AnalysisPipeline thật. `APP_ANALYSIS_WRITES_ENABLED=false` mặc định chặn thao tác ghi bằng 403. `APP_ANALYSIS_ENABLED=false` hoặc thiếu adapter → yêu cầu phân tích/bật lịch trả 503/ANALYSIS_PIPELINE_UNAVAILABLE. Không có API nhận metric giả từ client.

| Method / route | Input | Kết quả chính |
|---|---|---|
| POST /repositories/{repo}/analyses | observationDays 1..365, mặc định 180 | 202 jobId/status + Location; 409 active; 429 full; 503 unavailable |
| GET /jobs/{id} | — | id, repositoryId, status/outcome/phase/progress, timestamps, snapshotId/errorCode |
| GET /repositories/{repo}/monitoring | — | repositoryId, monitoringEnabled, intervalMinutes, nextScanAt, monitoringVersion |
| PUT /repositories/{repo}/monitoring | enabled, intervalMinutes 60..43200, version | 200; version bắt buộc, sai phiên bản 409 |
| GET /repositories/{repo}/snapshots | page,size | Collection snapshot metadata và evaluationStatus |
| GET /snapshots/{id} | — | SHA/window/configHash, commitCount, truncated, summary, evaluationStatus/notes |
| GET /snapshots/{id}/hotspots | page,size | fileId/pathAtSnapshot, frequency/churn/CCN/HHI/score và unavailableReason |
| GET /repositories/{repo}/alert-rules | page,size | Collection rules |
| POST /repositories/{repo}/alert-rules | type,threshold,minShared nếu coupling,enabled | 201 + Location, version=1 |
| PUT /alert-rules/{id} | Các trường trên + version | 200 version mới hoặc 409 |
| GET /repositories/{repo}/alerts | status=ALL/OPEN/ACKNOWLEDGED/CLOSED,page,size | Collection alert với ruleConfig/snapshot/evidence |
| PATCH /alerts/{id} | status=ACKNOWLEDGED/CLOSED | 200 idempotent; mở lại CLOSED trả 409 |

Collections trả `{items,page,size,hasNext}`, page>=0, size=1..100. JSONB được trả thành object, timestamp UTC. GET resource không tồn tại trả 404. Thông báo lỗi nghiệp vụ dùng ProblemDetail và code; không trả exception/provider output thô.

Tên DTO tuần này được liệt kê tường minh để Tưởng tích hợp, chưa có frontend tiêu thụ các route mới. Các route file detail/coupling/trend/comparison/AI vẫn thuộc contract dự kiến của tuần 4, không được nhận đã cài bởi bộ này. Dữ liệu test chỉ nằm trong test database.
