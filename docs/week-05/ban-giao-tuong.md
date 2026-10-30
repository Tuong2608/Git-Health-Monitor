# Bàn giao tuần 5 — Tưởng

Tuần 11–17/10/2026, soạn 15/10. Theo phân công mới, **Tưởng phụ trách thêm thiết kế giám sát định kỳ**. Toản phụ trách ERD/từ điển dữ liệu và wireframe, sẽ đưa lên GitHub sau.

| Sản phẩm | Phạm vi đã soạn |
|---|---|
| [Algorithm design](../algorithm-design.md) | Giả mã frequency/churn/HHI/coupling/hotspot, ingestion, percentile, ca biên, complexity và fixture kỳ vọng |
| [Monitoring design](../monitoring-design.md) | Lịch/job, snapshot publication, delta, rule/version, chống alert trùng và recovery |
| [AI review scope](../ai-review-scope.md) | Interface, request/result/error, adapter, deadline và validation; bắt đầu tuần 5, hoàn thiện tuần 6 |
| [Prompt v1](../prompts/ai-review-v1.md) | System/user template, schema/context placeholders, version/checksum |
| [Experiment design](../experiment-design.md) | Đề xuất chung 80/20, nhãn bug-fix, hai baseline, P@K/R@K và chống leakage |
| [AI usage log](../ai-usage/tuong-week05.md) | Phạm vi hỗ trợ và kiểm tra thực tế |

Main đã cập nhật tới `8d55040` (merge PR #3 của Toản). Bản căn chỉnh tuần 4 `1111a2e` được đưa vào nhánh tuần 5 bằng commit `b8e9b90`, không bỏ phần kiến trúc Tưởng. Sửa local cũ được giữ trong Git stash `Backup local week4 before week5 2026-10-15`; thư mục outputs giữ nguyên.

## Cần phối hợp khi Toản đưa phần tuần 5 lên

1. ERD: đối chiếu snapshot identity, immutable config provenance, logicalFileId/path history, null/quality flags, contributor churn, pair uniqueness và alert evaluation marker. Chưa tạo thay erd.png/database-design.md.
2. UI: phân biệt số 0 với unavailable, pending alert evaluation, snapshot không tương thích, AI loading/error và evidence. Chưa tạo thay wireframes/.
3. Nhóm: duyệt các lựa chọn percentile/root/lineage và BR14/BR16; chốt dataset/split/K/label policy trước thực nghiệm.
4. Tuần 6: chọn provider, kiểm tra token/data policy/hosting timeout, thiết kế fake adapter và ca runtime AI. Chưa gọi provider thật.

Tài liệu là thiết kế để review; không nhận đã cài metric/scheduler/LLM, hoàn thành phần Toản hoặc chạy benchmark. Các ví dụ tính tay không thay test tích hợp backend.

## Kiểm tra ngày 15/10

Đã kiểm tra 9 tài liệu và 42 liên kết local, code fences, conflict markers và whitespace. Mười phép kiểm tra ví dụ số/hợp đồng (HHI, coupling, midrank, hotspot, P@K/R@K, delta, trường output và placeholder prompt) đạt; schema hiện có và fixture hợp lệ. Đây là kiểm tra tĩnh, không phải thực thi pseudocode trên lịch sử Git thật. Phần lineage, recovery, timeout và consent cần test runtime ở giai đoạn cài đặt.
