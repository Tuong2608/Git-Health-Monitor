package com.example.demo.monitoring;

import java.math.BigDecimal;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.demo.monitoring.MonitoringStore.*;

@Service
public class AlertService {
    private final JdbcTemplate db;
    private final MonitoringStore store;
    public AlertService(JdbcTemplate db, MonitoringStore store) { this.db=db; this.store=store; }
    public record RuleRequest(String type, BigDecimal threshold, Integer minShared, boolean enabled, Integer version) { }
    static Map<String,Object> parameters(RuleRequest r) {
        if(r.type()==null || !Set.of("HOTSPOT_INCREASE","STRONG_COUPLING").contains(r.type()) || r.threshold()==null
            || r.threshold().signum()<0 || r.threshold().compareTo(BigDecimal.ONE)>0)
            throw new IllegalArgumentException("Supported rule type and threshold in [0,1] required");
        if(r.type().equals("STRONG_COUPLING")) {
            if(r.minShared()==null || r.minShared()<5 || r.threshold().compareTo(new BigDecimal("0.5"))<0)
                throw new IllegalArgumentException("Coupling requires minShared>=5 and threshold>=0.5");
            return Map.of("threshold",r.threshold(),"minShared",r.minShared());
        }
        return Map.of("threshold",r.threshold());
    }
    @Transactional
    public Map<String,Object> create(long repo,RuleRequest r) {
        store.repository(repo);
        long id=db.queryForObject("INSERT INTO alert_rule(repository_id,type,version,enabled,parameters,updated_at) VALUES (?,?,1,?,?::jsonb,clock_timestamp()) RETURNING id",Long.class,repo,r.type(),r.enabled(),json(parameters(r)));
        return store.one("SELECT * FROM alert_rule WHERE id=?",id);
    }
    @Transactional
    public Map<String,Object> update(long id,RuleRequest r) {
        var params=parameters(r); store.one("SELECT id FROM alert_rule WHERE id=?",id);
        int n=db.update("UPDATE alert_rule SET type=?,enabled=?,parameters=?::jsonb,version=version+1,updated_at=clock_timestamp() WHERE id=? AND version=?",r.type(),r.enabled(),json(params),id,r.version());
        if(n!=1) throw error(HttpStatus.CONFLICT,"RULE_VERSION_CONFLICT");
        return store.one("SELECT * FROM alert_rule WHERE id=?",id);
    }
    public List<Map<String,Object>> rules(long repo,int limit,int offset) {
        store.repository(repo); return db.queryForList("SELECT * FROM alert_rule WHERE repository_id=? ORDER BY id LIMIT ? OFFSET ?",repo,limit,offset);
    }
    public List<Map<String,Object>> alerts(long repo,String status,int limit,int offset) {
        store.repository(repo);
        if(!Set.of("OPEN","ACKNOWLEDGED","CLOSED","ALL").contains(status)) throw new IllegalArgumentException("Invalid alert status");
        return db.queryForList("SELECT * FROM alert WHERE repository_id=? AND (?='ALL' OR status=?) ORDER BY created_at DESC,id DESC LIMIT ? OFFSET ?",repo,status,status,limit,offset);
    }
    @Transactional
    public Map<String,Object> transition(long id,String status) {
        if(status==null || !Set.of("ACKNOWLEDGED","CLOSED").contains(status)) throw new IllegalArgumentException("status must be ACKNOWLEDGED or CLOSED");
        var old=store.one("SELECT status FROM alert WHERE id=? FOR UPDATE",id);
        if(old.get("status").equals("CLOSED") && !status.equals("CLOSED")) throw error(HttpStatus.CONFLICT,"ALERT_ALREADY_CLOSED");
        db.update("UPDATE alert SET status=?,acknowledged_at=CASE WHEN ?='ACKNOWLEDGED' THEN coalesce(acknowledged_at,clock_timestamp()) ELSE acknowledged_at END,closed_at=CASE WHEN ?='CLOSED' THEN coalesce(closed_at,clock_timestamp()) ELSE closed_at END WHERE id=?",status,status,status,id);
        return store.one("SELECT * FROM alert WHERE id=?",id);
    }
    /** Separate transaction from snapshot publication: a crash leaves durable PENDING work. */
    @Transactional
    public boolean evaluateNext() {
        var pending=db.queryForList("SELECT * FROM analysis_snapshot WHERE evaluation_status<>'COMPLETE' AND evaluation_next_at<=clock_timestamp() ORDER BY window_end,id LIMIT 1 FOR UPDATE SKIP LOCKED");
        if(pending.isEmpty()) return false;
        var s=pending.getFirst(); long id=((Number)s.get("id")).longValue();
        try { evaluate(s); }
        catch (RuntimeException failure) { throw new EvaluationFailure(id,failure); }
        db.update("UPDATE analysis_snapshot SET evaluation_status='COMPLETE',evaluation_attempts=evaluation_attempts+1,evaluation_error=NULL WHERE id=?",id);
        return true;
    }
    public static final class EvaluationFailure extends RuntimeException {
        public final long snapshotId;
        EvaluationFailure(long id,RuntimeException cause) { super("Alert evaluation failed",cause);snapshotId=id; }
    }
    // Dispatcher records an error only for its failed candidate after transaction rollback.
    public void recordFailure(long id) {
        db.update("UPDATE analysis_snapshot SET evaluation_status='FAILED',evaluation_attempts=evaluation_attempts+1,evaluation_error='ALERT_EVALUATION_FAILED',evaluation_next_at=clock_timestamp()+interval '60 seconds' WHERE id=? AND evaluation_status<>'COMPLETE'",id);
    }
    void evaluate(Map<String,Object> s) {
        long id=((Number)s.get("id")).longValue(), repo=((Number)s.get("repository_id")).longValue();
        var baseline=db.queryForList("SELECT * FROM analysis_snapshot WHERE repository_id=? AND (window_end,created_at,id)<(?,?,?) AND config_hash=? AND config_version=? AND window_end-window_start=(?::timestamptz-?::timestamptz) ORDER BY window_end DESC,created_at DESC,id DESC LIMIT 1",repo,s.get("window_end"),s.get("created_at"),id,s.get("config_hash"),s.get("config_version"),s.get("window_end"),s.get("window_start"));
        var rules=JSON.readTree(s.get("pinned_rules").toString());
        var notes=new ArrayList<Map<String,Object>>();
        boolean complete=Boolean.FALSE.equals(s.get("truncated")) && JSON.readTree(s.get("summary").toString()).path("coverageComplete").asBoolean(false);
        Map<String,Object> old=baseline.isEmpty()?null:baseline.getFirst();
        boolean comparable=complete && old!=null && Boolean.FALSE.equals(old.get("truncated")) && JSON.readTree(old.get("summary").toString()).path("coverageComplete").asBoolean(false);
        for(var rule:rules) {
            long ruleId=rule.path("id").asLong(); int version=rule.path("version").asInt();
            String type=rule.path("type").asText(); var p=rule.path("parameters");
            BigDecimal threshold=new BigDecimal(p.path("threshold").asText());
            if(type.equals("STRONG_COUPLING")) {
                if(!complete) { notes.add(Map.of("ruleId",ruleId,"reason","INCOMPLETE_COVERAGE"));continue; }
                for(var pair:db.queryForList("SELECT * FROM temporal_coupling WHERE snapshot_id=? AND shared_commits>=? AND coupling_degree>=?",id,p.path("minShared").asInt(),threshold)) {
                    insert(repo,ruleId,version,rule.toString(),id,null,((Number)pair.get("source_file_id")).longValue(),((Number)pair.get("target_file_id")).longValue(),pair.get("coupling_degree"),threshold,json(Map.of("sharedCommits",pair.get("shared_commits"),"countA",pair.get("commit_count_a"),"countB",pair.get("commit_count_b"),"metricEvidence",JSON.readTree(pair.get("evidence").toString()))));
                }
            } else if(type.equals("HOTSPOT_INCREASE")) {
                if(!comparable) { notes.add(Map.of("ruleId",ruleId,"reason",old==null?"NO_COMPATIBLE_BASELINE":"INCOMPLETE_COVERAGE"));continue; }
                long oldId=((Number)old.get("id")).longValue();
                for(var file:db.queryForList("SELECT n.file_id,n.hotspot_score AS new_score,o.hotspot_score AS old_score,n.hotspot_score-o.hotspot_score AS delta,n.evidence FROM file_metric n JOIN file_metric o ON o.file_id=n.file_id AND o.snapshot_id=? WHERE n.snapshot_id=? AND n.hotspot_score-o.hotspot_score>?",oldId,id,threshold)) {
                    insert(repo,ruleId,version,rule.toString(),id,oldId,((Number)file.get("file_id")).longValue(),null,file.get("delta"),threshold,json(Map.of("before",file.get("old_score"),"after",file.get("new_score"),"delta",file.get("delta"),"metricEvidence",JSON.readTree(file.get("evidence").toString()))));
                }
                int missing=db.queryForObject("SELECT count(*) FROM file_metric n LEFT JOIN file_metric o ON o.file_id=n.file_id AND o.snapshot_id=? WHERE n.snapshot_id=? AND (n.hotspot_score IS NULL OR o.hotspot_score IS NULL)",Integer.class,oldId,id);
                if(missing>0) notes.add(Map.of("ruleId",ruleId,"reason","MISSING_BASELINE_OR_VALUE","files",missing));
            } else throw new IllegalArgumentException("Unsupported pinned rule");
        }
        db.update("UPDATE analysis_snapshot SET evaluation_notes=?::jsonb WHERE id=?",json(notes),id);
    }
    private void insert(long repo,long rule,int version,String config,long snapshot,Long baseline,long file,Long related,Object value,BigDecimal threshold,String evidence) {
        db.update("INSERT INTO alert(repository_id,rule_id,rule_version,rule_config,snapshot_id,baseline_snapshot_id,file_id,related_file_id,status,observed_value,threshold,evidence,created_at) VALUES (?,?,?,?::jsonb,?,?,?,?,'OPEN',?,?,?::jsonb,clock_timestamp()) ON CONFLICT DO NOTHING",repo,rule,version,config,snapshot,baseline,file,related,value,threshold,evidence);
    }
}
