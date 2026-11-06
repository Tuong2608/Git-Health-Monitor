# Bàn giao tuần 8 cho Tưởng

Toản đã nối dashboard React vào API thật, thêm AI preview/review/audit, Docker frontend/backend và kiểm chứng persistence của GitAnalysisPipeline trên PostgreSQL.

## Minh chứng chính

- 111 tests backend duy nhất đạt: full verify 109, thêm 2 ca disabled/no provider. 11 UI tests đạt, gồm 2 live cases.
- Public repos: gs-rest-service (6 Java/29 selected commits), petclinic (50 Java/49 selected), Hello-World (0 Java/0 selected), cửa sổ 365 ngày. [SHA và kết quả](evidence/public-repositories-results.json).
- Hai repo Java có ambiguity nên coverageComplete=false; không đổi policy của bạn hoặc gán missing thành 0.
- API AI dùng source tại snapshot SHA; preview exact payload, hash ràng buộc consent; test provider chỉ nằm trong tests.

## Các điểm nhờ bạn review

1. `ReviewContextBuilder`/`AiReviewController`: thêm endpoint preview, request startLine/endLine/contextHash/consent; v1 chưa có diff selection. [Contract](api-and-ai-contract.md).
2. Đăng ký bean LLMProviderAdapter thật của bạn, chốt provider/model/data retention và budget. Adapter phải thực thi timeout/cancel/cap response khi đọc. Không bật public trước khi có authorization.
3. Effective config nay thêm pipelineVersion, Lizard/JGit version và aliases để snapshot không bị gắn chung config với pipeline cũ; snapshot cũ/new config không so sánh được là đúng.
4. V4 chỉ audit metadata. Git cache cần volume giữ bền để đọc source đúng SHA, không dùng HEAD hiện tại.
5. Image Java cũ chưa có Lizard; Docker/Compose mới đã thêm Python và lizard 1.24.3. CI cài Lizard cho tests.
6. Staging cũ chưa dùng được: Vercel 410, backend capabilities 404. Cần URL/project deploy mới và kiểm tra sau merge. Toản chưa tự merge/deploy thay nhóm.
7. Audit tooling minimatch còn 10 moderate, 0 high/critical. Theo dõi bản cập nhật tương thích; không force downgrade ESLint để che cảnh báo.

Task 24/26 đã có mã và minh chứng; task chung 27/28 còn cần xác nhận staging/provider thật trước khi nhóm đánh dấu nghiệm thu toàn M4. Split/tie/dataset/nhãn thực nghiệm chưa tự chốt.
