# Kiểm chứng tuần 6

Ngày 22/10/2026. Backend chạy Java 21 và PostgreSQL 18.3 trên database thử riêng ghm_week6_test. Không kiểm thử trên staging.

| Kiểm tra | Kết quả | Minh chứng |
|---|---|---|
| Backend Maven verify | 41 tests, 0 failure/error/skip, build thành công | [Danh sách test](backend-tests.json), [tóm tắt build](backend-build-summary.txt) |
| Migration có row cũ | V1→V2 giữ repository và mặc định monitoring=false, 10 bảng; rollback | [Log](migration-upgrade.txt), [script](verify-upgrade.sql) |
| Evaluator | 8 tests đạt | [Log](experiment-tests.txt), [source tests](../../../tools/experiments/test_evaluate_rankings.py) |
| Phân công | Trích task 19/20/21/23 và SHA nguồn | [JSON](assignment-source.json) |
| Fixture nghiên cứu | Có kind=fixture, không phải kết quả nghiên cứu | [Input](../experiment/fixture-input.json), [Output](../experiment/fixture-output.json) |
| Dependency audit | Vite 8.3.0 được cập nhật 8.3.5; npm audit báo 0 vulnerabilities | [JSON audit](npm-audit.json) |

Các tests mới gồm 15 integration cases, 3 API cases và 1 default-disabled case. Kiểm tra worker dùng adapter fixture chỉ trong test; không đăng ký bean giả trong runtime. Trường hợp concurrent jobs dùng thread thật và PostgreSQL thật. Retry evaluation được mô phỏng bằng marker; không nhận đã kill/restart VM hoặc chạy load test nhiều instance.

Tái lập: cấu hình DB_* tới database dùng riêng cho test, chạy `backend/mvnw -B -ntp verify` (Windows mvnw.cmd). Test mới TRUNCATE các bảng ứng dụng, tuyệt đối không dùng database dữ liệu thật. Chạy `python -m unittest discover -s tools/experiments -v` cho công cụ đánh giá. Script SQL tạo schema riêng trong transaction rồi rollback.

Log lỗi ban đầu được dùng để sửa fixture, không tính là kết quả cuối. Các log bàn giao được bỏ khoảng trắng cuối dòng và chỉ trích build/test summary, không chép môi trường/secrets vào báo cáo.

CI đầu tiên ở c943067 bị chặn bởi frontend dependency audit. [Advisory GHSA-587h-7wmj-h5pm](https://github.com/advisories/GHSA-587h-7wmj-h5pm) liệt kê Vite 8.3.0–8.3.2 bị ảnh hưởng, bản vá từ 8.3.3. Đã chọn bản 8.3.5 trong cùng dòng, cập nhật các dependency kèm theo theo lockfile và kiểm tra lại. Không bỏ bước audit hoặc hạ ngưỡng kiểm tra.
