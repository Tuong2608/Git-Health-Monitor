# Nội dung báo cáo tuần 6 (18/10–24/10/2026)

## Nội dung nộp trên web

Trong tuần 6, hai thành viên song song cài đặt theo phân công: Tưởng làm mô-đun thu thập dữ liệu Git, Toản làm mô-đun lưu trữ và giám sát. Nền so sánh là `main` `0b664d9` sau khi merge PR #5 của Toản.

**Phần Tưởng — mô-đun đọc Git (task 21, cửa sổ tuần 6–7).** Đã cài `GitRepositoryClient` và `GitIngestion` dùng JGit: clone/fetch bản bare của repository công khai GitHub, không checkout file, không chạy code hay script từ repository đầu vào, không gửi credential. Duyệt lịch sử theo thứ tự topo cha-trước-con để dựng định danh file nối qua rename, kể cả rename xảy ra ngoài cửa sổ quan sát; merge commit không tính là thay đổi mà chỉ dùng để hợp lineage. Vượt giới hạn commit/tree/blob/fact thì báo `RESOURCE_LIMIT_*`, không trả kết quả cắt cụt như kết quả đủ. Ngày 22/10 phát hiện lỗi trong ca kiểm thử hủy tác vụ, đã sửa; ngày 23/10 chạy lại **12/12 ca đạt** (10 ingestion + 2 lệnh mẫu). Đã đo ba repository công khai: gs-rest-service 421 commit → 83 fact, 16 rename; spring-petclinic 1.047 commit → 92 fact, 13/50 file Java có lineage bất định; Hello-World 0 fact. Kèm theo là lệnh chạy mẫu in kết quả thu thập **không cần API hay database**, để demo độc lập.

**Phần Toản — lưu trữ và giám sát (task 23, cửa sổ tuần 6–7).** Migration V2 thêm 10 bảng (job, snapshot, logical file, metric, contributor, contribution, coupling, rule, alert), hàng đợi job trên PostgreSQL, lịch quét, executor giới hạn, lease/heartbeat/recovery, publish snapshot nguyên tử, hai rule cảnh báo và API vận hành. Ba cờ an toàn: `APP_ANALYSIS_ENABLED` và `APP_ANALYSIS_WRITES_ENABLED` mặc định tắt, thiếu adapter thì trả 503 thay vì tạo metric giả. Toản ghi 41 test backend và 8 test công cụ đánh giá đạt.

**Minh chứng do Tưởng tự chạy lại ngày 23/10.** 8/8 test công cụ `evaluate_rankings.py` đạt khi chạy độc lập bằng `unittest`. Bộ test backend của Toản **chưa xác minh lại được**: Docker Engine trên máy hỏng và chưa dựng lại PostgreSQL portable, nên trên cây mã sau merge, **43/71 ca lỗi** ở bước tải Spring context vì không kết nối được `localhost:5432`. Con số 41 test vì thế là kết quả bàn giao, chưa phải kết quả kiểm chứng của báo cáo này. Cây mã sau merge biên dịch chung 18 class; 28/71 ca không cần database đều đạt, trong đó có 12 ca của mô-đun Git.

**Điểm tích hợp cần hai người chốt.** Toản đã để sẵn cổng `AnalysisPipeline`. Bốn lệch hợp đồng đo được ngày 23/10: (1) API chặn `observationDays` ở 1..365 trong khi có repository cần cửa sổ rộng hơn mới có dữ liệu; (2) `commitCount` chưa định nghĩa là toàn bộ commit hay commit trong cửa sổ, ảnh hưởng trực tiếp ràng buộc `frequency ≤ commitCount`; (3) cột `churn NOT NULL` và bắt buộc tổng contribution đúng bằng churn file, không giữ được phân biệt "không đo được" khác 0 mà thiết kế thuật toán chủ trương; (4) thiết kế báo lỗi khi vượt giới hạn nhưng schema lại có cờ `truncated` cho phép trả kết quả cắt cụt. Chi tiết ở [nội dung PR](noi-dung-pr-tuong.md) và [hợp đồng module](git-ingestion.md).

**Tiếp tục tuần 7.** Viết adapter `AnalysisPipeline`, cài lớp metric (change frequency, churn, HHI, hotspot, coupling) và lớp đo độ phức tạp CCN/NLOC theo thiết kế đã chốt; chạy pipeline thật trên repository công khai sau khi có PostgreSQL. **Chưa có** kết quả metric, chưa có điểm benchmark, chưa có nhãn thực nghiệm, chưa có cảnh báo chạy thật và chưa deploy staging.

## Khai báo AI theo từng ô

**Có sử dụng AI:** Có.

**Công cụ AI:** Codex desktop.

**Phiên bản/model:** Điền model thực tế hiển thị trong phiên sử dụng; chưa có thông tin được xác nhận trong nhật ký này.

**Prompt đã dùng (tóm tắt yêu cầu, không phải trích nguyên văn):** Lấy và đọc bàn giao tuần 6 của Toản trên GitHub, dựng mô-đun đọc Git kèm fixture, sửa ca kiểm thử hủy tác vụ, thêm lệnh chạy mẫu để xem kết quả mà chưa cần API/database, đo trên repository công khai, rồi đối chiếu hợp đồng `AnalysisPipeline` với thiết kế thuật toán và cập nhật báo cáo tiến độ.

**Nội dung AI tạo ra:** Bản nháp `GitRepositoryClient`, `GitIngestion`, fixture kiểm thử, lớp `GitIngestionSample` và báo cáo văn bản, tài liệu hợp đồng module, bản giải quyết xung đột `README.md` khi merge, bản nháp báo cáo và bảng trạng thái Excel. AI cũng chạy Maven, JGit và `unittest` để lấy số liệu ghi trong báo cáo.

**Phần bạn đã sửa/hoàn thiện:** Người dùng cung cấp phân công, bàn giao của Toản và quyết định thứ tự việc. Các điểm chốt (merge lineage, `commitCount`, null-churn, `truncated`) do người dùng quyết, AI chỉ đo và trình bày hệ quả; chưa tự đổi hành vi đã ghim trong test. Người dùng cần tự đọc lại mã và giải thích khi trình bày.

## Ghi nhớ khi trình bày

- 12 là số test của mô-đun Git, không phải số tính năng; phạm vi vẫn dừng ở fact, **chưa có metric**.
- "421 commit" là số commit tới được từ một SHA chốt, không phải số commit trong cửa sổ; hai con số này khác nhau và liên quan đúng điểm chốt (2).
- 13/50 file bất định trên petclinic là bằng chứng của lựa chọn bảo thủ ở merge, không phải lỗi chạy được.
- 41 test của phần Toản chưa tái lập trên máy này; nói rõ là "bạn cung cấp, mình chưa xác minh".
- 8/8 test tool thực nghiệm là mình tự chạy độc lập, có thể demo ngay.
- Health OK hoặc clone thành công không chứng minh pipeline cuối-to-end; chưa có job nào ghi database thành công.
