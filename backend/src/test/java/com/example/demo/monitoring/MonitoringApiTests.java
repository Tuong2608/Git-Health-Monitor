package com.example.demo.monitoring;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,properties={"app.analysis.enabled=false","app.analysis.writes-enabled=true"})
class MonitoringApiTests {
    @Value("${local.server.port}") int port;
    @Autowired JdbcTemplate db;
    HttpResponse<String> request(String method,String path,String body) throws Exception {
        return HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api"+path)).timeout(Duration.ofSeconds(10)).header("Content-Type","application/json").method(method,HttpRequest.BodyPublishers.ofString(body)).build(),HttpResponse.BodyHandlers.ofString());
    }
    long repo(){String n=UUID.randomUUID().toString();return db.queryForObject("INSERT INTO tracked_repository(url,owner,name,created_at) VALUES (?,'fixture',?,now()) RETURNING id",Long.class,"https://github.com/fixture/"+n,n);}
    @Test void unavailablePipelineIsExplicitAndDoesNotEnqueueFakeWork() throws Exception {
        long r=repo();String p="/repositories/"+r;
        assertThat(request("POST",p+"/analyses","{}").statusCode()).isEqualTo(503);
        assertThat(request("PUT",p+"/monitoring","{\"enabled\":true,\"intervalMinutes\":60,\"version\":1}").statusCode()).isEqualTo(503);
        assertThat(request("GET",p+"/snapshots","").body()).contains("\"items\":[]","\"hasNext\":false");
        assertThat(request("GET",p+"/monitoring","").body()).contains("\"monitoringEnabled\":false");
        assertThat(request("PUT",p+"/monitoring","{\"enabled\":false,\"intervalMinutes\":60,\"version\":1}").statusCode()).isEqualTo(200);
    }
    @Test void ruleValidationJsonShapePaginationAndMissingResources() throws Exception {
        long r=repo();String p="/repositories/"+r;
        var created=request("POST",p+"/alert-rules","{\"type\":\"STRONG_COUPLING\",\"threshold\":0.5,\"minShared\":5,\"enabled\":true}");
        assertThat(created.statusCode()).isEqualTo(201);assertThat(created.body()).contains("\"parameters\":{","\"repositoryId\":").doesNotContain("\"type\":\"jsonb\"");
        assertThat(request("POST",p+"/alert-rules","{\"type\":\"STRONG_COUPLING\",\"threshold\":0.1,\"minShared\":1,\"enabled\":true}").statusCode()).isEqualTo(400);
        assertThat(request("GET",p+"/alerts?status=INVALID","").statusCode()).isEqualTo(400);
        assertThat(request("GET",p+"/snapshots?size=101","").statusCode()).isEqualTo(400);
        assertThat(request("GET","/jobs/9223372036854775807","").statusCode()).isEqualTo(404);
        assertThat(request("GET","/snapshots/9223372036854775807","").statusCode()).isEqualTo(404);
    }
    @Test void localCorsAllowsScheduleAndAlertMethodsOnlyForConfiguredOrigins() throws Exception {
        for(String method: new String[]{"PUT","PATCH"}) {
            for(String origin:new String[]{"http://localhost:5173","https://unknown.example"}) {
                var response=HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/repositories/1/monitoring")).header("Origin",origin).header("Access-Control-Request-Method",method).method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build(),HttpResponse.BodyHandlers.ofString());
                assertThat(response.statusCode()).isEqualTo(origin.contains("unknown")?403:200);
            }
        }
    }
}
