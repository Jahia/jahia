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

import org.ops4j.pax.url.mvn.MavenResolver;
import org.ops4j.pax.url.mvn.ServiceConstants;
import org.ops4j.pax.url.mvn.internal.AetherBasedResolver;
import org.ops4j.pax.url.mvn.internal.config.MavenConfigurationImpl;
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

    private static final MavenResolver resolver;

    static {
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

}
