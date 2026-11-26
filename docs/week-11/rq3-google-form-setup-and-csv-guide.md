# Hướng dẫn dựng Google Form Khảo sát RQ3 & Xuất CSV Chấm điểm SUS (`STT 33`)

Tài liệu này giúp nhóm tạo nhanh biểu mẫu Google Form khớp 100% với tệp mẫu [rq3-responses-template.csv](evidence/rq3-responses-template.csv) và chạy trực tiếp công cụ chấm tự động `tools/experiments/evaluate_rq3_sus.py`.

---

## 1. Chuẩn bị trước khi mở khảo sát cho bạn bè

1. **Dựng máy chủ Staging Cách 1 theo [staging-runbook.md](staging-runbook.md):**
   - Chạy `docker compose -f compose.yaml -f compose.staging.yaml up -d --build`.
   - Nạp dữ liệu mẫu qua `python3 deploy/seed_staging.py --base-url https://<domain-staging>` để có sẵn kho `spring-projects/spring-petclinic` (và `apache/commons-csv`) phục vụ các tác vụ `T2..T7`.
   - Kiểm tra kho `spring-guides/gs-rest-service` chưa được thêm sẵn (hoặc hướng dẫn người test nhập URL để thử tác vụ `T1`), và bật `APP_AI_REVIEW_ENABLED=true` (cùng provider key nếu muốn thử luôn phản hồi LLM ở `T7`; lưu ý bước Preview + Consent `T7` hoạt động ngay cả khi chỉ bật `APP_AI_REVIEW_ENABLED=true`).
2. **Quy mô & tiêu chí đạt:**
   - **Đề cương cam kết:** `5-9` người dùng thực tế (sinh viên CNTT năm 3-4 hoặc lập trình viên có kinh nghiệm Git), `Task Success Rate >= 80%`, điểm `SUS >= 68`.
   - **Mục tiêu phấn đấu:** `>= 10` người (Tưởng mời 4-5 bạn, Toản mời 4-5 bạn), `Task Success Rate >= 90%`, `SUS >= 80`.
   - **Nguyên tắc điều phối:** Không gợi ý nút bấm khi người tham gia tự thao tác; chỉ bấm giờ (giây) và ghi nhận chỗ người dùng bị khựng.

---

## 2. Cấu trúc 4 phần trên Google Form

Tạo biểu mẫu Google Form theo đúng thứ tự câu hỏi dưới đây (hoặc phát phiếu giấy theo [mau-khao-sat-rq3-gui-nguoi-tham-gia.txt](../week-10/mau-khao-sat-rq3-gui-nguoi-tham-gia.txt) rồi nhập vào Google Sheets theo đúng cột mẫu):

### Phần A — Đồng thuận & Thông tin chung
1. **Đồng thuận tham gia** *(Trắc nghiệm bắt buộc)*: `Tôi đồng ý tham gia thực nghiệm và cho phép sử dụng kết quả ẩn danh trong báo cáo học thuật.`
2. **Mã người tham gia (`participantId`)** *(Trả lời ngắn bắt buộc)*: `P01`, `P02`, ... *(mỗi người 1 mã duy nhất).*
3. **Vai trò / Kinh nghiệm Git (`role`)** *(Trắc nghiệm bắt buộc)*:
   - `SV CNTT năm 3-4`
   - `Fresher/Junior Dev`
   - `Khác`

### Phần B — Kết quả thực hiện 7 tác vụ chuẩn (`T1..T7`)
*(Có thể để người điều phối tích trực tiếp hoặc điền sau mỗi task; các cột thời gian tính bằng giây)*
- **T1 (`T1`, `T1_sec`) — Đăng ký kho & kích hoạt phân tích:** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).
- **T2 (`T2`, `T2_sec`) — Tìm Top 5 tập tin ưu tiên rà soát (Hotspot):** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).
- **T3 (`T3`, `T3_sec`) — Giải thích lý do rủi ro (`change_frequency`, `max_ccn`, `HHI`, commit bằng chứng):** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).
- **T4 (`T4`, `T4_sec`) — Xác định cặp `Temporal Coupling` & `shared_commits`:** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).
- **T5 (`T5`, `T5_sec`) — Xem lịch sử Snapshot, xu hướng & kiểm tra `coverageComplete = false`:** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).
- **T6 (`T6`, `T6_sec`) — Kiểm tra cảnh báo (`Alerts`) & chuyển trạng thái `ACKNOWLEDGED`:** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).
- **T7 (`T7`, `T7_sec`) — Trợ lý AI Review 3 bước (nhập dòng, `Preview`, kiểm tra `contextHash`, tích `Consent`):** Trắc nghiệm `[Đạt (1) / Không đạt (0)]` + Trả lời ngắn (số giây).

### Phần C — Bảng hỏi System Usability Scale (SUS — Brooke, 1996)
*(Dạng Lưới trắc nghiệm hoặc 10 câu Thang đo tuyến tính `1` đến `5`, trong đó `1 = Hoàn toàn không đồng ý`, `5 = Hoàn toàn đồng ý`)*
- `Q1`: Tôi nghĩ rằng mình sẽ muốn sử dụng hệ thống này thường xuyên.
- `Q2`: Tôi thấy hệ thống này phức tạp một cách không cần thiết.
- `Q3`: Tôi nghĩ rằng hệ thống này dễ sử dụng.
- `Q4`: Tôi nghĩ rằng tôi sẽ cần sự hỗ trợ của nhân viên kỹ thuật để có thể sử dụng hệ thống này.
- `Q5`: Tôi thấy các chức năng khác nhau trong hệ thống này được tích hợp tốt.
- `Q6`: Tôi nghĩ rằng có quá nhiều sự không nhất quán trong hệ thống này.
- `Q7`: Tôi hình dung rằng hầu hết mọi người sẽ học cách sử dụng hệ thống này rất nhanh chóng.
- `Q8`: Tôi thấy hệ thống này rất rườm rà/khó thao tác khi sử dụng.
- `Q9`: Tôi cảm thấy rất tự tin khi sử dụng hệ thống này.
- `Q10`: Tôi cần phải học rất nhiều thứ trước khi có thể bắt đầu sử dụng hệ thống này.

### Phần D — Góp ý cải tiến giao diện (`feedback`)
- **Câu góp ý tổng hợp (`feedback`)** *(Đoạn văn)*: Thao tác hoặc thuật ngữ nào khiến bạn mất thời gian hoặc dễ hiểu nhầm nhất? Bạn đề xuất chỉnh sửa gì cho Dashboard / Chi tiết file / AI Review?

---

## 3. Quy trình xuất CSV và chạy chấm điểm tự động

1. Trong Google Sheets liên kết với Google Form, đổi tên hàng tiêu đề của tab xuất CSV cho khớp đúng 27 cột trong [rq3-responses-template.csv](evidence/rq3-responses-template.csv):
   `participantId,role,T1,T1_sec,T2,T2_sec,T3,T3_sec,T4,T4_sec,T5,T5_sec,T6,T6_sec,T7,T7_sec,Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8,Q9,Q10,feedback`
   *(Nếu có 2 câu góp ý ở Phần D trên Form, gộp 2 ô đó thành cột `feedback` trước khi lưu CSV.)*
2. `evaluate_rq3_sus.py` đã hỗ trợ trực tiếp cả nhãn tiếng Việt (`Đạt`, `Đạt (1)`, `Không đạt`, `Không đạt (0)`), `1`/`0`, `true`/`false`, `pass`/`fail`, tự bỏ khoảng trắng thừa ở header và bỏ qua dòng trống.
3. Tải về dạng `Comma Separated Values (.csv)` (UTF-8), lưu thành `rq3-responses.csv` rồi chạy từ thư mục gốc repository:

```powershell
python tools/experiments/evaluate_rq3_sus.py rq3-responses.csv --out-json docs/week-11/evidence/rq3-results.json
```

4. Sau khi có `docs/week-11/evidence/rq3-results.json`, lấy tỷ lệ đạt từng tác vụ, điểm trung bình/trung vị SUS và top 2-3 góp ý trong cột `feedback` để bổ sung chỉnh sửa UI vòng phản hồi người dùng thực tế (nếu phát sinh điểm nghẽn mới) và điền số liệu thật vào Chương 4 cuốn Tiểu luận.
