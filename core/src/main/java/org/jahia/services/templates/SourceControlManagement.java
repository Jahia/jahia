/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Base service class for SCM related operations.
 */
public abstract class SourceControlManagement {

    private static final Logger logger = LoggerFactory.getLogger(SourceControlManagement.class);

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

            // remove potential "hint:" in message string
            message = message.replace("hint:", "");

            throw new IOException(message);
        }
    }

    /**
     * Check if a commit is required
     * @return true if commit is required, false if no change
     * @throws IOException if merge conflict is still there
     */
    public boolean checkCommit() throws IOException {
        Map<String, Status> statusMap = createStatusMap();
        if (statusMap.values().contains(Status.UNMERGED)) {
            throw new IOException("Commit : remaining conflicts need to be resolved");
        }
        return statusMap.values().contains(Status.MODIFIED) || statusMap.values().contains(Status.ADDED)
                || statusMap.values().contains(Status.DELETED) || statusMap.values().contains(Status.RENAMED)
                || statusMap.values().contains(Status.COPIED) || statusMap.values().contains(Status.UNMERGED);
    }

    /**
     * Performs a commit into the SCM.
     *
     * @param message
     *            the commit message
     * @throws IOException
     *             in case of a commit process error
     */
    public abstract boolean commit(String message) throws IOException;

    protected abstract Map<String, Status> createStatusMap() throws IOException;

    protected ExecutionResult executeCommand(String command, String[] arguments) throws IOException {

        String argumentsString = StringUtils.trimToEmpty(StringUtils.join(arguments, " "));
        String commandString = command + " " + argumentsString;
        logger.info("Executing SCM command: '{}'...", commandString);

        int res;
        StringBuilder resultOut = new StringBuilder();
        StringBuilder resultErr = new StringBuilder();
        try {
            res = ProcessHelper.execute(command, arguments, null, rootFolder, resultOut, resultErr, false);
        } catch (Exception e) {
            throw new IOException(
                    "Failed to execute command '" + commandString + "'", e);
        }
        ExecutionResult result = new ExecutionResult(res, resultOut.toString(), resultErr.toString());

        if (logger.isInfoEnabled()) {
            StringBuilder logMessage = new StringBuilder("\n");
            logMessage.append("Executed SCM command: '").append(commandString).append("'\n");
            if (rootFolder != null) {
                logMessage.append("In the directory: '").append(rootFolder).append("'\n");
            }
            logMessage.append("Exit code: ").append(result.exitValue).append("\n");
            if (StringUtils.isNotBlank(result.out)) {
                logMessage.append("Command output:\n").append(result.out.trim()).append("\n");
            }
            if (StringUtils.isNotBlank(result.err)) {
                logMessage.append("Command errors:\n").append(result.err.trim()).append("\n");
            }
            logger.info(logMessage.toString());
        }

        return result;
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
     * @return the output of the SCM update command if available
     * @throws IOException
     *             in case of SCM errors
     */
    public abstract String update() throws IOException;

    /**
     * List all available tags on distant repository
     * @param uri the remote SCM repository URI (of the trunk for SVN)
     * @return a map tag/uri
     * @throws IOException
     */
    public abstract Map<String, String> getTagInfos(String uri) throws IOException;

    /**
     * List all available branches on distant repository
     * @param uri the remote SCM repository URI (of the trunk for SVN)
     * @return a map tag/uri
     * @throws IOException
     */
    public abstract Map<String, String> getBranchInfos(String uri) throws IOException;
}
