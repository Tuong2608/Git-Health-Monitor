package com.example.demo.monitoring;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import static com.example.demo.monitoring.AnalysisPipeline.*;

@Service
public class MonitoringStore {
    final JdbcTemplate db;
    static final JsonMapper JSON = JsonMapper.builder().build();
    public MonitoringStore(JdbcTemplate db) { this.db=db; }
    static String json(Object value) { return JSON.writeValueAsString(value); }
    static String hash(String text) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8))); }
        catch (java.security.NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    static ResponseStatusException error(HttpStatus status,String code) { return new ResponseStatusException(status,code); }
    Map<String,Object> one(String sql,Object... args) {
        var rows=db.queryForList(sql,args);
        if(rows.isEmpty()) throw error(HttpStatus.NOT_FOUND,"RESOURCE_NOT_FOUND");
        return rows.getFirst();
    }
    public void repository(long id) { one("SELECT id FROM tracked_repository WHERE id=?",id); }
    public Map<String,Object> settings(long id) {
        return one("SELECT id AS repository_id,monitoring_enabled,interval_minutes,next_scan_at,monitoring_version FROM tracked_repository WHERE id=?",id);
    }
    @Transactional
    public Map<String,Object> settings(long id, boolean enabled, int interval, int version) {
        if(interval<60 || interval>43200) throw new IllegalArgumentException("intervalMinutes must be 60..43200");
        repository(id);
        int n=db.update("UPDATE tracked_repository SET monitoring_enabled=?,interval_minutes=?,next_scan_at=CASE WHEN ? THEN clock_timestamp() ELSE NULL END,monitoring_version=monitoring_version+1 WHERE id=? AND monitoring_version=?",enabled,interval,enabled,id,version);
        if(n!=1) throw error(HttpStatus.CONFLICT,"SCHEDULE_VERSION_CONFLICT");
        return settings(id);
    }
    @Transactional
    public long enqueue(long repo, int days, String config) {
        if(days<1 || days>365) throw new IllegalArgumentException("observationDays must be 1..365");
        one("SELECT id FROM tracked_repository WHERE id=? FOR UPDATE",repo);
        Instant end=db.queryForObject("SELECT clock_timestamp()",Timestamp.class).toInstant();
        return insertJob(repo,end.minus(Duration.ofDays(days)),end,config,"MANUAL");
    }
    long insertJob(long repo,Instant start,Instant end,String config,String trigger) {
        if(Boolean.TRUE.equals(db.queryForObject("SELECT EXISTS(SELECT 1 FROM analysis_job WHERE repository_id=? AND status IN ('QUEUED','RUNNING'))",Boolean.class,repo)))
            throw error(HttpStatus.CONFLICT,"ACTIVE_JOB_EXISTS");
        // Durable backlog is also bounded, across app instances. All enqueue paths take this transaction lock.
        db.execute("SELECT pg_advisory_xact_lock(62023001)");
        if(db.queryForObject("SELECT count(*) FROM analysis_job WHERE status='QUEUED'",Integer.class)>=20)
            throw error(HttpStatus.TOO_MANY_REQUESTS,"QUEUE_FULL");
        if(!JSON.readTree(config).isObject()) throw new IllegalArgumentException("Provider configuration must be a JSON object");
        return db.queryForObject("INSERT INTO analysis_job(repository_id,status,requested_at,window_start,window_end,config_hash,config,trigger_type) VALUES (?,'QUEUED',clock_timestamp(),?,?,?,?::jsonb,?) RETURNING id",Long.class,repo,Timestamp.from(start),Timestamp.from(end),hash(config),config,trigger);
    }
    @Transactional
    public int scheduleDue(String config) {
        var rows=db.queryForList("SELECT id,interval_minutes FROM tracked_repository WHERE monitoring_enabled AND next_scan_at<=clock_timestamp() ORDER BY next_scan_at,id LIMIT 20 FOR UPDATE SKIP LOCKED");
        int queued=0;
        for(var row:rows) {
            long id=((Number)row.get("id")).longValue();
            try {
                Instant end=db.queryForObject("SELECT clock_timestamp()",Timestamp.class).toInstant();
                insertJob(id,end.minus(Duration.ofDays(180)),end,config,"SCHEDULED");
                // Coalesce missed ticks; never replay downtime. Preserve the original cadence.
                db.update("UPDATE tracked_repository SET next_scan_at=next_scan_at+(floor(extract(epoch FROM (clock_timestamp()-next_scan_at))/(interval_minutes*60))+1)*interval_minutes*interval '1 minute' WHERE id=?",id);
                queued++;
            } catch(ResponseStatusException busy) {
                if(busy.getStatusCode().value()!=409 && busy.getStatusCode().value()!=429) throw busy;
                db.update("UPDATE tracked_repository SET next_scan_at=clock_timestamp()+interval '60 seconds' WHERE id=?",id);
            }
        }
        return queued;
    }
    @Transactional
    public Optional<Claim> claim() {
        var rows=db.queryForList("SELECT j.*,r.url FROM analysis_job j JOIN tracked_repository r ON r.id=j.repository_id WHERE j.status='QUEUED' ORDER BY j.requested_at,j.id LIMIT 1 FOR UPDATE OF j SKIP LOCKED");
        if(rows.isEmpty()) return Optional.empty();
        var j=rows.getFirst(); long id=((Number)j.get("id")).longValue(); UUID token=UUID.randomUUID();
        db.update("UPDATE analysis_job SET status='RUNNING',started_at=clock_timestamp(),claim_token=?,lease_until=clock_timestamp()+interval '120 seconds',heartbeat_at=clock_timestamp(),phase='ANALYZING' WHERE id=?",token,id);
        return Optional.of(new Claim(id,((Number)j.get("repository_id")).longValue(),(String)j.get("url"),token,((Timestamp)j.get("window_start")).toInstant(),((Timestamp)j.get("window_end")).toInstant(),(String)j.get("config_hash"),j.get("config").toString()));
    }
    public boolean heartbeat(Claim c) {
        return db.update("UPDATE analysis_job SET heartbeat_at=clock_timestamp(),lease_until=clock_timestamp()+interval '120 seconds' WHERE id=? AND claim_token=? AND status='RUNNING' AND lease_until>clock_timestamp()",c.id(),c.token())==1;
    }
    public void fail(Claim c,String code) {
        db.update("UPDATE analysis_job SET status='FAILED',finished_at=clock_timestamp(),error_code=?,phase='FAILED' WHERE id=? AND claim_token=? AND status='RUNNING'",code,c.id(),c.token());
    }
    public int recoverExpired() {
        return db.update("UPDATE analysis_job SET status='FAILED',finished_at=clock_timestamp(),error_code='LEASE_EXPIRED',phase='FAILED' WHERE status='RUNNING' AND lease_until<=clock_timestamp()");
    }
    public Map<String,Object> job(long id) {
        return one("SELECT id,repository_id,status,outcome,phase,progress,requested_at,started_at,finished_at,window_start,window_end,result_snapshot_id AS snapshot_id,error_code,retry_of FROM analysis_job WHERE id=?",id);
    }
    public List<Map<String,Object>> jobs(long repo,int limit,int offset) {
        repository(repo);
        return db.queryForList("SELECT id,repository_id,status,outcome,phase,progress,requested_at,started_at,finished_at,result_snapshot_id AS snapshot_id,error_code,retry_of FROM analysis_job WHERE repository_id=? ORDER BY requested_at DESC,id DESC LIMIT ? OFFSET ?",repo,limit,offset);
    }
    @Transactional
    public long retry(long jobId,String currentConfig) {
        var previous=one("SELECT repository_id FROM analysis_job WHERE id=?",jobId);
        long repo=((Number)previous.get("repository_id")).longValue();
        one("SELECT id FROM tracked_repository WHERE id=? FOR UPDATE",repo);
        var job=one("SELECT * FROM analysis_job WHERE id=? FOR UPDATE",jobId);
        if(!job.get("status").equals("FAILED")) throw error(HttpStatus.CONFLICT,"ONLY_FAILED_JOB_CAN_RETRY");
        if(!job.get("config_hash").equals(hash(currentConfig))) throw error(HttpStatus.CONFLICT,"RETRY_CONFIG_CHANGED");
        long next=insertJob(repo,((Timestamp)job.get("window_start")).toInstant(),((Timestamp)job.get("window_end")).toInstant(),currentConfig,"MANUAL");
        db.update("UPDATE analysis_job SET retry_of=? WHERE id=?",jobId,next);
        return next;
    }
    public List<Map<String,Object>> snapshots(long repo,int limit,int offset) {
        repository(repo);
        return db.queryForList("SELECT id,repository_id,head_sha,window_start,window_end,config_hash,config_version,commit_count,truncated,created_at,evaluation_status,evaluation_attempts,evaluation_error FROM analysis_snapshot WHERE repository_id=? ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?",repo,limit,offset);
    }
    public Map<String,Object> snapshot(long id) {
        return one("SELECT id,repository_id,head_sha,window_start,window_end,config_hash,config_version,commit_count,truncated,created_at,summary,evaluation_status,evaluation_notes FROM analysis_snapshot WHERE id=?",id);
    }
    public List<Map<String,Object>> files(long snapshot,int limit,int offset) {
        snapshot(snapshot);
        return db.queryForList("SELECT file_id,path_at_snapshot,change_frequency,churn,max_ccn,avg_ccn,function_count,nloc,hotspot_score,hhi,unavailable_reason FROM file_metric WHERE snapshot_id=? ORDER BY hotspot_score DESC NULLS LAST,path_at_snapshot LIMIT ? OFFSET ?",snapshot,limit,offset);
    }
}
