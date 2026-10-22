# Thiết kế thực nghiệm chung — đề xuất tuần 6

Task 20 yêu cầu chốt split, nhãn, baseline và Precision@K/Recall@K/MAP. Hai bản tuần 5 chưa thống nhất. Bản này đưa ra cấu hình cụ thể để review và công cụ tính đã kiểm thử; không tuyên bố đã được Tưởng duyệt hoặc đã có kết quả trên repo thật.

## Đối chiếu và đề xuất

| Điểm | Toản tuần 5 | Tưởng tuần 5 | Đề xuất bản v1 tuần 6 |
|---|---|---|---|
| Cutoff | Mốc cố định + prediction 30 ngày | 80/20 commit theo thời gian | Dùng 80/20 làm primary, 30 ngày là sensitivity đăng ký trước |
| Observation | 180 ngày trước cutoff | 180 ngày trước cutoff | Giữ 180 ngày |
| Tie ranking | path tăng dần | logicalFileId tăng dần | ID tăng dần trong evaluator để ổn định qua rename; UI hiện vẫn theo path theo API đã có |
| MAP | Có AP/MAP toàn ranking | Để bổ sung sau | Có AP/MAP vì workbook task 20 yêu cầu |
| Nhãn | Issue → keyword → review mẫu | Candidate review + negative sample | Giữ provenance, tách confirmed khỏi heuristic, không dùng AI làm nhãn |

Đây là đề xuất kỹ thuật của bộ bàn giao; chưa sửa metric-spec/nhánh của Tưởng hay tự ghi quyết định nhóm. Khi chốt tie/split, đồng bộ config/version, spec và test. [Manifest template](experiment/manifest-template.json) ghi PROPOSED_NOT_APPROVED.

## Quy trình dữ liệu trước khi chạy thật

1. Hai người chọn 3–5 repo Java public phù hợp license/lịch sử/issue, ghi trước tiêu chí chọn/loại. Chưa điền danh sách giả hoặc chọn repo dựa trên điểm tốt.
2. Ghim end SHA/default branch. Non-merge commits sắp theo committer time, chọn cutoff gần 80%, mọi commit cùng timestamp ở cùng phía. Ghi tỷ lệ thực, observationStart=cutoff−180 ngày, prediction tới end SHA. Nếu clock skew/DAG không cho observation SHA không chứa tương lai, ghi loại/ngoại lệ trước chạy.
3. Checkout observation SHA để tính CCN, không dùng HEAD hiện tại. Tất cả feature chỉ dùng observation. Universe là file Java đủ frequency/CCN/lineage ở cutoff, cả ba phương pháp cùng tập. File tạo sau cutoff loại khỏi ranking và ghi số lượng.
4. Tìm bug-fix candidates bằng issue/PR có nhãn bug, tiếp theo keyword có word boundary. Review diff/issue, phân biệt typo/docs/build; keyword không tự thành nhãn thật. Review negative sample với seed 23110158, hai người review chéo mẫu và ghi bất đồng. [Mẫu nhãn](experiment/label-template.json) để trống các quan sát chưa có.
5. Positive là file trong universe bị chạm bởi confirmed fix trong prediction; là proxy, không khẳng định mọi file chạm đều gây bug. Rename tương lai chỉ dùng ánh xạ nhãn, không tạo feature. Uncertain phải xử lý/ghi exclusion riêng, không âm thầm đổi thành negative.
6. Khóa manifest/policy trước test. Xuất features/labels; chạy evaluator. Manifest lưu SHA/window/tools/config/universe hash/seed/provenance. Tách pilot/tuning khỏi evaluation.

## Công cụ đã chạy được

`tools/experiments/evaluate_rankings.py` dùng Python chuẩn, không cần thư viện ngoài. Input JSON có kind=fixture hoặc reviewed-dataset; mỗi case gồm repo/window và files: fileId, frequency, maxCcn, label. Dữ liệu reviewed phải có labelEvidence. Parser từ chối nhãn unresolved, số âm/NaN, ID/case trùng và window sai thứ tự. Kiểm tra này không tự chứng minh rằng pipeline không rò rỉ thời gian; cần audit nguồn.

Ranking: frequency-only, max-CCN-only, hotspot=sqrt(percentile frequency × percentile CCN). Midrank `(midrank−1)/(N−1)`, singleton=0.5, tie theo ID. K=5/10/20, kActual=min(K,N). Precision=TP/k, Recall=TP/P, AP=sum(Precision@i × rel(i))/P, MAP=mean(AP hợp lệ). N=0: precision NA; P=0: recall/AP NA; macro công bố validCases. MAP dùng toàn ranking, không gọi MAP@K.

Ví dụ kiểm chứng AP=[positive,negative,positive,negative,negative] → AP=5/6. Thư mục experiment có [fixture input](experiment/fixture-input.json) và [fixture output](experiment/fixture-output.json), **không phải kết quả thực nghiệm đề tài**. Có 8 tests về ties/AP/K cap/empty/missing/provenance/macro; kết quả [experiment-tests.txt](evidence/experiment-tests.txt).

```text
python -m unittest discover -s tools/experiments -v
python tools/experiments/evaluate_rankings.py docs/week-06/experiment/fixture-input.json docs/week-06/experiment/fixture-output.json
```

## Việc cần xác nhận trước đo thật

Tưởng và Toản chốt split/tie/K/dataset/nhãn; review manifest và phân chia người gán nhãn; chạy adapter Git/metric; kiểm tra kết quả cả khi không hơn baseline. Chưa có benchmark 10.000 commit/10 phút, SUS hay user study mới. Task success/SUS và đánh giá xếp hạng là các phép đo khác nhau.

Nguồn: [đề xuất Tưởng tại 0518e52](https://github.com/Tuong2608/Git-Health-Monitor/blob/0518e52/docs/experiment-design.md), [đề xuất Toản tuần 5](../week-05/experiment-design.md), [metric spec](../metric-spec-v1.md). Công thức được kiểm chứng bằng fixture, không thay kết quả nghiên cứu.
