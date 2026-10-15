# Business rules và NFR — tuần 4

Ngày 08/10/2026. Phụ trách Toản. Bản yêu cầu để review; mọi con số dưới đây là tiêu chí/đề xuất, không phải kết quả đo. Mã BR01–13 và NFR01–10 giữ theo đề cương. Metric theo [v1](metric-spec-v1.md), không dùng ví dụ tùy ý của file phân công làm cam kết mới.

## 1. Business rules

| Mã | Quy tắc | Điều kiện kiểm chứng |
|---|---|---|
| BR01 | Chỉ GitHub HTTPS public; chuẩn hóa URL và chống trùng | Private/local/protocol khác bị từ chối; network fail phân biệt repo không tồn tại. Prototype mới có syntax validation |
| BR02 | Chỉ default branch; cố định HEAD SHA khi worker bắt đầu | Metric và source AI đều truy về SHA đó, không trộn HEAD mới |
| BR03 | Bỏ merge commit trong frequency/churn/coupling/contribution | Regression history có merge không đếm hai lần |
| BR04 | Metric source-code chỉ Java hợp lệ; loại binary/generated/path theo cấu hình | Lưu exclude rules và version; file bị loại có lý do |
| BR05 | Chỉ công bố snapshot sau toàn bộ pipeline thành công | Failure giữa chừng không để UI đọc snapshot dở dang |
| BR06 | Không tạo snapshot lặp khi HEAD và cấu hình hiệu lực không đổi | Job trả SUCCEEDED/NO_CHANGE và snapshotId cũ. Ngoại lệ đổi cấu hình cần quyết định bên dưới |
| BR07 | Cùng repo/HEAD/cửa sổ/config/tool version → cùng metric | So output chuẩn hóa, bỏ id/timestamp; công thức percentile cần đặc tả tuần 5 |
| BR08 | Rename nối logical file khi đủ bằng chứng Git | Giữ path tại từng snapshot; không tự ghép file chỉ vì tên giống |
| BR09 | Ngưỡng là heuristic có version, không chuẩn chất lượng phổ quát | UI hiện ngưỡng và observedValue; HHI chưa có hard threshold |
| BR10 | Hotspot để ưu tiên điều tra, không xác nhận bug/nợ kỹ thuật | Nội dung UI và AI không kết luận chắc chắn từ score |
| BR11 | AI chỉ theo yêu cầu sau snapshot thành công | Không gọi AI khi chạy metric hoặc scheduler |
| BR12 | AI chỉ tư vấn; không auto-apply/commit | Không cấp công cụ ghi repo cho model; đầu ra không chạy như lệnh |
| BR13 | Chỉ gửi context public được chọn; secret ở environment; timeout/quota | Consent, giới hạn context, output validation và fallback theo AI scope |

## 2. Quy tắc bổ sung cần review

| Mã | Đề xuất cụ thể | Lý do / kiểm tra |
|---|---|---|
| BR14 | Coupling dùng shared/min, shared>=5 và score>=0,50. Loại changeset >30 file khỏi **cả tập mẫu số và tử số** coupling | Đề cương đã nêu loại changeset lớn nhưng metric v1 chưa định nghĩa phạm vi đếm. Đề xuất đếm file source sau exclusions, logical rename tính một; cần Tưởng duyệt trước code/test |
| BR15 | Tối đa một job QUEUED/RUNNING mỗi repo; giới hạn worker 2 và queue toàn hệ thống 20 | Đề xuất cho prototype; test request đồng thời/queue đầy; không coi cấu hình này bảo đảm hiệu năng |
| BR16 | Window mặc định 180 ngày; chọn tối đa 10.000 non-merge commit gần nhất trong window; ghi effective bounds, count và truncated | Không từ chối toàn bộ repo lớn; full clone vẫn có thể tải lịch sử lớn hơn giới hạn phân tích |
| BR17 | Config thay đổi dù HEAD giữ nguyên được phép tạo snapshot riêng | Làm rõ BR06 để không tái dùng sai metric. So sánh chỉ cùng metric/config tương thích; log lý do điều chỉnh so với câu BR06 cũ chỉ nói HEAD |
| BR18 | Alert duy nhất theo ruleVersion + snapshotId + logicalFile-or-pair; thay trạng thái không xóa evidence | Đánh giá lại không spam; alert CLOSED không mở lại ngầm |

Không tự đặt rule “file >=3 commit mới tính hotspot”: đề cương/metric v1 chưa yêu cầu. Trường hợp 0 contribution, không có function, percentile ties, root commit và thứ tự chọn commit cần Tưởng đặc tả rõ tuần 5. Khi chưa đủ định nghĩa, chưa công bố metric đó là đạt reproducibility.

## 3. NFR định lượng và cách nghiệm thu

| Mã | Chỉ tiêu | Cách đo/điều kiện | Trạng thái |
|---|---|---|---|
| NFR01 | API tạo analysis job p95 <=2 giây | 100 request hợp lệ sau 10 warm-up, 5 client; đo response 202, không đo clone; queue không đầy, ghi lỗi riêng | Chưa có endpoint/pilot |
| NFR02 | Dashboard đã có dữ liệu p95 <=1 giây | 100 GET, 5 client, snapshot cố định; ghi kích thước dataset/cache, tỷ lệ lỗi; không tính dữ liệu giả là benchmark | Chưa đo |
| NFR03 | Repo benchmark <=10.000 commit, tổng phân tích <=10 phút | 3 lượt cold clone và 3 warm fetch trên repo/HEAD cố định; báo từng lượt và tổng clone+extract+Lizard+metric+persist, CPU/RAM/OS/network/tool versions; đạt nếu mọi lượt đúng và <=600s | Chưa có pipeline đầy đủ; không thay bằng ví dụ 5 phút |
| NFR04 | Runtime deadline đề xuất 15 phút; lease/heartbeat 30 giây, stale sau 120 giây, phát hiện trong <=180 giây | Kill worker và giả timeout, xác minh FAILED/no snapshot; deadline không thay target 10 phút. Thời gian queue ghi riêng | Chờ nhóm duyệt giá trị và thử fault injection |
| NFR05 | 100% secret cấu hình qua env/secret store; 0 secret phát hiện trong repo/log fixture | Secret scan + kiểm tra negative fixture đã khử dữ liệu thật; scan sạch không chứng minh mọi secret đã bị phát hiện | Chưa nghiệm thu toàn hệ thống |
| NFR06 | 100% ca URL ngoài whitelist trong bộ kiểm thử bị từ chối trước clone | HTTPS GitHub host chính xác, chặn userinfo/port/protocol/path tùy ý; kiểm tra redirect/SSRF ở ingestion và outbound policy | Prototype mới kiểm tra cú pháp |
| NFR07 | UI dùng được tại 375px và desktop 1440px, không tràn ngang ở luồng core | E2E viewport và kiểm tra loading/empty/error/keyboard; tuần 3 chỉ là prototype | Chưa nghiệm thu UI đầy đủ |
| NFR08 | Core line coverage >=50% cam kết; mục tiêu rubric >=70% | JaCoCo cho ingestion/metric/monitoring/API; công bố exclusions. Không loại code khó chỉ để tăng số | Chưa có coverage report |
| NFR09 | 100% regression fixtures cho cùng input/config tạo metric bằng nhau trong 3 lần chạy | JSON chuẩn hóa, tolerance số thực 1e-9 đề xuất, id/time loại khỏi so sánh; giữ tool versions | Chưa cài metric |
| NFR10 | Mọi job có jobId/status/start/end/errorCode; health check và kiểm tra DB readiness riêng | Truy vết 100% job fixture; DB down không báo readiness tốt; log không lưu raw code/token | `/api/health` hiện chỉ liveness |

Các cấu hình thử tải/timeout/tolerance là **đề xuất đo tuần 4**, chưa có đồng thuận hay benchmark. Ghi môi trường thực tế trước mỗi lần đo, không tự điền máy giả định. Repo lớn hơn benchmark được báo truncated/resource limit; không áp cam kết 10 phút cho dung lượng clone vô hạn.

## 4. KPI giá trị sản phẩm — kế hoạch đo, không phải thành tích

| KPI | Định nghĩa | Mục tiêu đề xuất / cách thu |
|---|---|---|
| KPI01 | Thời gian chọn đúng file cần review | Median giảm >=20% so workflow Git hiện tại, bài tương đương/counterbalanced, cùng người |
| KPI02 | Tỷ lệ tìm đúng file liên quan | >=90% task co-change chuẩn có đáp án, tính số đúng/tổng task |
| KPI03 | Tỷ lệ task người dùng hoàn tất | >=90%, ghi tất cả task thất bại và thời gian |
| KPI04 | Khả năng sử dụng SUS | Trung bình >=80 với >=10 người phù hợp nếu theo mức tối đa rubric; khảo sát năm người hiện tại không thay user study |
| KPI05 | Khả năng truy vết bằng chứng | >=90% task giải thích người dùng mở đúng commit/metric tham chiếu |

Các mục tiêu phải chốt trước thực nghiệm và ghi cả kết quả không đạt. Không dùng thời gian tự ước tính phỏng vấn làm baseline định lượng; không nhận điểm tối đa chỉ vì có bảng mục tiêu.

## 5. Quyết định cần đồng thuận

BR14, BR16, BR17 và NFR04/09 bổ sung chi tiết so với baseline; cần Tưởng review và cập nhật metric spec/algorithm design tương ứng khi chấp nhận. Lịch tuần 4 giữ theo phân công, không đổi lịch workbook hoặc lấy milestone khác trong đề cương làm lý do cài vượt phạm vi. ERD/wireframe thuộc tuần 5.
