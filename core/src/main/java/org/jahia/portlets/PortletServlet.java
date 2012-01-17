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

package org.jahia.portlets;

import org.apache.commons.lang.StringUtils;
import org.apache.pluto.container.driver.*;
import org.apache.pluto.container.om.portlet.PortletDefinition;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 * User: jahia
 * Date: 9 juin 2009
 * Time: 16:25:27
 * 
 */

import java.io.InputStream;
import java.io.File;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;

import javax.portlet.*;
import javax.servlet.*;

import org.apache.pluto.container.FilterManager;
import org.apache.pluto.container.PortletInvokerService;
import org.apache.pluto.container.PortletRequestContext;
import org.apache.pluto.container.PortletResponseContext;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.driver.container.InitParameterApplicationIdResolver;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.applications.pluto.JahiaPortalServletRequest;

/**
 * Portlet Invocation Servlet. This servlet recieves cross context requests from
 * the the container and services the portlet request for the specified method.
 *
 * @version 1.1
 * @since 09/22/2004
 */
public class PortletServlet extends HttpServlet {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PortletServlet.class);
    private static final long serialVersionUID = -5096339022539360365L;

    static class NullPortlet implements EventPortlet, ResourceServingPortlet, Portlet {
        public void processEvent(EventRequest arg0, EventResponse arg1)
                throws PortletException, IOException {
        }

        public void serveResource(ResourceRequest arg0, ResourceResponse arg1)
                throws PortletException, IOException {
        }

        public void destroy() {
        }

        public void init(PortletConfig arg0) throws PortletException {
        }

        public void processAction(ActionRequest arg0, ActionResponse arg1)
                throws PortletException, IOException {
        }

        public void render(RenderRequest arg0, RenderResponse arg1)
                throws PortletException, IOException {
        }
    }

    // Private Member Variables ------------------------------------------------
    /**
     * The portlet name as defined in the portlet app descriptor.
     */
    private String portletName;

    /**
     * The portlet instance wrapped by this servlet.
     */
    private Map<String, Portlet> portlets = new HashMap<String, Portlet>();

    /**
     * The internal portlet context instance.
     */
    private DriverPortletContext portletContext;

    /**
     * The internal portlet config instance.
     */
    private Map<String, DriverPortletConfig> portletConfigs = new HashMap<String, DriverPortletConfig>();

    /**
     * The Event Portlet instance (the same object as portlet) wrapped by this servlet.
     */
    private Map<String, EventPortlet> eventPortlets = new HashMap<String, EventPortlet>();

    /**
     * The resource serving portlet instance wrapped by this servlet.
     */
    private Map<String, ResourceServingPortlet> resourceServingPortlets = new HashMap<String, ResourceServingPortlet>();

    private PortletContextService contextService;

    private boolean started = false;
    Timer startTimer;

    // HttpServlet Impl --------------------------------------------------------

    public String getServletInfo() {
        return "Pluto PortletServlet [" + portletName + "]";
    }

    /**
     * Initialize the portlet invocation servlet.
     *
     * @throws javax.servlet.ServletException if an error occurs while loading portlet.
     */
    public void init(ServletConfig config) throws ServletException {

        // Call the super initialization method.
        super.init(config);

        // Retrieve portlet name as defined as an initialization parameter.
        portletName = getInitParameter("portlet-name");

        started = false;

        startTimer = new Timer(true);
        final ServletContext servletContext = getServletContext();
        final ClassLoader paClassLoader = Thread.currentThread().getContextClassLoader();
        startTimer.schedule(new TimerTask() {
            public void run() {
                synchronized (servletContext) {
                    if (startTimer != null) {
                        if (attemptRegistration(servletContext, paClassLoader)) {
                            startTimer.cancel();
                            startTimer = null;
                        }
                    }
                }
            }
        }, 1, 10000);
    }

    protected boolean attemptRegistration(ServletContext context, ClassLoader paClassLoader) {
        if (PlutoServices.getServices() != null) {
            contextService = PlutoServices.getServices().getPortletContextService();
            File[] fragments = null;
            String fragmentsPath = getServletContext().getRealPath("/WEB-INF/fragments");
            if (StringUtils.isNotEmpty(fragmentsPath)) {
            	try {
            		fragments = new File(fragmentsPath).listFiles();
            	} catch (Exception e) {
            		logger.warn("Unable to list contentr of the /WEB-INF/fragments directory for internal portlets.", e);
            	}
            }

            for (int i = 0; fragments != null && i < fragments.length; i++) {
                try {
                    File fragment = fragments[i];
                    log("Processing fragment " + fragment);
                    ServletConfig config = new ServletConfigWrapper(getServletConfig(), fragment.getName());
                    String applicationName =  contextService.register(config);
                    portletContext = contextService.getPortletContext(applicationName);
                    List<? extends PortletDefinition> portlets = contextService.getPortletContext(applicationName).getPortletApplicationDefinition().getPortlets();
                    for (Iterator iterator = portlets.iterator(); iterator.hasNext();) {
                        PortletDefinition portletDD = (PortletDefinition) iterator.next();
                        portletConfigs.put(Jahia.getContextPath() + "/" + fragment.getName() + "." + portletDD.getPortletName(), contextService.getPortletConfig(applicationName, portletDD.getPortletName()));
                    }
                } catch (Exception ex) {
                    log("Error while registering portlet", ex);
                }
            }
            started = true;

            for (Map.Entry<String, DriverPortletConfig> entry : portletConfigs.entrySet()) {
                DriverPortletConfig portletConfig = entry.getValue();
                PortletDefinition portletDD = portletConfig.getPortletDefinition();

                //          Create and initialize the portlet wrapped in the servlet.
                try {
                    Class<?> clazz = paClassLoader.loadClass((portletDD.getPortletClass()));
                    Portlet portlet = (Portlet) clazz.newInstance();

                    String rootPath = portletConfig.getInitParameter("rootPath");
                    String realPath = portletConfig.getPortletContext().getRealPath(rootPath + "/definitions.cnd");
                    if (new File(realPath).exists()) {
                        try {
                            NodeTypeRegistry.getInstance().addDefinitionsFile(new File(realPath), portletConfig.getPortletName());
                            JCRStoreService.getInstance().deployDefinitions(portletConfig.getPortletName());
                        } catch (ParseException e) {
                            logger.error(e.getMessage(), e);
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    portlet.init(portletConfig);
                    portlets.put(entry.getKey(), portlet);
                    
                    initializeEventPortlet();
                    initializeResourceServingPortlet();
                    //return true;
                }
                catch (Exception ex) {
                    context.log(ex.getMessage(), ex);
                    // take out of service
                    //return true;
                }
            }
            return true;
        }
        return false;
    }

    public void destroy() {
        for (Portlet portlet : portlets.values()) {
            destroy(portlet);
        }
        super.destroy();
    }

    private void destroy(Portlet portlet) {
        synchronized (getServletContext()) {
            if (startTimer != null) {
                startTimer.cancel();
                startTimer = null;
            } else if (started && portletContext != null) {
                started = false;
                contextService.unregister(portletContext);
                if (portlet != null) {
                    try {
                        portlet.destroy();
                    }
                    catch (Exception e) {
                        // ignore
                    }
                    portlet = null;
                }
            }
            super.destroy();
        }
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }

    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, IOException {
        dispatch(request, response);
    }

    // Private Methods ---------------------------------------------------------

    /**
     * Dispatch the request to the appropriate portlet methods. This method
     * assumes that the following attributes are set in the servlet request
     * scope:
     * <ul>
     * <li>METHOD_ID: indicating which method to dispatch.</li>
     * <li>PORTLET_REQUEST: the internal portlet request.</li>
     * <li>PORTLET_RESPONSE: the internal portlet response.</li>
     * </ul>
     *
     * @param request  the servlet request.
     * @param response the servlet response.
     * @throws ServletException
     * @throws IOException
     */
    private void dispatch(HttpServletRequest request,
                          HttpServletResponse response) throws ServletException, IOException {

        String id = ((JahiaPortalServletRequest) request).getId();
        String portletName = "/"+id.substring(0, id.indexOf("!"));
        Portlet portlet = portlets.get(portletName);
        if (portlet == null) {
            throw new javax.servlet.UnavailableException("Portlet " + portletName + " unavailable");
        }
        EventPortlet eventPortlet = eventPortlets.get(portletName);
        ResourceServingPortlet resourceServingPortlet = resourceServingPortlets.get(portletName);


        // Retrieve attributes from the servlet request.
        Integer methodId = (Integer) request.getAttribute(PortletInvokerService.METHOD_ID);

        final PortletRequest portletRequest = (PortletRequest) request.getAttribute(PortletInvokerService.PORTLET_REQUEST);

        final PortletResponse portletResponse = (PortletResponse) request.getAttribute(PortletInvokerService.PORTLET_RESPONSE);

        final PortletRequestContext requestContext = (PortletRequestContext) portletRequest.getAttribute(PortletInvokerService.REQUEST_CONTEXT);
        final PortletResponseContext responseContext = (PortletResponseContext) portletRequest.getAttribute(PortletInvokerService.RESPONSE_CONTEXT);

        final FilterManager filterManager = (FilterManager) request.getAttribute(PortletInvokerService.FILTER_MANAGER);

        request.removeAttribute(PortletInvokerService.METHOD_ID);
        request.removeAttribute(PortletInvokerService.PORTLET_REQUEST);
        request.removeAttribute(PortletInvokerService.PORTLET_RESPONSE);
        request.removeAttribute(PortletInvokerService.FILTER_MANAGER);

        DriverPortletConfig portletConfig = portletConfigs.get(portletName);
        requestContext.init(portletConfig, getServletContext(), request, response);
        responseContext.init(request, response);

        PortletWindow window = requestContext.getPortletWindow();

        PortletInvocationEvent event = new PortletInvocationEvent(portletRequest, window, methodId.intValue());

        notify(event, true, null);

        // FilterManager filtermanager = (FilterManager) request.getAttribute(
        // "filter-manager");

        try {

            // The requested method is RENDER: call Portlet.render(..)
            if (methodId == PortletInvokerService.METHOD_RENDER) {
                RenderRequest renderRequest = (RenderRequest) portletRequest;
                RenderResponse renderResponse = (RenderResponse) portletResponse;
                filterManager.processFilter(renderRequest, renderResponse,
                        portlet, portletContext);
            }

            // The requested method is RESOURCE: call
            // ResourceServingPortlet.serveResource(..)
            else if (methodId == PortletInvokerService.METHOD_RESOURCE) {
                ResourceRequest resourceRequest = (ResourceRequest) portletRequest;
                ResourceResponse resourceResponse = (ResourceResponse) portletResponse;
                filterManager.processFilter(resourceRequest, resourceResponse,
                        resourceServingPortlet, portletContext);
            }

            // The requested method is ACTION: call Portlet.processAction(..)
            else if (methodId == PortletInvokerService.METHOD_ACTION) {
                ActionRequest actionRequest = (ActionRequest) portletRequest;
                ActionResponse actionResponse = (ActionResponse) portletResponse;
                filterManager.processFilter(actionRequest, actionResponse,
                        portlet, portletContext);
            }

            // The request methode is Event: call Portlet.processEvent(..)
            else if (methodId == PortletInvokerService.METHOD_EVENT) {
                EventRequest eventRequest = (EventRequest) portletRequest;
                EventResponse eventResponse = (EventResponse) portletResponse;
                filterManager.processFilter(eventRequest, eventResponse,
                        eventPortlet, portletContext);
            }
            // The requested method is ADMIN: call handlers.
            else if (methodId == PortletInvokerService.METHOD_ADMIN) {
                PortalAdministrationService pas = PlutoServices.getServices().getPortalAdministrationService();

                for (AdministrativeRequestListener l : pas.getAdministrativeRequestListeners()) {
                    l.administer(portletRequest, portletResponse);
                }
            }

            // The requested method is LOAD: do nothing.
            else if (methodId == PortletInvokerService.METHOD_LOAD) {
                // Do nothing.
            }

            notify(event, false, null);

        }
        /* catch (UnavailableException ex) {
           //
           // if (e.isPermanent()) { throw new
           // UnavailableException(e.getMessage()); } else { throw new
           // UnavailableException(e.getMessage(), e.getUnavailableSeconds());
           // }
           //

           // Portlet.destroy() isn't called by Tomcat, so we have to fix it.
           try {
               portlet.destroy();
           }
           catch (Throwable th) {
               // Don't care for Exception
               this.getServletContext().log("Error during portlet destroy.", th);
           }
           // take portlet out of service
           portlet = null;

           // TODO: Handle everything as permanently for now.
           throw new javax.servlet.UnavailableException(ex.getMessage());

       } */
        catch (PortletException ex) {
            notify(event, false, ex);
            throw new ServletException(ex);

        }
    }

    protected void notify(PortletInvocationEvent event, boolean pre, Throwable e) {
        PortalAdministrationService pas = PlutoServices.getServices().getPortalAdministrationService();

        for (PortletInvocationListener listener : pas.getPortletInvocationListeners()) {
            if (pre) {
                listener.onBegin(event);
            } else if (e == null) {
                listener.onEnd(event);
            } else {
                listener.onError(event, e);
            }
        }
    }

    private void initializeEventPortlet() {
        for (Map.Entry<String, Portlet> entry : portlets.entrySet()) {
            if (entry.getValue() instanceof EventPortlet) {
                eventPortlets.put(entry.getKey(), (EventPortlet) entry.getValue());
            } else {
                eventPortlets.put(entry.getKey(), new NullPortlet());
            }
        }
    }


    private void initializeResourceServingPortlet() {
        for (Map.Entry<String, Portlet> entry : portlets.entrySet()) {
            if (entry.getValue() instanceof ResourceServingPortlet) {
                resourceServingPortlets.put(entry.getKey(), (ResourceServingPortlet) entry.getValue());
            } else {
                resourceServingPortlets.put(entry.getKey(), new NullPortlet());
            }
        }
    }

    class ServletConfigWrapper implements ServletConfig {
        private ServletConfig deleguee;
        private String contextPath;

        ServletConfigWrapper(ServletConfig deleguee, String contextPath) {
            this.deleguee = deleguee;
            this.contextPath = contextPath;
        }

        public String getServletName() {
            return deleguee.getServletName();
        }

        public ServletContext getServletContext() {
            return new ServletContextWrapper(deleguee.getServletContext(), contextPath);
        }

        public String getInitParameter(String s) {
            return deleguee.getInitParameter(s);
        }

        public Enumeration getInitParameterNames() {
            return deleguee.getInitParameterNames();
        }
    }

    class ServletContextWrapper implements ServletContext {
        private ServletContext deleguee;
        private String contextPath;

        ServletContextWrapper(ServletContext deleguee, String contextPath) {
            this.deleguee = deleguee;
            this.contextPath = contextPath;
        }

        /**
         * This only gets called in Servlet API 2.5 and above. We need to find another way for Servlet API 2.4 and
         * earlier ?
         *
         * @return
         */
        public String getContextPath() {
            return Jahia.getContextPath() + "/" + contextPath;
        }

        public ServletContext getContext(String s) {
            return deleguee.getContext(s);
        }

        public int getMajorVersion() {
            return deleguee.getMajorVersion();
        }

        public int getMinorVersion() {
            return deleguee.getMinorVersion();
        }

        public String getMimeType(String s) {
            return deleguee.getMimeType(s);
        }

        public Set getResourcePaths(String s) {
            return deleguee.getResourcePaths(s);
        }

        public URL getResource(String s) throws MalformedURLException {
            return deleguee.getResource(s);
        }

        public InputStream getResourceAsStream(String s) {
            if (s.equals("/WEB-INF/portlet.xml")) {
                return deleguee.getResourceAsStream("/WEB-INF/fragments/" + contextPath + "/portlet.xml");
            }
            return deleguee.getResourceAsStream(s);
        }

        public RequestDispatcher getRequestDispatcher(String s) {
            return deleguee.getRequestDispatcher(s);
        }

        public RequestDispatcher getNamedDispatcher(String s) {
            return deleguee.getNamedDispatcher(s);
        }

        public Servlet getServlet(String s) throws ServletException {
            return deleguee.getServlet(s);
        }

        public Enumeration getServlets() {
            return deleguee.getServlets();
        }

        public Enumeration getServletNames() {
            return deleguee.getServletNames();
        }

        public void log(String s) {
            logger.info(s);
            deleguee.log(s);
        }

        public void log(Exception e, String s) {
            logger.error(s, e);
            deleguee.log(e, s);
        }

        public void log(String s, Throwable throwable) {
            logger.error(s, throwable);
            deleguee.log(s, throwable);
        }

        public String getRealPath(String s) {
            return deleguee.getRealPath(s);
        }

        public String getServerInfo() {
            return deleguee.getServerInfo();
        }

        public String getInitParameter(String s) {
            if (InitParameterApplicationIdResolver.CONTEXT_PATH_PARAM.equals(s)) {
                // this code is used internally by Pluto to determine the context if we are using a container
                // that is not Servlet API 2.5+ compliant.
                return new String(Jahia.getContextPath() + "/" + contextPath);
            }
            return deleguee.getInitParameter(s);
        }

        public Enumeration getInitParameterNames() {
            return deleguee.getInitParameterNames();
        }

        public Object getAttribute(String s) {
            return deleguee.getAttribute(s);
        }

        public Enumeration getAttributeNames() {
            return deleguee.getAttributeNames();
        }

        public void setAttribute(String s, Object o) {
            deleguee.setAttribute(s, o);
        }

        public void removeAttribute(String s) {
            deleguee.removeAttribute(s);
        }

        public String getServletContextName() {
            return deleguee.getServletContextName();
        }
    }
}
