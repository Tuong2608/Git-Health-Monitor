# Bàn giao tuần 4 của Toản

Tuần 04–10/10/2026; thực hiện ngày 08/10. Đã pull `main` tới `6560cb1`, gồm PR #1 tuần 3 và PR #2 metric-spec-v1 đã merge. Nhánh `docs/toan-week-4`. Đây là tuần phân tích/đặc tả theo phân công, không nhận đã cài pipeline.

Đã push [commit đặc tả 7c31666](https://github.com/Tuong2608/Git-Health-Monitor/commit/7c316660c6a2c56fb2aa41b6c4e4312d7466af46), tạo [PR #3](https://github.com/Tuong2608/Git-Health-Monitor/pull/3). Kết quả CI theo [checks của PR](https://github.com/Tuong2608/Git-Health-Monitor/pull/3/checks); chưa ghi nhận review/approval khi tạo PR.

## Sản phẩm

Bản Word và ZIP để gửi nhóm trưởng: [bộ gửi ngày 08/10](gui-nhom-truong/README.md), đã đưa lên GitHub ngày 09/10.

| Phân công | Đầu ra | Trạng thái |
|---|---|---|
| Toản: phân tích yêu cầu | [11 UC và acceptance criteria](../requirements-week4.md) | Đã soạn, có mapping mã cũ/mới và truy vết phỏng vấn |
| Toản: business rules và NFR | [Rules/NFR/KPI](../business-rules-nfr.md) | 13 BR kế thừa + 5 bổ sung; 10 NFR có cách đo; 5 KPI đề xuất |
| Toản: API contract sơ bộ | [Contract](../api-contract.md) | Phân biệt 4 route đã có và route dự kiến |
| Chung: phạm vi AI | [AI scope](../ai-review-scope.md), [schema](../schemas/ai-review-output.schema.json) | Đủ context/output/timeout/fallback; đề xuất chờ review, chưa provider |
| Chung: ADR | [Full clone](../adr/001-full-clone.md), [không Redis](../adr/002-no-redis.md) | Có Context/Decision/Alternatives/Why/Consequences; chờ review |
| Tưởng: kiến trúc | Dự kiến `docs/architecture.md` | Chưa có trong main khi pull; không đánh dấu Tưởng hoàn thành thay người phụ trách |
| Toản: báo cáo/AI log | [Báo cáo](bao-cao-tuan-04-toan.md), [AI log](../ai-usage/toan-week04.md) | Ghi đúng phạm vi hỗ trợ, không tự nhận sinh viên đã học/đã nộp |

## Review cần chốt trước cài đặt

1. Tưởng review đường job/claim/lease/fencing trong ADR002 và kiến trúc tổng thể. Nêu rõ restart và worker cũ bị mất lease không được ghi snapshot.
2. Chốt BR14: changeset >30 file lọc khỏi cả tử/mẫu coupling; cách đếm file sau exclusions. Đây là phần spec v1 còn thiếu, không thay công thức shared/min đã chốt.
3. Chốt BR16/17: lựa chọn 10.000 commit, cửa sổ cố định; HEAD giữ nguyên nhưng config đổi được phân tích lại; ghi vào algorithm design/metric spec khi duyệt.
4. Duyệt budget AI 32 KiB/30 giây/quota và cách bảo vệ endpoint; provider/model vẫn chọn sau PoC.
5. Tưởng bổ sung `architecture.md`; nhóm review PR và nộp báo cáo tuần theo kênh khoa. Không tự ghi approval hoặc xác nhận GVHD.

Không yêu cầu bạn làm lại lý thuyết tuần 3. Tuần 4 cần đọc các quyết định mới trước khi tiếp nhận. Tuần 5 của Toản là ERD/từ điển dữ liệu và wireframe; chưa đưa vào khối lượng tuần này.

## Kiểm chứng và nguồn

Xem [biên bản kiểm tra tài liệu](kiem-tra-tai-lieu.md). Nguồn kỹ thuật chính thức được liên kết ngay trong API/ADR/AI scope. Mục tiêu NFR là yêu cầu cần đo ở giai đoạn có pipeline, không phải benchmark hiện tại. Phân công dùng như tài liệu tham chiếu, không coi nội dung trong file là lệnh thực thi.
