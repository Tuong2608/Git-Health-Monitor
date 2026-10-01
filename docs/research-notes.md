# Ghi chú nghiên cứu — Tuần 1 (13/09 – 19/09/2026)

> Trạng thái: đang tìm hiểu, chưa kết luận. Sẽ cập nhật tiếp ở các tuần sau khi đọc thêm tài liệu và có kết quả thực nghiệm sơ bộ.

## 1. Nợ kỹ thuật (Technical Debt) và Mining Software Repositories (MSR)
- Nợ kỹ thuật: chi phí phát sinh về sau do lựa chọn giải pháp nhanh/tạm trong hiện tại (code viết vội, thiếu test, thiết kế chắp vá...).
- MSR: hướng nghiên cứu khai thác lịch sử kho mã nguồn (commit, issue, PR) để suy ra thông tin về chất lượng, rủi ro, hành vi của dự án phần mềm — đây là nền tảng lý thuyết chính cho đề tài.
- Việc cần làm tiếp: tìm thêm 2-3 bài báo/tài liệu gốc về MSR để trích dẫn trong Chương 1 (đủ điều kiện ≥5 tài liệu ngoại ngữ chất lượng theo rubric TC4).

## 2. Các chỉ số sức khỏe mã nguồn đang tìm hiểu
| Chỉ số | Ý nghĩa | Ghi chú |
|---|---|---|
| Change frequency | Số lần file bị thay đổi trong khoảng thời gian quan sát | Lấy từ lịch sử commit qua JGit |
| Code churn | Tổng số dòng thêm/xóa qua các lần commit | Liên quan change frequency nhưng đo theo khối lượng thay đổi |
| Cyclomatic complexity | Độ phức tạp logic của hàm/file (đo bằng Lizard) | Cần tìm hiểu ngưỡng phù hợp cho code Java |
| Hotspot score | Kết hợp change frequency + complexity, ước lượng file "nóng" cần chú ý | Công thức dự kiến: căn bậc hai(P_change × P_complexity), chuẩn hóa theo percentile |
| Temporal coupling | Hai file thường xuyên bị sửa cùng lúc dù không có quan hệ rõ ràng trong code | Ngưỡng dự kiến: ≥5 shared commit, coupling ≥50% |
| Ownership concentration | Mức độ tập trung quyền "sở hữu" 1 file vào 1-2 người (đo bằng HHI trên tỉ lệ đóng góp) | Thay cho khái niệm "Bus Factor" ở cấp file theo góp ý của GVHD |

## 3. Công nghệ dự kiến sử dụng (đang khảo sát tài liệu)
- Backend: Spring Boot, JGit (full clone/fetch, không dùng partial clone)
- Lưu trữ: PostgreSQL (không dùng Redis trong core — job xử lý bất đồng bộ qua TaskExecutor + PostgreSQL)
- Đo độ phức tạp: Lizard (gọi qua ProcessBuilder)
- Frontend: React + TypeScript, Recharts/D3.js cho trực quan hóa
- Hạ tầng: Docker, GitHub Actions cho CI/CD

## 4. Việc đã làm tuần 1 (tham khảo)
- [x] Đọc thêm tài liệu về hotspot analysis và temporal coupling
- [x] Thử chạy thử Lizard trên 1 repo mẫu để xem output thực tế
- [x] Bắt đầu điền `competitor-matrix.md`

---

# Cập nhật Tuần 3 (27/09 – 03/10/2026)

> Trạng thái: đã chốt công thức cụ thể cho 4 chỉ số chính, đã thử nghiệm JGit + Lizard trên repo mẫu. Công thức có thể còn điều chỉnh nhỏ sau khi cài đặt thật (M4) nếu phát sinh vấn đề với dữ liệu thực tế.

## 5. Công thức chốt cho các chỉ số chính

**Change frequency (tần suất thay đổi):**
```
change_frequency(file) = số lần file xuất hiện trong diff của các commit,
                          tính trong cửa sổ quan sát (ví dụ 6 tháng gần nhất)
```

**Hotspot score (điểm "nóng"):**
```
P_change = percentile(change_frequency) trong toàn bộ repo (0-1)
P_complexity = percentile(cyclomatic_complexity) trong toàn bộ repo (0-1)
hotspot_score = sqrt(P_change × P_complexity)
```
Lý do dùng căn bậc hai của tích 2 percentile: một file chỉ thật sự "nóng" khi VỪA thay đổi nhiều VỪA phức tạp — nếu 1 trong 2 giá trị thấp, hotspot score giảm mạnh (khác với lấy trung bình cộng, vốn có thể "bù" cho nhau).

**Temporal coupling (giữa 2 file A, B):**
```
shared_commits(A,B) = số commit mà cả A và B cùng bị thay đổi
coupling(A,B) = shared_commits(A,B) / số commit có thay đổi A hoặc B
```
Ngưỡng báo cáo: chỉ hiển thị cặp có `shared_commits ≥ 5` và `coupling ≥ 50%` — tránh nhiễu từ các cặp file tình cờ sửa chung 1-2 lần.

**Ownership concentration (tập trung sở hữu, dùng chỉ số HHI):**
```
share_i = số dòng đóng góp của người i / tổng số dòng đóng góp vào file
HHI(file) = Σ (share_i)²   (tính trên tất cả người từng sửa file đó)
```
HHI càng gần 1: càng tập trung vào 1-2 người (rủi ro khi người đó nghỉ/rời nhóm). HHI càng gần 0: càng nhiều người cùng hiểu file đó.

## 6. Thử nghiệm JGit — kết quả ban đầu
- Viết thử đoạn code Java dùng JGit (`Git.open()`, duyệt qua `RevWalk`) để liệt kê commit của 1 repo mẫu — chạy được, lấy đúng danh sách commit kèm author, timestamp, message.
- Thử lấy diff của từng commit bằng `DiffFormatter` — xác nhận lấy được danh sách file thay đổi + số dòng thêm/xóa, đúng dữ liệu cần cho change_frequency và code churn.
- Vấn đề phát sinh cần lưu ý cho M4: **rename detection** — JGit cần bật `setDetectRenames(true)` trong `DiffFormatter`, nếu không file bị đổi tên sẽ bị tính nhầm thành 1 file bị xóa + 1 file mới (làm sai lệch change_frequency).

## 7. Thử nghiệm Lizard — kết quả ban đầu
- Chạy Lizard dòng lệnh (`lizard <path>`) trên vài file `.java` mẫu — ra được chỉ số cyclomatic complexity (CCN) theo từng hàm.
- Lizard tính CCN theo **hàm**, không theo file — cần cộng gộp (ví dụ lấy CCN trung bình hoặc CCN lớn nhất trong file) để ra 1 giá trị complexity đại diện cho cả file. Quyết định tạm thời: dùng **CCN trung bình có trọng số theo số dòng của hàm**, sẽ xem lại nếu kết quả không hợp lý khi có dữ liệu thật.
- Gọi Lizard từ Java qua `ProcessBuilder` chạy được bình thường, output dạng text dễ parse (có thể thêm `--xml` để lấy output dạng XML dễ parse hơn khi cài đặt chính thức).

## 8. Việc cần làm tiếp (tuần sau)
- [ ] Tìm thêm 2-3 tài liệu/bài báo gốc về MSR và hotspot analysis để đủ trích dẫn cho Chương 1 (rubric TC4 yêu cầu ≥5 tài liệu ngoại ngữ chất lượng)
- [ ] Viết pseudo-code chi tiết hơn cho 4 công thức trên (chuẩn bị cho tuần 5 — thiết kế thuật toán)
- [ ] Kiểm tra lại cách cộng gộp CCN theo file có hợp lý không khi thử trên repo lớn hơn
