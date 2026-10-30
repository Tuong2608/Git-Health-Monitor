-- Preserve unknown measurements as NULL, not a fabricated zero. V1/V2 remain immutable.
ALTER TABLE file_metric ALTER COLUMN change_frequency DROP NOT NULL;
ALTER TABLE file_metric ALTER COLUMN churn DROP NOT NULL;
ALTER TABLE file_metric ALTER COLUMN function_count DROP NOT NULL;
ALTER TABLE file_metric ALTER COLUMN nloc DROP NOT NULL;
ALTER TABLE file_metric ADD CONSTRAINT metric_missing_requires_reason CHECK (
    (change_frequency IS NOT NULL AND churn IS NOT NULL AND function_count IS NOT NULL AND nloc IS NOT NULL)
    OR nullif(btrim(unavailable_reason),'') IS NOT NULL
);
ALTER TABLE file_metric ADD CONSTRAINT hotspot_requires_inputs CHECK (
    hotspot_score IS NULL OR (change_frequency IS NOT NULL AND max_ccn IS NOT NULL)
);
ALTER TABLE file_metric ADD CONSTRAINT hhi_requires_churn CHECK (hhi IS NULL OR (churn IS NOT NULL AND churn>0));
ALTER TABLE analysis_job ADD COLUMN retry_of bigint;
ALTER TABLE analysis_job ADD CONSTRAINT retry_same_repository FOREIGN KEY(retry_of,repository_id) REFERENCES analysis_job(id,repository_id);
CREATE INDEX job_repository_history ON analysis_job(repository_id,requested_at DESC,id DESC);
