/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin.listeners;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pluto.driver.PortalStartupListener;
import org.jahia.bin.Jahia;
import org.jahia.services.applications.ApplicationsManagerServiceImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.templates.TemplatePackageApplicationContextLoader;
import org.jahia.settings.SettingsBean;
import org.springframework.beans.FatalBeanException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.ContextLoader;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.jsp.jstl.core.Config;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Startup listener for the Spring's application context.
 * User: Serge Huber
 * Date: 22 juil. 2008
 * Time: 17:01:22
 */
public class JahiaContextLoaderListener extends PortalStartupListener implements ServletRequestListener, HttpSessionListener {
    
    private static final transient Logger logger = LoggerFactory
            .getLogger(JahiaContextLoaderListener.class);

    private static ServletContext servletContext;
    private static long startupTime;

    private static long sessionCount = 0;

    private static Map requestTimes = Collections.synchronizedMap(new LRUMap(1000));

    public void contextInitialized(ServletContextEvent event) {
        startupTime = System.currentTimeMillis();
        startupWithTrust(Jahia.getBuildNumber());

        logger.info("Starting up Jahia, please wait...");

        servletContext = event.getServletContext();
        String jahiaWebAppRoot = servletContext.getRealPath("/");
        System.setProperty("jahiaWebAppRoot", jahiaWebAppRoot);
        Jahia.initContextData(servletContext);
        
        try {
            boolean configExists = event.getServletContext().getResource(SettingsBean.JAHIA_PROPERTIES_FILE_PATH) != null;
            if (configExists) {
                super.contextInitialized(event);
                try {
                    ((TemplatePackageApplicationContextLoader)ContextLoader.getCurrentWebApplicationContext().getBean("TemplatePackageApplicationContextLoader")).start();
                } catch (Exception e) {
                    logger.error("Error initializing Jahia modules Spring application context. Cause: " + e.getMessage(), e);
                }
                if (Jahia.isEnterpriseEdition()) {
                    requireLicense();
                }
                // register listeners after the portal is started
                ApplicationsManagerServiceImpl.getInstance().registerListeners();
            } else {
                logger.warn("Configuration file could not be found at location " + SettingsBean.JAHIA_PROPERTIES_FILE_PATH + ", Jahia will not start !");
            }
            Config.set(servletContext, Config.FMT_FALLBACK_LOCALE, configExists ? SettingsBean
                    .getInstance().getDefaultLanguageCode() : Locale.ENGLISH.getLanguage());
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    private void requireLicense() {
        try {
            if (!ContextLoader.getCurrentWebApplicationContext().getBean("licenseChecker")
                    .getClass().getName().equals("org.jahia.security.license.LicenseChecker")
                    || !ContextLoader.getCurrentWebApplicationContext().getBean("LicenseFilter")
                            .getClass().getName()
                            .equals("org.jahia.security.license.LicenseFilter")) {
                throw new FatalBeanException("Required classes for license manager were not found");
            }
        } catch (NoSuchBeanDefinitionException e) {
            throw new FatalBeanException("Required classes for license manager were not found", e);
        }
    }

    public void contextDestroyed(ServletContextEvent event) {
        try {
            if (event.getServletContext().getResource(SettingsBean.JAHIA_PROPERTIES_FILE_PATH) != null) {
                try {
                    if (ContextLoader.getCurrentWebApplicationContext() != null
                            && ContextLoader.getCurrentWebApplicationContext().getBean(
                                    "TemplatePackageApplicationContextLoader") != null) {
                        ((TemplatePackageApplicationContextLoader) ContextLoader
                                .getCurrentWebApplicationContext().getBean(
                                        "TemplatePackageApplicationContextLoader")).stop();
                    }
                } catch (Exception e) {
                    logger.error(
                            "Error shutting down Jahia modules Spring application context. Cause: "
                                    + e.getMessage(), e);
                }
                super.contextDestroyed(event);
            }
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * startupWithTrust
     * AK    20.01.2001
     */
    private void startupWithTrust(int buildNumber) {
        Integer buildNumberInteger = new Integer(buildNumber);
        String buildString = buildNumberInteger.toString();
        StringBuilder buildBuffer = new StringBuilder();

        for (int i = 0; i < buildString.length(); i++) {
            buildBuffer.append(" ");
            buildBuffer.append(buildString.substring(i, i + 1));
        }

        String msg;
        InputStream is = null;
        try {
        	is = this.getClass().getResourceAsStream("/jahia-startup-intro.txt");
            msg = IOUtils.toString(is);
        } catch (Exception e) {
        	logger.warn(e.getMessage(), e);
            msg =
                    "\n\n\n\n"
                            + "                                     ____.\n"
                            + "                         __/\\ ______|    |__/\\.     _______\n"
                            + "              __   .____|    |       \\   |    +----+       \\\n"
                            + "      _______|  /--|    |    |    -   \\  _    |    :    -   \\_________\n"
                            + "     \\\\______: :---|    :    :           |    :    |         \\________>\n"
                            + "             |__\\---\\_____________:______:    :____|____:_____\\\n"
                            + "                                        /_____|\n"
                            + "\n"
                            + "      . . . s t a r t i n g   j a h i a   b u i l d  @BUILD_NUMBER@"+
                    " . . .\n"
                            + "\n\n"
                            + "   Copyright 2002-2011 - Jahia Solutions Group SA http://www.jahia.com - All Rights Reserved\n"
                            + "\n\n"
                            + " *******************************************************************************\n"
                            + " * The contents of this software, or the files included with this software,    *\n"
                            + " * are subject to the GNU General Public License (GPL).                        *\n"
                            + " * You may not use this software except in compliance with the license. You    *\n"
                            + " * may obtain a copy of the license at http://www.jahia.com/license. See the   *\n"
                            + " * license for the rights, obligations and limitations governing use of the    *\n"
                            + " * contents of the software.                                                   *\n"
                            + " *******************************************************************************\n"
                            + "\n\n";
		} finally {
			IOUtils.closeQuietly(is);
		}
        msg = msg.replace("@BUILD_NUMBER@", buildBuffer.toString());

        System.out.println (msg);
        System.out.flush();
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static long getStartupTime() {
        return startupTime;
    }

    public void sessionCreated(HttpSessionEvent se) {
        sessionCount++;
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        sessionCount--;
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        long requestStartTime = (Long) requestTimes.remove(sre.getServletRequest());
        long requestTotalTime = System.currentTimeMillis() - requestStartTime;
    }

    public void requestInitialized(ServletRequestEvent sre) {
        requestTimes.put(sre.getServletRequest(), System.currentTimeMillis());
    }

    public static long getSessionCount() {
        return sessionCount;
    }

    public static long getRequestCount() {
        return requestTimes.size();
    }
}
