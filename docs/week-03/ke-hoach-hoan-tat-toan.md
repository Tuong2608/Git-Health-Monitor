# Kế hoạch hoàn tất phần việc tuần 3 của Toản

**Thời gian:** 30/09–03/10/2026, theo giờ Việt Nam/Thái Lan (UTC+7).

**Mục tiêu:** Tự kiểm chứng và hiểu phần đã được hỗ trợ; hoàn thiện hồ sơ khảo sát; đưa thay đổi qua review, kiểm thử và triển khai; nộp báo cáo có minh chứng. Đây là kế hoạch hành động, không ghi nhận các bước dưới đây đã được Toản thực hiện.

**Thư mục làm việc:** `D:\Toane\TLCN\Git-Health-Monitor`.

**Hiện trạng kiểm tra khi lập kế hoạch:** đang ở nhánh `feat/toan-week-3`; các thay đổi chưa được commit/push. Kết quả kiểm thử do AI chạy trước đó là 22 test backend và 4 browser test đạt. Toản cần ghi riêng kết quả tự chạy. URL frontend staging đã cung cấp chuyển sang đăng nhập tại lần kiểm tra trước; cần chủ tài khoản xử lý khả năng truy cập.

## 1. Lịch thực hiện

Thời lượng là ước lượng thời gian làm việc tập trung, chưa gồm thời gian chờ phản hồi, tải dependency hoặc chờ triển khai. Có thể đổi giờ theo lịch cá nhân, nhưng giữ thứ tự phụ thuộc.

| Ngày | Toản thực hiện | Phối hợp | Kết quả cần có |
|---|---|---|---|
| 30/09, khoảng 2–3 giờ | Gửi yêu cầu bổ sung khảo sát; kiểm tra môi trường; chạy lại bản thực hành; đọc luồng API/database | Nhờ Tưởng xác nhận quyền repo, hạ tầng DB và thời gian review từ đầu | Ứng dụng chạy local, ghi nhận test, danh sách câu chưa hiểu |
| 01/10, khoảng 2–3 giờ | Đọc React/Recharts/CORS; hoàn thiện biên bản; điền phần tự kiểm chứng vào AI log; review diff | Tưởng review các quyết định API và phạm vi AI | Tài liệu cá nhân chính xác, diff được đọc, điều kiện nghiệm thu rõ |
| 02/10, khoảng 1,5–2 giờ | Commit, push, mở PR; xem CI và xử lý lỗi | Tưởng review PR, xác nhận DB/env và cơ chế deploy trước merge | PR có review thật, CI đạt, cấu hình staging sẵn sàng |
| 03/10, khoảng 1–2 giờ | Cùng kiểm tra bản triển khai; bổ sung minh chứng, báo cáo và nộp | Tưởng hoặc chủ account thực hiện phần quản trị; GVHD nhận báo cáo | URL truy cập phù hợp, log deploy, báo cáo và xác nhận nộp |

Chủ động nhờ Tưởng chuẩn bị account/DB ngay ngày 30/09; không chờ tới ngày triển khai mới hỏi. Nếu một việc phụ thuộc chưa xong, ghi đúng vướng mắc và lịch xử lý, vẫn nộp báo cáo theo hạn thực tế của môn học. Ngày 03/10 là cuối tuần theo lịch nhóm, không thay thế hạn nộp chính thức nếu khoa quy định khác.

## 2. Bước 1 — Chốt việc phối hợp và hồ sơ phỏng vấn

### 2.1. Nội dung cần trao đổi với Tưởng ngay

- Toản có quyền push nhánh lên repo chưa? Nếu chưa, cần được mời cộng tác hoặc thống nhất dùng fork.
- Tưởng có thể review PR ngày 02/10 không?
- Backend staging đang kết nối PostgreSQL nào? Ai cấu hình DB/env? Bản mới có JPA/Flyway nên cần database trước khi merge nếu auto-deploy đang bật.
- Domain frontend nào dành cho GVHD, không bắt đăng nhập tài khoản Vercel?
- Phần PoC JGit/Lizard, công thức metric và benchmark sơ bộ của Tưởng đang đến đâu? Các kết quả này cần được ghi riêng, không lấy test CRUD của Toản thay thế.
- Thống nhất `ai-review-scope.md` hiện mới là nháp và lịch chính giữa Excel/đề cương.

### 2.2. Bổ sung khảo sát

Mở [báo cáo phỏng vấn](../interview-notes.md), bổ sung đúng thông tin có thể xác nhận:

1. Với S01–S03: ngày/giờ hoặc ngày thu thập, hình thức (nhắn tin, biểu mẫu, gặp trực tiếp...), người thu thập. Nếu không nhớ giờ chính xác, ghi không ghi nhận giờ thay vì tự ước lượng.
2. Với S02: hỏi lại câu 10 về AI, câu 11 về ba ưu tiên và lý do không dùng, câu 12 về tham gia thử nghiệm sau. Dùng câu hỏi trong [mẫu](mau-phong-van-gui-nguoi-tham-gia.txt), không gợi ý đáp án để giống người khác.
3. Với S03: xác nhận `[2]` là khoảng hai năm Git và `[2–5]` là quy mô nhóm thực tế; tiếp tục tôn trọng lựa chọn không ghi âm.
4. Gửi phần tóm tắt tương ứng cho từng người kiểm tra nếu thuận tiện; ghi rõ có/chưa có xác nhận lại.
5. Giữ bản trả lời nguồn và liên hệ ở nơi riêng. Repository chỉ lưu bản đã ẩn danh.

**Hoàn thành bước này khi:** thông tin thu thập và phần trả lời bổ sung được ghi đúng vào báo cáo; những phần người tham gia chưa phản hồi vẫn được đánh dấu thiếu. Chưa có thêm phản hồi không được tự viết thay.

## 3. Bước 2 — Tự chạy ứng dụng trên máy

### 3.1. Kiểm tra môi trường

Mở PowerShell:

```powershell
Set-Location 'D:\Toane\TLCN\Git-Health-Monitor'
git branch --show-current
git status --short
node --version
npm.cmd --version
docker version
Test-Path 'C:\Users\tranq\.jdks\ms-21.0.10\bin\java.exe'
```

Kết quả mong đợi: nhánh `feat/toan-week-3`, Node 24, có Docker Client và Server sau khi mở Docker Desktop, đường dẫn Java trả `True`. Danh sách file modified/untracked hiện tại là phần bàn giao đang chờ commit, không phải dấu hiệu mất mã.

Nếu Docker chỉ có Client hoặc báo không kết nối daemon, mở Docker Desktop rồi đợi Engine sẵn sàng. Nếu đường dẫn JDK trả `False`, thay bằng JDK 21 thực tế của máy. Không clone lại repo vì thay đổi hiện nằm trong thư mục này.

### 3.2. Khởi động database local

Hướng dẫn chính dùng Docker cho database và Java có sẵn để chạy backend. Không khởi động thêm backend Docker vì backend Java sẽ dùng port 8080.

Ở thư mục gốc:

```powershell
if (-not (Test-Path '.env')) { Copy-Item '.env.example' '.env' }
notepad .env
```

Trong `.env`, giữ `DB_USERNAME=ghm`, `DB_NAME=git_health_monitor`; thay `DB_PASSWORD` bằng mật khẩu local do bạn đặt, lưu file rồi đóng Notepad. Nếu file đã tồn tại từ trước, xem cấu hình và dùng đúng user/database hiện có; không thay mật khẩu tùy tiện trên volume đã được khởi tạo.

```powershell
docker compose up -d db
docker compose ps
docker compose exec db createdb -U ghm ghm_week3_toan_test
```

Database `ghm_week3_toan_test` dành riêng cho lần tự kiểm tra này. Nếu thông báo database đã tồn tại, giữ lại và tiếp tục. Nếu đã chọn user khác `ghm`, thay `-U ghm` và biến username bên dưới cho khớp.

Nếu port 5432 đã bị PostgreSQL khác sử dụng, không dừng dịch vụ đó khi chưa biết mục đích. Có thể dùng database test riêng trên PostgreSQL hiện có theo README, hoặc đổi host port của Compose và `DB_URL` cùng nhau. Hướng dẫn bên dưới giả định database Docker đang ở port 5432.

### 3.3. Terminal A — Kiểm thử rồi chạy backend

```powershell
Set-Location 'D:\Toane\TLCN\Git-Health-Monitor\backend'
$env:JAVA_HOME='C:\Users\tranq\.jdks\ms-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:DB_URL='jdbc:postgresql://localhost:5432/ghm_week3_toan_test'
$env:DB_USERNAME='ghm'
$taskDbSecret = Read-Host 'Nhap mat khau DB local da dat trong .env' -AsSecureString
$env:DB_PASSWORD = [System.Net.NetworkCredential]::new('', $taskDbSecret).Password
$env:APP_REGISTRATION_ENABLED='true'
.\mvnw.cmd -B -ntp verify
```

Chờ Maven kết thúc. Mốc tham chiếu bản hiện tại: `BUILD SUCCESS`, 22 test, 0 failure/error/skip. Nếu số lượng thay đổi do mã đã được sửa thêm, ghi số thực tế, không ép báo cáo trùng 22. Nếu có lỗi, xử lý trước khi chạy tiếp.

Sau khi verify đạt:

```powershell
.\mvnw.cmd spring-boot:run
```

Giữ Terminal A mở. Truy cập `http://localhost:8080/api/health`, mong đợi `OK`. Backend đang dùng database test riêng nên các dữ liệu thực hành không tác động staging.

Nếu port 8080 bị chiếm bởi backend Docker của chính bài thực hành trước, từ thư mục gốc dùng `docker compose stop backend`. Nếu là tiến trình khác chưa rõ, xác định ứng dụng đó trước khi dừng.

### 3.4. Terminal B — Chạy frontend

Mở PowerShell thứ hai:

```powershell
Set-Location 'D:\Toane\TLCN\Git-Health-Monitor\frontend'
npm.cmd ci
npm.cmd run dev
```

Mở `http://localhost:5173`. Bản local dùng Vite proxy để chuyển `/api` về backend. Nếu `frontend/.env` hoặc biến `VITE_API_BASE_URL` đang trỏ tới Render, bỏ giá trị đó cho lần thử local và khởi động lại Vite; không vô tình thử ghi vào staging.

### 3.5. Thử bằng tay

| Thao tác | Kết quả mong đợi |
|---|---|
| Thêm `https://github.com/Tuong2608/Git-Health-Monitor` nếu chưa có trong DB test | Thông báo đã lưu và một dòng trong danh sách |
| Tải lại trang | Bản ghi vẫn tồn tại |
| Thêm lại URL trên hoặc dạng có `.git` | Báo trùng; không thêm dòng thứ hai |
| Nhập `https://gitlab.com/demo/project` | API từ chối định dạng không hỗ trợ |
| Xem biểu đồ | Có nhãn dữ liệu minh họa, không coi là chỉ số thật của repo vừa lưu |
| Thu cửa sổ xuống khoảng 375 px | Có thể đọc/thao tác, không phải kéo ngang toàn trang |
| Dừng backend bằng Ctrl+C tại Terminal A, rồi Làm mới trên UI | Hiển thị lỗi kết nối, không báo dữ liệu trống là kết quả thành công |
| Chạy lại backend, rồi thử tải lại danh sách | Đọc được dữ liệu đã lưu |

Nếu URL đầu tiên đã tồn tại, đó là dữ liệu của lần thử trước; chọn một repository public khác bạn biết để thử luồng tạo mới. Tuần này ứng dụng chỉ xác thực cú pháp URL, chưa kiểm tra repo thật có tồn tại/công khai.

### 3.6. Terminal C — Kiểm tra frontend và browser

Đảm bảo backend ở Terminal A và frontend ở Terminal B đang chạy:

```powershell
Set-Location 'D:\Toane\TLCN\Git-Health-Monitor\frontend'
npm.cmd run lint
npm.cmd run build
npx.cmd playwright install chromium
$env:GHM_LIVE_BACKEND='1'
npm.cmd run test:e2e
```

Kết quả tham chiếu hiện tại: lint/build thành công, bốn test trình duyệt đạt. Test live tạo bản ghi trong database test. Nếu quên cờ `GHM_LIVE_BACKEND`, một test bị skip là hành vi thiết kế; không ghi rằng đã kiểm tra toàn luồng thật.

Lưu kết quả tự chạy vào [phiếu minh chứng cá nhân](nhat-ky-tu-thuc-hien-toan.md). Kết quả do AI chạy trước đó giữ nguyên vai trò tham chiếu, không đổi người thực hiện thành Toản.

## 4. Bước 3 — Đọc hiểu và tự giải thích

Mở [tài liệu học](ly-thuyet-va-ap-dung-toan.txt), đọc theo từng chủ đề rồi lần theo mã. Mỗi dòng dưới đây có thể dành khoảng 15–25 phút tùy kiến thức sẵn có.

| Thứ tự | File/nhóm file | Câu hỏi cần trả lời bằng lời của bạn |
|---|---|---|
| 1 | `RepositoryController.java` | URL/method nào gọi hàm nào? Vì sao tạo thành công là 201, trùng là 409? |
| 2 | `RepositoryService.java`, `GitHubRepositoryUrl.java` | URL được chuẩn hóa ra sao? Vì sao không chấp nhận mọi protocol/path? |
| 3 | `TrackedRepository.java`, `TrackedRepositoryStore.java`, migration V1 | JPA liên hệ bảng thế nào? Vì sao UNIQUE ở database vẫn cần thiết? |
| 4 | `application.properties`, `WebConfiguration.java` | DB lấy cấu hình từ đâu? Vì sao localhost và 127.0.0.1 khác origin? |
| 5 | `frontend/src/api.ts`, `App.tsx` | State nào thuộc UI? Vì sao phải phân biệt lỗi mạng với danh sách rỗng? Timeout/cleanup làm gì? |
| 6 | `DemoTrend.tsx` | DataKey/trục 0–1 làm gì? Vì sao dữ liệu mẫu không phải kết quả phân tích? |
| 7 | Test và `.github/workflows/ci.yaml` | Test nào dùng mock, test nào dùng PostgreSQL thật? CI khác deploy ở đâu? |

Tự trình bày luồng “nhập URL → HTTP → controller → service → JPA → PostgreSQL → response → UI” trong 3–5 phút. Sau đó giải thích một trường hợp lỗi. Nếu không giải thích được, ghi câu hỏi cụ thể để trao đổi với Tưởng hoặc hỏi tiếp trong chat.

Điền Mục 5 của [AI log](../ai-usage/toan-week03.md): đã đọc gì, tự chạy gì, chấp nhận/sửa/loại bỏ phần nào và vì sao. Không bắt buộc sửa mã chỉ để chứng minh đóng góp; đọc hiểu, kiểm chứng và quyết định tiếp nhận cũng cần ghi đúng thực tế.

## 5. Bước 4 — Commit và mở Pull Request

Thực hiện sau khi đã đọc diff và test liên quan đạt. Các lệnh dưới đây là hướng dẫn cho Toản, chưa được chạy thay trong bước lập kế hoạch.

### 5.1. Kiểm tra danh tính và phạm vi thay đổi

```powershell
Set-Location 'D:\Toane\TLCN\Git-Health-Monitor'
git branch --show-current
git config user.name
git config user.email
git status --short
git diff --stat
git diff
```

`git diff` không hiện nội dung file untracked; đọc thêm các file mới trong trình soạn thảo. Nếu danh tính thiếu/sai, đặt cấu hình local cho repo bằng tên và email GitHub của chính bạn:

```powershell
git config --local user.name 'Trần Quang Toản'
git config --local user.email 'THAY_BANG_EMAIL_GITHUB_HOAC_NOREPLY_CUA_BAN'
```

Thay placeholder trước khi chạy. Không dùng email người tham gia khảo sát hoặc email của Tưởng. Danh tính Git của commit không thay thế việc đăng nhập đúng tài khoản khi push.

### 5.2. Hai commit theo nhóm thay đổi

Không cần chia số lượng lớn vì toàn bộ việc được chuẩn bị trong một phiên; giữ ngày giờ thực tế.

Commit mã và cấu hình sau khi rà soát:

```powershell
git add .gitignore .env.example README.md compose.yaml .github backend frontend
git diff --cached --stat
git diff --cached
```

Xem kỹ danh sách staged. `.env`, `.local`, `node_modules`, `target` và kết quả test tạm không được đưa vào commit. Không tiếp tục nếu thấy secret hoặc dữ liệu cá nhân. Có thể dùng `git restore --staged -- TEN_FILE` để bỏ riêng file khỏi staging mà giữ nội dung đang sửa.

```powershell
git commit -m "feat: add week 3 repository flow with PostgreSQL and UI checks"
git log -1 --format="%h %s"
```

Ghi SHA thực vào bảng truy vết của AI log. Với mã được AI tạo, giữ khai báo AI hỗ trợ; commit bằng tài khoản của bạn thể hiện việc bạn tiếp nhận thay đổi, không phải tuyên bố tự viết từng dòng.

Sau đó commit hồ sơ:

```powershell
git add docs
git diff --cached --stat
git diff --cached
git commit -m "docs: document week 3 learning, interviews and AI-assisted work"
git log -2 --oneline
git status --short
```

Chỉ commit nếu staged có thay đổi dự định. Không cố chèn SHA của commit tài liệu vào chính commit đó; có thể bổ sung trong lần cập nhật báo cáo tiếp theo.

### 5.3. Đồng bộ với nhánh chính và push

```powershell
git fetch origin
git log --oneline HEAD..origin/main
```

Nếu không có commit mới từ `origin/main`, tiếp tục push. Nếu có, khi working tree sạch, phối hợp Tưởng và merge nhánh chính vào nhánh làm việc bằng `git merge origin/main`, giải quyết xung đột theo nội dung, rồi chạy lại kiểm tra bị ảnh hưởng. Nếu chưa biết xử lý conflict, dừng tại trạng thái đó để nhờ hỗ trợ; không force push hoặc bỏ thay đổi để vượt lỗi.

```powershell
git push -u origin feat/toan-week-3
```

Nếu bị từ chối quyền, nhờ Tưởng mời đúng tài khoản và chấp nhận lời mời; hoặc thống nhất PR từ fork. Không thay origin sang repo khác theo phỏng đoán.

### 5.4. Mở PR trên GitHub

Vào repo → **Pull requests → New pull request**. Chọn base `main`, compare `feat/toan-week-3`. Đặt tiêu đề, ví dụ: `Tuần 3: lưu repository, giao diện thực hành và hồ sơ khảo sát`.

Trong mô tả, nêu:

- Luồng đã cài và giới hạn: chỉ lưu URL, chưa phân tích Git; biểu đồ giả có nhãn.
- Test bạn đã tự chạy với kết quả thực tế và ngày chạy.
- Link `docs/interview-notes.md`, báo cáo tuần và AI log trên nhánh.
- Điều kiện trước merge: DB/env staging sẵn, Tưởng review, CI đạt.

Chọn Tưởng làm reviewer nếu giao diện/quyền cho phép; nếu cần trao đổi ngoài GitHub, tự gửi link PR. Không tự approve thay Tưởng. Cách tạo PR tham chiếu [GitHub Docs](https://docs.github.com/en/pull-requests/how-tos/create-pull-requests/creating-a-pull-request).

## 6. Bước 5 — Xem CI và xử lý lỗi

Trên PR xem Checks hoặc vào tab Actions, mở run đúng SHA. Với workflow hiện tại cần xem các job `backend`, `frontend`, `secret-scan`, `package`.

| Lỗi thấy trong log | Cách kiểm tra đầu tiên |
|---|---|
| Backend không kết nối PostgreSQL | Service có healthy không; DB_URL/username có khớp workflow không |
| Flyway hoặc JPA validate lỗi | Đọc migration và mapping entity; không tắt validate để bỏ qua sai schema |
| Frontend lint/typecheck | Xem file/dòng lỗi, sửa rồi chạy lại lệnh local |
| Browser test | Phân biệt mock/live, port/server, đọc báo cáo hoặc trace đính kèm |
| Secret scan | Xác định có secret thật hay test fixture; nếu secret thật, xử lý thu hồi và lịch sử theo sự cố, không chỉ xóa dòng ở bản cuối |
| Docker build | Xem bước lỗi và context backend; không coi Maven local thành công là Docker đã được kiểm chứng |

Ghi lại lỗi, nguyên nhân, cách sửa và SHA vào AI log nếu có AI hỗ trợ. CI frontend mặc định chạy ba test mock và bỏ qua một live test; đó là cấu hình hiện tại. Để minh chứng live, dẫn riêng kết quả bạn chạy trên database test local.

**Hoàn thành:** các job cần thiết đạt trên phiên bản PR sẽ merge, review thực chất đã có và ý kiến đã được xử lý. CI xanh chưa chứng minh staging đã triển khai.

## 7. Bước 6 — Chuẩn bị và kiểm tra triển khai cùng Tưởng

### 7.1. Điều kiện trước merge

Chủ tài khoản xác nhận các mục sau. Toản có quyền quản trị thì có thể tự thực hiện theo thống nhất của nhóm; nếu không, phối hợp Tưởng, không gửi mật khẩu/token qua báo cáo hoặc chat.

**Render:**

- Dịch vụ đang theo dõi đúng repository/branch dự định, thường là `main` nhưng phải xem cấu hình thực tế.
- PostgreSQL đã có, DB_URL dạng JDBC, DB_USERNAME/DB_PASSWORD hợp lệ.
- APP_ALLOWED_ORIGINS chứa origin domain frontend chính xác, không có slash cuối.
- APP_REGISTRATION_ENABLED mặc định false cho preview công khai; chỉ bật true khi kiểm thử được kiểm soát, rồi trả về cấu hình đã thống nhất. Cờ này không thay thế đăng nhập/phân quyền.
- Nếu liên kết Git provider và có lựa chọn, dùng Auto-Deploy **After CI Checks Pass**. Dịch vụ triển khai từ public repo URL có thể cần thao tác deploy thủ công; xác nhận cấu hình account thay vì mặc định auto-deploy.

Nguồn: [Render Deploys](https://render.com/docs/deploys).

**Vercel:**

- Root directory `frontend`, build `npm run build`, output `dist`, Node 24.
- Cấu hình `VITE_API_BASE_URL=https://git-health-monitor.onrender.com` cho đúng môi trường; thay đổi biến cần một lần build/deploy mới để frontend sử dụng.
- Chọn domain ổn định phù hợp cho GVHD. Kiểm tra domain đó bằng cửa sổ ẩn danh; không dựa vào deployment URL yêu cầu đăng nhập.
- Chỉ điều chỉnh phạm vi bảo vệ cần thiết ở project, không tắt toàn bộ bảo vệ theo thói quen.

Nguồn: [Vercel Environment Variables](https://vercel.com/docs/environment-variables), [Deployment Protection](https://vercel.com/docs/deployment-protection).

### 7.2. Sau merge và deploy

1. Lưu SHA merge, link CI run, backend deploy id và frontend deployment.
2. Kiểm tra giao diện mới ở domain dự định bằng cửa sổ ẩn danh; không chỉ nhìn trang mặc định Vite cũ.
3. Gọi `/api/health` và `/api/repositories`. Health OK một mình không chứng minh database hoạt động.
4. Trong phiên đăng ký được bật có kiểm soát: thêm một URL thử, reload, thử trùng và URL không được hỗ trợ; ghi kết quả. Không dùng dữ liệu nhạy cảm.
5. Kiểm tra biểu đồ có nhãn mẫu, lỗi được trình bày rõ, giao diện mobile dùng được.
6. Ghi trạng thái đăng ký sau khi kiểm tra và cách khôi phục deployment trước theo cơ chế nhóm đang dùng.
7. Đo commit→staging chỉ từ thời điểm thực; nếu thiếu timestamp thì ghi chưa đo, không suy từ thời gian test local.

Nếu bị trang đăng nhập, CORS hoặc DB lỗi: ghi đúng lỗi và xử lý cùng chủ account. Không đánh dấu frontend công khai hoặc tính một lần deploy thành công chỉ vì đã nhấn nút deploy.

## 8. Bước 7 — Chốt hồ sơ và báo cáo tuần

Cập nhật [báo cáo tuần](bao-cao-tuan-03-toan.md), [AI log](../ai-usage/toan-week03.md) và [phiếu tự thực hiện](nhat-ky-tu-thuc-hien-toan.md):

- Phần đã tự học/giải thích được, test tự chạy và phần chưa hiểu.
- Hồ sơ khảo sát đã bổ sung đến đâu; đề xuất nào đã được nhóm chấp nhận.
- Link PR/review, SHA, CI run và deployment; ai làm phần nào.
- Các vướng mắc, người phụ trách và kế hoạch xử lý tuần sau.
- Ngày báo cáo GVHD/web khoa, nội dung góp ý/xác nhận theo cách môn học quy định.

Với Excel, chỉ cập nhật trạng thái những việc tương ứng đã được nhóm nghiệm thu; skeleton/CI/staging là đầu việc chung, không đánh dấu hoàn thành chỉ vì phần local của Toản đã chạy. Việc học công nghệ, phỏng vấn, phạm vi AI và AI log cần có trạng thái phù hợp riêng.

## 9. Điều kiện chốt tuần 3

- [ ] Đã tự chạy phần thực hành và giải thích được các thành phần mình tiếp nhận.
- [ ] Đã ghi kết quả tự học/tự kiểm chứng vào hồ sơ, tách khỏi kết quả AI chạy.
- [ ] Ba phản hồi đã có hồ sơ truy vết; câu thiếu và xác nhận còn chờ được ghi rõ.
- [ ] Có commit/PR thật, review chéo và kiểm tra CI của phiên bản liên quan.
- [ ] Mã mới được kiểm tra sau triển khai, hoặc vướng mắc triển khai được báo cáo rõ thay vì ghi hoàn thành.
- [ ] Báo cáo tuần được gửi đúng hạn, lưu minh chứng tiếp nhận/góp ý theo quy định.
- [ ] Tưởng và Toản đã rà phần chung còn thiếu, thống nhất việc chuyển sang tuần 4.

Tuần này ưu tiên hiểu, kiểm chứng và hoàn tất quy trình của luồng nhỏ đang có. Thuật toán hotspot, toàn bộ mười bảng, scheduler và AI provider không cần được cài thêm chỉ để làm danh sách tuần 3 dài hơn.
