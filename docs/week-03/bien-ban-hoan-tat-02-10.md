# Biên bản hoàn tất kỹ thuật tuần 3 — 02/10/2026

Sinh viên: Trần Quang Toản. Tài liệu ghi nhận công việc thực tế và phân biệt phần còn phụ thuộc người khác.

## Đã hoàn tất

- Toản xác nhận đã đọc phần lý thuyết và trao đổi xong với Tưởng ngày 02/10.
- Đã tích hợp commit nghiên cứu của Tưởng `43d614f` từ `origin/main`, không xung đột.
- Mã chức năng: [165df33](https://github.com/Tuong2608/Git-Health-Monitor/commit/165df3376182602d84607984b267d41cec15156e), lưu repository với PostgreSQL/Flyway, UI React và biểu đồ mẫu, validation/CORS và CI.
- AI chạy lại 22 backend test, 4 browser test với PostgreSQL thật; lint/build và npm audit đạt. Kết quả không được ghi là Toản tự chạy.
- Báo cáo, nhật ký AI theo hoạt động, tài liệu lý thuyết, contract, hồ sơ năm người phỏng vấn và ảnh minh chứng đã được chuẩn bị/cập nhật.
- Đã nhận bản tổng hợp hai người S04–S05 từ Tưởng tại `3a3fd30`; hợp nhất thành năm trường hợp, giữ phản hồi hoài nghi S05 và những phần còn thiếu.

## Truy vết Git và CI

Nhánh `feat/toan-week-3`; [PR #1](https://github.com/Tuong2608/Git-Health-Monitor/pull/1); commit mã `165df33`, tài liệu `a7e7187`. [CI đã đạt cả 4 job](https://github.com/Tuong2608/Git-Health-Monitor/actions/runs/36979524926) ở SHA `a7e71872bbb66f9bac71299feefb3bca282fe914`: backend, frontend, secret-scan, package (Docker build). Bản hợp nhất tài liệu mới tiếp tục được CI kiểm tra. Chưa có review độc lập khi kiểm tra PR. Vercel báo preview deploy success cho SHA này, nhưng không đồng nghĩa backend mới đã deploy hay preview công khai.

## Kiểm tra staging

| Kiểm tra ngày 02/10 | Kết quả thực tế |
|---|---|
| Backend `/api/health` | HTTP 200, `OK` |
| Backend `/api/repositories` | HTTP 404, chưa có route bản mới |
| Frontend URL nhóm cung cấp | Chuyển sang trang đăng nhập Vercel |

Hai URL: https://git-health-monitor.onrender.com và https://git-health-monitor-dzgm1qfj8-tuong9.vercel.app.

## Việc cần quyền hoặc minh chứng của nhóm

1. **Tưởng/người review:** review PR thực tế, lưu nhận xét hoặc approval trên GitHub. Việc đã trao đổi không thay thế bằng chứng review PR.
2. **Chủ Render/Vercel:** cấu hình PostgreSQL và `DB_*`, `APP_ALLOWED_ORIGINS`, `VITE_API_BASE_URL`; xác nhận branch triển khai; cung cấp domain phù hợp cho GVHD. Xem [hướng dẫn staging](staging-week3.md). Chỉ merge/deploy sau khi điều kiện môi trường và review sẵn sàng.
3. **Nhóm:** đã có năm trường hợp phỏng vấn; bổ sung metadata/câu còn thiếu khi nhận được nguồn và xác nhận bản tổng hợp. Không yêu cầu hỏi lại hai người đã được Tưởng bổ sung.
4. **Toản/nhóm:** nộp [báo cáo tuần](bao-cao-tuan-03-toan.md) qua kênh môn học và lưu xác nhận GVHD. Chưa có quyền truy cập kênh nộp hay minh chứng tiếp nhận trong phiên này.

## Baseline metric chuẩn bị cho tuần 4

Mâu thuẫn công thức coupling đã được chuẩn hóa trong [Metric Specification v1](../metric-spec-v1.md) trên nhánh tài liệu để nhóm review trước khi cài đặt:

- temporal coupling dùng `shared / min(|C_A|,|C_B|)`, phù hợp với công thức được ghi nhận từ đề cương; không dùng `shared / |C_A ∪ C_B|` với cùng ngưỡng 50%;
- ngưỡng mặc định: `shared_commits >= 5` và `coupling >= 0.50`;
- HHI được định nghĩa là concentration của contribution lịch sử dựa trên churn `additions + deletions`, không được diễn giải trực tiếp thành mức hiểu mã;
- Lizard v1 dùng max function CCN làm complexity đại diện cấp file cho hotspot;
- JGit ingestion phải bật rename detection, bỏ merge commit khỏi metric lịch sử và normalize developer identity;
- NFR 10.000 commit/10 phút vẫn là **mục tiêu chưa được benchmark** bằng pipeline hoàn chỉnh.

Các điểm trên là baseline kỹ thuật trong PR tài liệu, chưa được ghi là xác nhận của GVHD cho đến khi nhóm/GVHD review và merge.

**Kết luận:** phần kỹ thuật local và hồ sơ tuần 3 đã được kiểm chứng trong phạm vi ghi nhận; metric pipeline vẫn thuộc giai đoạn tiếp theo và phải triển khai/test theo spec đã review.
