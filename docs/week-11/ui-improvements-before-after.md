# Cải tiến Giao diện Người dùng (UI/UX) phục vụ Thực nghiệm RQ3 — Before & After (STT 34)

- **Người thực hiện:** Trần Quang Toản (`23110158`)
- **Ngày thực hiện:** 24/11/2026
- **Tập tin chỉnh sửa:** [AnalysisDashboard.tsx](../../frontend/src/components/AnalysisDashboard.tsx), [App.css](../../frontend/src/App.css), [week8.spec.ts](../../frontend/e2e/week8.spec.ts)
- **Ghi chú trung thực:** Vòng cải tiến này làm **trước** khi thu phiếu RQ3 (dựa trên rà soát 7 tác vụ chuẩn). Rubric TC2.7 yêu cầu cải tiến theo phản hồi người dùng có đo trước–sau, nên sau khi có phiếu thật cần bổ sung ít nhất 1 điều chỉnh theo góp ý Câu 11–12.
- **Mục tiêu:** Giải quyết các điểm dễ gây khựng thao tác hoặc hiểu nhầm khi người tham gia thực hiện 7 tác vụ chuẩn (`T1..T7`) của thực nghiệm người dùng `RQ3` (`STT 33`, `STT 34`).

---

## 1. Bảng đối chiếu 4 cải tiến giao diện Before / After theo tác vụ RQ3

| Tác vụ RQ3 liên quan | Hiện trạng trước cải tiến (Before) | Cải tiến đã thực hiện (After) | Giá trị đối với người dùng |
|---|---|---|---|
| **T2 — Tìm Top 5 tập tin ưu tiên rà soát** | Khu vực *Điểm nóng* hiển thị bản đồ ô màu và bảng dữ liệu nhưng chưa ghi chú rõ quy tắc sắp xếp mặc định và cách xác định nhanh nhóm Top 5. | Bổ sung dòng hướng dẫn ngay đầu khu vực *Điểm nóng*: *"Thứ tự mặc định xếp giảm dần theo Hotspot; 5 dòng đầu là Top 5 file ưu tiên rà soát (T2)"*. | Người dùng xác định được ngay Top 5 tập tin rủi ro cao nhất mà không cần tự suy đoán thứ tự bảng. |
| **T3 — Giải thích lý do rủi ro (`Hotspot`, `Max CCN`, `HHI`)** | Hiển thị 5 con số (`Tần suất`, `Churn`, `Max CCN`, `Hotspot`, `HHI`) và 1 câu lưu ý ngắn về HHI; thiếu công thức giải thích tại chỗ. | Thêm tooltip (`title`) có gạch chân chấm trên từng nhãn chỉ số và hộp mở rộng `<details className="metric-guide">` **Giải thích công thức chỉ số (Hotspot, Max CCN, HHI)** nêu rõ công thức $\sqrt{P_{\text{change}} \times P_{\text{complexity}}}$, ý nghĩa `Max CCN` (Lizard 1.24.3) và $\text{HHI} = \sum p_i^2$. | Giúp người dùng trả lời chính xác câu hỏi *"Tại sao tập tin này có điểm rủi ro cao?"* từ các chỉ số thành phần. |
| **T5 & T6 — Giải thích độ phủ `coverageComplete = false` và ngưỡng Cảnh báo (`Alerts`)** | Khi `coverageComplete = false`, màn hình chỉ báo *"Dữ liệu chưa đầy đủ..."* nhưng không giải thích nguyên nhân kỹ thuật (`LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS`); thẻ cảnh báo chỉ ghi *"Giá trị X · ngưỡng Y"*. | - Bổ sung giải thích rõ: `LINEAGE_AMBIGUOUS` (file qua merge commit chưa rõ nhánh cha) làm `coverageComplete = false` và chặn cảnh báo/so sánh để tránh báo động giả; `NO_FUNCTIONS` (interface/package-info không có hàm) chỉ tách khỏi mẫu số phân vị Hotspot.<br>- Trên thẻ cảnh báo, ghi rõ điều kiện: *"Kích hoạt khi giá trị vượt ngưỡng đã ghim của snapshot"*. | Người dùng phân biệt được ngay vì sao biểu đồ xu hướng ngắt nét hoặc cặp snapshot bị chặn mà không nhầm là lỗi phần mềm. |
| **T7 — Trợ lý AI Review có kiểm soát (Preview & Consent)** | Người dùng thấy ô nhập dòng và nút *Xem trước dữ liệu gửi*, nhưng chưa nhìn thấy quy trình tổng thể 3 bước và mã băm `contextHash` SHA-256 trên giao diện. | - Hiển thị thanh chỉ dẫn **Quy trình 3 bước**: *(1) Chọn dòng (tối đa 200 dòng) & bấm Xem trước dữ liệu gửi → (2) Kiểm tra contextHash SHA-256 & tích đồng thuận Consent → (3) Gửi yêu cầu AI review (chỉ đọc, không tự sửa code)*.<br>- Hiển thị tường minh hộp **Mã xác thực ngữ cảnh (`contextHash` SHA-256)** ngay phía trên khung JSON preview. | Người dùng hiểu rõ cơ chế bảo vệ quyền riêng tư 2 bước (Preview gắn mã băm SHA-256 trước khi Consent) và giới hạn AI không tự động sửa mã. |

---

## 2. Minh chứng hình ảnh Before & After (Desktop 1440px & Mobile 375px)

| Chế độ màn hình | Trước cải tiến (Before) | Sau cải tiến (After) | Kiểm tra chống tràn ngang (`scrollWidth <= innerWidth`) |
|---|---|---|---|
| **Desktop (`1440 × 1000`)** | [ui-before-desktop.png](evidence/ui-before-desktop.png) | [ui-after-desktop.png](evidence/ui-after-desktop.png) | **PASS** |
| **Mobile (`375 × 812`)** | [ui-before-mobile.png](evidence/ui-before-mobile.png) | [ui-after-mobile.png](evidence/ui-after-mobile.png) | **PASS** (`scrollWidth <= 375px`) |

---

## 3. Kiểm thử tự động Playwright E2E bảo vệ cải tiến

Đã bổ sung ca kiểm thử tự động mới trong `frontend/e2e/week8.spec.ts`:
- `week 11 RQ3 UI guidance exposes metric formulas, AI 3-step contextHash and coverage explanation`:
  - Kiểm tra hiển thị thông báo giải thích `LINEAGE_AMBIGUOUS` vs `NO_FUNCTIONS` khi `coverageComplete = false`.
  - Kiểm tra mở hộp `Giải thích công thức chỉ số (Hotspot, Max CCN, HHI)` và hiển thị đúng công thức `Hotspot = √(P_change × P_complexity):`.
  - Kiểm tra hiển thị `Quy trình 3 bước:` và mã băm `contextHash` SHA-256 (`preview-hash`) sau khi bấm `Xem trước dữ liệu gửi`.
  - Kiểm tra bố cục không tràn ngang trên màn hình di động `375 × 812`.
