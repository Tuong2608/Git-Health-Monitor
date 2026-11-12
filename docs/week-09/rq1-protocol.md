# Giao thức thực nghiệm RQ1 và chốt thiết kế thực nghiệm chung — Tuần 9

- **Ngày chốt:** 12/11/2026
- **Người phụ trách chính RQ1 (`STT 30`):** Trần Văn Tưởng (`23110170`)
- **Phối hợp chốt thiết kế & dữ liệu (`STT 20`, `STT 29`):** Trần Văn Tưởng (`23110170`) + Trần Quang Toản (`23110158`)
- **Trạng thái giao thức:** `GROUP_APPROVED_BLIND_RQ1_V1` (`week09-rq1-approved-v1`)

---

## 1. Quyết định thống nhất cho STT 20 (Chốt thiết kế thực nghiệm)

Sau khi đối chiếu hai bản đề xuất tuần 5 ([docs/experiment-design.md](../experiment-design.md) và [docs/week-05/experiment-design.md](../week-05/experiment-design.md)) cùng công cụ kiểm chuẩn tuần 6 ([docs/week-06/experiment-protocol.md](../week-06/experiment-protocol.md)), nhóm chính thức khóa các tham số thiết kế thực nghiệm **v1** trước khi chạy đánh giá RQ1 trên kho mã nguồn thật:

| Hạng mục | Quy tắc đã chốt (`week09-rq1-approved-v1`) | Lý do kỹ thuật |
|---|---|---|
| **Chia cắt thời gian (Temporal Split)** | **80/20** trên tập non-merge commit sắp theo `committerTime` UTC; gom các commit trùng timestamp về cùng một phía của `cutoff` | Bảo đảm đủ lịch sử quan sát và đủ số lượng commit sửa lỗi ở phần dự đoán 20% cuối mà không cắt ngang chuỗi commit cùng giây |
| **Chọn `observationSha`** | Commit mới nhất trên chuỗi `first-parent` của `endSha` thỏa mãn `committerTime < cutoff` **và** mọi tổ tiên reachable đều `< cutoff` (`validateHistoricalHead`) | Chống rò rỉ tương lai qua nhánh phụ hoặc lệch đồng hồ (`FUTURE_COMMIT_IN_HISTORICAL_TREE`) |
| **Cửa sổ quan sát (`observation`)** | `[cutoff - 180 ngày, cutoff)` | Khớp tuyệt đối với cấu hình mặc định `180 ngày` trong [metric-spec-v1.md](../metric-spec-v1.md) và `RepositoryMetricCalculator` |
| **Cửa sổ dự đoán (`prediction`)** | `[cutoff, predictionEnd)` với `predictionEnd = max_committerTime(endSha) + 1s` | Bao phủ toàn bộ 20% lịch sử non-merge sau mốc cắt đến `endSha` đã ghim |
| **Tập file đánh giá (`Universe` $R$)** | Toàn bộ file `.java` tại `observationSha` có đủ `frequency` và `maxCcn` (`hotspot != null`); cả 3 phương pháp (`frequency`, `complexity`, `hotspot`) xếp hạng trên cùng tập $R$ | Bảo đảm so sánh công bằng; các file `LINEAGE_AMBIGUOUS` / `NO_FUNCTIONS` được ghi vào bảng loại trừ (`excludedByReason`), không ép về `0` |
| **Quy tắc phá hòa (`Tie-break`)** | `fileId` nguyên dương tăng dần gán theo thứ tự từ điển của `lineageKey` (`1..N`) | Ổn định qua phép đổi tên file và khớp với `tools/experiments/evaluate_rankings.py` |
| **Thước đo xếp hạng** | `Precision@K`, `Recall@K` với $K \in \{5, 10, 20\}$ ($k_{\text{actual}} = \min(K, N)$), `AP` toàn bảng xếp hạng, `MAP` trung bình vĩ mô trên các dự án hợp lệ (`validCases`), kèm hệ số tương quan hạng **Spearman $\rho$** | Đáp ứng đầy đủ yêu cầu của `STT 20`, `STT 30` và chuẩn đánh giá truy hồi thông tin (Stanford IR Book) |

---

## 2. Tập dữ liệu chọn mù cho RQ1 (`STT 29` & `STT 30`)

Trong nhánh RQ2 tuần 9 ([rq2-protocol.md](rq2-protocol.md)), Toản đã chạy pilot trên 3 kho (`gs-rest-service`, `spring-petclinic`, `commons-cli`) và ghi nhận hai kho Spring từng được dùng trong kiểm thử tích hợp tuần 8 nên **không còn là mẫu chọn mù cho RQ1**.

Để tránh thiên lệch chọn mẫu (selection bias), nhóm đăng ký trước **5 kho mã nguồn Java OSS độc lập** trong [rq1-dataset-proposal.json](rq1-dataset-proposal.json) trước khi chạy xếp hạng:

1. **`apache/commons-csv`** (Apache-2.0): Thư viện phân tích dữ liệu bảng CSV; có mã lỗi Jira `CSV-xxx` rõ ràng.
2. **`apache/commons-codec`** (Apache-2.0): Thư viện mã hóa Base32/Base64, băm và ngữ âm; nhiều lớp thuật toán có độ phức tạp vòng lặp cao (`CODEC-xxx`).
3. **`apache/commons-dbutils`** (Apache-2.0): Thư viện tiện ích JDBC; cấu trúc phân층 handler/wrapper rõ ràng (`DBUTILS-xxx`).
4. **`apache/commons-validator`** (Apache-2.0): Thư viện kiểm tra hợp lệ dữ liệu (IBAN, ISIN, Regex, Email, Domain, IPv6); có mã lỗi Jira `VALIDATOR-xxx`.
5. **`square/javapoet`** (Apache-2.0): Thư viện sinh mã nguồn Java dùng quy trình merge Pull Request trên GitHub.

**Quy tắc chống cherry-picking:** Cả 5 kho đều được đóng băng SHA trong [rq1-frozen-manifest.json](evidence/rq1-frozen-manifest.json) trước khi đo. Khi chạy `HistoricalSnapshotExport` phiên bản `v1`, `javapoet` có `36/36` file bị gắn cờ `LINEAGE_AMBIGUOUS` (nên `eligibleFiles = 0`). Nhóm **giữ nguyên `javapoet` trong manifest và bảng kết quả** với trạng thái `EXCLUDED_ZERO_ELIGIBLE_FILES`, công bố `validCases = 4 / 5` khi tính trung bình vĩ mô, tuyệt đối không xóa kho thất bại để làm đẹp tỷ lệ.

---

## 3. Quy trình gán nhãn bug-fix 2 giai đoạn và lấy mẫu âm (`seed = 23110170`)

1. **Giai đoạn 1 — Lọc ứng viên tự động (`issue-or-keyword-candidate`):** Quét toàn bộ commit trong cửa sổ dự đoán `[cutoff, predictionEnd)` có chạm tới tập file đủ điều kiện $R$ (ánh xạ theo `lineageKey`) và khớp biểu thức chính quy:
   `\b(?:fix|fixes|fixed|bug|bugs|defect|defects|npe|regression|incorrect|wrong|broken)\b|(?:CSV|CODEC|DBUTILS|VALIDATOR)-\d+`.
2. **Giai đoạn 2 — Lấy mẫu âm cố định (`negative-sample-seed-23110170`):** Lấy ngẫu nhiên có tái lập `10` commit không khớp từ khóa trên mỗi dự án với `seed = 23110170` để phát hiện các commit sửa lỗi hành vi nhưng không dùng từ `fix`/`bug` trong tiêu đề.
3. **Giai đoạn 3 — Rà soát thủ công và lưu bằng chứng ([rq1-commit-reviews.json](rq1-commit-reviews.json)):**
   - Tổng cộng **141 commit** (`101` ứng viên + `40` mẫu âm) trên 4 dự án hợp lệ được kiểm tra thông điệp và diff trước khi chạy hàm tính điểm xếp hạng.
   - **Loại bỏ (`rejected`)** các commit chỉ sửa Javadoc, chính tả, định dạng Checkstyle/PMD, cập nhật bảng tên miền ICANN hoặc thêm tính năng mới (ví dụ commit `26c1193859` *"Fix grammar issues in the JavaDoc comments (#308)"* chạm tới 30 file trong `commons-validator`, hay commit `6a11b896aa` *"Enable Checkstyle for test sources and fix issues"* chạm tới 11 file trong `commons-csv`).
   - **Xác nhận (`confirmed`)** các commit sửa lỗi hành vi thực tế, kể cả **9 commit sửa lỗi phát hiện được nhờ mẫu âm** (như `27ca6db945` và `8c9b5b8266` sửa lỗi vi phạm hợp đồng `Iterator.next()/hasNext()` trong `ResultSetIterator.java` của `commons-dbutils`, hay `af20981bee` sửa lỗi chấp nhận nhóm hex có dấu trong địa chỉ IPv6 của `commons-validator`).
4. **Phân tích độ nhạy (Sensitivity Analysis):** Báo cáo song song 3 cấu hình trong [rq1-evaluation-output.json](evidence/rq1/rq1-evaluation-output.json):
   - Cấu hình chính (`primaryEvaluation`): Toàn bộ file `.java` hợp lệ trong $R$ với nhãn đã duyệt thủ công.
   - Cấu hình chỉ tính file nguồn (`productionMainJavaOnly`): Chỉ giữ các lớp trong `src/main/java/`, loại các file kiểm thử `src/test/java/` được sửa kèm trong cùng commit fix bug.
   - Cấu hình từ khóa thô chưa lọc (`unfilteredKeywordHeuristic`): Minh họa mức độ nhiễu nếu tự động lấy mọi commit chứa từ `fix`/`bug` làm ground truth.

---

## 4. Chẩn đoán nguyên nhân gốc rễ của `LINEAGE_AMBIGUOUS` qua Merge Commit

Kết hợp phát hiện từ pilot RQ2 của Toản (`6/6` cặp bị chặn do `coverageComplete = false`) và kho `javapoet` trong RQ1 (`36/36` file `LINEAGE_AMBIGUOUS`), nhóm đã xác định chính xác nguyên nhân kỹ thuật tại `GitIngestion.java` (dòng 103–110):
- Khi duyệt qua một merge commit (`c.getParentCount() > 1`), thuật toán `v1` yêu cầu file phải tồn tại ở **tất cả** các nhánh cha và có cùng `FileIdentity`:
  `if (!missing && candidates.size() == 1) state.put(path, candidates.iterator().next()); else state.put(path, new FileIdentity(id(sha, path), true));`
- Hệ quả: (1) Mọi file mới được thêm trên nhánh tính năng rồi gộp vào nhánh chính qua `git merge --no-ff` đều có `missing = true` ở nhánh cha thứ nhất nên bị gán `ambiguous = true` và đổi `id` mới tại merge commit; (2) Khi có một nhánh khác tách ra trước merge commit đó và gộp lại sau, toàn bộ các file đã bị đổi `id` ở lần merge trước sẽ có `candidates.size() == 2` và lan truyền trạng thái `LINEAGE_AMBIGUOUS` ra toàn kho (như xảy ra ở `javapoet`).
- **Quyết định kỹ thuật:** Không sửa lén `GitIngestion` trong tuần 9 để bảo toàn khả năng tái lập `15/15` file SHA-256 của RQ2 và `13/13` file SHA-256 của RQ1 trên `metricVersion = v1`. Giải pháp phân biệt merge một phía (one-sided addition/unchanged ancestor) với xung đột đổi tên thực sự sẽ được đưa vào nhánh cải tiến có đánh số phiên bản riêng ở tuần 10–11.
