package com.example.demo.monitoring;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"app.analysis.enabled=false","app.analysis.writes-enabled=false"})
class MonitoringDisabledTests {
    @Value("${local.server.port}") int port;
    @Test void writesDefaultToForbidden() throws Exception {
        for(String route:new String[]{"/repositories/1/analyses","/repositories/1/alert-rules"}) {
            String body=route.endsWith("analyses")?"{}":"{\"type\":\"HOTSPOT_INCREASE\",\"threshold\":0.1,\"enabled\":true}";
            var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api"+route)).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).isEqualTo(403);
        }
    }
}
