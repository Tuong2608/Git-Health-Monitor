# NỘI DUNG SLIDE THUYẾT TRÌNH, KỊCH BẢN DEMO 5 PHÚT VÀ CẨM NANG ÔN CODE CHÉO 3 BUỔI
*(Phục vụ Chặng 2 và Chặng 3 — STT 38, 39 — Trần Quang Toản 23110158)*

---

# PHẦN 1: NỘI DUNG CHI TIẾT CÁC TRANG SLIDE THUYẾT TRÌNH (CẤU TRÚC 18 TRANG)

Bộ Slide chuẩn 18 trang được chia đều cho hai thành viên để cả hai cùng lên trình bày mạch lạc trước Hội đồng:
- **Tưởng trình bày:** Trang 1–5 (Đặt vấn đề, Mục tiêu, RQ1–RQ3, Kiến trúc tổng thể & Thuật toán JGit/Lizard/Metric/LLM) và Trang 10–11, 17–18 (Thực nghiệm RQ1, Benchmark lõi 10.000 commit, Threats to Validity & Kết luận).
- **Toản trình bày:** Trang 6–9 (Thiết kế CSDL 11 bảng & Queue PostgreSQL, Cơ chế Snapshot/Alert & AI Consent, Giao diện React & Demo trực tiếp) và Trang 12–16 (Thực nghiệm RQ2 `S1..S5` & phân rã 369 file, Benchmark lưu trữ PostgreSQL, Kiểm thử 171 test & Độ phủ JaCoCo `89.07%`, Thực nghiệm người dùng RQ3 & Cải tiến UI Before/After).

Dưới đây là nội dung chữ cô đọng (bullet points đưa lên Slide) và lời nói thuyết trình (Speaker Notes) cho **9 trang Slide của Toản**:

---

### Slide 6: Thiết kế CSDL 11 bảng (Flyway `V1..V4`) & Hàng đợi Job không dùng Redis (`ADR-002`)
- **Nội dung trên Slide:**
  - **Lược đồ 11 bảng PostgreSQL 18 (`V1..V4`):** `tracked_repository`, `analysis_job`, `analysis_snapshot`, `logical_file`, `file_metric`, `contributor`, `file_contribution`, `temporal_coupling`, `alert_rule`, `alert`, `ai_review_audit`.
  - **Hàng đợi Job bền vững (`MonitoringStore` & `AnalysisWorker`):**
    - Nhận việc đồng thời không khóa chéo: `SELECT ... FOR UPDATE SKIP LOCKED`.
    - Chống chạy trùng trên cùng kho: Partial Unique Index `uk_analysis_job_one_active`.
    - Tự phục hồi sau sự cố: Cơ chế thuê bao `lease_until` + `heartbeat` + `recoverExpired()`.
    - Gộp lịch quét định kỳ (`scheduleDue`): Không gây bão job khi server tạm dừng nhiều ngày.
- **Lời thuyết trình của Toản:**
  > *"Thưa Hội đồng, ở tầng dữ liệu, nhóm thiết kế 11 bảng trên PostgreSQL 18 quản lý qua 4 bản migration Flyway. Thay vì dùng Redis làm phức tạp vận hành, nhóm áp dụng ADR-002 đặt hàng đợi ngay trên bảng `analysis_job` kết hợp khóa `FOR UPDATE SKIP LOCKED`, partial unique index chống trùng job và cơ chế `lease + heartbeat` để tự động thu hồi job nếu worker dừng đột ngột."*

---

### Slide 7: Xuất bản Snapshot nguyên tử, Đánh giá Cảnh báo & Kiểm soát AI Review 2 bước
- **Nội dung trên Slide:**
  - **Xuất bản Snapshot nguyên tử (`SnapshotPublisher` — `@Transactional`):**
    - Kiểm tra toàn vẹn tổng `churn` tác giả khớp `churn` tập tin; lỗi 1 tập tin sẽ rollback toàn bộ 6 bảng.
    - Phát hiện không thay đổi (`NO_CHANGE`) theo khóa `(repository_id, head_sha, window, config_hash)`.
  - **Đánh giá Cảnh báo tách giao dịch (`AlertService`):**
    - Chạy ở transaction riêng; tự động chặn phát cảnh báo giả khi `coverageComplete = false` (`INCOMPLETE_COVERAGE`).
  - **Kiểm soát AI Review 2 bước (`ReviewContextBuilder` & `AiReviewController`):**
    - Bước 1 (`Preview`): Đọc đúng blob tại `headSha` của snapshot $\to$ cấp mã băm `contextHash` SHA-256.
    - Bước 2 (`Review`): Bắt buộc `consent = true` khớp `contextHash`; chỉ lưu metadata vào `ai_review_audit`.
- **Lời thuyết trình của Toản:**
  > *"Mỗi snapshot được `SnapshotPublisher` ghi trong một giao dịch nguyên tử. Việc đánh giá cảnh báo được tách sang giao dịch riêng trong `AlertService` và tự động chặn báo động giả khi snapshot chưa đủ độ phủ phả hệ. Với AI Review, hệ thống bắt buộc qua bước Preview lấy mã băm SHA-256 của đúng phiên bản blob lịch sử rồi mới cho phép gửi kèm đồng thuận của người dùng."*

---

### Slide 8: Giao diện React 19 + TypeScript & Cơ chế bảo vệ tính đúng đắn trên UI
- **Nội dung trên Slide:**
  - **Không gian làm việc thống nhất (`AnalysisDashboard.tsx`):**
    - Bản đồ nhiệt Hotspot, bảng chỉ số phân trang, chi tiết đóng góp/HHI, bằng chứng commit, Temporal Coupling.
  - **3 chốt chặn kỹ thuật trên Frontend:**
    1. **Chống ghi đè phản hồi cũ (`Stale Response Guard`):** Hook `useRemote` dùng `AbortController` hủy request cũ ngay khi đổi lựa chọn.
    2. **Phân biệt `NULL` và `0`:** Hiển thị rõ *"Chưa đo được"* kèm `unavailableReason` (`LINEAGE_AMBIGUOUS` / `NO_FUNCTIONS`), không biến `null` thành `0`.
    3. **Biểu đồ xu hướng tự ngắt nét (`Trend`):** Tự tách đoạn (`connectNulls={false}`) khi lệch cấu hình/cửa sổ hoặc thiếu coverage.
- **Lời thuyết trình của Toản:**
  > *"Trên giao diện React 19, nhóm cài đặt 3 cơ chế bảo vệ dữ liệu: dùng `AbortController` để phản hồi mạng chậm của snapshot cũ không bao giờ đè lên lựa chọn mới; giữ nguyên giá trị `null` tách biệt với số `0`; và tự động ngắt nét đường biểu đồ xu hướng khi hai snapshot không tương thích cấu hình hoặc thiếu độ phủ."*

---

### Slide 9: DEMO TRỰC TIẾP HỆ THỐNG (5 PHÚT)
- **Nội dung trên Slide:**
  - **Luồng Demo trực tiếp trên kho mã nguồn thực tế (`spring-petclinic` & `gs-rest-service`):**
    1. Đăng ký kho & Kích hoạt Job phân tích bất đồng bộ (`202 Accepted` $\to$ `RUNNING` $\to$ `SUCCEEDED`).
    2. Xem Bản đồ Điểm nóng (Top 5 Hotspot), mở chi tiết tập tin, hộp công thức chỉ số, HHI, Temporal Coupling và bằng chứng Commit.
    3. Xem Biểu đồ Xu hướng (Trend), So sánh 2 Snapshot và xử lý Cảnh báo (`OPEN` $\to$ `ACKNOWLEDGED`).
    4. Thực hiện AI Review 3 bước: Chọn dòng $\to$ Xem trước `contextHash` SHA-256 $\to$ Tích Consent & Nhận gợi ý.

---

### Slide 12: Thực nghiệm RQ2 — 5 Kịch bản Tiến hóa có kiểm soát (`S1..S5`) (`STT 31`)
- **Nội dung trên Slide:**
  - **Công cụ:** `HistoricalSnapshotExport.java` (chống rò rỉ tương lai qua `validateHistoricalHead`) + `evaluate_rq2_scenarios.py`.
  - **Kết quả: Đạt tuyệt đối 5/5 kịch bản chuẩn (đồng nhất giữa Python & PostgreSQL 18.3):**
    - **S1 (Tăng rủi ro thật):** Frequency & CCN cùng tăng trên `A.java` $\to$ Hotspot $0,00 \to 1,00$, khớp $3/3$ SHA commit.
    - **S2 (Đổi tên `git mv`):** `LegacyService` $\to$ `RenamedService` giữ nguyên `lineageKey`, `added=0, removed=0`, bắt đúng 1 cảnh báo.
    - **S3 (Trượt cửa sổ 180 ngày):** Commit cũ rời cửa sổ (`frequency: 3 -> 0`, `maxCcn = 5` giữ nguyên) $\to$ giải thích rõ do trượt cửa sổ, không nhầm là đã sửa code.
    - **S4 (Biên ngưỡng `+0.15` bằng `Decimal`/`NUMERIC`):** Bước $0,40 \to 0,55$ ($+0,1500$) không báo giả như `float` IEEE-754; bước $0,55 \to 0,775$ ($+0,2250$) báo đúng 1 cảnh báo.
    - **S5 (Chốt chặn an toàn):** Chặn cảnh báo (`BLOCKED` / `409`) khi vướng `LINEAGE_AMBIGUOUS` hoặc lệch cửa sổ 90d vs 180d.

---

### Slide 13: Thực nghiệm RQ2 — Kiểm toán 9 Snapshot OSS & Phân rã 369 thực thể File
- **Nội dung trên Slide:**
  - **Tái lập & Truy vết trên 3 kho OSS (`gs-rest-service`, `spring-petclinic`, `commons-cli`):**
    - Hoàn tất `9/9` snapshot lịch sử (2024–2026), tái lập khớp mã băm SHA-256 `15/15` file JSON.
    - **91/91 cặp tập tin chung (100%)** khớp chính xác danh sách `evidenceCommits` trong cửa sổ.
  - **Phân rã nguyên nhân chặn `INCOMPLETE_COVERAGE` trên 369 lượt file Java (`audit_rq2_coverage.py`):**
    - `155 / 369` (**42,01%**): Đủ dữ liệu vào `hotspotPopulation` (`COMPLETE`).
    - `37 / 369` (**10,03%**): `NO_FUNCTIONS` (interface JPA, `package-info.java`) $\to$ **không** làm `coverageComplete = false`, chỉ tách khỏi mẫu số phân vị.
    - `177 / 369` (**47,97%**): `LINEAGE_AMBIGUOUS` qua merge commit ở `v1` $\to$ làm `coverageComplete = false`. Nếu phân giải merge ở `v2`, quần thể Hotspot đạt **315 / 369 (85,37%)**.

---

### Slide 14: Benchmark Lưu trữ PostgreSQL 18.3 (`STT 32`) & Kiểm thử Toàn hệ thống (`STT 28`)
- **Nội dung trên Slide:**
  - **Chi phí lưu trữ, đánh giá Cảnh báo & So sánh trên PostgreSQL 18.3 (`Week10PersistenceBenchmarkTests`):**
    - Mốc `1.000 commit` (`25` file, `75` dòng đóng góp): **50,08 ms**.
    - Mốc `5.000 commit` (`40` file, `120` dòng đóng góp): **63,34 ms**.
    - Mốc `10.000 commit` (`50` file, `150` dòng đóng góp, `55` cảnh báo): **59,90 ms (~0,06 giây)**.
    - $\Rightarrow$ Cộng vào lõi phân tích (`48,96 s`) chỉ mất tổng cộng **~49,02 giây** (nhanh gấp $>8$ lần ngưỡng `NFR03 <= 600s`).
  - **171 ca kiểm thử tự động trên 4 tầng & 5/5 Job GitHub Actions CI xanh:**
    - `122` JUnit Test (Backend + PostgreSQL 18.3) + `37` Python Experiment Test + `12` Playwright UI E2E Test.

---

### Slide 15: Độ phủ Mã nguồn JaCoCo `0.8.12` trên PostgreSQL 18.3 (`NFR08` & `TC2.5`)
- **Nội dung trên Slide:**
  - **Tổng độ phủ dòng lệnh toàn hệ thống (122 JUnit có PostgreSQL 18.3): `89,07%` (`1.630 / 1.830` dòng)**
    - Instruction Coverage: **89,52%** (`11.802 / 13.184`) | Branch Coverage: **67,80%** (`855 / 1.261`).
  - **Độ phủ theo từng Package nghiệp vụ (tất cả đều vượt ngưỡng `NFR08 >= 70%`):**
    - `com.example.demo.repository`: **100,00%** ($48/48$ dòng)
    - `com.example.demo.api`: **100,00%** ($14/14$ dòng)
    - `com.example.demo.monitoring`: **94,60%** ($333/352$ dòng)
    - `com.example.demo.gitanalyzer`: **88,12%** ($794/901$ dòng)
    - `com.example.demo.aireview`: **85,88%** ($438/510$ dòng)

---

### Slide 16: Thực nghiệm Người dùng RQ3 & Cải tiến Giao diện Before / After (`STT 33, 34`)
- **Nội dung trên Slide:**
  - **Giao thức đánh giá RQ3:** 7 tác vụ chuẩn (`T1..T7`) + thang đo SUS 10 câu (Brooke, 1996) chấm tự động qua `evaluate_rq3_sus.py`.
  - **4 cải tiến giao diện Before / After từ phản hồi người dùng (`ui-before-*.png` vs `ui-after-*.png`):**
    1. Chỉ dẫn xác định nhanh Top 5 Hotspot ngay đầu bảng Điểm nóng (`T2`).
    2. Hộp tra cứu công thức `Hotspot`, `Max CCN`, `HHI` và tooltip giải thích trực tiếp trên thẻ chỉ số (`T3`).
    3. Giải thích rõ sự khác biệt giữa `LINEAGE_AMBIGUOUS` và `NO_FUNCTIONS` khi `coverageComplete = false` (`T5, T6`).
    4. Thanh chỉ dẫn Quy trình 3 bước và hiển thị tường minh mã xác thực `contextHash` SHA-256 trong AI Review (`T7`).

---

# PHẦN 2: KỊCH BẢN DEMO TRỰC TIẾP 5 PHÚT (& QUAY VIDEO DỰ PHÒNG)

> **Chuẩn bị trước giờ Demo / trước khi bấm quay Video:**
> - Mở sẵn trình duyệt ở địa chỉ Staging HTTPS (hoặc `http://localhost:5173`), zoom màn hình `110%` cho Hội đồng dễ nhìn.
> - Đã chạy sẵn `deploy/seed_staging.py` để kho `spring-projects/spring-petclinic` đã có Snapshot hoàn chỉnh, còn kho `spring-guides/gs-rest-service` để trống nhằm bấm đăng ký trực tiếp.

| Thời lượng | Thao tác trên màn hình | Lời thuyết minh của Toản khi bấm Demo |
|---|---|---|
| **Phút 0:00 – 0:45**<br>*(Tác vụ T1: Đăng ký & Chạy Job bất đồng bộ)* | - Dán URL `https://github.com/spring-guides/gs-rest-service` vào ô đăng ký, bấm **Đăng ký repository**.<br>- Bấm **Mở phân tích gs-rest-service**, để cửa sổ `180` ngày và bấm **Phân tích repository**.<br>- Chuyển sang tab **Lịch sử phân tích** chỉ cho Hội đồng thấy Job `#...` chuyển từ `QUEUED` $\to$ `RUNNING` $\to$ `SUCCEEDED`. | *"Đầu tiên em xin demo luồng đăng ký và phân tích bất đồng bộ. Khi bấm Phân tích, API trả ngay mã 202 Accepted trong chưa tới 50ms để không treo giao diện, còn AnalysisWorker ở nền nhận job từ bảng PostgreSQL qua cơ chế lease và chạy JGit cùng Lizard."* |
| **Phút 0:45 – 2:15**<br>*(Tác vụ T2, T3, T4: Điểm nóng, Chi tiết file, HHI & Coupling)* | - Chọn kho `spring-projects/spring-petclinic`, bấm **Mở #1** trong danh sách Snapshot.<br>- Chỉ vào dòng hướng dẫn Top 5 Hotspot, bản đồ nhiệt và bảng phân trang.<br>- Bấm vào tập tin đứng đầu bảng, mở hộp **Giải thích công thức chỉ số (Hotspot, Max CCN, HHI)**.<br>- Cuộn xuống phần **Đóng góp theo thay đổi dòng**, **Bằng chứng commit** và bấm vào 1 tập tin trong mục **File thường thay đổi cùng** để mở **Bằng chứng cặp file**. | *"Trên kho Spring PetClinic, hệ thống xếp hạng các file theo điểm Hotspot giảm dần. Khi bấm vào một file rủi ro cao, hệ thống không chỉ đưa ra một con số hộp đen mà giải thích đầy đủ tần suất sửa, độ phức tạp Max CCN, chỉ số tập trung đóng góp HHI, danh sách SHA commit bằng chứng và các cặp file có liên kết thay đổi Temporal Coupling."* |
| **Phút 2:15 – 3:30**<br>*(Tác vụ T5, T6: Xu hướng, Chốt chặn Coverage & Cảnh báo)* | - Chỉ vào thông báo giải thích `coverageComplete = false` (`LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS`) và biểu đồ **Xu hướng file**.<br>- Chuyển sang tab **Giám sát & cảnh báo**, chỉ vào phần cấu hình chu kỳ quét, danh sách **Quy tắc đã lưu** và bấm nút **Đã xem** trên một thẻ **Cảnh báo** (`OPEN` $\to$ `ACKNOWLEDGED`). | *"Điểm đặc biệt của hệ thống là tính trung thực dữ liệu: khi kho có merge commit chưa rõ nhánh cha làm `coverageComplete = false`, hệ thống hiển thị rõ nguyên nhân và tự động chặn cảnh báo giả. Ở tab Giám sát, người dùng có thể bật lịch quét định kỳ và xác nhận trạng thái xử lý từng cảnh báo."* |
| **Phút 3:30 – 5:00**<br>*(Tác vụ T7: Trợ lý AI Review có kiểm soát)* | - Quay lại phần **Chi tiết file**, cuộn tới **Trợ lý AI review**.<br>- Chỉ vào dòng **Quy trình 3 bước**, nhập dòng `1` đến `25` và bấm **Xem trước dữ liệu gửi**.<br>- Chỉ vào dòng **Mã xác thực ngữ cảnh (`contextHash` SHA-256)** và khung JSON ngữ cảnh.<br>- Tích vào ô checkbox **Tôi đồng ý gửi đúng dữ liệu đang xem...** và bấm **Gửi yêu cầu AI review**.<br>- Chỉ vào kết quả có cấu trúc (`Nhận xét rủi ro`, `Bằng chứng E1..`, `Điểm cần kiểm tra`, `Giới hạn`). | *"Cuối cùng là Trợ lý AI Review có kiểm soát. Hệ thống bắt buộc qua bước Xem trước: trích xuất đúng đoạn mã tại commit SHA của snapshot và khóa bằng mã băm SHA-256 `contextHash`. Chỉ khi người dùng tích đồng thuận Consent và đoạn mã không chứa secret, hệ thống mới gọi LLM và ép phản hồi chỉ được trích dẫn các mã bằng chứng có thật."* |

---

# PHẦN 3: CẨM NANG ÔN CODE CHÉO 3 BUỔI & TRẢ LỜI PHẢN BIỆN HỘI ĐỒNG

## 3.1. Tài liệu Buổi 1 — Tóm tắt phần của Tưởng để Toản nắm lòng (`gitanalyzer` & `aireview`)

Khi Hội đồng gọi tên Toản nhưng hỏi sang phần của Tưởng, bạn cần mở đúng 4 file sau trong IDE và giải thích theo đúng các ý dưới đây:

1. **`GitIngestion.java` (`backend/src/main/java/com/example/demo/gitanalyzer/GitIngestion.java`):**
   - **Làm sao lấy lịch sử mà không tính nhầm merge commit?** Hàm `extract()` dùng JGit `RevWalk` duyệt đồ thị commit từ `headSha`, sắp theo thứ tự topo từ cũ đến mới để nối phả hệ `FileIdentity`. Tuy nhiên, khi ghi nhận sự kiện thay đổi (`Fact`: `additions`, `deletions`, `author`) vào cửa sổ `[windowStart, windowEnd)`, hệ thống **bỏ qua các commit có `parentCount > 1` (merge commit)** để không đếm lặp dòng code đã sửa ở nhánh con.
   - **Làm sao theo dõi đổi tên file (`git mv`)?** Dùng `DiffFormatter` bật `setDetectRenames(true)` với `RenameDetector` có ngưỡng tương đồng nội dung `renameScore = 60%`. Khi file đổi tên từ `A.java` sang `B.java`, nó vẫn giữ nguyên `FileIdentity.id = "<firstSeenSha>:<initialPath>"` (`lineageKey`).
   - **Vì sao sinh ra `LINEAGE_AMBIGUOUS` (dòng 103–110)?** Khi duyệt qua một merge commit có $\ge 2$ cha, hệ thống kiểm tra định danh của từng đường dẫn trên các nhánh cha. Nếu tập tin có mặt ở các cha với định danh khác nhau hoặc chỉ xuất hiện từ một nhánh theo cách quy tắc `v1` chưa thể khẳng định duy nhất phả hệ, nó gán `ambiguous = true` (`LINEAGE_AMBIGUOUS`) để thà báo *"chưa đo được"* chứ không ghép sai lịch sử hai file khác nhau.

2. **`LizardComplexityAnalyzer.java`:**
   - Không đọc file trên ổ đĩa working tree hiện tại mà dùng `ObjectReader` của JGit trích xuất nội dung blob của từng file `.java` tại đúng cây `headSha` ra thư mục tạm, gọi tiến trình `lizard --xml` qua `ProcessBuilder` (có timeout cứng), đọc XML lấy `maxCcn`, `avgCcn`, `functions`, `nloc` rồi xóa sạch thư mục tạm.
   - Nếu file Java là `interface`, `record` không hàm hoặc `package-info.java`, Lizard trả về `functions = 0`, hệ thống gán `maxCcn = null` và cờ `NO_FUNCTIONS`.

3. **`RepositoryMetricCalculator.java`:**
   - **Phân vị hạng giữa (`midrankPercentile`):** Với quần thể $N$ tập tin hợp lệ (có cả `frequency != null` và `maxCcn != null`):
     - Nếu $N = 0$: trả tập rỗng.
     - Nếu $N = 1$: trả phân vị cố định $0,5$ (vì 1 phần tử đứng ở chính giữa).
     - Nếu $N > 1$: Với giá trị $v$, đếm số phần tử nhỏ hơn ($L$) và số phần tử bằng nó ($E$), tính $\text{midrank} = L + \frac{E - 1}{2}$, sau đó chia cho $N - 1$:
       $$P(v) = \frac{L + 0,5 \times (E - 1)}{N - 1} \in [0, 1]$$
     - Điểm nóng: $\text{Hotspot} = \sqrt{P_{\text{change}} \times P_{\text{complexity}}}$.
   - **HHI (`computeHhi`):** Với mỗi tác giả $i$ đóng góp $c_i = \text{additions}_i + \text{deletions}_i$ dòng trên tổng $\text{Churn} = \sum c_i > 0$, tính $p_i = c_i / \text{Churn}$ và $\text{HHI} = \sum p_i^2 \in (0, 1]$. Nếu $\text{Churn} = 0$ thì $\text{HHI} = \text{null}$ (`NO_LINE_CONTRIBUTION`).

4. **`LlmReviewService.java` & `HttpLlmProviderAdapter.java`:**
   - `LlmReviewService` kiểm tra: (1) `consentReceipt` khớp `contextHash` SHA-256 của payload; (2) kích thước tổng $\le 32\text{ KiB}$, đoạn mã $\le 8\text{ KiB}$ và $\le 200$ dòng; (3) quét regex `SECRET_PATTERN` (chặn `PRIVATE KEY`, `ghp_`, `sk-`, `AKIA`, `password=...`, `api_key=...`); (4) sau khi nhận phản hồi từ `HttpLlmProviderAdapter` (đọc `InputStream` theo từng khối $4\text{ KiB}$ có kiểm tra deadline còn lại và giới hạn phản hồi $\le 64\text{ KiB}$), parse JSON nghiêm ngặt (chặn trailing garbage) và kiểm tra mọi `evidenceId` mà AI viện dẫn phải nằm trong tập `evidenceAllowlist` (`E1, E2...`) đã gửi đi.

---

## 3.2. Tài liệu Buổi 2 — Đề cương để Toản giảng lại phần của mình cho Tưởng (`monitoring`, `repository`, `aireview`, `frontend`)

Trong Buổi 2, Toản mở lần lượt 6 file sau để chỉ cho Tưởng:
1. **`V2__monitoring_and_snapshots.sql`, `V3__nullable_file_metrics.sql`, `V4__ai_review_audit.sql`:**
   - Giải thích Partial Unique Index `uk_analysis_job_one_active` trên `analysis_job(repository_id) WHERE status IN ('QUEUED', 'RUNNING')` (cách PostgreSQL chặn 2 job chạy trùng ở mức engine CSDL).
   - Giải thích khóa ngoại kép `(snapshot_id, file_id, repository_id)` giữa `file_metric` và `file_contribution`, cột sinh tự động `coupling_degree GENERATED ALWAYS AS ... STORED` trong `temporal_coupling`, và việc `V3` cho phép `change_frequency`, `churn`, `max_ccn`, `hotspot_score` nhận giá trị `NULL` kèm ràng buộc `CHECK` bắt buộc phải có `unavailable_reason`.
2. **`MonitoringStore.java` & `AnalysisWorker.java`:**
   - Mở hàm `claim()`: giải thích `SELECT id FROM analysis_job WHERE status='QUEUED' ORDER BY requested_at,id LIMIT 1 FOR UPDATE SKIP LOCKED` và việc sinh `claim_token` + `lease_until`.
   - Mở hàm `scheduleDue()`: giải thích công thức nhảy mốc `next_scan_at` qua các chu kỳ bị lỡ để gộp thành 1 job duy nhất.
3. **`SnapshotPublisher.java` & `AlertService.java`:**
   - Mở `SnapshotPublisher.publish()`: chỉ dòng kiểm tra `claim_token` còn hạn, dòng kiểm tra `existing` trả `NO_CHANGE`, vòng lặp kiểm tra `contributed != f.churn()` và lệnh `complete(c, snapshot, "CREATED")` trong cùng `@Transactional`.
   - Mở `AlertService.evaluate()`: chỉ dòng 81–94 kiểm tra `coverageComplete` và ghi `INCOMPLETE_COVERAGE` vào `evaluation_notes`, cùng câu SQL dòng 96 so sánh `n.hotspot_score - o.hotspot_score > ?` trên kiểu `NUMERIC`.
4. **`SnapshotQueries.java`:**
   - Mở hàm `contributions()` (dòng 31): chỉ rõ câu `SELECT` cố ý loại bỏ cột `identity_key` và `aliases` để không làm lộ email cá nhân của lập trình viên ra API công khai.
   - Mở hàm `comparison()` (dòng 50–88): chỉ rõ kiểm tra thứ tự thời gian, `config_hash`, độ dài cửa sổ, `coverageComplete` và câu truy vấn `FULL OUTER JOIN` giữ nguyên `NULL` (`reason = "UNAVAILABLE"` hoặc `"BASE_ZERO"`).
5. **`ReviewContextBuilder.java` & `AiReviewController.java`:**
   - Mở `ReviewContextBuilder.build()`: chỉ cách dùng JGit `RevWalk` + `TreeWalk.forPath(git, pathAtSnapshot, commit.getTree())` đọc đúng blob tại `headSha` của snapshot từ bare repo trong cache, gán nhãn `E1..E5` cho commit và `E6..` cho coupling, rồi băm SHA-256 ra `contextHash`.
6. **`AnalysisDashboard.tsx`:**
   - Mở hook `useRemote` (dòng 15–34): chỉ `new AbortController()` và điều kiện `state.path === path && state.refresh === refresh`.
   - Mở hàm `Trend` (dòng 109–120): chỉ điều kiện tăng `segment++` để tách đường biểu đồ `series0, series1...` khi cấu hình hoặc `coverageComplete` không tương thích.

---

## 3.3. Tài liệu Buổi 3 — Đáp án chuẩn trả lời 5 câu hỏi phản biện cốt lõi của Hội đồng

### Câu hỏi 1: *"Tại sao trong thực nghiệm RQ1 kho `javapoet` lại ra 0 file đủ điều kiện (`eligibleFiles = 0`), và trong thực nghiệm RQ2 cả 6/6 cặp dự án OSS đều bị trạng thái `BLOCKED`? Như vậy công cụ của nhóm có bị lỗi không?"*
- **Cách trả lời chuẩn:**
  > *"Dạ thưa Hội đồng, đó không phải lỗi crash mà là **cơ chế bảo vệ an toàn có chủ đích (Fail-safe against False Positives)** của quy tắc nối phả hệ `v1` trong `GitIngestion.java` (dòng 103–110). Khi một dự án dùng GitHub Merge PR dày đặc như `javapoet` hoặc `commons-cli`, tại các nút merge commit có 2 nhánh cha, nếu một tập tin chỉ thay đổi hoặc có định danh lệch giữa các nhánh cha, quy tắc `v1` đánh dấu `LINEAGE_AMBIGUOUS` và gán `frequency = null` thay vì đoán mò nhánh cha để tránh ghép nhầm lịch sử.*
  > *Để chứng minh thuật toán và hệ thống hoàn toàn chính xác, ở Tuần 10 nhóm đã làm 2 việc:*
  > *1. Xây dựng 5 kịch bản Git có kiểm soát (`S1..S5`) chạy qua đúng pipeline `HistoricalSnapshotExport`: cả 5/5 kịch bản đều phát hiện chính xác 100% biến động thật, giữ đúng `lineageKey` qua đổi tên `git mv` và chặn đúng tại biên `+0.15`.*
  > *2. Viết công cụ `audit_rq2_coverage.py` phân rã toàn bộ **369 thực thể file Java** trên 9 snapshot OSS: chứng minh **91/91 tập tin chung** có điểm chẩn đoán đều khớp 100% commit bằng chứng, tách bạch rõ `37` trường hợp `NO_FUNCTIONS` (interface không hàm) với `177` trường hợp `LINEAGE_AMBIGUOUS`, và chỉ ra rằng nếu mở rộng quy tắc merge một nhánh cha ở `v2`, tỷ lệ độ phủ Hotspot sẽ tăng từ **42,01% lên 85,37%**."*

### Câu hỏi 2: *"Ở RQ1, chỉ số `MAP` trung bình của `hotspot_score` đạt `0,3918`, chỉ cao hơn một chút so với chỉ dùng `complexity` (`0,3862`). Vậy việc kết hợp cả tần suất thay đổi và độ phức tạp có thực sự hiệu quả không?"*
- **Cách trả lời chuẩn:**
  > *"Dạ trên tập 4 kho hợp lệ (`401` file Java, `67` file có bug-fix được duyệt tay từ `141` commit), `hotspot_score` không chỉ dẫn đầu về **Macro-MAP (`0,3918` so với `0,3862` của `complexity` và `0,3290` của `frequency`)**, mà còn dẫn đầu ở toàn bộ các thước đo quan trọng khi rà soát thực tế là **Recall@10 (`0,3011`)**, **Precision@20 (`0,3625`)** và **Recall@20 (`0,5619`)**.*
  > *Đặc biệt, trên các dự án đang phát triển tích cực ở lớp lõi như `commons-csv`, `hotspot_score` đạt **AP = 0,7116, Precision@10 = 0,80 và tìm ra tới 91,67% tổng số file có lỗi trong Top 20 (`Recall@20 = 0,9167`)**; hay trên `commons-validator`, `Precision@5` của `hotspot_score` đạt **0,40 — gấp đôi** so với cả hai baseline (`0,20`). Trường hợp `complexity` xấp xỉ ở `commons-codec` là do một số lớp giải mã phức tạp ít bị sửa trong 180 ngày quan sát nhưng phát sinh bản vá nhỏ ở kỳ sau — điều này đã được nhóm phân tích minh bạch trong phần Threats to Validity."*

### Câu hỏi 3: *"Vì sao đề tài lại bỏ Redis/RabbitMQ mà lưu trạng thái Job trực tiếp trong PostgreSQL? Nếu repository có tới 10.000 commit thì PostgreSQL có bị nghẽn không?"*
- **Cách trả lời chuẩn:**
  > *"Dạ nhóm đã lập quyết định kiến trúc **ADR-002** và kiểm chứng bằng số liệu benchmark thực tế trên PostgreSQL 18.3 (`Week10PersistenceBenchmarkTests`). Việc dùng bảng `analysis_job` với khóa `SELECT ... FOR UPDATE SKIP LOCKED` mang lại 2 lợi ích lớn: thứ nhất là giảm một thành phần hạ tầng dễ lệch trạng thái khi restart; thứ hai là `SnapshotPublisher` có thể ghi toàn bộ 6 bảng dữ liệu snapshot và cập nhật trạng thái job sang `SUCCEEDED` trong **cùng một giao dịch ACID duy nhất**.*
  > *Về hiệu năng, phép đo thực nghiệm ở quy mô **10.000 commit** cho thấy thời gian phân tích tập trung ở chặng duyệt DAG của `JGit` (`45,16 giây`, chiếm `92,2%`), trong khi toàn bộ chu trình ghi snapshot vào PostgreSQL, đánh giá 55 cảnh báo và chạy truy vấn `FULL OUTER JOIN` so sánh 2 snapshot chỉ tốn **59,90 mili-giây (`~0,06 giây`)** — tức chưa tới `0,15%` tổng thời gian chạy."*

### Câu hỏi 4: *"Việc tích hợp LLM (AI Review) vào hệ thống có nguy cơ làm lộ mã nguồn nhạy cảm, AI bịa đặt nhận xét (hallucination) hoặc làm sai lệch điểm số đánh giá sức khỏe dự án không?"*
- **Cách trả lời chuẩn:**
  > *"Dạ nhóm đã thiết kế 4 lớp hàng rào kỹ thuật để cô lập hoàn toàn các rủi ro này:*
  > *1. **Cô lập một chiều:** Module `aireview` chỉ đọc dữ liệu từ snapshot đã lưu; đầu ra của LLM tuyệt đối không bao giờ được ghi ngược vào `hotspot_score` hay bảng `file_metric`.*
  > *2. **Đồng thuận 2 bước gắn mã băm SHA-256 (`Preview & ConsentReceipt`):** Người dùng phải bấm Xem trước đúng đoạn mã (tối đa 200 dòng / 8 KiB lấy từ đúng blob `headSha` của snapshot). Hệ thống tính `contextHash` SHA-256; chỉ khi người dùng tích `consent = true` khớp đúng `contextHash` đó thì yêu cầu mới được xử lý.*
  > *3. **Quét Secret trước khi gửi (`SECRET_PATTERN`):** Nếu đoạn mã hoặc message chứa khóa bí mật (`PRIVATE KEY`, `ghp_`, `sk-`, `AKIA`, `password`...), `LlmReviewService` chặn ngay với mã `403 SECRET_DETECTED` và không gọi provider.*
  > *4. **Chống ảo giác bằng `evidenceAllowlist`:** Phản hồi JSON từ LLM bị kiểm tra schema nghiêm ngặt và chỉ được phép trích dẫn các mã bằng chứng (`E1..E5`) có thật trong ngữ cảnh gửi đi; nếu AI tự bịa mã bằng chứng khác, hệ thống từ chối ngay với mã `502 Bad Gateway`."*

### Câu hỏi 5: *"Làm thế nào nhóm đảm bảo phép tính tăng điểm Hotspot (`> +0.15`) và cảnh báo không bị sai lệch do sai số dấu phẩy động hoặc do cửa sổ thời gian trượt?"*
- **Cách trả lời chuẩn:**
  > *"Dạ nhóm giải quyết bằng hai cơ chế đã kiểm chứng qua kịch bản `S3` và `S4`:*
  > *1. **Chống sai số dấu phẩy động nhị phân IEEE-754 (`S4`):** Trong kiểu `float/double` thông thường, phép tính `0.55 - 0.40` ra `0.15000000000000002 > 0.15`, dẫn tới phát cảnh báo giả ngay tại biên ngưỡng. Nhóm sử dụng kiểu thập phân chính xác trên toàn tuyến: `BigDecimal` trong Java, `NUMERIC(7,6)` trong PostgreSQL (`AlertService` & `SnapshotQueries`) và `Decimal` trong Python (`rq2_trends.py`).*
  > *2. **Giải thích trượt cửa sổ thời gian (`S3`):** Vì `hotspot_score` kết hợp phân vị trong cửa sổ trượt (ví dụ 180 ngày), khi các commit cũ rời khỏi cửa sổ mà file không có commit mới, điểm `hotspot_score` có thể giảm dù `max_ccn` giữ nguyên. Giao diện và API của nhóm luôn đặt các chỉ số gốc (`change_frequency`, `churn`, `max_ccn`, `hotspotPopulation`) cùng danh sách `evidenceCommits` bên cạnh `hotspot_score` để người dùng thấy rõ nguyên nhân biến động."*
