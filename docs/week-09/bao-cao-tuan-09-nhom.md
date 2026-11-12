# Báo cáo tuần 9 nhóm — Git Health Monitor

- **Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án dựa trên dữ liệu Git (Topic ID: `265`)
- **Thành viên:** Trần Văn Tưởng (`23110170` — Trưởng nhóm) & Trần Quang Toản (`23110158`)
- **Ngày tổng hợp:** 12/11/2026

---

## 1. Tổng quan các hạng mục mới hoàn thành & cập nhật trong Tuần 9 (`[MỚI TUẦN 9]`)

| STT | Mốc & Nhiệm vụ | Phụ trách | Trạng thái | Tóm tắt kết quả nổi bật trong Tuần 9 |
|---:|---|---|---|---|
| **20** | **M3 — Thiết kế thực nghiệm (temporal split & baseline)** | Cả 2 (Tưởng + Toản) | **[MỚI TUẦN 9 — HOÀN THÀNH]** | Chốt chính thức giao thức thực nghiệm v1 (`week09-rq1-approved-v1` & `rq2-protocol.md`): chia cắt thời gian 80/20 trên non-merge commit có gom nhóm timestamp trùng, cửa sổ quan sát 180 ngày tại `observationSha` không rò rỉ tương lai, phá hòa theo `fileId` (`lineageKey`), quy trình duyệt nhãn 2 giai đoạn + mẫu âm cố định (`seed=23110170`). |
| **29** | **M5 — Chọn tập dữ liệu thực nghiệm** | Cả 2 (Tưởng + Toản) | **[MỚI TUẦN 9 — HOÀN THÀNH]** | Đóng băng SHA trước khi đo trên tổng cộng **8 kho mã nguồn Java OSS công khai** (không trùng các kho tích hợp tuần 8 đối với RQ1): **5 kho chọn mù cho RQ1** (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`) và **3 kho pilot lịch sử cho RQ2** (`gs-rest-service`, `spring-petclinic`, `commons-cli`). |
| **30** | **M5 — Thực nghiệm RQ1: xếp hạng rủi ro** | Tưởng (`23110170`) | **[MỚI TUẦN 9 — HOÀN THÀNH]** | Xây dựng `run_rq1.py`, `summarize_rq1.py`, `test_rq1_runner.py`; duyệt thủ công **141 commit** (`101` ứng viên Jira/từ khóa + `40` mẫu âm `seed=23110170`) trên **401 file Java đủ điều kiện** ($P=67$ file dương tính); chứng minh `hotspot_score` đạt trung bình vĩ mô cao nhất về **MAP (`0.3918`)**, **Recall@10 (`0.3011`)**, **Precision@20 (`0.3625`)** và **Recall@20 (`0.5619`)** trên `validCases = 4/5`; tái lập khớp SHA-256 `13/13` file JSON. |
| **31** | **M5 — Thực nghiệm RQ2: theo dõi xu hướng qua snapshot** | Toản (`23110158`) | **[MỚI TUẦN 9 — ĐANG LÀM]** | Xây dựng CLI `HistoricalSnapshotExport.java` (4 JUnit test), `run_rq2.py`, `rq2_trends.py`, `summarize_rq2.py` (9 Python test); chạy đủ `9/9` snapshot lịch sử (2024–2026) và `6` cặp đối chiếu; phát hiện cả `6/6` cặp bị chặn (`BLOCKED`) do `INCOMPLETE_COVERAGE` từ quy tắc lineage qua merge ở `v1`; tái lập khớp SHA-256 `15/15` file JSON. Tiếp tục đánh giá tình huống tuần 10. |

---

## 2. Kết quả kiểm thử và tái lập toàn dự án đến hết Tuần 9

- **Backend JUnit Tests:** **119 ca kiểm thử duy nhất PASS (`0` lỗi)** — bổ sung 4 ca `HistoricalSnapshotExportTests` kiểm tra trích xuất độ phức tạp đúng cây lịch sử, chặn tổ tiên tương lai (`FUTURE_COMMIT_IN_HISTORICAL_TREE`), biên `cutoff` exclusive và giới hạn commit.
- **Experiment Python Unit Tests:** **22/22 ca kiểm thử PASS (`0` lỗi)** — gồm `8` ca `test_evaluate_rankings.py`, `9` ca `test_rq2_trends.py` và `5` ca `test_rq1_runner.py`.
- **Kiểm chứng tái lập độc lập (Reproducibility Verification):**
  - **RQ1:** `13/13` file JSON (`5` cặp snapshot `obs`/`pred` + dataset + evaluation + run-summary) khớp tuyệt đối mã băm SHA-256 ([rq1-replay-verification.json](evidence/rq1-replay-verification.json)).
  - **RQ2:** `15/15` file JSON (`9` snapshot + `6` comparison) khớp tuyệt đối mã băm SHA-256 ([replay-verification.json](evidence/replay-verification.json)).

---

## 3. Kế hoạch Tuần 10

1. Hoàn thiện đánh giá giải thích biến động cho **RQ2 (`STT 31`)** trên bộ tình huống có đối chiếu commit.
2. Thực hiện **Thực nghiệm hiệu năng (`STT 32`)**: đo thời gian phân tích theo từng giai đoạn (JGit ingestion, Lizard CCN, metric calculation, DB persistence) theo quy mô kho mã nguồn đến 10.000 commit (kiểm chứng ngưỡng `NFR03 <= 10 phút`).
3. Triển khai **Thực nghiệm người dùng RQ3 (`STT 33`)** trên 7 tác vụ chuẩn và thang đo SUS.
