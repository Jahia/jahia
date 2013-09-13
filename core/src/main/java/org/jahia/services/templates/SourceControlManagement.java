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
import org.jahia.utils.ProcessHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class SourceControlManagement {

    protected File rootFolder;

    protected Map<String, Status> statusMap;

    protected ExecutionResult executeCommand(String command, String arguments) throws IOException {
        try {
            StringBuilder resultOut = new StringBuilder();
            StringBuilder resultErr = new StringBuilder();
            int res = ProcessHelper.execute(command, arguments, null, rootFolder, resultOut, resultErr);
            return new ExecutionResult(res, resultOut.toString(), resultErr.toString());
        } catch (Exception e) {
            throw new IOException("Failed to execute command " + command + (arguments != null ? (" " + arguments) : ""), e);
        }
    }

    class ExecutionResult {
        int exitValue;
        String out;
        String err;

        ExecutionResult(int exitValue, String out, String err) {
            this.exitValue = exitValue;
            this.out = out;
            this.err = err;
        }
    }

    public enum Status {
        UNMODIFIED, MODIFIED, ADDED, DELETED, RENAMED, COPIED, UNMERGED, UNTRACKED
    }


    public File getRootFolder() {
        return rootFolder;
    }

    protected abstract void initWithEmptyFolder(File workingDirectory, String url) throws IOException;

    protected abstract void initWithWorkingDirectory(File workingDirectory) throws IOException;

    protected abstract void initFromURI(File workingDirectory, String uri, String branchOrTag) throws IOException;

    public abstract String getURI() throws IOException;

    public abstract void setModifiedFile(List<File> files) throws IOException;

    public abstract void setRemovedFile(File file) throws IOException;

    public abstract void setMovedFile(File src, File dst) throws IOException;

    public abstract void update() throws IOException;

    public abstract void commit(String message) throws IOException;

    public abstract void markConflictAsResolved(File file) throws IOException;

    public Status getStatus(String path) throws IOException {
        if (getStatusMap().containsKey(path)) {
            return getStatusMap().get(path);
        } else {
            String[] pathSegments = path.split("/");
            String subPath = "";
            for (String segment : pathSegments) {
                if (getStatusMap().containsKey(subPath) && getStatusMap().get(subPath) == Status.UNTRACKED) {
                    return Status.UNTRACKED;
                }
                if (subPath.isEmpty()) {
                    subPath = segment;
                } else {
                    subPath += "/" + segment;
                }
            }
        }
        return Status.UNMODIFIED;
    }

    protected abstract Map<String, Status> getStatusMap() throws IOException;

    public void invalidateStatusCache() {
        statusMap = null;
    }

    protected void checkExecutionResult(ExecutionResult result) throws IOException {
        if (result.exitValue != 0 || result.out.contains("conflicts")) {
            String message = result.err;
            if (StringUtils.isBlank(message)) {
                message = result.out;
            }
            throw new IOException(message);
        }
    }
}
