/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.listeners;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
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
import org.apache.logging.log4j.core.appender.WriterAppender;
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
        // if root logger or existing logger configuration is to be updated do it,
        // otherwise create the specific logger configuration
        if (StringUtils.isEmpty(logger) || LoggerConfig.ROOT.equals(logger)
                || getTargetLoggerConfig(config, logger).getName().equals(logger)) {
            getTargetLoggerConfig(config, logger).setLevel(Level.toLevel(level));
        } else {
            config.addLogger(logger, LoggerConfig.createLogger(true, Level.getLevel(level), logger, null, new AppenderRef[] {}, null,
                    new DefaultConfigurationBuilder<BuiltConfiguration>().build(), null));
        }
        logContext.updateLoggers();
        logContext.getLogger(logger);
        // send an OSGi event about changed configuration
        FrameworkService.sendEvent(EVENT_TOPIC_LOGGING,
                Collections.singletonMap("type", EVENT_TYPE_LOGGING_CONFIG_CHANGED), false);
    }

    /**
     * Creates a {@link Writer} and adds a dynamic {@link WriterAppender} to the specified logger.
     * The created and returned Writer needs to be removed with removeLogAwareWriter
     *
     * @param logger the name of the logger obtaining the WriterAppender
     * @return the created {@link Writer}
     */
    public static Writer createLogAwareWriter(String logger) {
        StringWriter stringWriter = new StringWriter();
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();

        WriterAppender writerAppender = WriterAppender.newBuilder().setName(logger + "writeLogger").setTarget(stringWriter)
                .build();
        writerAppender.start();
        config.addAppender(writerAppender);

        LoggerConfig loggerConfig = config.getLoggerConfig(logger);
        // if a logger higher in the hierarchy is returned, we have to create the configuration for the specific logger
        if (!loggerConfig.getName().equals(logger)) {
            config.addLogger(logger, LoggerConfig.createLogger(true, Level.INFO, logger, null, new AppenderRef[] {}, null,
                    new DefaultConfigurationBuilder<BuiltConfiguration>().build(), null));
            loggerConfig = config.getLoggerConfig(logger);
        }
        loggerConfig.addAppender(writerAppender, null, null);

        ctx.updateLoggers();
        return stringWriter;
    }

    /**
     * Removes the {@link WriterAppender} from the specified logger.
     *
     * @param logger the name of the logger getting the WriterAppender removed
     */
    public static void removeLogAwareWriter(String logger){
        final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        final Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(logger);
        loggerConfig.removeAppender(logger + "writeLogger");
        ctx.updateLoggers();
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
        logger.log(java.util.logging.Level.INFO, "Set log4j2.xml configuration location to: {0}", lookup);
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

        logger.log(java.util.logging.Level.INFO, "Logging directory set to: {0}", (logDir != null ? logDir : "<current>"));
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
