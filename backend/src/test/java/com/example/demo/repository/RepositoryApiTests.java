package com.example.demo.repository;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

/** Runs against a real PostgreSQL database supplied through DB_* environment variables. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.registration-enabled=true")
class RepositoryApiTests {
    @Value("${local.server.port}") int port;
    final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    HttpResponse<String> post(String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/repositories"))
                .timeout(Duration.ofSeconds(10)).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> get(String path) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(10)).build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void createReadAndRejectCanonicalDuplicate() throws Exception {
        String name = "test-" + UUID.randomUUID();
        var created = post("{\"url\":\"https://github.com/Toan/" + name + ".git\"}");
        assertThat(created.statusCode()).isEqualTo(201);
        assertThat(created.body()).contains("https://github.com/toan/" + name);
        assertThat(get(created.headers().firstValue("Location").orElseThrow()).statusCode()).isEqualTo(200);
        assertThat(post("{\"url\":\"https://github.com/toan/" + name + "/\"}").statusCode()).isEqualTo(409);
    }

    @Test
    void invalidInputsAreClientErrors() throws Exception {
        assertThat(post("{\"url\":\"file:///tmp/repo\"}").statusCode()).isEqualTo(400);
        assertThat(post("{\"url\":\"\"}").statusCode()).isEqualTo(400);
        assertThat(post("{}").statusCode()).isEqualTo(400);
        assertThat(post("not-json").statusCode()).isEqualTo(400);
        assertThat(get("/api/repositories?size=101").statusCode()).isEqualTo(400);
        assertThat(get("/api/repositories?page=-1").statusCode()).isEqualTo(400);
        assertThat(get("/api/repositories/9223372036854775807").statusCode()).isEqualTo(404);
    }

    @Test
    void corsAcceptsLocalBrowserAndRejectsUnknownOrigin() throws Exception {
        for (String origin : java.util.List.of("http://localhost:5173", "http://127.0.0.1:5173", "https://unknown.example")) {
            var response = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/repositories"))
                    .header("Origin", origin).header("Access-Control-Request-Method", "POST")
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(origin.contains("unknown") ? 403 : 200);
        }
    }

    @Test
    void listAndExistingHealthEndpointWork() throws Exception {
        assertThat(get("/api/repositories?size=1").statusCode()).isEqualTo(200);
        assertThat(get("/api/health").body()).isEqualTo("OK");
    }
}
