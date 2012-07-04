package org.jahia.services.templates;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class SourceControlManagement {

    public static SourceControlManagement getSourceControlManagement(File workingDir) throws Exception {
        SourceControlManagement scm = null;

        if (new File(workingDir, ".git").exists()) {
            scm = new GitSourceControlManagement();
        } else if (new File(workingDir, ".svn").exists()) {
            scm = new SvnSourceControlManagement();
        } else {
            return null;
        }

        scm.initWithWorkingDirectory(workingDir);
        return scm;
    }

    public static SourceControlManagement createNewRepository(File workingDir, String type, String url) throws Exception {
        SourceControlManagement scm = null;

        if (type.equals("git")) {
            scm = new GitSourceControlManagement();
        } else {
            throw new Exception("Unknown repository type");
        }

        scm.initWithEmptyFolder(workingDir, url);
        return scm;
    }

    public static SourceControlManagement checkoutRepository(File workingDir, String type, String url) throws Exception {
        SourceControlManagement scm = null;

        if (type.equals("git")) {
            scm = new GitSourceControlManagement();
        } else {
            throw new Exception("Unknown repository type");
        }

        scm.initFromURI(workingDir, url);
        return scm;
    }

    protected abstract void initWithEmptyFolder(File workingDirectory, String url) throws Exception ;

    protected abstract void initWithWorkingDirectory(File workingDirectory) throws Exception;

    protected abstract void initFromURI(File workingDirectory, String uri) throws Exception;

    public abstract File getRootFolder() throws Exception;

    public abstract void setModifiedFile(List<File> files) throws Exception;

    public abstract void update() throws Exception;

    public abstract void commit(String message) throws Exception;
}
