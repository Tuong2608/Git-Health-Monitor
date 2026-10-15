# Thiết kế thuật toán tính chỉ số

Tuần 5 (11–17/10/2026). Tưởng phụ trách. Soạn 15/10 trên main `8d55040` và bản căn chỉnh tuần 4. Đây là thiết kế, chưa phải pipeline đã triển khai.

## 1. Baseline và các lựa chọn cần review

[Metric Spec v1](metric-spec-v1.md) là nguồn công thức: default branch, 180 ngày, bỏ merge commit, rename detection, max function CCN, hotspot = sqrt(P_change × P_complexity), coupling = shared/min, HHI theo churn. Không thay công thức hoặc đặt ngưỡng HHI mới.

Các quy ước chi tiết dưới đây là **đề xuất tuần 5**, cần nhóm review rồi cập nhật spec/config/test cùng nhau trước cài đặt:

| Quyết định | Đề xuất để triển khai |
|---|---|
| Thời gian | UTC, lọc committer timestamp trong [start,end); end cố định lúc tạo yêu cầu, retry dùng lại bounds |
| Universe U | File Java hợp lệ còn tồn tại tại SHA phân tích, sau exclusions; file bị xóa vẫn có facts lịch sử nhưng không xếp hạng hiện tại |
| Percentile | Midrank chuẩn hóa theo (midrank−1)/(n−1); n=1 dùng 0,5; n=0 không có kết quả |
| Không hàm | Parse thành công nhưng function_count=0: max/avg CCN=null, hotspot=null; không giả complexity=0 |
| Churn bằng 0 | HHI=null, reason=NO_LINE_CONTRIBUTION; không coi là phân tán hoàn toàn |
| BR14 | Mặc định thiết kế baseline không lọc changeset lớn; chế độ bỏ >30 file chỉ bật sau khi được duyệt, lọc cả tử/mẫu coupling |
| BR16 | Baseline không tự cắt 10.000 commit; nếu vượt resource budget thì báo RESOURCE_LIMIT. Chế độ capped là đề xuất riêng, có truncated, không trộn với baseline |

Không có rule file phải ≥3 commit mới được tính hotspot. Phạm vi clone toàn lịch sử khác phạm vi tính metric. Window/commit selection/exclusions/identity map/rename settings/percentile/tool versions đều phải có provenance.

## 2. Input, facts và thứ tự xử lý

Input: repositoryId, resolvedBranch, headSha, observationStart/End, cấu hình bất biến và phiên bản công cụ. `headSha` cố định một lần; không đọc branch đang thay đổi giữa các bước. Không chạy build/script trong repository đầu vào.

```text
analyze(context):
  fetch and pin context.headSha
  U = eligible Java files in tree(headSha)
  history = all commits reachable from headSha
  establish logical-file lineage from history (also outside observation window)
  candidates = non-merge commits whose committerTime is in [start,end)
  selected = select(candidates, configured policy)
  facts = extract(selected, lineage, exclusionConfig)
  complexity = parse source of U at headSha using pinned Lizard version
  metrics = aggregate(facts, U, complexity)
  validate completeness, provenance, invariants
  return immutable result to snapshot publisher
```

Không dừng duyệt DAG khi gặp một timestamp cũ: timestamp không đảm bảo tăng theo topology. Dùng thứ tự topo cha trước con để dựng lineage; giữa các node cùng khả năng xử lý, sort SHA để tái lập. Nếu bật cap sau review, chọn candidates theo `(committerTime DESC, SHA ASC)` lấy N đầu, rồi xử lý theo topo. Lưu selected count, cutoff timestamp/SHA, hash danh sách SHA, truncated. Không đổi requested window thành effective bounds rồi coi hai mẫu là tương đương.

### Diff và logical file

- Root commit: diff với cây rỗng. Non-merge commit: diff với parent duy nhất. Merge không sinh metric facts.
- Rename detection nối ID khi có bằng chứng rename, giữ old/new path theo commit. Rename thuần không tăng churn nhưng vẫn là một lần thay đổi.
- Copy tạo logical ID mới; không sao chép contribution lịch sử của nguồn. Delete đóng lineage. Tạo lại cùng path sau delete là ID mới.
- Lịch sử có nhánh/merge: dựng ánh xạ `(commitSha,path) → logicalFileId` từ cây cha và bằng chứng diff. Merge chỉ dùng giải quyết lineage, không đếm change. Nếu nhiều lineage cùng cạnh tranh một path mà không xác định được, đánh dấu LINEAGE_AMBIGUOUS; không gộp đoán theo tên.
- Với file U có lineage không xác định, metric lịch sử liên quan là unavailable; snapshot có quality flag, không tuyên bố coverage đầy đủ. File cũ bị xóa không biến mất khỏi audit facts.
- Binary, generated, vendor và path exclusions theo config. Đếm logical ID một lần mỗi commit. Chỉ dùng line diff additions/deletions của text hợp lệ; không thay dữ liệu thiếu bằng 0.
- Tác giả dùng author, không dùng committer để phân bổ churn. Ưu tiên identity mapping tường minh; trim và chuẩn hóa email theo chính sách có version. Không gộp theo display name. Thiếu identity dùng key riêng theo commit, gắn quality flag, không gom mọi người vô danh thành một người.

Fact tối thiểu: commitSha, parentSha, committerTime, authorKey, logicalFileId, oldPath/newPath, changeType, additions, deletions, qualityFlags. Khóa chống trùng `(commitSha,logicalFileId)` sau khi hợp nhất diff entries; lưu đường dẫn và evidence để truy vết. Root/rename/merge/copy phải có fixture riêng.

## 3. Change frequency, churn và HHI

```text
for each selected non-merge commit c:
  changes = coalesce diff entries by logicalFileId
  for each eligible change f:
    commits[f].add(c.sha)
    churn[f] += f.additions + f.deletions
    contribution[f, normalize(c.author)] += f.additions + f.deletions
for each f in U:
  frequency[f] = size(commits[f])   # zero if complete history has no change
  total = sum(contribution[f,*])
  HHI[f] = null if total == 0 else sum((amount/total)^2)
```

Nếu extraction thiếu/hỏng, không trả frequency=0 như thành công; lỗi toàn extraction làm job FAILED, còn dữ liệu không đo được ở từng file phải có cờ rõ ràng. HHI là contribution concentration, không xác nhận quyền sở hữu hay hiểu biết. Pure rename có frequency=1, churn=0, HHI=null. Một người góp 100% → HHI=1; 75/25 → 0,625; 50/50 → 0,5.

## 4. Complexity và hotspot

Lizard đọc blob đúng headSha. Lưu max_ccn, avg_ccn, function_count, nloc, parseStatus và version. Parse lỗi → null/flag; timeout/crash công cụ làm thất bại bước phân tích, không xuất snapshot toàn số 0. Parse không hàm là trạng thái hợp lệ khác parse lỗi.

Population R gồm file U có frequency và max_ccn hợp lệ. Dùng cùng R cho hai percentile. Hiện số file excluded/unavailable để người dùng biết coverage.

```text
percentile(values, x):
  n = len(values)
  if n == 0: return null
  if n == 1: return 0.5
  lower = count(v < x)
  equal = count(v == x)
  midrank = lower + (equal + 1)/2
  return (midrank - 1)/(n - 1)

for f in R:
  P_change[f] = percentile(frequency[R], frequency[f])
  P_complexity[f] = percentile(max_ccn[R], max_ccn[f])
  hotspot[f] = sqrt(P_change[f] * P_complexity[f])
```

Tính percentile bằng sort/group ties một lần cho mỗi vector, không quét O(n) cho từng file. Score trong [0,1]; UI có thể nhân 100 để hiển thị nhưng không đổi thang dữ liệu/API. Cùng tất cả giá trị → percentile 0,5. Population thay đổi có thể đổi score dù metric thô giữ nguyên; cảnh báo cần kèm raw values và population count. Tie ranking: score giảm dần, logicalFileId tăng dần; thứ tự phụ không thay percentile.

Fixture: frequency=[1,2,2,4], CCN=[1,2,2,4] → percentile và hotspot đều là `[0.0, 0.5, 0.5, 1.0]` (ký pháp JSON). Hai vector ngược thứ tự cần tính tích theo đúng file ID, không zip hai mảng đã sort độc lập.

## 5. Temporal coupling

```text
for c in selected commits:
  S = unique eligible logical IDs touched by c
  if approved largeChangesetFilter and size(S) > 30: continue
  for f in S: couplingCommits[f].add(c.sha)
  for unordered pair (a,b) in S where a < b:
    shared[a,b] += 1
for pair with both endpoints in U:
  denominator = min(size(couplingCommits[a]),size(couplingCommits[b]))
  score = shared[a,b] / denominator  # positive for an observed pair
  visible = shared[a,b] >= 5 and score >= 0.50
```

Nếu bật BR14, đếm size(S) trên toàn file source hợp lệ trong changeset, gồm file bị xóa sau này; chỉ lọc endpoint còn ở U khi xuất. Không dùng frequency chưa lọc làm mẫu số trong chế độ coupling đã lọc. File cùng commit lặp path/rename chỉ đếm một lần. Không làm tròn trước so ngưỡng. Không có shared pair → không có pair record; absence không tự là missing metric.

Fixture A=10 commit, B=20, shared=5 → 0,5, hiển thị. shared=4 dù score=1 → chưa hiển thị. A=10,B=20,shared=4 → 0,4. Loại một commit lớn chung: mẫu số A=9,B=19, shared=4 → 4/9, không đạt shared≥5.

## 6. Chi phí, invariant và nghiệm thu dự kiến

Với C commit, F facts, N file, k_c file/commit: aggregation O(F), percentile O(N log N), coupling O(Σ k_c²), bộ nhớ sparse pairs O(P) và commit sets O(F). Full clone và lineage có thể lớn hơn window; giới hạn disk/time/pairs rõ ràng. Vượt giới hạn phải báo RESOURCE_LIMIT, không âm thầm bỏ pairs hoặc công bố kết quả đủ coverage. Mốc 10.000 commit/10 phút chưa benchmark.

| Fixture | Kỳ vọng |
|---|---|
| Root, merge, boundary | Root có diff; merge không tăng counts; start được lấy, end bị loại |
| Rename/copy/re-add | Rename giữ ID, copy/re-add ID mới; lineage bất định có flag |
| Identity alias | Mapping cùng người không làm HHI phân mảnh; không gộp theo tên |
| Empty/zero/no function | Không NaN; HHI/CCN/hotspot null có reason khi không đo được |
| Ties/singleton | Midrank đúng; một file percentile=0,5; không division by zero |
| Pair thresholds | Đúng shared/min và biên 5/0,50; không dùng Jaccard |
| BR14 modes | Chế độ baseline và đề xuất lọc khác configHash; không trộn mẫu số |
| Replay | Cùng SHA/window/config/tools cho output chuẩn hóa giống nhau, tolerance đề xuất 1e-9 |

Invariant: frequency ≤ selected count; churn ≥0; Σ contributor churn = file churn; nếu tổng>0 thì 0<HHI≤1; shared≤min(counts); mọi pair a<b; percentile/hotspot trong [0,1]. Đây là acceptance cases thiết kế, chưa là test backend đã chạy.

Liên quan: [giám sát](monitoring-design.md), [thực nghiệm](experiment-design.md), [kiến trúc](architecture.md).
