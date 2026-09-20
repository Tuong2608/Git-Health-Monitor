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

## 4. Việc cần làm tiếp (tuần sau)
- [ ] Đọc thêm 2-3 tài liệu/bài báo về hotspot analysis và temporal coupling
- [ ] Thử chạy thử Lizard trên 1 repo mẫu để xem output thực tế
- [ ] Bắt đầu điền `competitor-matrix.md`
