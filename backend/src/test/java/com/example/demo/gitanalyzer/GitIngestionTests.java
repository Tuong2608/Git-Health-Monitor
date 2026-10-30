package com.example.demo.gitanalyzer;

import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.assertj.core.api.Assertions.*;

class GitIngestionTests {
    @TempDir Path temp;
    static final Instant BASE=Instant.parse("2026-01-01T00:00:00Z");
    final GitIngestion ingestion=new GitIngestion();
    Git init(String name)throws Exception{return Git.init().setDirectory(temp.resolve(name).toFile()).setInitialBranch("main").call();}
    void write(Git git,String path,String text)throws Exception{
        Path p=git.getRepository().getWorkTree().toPath().resolve(path);Files.createDirectories(p.getParent());Files.writeString(p,text);
    }
    RevCommit commit(Git git,int seconds,String author)throws Exception {
        git.add().addFilepattern(".").call();git.add().setUpdate(true).addFilepattern(".").call();
        return git.commit().setMessage("fixture "+seconds).setAuthor(new PersonIdent("Author",author,Date.from(BASE.plusSeconds(seconds)),TimeZone.getTimeZone("UTC")))
            .setCommitter(new PersonIdent("Committer","committer@test",Date.from(BASE.plusSeconds(seconds)),TimeZone.getTimeZone("UTC"))).call();
    }
    GitIngestion.Options options(int start,int end){return GitIngestion.Options.defaults(BASE.plusSeconds(start),BASE.plusSeconds(end));}
    GitIngestion.Result extract(Git g,RevCommit head,int start,int end)throws Exception{return ingestion.extract(g.getRepository().getDirectory().toPath(),head.name(),options(start,end));}
    @Test void rootModifyRenameDeleteReaddAndCopyHaveCorrectIdentityAndChurn()throws Exception{
        try(var g=init("linear")) {
            write(g,"A.java","class A {\n int n;\n}\n");var root=commit(g,1," USER@Test ");
            write(g,"A.java","class A {\n int n;\n int m;\n}\n");commit(g,2,"user@test");
            Files.move(g.getRepository().getWorkTree().toPath().resolve("A.java"),g.getRepository().getWorkTree().toPath().resolve("B.java"));commit(g,3,"user@test");
            write(g,"C.java",Files.readString(g.getRepository().getWorkTree().toPath().resolve("B.java")));commit(g,4,"other@test");
            Files.delete(g.getRepository().getWorkTree().toPath().resolve("B.java"));commit(g,5,"user@test");
            write(g,"B.java","class New {}\n");var head=commit(g,6,"user@test");
            var r=extract(g,head,0,7);assertThat(r.reachableCommits()).isEqualTo(6);assertThat(r.facts()).hasSize(6);
            var first=r.facts().getFirst();assertThat(first.parentSha()).isNull();assertThat(first.additions()).isEqualTo(3);assertThat(first.deletions()).isZero();assertThat(first.authorKey()).isEqualTo("user@test");
            var renamed=r.facts().stream().filter(f->f.changeType().equals("RENAME")).findFirst().orElseThrow();
            assertThat(renamed.logicalFileId()).isEqualTo(first.logicalFileId());assertThat(renamed.additions()).isZero();assertThat(renamed.deletions()).isZero();
            assertThat(r.currentFiles().get("B.java").id()).isNotEqualTo(first.logicalFileId());
            assertThat(r.currentFiles().get("C.java").id()).isNotEqualTo(first.logicalFileId());
            assertThat(r).isEqualTo(extract(g,head,0,7));
        }
    }
    @Test void windowUsesCommitterTimeAndDoesNotPruneOldParents()throws Exception{
        try(var g=init("clock")) {
            write(g,"A.java","a\n");var first=commit(g,20,"a@test");
            write(g,"A.java","a\nb\n");var head=commit(g,5,"b@test");
            var r=extract(g,head,20,21);assertThat(r.selectedCommits()).isEqualTo(1);assertThat(r.facts()).extracting(GitIngestion.Fact::commitSha).containsExactly(first.name());
            assertThat(extract(g,head,0,20).facts()).extracting(GitIngestion.Fact::commitSha).containsExactly(head.name());
        }
    }
    @Test void renameBeforeWindowRetainsRootIdentityAndDeletedFilesStayInFacts()throws Exception{
        try(var g=init("window")) {
            write(g,"A.java","class A {}\n");var root=commit(g,1,"a@test");
            Files.move(g.getRepository().getWorkTree().toPath().resolve("A.java"),g.getRepository().getWorkTree().toPath().resolve("B.java"));commit(g,2,"a@test");
            Files.delete(g.getRepository().getWorkTree().toPath().resolve("B.java"));var head=commit(g,3,"a@test");
            var r=extract(g,head,3,4);assertThat(r.currentFiles()).isEmpty();assertThat(r.facts()).hasSize(1);assertThat(r.facts().getFirst().logicalFileId()).isEqualTo(root.name()+":A.java");assertThat(r.facts().getFirst().deletions()).isEqualTo(1);
        }
    }
    @Test void mergeDoesNotEmitFactsAndConflictingLineagesAreFlagged()throws Exception{
        try(var g=init("branch")) {
            write(g,"Base.java","class Base {}\n");var root=commit(g,1,"a@test");
            g.branchCreate().setName("side").call();write(g,"Main.java","class Main {}\n");commit(g,2,"a@test");
            g.checkout().setName("side").call();write(g,"Side.java","class Side {}\n");var side=commit(g,3,"b@test");
            g.checkout().setName("main").call();
            var merge=g.merge().include(side).setCommit(true).setMessage("merge fixture").call();
            try(var walk=new org.eclipse.jgit.revwalk.RevWalk(g.getRepository())) {
                var head=walk.parseCommit(merge.getNewHead());var r=extract(g,head,0,100);
                assertThat(r.reachableCommits()).isEqualTo(4);assertThat(r.selectedCommits()).isEqualTo(3);
                assertThat(r.facts()).noneMatch(f->f.commitSha().equals(head.name()));
                assertThat(r.currentFiles().get("Base.java").ambiguous()).isFalse();
                assertThat(r.currentFiles().get("Side.java").ambiguous()).isTrue();assertThat(r.qualityFlags()).contains("LINEAGE_AMBIGUOUS");
            }
        }
    }
    @Test void binaryHasMissingChurnAndExclusionsApply()throws Exception{
        try(var g=init("binary")) {
            write(g,"Binary.java","a\0b");write(g,"target/Generated.java","class G {}\n");write(g,"readme.txt","text");var head=commit(g,1,"a@test");
            var r=extract(g,head,0,2);assertThat(r.facts()).hasSize(1);assertThat(r.facts().getFirst().qualityFlags()).contains("BINARY");assertThat(r.facts().getFirst().additions()).isNull();
            assertThat(r.currentFiles()).containsOnlyKeys("Binary.java");
        }
    }
    @Test void aliasesUseAuthorNotCommitter()throws Exception{
        try(var g=init("author")) {
            write(g,"A.java","a\n");var head=commit(g,1,"alias@test");var o=options(0,2);
            var mapped=new GitIngestion.Options(o.start(),o.end(),o.maxCommits(),o.maxTreeEntries(),o.maxBlobBytes(),o.maxFacts(),o.deadline(),o.excludedPrefixes(),Map.of("alias@test","person-1"));
            assertThat(ingestion.extract(g.getRepository().getDirectory().toPath(),head.name(),mapped).facts().getFirst().authorKey()).isEqualTo("person-1");
        }
    }
    @Test void resourceLimitFailsInsteadOfReturningPartialResult()throws Exception{
        try(var g=init("limit")) {
            write(g,"A.java","a\n");commit(g,1,"a@test");write(g,"A.java","b\n");var head=commit(g,2,"a@test");var o=options(0,3);
            var limited=new GitIngestion.Options(o.start(),o.end(),1,10,1024,10,o.deadline(),List.of(),Map.of());
            assertThatThrownBy(()->ingestion.extract(g.getRepository().getDirectory().toPath(),head.name(),limited)).hasMessage("RESOURCE_LIMIT_COMMITS");
            assertThatThrownBy(()->ingestion.extract(g.getRepository().getDirectory().toPath(),"main",o)).isInstanceOf(IllegalArgumentException.class);
        }
    }
    @Test void expiredDeadlineAndShallowRepositoryAreRejected()throws Exception{
        try(var g=init("shallow")) {
            write(g,"A.java","a\n");var head=commit(g,1,"a@test");var o=options(0,2);
            var expired=new GitIngestion.Options(o.start(),o.end(),10,10,1024,10,Instant.EPOCH,List.of(),Map.of());
            assertThatThrownBy(()->ingestion.extract(g.getRepository().getDirectory().toPath(),head.name(),expired)).hasMessage("DEADLINE_EXCEEDED");
            Files.writeString(g.getRepository().getDirectory().toPath().resolve("shallow"),head.name()+"\n");
            assertThatThrownBy(()->extract(g,head,0,2)).hasMessage("SHALLOW_HISTORY");
        }
    }
    @Test void fullCloneFetchPinsDefaultBranchAndKeepsOldShaReplayable()throws Exception{
        try(var origin=init("remote")) {
            write(origin,"A.java","a\n");var old=commit(origin,1,"a@test");
            var client=new GitRepositoryClient();var directory=temp.resolve("cache.git");String uri=origin.getRepository().getDirectory().toURI().toString();
            var pin=client.cloneTrusted(uri,directory,Instant.now().plusSeconds(30));
            assertThat(pin.headSha()).isEqualTo(old.name());assertThat(pin.branch()).isEqualTo("refs/heads/main");
            write(origin,"A.java","a\nb\n");var newer=commit(origin,2,"a@test");
            var fetched=client.fetchTrusted(uri,directory,Instant.now().plusSeconds(30));assertThat(fetched.headSha()).isEqualTo(newer.name());
            assertThat(ingestion.extract(directory,old.name(),options(0,3)).reachableCommits()).isEqualTo(1);
            assertThat(ingestion.extract(directory,newer.name(),options(0,3)).reachableCommits()).isEqualTo(2);
            assertThatThrownBy(()->client.cloneTrusted(uri,directory,Instant.now().plusSeconds(30))).hasMessage("CLONE_TARGET_EXISTS");
            assertThatThrownBy(()->client.fetchTrusted("file:///other",directory,Instant.now().plusSeconds(30))).hasMessage("REMOTE_MISMATCH");
        }
    }
    @Test void publicClientRejectsLocalUrlBeforeCreatingDirectory() {
        Path destination=temp.resolve("never-created");
        assertThatThrownBy(()->new GitRepositoryClient().clonePublic("file:///tmp/repo",destination,Instant.now().plusSeconds(30))).isInstanceOf(IllegalArgumentException.class);
        assertThat(destination).doesNotExist();
    }
}
