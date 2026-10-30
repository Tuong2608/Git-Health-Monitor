# Thiết kế giám sát định kỳ và cảnh báo

Tuần 5, ngày 15/10/2026. Tưởng phụ trách theo phân công mới của người dùng. Đề xuất để phối hợp với ERD/API của Toản; chưa triển khai scheduler/snapshot/alert.

## 1. Scheduler và job

Lịch theo repository gồm enabled, interval, nextRunAt UTC và scheduleVersion. Giá trị interval cần người vận hành chọn, không hardcode từ ví dụ. Scheduler poll bằng DB time; chỉ enqueue repository đến hạn qua cùng application service với thao tác manual.

```text
transaction:
  claim due schedule row
  if active QUEUED/RUNNING job exists: defer due time with bounded backoff
  else:
    persist QUEUED job with frozen requested window and trigger=SCHEDULED
    advance nextRunAt to first future occurrence (coalesce missed ticks)
commit
dispatcher claims job and submits bounded executor
```

Không phát lại hàng trăm lịch bị lỡ sau downtime. Unique active job/repo ở DB chống race giữa scheduler/manual; executor từ chối không làm mất job. Lỗi enqueue rollback cả nextRunAt. Queue đầy defer có backoff và metadata, không vòng lặp nóng. Retry tạo job mới, có retryOf và cùng input window nếu muốn tái lập; không đổi FAILED thành RUNNING ngầm.

Claim token, heartbeat, lease và publication theo [kiến trúc](architecture.md). Đề xuất heartbeat 30s/lease 120s/watchdog ≤60s, chưa đo tải. Token hết lease không được publish dù watchdog chưa chạy; worker cũ phải dừng process, workspace riêng tránh ghi đè. Deadline không thay mục tiêu benchmark.

## 2. Snapshot bất biến và NO_CHANGE

Một **lần phân tích thành công có dữ liệu mới** công bố một analysis_snapshot cùng các bảng metric/pair/contribution. Snapshot là trạng thái toàn bộ metric trong universe, không chỉ các file thay đổi. Job thất bại không công bố snapshot. Một lượt quét có identity giống hệt có thể SUCCEEDED/NO_CHANGE và tham chiếu snapshot cũ theo BR06; vì vậy không mặc định mỗi tick luôn tạo một snapshot.

Metadata đề xuất cho Toản: repositoryId, snapshotId/jobId, headSha, resolvedBranch, observationStart/End, effectiveConfigHash, metricSpecVersion, toolVersions, analyzedCommitCount, selectedCommitSetHash, truncated, populationCount, coverage/qualityFlags, completedAt. Đây là hợp đồng logic, không áp đặt tên cột/migration trước ERD.

Identity = repo + SHA + hai window bounds + effectiveConfigHash. Hash chứa thuật toán/policy chọn commit, tool version, exclusions, identity mapping và lineage/percentile; window bounds lưu riêng. Cùng HEAD nhưng cửa sổ trượt hoặc config đổi vẫn tính lại. Chỉ reuse snapshot đã published và còn đủ dữ liệu; không reuse staging/failed.

Ghi staging theo job/claimToken. Transaction publication kiểm tra ownership/status/lease bằng DB time, uniqueness identity, completeness; lưu header + toàn bộ detail nhất quán và SUCCEEDED. API chỉ đọc published. Nếu cần batch lớn, detail chưa published không được lộ. Conflict identity do retry trả snapshot đã có thay vì tạo bản sao.

## 3. So sánh hai snapshot

S_new phải published. Chọn S_old là bản trước đó theo observationEnd (tie completedAt/id), cùng repository và cấu hình tương thích. Không dùng bản có windowEnd muộn hơn để giải thích quá khứ. Không có baseline → FIRST_SNAPSHOT; vẫn có thể xét ngưỡng tuyệt đối.

Tương thích: cùng metric definitions/tools, exclusions, identity/lineage policy, duration window và commit selection mode. Window trượt có bounds khác là hợp lệ, phải hiển thị cả hai. Truncated hoặc coverage khác làm delta không đáng tin: đề xuất không sinh delta alert, ghi NOT_COMPARABLE và lý do. Config/rule thay đổi phải hiển thị, không diễn giải thành code xấu đi.

Join file bằng logicalFileId; rename chỉ thay path. Join pair bằng ordered IDs. Phân loại ADDED/REMOVED/UNCHANGED/CHANGED/UNAVAILABLE. REMOVED nghĩa rời universe, có thể do xóa hoặc exclusion, không tự kết luận rủi ro đã giảm.

```text
for metric and file present with valid values in both snapshots:
  absoluteDelta = new - old
  relativeDelta = null if old == 0 else (new-old)/abs(old)
  keep old,new,absoluteDelta,relativeDelta,units,both snapshot IDs
```

Ví dụ churn 100→120: +20, +20%. 0→5: +5, relative=null/BASE_ZERO. HHI null→0,8: NEWLY_AVAILABLE, không delta. Hotspot 0,5→0,7: +0,2 trên thang [0,1]; lưu raw frequency/CCN/population để giải thích biến động percentile. Pair absence chỉ coi shared=0 nếu extraction đầy đủ và universe/config tương thích; không thay null bằng zero.

## 4. Rule và vòng đời alert

Rule có ruleId/version, enabled, metric, scope(file/pair), mode(ABSOLUTE/DELTA), operator, threshold, severity và effectiveFrom. So sánh không làm tròn. Lưu observedValue, threshold, ruleVersion, snapshot IDs và evidence; không lưu chỉ câu thông báo.

- Rule coupling mặc định: shared≥5 **và** coupling≥0,50 theo spec. Hai điều kiện cùng một rule.
- Hotspot/change/churn: người vận hành cấu hình ngưỡng, không giả có ngưỡng chuẩn. HHI chưa có hard threshold mặc định; UI hiển thị phân bố.
- DELTA chỉ chạy với baseline tương thích, metric hợp lệ và đủ coverage. ABSOLUTE có thể chạy lần đầu. Thiếu dữ liệu → SKIPPED với reason, không báo an toàn.
- `UNIQUE(ruleVersion,snapshotId,targetKey)` chống trùng khi retry. targetKey là fileId hoặc pair canonical. Đề xuất v1 tạo một event mỗi snapshot vi phạm, UI gom theo rule/target; chưa gửi email/push tự động.
- OPEN → ACKNOWLEDGED → CLOSED do thao tác có quyền. Re-evaluate không mở lại event đã đóng. Snapshot sau vi phạm là event mới; việc hết ngưỡng không xóa evidence cũ. NO_CHANGE không sinh event mới chỉ vì scheduler chạy lại.

```text
evaluate(snapshot, rulesPinnedAtPublication):
  find compatible previous snapshot
  for enabled applicable rule and target:
    if insufficient data: record skip reason; continue
    if predicate true: insert alert on unique key, do nothing if exists
  record evaluation completion for snapshot + rule-set version
```

Metric publication không bị rollback bởi lỗi alert. Sau publish, đánh dấu evaluation PENDING bền vững; dispatcher tìm snapshot chưa đánh giá xong, retry idempotent. UI phân biệt PENDING/FAILED/COMPLETE, không hiện “0 cảnh báo” như đã kiểm tra khi chưa chạy. Cần Toản bố trí evaluation marker/rule-set version trong ERD 10 bảng hoặc metadata được thống nhất; không tự thêm bảng thứ 11.

Rule mới mặc định áp dụng snapshot mới; backfill lịch sử là thao tác tường minh có phạm vi/version. Retention giữ snapshot được alert/evidence tham chiếu hoặc đánh dấu evidence unavailable, không xóa ngầm nguồn của alert.

## 5. Ca kiểm chứng khi cài đặt

Đến hạn đồng thời manual/scheduler → một active job. Restart sau enqueue → job còn chạy được. Worker mất lease → không publication. Crash sau snapshot publish trước alert → retry đánh giá không mất/nhân alert. Cùng identity → NO_CHANGE; khác window → snapshot mới. Rename giữ logical ID → so sánh cùng file. Config/coverage khác → delta SKIPPED. Threshold equality theo operator. Baseline zero/null → không NaN/false improvement. Retry alert → một event/key; CLOSED giữ đóng.

Tuần 5 chỉ thiết kế. Toản cần review trường metadata, constraint và transaction cùng [algorithm design](algorithm-design.md), [API contract](api-contract.md).
