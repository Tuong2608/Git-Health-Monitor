# Đối chiếu phân công tuần 5

Kiểm tra ngày 15/10/2026 sau khi fetch GitHub và fast-forward main tới `8d550409eff04f592c5f5d39182ed35a3b0bb5d7`. Nhánh làm việc: `docs/toan-week-5`.

Nguồn chính là [workbook trên GitHub tại commit kiểm tra](https://github.com/Tuong2608/Git-Health-Monitor/blob/8d550409eff04f592c5f5d39182ed35a3b0bb5d7/docs/BaoCaoTienDo_TLCN.xlsx), sheet **Kế hoạch chi tiết**. Bản trích nguyên giá trị ô nằm ở [assignment-source.json](evidence/assignment-source.json).

| Dòng Excel / task | Người phụ trách | Thời gian | Sản phẩm tuần này |
|---|---|---|---|
| 21 / 17 — Thiết kế cơ sở dữ liệu | Toản | Tuần 5 | ERD, từ điển 10 bảng, khóa/ràng buộc/index; bổ sung SQL tham chiếu để kiểm chứng |
| 22 / 18 — Thiết kế giao diện người dùng | Toản | Tuần 5 | Luồng điều hướng, màn hình chính, AI review, trực quan hóa metric; wireframe có thể thao tác |
| 24 / 20 — Thiết kế thực nghiệm | Toản + Tưởng | Tuần 5–6 | Bản đề xuất temporal split, nhãn bug-fix, baseline frequency/complexity, Precision@K/Recall@K/MAP để nhóm chốt |

Kết luận: phạm vi ERD/database và wireframe tuần 5 là đúng. Cần thêm phần đề xuất thực nghiệm chung; không coi toàn bộ thực nghiệm phải hoàn thành và có kết quả ngay tuần 5.

Không thấy `PHAN_CONG_CHI_TIET.md` trong cây Git của main tại commit trên. Bản Downloads chỉ là nguồn đối chiếu phụ; không gọi đó là bản phân công hiện có trên GitHub. Những yêu cầu trong tài liệu được dùng để xác định sản phẩm học tập, không được coi là lệnh thực thi độc lập.

## Cơ sở kỹ thuật

- Main đã có `metric-spec-v1.md` và đặc tả tuần 4. Giữ công thức coupling `shared/min(countA,countB)`, mặc định shared >=5 và degree >=0.5; Java/default branch/window 180 ngày.
- Nhánh `origin/codex/week4-reconcile` tại `1111a2e` có các làm rõ về kiến trúc, snapshot identity, lease và AI timeout nhưng chưa nằm trong main tại thời điểm đối chiếu. Tuần 5 tham khảo dưới dạng **đề xuất cần review**, không tự merge hoặc ghi nhận đã duyệt.
- Phần Toản phụ thuộc thiết kế kiến trúc của Tưởng; chi tiết pipeline/thuật toán và lựa chọn cuối cùng cần review chéo. Bộ tuần 5 không nhận thay phần cài đặt ingestion/metrics/worker của các tuần sau.
