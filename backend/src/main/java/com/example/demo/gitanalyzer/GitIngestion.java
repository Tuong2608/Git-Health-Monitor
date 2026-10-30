package com.example.demo.gitanalyzer;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.treewalk.*;
import org.eclipse.jgit.util.io.DisabledOutputStream;

/** Extraction only: does not execute checked-in code or publish a snapshot. */
public final class GitIngestion {
    public record Options(Instant start, Instant end, int maxCommits, int maxTreeEntries,
            int maxBlobBytes, int maxFacts, Instant deadline, List<String> excludedPrefixes,
            Map<String,String> authorAliases) {
        public Options {
            if (start == null || end == null || !start.isBefore(end) || deadline == null
                    || maxCommits < 1 || maxTreeEntries < 1 || maxBlobBytes < 1 || maxFacts < 1)
                throw new IllegalArgumentException("Invalid extraction bounds");
            excludedPrefixes = List.copyOf(excludedPrefixes);
            authorAliases = Map.copyOf(authorAliases);
        }
        public static Options defaults(Instant start, Instant end) {
            return new Options(start,end,10000,100000,2*1024*1024,500000,
                Instant.now().plusSeconds(300),List.of("vendor/","generated/","target/","build/"),Map.of());
        }
    }
    public record Fact(String commitSha, String parentSha, Instant committerTime, String authorKey,
            String logicalFileId, String oldPath, String newPath, String changeType,
            Integer additions, Integer deletions, Set<String> qualityFlags) {
        public Fact { qualityFlags = Set.copyOf(qualityFlags); }
    }
    public record FileIdentity(String id, boolean ambiguous) {}
    public record Result(String headSha, Instant start, Instant end, int reachableCommits,
            int selectedCommits, List<Fact> facts, Map<String,FileIdentity> currentFiles,
            Set<String> qualityFlags) {
        public Result { facts=List.copyOf(facts);currentFiles=Collections.unmodifiableMap(new TreeMap<>(currentFiles));qualityFlags=Set.copyOf(qualityFlags); }
    }
    private record Entry(ObjectId blob, FileMode mode) {}
    public static final class ExtractionException extends IOException {
        public ExtractionException(String code) { super(code); }
    }
    private static void check(Options o) throws ExtractionException {
        if (Thread.currentThread().isInterrupted()) throw new ExtractionException("CANCELLED");
        if (!Instant.now().isBefore(o.deadline())) throw new ExtractionException("DEADLINE_EXCEEDED");
    }
    private static String id(String sha,String path) { return sha+":"+path; }
    private static boolean eligible(String path, Options o) {
        return path.endsWith(".java") && o.excludedPrefixes().stream().noneMatch(path::startsWith);
    }
    private static String author(RevCommit c, Options o) {
        String email=c.getAuthorIdent().getEmailAddress().strip().toLowerCase(Locale.ROOT);
        return email.isBlank()?"unknown:"+c.name():o.authorAliases().getOrDefault(email,email);
    }
    private static Map<String,Entry> tree(Repository repo, RevCommit c, Options o) throws IOException {
        var result=new TreeMap<String,Entry>();
        try (var walk=new TreeWalk(repo)) {
            walk.addTree(c.getTree());walk.setRecursive(true);
            while(walk.next()) {
                check(o);
                if(result.size()>=o.maxTreeEntries())throw new ExtractionException("RESOURCE_LIMIT_TREE");
                result.put(walk.getPathString(),new Entry(walk.getObjectId(0),walk.getFileMode(0)));
            }
        }
        return result;
    }
    /** Accepts only a pinned full SHA, never a moving branch name. */
    public Result extract(Path repositoryPath, String headSha, Options options) throws IOException {
        if(headSha==null || !headSha.matches("[0-9a-fA-F]{40}"))throw new IllegalArgumentException("Pin a full commit SHA");
        try(var git=Git.open(repositoryPath.toFile());var walk=new RevWalk(git.getRepository());
            var diff=new DiffFormatter(DisabledOutputStream.INSTANCE)) {
            Repository repo=git.getRepository();
            if(!repo.getObjectDatabase().getShallowCommits().isEmpty())throw new ExtractionException("SHALLOW_HISTORY");
            RevCommit head=walk.parseCommit(ObjectId.fromString(headSha));
            diff.setRepository(repo);diff.setDetectRenames(true);
            diff.getRenameDetector().setRenameScore(60);
            diff.getRenameDetector().setRenameLimit(1000);
            diff.setBinaryFileThreshold(options.maxBlobBytes());
            walk.markStart(head);
            var commits=new HashMap<String,RevCommit>();
            for(var c:walk) {
                check(options);
                if(commits.size()>=options.maxCommits())throw new ExtractionException("RESOURCE_LIMIT_COMMITS");
                commits.put(c.name(),c);
            }
            // Kahn ordering: parents first; equal-ready nodes ordered by SHA, not timestamp.
            var children=new HashMap<String,List<String>>();var pending=new HashMap<String,Integer>();
            for(var c:commits.values()) {
                pending.put(c.name(),c.getParentCount());
                for(var p:c.getParents())children.computeIfAbsent(p.name(),k->new ArrayList<>()).add(c.name());
            }
            var ready=new PriorityQueue<String>();pending.forEach((sha,n)->{if(n==0)ready.add(sha);});
            var states=new HashMap<String,Map<String,FileIdentity>>();
            var remaining=new HashMap<String,Integer>();children.forEach((sha,list)->remaining.put(sha,list.size()));
            var facts=new ArrayList<Fact>();var flags=new TreeSet<String>();int selected=0;
            while(!ready.isEmpty()) {
                check(options);String sha=ready.remove();RevCommit c=commits.get(sha);
                Map<String,Entry> entries=tree(repo,c,options);
                var state=new TreeMap<String,FileIdentity>();
                if(c.getParentCount()>1) {
                    // Only a lineage supported by every parent is unambiguous at a merge.
                    for(String path:entries.keySet()) {
                        var candidates=new HashSet<FileIdentity>();boolean missing=false;
                        for(var p:c.getParents()) {var candidate=states.get(p.name()).get(path);if(candidate==null)missing=true;else candidates.add(candidate);}
                        if(!missing && candidates.size()==1)state.put(path,candidates.iterator().next());
                        else {state.put(path,new FileIdentity(id(sha,path),true));flags.add("LINEAGE_AMBIGUOUS");}
                    }
                } else {
                    RevCommit parent=c.getParentCount()==0?null:commits.get(c.getParent(0).name());
                    Map<String,FileIdentity> before=parent==null?Map.of():states.get(parent.name());
                    state.putAll(before);
                    var changes=diff.scan(parent==null?null:parent.getTree(),c.getTree());
                    if(diff.getRenameDetector().isOverRenameLimit())throw new ExtractionException("RESOURCE_LIMIT_RENAMES");
                    boolean inWindow=!c.getCommitterIdent().getWhenAsInstant().isBefore(options.start())
                            && c.getCommitterIdent().getWhenAsInstant().isBefore(options.end());
                    if(inWindow)selected++;
                    // Remove old paths first so a rename into a deleted path is deterministic.
                    for(var change:changes)if(change.getChangeType()==DiffEntry.ChangeType.DELETE || change.getChangeType()==DiffEntry.ChangeType.RENAME)state.remove(change.getOldPath());
                    for(var change:changes) {
                        check(options);String old=change.getOldPath(),next=change.getNewPath();
                        var type=change.getChangeType();
                        FileIdentity identity=(type==DiffEntry.ChangeType.ADD || type==DiffEntry.ChangeType.COPY)
                            ?new FileIdentity(id(sha,next),false):before.get(old);
                        if(identity==null){identity=new FileIdentity(id(sha,next),true);flags.add("LINEAGE_AMBIGUOUS");}
                        if(type!=DiffEntry.ChangeType.DELETE)state.put(next,identity);
                        if(!inWindow || !(eligible(old,options)||eligible(next,options)))continue;
                        var quality=new TreeSet<String>();if(identity.ambiguous())quality.add("LINEAGE_AMBIGUOUS");
                        if(c.getAuthorIdent().getEmailAddress().isBlank())quality.add("AUTHOR_MISSING");
                        Integer additions=null,deletions=null;
                        boolean regular=(change.getOldMode().equals(FileMode.MISSING)||change.getOldMode().equals(FileMode.REGULAR_FILE)||change.getOldMode().equals(FileMode.EXECUTABLE_FILE))
                            &&(change.getNewMode().equals(FileMode.MISSING)||change.getNewMode().equals(FileMode.REGULAR_FILE)||change.getNewMode().equals(FileMode.EXECUTABLE_FILE));
                        if(!regular)quality.add("NON_REGULAR_FILE");
                        else {
                            for(var blob:List.of(change.getOldId(),change.getNewId()))if(!blob.toObjectId().equals(ObjectId.zeroId()) && repo.open(blob.toObjectId(),Constants.OBJ_BLOB).getSize()>options.maxBlobBytes())throw new ExtractionException("RESOURCE_LIMIT_BLOB");
                            var header=diff.toFileHeader(change);
                            if(header.getPatchType()!=org.eclipse.jgit.patch.FileHeader.PatchType.UNIFIED)quality.add("BINARY");
                            else {additions=0;deletions=0;for(var edit:header.toEditList()){additions+=edit.getLengthB();deletions+=edit.getLengthA();}}
                        }
                        if(facts.size()>=options.maxFacts())throw new ExtractionException("RESOURCE_LIMIT_FACTS");
                        facts.add(new Fact(sha,parent==null?null:parent.name(),c.getCommitterIdent().getWhenAsInstant(),author(c,options),identity.id(),
                            old.equals(DiffEntry.DEV_NULL)?null:old,next.equals(DiffEntry.DEV_NULL)?null:next,type.name(),additions,deletions,quality));
                        flags.addAll(quality);
                    }
                }
                states.put(sha,state);
                for(var p:c.getParents())if(remaining.compute(p.name(),(k,v)->v-1)==0)states.remove(p.name());
                for(String child:children.getOrDefault(sha,List.of()))if(pending.compute(child,(k,v)->v-1)==0)ready.add(child);
            }
            var current=new TreeMap<String,FileIdentity>();
            states.get(head.name()).forEach((path,identity)->{if(eligible(path,options))current.put(path,identity);});
            return new Result(head.name(),options.start(),options.end(),commits.size(),selected,facts,current,flags);
        }
    }
}
