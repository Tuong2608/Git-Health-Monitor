package com.example.demo.repository;

import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

/** Real HTTP + PostgreSQL contract checks. Use a disposable DB, never staging. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.registration-enabled=true")
class RepositoryContractTests {
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate jdbc;
    final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    static final JsonMapper JSON = JsonMapper.builder().build();
    static final Path EVIDENCE = Path.of("target", "contract-http-exchanges.jsonl");

    @BeforeAll static void prepareEvidence() throws Exception {
        Files.createDirectories(EVIDENCE.getParent());
        Files.writeString(EVIDENCE, "");
    }
    static synchronized void record(String method, String path, String body, HttpResponse<String> r) throws Exception {
        Files.writeString(EVIDENCE, JSON.writeValueAsString(Map.of(
            "method", method, "path", path, "requestBody", body,
            "status", r.statusCode(), "responseBody", r.body(),
            "location", r.headers().firstValue("Location").orElse(""))) + "\n",
            StandardOpenOption.APPEND);
    }
    HttpResponse<String> send(String method, String path, String body) throws Exception {
        var b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .timeout(Duration.ofSeconds(10));
        if (method.equals("POST")) b.header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));
        else b.GET();
        var r = client.send(b.build(), HttpResponse.BodyHandlers.ofString());
        record(method, path, body, r);
        return r;
    }
    HttpResponse<String> post(String body) throws Exception { return send("POST", "/api/repositories", body); }
    HttpResponse<String> get(String path) throws Exception { return send("GET", path, ""); }
    String body(String url) { return JSON.writeValueAsString(Map.of("url", url)); }
    long count() { return jdbc.queryForObject("select count(*) from tracked_repository", Long.class); }
    void safeError(HttpResponse<String> r, int status) {
        assertThat(r.statusCode()).isEqualTo(status);
        assertThat(r.body()).doesNotContain("stackTrace", "org.springframework", "java.lang.", "jdbc:postgresql", "SQLState");
    }

    @Test void responseShapeMatchesPersistedRowAndReadback() throws Exception {
        String name = "contract-" + UUID.randomUUID();
        String canonical = "https://github.com/contract/" + name;
        long before = count();
        var created = post(body(" https://GitHub.com/Contract/" + name + ".git/ "));
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.headers().firstValue("Content-Type").orElse("")).startsWith("application/json");
        var dto = JSON.readTree(created.body());
        assertThat(dto.size()).isEqualTo(5);
        assertThat(dto.get("id").isIntegralNumber()).isTrue();
        long id = dto.get("id").longValue();
        assertThat(id).isPositive();
        assertThat(dto.get("url").asText()).isEqualTo(canonical);
        assertThat(dto.get("owner").asText()).isEqualTo("contract");
        assertThat(dto.get("name").asText()).isEqualTo(name);
        assertThat(Instant.parse(dto.get("createdAt").asText())).isBeforeOrEqualTo(Instant.now());
        assertThat(created.headers().firstValue("Location")).contains("/api/repositories/" + id);
        var read = get("/api/repositories/" + id);
        assertThat(read.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(read.body())).isEqualTo(dto);
        assertThat(jdbc.queryForObject("select url from tracked_repository where id=?", String.class, id)).isEqualTo(canonical);
        assertThat(count()).isEqualTo(before + 1);
        safeError(post(body(canonical + "/")), 409);
        assertThat(count()).isEqualTo(before + 1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"url\":null}", "{\"url\":\"\"}", "{\"url\":\" \"}", "not-json",
        "{\"url\":\"file:///tmp/repo\"}", "{\"url\":\"https://github.com.evil.test/a/b\"}",
        "{\"url\":\"https://github.com/a/b/tree/main\"}"})
    void invalidBodyDoesNotPersist(String input) throws Exception {
        long before = count();
        safeError(post(input), 400);
        assertThat(count()).isEqualTo(before);
    }
    @Test void overlongUrlIsRejectedWithoutWrite() throws Exception {
        long before = count();
        safeError(post(body("https://github.com/a/" + "x".repeat(513))), 400);
        assertThat(count()).isEqualTo(before);
    }
    @ParameterizedTest
    @ValueSource(strings = {"size=0", "size=-1", "size=101", "page=-1", "page=abc", "size=abc"})
    void invalidPaginationIs400(String query) throws Exception {
        safeError(get("/api/repositories?" + query), 400);
    }
    @Test void listBoundariesOrderingEmptyPageAndMissingId() throws Exception {
        for (int i=0;i<3;i++) assertThat(post(body("https://github.com/contract/list-" + UUID.randomUUID())).statusCode()).isEqualTo(201);
        for (int size : List.of(1,100)) {
            var response = get("/api/repositories?page=0&size=" + size);
            assertThat(response.statusCode()).isEqualTo(200);
            var rows = JSON.readTree(response.body());
            assertThat(rows.isArray()).isTrue();
            assertThat(rows.size()).isBetween(1,size);
            long previous = Long.MAX_VALUE;
            for (var row : rows) { long id=row.get("id").longValue(); assertThat(id).isLessThan(previous); previous=id; }
        }
        var empty = get("/api/repositories?page=2147483647&size=1");
        assertThat(empty.statusCode()).isEqualTo(200);
        assertThat(JSON.readTree(empty.body()).isArray()).isTrue();
        assertThat(JSON.readTree(empty.body()).size()).isZero();
        safeError(get("/api/repositories/9223372036854775807"),404);
        safeError(get("/api/repositories/not-a-number"),400);
        var health=get("/api/health");
        assertThat(health.statusCode()).isEqualTo(200);
        assertThat(health.body()).isEqualTo("OK");
    }
    @Test void concurrentCanonicalDuplicatesPersistOnlyOnce() throws Exception {
        String url="https://github.com/contract/race-" + UUID.randomUUID();
        try (var pool=Executors.newFixedThreadPool(2)) {
            var ready=new CountDownLatch(2); var start=new CountDownLatch(1);
            Callable<Integer> first=()->{ready.countDown();start.await();return post(body(url)).statusCode();};
            Callable<Integer> second=()->{ready.countDown();start.await();return post(body(url+".git/")).statusCode();};
            var a=pool.submit(first);var b=pool.submit(second);
            assertThat(ready.await(5,TimeUnit.SECONDS)).isTrue();start.countDown();
            assertThat(List.of(a.get(15,TimeUnit.SECONDS),b.get(15,TimeUnit.SECONDS))).containsExactlyInAnyOrder(201,409);
            assertThat(jdbc.queryForObject("select count(*) from tracked_repository where url=?", Long.class,url)).isEqualTo(1);
        }
    }
}