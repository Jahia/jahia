/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin.listeners;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.springframework.web.util.Log4jConfigListener;

import com.google.common.collect.ImmutableSet;

/**
 * Listener for log4j configuration initialization.
 * 
 * @author Sergiy Shyrkov
 */
public class InitLoggingConfigListener extends Log4jConfigListener {

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
                        if (war.getParentFile() != null
                                && ImmutableSet.copyOf(
                                        new String[] { "deploy", "deploy-hasingleton", "farm" })
                                        .contains(war.getParentFile().getName())) {
                            File jbossServer = war.getParentFile().getParentFile();
                            if (jbossServer.exists()) {
                                File log = new File(jbossServer, "log");
                                if (log.isDirectory() && log.canWrite()) {
                                    logDir = log.getAbsolutePath();
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
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // do no call super
    }
}
