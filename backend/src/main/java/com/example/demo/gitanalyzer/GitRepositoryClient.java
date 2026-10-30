package com.example.demo.gitanalyzer;

import com.example.demo.repository.GitHubRepositoryUrl;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.transport.*;

/** Worker-facing client. No controller, credentials, checkout, submodules or repository scripts. */
public final class GitRepositoryClient {
    public record PinnedRepository(Path directory,String branch,String headSha) {}
    private static final CredentialsProvider ANONYMOUS=new CredentialsProvider() {
        public boolean isInteractive(){return false;}
        public boolean supports(CredentialItem... items){return false;}
        public boolean get(URIish uri,CredentialItem... items){return false;}
    };
    public PinnedRepository clonePublic(String url,Path newDirectory,Instant deadline) throws IOException,GitAPIException {
        return cloneTrusted(GitHubRepositoryUrl.parse(url).url(),newDirectory,deadline);
    }
    public PinnedRepository fetchPublic(String url,Path directory,Instant deadline) throws IOException,GitAPIException {
        return fetchTrusted(GitHubRepositoryUrl.parse(url).url(),directory,deadline);
    }
    // Package-private local transport seam used only by deterministic repository fixtures.
    PinnedRepository cloneTrusted(String uri,Path directory,Instant deadline) throws IOException,GitAPIException {
        if(Files.exists(directory))throw new IOException("CLONE_TARGET_EXISTS");
        Budget monitor=new Budget(deadline);
        try(var git=Git.init().setDirectory(directory.toFile()).setBare(true).call()) {
            var config=git.getRepository().getConfig();
            config.setString("remote","origin","url",uri);
            config.setString("http",null,"followRedirects","false");config.save();
            return fetchAndPin(git,uri,directory,monitor);
        }
    }
    PinnedRepository fetchTrusted(String uri,Path directory,Instant deadline) throws IOException,GitAPIException {
        try(var git=Git.open(directory.toFile())) {
            if(!git.getRepository().isBare())throw new IOException("EXPECTED_MANAGED_BARE_REPOSITORY");
            if(!uri.equals(git.getRepository().getConfig().getString("remote","origin","url")))throw new IOException("REMOTE_MISMATCH");
            if(!git.getRepository().getObjectDatabase().getShallowCommits().isEmpty())throw new IOException("SHALLOW_HISTORY");
            return fetchAndPin(git,uri,directory,new Budget(deadline));
        }
    }
    private PinnedRepository fetchAndPin(Git git,String uri,Path directory,Budget budget) throws IOException,GitAPIException {
        budget.verify();
        git.getRepository().getConfig().setString("http",null,"followRedirects","false");
        var result=git.fetch().setRemote(uri).setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
            .setRemoveDeletedRefs(true).setTagOpt(TagOpt.FETCH_TAGS).setCredentialsProvider(ANONYMOUS)
            .setTimeout(30).setProgressMonitor(budget).call();
        budget.verify();
        Ref remoteHead=result.getAdvertisedRef(Constants.HEAD);
        if(remoteHead==null || remoteHead.getObjectId()==null)throw new IOException("EMPTY_OR_UNRESOLVED_HEAD");
        String branch;
        if(remoteHead.isSymbolic())branch=remoteHead.getLeaf().getName();
        else {
            var names=result.getAdvertisedRefs().stream().filter(r->r.getName().startsWith(Constants.R_HEADS)
                && remoteHead.getObjectId().equals(r.getObjectId())).map(Ref::getName).sorted().toList();
            if(names.size()!=1)throw new IOException("AMBIGUOUS_DEFAULT_BRANCH");
            branch=names.getFirst();
        }
        if(!branch.startsWith(Constants.R_HEADS) || !git.getRepository().getObjectDatabase().has(remoteHead.getObjectId()))throw new IOException("UNRESOLVED_DEFAULT_BRANCH");
        return new PinnedRepository(directory.toAbsolutePath(),branch,remoteHead.getObjectId().name());
    }
    private static final class Budget implements ProgressMonitor {
        private final Instant deadline;
        Budget(Instant deadline)throws IOException{this.deadline=Objects.requireNonNull(deadline);verify();}
        void verify()throws IOException{if(isCancelled())throw new IOException("CANCELLED_OR_DEADLINE_EXCEEDED");}
        public void start(int totalTasks){}public void beginTask(String title,int totalWork){}
        public void update(int completed){}public void endTask(){}public void showDuration(boolean enabled){}
        public boolean isCancelled(){return Thread.currentThread().isInterrupted()||!Instant.now().isBefore(deadline);}
    }
}
