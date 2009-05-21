/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.portlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.portlet.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.Constants;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.core.*;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.internal.InternalPortletConfig;
import org.apache.pluto.internal.InternalPortletContext;
import org.apache.pluto.internal.InternalPortletRequest;
import org.apache.pluto.internal.InternalPortletResponse;
import org.apache.pluto.internal.impl.ActionRequestImpl;
import org.apache.pluto.internal.impl.ActionResponseImpl;
import org.apache.pluto.internal.impl.EventRequestImpl;
import org.apache.pluto.internal.impl.EventResponseImpl;
import org.apache.pluto.internal.impl.PortletContextImpl;
import org.apache.pluto.internal.impl.RenderRequestImpl;
import org.apache.pluto.internal.impl.RenderResponseImpl;
import org.apache.pluto.internal.impl.ResourceRequestImpl;
import org.apache.pluto.internal.impl.ResourceResponseImpl;
import org.apache.pluto.spi.FilterManager;
import org.apache.pluto.spi.optional.AdministrativeRequestListener;
import org.apache.pluto.spi.optional.PortalAdministrationService;
import org.apache.pluto.spi.optional.PortletInvocationEvent;
import org.apache.pluto.spi.optional.PortletInvocationListener;
import org.jahia.bin.Jahia;
import org.jahia.services.applications.pluto.JahiaPortalServletRequest;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 17, 2008
 * Time: 3:07:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class PortletServlet extends HttpServlet {


    // Private Member Variables ------------------------------------------------

    /**
     * The portlet name as defined in the portlet app descriptor.
     */
    private String portletName;

    /**
     * The portlet instance wrapped by this servlet.
     */
    private Map<String,Portlet> portlets = new HashMap<String,Portlet>();

    /**
     * The internal portlet context instance.
     */
    private InternalPortletContext portletContext;

    /**
     * The internal portlet config instance.
     */
    private Map<String, InternalPortletConfig> portletConfigs = new HashMap<String, InternalPortletConfig>();

    /** The Event Portlet instance (the same object as portlet) wrapped by this servlet. */
    private Map<String,EventPortlet>  eventPortlets = new HashMap<String, EventPortlet>();

    /** The resource serving portlet instance wrapped by this servlet. */
    private Map<String, ResourceServingPortlet> resourceServingPortlets = new HashMap<String, ResourceServingPortlet>();

    // HttpServlet Impl --------------------------------------------------------

    public String getServletInfo() {
        return "Pluto PortletServlet [" + portletName + "]";
    }

    /**
     * Initialize the portlet invocation servlet.
     * @throws ServletException  if an error occurs while loading portlet.
     */
    public void init() throws ServletException {

    	// Call the super initialization method.
    	super.init();

        // Retrieve the associated internal portlet context.
        PortletContextManager mgr = PortletContextManager.getManager();
        File f = new File(getServletContext().getRealPath("/WEB-INF/fragments"));
        File[] fragments = f.listFiles();

        for (int i = 0; i < fragments.length; i++) {
            try {
                File fragment = fragments[i];
                log("Processing fragment " + fragment);
                ServletConfig config = new ServletConfigWrapper(getServletConfig(), fragment.getName());
                String applicationId = mgr.register(config);
                portletContext = (InternalPortletContext) mgr.getPortletContext(applicationId);
                List portlets = ((PortletContextImpl)mgr.getPortletContext(applicationId)).getPortletApplicationDefinition().getPortlets();
                for (Iterator iterator = portlets.iterator(); iterator.hasNext();) {
                    PortletDD portletDD = (PortletDD) iterator.next();
                    portletConfigs.put( Jahia.getContextPath()+"/"+fragment.getName()+"."+portletDD.getPortletName(), (InternalPortletConfig) mgr.getPortletConfig(applicationId, portletDD.getPortletName()));
                }
            } catch (PortletContainerException ex) {
                log("Error while registering portlet", ex);
            }
        }

        for (Map.Entry<String, InternalPortletConfig> entry : portletConfigs.entrySet()) {
            InternalPortletConfig portletConfig = entry.getValue();
            PortletDD portletDD = portletConfig.getPortletDefinition();

            // Create and initialize the portlet wrapped in the servlet.
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class clazz = loader.loadClass((portletDD.getPortletClass()));
                Portlet portlet = (Portlet) clazz.newInstance();

                String rootPath = portletConfig.getInitParameter("rootPath");
                String realPath = portletConfig.getPortletContext().getRealPath(rootPath + "/definitions.cnd" );
                if (new File(realPath).exists()) {
                    try {
                        NodeTypeRegistry.getInstance().addDefinitionsFile(new File(realPath), portletConfig.getPortletName(), true);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                portlet.init(portletConfig);

                portlets.put(entry.getKey(), portlet);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
                throw new ServletException(ex);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                throw new ServletException(ex);
            } catch (InstantiationException ex) {
                ex.printStackTrace();
                throw new ServletException(ex);
            } catch (PortletException ex) {
                ex.printStackTrace();
                throw new ServletException(ex);
            }
        }
        initializeEventPortlet();
        initializeResourceServingPortlet();
    }

    public void destroy() {
        PortletContextManager.getManager().remove(portletContext);
        for (Portlet portlet : portlets.values()) {
            portlet.destroy();
        }
        super.destroy();
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
    throws ServletException, IOException {
        dispatch(request, response);
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
    throws ServletException, IOException {
        dispatch(request, response);
    }

    protected void doPut(HttpServletRequest request,
                         HttpServletResponse response)
    throws ServletException, IOException {
        dispatch(request, response);
    }


    // Private Methods ---------------------------------------------------------

    /**
     * Dispatch the request to the appropriate portlet methods. This method
     * assumes that the following attributes are set in the servlet request
     * scope:
     * <ul>
     *   <li>METHOD_ID: indicating which method to dispatch.</li>
     *   <li>PORTLET_REQUEST: the internal portlet request.</li>
     *   <li>PORTLET_RESPONSE: the internal portlet response.</li>
     * </ul>
     *
     * @param request  the servlet request.
     * @param response  the servlet response.
     * @throws ServletException
     * @throws IOException
     */
    private void dispatch(HttpServletRequest request,
                          HttpServletResponse response)
    throws ServletException, IOException {
        InternalPortletRequest portletRequest = null;
        InternalPortletResponse portletResponse = null;
        // Save portlet config into servlet request.

        String id = ((JahiaPortalServletRequest)request).getId();
        String name = id.substring(0, id.indexOf("!"));

        Portlet portlet = portlets.get(name);
        EventPortlet eventPortlet = eventPortlets.get(name);
        ResourceServingPortlet resourceServingPortlet = resourceServingPortlets.get(name);
        InternalPortletConfig portletConfig = portletConfigs.get(name);

        request.setAttribute(Constants.PORTLET_CONFIG, portletConfig);

        // Retrieve attributes from the servlet request.
        Integer methodId = (Integer) request.getAttribute(
            Constants.METHOD_ID);

        portletRequest = (InternalPortletRequest) request.getAttribute(
            Constants.PORTLET_REQUEST);

        portletResponse = (InternalPortletResponse) request.getAttribute(
            Constants.PORTLET_RESPONSE);

        FilterManager filterManager = (FilterManager) request.getAttribute(Constants.FILTER_MANAGER);

        portletRequest.init(portletContext, request);

        PortletWindow window =
            ContainerInvocation.getInvocation().getPortletWindow();

        PortletInvocationEvent event =
            new PortletInvocationEvent(portletRequest, window, methodId.intValue());

        notify(event, true, null);

//      Init the classloader for the filter and get the Service for processing the filters.
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
//        FilterManager filtermanager = (FilterManager) request.getAttribute(
//        		"filter-manager");

        try {

            // The requested method is RENDER: call Portlet.render(..)
            if (methodId.equals(Constants.METHOD_RENDER)) {
                RenderRequestImpl renderRequest =
                		(RenderRequestImpl) portletRequest;
                RenderResponseImpl renderResponse =
                    	(RenderResponseImpl) portletResponse;
                filterManager.processFilter(renderRequest, renderResponse, loader, portlet,portletContext);
            }

            //The requested method is RESOURCE: call ResourceServingPortlet.serveResource(..)
            else if (methodId.equals(Constants.METHOD_RESOURCE)) {
            	ResourceRequestImpl resourceRequest =
                    	(ResourceRequestImpl) portletRequest;
            	ResourceResponseImpl resourceResponse =
                    	(ResourceResponseImpl) portletResponse;
            	filterManager.processFilter(resourceRequest, resourceResponse, loader, resourceServingPortlet,portletContext);
            }

            // The requested method is ACTION: call Portlet.processAction(..)
            else if (methodId.equals(Constants.METHOD_ACTION)) {
                ActionRequestImpl actionRequest =
                    	(ActionRequestImpl) portletRequest;
                ActionResponseImpl actionResponse =
                    	(ActionResponseImpl) portletResponse;
                filterManager.processFilter(actionRequest, actionResponse, loader, portlet,portletContext);
            }

            //The request methode is Event: call Portlet.processEvent(..)
            else if (methodId.equals(Constants.METHOD_EVENT)){
            	EventRequestImpl eventRequest =
                	(EventRequestImpl) portletRequest;
            	EventResponseImpl eventResponse =
                	(EventResponseImpl) portletResponse;
            	filterManager.processFilter(eventRequest, eventResponse, loader, eventPortlet,portletContext);
            }
            // The requested method is ADMIN: call handlers.
            else if (methodId.equals(Constants.METHOD_ADMIN)) {
                ContainerInvocation inv = ContainerInvocation.getInvocation();
                PortalAdministrationService pas =
                    inv.getPortletContainer()
                        .getOptionalContainerServices()
                        .getPortalAdministrationService();

                Iterator it = pas.getAdministrativeRequestListeners().iterator();
                while (it.hasNext()) {
                    AdministrativeRequestListener l = (AdministrativeRequestListener) it.next();
                    l.administer(portletRequest, portletResponse);
                }
            }

            // The requested method is NOOP: do nothing.
            else if (methodId.equals(Constants.METHOD_NOOP)) {
                // Do nothing.
            }

            notify(event, false, null);


        } catch (javax.portlet.UnavailableException ex) {
            ex.printStackTrace();
            /*
            if (e.isPermanent()) {
                throw new UnavailableException(e.getMessage());
            } else {
                throw new UnavailableException(e.getMessage(), e.getUnavailableSeconds());
            }*/

            // Portlet.destroy() isn't called by Tomcat, so we have to fix it.
            try {
                portlet.destroy();
            } catch (Throwable th) {
                // Don't care for Exception
            }

            // TODO: Handle everything as permanently for now.
            throw new javax.servlet.UnavailableException(ex.getMessage());

        } catch (PortletException ex) {
            notify(event, false, ex);
            ex.printStackTrace();
            throw new ServletException(ex);

        } finally {
            request.removeAttribute(Constants.PORTLET_CONFIG);
            if (portletRequest != null) {
            	portletRequest.release();
            }
        }
    }


    protected void notify(PortletInvocationEvent event, boolean pre, Throwable e) {
        ContainerInvocation inv = ContainerInvocation.getInvocation();
        PortalAdministrationService pas = inv.getPortletContainer()
            .getOptionalContainerServices()
            .getPortalAdministrationService();

        Iterator i = pas.getPortletInvocationListeners().iterator();
        while (i.hasNext()) {
            PortletInvocationListener listener = (PortletInvocationListener) i.next();
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
                    eventPortlets.put(entry.getKey(),(EventPortlet) entry.getValue());
            }
            else{
                eventPortlets.put(entry.getKey(),new NullPortlet());
            }
        }
    }

    private void initializeResourceServingPortlet() {
        for (Map.Entry<String, Portlet> entry : portlets.entrySet()) {
            if (entry.getValue() instanceof ResourceServingPortlet) {
                    resourceServingPortlets.put(entry.getKey(),(ResourceServingPortlet) entry.getValue());
            }
            else{
                resourceServingPortlets.put(entry.getKey(),new NullPortlet());
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
         * @return
         */
        public String getContextPath() {
            return Jahia.getContextPath()+"/"+contextPath;
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
                return deleguee.getResourceAsStream("/WEB-INF/fragments/"+contextPath+"/portlet.xml");
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
            deleguee.log(s);
        }

        public void log(Exception e, String s) {
            deleguee.log(e, s);
        }

        public void log(String s, Throwable throwable) {
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
                return new String(Jahia.getContextPath()+"/"+contextPath);
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