package com.example.demo.aireview;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
class AiReviewDisabledTests {
    @Value("${local.server.port}") int port;
    @Test void disabledPreviewAndReviewNeverExposeSource() throws Exception {
        for(String endpoint:new String[]{"ai-review-preview","ai-reviews"}) {
            var r=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/snapshots/1/files/1/"+endpoint)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{\"startLine\":1,\"endLine\":2,\"consent\":true,\"contextHash\":\"x\"}")).build(),HttpResponse.BodyHandlers.ofString());
            assertThat(r.statusCode()).isEqualTo(403);assertThat(r.body()).contains("AI_REVIEW_DISABLED");
        }
    }
}

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties="app.ai-review.enabled=true")
class AiReviewProviderUnavailableTests {
    @Value("${local.server.port}") int port;
    @Test void enabledWithoutAdapterReportsUnavailableInsteadOfFakeSuccess() throws Exception {
        var r=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/snapshots/1/files/1/ai-reviews")).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString("{\"startLine\":1,\"endLine\":2,\"consent\":true,\"contextHash\":\"x\"}")).build(),HttpResponse.BodyHandlers.ofString());
        assertThat(r.statusCode()).isEqualTo(503);assertThat(r.body()).contains("AI_PROVIDER_UNAVAILABLE");
    }
}
