package com.example.demo.gitanalyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.TimeZone;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;
import static org.assertj.core.api.Assertions.*;

class HistoricalSnapshotExportTests {
    @TempDir Path temp;
    private static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    private String commit(Git git, int seconds, String source) throws Exception {
        Files.writeString(temp.resolve("A.java"), source);
        git.add().addFilepattern("A.java").call();
        var identity = new PersonIdent("Fixture", "fixture@example.test", Date.from(BASE.plusSeconds(seconds)), TimeZone.getTimeZone("UTC"));
        return git.commit().setMessage("controlled fixture").setAuthor(identity).setCommitter(identity).call().name();
    }

    @Test void historicalExportMeasuresOldTreeEvenWhenHeadHasFutureComplexity() throws Exception {
        try (var git = Git.init().setDirectory(temp.toFile()).call()) {
            String old = commit(git, 1, "class A { int f() { return 1; } }\n");
            commit(git, 30, "class A { int f(int x) { if(x>0) return 1; if(x<0) return 2; return 0; } }\n");
            Path output = temp.resolve("result.json");
            HistoricalSnapshotExport.main(new String[]{temp.toString(), old, BASE.toString(), BASE.plusSeconds(10).toString(), output.toString()});
            var result = JsonMapper.builder().build().readTree(Files.readString(output));
            assertThat(result.path("headSha").asText()).isEqualTo(old);
            assertThat(result.path("files").get(0).path("maxCcn").asInt()).isEqualTo(1);
            assertThat(result.path("commitCount").asInt()).isEqualTo(1);
            assertThat(Files.readString(output)).doesNotContain("fixture@example.test");
        }
    }

    @Test void futureAncestorIsRejectedEvenWhenHeadTimestampIsOld() throws Exception {
        try (var git = Git.init().setDirectory(temp.toFile()).call()) {
            commit(git, 30, "class A {}\n");
            String head = commit(git, 1, "class A { int x; }\n");
            assertThatThrownBy(() -> HistoricalSnapshotExport.validateHistoricalHead(temp, head, BASE.plusSeconds(10), 100))
                    .hasMessageContaining("FUTURE_COMMIT_IN_HISTORICAL_TREE");
        }
    }

    @Test void controlledHistoryHasCompleteCoverageAndExplainsHotspotIncrease() throws Exception {
        try (var git = Git.init().setDirectory(temp.toFile()).call()) {
            Files.writeString(temp.resolve("B.java"), "class B { int f(int x) { if(x>0) return 1; if(x<0) return 2; return 0; } }\n");
            git.add().addFilepattern("B.java").call();
            String old = commit(git, 1, "class A { int f() { return 1; } }\n");
            String recent = commit(git, 20, "class A { int f(int x) { if(x>0) return 1; if(x<0) return 2; if(x==0) return 3; return 0; } }\n");
            var json = JsonMapper.builder().build();
            Path before = temp.resolve("before.json"), after = temp.resolve("after.json");
            HistoricalSnapshotExport.main(new String[]{temp.toString(), old, BASE.minusSeconds(95).toString(), BASE.plusSeconds(5).toString(), before.toString()});
            HistoricalSnapshotExport.main(new String[]{temp.toString(), recent, BASE.minusSeconds(75).toString(), BASE.plusSeconds(25).toString(), after.toString()});
            var a = json.readTree(Files.readString(before));
            var b = json.readTree(Files.readString(after));
            assertThat(a.path("summary").path("coverageComplete").asBoolean()).isTrue();
            assertThat(b.path("summary").path("coverageComplete").asBoolean()).isTrue();
            var oldFile = a.path("files").get(0);
            var newFile = b.path("files").get(0);
            assertThat(oldFile.path("path").asText()).isEqualTo("A.java");
            assertThat(newFile.path("lineageKey").asText()).isEqualTo(oldFile.path("lineageKey").asText());
            assertThat(oldFile.path("hotspot").asDouble()).isZero();
            assertThat(newFile.path("hotspot").asDouble()).isEqualTo(1.0);
            assertThat(newFile.path("frequency").asInt()).isEqualTo(2);
            assertThat(newFile.path("maxCcn").asInt()).isEqualTo(4);
            assertThat(b.path("facts").toString()).contains(recent);
        }
    }

    @Test void cutoffIsExclusiveAndResourceLimitIsEnforced() throws Exception {
        try (var git = Git.init().setDirectory(temp.toFile()).call()) {
            commit(git, 1, "class A {}\n");
            String head = commit(git, 2, "class A { int x; }\n");
            assertThatThrownBy(() -> HistoricalSnapshotExport.validateHistoricalHead(temp, head, BASE.plusSeconds(2), 100))
                    .hasMessageContaining("FUTURE_COMMIT_IN_HISTORICAL_TREE");
            assertThatThrownBy(() -> HistoricalSnapshotExport.validateHistoricalHead(temp, head, BASE.plusSeconds(10), 1))
                    .hasMessageContaining("RESOURCE_LIMIT_COMMITS");
            assertThatThrownBy(() -> HistoricalSnapshotExport.validateHistoricalHead(temp, "HEAD", BASE.plusSeconds(10), 100))
                    .hasMessageContaining("full lowercase SHA");
        }
    }
}
