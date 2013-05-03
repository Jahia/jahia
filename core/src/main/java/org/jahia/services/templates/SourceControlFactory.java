/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

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
