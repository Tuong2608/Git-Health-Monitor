# Báo cáo tiến độ tuần 4 — Trần Quang Toản

MSSV 23110158. Đề tài: Git Health Monitor. Tuần 04–10/10/2026; cập nhật 08/10/2026. Phân công: Toản dẫn dắt yêu cầu/API/CSDL/giao diện; Tưởng dẫn dắt kiến trúc/thuật toán. **Bản soạn để nộp sau review; chưa có xác nhận GVHD.**

## Mục tiêu và đầu vào

Chuyển use case draft và năm phỏng vấn thành yêu cầu có thể nghiệm thu; xác định business rules/NFR, API contract, phạm vi AI và ADR chung. Đã cập nhật repository tới `6560cb1`; giữ metric-spec-v1 đã được nhóm merge thay các công thức cũ chưa thống nhất.

## Kết quả

- Đặc tả 11 UC theo mã đề cương, mỗi UC có actor, input/output, điều kiện, luồng và ngoại lệ; 25 AC kiểm chứng được. Có bảng ánh xạ draft tuần 2 và liên hệ N01–N08 từ phỏng vấn.
- Giữ BR01–13 và NFR01–10 theo đề cương, thêm đề xuất BR14–18 cho coupling filtering, chống job trùng, giới hạn commit, config/snapshot và alert idempotency; ghi rõ chờ nhóm duyệt.
- Giữ target NFR03 <=10 phút, không đổi thành ví dụ 5 phút. Mỗi NFR có cách đo và trạng thái chưa nghiệm thu; bổ sung 5 KPI mục tiêu để chuẩn bị thực nghiệm.
- Contract phân biệt các endpoint hiện có với thiết kế mới cho job/snapshot/hotspot/coupling/monitoring/alert/AI. Không viết route giả để nhận là cài xong.
- Hoàn thiện AI scope: dữ liệu chọn đúng SHA, giới hạn context, schema có evidence, timeout/quota, fallback và không tự sửa code.
- Soạn hai ADR full clone/fetch và TaskExecutor + PostgreSQL không Redis; nêu chi phí tài nguyên, durability, lease/fencing và hệ quả.

Đường dẫn đầy đủ tại [bộ bàn giao](README.md). Các kiểm tra thực tế ở [biên bản](kiem-tra-tai-lieu.md); không lấy test tuần 3 làm minh chứng code tuần 4 mới.

## Lý do điều chỉnh/làm rõ

Phát sinh CI: dependency gián tiếp source-map-js 1.2.1 bị audit báo high; đã cập nhật lockfile lên bản vá 1.2.2, npm audit báo 0 vulnerabilities. Không đổi chức năng ứng dụng, không tắt kiểm tra bảo mật. Ghi nhận chi tiết trong AI log và kiểm tra frontend sau bản vá.

Mã UC draft khác đề cương nên chọn mã đề cương và lưu mapping. BR06 cũ chỉ xét HEAD chưa đủ khi config thay đổi, vì vậy đề xuất ngoại lệ có version. Cần cố định window để tái lập metric. Spec v1 đã chốt shared/min và max function CCN nên tài liệu tuần 4 tuân theo, không dùng công thức hợp commit hoặc CCN trung bình cũ.

Không thay lịch phân công: ERD/wireframe vẫn tuần 5. Không tự cập nhật workbook thành “hoàn thành toàn bộ M2” vì kiến trúc/review nhóm còn thiếu minh chứng.

## Sử dụng AI và phần cá nhân

AI hỗ trợ đọc nguồn, soạn đặc tả, schema, ADR và kiểm tra nhất quán. [AI log tuần 4](../ai-usage/toan-week04.md) chia theo hoạt động, không chép prompt hội thoại. Xác nhận đã đọc lý thuyết tuần 3 không được tự chuyển thành xác nhận đã đọc/hiểu tài liệu tuần 4. Chưa ghi nhận Toản tự sửa hoặc phát hiện lỗi AI trong phiên này.

## Bàn giao và kế hoạch tiếp

Tưởng review ADR/contract và bổ sung kiến trúc. Nhóm thống nhất các quyết định BR14/16/17, budget AI và môi trường benchmark trước implementation. Toản tiếp tục tuần 5: ERD 10 bảng/từ điển dữ liệu và 4 wireframe, dựa trên contract đã review. Provider/benchmark/user study chưa có kết quả mới.

Đã push commit đặc tả `7c31666` và tạo [PR #3](https://github.com/Tuong2608/Git-Health-Monitor/pull/3); [CI/checks](https://github.com/Tuong2608/Git-Health-Monitor/pull/3/checks) phản ánh kết quả từng lần chạy. Review nhóm và xác nhận nộp: chưa có tại thời điểm soạn. Không điền ngày phỏng vấn, approval hoặc điểm số giả.
