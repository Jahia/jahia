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

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Git based source control management service.
 * 
 * @author Thomas Draier
 */
public class GitSourceControlManagement extends SourceControlManagement {

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
    public void commit(String message) throws IOException {
        invalidateStatusCache();
        Map<String, Status> statusMap = createStatusMap(false);
        boolean commitRequired = statusMap.values().contains(Status.MODIFIED) || statusMap.values().contains(Status.ADDED)
                || statusMap.values().contains(Status.DELETED) || statusMap.values().contains(Status.RENAMED)
                || statusMap.values().contains(Status.COPIED) || statusMap.values().contains(Status.UNMERGED);
        if (commitRequired) {
            checkExecutionResult(executeCommand(executable, new String[]{"commit","-a","-m","\"" + message + "\""}));
            checkExecutionResult(executeCommand(executable, new String[]{"push","-u","origin","master"}));
        }
    }

    @Override
    protected Map<String, Status> createStatusMap() throws IOException {
        return createStatusMap(true);
    }

    private Map<String, Status> createStatusMap(boolean folder) throws IOException {
        Map<String, Status> newMap = new HashMap<String, Status>();
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
            String combinedStatus = line.substring(0, 2);
            char indexStatus = combinedStatus.charAt(0);
            char workTreeStatus = combinedStatus.charAt(1);
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
                newMap.put(path, status);
                if (folder) {
                    String[] pathSegments = path.split("/");
                    String subPath = "";
                    for (String segment : pathSegments) {
                        newMap.put(subPath, Status.MODIFIED);
                        if (subPath.isEmpty()) {
                            subPath = segment;
                        } else {
                            subPath += "/" + segment;
                        }
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

    protected void initFromURI(File workingDirectory, String uri, String branchOrTag) throws IOException {
        this.rootFolder = workingDirectory.getParentFile();
        ExecutionResult r = executeCommand(executable, new String[]{"clone",uri,workingDirectory.getName()});
        if (r.exitValue > 0) {
            throw new IOException(r.err);
        }
        this.rootFolder = workingDirectory;
        if (!StringUtils.isEmpty(branchOrTag)) {
            executeCommand(executable, new String[]{"checkout ",branchOrTag});
        }
        this.rootFolder = workingDirectory;
    }

    protected void initWithEmptyFolder(File workingDirectory, String url) throws IOException {
        this.rootFolder = workingDirectory;
        executeCommand(executable, new String[]{"init"});
        executeCommand(executable, new String[]{"add","."});
        executeCommand(executable, new String[]{"commit","-a","-m","First commit"});
        executeCommand(executable, new String[]{"remote","add","origin",url});
        executeCommand(executable, new String[]{"fetch"});
        executeCommand(executable, new String[]{"merge","origin/master"});
        executeCommand(executable, new String[]{"push","-u","origin","master"});
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
    public void update() throws IOException {
        executeCommand(executable, new String[]{"stash","clear"});

        Map<String, Status> statusMap = createStatusMap(false);
        boolean stashRequired = statusMap.values().contains(Status.MODIFIED) || statusMap.values().contains(Status.ADDED)
                || statusMap.values().contains(Status.DELETED) || statusMap.values().contains(Status.RENAMED)
                || statusMap.values().contains(Status.COPIED) || statusMap.values().contains(Status.UNMERGED);
        if (stashRequired) {
            executeCommand(executable, new String[]{"stash"});
        }
        ExecutionResult pullResult = executeCommand(executable, new String[]{"pull","--rebase"});
        ExecutionResult stashPopResult = null;
        if (stashRequired) {
            stashPopResult = executeCommand(executable, new String[]{"stash","pop"});
        }
        invalidateStatusCache();
        checkExecutionResult(pullResult);
        if (stashPopResult != null) {
            checkExecutionResult(stashPopResult);
        }
    }
}
