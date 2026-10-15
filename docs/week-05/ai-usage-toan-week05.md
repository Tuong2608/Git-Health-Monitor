# Nhật ký sử dụng AI — tuần 5

Sinh viên: Trần Quang Toản (23110158). Ngày: 15/10/2026. Công cụ: Codex. Nội dung yêu cầu được tóm tắt theo mục tiêu học thuật, không sao chép nguyên văn prompt và không dựng lại hội thoại giả. Chưa ghi tổng giờ công hoặc model/version chưa được xác minh.

## AI-05-01 — Đối chiếu kế hoạch

- **Mục tiêu:** xác định đúng nhiệm vụ và thời hạn cá nhân.
- **Đầu vào:** workbook GitHub, metric-spec, tài liệu tuần 4 và lịch sử Git.
- **Hỗ trợ:** trích dòng 21/22/24, phân biệt task cá nhân với task chung tuần 5–6; so main với nhánh reconcile.
- **Đầu ra:** assignment-check.md và assignment-source.json.
- **Kiểm chứng:** đọc workbook tại main 8d55040; không coi bản Downloads là file đã tồn tại trên GitHub.
- **Giới hạn/trách nhiệm:** nhóm trưởng vẫn quyết định thay đổi phân công; AI không tự cập nhật trạng thái nghiệm thu.

## AI-05-02 — Mô hình dữ liệu

- **Mục tiêu:** chuyển yêu cầu snapshot/metric/monitoring thành mô hình quan hệ có thể kiểm tra.
- **Đầu vào:** danh sách 10 bảng, V1 hiện hữu, API/rules/metric spec và điểm làm rõ kiến trúc chưa merge.
- **Hỗ trợ:** soạn từ điển, ERD, khóa ghép, partial index, idempotency và SQL tham chiếu.
- **Đầu ra:** database-design.md, erd.mmd/png, schema-reference.sql.
- **Kiểm chứng:** SQL thật trên PostgreSQL 18.3; case âm cho duplicate, NULL, cross-repo FK, metric âm và coupling sai; sửa cardinality job→snapshot trong Mermaid thành 0..1.
- **Giới hạn/trách nhiệm:** CHECK không chứng minh state transition/lease fencing; Tưởng review kiến trúc, Toản tiếp nhận và giải thích mô hình trước cài đặt.

## AI-05-03 — Thiết kế giao diện

- **Mục tiêu:** biểu diễn luồng tác vụ và bằng chứng metric dễ kiểm chứng.
- **Đầu vào:** UC tuần 4 và nhu cầu từ phỏng vấn đã tổng hợp; không bổ sung người tham gia/câu trả lời mới.
- **Hỗ trợ:** tạo wireframe offline bốn màn hình, điều hướng, consent AI, fallback và mapping visualization.
- **Đầu ra:** wireframes/index.html, README và ảnh desktop/mobile.
- **Kiểm chứng:** 18 kiểm tra Playwright, không lỗi JavaScript/HTTP request, kiểm tra tràn ngang ở 1440 và 375 px; xem ảnh tổng quan desktop và chi tiết mobile.
- **Giới hạn/trách nhiệm:** fixture không phải dữ liệu thật; chưa có chọn dòng mã, provider AI, lưu trạng thái server hay test usability với người dùng.

## AI-05-04 — Thiết kế đánh giá

- **Mục tiêu:** chuẩn bị phép so sánh hotspot với baseline, hạn chế data leakage.
- **Đầu vào:** task 20 và metric-spec-v1; nguồn Git/Stanford IR Book được liên kết trong tài liệu.
- **Hỗ trợ:** đề xuất cửa sổ quan sát/dự báo, quy trình nhãn, baseline, xử lý NA và công thức AP/MAP.
- **Đầu ra:** experiment-design.md.
- **Kiểm chứng:** đối chiếu công thức với tài liệu, đưa fixture kiểm tra bằng tay; tách dữ liệu tương lai khỏi feature, thống nhất eligible set.
- **Giới hạn/trách nhiệm:** chưa chạy thực nghiệm; Tưởng và Toản phải chốt dữ liệu/thông số và review nhãn thật. Không ghi số liệu giả làm kết quả nghiên cứu.

## AI-05-05 — Kiểm tra và báo cáo

- **Mục tiêu:** liên kết phát biểu hoàn thành với bằng chứng có thể tái lập.
- **Hỗ trợ:** tạo script kiểm chứng SQL/UI, chụp ảnh, tổng hợp báo cáo/checklist và nguồn tham khảo.
- **Đầu ra:** thư mục evidence, báo cáo tuần 5 và checklist.
- **Kiểm chứng:** lưu kết quả chạy thành công; SQL rollback schema riêng; thử browser ban đầu thiếu đường dẫn runtime, chạy lại thành công với browser đã cài. Lần khởi động PostgreSQL thừa báo đang chạy; kiểm tra sau đó dùng server local có sẵn và dừng khi xong.
- **Giới hạn/trách nhiệm:** AI thực hiện thao tác kỹ thuật trong phiên; không mô tả là sinh viên tự thao tác tất cả, không xác nhận đã nộp/đã được duyệt.

## Ghi nhận học thuật cần cá nhân tiếp nhận

Nội dung cần giải thích được: snapshot khác job thế nào; vì sao identity gồm window/config; vì sao HHI không đại diện năng lực; vì sao split theo thời gian; cách tính AP; SQL constraint nào không thay được logic service. Chưa đánh dấu đã tự học hoặc tự trình bày thành công nếu chưa có xác nhận của Toản.
