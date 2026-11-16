# Tổng hợp kết quả Thực nghiệm, So sánh Công cụ và Threats to Validity (STT 35)

- **Phạm vi tổng hợp:** Kết quả đo đạc đến Tuần 10 (`RQ1` Tuần 9, `RQ2` Tuần 9-10, `Benchmark NFR03` Tuần 10, `JaCoCo` & `CI/CD` Tuần 8-10). Riêng `RQ3` (khảo sát người dùng thật) được tách riêng phần thu thập thực tế tiếp nối.

---

## 1. Tổng hợp định lượng các câu hỏi nghiên cứu và yêu cầu hiệu năng

| Hạng mục | Dữ liệu / Thiết kế thực nghiệm | Kết quả chính đo được | Ý nghĩa kỹ thuật & Giới hạn |
|---|---|---|---|
| **RQ1: Xếp hạng rủi ro bảo trì (`STT 30`)** | Chia cắt thời gian `80/20` trên 5 kho Java OSS chọn mù; duyệt thủ công `git show` của `141` commit (`101` ứng viên Jira/từ khóa + `40` mẫu âm `seed=23110170`); `401` file Java đủ điều kiện (`67` file dương tính). | Trên `validCases = 4/5`, `hotspot_score` đạt trung bình cao nhất về **MAP (`0.3918`)** (hơn `complexity = 0.3862` và `frequency = 0.3290`), **Recall@10 (`0.3011`)**, **Precision@20 (`0.3625`)** và **Recall@20 (`0.5619`)**. | Kết hợp tần suất và độ phức tạp ưu tiên tốt hơn từng chỉ số đơn lẻ trên các kho có lỗi tập trung ở lớp lõi (`commons-csv` AP=`0.7116`, `commons-validator` AP=`0.2631`). Kho `javapoet` bị loại do `LINEAGE_AMBIGUOUS = 36/36` từ merge PR. |
| **RQ2: Theo dõi xu hướng lịch sử (`STT 31`)** | 5 kịch bản Git có kiểm soát (`S1..S5`) qua `HistoricalSnapshotExport` + kiểm toán 9 snapshot OSS (`gs-rest-service`, `spring-petclinic`, `commons-cli`). | **5/5 kịch bản** đúng toàn bộ bất biến: bắt đúng tăng trưởng thực (`S1`), giữ `lineageKey` qua `git mv` (`S2`), giải thích đúng sụt giảm do trượt cửa sổ (`S3`), chặn sai số float tại biên `+0.15` bằng `Decimal` (`S4`), chặn cảnh báo khi `LINEAGE_AMBIGUOUS`/đổi cửa sổ (`S5`). **91/91** file chung OSS khớp truy vết commit. | Cơ chế bảo vệ `coverageComplete = false` hoạt động chính xác để không báo động giả, nhưng làm `6/6` cặp OSS thực tế bị `BLOCKED` do quy tắc merge `v1` còn bảo thủ. |
| **NFR03: Benchmark hiệu năng (`STT 32`)** | Đo 3 lượt/mốc trên kho `1.000`, `5.000` và `10.000` commit (tất cả nằm trong cửa sổ 180 ngày) trên CPU Intel i3-7020U @ 2.30 GHz, RAM 19.87 GiB. | Mốc `10.000` commit (`12.045` facts): trung bình **48.96 giây** (cold `59.96 s`, warm TB `43.46 s`), tiến trình CLI độc lập **73.25 giây** (`1.22 phút` vs ngưỡng `10 phút`). | Đạt xa tiêu chuẩn `NFR03 <= 600s`. Chặng JGit DAG/diff chiếm `92.2%` thời gian; chưa tính độ trễ `git clone` mạng lần đầu hoặc ghi PostgreSQL. |
| **NFR08 & Kiểm thử (`STT 28`)** | 49 unit test JUnit không cần DB (chạy với JaCoCo `0.8.12`), 70 `@SpringBootTest` trên PostgreSQL 18 ở CI (tổng 119 JUnit), 11 E2E Playwright, 27 unit test Python. | Trên 49 test lõi không cần DB: `gitanalyzer` đạt **87.92%** (`793/902` dòng, riêng `GitIngestion` **100%**), `aireview` đạt **70.39%** (`359/510`), tổng 74 lớp là **64.06%** (khi tính riêng tập chưa chạy DB trên máy local). | Đảm bảo chất lượng cao cho engine lõi; độ phủ toàn hệ thống bao gồm `monitoring` & DB controller được kiểm chứng đầy đủ trên pipeline CI dùng `postgres:18`. |

---

## 2. Đối chiếu Git Health Monitor với các công cụ hiện có

| Tiêu chí so sánh | Git Health Monitor (Nhóm) | SonarQube (Community) | CodeScene | Code Maat | CodeCharta |
|---|---|---|---|---|---|
| **Loại dữ liệu chính** | Lịch sử Git + Lizard CCN | Phân tích tĩnh AST/rules + lịch sử lần quét | Lịch sử Git + phức tạp + tổ chức nhóm | Log Git/SVN/Hg dạng text (CLI) | Đa nguồn (Git + Sonar/Lizard import) |
| **Chỉ số hỗ trợ** | `change_frequency`, `churn`, `max_ccn`, `hotspot_score`, `temporal_coupling`, `HHI` | Bugs, Vulnerabilities, Code Smells, Coverage, Duplication | Hotspot, Change coupling, Code Health, Knowledge map, Bus Factor | Churn, Coupling, Revisions, Entity ownership | Metric cây bản đồ 3D (treemap) |
| **Lưu Snapshot & Chặn so sánh sai** | Snapshot bất biến gắn SHA/configHash; chặn cảnh báo khi `coverageComplete = false` | Có lưu lịch sử chất lượng, Quality Gate | Có theo dõi xu hướng trên Cloud/Enterprise | Không (chạy lệnh xuất CSV từng lần) | Chỉ lưu file JSON tĩnh từng lần |
| **Trợ lý AI giải thích có kiểm soát** | Có: bắt buộc Preview, Consent SHA-256, quét secret, Schema + `evidenceAllowlist` | Có ở các bản trả phí/mở rộng | Có tính năng gợi ý refactor AI (bản thương mại) | Không | Không |
| **Mô hình vận hành & Chi phí** | Web mở (Docker Compose, PostgreSQL, không Redis), minh bạch công thức & mã nguồn | Open-source bản Community, nặng hơn về tài nguyên JVM/ES | Thương mại (trả phí theo repo/user) | Mã nguồn mở CLI, thiếu giao diện web/giám sát | Mã nguồn mở trực quan hóa 3D, thiếu scheduler/alert |
| **Điểm yếu so với đối thủ** | Hiện mới hỗ trợ Java + repo GitHub công khai; lineage qua merge ở `v1` còn bảo thủ | Không phát hiện hotspot theo lịch sử Git & temporal coupling bằng JGit | Rất hoàn thiện đa ngôn ngữ và IDE plugin | Nhẹ và linh hoạt đa VCS (Git/SVN) | Trực quan hóa bản đồ cây 3D tốt hơn |

---

## 3. Threats to Validity (Các mối đe dọa đến tính hợp lệ của thực nghiệm)

### 3.1. Internal Validity (Tính hợp lệ nội tại)
- **Nhiễu khi gán nhãn bug-fix trong RQ1:** Nếu chỉ dùng từ khóa `fix/bug` sẽ bị dương tính giả rất nặng từ commit sửa Javadoc/Checkstyle (thực tế đã loại `commons-validator` từ 44 xuống 18 file dương). Nhóm khắc phục bằng cách đọc thủ công `git show` toàn bộ `141` commit (101 ứng viên + 40 mẫu âm `seed=23110170`), tuy nhiên vẫn có nguy cơ bỏ sót commit sửa lỗi nằm ngoài tập mẫu âm hoặc hiểu nhầm ý định một diff phức tạp.
- **Hiện tượng rò rỉ dữ liệu thời gian (Data Leakage):** Đã kiểm soát chặt bằng `HistoricalSnapshotExport.validateHistoricalHead` từ chối mọi tổ tiên reachable có `committerTime >= cutoff` để chống lệch đồng hồ Git (clock skew).
- **Quần thể thay đổi khi tính phân vị:** Điểm `hotspot_score` dùng percentile trong tập hợp lệ của từng snapshot, nên khi một file biến mất hoặc có thêm file mới, điểm của file đứng yên cũng có thể dịch chuyển. Nhóm đã bổ sung phân tích tách bạch trường hợp này ở kịch bản `S3` của RQ2.

### 3.2. External Validity (Khả năng tổng quát hóa)
- **Giới hạn ngôn ngữ và hệ sinh thái:** Thực nghiệm RQ1 mới đo trên 5 dự án thư viện Java mã nguồn mở (`Apache Commons` và `javapoet`), còn benchmark hiệu năng 10.000 commit thực hiện trên kho tổng hợp có kiểm soát phân bố commit. Chưa thể khẳng định tỷ lệ tương tự cho dự án công nghiệp đa ngôn ngữ (Python/TypeScript/C++), kiến trúc microservice lớn hay dự án áp dụng chiến lược squash-merge/rebase khác biệt.
- **Ảnh hưởng của quy tắc merge `v1`:** Dự án dùng workflow GitHub Merge PR dày đặc như `javapoet` hoặc `commons-cli` bị đánh dấu `LINEAGE_AMBIGUOUS` ở nhiều file, làm giảm tập mẫu đánh giá hợp lệ (`validCases = 4/5` ở RQ1, `0/6` cặp đủ coverage đầy đủ ở pilot RQ2).

### 3.3. Construct Validity (Tính hợp lệ khái niệm)
- **Hotspot không đồng nghĩa với lỗi phần mềm:** Số lượng bug-fix commit trong cửa sổ dự đoán chỉ phản ánh các lỗi đã được phát hiện và ghi nhận trong lịch sử commit, không đồng nhất với toàn bộ khiếm khuyết tiềm ẩn chưa được phát hiện. Ngoài ra, một file thay đổi nhiều có thể vì tính năng mở rộng hợp lệ chứ không nhất thiết do code xấu.
- **Chỉ số HHI không đo trình độ lập trình viên:** Tập trung đóng góp (`HHI = 1.0`) chỉ phản ánh lịch sử chỉnh sửa trong cửa sổ phân tích, không đo lường mức độ hiểu biết mã nguồn hoặc năng lực cá nhân.

### 3.4. Conclusion Validity (Tính hợp lệ kết luận)
- **Kích thước mẫu so sánh giữa các dự án:** Với `validCases = 4` dự án hợp lệ ở RQ1 (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`), chênh lệch Macro-MAP giữa `hotspot` (`0.3918`) và `complexity` (`0.3862`) còn nhỏ và có phân hóa theo từng kho (thắng rõ ở `commons-csv`/`commons-validator`, thấp hơn nhẹ ở `commons-codec`/`commons-dbutils` khi file phức tạp tạm dừng chỉnh sửa trong 180 ngày). Nhóm trình bày minh bạch bảng phân tích từng dự án và phân tích độ nhạy (`src/main/java` và nhãn thô) thay vì khái quát quá mức.
- **Phạm vi benchmark và độ phủ:** Kết quả benchmark 73.25s tại 10.000 commit tách riêng lõi JGit/Lizard/Metric khỏi mạng GitHub/PostgreSQL; số liệu JaCoCo `64.06%` / `87.92%` trên `gitanalyzer` phản ánh lượt chạy cục bộ 49 unit test không cần DB và được nêu rõ ràng phạm vi.
