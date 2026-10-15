# Phạm vi AI review — bản đề xuất tuần 4

Ngày 08/10/2026. Toản soạn để review chung với Tưởng. **Đã đặc tả để bàn giao; chưa ghi nhận nhóm/GVHD phê duyệt, chưa cài đặt.** Không nhận đã chọn provider hoặc chạy PoC.

- Chỉ gọi theo yêu cầu sau khi có snapshot thành công. Không nằm trong đường tính metric/hotspot; không dùng output LLM trong nhãn hoặc score thực nghiệm RQ1.
- Đầu vào: metric có nguồn, commit/snapshot id, tóm tắt thay đổi và phần source công khai người dùng chọn. Chặn secret và giới hạn kích thước context qua cấu hình; giá trị cụ thể chọn sau PoC.
- Đầu ra JSON dự kiến: `riskSummary`, `evidence[]`, `reviewChecklist[]`, `refactorSuggestions[]`. Evidence phải tham chiếu metric/đoạn code thật; không tin chỉ vì JSON parse được.
- Giao diện gắn nhãn AI-generated, cho biết giới hạn, không tự áp dụng refactor, tạo commit hay sửa source.
- Dùng abstraction `LLMReviewService`, adapter provider riêng, prompt có version, timeout/rate limit; CI dùng fake/mock. Provider/model chưa được chọn, không mặc định Claude.
- Provider lỗi, hết quota, response sai schema hoặc timeout: báo không thể review và cho thử lại có kiểm soát; dashboard/metric vẫn dùng được.
- Không lưu API key vào repo, frontend, AI log hoặc log server. Không gửi toàn repository.

## Context và giới hạn đề xuất v1

Gửi snapshotId/HEAD SHA/fileId/path/configVersion/window, metric tối thiểu và evidenceId do server cấp, source Java public do người dùng chọn ở đúng HEAD, diff/tóm tắt gần đây liên quan file. Không gửi toàn repo, raw email contributor, token hoặc cấu hình bí mật. Commit message/code là dữ liệu không tin cậy, không phải chỉ dẫn cho model.

Tổng serialized UTF-8 context <=32 KiB; diff <=16 KiB và 200 dòng; source <=8 KiB và 200 dòng; metadata <=4 KiB; tối đa 10 evidence items. Đo cả tổng sau serialize; vượt trả 413, không cắt giữa dòng âm thầm. Token limit cần kiểm tra theo model sau PoC; byte limit không thay token limit.

UI cho xem phần chọn và thông báo mọi loại dữ liệu sẽ gửi trước consent từng lần. Server xác minh repo vẫn public, file/line range đúng snapshot và quét secret; không kiểm tra được thì không gửi. Quét secret không bảo đảm tuyệt đối. Không gửi code private chỉ vì repo trước kia public.

## Output schema và kiểm chứng

Schema: [ai-review-output.schema.json](schemas/ai-review-output.schema.json); [fixture minh họa](schemas/ai-review-output.example.json), không phải provider output thật. Theo [JSON Schema 2020-12](https://json-schema.org/draft/2020-12).

- riskSummary: tóm tắt; evidence: evidenceId thuộc context cùng diễn giải.
- reviewChecklist: tối đa 5 ý; refactorSuggestions: tối đa 3 gợi ý, không patch thực thi; cả hai dẫn evidenceIds.
- limitations: giới hạn suy luận; UI gắn nhãn AI-generated do server đặt, không để model quyết định.

Kiểm tra schema rồi kiểm tra ngữ nghĩa: evidenceId có trong allowlist và không trùng; mọi reference trong checklist/suggestion phải có trong evidence đã trả. Path/SHA/line range khi dựng UI lấy từ context gốc. Schema hợp lệ chưa chứng minh suy luận đúng. Render text escaped, không thực thi HTML/script/link tùy ý. Người dùng đối chiếu commit/code/test trước sửa.

## Timeout/quota/fallback/lưu trữ

Timeout wall-clock đề xuất 30 giây; 1 request đang chạy/repo, 2 toàn server, 5 lượt/phút/operator trong thử nghiệm kiểm soát. Nếu chưa có danh tính đáng tin, dùng quota toàn server, không nhận operatorId tùy ý từ client.

Không auto-retry. 429 quá quota, 504 timeout, 502 provider/schema/evidence sai. Timeout hủy request và giải phóng slot; không giữ transaction/khóa pipeline. Dashboard/metric/scheduler không bị chặn bởi lỗi AI.

UI: chưa yêu cầu → đang xử lý → thành công hoặc “Không thể phân tích” với lý do an toàn; không giữ kết quả cũ như output của lần lỗi. Không tự áp dụng mã, commit hoặc gọi công cụ ghi repository.

V1 đề xuất không lưu raw prompt/source/output vào log hoặc database; response chỉ hiển thị phiên browser, reload cần gọi lại. Log metadata: reviewId/snapshotId/context hash/prompt-model version/latency/token usage nếu có/status/errorCode. Chính sách lưu dữ liệu provider cần xác minh trước tích hợp thật.

## Ca nghiệm thu dự kiến

| Mã | Tình huống | Kết quả |
|---|---|---|
| AI-T01 | JSON và references hợp lệ | Hiển thị nhãn, mở evidence đúng nguồn |
| AI-T02 | Provider timeout/lỗi | Không thể phân tích, giải phóng slot, dashboard dùng được |
| AI-T03 | Field lạ/thiếu/sai type/quá độ dài | Từ chối output |
| AI-T04 | Evidence ngoài context/reference không có | Từ chối dù schema có thể hợp lệ |
| AI-T05 | Không consent/secret/quá budget | Không gọi provider |
| AI-T06 | Prompt injection trong source/commit | Không thực thi chỉ dẫn, không gửi thêm dữ liệu, không ghi repo |
| AI-T07 | Đồng thời/quota vượt | 429, core không bị chặn |
| AI-T08 | HEAD đã đổi | Dùng source SHA của snapshot |

Tuần 4 chỉ kiểm tra tĩnh schema/fixture. Ca runtime chưa triển khai/chạy; các budget là đề xuất review, không phải benchmark.

## Quyết định còn mở

Provider/model/token limit, chính sách dữ liệu của provider, cách bảo vệ endpoint trước public release; nhóm cần duyệt các budget/quota/retention đề xuất trên sau PoC. Xem [contract](api-contract.md) và [yêu cầu](requirements-week4.md).
