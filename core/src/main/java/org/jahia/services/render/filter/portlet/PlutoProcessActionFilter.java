/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter.portlet;

import org.apache.commons.lang.StringUtils;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.pluto.JahiaPortletUtil;
import org.jahia.services.applications.pluto.JahiaUserRequestWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Rendering filter for portlet actions and resource serving requests.
 *  
 * @author ktlili
 */
public class PlutoProcessActionFilter extends AbstractFilter {
    private static final Logger logger = LoggerFactory.getLogger(PlutoProcessActionFilter.class);
    
    private boolean renderOnAction;
    
    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (renderOnAction) {
            return StringUtils.defaultString(performAction(previousOut, renderContext, resource, chain), previousOut);
        } else {
            return previousOut;
        }
    }

    @Override
    public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        return !renderOnAction ? performAction(null, renderContext, resource, chain) : null;
    }

    private String performAction(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        try {
            final JahiaUserRequestWrapper request = new JahiaUserRequestWrapper(renderContext.getUser(), renderContext.getRequest(), renderContext.getMainResource().getWorkspace());
            final HttpServletResponse response = renderContext.getResponse();
            final ServletContext servletContext = JahiaContextLoaderListener.getServletContext();
            final PortletContainer container = (PortletContainer) servletContext.getAttribute(AttributeKeys.PORTLET_CONTAINER);
            final PortalRequestContext portalRequestContext = new PortalRequestContext(servletContext, request, response);
            final PortalURL portalURL = portalRequestContext.getRequestedPortalURL();
            final String actionWindowId = portalURL.getActionWindow();
            final String resourceWindowId = portalURL.getResourceWindow();

            PortletWindowConfig actionWindowConfig = null;
            PortletWindowConfig resourceWindowConfig = null;

            if (resourceWindowId != null) {
                resourceWindowConfig = PortletWindowConfig.fromId(resourceWindowId);
            } else if (actionWindowId != null) {
                actionWindowConfig = PortletWindowConfig.fromId(actionWindowId);
            }

            // Action window config will only exist if there is an action request.
            if (actionWindowConfig != null) {
                PortletWindowImpl portletWindow = new PortletWindowImpl(container, actionWindowConfig, portalURL);
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing action request for window: " + portletWindow.getId().getStringId());
                }

                EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(actionWindowConfig.getMetaInfo(), renderContext.getMainResource().getWorkspace());
                if (entryPointInstance != null) {
                    request.setEntryPointInstance(entryPointInstance);
                } else {
                    logger.warn("Couldn't find related entryPointInstance, roles might not work properly !");
                }

                // copy jahia attibutes nested by the portlet
                JahiaPortletUtil.copyJahiaAttributes(entryPointInstance, renderContext.getRequest(), portletWindow, request, true, renderContext.getMainResource().getWorkspace());

                try {
                    container.doAction(portletWindow, request, renderContext.getResponse());
                    renderContext.setPortletActionRequest(!renderOnAction);
                    JahiaPortletUtil.copySharedMapFromPortletToJahia(renderContext.getRequest().getSession(), request, portletWindow);
                } catch (PortletContainerException ex) {
                    throw new ServletException(ex);
                } catch (PortletException ex) {
                    throw new ServletException(ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Action request processed, send a redirect.\n\n");

                }

                return "";
            }
            //Resource request
            else if (resourceWindowConfig != null) {
                PortletWindowImpl portletWindow = new PortletWindowImpl(container,
                        resourceWindowConfig, portalURL);
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing resource Serving request for window: " + portletWindow.getId().getStringId());
                }
                try {
                    container.doServeResource(portletWindow, request, response);
                    renderContext.setPortletActionRequest(!renderOnAction);
                } catch (PortletContainerException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new ServletException(ex);
                } catch (PortletException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new ServletException(ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Resource serving request processed.");
                }
                return "";
            }
        } catch (Exception t) {
            logger.error("Error while processing action", t);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(renderContext.getRequest().getRequestURI() + " is a renderURL");
        }
        return null;
    }
    
    public void setRenderOnAction(boolean renderOnAction) {
        this.renderOnAction = renderOnAction;
    }
}
