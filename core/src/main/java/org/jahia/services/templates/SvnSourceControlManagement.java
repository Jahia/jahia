package org.jahia.services.templates;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;

import java.io.File;
import java.util.List;

public class SvnSourceControlManagement extends SourceControlManagement {

    private File workingDirectory;
    private SVNClientManager svnClientManager;

    @Override
    protected void initWithEmptyFolder(File workingDirectory, String url) throws Exception {
        this.workingDirectory = workingDirectory;
        svnClientManager = SVNClientManager.newInstance();
    }

    @Override
    protected void initWithWorkingDirectory(File workingDirectory) throws Exception {
        this.workingDirectory = workingDirectory;
        svnClientManager = SVNClientManager.newInstance();
    }

    @Override
    protected void initFromURI(File workingDirectory, String uri) throws Exception {
        svnClientManager = SVNClientManager.newInstance();
    }

    @Override
    public File getRootFolder() {
        return workingDirectory;
    }

    @Override
    public void setModifiedFile(List<File> files) {
        if (files.isEmpty()) {
            return;
        }
        try {
            svnClientManager.getWCClient().doAdd(files.toArray(new File[0]), true, false, true, SVNDepth.INFINITY, true, false, true);
        } catch (SVNException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void update() {
        try {
            svnClientManager.getUpdateClient().doUpdate(workingDirectory, SVNRevision.HEAD, SVNDepth.INFINITY, true, true);
        } catch (SVNException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void commit(String message) {
        try {
            svnClientManager.getCommitClient().doCommit(new File[] {workingDirectory}, false, message,  null, null, false, false, SVNDepth.INFINITY);
        } catch (SVNException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
