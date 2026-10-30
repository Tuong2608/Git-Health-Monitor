package com.example.demo.gitanalyzer;

import com.example.demo.monitoring.AnalysisPipeline.FileData;
import com.example.demo.monitoring.AnalysisPipeline.PairData;
import com.example.demo.monitoring.AnalysisPipeline.SnapshotData;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standalone CLI command executing the full Week 7 analysis pipeline
 * (JGit extraction -> Lizard complexity -> Metric calculation -> SnapshotData)
 * without requiring a running PostgreSQL instance.
 */
public final class GitMetricAnalysisSample {
    private static final String USAGE =
            "Usage: GitMetricAnalysisSample <https://github.com/owner/repo> <cache-dir> [window-days=180]";

    private final GitRepositoryClient client = new GitRepositoryClient();
    private final GitIngestion ingestion = new GitIngestion();
    private final LizardComplexityAnalyzer complexityAnalyzer = new LizardComplexityAnalyzer();
    private final RepositoryMetricCalculator calculator = new RepositoryMetricCalculator();

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) {
            throw new IllegalArgumentException(USAGE);
        }
        int days = args.length == 3 ? Integer.parseInt(args[2]) : 180;
        System.out.println(new GitMetricAnalysisSample().run(args[0], Paths.get(args[1]), days));
    }

    String run(String url, Path cache, int windowDays) throws Exception {
        if (windowDays < 1) {
            throw new IllegalArgumentException(USAGE);
        }
        Instant end = Instant.now();
        Instant deadline = end.plusSeconds(600);
        var pinned = Files.exists(cache)
                ? client.fetchPublic(url, cache, deadline)
                : client.clonePublic(url, cache, deadline);
        var options = GitIngestion.Options.defaults(end.minusSeconds(86_400L * windowDays), end);
        var extracted = ingestion.extract(pinned.directory(), pinned.headSha(), options);
        var complexities = complexityAnalyzer.analyzeAtHead(
                pinned.directory(),
                pinned.headSha(),
                extracted.currentFiles().keySet(),
                options.maxBlobBytes(),
                deadline);
        var snapshot = calculator.calculate(
                extracted, complexities, RepositoryMetricCalculator.Policy.defaults());
        return report(pinned.branch(), extracted, snapshot);
    }

    static String report(String branch, GitIngestion.Result r, SnapshotData s) {
        var out = new StringBuilder();
        out.append("head ").append(s.headSha()).append(" branch ").append(branch)
                .append(" metricVersion ").append(s.metricVersion()).append('\n');
        out.append("window ").append(r.start()).append(" -> ").append(r.end()).append('\n');
        out.append("reachableCommits ").append(r.reachableCommits())
                .append(" selectedCommits ").append(s.commitCount())
                .append(" currentJavaFiles ").append(s.files().size())
                .append(" hotspotPopulation ").append(s.summary().get("hotspotPopulation"))
                .append(" ambiguousFiles ").append(s.summary().get("ambiguousFiles"))
                .append(" noFunctionFiles ").append(s.summary().get("noFunctionFiles"))
                .append(" coverageComplete ").append(s.summary().get("coverageComplete"))
                .append(" couplingPairs ").append(s.pairs().size()).append('\n');
        out.append("qualityFlags ").append(s.summary().get("qualityFlags")).append('\n');

        List<FileData> ranked = s.files().stream()
                .sorted(Comparator
                        .<FileData, BigDecimal>comparing(
                                f -> f.hotspot() == null ? BigDecimal.valueOf(-1) : f.hotspot(),
                                Comparator.reverseOrder())
                        .thenComparing(FileData::lineageKey))
                .limit(10)
                .toList();

        out.append("topHotspots (tie-break: score DESC, lineageKey ASC)\n");
        for (FileData f : ranked) {
            out.append("  hotspot=").append(fmt(f.hotspot()))
                    .append(" freq=").append(f.frequency())
                    .append(" churn=").append(f.churn())
                    .append(" maxCcn=").append(fmt(f.maxCcn()))
                    .append(" fn=").append(f.functions())
                    .append(" nloc=").append(f.nloc())
                    .append(" hhi=").append(fmt(f.hhi()))
                    .append(" reason=").append(f.unavailableReason())
                    .append(" path=").append(f.path())
                    .append('\n');
        }

        Map<String, String> pathByLineage = new HashMap<>();
        for (FileData f : s.files()) {
            pathByLineage.put(f.lineageKey(), f.path());
        }

        List<PairData> strongCouplings = s.pairs().stream()
                .filter(p -> p.shared() >= 5 && couplingDegree(p).compareTo(new BigDecimal("0.5")) >= 0)
                .sorted(Comparator.<PairData, BigDecimal>comparing(GitMetricAnalysisSample::couplingDegree).reversed()
                        .thenComparing(PairData::fileA)
                        .thenComparing(PairData::fileB))
                .limit(10)
                .toList();
        out.append("strongCouplings (shared>=5, degree>=0.50): ").append(strongCouplings.size()).append('\n');
        for (PairData p : strongCouplings) {
            out.append("  degree=").append(fmt(couplingDegree(p)))
                    .append(" shared=").append(p.shared())
                    .append(" countA=").append(p.countA())
                    .append(" countB=").append(p.countB())
                    .append(" ").append(pathByLineage.getOrDefault(p.fileA(), p.fileA()))
                    .append(" <-> ").append(pathByLineage.getOrDefault(p.fileB(), p.fileB()))
                    .append('\n');
        }
        return out.toString().stripTrailing();
    }

    private static BigDecimal couplingDegree(PairData p) {
        int denom = Math.min(p.countA(), p.countB());
        if (denom <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(p.shared()).divide(BigDecimal.valueOf(denom), MathContext.DECIMAL64);
    }

    private static String fmt(BigDecimal val) {
        return val == null ? "null" : val.setScale(4, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }
}
