package com.example.demo.monitoring;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import static org.assertj.core.api.Assertions.*;
import static com.example.demo.monitoring.AnalysisPipeline.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties={"app.analysis.enabled=false","app.analysis.writes-enabled=true"})
class Week7ApiTests {
    @Value("${local.server.port}") int port;
    @Autowired MonitoringStore store;
    @Autowired SnapshotPublisher publisher;
    @Autowired AlertService alerts;
    @Autowired JdbcTemplate db;
    static final String CONFIG="{\"metric\":\"week7-test-only\"}";
    static final Instant END=Instant.parse("2026-07-01T00:00:00Z");
    long repo;
    @BeforeEach void setup() {
        db.execute("TRUNCATE alert,file_contribution,temporal_coupling,file_metric,analysis_snapshot,analysis_job,logical_file,contributor,alert_rule,tracked_repository RESTART IDENTITY CASCADE");
        repo=createRepo();
    }
    long createRepo() {String n=UUID.randomUUID().toString();return db.queryForObject("INSERT INTO tracked_repository(url,owner,name,created_at) VALUES (?,'fixture',?,now()) RETURNING id",Long.class,"https://github.com/fixture/"+n,n);}
    Claim claim(long repo,Instant end,String config) {
        db.update("INSERT INTO analysis_job(repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (?,'QUEUED',now(),?,?,?,?::jsonb)",repo,Timestamp.from(end.minusSeconds(180L*86400)),Timestamp.from(end),MonitoringStore.hash(config),config);
        return store.claim().orElseThrow();
    }
    static FileData known(String key,String path,String score,int freq,long churn) {
        return new FileData(key,"a".repeat(40),key+".java",path,freq,churn,BigDecimal.TEN,BigDecimal.ONE,2,40L,new BigDecimal(score),churn==0?null:BigDecimal.ONE,null,
            Map.of("commits",List.of(Map.of("commitSha","b".repeat(40),"changeType","MODIFY"),Map.of("commitSha","c".repeat(40),"changeType","RENAME"))),
            churn==0?List.of():List.of(new Contribution("private@example.invalid","Contributor fixture",churn,0,freq)));
    }
    static FileData unknown(String key,String reason) {
        return new FileData(key,"a".repeat(40),key+".java",key+".java",null,null,null,null,null,null,null,null,reason,Map.of("qualityFlags",List.of("LINEAGE_AMBIGUOUS")),List.of());
    }
    long publish(long repo,Instant end,String config,boolean complete,boolean truncated,List<FileData> files,List<PairData> pairs) {
        return publisher.publish(claim(repo,end,config),new SnapshotData("a".repeat(40),"metric-v1",30,truncated,Map.of("coverageComplete",complete,"source","TEST_FIXTURE"),files,pairs));
    }
    long fileId(long snapshot,String key) { return db.queryForObject("SELECT f.file_id FROM file_metric f JOIN logical_file l ON l.id=f.file_id WHERE f.snapshot_id=? AND l.lineage_key=?",Long.class,snapshot,key); }
    HttpResponse<String> request(String method,String path,String body) throws Exception {
        var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api"+path)).header("Content-Type","application/json").timeout(java.time.Duration.ofSeconds(10)).method(method,HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
        Week7HttpEvidence.record("api",method,path,body,response);return response;
    }
    JsonNode get(String path) throws Exception {var r=request("GET",path,"");assertThat(r.statusCode()).as(r.body()).isEqualTo(200);return MonitoringStore.JSON.readTree(r.body());}
    String route(long s,long f) { return "/snapshots/"+s+"/files/"+f; }
    String compare(long a,long b) {return "/repositories/"+repo+"/snapshot-comparison?fromSnapshotId="+a+"&toSnapshotId="+b;}

    @Test void fileEvidenceAndContributionArePagedAndDoNotExposeEmailKey() throws Exception {
        long s=publish(repo,END,CONFIG,true,false,List.of(known("A","A.java","0.5",10,100)),List.of());long f=fileId(s,"A");
        var detail=get(route(s,f));assertThat(detail.path("evidenceCount").asInt()).isEqualTo(2);assertThat(detail.has("evidence")).isFalse();
        var page=get(route(s,f)+"/evidence?size=1");assertThat(page.path("items").size()).isEqualTo(1);assertThat(page.path("hasNext").asBoolean()).isTrue();
        var next=get(route(s,f)+"/evidence?size=1&page=1");assertThat(next.path("items").get(0).path("evidence").path("commitSha").asText()).isEqualTo("c".repeat(40));assertThat(next.path("hasNext").asBoolean()).isFalse();
        var contribution=get(route(s,f)+"/contributions");assertThat(contribution.toString()).doesNotContain("private@example.invalid","identityKey","aliases");assertThat(contribution.path("items").get(0).path("share").decimalValue()).isEqualByComparingTo("1");
    }
    @Test void unknownMeasurementsRemainNullInDatabaseAndHttp() throws Exception {
        long s=publish(repo,END,CONFIG,false,false,List.of(unknown("A","LINEAGE_AMBIGUOUS")),List.of());long f=fileId(s,"A");
        var detail=get(route(s,f));for(String k:List.of("changeFrequency","churn","maxCcn","nloc","functionCount","hotspotScore","hhi")) assertThat(detail.path(k).isNull()).as(k).isTrue();
        assertThat(detail.path("unavailableReason").asText()).isEqualTo("LINEAGE_AMBIGUOUS");assertThat(detail.path("evidenceAvailable").asBoolean()).isFalse();
        assertThat(get(route(s,f)+"/contributions").path("items").size()).isZero();assertThat(get(route(s,f)+"/evidence").path("items").size()).isZero();
    }
    @Test void missingReasonAndUnknownChurnWithContributionRollback() {
        var c=claim(repo,END,CONFIG);var bad=unknown("A",null);
        assertThatThrownBy(()->publisher.publish(c,new SnapshotData("a".repeat(40),"v1",1,false,Map.of(),List.of(bad),List.of()))).isInstanceOf(IllegalArgumentException.class);
        var invalid=new FileData("A","a".repeat(40),"A.java","A.java",1,null,null,null,0,1L,null,null,"BINARY",Map.of(),List.of(new Contribution("p","P",0,0,1)));
        assertThatThrownBy(()->publisher.publish(c,new SnapshotData("a".repeat(40),"v1",1,false,Map.of(),List.of(invalid),List.of()))).isInstanceOf(IllegalArgumentException.class);
        assertThat(db.queryForObject("SELECT count(*) FROM analysis_snapshot",Integer.class)).isZero();
    }
    @Test void nullableChurnDoesNotHideNegativeMeasuredFields() {
        long s=publish(repo,END,CONFIG,false,false,List.of(unknown("A","UNKNOWN")),List.of());
        assertThatThrownBy(()->db.update("UPDATE file_metric SET change_frequency=-1 WHERE snapshot_id=?",s)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        assertThatThrownBy(()->db.update("UPDATE file_metric SET hhi=0.5 WHERE snapshot_id=?",s)).isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
    }
    @Test void couplingDirectionSwapsCountsButKeepsDegreeAndEvidence() throws Exception {
        long s=publish(repo,END,CONFIG,true,false,List.of(known("A","A.java","0.5",10,100),known("B","B.java","0.6",20,100)),List.of(new PairData("B","A",5,20,10,Map.of("commits",List.of(Map.of("commitSha","d".repeat(40)))))));
        long a=fileId(s,"A"),b=fileId(s,"B");
        var pair=get(route(s,b)+"/couplings").path("items").get(0);assertThat(pair.path("fileCommitCount").asInt()).isEqualTo(20);assertThat(pair.path("relatedCommitCount").asInt()).isEqualTo(10);assertThat(pair.path("relatedFileId").asLong()).isEqualTo(a);assertThat(pair.path("couplingDegree").decimalValue()).isEqualByComparingTo("0.5");
        assertThat(get(route(s,b)+"/couplings/"+a+"/evidence").path("items").size()).isEqualTo(1);
    }
    @Test void couplingCannotInventCountsForUnknownFile() {
        var c=claim(repo,END,CONFIG);
        var data=new SnapshotData("a".repeat(40),"v1",30,false,Map.of(),List.of(unknown("A","UNKNOWN"),known("B","B.java","0.4",20,100)),List.of(new PairData("A","B",5,10,20,Map.of())));
        assertThatThrownBy(()->publisher.publish(c,data)).isInstanceOf(IllegalArgumentException.class);
        assertThat(db.queryForObject("SELECT count(*) FROM analysis_snapshot",Integer.class)).isZero();
    }
    @Test void snapshotAndRepositoryScopesRejectForeignFileIds() throws Exception {
        long s=publish(repo,END,CONFIG,true,false,List.of(known("A","A.java","0.2",10,100)),List.of());long a=fileId(s,"A");
        long other=createRepo();long foreign=publish(other,END,CONFIG,true,false,List.of(known("B","B.java","0.3",10,100)),List.of());long b=fileId(foreign,"B");
        for(String suffix:List.of("","/contributions","/evidence","/couplings")) assertThat(request("GET",route(s,b)+suffix,"").statusCode()).isEqualTo(404);
        assertThat(request("GET","/repositories/"+other+"/files/"+a+"/trend","").statusCode()).isEqualTo(404);
        assertThat(request("GET",compare(s,foreign),"").statusCode()).isEqualTo(404);
    }
    @Test void trendPreservesRenameIdentityAndOrdersByWindowNotInsertOrder() throws Exception {
        long late=publish(repo,END.plusSeconds(86400),CONFIG,true,false,List.of(known("A","Renamed.java","0.6",10,100)),List.of());
        long early=publish(repo,END,CONFIG,true,false,List.of(known("A","A.java","0.2",10,100)),List.of());long f=fileId(late,"A");
        var first=get("/repositories/"+repo+"/files/"+f+"/trend?size=1");assertThat(first.path("items").get(0).path("snapshotId").asLong()).isEqualTo(early);assertThat(first.path("hasNext").asBoolean()).isTrue();
        assertThat(get("/repositories/"+repo+"/files/"+f+"/trend?page=1&size=1").path("items").get(0).path("pathAtSnapshot").asText()).isEqualTo("Renamed.java");
    }
    @Test void comparisonPreservesZeroMissingAddedRemovedAndRename() throws Exception {
        long old=publish(repo,END,CONFIG,true,false,List.of(known("A","A.java","0.0",0,0),known("B","B.java","0.3",1,100)),List.of());
        long next=publish(repo,END.plusSeconds(86400),CONFIG,true,false,List.of(known("A","Renamed.java","0.4",10,100),known("C","C.java","0.2",1,100)),List.of());
        var body=get(compare(old,next));var rows=body.path("items");assertThat(rows.size()).isEqualTo(3);
        var change=rows.get(0);assertThat(change.path("beforePath").asText()).isEqualTo("A.java");assertThat(change.path("afterPath").asText()).isEqualTo("Renamed.java");
        var freq=change.path("metrics").path("changeFrequency");assertThat(freq.path("absoluteDelta").asInt()).isEqualTo(10);assertThat(freq.path("relativeDelta").isNull()).isTrue();assertThat(freq.path("reason").asText()).isEqualTo("BASE_ZERO");
        assertThat(change.path("metrics").path("hhi").path("before").isNull()).isTrue();assertThat(rows.get(1).path("state").asText()).isEqualTo("REMOVED");assertThat(rows.get(2).path("state").asText()).isEqualTo("ADDED");
        assertThat(body.toString()).doesNotContain("identityKey","commits");
        assertThat(get(compare(old,next)+"&size=1").path("hasNext").asBoolean()).isTrue();
    }
    @Test void incompatibleConfigCoverageAndReverseComparisonFailExplicitly() throws Exception {
        long a=publish(repo,END,CONFIG,true,false,List.of(),List.of());
        long b=publish(repo,END.plusSeconds(86400),"{}",true,false,List.of(),List.of());
        assertThat(request("GET",compare(a,b),"").body()).contains("SNAPSHOTS_NOT_COMPARABLE_CONFIG");
        long c=publish(repo,END.plusSeconds(2*86400),CONFIG,false,false,List.of(),List.of());
        assertThat(request("GET",compare(a,c),"").statusCode()).isEqualTo(409);
        assertThat(request("GET",compare(c,a),"").statusCode()).isEqualTo(400);
        assertThat(request("GET",compare(a,a),"").statusCode()).isEqualTo(400);
    }
    @Test void retryPreservesWindowAndOriginalFailureAndRejectsChangedConfig() {
        var failed=claim(repo,END,CONFIG);store.fail(failed,"TEST_FAILURE");
        assertThatThrownBy(()->store.retry(failed.id(),"{}" )).isInstanceOfSatisfying(ResponseStatusException.class,e->assertThat(e.getStatusCode().value()).isEqualTo(409));
        long next=store.retry(failed.id(),CONFIG);
        assertThat(store.job(next)).containsEntry("retry_of",failed.id()).containsEntry("window_start",Timestamp.from(failed.windowStart())).containsEntry("window_end",Timestamp.from(failed.windowEnd())).containsEntry("status","QUEUED");
        assertThat(store.job(failed.id())).containsEntry("status","FAILED").containsEntry("error_code","TEST_FAILURE");
        assertThatThrownBy(()->store.retry(failed.id(),CONFIG)).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(()->store.retry(next,CONFIG)).isInstanceOf(ResponseStatusException.class);
    }
    @Test void claimCannotPublishMutatedWindowOrConfig() {
        var c=claim(repo,END,CONFIG);var forged=new Claim(c.id(),c.repositoryId(),c.url(),c.token(),c.windowStart(),c.windowEnd().plusSeconds(1),c.configHash(),c.config());
        assertThatThrownBy(()->publisher.publish(forged,new SnapshotData("a".repeat(40),"v1",0,false,Map.of(),List.of(),List.of()))).isInstanceOf(ResponseStatusException.class);
    }
    @Test void capabilitiesAndJobHistoryExposeReadinessWithoutClaimToken() throws Exception {
        store.enqueue(repo,180,CONFIG);var caps=get("/analysis-capabilities");assertThat(caps.path("canAnalyze").asBoolean()).isFalse();assertThat(caps.path("pipelineAvailable").asBoolean()).isFalse();
        var jobs=get("/repositories/"+repo+"/jobs?size=1");assertThat(jobs.path("items").size()).isEqualTo(1);assertThat(jobs.toString()).doesNotContain("claimToken","configHash");
        assertThat(request("POST","/jobs/1/retry","").statusCode()).isEqualTo(503);
    }
    @Test void newCollectionsValidatePaginationAndMissingResources() throws Exception {
        assertThat(request("GET","/repositories/"+repo+"/jobs?size=0","").statusCode()).isEqualTo(400);
        assertThat(request("GET","/snapshots/999/files/999/evidence?size=101","").statusCode()).isEqualTo(400);
        assertThat(request("GET","/snapshots/999/files/999","").statusCode()).isEqualTo(404);
        assertThat(request("GET","/repositories/"+repo+"/jobs?page=2147483647&size=100","").statusCode()).isEqualTo(400);
    }
}
