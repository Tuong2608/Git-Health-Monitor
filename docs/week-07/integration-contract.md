# Hợp đồng nối phần Toản với Git/metric của Tưởng

Nguồn đối chiếu: [Git ingestion của Tưởng tại 15f291b](https://github.com/Tuong2608/Git-Health-Monitor/blob/15f291b/docs/week-06/git-ingestion.md). Không sửa quyết định lineage hoặc mã Git trên nhánh Tưởng. Phiên kiểm tra dùng bản checkout riêng với các thay đổi backend tuần 7, 88/88 tests đạt; [manifest overlay](evidence/compatibility-overlay.json).

## Bốn điểm Tưởng nêu trong bàn giao tuần 6

| Điểm | Xử lý phía Toản tuần 7 | Phần còn cần Tưởng |
|---|---|---|
| observationDays 1..365 không đủ với repo ít hoạt động | Giữ giới hạn API 365 và mặc định180; capability công khai giới hạn. Không âm thầm mở rộng để tạo dữ liệu đẹp | Chọn repo/window thực nghiệm hợp lý; nếu cần rộng hơn, chốt giới hạn CPU/disk/time trước mở API. Sample CLI riêng không phải cùng endpoint |
| commitCount có thể nhầm reachable/selected | Javadoc định nghĩa selected non-merge commits trong `[start,end)`; frequency phải ≤ selected count | Map `GitIngestion.Result.selectedCommits()`, lưu reachableCommits ở summary riêng |
| churn bắt buộc số, không lưu được unknown | Migration V3 nullable frequency/churn/functionCount/nloc; missing phải có reason; HHI/score chỉ khi đủ input | Map unknown thành null và qualityFlags, không gán0; tổng contribution chỉ kiểm khi churn đo được |
| truncated dù thiết kế vượt giới hạn phải fail | Giữ cờ để đọc dữ liệu/mode khác; không đổi ingestion sang trả kết quả một phần. Delta/comparison/coupling alert không coi truncated là đầy đủ | Khi RESOURCE_LIMIT_* phải ném lỗi để job fail; chỉ dùng truncated nếu sau này có mode được chốt/config version riêng |

Những lựa chọn này là hợp đồng cài đặt để review. Không ghi rằng nhóm đã thống nhất tất cả điểm còn mở.

## Mapping vào AnalysisPipeline

- lineageKey = `Fact.logicalFileId`/`FileIdentity.id` của Tưởng (`birth SHA:path`), không phải path hiện tại. firstSeenSha và initialPath tách tại dấu `:` đầu tiên; phần path sau dấu này giữ nguyên. Không tự đoán lại lineage khi merge ambiguous.
- Path lấy từ currentFiles ở SHA cố định. LINEAGE_AMBIGUOUS → metric lịch sử không đủ phải null/reason; coverageComplete phản ánh chất lượng thật. File mới/tái tạo phải có key mới; rename giữ key cũ.
- FileData dùng Integer/Long nullable cho frequency/churn/functions/nloc. Java call sites cần boxed Long hoặc literal `100L`, không gọi constructor với int literal cho trường Long. Các test tuần 6 đã cập nhật tương ứng.
- Nếu churn=null, contribution list phải rỗng và HHI=null. Nếu churn đo được, tổng additions+deletions của contribution phải bằng churn. Frequency unknown không được tham gia pair có mẫu số giả. Hotspot không hợp lệ nếu thiếu frequency hoặc CCN.
- File không có hàm có thể functionCount=0, max/avgCCN=null và hotspot=null, giữ nloc đã đo; khác parse-error khiến số đo không có. HHI khi churn0 là null, không gán0.
- Các metric vẫn do module của Tưởng tính; publisher không tự tính từ fact hoặc gọi Lizard. V3 không tạo bảng facts thứ 11. Fact/evidence dùng JSONB phải giới hạn dung lượng và theo chuẩn `commits` array để phân trang qua API.

## Dạng evidence đề xuất đã được API hỗ trợ

```json
{
  "qualityFlags": [],
  "commits": [
    {"commitSha": "<40 hex SHA đã kiểm tra>", "changeType": "RENAME", "oldPath": "src/A.java", "newPath": "src/B.java"}
  ]
}
```

Ví dụ trên là hình dạng dữ liệu, không phải bằng chứng thật. File and pair evidence dùng cùng `commits` key; sắp thứ tự ổn định khi publish để pagination tái lập. Không lưu raw source hoặc email nhạy cảm vào evidence để API phát ra ngoài. Contributor identity nội bộ có thể là email theo ingestion, nhưng endpoint contribution chỉ trả id/displayName/count/share, không identityKey/aliases.

## Worker/publication và retry

Publisher kiểm tra cả window/config hash/config JSON khớp job trong DB bên cạnh token/lease. Adapter không được tự đổi window hoặc cấu hình rồi dùng claim cũ. Retry FAILED giữ window/config và tạo id mới; config adapter khác thì 409. HEAD chưa resolve trước lỗi không được coi là replay cố định, vì vậy kiểm chứng snapshot identity chỉ dựa trên SHA thực của kết quả.

Đã kiểm thử scheduler thật, HTTP enqueue/retry, publication và alert với adapter fixture test-only. Chưa chạy ingestion → metrics → persistence của repo public: nhánh Tưởng mới có fact, chưa có metric/Lizard/adapter. Checkout compatibility không tự merge nhánh thiết kế hoặc thay thế review PR.
