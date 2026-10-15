# Đề xuất thực nghiệm chung — tuần 5

Ngày 15/10/2026. Tưởng soạn để trao đổi với Toản; chưa có đồng thuận, dataset hay kết quả đo.

## Mục tiêu và split

RQ1: hotspot kết hợp có ưu tiên được file xuất hiện trong bug-fix tương lai tốt hơn frequency-only và complexity-only không? Metric/AI không phải nhãn bug. Chọn trước 3–5 repo Java OSS có lịch sử phù hợp; lưu URL/license, SHA cuối, tiêu chí chọn/loại và lý do. Không chọn repo sau khi thấy score tốt.

Đề xuất split thời gian 80/20 trên non-merge commit của default branch. Sắp theo committerTime; chọn cutoff gần 80%, đưa toàn bộ commit cùng timestamp về cùng phía, công bố tỷ lệ thực. Observation dùng timestamp < cutoff, prediction dùng ≥cutoff. Cần có commit và positive labels ở cả phạm vi cần đánh giá; nếu không đủ thì ghi unavailable/exclusion, không tự chọn cutoff khác để tăng điểm.

Chọn SHA lịch sử trên first-parent default branch tại mốc cutoff sao cho toàn bộ reachable commits dùng trong observation có timestamp < cutoff. Nếu clock skew/DAG không cho một mốc phù hợp, kiểm tra và ghi loại hoặc quy tắc xử lý trước chạy. Không checkout HEAD hiện tại để đo complexity quá khứ. Observation metric vẫn dùng 180 ngày trước cutoff, prediction là phần thời gian sau cutoff đến end SHA đã pin; công bố realized windows/counts. Thử window khác là sensitivity riêng, không trộn kết quả.

Universe đánh giá: file Java tồn tại tại observation SHA, có đầy đủ frequency/CCN và lineage; ba phương pháp cùng universe. File chỉ xuất hiện trong prediction không có score ban đầu nên loại khỏi denominator, báo riêng số lượng. Identity mapping/exclusions không được học từ bug labels tương lai.

## Nhãn bug-fix và chống leakage

Tìm candidate trong prediction bằng issue label nếu có, hoặc regex case-insensitive có word boundary cho fix/fixes/fixed/bug/bugs. Từ khóa chỉ là candidate, có false positive (format/build/docs). Ghi candidate SHA, nguồn, reviewer, nhãn confirmed/rejected/uncertain và lý do. Review diff/issue độc lập với score.

Đề xuất review thủ công toàn bộ candidate khi khả thi, đồng thời lấy mẫu negative không khớp từ khóa với seed cố định để ước lượng bỏ sót. Nếu chỉ review một mẫu, báo rõ phần còn lại là nhãn heuristic và tách đánh giá sensitivity; không mô tả tất cả là bug đã xác nhận. Hai thành viên kiểm tra chéo một mẫu, ghi bất đồng và cách giải quyết. Không tự tạo câu trả lời/nhãn thực nghiệm.

Positive file = file trong universe được chạm bởi ít nhất một confirmed bug-fix commit trong prediction. Nối rename tương lai về ID observation để tạo label, không dùng thay đổi tương lai làm feature. Một commit fix nhiều file có thể chứa chỉnh sửa phụ: ghi đây là proxy label, chưa phải xác nhận từng file gây bug. Uncertain và lineage không rõ phải báo coverage/loại tường minh.

## Ranking và metric

Ba ranking: hotspot=sqrt(P_change×P_complexity), change-frequency-only, max-function-CCN-only. Tie-break cùng logicalFileId tăng dần; không dùng nhãn để phá tie. Chọn trước K=5,10,20; với N<K dùng k=min(K,N) và hiển thị k thực.

```text
TP_at_k = number of labeled positive files in top k
Precision_at_k = TP_at_k / k           # null if N=0
Recall_at_k = TP_at_k / all_positives  # null if all_positives=0
```

Ví dụ N=10, positives=4, top5 có2 → P@5=0,4 và R@5=0,5. Với zero positives, precision=0 nếu k>0, recall=null; không thay recall bằng0 khi macro-average. Báo per-repo, macro mean và số repo hợp lệ mỗi metric; không chỉ báo số trung bình thuận lợi. MAP có trong kế hoạch cũ, để bổ sung sau khi thống nhất toàn bộ ranking và nhãn; tuần này ưu tiên P@K/R@K theo phân công mới.

Lưu manifest: repo/cutoff/SHAs/windows/config/tool versions/universe hash/seed/labeling policy và ranking output. Không dùng LLM output để gán label hay điều chỉnh score. Chạy lại cùng manifest phải tái lập. Thử nhiều cutoff là mở rộng đăng ký trước, không chọn cutoff tốt nhất để báo.

## Giới hạn và bàn giao

Commit timestamp lệch, bug-fix keyword nhiễu, thay đổi phụ trong fix commit, repo nhỏ và khác độ dài prediction đều có thể làm sai lệch. Báo dataset bị loại, coverage và cả kết quả không cải thiện. Chưa đủ dữ liệu để khẳng định ý nghĩa thống kê.

Nhóm cần chốt dataset, cutoff policy, K, nhãn/khối lượng review và người kiểm tra trước chạy. Thực nghiệm hiệu năng 10.000 commit/10 phút và user study 5–9 người, task success≥80%, SUS≥68 là hai phép đo riêng theo đề cương, chưa có kết quả.
