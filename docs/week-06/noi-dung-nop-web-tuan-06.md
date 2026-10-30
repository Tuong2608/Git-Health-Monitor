# Nội dung nộp web — Báo cáo tuần 6 (18/10–24/10/2026)

Văn bản này soạn để dán vào form nộp hàng tuần. Đánh dấu đầu dòng, không dùng bảng, vì form không render bảng.

## NỘI DUNG BÁO CÁO

Tuần 6, hai thành viên song song cài đặt theo phân công: Tưởng làm mô-đun thu thập dữ liệu Git (STT 21), Toản làm mô-đun lưu trữ và giám sát (STT 23). Nền đối chiếu là `main` `0b664d9` sau khi merge PR #5. Đối chiếu tiến độ theo bảng "Kế hoạch chi tiết": 12 việc Hoàn thành, 12 việc Đang làm, 15 việc Chưa bắt đầu.

**STT 21 — Cài mô-đun thu thập dữ liệu Git (Tưởng, tuần 6–7) — Đang làm.** Đã cài `GitRepositoryClient` và `GitIngestion` dùng JGit: clone/fetch bản bare của repository công khai GitHub; không checkout file, không chạy code hay script từ repository được phân tích, không gửi credential; chỉ phân tích một SHA đầy đủ đã chốt chứ không phân tích tên branch đang di chuyển. Duyệt lịch sử theo thứ tự topo cha-trước-con để nối định danh file qua rename, kể cả rename xảy ra ngoài cửa sổ quan sát; merge commit không tính là một lần thay đổi mà chỉ dùng để hợp lineage; vượt giới hạn commit/file/blob thì báo `RESOURCE_LIMIT_*`, không trả kết quả bị cắt như kết quả đủ.

- Bằng chứng: commit `4462e2d` và `a5e2467` trên `codex/tuong-week6`; 12/12 ca kiểm thử đạt (10 ca ingestion + 2 ca lệnh chạy mẫu).
- Fixture phủ các trường hợp dễ làm sai số liệu: commit có thời gian không theo thứ tự, rename trước cửa sổ, file nhị phân, merge một nhánh, copy và tạo lại sau khi xóa, alias tác giả, repository shallow, hết deadline, và clone/fetch giữ SHA cũ vẫn phân tích lại được.
- Chưa làm: adapter nối vào tầng lưu trữ, tính chỉ số, đo độ phức tạp, ghi database.

**STT 23 — Cài mô-đun lưu trữ và giám sát (Toản, tuần 6–7) — Đang làm.** Migration V2 thêm 9 bảng mới (job, snapshot, logical file, chỉ số file, contributor, contribution, coupling, rule, alert); cùng bảng `tracked_repository` của V1 thành 10 bảng ứng dụng. Hàng đợi job chạy trên PostgreSQL, lịch quét, executor giới hạn, lease/heartbeat/recovery, publish snapshot nguyên tử, hai rule cảnh báo có version, và API vận hành. Thiếu adapter phân tích thì hệ thống trả 503 thay vì tạo chỉ số giả; hai cờ `APP_ANALYSIS_ENABLED` và `APP_ANALYSIS_WRITES_ENABLED` mặc định tắt.

- Bằng chứng: commit `c943067` và `477dd80`, đã merge `main` qua PR #5; 46 file, +2.204 dòng.
- Giới hạn xác minh: thành viên phụ trách ghi 41 test backend đạt. Tưởng **chưa tái lập được** con số này trên máy của mình vì lý do ở mục khối cản bên dưới.

**Tích hợp hai phần.** Tầng lưu trữ đã để sẵn cổng `AnalysisPipeline`. Quá trình đối chiếu ngày 23/10 tìm ra bốn lệch hợp đồng phải chốt trước khi cài lớp chỉ số, mỗi điểm kèm vị trí mã và phương án đề xuất: (1) API chặn cửa sổ quan sát ở 1–365 ngày trong khi có repository cần cửa sổ rộng hơn mới có dữ liệu; (2) `commitCount` chưa định nghĩa là toàn bộ commit hay commit trong cửa sổ, ảnh hưởng ràng buộc `frequency ≤ commitCount`; (3) cột churn bắt buộc khác null nên không giữ được phân biệt "không đo được" khác 0 mà thiết kế thuật toán chủ trương; (4) thiết kế báo lỗi khi vượt giới hạn nhưng schema lại có cờ cho phép trả kết quả cắt cụt. Chi tiết ở `docs/week-06/noi-dung-pr-tuong.md` và `docs/week-06/git-ingestion.md`.

**Kết quả đo được trên repository công khai** (bằng lệnh chạy mẫu, ngày 23/10):

- `spring-guides/gs-rest-service`: 421 commit tới được, 396 commit trong cửa sổ, 83 bản ghi thay đổi file Java, 16 lần rename, 2 file có lineage bất định. Con số này đo với cửa sổ rộng do chủ động truyền (20000 ngày); với cửa sổ mặc định 180 ngày repository này **không có commit Java nào** nên trả về 0 bản ghi.
- `spring-projects/spring-petclinic` (365 ngày): 1.047 commit tới được, 49 commit trong cửa sổ, 92 bản ghi, 13/50 file Java bất định về lineage. Cột rename trong cửa sổ bằng 0 vì các rename của repo này xảy ra ngoài cửa sổ — lineage vẫn được nối, nên đây là bộ đếm fact rename chứ không phải số rename của repo.
- `octocat/Hello-World`: 3 commit tới được, 0 bản ghi Java — xác nhận bộ lọc phạm vi hoạt động đúng.
- Hai con số 421/83/16 khớp với phép đo ngày 22/10, xác nhận mô-đun vẫn cho cùng kết quả sau khi sửa lỗi hủy tác vụ.

**STT 20 — Thiết kế thực nghiệm (cả hai, tuần 5–6) — Đang làm.** Đã có bản đối chiếu hai đề xuất, fixture và công cụ tính AP/P@K/R@K kèm kiểm tra từ chối dữ liệu chưa gán nhãn hoặc trùng. Tưởng chạy độc lập: 8/8 ca đạt. Chưa chốt: split 80/20 hay observation 180/prediction 30 ngày, quy tắc đồng hạng theo ID hay path, dataset và nhãn. Chưa có kết quả thực nghiệm.

**Khối cản môi trường và cách xử lý.** Docker Engine trên máy phát triển hỏng và chưa dựng lại PostgreSQL portable, nên trên cây mã sau merge, 43/71 ca kiểm thử backend không chạy được vì Spring context không tải được (lỗi kết nối `localhost:5432`); 28/71 ca không cần database đều đạt. Để không bị chặn bởi môi trường, đã thêm lệnh chạy mẫu in kết quả thu thập mà không cần API hay database. Số liệu trong báo cáo này chỉ gồm kết quả tự chạy lại được trên máy này; kết quả của bạn Toản cần database được ghi rõ là bàn giao chưa xác minh.

**Kế hoạch tuần 7.** STT 22 — cài mô-đun tính chỉ số (frequency, churn, HHI, hotspot, coupling) và đo CCN/NLOC; STT 25 — cài module trợ lý rà soát bằng LLM; viết adapter `AnalysisPipeline` để chạy pipeline thật khi có database; phản hồi bốn điểm hợp đồng với Toản. STT 24 (tầng API) đã được bắt đầu sớm một phần qua API vận hành tuần 6.

**Không tuyên bố trong tuần này.** Chưa có chỉ số đầu ra, chưa có điểm benchmark, chưa có nhãn thực nghiệm, chưa có cảnh báo chạy thật, chưa gọi LLM provider, chưa deploy staging, chưa có nghiệm thu của nhóm hoặc giảng viên.

- Link repository: https://github.com/Tuong2608/Git-Health-Monitor (PR tuần 6: `codex/tuong-week6` → `main`)
- Link đề cương: https://drive.google.com/drive/folders/1Nx1qMCjnKJr293tqZKtUZKhuFzJa9gZT?usp=sharing

## AI HỖ TRỢ — Codex (GPT-6 Astra)

**PROMPT**

- Cài mô-đun đọc Git theo thiết kế và công thức đã chốt trong Metric Spec v1 và algorithm design, kèm fixture cho các trường hợp biên; sau đó bổ sung lệnh chạy mẫu để xem kết quả thu thập khi chưa có API và database.
- Kéo bàn giao tuần 6 của thành viên phụ trách lưu trữ từ GitHub, đối chiếu cổng `AnalysisPipeline` với thiết kế thuật toán, đo hệ quả của từng điểm lệch, rồi tổng hợp tiến độ vào báo cáo và bảng Excel.

**AI TẠO RA**

Bản nháp mã JGit (`GitRepositoryClient`, `GitIngestion`, `GitIngestionSample`), fixture kiểm thử, tài liệu hợp đồng module, bản giải quyết xung đột khi merge `main`, bản nháp báo cáo tuần và bảng trạng thái Excel. AI chạy Maven, JGit và `unittest` để lấy số liệu, có lưu log trong `outputs/week6-progress-20261023/` để kiểm tra lại. Các lựa chọn chưa chốt được đánh dấu là đề xuất, không ghi thành quyết định của nhóm.

**SV ĐÃ SỬA**

Nhóm xác định mục tiêu, phân công và cung cấp đầu vào: đề cương, Metric Spec v1, algorithm design, monitoring design, bàn giao của hai thành viên và nhận xét "cần báo cáo chi tiết hơn" của giảng viên. Sinh viên quyết mọi điểm kỹ thuật: giữ hành vi đánh dấu lineage bất định ở merge dù biết làm giảm coverage (vì thiết kế đã ghi như vậy, thay thì phải đổi cả thiết kế lẫn test), không tự sửa schema của thành viên khác, chọn cách khai cả số liệu bất lợi (41 test chưa tái lập, 13/50 file bất định) thay vì bỏ đi, và chọn thứ tự việc tuần sau. Sinh viên đọc lại mã và tài liệu, tự chạy lại lệnh kiểm chứng khi cần, và là người giải trình kết quả trước giảng viên. Chưa ghi nhận giảng viên nghiệm thu bộ tài liệu này.
