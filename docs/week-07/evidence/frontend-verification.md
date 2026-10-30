# Kiểm tra frontend sau bản vá dependency

Ngày 29/10/2026. CI lần đầu của commit 74806a7 đạt backend, experiments, secret-scan và frontend lint/build nhưng dừng ở dependency audit.

- Advisory: https://github.com/advisories/GHSA-mrhx-c7xc-m3w5, npm audit báo mức high trên zod 4.6.5.
- Dependency gián tiếp của eslint-plugin-react-hooks/zod-validation-error, thuộc công cụ phát triển.
- Chỉ cập nhật zod lên 4.6.7 trong package-lock.json, giữ package.json và mã giao diện.
- npm ci: thành công; npm audit: 0 vulnerabilities ([JSON](frontend-audit.json)).
- npm run lint và npm run build: thành công.
- npm run test:e2e: 3 passed, 1 skipped. Ca live React→API→PostgreSQL không được bật trong lần chạy này; không báo ca bỏ qua là đạt.

Đây là kiểm tra hồi quy giao diện hiện có, không phải kiểm thử tích hợp các API tuần 7 vào frontend.
