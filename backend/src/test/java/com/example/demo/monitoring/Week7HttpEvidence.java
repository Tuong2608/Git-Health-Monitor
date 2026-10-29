package com.example.demo.monitoring;

import java.net.http.HttpResponse;
import java.nio.file.*;
import java.util.*;

/** Optional recording of test fixtures only. Disabled unless explicitly requested by the test runner. */
final class Week7HttpEvidence {
    private Week7HttpEvidence() { }
    static synchronized void record(String suite,String method,String path,String body,HttpResponse<String> response) throws java.io.IOException {
        String target=System.getProperty("week7.evidenceDir");if(target==null || target.isBlank())return;
        var directory=Path.of(target);Files.createDirectories(directory);
        var row=new LinkedHashMap<String,Object>();row.put("source","TEST_FIXTURE_NOT_REAL_GIT");row.put("suite",suite);row.put("method",method);row.put("path","/api"+path);row.put("requestBody",body);row.put("status",response.statusCode());row.put("responseBody",MonitoringStore.JSON.readTree(response.body()));
        Files.writeString(directory.resolve("http-exchanges.jsonl"),MonitoringStore.json(row)+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);
    }
}
