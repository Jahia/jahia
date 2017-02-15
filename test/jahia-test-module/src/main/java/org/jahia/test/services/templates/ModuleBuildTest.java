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
package org.jahia.test.services.templates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.jcr.RepositoryException;

import org.apache.commons.io.FileUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleBuildHelper;
import org.jahia.settings.SettingsBean;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test cases for the {@link ModuleBuildHelper}.
 * 
 * @author Sergiy Shyrkov
 */
public class ModuleBuildTest {
    private static Logger logger = LoggerFactory.getLogger(ModuleBuildTest.class);

    private static JahiaTemplateManagerService templateManagerService;

    private static boolean mavenAvailable;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        mavenAvailable = SettingsBean.getInstance().isMavenExecutableSet();
        if (!mavenAvailable) {
            logger.warn("Maven executable is not set. The module creation test will be skipped.");
            return;
        }
        templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        mavenAvailable = false;
        templateManagerService = null;
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testCreate() throws RepositoryException, IOException {
        if (!mavenAvailable) {
            return;
        }

        final File sources = Files.createTempDirectory("module-sources").toFile();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        Assert.assertNotNull("Module creation failed", templateManagerService.createModule(
                                "Test module A", "test-module-a", "org.jahia.modules", "module", sources, session));

                        templateManagerService.undeployModule("test-module-a", "1.0-SNAPSHOT");
                    } catch (IOException | BundleException e) {
                        logger.error(e.getMessage(), e);
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            });
        } finally {
            FileUtils.deleteQuietly(sources);
        }
    }
}
