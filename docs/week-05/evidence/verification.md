# Biên bản kiểm tra tuần 5

Thực hiện ngày 15/10/2026. Nền main 8d550409eff04f592c5f5d39182ed35a3b0bb5d7. Chỉ kiểm chứng sản phẩm thiết kế trong thư mục tuần 5, không nhận đã tích hợp pipeline hoặc deploy staging.

| Hạng mục | Kết quả | Bằng chứng |
|---|---|---|
| Phân công | Task 17,18 tuần 5 của Toản; task 20 chung tuần 5–6 | [Trích workbook](assignment-source.json) |
| SQL | PostgreSQL 18.3 tạo đủ 10 bảng; 12 nhóm kiểm tra PASS | [Log](schema-validation.txt), [script](verify-schema.sql) |
| Cô lập dữ liệu | Schema week05_design_review trong transaction; ROLLBACK; review_schema_removed=t | Cuối log SQL |
| Wireframe | 18 kiểm tra đạt; 0 lỗi JS; 0 HTTP request | [Kết quả](wireframe-results.json), [script](verify-wireframes.cjs) |
| Responsive | 4 màn hình × 2 viewport không tràn ngang trang | 8 ảnh dưới đây |
| Visual review | Đã xem ảnh tổng quan desktop, file mobile và ảnh schema; đọc được, không cắt nội dung | dashboard-1440.png, file-375.png, ../erd.png |

## Ảnh minh chứng

| Màn hình | Desktop 1440 px | Mobile 375 px |
|---|---|---|
| Tổng quan | [Ảnh](dashboard-1440.png) | [Ảnh](dashboard-375.png) |
| Hotspot | [Ảnh](hotspots-1440.png) | [Ảnh](hotspots-375.png) |
| Chi tiết/AI | [Ảnh](file-1440.png) | [Ảnh](file-375.png) |
| Cảnh báo | [Ảnh](alerts-1440.png) | [Ảnh](alerts-375.png) |

Trạng thái lỗi: [error-state.png](error-state.png). Mọi con số trên ảnh là minh họa.

## Tái lập

- Database thử nghiệm PostgreSQL >=15: chạy `psql -v ON_ERROR_STOP=1 -d <review_database> -f docs/week-05/evidence/verify-schema.sql`. Role cần CREATE schema. Không chạy DDL trực tiếp vào DB thật.
- UI: cài dependencies frontend và browser Playwright; từ root chạy `node docs/week-05/evidence/verify-wireframes.cjs`. Nếu dùng browser cache ngoài mặc định, đặt PLAYWRIGHT_BROWSERS_PATH phù hợp. Script ghi lại ảnh/JSON kiểm tra.
- Test SQL xác nhận constraints và fixture; không đo concurrency, tốc độ index, worker, rename detection hoặc pipeline thật. UI test chỉ xác nhận hành vi mô phỏng.

## Nguồn thiết kế đã kiểm tra

- [PostgreSQL constraints](https://www.postgresql.org/docs/18/ddl-constraints.html): áp dụng PK/FK/CHECK và UNIQUE NULLS NOT DISTINCT cho dedupe.
- [PostgreSQL partial indexes](https://www.postgresql.org/docs/18/indexes-partial.html): áp dụng unique cho các job QUEUED/RUNNING và index hàng cần quét.
- Git/Stanford và phương pháp thực nghiệm được ghi tại [experiment-design.md](../experiment-design.md). Ngày truy cập: 15/10/2026.
