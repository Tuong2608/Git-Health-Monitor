# Nội dung Pull Request Tuần 9 — Trần Văn Tưởng (23110170)

## Tóm tắt thay đổi
- **Chốt thiết kế thực nghiệm (`STT 20`) & tập dữ liệu (`STT 29`):** Ban hành `docs/week-09/rq1-protocol.md`, `docs/week-09/rq1-dataset-proposal.json` và `docs/week-09/evidence/rq1-frozen-manifest.json` cố định 5 kho mã nguồn Java OSS chọn mù (`commons-csv`, `commons-codec`, `commons-dbutils`, `commons-validator`, `javapoet`).
- **Thực nghiệm xếp hạng rủi ro RQ1 (`STT 30`):**
  - Bổ sung `tools/experiments/run_rq1.py`, `tools/experiments/summarize_rq1.py` và 5 unit test trong `tools/experiments/test_rq1_runner.py` (tổng cộng `22/22` test Python thực nghiệm PASS).
  - Rà soát thủ công `141` commit (`101` ứng viên Jira/từ khóa + `40` mẫu âm `seed=23110170`) trong `docs/week-09/rq1-commit-reviews.json`.
  - Xuất đầy đủ dữ liệu và bảng kết quả RQ1 tại `docs/week-09/evidence/rq1/`, kèm bằng chứng chạy lại độc lập khớp SHA-256 `13/13` file JSON (`docs/week-09/evidence/rq1-replay-verification.json`).
- **Tổng hợp báo cáo & bảng tiến độ Tuần 9:** Cập nhật `docs/BaoCaoTienDo_TLCN.xlsx`, `docs/week-09/bao-cao-tuan-09-nhom.md` và `docs/week-09/noi-dung-nop-web-tuan-09.md`.
