/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.fileinstall;

import org.apache.felix.fileinstall.CustomHandler;
import org.codehaus.plexus.util.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.*;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

/**
 * Utility class for registering/unregistering the FileInstall configuration for DX modules.
 */
public class FileInstallConfigurer {

    /**
     * Service tracker customizer for the configuration administration service.
     */
    public class ConfigAdminServiceCustomizer
            implements ServiceTrackerCustomizer<ConfigurationAdmin, ConfigurationAdmin> {

        private BundleContext bundleContext;

        public ConfigAdminServiceCustomizer(BundleContext bundleContext) {
            this.bundleContext = bundleContext;
        }

        @Override
        public ConfigurationAdmin addingService(ServiceReference<ConfigurationAdmin> reference) {
            ConfigurationAdmin configurationAdmin = bundleContext.getService(reference);
            register(configurationAdmin);
            return configurationAdmin;
        }

        @Override
        public void modifiedService(ServiceReference<ConfigurationAdmin> reference,
                ConfigurationAdmin configurationAdmin) {
        }

        @Override
        public void removedService(ServiceReference<ConfigurationAdmin> reference,
                ConfigurationAdmin configurationAdmin) {
        }
    }

    static Logger logger = LoggerFactory.getLogger(FileInstallConfigurer.class);

    private ServiceRegistration<CustomHandler> customHandlerRegistration;

    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> serviceTracker;

    private Configuration findExisting(ConfigurationAdmin configurationAdmin, String dirPath) {
        try {
            Configuration[] existingCfgs = configurationAdmin
                    .listConfigurations("(service.factoryPid=org.apache.felix.fileinstall)");
            if (existingCfgs != null) {
                String refDirPath = new File(dirPath).getCanonicalPath();
                for (Configuration cfg : existingCfgs) {
                    String dir = (String) cfg.getProperties().get("felix.fileinstall.dir");
                    if (StringUtils.isNotEmpty(dir) && new File(dir).getCanonicalPath().equals(refDirPath)) {
                        return cfg;
                    }
                }
            }
        } catch (InvalidSyntaxException | IOException e) {
            logger.error("Cannot get FileInstall configurations", e);
        }

        return null;
    }

    /**
     * Returns the configuration for this file install service.
     *
     * @return configuration properties
     */
    private Properties getConfig() {
        return (Properties) SpringContextSingleton.getBean("felixFileInstallConfig");
    }

    private void register(ConfigurationAdmin configurationAdmin) {
        Properties felixProperties = getConfig();
        String watchedDir = felixProperties.getProperty("felix.fileinstall.dir");
        Configuration cfg = findExisting(configurationAdmin, watchedDir);
        try {
            if (cfg == null) {
                cfg = configurationAdmin.createFactoryConfiguration("org.apache.felix.fileinstall");
                cfg.setBundleLocation(null);
            } else {
                logger.info("FileInstall configuration for directory {} already exists. Update it: {}", watchedDir, cfg);
            }
            Dictionary<String, Object> properties = cfg.getProperties();
            if (properties == null) {
                properties = new Hashtable<String, Object>();
            }
            for (Map.Entry<Object, Object> entry : felixProperties.entrySet()) {
                String key = entry.getKey().toString();
                if (key.startsWith("felix.fileinstall.")) {
                    properties.put(key, entry.getValue());
                }
            }
            cfg.update(properties);
            logger.info("Registered FileInstall configuration for directory {}: {}", watchedDir, cfg);
        } catch (IOException e) {
            logger.error("Cannot update FileInstall configuration", e);
        }
    }

    /**
     * Registers the custom handler for the FileInstall artifacts.
     *
     * @param context the OSGi bundle context object
     */
    private void registerCustomHandler(BundleContext context) {
        Properties cfg = getConfig();
        if (!cfg.containsKey(CustomHandler.PROP_ID)) {
            // no need to register handler
            return;
        }
        Dictionary<String, Object> props = new Hashtable<String, Object>();
        props.put(Constants.SERVICE_PID, "org.jahia.bundles.fileinstall.handler");
        props.put(Constants.SERVICE_DESCRIPTION, "Jahia handler for FileInstall artifacts");
        props.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        props.put("type", "dx-modules");
        customHandlerRegistration = context.registerService(CustomHandler.class, new ModuleFileInstallHandler(cfg),
                props);
    }

    /**
     * Performs the registration of the FileInstall configuration for DX modules.
     *
     * @param bundleContext
     *            the OSGi bundle context
     */
    public void start(BundleContext bundleContext) {
        registerCustomHandler(bundleContext);
        serviceTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(bundleContext,
                ConfigurationAdmin.class, new ConfigAdminServiceCustomizer(bundleContext));
        serviceTracker.open();
    }

    /**
     * Performs the unregistration of the FileInstall configuration for DX modules.
     */
    public void stop() {
        if (serviceTracker != null) {
            serviceTracker.close();
        }

        if (customHandlerRegistration != null) {
            customHandlerRegistration.unregister();
        }
    }

    private boolean unregister(ConfigurationAdmin configurationAdmin) {
        String watchedDir = getConfig().getProperty("felix.fileinstall.dir");
        Configuration cfg = findExisting(configurationAdmin, watchedDir);
        if (cfg != null) {
            try {
                logger.info("Unregistered FileInstall configuration for directory {}: {}", watchedDir, cfg);
                cfg.delete();
                return true;
            } catch (IOException e) {
                logger.error("Unable to remove FileInstall configuration", e);
            }
        }
        return false;
    }
}
