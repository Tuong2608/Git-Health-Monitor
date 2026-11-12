## Vấn đề và thay đổi

RQ2 cần đo đúng snapshot lịch sử thay vì lấy complexity hiện tại ghép với Git history cũ. Bổ sung HistoricalSnapshotExport sử dụng pipeline thật, pin SHA và kiểm tra toàn bộ tổ tiên trước cutoff; bộ freeze/run/evaluate lưu provenance, ghép lineage và chặn so sánh khác repo/config/version/window hoặc thiếu coverage.

## Kết quả

- Pilot 3 dự án Java × 3 mốc, đủ 9 snapshot; 6/6 cặp BLOCKED vì coverage thiếu. Không trình bày các delta chẩn đoán thành alert hợp lệ hay kết quả dự đoán bug.
- Chạy lại khớp SHA-256 15/15 snapshot/comparison.
- Backend 119 ca duy nhất đạt (full verify 118, sau đó suite lịch sử 4 thay cho 3); Python 17 ca đạt.
- Có báo cáo, protocol, runbook, AI log, dữ liệu gốc và bàn giao trong docs/week-09.

## Cần review

Dataset là pilot thuận tiện, hai repo đã dùng tuần 8; chưa là tập RQ1 chọn mù. RQ2 kéo dài đến tuần 10. Cần review lineage qua merge và coverage/NO_FUNCTIONS; không đổi policy để làm đẹp số liệu. Chưa có staging mới hoặc nhãn thực nghiệm người dùng. Không sửa API runtime/frontend hay gọi provider thương mại.
