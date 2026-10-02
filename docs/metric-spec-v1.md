# Metric Specification v1

> Trạng thái: đặc tả kỹ thuật v1 dùng làm baseline cho phần cài đặt metric của Git Health Monitor. Nếu GVHD hoặc nhóm thay đổi định nghĩa, phải cập nhật file này, tài liệu liên quan và test cùng lúc.

## 1. Phạm vi phân tích

- Phân tích lịch sử trên **default branch** của repository.
- Cửa sổ quan sát mặc định: **180 ngày**, nhưng phải cấu hình được.
- Các metric trong cùng một snapshot phải dùng cùng cửa sổ quan sát.
- Bỏ qua **merge commit** khi tính change frequency, churn, temporal coupling và contribution concentration để giảm coupling/churn giả do merge.
- Bật rename detection khi lấy diff bằng JGit: `DiffFormatter#setDetectRenames(true)`.
- Chuẩn hóa danh tính developer trước khi tính contribution; ưu tiên mapping cấu hình, sau đó email/identity đã chuẩn hóa.

## 2. Change frequency

```text
change_frequency(file)
  = số non-merge commit trong observation window
    mà file xuất hiện trong diff
```

Rename phải được nối lịch sử hợp lý để tránh tính file cũ và file mới thành hai thực thể độc lập chỉ vì đổi tên.

## 3. Code churn

```text
churn(file)
  = tổng additions(file) + deletions(file)
    trong observation window
```

Ở mức developer/file:

```text
contribution(dev, file)
  = additions(dev, file) + deletions(dev, file)
```

## 4. Temporal coupling

Với hai file A và B:

```text
C_A = tập non-merge commit trong observation window có thay đổi A
C_B = tập non-merge commit trong observation window có thay đổi B

shared_commits(A,B) = |C_A ∩ C_B|

coupling(A,B)
  = shared_commits(A,B) / min(|C_A|, |C_B|)
```

Điều kiện hiển thị/cảnh báo mặc định:

```text
shared_commits >= 5
AND
coupling >= 0.50
```

Ví dụ A xuất hiện trong 10 commit, B trong 20 commit và có 5 commit chung:

```text
coupling(A,B) = 5 / min(10,20) = 0.50
```

Không dùng công thức `shared / |C_A ∪ C_B|` cho cùng ngưỡng 50%, vì đó là một định nghĩa khác và cho kết quả khác.

## 5. Contribution concentration (HHI)

HHI trong đề tài đo **mức tập trung đóng góp lịch sử**, không trực tiếp đo mức hiểu mã nguồn.

```text
contribution_i(file)
  = additions_i(file) + deletions_i(file)

share_i(file)
  = contribution_i(file) / total_contribution(file)

HHI(file)
  = Σ share_i(file)^2
```

Diễn giải:

- HHI gần 1: đóng góp lịch sử tập trung vào ít developer.
- HHI thấp hơn: đóng góp lịch sử phân bố giữa nhiều developer hơn.
- Không kết luận từ HHI rằng một người "hiểu" hoặc "không hiểu" file.

Bản v1 chưa đặt hard threshold cho HHI; dashboard nên hiển thị score và thứ hạng/percentile trước khi nhóm có dữ liệu thực nghiệm đủ để đặt ngưỡng.

## 6. Cyclomatic complexity bằng Lizard

Lizard cung cấp CCN theo function. Đối với metric đại diện cấp file:

```text
file_complexity(file)
  = max(CCN(function))
    với mọi function trong file
```

Ngoài giá trị dùng cho hotspot, nên lưu thêm:

- `max_ccn`
- `avg_ccn`
- `function_count`
- `nloc`

Khi gọi Lizard từ Java qua `ProcessBuilder`, ưu tiên output có cấu trúc như `--xml` thay vì parse console text tự do.

## 7. Hotspot score

```text
P_change
  = percentile(change_frequency) trong repository

P_complexity
  = percentile(file_complexity) trong repository

hotspot_score
  = sqrt(P_change * P_complexity)
```

Trong v1, `file_complexity` là max function CCN của file.

## 8. JGit ingestion baseline

Pipeline dự kiến:

```text
clone/fetch repository
  -> walk non-merge commits
  -> diff với rename detection
  -> normalize developer identity
  -> persist file-change facts
  -> calculate metrics
  -> create snapshot
```

PoC JGit/Lizard đã được ghi nhận trong tài liệu nghiên cứu, nhưng chưa phải implementation của backend hiện tại. Khi bắt đầu cài đặt chính thức cần thêm dependency JGit, worker/job và schema dữ liệu tương ứng.

## 9. Performance target

NFR:

```text
repository <= 10,000 commits
target total analysis time <= 10 minutes
```

Trạng thái hiện tại: **chưa được benchmark bằng pipeline hoàn chỉnh**.

Benchmark phải ghi ít nhất:

- repository/dataset và commit count;
- file count/LOC nếu có;
- CPU, RAM, OS;
- Java/JGit/Lizard version;
- thời gian clone/fetch;
- thời gian JGit extraction;
- thời gian Lizard;
- thời gian tính metric;
- thời gian persist database;
- tổng thời gian.

Nên report riêng thời gian clone/fetch và thời gian core analysis để không đánh đồng tốc độ mạng với hiệu năng thuật toán.

## 10. Quy tắc thay đổi spec

Bất kỳ thay đổi nào về công thức, observation window mặc định, filter merge commit, cách aggregate CCN, contribution basis hoặc threshold phải:

1. cập nhật file này;
2. cập nhật code và test;
3. cập nhật tài liệu nghiên cứu/báo cáo liên quan;
4. ghi rõ lý do và ảnh hưởng tới khả năng so sánh các snapshot cũ/mới.
