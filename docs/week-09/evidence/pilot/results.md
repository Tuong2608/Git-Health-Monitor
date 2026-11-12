# Kết quả RQ2 — dữ liệu chạy thử

Các thay đổi ở cặp BLOCKED chỉ là dữ liệu chẩn đoán, không phải cảnh báo hợp lệ hay bằng chứng dự đoán lỗi.

| Dự án | Mốc cuối (UTC) | Commit trong cửa sổ | File Java | File có hotspot | Lineage chưa rõ | Coverage |
|---|---|---:|---:|---:|---:|---|
| gs-rest-service | 2024-01-01T00:00:00Z | 4 | 6 | 4 | 2 | False |
| gs-rest-service | 2025-01-01T00:00:00Z | 0 | 6 | 4 | 2 | False |
| gs-rest-service | 2026-01-01T00:00:00Z | 12 | 6 | 4 | 2 | False |
| spring-petclinic | 2024-01-01T00:00:00Z | 18 | 40 | 20 | 10 | False |
| spring-petclinic | 2025-01-01T00:00:00Z | 36 | 40 | 21 | 9 | False |
| spring-petclinic | 2026-01-01T00:00:00Z | 29 | 47 | 22 | 13 | False |
| commons-cli | 2024-01-01T00:00:00Z | 99 | 52 | 6 | 45 | False |
| commons-cli | 2025-01-01T00:00:00Z | 171 | 85 | 36 | 47 | False |
| commons-cli | 2026-01-01T00:00:00Z | 181 | 87 | 38 | 47 | False |

| Cặp snapshot | Trạng thái | File chung có điểm (chẩn đoán) | Thêm / mất lineage | Cảnh báo hợp lệ |
|---|---|---:|---|---|
| [gs-rest-service-0-to-1.json](gs-rest-service-0-to-1.json) | BLOCKED | 4 | 0 / 0 | N/A |
| [gs-rest-service-1-to-2.json](gs-rest-service-1-to-2.json) | BLOCKED | 4 | 0 / 0 | N/A |
| [spring-petclinic-0-to-1.json](spring-petclinic-0-to-1.json) | BLOCKED | 20 | 1 / 1 | N/A |
| [spring-petclinic-1-to-2.json](spring-petclinic-1-to-2.json) | BLOCKED | 21 | 7 / 0 | N/A |
| [commons-cli-0-to-1.json](commons-cli-0-to-1.json) | BLOCKED | 6 | 33 / 0 | N/A |
| [commons-cli-1-to-2.json](commons-cli-1-to-2.json) | BLOCKED | 36 | 2 / 0 | N/A |

## Ví dụ chẩn đoán có thể đối chiếu

Mỗi cặp lấy đúng một file có |delta hotspot| lớn nhất, hòa thì theo lineageKey; quy tắc này chỉ chọn ví dụ trình bày sau đo, không chọn/bỏ dự án thực nghiệm.

### gs-rest-service — cặp 0 → 1

File: `complete/src/test/java/com/example/restservice/GreetingControllerTests.java`. Frequency 0 → 0; max CCN 1 → 1; hotspot 0.500000 → 0.500000; delta +0.000000.

Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.

Không có commit của file trong cửa sổ sau. Frequency giảm khi các commit trước rời cửa sổ không có nghĩa code đã được sửa cho tốt hơn.

### gs-rest-service — cặp 1 → 2

File: `complete/src/test/java/com/example/restservice/GreetingControllerTests.java`. Frequency 0 → 2; max CCN 1 → 1; hotspot 0.500000 → 0.707107; delta +0.207107.

Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.

Commit trong cửa sổ sau: [389429a7](https://github.com/spring-guides/gs-rest-service/commit/389429a7345a718c27a77cabc495cf7ea68bbaee), [ccccdcd7](https://github.com/spring-guides/gs-rest-service/commit/ccccdcd71c068276d2fc5ccf1e155e26a5614a7c).

### spring-petclinic — cặp 0 → 1

File: `src/main/java/org/springframework/samples/petclinic/owner/PetValidator.java`. Frequency 1 → 0; max CCN 5 → 5; hotspot 0.842927 → 0.447214; delta -0.395714.

Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.

Không có commit của file trong cửa sổ sau. Frequency giảm khi các commit trước rời cửa sổ không có nghĩa code đã được sửa cho tốt hơn.

### spring-petclinic — cặp 1 → 2

File: `src/test/java/org/springframework/samples/petclinic/vet/VetTests.java`. Frequency 2 → 0; max CCN 1 → 1; hotspot 0.509902 → 0.202031; delta -0.307871.

Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.

Không có commit của file trong cửa sổ sau. Frequency giảm khi các commit trước rời cửa sổ không có nghĩa code đã được sửa cho tốt hơn.

### commons-cli — cặp 0 → 1

File: `src/test/java/org/apache/commons/cli/bug/BugCLI252Test.java`. Frequency 1 → 0; max CCN 1 → 1; hotspot 0.374166 → 0.181827; delta -0.192338.

Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.

Không có commit của file trong cửa sổ sau. Frequency giảm khi các commit trước rời cửa sổ không có nghĩa code đã được sửa cho tốt hơn.

### commons-cli — cặp 1 → 2

File: `src/test/java/org/apache/commons/cli/example/XhtmlHelpAppendable.java`. Frequency 9 → 0; max CCN 7 → 7; hotspot 0.842736 → 0.398133; delta -0.444603.

Điểm hotspot là thứ hạng tương đối trong quần thể file đủ dữ liệu. Thay đổi có thể đến từ cửa sổ trượt hoặc quần thể thay đổi; không suy ra nguyên nhân lỗi chỉ từ điểm số.

Không có commit của file trong cửa sổ sau. Frequency giảm khi các commit trước rời cửa sổ không có nghĩa code đã được sửa cho tốt hơn.
