/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.osgi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.modulemanager.spi.ConfigService;
import org.jahia.settings.SettingsBean;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ConfigTest {
    private static final Logger logger = LoggerFactory.getLogger(ConfigTest.class);
    private ConfigurationAdmin configAdmin = BundleUtils.getOsgiService(ConfigurationAdmin.class, null);
    private ConfigService configService = BundleUtils.getOsgiService(ConfigService.class, null);

    @Test
    public void testConfigTypes() throws Exception {
        deployConfig("test-user.cfg");
        deployConfig("test-default.cfg");
        deployConfig("test-module.cfg");

        Thread.sleep(5000);

        ConfigService.ConfigType type = checkType("test-default.cfg", ConfigService.ConfigType.MODULE_DEFAULT);

        assertEquals(ConfigService.ConfigType.MODULE_DEFAULT, type);
        assertEquals(ConfigService.ConfigType.MODULE_DEFAULT, type);
    }

    @Test
    public void testStoreConfig() throws Exception {
        deployConfig("test-user.cfg");
        deployConfig("test-default.cfg");
        deployConfig("test-module.cfg");

        Thread.sleep(5000);

        checkJCRContent("test-user.cfg");
        checkType("test-user.cfg", ConfigService.ConfigType.USER);

        checkJCRContent("test-default.cfg");
        checkType("test-default.cfg", ConfigService.ConfigType.MODULE_DEFAULT);

        checkJCRContent("test-module.cfg");
        checkType("test-module.cfg", ConfigService.ConfigType.MODULE);

    }

    @Test
    public void testRestoreConfig() throws Exception {
        deployConfig("test-user.cfg");
        deployConfig("test-default.cfg");
        deployConfig("test-module.cfg");

        Thread.sleep(5000);

        configService.setAutoSaveToJCR(false);

        deployConfig("test-user-modified.cfg", "test-user.cfg");
        deployConfig("test-default-modified.cfg","test-default.cfg");
        deployConfig("test-module-modified.cfg","test-module.cfg");

        checkFileContent("test-user-modified.cfg", "test-user.cfg");
        checkFileContent("test-default-modified.cfg","test-default.cfg");
        checkFileContent("test-module-modified.cfg","test-module.cfg");

        configService.restoreConfigurationsFromJCR(Arrays.asList(ConfigService.ConfigType.MODULE_DEFAULT, ConfigService.ConfigType.USER));

        checkFileContent("test-user.cfg", "test-user.cfg");
        checkFileContent("test-default.cfg","test-default.cfg");
        checkFileContent("test-module-modified.cfg","test-module.cfg");
    }

    private void checkJCRContent(final String configName) throws Exception {
        checkJCRContent(configName, configName);
    }

    private void checkJCRContent(final String resource, final String configName) throws Exception {
        JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
            String content = session.getNode("/module-management/configs/" + configName + "/jcr:content").getProperty("jcr:data").getString();
            String r = null;
            try {
                r = IOUtils.toString(getClass().getClassLoader().getResource("org/jahia/test/osgi/" + resource).openStream());
            } catch (IOException e) {
                logger.warn("Error getting resource", e);
            }
            assertEquals(r, content);
            return null;
        });
    }

    private void checkFileContent(String resource, String configName) throws IOException {
        String resourceContent = IOUtils.toString(getClass().getClassLoader().getResource("org/jahia/test/osgi/" + resource).openStream());
        String fileContent = FileUtils.readFileToString(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/" + configName));
        assertEquals(resourceContent, fileContent);
    }

    private ConfigService.ConfigType checkType(String file, ConfigService.ConfigType expected) {
        ConfigService.ConfigType type = configService.getAllConfigurationTypes().get(file);
        assertNotNull(type);
        assertEquals(expected, type);
        return type;
    }

    private void deployConfig(String config) throws Exception {
        deployConfig(config, config);
    }

    private void deployConfig(String source, String target) throws Exception {
        File folder = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc");
        File test = new File(folder, target);
        URL resource = getClass().getClassLoader().getResource("org/jahia/test/osgi/" + source);
        try (InputStream is = resource.openStream()) {
            FileUtils.copyInputStreamToFile(is, test);
        }
    }

}
