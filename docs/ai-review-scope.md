# Phạm vi AI review — bản đề xuất tuần 4

Ngày 08/10/2026. Toản soạn để review chung với Tưởng. **Đã đặc tả để bàn giao; chưa ghi nhận nhóm/GVHD phê duyệt, chưa cài đặt.** Không nhận đã chọn provider hoặc chạy PoC.

- Chỉ gọi theo yêu cầu sau khi có snapshot thành công. Không nằm trong đường tính metric/hotspot; không dùng output LLM trong nhãn hoặc score thực nghiệm RQ1.
- Đầu vào: metric có nguồn, commit/snapshot id, tóm tắt thay đổi và phần source công khai người dùng chọn. Chặn secret và giới hạn kích thước context qua cấu hình; budget đề xuất nằm dưới đây và cần xác nhận sau PoC.
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

Provider timeout đề xuất tối đa 30 giây, deadline toàn API 35 giây từ lúc nhận (gồm xác minh public, đọc source, secret scan, provider và validation); mỗi bước dùng budget còn lại, không cộng 30 giây nếu deadline đã hết; 1 request đang chạy/repo, 2 toàn server, 5 lượt/phút/operator trong thử nghiệm kiểm soát. Nếu chưa có danh tính đáng tin, dùng quota toàn server, không nhận operatorId tùy ý từ client.

Không auto-retry. 429 quá quota, 504 timeout, 502 provider/schema/evidence sai. Timeout hủy request và giải phóng slot khi tác vụ thực sự dừng; nếu upstream chưa dừng được thì vẫn giữ giới hạn in-flight, không mở thêm call vượt quota; không giữ transaction/khóa pipeline. Dashboard/metric/scheduler không bị chặn bởi lỗi AI.

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

## Căn chỉnh với kiến trúc và frontend ngày 09/10/2026

Bản review chung dùng POST đồng bộ trả 200 theo contract, không thêm GET reviewId hoặc output cache server. reviewId để truy vết response/log metadata, không phải tài nguyên đọc lại. Reload cần consent và gọi lại qua quota; mất kết nối không tự retry.

Frontend hiện dùng timeout chung 15 giây trong frontend/src/api.ts. Khi cài AI phải có request budget riêng 40 giây, xử lý timeout/cancel; giữ timeout hiện tại cho API thường. Backend 35 giây/provider 30 giây là đề xuất; kiểm tra proxy/host trước triển khai. Nếu không phù hợp thì mở quyết định async/retention mới, không âm thầm đổi 200 sang 202.

Dùng schema duy nhất trong schemas/: có limitations; checklist/suggestions chứa text/evidenceIds, không dùng mảng chuỗi của bản Tưởng trước. Giữ budget 32 KiB/quota của Toản để review chung; thay bộ 24 KiB/3 lượt phút/20 lượt ngày của bản Tưởng trước. Đây là căn chỉnh văn bản, chưa ghi nhận đồng thuận thật hoặc implementation.

Không lưu raw output ở server: buffer trong RAM chỉ sống trong request, không tạo bảng kết quả review. Output byte/token cap phải chốt theo model; schema không thay cap transport. Kiểm tra HTTP/APM logging không lưu request/response body chứa source/output.
