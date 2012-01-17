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

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.ContextLoader;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.jstl.core.Config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
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
public class JahiaContextLoaderListener extends PortalStartupListener implements
        ServletRequestListener,
        ServletRequestAttributeListener,
        HttpSessionListener,
        HttpSessionActivationListener,
        HttpSessionAttributeListener,
        HttpSessionBindingListener,
        ServletContextAttributeListener {
    
    private static final transient Logger logger = LoggerFactory
            .getLogger(JahiaContextLoaderListener.class);

    private static ServletContext servletContext;
    private static long startupTime;

    private static long sessionCount = 0;
    
    private static String pid = "";

    private static boolean contextInitialized = false;
    private static Map jahiaContextListenersConfiguration;

    @SuppressWarnings("unchecked")
    private static Map<ServletRequest, Long> requestTimes = Collections.synchronizedMap(new LRUMap(1000));

    public boolean isEventInterceptorActivated(String interceptorName) {
        if (jahiaContextListenersConfiguration == null) {
            return false; // by default all event interceptor are deactivated.
        }
        Object interceptorActivatedObject = jahiaContextListenersConfiguration.get(interceptorName);
        Boolean interceptorActivated = null;
        if (interceptorActivatedObject instanceof Boolean) {
            interceptorActivated = (Boolean) interceptorActivatedObject;
        } else if (interceptorActivatedObject instanceof String) {
            interceptorActivated = new Boolean((String) interceptorActivatedObject);
        } else {
            return false;
        }
        if (interceptorActivated == null) {
            return false;
        }
        return interceptorActivated.booleanValue();
    }

    public void contextInitialized(ServletContextEvent event) {
        startupTime = System.currentTimeMillis();
        startupWithTrust(Jahia.getBuildNumber());

        logger.info("Starting up Jahia, please wait...");

        servletContext = event.getServletContext();
        String jahiaWebAppRoot = servletContext.getRealPath("/");
        System.setProperty("jahiaWebAppRoot", jahiaWebAppRoot);
        if (System.getProperty("jahia.config") == null) {
            System.setProperty("jahia.config", "");
        }
        if (System.getProperty("jahia.license") == null) {
            System.setProperty("jahia.license", "");
        }
        Jahia.initContextData(servletContext);
        writePID(servletContext);

        try {
            super.contextInitialized(event);
            try {
                ((TemplatePackageApplicationContextLoader) ContextLoader.getCurrentWebApplicationContext().getBean("TemplatePackageApplicationContextLoader")).start();
            } catch (Exception e) {
                logger.error("Error initializing Jahia modules Spring application context. Cause: " + e.getMessage(), e);
            }
            if (Jahia.isEnterpriseEdition()) {
                requireLicense();
            }
            // register listeners after the portal is started
            ApplicationsManagerServiceImpl.getInstance().registerListeners();
            Config.set(servletContext, Config.FMT_FALLBACK_LOCALE, (SettingsBean.getInstance().getDefaultLanguageCode() != null) ? SettingsBean
                    .getInstance().getDefaultLanguageCode() : Locale.ENGLISH.getLanguage());
            jahiaContextListenersConfiguration = (Map) ContextLoader.getCurrentWebApplicationContext().getBean("jahiaContextListenersConfiguration");
            if (isEventInterceptorActivated("interceptServletContextListenerEvents")) {
                SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletContextInitializedEvent(event.getServletContext()));
            }
            contextInitialized = true;
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    private void writePID(ServletContext servletContext) {
        try {
            pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        } catch (Exception e) {
            logger.warn("Unable to determine process id", e);
        }
        String path = servletContext.getRealPath("WEB-INF/var");
        if (path != null) {
            try {
                FileUtils.writeStringToFile(new File(path, "jahia.pid"), pid);
            } catch (IOException e) {
                logger.warn("Unable to write process ID into a file " + new File(path, "jahia.pid"), e);
            }
        }
    }

    private void removePID(ServletContext servletContext) {
        String path = servletContext.getRealPath("WEB-INF/var");
        if (path != null) {
            FileUtils.deleteQuietly(new File(path, "jahia.pid"));
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
        contextInitialized = false;
        if (isEventInterceptorActivated("interceptServletContextListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletContextDestroyedEvent(event.getServletContext()));
        }
        removePID(servletContext);
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

        StringBuilder versionBuffer = new StringBuilder();
        for (int i = 0; i < Constants.JAHIA_PROJECT_VERSION.length(); i++) {
            versionBuffer.append(" ");
            versionBuffer.append(Constants.JAHIA_PROJECT_VERSION.substring(i, i + 1));
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
                            + "      . . . s t a r t i n g   j a h i a   b u i l d  @BUILD_NUMBER@ . . .\n"
                            + "\n"
                            + "                  v e r s i o n  :  @VERSION@\n"
                            + "\n\n"
                            + "   Copyright 2002-2012 - Jahia Solutions Group SA http://www.jahia.com - All Rights Reserved\n"
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
        msg = msg.replace("@VERSION@", versionBuffer.toString());

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
        if (isEventInterceptorActivated("interceptHttpSessionListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionCreatedEvent(se.getSession()));
        }
    }

    public void sessionDestroyed(HttpSessionEvent se) {
        sessionCount--;
        if (isEventInterceptorActivated("interceptHttpSessionListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionDestroyedEvent(se.getSession()));
        }
    }

    public void requestDestroyed(ServletRequestEvent sre) {
        requestTimes.remove(sre.getServletRequest());
        if (isEventInterceptorActivated("interceptServletRequestListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletRequestDestroyedEvent(sre.getServletRequest()));
        }
    }

    public void requestInitialized(ServletRequestEvent sre) {
        requestTimes.put(sre.getServletRequest(), System.currentTimeMillis());
        if (isEventInterceptorActivated("interceptServletRequestListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletRequestInitializedEvent(sre.getServletRequest()));
        }
    }

    public static long getSessionCount() {
        return sessionCount;
    }

    public static long getRequestCount() {
        return requestTimes.size();
    }

    public static String getPid() {
        return pid;
    }

    public void sessionWillPassivate(HttpSessionEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionActivationEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionWillPassivateEvent(se.getSession()));
        }
    }

    public void sessionDidActivate(HttpSessionEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionActivationEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionDidActivateEvent(se.getSession()));
        }
    }

    public void attributeAdded(HttpSessionBindingEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionAttributeAddedEvent(se));
        }
    }

    public void attributeRemoved(HttpSessionBindingEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionAttributeRemovedEvent(se));
        }
    }

    public void attributeReplaced(HttpSessionBindingEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionAttributeReplacedEvent(se));
        }
    }

    public void valueBound(HttpSessionBindingEvent event) {
        if (isEventInterceptorActivated("interceptHttpSessionBindingListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionValueBoundEvent(event));
        }
    }

    public void valueUnbound(HttpSessionBindingEvent event) {
        if (isEventInterceptorActivated("interceptHttpSessionBindingListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new HttpSessionValueUnboundEvent(event));
        }
    }

    public void attributeAdded(ServletContextAttributeEvent scab) {
        if (contextInitialized) {
            if (isEventInterceptorActivated("interceptServletContextAttributeListenerEvents")) {
                SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletContextAttributeAddedEvent(scab));
            }
        }
    }

    public void attributeRemoved(ServletContextAttributeEvent scab) {
        if (contextInitialized) {
            if (isEventInterceptorActivated("interceptServletContextAttributeListenerEvents")) {
                SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletContextAttributeRemovedEvent(scab));
            }
        }
    }

    public void attributeReplaced(ServletContextAttributeEvent scab) {
        if (contextInitialized) {
            if (isEventInterceptorActivated("interceptServletContextAttributeListenerEvents")) {
                SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletContextAttributeReplacedEvent(scab));
            }
        }
    }

    public void attributeAdded(ServletRequestAttributeEvent srae) {
        if (isEventInterceptorActivated("interceptServletRequestAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletRequestAttributeAddedEvent(srae));
        }
    }

    public void attributeRemoved(ServletRequestAttributeEvent srae) {
        if (isEventInterceptorActivated("interceptServletRequestAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletRequestAttributeRemovedEvent(srae));
        }
    }

    public void attributeReplaced(ServletRequestAttributeEvent srae) {
        if (isEventInterceptorActivated("interceptServletRequestAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().getModuleContext().publishEvent(new ServletRequestAttributeReplacedEvent(srae));
        }
    }

    public class HttpSessionCreatedEvent extends ApplicationEvent {
        public HttpSessionCreatedEvent(HttpSession session) {
            super(session);
        }

        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class HttpSessionDestroyedEvent extends ApplicationEvent {
        public HttpSessionDestroyedEvent(HttpSession session) {
            super(session);
        }
        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class ServletRequestDestroyedEvent extends ApplicationEvent {
        public ServletRequestDestroyedEvent(ServletRequest servletRequest) {
            super(servletRequest);
        }

        public ServletRequest getServletRequest() {
            return (ServletRequest) super.getSource();
        }
    }

    public class ServletRequestInitializedEvent extends ApplicationEvent {
        public ServletRequestInitializedEvent(ServletRequest servletRequest) {
            super(servletRequest);
        }
        public ServletRequest getServletRequest() {
            return (ServletRequest) super.getSource();
        }
    }

    public class HttpSessionWillPassivateEvent extends ApplicationEvent {
        public HttpSessionWillPassivateEvent(HttpSession session) {
            super(session);
        }
        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class HttpSessionDidActivateEvent extends ApplicationEvent {
        public HttpSessionDidActivateEvent(HttpSession session) {
            super(session);
        }
        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class HttpSessionAttributeAddedEvent extends ApplicationEvent {
        public HttpSessionAttributeAddedEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }

        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionAttributeRemovedEvent extends ApplicationEvent {
        public HttpSessionAttributeRemovedEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionAttributeReplacedEvent extends ApplicationEvent {
        public HttpSessionAttributeReplacedEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionValueBoundEvent extends ApplicationEvent {
        public HttpSessionValueBoundEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionValueUnboundEvent extends ApplicationEvent {
        public HttpSessionValueUnboundEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class ServletContextAttributeAddedEvent extends ApplicationEvent {
        public ServletContextAttributeAddedEvent(ServletContextAttributeEvent servletContextAttributeEvent) {
            super(servletContextAttributeEvent);
        }

        public ServletContextAttributeEvent getServletContextAttributeEvent() {
            return (ServletContextAttributeEvent) super.getSource();
        }
    }

    public class ServletContextAttributeRemovedEvent extends ApplicationEvent {
        public ServletContextAttributeRemovedEvent(ServletContextAttributeEvent servletContextAttributeEvent) {
            super(servletContextAttributeEvent);
        }
        public ServletContextAttributeEvent getServletContextAttributeEvent() {
            return (ServletContextAttributeEvent) super.getSource();
        }
    }

    public class ServletContextAttributeReplacedEvent extends ApplicationEvent {
        public ServletContextAttributeReplacedEvent(ServletContextAttributeEvent servletContextAttributeEvent) {
            super(servletContextAttributeEvent);
        }
        public ServletContextAttributeEvent getServletContextAttributeEvent() {
            return (ServletContextAttributeEvent) super.getSource();
        }
    }

    public class ServletRequestAttributeAddedEvent extends ApplicationEvent {
        public ServletRequestAttributeAddedEvent(ServletRequestAttributeEvent servletRequestAttributeEvent) {
            super(servletRequestAttributeEvent);
        }
        public ServletRequestAttributeEvent getServletRequestAttributeEvent() {
            return (ServletRequestAttributeEvent) super.getSource();
        }
    }

    private class ServletRequestAttributeRemovedEvent extends ApplicationEvent {
        public ServletRequestAttributeRemovedEvent(ServletRequestAttributeEvent servletRequestAttributeEvent) {
            super(servletRequestAttributeEvent);
        }
        public ServletRequestAttributeEvent getServletRequestAttributeEvent() {
            return (ServletRequestAttributeEvent) super.getSource();
        }
    }

    private class ServletRequestAttributeReplacedEvent extends ApplicationEvent {
        public ServletRequestAttributeReplacedEvent(ServletRequestAttributeEvent servletRequestAttributeEvent) {
            super(servletRequestAttributeEvent);
        }
        public ServletRequestAttributeEvent getServletRequestAttributeEvent() {
            return (ServletRequestAttributeEvent) super.getSource();
        }
    }

    public class ServletContextInitializedEvent extends ApplicationEvent {
        public ServletContextInitializedEvent(ServletContext servletContext) {
            super(servletContext);
        }
        public ServletContext getServletContext() {
            return (ServletContext) super.getSource();
        }
    }

    private class ServletContextDestroyedEvent extends ApplicationEvent {
        public ServletContextDestroyedEvent(ServletContext servletContext) {
            super(servletContext);
        }
        public ServletContext getServletContext() {
            return (ServletContext) super.getSource();
        }
    }
}
