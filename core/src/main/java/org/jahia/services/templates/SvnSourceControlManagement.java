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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class SvnSourceControlManagement extends SourceControlManagement {

    @Override
    protected void initWithEmptyFolder(File workingDirectory, String url) throws Exception {
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void initWithWorkingDirectory(File workingDirectory) throws Exception {
        this.rootFolder = workingDirectory;
    }

    @Override
    protected void initFromURI(File workingDirectory, String uri) throws Exception {
        this.rootFolder = workingDirectory.getParentFile();
        executeCommand("svn", "checkout " + uri + " " + workingDirectory.getName());
        this.rootFolder = workingDirectory;
    }

    @Override
    public void setModifiedFile(List<File> files) {
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
    public void update() {
        executeCommand("svn", "update");
    }

    @Override
    public void commit(String message) {
        executeCommand("svn", "commit -m \"" + message + "\"");
    }
}
