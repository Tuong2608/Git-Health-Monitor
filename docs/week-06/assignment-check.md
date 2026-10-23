# Đối chiếu phân công tuần 6

Ngày 22/10/2026, fetch GitHub trước khi làm. Main `188968c` đã merge tuần 5 của Toản. Nhánh Tưởng `codex/tuong-week5` ở `0518e52` có monitoring-design, algorithm-design và experiment-design nhưng chưa nằm trong main. Đọc các tài liệu này để tương thích, không tự merge hoặc sửa phần của Tưởng.

Nguồn [workbook GitHub](https://github.com/Tuong2608/Git-Health-Monitor/blob/188968c/docs/BaoCaoTienDo_TLCN.xlsx), sheet Kế hoạch chi tiết; [giá trị ô đã trích](evidence/assignment-source.json).

| Dòng / task | Phụ trách | Thời gian | Xử lý trong bộ này |
|---|---|---|---|
| 27 / 23 — lưu trữ và giám sát | Toản | Tuần 6–7 | Cài snapshot, schedule, durable jobs, bounded executor, threshold alerts và API vận hành |
| 24 / 20 — thiết kế thực nghiệm | Tưởng + Toản | Tuần 5–6 | Đối chiếu hai bản, chọn cấu hình đề xuất, công cụ baseline/P@K/R@K/MAP và template |
| 25 / 21 — thu thập dữ liệu Git | Tưởng | Tuần 6–7 | Chỉ tạo AnalysisPipeline port để nhận kết quả; chưa nhận đã clone/traverse bằng JGit |
| 23 / 19 — LLM review design | Tưởng | Tuần 5–6 | Không cài provider hoặc làm thay phần LLM |

Task 23 kéo dài hai tuần; bộ này hoàn thiện phần module độc lập trong tuần 6. Tích hợp Git thật, kiểm tra staging và tải thực tế còn cần module của Tưởng trong tuần 7. Task 20 vẫn cần hai thành viên xác nhận lựa chọn, không tự ghi đồng thuận.
