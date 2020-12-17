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
package org.jahia.bundles.config.impl;

import org.apache.commons.lang.StringUtils;
import org.jahia.bundles.config.Settings;
import org.jahia.bundles.config.SettingsService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Component(service = SettingsService.class)
public class SettingsServiceImpl implements SettingsService {

    private static final String FELIX_FILEINSTALL_FILENAME = "felix.fileinstall.filename";

    private ConfigurationAdmin configurationAdmin;

    public Settings getSettings(String pid) throws IOException {
        Configuration configuration = configurationAdmin.getConfiguration(pid);
        if (configuration.getBundleLocation() != null && configuration.getBundleLocation().contains("org.jahia.bundles.config.manager")) {
            configuration.setBundleLocation(null);
        }
        return new SettingsImpl(configuration, null);
    }

    public Settings getSettings(String factoryPid, String identifier) throws IOException  {
        try {
            Configuration[] configurations = configurationAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    Object filename = configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME);
                    if (filename != null && filename.toString().endsWith("/" + factoryPid + "-" + identifier + ".cfg")) {
                        return new SettingsImpl(configuration, identifier);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            // not possible
        }

        Configuration result = configurationAdmin.createFactoryConfiguration(factoryPid, "?");
        return new SettingsImpl(result, identifier);
    }

    public Settings getSettings(Configuration configuration) {
        String identifier = null;
        if (configuration.getFactoryPid() != null && configuration.getProperties() != null) {
            Object filename = configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME);
            if (filename != null) {
                identifier = StringUtils.substringBetween(filename.toString(), configuration.getFactoryPid() + "-", ".cfg");
            }
        }
        return new SettingsImpl(configuration, identifier);
    }

    public void storeSettings(Settings settings) throws IOException {
        Configuration configuration = settings.getConfiguration();

        // refresh and save settings
        if (configuration.getProperties() == null) {
            @SuppressWarnings("java:S1149") Dictionary<String, Object> properties = new Hashtable<>();
            File file = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/" + (settings.getIdentifier() != null ? (configuration.getFactoryPid() + "-" + settings.getIdentifier()) : (configuration.getPid())) + ".cfg");
            properties.put(FELIX_FILEINSTALL_FILENAME, file.toURI().toString());

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                setProperties(settings, properties, bw);
                configuration.update(properties);
            }
        } else {
            Dictionary<String, Object> properties = configuration.getProperties();
            settings.getRawProperties().forEach(properties::put);
            configuration.update(properties);
        }
    }

    public void deleteSettings(Settings settings) throws IOException {
        Configuration configuration = settings.getConfiguration();
        if (configuration.getProperties() != null) {
            configuration.delete();
        }
    }

    private void setProperties(Settings settings, Dictionary<String, Object> properties, BufferedWriter writer) throws IOException {
        Map<String, String> p = new TreeMap<>(settings.getRawProperties());

        for (Map.Entry<String, String> entry : p.entrySet()) {
            setProperty(properties, writer, entry.getKey(), entry.getValue());
        }
    }

    private void setProperty(Dictionary<String, Object> properties, BufferedWriter writer, String key, String value) throws IOException {
        if (value != null) {
            properties.put(key, value);
            if (writer != null) {
                writer.write(key + " = " + value);
                writer.newLine();
            }
        }
    }

    @Reference
    public void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
