# API/UI tuần 8

Các endpoint phân tích/monitoring tuần 6–7 giữ nguyên. UI gọi pagination `page`, `size=20`, hiển thị hasNext và tách trạng thái lỗi/loading/empty. Biểu đồ và heatmap chỉ biểu diễn trang đang xem. Không tự lọc mất file null, không nối trend khác configVersion/configHash/window duration hoặc coverage không đầy đủ.

## AI preview và consent

Cả hai endpoint mặc định tắt, bật bằng `APP_AI_REVIEW_ENABLED=true` trong môi trường kiểm soát. Không có fake provider trong runtime.

`POST /api/snapshots/{snapshot}/files/{file}/ai-review-preview`

```json
{"startLine":1,"endLine":20}
```

Server xác thực quan hệ snapshot/file qua DB, kiểm tra repository còn công khai bằng HEAD không credentials/redirect, nạp blob trong Git cache theo **headSha của snapshot**, đọc tối đa 200 dòng/8 KiB source, file blob tối đa 2 MiB. Không nhận path, SHA, metric, source hoặc prompt từ client.

Trả `{contextHash, context, providerAvailable}`. Context đúng payload canonical engine sẽ gửi, gồm snapshot/file/HEAD/config/window/metrics/source excerpt/evidence E1–E2; `diff=null` vì v1 này chưa chọn diff. Preview không gọi provider. Metadata dưới `context` cũng hiện trong bản xem trước, không thêm dữ liệu ẩn sau xác nhận.

`POST /api/snapshots/{snapshot}/files/{file}/ai-reviews`

```json
{"startLine":1,"endLine":20,"consent":true,"contextHash":"hash nhận từ preview"}
```

Server dựng lại context từ dữ liệu bất biến; hash thay đổi →409 và yêu cầu xem lại. Consent boolean/hash chưa có →403. Không dùng client tự khai metric làm căn cứ. Receipt có thời điểm xác nhận do server tạo.

Thành công 200: `{reviewId,snapshotId,fileId,generatedAt,aiGenerated,result,metadata}`. `result` đúng 5 trường schema của Tưởng; metadata chứa version/hash/provider/model/tokens nếu có. V4 chỉ lưu metadata; source/prompt body/output không lưu DB.

## Thời gian, quota và lỗi

Deadline tổng tối đa 35 giây (context/public check gồm trong tác vụ); frontend dành 40 giây, nginx proxy 40 giây. Executor riêng tối đa 2 tác vụ, không queue; provider không chịu interrupt vẫn chiếm slot đến lúc kết thúc. Engine giữ quota 1/repo, 2/server, 5/phút. Input budget 16384, output reserve 2048, output cap 16384 bytes là giới hạn tích hợp hiện tại, phải kiểm tra tương thích model/provider trước release. Provider adapter vẫn phải tự giới hạn HTTP response trong lúc đọc, không chỉ sau khi đã tải.

| HTTP | Code chính |
|---|---|
| 400 | INVALID_SELECTION, INSUFFICIENT_CONTEXT |
| 403 | AI_REVIEW_DISABLED, CONSENT_REQUIRED, SECRET_DETECTED, PUBLIC_CHECK_FAILED |
| 404 | RESOURCE_NOT_FOUND |
| 409 | CONTEXT_CHANGED_PREVIEW_AGAIN |
| 413 | CONTEXT_TOO_LARGE |
| 429 | RATE_LIMITED |
| 502 | PROVIDER_FAILED, INVALID_OUTPUT, INVALID_EVIDENCE |
| 503 | AI_PROVIDER_UNAVAILABLE, SNAPSHOT_SOURCE_UNAVAILABLE, PUBLIC_CHECK_UNAVAILABLE, CANCELLED |
| 504 | DEADLINE_EXCEEDED |

Không có retry provider tự động. Lỗi chỉ trả code an toàn, không trả raw output. Client không lưu lại consent khi thay file/snapshot/dòng. AI không sửa score, alert hay trạng thái analysis job.

## Điểm nối cho Tưởng

Đăng ký bean `LlmReviewService.LLMProviderAdapter` thật; Controller tạo một engine dùng chung để giữ quota. Adapter nhận `RenderedPrompt`/`ReviewBudget`; phải tuân thủ deadline/cancel, response byte cap, model budget và chính sách dữ liệu. Chưa chọn hoặc gọi nhà cung cấp thay Tưởng. Không bật AI công khai khi chưa có authorization: CORS không thay quyền truy cập.
