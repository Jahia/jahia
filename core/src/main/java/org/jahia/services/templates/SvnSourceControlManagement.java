/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * SVN based source control management service.
 */
public class SvnSourceControlManagement extends SourceControlManagement {

    /**
     * Initializes an instance of this class.
     * @param executable the SVN executable
     */
    public SvnSourceControlManagement(String executable) {
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
        args.add("--parents");
        for (File file : files) {
            if(file.getName().equals(".gitignore")) {
                List<String> ignoreCmd = new ArrayList<String>();
                ignoreCmd.add("propset");
                ignoreCmd.add("svn:ignore");
                ignoreCmd.add("-F");
                ignoreCmd.add(file.getAbsolutePath());
                ignoreCmd.add(".");
                executeCommand(executable, ignoreCmd.toArray(new String[ignoreCmd.size()]));
            }
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
        boolean commitRequired = checkCommit();
        if (commitRequired) {
            checkExecutionResult(executeCommand(executable, new String[]{"commit","-m",message}));
        }
        invalidateStatusCache();
        return commitRequired;
    }

    @Override
    protected Map<String, Status> createStatusMap() throws IOException {
        Map<String, Status> newMap = new HashMap<String, Status>();
        ExecutionResult result = executeCommand(executable, new String[]{"status"});
        for (String line : readLines(result.out)) {
            if (StringUtils.isBlank(line)) {
                continue;
            }
            String path = line.substring(8);
            char firstColumn = line.charAt(0);
            Status status = null;
            if (firstColumn == 'C' || line.charAt(1) == 'C' || line.charAt(6) == 'C') {
                status = Status.UNMERGED;
            } else if (firstColumn == 'A') {
                status = Status.ADDED;
            } else if (firstColumn == 'D' || firstColumn == '!') {
                status = Status.DELETED;
            } else if (firstColumn == 'M') {
                status = Status.MODIFIED;
            } else if (firstColumn == '?') {
                status = Status.UNTRACKED;
            }
            if (status != null) {
                if (!path.startsWith("/")) {
                    path = "/" + path;
                }
                path = FilenameUtils.separatorsToUnix(path);
                newMap.put(path, status);
                String[] pathSegments = StringUtils.split(path, '/');
                StringBuilder subPath = new StringBuilder(64);
                for (String segment : pathSegments) {
                    newMap.put(subPath.length() == 0 ? "/" : subPath.toString(), Status.MODIFIED);
                    subPath.append('/');
                    subPath.append(segment);
                }
            }
        }
        return newMap;
    }

    @Override
    public String getURI() throws IOException {
        ExecutionResult result = executeCommand(executable, new String[]{"info", "--xml"});
        String url = StringUtils.substringBetween(result.out,"<url>","</url>").trim();
        return "scm:svn:"+url;
    }

    @Override
    protected void getFromSCM(File workingDirectory, String uri, String branchOrTag) throws IOException {
        this.rootFolder = workingDirectory.getParentFile();
        ExecutionResult r = executeCommand(executable, new String[]{"checkout ", uri, workingDirectory.getName()});
        if (r.exitValue > 0) {
            throw new SourceControlException(r.err);
        }
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void sendToSCM(File workingDirectory, String uri) throws IOException {
        this.rootFolder = workingDirectory;
        ExecutionResult r = executeCommand(executable, new String[]{"checkout ", uri ,"."});

        File gitIgnore = new File(workingDirectory, ".gitignore");
        if (gitIgnore.exists()) {
            String ignorepath = gitIgnore.getAbsolutePath();
            executeCommand(executable, new String[]{"propset", "svn:ignore", "-F", ignorepath, "."});
            gitIgnore.delete();
        }
        executeCommand(executable, new String[]{"add","src"});
        executeCommand(executable, new String[]{"add","pom.xml"});
        executeCommand(executable, new String[]{"commit","-m","Initial commit"});
        if (r.exitValue > 0) {
            File svnDir = new File(workingDirectory.getPath() + "/.svn");
            if (svnDir.exists()) {
                FileUtils.deleteDirectory(svnDir);
            }
            throw new IOException(r.err);
        }
    }

    @Override
    protected void initWithWorkingDirectory(File workingDirectory) throws IOException {
        this.rootFolder = workingDirectory;
    }

    @Override
    public void markConflictAsResolved(File file) throws IOException {
        if (file == null) {
            return;
        }

        String rootPath = rootFolder.getPath();

        List<String> args = new ArrayList<String>();
        args.add("resolve");
        args.add("--accept=working");
        if (file.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(file.getPath().substring(rootPath.length() + 1));
        }
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public void move(File src, File dst) throws IOException {
        if (src == null || dst == null) {
            return;
        }

        String rootPath = rootFolder.getPath();

        List<String> args = new ArrayList<String>();
        args.add("move");
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
        args.add("remove");
        args.add("--force");
        if (file.getPath().equals(rootPath)) {
            args.add(".");
        } else {
            args.add(file.getPath().substring(rootPath.length() + 1));
        }
        try {
            executeCommand(executable, args.toArray(new String[args.size()]));
        } finally {
            invalidateStatusCache();
        }
    }

    @Override
    public String update() throws IOException {
        try {
            ExecutionResult result = executeCommand(executable, new String[]{"update","--non-interactive"});
            checkExecutionResult(result);
            return result.out;
        } finally {
            invalidateStatusCache();
        }
    }

    @Override
    public Map<String, String> getTagInfos(String uri) throws IOException {
        String separator = "/trunk";
        Iterator<String> it = Arrays.asList("/branches", "/tags").iterator();
        while (!StringUtils.contains(uri, separator) && it.hasNext()) {
            separator = it.next();
        }
        String base = StringUtils.substringBeforeLast(uri, separator) + "/tags/";
        String path = StringUtils.substringAfterLast(uri, separator + "/");
        if (!separator.equals("/trunk")) {
            path = StringUtils.substringAfter(path, "/");
        }
        Map<String, String> infos = new LinkedHashMap<>();
        ExecutionResult result = executeCommand(executable, new String[]{"list", base});
        List<String> lines = readLines(result.out);
        Collections.reverse(lines);
        for (String line : lines) {
            String tag = StringUtils.removeEnd(line, "/");
            infos.put(tag, "scm:svn:" + base + tag + (path.length() > 0 ? "/" + path : ""));
        }
        return infos;
    }

    @Override
    public Map<String, String> getBranchInfos(String uri) throws IOException {
        String separator = "/trunk";
        Iterator<String> it = Arrays.asList("/branches", "/tags").iterator();
        while (!StringUtils.contains(uri, separator) && it.hasNext()) {
            separator = it.next();
        }
        String base = StringUtils.substringBeforeLast(uri, separator) + "/branches/";
        String path = StringUtils.substringAfterLast(uri, separator + "/");
        if (!separator.equals("/trunk")) {
            path = StringUtils.substringAfter(path, "/");
        }
        Map<String, String> infos = new LinkedHashMap<>();
        ExecutionResult result = executeCommand(executable, new String[]{"list", base});
        List<String> lines = readLines(result.out);
        Collections.reverse(lines);
        infos.put("trunk", uri);
        for (String line : lines) {
            String branch = StringUtils.removeEnd(line, "/");
            infos.put(branch, "scm:svn:" + base + branch + (path.length() > 0 ? "/" + path : ""));
        }
        return infos;
    }
}
