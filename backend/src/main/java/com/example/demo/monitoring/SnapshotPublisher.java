package com.example.demo.monitoring;

import java.sql.Timestamp;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.example.demo.monitoring.AnalysisPipeline.*;
import static com.example.demo.monitoring.MonitoringStore.*;

@Service
public class SnapshotPublisher {
    private final JdbcTemplate db;
    public SnapshotPublisher(JdbcTemplate db) { this.db=db; }

    @Transactional
    public long publish(Claim c, SnapshotData data) {
        if(data==null || data.headSha()==null || !data.headSha().matches("[a-fA-F0-9]{40}|[a-fA-F0-9]{64}")
                || data.metricVersion()==null || data.metricVersion().isBlank() || data.summary()==null
                || data.files()==null || data.pairs()==null || data.commitCount()<0)
            throw new IllegalArgumentException("Invalid snapshot metadata");
        var owned=db.queryForList("SELECT id FROM analysis_job WHERE id=? AND repository_id=? AND status='RUNNING' AND claim_token=? AND lease_until>clock_timestamp() AND window_start=? AND window_end=? AND config_hash=? AND config=?::jsonb FOR UPDATE",c.id(),c.repositoryId(),c.token(),Timestamp.from(c.windowStart()),Timestamp.from(c.windowEnd()),c.configHash(),c.config());
        if(owned.isEmpty()) throw error(HttpStatus.CONFLICT,"CLAIM_EXPIRED");
        var existing=db.queryForList("SELECT id FROM analysis_snapshot WHERE repository_id=? AND head_sha=? AND window_start=? AND window_end=? AND config_hash=?",c.repositoryId(),data.headSha(),Timestamp.from(c.windowStart()),Timestamp.from(c.windowEnd()),c.configHash());
        if(!existing.isEmpty()) {
            long id=((Number)existing.getFirst().get("id")).longValue();
            complete(c,id,"NO_CHANGE"); return id;
        }
        String rules=db.queryForObject("SELECT coalesce(jsonb_agg(jsonb_build_object('id',id,'version',version,'type',type,'parameters',parameters) ORDER BY id),'[]'::jsonb)::text FROM alert_rule WHERE repository_id=? AND enabled",String.class,c.repositoryId());
        long snapshot=db.queryForObject("INSERT INTO analysis_snapshot(repository_id,created_by_job_id,head_sha,window_start,window_end,config_hash,config_version,config,commit_count,truncated,created_at,summary,pinned_rules) VALUES (?,?,?,?,?,?,?,?::jsonb,?,?,clock_timestamp(),?::jsonb,?::jsonb) RETURNING id",Long.class,c.repositoryId(),c.id(),data.headSha(),Timestamp.from(c.windowStart()),Timestamp.from(c.windowEnd()),c.configHash(),data.metricVersion(),c.config(),data.commitCount(),data.truncated(),json(data.summary()),rules);
        var ids=new HashMap<String,Long>();
        var filesByLineage=new HashMap<String,FileData>();
        for(var f:data.files()) {
            if(f.lineageKey()==null || f.lineageKey().isBlank() || f.path()==null || f.path().isBlank()
                    || f.firstSeenSha()==null || f.initialPath()==null || f.evidence()==null || f.contributions()==null
                    || ids.containsKey(f.lineageKey())) throw new IllegalArgumentException("Invalid or duplicate file lineage");
            if(f.frequency()!=null && f.frequency()>data.commitCount()) throw new IllegalArgumentException("File frequency exceeds selected commits");
            boolean unknown=f.frequency()==null || f.churn()==null || f.functions()==null || f.nloc()==null;
            if(unknown && (f.unavailableReason()==null || f.unavailableReason().isBlank())) throw new IllegalArgumentException("Unknown measurements require a reason");
            if(f.churn()==null && (!f.contributions().isEmpty() || f.hhi()!=null)) throw new IllegalArgumentException("Unknown churn cannot have measured contributions or HHI");
            if((f.frequency()==null || f.maxCcn()==null) && f.hotspot()!=null) throw new IllegalArgumentException("Hotspot requires measured frequency and CCN");
            long contributed=0;
            for(var person:f.contributions()) {
                if(person.additions()<0 || person.deletions()<0 || person.commits()<0 || (f.frequency()!=null && person.commits()>f.frequency())) throw new IllegalArgumentException("Invalid contribution counts");
                contributed=Math.addExact(contributed,Math.addExact(person.additions(),person.deletions()));
            }
            if(f.churn()!=null && contributed!=f.churn()) throw new IllegalArgumentException("Contributor churn must sum to file churn");
            long file=db.queryForObject("INSERT INTO logical_file(repository_id,first_seen_sha,initial_path,identity_evidence,lineage_key) VALUES (?,?,?,'{}',?) ON CONFLICT(repository_id,lineage_key) DO UPDATE SET lineage_key=EXCLUDED.lineage_key RETURNING id",Long.class,c.repositoryId(),f.firstSeenSha(),f.initialPath(),f.lineageKey());
            var origin=db.queryForMap("SELECT first_seen_sha,initial_path FROM logical_file WHERE id=?",file);
            if(!f.firstSeenSha().equals(origin.get("first_seen_sha")) || !f.initialPath().equals(origin.get("initial_path"))) throw new IllegalArgumentException("Lineage key reused with conflicting origin");
            ids.put(f.lineageKey(),file);
            filesByLineage.put(f.lineageKey(),f);
            db.update("INSERT INTO file_metric(snapshot_id,file_id,repository_id,path_at_snapshot,change_frequency,churn,max_ccn,avg_ccn,function_count,nloc,hotspot_score,hhi,unavailable_reason,evidence) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?::jsonb)",snapshot,file,c.repositoryId(),f.path(),f.frequency(),f.churn(),f.maxCcn(),f.avgCcn(),f.functions(),f.nloc(),f.hotspot(),f.hhi(),f.unavailableReason(),json(f.evidence()));
            for(var contribution:f.contributions()) {
                if(contribution.identityKey()==null || contribution.identityKey().isBlank()) throw new IllegalArgumentException("Missing contributor identity");
                long person=db.queryForObject("INSERT INTO contributor(repository_id,identity_key,display_name,aliases) VALUES (?,?,?,'[]') ON CONFLICT(repository_id,identity_key) DO UPDATE SET identity_key=EXCLUDED.identity_key RETURNING id",Long.class,c.repositoryId(),contribution.identityKey(),contribution.displayName());
                db.update("INSERT INTO file_contribution VALUES (?,?,?,?,?,?,?)",snapshot,file,person,c.repositoryId(),contribution.additions(),contribution.deletions(),contribution.commits());
            }
        }
        for(var pair:data.pairs()) {
            Long a=ids.get(pair.fileA()),b=ids.get(pair.fileB());
            if(a==null || b==null || a.equals(b) || pair.evidence()==null) throw new IllegalArgumentException("Invalid coupling targets");
            for(var f:List.of(filesByLineage.get(pair.fileA()),filesByLineage.get(pair.fileB()))) {
                int count=f.lineageKey().equals(pair.fileA())?pair.countA():pair.countB();
                if(f.frequency()==null || count>f.frequency()) throw new IllegalArgumentException("Coupling count requires measured file frequency");
            }
            boolean ordered=a<b;
            db.update("INSERT INTO temporal_coupling(snapshot_id,source_file_id,target_file_id,shared_commits,commit_count_a,commit_count_b,evidence) VALUES (?,?,?,?,?,?,?::jsonb)",snapshot,Math.min(a,b),Math.max(a,b),pair.shared(),ordered?pair.countA():pair.countB(),ordered?pair.countB():pair.countA(),json(pair.evidence()));
        }
        complete(c,snapshot,"CREATED");
        return snapshot;
    }
    private void complete(Claim c,long snapshot,String outcome) {
        int n=db.update("UPDATE analysis_job SET status='SUCCEEDED',outcome=?,result_snapshot_id=?,finished_at=clock_timestamp(),phase='COMPLETE',progress=100 WHERE id=? AND claim_token=? AND status='RUNNING' AND lease_until>clock_timestamp()",outcome,snapshot,c.id(),c.token());
        if(n!=1) throw error(HttpStatus.CONFLICT,"CLAIM_EXPIRED"); // Rolls back header AND all detail.
    }
}
