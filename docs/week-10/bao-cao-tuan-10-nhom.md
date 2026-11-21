# Báo cáo tuần 10 nhóm — Git Health Monitor

- **Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git (Topic ID: `265`)
- **Thành viên:** Trần Văn Tưởng (`23110170` — Trưởng nhóm) & Trần Quang Toản (`23110158`)
- **Ngày tổng hợp:** 16–18/11/2026

---

## 1. Tổng quan các hạng mục hoàn thành & cập nhật trong Tuần 10 (`[MỚI TUẦN 10]`)

Tổng tiến độ đến hết Tuần 10: **29 Hoàn thành / 4 Đang làm / 6 Chưa bắt đầu** (hoàn thành thêm 7 đầu việc: `STT 9, 10, 12, 14, 31, 32, 35`).

| STT | Mốc & Nhiệm vụ | Phụ trách | Trạng thái | Tóm tắt kết quả nổi bật trong Tuần 10 |
|---:|---|---|---|---|
| **9, 10, 12, 14** | **M2 — Khóa đặc tả UC, BR/NFR, API Contract/ADR & AI Review Scope** | Cả 2 (Tưởng + Toản) | **[MỚI TUẦN 10 — HOÀN THÀNH]** | Khóa toàn bộ đặc tả `UC01..UC11`, `BR01..BR18`, `NFR01..NFR08` (kèm số đo thực tế `NFR03` và `NFR08`), bộ API Contract, 2 ADR (`001-full-clone`, `002-no-redis`) và phạm vi AI Review có kiểm soát (`ConsentReceipt` SHA-256). |
| **31** | **M5 — Thực nghiệm RQ2: theo dõi xu hướng qua snapshot** | Toản (`23110158`) chủ trì, Tưởng phối hợp | **[MỚI TUẦN 10 — HOÀN THÀNH]** | Xây dựng `evaluate_rq2_scenarios.py`, `audit_rq2_coverage.py` và `Week10PersistenceBenchmarkTests.java`: vượt qua **5/5 kịch bản tiến hóa chuẩn (`S1..S5`)**, xác nhận **91/91** file chung trên 9 snapshot OSS khớp 100% commit bằng chứng, và phân rã **369 thực thể file Java** thành `155` `COMPLETE` (`42.01%`), `37` `NO_FUNCTIONS` phả hệ rõ (`10.03%`) và `177` `LINEAGE_AMBIGUOUS` (`47.97%`). |
| **32** | **M5 — Thực nghiệm hiệu năng (`NFR03`)** | Toản (`23110158`) chủ trì, Tưởng phối hợp | **[MỚI TUẦN 10 — HOÀN THÀNH]** | Đo 3 lượt/mốc ở quy mô `1.000 / 5.000 / 10.000` commit (`run_benchmark.py`) kết hợp đo lưu trữ/cảnh báo/so sánh trên PostgreSQL 18.3 (`Week10PersistenceBenchmarkTests`): ở mốc **10.000 commit** (`12.045` facts), lõi phân tích chạy trung bình **48.96 giây** (CLI **73.25 giây**), chu trình giao dịch PostgreSQL chỉ tốn thêm **59.90 ms (`~0.06 giây`)**, đạt xa ngưỡng `NFR03 <= 600 giây`. |
| **35** | **M5 — Tổng hợp kết quả, so sánh công cụ & Threats to Validity** | Cả 2 (Tưởng + Toản) | **[MỚI TUẦN 10 — HOÀN THÀNH]** | Tổng hợp định lượng `RQ1`, `RQ2`, `Benchmark NFR03` và `JaCoCo NFR08`; lập bảng đối chiếu với SonarQube, CodeScene, Code Maat, CodeCharta và phân tích 4 nhóm *Threats to Validity* (Internal, External, Construct, Conclusion). |
| **33** | **M5 — Thực nghiệm người dùng (RQ3)** | Cả 2 (Tưởng + Toản) | **Chưa bắt đầu** *(Đã chuẩn bị xong bộ đề & công cụ)* | Đã hoàn thiện kịch bản 7 tác vụ chuẩn (`T1..T7`), bảng hỏi SUS 10 câu (Brooke, 1996), phiếu khảo sát gửi người tham gia (`mau-khao-sat-rq3-gui-nguoi-tham-gia.txt`) và công cụ chấm điểm `evaluate_rq3_sus.py`. Giữ trạng thái *Chưa bắt đầu* cho tới khi thu thập xong kết quả từ người dùng thật ở Tuần 11. |

---

## 2. Kết quả kiểm thử và độ phủ toàn dự án đến hết Tuần 10

- **Backend JUnit Tests (với PostgreSQL 18.3 & JaCoCo `0.8.12`):** **122/122 ca kiểm thử duy nhất PASS (`0` lỗi, `0` skip)** — bao gồm 3 ca mới trong `Week10PersistenceBenchmarkTests`.
  - **Độ phủ dòng lệnh toàn hệ thống (Full-stack với PostgreSQL 18.3):** **89.07%** (`1.630 / 1.830` dòng), độ phủ lệnh **89.52%**, độ phủ nhánh **67.80%**:
    - `com.example.demo.repository`: **100.00%** (`48/48` dòng)
    - `com.example.demo.api`: **100.00%** (`14/14` dòng)
    - `com.example.demo.monitoring`: **94.60%** (`333/352` dòng)
    - `com.example.demo.gitanalyzer`: **88.12%** (`794/901` dòng)
    - `com.example.demo.aireview`: **85.88%** (`438/510` dòng)
  - **Độ phủ khi chạy riêng 49 unit test không cần PostgreSQL:** **64.06%** toàn cục (`gitanalyzer` đạt **87.92%**, `aireview` đạt **70.39%**).
- **Experiment Python Unit Tests:** **37/37 ca kiểm thử PASS (`0` lỗi)** — gồm `8` ca `test_evaluate_rankings.py`, `9` ca `test_rq2_trends.py`, `5` ca `test_rq1_runner.py`, `5` ca `test_rq2_scenarios.py`, `4` ca `test_rq3_sus.py` và `6` ca `test_rq2_coverage_audit.py`.
- **Frontend Verification:** `npm run lint` và `npm run build` PASS; `9/9` ca Playwright UI E2E (chế độ mock) PASS tại local (`11/11` khi bật live backend).

---

## 3. Kế hoạch Tuần 11

1. Tổ chức khảo sát thực tế **RQ3 (`STT 33`)** trên tối thiểu 5–9 người dùng thuộc đúng nhóm đối tượng (phấn đấu >= 10 người) với 7 tác vụ chuẩn và bảng hỏi SUS; chấm điểm bằng `evaluate_rq3_sus.py`.
2. Thực hiện tối thiểu 1 vòng **Cải tiến giao diện sau phản hồi người dùng (`STT 34`)**.
3. Biên soạn hoàn chỉnh **Chương 3, Chương 4 và Kết luận, Tài liệu tham khảo, Phụ lục (`STT 36`, `STT 37`)** của cuốn Tiểu luận chuyên ngành.
