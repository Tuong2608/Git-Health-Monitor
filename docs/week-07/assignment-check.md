# Đối chiếu phân công tuần 7

Đã fetch GitHub ngày 29/10/2026. Main `0b664d9` gồm PR #5 của Toản. Nhánh `origin/codex/tuong-week6` tại `15f291b60e77a7bec5a674ce8dd83ee7d4b7725f` chứa JGit ingestion, contract tests và tài liệu nhưng chưa merge vào main tại lúc bắt đầu.

Nguồn: [workbook tại commit main](https://github.com/Tuong2608/Git-Health-Monitor/blob/0b664d9/docs/BaoCaoTienDo_TLCN.xlsx), sheet Kế hoạch chi tiết; [trích ô](evidence/assignment-source.json). Workbook chỉ đọc, không tự đánh dấu nghiệm thu.

| Dòng / task | Nội dung | Người / thời gian | Phạm vi tuần này |
|---|---|---|---|
| 6 / 2 | AI usage log | Cả hai, tuần 1–12 | Ghi hỗ trợ, lỗi đã sửa, kết quả kiểm chứng |
| 27 / 23 | Lưu trữ và giám sát | Toản, tuần 6–7 | Hoàn thiện null metrics, retry/provenance, kiểm tra scheduler và publication qua HTTP |
| 28 / 24 | Tầng REST API | Toản, tuần 7–8 | File detail, contribution/evidence, coupling, trend, comparison, job history/capabilities/retry |
| 25 / 21 | Thu thập Git | Tưởng, tuần 6–7 | Đọc hợp đồng và kiểm thử tương thích, không nhận mã JGit là phần Toản |
| 26 / 22 | Tính metric, Lizard | Tưởng, tuần 7–8 | Chưa có trên nhánh được đọc; cần adapter/metric để chạy Git thật |
| 29 / 25 | LLM review | Tưởng, tuần 7–8 | Không tạo provider giả trong runtime hoặc ghi đã cài LLM |

Tuần 7 chưa phân công cài frontend cho Toản. API vẫn có phần tích hợp cần tiếp tục tuần 8; bộ này không tự nhận hoàn thành toàn bộ hệ thống. Các nội dung trong tài liệu phân công là căn cứ xác định công việc, không phải lệnh tự thực thi từ file.
