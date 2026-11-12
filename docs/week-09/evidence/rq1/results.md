# Kết quả thực nghiệm RQ1 — Đánh giá xếp hạng rủi ro bảo trì theo chia cắt thời gian 80/20

Dữ liệu đầu vào được cố định trong `rq1-frozen-manifest.json` trước khi chạy xếp hạng; nhãn `positive`/`negative` được kiểm tra thủ công từ tập commit ứng viên (Jira/keyword) và mẫu âm ngẫu nhiên (`seed=23110170`). Dự án có `eligibleFiles = 0` do quy tắc lineage qua merge ở `v1` (`javapoet`) được giữ nguyên trong bảng kiểm toán, không loại bỏ để làm đẹp số liệu.

## 1. Thống kê tập dữ liệu và rà soát nhãn theo dự án

| Dự án | Mốc cắt 80/20 (UTC) | Tỷ lệ commit thực | File Java | File đủ điều kiện ($N$) | Lý do loại (`v1`) | Ứng viên / Mẫu âm đã duyệt | Commit bug-fix xác nhận | File dương ($P$) | Nhãn từ khóa thô (chưa lọc) |
|---|---|---:|---:|---:|---|---:|---:|---:|---:|
| commons-csv | 2024-09-18T21:52:03Z | 0.7999 | 49 | 43 | LINEAGE_AMBIGUOUS:3, NO_FUNCTIONS:3 | 28 + 10 | 17 (mẫu âm: 4) | 12 | 21 |
| commons-codec | 2024-12-28T12:33:19Z | 0.8001 | 156 | 126 | LINEAGE_AMBIGUOUS:14, NO_FUNCTIONS:16 | 45 + 10 | 25 (mẫu âm: 1) | 33 | 49 |
| commons-dbutils | 2024-09-20T21:08:53Z | 0.8001 | 95 | 84 | LINEAGE_AMBIGUOUS:1, NO_FUNCTIONS:10 | 4 + 10 | 4 (mẫu âm: 2) | 4 | 4 |
| commons-validator | 2024-12-26T15:24:03Z | 0.8001 | 160 | 148 | LINEAGE_AMBIGUOUS:6, NO_FUNCTIONS:6 | 24 + 10 | 9 (mẫu âm: 2) | 18 | 44 |
| javapoet | 2018-04-29T00:18:33Z | 0.7997 | 36 | 0 | LINEAGE_AMBIGUOUS:36 | — | — | — | — |

## 2. Kết quả xếp hạng trên từng dự án hợp lệ ($N > 0, P > 0$)

| Dự án | Phương pháp | AP | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 | Spearman $\rho$ |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|
| commons-csv | `frequency` | 0.6431 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5000 | 0.8333 | 0.6241 |
| commons-csv | `complexity` | 0.6808 | 0.8000 | 0.3333 | 0.7000 | 0.5833 | 0.5500 | 0.9167 | 0.6067 |
| commons-csv | `hotspot` | 0.7116 | 0.8000 | 0.3333 | 0.8000 | 0.6667 | 0.5500 | 0.9167 | 0.6282 |
| commons-codec | `frequency` | 0.3319 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | 0.4000 | 0.2424 | 0.1297 |
| commons-codec | `complexity` | 0.4427 | 0.6000 | 0.0909 | 0.7000 | 0.2121 | 0.4500 | 0.2727 | 0.2371 |
| commons-codec | `hotspot` | 0.4244 | 0.4000 | 0.0606 | 0.4000 | 0.1212 | 0.5000 | 0.3030 | 0.2245 |
| commons-dbutils | `frequency` | 0.1458 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1000 | 0.5000 | 0.0848 |
| commons-dbutils | `complexity` | 0.1821 | 0.2000 | 0.2500 | 0.1000 | 0.2500 | 0.1500 | 0.7500 | 0.2413 |
| commons-dbutils | `hotspot` | 0.1681 | 0.0000 | 0.0000 | 0.1000 | 0.2500 | 0.1500 | 0.7500 | 0.2651 |
| commons-validator | `frequency` | 0.1951 | 0.2000 | 0.0556 | 0.3000 | 0.1667 | 0.1500 | 0.1667 | 0.1527 |
| commons-validator | `complexity` | 0.2393 | 0.2000 | 0.0556 | 0.2000 | 0.1111 | 0.2000 | 0.2222 | 0.1569 |
| commons-validator | `hotspot` | 0.2631 | 0.4000 | 0.1111 | 0.3000 | 0.1667 | 0.2500 | 0.2778 | 0.1749 |

## 3. Trung bình vĩ mô (Macro-Average) trên các dự án đủ điều kiện (`validCases = 4 / 5`)

| Phương pháp | MAP (`validCases=4`) | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 |
|---|---:|---:|---:|---:|---:|---:|---:|
| `frequency` | 0.3290 | 0.4000 | 0.1749 | 0.3750 | 0.2803 | 0.2875 | 0.4356 |
| `complexity` | 0.3862 | 0.4500 | 0.1824 | 0.4250 | 0.2891 | 0.3375 | 0.5404 |
| `hotspot` | 0.3918 | 0.4000 | 0.1263 | 0.4000 | 0.3011 | 0.3625 | 0.5619 |

## 4. Phân tích độ nhạy (Sensitivity Analysis)

| Cấu hình đánh giá | Phương pháp | MAP (`validCases=4`) | P@5 | R@5 | P@10 | R@10 | P@20 | R@20 |
|---|---|---:|---:|---:|---:|---:|---:|---:|
| Chỉ file nguồn `src/main/java` | `frequency` | 0.3831 | 0.4500 | 0.2635 | 0.3667 | 0.6375 | 0.3542 | 0.7750 |
| Chỉ file nguồn `src/main/java` | `complexity` | 0.4436 | 0.4500 | 0.2635 | 0.4417 | 0.6844 | 0.3417 | 0.7500 |
| Chỉ file nguồn `src/main/java` | `hotspot` | 0.4298 | 0.4000 | 0.2479 | 0.3917 | 0.6531 | 0.3542 | 0.7562 |
| Nhãn từ khóa thô chưa lọc (`fix/bug`) | `frequency` | 0.4883 | 0.6000 | 0.1544 | 0.5750 | 0.2968 | 0.4625 | 0.4040 |
| Nhãn từ khóa thô chưa lọc (`fix/bug`) | `complexity` | 0.5898 | 0.7000 | 0.2220 | 0.6750 | 0.3116 | 0.6000 | 0.5278 |
| Nhãn từ khóa thô chưa lọc (`fix/bug`) | `hotspot` | 0.5731 | 0.7000 | 0.1652 | 0.6750 | 0.2684 | 0.5875 | 0.5227 |
