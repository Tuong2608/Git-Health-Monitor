\set ON_ERROR_STOP on
BEGIN;
CREATE SCHEMA week07_upgrade_review;
SET LOCAL search_path=week07_upgrade_review;
\ir ../../../backend/src/main/resources/db/migration/V1__create_tracked_repository.sql
INSERT INTO tracked_repository(url,owner,name,created_at) VALUES ('https://github.com/fixture/legacy','fixture','legacy','2026-01-01');
\ir ../../../backend/src/main/resources/db/migration/V2__snapshot_jobs_and_monitoring.sql
INSERT INTO analysis_job(id,repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (1,1,'QUEUED',now(),'2026-01-01','2026-07-01','test','{}');
INSERT INTO analysis_snapshot(id,repository_id,created_by_job_id,head_sha,window_start,window_end,config_hash,config_version,config,commit_count,truncated,created_at,summary) VALUES (1,1,1,repeat('a',40),'2026-01-01','2026-07-01','test','v1','{}',10,false,now(),'{}');
INSERT INTO logical_file(id,repository_id,first_seen_sha,initial_path,identity_evidence,lineage_key) VALUES (1,1,repeat('a',40),'Legacy.java','{}','legacy');
INSERT INTO file_metric(snapshot_id,file_id,repository_id,path_at_snapshot,change_frequency,churn,function_count,nloc,evidence) VALUES (1,1,1,'Legacy.java',0,0,0,0,'{}');
\ir ../../../backend/src/main/resources/db/migration/V3__unavailable_metrics_and_retry_provenance.sql
DO $$BEGIN
 IF NOT EXISTS(SELECT 1 FROM file_metric WHERE snapshot_id=1 AND change_frequency=0 AND churn=0 AND function_count=0 AND nloc=0) THEN RAISE EXCEPTION 'Measured zeros changed'; END IF;
 IF NOT EXISTS(SELECT 1 FROM tracked_repository WHERE id=1 AND monitoring_enabled=false AND name='legacy') THEN RAISE EXCEPTION 'Legacy repository changed'; END IF;
 IF (SELECT count(*) FROM information_schema.tables WHERE table_schema='week07_upgrade_review')<>10 THEN RAISE EXCEPTION 'Expected 10 tables'; END IF;
 RAISE NOTICE 'PASS: V1/V2 existing repository and measured zeros preserved by V3';
END $$;
UPDATE file_metric SET change_frequency=NULL,churn=NULL,function_count=NULL,nloc=NULL,unavailable_reason='LINEAGE_AMBIGUOUS' WHERE snapshot_id=1;
DO $$BEGIN
 IF NOT EXISTS(SELECT 1 FROM file_metric WHERE snapshot_id=1 AND churn IS NULL AND change_frequency IS NULL AND unavailable_reason='LINEAGE_AMBIGUOUS') THEN RAISE EXCEPTION 'Unknown metrics not preserved'; END IF;
 RAISE NOTICE 'PASS: unknown values stay NULL with reason; no extra table introduced';
END $$;
ROLLBACK;
SELECT NOT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name='week07_upgrade_review') AS temporary_schema_removed;
