/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.osgi;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.modulemanager.spi.ConfigService;
import org.jahia.settings.SettingsBean;
import org.junit.Test;
import org.osgi.service.cm.ConfigurationAdmin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ConfigTest {

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

        configService.setAutoSave(false);

        deployConfig("test-user-modified.cfg", "test-user.cfg");
        deployConfig("test-default-modified.cfg","test-default.cfg");
        deployConfig("test-module-modified.cfg","test-module.cfg");

        checkFileContent("test-user-modified.cfg", "test-user.cfg");
        checkFileContent("test-default-modified.cfg","test-default.cfg");
        checkFileContent("test-module-modified.cfg","test-module.cfg");

        configService.restoreConfigurations(Arrays.asList(ConfigService.ConfigType.MODULE_DEFAULT, ConfigService.ConfigType.USER));

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
                e.printStackTrace();
            }
            assertEquals(r,content);
            return null;
        });
    }

    private void checkFileContent(String resource, String configName) throws IOException {
        String resourceContent = IOUtils.toString(getClass().getClassLoader().getResource("org/jahia/test/osgi/" + resource).openStream());
        String fileContent = FileUtils.readFileToString(new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/" + configName));
        assertEquals(resourceContent, fileContent);
    }

    private ConfigService.ConfigType checkType(String file, ConfigService.ConfigType expected) {
        ConfigService.ConfigType type = configService.getAllConfigurations().get(file);
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
