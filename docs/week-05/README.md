# Bàn giao chung tuần 5

Cập nhật 15/10/2026. Main 188968c đã có phần Toản (PR #4). Nhánh tích hợp giữ phần Tưởng 0518e52 và bản căn chỉnh tuần 4. Thiết kế để review, chưa phải runtime đã triển khai.

- [Bàn giao Tưởng](ban-giao-tuong.md): [thuật toán](../algorithm-design.md), [giám sát](../monitoring-design.md), [AI interface](../ai-review-scope.md), [prompt](../prompts/ai-review-v1.md).
- [Bàn giao Toản](ban-giao-toan.md): [database 10 bảng](database-design.md), [ERD](erd.png), [wireframe](wireframes/index.html), [minh chứng](evidence/verification.md).
- Thực nghiệm: [đề xuất Tưởng](../experiment-design.md), [đề xuất Toản](experiment-design.md). Chưa chốt phương án chung.

## Đối chiếu ngày 15/10

Hai phần cùng dùng snapshot identity gồm repo/SHA/window/config, logical file qua rename và metric thiếu không thay bằng 0. Toản có log 12 nhóm kiểm tra SQL và 18 kiểm tra wireframe offline; phiên tổng hợp này đọc lại minh chứng, không chạy lại hoặc coi là kiểm thử sản phẩm.

Các điểm còn mở trước cài đặt:
1. Tưởng đề xuất cutoff 80/20 với observation 180 ngày; Toản đề xuất observation 180/prediction 30 ngày. Chốt trước chọn dataset/chạy đánh giá.
2. Tie-break ranking: Tưởng dùng logicalFileId, Toản dùng path. Cần một quy tắc thống nhất cho cả ba ranking.
3. Tưởng ưu tiên Precision/Recall; Toản đã đặc tả thêm AP/MAP. Chốt phạm vi báo cáo, không bỏ kết quả không thuận lợi.
4. Alert evaluation marker/rule-set version của monitoring cần đối chiếu schema Toản trước migration; chưa coi SQL constraints là chứng minh recovery/lease.
5. Preview/consent AI cần hợp đồng binding context; wireframe offline chưa thực hiện chọn dòng và provider thật.

Phần thiết kế thuật toán, giám sát, DB và UI có bản bàn giao. AI và thực nghiệm tiếp tục tuần 6. Không xác nhận sinh viên đã tự thực hiện mọi thao tác kỹ thuật hay nhóm/GVHD đã nghiệm thu.
