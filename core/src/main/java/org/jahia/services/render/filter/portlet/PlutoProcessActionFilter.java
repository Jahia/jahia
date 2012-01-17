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

package org.jahia.services.render.filter.portlet;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.bin.Jahia;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.pluto.JahiaPortletUtil;
import org.jahia.services.applications.pluto.JahiaUserRequestWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.usermanager.JahiaUser;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * User: ktlili
 * Date: Dec 21, 2009
 * Time: 3:37:12 PM
 * 
 */
public class PlutoProcessActionFilter extends AbstractFilter {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PlutoProcessActionFilter.class);

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        try {
            final JahiaUserRequestWrapper request = new JahiaUserRequestWrapper(renderContext.getUser(), renderContext.getRequest(), renderContext.getMainResource().getWorkspace());
            final HttpServletResponse response = renderContext.getResponse();
            final ServletContext servletContext = Jahia.getStaticServletConfig().getServletContext();
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
                flushPortletCache(renderContext.getUser(), actionWindowConfig, renderContext.getMainResource().getWorkspace());
                PortletWindowImpl portletWindow = new PortletWindowImpl(container, actionWindowConfig, portalURL);
                //if (logger.isDebugEnabled()) {
                logger.debug("Processing action request for window: "
                        + portletWindow.getId().getStringId());
                //}

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
                } catch (PortletContainerException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new ServletException(ex);
                } catch (PortletException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new ServletException(ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Resource serving request processed.\n\n");
                }
                return "";
            }
        } catch (Exception t) {
            logger.error("Error while processing action", t);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(renderContext.getRequest().getRequestURI() + " is a renderURL");
        }
        return previousOut;
    }

    /**
     * Flush the portlet Cache
     * @param actionWindowConfig
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    private void flushPortletCache(JahiaUser user, PortletWindowConfig actionWindowConfig, String workspaceName) throws JahiaException {
        String cacheKey = null;
        // Check if cache is available for this portlet
        cacheKey = "portlet_instance_" + actionWindowConfig.getMetaInfo();
        final EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(actionWindowConfig.getMetaInfo(), workspaceName);
        if (entryPointInstance != null && entryPointInstance.getCacheScope() != null && entryPointInstance.getCacheScope().equals(MimeResponse.PRIVATE_SCOPE)) {
            cacheKey += "_" + user.getUserKey();
        }
    }
}
