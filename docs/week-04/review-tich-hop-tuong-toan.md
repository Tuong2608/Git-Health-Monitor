# Đối chiếu phần việc tuần 4 của Tưởng và Toản

Ngày kiểm tra: 09/10/2026. Base chung: `6560cb1`. Nhánh Toản sau fetch: `origin/docs/toan-week-4` tại `33fefddc6a47291bbad17e96b7819290da1ce16c`. Phần Tưởng là tài liệu local chưa commit ở thư mục project gốc.

## Kết luận

Có 5 đường dẫn chồng nhau. Công thức metric nền và hướng modular monolith/TaskExecutor/PostgreSQL nhất quán. Chỗ cần căn chỉnh chính là contract/schema/budget AI, snapshot NO_CHANGE và cách diễn đạt timing lease. Bản tích hợp đã căn chỉnh để review. Các ghi nhận chưa commit/push bên dưới mô tả thời điểm kiểm tra ban đầu; xem mục cập nhật bàn giao cuối tài liệu.

## 1. Phạm vi ZIP và GitHub

ZIP người dùng cung cấp bằng byte với ZIP đính kèm nhánh Toản. So sánh các Markdown/JSON bên trong sau chuẩn hóa CRLF/LF: 15/16 file cùng nội dung; README tuần 4 trên nhánh mới thêm liên kết bộ Word/ZIP. Khác byte ở vài file còn lại chỉ do xuống dòng.

Đã đọc nội dung cả hai Word báo cáo/bàn giao; các yêu cầu gửi tin, approve/nộp báo cáo trong gói được coi là nội dung bàn giao, không tự thực thi. Nhánh có 4 commit sau base, gồm đặc tả, truy vết PR, lockfile patch và bộ Word/ZIP. Word/ZIP là snapshot ngày 08/10, giữ nguyên để truy vết; chưa cập nhật theo bản căn chỉnh ngày 09/10.

## 2. Xung đột đường dẫn

| File | Tình trạng hai bên | Cách xử lý trong bản tích hợp |
|---|---|---|
| README.md | Cùng thêm liên kết tuần 4 gần cùng chỗ; mô phỏng merge-file có 1 vùng conflict | Giữ điều hướng Toản, thêm kiến trúc và review chung |
| docs/ai-review-scope.md | Cùng sửa nháp cũ; mô phỏng có 3 vùng conflict | Một bản chung theo API/schema Toản, bổ sung giới hạn deadline/transport |
| docs/adr/001-full-clone.md | Cùng tạo mới cùng tên, nội dung khác | Giữ bản Toản và bổ sung trade-off/workspace/fixture của Tưởng |
| docs/adr/002-no-redis.md | Cùng tạo mới cùng tên, nội dung khác | Giữ cơ chế claim/lease Toản, thêm rejection/recovery/cache safeguards |
| docs/week-04/README.md | Cùng tạo mới, một bên bàn giao Toản, một bên Tưởng | README chung; lưu bản riêng Tưởng thành ban-giao-tuong.md |

Trạng thái hiện tại chưa phải một merge Git thất bại: tài liệu Tưởng chưa commit. Pull/checkout trực tiếp có thể bị Git chặn vì sửa local và các file untracked trùng file nhánh Toản. Nếu ghi nhận hai bộ thành commit từ cùng base thì ba file mới có nguy cơ add/add conflict. Không nên giải quyết bằng chọn toàn bộ ours/theirs.

## 3. Khác biệt thiết kế và bản sửa

| Mức | Vấn đề | Hệ quả | Căn chỉnh |
|---|---|---|---|
| Cần sửa trước cài AI | Tưởng POST 202 + GET result; Toản POST 200, không lưu output server | Backend/UI và retention không thống nhất | Chọn POST 200 của Toản làm đề xuất chung, bỏ GET/cache trong thiết kế Tưởng |
| Cần sửa trước cài AI | Tưởng 4 field, checklist chuỗi; Toản có limitations và advice object/evidenceIds | Validator có thể bác output của phía kia | Dùng schema JSON duy nhất của Toản, không tạo schema cạnh tranh |
| Cần sửa trước cài AI | 24 KiB/3 lượt phút/20 ngày so với 32 KiB/5 lượt phút/operator và concurrency khác | Một request hợp lệ phía này bị phía kia từ chối | Giữ bộ budget Toản để review; mọi số vẫn Proposed |
| Cần sửa trước cài AI | Client hiện timeout 15s, provider đề xuất 30s | UI báo lỗi trước response dù backend vẫn chạy | Thiết kế client AI 40s, backend tổng 35s, provider tối đa 30s dùng budget còn lại; chưa sửa code khi chưa có AI route |
| Cần sửa trước cài snapshot | Một số câu NO_CHANGE chỉ nói HEAD/config, architecture có cả window | Bỏ qua tính lại khi cửa sổ trượt dù lịch sử trong kỳ đổi | BR06/AC02.3/contract/ADR/architecture dùng cùng identity repo/SHA/window/effectiveConfigHash gồm tool versions |
| Cần làm rõ | NFR04 ghi lease/heartbeat 30s nhưng stale 120s | Developer có thể cài lease 30s hoặc 120s khác nhau | Đề xuất heartbeat 30s, lease 120s, watchdog ≤60s; phát hiện ≤180s khi DB/watchdog hoạt động |
| Cần làm rõ | Chỉ dùng token, thiếu kiểm tra lease trong finalize | Worker hết lease vẫn có thể ghi trước watchdog | Kiểm tra token/status/lease trong transaction publication; dùng DB time |
| Cần thống nhất văn bản | README ghi architecture chưa có; tài liệu Tưởng đã tồn tại local | Người nhận tưởng phần kiến trúc chưa làm | README chung liên kết architecture và bàn giao riêng |

Deadline mới là đề xuất tích hợp, chưa là đồng thuận hoặc benchmark. Cần kiểm tra timeout reverse proxy/hosting; nếu không cho request 35s phải mở lại quyết định async và retention. Không bật endpoint AI public dựa trên cờ đăng ký repository hiện có.

## 4. Các điểm tương thích giữ nguyên

- Metric baseline v1: window 180 ngày, shared/min, max function CCN, HHI theo additions+deletions, hotspot căn tích percentile, target ≤10.000 commit trong ≤10 phút.
- Một backend modular monolith; PostgreSQL lưu job, executor chạy trong tiến trình; không thêm Redis ở giai đoạn đầu.
- QUEUED/RUNNING/SUCCEEDED/FAILED; không giữ DB transaction trong suốt Git/Lizard; AI tách khỏi metric.
- Phần Toản requirements/API/NFR/schema và phần Tưởng architecture là bổ sung cho nhau; không cần viết lại toàn bộ.

## 5. Các đề xuất vẫn cần hai người quyết định

1. BR14 lọc changeset >30 file khỏi cả tử/mẫu coupling: hợp hướng đã nêu nhưng phải chốt cách đếm/exclusion trước cập nhật metric spec.
2. BR16 chọn 10.000 non-merge commit gần nhất và truncated: chốt thứ tự/timestamp/ties/effective bounds. Không âm thầm coi đây là baseline đã Accepted.
3. Pool 2/queue 20, heartbeat/lease/deadline 15 phút: thông số thử nghiệm, chưa có đo tải.
4. KPI nâng cao task success 90%, SUS 80 với ≥10 người: không thay cam kết đề cương 80%/68/5–9 người khi chưa duyệt điều chỉnh.
5. Provider/tokenizer/output cap, quyền truy cập/quota và chính sách dữ liệu provider. Bản căn chỉnh chọn bộ đề xuất Toản để tránh hai cấu hình, không ghi nhóm đã duyệt.

## 6. Dependency và CI

Nhánh Toản có thay đổi duy nhất ở code/dependency là lockfile source-map-js 1.2.1 →1.2.2 (version/resolved/integrity). Bản tích hợp giữ nguyên lockfile đó, không đụng frontend/package.json hoặc source runtime. Không có thay đổi dependency từ phía Tưởng để tạo xung đột ở file này.

Word của Toản ghi CI commit aa59997 đã đạt; đây là ghi nhận lịch sử của tài liệu. Phiên review này không xác minh được live PR/CI: gh CLI không có và trang web GitHub không đọc được. Fetch nhánh thành công không chứng minh CI commit 33fefdd đạt. Không chạy lại backend/browser/benchmark hoặc LLM trong đợt căn chỉnh tài liệu.

## 7. Bản bàn giao và kiểm tra

Bản tích hợp được đặt trong checkout riêng trên commit nhánh Toản; project gốc và ZIP gốc giữ nguyên. Tài liệu Tưởng trước sửa đã sao lưu dưới outputs/week4-review-20261009/tuong-before trong project gốc. Không có commit/push mới hay thao tác merge/approval trên PR.

Kết quả kiểm tra tĩnh và cách áp patch được ghi tại mục bổ sung bên dưới sau kiểm tra. [Mục lục chung](README.md), [kiến trúc](../architecture.md), [AI scope](../ai-review-scope.md), [API contract](../api-contract.md), [NFR](../business-rules-nfr.md).

Để tiếp nhận: review bản chung, ghi nhận lựa chọn thật, rồi đưa phần căn chỉnh vào nhánh dựa trên Toản và chạy checks phù hợp trước merge. Không copy toàn bộ checkout lên main hoặc chạy patch mù lên bộ Tưởng chưa commit.


### Kết quả kiểm tra tĩnh ngày 09/10

- 12 tài liệu trong bản căn chỉnh, 72 link Markdown local đều trỏ tới file có thật; fences cân bằng, không còn conflict marker. Ba khối Mermaid được kiểm tra cấu trúc code fence, chưa render thành ảnh.
- Giữ 11 UC và 25 AC duy nhất. Schema Draft 2020-12 hợp lệ; fixture hợp lệ; 7 mẫu sai schema và 3 mẫu reference/duplicate sai bị kiểm tra tĩnh từ chối. Bộ kiểm tra ngữ nghĩa là mô phỏng bên ngoài, chưa là code backend.
- git diff --check đạt. Lockfile source-map-js từ Toản được giữ nguyên. Hash nội dung tài liệu Tưởng ở project gốc không thay đổi so bản sao lưu.
- Chưa xác minh live CI mới nhất, chưa chạy test ứng dụng hoặc benchmark. Các test schema không chứng minh AI giải thích đúng.

### Patch bàn giao

File week4-reconcile.patch trong outputs/week4-review-20261009 của project gốc chứa toàn bộ thay đổi tài liệu của checkout tích hợp so với 33fefdd. Patch chỉ dành cho checkout sạch ở base đó (hoặc base đã kiểm tra tương thích), không áp mù vào main 6560cb1 đang có sửa local. Đã kiểm tra khả năng áp patch vào index riêng ở base, không thay index main và không tạo commit.


### Cập nhật bàn giao theo yêu cầu người dùng ngày 09/10

Người dùng yêu cầu push bản tích hợp. Bản này được chuẩn bị trên nhánh codex/week4-reconcile, dựa trên nhánh Toản tại 33fefdd. Fetch trước khi bàn giao xác nhận origin/main vẫn ở 6560cb1 và origin/docs/toan-week-4 vẫn ở 33fefdd. Phạm vi gồm 12 tài liệu căn chỉnh; không thay source ứng dụng. Việc đưa nhánh lên GitHub không đồng nghĩa merge main, approve PR hoặc chuyển các đề xuất thiết kế thành Accepted.
