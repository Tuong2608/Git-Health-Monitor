# Tiếp thu nhận xét GVHD: xác thực API contract

Cập nhật 16/10/2026. Nhận xét tuần 4: “Cần xác thực API contract hoạt động phù hợp và thoả các ràng buộc thực tế thiết kế”.

## Kết quả

`mvn -B -ntp verify`: **40 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS**. Gồm 16 ca URL parser, 23 ca HTTP và 1 ca khởi tạo Spring context. Có 18 ca bổ sung trong RepositoryContractTests. Kiểm tra dùng HTTP server thật và PostgreSQL 18.6 riêng trên máy local, Java 21.0.10, Maven 3.9.16. Flyway/JPA hoạt động trên database kiểm thử, không dùng dữ liệu staging/production.

Mã nền: 340515a, cộng bản sửa TrackedRepository và test đi kèm báo cáo này. [Kết quả từng suite/ca](evidence/api-contract/test-results.json). [29 request/response của suite mới](evidence/api-contract/http-exchanges.jsonl). Số test khác số request; tệp HTTP không bao gồm request của các suite cũ. Minh chứng không chứa mật khẩu hoặc properties môi trường từ XML Surefire.

## Ma trận kết quả

| Ràng buộc | Cách xác thực | Kết quả |
|---|---|---|
| Chuẩn hóa URL, loại URL ngoài phạm vi | 16 ca GitHubRepositoryUrlTests | Đạt |
| POST 201, Location, DTO đủ 5 trường; GET 200 khớp | HTTP và đọc trực tiếp bản ghi DB | Đạt sau sửa timestamp |
| Thiếu/rỗng/sai JSON, URL sai hoặc quá dài | HTTP 400, số bản ghi không tăng | Đạt |
| URL trùng sau chuẩn hóa | HTTP 409, không lưu thêm | Đạt |
| Hai yêu cầu đồng thời cùng repository | Một 201, một 409, đúng một bản ghi | Đạt |
| page âm/không phải số, size 0/-1/101/không phải số | HTTP 400 | Đạt |
| size 1/100, thứ tự ID giảm dần, trang ngoài dữ liệu | HTTP 200, mảng và giới hạn đúng, trang rỗng [] | Đạt |
| ID không tồn tại/sai kiểu | HTTP 404/400 | Đạt |
| Cờ đăng ký tắt | RegistrationDisabledTests: HTTP 403 | Đạt |
| CORS local và origin ngoài cấu hình | RepositoryApiTests | Đạt |
| Health | HTTP 200, body OK | Đạt, chỉ liveness |
| Nội dung lỗi của các ca bổ sung | Không có stackTrace, tên lớp/framework, JDBC URL hoặc SQLState | Đạt trong các mẫu đã thử |

## Lỗi phát hiện và sửa

Lần chạy trước sửa: 40 tests, 1 failure. `createdAt` ở POST là `2026-10-15T15:44:31.840602100Z`, GET đọc từ PostgreSQL là `2026-10-15T15:44:31.840602Z`. Java tạo Instant có nanosecond trong khi DB lưu microsecond. Entity nay chuẩn hóa về microsecond trước khi lưu. Test vẫn so sánh toàn bộ DTO POST/GET, không nới lỏng assertion. Lần chạy sau sửa đạt 40/40.

Một lần chạy ngày 16/10 không kết nối được DB do tiến trình DB kiểm thử đã dừng giữa hai phiên; đã khởi động lại cluster riêng trước lần chạy thành công. Đây không phải kết quả kiểm chứng API. Docker không khởi động được nên dùng PostgreSQL portable, không thay đổi môi trường triển khai.

## Phạm vi và phần tiếp tục

Đã kiểm chứng bốn route hiện có: POST /api/repositories, GET /api/repositories, GET /api/repositories/{id}, GET /api/health. URL parser chỉ kiểm tra cú pháp; không chứng minh repository tồn tại hoặc public trên GitHub. CORS không thay thế xác thực. Chưa xác minh lại staging/CI hay thực hiện kiểm thử tải.

API analysis/job/snapshot/alert/AI chưa triển khai nên chưa có kết quả runtime. Trước cài đặt cần thống nhất tie-break ranking, observation/prediction window, metadata đánh giá alert và binding preview/consent AI. Bộ test hiện tại không thay thế kiểm thử tích hợp toàn pipeline ở giai đoạn sau.

## Chạy lại

1. Tạo PostgreSQL/database dùng riêng cho test. Bộ test tạo bản ghi tổng hợp; không dùng database thật của nhóm.
2. Dùng Java 21, đặt DB_URL, DB_USERNAME, DB_PASSWORD theo database thử nghiệm.
3. Từ thư mục backend chạy `mvn -B -ntp verify` (hoặc Maven wrapper của dự án).
4. Xem `target/surefire-reports` và `target/contract-http-exchanges.jsonl`. Chỉ trích kết quả cần thiết; không đưa properties/mật khẩu lên Git.

## Gợi ý trình bày với GVHD

“Sau nhận xét tuần 4, nhóm bổ sung kiểm thử API đang triển khai với PostgreSQL riêng. Bộ 40 test đạt, bao gồm mã phản hồi, dữ liệu trả về, dữ liệu lưu, input lỗi và hai yêu cầu đăng ký trùng đồng thời. Kiểm thử phát hiện sai khác độ chính xác thời gian giữa POST và GET; bản sửa đã được kiểm thử lại. Các API phân tích và AI sẽ kiểm chứng tiếp khi triển khai.”

Khi review, đọc một cặp POST/GET thành công và một ca 409 trong minh chứng; đối chiếu assertion đếm bản ghi DB. Chưa ghi nhận sinh viên đã tự thực hiện phần review thủ công này.
