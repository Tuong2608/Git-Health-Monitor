# NỘI DUNG SLIDE THUYẾT TRÌNH PHẦN TƯỞNG VÀ ĐỀ CƯƠNG ÔN CODE CHÉO BUỔI 1
*(Ghép bổ sung cùng [slide-demo-va-on-bao-ve-toan.md](slide-demo-va-on-bao-ve-toan.md) để thành bộ Slide 18 trang hoàn chỉnh — Trần Văn Tưởng 23110170)*

---

# PHẦN 1: NỘI DUNG CHI TIẾT 9 TRANG SLIDE CỦA TƯỞNG (KHỚP VỚI 9 TRANG CỦA TOẢN)

Thứ tự bộ Slide 18 trang:
- **Tưởng (Trang 1–5):** Đặt vấn đề, Khảo sát, Mục tiêu/Phạm vi, Kiến trúc tổng thể & Thuật toán lõi + AI Review.
- **Toản (Trang 6–9):** Thiết kế CSDL 11 bảng & Queue PostgreSQL, Snapshot/Alert & Consent, React UI & Demo trực tiếp 5 phút.
- **Tưởng (Trang 10–11):** Thực nghiệm RQ1 & Benchmark lõi 10.000 commit (`NFR03`).
- **Toản (Trang 12–16):** Thực nghiệm RQ2 (`S1..S5` & phân rã 369 file), Benchmark PostgreSQL, Kiểm thử 172 test & JaCoCo `89,07%`, RQ3 & Cải tiến UI.
- **Tưởng (Trang 17–18):** Đối chiếu công cụ, *Threats to Validity* & Kết luận / Hướng phát triển.

---

### Slide 1: Trang bìa đề tài
- **Nội dung trên Slide:**
  - **Tiêu đề:** XÂY DỰNG HỆ THỐNG PHÂN TÍCH VÀ GIÁM SÁT SỨC KHỎE DỰ ÁN DỰA TRÊN DỮ LIỆU GIT (*GIT HEALTH MONITOR*)
  - **Môn học:** Tiểu luận chuyên ngành Công nghệ phần mềm - HK1 (2026–2027)
  - **Giảng viên hướng dẫn:** PGS.TS. Hoàng Văn Dũng
  - **Nhóm sinh viên (Topic ID: 265):** Trần Văn Tưởng (`23110170`) & Trần Quang Toản (`23110158`)

---

### Slide 2: Đặt vấn đề & Khoảng trống từ khảo sát
- **Nội dung trên Slide:**
  - **Thực tế bảo trì:** Tệp vừa *phức tạp* vừa *sửa đổi liên tục* rủi ro hơn tệp phức tạp nhưng ổn định; dev mất `30 phút – 3 giờ` lần `git blame`/`log` qua khảo sát `5` lập trình viên (`S01..S05`).
  - **Khoảng trống 5 công cụ hiện có:**
    - `SonarQube`: Mạnh phân tích tĩnh, thiếu lịch sử tiến hóa Git & `temporal coupling`.
    - `CodeScene`: Mạnh tiến hóa nhưng đóng mã nguồn, trả phí.
    - `Code Maat` / `CodeCharta` / `Git-of-Theseus`: Chỉ xuất CLI/JSON tĩnh, thiếu web giám sát & cảnh báo theo thời gian.
  - **Yêu cầu rút ra:** Cần công cụ mở minh bạch công thức, có chuỗi snapshot bất biến, chặn cảnh báo giả khi thiếu dữ liệu và trợ lý AI giải thích có kiểm soát.
- **Lời thuyết trình:**
  > *"Kính thưa Hội đồng, trong bảo trì phần mềm, câu hỏi khó nhất không phải file nào dài hay phức tạp nhất, mà là file nào vừa phức tạp vừa bị sửa lặp lại và kéo theo nhiều file khác. Qua đối chiếu 5 công cụ và khảo sát 5 người phát triển, nhóm nhận thấy chưa có công cụ mã nguồn mở nào kết hợp liền mạch lịch sử Git + độ phức tạp, lưu snapshot bất biến có cảnh báo an toàn và trợ lý AI chỉ đọc gắn bằng chứng."*

---

### Slide 3: Mục tiêu, Phạm vi & Câu hỏi nghiên cứu
- **Nội dung trên Slide:**
  - **Mục tiêu:** Đo rủi ro ưu tiên rà soát, giám sát biến động qua các snapshot và hỗ trợ giải thích bằng AI có rào chắn bảo mật.
  - **Phạm vi v1 (In-Scope / Out-of-Scope):** Hỗ trợ kho GitHub công khai qua HTTPS và mã nguồn Java; không checkout/thực thi mã nguồn quét, không thay thế SAST (SonarQube), không tự động sửa code/PR.
  - **3 Câu hỏi nghiên cứu:**
    - `RQ1`: `hotspot_score` có xếp hạng ưu tiên file phát sinh bug-fix tốt hơn `frequency` và `complexity` đơn lẻ không?
    - `RQ2`: Hệ thống có theo dõi đúng xu hướng qua nhiều snapshot và chặn cảnh báo sai khi thiếu độ phủ hay đổi tên file không?
    - `RQ3`: Hệ thống có đạt hiệu năng `<= 10.000 commit / <= 10 phút` (`NFR03`) và khả năng sử dụng với người dùng thật (`SUS >= 68`, `Task Success >= 80%`) không?

---

### Slide 4: Kiến trúc tổng thể Modular Monolith (`ADR-001` & `ADR-002`)
- **Nội dung trên Slide:**
  - **3 thành phần triển khai qua Docker Compose + Caddy HTTPS:**
    - **Backend (`Java 21`, `Spring Boot 4.1.1`):** 4 package rõ ranh giới (`repository`, `monitoring`, `gitanalyzer`, `aireview`).
    - **Database (`PostgreSQL 18`, `Flyway V1..V4`):** Lưu 11 bảng quan hệ + cột `JSONB` + hàng đợi job bất đồng bộ.
    - **Frontend (`React 19`, `TypeScript`, `Recharts`):** Dashboard 3 tab kết nối REST API.
  - **2 Quyết định kiến trúc chính:**
    - `ADR-001`: Dùng `Eclipse JGit 7.8.0` clone bare repository cục bộ (tránh rate-limit GitHub API & nghẽn partial clone).
    - `ADR-002`: Hàng đợi PostgreSQL thay thế Redis để ghi snapshot + đổi trạng thái job trong 1 transaction ACID.

---

### Slide 5: Thuật toán lõi & Rào chắn Trợ lý AI Review
- **Nội dung trên Slide:**
  - **Thuật toán trích xuất (`GitIngestion` + `Lizard 1.24.3` + `RepositoryMetricCalculator`):**
    - Sắp topo Kahn, chỉ tính `non-merge commit` trong cửa sổ 180 ngày, bật `RenameDetector` (`>= 60%`).
    - Giữ trung thực `NULL`: `NO_FUNCTIONS` tách khỏi mẫu số; `LINEAGE_AMBIGUOUS` qua merge bật `coverageComplete = false`.
    - `hotspot_score = sqrt(P_change * P_complexity)` (midrank percentile); `coupling = shared / min(|C_A|, |C_B|)`; `HHI = sum(p_i^2)` (ẩn email thô).
  - **4 lớp an toàn AI Review (`LlmReviewService` & `HttpLlmProviderAdapter`):**
    - Preview + `ConsentReceipt` SHA-256 -> Quét `SECRET_PATTERN` -> Trần 32 KiB / 200 dòng, timeout `<= 30s`, đọc stream `4 KiB` -> Ép schema JSON + `evidenceAllowlist` (`E1..E10`).

---

### Slide 10: Thực nghiệm RQ1 — Giao thức 80/20 & Duyệt tay 141 Commit (`STT 30`)
- **Nội dung trên Slide:**
  - **5 kho Java OSS chọn mù trước khi đo (`rq1-frozen-manifest.json`):** `commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`.
  - **Giao thức `week09-rq1-approved-v1`:**
    - Cắt `80%` quá khứ (tính đặc trưng trên cửa sổ 180 ngày tại `observationSha` bằng `HistoricalSnapshotExport`) / `20%` tương lai (đối chiếu nhãn lỗi).
    - Chống rò rỉ thời gian qua `validateHistoricalHead` (`committerTime < cutoff`).
  - **Loại nhiễu nhãn bằng rà soát thủ công `git show` 141 commit:** `101` ứng viên từ khóa/Jira + `40` mẫu âm ngẫu nhiên (`seed=23110170`) trên `401` file Java đủ điều kiện (`67` file dương tính). Phát hiện `9` commit sửa lỗi hành vi bị từ khóa bỏ sót và loại hàng loạt commit chỉ sửa Javadoc/Checkstyle.

---

### Slide 11: Kết quả RQ1 & Benchmark Hiệu năng Lõi `NFR03` (`STT 30, 32`)
- **Nội dung trên Slide:**
  - **Trung bình vĩ mô RQ1 (`validCases = 4/5`, kiểm chứng tái lập SHA-256 `13/13`):**
    - `hotspot_score` dẫn đầu: **MAP = `0,3918`** (vs `complexity = 0,3862`, `frequency = 0,3290`), **R@10 = `0,3011`**, **P@20 = `0,3625`**, **R@20 = `0,5619`**.
    - Điểm sáng nổi bật: `commons-csv` đạt `AP = 0,7116`, `P@10 = 0,8000`, bắt `11/12` file lỗi trong Top 20 (`R@20 = 0,9167`); `commons-validator` đạt `P@5 = 0,4000` (gấp đôi baseline).
    - Giữ nguyên `javapoet` (`36/0`) trong bảng kiểm toán do `LINEAGE_AMBIGUOUS` ở v1.
  - **Benchmark lõi `NFR03` trên laptop Core i3-7020U @ 2.30 GHz (3 lượt/mốc):**
    - Mốc `10.000 commit` (`12.045` facts): lõi pipeline trung bình **`48,96 giây`** (`JGit 45,16s = 92,2%`, `Lizard 2,44s`, `Metric 0,71s`); CLI end-to-end **`73,25 giây` (`1,22 phút`)** — đạt xa chuẩn `<= 10 phút`.

---

### Slide 17: Đối chiếu Công cụ & *Threats to Validity* (`STT 35`)
- **Nội dung trên Slide:**
  - **Giá trị khác biệt của sản phẩm:** Mở mã nguồn, giữ chuỗi snapshot bất biến + chặn báo động giả khi thiếu độ phủ (`coverageComplete = false`) + AI Review có kiểm soát SHA-256 + `172` kiểm thử tự động (`89,07%` JaCoCo).
  - **4 nhóm *Threats to Validity*:**
    - *Internal:* Kiểm soát rò rỉ thời gian qua `validateHistoricalHead`; giảm nhiễu từ khóa bằng duyệt tay `141` commit; giải thích dịch phân vị ở `S3`.
    - *External:* Hiện đo trên dự án Java OSS; quy tắc merge `v1` bảo thủ làm giảm mẫu trên kho merge dày đặc (`javapoet`, `commons-cli`).
    - *Construct:* Hotspot và HHI là tín hiệu ưu tiên review, không khẳng định lỗi tuyệt đối hay đánh giá năng lực cá nhân.
    - *Conclusion:* Chênh lệch MAP vĩ mô (`0,3918` vs `0,3862`) nhỏ nên công bố minh bạch từng kho + phân tích độ nhạy; tách riêng phạm vi đo `NFR03` cục bộ với mạng GitHub.

---

### Slide 18: Kết luận, Hạn chế v1 & Hướng phát triển v2
- **Nội dung trên Slide:**
  - **Kết quả đạt được:** Hoàn thiện nền tảng web Java 21 / PostgreSQL 18 / React 19 + Docker Staging; `172` ca test (`89,07%` JaCoCo); hoàn tất thực nghiệm RQ1, RQ2, Benchmark `NFR03` (`48,96s`), độ trễ API `NFR01/NFR02` (`<= 46 ms`) và khảo sát người dùng RQ3.
  - **Hạn chế v1:** Mới hỗ trợ mã Java & repo công khai; nối phả hệ qua merge commit bảo thủ; chưa phân tích diff PR đang mở.
  - **Hướng phát triển v2:** Mở rộng `GitIngestion v2` theo nhánh cha chính để nâng độ phủ Hotspot lên `~85,37%`, hỗ trợ thêm TypeScript/Python/Go qua Lizard, và tích hợp bot nhận xét trực tiếp trên GitHub PR.
  - **Cảm ơn Hội đồng & Sẵn sàng trả lời câu hỏi!**

---

# PHẦN 2: ĐỀ CƯƠNG CHI TIẾT BUỔI 1 ÔN CODE CHÉO (TƯỞNG GIẢNG CHO TOẢN ~2 GIỜ)

Mục tiêu buổi ôn: Giúp Toản nắm chắc **100% mã nguồn gói `gitanalyzer` và lõi `aireview`** để giải thích trôi chảy khi Hội đồng chỉ bất kỳ file/hàm nào của Tưởng.

1. **Bước 1 — Mở `GitRepositoryClient.java` (15 phút):**
   - Chỉ hàm `clonePublic(...)`, `fetchPublic(...)` dùng bare repository (`setBare(true)`), thư mục cache theo `repoId`, kiểm tra deadline và khóa đúng `headSha` 40 ký tự hex (giải thích lý do `ADR-001` không dùng GitHub REST API hay partial clone).
2. **Bước 2 — Mở `GitIngestion.java` (40 phút — phần hay bị hỏi nhất):**
   - Dòng `26–29` (`Options.defaults`): chỉ rõ 4 ngưỡng giới hạn bảo vệ (`10.000` commit, `100.000` tree entries, `2 MiB` blob, `500.000` facts) và danh sách thư mục loại trừ (`vendor/`, `generated/`, `target/`, `build/`).
   - Dòng `71–88` (`extract`): chỉ kiểm tra `SHALLOW_HISTORY`, cấu hình `DiffFormatter` (`setDetectRenames(true)`, `renameScore = 60`, `renameLimit = 1000`), đọc đồ thị commit từ `headSha`.
   - Dòng `89–98`: giải thích thuật toán Kahn sắp thứ tự topo từ gốc đến ngọn và phá hòa bằng SHA (không phụ thuộc clock skew).
   - Dòng `103–110` (trọng tâm câu hỏi Hội đồng): chỉ đúng vòng lặp kiểm tra các nhánh cha tại `merge commit`; giải thích khi không đồng nhất `FileIdentity` thì gán `ambiguous = true` (`LINEAGE_AMBIGUOUS`).
   - Dòng `112–135`: giải thích commit 1 cha (`getParent(0)`), lọc cửa sổ `[start, end)` cho non-merge commit sinh `Fact` và giữ nguyên bản ghi lịch sử đổi tên qua `FileIdentity.id = "<firstSeenSha>:<initialPath>"`.
3. **Bước 3 — Mở `LizardComplexityAnalyzer.java` & `RepositoryMetricCalculator.java` (35 phút):**
   - `LizardComplexityAnalyzer.analyzeAtHead` (dòng `111`): đọc blob tại `headSha`, chạy `lizard -l java --xml` qua `ProcessBuilder` không shell, tắt XXE khi parse XML; dòng `51–52` (`noFunctions`) gán `maxCcn = null` và `NO_FUNCTIONS`.
   - `RepositoryMetricCalculator`: chỉ hàm `midrankPercentiles` (trường hợp $N=0$, $N=1 \to 0{,}5$, $N>1 \to (L + 0{,}5(E-1))/(N-1)$), `computeHotspotScores` ($\sqrt{P_{\text{change}} \times P_{\text{complexity}}}$ trên `BigDecimal`), `computeTemporalCoupling` (ngưỡng `shared >= 5`, `degree >= 0,50`, tùy chọn lọc `BR14 > 30` file), và `computeHhi` ($\sum p_i^2$, trả `null` khi `churn == 0`).
4. **Bước 4 — Mở `LlmReviewService.java` & `HttpLlmProviderAdapter.java` (30 phút):**
   - `LlmReviewService`: chỉ các hằng số dòng `29–34` (`32 KiB`, `16 KiB`, `8 KiB`, `4 KiB`, `200` dòng, `10` bằng chứng), regex `SECRET_PATTERN` (dòng `37–38`), `STRICT_JSON` chặn duplicate key / trailing token (dòng `64–67`), kiểm tra `ConsentReceipt` qua `computeContextHash` (dòng `333`) và kiểm tra `evidenceAllowlist` khi nhận JSON từ LLM.
   - `HttpLlmProviderAdapter`: chỉ `MAX_PROVIDER_TIMEOUT = 30s` (dòng `37`), cấu hình HTTP POST không tools, và vòng đọc `InputStream` theo khối `4 KiB` (dòng `211`) cắt luồng ngay khi vượt ngưỡng bộ nhớ.
