# Module thu thập Git — tuần 6

Soạn 23/10/2026 trên nhánh `codex/tuong-week6` (gốc `a7c84d1`, commit module `4462e2d`). Phạm vi: **đọc lịch sử Git bằng JGit và xuất fact**. Chưa có endpoint, chưa có job, chưa ghi PostgreSQL, chưa tính metric.

## Hai class

| Class | Việc | Ràng buộc |
|---|---|---|
| `GitRepositoryClient` | clone/fetch repo công khai GitHub vào thư mục **bare** do worker quản lý, trả `PinnedRepository(directory, branch, headSha)` | Chỉ nhận `https://github.com/owner/repo`; credentials trả lời `false` nên không thể gửi token; không checkout, không submodule, không chạy code trong repo; `followRedirects=false`; từ chối repo shallow; mỗi bước kiểm tra deadline và interrupt |
| `GitIngestion` | duyệt DAG từ SHA đã chốt, dựng lineage file, sinh `Fact` cho commit trong cửa sổ | Chỉ nhận **full SHA 40 ký tự**, không nhận tên branch đang di chuyển; vượt giới hạn thì ném `RESOURCE_LIMIT_*` chứ không trả kết quả cắt cụt |

Thứ tự xử lý là topo cha-trước-con (Kahn), các node cùng khả năng xử lý xếp theo SHA nên kết quả tái lập được. Không dùng timestamp để cắt lịch sử. Lineage dựng trên **toàn bộ** lịch sử, kể cả commit ngoài cửa sổ; fact chỉ phát sinh cho commit có committer time trong cửa sổ.

Rename detection dùng `renameScore=60`, `renameLimit=1000`, `binaryFileThreshold=maxBlobBytes`. Merge commit **không sinh fact**; nó chỉ dùng để hợp lineage. Copy tạo ID mới, delete đóng lineage, tạo lại cùng path sau delete là ID mới.

## Hợp đồng dữ liệu cho lớp lưu trữ

`Fact`: `commitSha, parentSha, committerTime, authorKey, logicalFileId, oldPath, newPath, changeType, additions, deletions, qualityFlags`.

- `logicalFileId` = `<sha>:<path>` của commit khai sinh ra file. Đây là khóa để nối một file qua rename, **không phải path**. Bảng của Toản nên lưu cả `oldPath`/`newPath` theo commit để truy vết.
- Khóa chống trùng theo [algorithm-design](../algorithm-design.md): `(commitSha, logicalFileId)`.
- `changeType` là ten nguyên của JGit: `ADD, CHANGE, MODIFY, DELETE, RENAME, COPY`. `oldPath` null với `ADD`, `newPath` null với `DELETE`.
- `additions`/`deletions` **null** nghĩa là không đo được (file nhị phân hoặc không phải file thường), **khác 0**. Rename thuần là `0/0`. UI và metric phải phân biệt null với 0.
- `authorKey` = email tác giả đã bỏ khoảng trắng và chuyển chữ thường; rỗng thì `unknown:<sha>` kèm cờ `AUTHOR_MISSING`. Map `authorAliases` cho phép gộp nhiều email về một người, chưa bật mặc định.
- `qualityFlags` của `Result` là hợp cờ toàn snapshot: `LINEAGE_AMBIGUOUS, AUTHOR_MISSING, BINARY, NON_REGULAR_FILE`. File có lineage bất định phải coi metric lịch sử là **unavailable**, không thay bằng 0.
- Bộ lọc mặc định: chỉ giữ file `.java`, loại prefix `vendor/ generated/ target/ build/`. Giới hạn mặc định 10.000 commit, 100.000 tree entry, 2 MiB/blob, 500.000 fact, deadline 300 giây.

## Lệnh chạy mẫu (không cần phần của Toản)

Lệnh này chạy thẳng JGit, không khởi động Spring nên không cần database.

```powershell
$env:JAVA_HOME='<duong dan JDK 21>'
cd backend
.\mvnw.cmd -B -ntp dependency:build-classpath "-Dmdep.outputFile=target/cp.txt"
java -cp "target/classes;$(Get-Content target/cp.txt)" `
  com.example.demo.gitanalyzer.GitIngestionSample `
  https://github.com/spring-guides/gs-rest-service target/sample-cache 20000
```

Đối số: URL GitHub, thư mục cache bare, số ngày cửa sổ (bỏ trống = 180 theo metric spec). Cache đã tồn tại sẽ `fetch`, chưa có sẽ `clone`, nên chạy lần hai rẻ hơn.

Kết quả đo ngày 23/10/2026:

| Repo | reachable | selected | fact | renameFacts | file Java hiện có | lineage bất định |
|---|---|---|---|---|---|---|
| `spring-guides/gs-rest-service` (window ~54 năm) | 421 | 396 | 83 | 16 | 6 | 2 |
| `spring-projects/spring-petclinic` (365 ngày) | 1.047 | 49 | 92 | 0 | 50 | 13 |
| `octocat/Hello-World` (window ~54 năm) | 3 | 2 | 0 | 0 | 0 | 0 |

Hai con số 421/83/16 khớp đúng phép đo ngày 22/10, xác nhận module vẫn cho cùng kết quả sau khi sửa lỗi hủy tác vụ. Cửa sổ mặc định 180 ngày trên gs-rest-service chỉ ra **0 fact** vì repo không có commit Java nào trong 180 ngày gần nhất; muốn phủ toàn lịch sử phải truyền số ngày lớn hơn.

## Minh chứng kiểm thử

`mvn -o -B test` ngày 23/10: `GitIngestionTests` 10/10, `GitIngestionSampleTests` 2/2, `GitHubRepositoryUrlTests` 16/16 → **28/28 ca không phụ thuộc database đạt**. Toàn bộ 52 ca: 24 lỗi, tất cả là `Connection to localhost:5432 refused` — Docker trên máy vẫn hỏng và chưa dựng lại PostgreSQL portable, **giống nguyên trạng tuần 5**, không phải lỗi sinh ra từ module này.

Fixture đã phủ: root/modify/rename/delete/re-add/copy, committer time ngược đồng hồ, rename trước cửa sổ, file nhị phân, merge một branch, alias tác giả, giới hạn commit, deadline hết hạn, repo shallow, clone/fetch giữ SHA cũ replay được, từ chối URL cục bộ trước khi tạo thư mục.

## Ba điểm cần hai người chốt trước khi nối vào job

1. **Merge lineage đang bảo thủ hơn design doc.** algorithm-design dòng 49 ghi chỉ đánh dấu khi "nhiều lineage cùng cạnh tranh một path"; code hiện đánh dấu cả trường hợp path **vắng ở một cha** (file chỉ tồn tại trên một nhánh). Hệ quả đo được: 13/50 file Java của petclinic bị coi là bất định, tức mất phần lớn coverage. Nới thì tăng coverage, giữ thì an toàn hơn nhưng metric thiếu nhiều. Test `mergeDoesNotEmitFactsAndConflictingLineagesAreFlagged` đang **ghim** hành vi giữ — đổi phải đổi cả design doc và test, không đổi một chỗ.
2. **Đừng đọc `renameFacts = 0` thành "repo không có rename".** Bộ đếm chỉ tính rename **trong cửa sổ**, còn lineage vẫn nối qua rename xảy ra trước cửa sổ (petclinic: 0 renameFacts nhưng hai file có origin `.../web/`). Nếu cần con số "tổng rename đã thấy", phải đếm riêng từ state lineage, chưa có.
3. **Chưa có số benchmark cho mốc 10.000 commit và deadline 300 giây.** Mốc này design doc đánh dấu "chưa benchmark"; petclinic 1.047 commit đi hết trong một lần gọi nhưng chưa tách thời gian clone/duyệt/diff, nên chưa đủ cơ sở chốt trần thời gian của job.

Việc còn lại của tuần 6: nối `Fact` vào bảng snapshot của Toản, hàng đợi job + lưu SHA đã chốt, hoàn thiện binding preview/consent của AI review, và chốt 5 quyết định còn mở ghi ở [week-05 README](../week-05/README.md).

Tài liệu mô tả module **đã cài và đã test ở mức đơn lập**; chưa phải pipeline hoàn chỉnh, chưa có metric đầu ra, chưa chứng minh trên PostgreSQL.
