# Đề xuất thiết kế thực nghiệm — task 20, tuần 5–6

Người phối hợp: Toản + Tưởng. Bản Toản chuẩn bị ngày 15/10/2026 để review; chưa có dữ liệu thực nghiệm, nhãn thật, kết quả hay xác nhận Tưởng. Thông số dưới đây là đề xuất, phải khóa trước khi chạy tập đánh giá.

## Câu hỏi và phạm vi

RQ1: xếp hạng hotspot kết hợp frequency/complexity có giúp ưu tiên file sẽ được sửa lỗi trong tương lai tốt hơn từng baseline đơn lẻ không? Nhãn đo **file có thay đổi trong commit sửa lỗi**, không chứng minh thời điểm bug được tạo ra hay mọi file trong commit đều có lỗi.

RQ2: thông tin coupling/evidence và snapshot có hỗ trợ khoanh vùng và tránh bỏ sót file khi review? Đánh giá bằng nhiệm vụ người dùng riêng; không suy ra hiệu quả nghiệp vụ chỉ từ Precision@K.

## Chọn dữ liệu và chống rò rỉ tương lai

- Đề xuất 3–5 repository Java public, license cho phép nghiên cứu, lịch sử và issue đủ đối chiếu; có tối thiểu 210 ngày dữ liệu và nhiều file Java hợp lệ. Danh sách repo, SHA, mốc thời gian, lý do nhận/loại cần Tưởng cùng chốt; chưa tuyên bố đã chọn được mẫu.
- Mỗi repo chọn cutoff T, lấy observation `[T-180 ngày,T)`, prediction `[T,T+30 ngày)`. Ví dụ lịch minh họa T=01/07/2026 UTC; khi chạy chọn mốc có đủ cả hai cửa sổ và công bố trong manifest. Không chia commit ngẫu nhiên.
- Checkout commit cuối trước T trên lịch sử default branch được ghim SHA. Lizard đọc mã ở cutoff, không đọc HEAD hiện tại để tạo feature quá khứ. Frequency/churn/contribution/coupling chỉ từ observation, loại merge/filters theo config đã khóa. Ghi rõ dùng committer timestamp UTC và chính sách xử lý timestamp bất thường.
- Chỉ rank file Java tồn tại tại cutoff, có đủ frequency và CCN theo metric-spec. Ba phương pháp cùng tập eligible để so sánh công bằng; báo cáo số file bị loại và lý do. Không thêm file chỉ xuất hiện sau T. File rename trong prediction chỉ ánh xạ để tạo nhãn, không đưa thông tin tương lai vào feature.
- Tách repo/mốc pilot để thử parser và thiết kế nhãn. Không điều chỉnh trọng số, ngưỡng hay K theo kết quả test. Nếu cần tuning, dùng mốc sớm hơn, giữ prediction window test tách biệt; công bố mọi thay đổi và chạy lại với version mới.

## Quy trình nhãn bug-fix

| Ưu tiên | Cách lấy | Minh chứng phải lưu |
|---|---|---|
| 1 | Commit/PR liên kết issue có nhãn bug và mô tả sửa lỗi phù hợp | issue URL, PR/commit SHA, trạng thái/nhãn và ngày thu thập |
| 2 | Không có liên kết rõ: heuristic commit message, ví dụ fix/bug/defect; loại fix typo/format/docs | thông điệp, heuristic version và lý do; chỉ là nhãn ứng viên |
| 3 | Kiểm tra thủ công mẫu dương và âm, đọc diff + issue, xử lý commit hỗn hợp | reviewer, quyết định, lý do, bất đồng và kết luận sau trao đổi |

Không coi từ `fix` tự động là ground truth. Đề xuất hai người gán độc lập 30 ứng viên dương + 30 âm ngẫu nhiên mỗi repo (lấy toàn bộ nếu ít hơn), khóa seed, báo cáo tỷ lệ đồng thuận và thống kê sai nhãn. Không biết thì đánh dấu uncertain; báo cáo tỷ lệ loại và sensitivity analysis giữa nhãn strict (issue + manual xác nhận) với nhãn mở rộng. Không giả hai người đã kiểm tra mẫu. Nhãn issue ở thời điểm thu thập có thể thay đổi so với quá khứ, cần ghi hạn chế.

File positive nếu có ít nhất một thay đổi sửa lỗi được xác nhận trong prediction. Loại chỉ đổi format/tài liệu/rename thuần theo quy tắc thống nhất. Không dùng SZZ để suy luận bug-introducing commit trong phạm vi này. File không có bug-fix được quan sát là negative theo phép đo, không khẳng định không có bug.

## Ba phương pháp cùng dữ liệu

| Phương pháp | Điểm xếp hạng | Không được dùng |
|---|---|---|
| Frequency-only | Số commit hợp lệ thay đổi file trong observation, giảm dần | Commit trong prediction |
| Complexity-only | Max function CCN ở cutoff, giảm dần | Mã nguồn sau cutoff |
| Hotspot | Công thức percentile kết hợp theo metric-spec-v1; đóng băng version và tie policy | Điều chỉnh để hợp kết quả test |

Tie cuối cùng theo path tăng dần; dùng cùng tập file, cùng window và cùng exclusions. Baseline không cần chuẩn hóa vì phép biến đổi đơn điệu không đổi thứ tự; vẫn phải cố định tie. Mọi phương pháp xuất danh sách xếp hạng đầy đủ, không chỉ top 10.

## Chỉ số và cách tổng hợp

Đặt N là số file eligible, P là số positive trong tập đó, `k=min(K,N)`, K ∈ {5,10,20}. TP@k là số positive trong top k.

- Precision@K = TP@k / k. Nếu N=0 thì NA, báo riêng, không cho 0.
- Recall@K = TP@k / P. Nếu P=0 thì NA và báo số trường hợp không có positive.
- AP = `(sum từ i=1 đến N của Precision@i × rel(i))/P`, dùng toàn bộ ranking; rel(i)=1 nếu positive. P=0 thì AP=NA.
- MAP = trung bình AP của các repo/cutoff hợp lệ. Đây là MAP toàn ranking, không gọi MAP@K. Tổng hợp Precision/Recall bằng macro-average trên các trường hợp có định nghĩa và báo rõ mẫu số từng chỉ số.

Ví dụ fixture để kiểm tra script: N=5, rel=[1,0,1,0,0], P=2, K=3 → Precision=2/3, Recall=1, AP=(1+2/3)/2=5/6. Không đưa các số này vào kết quả thực nghiệm. Với ít repo, trình bày từng repo và chênh lệch so baseline; không tuyên bố ý nghĩa thống kê từ mẫu nhỏ.

## Đánh giá thao tác người dùng và giám sát

Chuẩn bị các nhiệm vụ: tìm file ưu tiên, mở commit evidence, giải thích coupling có mẫu số, tìm người trao đổi, so sánh snapshot, xử lý cảnh báo, kiểm chứng gợi ý AI. Ghi thành công/thất bại, thời gian, lỗi thao tác và nhận xét. Đổi thứ tự công cụ giữa người tham gia nếu so với git log/blame để giảm hiệu ứng học. Chỉ thu thập khi có đồng ý; ẩn danh, không chép email vào báo cáo.

KPI theo đề cương/baseline hiện có: task success 80%, SUS 68, mẫu 5–9 người; các mức 90%/80/≥10 ở nhánh reconcile là mục tiêu nâng cao cần chốt, không tự thay thế. Chưa tuyển người hay ghi điểm SUS. Test lịch/lease/restart là thử nghiệm kỹ thuật riêng khi worker có thật, không lấy wireframe làm minh chứng.

## Đầu ra và phối hợp tuần 6

| Đầu ra | Đề xuất phụ trách | Điều kiện hoàn tất |
|---|---|---|
| Dataset manifest | Tưởng + Toản | Repo, license, cutoff SHA, window, exclusions, tool versions, seed, config hash |
| Pipeline/ranking | Tưởng, Toản kiểm tra schema đầu ra | Chạy được 3 phương pháp trên cùng eligible set, có fixture tests |
| Label sheet | Toản + Tưởng | Provenance + hai lượt review mẫu + xử lý bất đồng |
| Bảng kết quả/biểu đồ | Toản tổng hợp, Tưởng kiểm tra | CSV đầu vào, script, logs, Precision/Recall/MAP thật và hạn chế |

Các vai trò chi tiết là đề xuất phối hợp, không sửa phân công của nhóm trưởng. Tuần này hoàn tất bản thiết kế để review; thực thi và kết luận vẫn chưa làm.

## Nguồn và áp dụng

- [Git log](https://git-scm.com/docs/git-log): tham chiếu việc chọn revision/time window, loại merge và đọc lịch sử; phải khóa config thay vì dùng HEAD biến động.
- [Stanford IR Book — Evaluation of ranked retrieval results](https://nlp.stanford.edu/IR-book/html/htmledition/evaluation-of-ranked-retrieval-results-1.html): cơ sở Precision/Recall/AP/MAP. Quy tắc NA, tập eligible và thời gian ở trên là quyết định thiết kế của nhóm cần review.
- [Metric spec của dự án](../metric-spec-v1.md): công thức feature/ranking; [rules/NFR](../business-rules-nfr.md): tiêu chí chấp nhận. Đã kiểm tra các nguồn online ngày 15/10/2026.
