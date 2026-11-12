# Báo cáo tuần 9 — Trần Văn Tưởng

- **MSSV:** `23110170` (Trưởng nhóm)
- **Dự án:** Git Health Monitor
- **Ngày thực hiện:** 12/11/2026
- **Nhánh làm việc:** `codex/tuong-week9` (sau khi review và gộp PR #11 `feat/toan-week-9` của Toản vào `main`)

---

## 1. Phạm vi công việc tuần 9 theo phân công

- **STT 20 (`M3 — Thiết kế thực nghiệm`, Cả 2: Tưởng + Toản — `[MỚI TUẦN 9 — HOÀN THÀNH]`):** Thống nhất và khóa chính thức giao thức thực nghiệm v1 (`week09-rq1-approved-v1`) trước khi chạy đánh giá trên dữ liệu thật: chia cắt thời gian 80/20 trên non-merge commit có gom nhóm timestamp trùng, cửa sổ quan sát 180 ngày tại `observationSha` không rò rỉ tương lai, phá hòa theo `fileId` (`lineageKey` tăng dần), bộ chỉ số `Precision@K`, `Recall@K` ($K \in \{5, 10, 20\}$), `AP`, `MAP` và tương quan hạng `Spearman rho`.
- **STT 29 (`M5 — Chọn tập dữ liệu thực nghiệm`, Cả 2: Tưởng + Toản — `[MỚI TUẦN 9 — HOÀN THÀNH]`):** Phối hợp cùng bộ 3 kho RQ2 pilot của Toản (`gs-rest-service`, `spring-petclinic`, `commons-cli`) và chốt riêng bộ **5 kho Java OSS chọn mù cho RQ1** (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`) chưa từng tiếp xúc ở tuần 8; đóng băng SHA trong `rq1-frozen-manifest.json` trước khi đo.
- **STT 30 (`M5 — Thực nghiệm RQ1: xếp hạng rủi ro`, Tưởng phụ trách — `[MỚI TUẦN 9 — HOÀN THÀNH]`):** Xây dựng bộ công cụ `tools/experiments/run_rq1.py`, `summarize_rq1.py` và `test_rq1_runner.py`; rà soát thủ công `141` commit (`101` ứng viên Jira/từ khóa + `40` mẫu âm với `seed=23110170`) trong `rq1-commit-reviews.json`; chạy thực nghiệm RQ1 và kiểm chứng tái lập `13/13` file JSON khớp SHA-256 (`rq1-replay-verification.json`).

---

## 2. Sản phẩm và minh chứng kỹ thuật tuần 9

| Hạng mục | Nội dung thực hiện | Minh chứng trong repository |
|---|---|---|
| **Chốt giao thức & tập dữ liệu RQ1** | Khóa quy tắc split 80/20, tiêu chí chọn 5 dự án Java OSS độc lập, giữ nguyên dự án `javapoet` (`eligibleFiles = 0` do quy tắc merge `v1`) trong bảng kiểm toán | [rq1-protocol.md](rq1-protocol.md), [rq1-dataset-proposal.json](rq1-dataset-proposal.json), [rq1-frozen-manifest.json](evidence/rq1-frozen-manifest.json) |
| **Bộ chạy & tổng hợp RQ1** | Cài đặt `run_rq1.py` (tách `freeze` và `run`, gọi `HistoricalSnapshotExport`, kiểm tra `rq1-commit-reviews.json`, chạy `evaluate_rankings.py`, tính `Spearman rho` và phân tích độ nhạy) + `summarize_rq1.py` | [run_rq1.py](../../tools/experiments/run_rq1.py), [summarize_rq1.py](../../tools/experiments/summarize_rq1.py), [test_rq1_runner.py](../../tools/experiments/test_rq1_runner.py) |
| **Rà soát nhãn bug-fix (`141` commit)** | Kiểm tra thông điệp & diff của `101` ứng viên từ khóa/Jira và `40` mẫu âm (`seed=23110170`); loại bỏ các commit chỉ sửa Javadoc/Checkstyle/IANA TLD; phát hiện `9` commit sửa lỗi thực tế bị từ khóa bỏ sót nhờ mẫu âm | [rq1-commit-reviews.json](rq1-commit-reviews.json), [rq1-reviewed-dataset.json](evidence/rq1/rq1-reviewed-dataset.json) |
| **Kết quả & Tái lập RQ1** | Xuất đủ `10` snapshot (`obs`/`pred` cho 5 kho), bảng tổng hợp Markdown & CSV, đối chiếu chạy lại độc lập khớp SHA-256 `13/13` file JSON; đạt `22/22` unit test Python (`8` cũ + `9` RQ2 + `5` RQ1) | [results.md](evidence/rq1/results.md), [rq1-summary.csv](evidence/rq1/rq1-summary.csv), [rq1-replay-verification.json](evidence/rq1-replay-verification.json) |

---

## 3. Kết quả định lượng RQ1 và phân tích học thuật

1. **Thống kê quần thể và kiểm toán loại trừ (`5` dự án đã đóng băng):**
   - Bốn dự án Apache Commons có tỷ lệ file Java hợp lệ cao ở mốc cắt 80/20: `commons-csv` (`43/49` file, $P=12$), `commons-codec` (`126/156` file, $P=33$), `commons-dbutils` (`84/95` file, $P=4$), `commons-validator` (`148/160` file, $P=18$) — tổng cộng **401 file Java đủ điều kiện** và **67 file dương tính**.
   - Dự án thứ 5 (`javapoet`, `36` file Java) có `0` file hợp lệ do cả `36` file đều đi qua các merge commit PR trên GitHub nên bị quy tắc `c.getParentCount() > 1` trong `GitIngestion.java` (dòng 103–110) đánh dấu `LINEAGE_AMBIGUOUS`. Dự án được giữ nguyên với trạng thái `EXCLUDED_ZERO_ELIGIBLE_FILES`, công bố rõ `validCases = 4 / 5`.
2. **Hiệu quả của quy trình rà soát 2 giai đoạn + mẫu âm (`seed=23110170`):**
   - Nếu chỉ dùng từ khóa thô `fix/bug`, các commit dọn dẹp tài liệu/định dạng (như `26c1193859` *"Fix grammar issues in the JavaDoc comments"* chạm 30 file ở `commons-validator`, hay `6a11b896aa` *"Enable Checkstyle for test sources and fix issues"* chạm 11 file ở `commons-csv`) làm số file dương tính giả tăng vọt (`commons-validator` từ `18` lên `44` file; `commons-csv` từ `12` lên `21` file).
   - Ngược lại, mẫu âm ngẫu nhiên (`10` commit/dự án, `seed=23110170`) đã bắt được **9 commit sửa lỗi hành vi thực sự nhưng không chứa từ `fix`/`bug`** (tiêu biểu: `27ca6db945` và `8c9b5b8266` sửa lỗi `ResultSetIterator.next()/hasNext()` trong `commons-dbutils`; `af20981bee` sửa lỗi chấp nhận nhóm hex có dấu trong IPv6 ở `commons-validator`).
3. **So sánh `hotspot_score` với 2 baseline (`frequency-only`, `complexity-only`) trên 4 dự án hợp lệ (`validCases = 4`):**
   - **Trung bình vĩ mô (Macro-Average):**
     - **MAP:** `hotspot` đạt **0.3918**, cao hơn `complexity-only` (**0.3862**) và `frequency-only` (**0.3290**).
     - **Top-10 (`P@10` / `R@10`):** `hotspot` dẫn đầu về **`R@10 = 0.3011`** (so với `complexity = 0.2891`, `frequency = 0.2803`) và đạt `P@10 = 0.4000` (cao hơn `frequency = 0.3750`).
     - **Top-20 (`P@20` / `R@20`):** `hotspot` dẫn đầu cả **`P@20 = 0.3625`** (so với `complexity = 0.3375`, `frequency = 0.2875`) lẫn **`R@20 = 0.5619`** (thu hồi 56.19% file có sửa lỗi trong Top 20, cao hơn `complexity = 0.5404` và `frequency = 0.4356`).
   - **Phân tích theo từng dự án:**
     - Trên các dự án mà lỗi phát sinh ở các lớp lõi vừa phức tạp vừa đang được chỉnh sửa tích cực (`commons-csv` và `commons-validator`), `hotspot` vượt cả 2 baseline (`commons-csv`: `AP = 0.7116`, `P@10 = 0.8000`, `R@20 = 0.9167`, `Spearman rho = 0.6282`; `commons-validator`: `AP = 0.2631`, `P@5 = 0.4000` gấp đôi cả hai baseline `0.2000`, `Spearman rho = 0.1749`).
     - Trên `commons-codec` (`AP = 0.4244` so với `complexity = 0.4427`, nhưng `hotspot` thắng ở `P@20 = 0.5000` và `R@20 = 0.3030`) và `commons-dbutils` (`AP = 0.1681` so với `complexity = 0.1821`, nhưng `hotspot` dẫn đầu `Spearman rho = 0.2651`), một số lớp thuật toán/iterator phức tạp không có commit nào trong đúng 180 ngày trước mốc cắt (`frequency = 0`), khiến nhân tử phân vị `P_change` kéo điểm trung bình hình học `hotspot` ở Top 5 xuống thấp hơn một chút so với `complexity-only` nhưng vẫn cao hơn rõ rệt so với `frequency-only` (`0.3319` và `0.1458`).

---

## 4. Kế hoạch phối hợp tuần 10

- Phối hợp cùng Toản thực hiện **STT 31 (hoàn thiện đánh giá giải thích RQ2)**, **STT 32 (benchmark hiệu năng theo quy mô commit tối đa 10.000 commit)** và **STT 33 (thực nghiệm người dùng RQ3)**.
- Thiết kế bản vá có đánh số phiên bản (`v2`) cho quy tắc kế thừa `FileIdentity` qua merge commit một phía trong `GitIngestion.java` kèm bộ test fixture hồi quy để cải thiện tỷ lệ `coverageComplete` trên các dự án dùng luồng GitHub Merge PR.
