package com.example.demo.aireview;

import static com.example.demo.aireview.LlmReviewService.*;
import static com.example.demo.aireview.ReviewContextBuilder.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.json.JsonMapper;

@RestController
@RequestMapping("/api/snapshots/{snapshot}/files/{file}")
public class AiReviewController {
    private final ReviewContextBuilder builder;
    private final ObjectProvider<LLMProviderAdapter> providers;
    private final JdbcTemplate db;
    private final boolean enabled;
    private final long timeoutMs;
    private volatile LlmReviewService engine;
    // No queue: a provider ignoring interruption keeps its slot occupied until it actually exits.
    private final ThreadPoolExecutor executor = new ThreadPoolExecutor(2,2,0,TimeUnit.SECONDS,new SynchronousQueue<>());
    public AiReviewController(ReviewContextBuilder builder,ObjectProvider<LLMProviderAdapter> providers,JdbcTemplate db,
        @Value("${app.ai-review.enabled:false}") boolean enabled,@Value("${app.ai-review.timeout-ms:35000}") long timeoutMs) {
        this.builder=builder;this.providers=providers;this.db=db;this.enabled=enabled;this.timeoutMs=Math.min(35000,Math.max(1,timeoutMs));
    }
    public record Selection(Integer startLine,Integer endLine,String contextHash,Boolean consent) {}
    private void access() {if(!enabled) throw fail(HttpStatus.FORBIDDEN,"AI_REVIEW_DISABLED");}
    private void selection(Selection s) {if(s.startLine()==null || s.endLine()==null) throw fail(HttpStatus.BAD_REQUEST,"INVALID_SELECTION");}
    @PostMapping("/ai-review-preview")
    public Object preview(@PathVariable long snapshot,@PathVariable long file,@RequestBody Selection s) {
        access();selection(s);
        var c=builder.build(snapshot,file,s.startLine(),s.endLine());
        // Exactly the canonical payload hashed by the engine, no secrets/identity metadata added afterward.
        return Map.of("contextHash",c.contextHash(),"context",JsonMapper.builder().build().readTree(contextJson(c)),"providerAvailable",providers.getIfAvailable()!=null);
    }
    @PostMapping("/ai-reviews")
    public Object review(@PathVariable long snapshot,@PathVariable long file,@RequestBody Selection s) {
        access();selection(s);
        if(!Boolean.TRUE.equals(s.consent()) || s.contextHash()==null) throw fail(HttpStatus.FORBIDDEN,"CONSENT_REQUIRED");
        if(providers.getIfAvailable()==null) throw fail(HttpStatus.SERVICE_UNAVAILABLE,"AI_PROVIDER_UNAVAILABLE");
        Instant deadline=Instant.now().plusMillis(timeoutMs);
        Future<Object> task;
        try { task=executor.submit(()->{
            var c=builder.build(snapshot,file,s.startLine(),s.endLine());
            if(!c.contextHash().equals(s.contextHash())) throw fail(HttpStatus.CONFLICT,"CONTEXT_CHANGED_PREVIEW_AGAIN");
            c=withConsent(c,new ConsentReceipt(true,c.contextHash(),Instant.now()),c.contextHash());
            var outcome=engine().review(c,new ReviewBudget(deadline,16384,2048,16384,()->Thread.currentThread().isInterrupted()));
            if(outcome instanceof Failure f) throw fail(status(f.code()),f.code().name());
            if(Thread.currentThread().isInterrupted() || !Instant.now().isBefore(deadline)) throw fail(HttpStatus.GATEWAY_TIMEOUT,"DEADLINE_EXCEEDED");
            var success=(Success)outcome;
            // Only audit metadata: never persist source, prompt body or model output.
            db.update("INSERT INTO ai_review_audit(review_id,snapshot_id,file_id,created_at,metadata) VALUES (?,?,?,clock_timestamp(),?::jsonb)",success.metadata().reviewId(),snapshot,file,JsonMapper.builder().build().writeValueAsString(success.metadata()));
            return Map.of("reviewId",success.metadata().reviewId(),"snapshotId",snapshot,"fileId",file,"generatedAt",Instant.now(),"aiGenerated",true,"result",success.output(),"metadata",success.metadata());
        }); } catch(RejectedExecutionException e) {throw fail(HttpStatus.TOO_MANY_REQUESTS,"RATE_LIMITED");}
        try {return task.get(timeoutMs,TimeUnit.MILLISECONDS);}
        catch(TimeoutException e) {task.cancel(true);throw fail(HttpStatus.GATEWAY_TIMEOUT,"DEADLINE_EXCEEDED");}
        catch(InterruptedException e) {task.cancel(true);Thread.currentThread().interrupt();throw fail(HttpStatus.SERVICE_UNAVAILABLE,"CANCELLED");}
        catch(ExecutionException e) {if(e.getCause() instanceof RuntimeException r) throw r;throw fail(HttpStatus.BAD_GATEWAY,"PROVIDER_FAILED");}
    }
    private synchronized LlmReviewService engine() {if(engine==null)engine=new LlmReviewService(providers.getObject());return engine;}
    private static HttpStatus status(ReviewError code) {
        return switch(code) {
            case INVALID_SELECTION,INSUFFICIENT_CONTEXT -> HttpStatus.BAD_REQUEST;
            case CONSENT_REQUIRED,SECRET_DETECTED -> HttpStatus.FORBIDDEN;
            case CONTEXT_TOO_LARGE -> HttpStatus.PAYLOAD_TOO_LARGE;
            case RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case DEADLINE_EXCEEDED -> HttpStatus.GATEWAY_TIMEOUT;
            case CANCELLED -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_GATEWAY;
        };
    }
    @PreDestroy public void stop() {executor.shutdownNow();}
}
