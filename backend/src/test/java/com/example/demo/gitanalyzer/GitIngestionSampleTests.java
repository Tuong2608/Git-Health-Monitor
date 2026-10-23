package com.example.demo.gitanalyzer;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

class GitIngestionSampleTests {
    @TempDir Path temp;
    static final Instant BASE = Instant.parse("2026-01-01T00:00:00Z");

    private void write(Git git, String path, String text) throws Exception {
        Path target = git.getRepository().getWorkTree().toPath().resolve(path);
        Files.createDirectories(target.getParent());
        Files.writeString(target, text);
    }

    private RevCommit commit(Git git, int seconds) throws Exception {
        git.add().addFilepattern(".").call();
        var when = Date.from(BASE.plusSeconds(seconds));
        return git.commit().setMessage("fixture " + seconds)
                .setAuthor(new PersonIdent("Author", "user@test", when, TimeZone.getTimeZone("UTC")))
                .setCommitter(new PersonIdent("Committer", "committer@test", when, TimeZone.getTimeZone("UTC"))).call();
    }

    @Test void reportSummarisesCommitsRenamesAndChurnRanking() throws Exception {
        try (var git = Git.init().setDirectory(temp.resolve("fixture").toFile()).setInitialBranch("main").call()) {
            write(git, "src/A.java", "class A {}\n");
            write(git, "src/Big.java", "class Big {\n int a;\n}\n");
            commit(git, 1);
            write(git, "src/Big.java", "class Big {\n int a;\n int b;\n int c;\n}\n");
            Files.move(git.getRepository().getWorkTree().toPath().resolve("src/A.java"),
                    git.getRepository().getWorkTree().toPath().resolve("src/B.java"));
            var head = commit(git, 2);
            var result = new GitIngestion().extract(git.getRepository().getDirectory().toPath(), head.name(),
                    GitIngestion.Options.defaults(BASE, BASE.plusSeconds(3)));
            String report = GitIngestionSample.report("refs/heads/main", result);
            assertThat(report).contains("branch refs/heads/main").contains("reachableCommits 2").contains("selectedCommits 2")
                .contains("renameFacts 1").contains("currentJavaFiles 2").contains("ambiguousLineage 0")
                .contains("src/Big.java").contains("src/B.java   origin src/A.java").doesNotContain("null");
            assertThat(report.indexOf("src/Big.java")).isLessThan(report.indexOf("src/B.java"));
            assertThat(GitIngestionSample.report("refs/heads/main", result)).isEqualTo(report);
        }
    }

    @Test void rejectsNonPositiveWindowBeforeTouchingTheCache() {
        Path cache = temp.resolve("never-created");
        assertThatThrownBy(() -> new GitIngestionSample().run("https://github.com/tuong2608/demo", cache, 0))
            .isInstanceOf(IllegalArgumentException.class);
        assertThat(cache).doesNotExist();
    }
}
