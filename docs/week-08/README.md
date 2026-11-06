# Tuần 8 — Trần Văn Tưởng (23110170) & Trần Quang Toản (23110158)

Tuần 01–07/11/2026; thực hiện 05–06/11/2026. Hợp nhất nhánh `feat/toan-week-8` (`a9fdfc7`, PR #9) của Toản và nhánh `codex/tuong-week8` của Tưởng.

| Tài liệu | Mục đích |
|---|---|
| [Đối chiếu plan](assignment-check.md) | Task 21–28 trong bảng Kế hoạch chi tiết |
| [Báo cáo nhóm tuần 8](bao-cao-tuan-08-nhom.md) | Tổng hợp kết quả tuần 8 của cả nhóm (19 Hoàn thành / 9 Đang làm / 11 Chưa bắt đầu) |
| [Nội dung nộp web tuần 8](noi-dung-nop-web-tuan-08.md) | Văn bản phẳng `- [STT ...]` để dán thẳng vào form báo cáo tuần trên web khoa |
| [Adapter LLM Provider & Rà soát tích hợp (Tưởng)](provider-and-review-tuong.md) | Cài đặt `HttpLlmProviderAdapter`, 4 ca test `AI-T09`..`AI-T12`, và phản hồi 7 điểm tích hợp |
| [Báo cáo cá nhân (Toản)](bao-cao-tuan-08-toan.md) | Kết quả dashboard React, AI API, Docker/CI và kiểm chứng trên PostgreSQL |
| [API và tích hợp AI](api-and-ai-contract.md) | Preview, consent, lỗi, provider seam |
| [Chạy và đóng gói](runbook.md) | PostgreSQL, Lizard, Docker Compose, cờ cấu hình |
| [Minh chứng](evidence/verification.md) | Test, dữ liệu repo công khai, ảnh giao diện |
| [AI usage log (Toản)](ai-usage-toan-week08.md) | Nhật ký sử dụng AI tuần 8 của Toản |
| [AI usage log (Tưởng)](../ai-usage/tuong-week08.md) | Nhật ký sử dụng AI tuần 8 của Tưởng |
| [Bàn giao Tưởng](ban-giao-tuong.md) | Các điểm cần review, provider và staging |
| [Mô tả PR tuần 8 (Toản)](noi-dung-pr.md) | Nội dung PR #9 của Toản |
| [Mô tả PR tuần 8 (Tưởng)](noi-dung-pr-tuong.md) | Nội dung PR tuần 8 của Tưởng (`codex/tuong-week8` → `main`) |

Đã có dashboard dữ liệu thật, REST AI preview/review, lưu audit metadata (`V4`), adapter HTTP LLM provider thật (`HttpLlmProviderAdapter`), Docker frontend/backend có Lizard 1.24.3 và kiểm thử tích hợp.

**Kiểm chứng:**
- Trên máy Tưởng (không cần PostgreSQL): **45/45 ca kiểm thử backend đạt** (gồm 12/12 ca `LlmReviewServiceTests` `AI-T01`..`AI-T12`) + **8/8 ca kiểm thử công cụ thực nghiệm đạt**.
- Trên môi trường có PostgreSQL 18.3 của Toản & GitHub Actions CI (`main` `a9fdfc7`, run `42207922177`): **111 kiểm thử backend có PostgreSQL đạt** (tổng cộng toàn dự án **115 kiểm thử backend duy nhất**), **11 UI tests đạt** (gồm 2 ca live), 5/5 job CI đạt (gồm `docker compose` smoke). Ba repository công khai được phân tích và lưu PostgreSQL thành công.

**Còn mở:** Môi trường staging cũ từ tuần 3 (`410`/`404`) chưa được triển khai lại với cấu hình V4 + Lizard + persistent Git cache; khóa API thương mại mặc định tắt cho tới khi có authorization. Hai đầu việc dùng chung **STT 27** và **STT 28** được giữ ở trạng thái **Đang làm**.
