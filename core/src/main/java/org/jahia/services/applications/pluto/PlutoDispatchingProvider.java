/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.applications.pluto;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.config.AdminConfiguration;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortalServletRequest;
import org.apache.pluto.driver.core.PortalServletResponse;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.DispatchingProvider;
import org.jahia.services.cache.ContainerHTMLCacheEntry;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.bin.Jahia;
import org.jahia.ajax.gwt.client.core.JahiaType;

import javax.portlet.WindowState;
import javax.portlet.MimeResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.jcr.*;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 15 juil. 2008
 * Time: 15:15:36
 * To change this template use File | Settings | File Templates.
 */
public class PlutoDispatchingProvider implements DispatchingProvider {

    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(PlutoDispatchingProvider.class);

    PortletContainer portletContainer;
    DriverConfiguration driverConfiguration;
    AdminConfiguration adminConfiguration;

    public void start() throws JahiaInitializationException {
        // Copied from org.apache.pluto.driver.PortalStartupListener

    }

    public void stop() {
    }

    public void processAction(EntryPointInstance entryPointInstance, int windowID, ParamBean jParams) throws JahiaException {
    }

    public String render(EntryPointInstance entryPointInstance, String windowID, ParamBean jParams) throws JahiaException {
        String cacheKey = null;
        final ContainerHTMLCache cacheInstance = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
        // Check if cache is available for this portlet
        if (entryPointInstance.getExpirationTime() != 0) {
            cacheKey = "portlet_instance_" + windowID;
            if (entryPointInstance.getCacheScope().equals(MimeResponse.PRIVATE_SCOPE)) {
                cacheKey += "_" + jParams.getUser().getUserKey();
                // Try to find the entry in cache
                final ContainerHTMLCacheEntry htmlCacheEntry2 =
                        cacheInstance.getFromContainerCache(null, jParams, cacheKey, false, 0, null, null);
                if (htmlCacheEntry2 != null) return htmlCacheEntry2.getBodyContent();
            }
        }
        JahiaContextRequest jahiaContextRequest = new JahiaContextRequest(jParams, jParams.getRealRequest());
        if (Jahia.getServletPath() != null &&
                !Jahia.getServletPath().equals(jahiaContextRequest.getServletPath())) {
            String pageURL = jParams.composePageUrl(jParams.getPageID());
            jahiaContextRequest.setServletPath(Jahia.getServletPath());
            pageURL = pageURL.substring(Jahia.getServletPath().length());
            jahiaContextRequest.setPathInfo(jParams.getPathInfo());
            jahiaContextRequest.setQueryString(jParams.getQueryString());
        }
        jahiaContextRequest.setEntryPointInstance(entryPointInstance);

        PortalRequestContext portalContext = new PortalRequestContext(jParams.getContext(), jahiaContextRequest, jParams.getResponse());

        // Retrieve the portlet window config for the evaluated portlet ID.
        ServletContext servletContext = jParams.getContext();

        final String defName = entryPointInstance.getDefName();
        PortletWindowConfig windowConfig =
                PortletWindowConfig.fromId((defName.startsWith(".") ? "/" : "") + defName + "!" + windowID);
        windowConfig.setContextPath(entryPointInstance.getContextName());
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering Portlet Window: " + windowConfig);
        }

        // Retrieve the current portal URL.
        PortalRequestContext portalEnv = PortalRequestContext.getContext(
                (HttpServletRequest) jahiaContextRequest);
        PortalURL portalURL = portalEnv.getRequestedPortalURL();

        // Create the portlet window to render.
        PortletWindow window = new PortletWindowImpl(windowConfig, portalURL);

        // Check if someone else is maximized. If yes, don't show content.
        Map windowStates = portalURL.getWindowStates();
        for (Iterator it = windowStates.keySet().iterator(); it.hasNext();) {
            String windowId = (String) it.next();
            WindowState windowState = (WindowState) windowStates.get(windowId);
            if (WindowState.MAXIMIZED.equals(windowState)
                    && !window.getId().getStringId().equals(windowId)) {
                return "";
            }
        }

        // Create portal servlet request and response to wrap the original
        // HTTP servlet request and response.
        PortalServletRequest portalRequest = new JahiaPortalServletRequest(jParams.getUser(), (HttpServletRequest) jParams.getRealRequest(), window);
        // todo we should only add these if we are dispatching in the same context as Jahia.
        copyAttribute("org.jahia.data.JahiaData", jParams, portalRequest, window);
        copyAttribute("currentRequest", jParams, portalRequest, window);
        copyAttribute("currentSite", jParams, portalRequest, window);
        copyAttribute("currentPage", jParams, portalRequest, window);
        copyAttribute("currentUser", jParams, portalRequest, window);
        copyAttribute("currentJahia", jParams, portalRequest, window);
        copyAttribute("jahia", jParams, portalRequest, window);
        copyAttribute("fieldId", jParams, portalRequest, window);

        // porlet properties
        try {
            Node node = ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(entryPointInstance.getID(), jParams.getUser());
            if (node != null) {
                PropertyIterator propertyIterator = node.getProperties();
                if (propertyIterator != null) {
                    while (propertyIterator.hasNext()) {
                        Property property = propertyIterator.nextProperty();
                        String name = property.getName();
                        Object theValue;
                        Value val = property.getValue();
                        switch (val.getType()) {
                            case PropertyType.BINARY:
                                theValue = val.getString();
                                break;
                            case PropertyType.BOOLEAN:
                                theValue = Boolean.valueOf(val.getBoolean());
                                break;
                            case PropertyType.DATE:
                                theValue = val.getDate();
                                break;
                            case PropertyType.DOUBLE:
                                theValue = Double.valueOf(val.getDouble());
                                break;
                            case PropertyType.LONG:
                                theValue = Long.valueOf(val.getLong());
                                break;
                            case PropertyType.NAME:
                                theValue = val.getString();
                                break;
                            case PropertyType.PATH:
                                theValue = val.getString();
                                break;
                            case PropertyType.REFERENCE:
                                theValue = val.getString();
                                break;
                            case PropertyType.STRING:
                                theValue = val.getString();
                                break;
                            case PropertyType.UNDEFINED:
                                theValue = val.getString();
                                break;
                            default:
                                theValue = val.getString();
                        }
                        portalRequest.setAttribute("Pluto_" + window.getId().getStringId() + "_" + name, theValue);

                    }
                }
            }

        } catch (RepositoryException e) {
            logger.error(e, e);
        }


        portalRequest.setAttribute("Pluto_" + window.getId().getStringId() + "_EntryPointInstance", entryPointInstance);
        PortalServletResponse portalResponse = new PortalServletResponse(
                (HttpServletResponse) jParams.getResponse());

        // Retrieve the portlet container from servlet context.
        PortletContainer container = (PortletContainer)
                servletContext.getAttribute(AttributeKeys.PORTLET_CONTAINER);

        // Render the portlet and cache the response.
        try {
            container.doRender(window, portalRequest, portalResponse);
            /** todo do something with the response */
        } catch (Exception th) {
            logger.error("Error while rendering portlet", th);
        }
        final String portletRendering = portalResponse.getInternalBuffer().getBuffer().toString();
        if (cacheKey != null) {
            cacheInstance.writeToContainerCache(null, jParams, portletRendering, cacheKey, new HashSet(), entryPointInstance.getExpirationTime());
        }
        return portletRendering;
    }

    private void copyAttribute(String attributeName, ProcessingContext processingContext, PortalServletRequest portalRequest, PortletWindow window) {
        Object objectToCopy = processingContext.getAttribute(attributeName);
        if (objectToCopy != null) {
            portalRequest.setAttribute("Pluto_" + window.getId().getStringId() + "_" + attributeName, objectToCopy);
        }

    }
}
