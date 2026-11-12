package com.example.demo.gitanalyzer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevWalk;
import tools.jackson.databind.json.JsonMapper;

/** Research CLI: analyze a frozen historical tree, without a database or remote checkout. */
public final class HistoricalSnapshotExport {
    private HistoricalSnapshotExport() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 5) throw new IllegalArgumentException(
                "Usage: HistoricalSnapshotExport <git-dir> <full-sha> <start> <end-exclusive> <output.json>");
        Path repository = Path.of(args[0]);
        Instant start = Instant.parse(args[2]), end = Instant.parse(args[3]);
        var options = GitIngestion.Options.defaults(start, end);
        validateHistoricalHead(repository, args[1], end, options.maxCommits());
        var extracted = new GitIngestion().extract(repository, args[1], options);
        var complexities = new LizardComplexityAnalyzer().analyzeAtHead(repository, args[1],
                extracted.currentFiles().keySet(), options.maxBlobBytes(), options.deadline());
        var snapshot = new RepositoryMetricCalculator().calculate(extracted, complexities,
                RepositoryMetricCalculator.Policy.defaults());
        var report = new LinkedHashMap<String, Object>();
        report.put("schemaVersion", 1);
        try (var git = Git.open(repository.toFile())) {
            report.put("repositoryUrl", git.getRepository().getConfig().getString("remote", "origin", "url"));
        }
        report.put("headSha", snapshot.headSha());
        report.put("windowStart", start.toString());
        report.put("windowEnd", end.toString());
        report.put("config", new GitAnalysisPipeline(repository).effectiveConfig());
        report.put("metricVersion", snapshot.metricVersion());
        report.put("reachableCommits", extracted.reachableCommits());
        report.put("commitCount", snapshot.commitCount());
        report.put("truncated", snapshot.truncated());
        report.put("summary", snapshot.summary());
        var files = new ArrayList<Map<String, Object>>();
        for (var f : snapshot.files()) {
            var row = new LinkedHashMap<String, Object>();
            row.put("lineageKey", f.lineageKey());
            row.put("path", f.path());
            row.put("frequency", f.frequency());
            row.put("maxCcn", f.maxCcn());
            row.put("hotspot", f.hotspot());
            row.put("unavailableReason", f.unavailableReason());
            files.add(row);
        }
        files.sort(java.util.Comparator.comparing(f -> (String) f.get("lineageKey")));
        report.put("files", files);
        // Evidence intentionally omits contributor identities, source text and commit messages.
        report.put("facts", extracted.facts().stream().map(f -> {
            var row = new LinkedHashMap<String, Object>();
            row.put("commitSha", f.commitSha());
            row.put("committerTime", f.committerTime().toString());
            row.put("lineageKey", f.logicalFileId());
            row.put("oldPath", f.oldPath());
            row.put("newPath", f.newPath());
            row.put("changeType", f.changeType());
            row.put("qualityFlags", f.qualityFlags().stream().sorted().toList());
            return row;
        }).toList());
        Files.writeString(Path.of(args[4]), JsonMapper.builder().build()
                .writerWithDefaultPrettyPrinter().writeValueAsString(report) + "\n");
    }

    /** Reject future-dated ancestors too: checking only HEAD is insufficient for DAG/clock skew. */
    static void validateHistoricalHead(Path repository, String sha, Instant end, int limit) throws Exception {
        if (sha == null || !sha.matches("[0-9a-f]{40}"))
            throw new IllegalArgumentException("Pin a full lowercase SHA");
        try (var git = Git.open(repository.toFile()); var walk = new RevWalk(git.getRepository())) {
            if (!git.getRepository().getObjectDatabase().getShallowCommits().isEmpty())
                throw new IllegalArgumentException("SHALLOW_HISTORY");
            walk.markStart(walk.parseCommit(ObjectId.fromString(sha)));
            int count = 0;
            for (var commit : walk) {
                if (++count > limit) throw new IllegalArgumentException("RESOURCE_LIMIT_COMMITS");
                if (!commit.getCommitterIdent().getWhenAsInstant().isBefore(end))
                    throw new IllegalArgumentException("FUTURE_COMMIT_IN_HISTORICAL_TREE: " + commit.name());
            }
        }
    }
}
