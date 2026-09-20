# Competitor Matrix — So sánh công cụ hiện có

> Trạng thái: đang cập nhật, dựa trên tìm hiểu ban đầu qua trang chủ/tài liệu công khai của từng công cụ. Sẽ bổ sung sau khi dùng thử trực tiếp (nếu có bản miễn phí/dùng thử).

| Công cụ | Loại chỉ số chính | Phân tích lịch sử Git (temporal) | Trực quan hóa | Mã nguồn mở | Có trợ lý AI review | Ghi chú / khoảng trống |
|---|---|---|---|---|---|---|
| **SonarQube** | Chất lượng code tại 1 thời điểm (bug, code smell, duplication, coverage) | Hạn chế — chủ yếu snapshot hiện tại, ít phân tích xu hướng theo lịch sử | Dashboard chi tiết | Có bản Community | Không (không có LLM review tích hợp sẵn) | Mạnh về chất lượng tĩnh, yếu về "ai đang sở hữu file nào", "file nào rủi ro dài hạn" |
| **CodeScene** | Hotspot, temporal coupling, "knowledge loss"/bus factor | Có, đây là điểm mạnh chính | Bản đồ hotspot trực quan | Không (thương mại) | Không | Gần với hướng đề tài nhất, nhưng đóng, khó tùy biến, chi phí cao cho SV |
| **Code Maat** | Change frequency, coupling, ownership (dạng CLI, output dữ liệu thô) | Có | Không có UI, cần công cụ khác để vẽ | Có | Không | Công cụ nền tảng cho nhiều nghiên cứu MSR nhưng không có sản phẩm hoàn chỉnh cho người dùng cuối |
| **CodeCharta** | Trực quan hóa 3D quy mô/độ phức tạp code | Một phần (theo phiên bản) | Bản đồ 3D | Có | Không | Mạnh về trực quan hóa, yếu về cảnh báo/giám sát định kỳ |

## Khoảng trống mà đề tài hướng tới
- Không có công cụ mã nguồn mở nào kết hợp đủ 3 thứ: (1) phân tích lịch sử Git theo thời gian, (2) giám sát định kỳ có cảnh báo, (3) trợ lý AI hỗ trợ review dựa trên ngữ cảnh chỉ số — hầu hết chỉ mạnh 1 trong 3 hướng.
- CodeScene gần nhất về mặt tính năng nhưng là sản phẩm thương mại đóng, không phù hợp để sinh viên/nhóm nhỏ tùy biến hoặc tự triển khai.

## Việc cần làm tiếp
- [ ] Thử cài Code Maat trên 1 repo mẫu để xem output thực tế, đối chiếu với dự kiến công thức của nhóm
- [ ] Tìm hiểu thêm Git-of-Theseus nếu cần bổ sung góc nhìn trực quan hóa lịch sử
