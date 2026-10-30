# Nhật ký AI hỗ trợ — Tưởng tuần 6

Ngày 22–23/10/2026. Công cụ: Codex desktop. Không ghi phiên bản model chưa được xác nhận. Người dùng yêu cầu làm phần tuần 6 theo phân công (mô-đun thu thập dữ liệu Git), sau đó lấy và đọc bàn giao của Toản trên GitHub để đối chiếu.

Nguồn: nhánh `codex/tuong-week5` `0518e52`, `codex/week5-report` `a7c84d1`, Metric Spec v1, [algorithm-design](../algorithm-design.md) và main `188968c` → `0b664d9` sau khi Toản merge PR #5. AI tạo bản nháp `GitRepositoryClient`, `GitIngestion`, fixture JGit và `GitIngestionSample`, soạn tài liệu hợp đồng module, giải quyết xung đột `README.md` khi merge, cập nhật bảng trạng thái Excel và báo cáo tuần.

Người dùng giữ các quyết định kỹ thuật. AI đo và trình bày hệ quả của bốn lệch hợp đồng với tầng lưu trữ (cửa sổ 1..365, `commitCount`, churn không giữ được phân biệt null/0, `truncated` so với `RESOURCE_LIMIT`) nhưng **không tự đổi** hành vi merge lineage đang được ghim trong test, vì design doc ghi khác. Chưa có đồng thuận nhóm cho năm điểm còn mở ghi ở [tuần 5](../week-05/README.md).

Kiểm chứng AI tự chạy và trích số: `mvn test` cho 12/12 ca mô-đun Git; trên cây sau merge `main` `0b664d9` có 71 ca thì 28 ca đạt (không cần database) và 43 ca lỗi vì Spring context không tải được do không kết nối được PostgreSQL (Docker Engine trên máy hỏng, trùng nguyên trạng tuần 5); `python -m unittest` cho 8/8 ca công cụ đánh giá của Toản; `GitIngestionSample` trên ba repository công khai. AI không tạo nhãn thực nghiệm, không gọi LLM provider, không deploy, không ghi nhận kết quả chưa chạy. Người dùng cần tự đọc lại mã, tự chạy lệnh và giải thích từng con số khi trình bày; GVHD chưa nghiệm thu bộ này.
