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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SvnSourceControlManagement extends SourceControlManagement {

    @Override
    protected void initWithEmptyFolder(File workingDirectory, String url) throws IOException {
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void initWithWorkingDirectory(File workingDirectory) throws IOException {
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void initFromURI(File workingDirectory, String uri, String branchOrTag) throws IOException {
        this.rootFolder = workingDirectory.getParentFile();
        executeCommand("svn", "checkout " + uri + " " + workingDirectory.getName());
        this.rootFolder = workingDirectory;
    }

    @Override
    public String getURI() throws IOException {
        ExecutionResult result = executeCommand("svn", "info");
        String url = (String) CollectionUtils.find(Arrays.asList(result.out.split("\n")), new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return object.toString().startsWith("URL:");
            }
        });
        if (url != null) {
            url = StringUtils.substringAfter(url,"URL:").trim();
        }
        return "scm:svn:"+url;
    }

    @Override
    public void setModifiedFile(List<File> files) throws IOException {
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
        executeCommand("svn", StringUtils.join(args, ' '));
    }

    @Override
    public void update() throws IOException {
        executeCommand("svn", "update");
    }

    @Override
    public void commit(String message) throws IOException {
        executeCommand("svn", "commit -m \"" + message + "\"");
    }

    @Override
    protected Map<String, Status> getStatusMap() throws IOException {
        if (statusMap == null) {
            synchronized (SvnSourceControlManagement.class) {
                if (statusMap == null) {
                    Map<String, Status> newMap = new HashMap<String, Status>();
                    ExecutionResult result = executeCommand("svn", "status");
                    for (String line : result.out.split("\n")) {
                        if (StringUtils.isBlank(line)) {
                            continue;
                        }
                        String path = line.substring(8);
                        String combinedStatus = line.substring(0, 7);
                        char firstColumn = combinedStatus.charAt(0);
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
                            newMap.put(path, status);
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
                    statusMap = newMap;
                }
            }
        }
        return statusMap;
    }
}
