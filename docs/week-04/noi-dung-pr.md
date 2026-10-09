## Nội dung

Hoàn thiện phần phân tích tuần 4 của Toản theo phân công, dựa trên main `6560cb1`, năm phỏng vấn và metric-spec-v1 đã merge. Bổ sung 11 UC/25 acceptance criteria, mapping mã draft → đề cương, business rules/NFR có cách đo và API contract phân biệt route hiện có với thiết kế tương lai.

Cập nhật phạm vi AI review có context budget/schema/timeout/fallback, hai ADR full clone và TaskExecutor + PostgreSQL không Redis; kèm báo cáo tuần và AI log. Không cài pipeline hoặc đổi công thức metric v1. Kèm bản vá dependency gián tiếp source-map-js 1.2.1 → 1.2.2 trong lockfile vì audit CI phát hiện GHSA-68fv-2mgg-jv7q; không tắt audit hoặc thay đổi package.json.

## Kiểm chứng

- JSON Schema Draft 2020-12 hợp lệ; sample hợp lệ; 7 negative schema fixtures và 3 semantic reference cases được kiểm tra local.
- 11 UC/25 AC duy nhất; link local và conflict marker check đạt; git diff --check đạt.
- Không nhận đã benchmark NFR, gọi provider hoặc đạt các ca runtime chưa cài.

## Review chung

- Tưởng phụ trách architecture.md, chưa có ở main khi pull. Hai ADR là đề xuất chi tiết để review chung.
- Cần chốt BR14 lọc changeset lớn, BR16 giới hạn commit/window và BR17 config đổi khi HEAD không đổi; giữ coupling shared/min đã chốt.
- Budget AI/quota/timeout và lease/fencing cần review trước code; provider/model chưa chọn.
- Không thay lịch ERD/wireframe tuần 5 hoặc ghi báo cáo đã nộp/GVHD đã xác nhận.

Bắt đầu đọc tại `docs/week-04/README.md`. AI log ghi rõ phần AI hỗ trợ và phần sinh viên chưa xác nhận tự review tuần 4.
