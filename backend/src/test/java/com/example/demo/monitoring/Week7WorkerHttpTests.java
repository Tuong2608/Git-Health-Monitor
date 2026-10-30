package com.example.demo.monitoring;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import tools.jackson.databind.JsonNode;
import static org.assertj.core.api.Assertions.*;
import static com.example.demo.monitoring.AnalysisPipeline.*;

/** Actual HTTP -> scheduler/worker -> PostgreSQL. Only ingestion/metrics is a TEST-ONLY fixture. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties={"app.analysis.enabled=true","app.analysis.writes-enabled=true"})
@Import(Week7WorkerHttpTests.Fixture.class)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
class Week7WorkerHttpTests {
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate db;
    @TestConfiguration static class Fixture {
        @Bean AnalysisPipeline pipeline() {
            return new AnalysisPipeline() {
                public String effectiveConfig() {return "{\"source\":\"TEST_ONLY_WEEK7\"}";}
                public SnapshotData analyze(Claim c,Lease lease) {
                    lease.checkpoint();
                    return new SnapshotData("f".repeat(40),"fixture-v1",20,false,Map.of("coverageComplete",true,"source","TEST_ONLY_NOT_REAL_GIT"),
                        List.of(Week7ApiTests.known("A","A.java","0.5",10,100),Week7ApiTests.known("B","B.java","0.3",20,100)),
                        List.of(new PairData("A","B",5,10,20,Map.of("commits",List.of(Map.of("commitSha","f".repeat(40)))))));
                }
            };
        }
    }
    HttpResponse<String> request(String method,String path,String body) throws Exception {
        var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api"+path)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
        Week7HttpEvidence.record("worker-http",method,path,body,response);return response;
    }
    JsonNode get(String path) throws Exception {var response=request("GET",path,"");assertThat(response.statusCode()).isEqualTo(200);return MonitoringStore.JSON.readTree(response.body());}
    long repo() {String n=UUID.randomUUID().toString();return db.queryForObject("INSERT INTO tracked_repository(url,owner,name,created_at) VALUES (?,'fixture',?,now()) RETURNING id",Long.class,"https://github.com/fixture/"+n,n);}
    JsonNode awaitJob(long id) throws Exception {
        long deadline=System.nanoTime()+Duration.ofSeconds(15).toNanos();JsonNode j;
        do {j=get("/jobs/"+id);if(Set.of("SUCCEEDED","FAILED").contains(j.path("status").asText()))return j;Thread.sleep(100);}while(System.nanoTime()<deadline);
        throw new AssertionError("Job did not finish: "+j);
    }
    @Test void httpManualJobPublishesSnapshotThenEvaluatesAndClosesAlert() throws Exception {
        long repo=repo();
        assertThat(request("POST","/repositories/"+repo+"/alert-rules","{\"type\":\"STRONG_COUPLING\",\"threshold\":0.5,\"minShared\":5,\"enabled\":true}").statusCode()).isEqualTo(201);
        var accepted=request("POST","/repositories/"+repo+"/analyses","{\"observationDays\":180}");assertThat(accepted.statusCode()).isEqualTo(202);
        long job=MonitoringStore.JSON.readTree(accepted.body()).path("jobId").asLong();
        var completed=awaitJob(job);assertThat(completed.path("status").asText()).isEqualTo("SUCCEEDED");long snapshot=completed.path("snapshotId").asLong();
        long deadline=System.nanoTime()+Duration.ofSeconds(10).toNanos();JsonNode details;
        do {details=get("/snapshots/"+snapshot);if(details.path("evaluationStatus").asText().equals("COMPLETE"))break;Thread.sleep(100);}while(System.nanoTime()<deadline);
        assertThat(details.path("evaluationStatus").asText()).isEqualTo("COMPLETE");
        var alert=get("/repositories/"+repo+"/alerts").path("items").get(0);assertThat(alert.path("observedValue").decimalValue()).isEqualByComparingTo("0.5");
        long id=alert.path("id").asLong();assertThat(request("PATCH","/alerts/"+id,"{\"status\":\"CLOSED\"}").statusCode()).isEqualTo(200);
        assertThat(get("/snapshots/"+snapshot+"/hotspots").path("items").size()).isEqualTo(2);
    }
    @Test void actualSchedulerEnqueuesDueRepositoryOnceAndDisableStopsFutureScans() throws Exception {
        long repo=repo();String path="/repositories/"+repo+"/monitoring";
        assertThat(request("PUT",path,"{\"enabled\":true,\"intervalMinutes\":60,\"version\":1}").statusCode()).isEqualTo(200);
        long deadline=System.nanoTime()+Duration.ofSeconds(20).toNanos();JsonNode jobs;
        do {jobs=get("/repositories/"+repo+"/jobs").path("items");if(jobs.size()>0)break;Thread.sleep(100);}while(System.nanoTime()<deadline);
        assertThat(jobs.size()).isEqualTo(1);assertThat(awaitJob(jobs.get(0).path("id").asLong()).path("status").asText()).isEqualTo("SUCCEEDED");
        assertThat(request("PUT",path,"{\"enabled\":false,\"intervalMinutes\":60,\"version\":2}").statusCode()).isEqualTo(200);
        assertThat(get(path).path("nextScanAt").isNull()).isTrue();
    }
    @Test void httpRetryUsesFrozenWindowAndDistinctJobId() throws Exception {
        long repo=repo();String config="{\"source\":\"TEST_ONLY_WEEK7\"}";
        long old=db.queryForObject("INSERT INTO analysis_job(repository_id,status,requested_at,finished_at,window_start,window_end,config_hash,config,error_code) VALUES (?,'FAILED',now(),now(),'2026-01-01T00:00:00Z','2026-07-01T00:00:00Z',?,?::jsonb,'TEST_FAILURE') RETURNING id",Long.class,repo,MonitoringStore.hash(config),config);
        var response=request("POST","/jobs/"+old+"/retry","");assertThat(response.statusCode()).isEqualTo(202);
        long next=MonitoringStore.JSON.readTree(response.body()).path("jobId").asLong();assertThat(next).isNotEqualTo(old);
        var result=awaitJob(next);assertThat(result.path("status").asText()).isEqualTo("SUCCEEDED");assertThat(result.path("retryOf").asLong()).isEqualTo(old);
        assertThat(result.path("windowEnd").asText()).isEqualTo("2026-07-01T00:00:00Z");assertThat(get("/jobs/"+old).path("status").asText()).isEqualTo("FAILED");
    }
}
