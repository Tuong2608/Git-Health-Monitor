\set ON_ERROR_STOP on
BEGIN;
CREATE SCHEMA week06_upgrade_review;
SET LOCAL search_path=week06_upgrade_review;
\ir ../../../backend/src/main/resources/db/migration/V1__create_tracked_repository.sql
INSERT INTO tracked_repository(url,owner,name,created_at) VALUES ('https://github.com/fixture/legacy','fixture','legacy','2026-01-01');
\ir ../../../backend/src/main/resources/db/migration/V2__snapshot_jobs_and_monitoring.sql
DO $$BEGIN
 IF NOT EXISTS(SELECT 1 FROM tracked_repository WHERE id=1 AND name='legacy' AND monitoring_enabled=false AND interval_minutes=1440 AND created_at='2026-01-01') THEN RAISE EXCEPTION 'Legacy row/defaults changed'; END IF;
 IF (SELECT count(*) FROM information_schema.tables WHERE table_schema='week06_upgrade_review')<>10 THEN RAISE EXCEPTION 'Expected 10 tables'; END IF;
 RAISE NOTICE 'PASS: V1 existing row preserved, monitoring remains disabled, 10 application tables';
END $$;
ROLLBACK;
SELECT NOT EXISTS(SELECT 1 FROM information_schema.schemata WHERE schema_name='week06_upgrade_review') AS temporary_schema_removed;
