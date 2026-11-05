# Tuần 8 — Trần Quang Toản (23110158)

Tuần 01–07/11/2026; thực hiện 05/11/2026. Nhánh `feat/toan-week-8`, nền `main` `1878d2b` đã merge phần tuần 7 của cả hai thành viên.

| Tài liệu | Mục đích |
|---|---|
| [Đối chiếu plan](assignment-check.md) | Task 24, 26 và phần chung 27–28 |
| [Báo cáo cá nhân](bao-cao-tuan-08-toan.md) | Kết quả, kiểm chứng, giới hạn |
| [API và tích hợp AI](api-and-ai-contract.md) | Preview, consent, lỗi, provider seam |
| [Chạy và đóng gói](runbook.md) | PostgreSQL, Lizard, Docker Compose, cờ cấu hình |
| [Minh chứng](evidence/verification.md) | Test, dữ liệu repo công khai, ảnh giao diện |
| [AI usage log](ai-usage-toan-week08.md) | Tách theo hạng mục, không chép prompt gốc |
| [Bàn giao Tưởng](ban-giao-tuong.md) | Các điểm cần review, provider và staging |
| [Nội dung báo cáo web](noi-dung-nop-web-tuan-08.md) | Bản tóm tắt để Toản kiểm tra trước khi nộp |

Đã có dashboard dữ liệu thật, REST AI preview/review, lưu audit metadata, Docker frontend/backend có Lizard và kiểm thử tích hợp. Engine Git/metric/LLM của Tưởng được tái sử dụng, không tính là mã Toản tự viết.

**Kiểm chứng local:** 111 kiểm thử backend duy nhất đạt (109 full verify + 2 kiểm thử availability bổ sung); 11 UI tests đạt, gồm 2 ca live. Ba repository công khai được phân tích và lưu PostgreSQL thành công. Bộ test AI sử dụng provider giả lập chỉ trong test; chưa gọi provider thương mại.

**Còn mở:** cấu hình provider thật do Tưởng phụ trách; staging cũ không đủ dùng; Docker local không phản hồi nên kết quả container dựa vào CI khi hoàn tất. Audit frontend có 10 mục moderate cùng chuỗi dependency minimatch của tooling; không có high/critical, chưa ép đổi major. Đây chưa phải nghiệm thu toàn bộ M4 hoặc báo cáo thực nghiệm RQ1–RQ3.
