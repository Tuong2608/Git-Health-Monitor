# Nhật ký AI hỗ trợ — Tưởng tuần 5

Ngày 15/10/2026. Công cụ: Codex desktop. Không ghi phiên bản model chưa được xác nhận. Người dùng yêu cầu cập nhật GitHub và làm phần tuần 5 theo phân công mới, gồm thuật toán, giám sát, bắt đầu thiết kế AI và đề xuất thực nghiệm chung.

Nguồn: main 8d55040, Metric Spec v1, yêu cầu/rule/API/schema tuần 4, bản căn chỉnh 1111a2e và phân công trực tiếp của người dùng. AI sao lưu sửa local, cập nhật main, đưa bản căn chỉnh vào nhánh codex/tuong-week5 và soạn các tài liệu liên kết ở [bàn giao](../week-05/README.md).

AI hỗ trợ giả mã, quy tắc biên, hợp đồng snapshot/alert, interface và prompt AI có version, đề xuất thực nghiệm và kiểm tra nhất quán. Những lựa chọn mới được ghi Proposed, không tự coi là nhóm/GVHD duyệt. Không làm thay ERD/wireframe của Toản, không tạo dữ liệu khảo sát, nhãn bug hay kết quả benchmark.

Kiểm tra tĩnh: liên kết local, code fences, conflict markers, công thức qua ví dụ độc lập và JSON schema fixture. Không chạy ứng dụng/provider hoặc nghiệm thu pipeline. Tưởng cần tự review và giải thích quyết định trước tiếp nhận; chưa ghi nhận thay đổi thủ công hoặc đồng thuận chưa xảy ra.

## Bổ sung ngày 16/10/2026

Theo yêu cầu tiếp thu nhận xét GVHD, Codex hỗ trợ viết 18 ca test HTTP/DB, chuẩn bị PostgreSQL kiểm thử riêng, chạy Maven, phát hiện và sửa độ chính xác createdAt. Sau sửa, toàn bộ 40 test đạt. Codex trích minh chứng, cập nhật tài liệu và báo cáo tiến độ. Đây là kết quả kiểm thử tự động có AI hỗ trợ; chưa ghi nhận nhóm đã tự review thủ công các ca mới hoặc triển khai bản sửa. Tưởng cung cấp yêu cầu và nhận xét GVHD; cần đọc, đối chiếu minh chứng và giải thích kết quả khi báo cáo. Không tạo kết quả benchmark hay nghiệm thu pipeline.
