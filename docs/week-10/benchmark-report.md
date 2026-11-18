# Báo cáo Thực nghiệm Hiệu năng theo Quy mô Commit (STT 32 & NFR03)

- **Số liệu gốc:** [benchmark-results.json](evidence/benchmark-results.json) (lõi phân tích JGit/Lizard/Metric) và [postgres-persistence-benchmark.json](evidence/postgres-persistence-benchmark.json) (lưu trữ giao dịch, đánh giá cảnh báo & so sánh trên PostgreSQL 18.3)
- **Bộ đo tự động:** `tools/experiments/run_benchmark.py` và `backend/src/test/java/com/example/demo/monitoring/Week10PersistenceBenchmarkTests.java`
- **Mục tiêu nghiệm thu (`NFR03`):** Phân tích kho mã nguồn tối đa `10.000` commit trong thời gian `<= 10 phút` (`600.000 ms`).

---

## 1. Cấu hình môi trường đo thực tế

| Thông số | Giá trị đo được |
|---|---|
| Hệ điều hành | Windows 10 (`Windows-10-10.0.19045-SP0`) |
| CPU | Intel Core i3-7020U @ 2.30 GHz (4 luồng xử lý) |
| RAM | 19.87 GiB |
| Java Runtime | OpenJDK 21.0.8 (`JBR-21.0.8+9-1163.69-jcef`, `-Xmx1g`) |
| Git / JGit | Git `2.52.0.windows.1` / Eclipse JGit `7.8.0.202609011348-r` |
| Lizard | `1.24.3` |

**Phạm vi phép đo:** Đo riêng thời gian lõi phân tích (`JGit ingestion -> Lizard CCN -> Metric calculation -> JSON serialize`) và tổng thời gian chạy tiến trình CLI độc lập (`HistoricalSnapshotExport`) trên bare repository cục bộ (tách biệt khỏi độ trễ mạng Internet khi clone từ GitHub và chưa bao gồm thời gian JDBC INSERT PostgreSQL). Mỗi mốc quy mô chạy **3 lượt lặp** trong JVM (1 lượt cold + 2 lượt warm) cộng thêm 1 lượt CLI độc lập. Trong các bài test này, toàn bộ 1.000 / 5.000 / 10.000 commit đều nằm trọn trong cửa sổ 180 ngày (`selectedCommits == reachableCommits`) để kiểm tra trường hợp tải nặng nhất.

---

## 2. Kết quả đo định lượng theo 3 mốc quy mô

| Mốc quy mô | Số file Java | Số bản ghi facts | Lượt 1 Cold (s) | TB Warm (s) | TB 3 lượt (s) | Tiến trình CLI đầu cuối (s) | Đạt `NFR03 <= 600s` |
|---|---:|---:|---:|---:|---:|---:|---|
| **1.000 commit** | 25 | 1.222 | 32.53 s | 9.55 s | **17.21 s** | 19.27 s | **ĐẠT** (dư 96.8%) |
| **5.000 commit** | 40 | 6.036 | 36.74 s | 23.73 s | **28.07 s** | 41.25 s | **ĐẠT** (dư 93.1%) |
| **10.000 commit** | 50 | 12.045 | 59.96 s | 43.46 s | **48.96 s** | **73.25 s** | **ĐẠT** (dư 87.8%) |

### Phân rã thời gian từng chặng (Trung bình 3 lượt đo)

| Mốc quy mô | JGit Ingestion (ms) | Lizard CCN (ms) | Tính Metric (ms) | Xuất JSON (ms) | Tỷ trọng JGit / Tổng |
|---|---:|---:|---:|---:|---:|
| **1.000 commit** | 13,694.41 ms (13.69 s) | 2,886.21 ms (2.89 s) | 106.48 ms | 523.86 ms | 79.6% |
| **5.000 commit** | 25,325.12 ms (25.33 s) | 2,168.83 ms (2.17 s) | 353.25 ms | 222.39 ms | 90.2% |
| **10.000 commit** | 45,155.57 ms (45.16 s) | 2,439.16 ms (2.44 s) | 708.23 ms | 656.77 ms | **92.2%** |

### 2.2. Đo thời gian lưu trữ giao dịch, đánh giá Cảnh báo và truy vấn So sánh trên PostgreSQL 18.3

Để bổ sung phần thời gian lưu trữ cơ sở dữ liệu quan hệ (`SnapshotPublisher.publish`, `AlertService.evaluateNext`, `SnapshotQueries.comparison`) của đầu việc `STT 32`, bộ kiểm thử `Week10PersistenceBenchmarkTests.publishAlertAndComparisonScaleWithinNfr03Budget` đã đo trực tiếp trên PostgreSQL 18.3 ([postgres-persistence-benchmark.json](evidence/postgres-persistence-benchmark.json)) ứng với đúng 3 mốc quy mô:

| Mốc quy mô | File Java (`file_metric`) | Dòng `file_contribution` / snapshot | Cặp `temporal_coupling` / snapshot | Ghi Snapshot đích (`publishTargetMs`) | Đánh giá Alert (`evalTargetMs`) | Truy vấn Comparison (`compareQueryMs`) | Tổng chi phí chu trình DB (`totalTargetDbOverheadMs`) |
|---|---:|---:|---:|---:|---:|---:|---:|
| **1.000 commit** | 25 | 75 | 12 | 32.11 ms | 9.64 ms | 8.32 ms | **50.08 ms (0.05 s)** |
| **5.000 commit** | 40 | 120 | 20 | 45.49 ms | 10.59 ms | 7.26 ms | **63.34 ms (0.06 s)** |
| **10.000 commit** | 50 | 150 | 25 | 45.59 ms | 10.35 ms | 3.96 ms | **59.90 ms (0.06 s)** |

Khi cộng cả chu trình giao dịch PostgreSQL 18.3 (`~0.06 s`) vào thời gian lõi phân tích ở mốc 10.000 commit (`48.96 s` trung bình JVM / `73.25 s` CLI), tổng thời gian xử lý đầu-cuối vẫn đạt **`~49.02 s` (`73.31 s` CLI)**, nằm sâu trong ngưỡng `NFR03 <= 600 s`.

---

## 3. Phân tích nút thắt cổ chai và kết luận `NFR03`

1. **Vượt xa ngưỡng mục tiêu ngay trên CPU phổ thông:** Ngay cả trên máy laptop dùng CPU Intel Core i3-7020U @ 2.30 GHz với kịch bản stress toàn bộ 10.000 commit đều nằm trong cửa sổ 180 ngày (`12.045` facts), lõi phân tích chỉ mất trung bình **48.96 giây** (tối đa **59.96 giây** ở lượt cold JVM) và tiến trình CLI độc lập mất **73.25 giây (1.22 phút)**, nhanh gấp **8.19 lần** so với giới hạn 10 phút (600 giây) của `NFR03`.
2. **Chi phí JGit chiếm trên 90% tổng thời gian ở quy mô lớn:** Khi tăng từ 1.000 lên 10.000 commit, thời gian `GitIngestion` tăng gần tuyến tính từ `13.69 s` lên `45.16 s` (chiếm `92.2%` tổng thời gian), chủ yếu do thuật toán duyệt DAG theo thứ tự topo phải quét `TreeWalk` của từng commit để nối `FileIdentity` và chạy `DiffFormatter` kèm `RenameDetector`.
3. **Chi phí Lizard và Metric Calculator ổn định rất thấp:** Chặng `LizardComplexityAnalyzer` chỉ phụ thuộc vào số file Java hiện tại ở `headSha` (`25..50` file) nên giữ quanh `2.17 s - 2.89 s`; chặng `RepositoryMetricCalculator` dù xử lý `12.045` facts cũng chỉ tốn **0.71 giây**.
4. **Giới hạn phép đo:** Trên thực tế triển khai web trực tuyến, thời gian tổng từ lúc bấm nút phân tích lần đầu trên một repo mới còn cộng thêm bước `git clone` qua mạng từ GitHub (ở các lần phân tích sau chỉ `git fetch` phần tăng thêm) và thao tác lưu snapshot vào PostgreSQL. Ngoài ra, nếu một kho có hàng nghìn file Java ở `headSha` (lớn hơn bộ test 50 file), chặng Lizard và `TreeWalk` sẽ tăng tương ứng nhưng vẫn nằm trong vùng bảo vệ `maxTreeEntries = 100.000` và `maxCommits = 10.000` của `GitIngestion.Options.defaults`.
