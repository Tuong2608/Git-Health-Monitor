# Tổng hợp kết quả Thực nghiệm, So sánh Công cụ và Threats to Validity (STT 35)

- **Phạm vi tổng hợp:** Kết quả đo đạc đến Tuần 10 (`RQ1` Tuần 9, `RQ2` Tuần 9-10, `Benchmark NFR03` Tuần 10, `JaCoCo` & `CI/CD` Tuần 8-10). Riêng `RQ3` (khảo sát người dùng thật) được tách riêng phần thu thập thực tế tiếp nối.

---

## 1. Tổng hợp định lượng các câu hỏi nghiên cứu và yêu cầu hiệu năng

| Hạng mục | Dữ liệu / Thiết kế thực nghiệm | Kết quả chính đo được | Ý nghĩa kỹ thuật & Giới hạn |
|---|---|---|---|
| **RQ1: Xếp hạng rủi ro bảo trì (`STT 30`)** | Chia cắt thời gian `80/20` trên 5 kho Java OSS chọn mù; duyệt thủ công `git show` của `141` commit (`101` ứng viên Jira/từ khóa + `40` mẫu âm `seed=23110170`); `401` file Java đủ điều kiện (`67` file dương tính). | Trên `validCases = 4/5`, `hotspot_score` đạt trung bình cao nhất về **MAP (`0.3918`)** (hơn `complexity = 0.3862` và `frequency = 0.3290`), **Recall@10 (`0.3011`)**, **Precision@20 (`0.3625`)** và **Recall@20 (`0.5619`)**. | Kết hợp tần suất và độ phức tạp ưu tiên tốt hơn từng chỉ số đơn lẻ trên các kho có lỗi tập trung ở lớp lõi (`commons-csv` AP=`0.7116`, `commons-validator` AP=`0.2631`). Kho `javapoet` bị loại do `LINEAGE_AMBIGUOUS = 36/36` từ merge PR. |
| **RQ2: Theo dõi xu hướng lịch sử (`STT 31`)** | 5 kịch bản Git có kiểm soát (`S1..S5`) qua `HistoricalSnapshotExport` + kiểm toán 9 snapshot OSS (`gs-rest-service`, `spring-petclinic`, `commons-cli`) và phân rã `369` lượt file Java (`audit_rq2_coverage.py`). | **5/5 kịch bản** đúng toàn bộ bất biến (`S1..S5`); **91/91** file chung OSS khớp truy vết commit; phân rã `369` thực thể OSS thành `155` `COMPLETE` (42.01%), `37` `NO_FUNCTIONS` phả hệ rõ, `160` `LINEAGE_AMBIGUOUS` có hàm và `17` `LINEAGE_AMBIGUOUS` kiêm `NO_FUNCTIONS`. | Cơ chế bảo vệ `coverageComplete = false` hoạt động chính xác để không báo động giả (`NO_FUNCTIONS` chỉ giảm mẫu số phân vị, còn `LINEAGE_AMBIGUOUS` mới kích hoạt `BLOCKED`); nếu gỡ merge `v1`, quần thể hotspot đạt `315/369` (`85.37%`). |
| **NFR03: Benchmark hiệu năng (`STT 32`)** | Đo 3 lượt/mốc trên kho `1.000`, `5.000` và `10.000` commit (cửa sổ 180 ngày) trên CPU Intel i3-7020U @ 2.30 GHz + đo lưu trữ/alert/comparison trên PostgreSQL 18.3 (`Week10PersistenceBenchmarkTests`). | Mốc `10.000` commit (`12.045` facts): lõi phân tích TB **48.96 giây** (CLI **73.25 giây**); chu trình ghi snapshot + đánh giá alert + truy vấn comparison trên PostgreSQL 18.3 chỉ thêm **59.90 ms (~0.06 giây)**. | Đạt xa tiêu chuẩn `NFR03 <= 600s`. Chặng JGit DAG/diff chiếm `92.2%` thời gian; chi phí giao dịch PostgreSQL ở quy mô 50 file / 150 dòng đóng góp là không đáng kể (`< 0.1s`). |
| **NFR08 & Kiểm thử (`STT 28`)** | Full `mvnw verify` với JaCoCo `0.8.12` trên PostgreSQL 18.3 (**122** JUnit test), đối chiếu với lượt chạy riêng 49 unit test không DB; **11** E2E Playwright; **37** unit test Python. | Trên toàn bộ **122 test có PostgreSQL 18.3**: tổng độ phủ dòng lệnh đạt **89.07%** (`1.630/1.830` dòng: `repository` **100%**, `api` **100%**, `monitoring` **94.60%**, `gitanalyzer` **88.12%**, `aireview` **85.88%**). Khi chạy riêng 49 test không DB đạt `64.06%` (`gitanalyzer` `87.92%`). | Cả tổng thể lẫn từng package nghiệp vụ cốt lõi đều vượt ngưỡng `NFR08 >= 70%` và tiêu chí Mức 5 của Rubric. |

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
- **Phạm vi benchmark và độ phủ:** Kết quả benchmark 73.25s tại 10.000 commit đo lõi JGit/Lizard/Metric trên kho cục bộ (tách khỏi độ trễ mạng `git clone` từ GitHub), kết hợp phép đo chu trình lưu trữ/cảnh báo/so sánh trên PostgreSQL 18.3 (`50.08..63.34 ms`); báo cáo độ phủ JaCoCo công bố minh bạch cả hai phạm vi: chạy riêng 49 unit test không DB (`64.06%`, `gitanalyzer` `87.92%`) và chạy đầy đủ 122 JUnit test cùng PostgreSQL 18.3 (`89.07%` toàn cục, mọi package nghiệp vụ cốt lõi đều đạt `85.88%..100%`).
