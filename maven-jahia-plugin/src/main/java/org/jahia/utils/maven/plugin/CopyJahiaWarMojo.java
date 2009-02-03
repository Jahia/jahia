/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.Artifact;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;

/**
 * Created by IntelliJ IDEA.
 * User: islam
 * Date: 5 ao�t 2008
 * Time: 14:49:54
 * To change this template use File | Settings | File Templates.
 * @goal copy-jahiawar
 * @requiresDependencyResolution runtime
 */
public class CopyJahiaWarMojo extends AbstractMojo {


    /**
     * @parameter default-value="${project.build.directory}"
     */
    protected File output;

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    public void execute() throws MojoExecutionException, MojoFailureException {
        for (Artifact dependencyFile : (Iterable<Artifact>) project.getDependencyArtifacts()) {
            if ("org.jahia.server".equals(dependencyFile.getGroupId())
                    && "jahia-war".equals(dependencyFile.getArtifactId())) {
                try {
                    File webappDir = new File(output,"config/WEB-INF/jahia");
                    ZipInputStream z = new ZipInputStream(
                            new FileInputStream(dependencyFile.getFile()));
                    ZipEntry entry;
                    int cnt = 0;
                    while ((entry = z.getNextEntry()) != null) {
                        if (!entry.isDirectory()) {
                            File target = new File(webappDir, entry.getName());

                            if (entry.getTime() > target.lastModified()) {

                                target.getParentFile().mkdirs();
                                FileOutputStream fileOutputStream = new FileOutputStream(
                                        target);
                                IOUtils.copy(z, fileOutputStream);
                                fileOutputStream.close();
                                cnt++;
                            }
                        }
                    }
                    z.close();
                    getLog().info("Copied " + cnt + " files.");
                } catch (IOException e) {
                    getLog().error("Error when copying file");
                }
            }
        }
    }
}