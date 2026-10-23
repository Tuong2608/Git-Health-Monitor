# Noi dung PR tuan 6 — phan Tuong

Tao PR: `codex/tuong-week6` -> `main`. Cau len PR goi y:

```
feat: collect Git history and document its data contract
```

## Noi dung

```markdown
## Summary

- Module doc Git bang JGit: `GitRepositoryClient` (clone/fetch bare, chi nhan URL GitHub cong khai, khong checkout, khong chay code trong repo, khong gui token) va `GitIngestion` (duyet DAG theo thu tu topo, dinh danh file qua rename, sinh fact trong cua so, gioi han tai nguyen that bai ro rang).
- Lenh chay mau `GitIngestionSample`: bao cao ket qua thu thap ma **khong can** API/PostgreSQL, de demo va doi chieu khi chua co adapter.
- `docs/week-06/git-ingestion.md`: hop dong `Fact`/lineage cho lop luu tru, minh chung test, va cac diem con mo can hai nguoi chot.
- Merge `main` 0b664d9 (PR #5 cua Toan); README da giai quyet xung dot, giu nguyen phan `AnalysisPipeline` cua Toan.

## Kiem chung

| Lenh | Ket qua |
|---|---|
| `mvn -o -B test -Dtest='GitIngestion*Tests'` | 12/12 dat (10 ingestion + 2 sample) |
| `mvn -o -B test` (ca bo, sau merge) | bien dich 18 class; 24 loi la `Connection to localhost:5432 refused` |
| `python -m unittest -v test_evaluate_rankings` (tool Toan) | 8/8 dat, chay doc lap |
| `GitIngestionSample` tren 3 repo cong khai | xem bang duoi |

Cac test can PostgreSQL (toan bo package `repository` va `monitoring`) **chua kiem chung duoc o may nay** vi Docker Engine hong va chua dung lai PostgreSQL portable. Do vay con so 41 test cua phan Toan trong `docs/week-06` la tuyen bo chua tu kiem chung, khong phai ket qua cua PR nay.

| Repo | reachable | selected | fact | renameFacts | file Java | lineage bat dinh |
|---|---|---|---|---|---|---|
| spring-guides/gs-rest-service (window ~54 nam) | 421 | 396 | 83 | 16 | 6 | 2 |
| spring-projects/spring-petclinic (365 ngay) | 1047 | 49 | 92 | 0 | 50 | 13 |
| octocat/Hello-World | 3 | 2 | 0 | 0 | 0 | 0 |

## 4 diem can Toan doi chieu tru khi noi adapter

1. **`observationDays` chan 1..365** (`MonitoringStore.java:47`). Module Git cua minh can cua so >365 ngay de do nhung repo khong co commit gan day (gs-rest-service: 0 fact trong 180 ngay, 83 fact khi mo rong). Chot: day la gioi han cua **san pham** hay cua ca bo thu nghiem offline?
2. **`commitCount`**: `SnapshotPublisher` loi neu `frequency > commitCount`. `Result` co hai gia tri `reachableCommits` (ca lich su) va `selectedCommits` (trong cua so). Minh cho rang `commitCount = selectedCommits`, de nghi ghi ro vao hop dong.
3. **churn null**: `file_metric.churn` la `bigint NOT NULL` va `contributions` phai cong dung bang churn file, trong khi fact cua minh tra `additions/deletions = null` cho file nhi phin/khong phai file thuong. Phan biet null-vs-0 ma `algorithm-design.md` chu truong **khong song qua schema V2**; can chot: loai khoi universe, hay ghi churn = 0 kem `unavailable_reason`?
4. **`truncated` vs `RESOURCE_LIMIT`**: minh that bai toan bo extraction khi vuot gioi han (theo `algorithm-design.md` dong 120), con schema co cot `truncated` va `summary.coverageComplete` de cho phep tra ket qua cat cot. Hai cach mau thuan nhau; chot mot, hoac `truncated` se chi la hang so false.

Hai diem nho khong chan loi: `logical_file.identity_evidence` gan nhu co dinh `'{}'` trong `SnapshotPublisher.java:44` (evidence lineage khong duoc luu), va ten file `docs/week-06/ban-giao-tuong.md` mang nghia "ban giao **cho** Tuong" trong khi cung ten o tuan 5 la Tuong tu ban giao — nen doi thanh `ban-giao-toan-tuan-06.md`.

## Khong tuyen bo trong PR nay

Chua co adapter `AnalysisPipeline`, chua tinh metric (frequency/churn/HHI/hotspot/coupling), chua do CCN/NLOC, chua ghi PostgreSQL, chua chay pipeline end-to-end, chua deploy staging, chua co nhan cua nhom hay GVHD.
```

## Cach chay de kiem chung nhanh

```powershell
$env:JAVA_HOME='<duong dan JDK 21>'
cd backend
.\mvnw.cmd -o -B test "-Dtest=GitIngestion*Tests"
.\mvnw.cmd -o -B dependency:build-classpath "-Dmdep.outputFile=target/cp.txt"
java -cp "target/classes;$(Get-Content target/cp.txt)" `
  com.example.demo.gitanalyzer.GitIngestionSample `
  https://github.com/spring-projects/spring-petclinic target/sample-cache 365
```
