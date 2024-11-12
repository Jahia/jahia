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
package org.jahia.bundles.config.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.bundles.config.ConfigUtil;
import org.jahia.bundles.config.Format;
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
import pl.touk.throwing.ThrowingRunnable;

import javax.jcr.RepositoryException;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
    private ConcurrentHashMap<String, CountDownLatch> latches = new ConcurrentHashMap<>();
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
            restoreConfigurationsFromJCR(Arrays.asList(ConfigType.MODULE_DEFAULT, ConfigType.USER), false);
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
        Configuration configuration = configAdmin.getConfiguration(pid, "?");
        Object filename = configuration.getProperties() != null ? configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME) : null;
        return new ConfigImpl(configuration, null, getFormat(filename));
    }

    public Config getConfig(String factoryPid, String identifier) throws IOException  {
        try {
            Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
            if (configurations != null) {
                for (Configuration configuration : configurations) {
                    Object filename = configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME);
                    if (filename != null) {
                        String name = new File(filename.toString()).getName();
                        if (name.startsWith(factoryPid + "-" + identifier + ".")) {
                            return new ConfigImpl(configuration, identifier, getFormat(name));
                        }
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

        Object filename = configuration.getProperties().get(FELIX_FILEINSTALL_FILENAME);
        Format format = getFormat(filename);

        if (configuration.getFactoryPid() != null && configuration.getProperties() != null && filename != null) {
            identifier = StringUtils.substringBetween(filename.toString(), configuration.getFactoryPid() + "-", ".");
        }

        return new ConfigImpl(configuration, identifier, format);
    }

    public void storeConfig(Config config) throws IOException {
        if (!(config instanceof ConfigImpl)) {
            throw new IllegalArgumentException("Config must have been created with ConfigService");
        }
        Configuration configuration = ((ConfigImpl)config).getConfiguration();

        // refresh and save config
        if (configuration.getProperties() == null) {
            @SuppressWarnings("java:S1149") Dictionary<String, Object> properties = new Hashtable<>();
            String baseName = config.getIdentifier() != null ? (configuration.getFactoryPid() + "-" + config.getIdentifier()) : (configuration.getPid());
            Format format = Format.valueOf(config.getFormat());
            File file = new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/" + baseName + format.getDefaultExtension());

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                bw.write(config.getContent());
            }

            Arrays.stream(Format.values()).filter(f -> f != format).flatMap(f -> f.getSupportedExtensions().stream()).forEach(
                    ext -> new File(SettingsBean.getInstance().getJahiaVarDiskPath(), "karaf/etc/" + baseName + ext).delete()
            );

            properties.put(FELIX_FILEINSTALL_FILENAME, file.toURI().toString());
            config.getRawProperties().forEach(properties::put);
            awaitConfigOperation(configuration.getPid(), ThrowingRunnable.unchecked(() -> configuration.update(properties)));
        } else {
            Dictionary<String, Object> properties = configuration.getProperties();
            config.getRawProperties().forEach(properties::put);
            Set<String> toRemove = new HashSet<>(ConfigUtil.getMap(properties).keySet());
            toRemove.removeAll(config.getRawProperties().keySet());
            toRemove.forEach(properties::remove);
            awaitConfigOperation(configuration.getPid(), ThrowingRunnable.unchecked(() -> configuration.update(properties)));
        }
    }

    public void deleteConfig(Config config) throws IOException {
        if (!(config instanceof ConfigImpl)) {
            throw new IllegalArgumentException("Config must have been created with ConfigService");
        }
        Configuration configuration = ((ConfigImpl)config).getConfiguration();
        if (configuration.getProperties() != null) {
            awaitConfigOperation(configuration.getPid(), ThrowingRunnable.unchecked(configuration::delete));
        }
    }

    private void awaitConfigOperation(String id, Runnable runnable) {
        CountDownLatch latch = latches.computeIfAbsent(id, pid -> new CountDownLatch(1));
        runnable.run();
        try {
            boolean success = latch.await(10, TimeUnit.SECONDS);
            if (!success) {
                logger.warn("Timeout after updating configuration, will continue");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Format getFormat(Object filename) {
        return Arrays.stream(Format.values())
                .filter(f -> filename != null && f.getSupportedExtensions().stream().anyMatch(ext -> filename.toString().endsWith(ext)))
                .findFirst()
                .orElse(Format.CFG);
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
                    try {
                        JCRObservationManager.setAllEventListenersDisabled(true);
                        session.save();
                    } finally {
                        JCRObservationManager.setAllEventListenersDisabled(false);
                    }
                    return l;
                }
            });
        } catch (IOException | RepositoryException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public Collection<String> restoreConfigurationsFromJCR(Collection<ConfigType> types) {
        return restoreConfigurationsFromJCR(types, true);
    }

    public Collection<String> restoreConfigurationsFromJCR(Collection<ConfigType> types, boolean removeUnused) {
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
                        if (removeUnused) {
                            removeUnusedConfigurations(all, types, folder);
                        }
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
                try (InputStream openStream = new URL(filename).openStream()){
                    List<String> lines = IOUtils.readLines(openStream, StandardCharsets.UTF_8);
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
        if (event.getType() == ConfigurationEvent.CM_UPDATED || event.getType() == ConfigurationEvent.CM_DELETED) {
            CountDownLatch l = latches.remove(event.getPid());
            if (l != null) {
                l.countDown();
            }
        }
        if (autoSave && SettingsBean.getInstance().isProcessingServer()) {
            storeAllConfigurationsToJCR();
        }
    }
}
