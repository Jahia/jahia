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

package org.jahia.services.applications.pluto;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.config.AdminConfiguration;
import org.apache.pluto.driver.config.DriverConfiguration;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortalServletResponse;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;
import org.jahia.services.applications.DispatchingProvider;
import org.jahia.services.usermanager.JahiaUser;

import javax.portlet.MimeResponse;
import javax.portlet.WindowState;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * User: Serge Huber
 * Date: 15 juil. 2008
 * Time: 15:15:36
 * 
 */
public class PlutoDispatchingProvider implements DispatchingProvider {

    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
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

    public String render(EntryPointInstance entryPointInstance, String windowID, JahiaUser jahiaUser,
                         HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse,
                         ServletContext servletContext, String workspaceName) throws JahiaException {
        String cacheKey = null;
//        final ContainerHTMLCache cacheInstance = ServicesRegistry.getInstance().getCacheService().getContainerHTMLCacheInstance();
        // Check if cache is available for this portlet
        if (entryPointInstance.getExpirationTime() != 0) {
            cacheKey = "portlet_instance_" + windowID;
            if (entryPointInstance.getCacheScope().equals(MimeResponse.PRIVATE_SCOPE)) {
                cacheKey += "_" + jahiaUser.getUserKey();
                // Try to find the entry in cache
//                final ContainerHTMLCacheEntry htmlCacheEntry2 =
//                        cacheInstance.getFromContainerCache(null, jParams, cacheKey, false, 0, null, null);
//                if (htmlCacheEntry2 != null) return htmlCacheEntry2.getBodyContent();
            }
        }
        JahiaContextRequest jahiaContextRequest = new JahiaContextRequest(jahiaUser, httpServletRequest, workspaceName);
        jahiaContextRequest.setEntryPointInstance(entryPointInstance);

        PortalRequestContext portalContext = new PortalRequestContext(servletContext, jahiaContextRequest, httpServletResponse);

        final String defName = entryPointInstance.getDefName();
        PortletWindowConfig windowConfig = PortletWindowConfig.fromId((defName.startsWith(".") ? "/" : "") + defName + "!" + windowID);
        windowConfig.setContextPath(entryPointInstance.getContextName());
        if (logger.isDebugEnabled()) {
            logger.debug("Rendering Portlet Window: " + windowConfig);
        }

        // Retrieve the current portal URL.
        PortalRequestContext portalEnv = PortalRequestContext.getContext(jahiaContextRequest);
        PortalURL portalURL = portalEnv.getRequestedPortalURL();

        // Retrieve the portlet container from servlet context.
        PortletContainer container = (PortletContainer) servletContext.getAttribute(AttributeKeys.PORTLET_CONTAINER);

        // Create the portlet window to render.
        PortletWindow window = new PortletWindowImpl(container, windowConfig, portalURL);

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
        HttpServletRequest portalRequest = new JahiaPortalServletRequest(entryPointInstance, jahiaUser, httpServletRequest, window, workspaceName);


        // copy jahia attibutes nested by the portlet
        JahiaPortletUtil.copyJahiaAttributes(entryPointInstance, httpServletRequest, window, portalRequest, false, workspaceName);

        // wrappe in a portal response
        PortalServletResponse portalResponse = new JahiaPortalServletResponse(httpServletResponse);

        // Render the portlet and cache the response.
        try {
            Map<String, Object> map = JahiaPortletUtil.filterJahiaAttributes(portalRequest);
            container.doRender(window, portalRequest, portalResponse);
            JahiaPortletUtil.setJahiaAttributes(portalRequest, map);
        } catch (Exception th) {
            logger.error("Error while rendering portlet", th);
        }
        final String portletRendering = portalResponse.getInternalBuffer().getBuffer().toString();
        if (cacheKey != null) {
//            cacheInstance.writeToContainerCache(null, jParams, portletRendering, cacheKey, new HashSet(), entryPointInstance.getExpirationTime());
        }
        return portletRendering;
    }


}
