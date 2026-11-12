# Kiểm chứng tuần 9

Ngày 12/11/2026, Windows, Java 21, PostgreSQL 18 local port 55432, Lizard 1.24.3. Database riêng `ghm_week9_test`; dữ liệu OSS phân tích bằng CLI, không cần database.

| Hạng mục | Kết quả | Nguồn |
|---|---|---|
| Backend full verify | 118 ca, 0 lỗi/skip | Chạy sau exporter ban đầu có 3 test |
| HistoricalSnapshotExportTests sau bổ sung fixture | 4 ca, 0 lỗi/skip | Bao gồm 3 ca trên và 1 ca mới; tổng duy nhất 119 |
| Danh sách backend | 119 ca duy nhất | [backend-tests.json](backend-tests.json), trích từ XML Surefire |
| Python | 17 ca, 0 lỗi | 8 xếp hạng cũ + 9 RQ2 |
| Pilot Git/Lizard | 9/9 snapshot thành công | [run-summary](pilot/run-summary.json) |
| So sánh chính thức | 0/6 đủ điều kiện; 6 BLOCKED | [kết quả](pilot/results.md) |
| Tái lập | 15/15 file khớp SHA-256 | [replay-verification](replay-verification.json) |

Các ca mới kiểm tra: complexity đúng cây quá khứ dù HEAD đã đổi, tổ tiên tương lai, biên cutoff exclusive, giới hạn commit, từ chối branch di động; fixture tăng frequency/CCN tạo hotspot tăng 0→1 với coverage đầy đủ. Python kiểm tra biên ngưỡng, rename/reuse đường dẫn, null/zero, cấu hình/version/window/coverage, sai thứ tự/cross-repo, số không hợp lệ và evidence ngoài cửa sổ.

Không có sửa frontend tuần 9, không lấy UI test tuần 8 làm lượt chạy mới. CI của PR sẽ chạy các job hiện có gồm backend, frontend, experiments, secret-scan và package; trạng thái đúng commit đóng gói được lưu ngoài repo trong CI_STATUS.json của ZIP. Không khẳng định CI đã đạt khi chỉ có kết quả local.

Không gọi provider AI thương mại, không triển khai staging trong lượt thực nghiệm này. Không dùng nội dung commit làm nhãn bug tự động. Chỉ công bố số liệu quan sát được; các điểm thiếu được ghi ngay trong báo cáo.

`.gitattributes` giữ nguyên byte JSON của manifest/dữ liệu để Git không đổi xuống dòng và làm sai hash khi checkout/đóng ZIP. Replay 15/15 được xác nhận trong cùng môi trường Windows/Java/Lizard; chưa khẳng định byte output giống nhau giữa các hệ điều hành. Trên môi trường khác cần so nội dung JSON/config và ghi riêng khác biệt định dạng.
