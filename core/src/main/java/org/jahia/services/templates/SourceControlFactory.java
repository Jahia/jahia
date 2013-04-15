package org.jahia.services.templates;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.jahia.utils.ProcessHelper;
import org.jahia.utils.StringOutputStream;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SourceControlFactory {
    private Map<String,String> sourceControlExecutables;

    public Map<String, String> getSourceControlExecutables() {
        return sourceControlExecutables;
    }

    public void setSourceControlExecutables(Map<String, String> sourceControlExecutables) {
        this.sourceControlExecutables = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : sourceControlExecutables.entrySet()) {
            try {
                DefaultExecutor executor = new DefaultExecutor();
                executor.setStreamHandler(new PumpStreamHandler(new StringOutputStream(), new StringOutputStream()));
                executor.execute(new CommandLine(entry.getValue()), System.getenv());
            } catch (ExecuteException e) {
            } catch (IOException e) {
                continue;
            }
            this.sourceControlExecutables.put(entry.getKey(), entry.getValue());
        }
    }

    public Set<String> getSupportedSourceControls() {
        return sourceControlExecutables.keySet();
    }

    public SourceControlManagement getSourceControlManagement(File workingDir) throws IOException {
        SourceControlManagement scm = null;
        while (true) {
            if (new File(workingDir,".git").exists()) {
                if (!sourceControlExecutables.containsKey("git")) {
                    return null;
                }
                scm = new GitSourceControlManagement(sourceControlExecutables.get("git"));
                break;
            } else if (new File(workingDir,".svn").exists()) {
                if (!sourceControlExecutables.containsKey("svn")) {
                    return null;
                }
                scm = new SvnSourceControlManagement(sourceControlExecutables.get("svn"));
                break;
            } else {
                if (workingDir.getParentFile() == null) {
                    break;
                }  else {
                    workingDir = workingDir.getParentFile();
                }
            }
        }
        if (scm != null) {
            scm.initWithWorkingDirectory(workingDir);
        }
        return scm;
    }

    public SourceControlManagement createNewRepository(File workingDir, String scmURI) throws IOException {
        SourceControlManagement scm = null;

        if (scmURI.startsWith("scm:")) {
            String scmProvider = scmURI.substring(4, scmURI.indexOf(":", 4));
            String scmUrl = scmURI.substring(scmURI.indexOf(":", 4) + 1);

            if (scmProvider.equals("git") && sourceControlExecutables.containsKey("git")) {
                scm = new GitSourceControlManagement(sourceControlExecutables.get("git"));
            } else {
                throw new IOException("Unknown repository type");
            }

            scm.initWithEmptyFolder(workingDir, scmUrl);
        }

        return scm;
    }

    public SourceControlManagement checkoutRepository(File workingDir, String scmURI, String branchOrTag) throws IOException {
        SourceControlManagement scm = null;

        if (scmURI.startsWith("scm:")) {
            String scmProvider = scmURI.substring(4, scmURI.indexOf(":", 4));
            String scmUrl = scmURI.substring(scmURI.indexOf(":", 4) + 1);

            if (scmProvider.equals("git") && sourceControlExecutables.containsKey("git")) {
                scm = new GitSourceControlManagement(sourceControlExecutables.get("git"));
            } else if (scmProvider.equals("svn") && sourceControlExecutables.containsKey("svn")) {
                scm = new SvnSourceControlManagement(sourceControlExecutables.get("svn"));
            } else {
                throw new IOException("Unknown repository type");
            }

            scm.initFromURI(workingDir, scmUrl, branchOrTag);
        }
        return scm;
    }
}
