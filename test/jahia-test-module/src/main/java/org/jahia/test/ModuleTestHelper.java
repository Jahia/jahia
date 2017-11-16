/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState.State;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.modulemanager.ModuleManager;
import org.jahia.services.modulemanager.OperationResult;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.utils.PomUtils;
import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.pax.url.mvn.internal.AetherBasedResolver;
import org.ops4j.pax.url.mvn.internal.config.MavenConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.sun.star.uno.RuntimeException;

import shaded.org.apache.maven.settings.Profile;
import shaded.org.apache.maven.settings.Repository;
import shaded.org.apache.maven.settings.RepositoryPolicy;
import shaded.org.apache.maven.settings.Settings;
import shaded.org.ops4j.util.property.PropertiesPropertyResolver;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * A helper class to retrieve module files from maven.
 *
 * @author Christophe Laprun
 */
public class ModuleTestHelper {

    private static final Logger logger = LoggerFactory.getLogger(ModuleTestHelper.class);

    private static JahiaTemplateManagerService managerService;

    private static ModuleManager moduleManager;

    private static final MavenResolver resolver;

    static {
        managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        moduleManager = ModuleUtils.getModuleManager();
        Settings settings = new Settings();
        Profile jahiaProfile = new Profile();
        jahiaProfile.setId("jahia");
        Repository jahia = new Repository();
        jahia.setId("jahia-public");
        jahia.setUrl("https://devtools.jahia.com/nexus/content/groups/public");
        jahiaProfile.addRepository(jahia);

        jahia = new Repository();
        jahia.setId("jahia-snapshots");
        jahia.setUrl("https://devtools.jahia.com/nexus/content/repositories/jahia-snapshots/");
        final RepositoryPolicy snapshots = new RepositoryPolicy();
        snapshots.setEnabled(true);
        jahia.setSnapshots(snapshots);
        jahiaProfile.addRepository(jahia);

        settings.addProfile(jahiaProfile);
        settings.addActiveProfile("jahia");

        final MavenConfigurationImpl configuration = new MavenConfigurationImpl(new PropertiesPropertyResolver(new Properties()), ServiceConstants.PID);
        settings.setLocalRepository(configuration.getSettings().getLocalRepository());
        configuration.setSettings(settings);
        resolver = new AetherBasedResolver(configuration);
    }

    public static File getModuleFromMaven(String groupId, String artifactId) throws IOException {
        return getModuleFromMaven(groupId, artifactId, null);
    }

    public static File getModuleFromMaven(String groupId, String artifactId, String version) throws IOException {
        if(version == null ||  version.trim().isEmpty()) {
            version = "LATEST";
        }
        return resolver.resolve(groupId, artifactId, "", "jar", version);
    }

    private static Dependency getArtifactInfo(String artifactId) {
        try {
            Model model = PomUtils.read(ModuleTestHelper.class.getClassLoader()
                    .getResourceAsStream("META-INF/maven/org.jahia.test/jahia-test-module/pom.xml"));
            for (Dependency dep : model.getDependencies()) {
                if (artifactId.equals(dep.getArtifactId())) {
                    return dep;
                }
            }
        } catch (IOException | XmlPullParserException e) {
            logger.error("Unable to read module information from pom.xml file. Cause: " + e.getMessage(), e);
            throw new RuntimeException("Unable to read module information from pom.xml file. Cause: " + e.getMessage(),
                    e);
        }
        return null;
    }

    public static void ensureModuleStarted(String artifactId) {
        JahiaTemplatesPackage pkg = managerService.getTemplatePackageById(artifactId);
        if (pkg != null) {
            State state = pkg.getState().getState();
            if (state != State.STARTED) {
                if (state == State.INSTALLED || state == State.RESOLVED) {
                    // start the module
                    moduleManager.start(pkg.getBundleKey(), null);
                } else {
                    throw new RuntimeException(
                            "Module " + artifactId + " is in the state " + state + " and cannot be started");
                }
            }
        } else {
            logger.info("Module {} is not deployed. Retrieving required version information from pom.xml file",
                    artifactId);
            Dependency info = getArtifactInfo(artifactId);
            if (info == null) {
                throw new RuntimeException("Unable to find version information for module " + artifactId
                        + " in the pom.xml of the jahia-test-module project");
            }

            logger.info("Resolved module artifact information: {}. Resolving corresponding Maven artifact", info);
            try {
                File moduleFile = getModuleFromMaven(info.getGroupId(), info.getArtifactId(), info.getVersion());

                logger.info("Module Maven artifact resolved to file: {}. Installing and starting module", moduleFile);

                OperationResult opResult = moduleManager.install(new FileSystemResource(moduleFile), null, true);

                logger.info("Module {} has been installed and started with status {}", artifactId, opResult);
            } catch (IOException e) {
                throw new RuntimeException("Unable to resolve maven artifact for module " + info);
            }
        }
    }
}
