\set ON_ERROR_STOP on
-- psql -d DISPOSABLE_REVIEW_DB -f docs/week-05/evidence/verify-schema.sql
-- All objects and fixtures are rolled back, including the isolated schema.
BEGIN;
CREATE SCHEMA week05_design_review;
SET LOCAL search_path=week05_design_review;
\ir ../schema-reference.sql
CREATE FUNCTION expect_rejection(label text, statement text, expected text) RETURNS void LANGUAGE plpgsql AS $$
DECLARE caught text;
BEGIN
 BEGIN EXECUTE statement; EXCEPTION WHEN OTHERS THEN GET STACKED DIAGNOSTICS caught=RETURNED_SQLSTATE; END;
 IF caught IS DISTINCT FROM expected THEN RAISE EXCEPTION '%: expected %, got %',label,expected,caught; END IF;
 RAISE NOTICE 'PASS: % (%)',label,caught;
END $$;
INSERT INTO tracked_repository(id,url,owner,name,created_at) VALUES (1,'https://github.com/example/a','example','a',now()),(2,'https://github.com/example/b','example','b',now());
INSERT INTO analysis_job(id,repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (1,1,'QUEUED',now(),'2026-01-01','2026-07-01','config1','{}');
SELECT expect_rejection('one active job per repository',$q$INSERT INTO analysis_job(repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (1,'QUEUED',now(),'2026-01-01','2026-07-01','config1','{}')$q$,'23505');
SELECT expect_rejection('running requires claim and lease',$q$UPDATE analysis_job SET status='RUNNING' WHERE id=1$q$,'23514');
UPDATE analysis_job SET status='RUNNING',started_at=now(),claim_token='00000000-0000-0000-0000-000000000001',lease_until=now()+interval '120 seconds' WHERE id=1;
INSERT INTO analysis_snapshot(id,repository_id,created_by_job_id,head_sha,window_start,window_end,config_hash,config_version,config,commit_count,truncated,created_at,summary) VALUES (1,1,1,'demo-sha','2026-01-01','2026-07-01','config1','v1','{}',20,false,now(),'{}');
UPDATE analysis_job SET status='SUCCEEDED',outcome='CREATED',result_snapshot_id=1,finished_at=now() WHERE id=1;
INSERT INTO analysis_job(id,repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (3,1,'QUEUED',now(),'2026-01-01','2026-07-01','config1','{}');
SELECT expect_rejection('snapshot identity dedupe',$q$INSERT INTO analysis_snapshot(repository_id,created_by_job_id,head_sha,window_start,window_end,config_hash,config_version,config,commit_count,truncated,created_at,summary) VALUES (1,3,'demo-sha','2026-01-01','2026-07-01','config1','v1','{}',20,false,now(),'{}')$q$,'23505');
UPDATE analysis_job SET status='SUCCEEDED',outcome='NO_CHANGE',result_snapshot_id=1,finished_at=now() WHERE id=3;
INSERT INTO logical_file(id,repository_id,first_seen_sha,initial_path,identity_evidence) VALUES (1,1,'demo','A.java','{}'),(2,1,'demo','B.java','{}'),(3,2,'demo','C.java','{}');
INSERT INTO file_metric(snapshot_id,file_id,repository_id,path_at_snapshot,change_frequency,churn,function_count,nloc,evidence) VALUES (1,1,1,'A.java',10,100,2,50,'{}'),(1,2,1,'B.java',20,200,4,100,'{}');
SELECT expect_rejection('cross repository file blocked',$q$INSERT INTO file_metric(snapshot_id,file_id,repository_id,path_at_snapshot,change_frequency,churn,function_count,nloc,evidence) VALUES (1,3,1,'C.java',1,1,1,1,'{}')$q$,'23503');
SELECT expect_rejection('negative frequency blocked',$q$UPDATE file_metric SET change_frequency=-1 WHERE file_id=1$q$,'23514');
INSERT INTO temporal_coupling VALUES (1,1,2,5,10,20,DEFAULT,'{}');
DO $$BEGIN IF (SELECT coupling_degree FROM temporal_coupling)<>0.5 THEN RAISE EXCEPTION 'wrong coupling formula'; END IF; RAISE NOTICE 'PASS: coupling = 5/min(10,20) = 0.5'; END $$;
SELECT expect_rejection('reversed pair blocked',$q$INSERT INTO temporal_coupling VALUES (1,2,1,5,20,10,DEFAULT,'{}')$q$,'23514');
SELECT expect_rejection('self pair blocked',$q$INSERT INTO temporal_coupling VALUES (1,1,1,5,10,10,DEFAULT,'{}')$q$,'23514');
SELECT expect_rejection('impossible shared count blocked',$q$UPDATE temporal_coupling SET shared_commits=11$q$,'23514');
INSERT INTO contributor(id,repository_id,identity_key,display_name,aliases) VALUES (1,1,'local1','Person A','[]'),(2,2,'local2','Person B','[]');
INSERT INTO file_contribution VALUES (1,1,1,1,70,30,10);
SELECT expect_rejection('cross repository contributor blocked',$q$INSERT INTO file_contribution VALUES (1,1,2,1,70,30,10)$q$,'23503');
INSERT INTO alert_rule(id,repository_id,type,version,enabled,parameters,updated_at) VALUES (1,1,'HOTSPOT_INCREASE',1,true,'{}',now());
INSERT INTO alert(repository_id,rule_id,rule_version,rule_config,snapshot_id,file_id,status,observed_value,threshold,evidence,created_at) VALUES (1,1,1,'{}',1,1,'OPEN',0.07,0.05,'{}',now());
SELECT expect_rejection('NULL related file still deduplicated',$q$INSERT INTO alert(repository_id,rule_id,rule_version,rule_config,snapshot_id,file_id,status,observed_value,threshold,evidence,created_at) VALUES (1,1,1,'{}',1,1,'OPEN',0.07,0.05,'{}',now())$q$,'23505');
DO $$BEGIN IF (SELECT count(*) FROM information_schema.tables WHERE table_schema='week05_design_review')<>10 THEN RAISE EXCEPTION 'expected 10 tables'; END IF; RAISE NOTICE 'PASS: exactly 10 tables; CREATED and NO_CHANGE reference same published snapshot'; END $$;
SELECT version();
ROLLBACK;
SELECT NOT EXISTS (SELECT 1 FROM information_schema.schemata WHERE schema_name='week05_design_review') AS review_schema_removed;
