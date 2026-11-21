# Hướng dẫn chạy lại và kiểm chứng — Tuần 10

Tài liệu hướng dẫn tái lập toàn bộ các phép đo và kiểm thử của Tuần 10 từ thư mục gốc `Git-Health-Monitor` trên Windows PowerShell.

---

## 1. Chạy bộ kiểm thử Python thực nghiệm (37 ca) và kiểm toán độ phủ RQ2

Không cần cơ sở dữ liệu hay kết nối mạng:

```powershell
& .local/week8-python/Scripts/python.exe -m unittest discover -s tools/experiments -v
& .local/week8-python/Scripts/python.exe tools/experiments/audit_rq2_coverage.py `
    --pilot-dir docs/week-09/evidence/pilot `
    --scenarios-dir docs/week-10/evidence/rq2-scenarios `
    --out-json .local/rq2-coverage-audit-check.json
```

---

## 2. Chạy lại 5 kịch bản tiến hóa mã nguồn RQ2 (`S1..S5`) và Benchmark lõi 10.000 commit

Cần Java 21, Git và `lizard==1.24.3` trong `PATH`:

```powershell
$env:JAVA_HOME = "C:\Users\tranq\.jdks\ms-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$(Resolve-Path .local/week8-python/Scripts);$env:PATH"
Push-Location backend
.\mvnw.cmd -B -ntp compile dependency:build-classpath "-Dmdep.outputFile=target/research-classpath.txt"
Pop-Location
$cp = (Resolve-Path backend/target/classes).Path + ";" + (Get-Content backend/target/research-classpath.txt -Raw).Trim()

# Chạy 5 kịch bản S1..S5 + kiểm toán 9 snapshot OSS
& .local/week8-python/Scripts/python.exe tools/experiments/evaluate_rq2_scenarios.py `
    --out-dir .local/rq2-scenarios-replay `
    --classpath $cp

# Chạy benchmark hiệu năng 1.000 / 5.000 / 10.000 commit
& .local/week8-python/Scripts/python.exe tools/experiments/run_benchmark.py `
    --out-json .local/benchmark-replay.json `
    --classpath $cp `
    --iterations 3
```

---

## 3. Chạy đầy đủ 122 JUnit test, Benchmark lưu trữ PostgreSQL 18.3 và báo cáo JaCoCo

Khởi động PostgreSQL 18 cục bộ trên cổng `55432` với database thử nghiệm riêng `ghm_week10_test`:

```powershell
$pgbin = "C:\Program Files\PostgreSQL\18\bin"
$env:JAVA_HOME = "C:\Users\tranq\.jdks\ms-21.0.10"
$env:PATH = "$env:JAVA_HOME\bin;$(Resolve-Path .local/week8-python/Scripts);$env:PATH"
$env:DB_URL = "jdbc:postgresql://127.0.0.1:55432/ghm_week10_test"
$env:DB_USERNAME = "ghm"
$env:DB_PASSWORD = "local-test-only"

& "$pgbin\pg_ctl.exe" -D ".local\pgdata" -l ".local\postgres-week10.log" -o "-p 55432 -h 127.0.0.1" -w start
try {
    & "$pgbin\psql.exe" -h 127.0.0.1 -p 55432 -U ghm -d postgres -c "DROP DATABASE IF EXISTS ghm_week10_test;"
    & "$pgbin\psql.exe" -h 127.0.0.1 -p 55432 -U ghm -d postgres -c "CREATE DATABASE ghm_week10_test;"
    Push-Location backend
    .\mvnw.cmd -B -ntp clean verify "-Dweek10.evidenceDir=D:\Toane\TLCN\Git-Health-Monitor\.local\week10-evidence"
    Pop-Location
} finally {
    & "$pgbin\pg_ctl.exe" -D ".local\pgdata" -m fast -w stop
}
```

- Báo cáo JaCoCo HTML/XML được sinh tại `backend/target/site/jacoco/index.html` và `backend/target/site/jacoco/jacoco.xml`.
- Số liệu benchmark lưu trữ PostgreSQL được xuất tại `.local/week10-evidence/postgres-persistence-benchmark.json`.

---

## 4. Kiểm thử Frontend và chấm điểm khảo sát người dùng RQ3

```powershell
Push-Location frontend
npm.cmd run lint
npm.cmd run build
npm.cmd run test:e2e
Pop-Location
```

Khi thu thập xong dữ liệu thực tế từ người tham gia khảo sát theo mẫu [rq3-response-template.csv](rq3-response-template.csv), chạy lệnh chấm điểm SUS và tỷ lệ hoàn thành 7 tác vụ:

```powershell
& .local/week8-python/Scripts/python.exe tools/experiments/evaluate_rq3_sus.py `
    du-lieu-khao-sat-thuc-te.csv `
    --out-json docs/week-11/evidence/rq3-results.json
```
