package com.example.demo.gitanalyzer;

import com.example.demo.monitoring.AnalysisPipeline;
import com.example.demo.monitoring.AnalysisPipeline.Contribution;
import com.example.demo.monitoring.AnalysisPipeline.FileData;
import com.example.demo.monitoring.AnalysisPipeline.PairData;
import com.example.demo.monitoring.AnalysisPipeline.SnapshotData;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;

/**
 * Computes repository maintenance metrics (change frequency, code churn, HHI contribution
 * concentration, Lizard complexity aggregation, midrank percentile hotspot score, and
 * temporal coupling) according to Metric Spec v1, algorithm-design.md, and the Week 7
 * integration contract.
 */
public final class RepositoryMetricCalculator {

    public record Policy(
            String metricVersion,
            boolean filterLargeChangesets,
            int maxChangesetFiles,
            int maxCouplingPairs,
            int maxEvidenceCommits) {
        public Policy {
            if (metricVersion == null || metricVersion.isBlank()
                    || maxChangesetFiles < 1 || maxCouplingPairs < 1 || maxEvidenceCommits < 1) {
                throw new IllegalArgumentException("Invalid metric calculation policy");
            }
        }

        public static Policy defaults() {
            return new Policy("metric-v1", false, 30, 50_000, 500);
        }
    }

    private record CoalescedCommitChange(
            String commitSha,
            String authorKey,
            String changeType,
            String oldPath,
            String newPath,
            Integer additions,
            Integer deletions,
            Set<String> qualityFlags) {}

    private record PartialFileMetric(
            String lineageKey,
            String firstSeenSha,
            String initialPath,
            String currentPath,
            Integer frequency,
            Long churn,
            BigDecimal maxCcn,
            BigDecimal avgCcn,
            Integer functions,
            Long nloc,
            BigDecimal hhi,
            String unavailableReason,
            Map<String, Object> evidence,
            List<Contribution> contributions,
            Set<String> qualityFlags) {}

    public SnapshotData calculate(
            GitIngestion.Result ingestion,
            Map<String, LizardComplexityAnalyzer.FileComplexity> complexities,
            Policy policy) throws GitIngestion.ExtractionException {
        Objects.requireNonNull(ingestion, "ingestion");
        Objects.requireNonNull(complexities, "complexities");
        Objects.requireNonNull(policy, "policy");

        Map<String, List<GitIngestion.Fact>> factsByLineage = new HashMap<>();
        for (GitIngestion.Fact fact : ingestion.facts()) {
            factsByLineage.computeIfAbsent(fact.logicalFileId(), k -> new ArrayList<>()).add(fact);
        }

        List<PartialFileMetric> partials = new ArrayList<>(ingestion.currentFiles().size());
        Set<String> unambiguousCurrentLineages = new HashSet<>();

        for (var entry : ingestion.currentFiles().entrySet()) {
            String currentPath = entry.getKey();
            GitIngestion.FileIdentity identity = entry.getValue();
            String lineageKey = identity.id();
            int colon = lineageKey.indexOf(':');
            if (colon <= 0 || colon == lineageKey.length() - 1) {
                throw new IllegalArgumentException("Invalid lineageKey format: " + lineageKey);
            }
            String firstSeenSha = lineageKey.substring(0, colon);
            String initialPath = lineageKey.substring(colon + 1);

            List<GitIngestion.Fact> rawFacts = factsByLineage.getOrDefault(lineageKey, List.of());
            List<CoalescedCommitChange> coalesced = coalesceByCommit(rawFacts);

            Set<String> fileFlags = new TreeSet<>();
            if (identity.ambiguous()) {
                fileFlags.add("LINEAGE_AMBIGUOUS");
            }
            for (CoalescedCommitChange c : coalesced) {
                fileFlags.addAll(c.qualityFlags());
            }

            LizardComplexityAnalyzer.FileComplexity comp = complexities.getOrDefault(
                    currentPath, LizardComplexityAnalyzer.FileComplexity.unavailable("CCN_UNAVAILABLE"));
            fileFlags.addAll(comp.qualityFlags());

            boolean lineageAmbiguous = fileFlags.contains("LINEAGE_AMBIGUOUS");
            boolean churnMissing = coalesced.stream().anyMatch(c -> c.additions() == null || c.deletions() == null);

            Integer frequency;
            Long churn;
            List<Contribution> contributions;
            BigDecimal hhi;
            Map<String, Object> evidence;

            if (lineageAmbiguous) {
                frequency = null;
                churn = null;
                contributions = List.of();
                hhi = null;
                evidence = Map.of("qualityFlags", List.copyOf(fileFlags));
            } else {
                unambiguousCurrentLineages.add(lineageKey);
                frequency = coalesced.size();
                List<Map<String, Object>> evidenceCommits = buildFileEvidenceCommits(coalesced, policy.maxEvidenceCommits());
                Map<String, Object> ev = new LinkedHashMap<>();
                ev.put("qualityFlags", List.copyOf(fileFlags));
                ev.put("commits", evidenceCommits);
                evidence = Collections.unmodifiableMap(ev);

                if (churnMissing) {
                    churn = null;
                    contributions = List.of();
                    hhi = null;
                } else {
                    long totalChurn = 0L;
                    Map<String, long[]> authorTotals = new LinkedHashMap<>();
                    for (CoalescedCommitChange c : coalesced) {
                        long add = c.additions();
                        long del = c.deletions();
                        totalChurn = Math.addExact(totalChurn, Math.addExact(add, del));
                        authorTotals.computeIfAbsent(c.authorKey(), k -> new long[3]);
                        long[] acc = authorTotals.get(c.authorKey());
                        acc[0] = Math.addExact(acc[0], add);
                        acc[1] = Math.addExact(acc[1], del);
                        acc[2] = Math.addExact(acc[2], 1L);
                    }
                    churn = totalChurn;
                    if (totalChurn == 0L) {
                        if (frequency > 0) {
                            fileFlags.add("NO_LINE_CONTRIBUTION");
                        }
                        contributions = List.of();
                        hhi = null;
                    } else {
                        List<Contribution> contribList = new ArrayList<>(authorTotals.size());
                        for (var aEntry : authorTotals.entrySet()) {
                            String authorKey = aEntry.getKey();
                            long[] acc = aEntry.getValue();
                            contribList.add(new Contribution(
                                    authorKey,
                                    safeDisplayName(authorKey),
                                    acc[0],
                                    acc[1],
                                    Math.toIntExact(acc[2])));
                        }
                        contribList.sort(Comparator
                                .<Contribution>comparingLong(c -> Math.addExact(c.additions(), c.deletions()))
                                .reversed()
                                .thenComparing(Contribution::identityKey));
                        contributions = List.copyOf(contribList);
                        hhi = computeHhi(contributions, totalChurn);
                    }
                }
            }

            String unavailableReason = determineUnavailableReason(
                    lineageAmbiguous, churn, fileFlags, comp);

            partials.add(new PartialFileMetric(
                    lineageKey,
                    firstSeenSha,
                    initialPath,
                    currentPath,
                    frequency,
                    churn,
                    comp.maxCcn(),
                    comp.avgCcn(),
                    comp.functions(),
                    comp.nloc(),
                    hhi,
                    unavailableReason,
                    evidence,
                    contributions,
                    fileFlags));
        }

        // Compute midrank percentiles and hotspot scores over population R
        List<PartialFileMetric> population = partials.stream()
                .filter(p -> p.frequency() != null && p.maxCcn() != null)
                .toList();
        Map<String, BigDecimal> hotspotsByLineage = computeHotspotScores(population);

        List<FileData> files = partials.stream()
                .sorted(Comparator.comparing(PartialFileMetric::lineageKey))
                .map(p -> new FileData(
                        p.lineageKey(),
                        p.firstSeenSha(),
                        p.initialPath(),
                        p.currentPath(),
                        p.frequency(),
                        p.churn(),
                        p.maxCcn(),
                        p.avgCcn(),
                        p.functions(),
                        p.nloc(),
                        hotspotsByLineage.get(p.lineageKey()),
                        p.hhi(),
                        p.unavailableReason(),
                        p.evidence(),
                        p.contributions()))
                .toList();

        List<PairData> pairs = computeTemporalCoupling(
                ingestion.facts(), unambiguousCurrentLineages, policy);

        boolean coverageComplete = ingestion.qualityFlags().isEmpty()
                && files.stream().allMatch(f ->
                        f.frequency() != null && f.churn() != null && f.functions() != null && f.nloc() != null);

        Set<String> allFlags = new TreeSet<>(ingestion.qualityFlags());
        for (PartialFileMetric p : partials) {
            allFlags.addAll(p.qualityFlags());
        }

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("coverageComplete", coverageComplete);
        summary.put("reachableCommits", ingestion.reachableCommits());
        summary.put("selectedCommits", ingestion.selectedCommits());
        summary.put("currentJavaFiles", files.size());
        summary.put("hotspotPopulation", population.size());
        summary.put("ambiguousFiles", partials.stream().filter(p -> p.frequency() == null).count());
        summary.put("noFunctionFiles", partials.stream().filter(p -> Integer.valueOf(0).equals(p.functions())).count());
        summary.put("filterLargeChangesets", policy.filterLargeChangesets());
        summary.put("qualityFlags", List.copyOf(allFlags));

        return new SnapshotData(
                ingestion.headSha(),
                policy.metricVersion(),
                ingestion.selectedCommits(),
                false,
                Collections.unmodifiableMap(summary),
                files,
                pairs);
    }

    static Map<String, BigDecimal> computeHotspotScores(List<PartialFileMetric> population) {
        int n = population.size();
        if (n == 0) {
            return Map.of();
        }
        if (n == 1) {
            return Map.of(population.getFirst().lineageKey(), new BigDecimal("0.5"));
        }
        List<BigDecimal> frequencies = population.stream()
                .map(p -> BigDecimal.valueOf(p.frequency()))
                .toList();
        List<BigDecimal> complexities = population.stream()
                .map(PartialFileMetric::maxCcn)
                .toList();

        Map<BigDecimal, BigDecimal> freqPercentile = midrankPercentiles(frequencies);
        Map<BigDecimal, BigDecimal> ccnPercentile = midrankPercentiles(complexities);

        Map<String, BigDecimal> out = new HashMap<>(n);
        for (PartialFileMetric p : population) {
            BigDecimal pChange = freqPercentile.get(BigDecimal.valueOf(p.frequency()));
            BigDecimal pCcn = ccnPercentile.get(p.maxCcn());
            BigDecimal score = pChange.multiply(pCcn, MathContext.DECIMAL64).sqrt(MathContext.DECIMAL64);
            if (score.compareTo(BigDecimal.ONE) > 0) {
                score = BigDecimal.ONE;
            }
            out.put(p.lineageKey(), score);
        }
        return out;
    }

    /**
     * Midrank percentile normalized to [0, 1]:
     * midrank(x) = lower(x) + (equal(x) + 1) / 2
     * percentile(x) = (midrank(x) - 1) / (n - 1)
     */
    static Map<BigDecimal, BigDecimal> midrankPercentiles(List<BigDecimal> values) {
        int n = values.size();
        if (n == 0) return Map.of();
        if (n == 1) return Map.of(values.getFirst(), new BigDecimal("0.5"));

        TreeMap<BigDecimal, Integer> counts = new TreeMap<>();
        for (BigDecimal v : values) {
            counts.merge(v, 1, Integer::sum);
        }

        Map<BigDecimal, BigDecimal> result = new HashMap<>();
        long lower = 0;
        BigDecimal denom = BigDecimal.valueOf(n - 1L);
        BigDecimal two = BigDecimal.valueOf(2L);
        for (var entry : counts.entrySet()) {
            BigDecimal key = entry.getKey();
            int equal = entry.getValue();
            BigDecimal midrank = BigDecimal.valueOf(lower)
                    .add(BigDecimal.valueOf(equal + 1L).divide(two, MathContext.DECIMAL64), MathContext.DECIMAL64);
            BigDecimal p = midrank.subtract(BigDecimal.ONE, MathContext.DECIMAL64)
                    .divide(denom, MathContext.DECIMAL64);
            result.put(key, p);
            lower += equal;
        }
        return result;
    }

    private static BigDecimal computeHhi(List<Contribution> contributions, long totalChurn) {
        BigDecimal total = BigDecimal.valueOf(totalChurn);
        BigDecimal sumSquares = BigDecimal.ZERO;
        for (Contribution c : contributions) {
            long devChurn = Math.addExact(c.additions(), c.deletions());
            if (devChurn > 0) {
                BigDecimal share = BigDecimal.valueOf(devChurn).divide(total, MathContext.DECIMAL64);
                sumSquares = sumSquares.add(share.multiply(share, MathContext.DECIMAL64), MathContext.DECIMAL64);
            }
        }
        if (sumSquares.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE;
        }
        return sumSquares;
    }

    private static List<PairData> computeTemporalCoupling(
            List<GitIngestion.Fact> facts,
            Set<String> unambiguousCurrentLineages,
            Policy policy) throws GitIngestion.ExtractionException {
        Map<String, Set<String>> filesByCommit = new LinkedHashMap<>();
        for (GitIngestion.Fact fact : facts) {
            filesByCommit.computeIfAbsent(fact.commitSha(), k -> new TreeSet<>()).add(fact.logicalFileId());
        }

        Map<String, Set<String>> couplingCommitsByFile = new HashMap<>();
        Map<String, Map<String, List<String>>> sharedCommitsByPair = new TreeMap<>();
        int pairCount = 0;

        for (var entry : filesByCommit.entrySet()) {
            String sha = entry.getKey();
            Set<String> touchedAll = entry.getValue();
            if (policy.filterLargeChangesets() && touchedAll.size() > policy.maxChangesetFiles()) {
                continue;
            }
            List<String> validCurrent = touchedAll.stream()
                    .filter(unambiguousCurrentLineages::contains)
                    .sorted()
                    .toList();
            for (String fileId : validCurrent) {
                couplingCommitsByFile.computeIfAbsent(fileId, k -> new LinkedHashSet<>()).add(sha);
            }
            for (int i = 0; i < validCurrent.size(); i++) {
                String a = validCurrent.get(i);
                for (int j = i + 1; j < validCurrent.size(); j++) {
                    String b = validCurrent.get(j);
                    Map<String, List<String>> inner = sharedCommitsByPair.computeIfAbsent(a, k -> new TreeMap<>());
                    List<String> sharedList = inner.get(b);
                    if (sharedList == null) {
                        pairCount++;
                        if (pairCount > policy.maxCouplingPairs()) {
                            throw new GitIngestion.ExtractionException("RESOURCE_LIMIT_COUPLING_PAIRS");
                        }
                        sharedList = new ArrayList<>();
                        inner.put(b, sharedList);
                    }
                    sharedList.add(sha);
                }
            }
        }

        List<PairData> pairs = new ArrayList<>(pairCount);
        for (var outer : sharedCommitsByPair.entrySet()) {
            String a = outer.getKey();
            int countA = couplingCommitsByFile.getOrDefault(a, Set.of()).size();
            for (var inner : outer.getValue().entrySet()) {
                String b = inner.getKey();
                int countB = couplingCommitsByFile.getOrDefault(b, Set.of()).size();
                List<String> sharedShas = inner.getValue();
                int shared = sharedShas.size();
                if (shared > 0 && countA > 0 && countB > 0) {
                    List<Map<String, Object>> commitItems = sharedShas.stream()
                            .limit(policy.maxEvidenceCommits())
                            .map(sha -> Map.<String, Object>of("commitSha", sha))
                            .toList();
                    pairs.add(new PairData(a, b, shared, countA, countB, Map.of("commits", commitItems)));
                }
            }
        }
        return List.copyOf(pairs);
    }

    private static List<CoalescedCommitChange> coalesceByCommit(List<GitIngestion.Fact> facts) {
        Map<String, CoalescedCommitChange> byCommit = new LinkedHashMap<>();
        for (GitIngestion.Fact f : facts) {
            byCommit.compute(f.commitSha(), (sha, existing) -> {
                if (existing == null) {
                    return new CoalescedCommitChange(
                            f.commitSha(),
                            f.authorKey(),
                            f.changeType(),
                            f.oldPath(),
                            f.newPath(),
                            f.additions(),
                            f.deletions(),
                            new TreeSet<>(f.qualityFlags()));
                }
                Set<String> mergedFlags = new TreeSet<>(existing.qualityFlags());
                mergedFlags.addAll(f.qualityFlags());
                Integer add = (existing.additions() == null || f.additions() == null)
                        ? null
                        : Math.addExact(existing.additions(), f.additions());
                Integer del = (existing.deletions() == null || f.deletions() == null)
                        ? null
                        : Math.addExact(existing.deletions(), f.deletions());
                String nextPath = f.newPath() != null ? f.newPath() : existing.newPath();
                return new CoalescedCommitChange(
                        sha, existing.authorKey(), f.changeType(), existing.oldPath(), nextPath, add, del, mergedFlags);
            });
        }
        return List.copyOf(byCommit.values());
    }

    private static List<Map<String, Object>> buildFileEvidenceCommits(
            List<CoalescedCommitChange> coalesced, int maxEvidenceCommits) {
        List<Map<String, Object>> out = new ArrayList<>(Math.min(coalesced.size(), maxEvidenceCommits));
        for (int i = 0; i < Math.min(coalesced.size(), maxEvidenceCommits); i++) {
            CoalescedCommitChange c = coalesced.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("commitSha", c.commitSha());
            item.put("changeType", c.changeType());
            if (c.oldPath() != null) item.put("oldPath", c.oldPath());
            if (c.newPath() != null) item.put("newPath", c.newPath());
            out.add(Collections.unmodifiableMap(item));
        }
        return List.copyOf(out);
    }

    private static String determineUnavailableReason(
            boolean lineageAmbiguous,
            Long churn,
            Set<String> fileFlags,
            LizardComplexityAnalyzer.FileComplexity comp) {
        if (lineageAmbiguous) {
            return "LINEAGE_AMBIGUOUS";
        }
        if (churn == null) {
            if (fileFlags.contains("BINARY")) return "BINARY";
            if (fileFlags.contains("NON_REGULAR_FILE")) return "NON_REGULAR_FILE";
            return "CHURN_UNAVAILABLE";
        }
        if (comp.functions() == null || comp.nloc() == null) {
            return comp.unavailableReason() != null ? comp.unavailableReason() : "CCN_UNAVAILABLE";
        }
        if (comp.maxCcn() == null) {
            return comp.unavailableReason() != null ? comp.unavailableReason() : "NO_FUNCTIONS";
        }
        return null;
    }

    static String safeDisplayName(String authorKey) {
        if (authorKey == null || authorKey.isBlank() || authorKey.startsWith("unknown:")) {
            return "Unknown contributor";
        }
        int at = authorKey.indexOf('@');
        if (at > 0) {
            return authorKey.substring(0, at);
        }
        return authorKey;
    }
}
