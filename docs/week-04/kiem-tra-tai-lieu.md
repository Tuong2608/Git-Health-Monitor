# Kiểm tra tài liệu tuần 4

Ngày 08/10/2026; thực hiện bởi AI. Phạm vi là đặc tả/JSON schema, không phải kiểm thử pipeline chưa có.

## Phương pháp

- Đối chiếu controller hiện tại và contract tuần 3 để tách route hiện hữu/dự kiến.
- Đối chiếu metric v1: shared/min, max function CCN, HHI theo additions+deletions, window mặc định 180 ngày, NFR03 10 phút.
- Kiểm tra JSON Schema Draft 2020-12 bằng jsonschema 4.25.1 và fixture: một mẫu hợp lệ, bảy biến thể sai cấu trúc (thiếu trường, trường lạ, evidence rỗng, quá độ dài, quá số checklist, id sai định dạng, reference trùng).
- Mô phỏng kiểm tra ngữ nghĩa: sample hợp lệ; từ chối id ngoài allowlist, reference không có, evidenceId lặp. Đây là minh họa quy tắc, không phải validator backend đã cài.
- Kiểm tra UC01–UC11, 25 AC duy nhất, liên kết tài liệu local và conflict markers; Git whitespace check.

## Giới hạn

Kết quả kiểm tra local ngày 08/10: schema hợp lệ; sample đạt; 7 mẫu sai schema bị từ chối; 3 mẫu reference/duplicate sai bị kiểm tra ngữ nghĩa minh họa từ chối; đủ 11 UC/25 AC duy nhất; không có link local hỏng hoặc conflict marker. `git diff --check` đạt. jsonschema được cài trong `.local/week4-tools`, không thêm dependency runtime backend/frontend.

Không benchmark hiệu năng, không gọi LLM, không chạy AI timeout/security runtime hoặc claim pipeline đã đạt AC. Schema/evidence reference hợp lệ không chứng minh lời giải thích AI đúng. Không tạo dữ liệu phỏng vấn mới hoặc xác nhận review thay người khác. Workflow GitHub hiện có được theo dõi qua PR; kết quả tuần 3 chỉ là lịch sử, không tự chuyển thành kết quả tuần 4.
