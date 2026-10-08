# Nhật ký sử dụng AI — tuần 4

Sinh viên: Trần Quang Toản, 23110158. Tuần 04–10/10/2026, ngày hỗ trợ 08/10. Công cụ: Codex; không tự ghi phiên bản mô hình. Base `6560cb1`, nhánh `docs/toan-week-4`. Các mục diễn giải mục tiêu chuyên môn, không trích prompt gốc.

## W4-AI-01 — Đối chiếu đầu vào

**Mục tiêu:** Làm đúng phần phân công tuần 4. **Input:** phân công, đề cương trích xuất, draft UC, năm phỏng vấn, metric-spec-v1 và mã hiện tại. **AI hỗ trợ:** pull main, đọc và phân biệt baseline đã merge với đề xuất. **Kết quả:** PR tuần 3/metric spec đã merge; không tiếp tục coi coupling là tranh chấp chưa giải quyết. **Kiểm chứng:** Git history/base và đối chiếu công thức shared/min, max CCN, target 10 phút. **Giới hạn:** không tự xác nhận kiến trúc của Tưởng hoặc review nhóm.

## W4-AI-02 — Đặc tả yêu cầu và truy vết

**Mục tiêu:** Chuyển nhu cầu thành tiêu chí kiểm tra. **Input:** UC draft/đề cương và N01–N08. **Output:** requirements-week4.md, mapping UC, actor/input/output/luồng/ngoại lệ/AC. **Kiểm chứng:** đủ 11 UC; giữ ý kiến hoài nghi S05; không biến cảnh báo PR/multi-repo thành core. **Sinh viên:** chưa có ghi nhận đọc/hiệu chỉnh độc lập tuần 4.

## W4-AI-03 — Business rules/NFR/KPI

**Mục tiêu:** Yêu cầu rõ, định lượng và đo được. **Output:** business-rules-nfr.md. **Kiểm chứng:** BR01–13/NFR01–10 giữ mã đề cương; target 10 phút, không tự thay 5 phút. Budget/BR mới và KPI đều được đánh dấu đề xuất/chưa đo. **Giới hạn:** chưa có pipeline để benchmark, không tuyên bố đạt coverage/SUS/hiệu quả.

## W4-AI-04 — API contract

**Mục tiêu:** Bàn giao giao diện giữa frontend/API/worker. **Input:** controller tuần 3 và yêu cầu mới. **Output:** api-contract.md. **Kiểm chứng:** GET repositories giữ mảng hiện có; route tương lai ghi dự kiến; có job states, errors, window/config và snapshot consistency. **Giới hạn:** không bổ sung runtime endpoint trong tuần phân tích.

## W4-AI-05 — Phạm vi AI và JSON Schema

**Mục tiêu:** Ràng buộc context/output/fallback. **Output:** ai-review-scope.md và schema/fixture. **Kiểm chứng:** JSON/schema validation, sample hợp lệ và negative cases; semantic evidence được yêu cầu riêng vì schema không tự xác minh dữ liệu thật. **Giới hạn:** không gọi provider, không có test timeout/security runtime; budget là đề xuất.

## W4-AI-06 — ADR và hồ sơ bàn giao

**Mục tiêu:** Ghi lý do lựa chọn full clone/no Redis. **Nguồn:** Git clone docs, Spring scheduling, PostgreSQL SELECT, RFC 9457 và JSON Schema; link trong artifact. **Output:** ADR001/002, báo cáo, checklist review. **Kiểm chứng:** có Context/Decision/Alternatives/Why/Consequences; không đồng nhất @Async với durable queue, không hứa exactly-once execution. **Giới hạn:** Tưởng phụ trách architecture.md; chưa có khi pull, không nhận đã hoàn tất thay Tưởng.

## Kiểm soát và truy vết

Kiểm tra kỹ thuật do AI chạy, chi tiết ở [biên bản](../week-04/kiem-tra-tai-lieu.md). Khi commit, tra `git log -- docs/requirements-week4.md docs/ai-usage/toan-week04.md` trên nhánh/PR để lấy SHA thật, không tự tạo hash trong báo cáo. Không backdate commit hay nhận AI sửa lỗi là sinh viên phát hiện. Chưa có review độc lập/approval hoặc xác nhận nộp tuần 4.

Toản cần review những quyết định mới trước tiếp nhận; nếu sửa/loại đề xuất, ghi file, lý do và commit tương ứng. Không tự điền lời giải thích hoặc thời gian học thay sinh viên.
