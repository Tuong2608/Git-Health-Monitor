# Kiểm chứng tuần 10

Ngày thực hiện: 18/11/2026. Môi trường kiểm chứng của Toản: Windows 11, JDK 21 (`ms-21.0.10`), PostgreSQL 18.3 local port `55432` (database thử nghiệm riêng `ghm_week10_test`), Python 3.12.14, Lizard 1.24.3, Node 24.11.1, Playwright Chromium 153.

---

## 1. Bảng tổng hợp kết quả kiểm thử và đo đạc

| Hạng mục kiểm chứng | Kết quả đạt được | Nguồn minh chứng |
|---|---|---|
| **Backend full `mvnw clean verify` (với PostgreSQL 18.3)** | **122 / 122 ca PASS**, `0` lỗi, `0` skip (gồm 3 ca mới trong `Week10PersistenceBenchmarkTests`) | [backend-tests.json](backend-tests.json) |
| **Độ phủ JaCoCo `0.8.12` (Full-stack 122 JUnit có PostgreSQL 18.3)** | Line coverage **89.07%** (`1.630/1.830`), Instruction **89.52%**, Branch **67.80%** (`repository` 100%, `api` 100%, `monitoring` 94.60%, `gitanalyzer` 88.12%, `aireview` 85.88%) | [jacoco-fullstack-coverage.json](jacoco-fullstack-coverage.json) |
| **Độ phủ JaCoCo `0.8.12` (49 unit test không cần DB)** | Line coverage **64.06%** toàn cục (`gitanalyzer` **87.92%**, `aireview` **70.39%`) | [jacoco-coverage-summary.json](jacoco-coverage-summary.json) |
| **Python Experiment Unit Tests** | **37 / 37 ca PASS**, `0` lỗi (`8` ranking + `9` RQ2 trends + `5` RQ1 runner + `5` RQ2 scenarios + `4` RQ3 SUS + `6` RQ2 coverage audit) | `python -m unittest discover -s tools/experiments -v` |
| **Kịch bản tiến hóa RQ2 (`S1..S5`) & Kiểm toán 9 snapshot OSS** | **5 / 5 kịch bản PASS**; **91 / 91** file chung OSS khớp commit bằng chứng; **21 / 21** snapshot (`9` OSS + `12` kịch bản) khớp phân rã `COMPLETE` / `NO_FUNCTIONS` / `LINEAGE_AMBIGUOUS` | [rq2-scenarios-summary.json](rq2-scenarios/rq2-scenarios-summary.json), [rq2-coverage-audit.json](rq2-coverage-audit.json) |
| **Benchmark hiệu năng lõi (`1k / 5k / 10k` commit)** | Mốc `10.000` commit (`12.045` facts): TB **48.96 s** (cold `59.96 s`, warm TB `43.46 s`), CLI **73.25 s** (`<= 600 s` của `NFR03`) | [benchmark-results.json](benchmark-results.json) |
| **Benchmark lưu trữ & cảnh báo PostgreSQL 18.3 (`1k / 5k / 10k`)** | Tổng chu trình `publish + evaluateNext + comparison`: `1k` = **50.08 ms**, `5k` = **63.34 ms**, `10k` = **59.90 ms** (`55` cảnh báo) | [postgres-persistence-benchmark.json](postgres-persistence-benchmark.json) |
| **Frontend Lint, Build & Playwright E2E** | `npm run lint` PASS; `npm run build` PASS; Playwright E2E **9 passed, 2 skipped** (2 ca live skip khi không bật cờ `GHM_LIVE_BACKEND=1`) | `frontend/` |

---

## 2. Chi tiết các bất biến được kiểm chứng mới trong Tuần 10

1. **Đồng nhất giữa công cụ thực nghiệm Python và Backend PostgreSQL:**
   - Trong `rq2_trends.py` và `evaluate_rq2_scenarios.py` (kịch bản `S4`), phép trừ `0.55 - 0.40 = 0.15` dùng `Decimal` để không báo cảnh báo giả tại đúng biên ngưỡng `+0.15`. Trong `Week10PersistenceBenchmarkTests.hotspotIncreaseAlertRespectsExactBigDecimalBoundaryInPostgres`, câu truy vấn SQL `n.hotspot_score - o.hotspot_score > threshold` trên kiểu `NUMERIC` của PostgreSQL 18.3 cho kết quả giống hệt (`0` cảnh báo ở `0.40 -> 0.55`, `1` cảnh báo ở `0.55 -> 0.775`).
   - Trong kịch bản `S5`, khi snapshot có `coverageComplete = false`, cả `rq2_trends.py` lẫn `AlertService.evaluateNext()` / `SnapshotQueries.comparison()` trên PostgreSQL 18.3 đều chặn phát sinh cảnh báo giả và ghi nhận `INCOMPLETE_COVERAGE`.
2. **Phân rã `NO_FUNCTIONS` và `LINEAGE_AMBIGUOUS`:**
   - Kiểm chứng tự động trên `369` thực thể file Java của 9 snapshot OSS xác nhận không có bất kỳ trường hợp mất điểm nào nằm ngoài hai nguyên nhân đã định nghĩa (`otherUnmeasuredFiles = 0`), và `summary.noFunctionFiles` (`54`) bằng tổng của `unambiguousNoFunctionFiles` (`37`) cộng `lineageAmbiguousAndNoFunctions` (`17`).
3. **Giới hạn trung thực:**
   - Chưa có dữ liệu khảo sát người dùng thật cho `RQ3` (`STT 33`); chỉ kiểm chứng công cụ chấm điểm `evaluate_rq3_sus.py` bằng unit test và chuẩn bị sẵn mẫu phiếu khảo sát.
   - Chưa có môi trường cloud staging mới trong Tuần 10; trạng thái GitHub Actions CI của đúng commit đóng gói được lưu trong `CI_STATUS.json` của gói bàn giao.
