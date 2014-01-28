/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.ProcessHelper;

import com.google.common.collect.ImmutableList;

/**
 * Base service class for SCM related operations.
 */
public abstract class SourceControlManagement {

    /**
     * Represents the result of an external command execution.
     */
    protected static class ExecutionResult {
        protected String err;
        protected int exitValue;
        protected String out;

        protected ExecutionResult(int exitValue, String out, String err) {
            this.exitValue = exitValue;
            this.out = out;
            this.err = err;
        }
    }

    /**
     * The source control status of a resource.
     */
    public enum Status {
        ADDED, COPIED, DELETED, MODIFIED, RENAMED, UNMERGED, UNMODIFIED, UNTRACKED
    }

    protected static List<String> readLines(String source) throws IOException {
        StringReader input = null;
        try {
            input = new StringReader(source);
            return IOUtils.readLines(input);
        } finally {
            IOUtils.closeQuietly(input);
        }
    }

    protected String executable;

    protected File rootFolder;

    private volatile Map<String, Status> statusMap;
    
    protected SourceControlManagement(String executable) {
        super();
        this.executable = executable;
    }

    /**
     * Adds the specified file to be included into the next commit.
     * 
     * @param file
     *            a file to be considered as modified
     * @throws IOException
     *             in case of an SCM related error
     */
    public final void add(File file) throws IOException {
        add(ImmutableList.of(file));
    }

    /**
     * Adds the specified files to be included into the next commit.
     * 
     * @param files
     *            the list of files to be considered as modified
     * @throws IOException
     *             in case of an SCM related error
     */
    public abstract void add(List<File> files) throws IOException;

    protected void checkExecutionResult(ExecutionResult result) throws IOException {
        if (result.exitValue != 0 || result.out.contains("conflicts")) {
            String message = result.err;
            if (StringUtils.isBlank(message)) {
                message = result.out;
            }
            throw new IOException(message);
        }
    }

    /**
     * Performs a commit into the SCM.
     * 
     * @param message
     *            the commit message
     * @throws IOException
     *             in case of a commit process error
     */
    public abstract void commit(String message) throws IOException;

    protected abstract Map<String, Status> createStatusMap() throws IOException;

    protected ExecutionResult executeCommand(String command, String[] arguments) throws IOException {
        try {
            StringBuilder resultOut = new StringBuilder();
            StringBuilder resultErr = new StringBuilder();
            int res = ProcessHelper.execute(command, arguments, null, rootFolder, resultOut, resultErr, false);
            return new ExecutionResult(res, resultOut.toString(), resultErr.toString());
        } catch (Exception e) {
            throw new IOException(
                    "Failed to execute command " + command + (arguments != null ? (" " + arguments) : ""), e);
        }
    }

    /**
     * Returns the root folder of the module.
     * 
     * @return the root folder of the module
     */
    public File getRootFolder() {
        return rootFolder;
    }

    /**
     * Returns the SCM status of the specified resource.
     * 
     * @param path
     *            the resource to check the SCM status for
     * @return the SCM status of the specified resource
     * @throws IOException
     *             in case of SCM errors
     */
    public Status getStatus(String path) throws IOException {
        Map<String, Status> statuses = getStatusMap();
        Status s = statuses.get(path);
        if (s != null) {
            return s;
        } else {
            if (path.indexOf('/') != -1 && statuses.values().contains(Status.UNTRACKED)) {
                StringBuilder subPath = new StringBuilder(32);
                for (String segment : StringUtils.split(path, '/')) {
                    if (subPath.length() > 0) {
                        if (statuses.get(subPath.toString()) == Status.UNTRACKED) {
                            return Status.UNTRACKED;
                        }
                        subPath.append('/');
                    }
                    subPath.append(segment);
                }
            }
        }
        return Status.UNMODIFIED;
    }

    protected final Map<String, Status> getStatusMap() throws IOException {
        if (statusMap == null) {
            synchronized (this) {
                if (statusMap == null) {
                    statusMap = createStatusMap();
                }
            }
        }
        return statusMap;
    }

    /**
     * Returns an SCM URI.
     * 
     * @return an SCM URI
     * @throws IOException
     *             in case of an SCM related error
     */
    public abstract String getURI() throws IOException;

    protected abstract void getFromSCM(File workingDirectory, String uri, String branchOrTag) throws IOException;

    protected abstract void sendToSCM(File workingDirectory, String url) throws IOException;

    protected abstract void initWithWorkingDirectory(File workingDirectory) throws IOException;

    /**
     * Invalidates the SCM status cache and forces for the next check the SCM interaction.
     */
    public synchronized void invalidateStatusCache() {
        statusMap = null;
    }

    /**
     * Mark SCM conflict as resolved for the specified resource.
     * 
     * @param file
     *            the resource to make as resolved
     * @throws IOException
     *             in case of SCM errors
     */
    public abstract void markConflictAsResolved(File file) throws IOException;

    /**
     * Moves the specified resource to a new location.
     * 
     * @param src
     *            the source
     * @param dst
     *            the destination
     * @throws IOException
     *             in case of SCM errors
     */
    public abstract void move(File src, File dst) throws IOException;

    /**
     * Deletes the specified item from the working copy or repository.
     * 
     * @param file
     *            the resource to be removed
     * @throws IOException
     *             in case of SCM errors
     */
    public abstract void remove(File file) throws IOException;

    /**
     * Performs SCM update.
     * 
     * @throws IOException
     *             in case of SCM errors
     */
    public abstract void update() throws IOException;
}
