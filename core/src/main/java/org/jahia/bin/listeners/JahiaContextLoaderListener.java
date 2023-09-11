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

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.VFS;
import org.apache.hc.core5.http.HttpHeaders;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.osgi.FrameworkService;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.patches.Patcher;
import org.jahia.utils.Patterns;
import org.jahia.utils.WebAppPathResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.jstl.core.Config;
import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;

/**
 * Entry point and startup/shutdown listener for all Jahia services, including Spring application context, OSGi platform service etc.
 *
 * @author Serge Huber
 */
public class JahiaContextLoaderListener extends ContextLoaderListener implements
        ServletRequestListener,
        ServletRequestAttributeListener,
        HttpSessionListener,
        HttpSessionActivationListener,
        HttpSessionAttributeListener,
        HttpSessionBindingListener,
        ServletContextAttributeListener {

    /**
     * This event is fired when the root application context is initialized.
     */
    public static class RootContextInitializedEvent extends ApplicationEvent {

        private static final long serialVersionUID = 8215602249732419470L;

        public RootContextInitializedEvent(Object source) {
            super(source);
        }

        public XmlWebApplicationContext getContext() {
            return (XmlWebApplicationContext) getSource();
        }
    }

    private static final Pattern HTTP_METHOD = Pattern.compile("POST|PUT|GET|DELETE");
    private static final Logger logger = LoggerFactory.getLogger(JahiaContextLoaderListener.class);

    private static Set<String> addedSystemProperties = new ConcurrentSkipListSet<String>();
    private static ServletContext servletContext;
    private static long startupTime;
    private static String pid = "";
    private static String webAppRoot;
    private static Map<String, Object> jahiaContextListenersConfiguration;

    private static volatile long sessionCount = 0;
    private static volatile boolean contextInitialized = false;
    private static volatile boolean running = false;

    @SuppressWarnings("unchecked")
    private static Map<ServletRequest, Long> requestTimes = Collections.synchronizedMap(new LRUMap(1000));

    @SuppressWarnings("unchecked")
    public static void endContextInitialized() {
        try {
            FrameworkService frameworkService = FrameworkService.getInstance();

            frameworkService.waitForInitialStartLevelReached();
            frameworkService.waitForFileInstallStarted();
            frameworkService.waitForSpringBridgeStarted();

            // execute patches after the complete initialization
            if (SettingsBean.getInstance().isProcessingServer()) {
                Patcher.getInstance().executeScripts("contextInitialized-processingServer");
            } else {
                // we leave the possibility to provide Groovy scripts for non-processing servers
                Patcher.getInstance().executeScripts("contextInitialized-nonProcessingServer");
            }

            Patcher.getInstance().executeScripts("contextInitialized");

            frameworkService.raiseStartLevel();
            frameworkService.waitForFinalStartLevelReached();
            // wait for cluster synchronization if enabled
            frameworkService.waitForClusterStarted();

            logger.info("Finishing context initialization phase");

            if (SettingsBean.getInstance().isProcessingServer()) {
                ModuleUtils.getModuleManager().storeAllLocalPersistentStates();
            }

            // do initialization of all services, implementing JahiaAfterInitializationService
            initJahiaAfterInitializationServices();

            // set fallback locale
            Config.set(servletContext, Config.FMT_FALLBACK_LOCALE, (SettingsBean.getInstance().getDefaultLanguageCode() != null) ? SettingsBean
                    .getInstance().getDefaultLanguageCode() : Locale.ENGLISH.getLanguage());

            jahiaContextListenersConfiguration = (Map<String, Object>) ContextLoader.getCurrentWebApplicationContext().getBean("jahiaContextListenersConfiguration");
            if (isEventInterceptorActivated("interceptServletContextListenerEvents")) {
                SpringContextSingleton.getInstance().publishEvent(new ServletContextInitializedEvent(getServletContext()));
            }
            contextInitialized = true;

            ServicesRegistry.getInstance().getSchedulerService().startSchedulers();

            logger.info("Context initialization phase finished");
        } catch (JahiaException e) {
            running = false;
            logger.error(e.getMessage(), e);
            throw new JahiaRuntimeException(e);
        } catch (RuntimeException e) {
            running = false;
            throw e;
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    public static boolean isEventInterceptorActivated(String interceptorName) {
        if (jahiaContextListenersConfiguration == null) {
            return false; // by default all event interceptor are deactivated.
        }
        Object interceptorActivatedObject = jahiaContextListenersConfiguration.get(interceptorName);
        if (interceptorActivatedObject instanceof Boolean) {
            return (Boolean) interceptorActivatedObject;
        } else {
            return interceptorActivatedObject instanceof String && Boolean.parseBoolean((String) interceptorActivatedObject);
        }
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {

        startupTime = System.currentTimeMillis();
        startupWithTrust(Jahia.getBuildNumber());

        logger.info("Starting up Jahia, please wait...");

        servletContext = event.getServletContext();

        Jahia.setContextPath(servletContext.getContextPath());

        initWebAppRoot();

        if (System.getProperty("jahia.config") == null) {
            setSystemProperty("jahia.config", "");
        }
        if (System.getProperty("jahia.license") == null) {
            setSystemProperty("jahia.license", "");
        }

        try {
            // verify supported Java version
            Jahia.verifyJavaVersion(servletContext.getInitParameter("supported_jdk_versions"));
        } catch (JahiaInitializationException e) {
            throw new JahiaRuntimeException(e);
        }

        detectPID(servletContext);

        initializeTemporarySettingsBean();

        Patcher.getInstance().setServletContext(servletContext);
        Patcher.getInstance().executeScripts("beforeContextInitializing");

        // initialize VFS file system (solves classloader issue: https://issues.apache.org/jira/browse/VFS-228 )
        try {
            VFS.getManager();
        } catch (FileSystemException e) {
            throw new JahiaRuntimeException(e);
        }

        try {
            long timer = System.currentTimeMillis();
            logger.info("Start initializing Spring root application context");

            running = true;

            super.contextInitialized(event);

            logger.info("Spring Root application context initialized in {} ms", (System.currentTimeMillis() - timer));

            // initialize services registry
            ServicesRegistry.getInstance().init();

            // fire Spring event that the root context is initialized
            WebApplicationContext rootCtx = ContextLoader.getCurrentWebApplicationContext();
            rootCtx.publishEvent(new RootContextInitializedEvent(rootCtx));

            boolean isProcessingServer = SettingsBean.getInstance().isProcessingServer();

            // execute patches after root context initialization
            if (isProcessingServer) {
                Patcher.getInstance().executeScripts("rootContextInitialized");
            }

            // start OSGi container
            FrameworkService.getInstance().start();

        } catch (JahiaException e) {
            running = false;
            logger.error(e.getMessage(), e);
            throw new JahiaRuntimeException(e);
        } catch (RuntimeException e) {
            running = false;
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            JCRSessionFactory.getInstance().closeAllSessions();
        }
    }

    private void initializeTemporarySettingsBean() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath*:org/jahia/defaults/config/**/applicationcontext-jahiaproperties.xml");
        WebAppPathResolver pathResolver = (WebAppPathResolver) ctx.getBean("pathResolver");
        pathResolver.setServletContext(servletContext);
        SettingsBean settingsBean = new SettingsBean(pathResolver, (Properties) ctx.getBean("jahiaProperties"), (List<String>) ctx.getBean("licensesList"));
        settingsBean.setStartupOptionsMapping((Map<String, Set<String>>) ctx.getBean("startupOptionsMapping"));
        settingsBean.setApplicationContext(ctx);
        settingsBean.initPaths();
    }

    private static void initWebAppRoot() {
        webAppRoot = servletContext.getRealPath("/");
        if (webAppRoot != null
                && webAppRoot.length() > 1
                && webAppRoot.charAt(webAppRoot.length() - 1) == File.separatorChar) {
            webAppRoot = webAppRoot.substring(0, webAppRoot.length() - 1);
        }
        if (webAppRoot != null) {
            try {
                setSystemProperty("jahiaWebAppRoot", webAppRoot);
            } catch (SecurityException se) {
                logger.error(
                        "System property jahiaWebAppRoot was NOT set to " + webAppRoot
                                + " successfully ! Please check app server security manager policies to allow this.",
                        se);
            }
        }
        // let's try to read it to make sure it was set properly as this is
        // critical for Jahia startup and may fail on some application servers
        // that have SecurityManager permissions set.
        if (System.getProperty("jahiaWebAppRoot") != null
                && System.getProperty("jahiaWebAppRoot").equals(webAppRoot)) {
            logger.info("System property jahiaWebAppRoot set to " + webAppRoot
                    + " successfully.");
        } else {
            logger.error("System property jahiaWebAppRoot was NOT set to "
                    + webAppRoot
                    + " successfully ! Please check app server security manager policies to allow this.");
        }
    }

    private static void initJahiaAfterInitializationServices() throws JahiaInitializationException {
        try {
            JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());

            // initializing core services
            for (JahiaAfterInitializationService service : SpringContextSingleton.getInstance().getContext()
                    .getBeansOfType(JahiaAfterInitializationService.class).values()) {
                service.initAfterAllServicesAreStarted();
            }

            // initializing services for modules
            ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry()
                    .afterInitializationForModules();
        } finally {
            JCRSessionFactory.getInstance().setCurrentUser(null);
        }
    }

    private static void detectPID(ServletContext servletContext) {
        try {
            pid = Patterns.AT.split(ManagementFactory.getRuntimeMXBean().getName())[0];
        } catch (Exception e) {
            logger.warn("Unable to determine process id", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

        if (!running) {
            return;
        }

        contextInitialized = false;
        running = false;

        logger.info("Shutting down scheduler, wait for running jobs");
        ServicesRegistry.getInstance().getSchedulerService().stop();

        if (isEventInterceptorActivated("interceptServletContextListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(
                    new ServletContextDestroyedEvent(event.getServletContext()));
        }
        for (HttpListener listener : getListeners()) {
            listener.contextDestroyed(event);
        }

        long timer = System.currentTimeMillis();
        logger.info("Stopping OSGi platform service");

        try {
            FrameworkService.getInstance().stop();
        } catch (Exception e) {
            logger.error("Error stopping OSGi platform service. Cause: " + e.getMessage(), e);
        }

        logger.info("OSGi platform service stopped in {} ms", (System.currentTimeMillis() - timer));

        timer = System.currentTimeMillis();
        logger.info("Shutting down Spring root application context");

        super.contextDestroyed(event);

        removeAddedSystemProperties();

        logger.info("Spring Root application context shut down in {} ms", (System.currentTimeMillis() - timer));
    }

    /**
     * startupWithTrust
     * AK    20.01.2001
     */
    private void startupWithTrust(String buildString) {
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

        StringBuilder codeNameBuffer = new StringBuilder();
        for (int i = 0; i < Jahia.CODE_NAME.length(); i++) {
            codeNameBuffer.append(" ");
            codeNameBuffer.append(Jahia.CODE_NAME.substring(i, i + 1));
        }

        String msg;
        InputStream is = null;
        try {
            is = this.getClass().getResourceAsStream(Jahia.isEnterpriseEdition() ? "/META-INF/jahia-ee-startup-intro.txt" : "/META-INF/jahia-startup-intro.txt");
            msg = IOUtils.toString(is);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            msg = "";
        } finally {
            IOUtils.closeQuietly(is);
        }
        msg = StringUtils.replace(msg, "@BUILD_NUMBER@", buildBuffer.toString());
        msg = StringUtils.replace(msg, "@BUILD_DATE@", Jahia.getBuildDate());
        msg = StringUtils.replace(msg, "@VERSION@", versionBuffer.toString());
        msg = StringUtils.replace(msg, "@CODENAME@", codeNameBuffer.toString());
        msg = StringUtils.replace(msg, "@YEAR@", Jahia.YEAR);

        System.out.println(msg);
        System.out.flush();
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static long getStartupTime() {
        return startupTime;
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        logger.debug("HTTP session created: {}", se.getSession().getId());
        sessionCount++;
        if (isEventInterceptorActivated("interceptHttpSessionListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionCreatedEvent(se.getSession()));
        }
        for (HttpListener listener : getListeners()) {
            listener.sessionCreated(se);
        }
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        logger.debug("HTTP session destroyed: {}", se.getSession().getId());
        sessionCount--;
        if (isEventInterceptorActivated("interceptHttpSessionListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionDestroyedEvent(se.getSession()));
        }
        for (HttpListener listener : getListeners()) {
            listener.sessionDestroyed(se);
        }
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        requestTimes.remove(sre.getServletRequest());
        if (isEventInterceptorActivated("interceptServletRequestListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new ServletRequestDestroyedEvent(sre.getServletRequest()));
        }
        for (HttpListener listener : getListeners()) {
            listener.requestDestroyed(sre);
        }
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        ServletRequest servletRequest = sre.getServletRequest();
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            // Filter SSE request
            if (HTTP_METHOD.matcher(req.getMethod().toUpperCase()).matches()
                    && !StringUtils.contains(req.getQueryString(), "X-Atmosphere-Transport=")
                    && !StringUtils.equals(req.getHeader(HttpHeaders.ACCEPT), "text/event-stream")) {
                requestTimes.put(servletRequest, System.currentTimeMillis());
            }
        }
        if (isEventInterceptorActivated("interceptServletRequestListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new ServletRequestInitializedEvent(servletRequest));
        }
        for (HttpListener listener : getListeners()) {
            listener.requestInitialized(sre);
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

    /**
     * Sets the system property keeping track of properties, we added (there was no value present before we set it).
     *
     * @param key the property key
     * @param value the value to be set
     */
    public static void setSystemProperty(String key, String value) {
        if (System.setProperty(key, value) == null) {
            addedSystemProperties.add(key);
        }
    }

    private static void removeAddedSystemProperties() {
        try {
            for (String key : addedSystemProperties) {
                System.clearProperty(key);
            }
        } finally {
            addedSystemProperties.clear();
        }
    }

    private List<HttpListener> getListeners() {
        if (contextInitialized) {
            HttpListenersRegistry registry = (HttpListenersRegistry) SpringContextSingleton.getInstance().getContext().getBean("HttpListenersRegistry");
            return registry.getEventListeners();
        }
        return Collections.emptyList();
    }

    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionActivationEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionWillPassivateEvent(se.getSession()));
        }
        for (HttpListener listener : getListeners()) {
            listener.sessionWillPassivate(se);
        }
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionActivationEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionDidActivateEvent(se.getSession()));
        }
        for (HttpListener listener : getListeners()) {
            listener.sessionDidActivate(se);
        }
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionAttributeAddedEvent(se));
        }
        for (HttpListener listener : getListeners()) {
            listener.attributeAdded(se);
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionAttributeRemovedEvent(se));
        }
        for (HttpListener listener : getListeners()) {
            listener.attributeRemoved(se);
        }
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent se) {
        if (isEventInterceptorActivated("interceptHttpSessionAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionAttributeReplacedEvent(se));
        }
        for (HttpListener listener : getListeners()) {
            listener.attributeReplaced(se);
        }
    }

    @Override
    public void valueBound(HttpSessionBindingEvent event) {
        if (isEventInterceptorActivated("interceptHttpSessionBindingListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionValueBoundEvent(event));
        }
        for (HttpListener listener : getListeners()) {
            listener.valueBound(event);
        }
    }

    @Override
    public void valueUnbound(HttpSessionBindingEvent event) {
        if (isEventInterceptorActivated("interceptHttpSessionBindingListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new HttpSessionValueUnboundEvent(event));
        }
        for (HttpListener listener : getListeners()) {
            listener.valueUnbound(event);
        }
    }

    @Override
    public void attributeAdded(ServletContextAttributeEvent scab) {
        if (contextInitialized) {
            if (isEventInterceptorActivated("interceptServletContextAttributeListenerEvents")) {
                SpringContextSingleton.getInstance().publishEvent(new ServletContextAttributeAddedEvent(scab));
            }
            for (HttpListener listener : getListeners()) {
                listener.attributeAdded(scab);
            }
        }
    }

    @Override
    public void attributeRemoved(ServletContextAttributeEvent scab) {
        if (contextInitialized) {
            if (isEventInterceptorActivated("interceptServletContextAttributeListenerEvents")) {
                SpringContextSingleton.getInstance().publishEvent(new ServletContextAttributeRemovedEvent(scab));
            }
            for (HttpListener listener : getListeners()) {
                listener.attributeRemoved(scab);
            }
        }
    }

    @Override
    public void attributeReplaced(ServletContextAttributeEvent scab) {
        if (contextInitialized) {
            if (isEventInterceptorActivated("interceptServletContextAttributeListenerEvents")) {
                SpringContextSingleton.getInstance().publishEvent(new ServletContextAttributeReplacedEvent(scab));
            }
            for (HttpListener listener : getListeners()) {
                listener.attributeReplaced(scab);
            }
        }
    }

    @Override
    public void attributeAdded(ServletRequestAttributeEvent srae) {
        if (isEventInterceptorActivated("interceptServletRequestAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new ServletRequestAttributeAddedEvent(srae));
        }
        for (HttpListener listener : getListeners()) {
            listener.attributeAdded(srae);
        }
    }

    @Override
    public void attributeRemoved(ServletRequestAttributeEvent srae) {
        if (isEventInterceptorActivated("interceptServletRequestAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new ServletRequestAttributeRemovedEvent(srae));
        }
        for (HttpListener listener : getListeners()) {
            listener.attributeRemoved(srae);
        }
    }

    @Override
    public void attributeReplaced(ServletRequestAttributeEvent srae) {
        if (isEventInterceptorActivated("interceptServletRequestAttributeListenerEvents")) {
            SpringContextSingleton.getInstance().publishEvent(new ServletRequestAttributeReplacedEvent(srae));
        }
        for (HttpListener listener : getListeners()) {
            listener.attributeReplaced(srae);
        }
    }

    public static boolean isContextInitialized() {
        return contextInitialized;
    }

    public class HttpSessionCreatedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -7421486835176013728L;

        public HttpSessionCreatedEvent(HttpSession session) {
            super(session);
        }

        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class HttpSessionDestroyedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -1387944667725619591L;

        public HttpSessionDestroyedEvent(HttpSession session) {
            super(session);
        }
        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class ServletRequestDestroyedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 7596456549896361175L;

        public ServletRequestDestroyedEvent(ServletRequest servletRequest) {
            super(servletRequest);
        }

        public ServletRequest getServletRequest() {
            return (ServletRequest) super.getSource();
        }
    }

    public class ServletRequestInitializedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 5822992792782543993L;

        public ServletRequestInitializedEvent(ServletRequest servletRequest) {
            super(servletRequest);
        }
        public ServletRequest getServletRequest() {
            return (ServletRequest) super.getSource();
        }
    }

    public class HttpSessionWillPassivateEvent extends ApplicationEvent {
        private static final long serialVersionUID = 6886011344567163295L;

        public HttpSessionWillPassivateEvent(HttpSession session) {
            super(session);
        }
        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class HttpSessionDidActivateEvent extends ApplicationEvent {
        private static final long serialVersionUID = 5814761122135408014L;

        public HttpSessionDidActivateEvent(HttpSession session) {
            super(session);
        }
        public HttpSession getSession() {
            return (HttpSession) super.getSource();
        }
    }

    public class HttpSessionAttributeAddedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 7316259699549761735L;

        public HttpSessionAttributeAddedEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }

        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionAttributeRemovedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 876708448117102271L;
        public HttpSessionAttributeRemovedEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionAttributeReplacedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 8128290080471455221L;
        public HttpSessionAttributeReplacedEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionValueBoundEvent extends ApplicationEvent {
        private static final long serialVersionUID = -3415824235349946403L;
        public HttpSessionValueBoundEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class HttpSessionValueUnboundEvent extends ApplicationEvent {
        private static final long serialVersionUID = 8453994121930169941L;
        public HttpSessionValueUnboundEvent(HttpSessionBindingEvent httpSessionBindingEvent) {
            super(httpSessionBindingEvent);
        }
        public HttpSessionBindingEvent getHttpSessionBindingEvent() {
            return (HttpSessionBindingEvent) super.getSource();
        }
    }

    public class ServletContextAttributeAddedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 3430737803878399224L;

        public ServletContextAttributeAddedEvent(ServletContextAttributeEvent servletContextAttributeEvent) {
            super(servletContextAttributeEvent);
        }

        public ServletContextAttributeEvent getServletContextAttributeEvent() {
            return (ServletContextAttributeEvent) super.getSource();
        }
    }

    public class ServletContextAttributeRemovedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -3543715780914938235L;
        public ServletContextAttributeRemovedEvent(ServletContextAttributeEvent servletContextAttributeEvent) {
            super(servletContextAttributeEvent);
        }
        public ServletContextAttributeEvent getServletContextAttributeEvent() {
            return (ServletContextAttributeEvent) super.getSource();
        }
    }

    public class ServletContextAttributeReplacedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 5729697513603811739L;
        public ServletContextAttributeReplacedEvent(ServletContextAttributeEvent servletContextAttributeEvent) {
            super(servletContextAttributeEvent);
        }
        public ServletContextAttributeEvent getServletContextAttributeEvent() {
            return (ServletContextAttributeEvent) super.getSource();
        }
    }

    public class ServletRequestAttributeAddedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 3317475270634384739L;
        public ServletRequestAttributeAddedEvent(ServletRequestAttributeEvent servletRequestAttributeEvent) {
            super(servletRequestAttributeEvent);
        }
        public ServletRequestAttributeEvent getServletRequestAttributeEvent() {
            return (ServletRequestAttributeEvent) super.getSource();
        }
    }

    private class ServletRequestAttributeRemovedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 4181992489489417634L;
        public ServletRequestAttributeRemovedEvent(ServletRequestAttributeEvent servletRequestAttributeEvent) {
            super(servletRequestAttributeEvent);
        }
    }

    private class ServletRequestAttributeReplacedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 1785714293103597626L;
        public ServletRequestAttributeReplacedEvent(ServletRequestAttributeEvent servletRequestAttributeEvent) {
            super(servletRequestAttributeEvent);
        }
    }

    public static class ServletContextInitializedEvent extends ApplicationEvent {
        private static final long serialVersionUID = 7380625349896182566L;
        public ServletContextInitializedEvent(ServletContext servletContext) {
            super(servletContext);
        }
        public ServletContext getServletContext() {
            return (ServletContext) super.getSource();
        }
    }

    private class ServletContextDestroyedEvent extends ApplicationEvent {
        private static final long serialVersionUID = -2099082546094025673L;
        public ServletContextDestroyedEvent(ServletContext servletContext) {
            super(servletContext);
        }
    }

    /**
     * Returns <code>true</code> if Jahia is either starting or is currently running, but is not in a process of shutting down.
     *
     * @return <code>true</code> if Jahia is either starting or is currently running, but is not in a process of shutting down; otherwise
     *         returns <code>false</code>
     */
    public static boolean isRunning() {
        return running;
    }

    public static String getWebAppRoot() {
        return webAppRoot;
    }
}
