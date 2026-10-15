# ADR 001 — Full clone/fetch

Ngày 08/10/2026. Trạng thái: đề xuất ghi nhận định hướng đề cương, chờ review nhóm. Toản soạn; Tưởng phụ trách ingestion.

## Context

Pipeline cần history/diff/rename và Java source tại HEAD để tính metric tái lập. Giới hạn 10.000 commit phân tích không có nghĩa clone chỉ tải 10.000 commit.

## Decision

Full clone ban đầu, fetch lần sau; không shallow/partial clone trong core. Chỉ phân tích default branch, cố định SHA. Không chạy code/build script repository nguồn, không tự tải submodule/LFS. Workspace theo id server sinh, có timeout/quota disk; tham số JGit thiết kế tuần 5.

## Alternatives

Shallow clone giảm history nhưng có thể thiếu dữ liệu cần; partial clone giảm blob ban đầu nhưng tăng phụ thuộc lượt fetch/khả năng thư viện; GitHub API chịu quota và cần ghép nhiều request.

## Why chosen

Ưu tiên dữ liệu đầy đủ và đường xử lý dễ kiểm chứng trong nhóm nhỏ, không khẳng định full clone luôn nhanh hơn hoặc JGit không hỗ trợ partial clone. [Git clone documentation](https://git-scm.com/docs/git-clone.html) phân biệt depth và filter; lựa chọn này là quyết định dự án.

## Consequences

Tốn băng thông/disk; repo rất lớn có thể vượt tài nguyên dù chỉ phân tích 10.000 commit. Benchmark tách clone và core nhưng tổng NFR03 vẫn tính clone. Cache phải xác minh remote/HEAD. Nếu pilot không đạt, ghi ADR thay thế trước đổi cách làm, không âm thầm đổi window/công thức. Kiểm thử sau cài đặt: history rename, clone timeout không tạo snapshot, giới hạn workspace, cold/warm benchmark.

## Bổ sung bản review chung ngày 09/10/2026

Tưởng giữ hướng full history cho baseline. “Full” là đủ object/history default branch, không bắt buộc mirror mọi ref. Cache xác minh remote, workspace riêng theo job/claimToken; không đổi checkout worker khác đang đọc.

BR16 cắt tới 10.000 commit gần nhất là đề xuất chờ duyệt, độc lập tài nguyên full clone. Nếu chấp nhận phải công bố truncated/count/effective bounds; không gọi là toàn bộ lịch sử khi cắt. Clone vượt tài nguyên trả lỗi riêng thay vì tính object thiếu. Fixture kiểm tra root/merge/rename/window/SHA.

Xem [architecture](../architecture.md) và [đối chiếu](../week-04/review-tich-hop-tuong-toan.md). Bổ sung chưa phải xác nhận Accepted của thành viên.
