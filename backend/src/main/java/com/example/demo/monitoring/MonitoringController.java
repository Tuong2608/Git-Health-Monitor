package com.example.demo.monitoring;

import java.net.URI;
import java.sql.Timestamp;
import java.util.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import static com.example.demo.monitoring.MonitoringStore.*;

@RestController
@RequestMapping("/api")
public class MonitoringController {
    private final MonitoringStore store;
    private final AlertService alerts;
    private final ObjectProvider<AnalysisPipeline> pipelines;
    private final boolean writes,enabled;
    public MonitoringController(MonitoringStore store,AlertService alerts,ObjectProvider<AnalysisPipeline> pipelines,
            @Value("${app.analysis.writes-enabled:false}") boolean writes,@Value("${app.analysis.enabled:false}") boolean enabled) {
        this.store=store;this.alerts=alerts;this.pipelines=pipelines;this.writes=writes;this.enabled=enabled;
    }
    void writeAccess() { if(!writes) throw error(HttpStatus.FORBIDDEN,"MONITORING_WRITES_DISABLED"); }
    AnalysisPipeline pipeline() {
        var p=pipelines.getIfAvailable();
        if(!enabled || p==null) throw error(HttpStatus.SERVICE_UNAVAILABLE,"ANALYSIS_PIPELINE_UNAVAILABLE");
        return p;
    }
    public record AnalysisRequest(Integer observationDays) { }
    public record ScheduleRequest(Boolean enabled,Integer intervalMinutes,Integer version) { }
    public record StatusRequest(String status) { }
    @PostMapping("/repositories/{repo}/analyses")
    public ResponseEntity<?> enqueue(@PathVariable long repo,@RequestBody AnalysisRequest r) {
        writeAccess();var p=pipeline();
        long id=store.enqueue(repo,r.observationDays()==null?180:r.observationDays(),p.effectiveConfig());
        return ResponseEntity.accepted().location(URI.create("/api/jobs/"+id)).body(Map.of("jobId",id,"status","QUEUED"));
    }
    @GetMapping("/jobs/{id}") public Object job(@PathVariable long id) { return dto(store.job(id)); }
    @GetMapping("/repositories/{repo}/monitoring") public Object settings(@PathVariable long repo) { return dto(store.settings(repo)); }
    @PutMapping("/repositories/{repo}/monitoring") public Object settings(@PathVariable long repo,@RequestBody ScheduleRequest r) {
        writeAccess();
        if(r.enabled()==null || r.intervalMinutes()==null || r.version()==null) throw new IllegalArgumentException("enabled, intervalMinutes and version required");
        if(r.enabled()) pipeline();
        return dto(store.settings(repo,r.enabled(),r.intervalMinutes(),r.version()));
    }
    @GetMapping("/repositories/{repo}/snapshots") public Object snapshots(@PathVariable long repo,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        int offset=offset(page,size);return collection(store.snapshots(repo,size+1,offset),page,size);
    }
    @GetMapping("/snapshots/{id}") public Object snapshot(@PathVariable long id) { return dto(store.snapshot(id)); }
    @GetMapping("/snapshots/{id}/hotspots") public Object files(@PathVariable long id,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        int offset=offset(page,size);return collection(store.files(id,size+1,offset),page,size);
    }
    @GetMapping("/repositories/{repo}/alert-rules") public Object rules(@PathVariable long repo,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        int offset=offset(page,size);return collection(alerts.rules(repo,size+1,offset),page,size);
    }
    @PostMapping("/repositories/{repo}/alert-rules") public ResponseEntity<?> create(@PathVariable long repo,@RequestBody AlertService.RuleRequest r) {
        writeAccess();var created=alerts.create(repo,r);
        return ResponseEntity.created(URI.create("/api/alert-rules/"+created.get("id"))).body(dto(created));
    }
    @PutMapping("/alert-rules/{id}") public Object update(@PathVariable long id,@RequestBody AlertService.RuleRequest r) { writeAccess();return dto(alerts.update(id,r)); }
    @GetMapping("/repositories/{repo}/alerts") public Object alerts(@PathVariable long repo,@RequestParam(defaultValue="ALL") String status,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        int offset=offset(page,size);return collection(alerts.alerts(repo,status,size+1,offset),page,size);
    }
    @PatchMapping("/alerts/{id}") public Object transition(@PathVariable long id,@RequestBody StatusRequest r) { writeAccess();return dto(alerts.transition(id,r.status())); }
    static int offset(int page,int size) {
        if(page<0 || size<1 || size>100 || (long)page*size>Integer.MAX_VALUE) throw new IllegalArgumentException("page>=0; size=1..100; offset within integer range");
        return page*size;
    }
    static Object collection(List<Map<String,Object>> rows,int page,int size) {
        return Map.of("items",rows.stream().limit(size).map(MonitoringController::dto).toList(),"page",page,"size",size,"hasNext",rows.size()>size);
    }
    static Map<String,Object> dto(Map<String,Object> row) {
        var result=new LinkedHashMap<String,Object>();
        row.forEach((key,value)->{
            String camel=java.util.regex.Pattern.compile("_([a-z])").matcher(key).replaceAll(m->m.group(1).toUpperCase(Locale.ROOT));
            if(value instanceof Timestamp t) value=t.toInstant();
            else if(value!=null && value.getClass().getName().equals("org.postgresql.util.PGobject")) value=JSON.readTree(value.toString());
            result.put(camel,value);
        });return result;
    }
}
