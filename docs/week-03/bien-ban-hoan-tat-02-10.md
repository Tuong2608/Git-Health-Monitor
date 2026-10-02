# Biên bản hoàn tất kỹ thuật tuần 3 — 02/10/2026

Sinh viên: Trần Quang Toản. Tài liệu ghi nhận công việc thực tế và phân biệt phần còn phụ thuộc người khác.

## Đã hoàn tất

- Toản xác nhận đã đọc phần lý thuyết và trao đổi xong với Tưởng ngày 02/10.
- Đã tích hợp commit nghiên cứu của Tưởng `43d614f` từ `origin/main`, không xung đột.
- Mã chức năng: [165df33](https://github.com/Tuong2608/Git-Health-Monitor/commit/165df3376182602d84607984b267d41cec15156e), lưu repository với PostgreSQL/Flyway, UI React và biểu đồ mẫu, validation/CORS và CI.
- AI chạy lại 22 backend test, 4 browser test với PostgreSQL thật; lint/build và npm audit đạt. Kết quả không được ghi là Toản tự chạy.
- Báo cáo, nhật ký AI theo hoạt động, tài liệu lý thuyết, contract, hồ sơ ba người phỏng vấn và ảnh minh chứng đã được chuẩn bị/cập nhật.
- Tưởng phụ trách hỏi thêm hai người và tổng hợp sau, theo thông tin Toản cung cấp. Chưa thêm hai người này vào dữ liệu phân tích.

## Truy vết Git và CI

Nhánh `feat/toan-week-3`; commit chức năng ở trên. PR và kết quả CI được bổ sung sau khi hệ thống trả kết quả. Không tự xác nhận review của Tưởng.

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
3. **Tưởng:** bàn giao bản tổng hợp hai người bổ sung khi có. Những câu thiếu của dữ liệu hiện tại vẫn được ghi rõ, không tự điền.
4. **Toản/nhóm:** nộp [báo cáo tuần](bao-cao-tuan-03-toan.md) qua kênh môn học và lưu xác nhận GVHD. Chưa có quyền truy cập kênh nộp hay minh chứng tiếp nhận trong phiên này.

## Điểm cần thống nhất trước tuần 4

Ghi chú mới của Tưởng dùng coupling `shared / |C_A ∪ C_B|`, trong khi đề cương dùng `shared / min(|C_A|,|C_B|)`. Ví dụ A có 10 commit, B có 20, chung 5: hai cách lần lượt là 20% và 50%. Không dùng chung ngưỡng 50% rồi coi kết quả tương đương. Chưa sửa công thức trong tài liệu của Tưởng hoặc đề cương; chưa có code metric bị ảnh hưởng. Nhóm cần ghi quyết định trong SRS trước khi cài đặt.

HHI đo mức tập trung đóng góp theo định nghĩa dữ liệu, không trực tiếp chứng minh mức hiểu mã. Tương tự, phần thử JGit/Lizard trong ghi chú của Tưởng chưa thay thế benchmark 10.000 commit/10 phút có log đo cụ thể.

**Kết luận:** phần kỹ thuật local và hồ sơ đã được kiểm chứng; trạng thái nộp tuần 3 chỉ hoàn tất khi review, staging và tiếp nhận báo cáo có minh chứng.
