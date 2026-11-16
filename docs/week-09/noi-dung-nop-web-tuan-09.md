# Báo cáo tiến độ Tuần 9 - Nhóm Git Health Monitor (23110170 & 23110158)

> **Tóm tắt phần mới làm được trong Tuần 9 (Highlight):**
> - **Hoàn thành mới 3 đầu việc lớn (STT 20, STT 29, STT 30)** và triển khai **1 đầu việc thực nghiệm (STT 31)**. Tiến độ tổng thể tăng lên **22 Hoàn thành / 9 Đang làm / 8 Chưa bắt đầu**.
> - **Xây dựng mới CLI `HistoricalSnapshotExport.java`** để trích xuất trực tiếp lịch sử Git tại commit quá khứ (không cần bật server Spring Boot) và hoàn tất bộ script chạy thực nghiệm tự động cho RQ1, RQ2.
> - **Hoàn tất thực nghiệm RQ1 (Tưởng - 23110170):** Tự đọc diff rà soát thủ công **141 commit** trên **401 file Java** để gán nhãn bug-fix thực tế; chứng minh `hotspot_score` đạt điểm trung bình cao nhất (**MAP = 0.3918**, **Recall@10 = 0.3011**, **Precision@20 = 0.3625**, **Recall@20 = 0.5619**).
> - **Triển khai thực nghiệm RQ2 (Toản - 23110158):** Chạy xong **9/9 snapshot lịch sử** (2024 - 2026) trên 3 repo; phát hiện giới hạn thực tế của thuật toán bảo thủ `v1` khi đi qua merge commit (`LINEAGE_AMBIGUOUS`) khiến 6/6 cặp so sánh bị chặn bởi cơ chế an toàn `coverageComplete = false`.
> - **Kiểm thử & độ lặp lại:** Đạt **119/119 JUnit test** ở backend (+4 test mới) và **22/22 unit test Python** (+14 test mới); chạy kiểm chứng độc lập lần 2 khớp 100% mã băm SHA-256 (**13/13 file RQ1** và **15/15 file RQ2**).

---

## 1. Chi tiết công việc đã thực hiện trong Tuần 9

### 1.1. [Hoàn thành mới - STT 20 & STT 29] Chốt giao thức thực nghiệm và đóng băng tập dữ liệu (Cả nhóm)
- **Giao thức đánh giá:** Thống nhất quy tắc chia lịch sử commit theo tỷ lệ **80% quá khứ / 20% tương lai** trên các non-merge commit. Tại mốc cắt 80% (`observationSha`), hệ thống chỉ dùng cửa sổ 180 ngày trước đó để tính metric và kiểm tra điều kiện tiên quyết để đảm bảo không rò rỉ dữ liệu tương lai.
- **Đóng băng SHA đầu vào:** Chốt cứng danh sách commit SHA trong file manifest trước khi chạy đo trên **5 kho mã nguồn mở cho RQ1** (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`) và **3 kho cho RQ2** (`gs-rest-service`, `spring-petclinic`, `commons-cli`).

### 1.2. [Hoàn thành mới - STT 30] Thực nghiệm đánh giá xếp hạng rủi ro RQ1 (Trần Văn Tưởng - 23110170)
- **Công cụ:** Viết `run_rq1.py` và `summarize_rq1.py` kết nối với `HistoricalSnapshotExport` (JGit + Lizard 1.24.3 + `RepositoryMetricCalculator`) để tính `frequency`, `complexity` và `hotspot_score` tại mốc quá khứ `observationSha`.
- **Rà soát dữ liệu lỗi (Ground Truth):** Ở 20% lịch sử tương lai, lọc các commit ứng viên có từ khóa sửa lỗi/mã Jira kết hợp lấy mẫu âm ngẫu nhiên (`seed=23110170`, 10 commit/kho). Trực tiếp đọc `git show` từng commit để kiểm tra **141 commit** (101 ứng viên + 40 mẫu âm):
  - Loại bỏ các commit dương tính giả chỉ sửa chú thích hoặc format (như commit `26c1193859` sửa ngữ pháp Javadoc trên 30 file của `commons-validator`, hay `6a11b896aa` bật Checkstyle trên 11 file của `commons-csv`).
  - Phát hiện **9 commit sửa lỗi thật** không chứa từ khóa trong tiêu đề nhờ kiểm tra tập mẫu âm.
  - Tổng cộng xác nhận **67 file có lỗi thực tế (P = 67)** trên **401 file Java đủ điều kiện (N = 401)** của 4 kho Apache Commons. Kho `javapoet` có 36/36 file bị đánh dấu `LINEAGE_AMBIGUOUS` do lịch sử nhiều merge commit nên có 0 file đủ điều kiện ở bản `v1`, nhưng nhóm vẫn giữ nguyên trong bảng kết quả để minh bạch số liệu.

**Bảng kết quả thực nghiệm RQ1 trên các kho mã nguồn:**

| Dự án (Số file N / Số file lỗi P) | Phương pháp xếp hạng | AP | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 | Spearman rho |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :---: | :---: |
| **commons-csv** (N = 43, P = 12) | `frequency` | 0.6431 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5000 | 0.8333 | 0.6241 |
| **commons-csv** (N = 43, P = 12) | `complexity` | 0.6808 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5500 | 0.9167 | 0.6067 |
| **commons-csv** (N = 43, P = 12) | **`hotspot_score`** | **0.7116** | **0.8000** | **0.3333** | **0.8000** | **0.6667** | **0.5500** | **0.9167** | **0.6282** |
| **commons-codec** (N = 126, P = 33) | `frequency` | 0.3319 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | 0.4000 | 0.2424 | 0.1297 |
| **commons-codec** (N = 126, P = 33) | `complexity` | 0.4427 | 0.6000 | 0.0909 | 0.7000 | 0.2121 | 0.4500 | 0.2727 | 0.2371 |
| **commons-codec** (N = 126, P = 33) | **`hotspot_score`** | 0.4244 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | **0.5000** | **0.3030** | 0.2245 |
| **commons-dbutils** (N = 84, P = 4) | `frequency` | 0.1458 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1000 | 0.5000 | 0.0848 |
| **commons-dbutils** (N = 84, P = 4) | `complexity` | 0.1821 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1500 | 0.7500 | 0.2413 |
| **commons-dbutils** (N = 84, P = 4) | **`hotspot_score`** | 0.1681 | 0.0000 | 0.0000 | 0.1000 | 0.2500 | **0.1500** | **0.7500** | **0.2651** |
| **commons-validator** (N = 148, P = 18) | `frequency` | 0.1951 | 0.2000 | 0.0556 | 0.3000 | 0.1667 | 0.1500 | 0.1667 | 0.1527 |
| **commons-validator** (N = 148, P = 18) | `complexity` | 0.2393 | 0.2000 | 0.0556 | 0.2000 | 0.1111 | 0.2000 | 0.2222 | 0.1569 |
| **commons-validator** (N = 148, P = 18) | **`hotspot_score`** | **0.2631** | **0.4000** | **0.1111** | **0.3000** | **0.1667** | **0.2500** | **0.2778** | **0.1749** |
| **javapoet** (36 file / N = 0) | *(Loại do merge lineage v1)* | N/A | N/A | N/A | N/A | N/A | N/A | N/A | N/A |
| **Trung bình 4 kho hợp lệ** | `frequency` | 0.3290 | 0.4000 | 0.1749 | 0.3750 | 0.2803 | 0.2875 | 0.4356 | - |
| **Trung bình 4 kho hợp lệ** | `complexity` | 0.3862 | **0.4500** | **0.1824** | **0.4250** | 0.2891 | 0.3375 | 0.5404 | - |
| **Trung bình 4 kho hợp lệ** | **`hotspot_score`** | **0.3918** | 0.4000 | 0.1263 | 0.4000 | **0.3011** | **0.3625** | **0.5619** | - |

### 1.3. [Đang thực hiện - STT 31] Thực nghiệm theo dõi xu hướng lịch sử RQ2 (Trần Quang Toản - 23110158)
- **Công cụ:** Xây dựng `HistoricalSnapshotExport.java` (kèm 4 JUnit test) và bộ script `rq2_trends.py`, `run_rq2.py`, `summarize_rq2.py` (kèm 9 unit test Python) để nối chuỗi file theo `lineageKey` qua nhiều thời điểm và kiểm tra ngưỡng cảnh báo tăng rủi ro `+0.15`.
- **Kết quả chạy 9 snapshot lịch sử:** Đã trích xuất thành công 9/9 snapshot tại 3 mốc (`2024-01-01`, `2025-01-01`, `2026-01-01`) cho `gs-rest-service`, `spring-petclinic` và `commons-cli`.
- **Phát hiện kỹ thuật:** Toàn bộ 6/6 cặp so sánh giữa các năm đều rơi vào trạng thái `BLOCKED` (`INCOMPLETE_COVERAGE`). Khi kiểm tra mã nguồn `GitIngestion.java` (dòng 103 - 110), nhóm xác định nguyên nhân là quy tắc nối lịch sử file `v1` đang đánh dấu `LINEAGE_AMBIGUOUS` mỗi khi kho có merge commit mà chỉ 1 nhánh cha chạm vào file. Nhờ vậy cơ chế chặn cảnh báo sai của hệ thống đã hoạt động đúng thiết kế, và nhóm sẽ bổ sung đánh giá theo tình huống kiểm soát ở Tuần 10.

---

## 2. Minh chứng mã nguồn và Kế hoạch Tuần 10

- **Pull Request đã merge trong Tuần 9:**
  - PR #11 (Toản - CLI xuất snapshot & RQ2): https://github.com/Tuong2608/Git-Health-Monitor/pull/11
  - PR #12 (Tưởng - Thực nghiệm RQ1 & tổng hợp Tuần 9): https://github.com/Tuong2608/Git-Health-Monitor/pull/12
- **File minh chứng trong repository (`main` @ `3fce92d`):**
  - RQ1: `docs/week-09/rq1-protocol.md`, `docs/week-09/rq1-commit-reviews.json`, `docs/week-09/evidence/rq1/results.md`, `docs/week-09/evidence/rq1/rq1-summary.csv`
  - RQ2: `docs/week-09/rq2-protocol.md`, `docs/week-09/evidence/pilot/results.md`, `docs/week-09/evidence/pilot/comparison-summary.csv`
- **Kế hoạch Tuần 10:** Hoàn tất đánh giá tình huống cho RQ2 (STT 31), đo benchmark thời gian phân tích tới 10.000 commit (STT 32) và tiến hành khảo sát người dùng RQ3 (STT 33).
