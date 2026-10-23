package com.example.demo.monitoring;

import java.util.concurrent.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import jakarta.annotation.PreDestroy;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name="app.analysis.enabled",havingValue="true")
public class AnalysisWorker {
    private final MonitoringStore store;
    private final SnapshotPublisher publisher;
    private final AlertService alerts;
    private final ObjectProvider<AnalysisPipeline> pipelines;
    private final ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
    private final ScheduledExecutorService timer=Executors.newScheduledThreadPool(2);
    public AnalysisWorker(MonitoringStore store,SnapshotPublisher publisher,AlertService alerts,ObjectProvider<AnalysisPipeline> pipelines) {
        this.store=store;this.publisher=publisher;this.alerts=alerts;this.pipelines=pipelines;
        executor.setCorePoolSize(2);executor.setMaxPoolSize(2);executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("analysis-");executor.setWaitForTasksToCompleteOnShutdown(false);
        executor.initialize();
    }
    @Scheduled(fixedDelay=1000,initialDelay=2000)
    public void dispatch() {
        if(pipelines.getIfAvailable()==null) return;
        // Claim inside the running task. Saturation therefore never strands a RUNNING job in memory.
        for(int i=0;i<2;i++) {
            try { executor.execute(this::runOne); }
            catch(TaskRejectedException full) { return; }
        }
    }
    public void runOne() {
        var pipeline=pipelines.getIfAvailable(); if(pipeline==null) return;
        store.claim().ifPresent(c->{
            Thread thread=Thread.currentThread();
            Object lifecycle=new Object();
            boolean[] finished={false};
            var heartbeat=timer.scheduleAtFixedRate(()->{
                synchronized(lifecycle) {
                    if(finished[0]) return;
                    try { if(!store.heartbeat(c)) thread.interrupt(); }
                    catch(RuntimeException databaseUnavailable) { thread.interrupt(); }
                }
            },30,30,TimeUnit.SECONDS);
            var deadline=timer.schedule(()->{
                synchronized(lifecycle) {
                    if(finished[0]) return;
                    try { store.fail(c,"ANALYSIS_TIMEOUT"); } finally { thread.interrupt(); }
                }
            },15,TimeUnit.MINUTES);
            try {
                // A queued job may outlive deployment of a new adapter configuration. Never mislabel its data.
                if(!MonitoringStore.hash(pipeline.effectiveConfig()).equals(c.configHash())) {
                    store.fail(c,"CONFIG_CHANGED_REQUEUE_REQUIRED");return;
                }
                var result=pipeline.analyze(c,()->{
                    if(Thread.currentThread().isInterrupted() || !store.heartbeat(c)) throw new IllegalStateException("Lease lost");
                });
                publisher.publish(c,result);
            } catch(Exception failure) { store.fail(c,"ANALYSIS_FAILED"); }
            finally {
                // A late timer callback must not interrupt the next job on the reused worker thread.
                synchronized(lifecycle) { finished[0]=true;heartbeat.cancel(false);deadline.cancel(false); }
                Thread.interrupted();
            }
        });
    }
    @Scheduled(fixedDelay=10000,initialDelay=2000)
    public void scheduleDue() {
        var pipeline=pipelines.getIfAvailable();if(pipeline!=null) store.scheduleDue(pipeline.effectiveConfig());
    }
    @Scheduled(fixedDelay=30000,initialDelay=2000)
    public void recover() { store.recoverExpired(); }
    @Scheduled(fixedDelay=2000,initialDelay=2000)
    public void evaluate() {
        try { alerts.evaluateNext(); }
        catch(AlertService.EvaluationFailure failure) { alerts.recordFailure(failure.snapshotId); }
    }
    @PreDestroy public void stop() { executor.shutdown();timer.shutdownNow(); }
}
