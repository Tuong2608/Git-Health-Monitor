# Bàn giao tuần 9 cho Tưởng

Toản đã triển khai công cụ RQ2 và chạy 3 dự án × 3 mốc lịch sử. Mở [README](README.md), [báo cáo](bao-cao-tuan-09-toan.md), [kết quả](evidence/pilot/results.md), [runbook](runbook.md) để review. ZIP là bản mã và minh chứng; không chứa database, cache Git, API key hoặc môi trường Python.

## Những quyết định cần nhóm review

| Nội dung | Hiện trạng | Đề xuất |
|---|---|---|
| Dữ liệu chung STT 29 | 3 repo pilot đã pin; hai repo có tiếp xúc từ tuần 8 | Chấp nhận cho RQ2 pilot; quyết định riêng bộ RQ1 trước khi xem kết quả/nhãn |
| Lineage/coverage | 6/6 cặp bị chặn, commons-cli 45–47 file lineage chưa rõ | Review merge/NO_FUNCTIONS; viết fixture; nếu đổi policy phải version hóa và chạy mới |
| Mốc RQ2 | 3 mốc năm, cửa sổ 180 ngày | Chốt lịch dày hơn cho tuần 10 trước khi chạy; giữ pilot gốc |
| Kết luận nghiên cứu | Chưa có ground truth người review | Không công bố precision/recall hay hiệu quả giảm bug từ pilot |
| Staging tuần 8 | Chưa có URL mới theo thông tin gần nhất | Giữ STT 27/28 đang làm; kiểm thử staging/provider riêng khi có môi trường |

## Gợi ý cập nhật tiến độ

- STT 29: Đang làm — đã chọn và chạy pilot 3 dự án; cần chốt bộ nghiên cứu chung.
- STT 31: Đang làm — xong công cụ, manifest, pilot và replay tuần 9; còn đánh giá/giải thích tuần 10.
- STT 30: thuộc Tưởng, không thay kết quả RQ1 bằng các điểm mô tả RQ2.
- STT 32/33: tuần 10, chưa điền là hoàn thành.

Đã chuẩn bị nội dung bàn giao, chưa gửi tin nhắn/email và chưa tự merge PR. Review kỹ protocol và trạng thái BLOCKED trước khi đưa vào báo cáo nhóm.
