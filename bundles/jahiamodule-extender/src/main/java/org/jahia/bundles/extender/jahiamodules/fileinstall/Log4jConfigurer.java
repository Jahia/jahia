/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.extender.jahiamodules.fileinstall;

import org.jahia.bin.listeners.LoggingConfigListener;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class Log4jConfigurer {


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
            configurationAdmin = bundleContext.getService(reference);
            return configurationAdmin;
        }

        @Override
        public void modifiedService(ServiceReference<ConfigurationAdmin> reference,
                                    ConfigurationAdmin configurationAdmin) {
            Log4jConfigurer.this.configurationAdmin = bundleContext.getService(reference);
        }

        @Override
        public void removedService(ServiceReference<ConfigurationAdmin> reference,
                                   ConfigurationAdmin configurationAdmin) {
            Log4jConfigurer.this.configurationAdmin = null;
        }
    }

    static Logger logger = LoggerFactory.getLogger(Log4jConfigurer.class);

    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> serviceTracker;

    private Timer timer;

    private ConfigurationAdmin configurationAdmin;

    /**
     * Performs the registration of the log4j configuration.
     *
     * @param bundleContext the OSGi bundle context
     */
    public void start(BundleContext bundleContext) {
        serviceTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(bundleContext,
                ConfigurationAdmin.class, new Log4jConfigurer.ConfigAdminServiceCustomizer(bundleContext));
        serviceTracker.open();
        timer = new Timer("Log4j config synchronization");

        final Hashtable<String,Object> defaultConfig = new Hashtable<>();
        final Hashtable<String,Object> loggerConfig = new Hashtable<>();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (configurationAdmin != null) {
                    try {
                        if (defaultConfig.isEmpty()) {
                            Dictionary<String, Object> properties = configurationAdmin.getConfiguration("org.ops4j.pax.logging").getProperties();
                            Enumeration<String> keys = properties.keys();
                            while (keys.hasMoreElements()) {
                                String s = (String) keys.nextElement();
                                defaultConfig.put(s, properties.get(s));
                            }
                        }
                        Hashtable<String,Object> p = LoggingConfigListener.getConfig();
                        p.putAll(defaultConfig);
                        if (!p.equals(loggerConfig)) {
                            loggerConfig.clear();
                            loggerConfig.putAll(p);
                            logger.debug("Log4j config updated, sending new properties to pax");
                            configurationAdmin.getConfiguration("org.ops4j.pax.logging").update(loggerConfig);
                        }
                    } catch (IOException e) {
                        logger.error("Cannot update configuration",e);
                    }
                }
            }
        }, 0, 5000);
    }

    /**
     * Performs the unregistration of the log4j configuration.
     */
    public void stop() {
        if (serviceTracker != null) {
            serviceTracker.close();
        }
        if (timer != null) {
            timer.cancel();
        }
    }

}
