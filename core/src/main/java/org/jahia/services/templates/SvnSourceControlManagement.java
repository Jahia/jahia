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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.utils.xml.XMLParser;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * SVN based source control management service.
 */
public class SvnSourceControlManagement extends SourceControlManagement {

    private static final Predicate URL_PREDICATE = new Predicate() {
        @Override
        public boolean evaluate(Object object) {
            return object.toString().startsWith("URL:");
        }
    };
    
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
    public void commit(String message) throws IOException {
        invalidateStatusCache();
        checkExecutionResult(executeCommand(executable, new String[]{"commit","-m",message}));
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
            if (firstColumn == 'A') {
                status = Status.ADDED;
            } else if (firstColumn == 'C') {
                status = Status.UNMERGED;
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
        ExecutionResult r = executeCommand(executable, new String[]{"checkout ", uri ,workingDirectory.getName()});
        if (r.exitValue > 0) {
            throw new IOException(r.err);
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
        executeCommand(executable, new String[]{"commit","-m","First commit"});
        if (r.exitValue > 0) {
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
        executeCommand(executable, args.toArray(new String[args.size()]));
        invalidateStatusCache();
    }

    @Override
    public void update() throws IOException {
        invalidateStatusCache();
        checkExecutionResult(executeCommand(executable, new String[]{"update","--non-interactive"}));
    }
    
}
