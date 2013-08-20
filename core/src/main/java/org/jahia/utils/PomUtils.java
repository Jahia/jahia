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
package org.jahia.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;

import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Scm;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.tika.io.IOUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * Utility class for reading and manipulating Maven project files (pom.xml).
 * 
 * @author Sergiy Shyrkov
 */
public final class PomUtils {

    /**
     * Reads the artifact version from the provided pom.xml file. If the artifact version is not specified, takes the version of the parent,
     * if present. Otherwise returns <code>null</code>.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to read
     * @return the artifact version from the provided pom.xml file. If the artifact version is not specified, takes the version of the
     *         parent, if present. Otherwise returns <code>null</code>
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static String getVersion(File pomXmlFile) throws IOException, XmlPullParserException {
        return getVersion(read(pomXmlFile));
    }

    /**
     * Gets the artifact version from the provided model. If the artifact version is not specified, takes the version of the parent, if
     * present. Otherwise returns <code>null</code>.
     * 
     * @param model
     *            the Maven project model
     * @return the artifact version from the provided model. If the artifact version is not specified, takes the version of the parent, if
     *         present. Otherwise returns <code>null</code>
     */
    public static String getVersion(Model model) {
        String version = model.getVersion();
        if (version == null && model.getParent() != null) {
            version = model.getParent().getVersion();
        }

        return version;
    }

    /**
     * Parses the Maven project model from the specified pom.xml file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor to read model from
     * @return the Maven project model from the specified pom.xml file
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static Model read(File pomXmlFile) throws IOException, XmlPullParserException {
        Reader reader = new FileReader(pomXmlFile);
        try {
            return new MavenXpp3Reader().read(reader);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Updates the distribution management repository in the specified Maven project file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param repositoryId
     *            the server ID for the distribution repository
     * @param repositoryUrl
     *            the URL of the target distribution repository
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateDistributionManagement(File pomXmlFile, String repositoryId, String repositoryUrl)
            throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        DistributionManagement dm = model.getDistributionManagement();
        if (dm == null) {
            dm = new DistributionManagement();
            model.setDistributionManagement(dm);
        }
        DeploymentRepository repo = dm.getRepository();
        if (repo == null) {
            repo = new DeploymentRepository();
            dm.setRepository(repo);
        }
        repo.setId(repositoryId);
        repo.setUrl(repositoryUrl);

        write(model, pomXmlFile);
    }

    public static void updateForgeUrl(File pomXmlFile, String forgeUrl)
            throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        model.getProperties().put("jahia-forge", forgeUrl);
        write(model, pomXmlFile);
    }

    /**
     * Updates the SCM connection URL in the specified Maven project file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param scmUrl
     *            the SCM connection URL to set
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateScm(File pomXmlFile, String scmUrl) throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        Scm scm = model.getScm();
        if (scm == null) {
            scm = new Scm();
            model.setScm(scm);
        }
        scm.setConnection(scmUrl);
        scm.setDeveloperConnection(scmUrl);

        write(model, pomXmlFile);
    }

    /**
     * Updates the project version in the specified Maven project file.
     * 
     * @param pomXmlFile
     *            the Maven project descriptor file to update
     * @param version
     *            the new version to set
     * @throws IOException
     *             in case of a reading problem
     * @throws XmlPullParserException
     *             in case of a parsing error
     */
    public static void updateVersion(File pomXmlFile, String version) throws IOException, XmlPullParserException {
        Model model = read(pomXmlFile);
        model.setVersion(version);
        write(model, pomXmlFile);
    }

    /**
     * Serializes Maven project model into a specified file.
     * 
     * @param model
     *            the Maven project model to serialize
     * @param targetPomXmlFile
     *            the target file to write the provided model into
     * @throws IOException
     *             in case of a serialization error
     */
    public static void write(Model model, File targetPomXmlFile) throws IOException {
        MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(targetPomXmlFile));
            xpp3Writer.write(os, model);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private PomUtils() {
        super();
    }

}
