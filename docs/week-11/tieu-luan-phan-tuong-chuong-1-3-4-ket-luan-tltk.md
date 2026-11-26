# BẢN THẢO TIỂU LUẬN CHUYÊN NGÀNH — PHẦN CỦA TRẦN VĂN TƯỞNG (23110170)

*(Ghép nối tiếp cùng tệp [tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md](tieu-luan-phan-toan-chuong-2-3-4-phu-luc.md) của Trần Quang Toản để tạo thành cuốn Tiểu luận chuyên ngành hoàn chỉnh gồm Chương 1, Chương 2, Chương 3, Chương 4, Chương 5, Tài liệu tham khảo và Phụ lục.)*

---

# CHƯƠNG 1: TỔNG QUAN ĐỀ TÀI VÀ CƠ SỞ LÝ THUYẾT

## 1.1. Bối cảnh và động lực nghiên cứu

Trong quá trình phát triển và bảo trì phần mềm, chi phí đọc hiểu, sửa lỗi và mở rộng mã nguồn thường chiếm tỷ trọng lớn hơn nhiều so với giai đoạn viết mới ban đầu. Một vấn đề thực tiễn mà nhiều nhóm phát triển gặp phải là khó xác định nhanh **tập tin nào cần ưu tiên kiểm tra trước khi ghép mã hoặc tái cấu trúc**. Nếu chỉ dựa vào trực giác cá nhân hay chỉ số kích thước/độ phức tạp tĩnh tại một thời điểm duy nhất, nhóm phát triển dễ bỏ qua yếu tố tiến hóa theo thời gian: một tập tin có cấu trúc phức tạp nhưng ổn định suốt nhiều tháng ít tạo rủi ro vận hành hơn một tập tin vừa có độ phức tạp cao vừa liên tục bị chỉnh sửa qua nhiều commit.

Các công cụ phân tích tĩnh phổ biến như SonarQube hay PMD giải quyết tốt việc phát hiện vi phạm cú pháp, bug mẫu và code smell trên ảnh chụp mã nguồn hiện tại, nhưng chưa khai thác sâu lịch sử kho lưu trữ Git như tần suất thay đổi, lượng dòng thêm/xóa (code churn), mối liên kết đồng thay đổi giữa các tập tin (temporal coupling) hay mức độ tập trung đóng góp. Ngược lại, một số công cụ khai phá kho phần mềm chuyên biệt hoặc thương mại như CodeScene hỗ trợ phân tích tiến hóa nhưng đóng mã nguồn, còn các công cụ dòng lệnh như Code Maat thiếu giao diện web, thiếu cơ chế lưu chuỗi snapshot bất biến để theo dõi xu hướng dài hạn và không có trợ lý giải thích ngữ cảnh rủi ro có kiểm soát.

Từ khoảng trống thực tế đó, đề tài **"Xây dựng hệ thống phân tích và giám sát sức khỏe dự án dựa trên dữ liệu Git (Git Health Monitor)"** được thực hiện nhằm xây dựng một nền tảng web mã nguồn mở kết hợp lịch sử Git với phân tích độ phức tạp mã nguồn Java, đi kèm giám sát định kỳ, cảnh báo an toàn dữ liệu và trợ lý AI Review minh bạch bằng chứng.

---

## 1.2. Cơ sở lý thuyết về Nợ kỹ thuật và Khai phá kho phần mềm (MSR)

### 1.2.1. Nợ kỹ thuật (Technical Debt) và tín hiệu bảo trì
Khái niệm Nợ kỹ thuật do Ward Cunningham đề xuất năm 1992 mô tả sự đánh đổi giữa tốc độ bàn giao ngắn hạn và chi phí bảo trì dài hạn của mã nguồn. Trong phạm vi đề tài này, nhóm tiếp cận thận trọng ở góc độ học thuật: các chỉ số trích xuất từ kho Git **không khẳng định chắc chắn một tập tin có lỗi hay không**, mà đóng vai trò **tín hiệu ưu tiên rà soát** giúp lập trình viên khoanh vùng những khu vực có xác suất phát sinh chi phí bảo trì cao.

### 1.2.2. Khai phá kho phần mềm (Mining Software Repositories - MSR) và bộ chỉ số v1
Hướng nghiên cứu MSR khai thác nhật ký lịch sử commit, diff và thông tin tác giả trong hệ thống quản lý phiên bản để đo lường hành vi tiến hóa phần mềm. Tham khảo các công trình nền tảng của Gall và cộng sự (1998) về *Logical/Temporal Coupling*, Nagappan & Ball (2005) về mối liên hệ giữa *Code Churn* và mật độ lỗi, Bird và cộng sự (2011) về phân bố đóng góp (*Ownership*), cùng phương pháp phân tích *Hotspot* của Adam Tornhill (2015), nhóm chuẩn hóa 5 nhóm chỉ số trong tài liệu `docs/metric-spec-v1.md`:

1. **Tần suất thay đổi (`change_frequency`) và lượng dòng biến động (`churn`):** Đếm số lượng `non-merge commit` làm thay đổi tập tin và tổng `additions + deletions` trong cửa sổ quan sát $W$ (mặc định 180 ngày, cấu hình 1–365 ngày). Loại bỏ `merge commit` để tránh đếm trùng các thay đổi đã ghi nhận trên nhánh con.
2. **Độ phức tạp mã nguồn (`max_ccn`, `avg_ccn`, `function_count`, `nloc`):** Đo trực tiếp trên nội dung tập tin `.java` tại đúng commit `headSha` bằng `Lizard 1.24.3`, lấy giá trị độ phức tạp chu trình lớn nhất giữa các hàm (`max_ccn`) làm đại diện cấp tập tin nhằm tránh trường hợp một hàm quá phức tạp bị nhiều hàm ngắn kéo trung bình xuống.
3. **Điểm rủi ro tổng hợp (`hotspot_score`):** Kết hợp đồng thời thứ hạng tương đối của tần suất sửa và độ phức tạp thông qua phân vị hạng giữa (*midrank percentile*) $P_{\text{change}}, P_{\text{complexity}} \in [0, 1]$:
   $$\text{hotspot\_score} = \sqrt{P_{\text{change}} \times P_{\text{complexity}}}$$
4. **Liên kết đồng thay đổi (`temporal_coupling`):** Đo mức độ hai tập tin $A, B$ cùng xuất hiện trong các non-merge commit:
   $$\text{coupling}(A, B) = \frac{|C_A \cap C_B|}{\min(|C_A|, |C_B|)}$$
   Mặc định chỉ giữ các cặp có số commit chung $\ge 5$ và tỷ lệ $\ge 0{,}50$ (hỗ trợ lọc các commit thay đổi hàng loạt trên 30 tập tin theo quy tắc `BR14`).
5. **Mức tập trung đóng góp (`HHI` - Herfindahl-Hirschman Index):** Tính trên tỷ trọng `churn` của từng tác giả trong cửa sổ:
   $$\text{HHI} = \sum_{i} \left(\frac{\text{churn}_i}{\text{Churn}_{\text{total}}}\right)^2 \in (0, 1]$$
   Hệ thống ghi nhận rõ trên giao diện và prompt AI rằng `HHI` chỉ phản ánh độ tập trung lịch sử chỉnh sửa, **không dùng để đánh giá năng lực hay chấm điểm cá nhân**.

---

## 1.3. Khảo sát và đối chiếu các công cụ hiện có

Nhóm thực hiện đối chiếu 5 công cụ tiêu biểu ở Tuần 2 (`docs/competitor-matrix.md`, mở rộng ở Tuần 10 tại `docs/week-10/synthesis-and-threats.md`) để xác định khoảng trống sản phẩm:

| Công cụ | Nguồn dữ liệu chính | Chỉ số & Khả năng theo dõi xu hướng | Giao diện & Vận hành | Trợ lý AI giải thích | Khoảng trống cần giải quyết |
|---|---|---|---|---|---|
| **SonarQube (Community)** | Phân tích tĩnh AST/Rules | Bugs, Code Smells, Coverage; thiên về trạng thái tĩnh từng lần quét | Web hoàn chỉnh, cần JVM/ES nặng hơn | Bản trả phí / mở rộng | Chưa khai thác tần suất sửa Git lịch sử, `temporal_coupling` và phả hệ đổi tên |
| **CodeScene** | Lịch sử Git + độ phức tạp + tổ chức nhóm | Hotspot, Change Coupling, Code Health, theo dõi xu hướng | Web thương mại đẹp | Có gợi ý AI ở bản trả phí | Đóng mã nguồn, chi phí cao đối với sinh viên/nhóm nhỏ |
| **Code Maat** | Log Git/SVN/Hg xuất văn bản | Churn, Revisions, Coupling, Ownership | Chỉ có CLI xuất CSV | Không | Thiếu giao diện web, thiếu lưu DB/cảnh báo định kỳ |
| **CodeCharta** | Đa nguồn (Git + Sonar/Lizard) | Trực quan hóa bản đồ cây 3D | Web đọc file JSON tĩnh | Không | Thiếu scheduler định kỳ, cảnh báo xu hướng và chi tiết chứng cứ |
| **Git-of-Theseus** | Lịch sử commit Git theo thời gian | Tuổi thọ dòng mã, tỷ lệ tồn tại theo tác giả/thư mục | CLI xuất biểu đồ tĩnh | Không | Không tính `max_ccn`, không có hotspot hay bảng cảnh báo tương tác |

**Kết luận thiết kế rút ra:** Không cạnh tranh phân tích tĩnh sâu kiểu SonarQube hay trực quan hóa 3D kiểu CodeCharta, hệ thống **Git Health Monitor** tập trung làm tốt 3 mảng liên kết chặt với nhau: (1) trích xuất lịch sử Git + Lizard minh bạch công thức kèm giữ `NULL` khi thiếu dữ liệu; (2) lưu chuỗi snapshot bất biến trên PostgreSQL phục vụ so sánh và cảnh báo xu hướng an toàn; (3) tích hợp trợ lý AI Review chỉ đọc, có bước xem trước gắn mã băm SHA-256 trước khi gửi.

---

## 1.4. Khảo sát nhu cầu thực tế của người phát triển (`S01..S05`)

Trước khi chốt đặc tả yêu cầu ở Tuần 4, nhóm khảo sát 5 người phát triển có `2–3` năm kinh nghiệm dùng Git trong các dự án phần mềm nhóm (`S01..S05` trong `docs/interview-notes.md`), gồm 3 sinh viên CNTT năm cuối và 2 lập trình viên backend/frontend/full-stack, dùng chung bộ câu hỏi 12 mục. *Lưu ý phương pháp: đây là khảo sát định tính thu thập nhu cầu thiết kế trước khi lập trình, không phải khảo sát khả năng sử dụng SUS của hệ thống.*

Những điểm đồng thuận nổi bật từ 5 trường hợp khảo sát:
- **Nhu cầu khoanh vùng file ưu tiên & liên kết ẩn:** Cả `5/5` người đều mất `30 phút – 3 giờ` truy vết bằng `git log`/`git blame` khi gặp lỗi tích hợp chéo module (`S01, S02, S03, S04`), và đánh giá cao khả năng gợi ý *file hay sửa cùng* (`temporal coupling`) để tránh sót DTO/Controller/UI khi đổi Entity.
- **Kỳ vọng về bằng chứng và cảnh báo không gây nhiễu:** Người dùng (`S01, S02, S03, S04`) yêu cầu mọi con số rủi ro và cảnh báo phải đi kèm mã commit bằng chứng và lý do ngưỡng cụ thể, tránh báo động giả từ các thay đổi format hoặc dữ liệu thiếu.
- **Lo ngại về tính riêng tư và ảo giác của AI:** Các câu trả lời `S01, S03, S04` đều sẵn sàng dùng AI để tóm tắt ngữ cảnh review (`3–5` gạch đầu dòng) nhưng lo ngại rò rỉ mã nhạy cảm ra API ngoài và AI tự bịa thông tin. Yêu cầu này dẫn trực tiếp đến cơ chế `Preview + ConsentReceipt SHA-256 + SECRET_PATTERN + evidenceAllowlist`.
- **Thái độ thận trọng với chỉ số đóng góp:** `S02` và `S04` nêu rõ không muốn dùng chỉ số đóng góp để đánh giá cá nhân, nên nhóm thiết kế `HHI` thuần túy phục vụ hiểu mức độ tập trung lịch sử chỉnh sửa và ẩn toàn bộ email thô qua API.

---

## 1.5. Mục tiêu, phạm vi và bố cục tiểu luận

- **Mục tiêu đề tài:** (1) Xây dựng pipeline trích xuất chỉ số lịch sử Git + Lizard tái lập được theo `headSha` cố định và giữ trung thực trạng thái `NULL`; (2) Lưu chuỗi snapshot bất biến trên PostgreSQL 18 phục vụ theo dõi xu hướng, cảnh báo định kỳ và trực quan hóa trên React; (3) Xây dựng trợ lý AI Review opt-in 2 bước an toàn; (4) Kiểm chứng định lượng qua 3 câu hỏi nghiên cứu `RQ1` (xếp hạng rủi ro bảo trì), `RQ2` (theo dõi xu hướng qua nhiều snapshot) và `RQ3` (đo hiệu năng hệ thống cùng thực nghiệm người dùng SUS).
- **Phạm vi thực hiện (In-Scope / Out-of-Scope):** Hỗ trợ kho GitHub công khai qua HTTPS và phân tích tập tin `.java` trong bản v1 (loại trừ `vendor/`, `generated/`, `target/`, `build/`); không tự động sửa code hoặc tạo PR, không phân tích diff PR đang mở và không chạy/biên dịch mã nguồn của kho được quét.
- **Bố cục cuốn tiểu luận:** Chương 1 trình bày tổng quan, lý thuyết MSR và khảo sát nhu cầu; Chương 2 phân tích yêu cầu SRS, kiến trúc Modular Monolith và thiết kế CSDL; Chương 3 trình bày chi tiết cài đặt hệ thống và thuật toán lõi; Chương 4 báo cáo thực nghiệm `RQ1`, `RQ2`, Benchmark, kiểm thử/độ phủ `JaCoCo` và thực nghiệm người dùng `RQ3`; Chương 5 rút ra kết luận và hướng phát triển.

---

# CHƯƠNG 3 *(NỐI TIẾP MỤC 3.1..3.3)*: THUẬT TOÁN LÕI VÀ TRỢ LÝ AI REVIEW

## 3.4. Thuật toán trích xuất lịch sử Git và phả hệ tập tin (`GitRepositoryClient` & `GitIngestion`)

Để đảm bảo mọi lần quét ở cùng trạng thái kho đều cho ra kết quả tất định, tầng `gitanalyzer` tách riêng việc tải dữ liệu Git (`GitRepositoryClient`) và việc duyệt đồ thị commit (`GitIngestion`).

### 3.4.1. Thu thập kho dạng Bare Repository an toàn (`GitRepositoryClient`)
Thay vì `git checkout` toàn bộ tệp ra cây thư mục làm việc hoặc phụ thuộc vào API GitHub vốn bị giới hạn rate-limit, `GitRepositoryClient` gọi `Eclipse JGit 7.8.0` thực hiện `clone/fetch` dạng *bare repository* (`setBare(true)`) vào thư mục cache cục bộ (định danh theo `repoId`), tắt các tiến trình hook và tuyệt đối không biên dịch hay thực thi mã nguồn của kho được phân tích (`ADR-001`). Trước khi chuyển sang bước phân tích, hệ thống phân giải nhánh mặc định thành đúng một chuỗi 40 ký tự hex `headSha` cố định.

### 3.4.2. Duyệt đồ thị DAG, phát hiện đổi tên và quy tắc `LINEAGE_AMBIGUOUS` (`GitIngestion`)
Hàm `GitIngestion.extract(Path repositoryPath, String headSha, Options options)` (từ dòng 71) thực hiện tuần tự 4 bước:
1. **Kiểm tra giới hạn tài nguyên bảo vệ (`Options.defaults`, dòng 26–29):** Thiết lập sẵn trần `maxCommits = 10.000`, `maxTreeEntries = 100.000`, `maxBlobBytes = 2 MiB`, `maxFacts = 500.000` và `deadline = 300 giây`. Nếu kho là *shallow clone* (`getShallowCommits()` khác rỗng), hệ thống từ chối ngay với mã `SHALLOW_HISTORY` vì không đủ lịch sử nối phả hệ.
2. **Sắp xếp topo tất định theo thuật toán Kahn (dòng 89–95):** Thu thập các commit reachable từ `headSha`, sắp xếp thứ tự duyệt từ commit gốc đến ngọn sao cho mọi commit cha đều được xử lý trước commit con; khi nhiều nút sẵn sàng đồng thời, hàng đợi ưu tiên phá hòa theo chuỗi SHA tăng dần thay vì `timestamp` để loại bỏ ảnh hưởng của lệch đồng hồ hệ thống.
3. **Truy vết đổi tên tập tin qua `DiffFormatter` & `RenameDetector` (dòng 78–80, 112–128):** Ở các commit 1 cha (`non-merge`), bật `diff.setDetectRenames(true)` với ngưỡng tương đồng nội dung `renameScore = 60%` và `renameLimit = 1000`. Khi tập tin đổi tên từ `oldPath` sang `newPath`, tập tin tiếp tục kế thừa `FileIdentity.id = "<firstSeenSha>:<initialPath>"` (`lineageKey`) thay vì bị tách thành 2 tập tin mới. Chỉ các `non-merge commit` có `committerTime` nằm trong cửa sổ `[start, end)` mới sinh `Fact` cho bộ đếm `change_frequency`, `churn`, `coupling` và `HHI`.
4. **Quy tắc bảo thủ `LINEAGE_AMBIGUOUS` tại nút merge (dòng 103–110):** Khi đi qua `merge commit` ($\ge 2$ cha), hệ thống đối chiếu định danh `FileIdentity` của từng đường dẫn trên tất cả các nhánh cha. Chỉ khi **mọi** nhánh cha đều tồn tại tập tin đó với cùng một `FileIdentity`, phả hệ mới giữ trạng thái rõ ràng; ngược lại dòng 109 đánh dấu `ambiguous = true` và gắn cờ `LINEAGE_AMBIGUOUS` nhằm tránh ghép nhầm lịch sử hai tập tin trùng tên từ hai nhánh khác nhau.

---

## 3.5. Phân tích độ phức tạp Lizard và mô hình tính chỉ số (`LizardComplexityAnalyzer` & `RepositoryMetricCalculator`)

### 3.5.1. Đo độ phức tạp tại đúng commit lịch sử (`LizardComplexityAnalyzer`)
Hàm `analyzeAtHead(...)` (dòng 111) mở trực tiếp các Git blob `.java` tại đúng cây `headSha` (không phụ thuộc vào trạng thái working directory hiện tại), trích ra thư mục tạm cô lập và gọi tiến trình `lizard -l java --xml` thông qua `ProcessBuilder` với mảng tham số cố định (không qua shell trung gian) để phòng ngừa *command injection*. Dữ liệu XML trả về được phân tích bằng `DocumentBuilderFactory` đã tắt `XXE` nhằm lấy `maxCcn`, `avgCcn`, `functions` và `nloc`. Với các tệp hợp lệ nhưng không có hàm nào (`interface`, `package-info.java`, tệp khai báo hằng), `FileComplexity.noFunctions(nloc)` (dòng 51–52) trả `functions = 0`, `maxCcn = null` kèm lý do `NO_FUNCTIONS`.

### 3.5.2. Chuẩn hóa tính toán chỉ số và chính sách `NULL` vs `0` (`RepositoryMetricCalculator`)
`RepositoryMetricCalculator` hợp nhất kết quả từ `GitIngestion` và `LizardComplexityAnalyzer` theo nguyên tắc giữ nguyên tính trung thực dữ liệu:
- **Phân tách mẫu số `hotspotPopulation`:** Chỉ các tập tin đủ điều kiện đồng thời có `change_frequency != null` và `max_ccn != null` mới tham gia tính phân vị `hotspot_score`. Các tập tin `NO_FUNCTIONS` vẫn xuất hiện trong danh sách tệp nhưng giữ `max_ccn = null`, `hotspot_score = null` và **không** làm `coverageComplete` của snapshot trở thành `false`. Ngược lại, nếu có bất kỳ tập tin hiện tại nào mang cờ `LINEAGE_AMBIGUOUS`, tập tin giữ `change_frequency = null`, `hotspot_score = null`, đồng thời toàn snapshot bị gắn `coverageComplete = false` để chặn cảnh báo sai.
- **Công thức phân vị hạng giữa (*midrank percentile*):** Với $N$ tập tin trong `hotspotPopulation`: nếu $N = 0$ trả tập rỗng (không có hotspot nào); nếu $N = 1$ gán phân vị $0{,}5$; nếu $N > 1$, với mỗi giá trị $v$, đếm số phần tử nhỏ hơn ($L$) và số phần tử bằng ($E$), rồi tính:
  $$P(v) = \frac{L + 0{,}5 \times (E - 1)}{N - 1} \in [0, 1], \qquad \text{hotspot\_score} = \sqrt{P_{\text{change}} \times P_{\text{complexity}}}$$
  Cách tính *midrank* đảm bảo mọi tập tin đồng hạng có cùng phân vị công bằng và đối xứng. Toàn bộ kết quả chia tỷ lệ `hotspot_score` và `coupling_degree` được tính toán chuẩn hóa bằng `BigDecimal` để khớp với kiểu `NUMERIC(7,6)` trong PostgreSQL.
- **Tính `temporal_coupling` và giới hạn an toàn (`BR14`):** Gom nhóm các tập tin theo từng `non-merge commit`; khi tùy chọn lọc commit lớn được bật, bỏ qua các commit sửa quá `30` tập tin để tránh nhiễu đổi tên hàng loạt hoặc sửa format toàn dự án. Chỉ lưu các cặp có `shared_commits >= 5` và `coupling >= 0,50`, tối đa `50.000` cặp mỗi snapshot.
- **Tính `HHI` và bảo vệ danh tính tác giả:** Tổng đóng góp theo tác giả được kiểm chứng đối chiếu cân bằng bằng tổng `churn` của tập tin trước khi xuất bản; nếu `churn == 0` thì gán `HHI = null` (`NO_LINE_CONTRIBUTION`) thay vì `0`. Khóa danh tính tác giả được gom bằng bảng ánh xạ alias nội bộ và không trả email thô ra API công khai.

---

## 3.6. Kiến trúc Trợ lý AI Review có kiểm soát (`LlmReviewService` & `HttpLlmProviderAdapter`)

Nhằm giải quyết trực tiếp hai rủi ro mà người dùng phản ánh ở Mục 1.4 — rò rỉ bí mật mã nguồn và AI tự bịa đặt nhận xét — mô-đun `aireview` được thiết kế hoàn toàn chỉ đọc, tách biệt khỏi bảng điểm sức khỏe và đi qua 4 lớp rào chắn kiểm soát trong `LlmReviewService` và `HttpLlmProviderAdapter`:

1. **Ngân sách dữ liệu và quét khóa bí mật trước khi gọi ra ngoài:** `LlmReviewService` áp dụng ngưỡng cứng cho ngữ cảnh JSON: tổng kích thước `<= 32 KiB` (`MAX_TOTAL_CONTEXT_BYTES`, dòng 29), phần diff `<= 16 KiB`, đoạn mã nguồn trích từ `headSha` `<= 8 KiB` và `<= 200` dòng (`MAX_LINES_PER_SECTION`, dòng 33), tối đa `10` bằng chứng. Biểu thức chính quy `SECRET_PATTERN` (dòng 37–38) quét toàn bộ ngữ cảnh để phát hiện khóa riêng tư `PRIVATE KEY`, token `ghp_` / `github_pat_`, khóa `AKIA` hoặc cặp `api_key` / `secret` / `password`; nếu phát hiện sẽ ngắt ngay và trả mã `403 SECRET_DETECTED`.
2. **Đồng thuận 2 bước gắn mã băm SHA-256 (`Preview` và `ConsentReceipt`):** API bắt buộc gọi trước bước xem trước (`/ai-review-preview`), đóng gói đúng đoạn mã lịch sử + chỉ số + danh sách bằng chứng (`E1..`) kèm mã băm SHA-256 `contextHash`. Khi gửi yêu cầu chính thức (`/ai-reviews`), hệ thống kiểm tra `consent = true` và `contextHash` trùng khớp chính xác (`computeContextHash`, dòng 333); nếu ngữ cảnh bị thay đổi hoặc thiếu đồng thuận sẽ trả `409`/`403`.
3. **Truyền tải HTTP giới hạn tài nguyên (`HttpLlmProviderAdapter`):** Kết nối tới endpoint chuẩn OpenAI Chat Completions (mặc định cấu hình cho Mistral `codestral-latest`) qua `java.net.http.HttpClient`, không truyền tool/function-calling và không lộ API key trong log hay thông báo lỗi. Bộ điều hợp tự tính thời gian chờ theo deadline còn lại của request với trần `MAX_PROVIDER_TIMEOUT = 30 giây` (dòng 37), kiểm tra tín hiệu hủy và đọc phản hồi qua `InputStream` theo từng khối `4 KiB` (dòng 211) để ngắt ngay nếu payload phản hồi vượt quá giới hạn bộ nhớ cho phép.
4. **Kiểm tra cấu trúc JSON và `evidenceAllowlist`:** Chuỗi JSON trả về được phân tích bằng `STRICT_JSON` bật cả `STRICT_DUPLICATE_DETECTION` và `FAIL_ON_TRAILING_TOKENS` (dòng 64–67) để chặn JSON sai định dạng hoặc văn bản rác phía sau. Mọi mục trong danh sách bằng chứng và checklist được đối chiếu bắt buộc với tập `evidenceAllowlist` (chỉ chứa các mã `E1..E10` thực sự có trong payload gửi đi). Nếu AI viện dẫn mã không tồn tại, hệ thống từ chối kết quả bằng mã `502 INVALID_EVIDENCE` và chỉ lưu metadata truy vết (không lưu mã nguồn hay nội dung trả về) vào bảng `ai_review_audit`.

---

# CHƯƠNG 4 *(NỐI TIẾP MỤC 4.1..4.4)*: THỰC NGHIỆM RQ1, BENCHMARK LÕI, ĐỐI CHIẾU CÔNG CỤ VÀ THREATS TO VALIDITY

## 4.5. Thực nghiệm RQ1: Đánh giá khả năng xếp hạng rủi ro bảo trì (`STT 30`)

### 4.5.1. Thiết kế chia cắt thời gian 80/20 và quy trình gán nhãn thủ công
Câu hỏi nghiên cứu `RQ1` kiểm chứng liệu việc kết hợp tần suất sửa và độ phức tạp (`hotspot_score`) có ưu tiên các tập tin phát sinh hoạt động sửa lỗi trong tương lai tốt hơn hai phương pháp đơn lẻ (`frequency` và `complexity`) hay không. Nhóm xây dựng giao thức `week09-rq1-approved-v1` trên **5 kho Java mã nguồn mở chọn mù** trước khi đo (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator` và `javapoet`, đóng băng trong `rq1-frozen-manifest.json`):
- **Chia cắt thời gian chống rò rỉ dữ liệu:** Tách tập `non-merge commit` theo tỷ lệ `80%` quá khứ / `20%` tương lai (mốc cắt `observationSha`), trích xuất đặc trưng trên cửa sổ `180 ngày` trước mốc cắt bằng chính `HistoricalSnapshotExport`. Hàm `validateHistoricalHead` kiểm tra không có commit tổ tiên nào mang timestamp lớn hơn mốc cắt để chặn rò rỉ tương lai. Phá hòa đồng hạng theo `fileId`/`lineageKey`.
- **Rà soát thủ công để loại nhiễu từ khóa:** Nhận thấy nếu chỉ lọc nhãn bug-fix bằng từ khóa `fix/bug` sẽ sinh dương tính giả rất lớn từ các commit sửa comment Javadoc hoặc Checkstyle, nhóm trực tiếp đọc `git show` của **141 commit** trong tập tương lai (gồm `101` commit ứng viên từ khóa/Jira và `40` mẫu âm lấy ngẫu nhiên với `seed=23110170`). Quá trình này loại bỏ hàng loạt commit không sửa hành vi (riêng `commons-validator` giảm từ `44` xuống `18` tập tin dương) và phát hiện thêm `9` commit sửa lỗi hành vi bị bộ từ khóa bỏ sót.

### 4.5.2. Kết quả định lượng chi tiết trên 5 dự án và trung bình vĩ mô

| Dự án | File Java / Đủ ĐK ($N$) | File dương ($P$) | Phương pháp | AP | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 | Spearman $\rho$ |
|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|
| `commons-csv` | 49 / 43 | 12 | `frequency` | 0.6431 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5000 | 0.8333 | 0.6241 |
| `commons-csv` | 49 / 43 | 12 | `complexity` | 0.6808 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5500 | 0.9167 | 0.6067 |
| `commons-csv` | 49 / 43 | 12 | **`hotspot`** | **0.7116** | **0.8000** | **0.3333** | **0.8000** | **0.6667** | **0.5500** | **0.9167** | **0.6282** |
| `commons-codec` | 156 / 126 | 33 | `frequency` | 0.3319 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | 0.4000 | 0.2424 | 0.1297 |
| `commons-codec` | 156 / 126 | 33 | `complexity` | 0.4427 | 0.6000 | 0.0909 | 0.7000 | 0.2121 | 0.4500 | 0.2727 | 0.2371 |
| `commons-codec` | 156 / 126 | 33 | `hotspot` | 0.4244 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | 0.5000 | 0.3030 | 0.2245 |
| `commons-dbutils` | 95 / 84 | 4 | `frequency` | 0.1458 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1000 | 0.5000 | 0.0848 |
| `commons-dbutils` | 95 / 84 | 4 | `complexity` | 0.1821 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1500 | 0.7500 | 0.2413 |
| `commons-dbutils` | 95 / 84 | 4 | `hotspot` | 0.1681 | 0.0000 | 0.0000 | 0.1000 | 0.2500 | 0.1500 | 0.7500 | 0.2651 |
| `commons-validator` | 160 / 148 | 18 | `frequency` | 0.1951 | 0.2000 | 0.0556 | 0.3000 | 0.1667 | 0.1500 | 0.1667 | 0.1527 |
| `commons-validator` | 160 / 148 | 18 | `complexity` | 0.2393 | 0.2000 | 0.0556 | 0.2000 | 0.1111 | 0.2000 | 0.2222 | 0.1569 |
| `commons-validator` | 160 / 148 | 18 | **`hotspot`** | **0.2631** | **0.4000** | **0.1111** | **0.3000** | **0.1667** | **0.2500** | **0.2778** | **0.1749** |
| `javapoet` | 36 / 0 | — | *(Giữ kiểm toán)* | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| **Macro-Average** (`4/5`) | **401 ĐK** | **67** | `frequency` | 0.3290 | 0.4000 | 0.1749 | 0.3750 | 0.2803 | 0.2875 | 0.4356 | — |
| **Macro-Average** (`4/5`) | **401 ĐK** | **67** | `complexity` | 0.3862 | **0.4500** | **0.1824** | **0.4250** | 0.2891 | 0.3375 | 0.5404 | — |
| **Macro-Average** (`4/5`) | **401 ĐK** | **67** | **`hotspot`** | **0.3918** | 0.4000 | 0.1263 | 0.4000 | **0.3011** | **0.3625** | **0.5619** | — |

Toàn bộ kết quả trên `validCases = 4/5` được chạy kiểm chứng tái lập độc lập khớp `13/13` mã băm SHA-256 tệp JSON (`docs/week-09/evidence/rq1/results.md`). Phân tích cho thấy:
- Trên bình diện vĩ mô 4 dự án hợp lệ, `hotspot_score` dẫn đầu ở 4 tiêu chí trọng tâm khi lập kế hoạch review: **MAP (`0,3918`)**, **Recall@10 (`0,3011`)**, **Precision@20 (`0,3625`)** và **Recall@20 (`0,5619`)**.
- Ở các kho có hoạt động sửa lỗi tập trung ở tầng xử lý lõi trong cửa sổ 180 ngày, sự kết hợp hai chiều vượt trội rõ rệt: trên `commons-csv`, `hotspot_score` đạt `AP = 0,7116`, `P@10 = 0,8000` và bắt trúng **`11/12` tập tin phát sinh lỗi trong Top 20 (`R@20 = 0,9167`)**; trên `commons-validator`, `hotspot_score` đạt `AP = 0,2631` và **`P@5 = 0,4000` (gấp đôi cả hai baseline)**.
- Ở `javapoet`, cả `36/36` tập tin bị gắn `LINEAGE_AMBIGUOUS` do quy trình hợp nhất PR qua nhiều nhánh merge commit tại `v1`; thay vì loại bỏ khỏi báo cáo, nhóm giữ nguyên trong bảng kiểm toán để phản ánh trung thực giới hạn thiết kế của phiên bản `v1`. Phân tích độ nhạy khi chỉ tính trên `src/main/java` (bỏ thư mục test) cho thấy xu hướng chung vẫn bảo toàn (`hotspot` đạt `MAP = 0,4298`), trong khi dùng nhãn từ khóa thô chưa rà soát tay làm thổi phồng giả MAP lên `0,5731–0,5898`, khẳng định giá trị của bước rà soát thủ công `141` commit.

---

## 4.6. Thực nghiệm Benchmark hiệu năng lõi `NFR03` và liên kết độ trễ API `NFR01/NFR02` (`STT 32`)

Để kiểm chứng yêu cầu `NFR03` (phân tích kho quy mô đến `10.000` commit trong `<= 10 phút = 600 giây`) trên phần cứng phổ thông, nhóm viết `tools/experiments/run_benchmark.py` sinh kho Git lịch sử chuẩn có kiểm soát qua `git fast-import` ở 3 mốc quy mô (`1.000`, `5.000` và `10.000` commit trong cửa sổ `180 ngày`), đo lặp `3` lần cho mỗi mốc (`1` lượt *cold* + `2` lượt *warm*) trên máy cấu hình **Windows 10, CPU Intel Core i3-7020U @ 2.30 GHz, RAM 19,87 GiB, OpenJDK 21.0.8, JGit 7.8.0, Lizard 1.24.3**, và kết hợp với phép đo PostgreSQL 18.3 của Toản (`Week10PersistenceBenchmarkTests`):

| Quy mô kho thử nghiệm | Số facts trích xuất | Lõi Pipeline TB (Cold / Warm) | JGit (`ms`) | Lizard (`ms`) | Tính Metric (`ms`) | CLI end-to-end TB (`ms`) | PostgreSQL lưu/cảnh báo/so sánh (`ms`) | Tổng thời gian & Đối chiếu `NFR03 <= 600s` |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| **1.000 commit** (`25` file) | 1.320 | **17,21 s** (`24,84 s / 13,40 s`) | 13.733,96 | 2.681,16 | 190,66 | 19.269,25 (`19,27 s`) | **50,08 ms** (`29` alerts) | Đạt (~`19,32 s` CLI + DB) |
| **5.000 commit** (`40` file) | 6.040 | **28,07 s** (`34,62 s / 24,79 s`) | 24.477,67 | 2.548,14 | 436,05 | 41.250,04 (`41,25 s`) | **63,34 ms** (`45` alerts) | Đạt (~`41,31 s` CLI + DB) |
| **10.000 commit** (`50` file) | 12.045 | **48,96 s** (`59,96 s / 43,46 s`) | **45.155,57** (`92,2%`) | 2.439,16 | 708,23 | **73.247,13 (`73,25 s`)** | **59,90 ms** (`55` alerts) | **Đạt xa chuẩn (`1,22 phút` << `10 phút`)** |

*Ghi chú tách biệt phạm vi đo (`benchmark-results.json`):* Các mốc thời gian trên đo trực tiếp trên kho cục bộ (tách riêng khỏi độ trễ mạng `git clone` từ GitHub). Kết quả chỉ rõ điểm nghẽn chủ yếu nằm ở bước duyệt đồ thị DAG và diff của `JGit` (`92,2%` thời gian ở mốc `10.000` commit), trong khi thuật toán tính metric (`0,71 s`) và toàn bộ giao dịch lưu trữ, đánh giá 55 cảnh báo, truy vấn so sánh trên PostgreSQL 18.3 (`0,06 s`) chỉ chiếm tỷ trọng không đáng kể.

Song song với `NFR03`, phép đo trực tiếp độ trễ API REST (`measure_api_latency.py` trên backend + PostgreSQL 18.3 ở Tuần 11, lưu tại `docs/week-11/evidence/nfr01-nfr02-api-latency.json`) xác nhận `POST /api/repositories/{id}/analyses` trả `202 Accepted` với thời gian tối đa `46,00 ms` (đạt xa ngưỡng `NFR01 <= 2.000 ms`), còn `GET /hotspots?size=100` (`p95 = 28,42 ms`) và `GET /files/{id}` (`p95 = 28,05 ms`) đều đạt xa ngưỡng `NFR02 <= 1.000 ms`.

---

## 4.7. Đối chiếu sản phẩm với 5 công cụ hiện có theo bằng chứng thực tế

Dựa trên sản phẩm đã chạy thật và bộ số liệu thu được đến Tuần 11, nhóm đối chiếu lại **Git Health Monitor** với 5 công cụ đã khảo sát ở Chương 1 (`synthesis-and-threats.md`):

| Tiêu chí so sánh | Git Health Monitor (Nhóm) | SonarQube (Community) | CodeScene | Code Maat | CodeCharta | Git-of-Theseus |
|---|---|---|---|---|---|---|
| **Chỉ số cốt lõi** | `change_frequency`, `churn`, `max_ccn`, `hotspot_score`, `temporal_coupling`, `HHI` | Bugs, Vulnerabilities, Code Smells, Coverage | Hotspot, Change Coupling, Code Health, Bus Factor | Churn, Coupling, Revisions, Ownership | Metric cây bản đồ 3D | Tuổi thọ dòng code |
| **Lưu Snapshot & Chặn so sánh sai** | Bất biến theo `headSha` + `configHash`; chặn alert & ngắt chart khi `coverageComplete = false` | Có lưu lịch sử chất lượng, Quality Gate | Có theo dõi xu hướng (bản Cloud/Enterprise) | Không (xuất CSV từng lệnh) | Chỉ đọc JSON tĩnh | Chỉ xuất ảnh tĩnh |
| **Trợ lý AI Review an toàn** | Có: opt-in 2 bước Preview + Consent SHA-256, `SECRET_PATTERN`, `evidenceAllowlist` | Có ở bản mở rộng/trả phí | Có gợi ý refactor AI (bản thương mại) | Không | Không | Không |
| **Triển khai & Chi phí** | Mã nguồn mở, Docker Compose (`PostgreSQL 18`, không Redis), kiểm chứng `172` test tự động | Open-source nhưng JVM/ES nặng hơn | Thương mại theo repo/user | Mã nguồn mở CLI, không có Web UI | Mã nguồn mở 3D, thiếu cảnh báo | Mã nguồn mở CLI |
| **Điểm hạn chế so với đối thủ** | Hiện bản v1 mới đo Java + repo GitHub công khai; merge lineage `v1` bảo thủ | Không khai thác tiến hóa lịch sử Git như hệ thống | Hỗ trợ đa ngôn ngữ & plugin IDE sâu hơn | Linh hoạt nhiều VCS (Git/SVN/Hg) | Trực quan 3D ấn tượng hơn | Thống kê phân rã cohort dòng mã lâu năm tốt hơn |

---

## 4.8. Phân tích các mối đe dọa đến tính hợp lệ (*Threats to Validity*)

1. **Internal Validity (Tính hợp lệ nội tại):**
   - *Nhiễu nhãn sửa lỗi ở RQ1:* Dùng biểu thức từ khóa `fix/bug` thuần túy dễ gán nhầm các commit sửa Javadoc/Checkstyle thành sửa lỗi logic. Nhóm kiểm soát rủi ro này bằng việc rà soát thủ công `git show` của `141` commit (`101` ứng viên + `40` mẫu âm `seed=23110170`), dù vẫn tồn tại khả năng bỏ sót một số commit nằm ngoài tập mẫu âm.
   - *Rò rỉ dữ liệu thời gian (Data Leakage):* Được loại trừ ở cấp công cụ khi `HistoricalSnapshotExport.validateHistoricalHead` từ chối mọi commit tổ tiên có `committerTime >= windowEnd`.
   - *Dịch chuyển phân vị theo quần thể snapshot:* Vì `hotspot_score` dùng percentile trên tập hợp lệ của từng snapshot, khi có file mới thêm vào hoặc rút khỏi quần thể, điểm của một tập tin đứng yên vẫn có thể thay đổi nhẹ. Nhóm đã minh họa tách bạch cơ chế này trong kịch bản `S3` của RQ2 và luôn hiển thị các chỉ số thô (`frequency`, `max_ccn`, `hotspotPopulation`) song song trên giao diện.
2. **External Validity (Khả năng tổng quát hóa):**
   - Thực nghiệm RQ1 mới thực hiện trên 5 dự án thư viện Java mã nguồn mở (`Apache Commons` và `javapoet`), còn benchmark `10.000` commit dùng kho tổng hợp kiểm soát phân bố commit. Tỷ lệ độ phủ và kết quả xếp hạng có thể khác biệt trên các hệ thống microservice đa ngôn ngữ hoặc các kho dùng chiến lược *squash-merge*/*rebase* khác biệt.
   - Quy tắc nối phả hệ qua merge của `v1` (`LINEAGE_AMBIGUOUS` khi không đồng nhất giữa các nhánh cha) mang tính bảo thủ cao nên các kho có mật độ merge PR dày đặc như `javapoet` (`36/36` file) hoặc `commons-cli` bị giảm tập hợp lệ. Tuy nhiên kiểm toán `369` thực thể file Java ở RQ2 chứng minh nếu nới lỏng quy tắc nối theo nhánh cha chính ở phiên bản `v2`, tỷ lệ độ phủ Hotspot tăng từ `42,01%` lên `85,37%`.
3. **Construct Validity (Tính hợp lệ khái niệm):**
   - `hotspot_score` cao phản ánh sự kết hợp giữa tần suất sửa và độ phức tạp trong quá khứ, không phải chứng minh tuyệt đối tập tin đang chứa lỗi; tương tự, chỉ số `HHI` đo mức độ tập trung dòng thay đổi trong cửa sổ và không phản ánh năng lực lập trình viên. Cả giao diện và prompt AI đều gắn kèm cảnh báo giải nghĩa rõ ràng các giới hạn này.
4. **Conclusion Validity (Tính hợp lệ kết luận):**
   - Với `validCases = 4` kho hợp lệ trong RQ1, chênh lệch `Macro-MAP` giữa `hotspot` (`0,3918`) và `complexity` (`0,3862`) còn hẹp và phân hóa theo đặc thù từng kho (`commons-csv` và `commons-validator` thắng rõ, còn `commons-codec` xấp xỉ do các lớp giải mã phức tạp ít thay đổi ở nửa đầu nhưng phát sinh bản vá nhỏ về sau). Nhóm công bố đầy đủ bảng số liệu từng dự án và phân tích độ nhạy thay vì khái quát quá mức. Tương tự, kết quả benchmark hiệu năng và độ phủ `JaCoCo` công bố minh bạch cả hai phạm vi (không DB vs đầy đủ PostgreSQL 18.3) để bảo đảm khả năng tái lập.

---

# CHƯƠNG 5: KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 5.1. Tổng kết kết quả đạt được

Sau 11 tuần triển khai bám sát quy trình kỹ nghệ phần mềm và thực nghiệm khoa học, nhóm đã hoàn thiện hệ thống **Git Health Monitor** đáp ứng toàn bộ mục tiêu đề ra trong đề cương:
1. **Xây dựng hoàn chỉnh hệ thống web đa tầng hoạt động thực tế:** Pipeline Java 21 / Spring Boot 4.1.1 kết hợp `JGit 7.8.0` + `Lizard 1.24.3` lưu trữ trên PostgreSQL 18 (`Flyway V1..V4`), giao diện `React 19 + TypeScript` tương tác trực tiếp REST API, đóng gói Docker Compose kèm cấu hình Staging HTTPS (`Caddy`) và vận hành ổn định qua pipeline GitHub Actions CI `5/5` chặng xanh.
2. **Đảm bảo tính chính xác số liệu và an toàn chống báo động giả:** Thiết kế cơ chế phân biệt rạch ròi `NULL` với `0` (`NO_FUNCTIONS` vs `LINEAGE_AMBIGUOUS`), sử dụng tính toán số thập phân chính xác `BigDecimal`/`NUMERIC`/`Decimal` ở biên cảnh báo `+0.15`, tự động chặn cảnh báo và ngắt đường xu hướng khi `coverageComplete = false`.
3. **Tích hợp trợ lý AI Review có kiểm soát chặt chẽ:** Áp dụng quy trình 2 bước Xem trước gắn mã băm SHA-256 `contextHash` và đồng thuận `ConsentReceipt`, quét secret `SECRET_PATTERN`, giới hạn byte/dòng đọc luồng `4 KiB` và kiểm tra `evidenceAllowlist` chống AI bịa đặt.
4. **Kiểm chứng định lượng và kiểm thử đa tầng đạt ngưỡng cao:** Đạt tổng cộng **172 ca kiểm thử tự động** (`122` JUnit trên PostgreSQL 18.3 với độ phủ dòng lệnh `JaCoCo` đạt **`89,07%`** — mọi package nghiệp vụ đạt `85,88%..100%`, vượt ngưỡng `70%` của `NFR08`; `12` Playwright E2E; `38` Python unittest). Thực nghiệm `RQ1` chứng minh `hotspot_score` dẫn đầu về `MAP` (`0,3918`) và `Recall@20` (`0,5619`) trên 4 kho hợp lệ sau khi duyệt tay `141` commit; thực nghiệm `RQ2` đạt `5/5` kịch bản chuẩn và kiểm toán trọn `369` thực thể Java trên 9 snapshot OSS; Benchmark `NFR03` xử lý `10.000` commit chỉ mất `48,96 s` lõi (`73,25 s` CLI + `59,90 ms` PostgreSQL), đạt xa ngưỡng `10 phút`.
5. **Thực nghiệm người dùng thực tế `RQ3` và hoàn thiện trải nghiệm *(cập nhật số liệu sau khi thu phiếu Google Form)*:** Thực hiện vòng cải tiến giao diện Before/After (Desktop `1440px` và Mobile `375px`) và đánh giá theo 7 tác vụ chuẩn kèm thang điểm SUS `[CHỜ ĐIỀN TỶ LỆ THÀNH CÔNG VÀ ĐIỂM SUS THỰC TẾ TỪ rq3-results.json]`.

## 5.2. Hạn chế còn tồn tại của phiên bản v1

1. **Giới hạn ngôn ngữ và phạm vi kho:** Hiện bộ đo độ phức tạp `Lizard` và bộ lọc tập tin mới cấu hình cho mã nguồn `.java` và các kho GitHub công khai qua HTTPS (chưa hỗ trợ kho private cần token hay các nền tảng GitLab/Bitbucket).
2. **Quy tắc truy vết phả hệ qua merge commit bảo thủ:** Ở phiên bản v1, khi tập tin đi qua merge commit có hai nhánh cha không đồng nhất hoàn toàn, hệ thống đánh dấu `LINEAGE_AMBIGUOUS` để đảm bảo không ghép sai lịch sử; điều này làm giảm tập mẫu đủ điều kiện trên các kho có mật độ merge PR cao (như `javapoet` hoặc `commons-cli`).
3. **Chưa hỗ trợ phân tích trực tiếp Pull Request đang mở và kiến trúc phân tán quy mô doanh nghiệp:** Hệ thống hiện đánh giá trên các commit đã hợp nhất vào nhánh mặc định và chạy theo mô hình Modular Monolith một node cơ sở dữ liệu.

## 5.3. Hướng phát triển trong phiên bản v2

1. **Mở rộng quy tắc truy vết phả hệ ở `GitIngestion v2`:** Áp dụng chiến lược lần theo nhánh cha chính (`first-parent`) kết hợp theo dõi độ tương đồng nội dung qua nút merge để nâng độ phủ quần thể Hotspot từ `42,01%` lên mức `85,37%` như đã ước lượng trong kiểm toán RQ2 mà vẫn kiểm soát được độ chính xác.
2. **Hỗ trợ đa ngôn ngữ và kho private:** Tận dụng lớp trừu tượng sẵn có quanh `LizardComplexityAnalyzer` để mở rộng đo độ phức tạp cho TypeScript, Python, Go, C#; bổ sung quản lý PAT/OAuth có phạm vi chỉ đọc cho các kho nội bộ.
3. **Tích hợp phản hồi trực tiếp trên GitHub Pull Request:** Tự động tóm tắt các tập tin thay đổi rơi vào nhóm Hotspot hoặc thiếu cập nhật cặp `Temporal Coupling` ngay trong nhận xét PR để hỗ trợ review mã nguồn tức thời hơn.

---

# TÀI LIỆU THAM KHẢO

1. **Cunningham, W. (1992).** *The WyCash Portfolio Management System.* OOPSLA '92 Experience Report, ACM SIGPLAN. (Tài liệu nền tảng về khái niệm Nợ kỹ thuật - *Technical Debt*).
2. **Gall, H., Hajek, K., & Jazayeri, M. (1998).** *Detection of Logical Coupling Based on Product Release History.* Proceedings of the International Conference on Software Maintenance (ICSM '98), IEEE, pp. 190–198.
3. **Nagappan, N., & Ball, T. (2005).** *Use of Relative Code Churn Measures to Predict System Defect Density.* Proceedings of the 27th International Conference on Software Engineering (ICSE '05), ACM, pp. 284–292.
4. **Bird, C., Nagappan, N., Murphy, B., Gall, H., & Devanbu, P. (2011).** *Don't Touch My Code! Examining the Effects of Ownership on Software Quality.* Proceedings of the 19th ACM SIGSOFT Symposium on Foundations of Software Engineering (ESEC/FSE '11), pp. 4–14.
5. **Tornhill, A. (2015).** *Your Code as a Crime Scene: Use Forensic Techniques to Arrest Defects, Bottlenecks, and Bad Design in Your Programs.* Pragmatic Bookshelf. (Cơ sở phương pháp *Hotspot*, *Temporal Coupling* và phân tích tiến hóa kho Git).
6. **Brooke, J. (1996).** *SUS: A 'Quick and Dirty' Usability Scale.* In P. W. Jordan, B. Thomas, I. L. McClelland, & B. Weerdmeester (Eds.), *Usability Evaluation in Industry*, Taylor & Francis, pp. 189–194.
7. **Eclipse Foundation (2026).** *Eclipse JGit Documentation (v7.8.0).* Truy cập từ: https://eclipse.dev/jgit/
8. **Yin, T. (2025).** *Lizard: An Extensible Cyclomatic Complexity Analyzer (v1.24.3).* Truy cập từ: https://github.com/terryyin/lizard
9. **Spring Team (2026).** *Spring Boot Reference Documentation (v4.1.1).* Truy cập từ: https://spring.io/projects/spring-boot
10. **PostgreSQL Global Development Group (2025).** *PostgreSQL 18 Documentation.* Truy cập từ: https://www.postgresql.org/docs/18/
