package com.example.demo.monitoring;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.demo.monitoring.MonitoringStore.*;

/** Read-only views of published snapshots. IDs, never paths, select file resources. */
@Service
@Transactional(readOnly=true)
public class SnapshotQueries {
    private final JdbcTemplate db;
    private final MonitoringStore store;
    private static final List<String> METRICS=List.of("change_frequency","churn","max_ccn","avg_ccn","function_count","nloc","hotspot_score","hhi");
    public SnapshotQueries(JdbcTemplate db,MonitoringStore store) { this.db=db;this.store=store; }
    public Map<String,Object> file(long snapshot,long file) {
        var result=store.one("SELECT snapshot_id,file_id,repository_id,path_at_snapshot,change_frequency,churn,max_ccn,avg_ccn,function_count,nloc,hotspot_score,hhi,unavailable_reason FROM file_metric WHERE snapshot_id=? AND file_id=?",snapshot,file);
        result.put("evidence_count",db.queryForObject("SELECT CASE WHEN jsonb_typeof(evidence->'commits')='array' THEN jsonb_array_length(evidence->'commits') ELSE 0 END FROM file_metric WHERE snapshot_id=? AND file_id=?",Integer.class,snapshot,file));
        result.put("evidence_available",db.queryForObject("SELECT coalesce(jsonb_typeof(evidence->'commits')='array',false) FROM file_metric WHERE snapshot_id=? AND file_id=?",Boolean.class,snapshot,file));
        return result;
    }
    public List<Map<String,Object>> contributions(long snapshot,long file,int limit,int offset) {
        file(snapshot,file);
        // Deliberately exclude identity_key/aliases (may contain raw author email).
        return db.queryForList("SELECT c.id AS contributor_id,c.display_name,fc.additions,fc.deletions,fc.commit_count,CASE WHEN fm.churn>0 THEN (fc.additions::numeric+fc.deletions)/fm.churn END AS share FROM file_contribution fc JOIN contributor c ON c.id=fc.contributor_id JOIN file_metric fm ON fm.snapshot_id=fc.snapshot_id AND fm.file_id=fc.file_id WHERE fc.snapshot_id=? AND fc.file_id=? ORDER BY (fc.additions::numeric+fc.deletions) DESC,c.id LIMIT ? OFFSET ?",snapshot,file,limit,offset);
    }
    public List<Map<String,Object>> evidence(long snapshot,long file,int limit,int offset) {
        file(snapshot,file);
        return db.queryForList("SELECT (e.ordinality-1) AS evidence_index,e.item AS evidence FROM file_metric f CROSS JOIN LATERAL jsonb_array_elements(CASE WHEN jsonb_typeof(f.evidence->'commits')='array' THEN f.evidence->'commits' ELSE '[]'::jsonb END) WITH ORDINALITY AS e(item,ordinality) WHERE f.snapshot_id=? AND f.file_id=? ORDER BY e.ordinality LIMIT ? OFFSET ?",snapshot,file,limit,offset);
    }
    public List<Map<String,Object>> couplings(long snapshot,long file,int limit,int offset) {
        file(snapshot,file);
        return db.queryForList("SELECT tc.snapshot_id,?::bigint AS file_id,CASE WHEN tc.source_file_id=? THEN tc.target_file_id ELSE tc.source_file_id END AS related_file_id,f.path_at_snapshot AS related_path,tc.shared_commits,CASE WHEN tc.source_file_id=? THEN tc.commit_count_a ELSE tc.commit_count_b END AS file_commit_count,CASE WHEN tc.source_file_id=? THEN tc.commit_count_b ELSE tc.commit_count_a END AS related_commit_count,tc.coupling_degree FROM temporal_coupling tc JOIN file_metric f ON f.snapshot_id=tc.snapshot_id AND f.file_id=CASE WHEN tc.source_file_id=? THEN tc.target_file_id ELSE tc.source_file_id END WHERE tc.snapshot_id=? AND (tc.source_file_id=? OR tc.target_file_id=?) AND tc.shared_commits>=5 AND tc.coupling_degree>=0.5 ORDER BY tc.coupling_degree DESC,f.file_id LIMIT ? OFFSET ?",file,file,file,file,file,snapshot,file,file,limit,offset);
    }
    public List<Map<String,Object>> couplingEvidence(long snapshot,long file,long related,int limit,int offset) {
        file(snapshot,file);file(snapshot,related);
        store.one("SELECT snapshot_id FROM temporal_coupling WHERE snapshot_id=? AND source_file_id=? AND target_file_id=?",snapshot,Math.min(file,related),Math.max(file,related));
        return db.queryForList("SELECT (e.ordinality-1) AS evidence_index,e.item AS evidence FROM temporal_coupling c CROSS JOIN LATERAL jsonb_array_elements(CASE WHEN jsonb_typeof(c.evidence->'commits')='array' THEN c.evidence->'commits' ELSE '[]'::jsonb END) WITH ORDINALITY e(item,ordinality) WHERE c.snapshot_id=? AND c.source_file_id=? AND c.target_file_id=? ORDER BY e.ordinality LIMIT ? OFFSET ?",snapshot,Math.min(file,related),Math.max(file,related),limit,offset);
    }
    public List<Map<String,Object>> trend(long repo,long file,int limit,int offset) {
        store.one("SELECT id FROM logical_file WHERE id=? AND repository_id=?",file,repo);
        return db.queryForList("SELECT s.id AS snapshot_id,s.head_sha,s.window_start,s.window_end,s.config_hash,s.config_version,s.truncated,s.created_at,s.summary->'coverageComplete' AS coverage_complete,f.path_at_snapshot,f.change_frequency,f.churn,f.max_ccn,f.hotspot_score,f.hhi,f.unavailable_reason FROM analysis_snapshot s JOIN file_metric f ON f.snapshot_id=s.id WHERE s.repository_id=? AND f.file_id=? ORDER BY s.window_end,s.created_at,s.id LIMIT ? OFFSET ?",repo,file,limit,offset);
    }
    public Map<String,Object> comparison(long repo,long from,long to,int page,int size) {
        int offset=MonitoringController.offset(page,size);
        var a=store.snapshot(from);var b=store.snapshot(to);
        if(((Number)a.get("repository_id")).longValue()!=repo || ((Number)b.get("repository_id")).longValue()!=repo)
            throw error(HttpStatus.NOT_FOUND,"SNAPSHOT_NOT_IN_REPOSITORY");
        if(from==to || compareOrder(a,b)>=0) throw new IllegalArgumentException("from snapshot must precede to snapshot by windowEnd/createdAt/id");
        if(!Objects.equals(a.get("config_hash"),b.get("config_hash")) || !Objects.equals(a.get("config_version"),b.get("config_version"))
                || !duration(a).equals(duration(b))) throw error(HttpStatus.CONFLICT,"SNAPSHOTS_NOT_COMPARABLE_CONFIG");
        if(!complete(a) || !complete(b)) throw error(HttpStatus.CONFLICT,"SNAPSHOTS_NOT_COMPARABLE_COVERAGE");
        // LEFT/RIGHT universe is preserved, including removed files. No null is converted to zero.
        String columns="file_id,path_at_snapshot,"+String.join(",",METRICS);
        var rows=db.queryForList("WITH a AS (SELECT "+columns+" FROM file_metric WHERE snapshot_id=?), b AS (SELECT "+columns+" FROM file_metric WHERE snapshot_id=?) SELECT coalesce(a.file_id,b.file_id) AS file_id,to_jsonb(a) AS before,to_jsonb(b) AS after FROM a FULL OUTER JOIN b ON a.file_id=b.file_id ORDER BY coalesce(a.file_id,b.file_id) LIMIT ? OFFSET ?",from,to,size+1,offset);
        var items=new ArrayList<Map<String,Object>>();
        for(var row:rows.stream().limit(size).toList()) {
            var before=row.get("before")==null?null:JSON.readTree(row.get("before").toString());
            var after=row.get("after")==null?null:JSON.readTree(row.get("after").toString());
            var item=new LinkedHashMap<String,Object>();item.put("fileId",row.get("file_id"));
            item.put("beforePath",before==null?null:before.path("path_at_snapshot").asText());item.put("afterPath",after==null?null:after.path("path_at_snapshot").asText());
            String state=before==null?"ADDED":after==null?"REMOVED":"UNCHANGED";
            boolean changed=false,unavailable=false;
            var deltas=new LinkedHashMap<String,Object>();
            for(String key:METRICS) {
                BigDecimal x=before==null || !before.path(key).isNumber()?null:before.path(key).decimalValue();
                BigDecimal y=after==null || !after.path(key).isNumber()?null:after.path(key).decimalValue();
                var d=new LinkedHashMap<String,Object>();d.put("before",x);d.put("after",y);
                BigDecimal delta=x==null || y==null?null:y.subtract(x);
                d.put("absoluteDelta",delta);d.put("relativeDelta",delta==null || x.signum()==0?null:delta.divide(x.abs(),MathContext.DECIMAL64));
                d.put("reason",x==null || y==null?"UNAVAILABLE":x.signum()==0?"BASE_ZERO":null);
                if(delta==null) unavailable=true;else if(delta.signum()!=0) changed=true;
                deltas.put(MonitoringController.camel(key),d);
            }
            if(before!=null && after!=null) {
                changed|=!Objects.equals(item.get("beforePath"),item.get("afterPath"));
                state=unavailable?"UNAVAILABLE":changed?"CHANGED":"UNCHANGED";
            }
            item.put("state",state);item.put("metrics",deltas);items.add(item);
        }
        return Map.of("fromSnapshot",MonitoringController.dto(a),"toSnapshot",MonitoringController.dto(b),"items",items,"page",page,"size",size,"hasNext",rows.size()>size);
    }
    private static Duration duration(Map<String,Object> s) { return Duration.between(((Timestamp)s.get("window_start")).toInstant(),((Timestamp)s.get("window_end")).toInstant()); }
    private static boolean complete(Map<String,Object> s) { return Boolean.FALSE.equals(s.get("truncated")) && JSON.readTree(s.get("summary").toString()).path("coverageComplete").asBoolean(false); }
    private static int compareOrder(Map<String,Object> a,Map<String,Object> b) {
        for(String key:List.of("window_end","created_at")) { int cmp=((Timestamp)a.get(key)).compareTo((Timestamp)b.get(key));if(cmp!=0)return cmp; }
        return Long.compare(((Number)a.get("id")).longValue(),((Number)b.get("id")).longValue());
    }
}
