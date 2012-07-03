package org.jahia.services.templates;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: toto
 * Date: 7/2/12
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class GitSourceControlManagement extends SourceControlManagement {

    private Git git;

    public GitSourceControlManagement() {
    }

    protected void initWithEmptyFolder(File workingDirectory, String url) throws Exception {
        git = Git.init()
                .setBare(false)
                .setDirectory(workingDirectory)
                .call();

        StoredConfig config = git.getRepository().getConfig();
        config.setString("remote", "origin", "url", url);
        config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
        config.setString("branch", "master", "merge", "refs/heads/master");
        config.setString("branch", "master", "remote", "origin");
        config.setString("branch", "master", "rebase", "true");
        config.save();
    }

    protected void initWithWorkingDirectory(File workingDirectory) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        Repository repository = builder.setWorkTree(workingDirectory)
                .setup()
                .readEnvironment() // scan environment GIT_* variables
                .build();
        git = new Git(repository);
    }

    protected void initFromURI(File workingDirectory, String uri) throws Exception {
        git = Git.cloneRepository()
                .setURI(uri)
                .setDirectory(workingDirectory)
                .call();
    }

    @Override
    public File getRootFolder() {
        return git.getRepository().getWorkTree();
    }

    public void setModifiedFile(List<File> files) {
        AddCommand add = git.add();
        String rootPath = git.getRepository().getWorkTree().getPath();

        for (File file : files) {
            if (file.getPath().equals(rootPath)) {
                add.addFilepattern(".");
            } else {
                add.addFilepattern(file.getPath().substring(rootPath.length()+ 1));
            }
        }
        try {
            add.call();
        } catch (GitAPIException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void update() {
        try {
//            RevCommit r = git.stashCreate().call();
            FetchResult fr = git.fetch()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                    .call();
            MergeCommand merge = git.merge();
            merge.include(fr.getAdvertisedRef("refs/heads/master"));
            MergeResult mr = merge.call();

            System.out.println("--- conflicts = " + mr.getConflicts());
            for (ObjectId id : mr.getMergedCommits()) {
                System.out.println("merged : "+id);
            }
//            if (r != null) {
//                git.stashApply().call();
//            }
        } catch (GitAPIException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void commit(String message) {
        try {
            RevCommit commit = git.commit()
                    .setAll(true)
                    .setMessage(message)
                    .call();

            Iterable<PushResult> push = git.push()
                    .call();
        } catch (GitAPIException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
