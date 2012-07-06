package org.jahia.services.templates;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SvnSourceControlManagement extends SourceControlManagement {

    @Override
    protected void initWithEmptyFolder(File workingDirectory, String url) throws Exception {
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void initWithWorkingDirectory(File workingDirectory) throws Exception {
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void initFromURI(File workingDirectory, String uri) throws Exception {
        this.rootFolder = workingDirectory.getParentFile();
        executeCommand(Arrays.asList("svn", "checkout", uri, workingDirectory.getName()));
        this.rootFolder = workingDirectory;
    }

    @Override
    public void setModifiedFile(List<File> files) {
        if (files.isEmpty()) {
            return;
        }

        String rootPath = rootFolder.getPath();
        List<String> cmdList = new ArrayList<String>();
        cmdList.add("svn");
        cmdList.add("add");
        for (File file : files) {
            if (file.getPath().equals(rootPath)) {
                cmdList.add(".");
            } else {
                cmdList.add(file.getPath().substring(rootPath.length() + 1));
            }
        }
        executeCommand(cmdList);
    }

    @Override
    public void update() {
        executeCommand(Arrays.asList("svn", "pull"));
    }

    @Override
    public void commit(String message) {
        executeCommand(Arrays.asList("svn", "commit", "-m", message));
    }
}
