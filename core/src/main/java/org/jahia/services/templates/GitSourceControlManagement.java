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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Git based source control management service.
 *
 * @author Thomas Draier
 */
public class GitSourceControlManagement extends SourceControlManagement {


    private static final Logger logger = LoggerFactory.getLogger(GitSourceControlManagement.class);

    /**
     * Initializes an instance of this class.
     * @param executable the git executable
     */
    public GitSourceControlManagement(String executable) {
        super(executable);
    }

    @Override
    public void add(List<File> files) throws IOException {
        if (files.isEmpty()) {
            return;
        }

        String rootPath = rootFolder.getPath();
        List<String> args = new ArrayList<String>();
        args.add("add");
        for (File file : files) {
            if (file.getPath().equals(rootPath)) {
                args.add(".");
            } else {
                args.add(file.getPath().substring(rootPath.length() + 1));
            }
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public boolean commit(String message) throws IOException {
        // retrieve the name of the current branch
        ExecutionResult result = executeCommand(executable, new String[]{"symbolic-ref", "--short", "HEAD"});
        // if we have an error getting the current branch name, we're on a temp branch so probably in the middle of a rebase
        boolean needRebaseContinue = result.exitValue > 0;
        String branch = needRebaseContinue ? null : result.out.trim();

        // commit stuff if needed
        boolean commitRequired = checkCommit();
        if (commitRequired) {
            checkExecutionResult(executeCommand(executable, new String[]{"commit","-a","-m", message }));
        }

        boolean didWeDoAnything = commitRequired;

        // if we're in the middle of a rebase, continue it and see if we can be done
        if(needRebaseContinue) {
            // check status to see if we're done
            result = executeCommand(executable, new String[]{"status"});
            if(result.exitValue == 0 && result.out.contains("all conflicts fixed")) {
                // we don't have any more conflicts so we can finish the rebase
                result = executeCommand(executable, new String[]{"rebase", "--continue"});

                // it's possible that the changes have already been applied to we can just skip this patch
                if(result.exitValue > 0 && result.out.contains("No changes")) {
                    result = executeCommand(executable, new String[]{"rebase", "--skip"});
                }
                checkExecutionResult(result);

                // retrieve the branch name again
                result = executeCommand(executable, new String[]{"symbolic-ref", "--short", "HEAD"});
                checkExecutionResult(result);
                branch = result.out.trim();

                didWeDoAnything = true;
            }
            else {
                checkExecutionResult(result);
            }
        }

        // check if we need to push to remote for the current branch
        // git log --branches --not --remotes --simplify-by-decoration --decorate --oneline
        // output looks like: 120bc4a (HEAD,master) afsddsgdsg
        result = executeCommand(executable, new String[]{"log", "--branches","--not","--remotes","--simplify-by-decoration","--decorate","--oneline"});
        if(result.out.contains(branch)) {
            // we have un-pushed commits for the current branch so push them
            result = executeCommand(executable, new String[]{"-c", "core.askpass=true", "push", "--porcelain", "-u", "origin", branch});
            checkExecutionResult(result);

            didWeDoAnything = true;
        }

        invalidateStatusCache();

        return didWeDoAnything;
    }

    @Override
    protected Map<String, Status> createStatusMap() throws IOException {
        return createStatusMap(true);
    }

    private Map<String, Status> createStatusMap(boolean folder) throws IOException {
        Map<String, Status> newMap = new HashMap<String, Status>();
        List<String> paths = readLines(executeCommand(executable, new String[]{"rev-parse", "--show-prefix"}).out);
        String relPath = paths.isEmpty()?"":paths.get(0);
        ExecutionResult result = executeCommand(executable, new String[]{"status","--porcelain"});
        for (String line : readLines(result.out)) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String path = line.substring(3);
            if (path.contains(" -> ")) {
                path = StringUtils.substringAfter(path, " -> ");
            }
            path = StringUtils.removeEnd(path, "/");
            path = StringUtils.removeStart(path, relPath);
            char indexStatus = line.charAt(0);
            char workTreeStatus = line.charAt(1);
            Status status = null;
            if (workTreeStatus == ' ') {
                if (indexStatus == 'M') {
                    status = Status.MODIFIED;
                } else if (indexStatus == 'A') {
                    status = Status.ADDED;
                } else if (indexStatus == 'D') {
                    status = Status.DELETED;
                } else if (indexStatus == 'R') {
                    status = Status.RENAMED;
                } else if (indexStatus == 'C') {
                    status = Status.COPIED;
                }
            } else if (workTreeStatus == 'M') {
                status = Status.MODIFIED;
            } else if (workTreeStatus == 'D') {
                if (indexStatus == 'D' || indexStatus == 'U') {
                    status = Status.UNMERGED;
                } else {
                    status = Status.DELETED;
                }
            } else if (workTreeStatus == 'A' || workTreeStatus == 'U') {
                status = Status.UNMERGED;
            } else if (workTreeStatus == '?') {
                status = Status.UNTRACKED;
            }
            if (status != null) {
                // put resource status
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                newMap.put(path, status);
                if (folder) {
                    // store intermediate folder status as MODIFIED 
                    StringBuilder subPath = new StringBuilder();
                    for (String segment : StringUtils.split(path, '/')) {
                        newMap.put(subPath.length() == 0 ? "/" : subPath.toString(), Status.MODIFIED);
                        subPath.append('/');
                        subPath.append(segment);
                    }
                }
            }
        }
        return newMap;
    }

    @Override
    public File getRootFolder() {
        return rootFolder;
    }

    @Override
    public String getURI() throws IOException {
        ExecutionResult result = executeCommand(executable, new String[]{"remote","-v"});
        String url = StringUtils.substringBefore(StringUtils.substringAfter(result.out,"origin"),"(").trim();
        if (!StringUtils.isEmpty(url)) {
            return "scm:git:"+url;
        }
        return null;
    }

    protected void getFromSCM(File workingDirectory, String uri, String branchOrTag) throws IOException {
        this.rootFolder = workingDirectory.getParentFile();
        ExecutionResult r = executeCommand(executable, new String[]{"-c", "core.askpass=true", "clone", uri, workingDirectory.getName()});
        if (r.exitValue > 0) {
            throw new SourceControlException(r.err);
        }
        this.rootFolder = workingDirectory;
        if (!StringUtils.isEmpty(branchOrTag)) {
            executeCommand(executable, new String[]{"checkout ",branchOrTag});
        }
        this.rootFolder = workingDirectory;
    }

    protected void sendToSCM(File workingDirectory, String url) throws IOException {
        int  MERGE_COMMAND_INDEX = 5;
        List<String[]> commands = Arrays.asList(
                new String[]{"init"},
                new String[]{"add", "."},
                new String[]{"commit", "-a", "-m", "First commit"},
                new String[]{"remote", "add", "origin", url},
                new String[]{"-c", "core.askpass=true", "fetch"},
                new String[]{"merge", "origin/master", "--allow-unrelated-histories"},
                new String[]{"-c", "core.askpass=true", "push", "-u", "origin", "master"});

        this.rootFolder = workingDirectory;
        for (String[] command : commands) {
            logger.debug("executing command : {}", Arrays.toString(command));
            ExecutionResult res = executeCommand(executable, command);

            // if the remote repo is empty, the merge orgin/master fail but we have to continue
            // the merge is only used for repo with existing content
            if (!Arrays.equals(command,commands.get(MERGE_COMMAND_INDEX)) && res.exitValue > 0) {
                // an issue occurs during first commit
                // clean up
                executeCommand(executable,new String[]{"merge", "--abort"});
                File gitDir = new File(workingDirectory.getPath() + "/.git");
                if (gitDir.exists()) {
                    FileUtils.deleteDirectory(gitDir);
                }
                logger.error("unable to init git repository {} : {}", url, res.err);
                if (!StringUtils.isEmpty(res.out)) {
                    logger.error(res.out);
                }
                StringBuilder message = new StringBuilder();
                if (!StringUtils.isEmpty(res.err)) {
                    if (StringUtils.contains(res.err,"tree files would be overwritten")) {
                        // tree issue, unable to merge existing content
                        message.append("Unable to send sources to a non empty repository");
                    } else if (StringUtils.contains(res.err,"Repository not found")) {
                        // repo not found
                        message.append("Repository not found");
                    } else {
                        message.append("Repository not accessible, see the log for more information");
                    }
                } else {
                    message.append("Repository not accessible, see the log for more information");
                }
                throw new SourceControlException("Unable to init git repository. " + message.toString());
            }
        }


    }

    protected void initWithWorkingDirectory(File workingDirectory) throws IOException {
        this.rootFolder = workingDirectory;
    }

    @Override
    public void markConflictAsResolved(File file) throws IOException {
        add(file);
    }

    @Override
    public void move(File src, File dst) throws IOException {
        if (src == null || dst == null) {
            return;
        }

        String rootPath = rootFolder.getPath();

        List<String> args = new ArrayList<String>();
        args.add("mv");
        if (src.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(src.getPath().substring(rootPath.length() + 1));
        }
        if (dst.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(dst.getPath().substring(rootPath.length() + 1));
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public void remove(File file) throws IOException {
        if (file == null) {
            return;
        }

        String rootPath = rootFolder.getPath();

        List<String> args = new ArrayList<String>();
        args.add("rm");
        args.add("-f");
        if (file.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(file.getPath().substring(rootPath.length() + 1));
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public String update() throws IOException {
        StringBuilder out = new StringBuilder();
        out.append("[").append(executable).append(" stash clear").append("]:\n");
        ExecutionResult result = executeCommand(executable, new String[]{"stash", "clear"});
        out.append(result.out);
        out.append("\n");
        if (StringUtils.isNotEmpty(result.err)) {
            out.append(result.err).append("\n");
        }

        Map<String, Status> statusMap = createStatusMap(false);
        boolean stashRequired = statusMap.values().contains(Status.MODIFIED) || statusMap.values().contains(Status.ADDED)
                || statusMap.values().contains(Status.DELETED) || statusMap.values().contains(Status.RENAMED)
                || statusMap.values().contains(Status.COPIED) || statusMap.values().contains(Status.UNMERGED);
        if (stashRequired) {
            out.append("[").append(executable).append(" stash").append("]:\n");
            result = executeCommand(executable, new String[]{"stash"});
            out.append(result.out);
            out.append("\n");
            if (StringUtils.isNotEmpty(result.err)) {
                out.append(result.err).append("\n");
            }
        }
        out.append("[").append(executable).append(" pull --rebase").append("]:\n");
        ExecutionResult pullResult = executeCommand(executable, new String[]{"pull","--rebase"});
        out.append(pullResult.out);
        out.append("\n");
        if (StringUtils.isNotEmpty(pullResult.err)) {
            out.append(pullResult.err).append("\n");
        }
        ExecutionResult stashPopResult = null;
        if (stashRequired) {
            out.append("[").append(executable).append(" stash pop").append("]:\n");
            stashPopResult = executeCommand(executable, new String[]{"stash","pop"});
            out.append(stashPopResult.out);
            out.append("\n");
            if (StringUtils.isNotEmpty(stashPopResult.err)) {
                out.append(stashPopResult.err).append("\n");
            }
        }
        invalidateStatusCache();
        checkExecutionResult(pullResult);
        if (stashPopResult != null) {
            checkExecutionResult(stashPopResult);
        }

        return out.toString();
    }

    @Override
    public Map<String, String> getTagInfos(String uri) throws IOException {
        Map<String, String> infos = new LinkedHashMap<>();
        ExecutionResult result = executeCommand(executable, new String[]{"ls-remote", "--tags", uri});
        List<String> lines = readLines(result.out);
        Collections.reverse(lines);
        for (String line : lines) {
            String tag = StringUtils.substringAfter(line, "refs/tags/");
            if (!tag.endsWith("^{}")) {
                infos.put(tag, "scm:git:" + uri);
            }
        }
        return infos;
    }

    @Override
    public Map<String, String> getBranchInfos(String uri) throws IOException {
        Map<String, String> infos = new LinkedHashMap<>();
        ExecutionResult result = executeCommand(executable, new String[]{"ls-remote", "--heads", uri});
        List<String> lines = readLines(result.out);
        Collections.reverse(lines);
        for (String line : lines) {
            String tag = StringUtils.substringAfter(line, "refs/heads/");
            infos.put(tag, "scm:git:" + uri);
        }
        return infos;
    }
}
