# Nội dung tìm hiểu và áp dụng — tuần 9

Tài liệu do AI hỗ trợ tổng hợp ngày 12/11/2026; đây là nội dung để Toản nghiên cứu/đối chiếu, không xác nhận đã đọc hoặc đã thành thạo.

| Chủ đề | Nguồn gốc | Áp dụng và lý do |
|---|---|---|
| Reachability, first-parent, giới hạn lịch sử | [Git rev-list](https://git-scm.com/docs/git-rev-list) | Chọn SHA trên nhánh lịch sử chính; kiểm tra toàn bộ tổ tiên của snapshot. Giới hạn theo ngày không thay thế việc cố định cây mã nguồn |
| Commit date và định dạng log | [Git log](https://git-scm.com/docs/git-log) | Dùng `%ct` cho committer timestamp, cửa sổ UTC; author date có ý nghĩa khác và không dùng lẫn |
| Cyclomatic complexity và Lizard | [Kho chính thức Lizard](https://github.com/terryyin/lizard) | Dùng Lizard 1.24.3 từ module sẵn có để đo các hàm Java tại đúng SHA; complexity là chỉ số cấu trúc, không là nhãn bug |
| Hợp đồng thực nghiệm của nhóm | [Protocol tuần 6](../week-06/experiment-protocol.md), [thiết kế thực nghiệm](../experiment-design.md) | Giữ provenance, temporal split và phân biệt nhãn thực với candidate; RQ2 pilot không quyết định thay RQ1 |

## Những điểm cần giải thích được

**Snapshot lịch sử:** cutoff là ranh giới thời gian; SHA xác định cây file. Nếu lấy frequency quá khứ nhưng complexity từ HEAD mới thì dùng thông tin chưa tồn tại ở thời điểm đánh giá. CLI tuần 9 đo cả hai ở cây đúng SHA, đồng thời từ chối commit tổ tiên có timestamp vượt cutoff.

**Lineage:** đường dẫn là tên có thể thay đổi. Đổi tên file nên giữ danh tính; xóa file rồi tạo file khác cùng tên không nên bị ghép. Merge có thể làm nguồn gốc chưa rõ; kết quả hiện tại giữ null và chặn kết luận thay vì đoán.

**Phân vị và quần thể:** cùng frequency/CCN nhưng vị trí tương đối có thể đổi vì các file khác đổi. Do đó delta hotspot không trực tiếp là delta số lỗi. Luôn đọc các số đo gốc, số file có điểm và window cạnh điểm tổng hợp.

**Tái lập:** manifest giữ đầu vào; hash kiểm tra dữ liệu không đổi giữa hai lần chạy trong cùng môi trường. Khớp hash không chứng minh mô hình đúng ngoài thực tế. Chuyển phiên bản Lizard/JGit/metric có thể đổi kết quả và phải được ghi thành cấu hình khác.

**Đánh giá có giới hạn:** Git fixture kiểm chứng được một hành vi đã biết. Repository thật kiểm chứng khả năng vận hành và chất lượng dữ liệu. Chỉ khi có tình huống/nhãn được đánh giá độc lập mới được tính độ chính xác phát hiện hoặc hiệu quả hỗ trợ người dùng.
