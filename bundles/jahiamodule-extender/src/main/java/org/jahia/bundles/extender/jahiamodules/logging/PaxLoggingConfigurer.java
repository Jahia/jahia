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
package org.jahia.bundles.extender.jahiamodules.logging;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
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
 * Synchronizes Log4j configuration from DX core into Pax Logging configuration.
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
                Hashtable<String, Object> coreConfig = LoggingConfigListener.getConfig();
                if (defaultConfig.isEmpty()) {
                    Dictionary<String, Object> properties = configurationAdmin.getConfiguration(CFG_PID, null)
                            .getProperties();
                    if (properties != null) {
                        Enumeration<String> keys = properties.keys();
                        while (keys.hasMoreElements()) {
                            String s = (String) keys.nextElement();
                            if (s != null && s.startsWith("log4j.category.") && !coreConfig.containsKey(s)) {
                                logger.info("Logging category {} does not present in DX core configuration."
                                        + " Will remove it from the " + CFG_PID + ".cfg file", s);
                                needsUpdate = true;
                            } else {
                                defaultConfig.put(s, properties.get(s));
                            }
                        }
                        loggerConfig.putAll(defaultConfig);
                    }
                }
                needsUpdate = needsUpdate | syncRootLoggerLevel();
                Hashtable<String, Object> newConfig = new Hashtable<>();
                // put the defaults
                newConfig.putAll(defaultConfig);
                // put the current DX core levels
                newConfig.putAll(coreConfig);
                if (needsUpdate || !newConfig.equals(loggerConfig)) {
                    loggerConfig.clear();
                    loggerConfig.putAll(newConfig);
                    logger.info("Log4j configuration updated. Updating configuration " + CFG_PID);
                    configurationAdmin.getConfiguration(CFG_PID, null).update(loggerConfig);
                }
            } catch (IOException e) {
                logger.error("Cannot update logging configuration", e);
            }
        }

        private boolean syncRootLoggerLevel() {
            String rootLogger = ObjectUtils.toString(defaultConfig.get("log4j.rootLogger"), null);
            if (null == rootLogger) {
                logger.warn("Unable to find log4j.rootLogger in the logging configuration " + CFG_PID
                        + ". Please, check your configuration and correct it.");
                return false;
            }
            String level = StringUtils.substringBefore(rootLogger, ",");
            String newLevel = LoggingConfigListener.getRootLoggerLevel();
            if (!level.trim().equalsIgnoreCase(newLevel)) {
                logger.info("Root logger level has changed to {}. Updating it in Pax Logging configuration.", newLevel);
                defaultConfig.put("log4j.rootLogger", newLevel + "," + StringUtils.substringAfter(rootLogger, ","));

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
        serviceTrackerConfigAdmin = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(bundleContext,
                ConfigurationAdmin.class, null);
        serviceTrackerConfigAdmin.open();
        timer = new Timer("Log4j configuration synchronization", true);

        long timerPeriod = getTimerPeriod();
        logger.info("Scheduling logging configuration synchronization task with interval of {} ms", timerPeriod);
        syncTask = new ConfigSynchronizerTimer();
        timer.schedule(syncTask, 0, timerPeriod);
        
        Dictionary<String, String> props = new Hashtable<>();
        props.put(Constants.SERVICE_PID, "org.jahia.bundles.extender.jahiamodules.PaxLoggingConfigurer");
        props.put(Constants.SERVICE_DESCRIPTION, "Synchronizes Log4j configuration from DX core into Pax Logging configuration");
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
