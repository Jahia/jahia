/**
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
package org.jahia.bin.listeners;

import java.io.File;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.jahia.osgi.FrameworkService;
import org.springframework.web.util.Log4jConfigListener;

/**
 * Listener for log4j configuration initialization.
 * 
 * @author Sergiy Shyrkov
 */
public class LoggingConfigListener extends Log4jConfigListener {
    
    public static final String EVENT_TOPIC_LOGGING = "org/jahia/dx/logging";
    public static final String EVENT_TYPE_LOGGING_CONFIG_CHANGED = "loggingConfigurationChanged";
    private static final String JAHIA_LOG_DIR = "jahia.log.dir";
    private static final String JAHIA_LOG4J_CONFIG = "jahia.log4j.config";
    private static final String JAHIA_LOG4J_XML = "jahia/log4j.xml";

    public static Hashtable<String, Object> getConfig() {
        Hashtable<String, Object> p = new Hashtable<>();
        @SuppressWarnings("rawtypes")
        Enumeration loggers = LogManager.getCurrentLoggers();
        while (loggers.hasMoreElements()) {
            org.apache.log4j.Logger next = (org.apache.log4j.Logger) loggers.nextElement();
            if (next.getLevel() != null) {
                p.put("log4j.category." + next.getName(), next.getLevel().toString());
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

    /**
     * Changes the level for the specified logger.
     * 
     * @param logger the name of the logger to change the level for
     * @param level the logging level value
     */
    public static void setLoggerLevel(String logger, String level) {
        LogManager.getLogger(logger).setLevel(Level.toLevel(level));

        // send an OSGi event about changed configuration
        FrameworkService.sendEvent(EVENT_TOPIC_LOGGING,
                Collections.singletonMap("type", EVENT_TYPE_LOGGING_CONFIG_CHANGED), false);
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initLogDir(event.getServletContext());
        initLog4jLocation();
        super.contextInitialized(event);
    }

    private void initLog4jLocation() {
        String lookup = null;
        if (System.getProperty(JAHIA_LOG4J_CONFIG) == null) {
            lookup = getClass().getResource("/" + JAHIA_LOG4J_XML) != null ? "classpath:" + JAHIA_LOG4J_XML
                    : "/WEB-INF/etc/config/log4j.xml";
            JahiaContextLoaderListener.setSystemProperty(JAHIA_LOG4J_CONFIG, lookup);
        } else {
            lookup = System.getProperty(JAHIA_LOG4J_CONFIG, lookup);
        }
        System.out.println("Set log4j.xml configuration location to: " + lookup);
    }

    protected void initLogDir(ServletContext servletContext) {
        String logDir = System.getProperty(JAHIA_LOG_DIR);

        if (logDir == null) {
            try {
                String server = servletContext.getServerInfo() != null ? servletContext
                        .getServerInfo().toLowerCase() : null;
                String path = servletContext.getRealPath("/");
                if (server != null && path != null) {
                    if (server.contains("tomcat")) {
                        File war = new File(path);
                        if (war.getParentFile() != null
                                && "webapps".equals(war.getParentFile().getName())) {
                            File tomcatHome = war.getParentFile().getParentFile();
                            if (tomcatHome.exists()) {
                                File logs = new File(tomcatHome, "logs");
                                if (logs.isDirectory() && logs.canWrite()) {
                                    logDir = logs.getAbsolutePath();
                                }
                            }
                        }
                    } else if (server.contains("jboss")) {
                        File war = new File(path);
                        File earFolder = war.getParentFile();
                        if (earFolder != null) {
                            File deploymentsFolder = earFolder.getParentFile();
                            if (deploymentsFolder != null) {
                                if ("deployments".equals(deploymentsFolder.getName())) {
                                    // exploded EAR deployment on JBoss
                                    File standaloneFolder = deploymentsFolder.getParentFile();
                                    if (standaloneFolder != null) {
                                        File log = new File(standaloneFolder, "log");
                                        if (log.isDirectory() && log.canWrite()) {
                                            logDir = log.getAbsolutePath();
                                        }
                                    }
                                } else if ("deployment".equals(deploymentsFolder.getName())) {
                                    // packaged EAR deployment on JBoss
                                    File vfsFolder = deploymentsFolder.getParentFile();
                                    if (vfsFolder != null && "vfs".equals(vfsFolder.getName())) {
                                        File standaloneFolder = vfsFolder.getParentFile() != null ? vfsFolder.getParentFile()
                                                .getParentFile() : null;
                                        if (standaloneFolder != null) {
                                            File log = new File(standaloneFolder, "log");
                                            if (log.isDirectory() && log.canWrite()) {
                                                logDir = log.getAbsolutePath();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (server.contains("websphere")) {
                        File logs = new File("logs");
                        if (logs.isDirectory() && new File("installedApps").exists()) {
                            logDir = logs.getAbsolutePath();
                        } else {
                            File war = new File(path);
                            File earFolder = war.getParentFile();
                            if (earFolder != null) {
                                File nodeCell = earFolder.getParentFile();
                                if (nodeCell != null) {
                                    File installedApps = nodeCell.getParentFile();
                                    if (installedApps != null && "installedApps".equals(installedApps.getName())) {
                                        File appSrv = installedApps.getParentFile();
                                        if (appSrv.exists()) {
                                            File log = new File(appSrv, "logs");
                                            if (log.isDirectory() && log.canWrite()) {
                                                logDir = log.getAbsolutePath();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // no handling for other application servers
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (logDir != null) {
            if (!logDir.endsWith("/") || !logDir.endsWith("\\")) {
                logDir = logDir + File.separator;
            }

            JahiaContextLoaderListener.setSystemProperty(JAHIA_LOG_DIR, logDir);
        }

        System.out.println("Logging directory set to: " + (logDir != null ? logDir : "<current>"));
    }

}
