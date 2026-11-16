# Kịch bản Thực nghiệm Người dùng RQ3 và Biểu mẫu Khảo sát SUS (STT 33 & STT 34)

- **Công cụ chấm điểm tự động:** `tools/experiments/evaluate_rq3_sus.py` (kèm `test_rq3_sus.py`)
- **Mục tiêu nghiệm thu:**
  - **Ngưỡng cam kết tối thiểu (Đề cương v4):** Tối thiểu `5-9` người tham gia thực tế (sinh viên năm 3-4 CNTT / lập trình viên có kinh nghiệm Git), **Task Success Rate >= 80%**, điểm trung bình **SUS >= 68**.
  - **Mục tiêu phấn đấu theo Rubric Khoa:** `>= 10` người tham gia, **Task Success Rate >= 90%**, điểm trung bình **SUS >= 80**, kèm **1 vòng cải tiến giao diện (STT 34)** dựa trên phản hồi thực tế.

---

## 1. Chuẩn bị môi trường trước khi mời người tham gia

1. Khởi chạy hệ thống bằng Docker Compose (`docker compose up -d --build`) với cấu hình demo đã bật 3 cờ `APP_ANALYSIS_ENABLED=true`, `APP_ANALYSIS_WRITES_ENABLED=true`, `APP_ANALYSIS_PIPELINE_ENABLED=true` và `APP_REGISTRATION_ENABLED=true` (nếu test tính năng AI Review Preview thì bật `APP_AI_REVIEW_ENABLED=true`).
2. Nạp sẵn ít nhất 1 kho mẫu đã phân tích xong (khuyên dùng `https://github.com/spring-projects/spring-petclinic` hoặc `https://github.com/apache/commons-csv`) để tiết kiệm thời gian chờ clone cho người dùng khi thực hiện `T2..T7`, còn `T1` có thể thử với repo nhỏ như `https://github.com/spring-guides/gs-rest-service`.
3. **Không gợi ý nút bấm** trong lúc người tham gia thao tác; chỉ ghi nhận `Đạt (True)` / `Không đạt (False)`, thời gian hoàn thành (giây) và chỗ người dùng bị khựng/thắc mắc.

---

## 2. Đề bài 7 Tác vụ Chuẩn (`T1..T7`)

| Mã | Tên tác vụ | Yêu cầu giao cho người tham gia | Tiêu chí chấm Đạt (`passed = true`) |
|---|---|---|---|
| **T1** | Thêm kho & chạy phân tích | Nhập URL `https://github.com/spring-guides/gs-rest-service`, đăng ký repo và bấm kích hoạt một lượt phân tích mới. | Tìm được nút thêm repo, khởi chạy job trạng thái `QUEUED/RUNNING` thành công. |
| **T2** | Tìm Top 5 file ưu tiên rà soát | Mở snapshot đã hoàn thành của dự án mẫu, tìm 5 tập tin đứng đầu bảng xếp hạng rủi ro (Hotspot). | Chỉ đúng danh sách Top 5 tập tin theo thứ tự điểm `hotspot_score` giảm dần. |
| **T3** | Giải thích lý do rủi ro của tập tin | Mở chi tiết tập tin đứng đầu (hoặc 1 file cụ thể) và cho biết `change_frequency`, `max_ccn`, `HHI` cùng tối thiểu 1 commit bằng chứng. | Đọc đúng giá trị thực tế và nhận diện được commit làm thay đổi file đó (hiểu phân biệt `null` nếu có). |
| **T4** | Xác định cặp Temporal Coupling | Tìm xem tập tin đang xem có liên kết thay đổi cùng lúc (`temporal coupling`) với tập tin nào khác không và số commit chung là bao nhiêu. | Đọc đúng cặp file liên kết và số `shared_commits` / mẫu số `min`. |
| **T5** | Xem lịch sử Snapshot & Xu hướng | Mở biểu đồ xu hướng hoặc so sánh giữa 2 snapshot theo thời gian của dự án, nhận biết xem đường nối có bị ngắt do thiếu coverage hay không. | Chọn được snapshot, đọc được giá trị xu hướng hoặc lý do ngắt/chặn nếu `coverageComplete = false`. |
| **T6** | Xác định cảnh báo | Mở danh sách cảnh báo (`Alerts`), đọc nguyên nhân và thử chuyển trạng thái một cảnh báo sang `ACKNOWLEDGED` (nếu có quyền). | Xác định đúng loại cảnh báo, giá trị quan sát vs ngưỡng và trạng thái hiện tại. |
| **T7** | Trợ lý AI Review có kiểm soát | Chọn một đoạn dòng code (`startLine..endLine`), bấm xem trước dữ liệu gửi đi (`Preview`), kiểm tra cam kết không lộ code nhạy cảm và xác nhận consent (hoặc giải thích bước Preview). | Thực hiện đủ bước xem trước payload / xác nhận đồng thuận mà không nhầm AI tự động sửa code. |

---

## 3. Mẫu Câu Hỏi Google Form (Gửi cho người tham gia điền sau khi test)

**Phần A - Thông tin chung**
1. Mã người tham gia *(VD: P01, P02...)* `[Trả lời ngắn - Bắt buộc]`
2. Vai trò / Kinh nghiệm sử dụng Git `[Trắc nghiệm: Sinh viên CNTT năm 3-4 / Fresher-Junior Dev / Khác]`

**Phần B - Ghi nhận hoàn thành 7 tác vụ (`T1..T7`)** *(Người điều phối hoặc người test tích trực tiếp theo từng task)*
- Với mỗi task `T1..T7`: Trạng thái `[Đạt / Không đạt]` và Thời gian hoàn thành (giây).

**Phần C - Thang đo Khả năng sử dụng SUS (Brooke, 1996 - Thang 1: Hoàn toàn không đồng ý đến 5: Hoàn toàn đồng ý)**
1. Tôi nghĩ rằng mình sẽ muốn sử dụng hệ thống này thường xuyên. `(1 - 5)`
2. Tôi thấy hệ thống này phức tạp một cách không cần thiết. `(1 - 5)`
3. Tôi nghĩ rằng hệ thống này dễ sử dụng. `(1 - 5)`
4. Tôi nghĩ rằng tôi sẽ cần sự hỗ trợ của nhân viên kỹ thuật để có thể sử dụng hệ thống này. `(1 - 5)`
5. Tôi thấy các chức năng khác nhau trong hệ thống này được tích hợp tốt. `(1 - 5)`
6. Tôi nghĩ rằng có quá nhiều sự không nhất quán trong hệ thống này. `(1 - 5)`
7. Tôi hình dung rằng hầu hết mọi người sẽ học cách sử dụng hệ thống này rất nhanh chóng. `(1 - 5)`
8. Tôi thấy hệ thống này rất rườm rà/khó thao tác khi sử dụng. `(1 - 5)`
9. Tôi cảm thấy rất tự tin khi sử dụng hệ thống này. `(1 - 5)`
10. Tôi cần phải học rất nhiều thứ trước khi có thể bắt đầu sử dụng hệ thống này. `(1 - 5)`

**Phần D - Góp ý cải tiến UI/UX (Dùng cho STT 34)**
- Câu 11: Thao tác nào khiến bạn tốn nhiều thời gian hoặc dễ hiểu nhầm nhất trên giao diện? `[Đoạn văn]`
- Câu 12: Bạn đề xuất chỉnh sửa nhỏ nào để màn hình Dashboard/Chi tiết file dễ dùng hơn? `[Đoạn văn]`

---

## 4. Lệnh chấm điểm tự động sau khi thu CSV từ Google Form

Xuất file CSV từ Google Form với tiêu đề cột `participantId,role,T1,T1_sec,...,T7,T7_sec,Q1,...,Q10,feedback`, rồi chạy:

```bash
python tools/experiments/evaluate_rq3_sus.py rq3-responses.csv --out-json docs/week-11/evidence/rq3-results.json
```
