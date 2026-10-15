# Đặc tả yêu cầu tuần 4 — bản 1

Ngày 08/10/2026. Người phụ trách: Trần Quang Toản. **Bản hoàn chỉnh để review, chưa ghi nhận phê duyệt của Tưởng/GVHD.** Đây là yêu cầu đích, không phải danh sách chức năng đã triển khai.

## 1. Cơ sở và phạm vi

Đối chiếu phân công tuần 4 (04–10/10), đề cương v3, [draft tuần 2](usecases-draft.md), [năm trường hợp phỏng vấn](interview-notes.md) và [metric spec v1 đã merge](metric-spec-v1.md). Core phân tích repository GitHub công khai, default branch, Java; chính sách tối đa 10.000 commit gần nhất trong cửa sổ là đề xuất BR16 chờ review; thứ tự chọn/truncated phải chốt trước code. Không cam kết đa repo, cảnh báo diff PR đang mở, tự sửa mã hoặc đánh giá năng suất developer.

Actor chính: người dùng có kinh nghiệm Git (developer/maintainer). Actor cấu hình: người vận hành nhóm được phép thay lịch và rule. Actor phụ: scheduler, GitHub, công cụ Lizard và provider LLM. Đây là vai trò thiết kế; bản tuần 3 **chưa có hệ thống tài khoản/phân quyền**, nên thao tác ghi chỉ dành cho phiên thử kiểm soát đến khi bổ sung bảo vệ.

## 2. Quy ước mã use case

Dùng UC01–UC11 của đề cương làm mã chuẩn từ tuần 4; không hiểu mã UC trong biên bản tuần 3 là mã mới. Ánh xạ giữ truy vết:

| Mã draft tuần 2 | Mã chuẩn | Ý nghĩa |
|---|---|---|
| UC1 | UC01 | Đăng ký repository |
| UC2 | UC02, UC03 | Khởi chạy và theo dõi job |
| UC3 | UC04, UC05 | Dashboard/hotspot và chi tiết file |
| UC4 | UC07, UC10 | Xu hướng và so sánh hai snapshot |
| UC5 | UC06 | Coupling |
| UC6 | UC05 | Ownership là phần chi tiết file |
| UC7, UC8 | UC08 | Lịch và rule giám sát |
| UC9 | UC09 | Cảnh báo |
| UC10, UC11 | UC11 | AI giải thích và checklist trong cùng yêu cầu |

Mã W3-AC của prototype giữ nguyên, không đồng nghĩa đủ nghiệm thu UC đích.

## 3. Use case và acceptance criteria

### UC01 — Đăng ký repository

- **Actor:** developer. **Điều kiện:** thao tác đăng ký được cho phép.
- **Input:** URL GitHub HTTPS; **output:** id, URL chuẩn hóa, owner/name, createdAt và kết quả xác minh công khai ở phiên bản đích.
- **Luồng:** kiểm tra cú pháp/whitelist → xác minh tồn tại và public → chuẩn hóa → lưu duy nhất → hiển thị để yêu cầu phân tích riêng.
- **Ngoại lệ:** URL sai; private/không tồn tại; GitHub timeout/rate limit; trùng; đăng ký bị tắt. Không biến lỗi mạng thành “repository không tồn tại”.
- **AC01.1:** Given URL public hợp lệ chưa có, When lưu thành công, Then trả 201 và reload vẫn thấy cùng id; chưa có job hay snapshot tự sinh.
- **AC01.2:** Given URL cùng repo khác hoa/thường hoặc `.git`, When đăng ký lại, Then 409 và chỉ có một bản ghi; URL/protocol ngoài whitelist không gây network/clone tùy ý.
- **AC01.3:** Given không xác minh được public, When đăng ký theo contract đích, Then trả lỗi phân loại và không ghi thành repository đã xác minh. Tuần 3 hiện mới kiểm tra cú pháp; cần bổ sung trước nghiệm thu UC này.

### UC02 — Khởi chạy phân tích

- **Actor:** developer hoặc scheduler. **Điều kiện:** repo đã được xác minh; tài nguyên cho phép.
- **Input:** repositoryId, cửa sổ quan sát, configVersion; **output:** jobId và status QUEUED.
- **Luồng:** kiểm tra quyền/cấu hình → chống job trùng → ghi job bền vững → trả 202 → worker fetch, cố định HEAD và thực hiện pipeline.
- **Ngoại lệ:** repo mất public, job đang hoạt động, hàng đợi đầy, clone/Lizard lỗi; chuyển FAILED có mã lỗi an toàn. Không giữ request chờ phân tích xong.
- **AC02.1:** Given repo hợp lệ không có job hoạt động, When yêu cầu, Then 202 + Location của job trong mục tiêu NFR01; chưa công bố snapshot dở dang.
- **AC02.2:** Given hai request đồng thời cùng repo, Then tối đa một job QUEUED/RUNNING; request còn lại 409 tham chiếu job đang chạy.
- **AC02.3:** Given pipeline lỗi/timeout, Then job FAILED và không có snapshot mới hiển thị. Chỉ khi có snapshot thành công cùng repo/HEAD/cả hai window bounds/effectiveConfigHash (gồm tool versions), job mới SUCCEEDED/NO_CHANGE trỏ snapshot cũ. HEAD giữ nguyên nhưng window trượt không đủ điều kiện.

### UC03 — Theo dõi tiến độ

- **Actor:** developer. **Input:** jobId; **output:** status, phase, progress có thể null, timestamps, snapshotId/outcome hoặc errorCode.
- **Điều kiện:** job tồn tại. **Luồng:** truy vấn/poll định kỳ → hiển thị trạng thái → mở snapshot khi thành công.
- **Ngoại lệ:** 404, mất mạng, job lỗi. Không bịa phần trăm khi không có mẫu số.
- **AC03.1:** Given job đã ghi, When truy vấn, Then thấy một trạng thái QUEUED/RUNNING/SUCCEEDED/FAILED cùng phase thực; không hiển thị 100% trước thành công.
- **AC03.2:** Given worker chết giữa chừng, Then cơ chế phục hồi đánh dấu lease hết hạn là FAILED trong giới hạn NFR04; UI cho phép yêu cầu job mới.

### UC04 — Xem tổng quan rủi ro

- **Actor:** developer. **Input:** repositoryId, snapshotId, phân trang/lọc; **output:** số liệu snapshot, danh sách hotspot và bằng chứng.
- **Điều kiện:** snapshot thành công. **Luồng:** chọn snapshot → xem xếp hạng → mở chi tiết file.
- **Ngoại lệ:** chưa có snapshot, không đủ dữ liệu, snapshot không thuộc repo; thể hiện rỗng/lỗi riêng.
- **AC04.1:** Given snapshot thành công, Then mỗi hàng có fileId/path, frequency, max CCN, score và configVersion/window; xếp hạng score giảm dần, tie theo path tăng dần.
- **AC04.2:** Given chưa phân tích, Then không hiển thị dữ liệu mẫu như kết quả thật; null không bị vẽ thành 0. Không khẳng định file có bug chỉ từ score.

### UC05 — Phân tích một tập tin

- **Actor:** developer. **Input:** snapshotId, logicalFileId; **output:** metric, change evidence, contribution/HHI, lý do xếp hạng.
- **Luồng:** đọc đúng file trong snapshot → hiển thị chỉ số và nguồn → đi tới commit hoặc coupling/AI.
- **Ngoại lệ:** file không thuộc snapshot, rename không xác định, contribution bằng 0, không có function Java.
- **AC05.1:** Given file có dữ liệu, Then hiển thị max_ccn, avg_ccn, function_count, nloc, frequency, churn, hotspot và contribution basis đúng metric-spec-v1; dẫn chứng commit thuộc cửa sổ đã công bố.
- **AC05.2:** Given HHI cao, Then chỉ mô tả tập trung đóng góp; không gán năng lực/mức hiểu mã. Thiếu dữ liệu có trạng thái/lý do, không tự thay bằng 0.

### UC06 — Xem temporal coupling

- **Actor:** developer. **Input:** snapshotId, fileId; **output:** cặp file, sharedCommits, commitCountA/B, coupling và danh sách evidence có phân trang.
- **Luồng:** lấy các cặp đạt ngưỡng → xem mẫu số/cửa sổ → mở commit chung.
- **Ngoại lệ:** thiếu dữ liệu hoặc không có cặp đạt ngưỡng; không suy ra độc lập ngữ nghĩa.
- **AC06.1:** Given A=10 commit, B=20, chung=5 trong tập đủ điều kiện, Then coupling=0,50 và qua ngưỡng mặc định; chung=4 thì không qua dù tỷ lệ cao.
- **AC06.2:** Given xem bằng chứng, Then mẫu số min và commit nguồn được công bố; không diễn giải score đối xứng thành xác suất B thay đổi khi A đổi.

### UC07 — Xem xu hướng

- **Actor:** developer. **Input:** repositoryId, logicalFileId, tập snapshot; **output:** chuỗi metric theo thời điểm.
- **Luồng:** chọn file/khoảng → kiểm tra tính so sánh → vẽ điểm kèm snapshot/HEAD/config.
- **Ngoại lệ:** một snapshot, file xuất hiện/biến mất, configVersion khác; không nối khoảng thiếu thành số 0.
- **AC07.1:** Given ít nhất hai snapshot so sánh được, Then sắp theo thời gian, click từng điểm mở bằng chứng snapshot tương ứng.
- **AC07.2:** Given công thức/cấu hình khác, Then hiển thị dấu ngắt/cảnh báo không so sánh trực tiếp, không kết luận tăng rủi ro từ việc đổi cấu hình.

### UC08 — Cấu hình giám sát

- **Actor:** người vận hành được phép. **Input:** enabled, intervalMinutes, ruleType/threshold; **output:** cấu hình có version.
- **Luồng:** kiểm tra giá trị → lưu → scheduler đọc lịch đến hạn → tạo job theo UC02.
- **Ngoại lệ:** không được phép, lịch quá dày, rule không hỗ trợ, repo đang có job; không sinh backlog vô hạn.
- **AC08.1:** Given interval >=60 phút và cấu hình hợp lệ, Then lưu được; disabled không sinh job; giá trị ngoài giới hạn trả 400. Mốc 60 phút là đề xuất vận hành cần review.
- **AC08.2:** Given đã có job hoạt động khi lịch đến hạn, Then bỏ lượt đó và ghi lý do; không tạo job song song cho cùng repo.

### UC09 — Xem và xử lý cảnh báo

- **Actor:** developer/người vận hành. **Input:** filter status, alertId, hành động; **output:** rule, giá trị, snapshot/evidence và trạng thái OPEN/ACKNOWLEDGED/CLOSED.
- **Luồng:** xem → đọc lý do → acknowledge hoặc đóng; hệ thống chỉ sinh từ snapshot thành công.
- **Ngoại lệ:** id không tồn tại, hành động không hợp lệ, thiếu snapshot so sánh; không bịa cảnh báo.
- **AC09.1:** Given cùng rule/file-or-pair/snapshot được đánh giá hai lần, Then có tối đa một alert; có observedValue, threshold, ruleVersion và evidence.
- **AC09.2:** Given OPEN, When acknowledge, Then ACKNOWLEDGED; khi đóng chuyển CLOSED, gọi đóng lại không tạo bản ghi mới. CLOSED không mở lại ngầm.

### UC10 — So sánh hai snapshot

- **Actor:** developer. **Input:** fromSnapshotId, toSnapshotId cùng repo; **output:** metric trước/sau/delta và file added/removed/changed.
- **Luồng:** kiểm tra phạm vi/phiên bản → ghép logicalFileId → hiện thay đổi, rename có evidence.
- **Ngoại lệ:** khác repo hoặc khác cấu hình tính metric; trả lỗi rõ thay vì delta gây hiểu nhầm.
- **AC10.1:** Given hai snapshot tương thích cùng repo, Then delta=after-before, rename có nhận diện không bị đếm thành file mới chỉ vì đổi tên.
- **AC10.2:** Given file chỉ có ở một snapshot, Then before/after thiếu là null, trạng thái added/removed; khác config trả 409 INCOMPATIBLE_SNAPSHOTS.

### UC11 — Rà soát hotspot bằng LLM

- **Actor:** developer; provider là actor phụ. **Điều kiện:** snapshot thành công, code public đúng HEAD, người dùng chủ động chọn phần mã và đồng ý gửi.
- **Input:** snapshotId/fileId, lineRanges và consent; **output:** JSON theo [schema](schemas/ai-review-output.schema.json), nhãn AI-generated.
- **Luồng:** dựng context giới hạn → kiểm tra secret → gọi provider → validate cấu trúc và evidence allowlist → hiển thị gợi ý; không sửa repository.
- **Ngoại lệ:** quá giới hạn, thiếu consent, quota, timeout, provider lỗi, JSON/evidence sai; hiển thị “Không thể phân tích” và mã lỗi an toàn.
- **AC11.1:** Given context hợp lệ và provider trả schema/evidence hợp lệ, Then hiện riskSummary/evidence/reviewChecklist/refactorSuggestions/limitations theo schema; điểm hotspot gốc không thay đổi.
- **AC11.2:** Given provider timeout hoặc trả reference không có trong context, Then không hiển thị như kết quả hợp lệ, dashboard vẫn sử dụng được; không tự retry gây phí.
- **AC11.3:** Given không consent hoặc context có secret/chưa kiểm tra được, Then không gửi ra provider. Mọi output chỉ tư vấn, không auto-apply/commit/PR.

## 4. Truy vết khảo sát → yêu cầu → API

| Nhu cầu | Cơ sở | UC / AC | API chính |
|---|---|---|---|
| Khoanh vùng file | N01, S01–S04 | UC04/05; AC04.1, AC05.1 | hotspots, file detail |
| Không bỏ sót file liên quan | N02 | UC06; AC06.1–2 | coupling; chỉ lịch sử, không diff PR |
| Bằng chứng giải thích | N03 | UC04–07, UC10–11 | metric/commit/snapshot evidence |
| Hỏi đúng người | N04 | UC05; AC05.2 | contribution/HHI trong file detail |
| Giảm nhiễu cảnh báo | N05 | UC08–09 | monitoring, alert rules, alerts |
| AI có kiểm chứng và riêng tư | N06–07 | UC11; AC11.1–3 | ai-reviews |
| Git + AI hiện tại đã đủ | N08, S05 | Tiêu chí đánh giá giá trị, không tự thêm feature | Đo thử nghiệm so với workflow hiện tại |

API chi tiết ở [contract](api-contract.md); rule/NFR ở [business-rules-nfr.md](business-rules-nfr.md). Mỗi UC có tiêu chí nghiệm thu nhưng **chưa đồng nghĩa đã có test tự động hoặc đã đạt**.

## 5. Bàn giao review

Tưởng review đường job, snapshot, evidence và tính khả thi; Toản phụ trách UI/API và truy vết. Xem [danh sách quyết định](week-04/README.md). Chưa chốt auth đầy đủ, percentile/tie edge cases, budget benchmark hoặc provider. Không đưa quyết định chưa duyệt thành kết quả thực nghiệm.

Bản đối chiếu 09/10/2026: kiến trúc/contract căn chỉnh trong [review chung](week-04/review-tich-hop-tuong-toan.md); chưa ghi nhận approval thành viên/GVHD.
