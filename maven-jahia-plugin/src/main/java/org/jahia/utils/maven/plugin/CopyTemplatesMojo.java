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

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jul 23, 2008
 * Time: 10:31:17 AM
 * To change this template use File | Settings | File Templates.
 * @goal copy-templates
 * @requiresDependencyResolution runtime
 */
public class CopyTemplatesMojo extends AbstractMojo {


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
        Set dependencyFiles = project.getDependencyArtifacts();
        Set<Artifact> dependenciesToRemove = new HashSet<Artifact>();
        for (Artifact dependencyFile : (Iterable<Artifact>) dependencyFiles) {
            if (dependencyFile.getGroupId().equals("org.jahia.templates")) {
                try {
                    FileUtils.copyFileToDirectory(dependencyFile.getFile(), new File(output,"jahia/WEB-INF/var/shared_templates"));
                    getLog().info("Copy templates jar "+dependencyFile.getFile().getName() + " to shared templates");
                    dependenciesToRemove.add(dependencyFile);
                } catch (IOException e) {
                    getLog().error("Error when copying file");
                }
            }
        }
        for (Artifact dependencyFile : dependenciesToRemove) {
            dependencyFiles.remove(dependencyFile);
        }
    }
}
