/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules;

import java.io.File;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.codehaus.plexus.util.StringUtils;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for registering/unregistering the FileInstall configuration for DX modules.
 */
class FileInstallConfigurer {

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
            unregister(configurationAdmin);
            register(configurationAdmin);
            return configurationAdmin;
        }

        @Override
        public void modifiedService(ServiceReference<ConfigurationAdmin> reference,
                ConfigurationAdmin configurationAdmin) {
            unregister(configurationAdmin);
            register(configurationAdmin);
        }

        @Override
        public void removedService(ServiceReference<ConfigurationAdmin> reference,
                ConfigurationAdmin configurationAdmin) {
            unregister(configurationAdmin);
        }
    }

    static Logger logger = LoggerFactory.getLogger(FileInstallConfigurer.class);

    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> serviceTracker;

    private Properties felixProperties;

    private boolean startNewBundles;
    
    FileInstallConfigurer() {
        felixProperties = (Properties) SpringContextSingleton.getBean("felixFileInstallConfig");
        startNewBundles = Boolean.valueOf(felixProperties.getProperty("felix.fileinstall.bundles.new.start", "true"));
    }

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

    private void register(ConfigurationAdmin configurationAdmin) {
        String watchedDir = felixProperties.getProperty("felix.fileinstall.dir");
        Configuration cfg = findExisting(configurationAdmin, watchedDir);
        if (cfg != null) {
            logger.info("FileInstall configuration for directory {} already exists. No need to create it: {}",
                    watchedDir, cfg);
            return;
        }
        try {
            cfg = configurationAdmin.createFactoryConfiguration("org.apache.felix.fileinstall");
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
            cfg.setBundleLocation(null);
            cfg.update(properties);
            logger.info("Registered FileInstall configuration for directory {}: {}", watchedDir, cfg);
        } catch (IOException e) {
            logger.error("Cannot update FileInstall configuration", e);
        }
    }

    /**
     * Performs the registration of the FileInstall configuration for DX modules.
     * 
     * @param bundleContext
     *            the OSGi bundle context
     */
    public void start(BundleContext bundleContext) {
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
    }

    private void unregister(ConfigurationAdmin configurationAdmin) {
        Properties felixProperties = ((Properties) SpringContextSingleton.getBean("felixFileInstallConfig"));
        String watchedDir = felixProperties.getProperty("felix.fileinstall.dir");
        Configuration cfg = findExisting(configurationAdmin, watchedDir);
        if (cfg != null) {
            try {
                cfg.delete();
                logger.info("Unregistered FileInstall configuration for directory {}: {}", watchedDir, cfg);
            } catch (IOException e) {
                logger.error("Unable to remove FileInstall configuration", e);
            }
        }
    }
    
    boolean isStartNewBundles() {
        return startNewBundles;
    }
}
