# BÁO CÁO TIẾN ĐỘ TUẦN 9 — NHÓM GIT HEALTH MONITOR (23110170 & 23110158)

> **ĐIỂM NỔI BẬT LÀM ĐƯỢC TRONG TUẦN 9 (HIGHLIGHT CẬP NHẬT MỚI):**
> 1. **[MỚI TUẦN 9 — HOÀN THÀNH] Chốt giao thức thực nghiệm chung (`STT 20`) và tập dữ liệu đóng băng SHA (`STT 29`):** Thống nhất giao thức `week09-rq1-approved-v1` (chia cắt thời gian **80/20** trên non-merge commit, cửa sổ quan sát **180 ngày** tại `observationSha` đã kiểm tra không chứa tổ tiên tương lai, phá hòa theo `fileId`/`lineageKey`). Đóng băng manifest trước khi đo trên **5 kho Java OSS chọn mù cho RQ1** (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`) và **3 kho lịch sử cho RQ2** (`gs-rest-service`, `spring-petclinic`, `commons-cli`).
> 2. **[MỚI TUẦN 9 — HOÀN THÀNH] Xây dựng CLI `HistoricalSnapshotExport.java` và hoàn tất thực nghiệm xếp hạng rủi ro RQ1 (`STT 30` — Trần Văn Tưởng `23110170`):**
>    - Trích xuất trực tiếp đặc trưng lịch sử bằng JGit + Lizard 1.24.3 + `RepositoryMetricCalculator` tại đúng cây mã nguồn quá khứ (`observationSha`).
>    - Rà soát thủ công thông điệp và diff của **141 commit** (`101` ứng viên Jira/từ khóa + `40` mẫu âm ngẫu nhiên với `seed=23110170`) trên **401 file Java đủ điều kiện** ($P=67$ file có sửa lỗi xác nhận), loại bỏ các commit chỉ sửa Javadoc/Checkstyle/bảng TLD và phát hiện **9 commit sửa lỗi hành vi bị từ khóa bỏ sót** nhờ mẫu âm.
>    - Kết quả trung bình vĩ mô trên `validCases = 4/5` kho (`javapoet` được giữ nguyên trong bảng kiểm toán với `eligibleFiles = 0` do quy tắc lineage qua merge ở `v1`): **`hotspot_score` đạt trung bình cao nhất về MAP (`0.3918` so với `complexity = 0.3862`, `frequency = 0.3290`), Recall@10 (`0.3011`), Precision@20 (`0.3625`) và Recall@20 (`0.5619`)**. Đặc biệt trên `commons-csv`, `hotspot_score` đạt `AP = 0.7116`, `P@10 = 0.8000`, `R@20 = 0.9167` (bắt trúng 11/12 file có lỗi tương lai trong Top 20) và trên `commons-validator` đạt `AP = 0.2631`, `P@5 = 0.4000` (gấp đôi cả 2 baseline). Kiểm chứng chạy lại độc lập khớp SHA-256 **13/13 file JSON**.
> 3. **[MỚI TUẦN 9 — ĐANG LÀM] Thực nghiệm theo dõi xu hướng qua các snapshot lịch sử RQ2 (`STT 31` — Trần Quang Toản `23110158`):**
>    - Xây dựng bộ công cụ `run_rq2.py`, `rq2_trends.py`, `summarize_rq2.py` (so sánh theo `lineageKey`, kiểm tra tương thích cấu hình/phiên bản/độ dài cửa sổ, dùng `Decimal` tại biên ngưỡng `+0.15`).
>    - Chạy thành công **9/9 snapshot lịch sử** (3 mốc `2024-01-01`, `2025-01-01`, `2026-01-01` × 3 dự án) và **6 cặp so sánh**; phát hiện cả `6/6` cặp bị chặn cảnh báo chính thức (`BLOCKED` do `coverageComplete = false`) vì thuật toán `GitIngestion` `v1` đánh dấu `LINEAGE_AMBIGUOUS` khi file đi qua merge commit không đồng nhất giữa các nhánh cha. Kiểm chứng chạy lại độc lập khớp SHA-256 **15/15 file JSON**.
> 4. **[MỚI TUẦN 9 — KIỂM THỬ]** Toàn dự án hiện đạt **119/119 test JUnit backend** (thêm 4 ca `HistoricalSnapshotExportTests`) và **22/22 test Python thực nghiệm** (`8` ca xếp hạng + `9` ca RQ2 + `5` ca RQ1).

---

## 1. Bảng kết quả định lượng thực nghiệm RQ1 (Chia cắt thời gian 80/20)

| Dự án | File Java / Đủ điều kiện ($N$) | File dương ($P$) | Phương pháp | AP | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 | Spearman $\rho$ |
|---|---:|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|
| **commons-csv** | 49 / **43** | **12** | `frequency` | 0.6431 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5000 | 0.8333 | 0.6241 |
| | | | `complexity` | 0.6808 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5500 | 0.9167 | 0.6067 |
| | | | **`hotspot`** | **0.7116** | **0.8000** | **0.3333** | **0.8000** | **0.6667** | **0.5500** | **0.9167** | **0.6282** |
| **commons-codec** | 156 / **126** | **33** | `frequency` | 0.3319 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | 0.4000 | 0.2424 | 0.1297 |
| | | | `complexity` | **0.4427** | **0.6000** | **0.0909** | **0.7000** | **0.2121** | 0.4500 | 0.2727 | **0.2371** |
| | | | **`hotspot`** | 0.4244 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | **0.5000** | **0.3030** | 0.2245 |
| **commons-dbutils** | 95 / **84** | **4** | `frequency` | 0.1458 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1000 | 0.5000 | 0.0848 |
| | | | `complexity` | **0.1821** | **0.2000** | **0.2500** | 0.1000 | 0.2500 | 0.1500 | 0.7500 | 0.2413 |
| | | | **`hotspot`** | 0.1681 | 0.0000 | 0.0000 | 0.1000 | 0.2500 | **0.1500** | **0.7500** | **0.2651** |
| **commons-validator** | 160 / **148** | **18** | `frequency` | 0.1951 | 0.2000 | 0.0556 | 0.3000 | 0.1667 | 0.1500 | 0.1667 | 0.1527 |
| | | | `complexity` | 0.2393 | 0.2000 | 0.0556 | 0.2000 | 0.1111 | 0.2000 | 0.2222 | 0.1569 |
| | | | **`hotspot`** | **0.2631** | **0.4000** | **0.1111** | **0.3000** | **0.1667** | **0.2500** | **0.2778** | **0.1749** |
| **javapoet** | 36 / **0** | — | *(Giữ kiểm toán)* | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| **Macro-Average** | **validCases = 4/5** | | `frequency` | 0.3290 | 0.4000 | 0.1749 | 0.3750 | 0.2803 | 0.2875 | 0.4356 | — |
| | | | `complexity` | 0.3862 | **0.4500** | **0.1824** | **0.4250** | 0.2891 | 0.3375 | 0.5404 | — |
| | | | **`hotspot`** | **0.3918** | 0.4000 | 0.1263 | 0.4000 | **0.3011** | **0.3625** | **0.5619** | — |

---

## 2. Minh chứng trên GitHub và Kế hoạch Tuần 10

- **Link Pull Request Tuần 9:**
  - PR #11 (Toản — RQ2 & `HistoricalSnapshotExport`): `https://github.com/Tuong2608/Git-Health-Monitor/pull/11` (Đã merge)
  - PR #12 (Tưởng — RQ1, chốt giao thức & tổng hợp Tuần 9): `https://github.com/Tuong2608/Git-Health-Monitor/pull/12`
- **Tài liệu & dữ liệu kiểm chứng trong repo:**
  - Giao thức & kết quả RQ1: `docs/week-09/rq1-protocol.md`, `docs/week-09/rq1-commit-reviews.json`, `docs/week-09/evidence/rq1/results.md`
  - Giao thức & kết quả RQ2: `docs/week-09/rq2-protocol.md`, `docs/week-09/evidence/pilot/results.md`
- **Ghi chú sử dụng công cụ hỗ trợ (theo đúng góp ý của GVHD):**
  - Nhóm chỉ dùng AI ở mức hỗ trợ tra cứu cú pháp script CLI thực nghiệm và kiểm tra biên unit test. Toàn bộ quy trình chọn kho mã nguồn, kiểm tra diff của `141` commit ứng viên/mẫu âm để gán nhãn bug-fix, chạy JGit/Lizard trích xuất số liệu, chẩn đoán nguyên nhân `LINEAGE_AMBIGUOUS` tại `GitIngestion.java` (dòng 103–110) và viết phân tích kết quả đều do hai thành viên trực tiếp thực hiện và đối chiếu mã băm SHA-256.
- **Kế hoạch Tuần 10:** Hoàn thành đánh giá tình huống RQ2 (`STT 31`), đo benchmark hiệu năng theo quy mô commit đến 10.000 commit (`STT 32`) và thực hiện khảo sát người dùng thực tế RQ3 (`STT 33`).
