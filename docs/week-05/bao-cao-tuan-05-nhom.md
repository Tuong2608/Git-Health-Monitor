# Nội dung báo cáo tuần 5 (11/10–17/10/2026)

## Nội dung nộp trên web

Trong tuần 5, nhóm tập trung cụ thể hóa thiết kế và bổ sung minh chứng kiểm chứng API theo nhận xét của GVHD ở tuần 4.

**Kết quả thiết kế:** Đã có mô tả thuật toán change frequency, hotspot score, temporal coupling và ownership concentration; thiết kế snapshot, so sánh thay đổi và cảnh báo theo ngưỡng. Đã bàn giao ERD, mô tả 10 bảng và SQL tham chiếu, cùng wireframe cho 4 màn hình chính. Minh chứng của phần thiết kế dữ liệu ghi nhận 12 nhóm kiểm tra SQL; wireframe offline có 18 kiểm tra. Đây là kết quả thiết kế, chưa phải chức năng phân tích hoàn chỉnh chạy trên sản phẩm.

**Tiếp thu nhận xét GVHD:** Đã bổ sung kiểm thử API hiện có bằng HTTP và PostgreSQL riêng, kiểm tra cấu trúc phản hồi, mã trạng thái, ràng buộc dữ liệu, phân trang và đăng ký trùng đồng thời. Toàn bộ 40 test backend đạt (16 ca URL, 23 ca HTTP và 1 ca khởi tạo ứng dụng). Kiểm thử phát hiện sai khác độ chính xác createdAt giữa POST và GET; đã sửa về microsecond và chạy lại thành công. Có lưu kết quả từng ca và request/response để đối chiếu. Kết quả áp dụng cho API repository và health hiện có trên môi trường kiểm thử local.

**Phân công:** Tưởng phụ trách thiết kế thuật toán, cơ chế giám sát và bước đầu module AI review; Toản phụ trách thiết kế dữ liệu và wireframe. Hai phần bàn giao đã được tổng hợp và ghi lại các điểm cần thống nhất trước cài đặt.

**Phần tiếp tục tuần 6:** Hoàn thiện interface LLMReviewService và prompt có version; thống nhất observation/prediction window, quy tắc xếp hạng khi bằng điểm và phạm vi metric đánh giá. Tiếp tục triển khai và xác thực các API job/snapshot/alert/AI theo tiến độ. Chưa có kết quả thực nghiệm hoặc benchmark để báo cáo.

## Khai báo AI theo từng ô

**Có sử dụng AI:** Có.

**Công cụ AI:** Codex desktop.

**Phiên bản/model:** Điền model thực tế hiển thị trong phiên sử dụng; chưa có thông tin được xác nhận trong nhật ký này.

**Prompt đã dùng (tóm tắt yêu cầu, không phải trích nguyên văn):** Đối chiếu phần việc tuần 5 của hai thành viên với tài liệu đã chốt. Cụ thể hóa thiết kế thuật toán, snapshot và interface AI. Bổ sung kiểm thử API theo góp ý GVHD, sửa sai khác phát hiện được và tổng hợp kết quả vào báo cáo tiến độ.

**Nội dung AI tạo ra:** Bản nháp tài liệu thiết kế, đề xuất xử lý trường hợp biên, mã kiểm thử HTTP/DB, bản sửa timestamp và nội dung tổng hợp báo cáo. AI hỗ trợ chạy kiểm thử và trích minh chứng. Các quyết định chưa chốt được ghi rõ để tiếp tục review.

**Phần bạn đã sửa/hoàn thiện:** Đã cung cấp phân công, tài liệu và nhận xét GVHD, cập nhật phần bàn giao của hai thành viên và merge phần Toản. Phần kiểm chứng mới được thực hiện bằng kiểm thử tự động có AI hỗ trợ; chưa ghi nhận nhóm đã kiểm tra thủ công các ca này. Chỉ bổ sung các bước tự đọc mã, chạy lại hoặc sửa thủ công sau khi thực hiện thực tế.

## Ghi nhớ khi trình bày

- Thầy yêu cầu xác thực contract: đã kiểm tra HTTP và dữ liệu lưu thật trên DB riêng.
- Con số 40 là số test, không phải 40 API. Phạm vi có 4 route hiện có.
- Lỗi cụ thể: độ chính xác thời gian POST khác GET. Sửa microsecond, giữ assertion so sánh DTO.
- Hai request trùng đồng thời: kỳ vọng một 201, một 409, DB chỉ có một bản ghi.
- Health OK chỉ chứng minh tiến trình phản hồi; không chứng minh toàn pipeline hoạt động.
- AI/worker/snapshot chưa triển khai: sẽ xác thực khi có mã chạy.
