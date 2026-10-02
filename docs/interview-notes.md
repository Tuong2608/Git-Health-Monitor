# Báo cáo phỏng vấn khảo sát nhu cầu người dùng

**Đề tài:** Xây dựng hệ thống phân tích và giám sát sức khỏe dự án phần mềm dựa trên lịch sử kho mã nguồn Git.

**Sinh viên phụ trách tổng hợp:** Trần Quang Toản — MSSV 23110158.

**Ngày tổng hợp:** 30/09/2026. Ngày này là ngày lập báo cáo, không thay thế ngày phỏng vấn của từng người.

**Nguồn dữ liệu:** Năm bộ câu trả lời. Ba bộ đầu (S01–S03) do Toản cung cấp: một tệp văn bản đính kèm và hai phần trả lời trong trao đổi bổ sung. Hai bộ sau (S04–S05) do Tưởng thu thập, bổ sung ngày 30/09/2026, dùng cùng bộ câu hỏi 12 mục. Quy ước bộ trả lời trong tệp đính kèm là S01; “người 2” là S02; “người 3” là S03.

## 1. Mục tiêu khảo sát

Khảo sát nhằm tìm hiểu cách người phát triển xác định file cần kiểm tra, truy vết các thay đổi liên quan và tìm người có kiến thức phù hợp khi bảo trì phần mềm. Báo cáo sử dụng phản hồi để xem xét sự phù hợp của các chức năng hotspot, temporal coupling, thông tin người đóng góp, giám sát xu hướng và trợ lý AI review.

Đây là khảo sát nhu cầu trước khi hoàn thiện đặc tả. Người tham gia chưa được ghi nhận là đã sử dụng sản phẩm trong một thực nghiệm có kiểm soát; báo cáo không đánh giá hiệu quả của hệ thống, task success rate hoặc SUS.

## 2. Cơ sở dữ liệu và cách tổng hợp

### 2.1. Phạm vi dữ liệu

Nội dung được tổ chức theo bộ câu hỏi 12 mục của nhóm, gồm đồng ý tham gia, bối cảnh sử dụng Git, tình huống bảo trì, khó khăn, công cụ hiện tại, thay đổi liên quan giữa các file, phân bố tri thức, cảnh báo, khả năng giải thích, kỳ vọng về AI và ưu tiên sử dụng.

Định dạng tiếp nhận là văn bản. Ngày giờ phỏng vấn và kênh thu thập từng trường hợp chưa được cung cấp đầy đủ. S04–S05 do Tưởng thu thập theo bản tổng hợp trong commit `3a3fd30`; thời điểm commit không thay thế ngày phỏng vấn. Các mốc “tuần trước”, “hai tuần trước”, “vài tháng trước” được giữ là mô tả tương đối trong câu trả lời, không quy đổi sang ngày lịch cụ thể.

### 2.2. Cách xử lý

- Gán mã S01–S05; lược bỏ thông tin liên hệ và biểu tượng phản ứng không mang nội dung trả lời.
- Tóm tắt theo từng câu hỏi, giữ các ý kiến khác biệt và phần còn thiếu.
- Nhóm phản hồi thành chủ đề và liên kết với yêu cầu đề xuất bằng mã người và số câu hỏi.
- Phân biệt phát biểu của người tham gia, diễn giải của người tổng hợp và quyết định thiết kế cần nhóm duyệt.

Đây là tổng hợp định tính mô tả từ năm phản hồi, chưa thực hiện mã hóa độc lập giữa nhiều người phân tích hoặc kiểm tra mức độ đồng thuận giữa người mã hóa.

### 2.3. Đồng ý tham gia và bảo mật

Cả năm bộ trả lời đều ghi nhận đồng ý sử dụng nội dung đã ẩn danh trong báo cáo (câu 1). S03 nêu rõ không đồng ý ghi âm; các trường hợp còn lại chưa có thông tin về đồng ý ghi âm. Báo cáo không sử dụng hoặc giả định có dữ liệu ghi âm.

Địa chỉ liên hệ được cung cấp bởi S03 không được đưa vào tài liệu công khai. Nếu cần mời thử nghiệm, thông tin liên hệ được quản lý riêng bởi người phụ trách, không lưu trong repository.

## 3. Đặc điểm người tham gia

| Mã | Vai trò tự khai | Kinh nghiệm Git tự khai | Quy mô nhóm | Mức độ đầy đủ |
|---|---|---|---|---|
| S01 | Sinh viên Kỹ thuật Phần mềm; Full-stack/Mobile Developer trong đồ án và dự án nhóm | Khoảng 2–3 năm | 3–5 người | Có câu trả lời 1–12 |
| S02 | Backend và Frontend Developer; có kinh nghiệm từ đồ án đến công việc thực tế | Khoảng 3 năm | 3–5 người | Có câu trả lời 1–9; chưa có 10–12 |
| S03 | Sinh viên năm 4; developer, đôi khi phụ trách merge code | Khoảng 2 năm, bản trả lời ghi `[2]` | 2–5 người, bản trả lời ghi `[2-5]` | Có câu trả lời 1–12; cần xác nhận các số đặt trong ngoặc vuông |
| S04 | Sinh viên năm cuối; developer trong đồ án môn học | Khoảng 3 năm | 3–5 người | Có câu trả lời 1–12 |
| S05 | Developer | Khoảng 2 năm | 5 người | Có câu trả lời 1–12; nhiều câu trả lời rất ngắn, một câu (6) không trả lời rõ nội dung |

Các hồ sơ tự khai đều mô tả kinh nghiệm dùng Git trong dự án nhóm, phù hợp để khảo sát sơ bộ nhu cầu developer. Thông tin này chưa cho phép kết luận mẫu đại diện cho toàn bộ developer, maintainer hoặc technical lead.

## 4. Tổng hợp từng trường hợp

### 4.1. S01 — Phát triển full-stack và ứng dụng di động

| Câu hỏi | Nội dung ghi nhận |
|---|---|
| 3. Tình huống gần đây | Khi tích hợp thanh toán trực tuyến vào backend Express/Node.js và frontend Next.js, người tham gia kiểm tra log callback, logic chữ ký, Git diff/PR rồi dò luồng từ callback đến lưu dữ liệu và trạng thái giao diện. |
| 4. Khó khăn | Khó truy vết tác động chéo backend–frontend, đặc biệt khi thiếu contract API rõ ràng. Tự ước tính 2–3 giờ để debug, đối chiếu commit và xác định nguyên nhân không khớp tham số. |
| 5. Công cụ | Dùng git log, git blame, PR trên GitHub, IDE và linter. Công cụ thuận tiện tại vị trí làm việc nhưng chưa cung cấp cái nhìn tổng thể về file thường thay đổi hoặc có dấu hiệu cần ưu tiên kiểm tra. |
| 6. Thay đổi liên quan | Thay Entity/DTO hoặc schema có thể kéo theo DAO, Repository, ViewModel và Adapter. Bỏ sót thường được phát hiện bởi compiler hoặc khi chạy test/ứng dụng; có thể gây lỗi runtime hoặc lưu dữ liệu sai. |
| 7. Tri thức về mã | Khi người phụ trách vắng mặt, đọc commit, blame, tài liệu và mã. Muốn biết người đóng góp chính hoặc người sửa gần nhất để hỏi lý do thiết kế và mời review. |
| 8. Cảnh báo | Mong cảnh báo khi mức thay đổi tăng nhanh, complexity tăng hoặc thay A nhưng bỏ sót B thường đi cùng. Cần tên file, mức độ và lý do; không muốn cảnh báo sai hoặc thông báo style/format ít liên quan. |
| 9. Giá trị và bằng chứng | Đánh giá cao co-change và kết hợp tần suất thay đổi với complexity. Ít ưu tiên biểu đồ phân bố đóng góp trong nhóm nhỏ. Muốn có commit hash và số liệu chứng minh quan hệ. |
| 10. Trợ lý AI | Sẵn sàng dùng cho review lớn hoặc module chưa quen. Lo gửi mã lên dịch vụ bên ngoài và gợi ý chung chung/ảo giác. Muốn tóm tắt theo ngữ cảnh, checklist khoảng 3–5 ý; đối chiếu với test hiện có. |
| 11. Ưu tiên và rào cản | Ưu tiên co-change, hotspot và checklist theo ngữ cảnh. Không muốn cài đặt phức tạp, chậm tiến trình làm việc hoặc nhiều cảnh báo sai. |
| 12. Thử nghiệm sau | Đồng ý tham gia; thông tin liên hệ dự kiến lưu riêng. |

**Diễn giải:** Phản hồi ủng hộ nhu cầu xem tác động liên quan giữa các file và bằng chứng lịch sử. Cách diễn đạt của người tham gia về hotspot như dấu hiệu nhận biết technical debt được ghi nhận là kỳ vọng; báo cáo không coi chỉ số này là bằng chứng xác định nợ kỹ thuật.

### 4.2. S02 — Backend và Frontend Developer

| Câu hỏi | Nội dung ghi nhận |
|---|---|
| 3. Tình huống gần đây | Trong task refactor API tính khuyến mãi cho đơn hàng, dùng GitLens để xem tác giả, lịch sử dòng mã và commit message; search/grep toàn dự án để tìm nơi gọi hàm trước khi sửa. |
| 4. Khó khăn | Mất thời gian truy vết phụ thuộc và hiểu nguyên nhân tồn tại của logic cũ. Tự ước tính khoảng 45 phút–1,5 giờ để hiểu ngữ cảnh trước khi viết mã, lặp lại khi làm việc với legacy code. |
| 5. Công cụ | Dùng GitLens, git log/blame và PR GitHub/GitLab. Hữu ích cho truy vết chi tiết nhưng thiếu góc nhìn tổng thể về lịch sử sửa lặp lại và các file có thể bị ảnh hưởng. |
| 6. Thay đổi liên quan | Thay trường trong Entity/Model backend nhưng bỏ sót DTO hoặc mapping frontend; backend test có thể đạt trong khi giao diện lỗi. Thường phát hiện qua integration test hoặc kiểm tra staging, phát sinh sửa bổ sung và gián đoạn kiểm thử. |
| 7. Tri thức về mã | Đọc commit/PR cũ khi người hiểu mã không có mặt. Cần biết người thường sửa file để hỏi và mời review đúng người. |
| 8. Cảnh báo | Muốn biết file thường đi cùng nhưng bị bỏ sót, hoặc file vừa phức tạp vừa thay đổi nhiều. Cần tên file, tỷ lệ đi kèm và lý do; tránh cảnh báo vụn vặt và quá tải thông tin. |
| 9. Giá trị và bằng chứng | Đánh giá cao co-change và hotspots. Ít ưu tiên phân bố đóng góp vì lo tạo cảm giác đánh giá cá nhân. Muốn bằng chứng cụ thể, ví dụ số commit cùng thay đổi trong một tập commit xác định. |
| 10. Trợ lý AI | Chưa có câu trả lời trong dữ liệu được cung cấp. |
| 11. Ba ưu tiên và lý do không dùng | Chưa có câu trả lời riêng. Các đánh giá ở câu 9 không được tự chuyển thành danh sách ba ưu tiên. |
| 12. Thử nghiệm sau | Chưa có câu trả lời. |

**Diễn giải:** Nhu cầu nổi bật là giảm thời gian tìm ngữ cảnh và tránh bỏ sót thay đổi liên quan. Việc cần xác định người để trao đổi ở câu 7 và ít ưu tiên biểu đồ đóng góp ở câu 9 phản ánh hai mục đích khác nhau: hỗ trợ phối hợp và thống kê cá nhân.

### 4.3. S03 — Sinh viên năm 4 phụ trách phát triển và tích hợp mã

| Câu hỏi | Nội dung ghi nhận |
|---|---|
| 3. Tình huống gần đây | Sau lỗi đăng nhập khi merge, xem log/blame các file nghi ngờ, đọc commit và hỏi người từng làm phần đó; phải mở khoảng 5–6 file để tìm vị trí cần sửa. |
| 4. Khó khăn | Khó xác định file nên đọc trước; commit message chung chung hạn chế khả năng hiểu lịch sử. Tự ước tính 30–60 phút để khoanh vùng. Họp nhóm hằng tuần giúp biết người phụ trách công việc. |
| 5. Công cụ | Chủ yếu dùng git log/blame, history, compare và PR trên GitHub. Công cụ hữu ích khi đã biết file cần xem; thiếu tổng quan để chọn file ban đầu. |
| 6. Thay đổi liên quan | Đổi trường backend nhưng quên phần hiển thị; phát hiện khi chạy thử hoặc được nhắc khi review. Hậu quả có thể là lỗi vào nhánh chính, mất thời gian sửa và giảm tin cậy vào build. |
| 7. Tri thức về mã | Hỏi người phụ trách hoặc đọc PR/comment cũ; muốn biết người thường sửa để hỏi đúng người và nội dung. |
| 8. Cảnh báo | Mong biết file thay đổi bất thường, nhiều người cùng sửa trong thời gian ngắn hoặc file liên quan chưa được cập nhật. Cần chỉ rõ file, biến động và lý do; tránh lặp thông báo. |
| 9. Giá trị và bằng chứng | Ưu tiên tần suất thay đổi và co-change; phân bố đóng góp hữu ích ở mức vừa phải để tìm người trao đổi. Complexity cần được giải thích dễ hiểu. Muốn xem commit và cửa sổ thời gian dùng để tính. |
| 10. Trợ lý AI | Có ý định dùng để hiểu chỉ số và nhận gợi ý review. Lo rò rỉ mã và AI giải thích sai với giọng khẳng định. Muốn nội dung ngắn, có dẫn chứng, ghi rõ là gợi ý; kiểm tra lại commit và mã. |
| 11. Ưu tiên và rào cản | Ưu tiên khoanh vùng file, biết file thường sửa cùng nhau và tìm người hiểu mã. Không muốn cài đặt phức tạp, gửi mã ra ngoài hoặc kết quả khó giải thích/hay sai. |
| 12. Thử nghiệm sau | Đồng ý tham gia. Thông tin liên hệ đã được loại khỏi báo cáo công khai. |

**Diễn giải:** Phản hồi nhấn mạnh việc ưu tiên đọc mã và khả năng kiểm tra lại bằng chứng. Thông tin tác giả nên phục vụ tìm người trao đổi; cảnh báo nhiều người cùng sửa chưa tự chứng minh file có vấn đề.

### 4.4. S04 — Sinh viên năm cuối, developer trong đồ án môn học

| Câu hỏi | Nội dung ghi nhận |
|---|---|
| 3. Tình huống gần đây | Khi ghép code backend, bạn cùng nhóm push code làm lỗi API thanh toán. Dùng GitLens (VS Code) xem git blame từng dòng, đọc lại lịch sử commit trên GitHub xem nhánh đó đổi file nào. |
| 4. Khó khăn | Hiểu flow code của người khác khi họ lười comment/đặt tên biến khó hiểu. Lặp lại đều đặn mỗi đợt ghép code cuối kỳ. Tự ước tính 2–3 giờ mỗi lần. |
| 5. Công cụ | GitLens và giao diện GitHub (PR, commit history). Hữu ích: biết ai viết dòng nào. Thiếu: không biết file đó trước giờ có hay lỗi không trước khi sửa. |
| 6. Thay đổi liên quan | Đổi tên trường trong Entity quên sửa DTO/Controller; phát hiện lúc build lỗi hoặc test Postman crash. Hậu quả: tốn thời gian debug lại, ảnh hưởng điểm nếu dính lúc demo. |
| 7. Tri thức về mã | Tự đọc mò hoặc nhắn tin chờ trả lời khi người hiểu mã vắng mặt. Muốn biết ai hay sửa file đó nhất — cho rằng người sửa nhiều nhất mới thật sự nắm logic, không nhất thiết là người tạo file. |
| 8. Cảnh báo | Muốn cảnh báo khi 2–3 người cùng sửa một file quan trọng, hoặc file "core" bị sửa mạnh tay. Không muốn cảnh báo vặt (format code, đổi README). |
| 9. Giá trị và bằng chứng | Đánh giá cao co-change và tần suất thay đổi. Ít ưu tiên phân bố đóng góp, nhận định có thể hữu ích hơn với người chấm điểm hơn là với dev. Để tin kết quả, cần công cụ chạy đúng trên lịch sử commit thật và chỉ đúng cặp file liên quan. |
| 10. Trợ lý AI | Có ý định dùng khi review code lạ trước merge. Lo ngại AI "ảo giác" nhận diện sai logic và bảo mật mã nguồn khi gửi lên API ngoài. Muốn đầu ra ngắn gọn dạng gạch đầu dòng; kiểm chứng bằng cách chạy test thử. |
| 11. Ưu tiên và rào cản | Ưu tiên: co-change, cảnh báo rủi ro ở file phức tạp/đổi thường xuyên, tìm người am hiểu đoạn code. Rào cản có thể khiến không dùng: setup rườm rà, tích hợp IDE nặng máy, cảnh báo sai (false positive) nhiều gây nhiễu. |
| 12. Thử nghiệm sau | Đồng ý tham gia. |

**Diễn giải:** Phản hồi khớp với xu hướng chung ở N01–N03, và củng cố thêm nhận định ở S02 rằng thông tin đóng góp cá nhân ít giá trị thực dụng với dev — người tham gia tự nêu giả thuyết rằng chỉ số này có thể phục vụ mục đích đánh giá/chấm điểm hơn là hỗ trợ công việc hằng ngày. Đây là diễn giải của người tham gia, chưa phải kết luận đã kiểm chứng.

### 4.5. S05 — Developer

| Câu hỏi | Nội dung ghi nhận |
|---|---|
| 3. Tình huống gần đây | Trả lời ngắn: "review code hay PR bằng mắt". Không nêu thêm chi tiết tình huống cụ thể. |
| 4. Khó khăn | Trả lời: "Từ ngày có AI hỗ trợ thì không còn khó khăn/mất thời gian nữa". Không làm rõ công cụ AI cụ thể hay cách dùng. |
| 5. Công cụ | Dùng Git thuần; hữu ích để xem lại mình đã làm gì qua lịch sử commit, có thể rollback nếu có vấn đề. |
| 6. Thay đổi liên quan | Không trả lời rõ nội dung (nguyên văn: "wtf câu hỏi???"). Ghi nhận là không thu được dữ liệu hợp lệ cho câu này, không suy diễn thay. |
| 7. Tri thức về mã | Dùng Git để biết ai từng sửa file đó. |
| 8. Cảnh báo | Muốn biết khi có khả năng conflict trước khi merge code. |
| 9. Giá trị và bằng chứng | Không đánh giá theo từng loại chỉ số; nhận định chung: "Git + AI hiện tại là đủ, không cần thêm". |
| 10. Trợ lý AI | Dùng thường xuyên các công cụ AI nói chung, từ lúc commit, tạo branch tới review — không phải trả lời trực tiếp về trợ lý AI review theo mô tả tính năng của nhóm. |
| 11. Ưu tiên và rào cản | Không nêu ưu tiên cụ thể; lặp lại quan điểm cho rằng công cụ hiện tại (Git kết hợp AI chat thông thường) đã giải quyết đủ nhu cầu. |
| 12. Thử nghiệm sau | Đồng ý tham gia thử nghiệm sau. |

**Diễn giải:** Đây là phản hồi mức độ chi tiết thấp và có thái độ hoài nghi rõ rệt về sự cần thiết của sản phẩm — khác biệt với bốn phản hồi còn lại. Báo cáo giữ nguyên phản hồi này thay vì loại bỏ, theo đúng yêu cầu không chỉ chọn ý kiến ủng hộ đề tài. Một số câu trả lời (ví dụ câu 10) không khớp sát với nội dung câu hỏi, chưa đủ dữ liệu để xác định nguyên nhân; không nên dùng các câu này làm minh chứng định lượng.

## 5. Các chủ đề rút ra từ phản hồi

| Mã | Chủ đề | Bằng chứng | Nhận định |
|---|---|---|---|
| N01 | Khó lựa chọn file cần kiểm tra và hiểu ngữ cảnh | S01, S02, S03, S04 câu 3–5 | Cần màn hình tổng quan giúp ưu tiên điều tra trước khi đi sâu vào lịch sử. |
| N02 | Dễ bỏ sót thay đổi ở file liên quan | S01–S04, câu 6, 8–9 | Co-change là nhu cầu lặp lại trong bốn trên năm phản hồi; cần trình bày cùng bằng chứng và giới hạn. |
| N03 | Cần dữ liệu để giải thích xếp hạng và quan hệ | S01–S04, câu 9 | Điểm tổng hợp đơn lẻ chưa đủ; cần số liệu, phạm vi tính và đường truy vết commit. |
| N04 | Cần biết người phù hợp để hỏi/review | S01–S05, câu 7; khác biệt ở câu 9 | Giữ chức năng hỗ trợ tìm người, không diễn giải số commit thành năng lực hay năng suất. S04 nêu thêm giả thuyết: người sửa nhiều nhất mới thật sự nắm logic, không nhất thiết là người tạo file — cân nhắc khi thiết kế cách hiển thị ownership. |
| N05 | Cảnh báo phải có lý do và hạn chế nhiễu | S01–S04, câu 8; S01, S03, S04 câu 11 | Chất lượng nội dung và khả năng kiểm soát cảnh báo quan trọng hơn số lượng. |
| N06 | AI hữu ích khi có ngữ cảnh và có thể kiểm chứng | S01, S03, S04 câu 10; S02 thiếu câu 10; S05 trả lời lệch câu hỏi | Ba người bày tỏ ý định sử dụng có điều kiện (ngữ cảnh, kiểm chứng được); chưa thể kết luận ý kiến chung của cả nhóm. |
| N07 | Chi phí thiết lập, độ trễ và riêng tư có thể cản trở sử dụng | S01, S03, S04 câu 10–11 | Cần hướng dẫn đơn giản và luồng sử dụng có kiểm soát; chưa có ngưỡng thời gian được người dùng thống nhất. |
| N08 | Một bộ phận người dùng cho rằng Git kết hợp AI chat thông thường đã đủ, chưa thấy rõ nhu cầu công cụ chuyên biệt | S05 câu 9, 11 | Chỉ một trên năm phản hồi; không đủ cơ sở kết luận xu hướng chung, nhưng cần giữ lại làm tín hiệu cảnh báo — sản phẩm phải chứng minh giá trị khác biệt rõ ràng so với việc hỏi AI chat thông thường, không chỉ lặp lại thông tin Git đã có. |

### 5.1. Diễn giải thời gian tự ước tính

S01 nêu 2–3 giờ cho hoạt động debug và đối chiếu liên tầng; S02 nêu 45–90 phút để hiểu ngữ cảnh trước khi sửa; S03 nêu 30–60 phút để khoanh vùng file. Các số liệu có phạm vi công việc khác nhau, do người tham gia nhớ lại, chưa được đo trực tiếp. Không tính trung bình chung hoặc sử dụng làm baseline định lượng cho hiệu quả sản phẩm.

### 5.2. Khác biệt về thông tin đóng góp

S01 ít ưu tiên biểu đồ phân bố đóng góp vì nhóm nhỏ đã biết phân công; S02 lo ngại cảm giác đánh giá cá nhân; S03 thấy hữu ích vừa phải; S04 cho rằng thông tin này có thể hữu ích hơn với người chấm điểm hơn là với dev. Tuy nhiên, bốn trên năm phản hồi đều cần biết người thường sửa để trao đổi. Do đó, đề xuất trình bày người đóng góp trong ngữ cảnh file, giải thích nguồn và giới hạn, tránh thiết kế bảng xếp hạng cá nhân.

## 6. Đề xuất chuyển hóa thành yêu cầu hệ thống

Các nội dung dưới đây là đề xuất sau tổng hợp, chưa phải thay đổi đã được nhóm hoặc GVHD phê duyệt. Mã UC tham chiếu `docs/usecases-draft.md` hiện có; cần đối chiếu lại khi chốt SRS do đề cương dùng cách đánh số khác.

| Mã đề xuất | Yêu cầu và điều kiện kiểm tra dự kiến | Cơ sở | Liên hệ/phạm vi |
|---|---|---|---|
| PV-R01 | Danh sách ưu tiên file hiển thị hotspot cùng tần suất thay đổi, complexity, snapshot và cửa sổ tính; cho phép xem chi tiết lý do | N01, N03 | UC3; phù hợp core |
| PV-R02 | Chi tiết coupling hiển thị cặp file, số commit chung, mẫu số/công thức và commit liên quan; thể hiện khi bằng chứng chưa đủ ngưỡng | N02, N03 | UC5; phù hợp core, cần thiết kế lưu/truy xuất evidence |
| PV-R03 | Từ một kết quả rủi ro có thể truy vết tới số liệu đầu vào và commit tham chiếu; dữ liệu thiếu phải được thông báo | N03 | UC3–UC5; làm rõ khả năng giải thích |
| PV-R04 | Chi tiết file có thông tin người đóng góp phù hợp để trao đổi và cách tính; không gắn nhãn đánh giá hiệu suất cá nhân | N04 | UC6; điều chỉnh cách trình bày, không tự loại HHI khỏi cam kết |
| PV-R05 | Cảnh báo có file, rule, snapshot, biến động và lý do; thiết kế cách tránh hiển thị lặp cùng sự kiện và cho cấu hình rule | N05 | UC7–UC9; cần nhóm chốt quy tắc chống lặp |
| PV-R06 | AI review theo yêu cầu, output ngắn có evidence, nhãn tư vấn và trạng thái lỗi; người dùng biết phần dữ liệu dự kiến gửi ra ngoài | N06, N07 | UC10–UC11; cần cơ chế chấp thuận dữ liệu, không tự gọi AI |
| PV-R07 | Có hướng dẫn bắt đầu ngắn và phân biệt trạng thái đang xử lý/thất bại; phân tích dài không giữ người dùng chờ một request đồng bộ | N07 | UC1–UC2; phù hợp định hướng bất đồng bộ, ngưỡng NFR phải đo/chốt riêng |

### 6.1. Những nhu cầu vượt hoặc chưa được bảo đảm trong phạm vi hiện tại

**Cảnh báo thiếu file trong PR đang mở.** S01/S02/S03 đều đề cập tình huống đã sửa A nhưng chưa sửa B. Hệ thống core hiện phân tích default branch và snapshot, chưa có đầu vào diff của PR hoặc thay đổi chưa commit. Vì vậy, core có thể hiển thị quan hệ lịch sử để người dùng tự kiểm tra; phát hiện tự động file bị bỏ sót trong một PR là đề xuất mở rộng, cần nguồn dữ liệu và tiêu chí riêng.

**Quan hệ giữa backend và frontend khác ngôn ngữ hoặc khác repository.** Các ví dụ của người tham gia chứa công nghệ web/mobile. Trong khi đó, thực nghiệm core dự kiến giới hạn Java và lịch sử trong repository được phân tích. Chưa thể cam kết bao phủ các ví dụ liên tầng, khác ngôn ngữ hoặc khác repo chỉ từ phản hồi này. Cần hỏi thêm người tham gia về nhu cầu trên dự án Java phù hợp.

**Nhiều người sửa cùng file và complexity vượt ngưỡng.** Đây là nhu cầu cần đánh giá tiếp, không tự biến thành rule đã có. Nhiều tác giả không đồng nghĩa rủi ro cao; complexity không có một ngưỡng phổ quát được xác nhận bởi khảo sát này.

### 6.2. Quy ước chỉ số cần làm rõ khi đặc tả

- Tần suất thay đổi đếm commit; code churn đo dòng thêm/xóa. S01 dùng thuật ngữ gần nhau trong mô tả, nên phần thiết kế phải phân biệt đơn vị thay vì sao chép nguyên cách gọi.
- Ví dụ “12 trong 15 commit của A có B” biểu thị tỷ lệ có hướng `shared(A,B)/commits(A)`. Công thức coupling dự kiến trong đề cương dùng `shared(A,B)/min(commits(A),commits(B))`. Hai tỷ lệ không luôn bằng nhau. Giao diện phải ghi rõ mẫu số và tránh diễn giải coupling đối xứng như xác suất B xuất hiện khi A thay đổi.
- Các giá trị “80%”, “15 commit” trong câu trả lời là ví dụ minh họa kỳ vọng về bằng chứng, không phải kết quả đo hoặc ngưỡng đã được thống nhất.
- Co-change không chứng minh phụ thuộc ngữ nghĩa; hotspot không xác nhận file có bug hoặc technical debt. Nội dung UI và AI cần giữ giới hạn này.

## 7. Hạn chế và thông tin cần bổ sung

1. Mẫu gồm năm trường hợp tự khai kinh nghiệm, chưa có thông tin về cách tuyển chọn; kết quả chỉ phục vụ định hướng sơ bộ.
2. Chưa có ngày giờ, hình thức phỏng vấn và xác nhận bản tóm tắt đầy đủ của từng người; người thu thập S04–S05 đã ghi nhận là Tưởng. Cần bổ sung để hồ sơ có thể truy vết.
3. S02 còn thiếu câu 10–12. Chưa có dữ liệu về kỳ vọng AI, ba ưu tiên chính thức và sự sẵn lòng tham gia user study của trường hợp này.
4. S03 ghi thời gian Git và quy mô nhóm trong ngoặc vuông; cần xác nhận đó là số liệu thực hay trường chưa hoàn thiện.
5. Một phần câu hỏi giới thiệu sẵn các nhóm tính năng, có thể ảnh hưởng cách trả lời. Các tình huống thực tế ở câu 3–7 cần được xem cùng với đánh giá tính năng ở câu 9–11.
6. Không có đo thời gian trực tiếp, kiểm tra mã nguồn các tình huống hoặc thực nghiệm so sánh công cụ. Chưa thể kết luận hệ thống sẽ giảm thời gian hay số lỗi bao nhiêu.
7. Mẫu có ba sinh viên (S01, S03, S04) và hai developer mô tả kinh nghiệm đi làm (S02, S05); cần mở rộng người dùng phù hợp ở giai đoạn thực nghiệm, đặc biệt bối cảnh Java và vai trò maintainer/technical lead.
8. S05 có nhiều câu trả lời ngắn, một câu không rõ nội dung — độ chi tiết không đồng đều giữa các bộ trả lời; không suy diễn mức độ đầu tư thời gian hoặc tự gán trọng số cho người tham gia.

### Bảng bổ sung hồ sơ

| Mã | Ngày giờ và hình thức | Người trực tiếp phỏng vấn/thu thập | Xác nhận nội dung cần hỏi thêm | Nguồn lưu đối chiếu riêng |
|---|---|---|---|---|
| S01 | Chưa cung cấp | Chưa cung cấp | Xác nhận bản tóm tắt; bối cảnh repo/ngôn ngữ khi thử nghiệm | Chưa bổ sung |
| S02 | Chưa cung cấp | Chưa cung cấp | Bổ sung câu 10–12; xác nhận bản tóm tắt | Chưa bổ sung |
| S03 | Chưa cung cấp | Chưa cung cấp | Xác nhận `[2]`, `[2-5]`; bản tóm tắt; không ghi âm | Chưa bổ sung |
| S04 | Chưa cung cấp | Tưởng, theo bản tổng hợp `3a3fd30` | Xác nhận bản tóm tắt | Chưa bổ sung |
| S05 | Chưa cung cấp | Tưởng, theo bản tổng hợp `3a3fd30` | Làm rõ câu 6, 10, 11 nếu có dịp phỏng vấn tiếp | Chưa bổ sung |

## 8. Kết luận và công việc tiếp theo

Năm phản hồi cung cấp cơ sở định tính ban đầu cho nhu cầu ưu tiên file cần kiểm tra, nhận biết file thường thay đổi cùng nhau và truy vết bằng chứng từ lịch sử Git. Một phản hồi (S05) thể hiện quan điểm hoài nghi về sự cần thiết của công cụ chuyên biệt so với Git kết hợp AI chat thông thường (chủ đề N08) — cần được xem là tín hiệu rủi ro cần phản biện bằng giá trị khác biệt rõ ràng của sản phẩm, không bỏ qua. Khả năng giải thích và kiểm soát nhiễu cảnh báo là yêu cầu lặp lại trong các trường hợp S01–S04. Vai trò của thông tin người đóng góp cần được giới hạn vào hỗ trợ trao đổi và hiểu bối cảnh, phù hợp với các ý kiến khác biệt về thống kê cá nhân.

Dữ liệu hiện có ủng hộ việc tiếp tục đặc tả các chức năng core đã đề xuất, đồng thời chỉ ra khoảng cách giữa nhu cầu cảnh báo trong PR và phạm vi snapshot/default branch. Kỳ vọng đối với AI được ghi nhận từ S01, S03 và S04 theo hướng tư vấn ngắn, có bằng chứng và kiểm soát dữ liệu gửi đi; chưa có ý kiến của S02 về nội dung này.

Toản và Tưởng cần rà soát các đề xuất PV-R01–PV-R07, cân nhắc thêm phản biện cho N08, bổ sung thông tin hồ sơ còn thiếu và xác định phần đưa vào SRS. Không tự thêm chức năng PR/multi-repository vào khối lượng cam kết. User study và các chỉ số hiệu quả sẽ được thiết kế, thu thập riêng sau khi có sản phẩm phù hợp.

## 9. Ghi nhận sử dụng AI và xác nhận

AI hỗ trợ chuẩn hóa văn phong, tóm tắt các câu trả lời do Toản và Tưởng cung cấp, nhóm chủ đề và đề xuất liên hệ với yêu cầu dự án. AI không tham gia thay người trả lời, không bổ sung câu trả lời còn thiếu và không tạo số liệu thực nghiệm. Toản và Tưởng cần đối chiếu báo cáo với nguồn gốc trước khi nộp và xác nhận các diễn giải chuyên môn cùng nhau.

Người rà soát nội dung: ____________________

Ngày rà soát: ____________________

Ý kiến xác nhận/điều chỉnh của nhóm hoặc GVHD: ____________________

## Cập nhật tích hợp ngày 02/10/2026

Đã tiếp nhận bản tổng hợp S04–S05 do Tưởng đưa lên nhánh chính tại `3a3fd30ff10048c382dcba7b122bd6dbeda7827f`. Báo cáo hiện có năm trường hợp; nguồn S04–S05 là bản tổng hợp của Tưởng, chưa đối chiếu độc lập bản trả lời gốc. Giữ phản hồi hoài nghi S05 và các trường còn thiếu. Đây là khảo sát nhu cầu, không phải user study đo SUS hoặc hiệu quả.

Công thức coupling trong ghi chú nghiên cứu `43d614f` dùng mẫu số hợp commit, khác công thức mẫu số min trong đề cương; cần thống nhất trước khi cài đặt metric.
