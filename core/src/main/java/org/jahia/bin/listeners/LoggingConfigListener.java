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
package org.jahia.bin.listeners;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.builder.impl.DefaultConfigurationBuilder;
import org.apache.logging.log4j.web.Log4jServletContextListener;
import org.jahia.osgi.FrameworkService;

/**
 * Listener for log4j configuration initialization.
 * 
 * @author Sergiy Shyrkov
 */
public class LoggingConfigListener extends Log4jServletContextListener {
    static final Logger logger = Logger.getLogger(LoggingConfigListener.class.getName());
    
    public static final String EVENT_TOPIC_LOGGING = "org/jahia/dx/logging";
    public static final String EVENT_TYPE_LOGGING_CONFIG_CHANGED = "loggingConfigurationChanged";
    private static final String JAHIA_LOG_DIR = "jahia.log.dir";
    private static final String JAHIA_LOG4J_CONFIG = "jahia.log4j.config";
    private static final String JAHIA_LOG4J_XML = "jahia/log4j2.xml";

    public static Map<String, Object> getConfig() {
        Map<String, Object> p = new HashMap<>();
        LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
        for (LoggerConfig logger : logContext.getConfiguration().getLoggers().values()) {
            if (logger.getLevel() != null) {
                if (StringUtils.isEmpty(logger.getName())) {
                    p.put("log4j2.rootLogger.level", logger.getLevel().toString());
                } else {
                    String loggerIdentifier = StringUtils.replace(logger.getName(), ".", "_");
                    p.put("log4j2.logger." + loggerIdentifier + ".name", logger.getName());
                    p.put("log4j2.logger." + loggerIdentifier + ".level", logger.getLevel().toString());
                }
            }
        }
        return p;
    }

    /**
     * Returns the logging level of the root logger.
     * 
     * @return the logging level of the root logger
     */
    public static String getRootLoggerLevel() {
        return LogManager.getRootLogger().getLevel().toString();
    }

    private static LoggerConfig getTargetLoggerConfig(Configuration config, String logger) {
        return StringUtils.isEmpty(logger) || StringUtils.equalsIgnoreCase(logger, LoggerConfig.ROOT) ? config.getRootLogger()
                : config.getLoggerConfig(logger);
    }

    /**
     * Changes the level for the specified logger.
     * 
     * @param logger the name of the logger to change the level for
     * @param level the logging level value
     */
    public static void setLoggerLevel(String logger, String level) {
        LoggerContext logContext = (LoggerContext) LogManager.getContext(false);
        Configuration config = logContext.getConfiguration();
        if (StringUtils.isEmpty(logger) || LoggerConfig.ROOT.equals(logger) || getTargetLoggerConfig(config, logger).getName().equals(logger)) {
            getTargetLoggerConfig(config, logger).setLevel(Level.toLevel(level));
        } else {
            config.addLogger(logger, LoggerConfig.createLogger(true, Level.getLevel(level), logger, null, new AppenderRef[] {}, null,
                    new DefaultConfigurationBuilder<BuiltConfiguration>().build(), null));
        }
        logContext.updateLoggers();

        // send an OSGi event about changed configuration
        FrameworkService.sendEvent(EVENT_TOPIC_LOGGING,
                Collections.singletonMap("type", EVENT_TYPE_LOGGING_CONFIG_CHANGED), false);
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initLogDir(event.getServletContext());
        initLog4jLocation();
        JahiaContextLoaderListener.setSystemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        super.contextInitialized(event);
    }

    private void initLog4jLocation() {
        String lookup = null;
        if (System.getProperty(JAHIA_LOG4J_CONFIG) == null) {
            lookup = getClass().getResource("/" + JAHIA_LOG4J_XML) != null ? "classpath:" + JAHIA_LOG4J_XML
                    : "/WEB-INF/etc/config/log4j2.xml";
            JahiaContextLoaderListener.setSystemProperty(JAHIA_LOG4J_CONFIG, lookup);
        } else {
            lookup = System.getProperty(JAHIA_LOG4J_CONFIG, lookup);
        }
        logger.log(java.util.logging.Level.INFO, "Set log4j2.xml configuration location to: {}", lookup);
    }

    protected void initLogDir(ServletContext servletContext) {
        String logDir = System.getProperty(JAHIA_LOG_DIR);

        if (logDir == null) {
            if (StringUtils.containsIgnoreCase(servletContext.getServerInfo(), "tomcat")) {
                logDir = resolveLogDir(servletContext.getRealPath("/"));
            } else {
                // no handling for other application servers
            }

        }

        if (logDir != null) {
            if (!logDir.endsWith("/") || !logDir.endsWith("\\")) {
                logDir = logDir + File.separator;
            }

            JahiaContextLoaderListener.setSystemProperty(JAHIA_LOG_DIR, logDir);
        }

        logger.log(java.util.logging.Level.INFO, "Logging directory set to: {}", (logDir != null ? logDir : "<current>"));
    }

    private String resolveLogDir(String path) {
        try {
            if (path != null) {
                File war = new File(path);
                if (war.getParentFile() != null && "webapps".equals(war.getParentFile().getName())) {
                    File tomcatHome = war.getParentFile().getParentFile();
                    if (tomcatHome.exists()) {
                        File logs = new File(tomcatHome, "logs");
                        if (logs.isDirectory() && logs.canWrite()) {
                            return logs.getAbsolutePath();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(java.util.logging.Level.WARNING, "Cannot resolve logging directory", e);
        }
        return null;
    }
}
