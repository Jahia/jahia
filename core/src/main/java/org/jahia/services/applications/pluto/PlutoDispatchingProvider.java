/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

    @Override
    public void start() throws JahiaInitializationException {
        // Copied from org.apache.pluto.driver.PortalStartupListener
    }

    @Override
    public void stop() {
        // do nothing
    }

     @Override
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

        new PortalRequestContext(servletContext, jahiaContextRequest, httpServletResponse);

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
