package com.example.demo.aireview;

import static com.example.demo.aireview.LlmReviewService.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.*;
import java.util.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.demo.repository.GitHubRepositoryUrl;

/** Rebuild the exact preview from immutable snapshot metadata and a pinned Git blob. */
@Service
public class ReviewContextBuilder {
    public interface PublicRepositoryCheck { boolean isPublic(String canonicalUrl) throws Exception; }
    private final JdbcTemplate db;
    private final Path cache;
    private final PublicRepositoryCheck publicCheck;
    public ReviewContextBuilder(JdbcTemplate db, @Value("${app.analysis.cache-dir:target/git-analysis-cache}") String cache, PublicRepositoryCheck publicCheck) {
        this.db = db; this.cache = Path.of(cache).toAbsolutePath().normalize(); this.publicCheck = publicCheck;
    }
    public VerifiedReviewContext build(long snapshot, long file, int start, int end) {
        if (start < 1 || end < start || (long)end - start >= MAX_LINES_PER_SECTION) throw fail(HttpStatus.BAD_REQUEST, "INVALID_SELECTION");
        var rows = db.queryForList("SELECT s.repository_id,s.head_sha,s.config_hash,s.window_start,s.window_end,r.url,f.* FROM analysis_snapshot s JOIN tracked_repository r ON r.id=s.repository_id JOIN file_metric f ON f.snapshot_id=s.id WHERE s.id=? AND f.file_id=?", snapshot, file);
        if (rows.isEmpty()) throw fail(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        var row = rows.getFirst();
        try {
            if (!publicCheck.isPublic((String)row.get("url"))) throw fail(HttpStatus.FORBIDDEN, "PUBLIC_CHECK_FAILED");
        } catch (ResponseStatusException e) { throw e; }
        catch (Exception e) { throw fail(HttpStatus.SERVICE_UNAVAILABLE, "PUBLIC_CHECK_UNAVAILABLE"); }
        long repo = ((Number)row.get("repository_id")).longValue();
        String sha = (String)row.get("head_sha"), path = (String)row.get("path_at_snapshot");
        String source = source(repo, sha, path, start, end);
        var metrics = new MetricEvidence(integer(row,"change_frequency"),longValue(row,"churn"),decimal(row,"max_ccn"),decimal(row,"avg_ccn"),integer(row,"function_count"),longValue(row,"nloc"),decimal(row,"hotspot_score"),decimal(row,"hhi"),(String)row.get("unavailable_reason"));
        var evidence = new LinkedHashMap<String,EvidenceReference>();
        evidence.put("E1",new EvidenceReference("E1","SNAPSHOT_METRICS","Measured metrics in the selected observation window",sha,path));
        evidence.put("E2",new EvidenceReference("E2","SOURCE_EXCERPT","Source lines "+start+"–"+end+" at snapshot HEAD",sha,path));
        var raw = new VerifiedReviewContext(UUID.randomUUID().toString(),String.valueOf(repo),String.valueOf(snapshot),String.valueOf(file),sha,(String)row.get("config_hash"),
            new Window(((Timestamp)row.get("window_start")).toInstant(),((Timestamp)row.get("window_end")).toInstant()),metrics,null,
            List.of(new SourceExcerpt(sha,path,start,end,source)),evidence,null,null);
        return withConsent(raw,null,computeContextHash(raw));
    }
    public static VerifiedReviewContext withConsent(VerifiedReviewContext c, ConsentReceipt receipt, String hash) {
        return new VerifiedReviewContext(c.reviewId(),c.repositoryId(),c.snapshotId(),c.fileId(),c.headSha(),c.configHash(),c.observationWindow(),c.metrics(),c.diff(),c.excerpts(),c.evidenceAllowlist(),receipt,hash);
    }
    String source(long repo, String sha, String path, int start, int end) {
        Path gitDir = cache.resolve("repo-"+repo+".git").normalize();
        if (!gitDir.startsWith(cache) || !Files.isDirectory(gitDir)) throw fail(HttpStatus.SERVICE_UNAVAILABLE,"SNAPSHOT_SOURCE_UNAVAILABLE");
        try (var git = new FileRepositoryBuilder().setGitDir(gitDir.toFile()).setMustExist(true).build(); var walk = new RevWalk(git)) {
            var commit = walk.parseCommit(ObjectId.fromString(sha));
            try (var tree = TreeWalk.forPath(git,path,commit.getTree())) {
                if (tree == null || tree.getFileMode(0) != FileMode.REGULAR_FILE && tree.getFileMode(0) != FileMode.EXECUTABLE_FILE) throw fail(HttpStatus.BAD_REQUEST,"INVALID_SELECTION");
                var blob = git.open(tree.getObjectId(0));
                if (blob.getSize() > 2 * 1024 * 1024) throw fail(HttpStatus.PAYLOAD_TOO_LARGE,"CONTEXT_TOO_LARGE");
                String content = new String(blob.getBytes(2*1024*1024),StandardCharsets.UTF_8);
                if (content.indexOf('\0') >= 0) throw fail(HttpStatus.BAD_REQUEST,"INVALID_SELECTION");
                var lines = content.lines().toList();
                if (end > lines.size()) throw fail(HttpStatus.BAD_REQUEST,"INVALID_SELECTION");
                String selected = String.join("\n",lines.subList(start-1,end));
                if (selected.getBytes(StandardCharsets.UTF_8).length > MAX_SOURCE_BYTES) throw fail(HttpStatus.PAYLOAD_TOO_LARGE,"CONTEXT_TOO_LARGE");
                return selected;
            }
        } catch (ResponseStatusException e) { throw e; }
        catch (Exception e) { throw fail(HttpStatus.SERVICE_UNAVAILABLE,"SNAPSHOT_SOURCE_UNAVAILABLE"); }
    }
    static Integer integer(Map<String,Object> m,String k) {return m.get(k)==null?null:((Number)m.get(k)).intValue();}
    static Long longValue(Map<String,Object> m,String k) {return m.get(k)==null?null:((Number)m.get(k)).longValue();}
    static BigDecimal decimal(Map<String,Object> m,String k) {return (BigDecimal)m.get(k);}
    static ResponseStatusException fail(HttpStatus status,String code) {return new ResponseStatusException(status,code);}
    @Configuration static class PublicCheckConfiguration {
        @Bean @ConditionalOnMissingBean(PublicRepositoryCheck.class)
        PublicRepositoryCheck publicRepositoryCheck() {
            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).followRedirects(HttpClient.Redirect.NEVER).build();
            return url -> {
                // Canonical validation prevents arbitrary hosts/paths; no credentials, cookies or redirects.
                GitHubRepositoryUrl.parse(url);
                var response = client.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).method("HEAD",HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.discarding());
                return response.statusCode()==200;
            };
        }
    }
}
