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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bundles.config.OsgiConfigService;
import org.jahia.services.content.*;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper;
import org.jahia.services.modulemanager.spi.Config;
import org.jahia.services.modulemanager.spi.ConfigService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationEvent;
import org.osgi.service.cm.ConfigurationListener;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Service to store and restore OSGi configurations to/from JCR
 */
@Component(service = {OsgiConfigService.class, ConfigService.class, ConfigurationListener.class})
public class ConfigServiceImpl implements OsgiConfigService, ConfigurationListener {

    public static final String CONFIGS_NODE_NAME = "configs";

    public static final String CONFIG_TYPES = "org.jahia.bundles.config";
    public static final String CONFIG_TYPE = "configType";

    private static Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
    private static final String FELIX_FILEINSTALL_FILENAME = "felix.fileinstall.filename";

    private ConfigurationAdmin configAdmin;

    private boolean autoSave = true;

    @Reference
    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    /**
     * Activate
     */
    @Activate
    public void start() {
        File restoreConfigurations = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "[persisted-configurations].dorestore");
        if (System.getProperty("restoreConfigurations") != null || restoreConfigurations.exists()) {
            restoreConfigurationsFromJCR(Arrays.asList(ConfigType.MODULE_DEFAULT, ConfigType.USER));
            if (restoreConfigurations.exists()) {
                try {
                    Files.delete(restoreConfigurations.toPath());
                } catch (IOException e) {
                    logger.debug("Cannot delete marker file", e);
                }
            }
        }
    }

    public Config getConfig(String pid) throws IOException {
        Configuration configuration = configAdmin.getConfiguration(pid);
        if (configuration.getBundleLocation() != null && configuration.getBundleLocation().contains("org.jahia.bundles.config.manager")) {
            configuration.setBundleLocation(null);
        }
        return new ConfigImpl(configuration, null);
    }

    public Config getConfig(String factoryPid, String identifier) throws IOException  {
        try {
            Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    Object filename = configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME);
                    if (filename != null && filename.toString().endsWith("/" + factoryPid + "-" + identifier + ".cfg")) {
                        return new ConfigImpl(configuration, identifier);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            logger.debug("invalid syntax", e);
            // not possible
        }

        Configuration result = configAdmin.createFactoryConfiguration(factoryPid, "?");
        return new ConfigImpl(result, identifier);
    }

    public Config getConfig(Configuration configuration) {
        String identifier = null;
        if (configuration.getFactoryPid() != null && configuration.getProperties() != null) {
            Object filename = configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME);
            if (filename != null) {
                identifier = StringUtils.substringBetween(filename.toString(), configuration.getFactoryPid() + "-", ".cfg");
            }
        }
        return new ConfigImpl(configuration, identifier);
    }

    public void storeConfig(Config config) throws IOException {
        if (!(config instanceof ConfigImpl)) {
            throw new IllegalArgumentException("Config must have been created with ConfigService");
        }
        Configuration configuration = ((ConfigImpl)config).getConfiguration();

        // refresh and save config
        if (configuration.getProperties() == null) {
            @SuppressWarnings("java:S1149") Dictionary<String, Object> properties = new Hashtable<>();
            String filename = (config.getIdentifier() != null ? (configuration.getFactoryPid() + "-" + config.getIdentifier()) : (configuration.getPid())) + ".cfg";
            File file = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/" + filename);

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                bw.write(config.getContent());
            }

            properties.put(FELIX_FILEINSTALL_FILENAME, file.toURI().toString());
            config.getRawProperties().forEach(properties::put);
            configuration.update(properties);
        } else {
            Dictionary<String, Object> properties = configuration.getProperties();
            config.getRawProperties().forEach(properties::put);
            configuration.update(properties);
        }
    }

    public void deleteConfig(Config config) throws IOException {
        if (!(config instanceof ConfigImpl)) {
            throw new IllegalArgumentException("Config must have been created with ConfigService");
        }
        Configuration configuration = ((ConfigImpl)config).getConfiguration();
        if (configuration.getProperties() != null) {
            configuration.delete();
        }
    }

    public Map<String, ConfigType> getAllConfigurationTypes() {
        try {
            Map<String, List<String>> configurationsContent = loadConfigs();
            return getConfigTypes(configurationsContent);
        } catch (IOException e) {
            throw new ModuleManagementException(e);
        }
    }

    public Collection<String> storeAllConfigurationsToJCR() {
        try {
            Map<String, List<String>> configurationsContent = loadConfigs();
            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Collection<String>>() {
                @Override
                public Collection<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper moduleManagement = BundleInfoJcrHelper.getRootNode(session);
                    if (!moduleManagement.hasNode(CONFIGS_NODE_NAME)) {
                        moduleManagement.addNode(CONFIGS_NODE_NAME, "jnt:configurationsFolder");
                    }
                    JCRNodeWrapper configsNode = moduleManagement.getNode(CONFIGS_NODE_NAME);
                    removeUnusedConfigurations(configsNode, configurationsContent);
                    Collection<String> l = storeConfigurationsToJCR(configsNode, configurationsContent);
                    session.save();
                    return l;
                }
            });
        } catch (IOException | RepositoryException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public Collection<String> restoreConfigurationsFromJCR(Collection<ConfigType> types) {
        try {
            File folder = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc");

            return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Collection<String>>() {
                @Override
                public Collection<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JCRNodeWrapper moduleManagement = session.getNode("/module-management");
                    if (moduleManagement.hasNode(CONFIGS_NODE_NAME)) {
                        Map<String, ConfigType> all = getAllConfigurationTypes();
                        JCRNodeWrapper configsNode = moduleManagement.getNode(CONFIGS_NODE_NAME);
                        List<String> restored = restoreConfigurationsFromJCR(all, configsNode, types, folder);
                        removeUnusedConfigurations(all, types, folder);
                        return restored;
                    }
                    return Collections.emptySet();
                }
            });
        } catch (RepositoryException e) {
            throw new ModuleManagementException(e);
        }
    }

    private List<String> restoreConfigurationsFromJCR(Map<String, ConfigType> all,
                                                      JCRNodeWrapper configsNode,
                                                      Collection<ConfigType> types,
                                                      File folder) throws RepositoryException {
        List<String> restored = new ArrayList<>();
        JCRNodeIteratorWrapper ni = configsNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper node = (JCRNodeWrapper) ni.nextNode();
            all.remove(node.getName());
            boolean shouldApply = types == null || (node.hasProperty(CONFIG_TYPE) && types.contains(ConfigType.valueOf(node.getProperty(CONFIG_TYPE).getString())));
            if (shouldApply && restoreConfiguration(node, folder)) {
                restored.add(node.getName());
            }
        }
        return restored;
    }

    private void removeUnusedConfigurations(Map<String, ConfigType> all, Collection<ConfigType> types, File folder) {
        for (Map.Entry<String, ConfigType> entry : all.entrySet()) {
            if (types == null || types.contains(entry.getValue())) {
                FileUtils.deleteQuietly(new File(folder, entry.getKey()));
            }
        }
    }

    @Override
    public void setAutoSaveToJCR(boolean autoSave) {
        this.autoSave = autoSave;
    }

    /**
     * Load all available configurations
     * @return config-name/content map
     * @throws IOException exception
     */
    public Map<String, List<String>> loadConfigs() throws IOException {
        Map<String, List<String>> configurationsContent = new TreeMap<>();
        Configuration[] configs = new Configuration[0];
        try {
            configs = configAdmin.listConfigurations(null);
        } catch (InvalidSyntaxException e) {
            // not possible
            logger.debug("invalid syntax", e);
            return configurationsContent;
        }
        for (Configuration config : configs) {
            String filename = (String) config.getProperties().get(FELIX_FILEINSTALL_FILENAME);
            if (filename != null) {
                try {
                    List<String> lines = IOUtils.readLines(new URL(filename).openStream(), StandardCharsets.UTF_8);
                    configurationsContent.put(StringUtils.substringAfterLast(filename, "/"), lines);
                } catch (FileNotFoundException e) {
                    logger.warn("Cannot find file {}", filename, e);
                }
            }
        }
        return configurationsContent;
    }

    /**
     * Get the types of all specified configurations
     * @param configurationsContent configuration name with their content
     * @return name/type map
     * @throws IOException exception
     */
    public Map<String, ConfigType> getConfigTypes(Map<String, List<String>> configurationsContent) throws IOException {
        Dictionary<String, Object> configTypes = configAdmin.getConfiguration(CONFIG_TYPES).getProperties();

        Map<String, ConfigType> result = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : configurationsContent.entrySet()) {
            if (configTypes.get(entry.getKey()) != null) {
                String value = (String) configTypes.get(entry.getKey());
                try {
                    result.put(entry.getKey(), ConfigType.valueOf(value.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid value for {} : {}", entry.getKey(), value);
                }
            } else if (entry.getValue().stream().map(String::toLowerCase).anyMatch(p -> p.startsWith("# do not edit"))) {
                result.put(entry.getKey(), ConfigType.MODULE);
            } else if (entry.getValue().stream().map(String::toLowerCase).anyMatch(p -> p.startsWith("# default configuration"))) {
                result.put(entry.getKey(), ConfigType.MODULE_DEFAULT);
            } else {
                result.put(entry.getKey(), ConfigType.USER);
            }
        }
        return result;
    }

    private Collection<String> storeConfigurationsToJCR(JCRNodeWrapper configsNode, Map<String, List<String>> configurationsContent) throws RepositoryException {
        try {
            List<String> saved = new ArrayList<>();
            Map<String, ConfigType> configTypes = getConfigTypes(configurationsContent);

            for (Map.Entry<String, List<String>> entry : configurationsContent.entrySet()) {
                StringWriter writer = new StringWriter();
                IOUtils.writeLines(entry.getValue(), null, writer);
                String configContent = writer.getBuffer().toString();

                try (InputStream is = IOUtils.toInputStream(configContent, StandardCharsets.UTF_8)) {
                    boolean exists = configsNode.hasNode(entry.getKey());
                    if (exists) {
                        JCRNodeWrapper previousFile = configsNode.getNode(entry.getKey());
                        String previousContent = previousFile.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_DATA).getString();
                        if (!previousContent.equals(configContent)) {
                            previousFile.remove();
                            exists = false;
                        }
                    }
                    if (!exists) {
                        JCRNodeWrapper file = configsNode.addNode(entry.getKey(), "jnt:configurationFile");
                        file.getFileContent().uploadFile(is, "text/plain");
                        file.setProperty(CONFIG_TYPE, configTypes.get(entry.getKey()).toString());
                        saved.add(entry.getKey());
                    }
                }
            }
            return saved;
        } catch (IOException e) {
            throw new ModuleManagementException(e);
        }
    }

    private void removeUnusedConfigurations(JCRNodeWrapper configsNode, Map<String, List<String>> configurationsContent) throws RepositoryException {
        JCRNodeIteratorWrapper ni = configsNode.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper file = (JCRNodeWrapper) ni.next();
            if (!configurationsContent.containsKey(file.getName())) {
                file.remove();
            }
        }
    }

    private boolean restoreConfiguration(JCRNodeWrapper savedFile, File folder) throws RepositoryException {
        try {
            List<String> savedContent = IOUtils.readLines(savedFile.getNode(Constants.JCR_CONTENT).getProperty(Constants.JCR_DATA).getBinary().getStream(), StandardCharsets.UTF_8);
            File configFile = new File(folder, savedFile.getName());
            if (!configFile.exists() || !FileUtils.readLines(configFile, StandardCharsets.UTF_8).equals(savedContent)) {
                FileUtils.writeLines(configFile, StandardCharsets.UTF_8.name(), savedContent);
                return true;
            }

            return false;
        } catch (IOException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public void configurationEvent(ConfigurationEvent event) {
        if (autoSave && SettingsBean.getInstance().isProcessingServer()) {
            storeAllConfigurationsToJCR();
        }
    }
}
