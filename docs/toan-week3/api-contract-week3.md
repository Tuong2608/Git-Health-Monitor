> Bản bàn giao trước tích hợp, được giữ để truy vết. Trạng thái cập nhật ngày 02/10 và PR/CI xem [hồ sơ hiện hành](../week-03/README.md). Các dòng “chưa commit/chưa học” bên dưới phản ánh thời điểm bản cũ.

# Contract API tuần 3

Ngày 30/09/2026. Đây là contract của phần thực hành hiện có. Các route job ở cuối tài liệu chỉ là đề xuất bàn giao, chưa được cài đặt.

## Đã cài đặt

Base path: `/api`. JSON UTF-8. Chỉ `https://github.com/owner/repository`, có thể kèm `.git` hoặc dấu `/` cuối; bỏ khoảng trắng hai đầu, chuẩn hóa owner/name chữ thường để chống trùng. Từ chối query, fragment, userinfo, port tường minh, protocol khác, subpath `/tree/...`, URL giả hostname và path mã hóa.

### POST /api/repositories

Body: `{"url":"https://github.com/Tuong2608/Git-Health-Monitor"}`.

- `201 Created`: lưu thành công, `Location: /api/repositories/{id}`.
- Response: `id` (number), `url`, `owner`, `name`, `createdAt` (ISO timestamp).
- `400`: JSON lỗi, url thiếu/rỗng/quá 512 ký tự/sai cú pháp.
- `403`: cờ `APP_REGISTRATION_ENABLED` đang tắt (mặc định).
- `409`: URL chuẩn hóa đã có hoặc ràng buộc dữ liệu xung đột.
- Chưa gọi GitHub, chưa clone, chưa tự phân tích. Không tạo snapshot hay thay đổi repo nguồn.

### GET /api/repositories?page=0&size=20

Trả mảng repository, sắp xếp id giảm dần. `page` ≥0; `size` từ 1–100, mặc định 20. Trang rỗng trả `[]`, không phải lỗi. Phiên bản nhỏ này chưa trả total/count; nút Sau có thể dẫn đến trang rỗng nếu trang trước vừa đủ 20 dòng.

### GET /api/repositories/{id}

Trả repository hoặc `404` nếu không tồn tại.

### GET /api/health

Giữ hành vi skeleton cũ: chuỗi `OK`, HTTP 200. Đây là liveness đơn giản, **không chứng minh database hoặc pipeline phân tích khỏe**. Readiness/monitoring chi tiết thuộc việc cần bổ sung.

## Acceptance criteria và truy vết phần tuần 3

| Mã cục bộ | Tiêu chí | Minh chứng test |
|---|---|---|
| W3-AC01 | URL hợp cú pháp và đăng ký bật → 201, có id và lưu DB | RepositoryApiTests.createReadAndRejectCanonicalDuplicate |
| W3-AC02 | Khác hoa/thường, `.git` hoặc slash cuối vẫn nhận diện trùng → 409 | GitHubRepositoryUrlTests + RepositoryApiTests |
| W3-AC03 | URL không được hỗ trợ hoặc body lỗi → 400, không lộ stack trace | GitHubRepositoryUrlTests + RepositoryApiTests.invalidInputsAreClientErrors |
| W3-AC04 | GET danh sách/chi tiết; id không có → 404; phân trang sai →400 | RepositoryApiTests |
| W3-AC05 | UI có trạng thái tải/rỗng/lỗi; lỗi đăng ký không làm mất danh sách hiện có | repositories.spec.ts và kiểm tra mã |
| W3-AC06 | Biểu đồ giả phải có nhãn, không ghép vào dữ liệu thật | repositories.spec.ts |
| W3-AC07 | Tải lại trang vẫn thấy bản ghi vừa tạo qua backend thật | live.spec.ts, bật GHM_LIVE_BACKEND=1 |
| W3-AC08 | Tắt đăng ký → 403 | RegistrationDisabledTests |

Mã W3-AC không thay số use case hiện có. `docs/usecases-draft.md` và đề cương v3 dùng danh sách/số UC khác nhau; nhóm cần thống nhất bảng đối chiếu ở tuần 4 thay vì tự ghi đè.

## Đề xuất cho tuần 4, chưa cài đặt

`POST /api/repositories/{id}/analyses` → `202 Accepted`, `{jobId,status:"QUEUED"}` và Location `/api/jobs/{id}`. Mục tiêu phản hồi p95 ≤2 giây cần đo, chưa được xác nhận đạt.

`GET /api/jobs/{id}` → `{id,repositoryId,status,progress,errorCode}`; status dự kiến QUEUED/RUNNING/SUCCEEDED/FAILED. Repository không tồn tại →404; job đang chạy cho cùng repo →409 theo quy tắc cần nhóm chốt. Không giữ HTTP request tới khi phân tích xong.

Given job lỗi/timeout, Then không công bố snapshot dở dang, có nguyên nhân lỗi an toàn cho client. Restart phải xử lý job RUNNING bị bỏ lại; cần chống job trùng. DTO của pipeline giữa Tưởng và Toản phải chốt trước khi code hai phía.

`GET /api/repositories/{id}/snapshots`, `GET /api/snapshots/{id}/hotspots`, `GET /api/repositories/{id}/alerts` là route dự kiến; chưa có handler. Không ghi các route này là sản phẩm đã hoàn thành.
