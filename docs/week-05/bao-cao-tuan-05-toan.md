# Báo cáo tiến độ tuần 5

**Sinh viên:** Trần Quang Toản — 23110158. **Đề tài:** Git Health Monitor. **Tuần:** 11–17/10/2026. **Ngày lập:** 15/10/2026. **Tình trạng:** sản phẩm thiết kế sẵn sàng review, chưa nghiệm thu.

## 1. Mục tiêu theo phân công

Task 17: thiết kế ERD và đặc tả 10 bảng cùng index. Task 18: sơ đồ điều hướng, giao diện chính/AI review, hình thức trực quan hóa từng chỉ số. Task 20 làm chung với Tưởng trong tuần 5–6: thiết kế temporal split, nhãn bug-fix, baseline và chỉ số đánh giá. Nguồn và dòng Excel tại [đối chiếu phân công](assignment-check.md).

## 2. Kết quả chuẩn bị được

| Mảng | Công việc và kết quả | Minh chứng |
|---|---|---|
| Backend/database — thiết kế | Đặc tả đủ 10 bảng, khóa chính/ngoại, uniqueness, partial index, snapshot identity, file rename identity; hướng nâng cấp additive từ V1 | database-design.md, erd.mmd, schema-reference.sql |
| Kiểm chứng dữ liệu | Chạy SQL trên PostgreSQL 18.3; 12 nhóm kiểm tra đạt, có case âm; rollback schema thử nghiệm | evidence/schema-validation.txt, verify-schema.sql |
| Frontend — thiết kế | Bốn wireframe: tổng quan, hotspot, file detail/AI, giám sát/cảnh báo; desktop/mobile; có trạng thái tải/rỗng/lỗi | wireframes/index.html, 9 ảnh trong evidence |
| Nghiệp vụ | Ánh xạ UC02–11, evidence và consent AI, HHI không đánh giá cá nhân, missing metric không thay bằng 0, alert có lý do | wireframes/README.md, database-design.md |
| Nghiên cứu chung | Đề xuất observation 180 ngày/prediction 30 ngày, nhãn có provenance, hai baseline, Precision/Recall/AP/MAP, tránh rò rỉ tương lai | experiment-design.md |
| Quản lý và báo cáo | Đối chiếu workbook tại main 8d55040; tách nhánh tuần 5; lưu minh chứng và nhật ký AI riêng | assignment-check.md, ai-usage-toan-week05.md |

Không ghi nhận đã xây thêm API/worker/pipeline metric/LLM trong tuần này. SQL tham chiếu không phải migration đã deploy. Dữ liệu wireframe hoàn toàn minh họa, không phải kết quả khai thác Git thực tế.

## 3. Kiến thức và cách áp dụng được thể hiện trong sản phẩm

- Khóa ngoại ghép theo repository giúp ngăn ghép dữ liệu file/contributor khác kho. Unique có xử lý NULL giúp loại cảnh báo trùng ngay cả khi không có related file.
- Snapshot cần gắn SHA, cửa sổ và cấu hình để tái lập kết quả; cùng HEAD chưa đủ kết luận NO_CHANGE. Snapshot bất biến khác với job là lần thực thi.
- Hình thức trực quan hóa cần đi kèm số và bằng chứng: coupling phải có shared/count A/count B, score không phải xác suất có bug, contribution không đo năng lực.
- Temporal split tách dữ liệu làm feature với giai đoạn quan sát bug-fix; so baseline trên cùng eligible files tránh kết luận thiên lệch.

Đây là nội dung được nghiên cứu và thể hiện trong bộ tài liệu với hỗ trợ AI; chưa thay cho xác nhận cá nhân đã đọc hiểu. Toản cần giải thích lại các quyết định khi báo cáo, không lấy phần này làm bằng chứng đã tự thực hiện mọi thao tác.

## 4. Kiểm tra và giới hạn

SQL: tạo 10 bảng thành công, công thức coupling đúng fixture, chặn pair sai/trùng active job/metric âm/FK chéo repo và snapshot/cảnh báo trùng. Có 12 thông báo PASS và rollback thành công. Playwright: 18 kiểm tra wireframe đạt, không lỗi JavaScript hoặc HTTP request; 8 ảnh màn hình theo hai kích thước và 1 ảnh lỗi. Đây là kiểm tra thiết kế/offline, không phải test backend/frontend sản phẩm hay hiệu năng 10.000 commit.

Kiến trúc reconcile của Tưởng ở nhánh 1111a2e chưa merge vào main khi kiểm tra. Các điểm tiếp nhận được ghi là đề xuất: lease/fencing cần kiểm thử service/concurrency sau này; cấu trúc JSONB evidence và ngưỡng alert cần chốt. Chưa thực hiện benchmark, gán nhãn dữ liệu, khảo sát SUS hay nghiệm thu với người dùng trong tuần này.

## 5. Nội dung cần nhóm trưởng review

1. Duyệt 10 bảng và khả năng lưu commit evidence trong JSONB; có cần mở rộng facts tables không.
2. Chốt snapshot identity, publication transaction, NO_CHANGE, lease/fencing theo kiến trúc cuối cùng.
3. Review bốn màn hình, cách chọn/preview context AI và hình thức metric visualization.
4. Chốt repo/cutoff/nhãn/baseline với Tưởng để triển khai thực nghiệm tuần 6; không điều chỉnh theo test results.

## 6. Sử dụng AI và trách nhiệm

AI hỗ trợ đọc/đối chiếu tài liệu, soạn thiết kế, dựng wireframe, tạo script và chạy kiểm tra trong phiên làm việc. Log chia từng mục tại [ai-usage-toan-week05.md](ai-usage-toan-week05.md). Các quyết định chưa được người phụ trách xác nhận vẫn ghi chờ review. Không có kết quả phỏng vấn, giờ công, approval, dữ liệu thực nghiệm hay commit giả được bổ sung.
