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
| Code churn | Tổng số dòng thêm/xóa qua các lần commit | Dùng làm cơ sở contribution trong HHI v1 |
| Cyclomatic complexity | Độ phức tạp logic của hàm/file (đo bằng Lizard) | V1 dùng max function CCN làm complexity đại diện cấp file |
| Hotspot score | Kết hợp change frequency + complexity, ước lượng file "nóng" cần chú ý | `sqrt(P_change × P_complexity)`, chuẩn hóa theo percentile |
| Temporal coupling | Hai file thường xuyên bị sửa cùng lúc | V1 dùng `shared / min(|C_A|,|C_B|)`; lọc ≥5 shared commit và coupling ≥50% |
| Contribution concentration | Mức độ tập trung đóng góp lịch sử vào một số developer | Dùng HHI trên tỉ lệ churn; không suy ra trực tiếp mức hiểu mã |

Đặc tả baseline để cài đặt: [Metric Specification v1](metric-spec-v1.md).

## 3. Công nghệ dự kiến sử dụng (đang khảo sát tài liệu)
- Backend: Spring Boot, JGit (full clone/fetch, không dùng partial clone)
- Lưu trữ: PostgreSQL (không dùng Redis trong core — job xử lý bất đồng bộ qua TaskExecutor + PostgreSQL)
- Đo độ phức tạp: Lizard (gọi qua ProcessBuilder)
- Frontend: React + TypeScript, Recharts/D3.js cho trực quan hóa
- Hạ tầng: Docker, GitHub Actions cho CI/CD

## 4. Việc đã làm tuần 1 (tham khảo)
- [x] Đọc thêm tài liệu về hotspot analysis và temporal coupling
- [x] Thử chạy Lizard trên repo mẫu để xác minh khả năng lấy CCN
- [x] Bắt đầu điền `competitor-matrix.md`

---

# Cập nhật Tuần 3 (27/09 – 03/10/2026)

> Trạng thái: đã có baseline công thức v1 để chuẩn bị cài đặt metric. JGit và Lizard mới được thử ở mức PoC/feasibility; chưa được tích hợp vào backend hiện tại. Mọi thay đổi công thức sau này phải cập nhật đồng bộ spec, code, test và báo cáo.

## 5. Baseline cho các chỉ số chính

**Cửa sổ quan sát:**
```
observation_window mặc định = 180 ngày
```
Cửa sổ phải cấu hình được và tất cả metric trong cùng snapshot dùng cùng một cửa sổ.

**Change frequency (tần suất thay đổi):**
```
change_frequency(file) = số non-merge commit mà file xuất hiện trong diff
                          trong observation window
```

**Hotspot score (điểm "nóng"):**
```
P_change = percentile(change_frequency) trong toàn bộ repo (0-1)
P_complexity = percentile(file_complexity) trong toàn bộ repo (0-1)
hotspot_score = sqrt(P_change × P_complexity)
```
Trong v1, `file_complexity = max(CCN(function))` của file.

**Temporal coupling (giữa 2 file A, B):**
```
C_A = tập non-merge commit có thay đổi A
C_B = tập non-merge commit có thay đổi B

shared_commits(A,B) = |C_A ∩ C_B|
coupling(A,B) = shared_commits(A,B) / min(|C_A|, |C_B|)
```
Ngưỡng báo cáo mặc định: chỉ hiển thị cặp có `shared_commits ≥ 5` và `coupling ≥ 50%`.

Không dùng `shared / |C_A ∪ C_B|` với cùng ngưỡng 50% vì đó là định nghĩa khác và cho kết quả khác.

**Contribution concentration (HHI):**
```
contribution_i(file) = additions_i(file) + deletions_i(file)
share_i = contribution_i(file) / tổng contribution của file
HHI(file) = Σ (share_i)²
```
HHI gần 1 cho thấy đóng góp lịch sử tập trung vào ít developer; HHI thấp hơn cho thấy đóng góp phân bố rộng hơn. HHI **không trực tiếp chứng minh mức hiểu mã**. V1 chưa đặt hard threshold cho HHI.

## 6. Thử nghiệm JGit — kết quả ban đầu
- Đã thử đoạn Java dùng JGit (`Git.open()`, `RevWalk`) trên repo mẫu để liệt kê commit, author, timestamp và message.
- Đã thử `DiffFormatter` để lấy danh sách file thay đổi và số dòng thêm/xóa.
- Khi cài đặt pipeline chính thức phải bật **rename detection** bằng `setDetectRenames(true)`.
- Baseline v1 bỏ **merge commit** khỏi các metric lịch sử nhằm giảm coupling/churn giả do merge.
- Cần chuẩn hóa developer identity trước khi tính contribution concentration.
- Các thử nghiệm trên là PoC; `backend/pom.xml` hiện chưa có JGit và backend chưa có ingestion/metric pipeline.

## 7. Thử nghiệm Lizard — kết quả ban đầu
- Đã thử Lizard trên file `.java` mẫu và xác nhận lấy được cyclomatic complexity (CCN) theo function.
- V1 dùng **max function CCN** làm complexity đại diện cấp file cho hotspot, tránh việc một function rất phức tạp bị nhiều function đơn giản kéo trung bình xuống.
- Nên vẫn lưu thêm `avg_ccn`, `function_count` và `nloc` để phân tích sau.
- Khi tích hợp Java, gọi Lizard qua `ProcessBuilder` và ưu tiên output có cấu trúc như `--xml`, không parse console text tự do.

## 8. Benchmark hiệu năng
NFR hiện tại:
```
repository <= 10.000 commits
target analysis time <= 10 phút
```
Đây là **mục tiêu chưa được kiểm chứng** bằng pipeline hoàn chỉnh. Benchmark sau này phải ghi riêng thời gian clone/fetch, JGit extraction, Lizard, tính metric và persist database cùng thông tin môi trường test.

## 9. Việc cần làm tiếp
- [ ] Tìm thêm 2-3 tài liệu/bài báo gốc về MSR và hotspot analysis để đủ trích dẫn cho Chương 1.
- [ ] Triển khai ingestion JGit theo [Metric Specification v1](metric-spec-v1.md).
- [ ] Viết unit/integration test cho coupling, HHI, rename, merge-commit filtering và identity normalization.
- [ ] Tích hợp Lizard output có cấu trúc và test aggregation max CCN.
- [ ] Benchmark pipeline thật trước khi tuyên bố đạt NFR 10.000 commit/10 phút.
