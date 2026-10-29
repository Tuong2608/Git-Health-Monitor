## Kết quả

Hoàn thiện lưu trữ/giám sát tuần 7 và triển khai REST API cho màn hình snapshot. Metric không đo được từ ingestion được giữ NULL kèm reason thay vì buộc thành 0. V3 additive giữ V1/V2, thêm retry provenance, kiểm tra claim context khi publish.

Bổ sung file detail, contribution và evidence phân trang, coupling hai chiều, trend và snapshot comparison có kiểm tra config/coverage. Thêm capabilities, job history và retry FAILED giữ window/config. Không expose contributor identity keys; không tạo adapter giả trong runtime.

## Kiểm chứng

- 58 backend tests đạt trên PostgreSQL 18.3, gồm 17 tests mới cho API/storage và worker HTTP/scheduler.
- Checkout thử gồm nhánh Tưởng 15f291b + backend tuần 7: 88 tests đạt. Chưa merge nhánh Tưởng vào PR.
- V1/V2 có dữ liệu→V3 giữ measured zero/repository cũ, hỗ trợ unknown null; rollback schema thử.
- Báo cáo, AI log, HTTP fixture evidence và hướng dẫn nối adapter trong docs/week-07.

## Phạm vi và review

Bảo trì phát sinh khi chạy CI: cập nhật riêng zod 4.6.5 → 4.6.7 trong frontend lockfile để sửa advisory GHSA-mrhx-c7xc-m3w5. Audit 0 cảnh báo, lint/build đạt; 3 UI tests đạt, 1 live test bỏ qua. Không đổi chức năng giao diện.

Task 23 tuần 6–7 và task 24 tuần 7–8 của Toản. Nhờ Tưởng review nullable fields, commitCount=selected non-merge commits, evidence.commits và semantics comparison/retry. Adapter Git/metric, Lizard và LLM còn thuộc phần Tưởng; chưa có pipeline repo public/staging/benchmark mới. Giới hạn API 365 ngày giữ nguyên, không đổi ingestion resource-limit policy hoặc quyết định lineage của Tưởng.
