package com.example.demo.monitoring;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Integration port for Tuong's ingestion/metrics module. No production mock is registered. */
public interface AnalysisPipeline {
    /** Canonical configuration INCLUDING tool/algorithm versions and filtering policies. No secrets. */
    String effectiveConfig();
    SnapshotData analyze(Claim claim, Lease lease) throws Exception;
    interface Lease { void checkpoint(); }
    record Claim(long id, long repositoryId, String url, UUID token,
                 Instant windowStart, Instant windowEnd, String configHash, String config) { }
    record SnapshotData(String headSha, String metricVersion, int commitCount, boolean truncated,
                        Map<String,Object> summary, List<FileData> files, List<PairData> pairs) { }
    /** lineageKey is repository-scoped and stable across renames, supplied by ingestion. */
    record FileData(String lineageKey, String firstSeenSha, String initialPath, String path,
                    int frequency, long churn, BigDecimal maxCcn, BigDecimal avgCcn,
                    int functions, long nloc, BigDecimal hotspot, BigDecimal hhi,
                    String unavailableReason, Map<String,Object> evidence, List<Contribution> contributions) { }
    record Contribution(String identityKey, String displayName, long additions, long deletions, int commits) { }
    record PairData(String fileA, String fileB, int shared, int countA, int countB, Map<String,Object> evidence) { }
}
