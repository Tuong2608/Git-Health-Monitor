# Báo cáo tuần 9 — Trần Quang Toản

MSSV: 23110158. Dự án: Git Health Monitor. Ngày thực hiện: 12/11/2026. Nhánh: `feat/toan-week-9`, phát triển từ main `5732d7b`. Báo cáo được soạn với AI hỗ trợ, Toản cần đọc và xác nhận trước khi nộp.

## Phạm vi theo phân công

STT 29: phối hợp chọn 3–5 dự án Java OSS. STT 31: RQ2 theo dõi xu hướng qua snapshot, thời gian tuần 9–10. Tuần 9 hoàn thành bộ công cụ, quy tắc thực nghiệm, dữ liệu pilot và đánh giá khả năng so sánh. Không nhận phần RQ1 của Tưởng là kết quả cá nhân; chưa tính hiệu năng/người dùng tuần 10 là đã hoàn thành.

## Công việc và sản phẩm

| Nhóm việc | Đã thực hiện | Minh chứng |
|---|---|---|
| Backend/công cụ phân tích | Thêm CLI HistoricalSnapshotExport dùng JGit → Lizard → RepositoryMetricCalculator; pin SHA lịch sử; kiểm tra tổ tiên có timestamp tương lai; xuất JSON không chứa mã nguồn/email contributor | Mã Java và 4 test lịch sử |
| Công cụ RQ2 | Tách freeze/run, giữ manifest; so sánh đúng repo, lineage, phiên bản, cấu hình, độ dài cửa sổ; phân biệt null/0; chặn coverage thiếu; Decimal ở biên ngưỡng | tools/experiments/run_rq2.py và rq2_trends.py |
| Nghiệp vụ nghiên cứu | Chọn 3 dự án dạng hướng dẫn/ứng dụng/thư viện, lịch 3 mốc và cửa sổ 180 ngày trước khi đo; lưu cả cửa sổ rỗng; phân biệt pilot và nghiên cứu có nhãn | [Protocol](rq2-protocol.md), [manifest](evidence/frozen-manifest.json) |
| Thực nghiệm | Chạy 9 snapshot, tạo 6 comparison; xuất bảng kết quả và ví dụ kèm link commit | [Kết quả](evidence/pilot/results.md) |
| Kiểm chứng | Backend 119 ca duy nhất đạt; Python 17 ca đạt; chạy lại và khớp hash 15/15 file dữ liệu | [Verification](evidence/verification.md) |
| Frontend | Sử dụng hợp đồng trend/comparison đã có để xác định điều kiện hợp lệ; tuần này không thêm màn hình hay sửa React | Không nhận lại thành quả UI tuần 8 là code mới tuần 9 |
| Báo cáo/handoff | Runbook, phân tích vấn đề, tài liệu học, AI log theo từng phần và nội dung PR | Thư mục docs/week-09 |

## Kết quả định lượng và ý nghĩa

Ba dự án: gs-rest-service, spring-petclinic và commons-cli. 9/9 snapshot hoàn thành, không vượt giới hạn 10.000 commit. Tổng 6/6 cặp liền kề bị BLOCKED bởi INCOMPLETE_COVERAGE; số cảnh báo hợp lệ là **N/A**, không phải 0 cảnh báo đúng hoặc 100% phát hiện.

Quần thể có hotspot/Java ở mốc đầu lần lượt 4/6, 20/40, 6/52. Đây là phát hiện quan trọng: việc chạy xong pipeline chưa bảo đảm có đủ dữ liệu cho đánh giá theo thời gian. Bảng đầy đủ nằm trong [kết quả](evidence/pilot/results.md).

Ví dụ chẩn đoán gs-rest-service ở cặp 2025→2026: GreetingControllerTests.java có frequency 0→2, CCN giữ 1, hotspot 0.500000→0.707107; có hai SHA commit đối chiếu. Tuy vượt ngưỡng pilot +0.15, cả snapshot thiếu coverage nên **không** báo cáo thành alert hợp lệ. Một ví dụ khác commons-cli có frequency 9→0, CCN giữ 7, hotspot giảm khoảng 0.444603; không được diễn giải là đã sửa lỗi, vì commit có thể đã rời cửa sổ và quần thể đã đổi.

Test kiểm soát tạo hai file Java rồi tăng số nhánh và tần suất sửa A.java: coverage đầy đủ, frequency 1→2, CCN 1→4, hotspot 0→1, có SHA bằng chứng. Ca này kiểm chứng đường xử lý kỹ thuật, không đại diện độ chính xác trên dự án thực.

## Những chỗ chưa ổn và đề xuất xử lý

1. **Lineage qua merge:** thuật toán hiện tại thận trọng khi file không có cùng danh tính ở tất cả cha. Số file mơ hồ cao, nhất là commons-cli. Cần phân loại merge có nguồn gốc xác định và merge thực sự mơ hồ, thêm fixture trước khi sửa; không tự ép nối theo đường dẫn.
2. **Coverage quá hạn chế cho thực nghiệm:** LINEAGE_AMBIGUOUS và NO_FUNCTIONS xuất hiện trong dữ liệu. Cần review với Tưởng cách xử lý file không có hàm/quần thể đủ dữ liệu; mọi thay đổi phải version hóa, chạy lại riêng, không nối với pilot cũ.
3. **Hotspot là điểm tương đối:** điểm thay đổi khi quần thể/khung thời gian đổi. Báo cáo phải kèm frequency, CCN, population và commit; chưa đủ cơ sở khẳng định tác động nhân quả hay lỗi phần mềm.
4. **Thiết kế mẫu:** mẫu thuận tiện, hai dự án đã dùng tuần 8, các mốc cách nhau một năm. Chưa đủ đại diện cho hiệu quả giám sát liên tục; chưa phải tập RQ1 chọn mù.
5. **Triển khai cũ:** chưa có URL staging mới theo thông tin nhận từ người dùng. Thực nghiệm tuần 9 chạy local bằng CLI, không ghi là đã kiểm thử staging. Không gọi API AI thương mại trong RQ2.

## Kế hoạch tiếp nối

Gửi bộ dữ liệu và vấn đề coverage cho Tưởng review; thống nhất protocol chung trước khi đưa kết quả vào kết luận chính thức. Tuần 10 hoàn thiện RQ2 bằng bộ tình huống có người đối chiếu, lịch snapshot phù hợp và đánh giá giải thích; thực hiện hiệu năng theo STT 32, phối hợp người dùng thật theo STT 33. Không tự điền dữ liệu khảo sát, đánh giá của người dùng hay xác nhận duyệt PR.

Đề xuất cập nhật bảng tiến độ: STT 29 “Đang làm — đã chọn và chạy pilot 3 dự án, chờ chốt bộ nghiên cứu chung”; STT 31 “Đang làm — hoàn thành công cụ và pilot tuần 9, phát hiện 6/6 cặp bị chặn bởi coverage; tiếp tục tuần 10”. Workbook của nhóm được giữ nguyên để nhóm trưởng kiểm tra trước khi cập nhật.
