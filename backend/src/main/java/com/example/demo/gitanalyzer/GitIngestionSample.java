package com.example.demo.gitanalyzer;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Operator command reporting extraction straight from JGit: no Spring context, no API, no database. */
public final class GitIngestionSample {
    private static final String USAGE = "Usage: GitIngestionSample <https://github.com/owner/repo> <cache-dir> [window-days=180]";
    private final GitRepositoryClient client = new GitRepositoryClient();
    private final GitIngestion ingestion = new GitIngestion();

    public static void main(String[] args) throws Exception {
        if (args.length < 2 || args.length > 3) throw new IllegalArgumentException(USAGE);
        System.out.println(new GitIngestionSample().run(args[0], Paths.get(args[1]),
                args.length == 3 ? Integer.parseInt(args[2]) : 180));
    }

    /** Reuses an existing bare cache when present, otherwise clones it. */
    String run(String url, Path cache, int windowDays) throws Exception {
        if (windowDays < 1) throw new IllegalArgumentException(USAGE);
        Instant end = Instant.now();
        var pinned = Files.exists(cache) ? client.fetchPublic(url, cache, end.plusSeconds(600))
                : client.clonePublic(url, cache, end.plusSeconds(600));
        var result = ingestion.extract(pinned.directory(), pinned.headSha(),
                GitIngestion.Options.defaults(end.minusSeconds(86_400L * windowDays), end));
        return report(pinned.branch(), result);
    }

    static String report(String branch, GitIngestion.Result r) {
        var pathsByFileId = new HashMap<String, String>();
        r.currentFiles().forEach((path, identity) -> pathsByFileId.putIfAbsent(identity.id(), path));
        var churn = new TreeMap<String, long[]>();
        for (var fact : r.facts())
            churn.merge(fact.logicalFileId(), new long[] {count(fact.additions()), count(fact.deletions())},
                    (a, b) -> new long[] {a[0] + b[0], a[1] + b[1]});
        var top = churn.entrySet().stream()
                .sorted(Comparator.<Map.Entry<String, long[]>>comparingLong(e -> e.getValue()[0] + e.getValue()[1]).reversed()
                        .thenComparing(Map.Entry::getKey))
                .limit(10).toList();
        var out = new StringBuilder();
        out.append("head ").append(r.headSha()).append(" branch ").append(branch).append('\n');
        out.append("window ").append(r.start()).append(" -> ").append(r.end()).append('\n');
        out.append("reachableCommits ").append(r.reachableCommits()).append(" selectedCommits ").append(r.selectedCommits())
            .append(" facts ").append(r.facts().size()).append(" renameFacts ")
            .append(r.facts().stream().filter(f -> f.changeType().equals("RENAME")).count())
            .append(" currentJavaFiles ").append(r.currentFiles().size()).append(" ambiguousLineage ")
            .append(r.currentFiles().values().stream().filter(GitIngestion.FileIdentity::ambiguous).count()).append('\n');
        out.append("qualityFlags ").append(r.qualityFlags()).append('\n');
        out.append("topFilesByChurn (ties broken by logicalFileId)\n");
        for (var entry : top) {
            var id = entry.getKey();
            out.append("  +").append(entry.getValue()[0]).append(" -").append(entry.getValue()[1]).append("  ")
                .append(pathsByFileId.getOrDefault(id, "(no longer present)")).append("   origin ")
                .append(id, 41, id.length()).append(" @").append(id, 0, 12).append('\n');
        }
        return out.toString().stripTrailing();
    }

    private static long count(Integer value) { return value == null ? 0 : value; }
}
