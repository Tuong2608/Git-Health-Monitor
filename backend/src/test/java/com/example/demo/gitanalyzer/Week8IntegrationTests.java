package com.example.demo.gitanalyzer;

import com.example.demo.aireview.*;
import com.example.demo.monitoring.AnalysisPipeline;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.*;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.*;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

/** HTTP -> durable job -> real JGit/Lizard -> PostgreSQL; only network source and LLM are test adapters. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"app.analysis.enabled=true","app.analysis.writes-enabled=true","app.ai-review.enabled=true","app.ai-review.timeout-ms=1000"})
@Import(Week8IntegrationTests.Configuration.class)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class Week8IntegrationTests {
    static final Path ROOT;
    static {try {ROOT=Files.createTempDirectory("ghm-week8-test-");}catch(Exception e){throw new ExceptionInInitializerError(e);}}
    static final AtomicInteger CALLS=new AtomicInteger();
    static volatile String mode="success";
    static volatile boolean publicAllowed=true;
    static final JsonMapper JSON=JsonMapper.builder().build();
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {r.add("app.analysis.cache-dir",()->ROOT.resolve("cache").toString());}
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate db;
    long repo,snapshot,file,secretFile;
    String head;
    @TestConfiguration static class Configuration {
        @Bean AnalysisPipeline localGitPipeline() {
            return new GitAnalysisPipeline(ROOT.resolve("cache"),Duration.ofSeconds(30),List.of(),Map.of(),1000,10000,2*1024*1024,10000,
                RepositoryMetricCalculator.Policy.defaults(),(url,cache,deadline)->GitAnalysisPipeline.trustedLocalPinStrategy(new GitRepositoryClient()).pin(ROOT.resolve("source").toUri().toString(),cache,deadline),new GitIngestion(),new LizardComplexityAnalyzer(),new RepositoryMetricCalculator());
        }
        @Bean @Primary ReviewContextBuilder.PublicRepositoryCheck publicCheck() {return url->publicAllowed;}
        @Bean LlmReviewService.LLMProviderAdapter provider() {return (prompt,budget)->{
            CALLS.incrementAndGet();
            if(mode.equals("timeout")) Thread.sleep(3000);
            String json="{\"riskSummary\":\"Test advisory\",\"evidence\":[{\"evidenceId\":\"E1\",\"explanation\":\"Snapshot metric\"}],\"reviewChecklist\":[{\"text\":\"Check branches\",\"evidenceIds\":[\"E1\"]}],\"refactorSuggestions\":[],\"limitations\":[\"Test provider, not real AI\"]}";
            if(mode.equals("invalid")) json="{\"wrong\":true}";
            if(mode.equals("trailing")) json+=" {}";
            return new LlmReviewService.ProviderResponse(json,"test-only","fixture",100,50);
        };}
    }
    HttpResponse<String> call(String method,String path,Object body) throws Exception {
        String payload=body==null?"":JSON.writeValueAsString(body);
        var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+"/api"+path)).timeout(Duration.ofSeconds(40)).header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(payload)).build(),HttpResponse.BodyHandlers.ofString());
        String dir=System.getProperty("week8.evidenceDir");
        if(dir!=null) {Path out=Path.of(dir);Files.createDirectories(out);Files.writeString(out.resolve("integration-http.jsonl"),JSON.writeValueAsString(Map.of("source","CONTROLLED_GIT_FIXTURE_REAL_JGIT_LIZARD_POSTGRES_TEST_LLM","method",method,"path",path,"status",response.statusCode(),"response",path.endsWith("/ai-review-preview") ? Map.of("previewBodyOmitted",true) : JSON.readTree(response.body())))+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}
        return response;
    }
    JsonNode get(String path) throws Exception {var r=call("GET",path,null);assertThat(r.statusCode()).isEqualTo(200);return JSON.readTree(r.body());}
    @BeforeAll void pipeline() throws Exception {
        try(var git=Git.init().setDirectory(ROOT.resolve("source").toFile()).setInitialBranch("main").call()) {
            for(int i=0;i<6;i++) {
                Files.writeString(ROOT.resolve("source/A.java"),"class A {\n int calculate(int x) {\n  if(x>"+i+") return x;\n  return 0;\n }\n}\n");
                Files.writeString(ROOT.resolve("source/B.java"),"class B { int value() { return "+i+"; } }\n");
                Files.writeString(ROOT.resolve("source/Secret.java"),"class Secret { String "+"pass"+"word = \""+"x".repeat(16)+"\"; }\n");
                git.add().addFilepattern(".").call();
                head=git.commit().setMessage("Controlled fixture "+i).setAuthor("Fixture","fixture@example.invalid").setCommitter("Fixture","fixture@example.invalid").call().name();
            }
        }
        repo=db.queryForObject("INSERT INTO tracked_repository(url,owner,name,created_at) VALUES ('https://github.com/fixture/week8','fixture','week8',now()) RETURNING id",Long.class);
        assertThat(call("POST","/repositories/"+repo+"/alert-rules",Map.of("type","STRONG_COUPLING","threshold",.5,"minShared",5,"enabled",true)).statusCode()).isEqualTo(201);
        var accepted=call("POST","/repositories/"+repo+"/analyses",Map.of("observationDays",180));assertThat(accepted.statusCode()).isEqualTo(202);
        long job=JSON.readTree(accepted.body()).path("jobId").asLong();
        long until=System.nanoTime()+Duration.ofSeconds(40).toNanos();JsonNode state;
        do {state=get("/jobs/"+job);if(Set.of("SUCCEEDED","FAILED").contains(state.path("status").asText()))break;Thread.sleep(200);}while(System.nanoTime()<until);
        assertThat(state.path("status").asText()).isEqualTo("SUCCEEDED");snapshot=state.path("snapshotId").asLong();
        file=db.queryForObject("SELECT file_id FROM file_metric WHERE snapshot_id=? AND path_at_snapshot='A.java'",Long.class,snapshot);
        secretFile=db.queryForObject("SELECT file_id FROM file_metric WHERE snapshot_id=? AND path_at_snapshot='Secret.java'",Long.class,snapshot);
    }
    @BeforeEach void reset() {mode="success";CALLS.set(0);publicAllowed=true;}
    String path(long f) {return "/snapshots/"+snapshot+"/files/"+f;}
    JsonNode preview(long f,int end) throws Exception {var r=call("POST",path(f)+"/ai-review-preview",Map.of("startLine",1,"endLine",end));assertThat(r.statusCode()).isEqualTo(200);return JSON.readTree(r.body());}
    Map<String,Object> selection(String hash) {return Map.of("startLine",1,"endLine",5,"consent",true,"contextHash",hash);}
    @Test void realPipelinePersistsMeasuredMetricsEvidenceAndAlerts() throws Exception {
        assertThat(get("/snapshots/"+snapshot).path("headSha").asText()).isEqualTo(head);
        var f=get(path(file));assertThat(f.path("changeFrequency").asInt()).isEqualTo(6);assertThat(f.path("maxCcn").asInt()).isEqualTo(2);assertThat(f.path("evidenceAvailable").asBoolean()).isTrue();
        assertThat(get(path(file)+"/couplings").path("items").size()).isGreaterThan(0);
        assertThat(get(path(file)+"/evidence").path("items").size()).isEqualTo(6);
        long until=System.nanoTime()+Duration.ofSeconds(10).toNanos();JsonNode alerts;
        do {alerts=get("/repositories/"+repo+"/alerts");if(!alerts.path("items").isEmpty())break;Thread.sleep(200);}while(System.nanoTime()<until);
        assertThat(alerts.path("items").size()).isGreaterThan(0);
    }
    @Test void previewUsesSnapshotBlobEvenWhenCacheHeadMovesAndConsentBindsSelection() throws Exception {
        var p=preview(file,5);assertThat(p.path("context").path("headSha").asText()).isEqualTo(head);
        try(var git=Git.open(ROOT.resolve("source").toFile())) {Files.writeString(ROOT.resolve("source/A.java"),"class A { int newHeadOnly = 99; }\n");git.add().addFilepattern(".").call();git.commit().setMessage("Move HEAD after snapshot").setAuthor("Fixture","fixture@example.invalid").call();}
        new GitRepositoryClient().fetchTrusted(ROOT.resolve("source").toUri().toString(),ROOT.resolve("cache/repo-"+repo+".git"),Instant.now().plusSeconds(10));
        assertThat(preview(file,5).path("contextHash").asText()).isEqualTo(p.path("contextHash").asText());
        var r=call("POST",path(file)+"/ai-reviews",selection(p.path("contextHash").asText()));assertThat(r.statusCode()).isEqualTo(200);assertThat(CALLS.get()).isEqualTo(1);
        assertThat(JSON.readTree(r.body()).path("aiGenerated").asBoolean()).isTrue();
        String audit=db.queryForObject("SELECT metadata::text FROM ai_review_audit WHERE review_id=?",String.class,JSON.readTree(r.body()).path("reviewId").asText());assertThat(audit).contains("contextHash").doesNotContain("calculate", "riskSummary");
    }
    @Test void noConsentAndChangedPreviewNeverCallProvider() throws Exception {
        assertThat(call("POST",path(file)+"/ai-reviews",Map.of("startLine",1,"endLine",5,"consent",false)).statusCode()).isEqualTo(403);
        assertThat(call("POST",path(file)+"/ai-reviews",selection("incorrect")).statusCode()).isEqualTo(409);assertThat(CALLS.get()).isZero();
    }
    @Test void foreignFileInvalidRangeAndPublicCheckFailClosed() throws Exception {
        assertThat(call("POST",path(Long.MAX_VALUE)+"/ai-review-preview",Map.of("startLine",1,"endLine",2)).statusCode()).isEqualTo(404);
        assertThat(call("POST",path(file)+"/ai-review-preview",Map.of("startLine",1,"endLine",300)).statusCode()).isEqualTo(400);
        publicAllowed=false;assertThat(call("POST",path(file)+"/ai-review-preview",Map.of("startLine",1,"endLine",2)).statusCode()).isEqualTo(403);assertThat(CALLS.get()).isZero();
    }
    @Test void secretIsBlockedBeforeProvider() throws Exception {
        var p=preview(secretFile,1);
        var r=call("POST",path(secretFile)+"/ai-reviews",Map.of("startLine",1,"endLine",1,"contextHash",p.path("contextHash").asText(),"consent",true));
        assertThat(r.statusCode()).isEqualTo(403);assertThat(r.body()).contains("SECRET_DETECTED");assertThat(CALLS.get()).isZero();
    }
    @Test void invalidProviderOutputDoesNotChangeSuccessfulSnapshot() throws Exception {
        var p=preview(file,5);mode="invalid";var r=call("POST",path(file)+"/ai-reviews",selection(p.path("contextHash").asText()));assertThat(r.statusCode()).isEqualTo(502);assertThat(r.body()).doesNotContain("wrong");assertThat(get("/snapshots/"+snapshot).path("headSha").asText()).isEqualTo(head);
    }
    @Test void trailingJsonObjectIsRejected() throws Exception {
        var p=preview(file,5);mode="trailing";assertThat(call("POST",path(file)+"/ai-reviews",selection(p.path("contextHash").asText())).statusCode()).isEqualTo(502);
    }
    @Test void deadlineReturns504WithoutLateAudit() throws Exception {
        var p=preview(file,5);mode="timeout";int before=db.queryForObject("SELECT count(*) FROM ai_review_audit",Integer.class);
        assertThat(call("POST",path(file)+"/ai-reviews",selection(p.path("contextHash").asText())).statusCode()).isEqualTo(504);
        Thread.sleep(100);assertThat(db.queryForObject("SELECT count(*) FROM ai_review_audit",Integer.class)).isEqualTo(before);
    }
}
