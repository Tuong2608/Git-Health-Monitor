package com.example.demo.monitoring;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.json.JsonMapper;
import static com.example.demo.monitoring.AnalysisPipeline.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Week 10 PostgreSQL persistence benchmark (STT 32 / NFR03) and RQ2 invariant parity tests (STT 31).
 */
@SpringBootTest(properties = {"app.analysis.enabled=false", "app.analysis.writes-enabled=false"})
class Week10PersistenceBenchmarkTests {

    private static final String CONFIG = "{\"metric\":\"v1\",\"tool\":\"week10-postgres-benchmark\"}";
    private static final Instant BASE_END = Instant.parse("2026-05-01T00:00:00Z");
    private static final Instant TARGET_END = Instant.parse("2026-07-01T00:00:00Z");
    private static final Instant THIRD_END = Instant.parse("2026-09-01T00:00:00Z");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    MonitoringStore store;

    @Autowired
    SnapshotPublisher publisher;

    @Autowired
    AlertService alerts;

    @Autowired
    SnapshotQueries queries;

    @Autowired
    JdbcTemplate db;

    @BeforeEach
    void clean() {
        db.execute(
                "TRUNCATE alert,file_contribution,temporal_coupling,file_metric,analysis_snapshot,analysis_job,logical_file,contributor,alert_rule,tracked_repository RESTART IDENTITY CASCADE");
    }

    private long createRepo(String suffix) {
        String name = "week10-" + suffix + "-" + UUID.randomUUID();
        return db.queryForObject(
                "INSERT INTO tracked_repository(url,owner,name,created_at) VALUES (?,'bench',?,now()) RETURNING id",
                Long.class,
                "https://github.com/bench/" + name,
                name);
    }

    private Claim claimAt(long repo, Instant end) {
        db.update(
                "INSERT INTO analysis_job(repository_id,status,requested_at,window_start,window_end,config_hash,config) VALUES (?,'QUEUED',now(),?,?,?,?::jsonb)",
                repo,
                Timestamp.from(end.minusSeconds(180L * 86400)),
                Timestamp.from(end),
                MonitoringStore.hash(CONFIG),
                CONFIG);
        return store.claim().orElseThrow();
    }

    private static SnapshotData syntheticSnapshot(
            String shaChar, int commitCount, int fileCount, boolean bumpScores, boolean coverageComplete) {
        String headSha = shaChar.repeat(40);
        List<FileData> files = new ArrayList<>(fileCount);
        for (int i = 0; i < fileCount; i++) {
            String key = String.format("00000000000000000000000000000000000000%02d:src/Module%02d.java", i, i);
            String path = String.format("src/Module%02d.java", i);
            int freq = 12 + (i % 15);
            long churn = 120L + (i * 6L);
            BigDecimal baseScore = BigDecimal.valueOf(20 + (i % 50)).movePointLeft(2);
            BigDecimal score = bumpScores && i < 5 ? baseScore.add(new BigDecimal("0.25")) : baseScore;
            List<Contribution> contribs = List.of(
                    new Contribution("dev-alpha", "Dev Alpha", churn / 2, 0L, Math.max(1, freq / 2)),
                    new Contribution("dev-beta", "Dev Beta", churn / 3, 0L, Math.max(1, freq / 3)),
                    new Contribution(
                            "dev-gamma",
                            "Dev Gamma",
                            churn - (churn / 2) - (churn / 3),
                            0L,
                            Math.max(1, freq - (freq / 2) - (freq / 3))));
            files.add(new FileData(
                    key,
                    "0".repeat(38) + String.format("%02d", i),
                    path,
                    path,
                    freq,
                    churn,
                    BigDecimal.valueOf(4 + (i % 8)),
                    BigDecimal.valueOf(2 + (i % 4)),
                    3,
                    80L + i,
                    score,
                    new BigDecimal("0.388889"),
                    null,
                    Map.of("commits", List.of(Map.of("sha", headSha))),
                    contribs));
        }
        List<PairData> pairs = new ArrayList<>();
        for (int i = 0; i + 1 < fileCount; i += 2) {
            FileData a = files.get(i);
            FileData b = files.get(i + 1);
            int shared = Math.min(a.frequency(), b.frequency());
            pairs.add(new PairData(
                    a.lineageKey(),
                    b.lineageKey(),
                    shared,
                    a.frequency(),
                    b.frequency(),
                    Map.of("commits", List.of(headSha))));
        }
        Map<String, Object> summary = Map.of(
                "coverageComplete", coverageComplete,
                "reachableCommits", commitCount,
                "selectedCommits", commitCount,
                "currentJavaFiles", fileCount,
                "hotspotPopulation", fileCount,
                "ambiguousFiles", coverageComplete ? 0 : 1,
                "noFunctionFiles", 0);
        return new SnapshotData(headSha, "metric-v1", commitCount, false, summary, files, pairs);
    }

    @Test
    void publishAlertAndComparisonScaleWithinNfr03Budget() throws Exception {
        int[][] specs = {
            {1000, 25},
            {5000, 40},
            {10000, 50}
        };
        List<Map<String, Object>> scaleRecords = new ArrayList<>();

        for (int[] spec : specs) {
            int commits = spec[0];
            int fileCount = spec[1];
            long repo = createRepo("scale-" + commits);
            alerts.create(repo, new AlertService.RuleRequest("HOTSPOT_INCREASE", new BigDecimal("0.15"), null, true, null));
            alerts.create(repo, new AlertService.RuleRequest("STRONG_COUPLING", new BigDecimal("0.50"), 5, true, null));

            SnapshotData baselineData = syntheticSnapshot("a", commits, fileCount, false, true);
            SnapshotData targetData = syntheticSnapshot("b", commits, fileCount, true, true);

            Claim c1 = claimAt(repo, BASE_END);
            long t0 = System.nanoTime();
            long s1 = publisher.publish(c1, baselineData);
            long t1 = System.nanoTime();
            assertThat(alerts.evaluateNext()).isTrue();
            long t2 = System.nanoTime();

            Claim c2 = claimAt(repo, TARGET_END);
            long t3 = System.nanoTime();
            long s2 = publisher.publish(c2, targetData);
            long t4 = System.nanoTime();
            assertThat(alerts.evaluateNext()).isTrue();
            long t5 = System.nanoTime();

            Map<String, Object> comp = queries.comparison(repo, s1, s2, 0, 100);
            long t6 = System.nanoTime();

            double publishBaselineMs = (t1 - t0) / 1_000_000.0;
            double evalBaselineMs = (t2 - t1) / 1_000_000.0;
            double publishTargetMs = (t4 - t3) / 1_000_000.0;
            double evalTargetMs = (t5 - t4) / 1_000_000.0;
            double compareQueryMs = (t6 - t5) / 1_000_000.0;
            double totalDbCycleMs = publishTargetMs + evalTargetMs + compareQueryMs;

            assertThat(comp.get("items")).asInstanceOf(org.assertj.core.api.InstanceOfAssertFactories.LIST).hasSize(fileCount);
            assertThat(totalDbCycleMs).isLessThan(10_000.0);

            int openAlerts = alerts.alerts(repo, "OPEN", 100, 0).size();
            assertThat(openAlerts).isGreaterThanOrEqualTo(5);

            Map<String, Object> record = new LinkedHashMap<>();
            record.put("scaleId", "scale_" + (commits / 1000) + "k");
            record.put("selectedCommits", commits);
            record.put("javaFiles", fileCount);
            record.put("contributionsRowsPerSnapshot", fileCount * 3);
            record.put("couplingPairsPerSnapshot", fileCount / 2);
            record.put("publishBaselineMs", Math.round(publishBaselineMs * 100.0) / 100.0);
            record.put("evalBaselineMs", Math.round(evalBaselineMs * 100.0) / 100.0);
            record.put("publishTargetMs", Math.round(publishTargetMs * 100.0) / 100.0);
            record.put("evalTargetMs", Math.round(evalTargetMs * 100.0) / 100.0);
            record.put("compareQueryMs", Math.round(compareQueryMs * 100.0) / 100.0);
            record.put("totalTargetDbOverheadMs", Math.round(totalDbCycleMs * 100.0) / 100.0);
            record.put("generatedAlerts", openAlerts);
            scaleRecords.add(record);
        }

        String evidenceDir = System.getProperty("week10.evidenceDir");
        if (evidenceDir != null && !evidenceDir.isBlank()) {
            Path outDir = Path.of(evidenceDir);
            Files.createDirectories(outDir);
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("schemaVersion", 1);
            report.put("kind", "NFR03_POSTGRES_PERSISTENCE_AND_ALERT_BENCHMARK");
            report.put("measuredAtUtc", Instant.now().toString());
            report.put("scales", scaleRecords);
            Files.writeString(
                    outDir.resolve("postgres-persistence-benchmark.json"),
                    JSON.writerWithDefaultPrettyPrinter().writeValueAsString(report) + "\n");
        }
    }

    @Test
    void hotspotIncreaseAlertRespectsExactBigDecimalBoundaryInPostgres() {
        long repo = createRepo("s4-boundary");
        alerts.create(repo, new AlertService.RuleRequest("HOTSPOT_INCREASE", new BigDecimal("0.15"), null, true, null));

        FileData f0 = new FileData(
                "1".repeat(40) + ":F08.java",
                "1".repeat(40),
                "F08.java",
                "F08.java",
                5,
                50L,
                new BigDecimal("4"),
                new BigDecimal("2"),
                2,
                40L,
                new BigDecimal("0.400000"),
                BigDecimal.ONE,
                null,
                Map.of("commits", List.of()),
                List.of(new Contribution("dev1", "Dev 1", 50L, 0L, 5)));
        FileData f1 = new FileData(
                f0.lineageKey(),
                f0.firstSeenSha(),
                f0.initialPath(),
                "F08.java",
                6,
                60L,
                new BigDecimal("5"),
                new BigDecimal("2.5"),
                2,
                45L,
                new BigDecimal("0.550000"),
                BigDecimal.ONE,
                null,
                Map.of("commits", List.of()),
                List.of(new Contribution("dev1", "Dev 1", 60L, 0L, 6)));
        FileData f2 = new FileData(
                f0.lineageKey(),
                f0.firstSeenSha(),
                f0.initialPath(),
                "F08.java",
                8,
                80L,
                new BigDecimal("7"),
                new BigDecimal("3.5"),
                2,
                55L,
                new BigDecimal("0.775000"),
                BigDecimal.ONE,
                null,
                Map.of("commits", List.of()),
                List.of(new Contribution("dev1", "Dev 1", 80L, 0L, 8)));

        Map<String, Object> summary = Map.of("coverageComplete", true, "currentJavaFiles", 1, "hotspotPopulation", 1);
        publisher.publish(claimAt(repo, BASE_END), new SnapshotData("1".repeat(40), "metric-v1", 5, false, summary, List.of(f0), List.of()));
        assertThat(alerts.evaluateNext()).isTrue();
        assertThat(alerts.alerts(repo, "OPEN", 10, 0)).isEmpty();

        // Step 1: 0.400000 -> 0.550000 (exact +0.150000 delta) must NOT trigger > 0.15 alert
        publisher.publish(claimAt(repo, TARGET_END), new SnapshotData("2".repeat(40), "metric-v1", 6, false, summary, List.of(f1), List.of()));
        assertThat(alerts.evaluateNext()).isTrue();
        assertThat(alerts.alerts(repo, "OPEN", 10, 0)).isEmpty();

        // Step 2: 0.550000 -> 0.775000 (+0.225000 > 0.15) MUST trigger 1 alert
        publisher.publish(claimAt(repo, THIRD_END), new SnapshotData("3".repeat(40), "metric-v1", 8, false, summary, List.of(f2), List.of()));
        assertThat(alerts.evaluateNext()).isTrue();
        assertThat(alerts.alerts(repo, "OPEN", 10, 0)).hasSize(1);
    }

    @Test
    void incompleteCoverageSnapshotBlocksComparisonAndAlertGenerationInPostgres() {
        long repo = createRepo("s5-blocker");
        alerts.create(repo, new AlertService.RuleRequest("HOTSPOT_INCREASE", new BigDecimal("0.15"), null, true, null));

        long s1 = publisher.publish(claimAt(repo, BASE_END), syntheticSnapshot("c", 20, 4, false, true));
        assertThat(alerts.evaluateNext()).isTrue();

        long s2 = publisher.publish(claimAt(repo, TARGET_END), syntheticSnapshot("d", 25, 4, true, false));
        assertThat(alerts.evaluateNext()).isTrue();

        // No HOTSPOT_INCREASE alert may be emitted when coverageComplete is false
        assertThat(alerts.alerts(repo, "OPEN", 10, 0)).isEmpty();
        String notes = db.queryForObject("SELECT evaluation_notes::text FROM analysis_snapshot WHERE id=?", String.class, s2);
        assertThat(notes).contains("INCOMPLETE_COVERAGE");

        // Snapshot comparison endpoint must also reject incomplete coverage with 409 CONFLICT
        assertThatThrownBy(() -> queries.comparison(repo, s1, s2, 0, 20))
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        ex -> assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
    }
}
