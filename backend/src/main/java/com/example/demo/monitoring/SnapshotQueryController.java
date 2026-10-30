package com.example.demo.monitoring;

import org.springframework.web.bind.annotation.*;
import static com.example.demo.monitoring.MonitoringController.*;

@RestController
@RequestMapping("/api")
public class SnapshotQueryController {
    private final SnapshotQueries queries;
    public SnapshotQueryController(SnapshotQueries queries) { this.queries=queries; }
    @GetMapping("/snapshots/{snapshot}/files/{file}")
    public Object file(@PathVariable long snapshot,@PathVariable long file) { return dto(queries.file(snapshot,file)); }
    @GetMapping("/snapshots/{snapshot}/files/{file}/contributions")
    public Object contributions(@PathVariable long snapshot,@PathVariable long file,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return collection(queries.contributions(snapshot,file,size+1,offset(page,size)),page,size);
    }
    @GetMapping("/snapshots/{snapshot}/files/{file}/evidence")
    public Object evidence(@PathVariable long snapshot,@PathVariable long file,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return collection(queries.evidence(snapshot,file,size+1,offset(page,size)),page,size);
    }
    @GetMapping("/snapshots/{snapshot}/files/{file}/couplings")
    public Object couplings(@PathVariable long snapshot,@PathVariable long file,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return collection(queries.couplings(snapshot,file,size+1,offset(page,size)),page,size);
    }
    @GetMapping("/snapshots/{snapshot}/files/{file}/couplings/{related}/evidence")
    public Object couplingEvidence(@PathVariable long snapshot,@PathVariable long file,@PathVariable long related,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return collection(queries.couplingEvidence(snapshot,file,related,size+1,offset(page,size)),page,size);
    }
    @GetMapping("/repositories/{repo}/files/{file}/trend")
    public Object trend(@PathVariable long repo,@PathVariable long file,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return collection(queries.trend(repo,file,size+1,offset(page,size)),page,size);
    }
    @GetMapping("/repositories/{repo}/snapshot-comparison")
    public Object comparison(@PathVariable long repo,@RequestParam long fromSnapshotId,@RequestParam long toSnapshotId,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return queries.comparison(repo,fromSnapshotId,toSnapshotId,page,size);
    }
}
