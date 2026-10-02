package com.example.demo.repository;

import java.net.URI;
import java.net.http.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "app.registration-enabled=false")
class RegistrationDisabledTests {
    @Value("${local.server.port}") int port;

    @Test
    void publicPreviewCannotRegisterWhenDisabled() throws Exception {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/repositories"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"url\":\"https://github.com/toan/demo\"}")).build();
        assertThat(HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString()).statusCode())
                .isEqualTo(403);
    }
}
