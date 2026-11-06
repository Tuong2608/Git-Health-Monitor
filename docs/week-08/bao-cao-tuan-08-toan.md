# Báo cáo tuần 8 — Trần Quang Toản

MSSV **23110158**. Đề tài **Git Health Monitor**. Tuần **01–07/11/2026**, thực hiện **05/11/2026**.

## 1. Công việc theo kế hoạch

Hoàn thiện tầng API (task 24), cài giao diện React (task 26), tham gia đóng gói và tích hợp (task 27–28). Bắt đầu từ main 1878d2b đã có các PR tuần 7 của Tưởng và Toản. Phần thu thập Git, tính metric/Lizard và LlmReviewService là sản phẩm của Tưởng; tuần này Toản nối và kiểm chứng các module đó.

## 2. Sản phẩm

| Mảng | Đã thực hiện | Ý nghĩa |
|---|---|---|
| Frontend | Dashboard snapshot, bản đồ điểm nóng, bảng số đo, file detail, contribution, bằng chứng commit/coupling, trend, comparison | Xem dữ liệu thật thay biểu đồ minh họa tuần 3 |
| Điều khiển phân tích | Enqueue job, theo dõi trạng thái, retry FAILED, cấu hình lịch, quản lý quy tắc và ACK/CLOSE cảnh báo | Giao diện dùng đúng trạng thái và version từ backend |
| API AI | Xem trước context, đọc source tại SHA snapshot, xác nhận contextHash, giới hạn thời gian và số request đang chạy | Chỉ gửi đúng dữ liệu đã xác nhận; lỗi AI không làm mất snapshot |
| Database | V4 audit metadata AI, khóa ngoại tới file trong snapshot | Truy vết phiên bản prompt/model/context mà không lưu raw source hoặc output |
| Tích hợp | Nối UI/API với GitAnalysisPipeline, kiểm thử JGit/Lizard/PostgreSQL thật và ba repo công khai | Xác nhận dữ liệu đi qua các module và được lưu/read back |
| Docker/CI | Frontend multi-stage + nginx proxy, backend có Lizard 1.24.3, volume Git cache; CI cài Lizard và kiểm tra Compose | Khắc phục image backend trước đó thiếu môi trường chạy complexity analyzer |

Nghiệp vụ: phân biệt null và số 0; cảnh báo coverage không đầy đủ; không nối trend qua cấu hình/cửa sổ không tương thích; không diễn giải HHI thành năng lực cá nhân; coupling là tương quan lịch sử. Huỷ request cũ khi đổi lựa chọn để tránh hiển thị dữ liệu snapshot sai.

## 3. Kiểm chứng

- Backend: **111 test duy nhất đạt**, 0 fail/error/skip. Full verify có 109 test; thêm 2 test availability chạy riêng sau đó. Trong đó có 10 ca mới tuần 8.
- Frontend: lint/build đạt; **11 UI tests đạt**, không bỏ qua trong lần local cuối, gồm đăng ký/read-back thật và xem snapshot `spring-petclinic` thật. Các test hành vi còn lại dùng response fixture có ghi nhãn.
- Pipeline công khai: `gs-rest-service` lưu 6 file Java, 29 commit trong cửa sổ; `spring-petclinic` 50 file Java, 49 commit; `Hello-World` 0 file Java/0 commit trong cửa sổ nhưng xử lý empty hợp lệ. Cửa sổ 365 ngày; SHA/bounds đầy đủ trong JSON minh chứng.
- `coverageComplete=false` với hai repo Java do lineage ambiguity (2 và 13 file); vẫn lưu được số đo có căn cứ, không gọi đây là dữ liệu đầy đủ để so sánh.
- AI: kiểm tra pinned SHA khi HEAD di chuyển, consent thiếu/hash sai, public check thất bại, secret, output sai, trailing JSON, timeout, disabled/no provider. Provider giả chỉ ở test, không có kết quả AI thương mại.
- Audit frontend: 0 high/critical, 10 moderate thuộc chuỗi minimatch trong tooling; chưa force downgrade ESLint/TypeScript-ESLint để che cảnh báo.

Thời gian 8,67 / 8,30 / 2,09 giây là thời gian quan sát một lượt HTTP có polling trên máy local, **không phải benchmark** hoặc kết luận hiệu năng 10.000 commit.

## 4. Lỗi đã phát hiện và xử lý

Sửa kiểm tra JSON để từ chối object dư sau output; bổ sung môi trường Lizard vào Docker/CI; ghim phiên bản pipeline/tool và author aliases trong effective config. Trong test, sửa bộ chọn nút đăng ký bị trùng khi có nhiều repository. Khi rà ảnh mobile, sửa bảng thành vùng cuộn ngang để đường dẫn dài không ép cột thành từng ký tự. Các lần lỗi môi trường PostgreSQL/sandbox được xử lý riêng, không tính là test đạt.

## 5. Phần chưa nghiệm thu

Provider thật/data-retention/model budget còn do Tưởng cấu hình; production auth chưa có nên thao tác ghi/AI chỉ bật trong môi trường kiểm soát. Chưa deploy/merge thay nhóm. Kiểm tra staging cũ: frontend 410, capabilities backend 404, lần health timeout; không dùng làm minh chứng deploy tuần 8. Docker local timeout; CI lưu trạng thái cụ thể tại PR/bộ ZIP. Nhóm vẫn cần chốt thực nghiệm và GVHD/nhóm trưởng review. Không tự ghi đã có người dùng thật hoặc đồng thuận chưa được cung cấp.

Toản cần đọc, chạy lại các bước cần thiết và giải thích mã được AI hỗ trợ trước khi nộp. [AI log](ai-usage-toan-week08.md), [minh chứng](evidence/verification.md), [bàn giao](ban-giao-tuong.md).
