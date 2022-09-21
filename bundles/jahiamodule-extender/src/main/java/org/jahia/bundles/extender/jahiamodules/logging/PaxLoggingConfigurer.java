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
package org.jahia.bundles.extender.jahiamodules.logging;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.bin.listeners.LoggingConfigListener;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Synchronizes Log4j configuration from Jahia core into Pax Logging configuration.
 */
public class PaxLoggingConfigurer implements EventHandler {

    private final class ConfigSynchronizerTimer extends TimerTask {
        private Hashtable<String, Object> defaultConfig = new Hashtable<>();
        private Hashtable<String, Object> loggerConfig = new Hashtable<>();

        @Override
        public void run() {
            syncConfig();
        }

        protected void syncConfig() {
            ConfigurationAdmin configurationAdmin = serviceTrackerConfigAdmin.getService();
            if (configurationAdmin == null) {
                return;
            }
            try {
                boolean needsUpdate = false;
                Map<String, Object> coreConfig = LoggingConfigListener.getConfig();
                if (defaultConfig.isEmpty()) {
                    Dictionary<String, Object> properties = configurationAdmin.getConfiguration(CFG_PID, null)
                            .getProperties();
                    if (properties != null) {
                        needsUpdate = checkOsgiLogConfig(properties, coreConfig);
                        loggerConfig.putAll(defaultConfig);
                    }
                }
                needsUpdate = needsUpdate || syncRootLoggerLevel();
                // put the defaults
                Map<String, Object> newConfig = new HashMap<>(defaultConfig);
                // put the current Jahia core levels
                newConfig.putAll(coreConfig);
                if (needsUpdate || !newConfig.equals(loggerConfig)) {
                    updateConfig(newConfig);
                }
            } catch (IOException e) {
                logger.error("Cannot update logging configuration", e);
            }
        }
        
        private boolean checkOsgiLogConfig(Dictionary<String, Object> properties, Map<String, Object> coreConfig) {
            boolean needsUpdate = false;
            Enumeration<String> keys = properties.keys();
            while (keys.hasMoreElements()) {
                String s = keys.nextElement();
                if (s != null && s.startsWith("log4j2.logger.") && !coreConfig.containsKey(s)) {
                    logger.info("Logging logger {} is not present in Jahia core configuration. Will remove it from the {}.cfg file", s,
                            CFG_PID);
                    needsUpdate = true;
                } else {
                    defaultConfig.put(s, properties.get(s));
                }
            }
            return needsUpdate;
        }
        
        private void updateConfig(Map<String, Object> newConfig) throws IOException {
            loggerConfig.clear();
            loggerConfig.putAll(newConfig);
            logger.info("Log4j configuration updated. Updating configuration {}", CFG_PID);
            serviceTrackerConfigAdmin.getService().getConfiguration(CFG_PID, null).update(loggerConfig);
        }

        private boolean syncRootLoggerLevel() {
            String rootLoggerLevel = ObjectUtils.toString(defaultConfig.get("log4j2.rootLogger.level"), null);
            if (null == rootLoggerLevel) {
                logger.warn("Unable to find log4j2.rootLogger.level in the logging configuration " + CFG_PID
                        + ". Please, check your configuration and correct it.");
                return false;
            }
            String newLevel = LoggingConfigListener.getRootLoggerLevel();
            if (!rootLoggerLevel.trim().equalsIgnoreCase(newLevel)) {
                logger.info("Root logger level has changed to {}. Updating it in Pax Logging configuration.", newLevel);
                defaultConfig.put("log4j2.rootLogger.level", newLevel);
                return true;
            }
            return false;
        }
    }

    private static final String CFG_PID = "org.ops4j.pax.logging";

    static Logger logger = LoggerFactory.getLogger(PaxLoggingConfigurer.class);

    private ServiceRegistration<EventHandler> eventHandlerServiceRegistration;
    
    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> serviceTrackerConfigAdmin;

    private ConfigSynchronizerTimer syncTask;

    private Timer timer;

    private long getTimerPeriod() {
        String intervalString = JahiaContextLoaderListener.getServletContext().getInitParameter("log4jRefreshInterval");
        if (StringUtils.isNotBlank(intervalString)) {
            return Long.parseLong(intervalString);
        }
        return SettingsBean.getInstance().isDevelopmentMode() ? 5000 : 60000;
    }

    @Override
    public void handleEvent(Event event) {
        logger.info("Received event about changes in logging configuration {}", event);
        syncTask.syncConfig();
    }

    /**
     * Performs the registration of the log4j configuration.
     *
     * @param bundleContext the OSGi bundle context
     */
    public void start(BundleContext bundleContext) {
        serviceTrackerConfigAdmin = new ServiceTracker<>(bundleContext, ConfigurationAdmin.class, null);
        serviceTrackerConfigAdmin.open();
        timer = new Timer("Log4j configuration synchronization", true);

        long timerPeriod = getTimerPeriod();
        logger.info("Scheduling logging configuration synchronization task with interval of {} ms", timerPeriod);
        syncTask = new ConfigSynchronizerTimer();
        timer.schedule(syncTask, 0, timerPeriod);
        
        Dictionary<String, String> props = new Hashtable<>();
        props.put(Constants.SERVICE_PID, "org.jahia.bundles.extender.jahiamodules.PaxLoggingConfigurer");
        props.put(Constants.SERVICE_DESCRIPTION, "Synchronizes Log4j configuration from Jahia core into Pax Logging configuration");
        props.put(Constants.SERVICE_VENDOR, Jahia.VENDOR_NAME);
        props.put(EventConstants.EVENT_TOPIC, LoggingConfigListener.EVENT_TOPIC_LOGGING);
        props.put(EventConstants.EVENT_FILTER, "(type=" + LoggingConfigListener.EVENT_TYPE_LOGGING_CONFIG_CHANGED + ")");
        eventHandlerServiceRegistration = bundleContext.registerService(EventHandler.class, this, props);
    }

    /**
     * Performs the unregistration of the log4j configuration synchronizer.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel();
            syncTask = null;
        }
        if (eventHandlerServiceRegistration != null) {
            eventHandlerServiceRegistration.unregister();
        }
        if (serviceTrackerConfigAdmin != null) {
            serviceTrackerConfigAdmin.close();
        }
    }

}
