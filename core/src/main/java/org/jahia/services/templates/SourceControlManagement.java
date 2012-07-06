package org.jahia.services.templates;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public abstract class SourceControlManagement {

    private static Logger logger = LoggerFactory.getLogger(SourceControlManagement.class);

    protected File rootFolder;

    protected void executeCommand(List<String> cmdList) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
            processBuilder.directory(rootFolder);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            int code = process.waitFor();
            if (logger.isDebugEnabled()) {
                logger.debug("Return with code " + code);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    logger.debug(line);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to execute command " + StringUtils.join(cmdList, ' '), e);
        }
    }

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
        } else if (type.equals("svn")) {
            scm = new SvnSourceControlManagement();
        } else {
            throw new Exception("Unknown repository type");
        }

        scm.initFromURI(workingDir, url);
        return scm;
    }

    public File getRootFolder() {
        return rootFolder;
    }

    protected abstract void initWithEmptyFolder(File workingDirectory, String url) throws Exception ;

    protected abstract void initWithWorkingDirectory(File workingDirectory) throws Exception;

    protected abstract void initFromURI(File workingDirectory, String uri) throws Exception;

    public abstract void setModifiedFile(List<File> files) throws Exception;

    public abstract void update() throws Exception;

    public abstract void commit(String message) throws Exception;
}
