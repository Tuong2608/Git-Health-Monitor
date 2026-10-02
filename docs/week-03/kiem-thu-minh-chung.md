# Kết quả kiểm thử và minh chứng ngày 30/09/2026

Người thực thi các kiểm tra trong phiên này: AI qua công cụ local theo yêu cầu Toản. Đây là bằng chứng sản phẩm đã được kiểm tra, **chưa phải bằng chứng Toản tự chạy hoặc làm chủ mã**.

Môi trường: Windows, Java Microsoft 21.0.10, PostgreSQL 18.3, Node 24.11.1, npm 11.6.2; Spring Boot 4.1.1, React 19.2.8, Recharts 3.10.1, Playwright 1.63.0. Database test riêng, không tác động database staging.

| Kiểm tra | Kết quả cuối | Phạm vi |
|---|---|---|
| Maven `verify` | BUILD SUCCESS, 22 test, 0 fail/error/skip | Context 1; URL 16; đăng ký tắt 1; API 4 |
| PostgreSQL/Flyway | V1 `create tracked repository`, success=true | Migration thật; JPA validate schema |
| ESLint | 0 lỗi | Mã frontend hiện có |
| TypeScript/Vite build | Thành công | Bundle chính khoảng 226,22 kB, biểu đồ riêng khoảng 352,86 kB, chưa gzip |
| npm audit | 0 vulnerabilities tại thời điểm chạy | Dependency frontend theo lockfile; không đại diện audit Java/secret/license |
| Playwright | 4 passed, 0 skipped, khoảng 4,9 giây | 3 test UI dùng API mock; 1 test live React→API→PostgreSQL |
| Desktop/mobile | Đã mở và xem 2 ảnh; test không tràn ngang ở 375px | Minh chứng trong `evidence/` |
| Git whitespace check | `git diff --check` không báo lỗi | Không phải review nội dung hoặc secret scan |
| Compose config | `docker compose config --quiet` chấp nhận cấu hình | Chỉ kiểm tra cấu hình, chưa build/chạy container; có cảnh báo không đọc được Docker config của tài khoản |

## Những trường hợp đã kiểm tra

- Chuẩn hóa hoa/thường, `.git`, slash cuối; từ chối protocol/host/userinfo/port/query/fragment/path không phù hợp.
- POST hợp lệ trả 201, đọc Location được; URL chuẩn hóa trùng trả 409.
- Body sai/rỗng, phân trang ngoài giới hạn trả 400; id không có trả 404.
- Cờ đăng ký tắt trả 403; preflight origin localhost và 127.0.0.1 được chấp nhận, origin lạ bị từ chối.
- UI hiển thị rỗng, lỗi trùng, lỗi mạng; biểu đồ được gắn nhãn minh họa.
- Test live thêm URL vào database test, tải lại vẫn thấy dữ liệu. Đây là kiểm tra lưu trữ, không kiểm tra URL repo tồn tại hoặc ingestion.

## Chạy lại

Theo README ở root. Backend verify phải trỏ tới PostgreSQL test. Browser test live cần backend port 8080 và `APP_REGISTRATION_ENABLED=true`, sau đó đặt `GHM_LIVE_BACKEND=1` khi chạy `npm run test:e2e`.

Các test hiện tại thêm dữ liệu có tên riêng vào database test. Chúng không tự xóa database và không được trỏ vào DB production. Mặc định CI chỉ chạy 3 test browser mock, 1 live test skip có lý do; test API PostgreSQL thật chạy trong backend job.

## Lần lỗi đã được sửa

1. ESLint bắt mất error cause; thêm `{cause: error}` rồi lint lại.
2. Test browser live thất bại vì CORS origin 127.0.0.1 chưa được phép. Probe trực tiếp ghi 403 `Invalid CORS request`, còn localhost trả 201. Thêm đúng origin local và regression test, không mở wildcard.
3. Thông báo mọi 403 đều là đăng ký tắt gây hiểu sai; thay fallback phù hợp.
4. Windows giữ file JAR đang chạy làm bước repackage thất bại. Dừng đúng backend thử nghiệm, chạy verify lại thành công.

## Chưa kiểm tra, không được suy ra là đã đạt

- GitHub Actions run của bản sửa; Docker build/Compose; Gitleaks; static analysis Java/duplication; coverage và tỷ lệ tự động hóa trên toàn đồ án.
- Deployment của mã mới, monitoring/rollback production, phân quyền đầy đủ và khả năng chịu tải.
- Ingestion, metric, snapshot, scheduler, AI provider và NFR03 10.000 commit/10 phút.
- Phỏng vấn, user study, SUS và đóng góp/cách hiểu cá nhân của Toản.

22 test backend cộng 4 test browser không chứng minh đã đạt mức 5 TC2.5; cần đủ tầng, coverage, truy vết và toàn bộ phạm vi cam kết ở giai đoạn sau.

## Kiểm chứng lại ngày 02/10/2026

Sau khi tích hợp cập nhật `43d614f` của Tưởng, mã chức năng `165df33` đạt Maven verify 22 test, 0 failures/errors/skipped; lint/build frontend đạt; npm audit 0 vulnerabilities; Playwright 4 passed (4,9 giây), có live PostgreSQL và kiểm tra màn hình 375px. Ảnh `evidence/week3-desktop.png` và `week3-mobile.png` được cập nhật từ lần chạy này. Docker Engine local chưa chạy; kiểm chứng container/secret scan được theo dõi riêng qua CI trong biên bản hoàn tất.
