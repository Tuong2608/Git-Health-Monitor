# Chạy lại RQ2

Không cần Docker, PostgreSQL hay API key. Cần Java 21, Git, Python 3.12+ và `lizard==1.24.3`. Không chạy build/test của các kho OSS được phân tích. Các lệnh sau chạy PowerShell từ root Git-Health-Monitor.

```powershell
python -m venv .local/rq2-python
& .local/rq2-python/Scripts/python.exe -m pip install lizard==1.24.3
$env:PATH="$(Resolve-Path .local/rq2-python/Scripts);$env:PATH"
Push-Location backend
.\mvnw.cmd -B -ntp compile dependency:build-classpath '-Dmdep.outputFile=target/research-classpath.txt'
Pop-Location
$rq2cp=(Resolve-Path backend/target/classes).Path+';'+(Get-Content backend/target/research-classpath.txt -Raw).Trim()
```

## Tái lập bộ đã bàn giao

Clone bare vào thư mục mới; không dùng shallow clone. Manifest đã pin SHA nên HEAD mới trên GitHub không đổi kết quả lịch sử. Nếu object đã không còn truy xuất được, báo lỗi, không thay SHA.

```powershell
git clone --bare https://github.com/spring-guides/gs-rest-service .local/rq2-cache/gs-rest-service.git
git clone --bare https://github.com/spring-projects/spring-petclinic .local/rq2-cache/spring-petclinic.git
git clone --bare https://github.com/apache/commons-cli .local/rq2-cache/commons-cli.git
python tools/experiments/run_rq2.py run docs/week-09/evidence/frozen-manifest.json .local/rq2-cache .local/rq2-replay --classpath $rq2cp
python tools/experiments/summarize_rq2.py .local/rq2-replay
```

Đọc `.local/rq2-replay/run-summary.json`: kiểm tra từng status, selectionStatus và comparisonStatus. Lệnh lưu cả thất bại để kiểm toán; không lấy việc tạo được file JSON làm bằng chứng mọi snapshot thành công. Thư mục output phải chưa tồn tại, để không ghi đè minh chứng. Trên Linux dùng `:` thay `;` trong classpath và đường dẫn venv `bin` thay `Scripts`.

## Một lần thử nghiệm mới

Sao chép proposal, sửa danh sách/mốc trước đo, lưu lý do thay đổi; dùng tên manifest/output mới.

```powershell
python tools/experiments/run_rq2.py freeze docs/week-09/dataset-proposal.json .local/new-rq2-cache .local/new-rq2-manifest.json
python tools/experiments/run_rq2.py run .local/new-rq2-manifest.json .local/new-rq2-cache .local/new-rq2-results --classpath $rq2cp
```

`freeze` tái sử dụng HEAD trong cache hiện có, không tự fetch. Muốn lấy trạng thái mới nhất thì dùng cache mới hoặc fetch có chủ đích trước freeze. Mỗi lần manifest ghi endSha thật của cache.

## Tái lập bộ thực nghiệm RQ1 (Chia cắt thời gian 80/20)

```powershell
git clone --bare https://github.com/apache/commons-csv .local/rq1-cache/commons-csv.git
git clone --bare https://github.com/apache/commons-codec .local/rq1-cache/commons-codec.git
git clone --bare https://github.com/apache/commons-dbutils .local/rq1-cache/commons-dbutils.git
git clone --bare https://github.com/apache/commons-validator .local/rq1-cache/commons-validator.git
git clone --bare https://github.com/square/javapoet .local/rq1-cache/javapoet.git
python tools/experiments/run_rq1.py run docs/week-09/evidence/rq1-frozen-manifest.json .local/rq1-cache docs/week-09/rq1-commit-reviews.json .local/rq1-replay --classpath $rq2cp
python tools/experiments/summarize_rq1.py .local/rq1-replay
```

## Kiểm thử

```powershell
python -m unittest discover -s tools/experiments -v
Push-Location backend
.\mvnw.cmd -B -ntp test '-Dtest=HistoricalSnapshotExportTests'
Pop-Location
```

Full `mvnw verify` cần PostgreSQL riêng và DB_URL/DB_USERNAME/DB_PASSWORD như README chính. Chạy test CLI lịch sử không cần database. CI hiện có tự chạy các test mới; không cần workflow thứ hai.
