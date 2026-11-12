# Nhật ký sử dụng AI — tuần 9

Người phụ trách: Trần Quang Toản (23110158). Ngày: 12/11/2026. Công cụ: Codex trong ứng dụng desktop. Không suy đoán tên model/phiên bản khi môi trường không cung cấp. Nhật ký ghi hoạt động hỗ trợ thật trong phiên làm việc, không dùng để chứng nhận mức hiểu biết cá nhân. Không chép nguyên văn prompt ban đầu.

## 1. Phân tích nhiệm vụ và thiết kế phương pháp

- Mục tiêu: đối chiếu kế hoạch GitHub sau khi tích hợp tuần 8; xác định phần Toản trong tuần 9.
- Dữ liệu vào: workbook tiến độ, protocol tuần 5–6, mã ingestion/metric/monitoring, ghi chú tích hợp của Tưởng.
- Hỗ trợ của AI: xác định STT 29/31, đề xuất pilot ba dự án và ba mốc lịch sử; tách RQ2 mô tả khỏi RQ1 có nhãn.
- Kiểm chứng: đọc trực tiếp các hàng kế hoạch; lưu proposal trước khi chạy; người dùng cho phép tự hoàn thiện, chưa có xác nhận protocol của nhóm trưởng.
- Kết quả: dataset-proposal.json, rq2-protocol.md. Giới hạn: mẫu thuận tiện và hai repo đã được dùng ở tuần 8.

## 2. Cài đặt công cụ xuất snapshot lịch sử

- Mục tiêu: đo đúng cây mã nguồn ở mốc lịch sử, tránh lấy complexity hiện tại cho dữ liệu quá khứ.
- Hỗ trợ của AI: viết HistoricalSnapshotExport, kiểm tra SHA đầy đủ, shallow history, giới hạn commit và timestamp tất cả tổ tiên; xuất metric và SHA bằng chứng.
- Kiểm chứng: test Git có HEAD tương lai khác complexity; test tổ tiên tương lai nhưng HEAD cũ; test biên cutoff và giới hạn commit; chạy Lizard thật trên blob.
- Kết quả: 4 test lịch sử đạt; tái sử dụng thuật toán của nhóm, không thay công thức metric hay tự sửa dữ liệu không hợp lệ.

## 3. Cài đặt bộ chạy và đánh giá RQ2

- Mục tiêu: cố định đầu vào, so sánh có điều kiện, lưu được các trường hợp không thể kết luận.
- Hỗ trợ của AI: viết freeze/run, so sánh lineage, kiểm tra config/version/window/coverage, phân biệt null/0, bảng tổng hợp.
- Kiểm chứng: 9 test RQ2 mới cùng 8 test xếp hạng cũ đều đạt. Biên tăng đúng 0.15 được kiểm tra bằng Decimal; thay đường dẫn không tạo danh tính mới giả, file tái sử dụng đường dẫn không bị nối nhầm.
- Kết quả: không phát alertCandidates hợp lệ khi coverage thiếu; bảng số liệu sinh từ JSON và kiểm tra hash trước tổng hợp.

## 4. Chạy thử nghiệm và phân tích dữ liệu

- Mục tiêu: có minh chứng thực, có thể chạy lại.
- Hỗ trợ của AI: điều phối công cụ Git/Java/Lizard trên 9 snapshot; đọc kết quả, chỉ ra coverage thiếu và ảnh hưởng của cửa sổ/quần thể tới điểm.
- Kiểm chứng: 9/9 snapshot thành công; 6/6 cặp bị chặn; chạy lại độc lập cùng manifest khớp hash 15/15 snapshot/comparison. Số liệu do chương trình tính, không do mô hình ngôn ngữ ước lượng.
- Kết quả: dữ liệu pilot, bảng kết quả, ví dụ có commit, replay-verification.json. Không gọi LLM provider thương mại, không dùng AI gán bug label hoặc tạo kết quả người dùng.

## 5. Soạn báo cáo và bàn giao

- Mục tiêu: trình bày phạm vi cá nhân, bằng chứng và hạn chế nhất quán.
- Hỗ trợ của AI: soạn báo cáo, runbook, tài liệu lý thuyết, nội dung PR và danh sách việc tuần 10.
- Kiểm chứng: đối chiếu số ca test, số snapshot/cặp so sánh, đường dẫn liên kết và dữ liệu gốc; ghi rõ pilot chưa được nhóm duyệt.
- Trách nhiệm của Toản: đọc mã, giải thích được SHA/cutoff/lineage/null/coverage, chạy lại ví dụ, kiểm tra báo cáo và tự xác nhận nội dung trước nộp. Chưa có bằng chứng trong phiên này rằng các bước xác nhận cá nhân đó đã hoàn tất.

## Điều chỉnh sau kiểm tra

AI đã bổ sung chặn so sánh khác repository, dùng Decimal cho biên ngưỡng, thêm ca Git kiểm soát có coverage đầy đủ, và chạy lại toàn bộ dữ liệu để kiểm tra tính tái lập. Những điều chỉnh này nhằm khắc phục nguy cơ diễn giải sai, không thay kết quả pilot để tăng điểm.
