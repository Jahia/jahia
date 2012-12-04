/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.utils.ProcessHelper;
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

    protected ExecutionResult executeCommand(String command, String arguments) {
        try {
            StringBuilder resultOut = new StringBuilder();
            StringBuilder resultErr = new StringBuilder();
            int res = ProcessHelper.execute(command, arguments, null, rootFolder, resultOut, resultErr);
            return new ExecutionResult(res, resultOut.toString(), resultErr.toString());
        } catch (Exception e) {
            // TODO can we find a way to not call "svn add" on already versioned files? 
            if (command.equals("svn") && arguments.startsWith("add")) {
                logger.warn("Failed to execute command " + command + (arguments != null ? (" " + arguments) : ""));
            } else {
                logger.error("Failed to execute command " + command + (arguments != null ? (" " + arguments) : ""), e);
            }
        }
        return null;
    }

    class ExecutionResult {
        int resultCode;
        String out;
        String err;

        ExecutionResult(int resultCode, String out, String err) {
            this.resultCode = resultCode;
            this.out = out;
            this.err = err;
        }
    }

    protected void executeCommandUsingProcessBuidler(List<String> cmdList) {
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

    public abstract String getURI() throws Exception;

    public abstract void setModifiedFile(List<File> files) throws Exception;

    public abstract void update() throws Exception;

    public abstract void commit(String message) throws Exception;
}
