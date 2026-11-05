# Nhật ký sử dụng AI — tuần 8

Trần Quang Toản, 23110158. Ngày 05/11/2026. Công cụ: Codex. Ghi yêu cầu theo mục tiêu học thuật, không chép prompt gốc; không khai model hoặc giờ công chưa xác minh.

## AI-08-01 — Đối chiếu kế hoạch và tích hợp mã nhóm

**Mục tiêu:** xác định task 24/26/27/28, tách trách nhiệm với Tưởng. **Đầu vào:** workbook và main 1878d2b, tài liệu bàn giao tuần 7. **Hỗ trợ:** fetch, đọc và trích nguồn phân công. **Kiểm chứng:** SHA và dòng nguồn lưu trong assignment-source.json. **Giới hạn:** không tự ghi quyết định nhóm hoặc nghiệm thu.

## AI-08-02 — Giao diện dữ liệu phân tích

**Mục tiêu:** trình bày snapshot/metric/evidence để người dùng kiểm chứng. **Hỗ trợ:** React dashboard, API client, pagination, trạng thái loading/error/empty, huỷ response cũ, monitoring/alerts và AI consent. **Điều chỉnh:** ngắt trend khi không tương thích; giữ null khác 0; bảng mobile có cuộn ngang; AI output chia theo từng mục. **Kiểm chứng:** lint/build và 11 UI tests, gồm 2 ca gọi backend thật. **Lỗi phát hiện:** bộ chọn test trùng khi có nhiều repository, đã đổi sang tên cụ thể. **Giới hạn:** các ca browser fixture không được gọi là người dùng thật hoặc benchmark usability.

## AI-08-03 — API AI và truy vết

**Mục tiêu:** nối engine của Tưởng với HTTP/UI mà giữ consent và SHA snapshot. **Hỗ trợ:** context builder từ DB/Git blob, hash preview, executor/deadline, error mapping, V4 audit metadata. **Điều chỉnh:** parser từ chối trailing JSON; provider không chịu interrupt chưa được giải phóng slot sớm. **Kiểm chứng:** success/invalid output/timeout bằng adapter chỉ trong test; public check, no consent, wrong hash, secret, moved HEAD, disabled/no provider. **Giới hạn:** chưa gọi provider thương mại, chưa xác nhận data-retention/model budget hoặc authorization production.

## AI-08-04 — Tích hợp pipeline và môi trường

**Mục tiêu:** chứng minh Git→metric→PostgreSQL→API hoạt động. **Hỗ trợ:** test local Git dùng JGit/Lizard thật, Java/Python setup, đưa Lizard vào Docker/CI, ghi tool/version/aliases trong config. **Kiểm chứng:** 109 tests full verify + 2 availability tests riêng đạt; ba repo công khai lưu snapshot thành công. **Lỗi môi trường:** lần đầu bị hạn chế socket/Git temp; PostgreSQL cần cổng 55432 và một tiến trình kiểm tra Docker bị treo. Đã sửa môi trường và chạy lại, không tính các lần lỗi là đạt. **Giới hạn:** số đo thời gian một lượt có polling không phải thực nghiệm hiệu năng; coverage ambiguity giữ nguyên.

## AI-08-05 — Báo cáo, bảo trì và bàn giao

**Mục tiêu:** truy vết từ báo cáo tới mã/test/kết quả thật. **Hỗ trợ:** tổng hợp Surefire XML, HTTP exchanges, ảnh desktop/mobile, audit và kiểm tra staging chỉ đọc, PR/ZIP. **Kiểm chứng:** test names/counters, ảnh đã rà; staging frontend 410, capabilities backend 404. **Giới hạn:** 10 mục audit moderate của tooling còn mở, không force major chỉ để xoá báo cáo. Không tự ghi đã nộp web khoa, được GVHD duyệt hoặc có đồng thuận thực nghiệm. Toản chịu trách nhiệm đọc, giải thích và xác nhận tài liệu trước nộp; Tưởng review các điểm tích hợp.
