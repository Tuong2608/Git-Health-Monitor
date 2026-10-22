# Kiểm chứng tuần 6

Ngày 22/10/2026. Backend chạy Java 21 và PostgreSQL 18.3 trên database thử riêng ghm_week6_test. Không kiểm thử trên staging.

| Kiểm tra | Kết quả | Minh chứng |
|---|---|---|
| Backend Maven verify | 41 tests, 0 failure/error/skip, build thành công | [Danh sách test](backend-tests.json), [tóm tắt build](backend-build-summary.txt) |
| Migration có row cũ | V1→V2 giữ repository và mặc định monitoring=false, 10 bảng; rollback | [Log](migration-upgrade.txt), [script](verify-upgrade.sql) |
| Evaluator | 8 tests đạt | [Log](experiment-tests.txt), [source tests](../../../tools/experiments/test_evaluate_rankings.py) |
| Phân công | Trích task 19/20/21/23 và SHA nguồn | [JSON](assignment-source.json) |
| Fixture nghiên cứu | Có kind=fixture, không phải kết quả nghiên cứu | [Input](../experiment/fixture-input.json), [Output](../experiment/fixture-output.json) |

Các tests mới gồm 15 integration cases, 3 API cases và 1 default-disabled case. Kiểm tra worker dùng adapter fixture chỉ trong test; không đăng ký bean giả trong runtime. Trường hợp concurrent jobs dùng thread thật và PostgreSQL thật. Retry evaluation được mô phỏng bằng marker; không nhận đã kill/restart VM hoặc chạy load test nhiều instance.

Tái lập: cấu hình DB_* tới database dùng riêng cho test, chạy `backend/mvnw -B -ntp verify` (Windows mvnw.cmd). Test mới TRUNCATE các bảng ứng dụng, tuyệt đối không dùng database dữ liệu thật. Chạy `python -m unittest discover -s tools/experiments -v` cho công cụ đánh giá. Script SQL tạo schema riêng trong transaction rồi rollback.

Log lỗi ban đầu được dùng để sửa fixture, không tính là kết quả cuối. Các log bàn giao được bỏ khoảng trắng cuối dòng và chỉ trích build/test summary, không chép môi trường/secrets vào báo cáo.
