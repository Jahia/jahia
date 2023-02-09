/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
                                "Test module A", "test-module-a", "org.jahia.test.modules", "module", sources, session));

                        templateManagerService.undeployModule("test-module-a", "1.0.0-SNAPSHOT");
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
