package com.example.demo.monitoring;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import static org.assertj.core.api.Assertions.*;
import static com.example.demo.monitoring.AnalysisPipeline.*;

/** Real PostgreSQL tests. DB_* MUST point at a disposable test database. */
@SpringBootTest(properties={"app.analysis.enabled=false","app.analysis.writes-enabled=false"})
class MonitoringIntegrationTests {
    @Autowired MonitoringStore store;
    @Autowired SnapshotPublisher publisher;
    @Autowired AlertService alerts;
    @Autowired JdbcTemplate db;
    static final String CONFIG="{\"metric\":\"v1\",\"tool\":\"fixture-only\"}";
    static final Instant END=Instant.parse("2026-07-01T00:00:00Z");
    @BeforeEach void clean() {
        db.execute("TRUNCATE alert,file_contribution,temporal_coupling,file_metric,analysis_snapshot,analysis_job,logical_file,contributor,alert_rule,tracked_repository RESTART IDENTITY CASCADE");
    }
    long repo() {
        String name=UUID.randomUUID().toString();
        return db.queryForObject("INSERT INTO tracked_repository(url,owner,name,created_at) VALUES (?,'test',?,now()) RETURNING id",Long.class,"https://github.com/test/"+name,name);
    }
    Claim fixed(long repo,Instant end,String config) {
        db.update("INSERT INTO analysis_job(repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (?,'QUEUED',now(),?,?,?,?::jsonb)",repo,Timestamp.from(end.minusSeconds(180L*86400)),Timestamp.from(end),MonitoringStore.hash(config),config);
        return store.claim().orElseThrow();
    }
    static SnapshotData data(String score,boolean complete,boolean truncated) {
        return new SnapshotData("a".repeat(40),"metric-v1",20,truncated,Map.of("coverageComplete",complete,"source","TEST_FIXTURE"),List.of(
            file("A","A.java",score,10),file("B","B.java","0.2",20)),
            List.of(new PairData("A","B",5,10,20,Map.of("commitShas",List.of("b".repeat(40))))));
    }
    static FileData file(String key,String path,String score,int frequency) {
        return new FileData(key,"a".repeat(40),key+".java",path,frequency,100L,new BigDecimal("10"),new BigDecimal("5"),2,50L,score==null?null:new BigDecimal(score),BigDecimal.ONE,score==null?"CCN_UNAVAILABLE":null,Map.of("source","TEST_FIXTURE"),List.of(new Contribution("person1","Anonymous fixture",60,40,frequency)));
    }
    void coupling(long repo) { alerts.create(repo,new AlertService.RuleRequest("STRONG_COUPLING",new BigDecimal("0.5"),5,true,null)); }
    void delta(long repo) { alerts.create(repo,new AlertService.RuleRequest("HOTSPOT_INCREASE",new BigDecimal("0.1"),null,true,null)); }
    int count(String table) { return db.queryForObject("SELECT count(*) FROM "+table,Integer.class); }

    @Test void manualRequestsRaceToOneActiveJob() throws Exception {
        long r=repo();var start=new CountDownLatch(1);
        try(var pool=Executors.newFixedThreadPool(2)) {
            Callable<Integer> enqueue=()->{start.await();try {store.enqueue(r,180,CONFIG);return 202;}catch(ResponseStatusException e){return e.getStatusCode().value();}};
            var a=pool.submit(enqueue);var b=pool.submit(enqueue);start.countDown();
            assertThat(List.of(a.get(10,TimeUnit.SECONDS),b.get(10,TimeUnit.SECONDS))).containsExactlyInAnyOrder(202,409);
        }
        assertThat(count("analysis_job")).isEqualTo(1);
    }
    @Test void durableBacklogBoundAndPaginationInput() {
        for(int i=0;i<20;i++) store.enqueue(repo(),180,CONFIG);
        long next=repo();assertThatThrownBy(()->store.enqueue(next,180,CONFIG)).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(429));
        assertThat(count("analysis_job")).isEqualTo(20);
        assertThatThrownBy(()->MonitoringController.offset(Integer.MAX_VALUE,100)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void concurrentClaimsHaveDifferentOwners() throws Exception {
        store.enqueue(repo(),180,CONFIG);store.enqueue(repo(),180,CONFIG);
        try(var pool=Executors.newFixedThreadPool(2)) {
            var a=pool.submit(()->store.claim().orElseThrow());var b=pool.submit(()->store.claim().orElseThrow());
            var ca=a.get(10,TimeUnit.SECONDS);var cb=b.get(10,TimeUnit.SECONDS);
            assertThat(ca.id()).isNotEqualTo(cb.id());assertThat(ca.token()).isNotEqualTo(cb.token());
        }
    }
    @Test void publicationStoresAllDetailsAndNoChangeReusesIdentity() {
        long r=repo();coupling(r);var c=fixed(r,END,CONFIG);long s=publisher.publish(c,data("0.5",true,false));
        assertThat(store.job(c.id())).containsEntry("status","SUCCEEDED").containsEntry("outcome","CREATED");
        assertThat(count("file_metric")).isEqualTo(2);assertThat(count("file_contribution")).isEqualTo(2);
        assertThat(count("contributor")).isEqualTo(1);assertThat(count("temporal_coupling")).isEqualTo(1);
        assertThat(db.queryForObject("SELECT coupling_degree FROM temporal_coupling",BigDecimal.class)).isEqualByComparingTo("0.5");
        var retry=fixed(r,END,CONFIG);assertThat(publisher.publish(retry,data("0.5",true,false))).isEqualTo(s);
        assertThat(store.job(retry.id())).containsEntry("outcome","NO_CHANGE");assertThat(count("analysis_snapshot")).isEqualTo(1);
        publisher.publish(fixed(r,END.plusSeconds(86400),CONFIG),data("0.5",true,false));assertThat(count("analysis_snapshot")).isEqualTo(2);
    }
    @Test void invalidDetailRollsBackWholePublication() {
        long r=repo();var c=fixed(r,END,CONFIG);var valid=data("0.5",true,false);
        var bad=new SnapshotData(valid.headSha(),valid.metricVersion(),20,false,valid.summary(),List.of(file("A","A.java","0.5",10),file("B","B.java","0.4",-1)),List.of());
        assertThatThrownBy(()->publisher.publish(c,bad)).isInstanceOf(RuntimeException.class);
        assertThat(count("analysis_snapshot")).isZero();assertThat(count("file_metric")).isZero();assertThat(count("logical_file")).isZero();
        assertThat(store.job(c.id())).containsEntry("status","RUNNING");
    }
    @Test void expiredOrWrongClaimCannotHeartbeatOrPublish() {
        var c=fixed(repo(),END,CONFIG);assertThat(store.heartbeat(c)).isTrue();
        var wrong=new Claim(c.id(),c.repositoryId(),c.url(),UUID.randomUUID(),c.windowStart(),c.windowEnd(),c.configHash(),c.config());
        assertThat(store.heartbeat(wrong)).isFalse();assertThatThrownBy(()->publisher.publish(wrong,data("0.5",true,false))).isInstanceOf(ResponseStatusException.class);
        db.update("UPDATE analysis_job SET lease_until=clock_timestamp()-interval '1 second' WHERE id=?",c.id());
        assertThat(store.heartbeat(c)).isFalse();assertThatThrownBy(()->publisher.publish(c,data("0.5",true,false))).isInstanceOf(ResponseStatusException.class);
        assertThat(store.recoverExpired()).isEqualTo(1);assertThat(store.job(c.id())).containsEntry("error_code","LEASE_EXPIRED");assertThat(count("analysis_snapshot")).isZero();
    }
    @Test void dueScheduleCoalescesMissedTicksAndDefersActiveJob() {
        long r=repo();store.settings(r,true,60,1);db.update("UPDATE tracked_repository SET next_scan_at=clock_timestamp()-interval '20 days' WHERE id=?",r);
        assertThat(store.scheduleDue(CONFIG)).isEqualTo(1);assertThat(count("analysis_job")).isEqualTo(1);
        assertThat(db.queryForObject("SELECT next_scan_at>clock_timestamp() AND next_scan_at<clock_timestamp()+interval '61 minutes' FROM tracked_repository WHERE id=?",Boolean.class,r)).isTrue();
        db.update("UPDATE tracked_repository SET next_scan_at=clock_timestamp()-interval '1 second' WHERE id=?",r);
        assertThat(store.scheduleDue(CONFIG)).isZero();assertThat(count("analysis_job")).isEqualTo(1);
        assertThatThrownBy(()->store.settings(r,false,60,1)).isInstanceOf(ResponseStatusException.class);
        store.settings(r,false,60,2);assertThat(store.settings(r)).containsEntry("monitoring_enabled",false).containsEntry("next_scan_at",null);
    }
    @Test void rulePinnedAtPublishAndRetryCannotReopenClosedAlert() {
        long r=repo();var rule=alerts.create(r,new AlertService.RuleRequest("STRONG_COUPLING",new BigDecimal("0.5"),5,true,null));
        long s=publisher.publish(fixed(r,END,CONFIG),data("0.5",true,false));
        assertThat(store.snapshot(s)).containsEntry("evaluation_status","PENDING");
        alerts.update(((Number)rule.get("id")).longValue(),new AlertService.RuleRequest("STRONG_COUPLING",new BigDecimal("0.9"),5,true,1));
        assertThat(alerts.evaluateNext()).isTrue();assertThat(count("alert")).isEqualTo(1);
        long id=db.queryForObject("SELECT id FROM alert",Long.class);
        assertThat(db.queryForObject("SELECT rule_version FROM alert",Integer.class)).isEqualTo(1);
        alerts.transition(id,"ACKNOWLEDGED");alerts.transition(id,"CLOSED");alerts.transition(id,"CLOSED");
        assertThatThrownBy(()->alerts.transition(id,"ACKNOWLEDGED")).isInstanceOf(ResponseStatusException.class);
        db.update("UPDATE analysis_snapshot SET evaluation_status='PENDING' WHERE id=?",s);alerts.evaluateNext();
        assertThat(count("alert")).isEqualTo(1);assertThat(db.queryForObject("SELECT status FROM alert",String.class)).isEqualTo("CLOSED");
    }
    @Test void deltaUsesStableLineageAcrossRenameAndStrictThreshold() {
        long r=repo();delta(r);long first=publisher.publish(fixed(r,END,CONFIG),data("0.5",true,false));alerts.evaluateNext();
        var v=data("0.6",true,false);var renamed=new SnapshotData(v.headSha(),v.metricVersion(),20,false,v.summary(),List.of(file("A","Renamed.java","0.6",10),file("B","B.java","0.2",20)),v.pairs());
        publisher.publish(fixed(r,END.plusSeconds(86400),CONFIG),renamed);alerts.evaluateNext();assertThat(count("alert")).isZero();
        long third=publisher.publish(fixed(r,END.plusSeconds(2*86400),CONFIG),data("0.8",true,false));alerts.evaluateNext();
        assertThat(count("logical_file")).isEqualTo(2);assertThat(count("alert")).isEqualTo(1);
        assertThat(db.queryForObject("SELECT observed_value FROM alert",BigDecimal.class)).isEqualByComparingTo("0.2");
        assertThat(store.snapshot(first).get("evaluation_notes").toString()).contains("NO_COMPATIBLE_BASELINE");assertThat(store.snapshot(third)).containsEntry("evaluation_status","COMPLETE");
    }
    @Test void coverageOrTruncationSuppressesUnreliableAlerts() {
        long r=repo();delta(r);coupling(r);publisher.publish(fixed(r,END,CONFIG),data("0.1",true,false));alerts.evaluateNext();
        long later=publisher.publish(fixed(r,END.plusSeconds(86400),CONFIG),data("0.9",true,true));alerts.evaluateNext();
        assertThat(count("alert")).isEqualTo(1);assertThat(store.snapshot(later).get("evaluation_notes").toString()).contains("INCOMPLETE_COVERAGE");
    }
    @Test void equalWindowEndUsesPublicationOrderForDifferentHeadSha() {
        long r=repo();delta(r);publisher.publish(fixed(r,END,CONFIG),data("0.1",true,false));alerts.evaluateNext();
        var d=data("0.5",true,false);
        var changedHead=new SnapshotData("c".repeat(40),d.metricVersion(),d.commitCount(),false,d.summary(),d.files(),d.pairs());
        publisher.publish(fixed(r,END,CONFIG),changedHead);alerts.evaluateNext();
        assertThat(count("alert")).isEqualTo(1);
        assertThat(db.queryForObject("SELECT observed_value FROM alert",BigDecimal.class)).isEqualByComparingTo("0.4");
    }
    @Test void changedConfigurationIsNotComparableAndNullIsNotZero() {
        long r=repo();delta(r);publisher.publish(fixed(r,END,CONFIG),data("0.1",true,false));alerts.evaluateNext();
        long next=publisher.publish(fixed(r,END.plusSeconds(86400),"{\"metric\":\"v2\"}"),data("0.9",true,false));alerts.evaluateNext();
        assertThat(count("alert")).isZero();assertThat(store.snapshot(next).get("evaluation_notes").toString()).contains("NO_COMPATIBLE_BASELINE");
        long missing=publisher.publish(fixed(r,END.plusSeconds(2*86400),CONFIG),data(null,true,false));alerts.evaluateNext();
        assertThat(count("alert")).isZero();assertThat(store.snapshot(missing).get("evaluation_notes").toString()).contains("MISSING_BASELINE_OR_VALUE");
    }
    @Test void failedEvaluationDoesNotUndoPublishedSnapshotAndCanRetry() {
        long r=repo();coupling(r);long s=publisher.publish(fixed(r,END,CONFIG),data("0.5",true,false));
        String rules=db.queryForObject("SELECT pinned_rules::text FROM analysis_snapshot WHERE id=?",String.class,s);
        db.update("UPDATE analysis_snapshot SET pinned_rules='[{\"id\":1,\"type\":\"BAD\",\"parameters\":{\"threshold\":\"bad\"}}]'::jsonb WHERE id=?",s);
        assertThatThrownBy(alerts::evaluateNext).isInstanceOf(AlertService.EvaluationFailure.class);alerts.recordFailure(s);
        assertThat(store.snapshot(s)).containsEntry("evaluation_status","FAILED");assertThat(count("file_metric")).isEqualTo(2);
        db.update("UPDATE analysis_snapshot SET pinned_rules=?::jsonb,evaluation_next_at=clock_timestamp() WHERE id=?",rules,s);
        assertThat(alerts.evaluateNext()).isTrue();assertThat(count("alert")).isEqualTo(1);assertThat(store.snapshot(s)).containsEntry("evaluation_status","COMPLETE");
    }
    @Test void boundedExecutorKeepsWaitingJobsDurableAndProcessesAfterRelease() throws Exception {
        for(int i=0;i<6;i++) store.enqueue(repo(),180,CONFIG);
        var started=new CountDownLatch(2);var release=new CountDownLatch(1);
        var beans=new DefaultListableBeanFactory();
        beans.registerSingleton("pipeline",new AnalysisPipeline(){
            public String effectiveConfig(){return CONFIG;}
            public SnapshotData analyze(Claim c,Lease lease) throws Exception {started.countDown();if(!release.await(10,TimeUnit.SECONDS))throw new IllegalStateException("test timeout");lease.checkpoint();return data("0.5",true,false);}
        });
        var worker=new AnalysisWorker(store,publisher,alerts,beans.getBeanProvider(AnalysisPipeline.class));
        try {
            for(int i=0;i<30;i++) worker.dispatch();
            assertThat(started.await(5,TimeUnit.SECONDS)).isTrue();
            assertThat(db.queryForObject("SELECT count(*) FROM analysis_job WHERE status='RUNNING'",Integer.class)).isEqualTo(2);
            assertThat(db.queryForObject("SELECT count(*) FROM analysis_job WHERE status='QUEUED'",Integer.class)).isEqualTo(4);
            release.countDown();long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(10);
            while(count("analysis_snapshot")<6 && System.nanoTime()<until) Thread.sleep(25);
            assertThat(count("analysis_snapshot")).isEqualTo(6);
        } finally {release.countDown();worker.stop();}
    }
    @Test void queuedJobSurvivesNewWorkerButChangedConfigFailsExplicitly() {
        var c=fixed(repo(),END,CONFIG);store.fail(c,"TEST_INTERRUPTED");
        long id=store.enqueue(c.repositoryId(),180,CONFIG);
        var beans=new DefaultListableBeanFactory();beans.registerSingleton("pipeline",new AnalysisPipeline(){
            public String effectiveConfig(){return "{}";}
            public SnapshotData analyze(Claim c,Lease lease){throw new AssertionError("Must not analyze changed config");}
        });
        var worker=new AnalysisWorker(store,publisher,alerts,beans.getBeanProvider(AnalysisPipeline.class));
        try {worker.runOne();assertThat(store.job(id)).containsEntry("error_code","CONFIG_CHANGED_REQUEUE_REQUIRED");}
        finally {worker.stop();}
    }
}
