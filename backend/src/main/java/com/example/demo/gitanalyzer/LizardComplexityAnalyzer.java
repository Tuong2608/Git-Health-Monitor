package com.example.demo.gitanalyzer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Measures Java file cyclomatic complexity (max CCN, avg CCN, function count, NLOC)
 * at a pinned commit SHA by reading blobs from the JGit object database and invoking
 * Lizard in structured XML mode (--xml) without checking out the repository worktree.
 */
public final class LizardComplexityAnalyzer {

    public interface Runner {
        String runXml(Path tempDirectory, Instant deadline) throws IOException;
    }

    public record FileComplexity(
            Integer functions,
            Long nloc,
            BigDecimal maxCcn,
            BigDecimal avgCcn,
            String unavailableReason,
            Set<String> qualityFlags) {
        public FileComplexity {
            qualityFlags = Set.copyOf(qualityFlags);
        }

        public static FileComplexity unavailable(String reason) {
            return new FileComplexity(null, null, null, null, reason, Set.of(reason));
        }

        public static FileComplexity noFunctions(long nloc) {
            return new FileComplexity(0, nloc, null, null, "NO_FUNCTIONS", Set.of("NO_FUNCTIONS"));
        }
    }

    private final Runner runner;

    public LizardComplexityAnalyzer() {
        this(defaultCommandRunner(List.of("python", "-m", "lizard", "--xml")));
    }

    public LizardComplexityAnalyzer(Runner runner) {
        this.runner = Objects.requireNonNull(runner);
    }

    public static Runner defaultCommandRunner(List<String> commandPrefix) {
        List<String> prefix = List.copyOf(commandPrefix);
        return (tempDir, deadline) -> {
            if (Thread.currentThread().isInterrupted()) {
                throw new GitIngestion.ExtractionException("CANCELLED");
            }
            long remainingMillis = Duration.between(Instant.now(), deadline).toMillis();
            if (remainingMillis <= 0) {
                throw new GitIngestion.ExtractionException("DEADLINE_EXCEEDED");
            }
            Path outFile = Files.createTempFile("ghm-lizard-out-", ".xml");
            try {
                List<String> cmd = new ArrayList<>(prefix);
                cmd.add(tempDir.toAbsolutePath().toString());
                Process process = new ProcessBuilder(cmd)
                        .redirectOutput(outFile.toFile())
                        .redirectError(ProcessBuilder.Redirect.DISCARD)
                        .start();
                try {
                    boolean finished = process.waitFor(Math.max(1L, remainingMillis), TimeUnit.MILLISECONDS);
                    if (!finished) {
                        process.destroyForcibly();
                        throw new GitIngestion.ExtractionException("DEADLINE_EXCEEDED");
                    }
                    String xml = Files.readString(outFile, StandardCharsets.UTF_8);
                    int start = xml.indexOf("<?xml");
                    if (start < 0) start = xml.indexOf("<cppncss");
                    if (start < 0) {
                        throw new GitIngestion.ExtractionException("LIZARD_FAILED");
                    }
                    return xml.substring(start);
                } catch (InterruptedException e) {
                    process.destroyForcibly();
                    Thread.currentThread().interrupt();
                    throw new GitIngestion.ExtractionException("CANCELLED");
                }
            } finally {
                try {
                    Files.deleteIfExists(outFile);
                } catch (IOException ignored) {
                }
            }
        };
    }

    public Map<String, FileComplexity> analyzeAtHead(
            Path repositoryPath,
            String headSha,
            Collection<String> eligiblePaths,
            int maxBlobBytes,
            Instant deadline) throws IOException {
        if (headSha == null || !headSha.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException("Pin a full commit SHA");
        }
        if (eligiblePaths.isEmpty()) {
            return Map.of();
        }
        Path stagingDir = Files.createTempDirectory("ghm-lizard-");
        try (var git = Git.open(repositoryPath.toFile());
             var walk = new RevWalk(git.getRepository());
             var treeWalk = new TreeWalk(git.getRepository())) {
            Repository repo = git.getRepository();
            RevCommit head = walk.parseCommit(ObjectId.fromString(headSha));
            treeWalk.addTree(head.getTree());
            treeWalk.setRecursive(true);

            Set<String> targetPaths = new HashSet<>(eligiblePaths);
            Map<String, ObjectId> blobs = new HashMap<>();
            Map<String, FileMode> modes = new HashMap<>();
            while (treeWalk.next()) {
                checkDeadline(deadline);
                String path = treeWalk.getPathString();
                if (targetPaths.contains(path)) {
                    blobs.put(path, treeWalk.getObjectId(0));
                    modes.put(path, treeWalk.getFileMode(0));
                }
            }

            Map<String, FileComplexity> results = new TreeMap<>();
            // Deduplicate identical blobs by ObjectId so Lizard never skips duplicate files in a repo
            Map<ObjectId, String> blobToStagedName = new LinkedHashMap<>();
            Map<String, String> repoPathToStagedName = new LinkedHashMap<>();
            int index = 0;

            for (String path : new TreeSet<>(eligiblePaths)) {
                checkDeadline(deadline);
                FileMode mode = modes.get(path);
                ObjectId blobId = blobs.get(path);
                if (mode == null || blobId == null) {
                    results.put(path, FileComplexity.unavailable("MISSING_AT_HEAD"));
                    continue;
                }
                if (!mode.equals(FileMode.REGULAR_FILE) && !mode.equals(FileMode.EXECUTABLE_FILE)) {
                    results.put(path, FileComplexity.unavailable("NON_REGULAR_FILE"));
                    continue;
                }
                String existingStaged = blobToStagedName.get(blobId);
                if (existingStaged != null) {
                    repoPathToStagedName.put(path, existingStaged);
                    continue;
                }
                ObjectLoader loader = repo.open(blobId, Constants.OBJ_BLOB);
                if (loader.getSize() > maxBlobBytes) {
                    throw new GitIngestion.ExtractionException("RESOURCE_LIMIT_BLOB");
                }
                byte[] bytes = loader.getCachedBytes(maxBlobBytes);
                if (RawText.isBinary(bytes)) {
                    results.put(path, FileComplexity.unavailable("BINARY"));
                    continue;
                }
                String stagedName = "f" + (index++) + ".java";
                Files.write(stagingDir.resolve(stagedName), bytes);
                blobToStagedName.put(blobId, stagedName);
                repoPathToStagedName.put(path, stagedName);
            }

            if (!blobToStagedName.isEmpty()) {
                Set<String> stagedNames = new LinkedHashSet<>(blobToStagedName.values());
                String xml = runner.runXml(stagingDir, deadline);
                Map<String, FileComplexity> parsed = parseLizardXml(xml, stagedNames);
                repoPathToStagedName.forEach((repoPath, stagedName) ->
                        results.put(repoPath, parsed.getOrDefault(stagedName, FileComplexity.unavailable("CCN_PARSE_ERROR"))));
            }
            return Collections.unmodifiableMap(results);
        } finally {
            deleteRecursively(stagingDir);
        }
    }

    static Map<String, FileComplexity> parseLizardXml(String xml, Set<String> stagedFileNames) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            Document doc = factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

            Map<String, List<Integer>> functionCcnsByStagedFile = new HashMap<>();
            Map<String, long[]> fileMeasuresByStagedFile = new HashMap<>();

            NodeList measures = doc.getElementsByTagName("measure");
            for (int i = 0; i < measures.getLength(); i++) {
                Element measure = (Element) measures.item(i);
                String type = measure.getAttribute("type");
                NodeList items = measure.getElementsByTagName("item");
                if ("Function".equals(type)) {
                    for (int j = 0; j < items.getLength(); j++) {
                        Element item = (Element) items.item(j);
                        String name = item.getAttribute("name");
                        String staged = extractStagedFileNameFromFunctionItem(name, stagedFileNames);
                        List<String> values = valuesOf(item);
                        if (staged != null && values.size() >= 3) {
                            int ccn = Integer.parseInt(values.get(2));
                            functionCcnsByStagedFile.computeIfAbsent(staged, k -> new ArrayList<>()).add(ccn);
                        }
                    }
                } else if ("File".equals(type)) {
                    for (int j = 0; j < items.getLength(); j++) {
                        Element item = (Element) items.item(j);
                        String name = item.getAttribute("name");
                        String staged = extractStagedFileNameFromFileItem(name, stagedFileNames);
                        List<String> values = valuesOf(item);
                        if (staged != null && values.size() >= 4) {
                            long nloc = Long.parseLong(values.get(1));
                            long ccnSum = Long.parseLong(values.get(2));
                            long fnCount = Long.parseLong(values.get(3));
                            fileMeasuresByStagedFile.put(staged, new long[] {nloc, ccnSum, fnCount});
                        }
                    }
                }
            }

            Map<String, FileComplexity> out = new HashMap<>();
            for (String staged : stagedFileNames) {
                long[] fileVals = fileMeasuresByStagedFile.get(staged);
                if (fileVals == null) {
                    out.put(staged, FileComplexity.unavailable("CCN_PARSE_ERROR"));
                    continue;
                }
                long nloc = fileVals[0];
                long ccnSumFromFile = fileVals[1];
                int fnCount = Math.toIntExact(fileVals[2]);
                List<Integer> fnCcns = functionCcnsByStagedFile.getOrDefault(staged, List.of());
                if (fnCount < 0 || nloc < 0) {
                    out.put(staged, FileComplexity.unavailable("CCN_PARSE_ERROR"));
                } else if (fnCount == 0 || fnCcns.isEmpty()) {
                    out.put(staged, FileComplexity.noFunctions(nloc));
                } else {
                    int maxCcn = fnCcns.stream().mapToInt(Integer::intValue).max().orElse(0);
                    long sumCcn = fnCcns.stream().mapToLong(Integer::longValue).sum();
                    if (maxCcn <= 0) {
                        out.put(staged, FileComplexity.unavailable("CCN_PARSE_ERROR"));
                    } else {
                        BigDecimal avg = BigDecimal.valueOf(sumCcn > 0 ? sumCcn : ccnSumFromFile)
                                .divide(BigDecimal.valueOf(fnCcns.size()), MathContext.DECIMAL64);
                        out.put(staged, new FileComplexity(
                                fnCcns.size(), nloc, BigDecimal.valueOf(maxCcn), avg, null, Set.of()));
                    }
                }
            }
            return out;
        } catch (Exception e) {
            throw new GitIngestion.ExtractionException("LIZARD_XML_INVALID");
        }
    }

    private static String extractStagedFileNameFromFunctionItem(String attrName, Set<String> stagedFileNames) {
        int atIdx = attrName.lastIndexOf(" at ");
        String location = atIdx >= 0 ? attrName.substring(atIdx + 4) : attrName;
        int colonIdx = location.lastIndexOf(':');
        String filePart = colonIdx > 0 ? location.substring(0, colonIdx) : location;
        return extractStagedFileNameFromFileItem(filePart, stagedFileNames);
    }

    private static String extractStagedFileNameFromFileItem(String pathStr, Set<String> stagedFileNames) {
        String normalized = pathStr.replace('\\', '/');
        int slash = normalized.lastIndexOf('/');
        String base = slash >= 0 ? normalized.substring(slash + 1) : normalized;
        return stagedFileNames.contains(base) ? base : null;
    }

    private static List<String> valuesOf(Element item) {
        NodeList nodes = item.getElementsByTagName("value");
        List<String> values = new ArrayList<>(nodes.getLength());
        for (int i = 0; i < nodes.getLength(); i++) {
            values.add(nodes.item(i).getTextContent().strip());
        }
        return values;
    }

    private static void checkDeadline(Instant deadline) throws GitIngestion.ExtractionException {
        if (Thread.currentThread().isInterrupted()) {
            throw new GitIngestion.ExtractionException("CANCELLED");
        }
        if (!Instant.now().isBefore(deadline)) {
            throw new GitIngestion.ExtractionException("DEADLINE_EXCEEDED");
        }
    }

    private static void deleteRecursively(Path root) {
        try (var stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }
}
