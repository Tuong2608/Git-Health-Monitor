# Giao thức RQ2 tuần 9

Ngày 12/11/2026. Tác giả phụ trách: Trần Quang Toản; triển khai và biên soạn có AI hỗ trợ. Trạng thái: PILOT_PROPOSED_NOT_GROUP_APPROVED. Người dùng đã cho phép tự hoàn thiện bộ chạy thử; chưa có bằng chứng Tưởng/giảng viên phê duyệt protocol nghiên cứu chung.

## Mục tiêu và tập dữ liệu

Đánh giá khả năng thu thập snapshot lịch sử, đối chiếu chỉ số và cung cấp bằng chứng giải thích biến động. Không đồng nhất điểm hotspot với xác suất có bug; không đo precision/recall cảnh báo khi chưa có nhãn độc lập.

Chọn trước khi chạy lần này: spring-guides/gs-rest-service (hướng dẫn Java), spring-projects/spring-petclinic (ứng dụng), apache/commons-cli (thư viện). Kho công khai, có Java, có lịch sử trước mốc đầu, full history ≤10.000 commit ở các mốc; kiểm tra license qua Git blob được ghi trong manifest. Ba dự án dùng Apache License 2.0 cho mã nguồn; gs-rest-service còn có LICENSE.writing.txt riêng cho tài liệu. Không sao chép mã nguồn các dự án vào bộ bàn giao.

Đây là mẫu thuận tiện, không đại diện cho toàn bộ Java OSS. Hai dự án Spring đã được nhìn kết quả tích hợp tuần 8: **không gọi là bộ chọn mù cho RQ1**. Tưởng cần quyết định riêng tập RQ1 trước khi đo nhãn/kết quả. Giữ cả cửa sổ không có commit và mọi thất bại; không thay repo theo chất lượng điểm số.

## Quy tắc thời gian cố định

- Ba cutoff UTC: 01/01/2024, 01/01/2025, 01/01/2026; mỗi cửa sổ đúng 180 ngày, `[start, end)`. Đây là ba lát cắt lịch sử thưa để kiểm chứng ban đầu, không mô phỏng lịch giám sát hằng ngày và có khoảng trống giữa các cửa sổ.
- Đóng băng endSha và chọn commit đầu tiên trên chuỗi first-parent của endSha có committerTime < cutoff. Không dùng author date; không dùng tên nhánh di động trong phân tích.
- Kiểm tra **mọi tổ tiên reachable** phải trước cutoff, không chỉ HEAD. Nếu clock skew vi phạm: ghi FUTURE_ANCESTOR, giữ thất bại, không lùi mốc tùy tiện để có kết quả đẹp.
- JGit chỉ nhận SHA đầy đủ. Đo Lizard trên blob tại chính SHA đó; không lấy complexity hiện tại rồi gắn lịch sử cũ. Chặn shallow history, quá 10.000 commit, quá giới hạn blob/tree/facts/deadline của pipeline.
- Freeze manifest là thao tác riêng, hoàn tất trước lệnh run; không ghi đè manifest. Replay không fetch remote. License, SHA và hash cấu hình/dữ liệu giúp kiểm tra nguồn gốc.

## So sánh và diễn giải

So sánh đúng repo, theo thứ tự thời gian và ancestry; cùng metricVersion, cấu hình (kể cả version công cụ), độ dài cửa sổ, coverage đầy đủ, không truncated. Ghép bằng lineageKey trong repo; không ghép theo tên file khi file đã bị xóa/tạo lại. File mới/mất tách riêng, không gán điểm trước/sau bằng 0.

Ngưỡng pilot: `hotspot_after - hotspot_before > 0.15`, tuyệt đối, không phải tăng 15%. Ngưỡng được cố định trong proposal trước đo, chưa tối ưu trên dữ liệu. Tính so sánh ngưỡng bằng Decimal để không phát cảnh báo giả tại đúng biên do sai số float. Baseline 0: relativeDelta=null/BASE_ZERO; điểm null: không so sánh.

Nếu coverage thiếu, `alertCandidates=null`, status=BLOCKED. Các dòng số liệu còn lại có nhãn diagnosticChangesOnly; chỉ giúp điều tra cách điểm thay đổi, không là cảnh báo được backend phát ra. Trường comparableCommonFiles trong dữ liệu là số file chung có điểm số, **không** phủ nhận BLOCKED ở mức snapshot.

Điểm hotspot dùng phân vị theo quần thể từng snapshot: điểm đổi có thể do frequency đổi, complexity đổi, hoặc các file khác đổi. Frequency giảm còn có thể vì commit rời cửa sổ; không diễn giải thành đã sửa bug. Evidence là SHA commit trong cửa sổ, đường dẫn và số đo trước/sau. Không dùng AI tạo nhãn hoặc giải thích nguyên nhân chưa kiểm chứng.

## Chỉ tiêu báo cáo

1. Số snapshot hoàn thành / số đã cố định; ghi lỗi nếu có.
2. Số cặp đủ điều kiện / toàn bộ cặp liền kề theo lịch; lý do loại.
3. File Java, quần thể hotspot, lineage chưa rõ, file thêm/mất và file không đo được.
4. Delta và commit đối chiếu cho ví dụ chẩn đoán; quy tắc chọn ví dụ là |delta| lớn nhất mỗi cặp, hòa theo lineageKey.
5. Tái lập: so SHA-256 cả 9 snapshot và 6 comparison với lần chạy độc lập trên cùng manifest/công cụ.

Chưa có ground truth RQ2 được hai người review: không tính detection precision/recall, không nói công cụ giúp giảm thời gian thực tế. Test Git kiểm soát chỉ chứng minh hành vi kỹ thuật đúng trong tình huống đã tạo.

## Việc tiếp nối tuần 10

Tưởng và Toản review lineage qua merge và điều kiện coverage (đặc biệt NO_FUNCTIONS). Nếu sửa metric/coverage policy, tăng version/config và chạy thành nhánh thực nghiệm mới; giữ nguyên pilot gốc, không trộn chuỗi. Chốt lịch snapshot dày hơn trước khi chạy; ghi rõ phân tích bổ sung. Lập bộ tình huống được người review độc lập để đánh giá giải thích, rồi mới kết luận RQ2; đo hiệu năng theo STT 32 riêng.
