# Minh chứng tuần 8

Ngày 05/11/2026. Java 21, PostgreSQL 18.3, Lizard 1.24.3. Test và demo dùng hai database riêng: ghm_week8_test, ghm_week8_live. Không chạy test phá dữ liệu trên staging.

| Kiểm chứng | Kết quả / tệp |
|---|---|
| Plan | [Dòng workbook và base SHA](assignment-source.json) |
| Backend | [111 tests duy nhất và tên từng ca](backend-tests.json), [109 full verify](backend-verify-summary.txt), [2 availability](availability-summary.txt) |
| Tích hợp có kiểm soát | [HTTP exchanges](integration-http.jsonl): Git fixture thật qua JGit/Lizard/DB, LLM fake chỉ trong test; preview body bỏ khỏi log |
| Repo công khai | [Kết quả/SHA/window](public-repositories-results.json), [HTTP](public-repositories-http.jsonl) |
| UI | [11 tests local đạt](frontend-tests.txt), gồm 2 live cases; default CI không bật 2 ca live |
| Desktop/mobile thật | [Desktop](week8-live-desktop.png), [mobile](week8-live-mobile.png), snapshot public petclinic |
| Desktop/mobile fixture | [Desktop](week8-desktop-fixture.png), [mobile](week8-mobile-fixture.png), dùng kiểm tra null/consent; không phải metric thật |
| Dependency | [Audit](frontend-audit.json): 10 moderate của minimatch tooling, 0 high/critical |
| Migration | [SQL V1–V4](verify-upgrade.sql), [kết quả](migration-upgrade.txt): snapshot/zero/null giữ nguyên; schema tạm rollback |
| Staging chỉ đọc | [HTTP status](staging-readonly-check.json); không triển khai trong phiên này |

Không cộng 109 và 111 thành hai tập độc lập. 111 là tổng duy nhất sau full verify và 2 test availability bổ sung. Test integration mới 8 ca; test guard mới 2 ca. HTTP fixture và public repository evidence lưu riêng. Không ghi public repo là dữ liệu đã label để nghiên cứu hoặc ghi engine fake provider là AI thật.

Thời gian public runs gồm clone/fetch, queue, polling và API đọc, chỉ là quan sát local một lượt. gs-rest-service/petclinic có coverageComplete=false, vẫn giữ reason/quality flags. Không hạ giới hạn hoặc thay lineage policy để tạo số đẹp.

Compose config validate đạt. Docker Desktop local không phản hồi trong 20 giây; CI package chạy kiểm chứng image/Compose riêng. Trạng thái đúng commit cuối lưu trong CI_STATUS.json của ZIP và PR, không dùng kết quả của commit cũ thay cho bản bàn giao.
