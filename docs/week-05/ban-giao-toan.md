# Bàn giao tuần 5 — Trần Quang Toản

MSSV 23110158 · Tuần 11–17/10/2026 · Thực hiện ngày 15/10/2026.

Đã đối chiếu workbook trên GitHub: Toản phụ trách thiết kế database và giao diện trong tuần 5; thực nghiệm là việc chung tuần 5–6. Xem [nguồn phân công](assignment-check.md). Bộ này là **thiết kế đã kiểm tra để nhóm review**, chưa triển khai pipeline lên staging và chưa được nhóm trưởng/GVHD nghiệm thu.

## Mở theo thứ tự

| Sản phẩm | Tệp | Trạng thái |
|---|---|---|
| Báo cáo gửi nhóm trưởng/GVHD | [Báo cáo tuần 5](bao-cao-tuan-05-toan.md) | Đã soạn dựa trên kết quả thật |
| Database 10 bảng | [Từ điển và quyết định](database-design.md), [ERD xem trên GitHub](erd.md), [nguồn](erd.mmd), [ảnh tổng quan](erd.png), [SQL](schema-reference.sql) | Đã soạn; SQL kiểm tra PostgreSQL 18.3 |
| Giao diện và điều hướng | [Mở wireframe](wireframes/index.html), [hướng dẫn và mapping](wireframes/README.md) | Bốn màn hình offline; 18 kiểm tra đạt |
| Thực nghiệm chung | [Thiết kế thực nghiệm](experiment-design.md) | Đề xuất tuần 5–6, chờ nhóm chốt |
| Minh chứng | [Biên bản kiểm tra](evidence/verification.md) | Log SQL, JSON kiểm tra, ảnh desktop/mobile, nguồn phân công |
| AI học thuật | [Nhật ký AI theo hạng mục](ai-usage-toan-week05.md) | Tách mục tiêu/đầu vào/đầu ra/kiểm chứng/giới hạn, không chép prompt gốc |
| Review/bàn giao | [Checklist](review-checklist.md) | Các quyết định cần Tưởng phản hồi |

## Cách dùng

Mở `wireframes/index.html` bằng trình duyệt để demo. Trên GitHub, xem ảnh trong `evidence/` vì GitHub không chạy HTML trực tiếp. ERD Mermaid có thể copy vào Mermaid editor hoặc xem bằng công cụ hỗ trợ; ảnh PNG chỉ tóm tắt các bảng, SQL/từ điển là chi tiết ràng buộc.

SQL là tài liệu kiểm chứng, không đặt trong Flyway. Chỉ chạy `evidence/verify-schema.sql` trên DB thử nghiệm; script tạo schema riêng và rollback. Không chạy `schema-reference.sql` trực tiếp vào database staging.

Để kết thúc tuần theo quy trình nhóm: Toản đọc/tiếp nhận bộ thiết kế, Tưởng review các điểm mở, gắn bằng chứng review thật và cập nhật/nộp tiến độ qua kênh nhóm. Không tự đánh dấu có phản hồi hoặc đã nộp khi chưa xảy ra.
