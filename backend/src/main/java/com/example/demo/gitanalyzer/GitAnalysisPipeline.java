package com.example.demo.gitanalyzer;

import com.example.demo.monitoring.AnalysisPipeline;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

/**
 * Adapter bridging {@link GitRepositoryClient}, {@link GitIngestion},
 * {@link LizardComplexityAnalyzer}, and {@link RepositoryMetricCalculator}
 * into {@link AnalysisPipeline}.
 */
public final class GitAnalysisPipeline implements AnalysisPipeline {

    @FunctionalInterface
    public interface PinStrategy {
        GitRepositoryClient.PinnedRepository pin(String url, Path cacheDir, Instant deadline)
                throws IOException, GitAPIException;
    }

    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final Path cacheRoot;
    private final Duration timeout;
    private final List<String> excludedPrefixes;
    private final Map<String, String> authorAliases;
    private final int maxCommits;
    private final int maxTreeEntries;
    private final int maxBlobBytes;
    private final int maxFacts;
    private final RepositoryMetricCalculator.Policy policy;
    private final PinStrategy pinStrategy;
    private final GitIngestion ingestion;
    private final LizardComplexityAnalyzer complexityAnalyzer;
    private final RepositoryMetricCalculator metricCalculator;
    private final String effectiveConfigJson;

    public GitAnalysisPipeline(Path cacheRoot) {
        this(
                cacheRoot,
                Duration.ofMinutes(5),
                List.of("vendor/", "generated/", "target/", "build/"),
                Map.of(),
                10_000,
                100_000,
                2 * 1024 * 1024,
                500_000,
                RepositoryMetricCalculator.Policy.defaults(),
                publicPinStrategy(new GitRepositoryClient()),
                new GitIngestion(),
                new LizardComplexityAnalyzer(),
                new RepositoryMetricCalculator());
    }

    public GitAnalysisPipeline(
            Path cacheRoot,
            Duration timeout,
            List<String> excludedPrefixes,
            Map<String, String> authorAliases,
            int maxCommits,
            int maxTreeEntries,
            int maxBlobBytes,
            int maxFacts,
            RepositoryMetricCalculator.Policy policy,
            PinStrategy pinStrategy,
            GitIngestion ingestion,
            LizardComplexityAnalyzer complexityAnalyzer,
            RepositoryMetricCalculator metricCalculator) {
        this.cacheRoot = Objects.requireNonNull(cacheRoot, "cacheRoot").toAbsolutePath();
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.excludedPrefixes = List.copyOf(excludedPrefixes);
        this.authorAliases = Map.copyOf(authorAliases);
        this.maxCommits = maxCommits;
        this.maxTreeEntries = maxTreeEntries;
        this.maxBlobBytes = maxBlobBytes;
        this.maxFacts = maxFacts;
        this.policy = Objects.requireNonNull(policy, "policy");
        this.pinStrategy = Objects.requireNonNull(pinStrategy, "pinStrategy");
        this.ingestion = Objects.requireNonNull(ingestion, "ingestion");
        this.complexityAnalyzer = Objects.requireNonNull(complexityAnalyzer, "complexityAnalyzer");
        this.metricCalculator = Objects.requireNonNull(metricCalculator, "metricCalculator");
        this.effectiveConfigJson = buildEffectiveConfigJson();
    }

    public static PinStrategy publicPinStrategy(GitRepositoryClient client) {
        Objects.requireNonNull(client, "client");
        return (url, cacheDir, deadline) -> {
            if (Files.exists(cacheDir)) {
                return client.fetchPublic(url, cacheDir, deadline);
            }
            Files.createDirectories(cacheDir.getParent());
            return client.clonePublic(url, cacheDir, deadline);
        };
    }

    // Package-private seam for deterministic local repository tests without network access.
    static PinStrategy trustedLocalPinStrategy(GitRepositoryClient client) {
        Objects.requireNonNull(client, "client");
        return (uri, cacheDir, deadline) -> {
            if (Files.exists(cacheDir)) {
                return client.fetchTrusted(uri, cacheDir, deadline);
            }
            Files.createDirectories(cacheDir.getParent());
            return client.cloneTrusted(uri, cacheDir, deadline);
        };
    }

    @Override
    public String effectiveConfig() {
        return effectiveConfigJson;
    }

    @Override
    public SnapshotData analyze(Claim claim, Lease lease) throws Exception {
        Objects.requireNonNull(claim, "claim");
        Objects.requireNonNull(lease, "lease");
        lease.checkpoint();

        Instant deadline = Instant.now().plus(timeout);
        Path repoCacheDir = cacheRoot.resolve("repo-" + claim.repositoryId() + ".git");
        GitRepositoryClient.PinnedRepository pinned = pinStrategy.pin(claim.url(), repoCacheDir, deadline);
        lease.checkpoint();

        GitIngestion.Options options = new GitIngestion.Options(
                claim.windowStart(),
                claim.windowEnd(),
                maxCommits,
                maxTreeEntries,
                maxBlobBytes,
                maxFacts,
                deadline,
                excludedPrefixes,
                authorAliases);
        GitIngestion.Result extracted = ingestion.extract(pinned.directory(), pinned.headSha(), options);
        lease.checkpoint();

        Map<String, LizardComplexityAnalyzer.FileComplexity> complexities =
                complexityAnalyzer.analyzeAtHead(
                        pinned.directory(),
                        pinned.headSha(),
                        extracted.currentFiles().keySet(),
                        maxBlobBytes,
                        deadline);
        lease.checkpoint();

        SnapshotData snapshot = metricCalculator.calculate(extracted, complexities, policy);
        lease.checkpoint();
        return snapshot;
    }

    private String buildEffectiveConfigJson() {
        var map = new java.util.LinkedHashMap<String, Object>();
        map.put("metricVersion", policy.metricVersion());
        map.put("excludedPrefixes", excludedPrefixes);
        map.put("maxCommits", maxCommits);
        map.put("maxTreeEntries", maxTreeEntries);
        map.put("maxBlobBytes", maxBlobBytes);
        map.put("maxFacts", maxFacts);
        map.put("filterLargeChangesets", policy.filterLargeChangesets());
        map.put("maxChangesetFiles", policy.maxChangesetFiles());
        map.put("renameScore", 60);
        map.put("renameLimit", 1000);
        return JSON.writeValueAsString(map);
    }

    @Configuration
    static class PipelineConfiguration {
        @Bean
        @ConditionalOnMissingBean(AnalysisPipeline.class)
        @ConditionalOnProperty(name = "app.analysis.pipeline.enabled", havingValue = "true")
        AnalysisPipeline gitAnalysisPipeline(
                @Value("${app.analysis.cache-dir:target/git-analysis-cache}") String cacheDir) {
            return new GitAnalysisPipeline(Paths.get(cacheDir));
        }
    }
}
