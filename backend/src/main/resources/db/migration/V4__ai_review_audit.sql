-- Audit metadata only. Raw source, prompts and provider output are not stored.
CREATE TABLE ai_review_audit (
  review_id text PRIMARY KEY,
  snapshot_id bigint NOT NULL,
  file_id bigint NOT NULL,
  created_at timestamptz NOT NULL,
  metadata jsonb NOT NULL,
  FOREIGN KEY (snapshot_id,file_id) REFERENCES file_metric(snapshot_id,file_id)
);
