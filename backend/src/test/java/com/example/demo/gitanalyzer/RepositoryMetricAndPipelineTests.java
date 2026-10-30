package com.example.demo.gitanalyzer;

import com.example.demo.monitoring.AnalysisPipeline;
import com.example.demo.monitoring.AnalysisPipeline.Claim;
import com.example.demo.monitoring.AnalysisPipeline.FileData;
import com.example.demo.monitoring.AnalysisPipeline.PairData;
import com.example.demo.monitoring.AnalysisPipeline.SnapshotData;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

class RepositoryMetricAndPipelineTests {
    @TempDir Path temp;
    static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    private Git init(String name) throws Exception {
        return Git.init().setDirectory(temp.resolve(name).toFile()).setInitialBranch("main").call();
    }

    private void write(Git git, String path, String content) throws Exception {
        Path p = git.getRepository().getWorkTree().toPath().resolve(path);
        Files.createDirectories(p.getParent());
        Files.writeString(p, content);
    }

    private RevCommit commit(Git git, int secondsOffset, String authorEmail) throws Exception {
        git.add().addFilepattern(".").call();
        git.add().setUpdate(true).addFilepattern(".").call();
        Date when = Date.from(BASE.plusSeconds(secondsOffset));
        return git.commit()
                .setMessage("commit-" + secondsOffset)
                .setAuthor(new PersonIdent("Author Name", authorEmail, when, TimeZone.getTimeZone("UTC")))
                .setCommitter(new PersonIdent("Committer", "committer@test", when, TimeZone.getTimeZone("UTC")))
                .call();
    }

    @Test
    void lizardAnalyzerMeasuresMaxAvgCcnFunctionCountNlocAndHandlesNoFunctionsAndBinary() throws Exception {
        try (var git = init("lizard-repo")) {
            write(git, "src/Complex.java", """
                    package demo;
                    public class Complex {
                        public int branchy(int a, int b) {
                            if (a > 0 && b > 0) {
                                return a + b;
                            } else if (a < 0) {
                                return a - b;
                            }
                            return 0;
                        }
                        public int simple() {
                            return 42;
                        }
                    }
                    """);
            write(git, "src/Marker.java", """
                    package demo;
                    public interface Marker {
                        String CONSTANT = "v1";
                    }
                    """);
            // Identical content in a second path to verify blob-SHA deduplication against Lizard duplicate-skipping
            write(git, "src/ComplexCopy.java", """
                    package demo;
                    public class Complex {
                        public int branchy(int a, int b) {
                            if (a > 0 && b > 0) {
                                return a + b;
                            } else if (a < 0) {
                                return a - b;
                            }
                            return 0;
                        }
                        public int simple() {
                            return 42;
                        }
                    }
                    """);
            write(git, "src/BinaryBlob.java", "corrupted\0binary");
            RevCommit head = commit(git, 10, "dev@example.com");

            var analyzer = new LizardComplexityAnalyzer();
            var map = analyzer.analyzeAtHead(
                    git.getRepository().getDirectory().toPath(),
                    head.name(),
                    List.of("src/Complex.java", "src/ComplexCopy.java", "src/Marker.java", "src/BinaryBlob.java"),
                    1024 * 1024,
                    Instant.now().plusSeconds(60));

            var complex = map.get("src/Complex.java");
            assertThat(complex.functions()).isEqualTo(2);
            assertThat(complex.nloc()).isGreaterThan(5L);
            assertThat(complex.maxCcn()).isEqualByComparingTo("4");
            assertThat(complex.avgCcn()).isEqualByComparingTo("2.5");
            assertThat(complex.unavailableReason()).isNull();
            assertThat(map.get("src/ComplexCopy.java")).isEqualTo(complex);

            var marker = map.get("src/Marker.java");
            assertThat(marker.functions()).isZero();
            assertThat(marker.nloc()).isGreaterThan(0L);
            assertThat(marker.maxCcn()).isNull();
            assertThat(marker.avgCcn()).isNull();
            assertThat(marker.unavailableReason()).isEqualTo("NO_FUNCTIONS");

            var binary = map.get("src/BinaryBlob.java");
            assertThat(binary.functions()).isNull();
            assertThat(binary.nloc()).isNull();
            assertThat(binary.maxCcn()).isNull();
            assertThat(binary.unavailableReason()).isEqualTo("BINARY");
        }
    }

    @Test
    void midrankPercentilesAndHotspotsMatchAlgorithmDesignFixtures() throws Exception {
        String sha = "a".repeat(40);
        Map<String, GitIngestion.FileIdentity> currentFiles = new LinkedHashMap<>();
        currentFiles.put("F1.java", new GitIngestion.FileIdentity(sha + ":F1.java", false));
        currentFiles.put("F2.java", new GitIngestion.FileIdentity(sha + ":F2.java", false));
        currentFiles.put("F3.java", new GitIngestion.FileIdentity(sha + ":F3.java", false));
        currentFiles.put("F4.java", new GitIngestion.FileIdentity(sha + ":F4.java", false));

        List<GitIngestion.Fact> facts = new ArrayList<>();
        int[] freqs = {1, 2, 2, 4};
        String[] names = {"F1.java", "F2.java", "F3.java", "F4.java"};
        for (int i = 0; i < names.length; i++) {
            for (int c = 0; c < freqs[i]; c++) {
                String cSha = String.format("%040d", i * 10 + c + 1);
                facts.add(new GitIngestion.Fact(
                        cSha, null, BASE.plusSeconds(c + 1), "dev@example.com",
                        sha + ":" + names[i], null, names[i], "MODIFY", 5, 0, Set.of()));
            }
        }

        var ingestionResult = new GitIngestion.Result(
                sha, BASE, BASE.plusSeconds(100), 10, 9, facts, currentFiles, Set.of());

        Map<String, LizardComplexityAnalyzer.FileComplexity> comp = Map.of(
                "F1.java", new LizardComplexityAnalyzer.FileComplexity(1, 10L, new BigDecimal("1"), new BigDecimal("1"), null, Set.of()),
                "F2.java", new LizardComplexityAnalyzer.FileComplexity(1, 20L, new BigDecimal("2"), new BigDecimal("2"), null, Set.of()),
                "F3.java", new LizardComplexityAnalyzer.FileComplexity(1, 20L, new BigDecimal("2"), new BigDecimal("2"), null, Set.of()),
                "F4.java", new LizardComplexityAnalyzer.FileComplexity(1, 40L, new BigDecimal("4"), new BigDecimal("4"), null, Set.of()));

        SnapshotData data = new RepositoryMetricCalculator()
                .calculate(ingestionResult, comp, RepositoryMetricCalculator.Policy.defaults());

        Map<String, FileData> byPath = new HashMap<>();
        data.files().forEach(f -> byPath.put(f.path(), f));

        assertThat(byPath.get("F1.java").hotspot()).isEqualByComparingTo("0");
        assertThat(byPath.get("F2.java").hotspot()).isEqualByComparingTo("0.5");
        assertThat(byPath.get("F3.java").hotspot()).isEqualByComparingTo("0.5");
        assertThat(byPath.get("F4.java").hotspot()).isEqualByComparingTo("1");

        // Singleton population produces 0.5
        var singleIngestion = new GitIngestion.Result(
                sha, BASE, BASE.plusSeconds(100), 1, 1,
                List.of(), Map.of("Only.java", new GitIngestion.FileIdentity(sha + ":Only.java", false)), Set.of());
        var singleComp = Map.of(
                "Only.java", new LizardComplexityAnalyzer.FileComplexity(1, 10L, new BigDecimal("3"), new BigDecimal("3"), null, Set.of()));
        SnapshotData singleData = new RepositoryMetricCalculator()
                .calculate(singleIngestion, singleComp, RepositoryMetricCalculator.Policy.defaults());
        assertThat(singleData.files().getFirst().hotspot()).isEqualByComparingTo("0.5");
    }

    @Test
    void hhiAndContributionsPreserveSumInvariantsAndRedactEmailInDisplayName() throws Exception {
        String sha = "b".repeat(40);
        String id75_25 = sha + ":Split75_25.java";
        String id50_50 = sha + ":Split50_50.java";
        String idPureRename = sha + ":PureRename.java";

        Map<String, GitIngestion.FileIdentity> currentFiles = Map.of(
                "Split75_25.java", new GitIngestion.FileIdentity(id75_25, false),
                "Split50_50.java", new GitIngestion.FileIdentity(id50_50, false),
                "PureRename.java", new GitIngestion.FileIdentity(idPureRename, false));

        List<GitIngestion.Fact> facts = List.of(
                new GitIngestion.Fact("1".repeat(40), null, BASE.plusSeconds(1), "alice@secret.org", id75_25, null, "Split75_25.java", "ADD", 75, 0, Set.of()),
                new GitIngestion.Fact("2".repeat(40), "1".repeat(40), BASE.plusSeconds(2), "bob@secret.org", id75_25, "Split75_25.java", "Split75_25.java", "MODIFY", 15, 10, Set.of()),
                new GitIngestion.Fact("3".repeat(40), "2".repeat(40), BASE.plusSeconds(3), "alice@secret.org", id50_50, null, "Split50_50.java", "ADD", 50, 0, Set.of()),
                new GitIngestion.Fact("4".repeat(40), "3".repeat(40), BASE.plusSeconds(4), "bob@secret.org", id50_50, "Split50_50.java", "Split50_50.java", "MODIFY", 50, 0, Set.of()),
                new GitIngestion.Fact("5".repeat(40), "4".repeat(40), BASE.plusSeconds(5), "alice@secret.org", idPureRename, "Old.java", "PureRename.java", "RENAME", 0, 0, Set.of()));

        var ingestionResult = new GitIngestion.Result(
                sha, BASE, BASE.plusSeconds(10), 5, 5, facts, currentFiles, Set.of());
        var comp = new LizardComplexityAnalyzer.FileComplexity(1, 20L, new BigDecimal("2"), new BigDecimal("2"), null, Set.of());
        SnapshotData snapshot = new RepositoryMetricCalculator().calculate(
                ingestionResult,
                Map.of("Split75_25.java", comp, "Split50_50.java", comp, "PureRename.java", comp),
                RepositoryMetricCalculator.Policy.defaults());

        Map<String, FileData> byPath = new HashMap<>();
        snapshot.files().forEach(f -> byPath.put(f.path(), f));

        FileData f75 = byPath.get("Split75_25.java");
        assertThat(f75.churn()).isEqualTo(100L);
        assertThat(f75.hhi()).isEqualByComparingTo("0.625");
        assertThat(f75.contributions()).extracting(AnalysisPipeline.Contribution::displayName)
                .containsExactly("alice", "bob")
                .allMatch(name -> !name.contains("@"));

        FileData f50 = byPath.get("Split50_50.java");
        assertThat(f50.churn()).isEqualTo(100L);
        assertThat(f50.hhi()).isEqualByComparingTo("0.5");

        FileData fRename = byPath.get("PureRename.java");
        assertThat(fRename.frequency()).isEqualTo(1);
        assertThat(fRename.churn()).isZero();
        assertThat(fRename.hhi()).isNull();
        assertThat(fRename.contributions()).isEmpty();
    }

    @Test
    void ambiguousLineageAndLargeChangesetFilterRespectWeek7Contract() throws Exception {
        String sha = "c".repeat(40);
        String idA = sha + ":A.java";
        String idB = sha + ":B.java";
        String idAmbig = sha + ":Ambig.java";

        Map<String, GitIngestion.FileIdentity> current = Map.of(
                "A.java", new GitIngestion.FileIdentity(idA, false),
                "B.java", new GitIngestion.FileIdentity(idB, false),
                "Ambig.java", new GitIngestion.FileIdentity(idAmbig, true));

        List<GitIngestion.Fact> facts = new ArrayList<>();
        // 5 normal shared commits touching A and B
        for (int i = 1; i <= 5; i++) {
            String cSha = String.format("%040d", i);
            facts.add(new GitIngestion.Fact(cSha, null, BASE.plusSeconds(i), "dev@test", idA, "A.java", "A.java", "MODIFY", 2, 1, Set.of()));
            facts.add(new GitIngestion.Fact(cSha, null, BASE.plusSeconds(i), "dev@test", idB, "B.java", "B.java", "MODIFY", 3, 1, Set.of()));
            facts.add(new GitIngestion.Fact(cSha, null, BASE.plusSeconds(i), "dev@test", idAmbig, "Ambig.java", "Ambig.java", "MODIFY", 1, 0, Set.of("LINEAGE_AMBIGUOUS")));
        }
        // 1 large commit touching A, B, and 3 deleted files (total 5 files in commit > maxChangesetFiles=3)
        String bigSha = String.format("%040d", 99);
        facts.add(new GitIngestion.Fact(bigSha, null, BASE.plusSeconds(99), "dev@test", idA, "A.java", "A.java", "MODIFY", 1, 0, Set.of()));
        facts.add(new GitIngestion.Fact(bigSha, null, BASE.plusSeconds(99), "dev@test", idB, "B.java", "B.java", "MODIFY", 1, 0, Set.of()));
        for (int k = 1; k <= 3; k++) {
            facts.add(new GitIngestion.Fact(bigSha, null, BASE.plusSeconds(99), "dev@test", sha + ":Del" + k + ".java", "Del.java", "Del.java", "MODIFY", 1, 0, Set.of()));
        }

        var result = new GitIngestion.Result(sha, BASE, BASE.plusSeconds(200), 6, 6, facts, current, Set.of("LINEAGE_AMBIGUOUS"));
        var comp = new LizardComplexityAnalyzer.FileComplexity(2, 30L, new BigDecimal("5"), new BigDecimal("3"), null, Set.of());
        var comps = Map.of("A.java", comp, "B.java", comp, "Ambig.java", comp);

        var calc = new RepositoryMetricCalculator();
        SnapshotData baseline = calc.calculate(result, comps, new RepositoryMetricCalculator.Policy("metric-v1", false, 3, 1000, 100));
        SnapshotData filtered = calc.calculate(result, comps, new RepositoryMetricCalculator.Policy("metric-v1", true, 3, 1000, 100));

        assertThat(baseline.summary().get("coverageComplete")).isEqualTo(false);
        FileData ambig = baseline.files().stream().filter(f -> f.path().equals("Ambig.java")).findFirst().orElseThrow();
        assertThat(ambig.frequency()).isNull();
        assertThat(ambig.churn()).isNull();
        assertThat(ambig.hotspot()).isNull();
        assertThat(ambig.hhi()).isNull();
        assertThat(ambig.unavailableReason()).isEqualTo("LINEAGE_AMBIGUOUS");
        assertThat(ambig.evidence()).doesNotContainKey("commits");

        // Ambig.java must not appear in any temporal coupling pair
        assertThat(baseline.pairs()).hasSize(1);
        PairData pairBaseline = baseline.pairs().getFirst();
        assertThat(pairBaseline.shared()).isEqualTo(6);
        assertThat(pairBaseline.countA()).isEqualTo(6);
        assertThat(pairBaseline.countB()).isEqualTo(6);

        PairData pairFiltered = filtered.pairs().getFirst();
        assertThat(pairFiltered.shared()).isEqualTo(5);
        assertThat(pairFiltered.countA()).isEqualTo(5);
        assertThat(pairFiltered.countB()).isEqualTo(5);
    }

    @Test
    void fullPipelineAdapterAndCliReportSatisfyPublisherContractOnLocalRepository() throws Exception {
        try (var origin = init("origin-repo")) {
            write(origin, "src/ServiceA.java", """
                    package app;
                    public class ServiceA {
                        public int compute(int x) {
                            if (x > 10) return x * 2;
                            return x + 1;
                        }
                    }
                    """);
            write(origin, "src/ServiceB.java", """
                    package app;
                    public class ServiceB {
                        public String format(int v) {
                            return v >= 0 ? "pos:" + v : "neg:" + v;
                        }
                    }
                    """);
            commit(origin, 10, "alice@company.io");

            for (int i = 2; i <= 5; i++) {
                write(origin, "src/ServiceA.java", """
                        package app;
                        public class ServiceA {
                            public int compute(int x) {
                                if (x > %d) return x * 2;
                                if (x < 0) return -x;
                                return x + 1;
                            }
                        }
                        """.formatted(i));
                write(origin, "src/ServiceB.java", """
                        package app;
                        public class ServiceB {
                            public String format(int v) {
                                return v >= %d ? "pos:" + v : "neg:" + v;
                            }
                        }
                        """.formatted(i));
                commit(origin, i * 10, i % 2 == 0 ? "bob@company.io" : "alice@company.io");
            }

            Path cacheRoot = temp.resolve("pipeline-cache");
            var client = new GitRepositoryClient();
            var pipeline = new GitAnalysisPipeline(
                    cacheRoot,
                    Duration.ofSeconds(60),
                    List.of("vendor/", "generated/", "target/", "build/"),
                    Map.of(),
                    10_000,
                    100_000,
                    2 * 1024 * 1024,
                    500_000,
                    RepositoryMetricCalculator.Policy.defaults(),
                    GitAnalysisPipeline.trustedLocalPinStrategy(client),
                    new GitIngestion(),
                    new LizardComplexityAnalyzer(),
                    new RepositoryMetricCalculator());

            String config = pipeline.effectiveConfig();
            assertThat(config).contains("\"metricVersion\":\"metric-v1\"");

            AtomicInteger checkpoints = new AtomicInteger();
            Claim claim = new Claim(
                    1L,
                    42L,
                    origin.getRepository().getDirectory().toURI().toString(),
                    UUID.randomUUID(),
                    BASE,
                    BASE.plusSeconds(100),
                    "hash-fixture",
                    config);

            SnapshotData snapshot = pipeline.analyze(claim, checkpoints::incrementAndGet);
            assertThat(checkpoints.get()).isGreaterThanOrEqualTo(4);
            assertThat(snapshot.commitCount()).isEqualTo(5);
            assertThat(snapshot.truncated()).isFalse();
            assertThat(snapshot.summary().get("coverageComplete")).isEqualTo(true);
            assertThat(snapshot.files()).hasSize(2);
            assertThat(snapshot.pairs()).hasSize(1);
            assertThat(snapshot.pairs().getFirst().shared()).isEqualTo(5);

            // Verify CLI sample report formatting
            var ingestionResult = new GitIngestion().extract(
                    cacheRoot.resolve("repo-42.git"),
                    snapshot.headSha(),
                    GitIngestion.Options.defaults(BASE, BASE.plusSeconds(100)));
            String cliReport = GitMetricAnalysisSample.report("refs/heads/main", ingestionResult, snapshot);
            assertThat(cliReport)
                    .contains("selectedCommits 5")
                    .contains("currentJavaFiles 2")
                    .contains("coverageComplete true")
                    .contains("strongCouplings (shared>=5, degree>=0.50): 1")
                    .contains("src/ServiceA.java <-> src/ServiceB.java");
        }
    }
}
