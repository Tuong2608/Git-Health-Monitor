# Minh chứng tuần 7

Ngày 29/10/2026. Java 21, PostgreSQL 18.3. Database riêng: ghm_week7_test cho nhánh Toản, ghm_week7_compat cho checkout kết hợp. Không chạy tests trên staging.

| Kiểm chứng | Kết quả | Minh chứng |
|---|---|---|
| Nhánh Toản Maven verify | 58 tests, 0 failure/error/skip | [Tên test và tổng](backend-tests.json), [log build](backend-build-summary.txt) |
| Checkout kết hợp Tưởng 15f291b + backend Toản | 88 tests, 0 failure/error/skip | [Tên test](compatibility-tests.json), [log](compatibility-build-summary.txt), [overlay manifest](compatibility-overlay.json) |
| V1/V2→V3 | Giữ row cũ và số0, unknown lưu null có reason, 10 bảng | [SQL tái lập](verify-upgrade.sql), [log](migration-upgrade.txt) |
| HTTP + worker/scheduler | Enqueue→SUCCEEDED→snapshot→alert; lịch đến hạn chạy; retry giữ window | [HTTP exchanges](http-exchanges.jsonl), Week7WorkerHttpTests |
| API đọc | Scope, null, pagination, contribution, oriented coupling, trend, delta, readiness | Week7ApiTests, HTTP exchanges |
| Phân công | Task 23/24 của Toản và các nhiệm vụ Tưởng/chung | [Workbook extract](assignment-source.json) |

17 tests mới gồm 14 API/storage và3 worker HTTP; tests dùng adapter fixture chỉ khai báo trong test configuration. HTTP exchanges là kết quả gọi thật vào server test, không phải response viết tay, nhưng source metric là fixture. Không dùng làm bằng chứng chạy repo public. Có0 số liệu benchmark/SUS/nhãn nghiên cứu mới trong bộ này.

Tổng88 chứa tests trong58 và các test thêm trên nhánh Tưởng; không cộng hai con số. Checkout tương thích chỉ áp thay đổi backend của Toản lên nhánh Tưởng đã pin, giữ mã JGit và contract tests của Tưởng. Không merge nhánh Tưởng vào sản phẩm bàn giao.

Ca retry lần đầu lệch 7 giờ do fixture date literal thiếu timezone. Đã sửa sang ISO timestamp có Z, chạy lại toàn bộ và lưu kết quả cuối. Log tóm tắt chỉ trích test/build, bỏ khoảng trắng cuối dòng. Lần chạy API evidence dùng system property trỏ thư mục evidence; mặc định tests không ghi file ra workspace.
