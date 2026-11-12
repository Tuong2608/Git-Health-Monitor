# Tuần 9 — Trần Quang Toản (23110158)

Đối chiếu `docs/BaoCaoTienDo_TLCN.xlsx` trên main `5732d7b` ngày 12/11/2026: STT 29 chọn 3–5 dự án Java là việc chung; STT 31 RQ2 là phần Toản, tuần 9–10. STT 30 RQ1 thuộc Tưởng; STT 32 hiệu năng và STT 33 người dùng thuộc tuần 10.

Đã bổ sung công cụ xuất snapshot lịch sử dùng đúng JGit/Lizard/bộ tính metric hiện có, bộ đánh giá thay đổi theo lineage, dữ liệu 3 dự án × 3 mốc và kiểm thử. Đây là **pilot mô tả có thể tái lập**, chưa phải kết luận hệ thống dự đoán đúng lỗi bảo trì.

## Đọc theo thứ tự

1. [Báo cáo cá nhân](bao-cao-tuan-09-toan.md).
2. [Giao thức và giới hạn](rq2-protocol.md), [đề xuất dữ liệu](dataset-proposal.json), [manifest đã cố định](evidence/frozen-manifest.json).
3. [Bảng kết quả và ví dụ có commit đối chiếu](evidence/pilot/results.md), [CSV số liệu](evidence/pilot/snapshot-summary.csv).
4. [Hướng dẫn chạy lại](runbook.md), [kiểm chứng](evidence/verification.md).
5. [Bàn giao Tưởng](ban-giao-tuong.md), [nhật ký AI](ai-usage-toan-week09.md), [lý thuyết áp dụng](ly-thuyet-va-ap-dung.md).

**Phát hiện chính:** chạy đủ 9/9 snapshot; cả 6/6 cặp bị chặn bởi coverage không đầy đủ. Không đổi `coverageComplete` thành true, không biến null thành 0, không bỏ dự án để làm đẹp số liệu. RQ2 vẫn ở trạng thái **Đang làm**, đúng kế hoạch kéo dài đến tuần 10. Staging chưa có URL mới theo thông tin đã nhận; chưa nghiệm thu triển khai công khai.
