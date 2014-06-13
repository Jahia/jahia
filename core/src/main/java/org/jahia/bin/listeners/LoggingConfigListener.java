/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bin.listeners;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.springframework.web.util.Log4jConfigListener;

/**
 * Listener for log4j configuration initialization.
 * 
 * @author Sergiy Shyrkov
 */
public class LoggingConfigListener extends Log4jConfigListener {

    private static final String JAHIA_LOG_DIR = "jahia.log.dir";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        initLogDir(event.getServletContext());
        super.contextInitialized(event);
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
                            if (deploymentsFolder != null && "deployments".equals(deploymentsFolder.getName())) {
                                File standaloneFolder = deploymentsFolder.getParentFile();
                                if (standaloneFolder.exists()) {
                                    File log = new File(standaloneFolder, "log");
                                    if (log.isDirectory() && log.canWrite()) {
                                        logDir = log.getAbsolutePath();
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

            System.setProperty(JAHIA_LOG_DIR, logDir);
        }

        System.out.println("Logging directory set to: " + (logDir != null ? logDir : "<current>"));
    }
}
