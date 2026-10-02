> Bản bàn giao trước tích hợp, được giữ để truy vết. Trạng thái cập nhật ngày 02/10 và PR/CI xem [hồ sơ hiện hành](../week-03/README.md). Các dòng “chưa commit/chưa học” bên dưới phản ánh thời điểm bản cũ.

# Kiểm tra và bàn giao staging tuần 3

## Kết quả kiểm tra chỉ đọc ngày 30/09/2026

| Thành phần | URL được Toản cung cấp | Quan sát |
|---|---|---|
| Backend | https://git-health-monitor.onrender.com/api/health | HTTP 200, body `OK` |
| Frontend | https://git-health-monitor-dzgm1qfj8-tuong9.vercel.app | Chuyển sang trang `vercel.com/login`; HTTP cuối là 200 của trang đăng nhập |

Backend health chỉ xác nhận endpoint skeleton có thể truy cập. Không suy ra frontend công khai, database/analysis hoạt động hay mã mới đã deploy. Không có deploy id/run URL cho thay đổi tuần 3 trong phiên này.

Theo [Vercel Deployment Protection](https://vercel.com/docs/deployment-protection), phạm vi bảo vệ có thể khác giữa deployment URL và production domain. Chủ dự án cần lấy đúng domain công khai dự định cho GVHD, kiểm tra bằng cửa sổ ẩn danh. Không cần tắt bảo vệ tất cả project hoặc gửi token trong chat.

## Cấu hình bắt buộc trước khi triển khai bản sửa

Backend mới cần PostgreSQL. Nếu merge khi Render tự deploy nhưng DB_* chưa sẵn sàng, backend có thể không khởi động.

### Render

- Giữ Docker context đúng với backend/Dockerfile (context `backend`).
- Cấp database PostgreSQL theo hạ tầng nhóm đã chọn, tránh tự tạo dịch vụ tính phí ngoài kế hoạch.
- Đặt `DB_URL` dạng JDBC PostgreSQL, `DB_USERNAME`, `DB_PASSWORD` qua environment/secrets của dịch vụ. URL do nhà cung cấp đưa có thể phải chuyển sang dạng JDBC, không dán credential vào repo.
- Đặt `APP_ALLOWED_ORIGINS` bằng origin frontend chính xác, phân cách dấu phẩy nếu nhiều origin. Không thêm dấu `/` cuối, không dùng `*` cho tiện.
- `APP_REGISTRATION_ENABLED=false` cho preview công khai chỉ đọc. Bật true chỉ trong phiên thử được kiểm soát cho đến khi có auth/quota. Prototype chưa có cơ chế tài khoản đầy đủ.
- `PORT` được map tới server.port. Giữ health path `/api/health`, nhưng thêm readiness thật ở giai đoạn sau.
- Xác nhận phương thức auto-deploy hiện có và ghi commit SHA/deploy id; chưa kiểm tra được cấu hình account từ URL public.

### Vercel

- Root directory `frontend`, install `npm ci`, build `npm run build`, output `dist`, Node 24 phù hợp CI.
- Đặt `VITE_API_BASE_URL=https://git-health-monitor.onrender.com` rồi build/deploy lại. Đây là giá trị đưa vào bundle, không phải biến backend động.
- Không đặt API key, mật khẩu DB hoặc token trong `VITE_*`.
- Dùng domain truy cập phù hợp cho GVHD. Link preview bị bảo vệ không đủ chứng minh staging công khai.

### Sau triển khai

1. Mở frontend bằng phiên không đăng nhập, kiểm tra đúng giao diện tuần 3 và nhãn biểu đồ mẫu.
2. GET health, GET repositories; bật đăng ký trong phiên kiểm soát rồi thêm URL và reload kiểm tra lưu DB.
3. Thử URL sai, trùng, server lỗi. Ghi trạng thái, không giả kết quả.
4. Lưu commit SHA, CI run URL, deploy id/URL, thời điểm bắt đầu/kết thúc; tính thời gian commit→staging từ bằng chứng thật.
5. Ghi lại cách quay về deployment trước. Nếu đã áp migration không tương thích, rollback mã không đồng nghĩa rollback dữ liệu; tuần 3 chỉ thêm một bảng mới.

## CI hiện được chuẩn bị

`.github/workflows/ci.yaml` chạy backend + PostgreSQL, frontend lint/build/browser, dependency audit, secret scan rồi Docker build. Chưa có CD action điều khiển account Render/Vercel; provider có thể đã auto-deploy theo kết nối Git nhưng chưa có quyền kiểm chứng cấu hình.

Docker Engine không chạy trong môi trường kiểm tra hiện tại; Docker build/Compose và Gitleaks container **chưa được chạy local**. Không ghi CI xanh hoặc secret scan sạch trước khi có run thật.

## Việc chủ account cần bổ sung

- [ ] Domain frontend cho GVHD truy cập được.
- [ ] PostgreSQL + biến môi trường đã cấu hình cho backend mới.
- [ ] Link PR có Tưởng review và SHA merge.
- [ ] Run CI bản mới và deploy thành công gắn đúng SHA.
- [ ] Biên bản smoke test, rollback và URL minh chứng.
