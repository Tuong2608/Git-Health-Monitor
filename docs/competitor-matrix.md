# Competitor Matrix — So sánh công cụ hiện có

> Cập nhật tuần 2: bổ sung kết luận thiết kế rút ra từ khảo sát, không chỉ liệt kê tính năng.

| Công cụ | Loại chỉ số chính | Phân tích lịch sử Git (temporal) | Trực quan hóa | Mã nguồn mở | Trợ lý AI review | Điểm đề tài nên học theo | Điểm đề tài nên làm khác đi |
|---|---|---|---|---|---|---|---|
| **SonarQube** | Chất lượng code tại 1 thời điểm (bug, code smell, duplication, coverage) | Hạn chế — chủ yếu snapshot hiện tại | Dashboard chi tiết, rõ ràng, dễ đọc | Có bản Community | Không | Cách trình bày dashboard rõ ràng, phân loại issue theo mức độ nghiêm trọng | Không dừng ở snapshot 1 thời điểm — đề tài tập trung vào **xu hướng theo thời gian** (nhiều lần quét, so sánh trước-sau) |
| **CodeScene** | Hotspot, temporal coupling, knowledge loss/bus factor | Có — điểm mạnh chính, gần nhất với hướng đề tài | Bản đồ hotspot trực quan, đẹp | Không (thương mại) | Không | Cách tính hotspot kết hợp change frequency + complexity, cách trực quan hóa bản đồ file | Đề tài mã nguồn mở, chi phí thấp hơn hẳn; bổ sung trợ lý AI review mà CodeScene không có |
| **Code Maat** | Change frequency, coupling, ownership (CLI, output dữ liệu thô) | Có | Không có UI, cần công cụ khác để vẽ | Có | Không | Thuật toán tính coupling và ownership đã được nhiều nghiên cứu MSR kiểm chứng, có thể tham khảo công thức | Đề tài cần đóng gói thành **sản phẩm hoàn chỉnh có UI + giám sát định kỳ tự động**, không chỉ là công cụ CLI chạy 1 lần |
| **CodeCharta** | Trực quan hóa 3D quy mô/độ phức tạp code | Một phần (theo phiên bản) | Bản đồ 3D ấn tượng | Có | Không | Ý tưởng trực quan hóa quy mô file bằng kích thước/màu sắc | Đề tài ưu tiên trực quan hóa 2D dễ đọc (biểu đồ xu hướng, bản đồ hotspot dạng heatmap) hơn 3D — vì mục tiêu chính là ra quyết định nhanh, không phải khám phá trực quan phức tạp |

## Kết luận: sản phẩm nên có những chức năng nào

Dựa trên khoảng trống thấy được (không công cụ mã nguồn mở nào kết hợp đủ 3 thứ: phân tích lịch sử theo thời gian + giám sát định kỳ có cảnh báo + trợ lý AI review), danh sách chức năng cốt lõi đề xuất:

1. **Phân tích repository** — clone/fetch kho mã nguồn, trích xuất lịch sử commit, tính các chỉ số (change frequency, complexity, hotspot score, temporal coupling, ownership concentration).
2. **Bản đồ hotspot** — trực quan hóa file rủi ro cao (học theo CodeScene, nhưng đơn giản/dễ đọc hơn).
3. **Theo dõi xu hướng theo thời gian** — so sánh nhiều lần quét (snapshot), thứ mà SonarQube/CodeScene không làm mạnh.
4. **Giám sát định kỳ + cảnh báo tự động** — quét lại theo lịch, sinh cảnh báo khi vượt ngưỡng — Code Maat/CodeCharta không có phần này.
5. **Trợ lý AI review** — giải thích rủi ro bằng ngôn ngữ tự nhiên, gợi ý checklist review — chưa công cụ nào trong bảng có.

Những chức năng **không làm** (vì công cụ khác đã làm tốt, làm lại không tạo giá trị mới):
- Phân tích chất lượng code tĩnh chi tiết kiểu SonarQube (bug/code smell/coverage) — đề tài chỉ dùng complexity ở mức cần thiết cho hotspot score, không cạnh tranh trực tiếp với SonarQube.
- Trực quan hóa 3D kiểu CodeCharta — không phù hợp mục tiêu ra quyết định nhanh của đề tài.

## Việc cần làm tiếp
- [ ] Nếu có thể, cài thử Code Maat/SonarQube Community trên 1 repo mẫu để kiểm chứng trực tiếp thay vì chỉ đọc tài liệu
- [ ] Đối chiếu danh sách chức năng này với use case đang draft (`usecases-draft.md`) để đảm bảo nhất quán
