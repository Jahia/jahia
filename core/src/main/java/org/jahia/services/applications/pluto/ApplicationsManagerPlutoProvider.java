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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PlutoServices;
import org.apache.pluto.container.driver.PortletRegistryService;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.PortletEntryPointDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.applications.ApplicationsManagerProvider;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Pluto-based implementation of the {@link ApplicationsManagerProvider}.
 * User: Serge Huber
 * Date: 15 juil. 2008
 * Time: 15:54:24
 */
public class ApplicationsManagerPlutoProvider implements ApplicationsManagerProvider {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ApplicationsManagerPlutoProvider.class);

    /**
     * Create an entryPointInstance from the entryPointDefinition
     *
     * @param entryPointDefinition EntryPointDefinition
     * @return
     * @throws JahiaException
     */
    public EntryPointInstance createEntryPointInstance(EntryPointDefinition entryPointDefinition) throws JahiaException {
        final EntryPointInstance instance = new EntryPointInstance(null, entryPointDefinition.getContext(), entryPointDefinition.getName());
        if (entryPointDefinition instanceof PortletEntryPointDefinition) {
            PortletEntryPointDefinition portletEntryPointDefinition = (PortletEntryPointDefinition) entryPointDefinition;
            instance.setExpirationTime(portletEntryPointDefinition.getExpirationCache());
            instance.setCacheScope(portletEntryPointDefinition.getCacheScope());
        }
        return instance;
    }

    /**
     * Get portlet window
     *
     * @param entryPointInstance
     * @param windowID
     * @return
     */
    public PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID,
                                          JahiaUser jahiaUser,
                                          HttpServletRequest httpServletRequest,
                                          HttpServletResponse httpServletResponse,
                                          ServletContext servletContext, String workspaceName) throws JahiaException {

        JahiaContextRequest jahiaContextRequest = new JahiaContextRequest(jahiaUser, httpServletRequest, workspaceName);

        new PortalRequestContext(servletContext, jahiaContextRequest, httpServletResponse);

        PortletWindowConfig windowConfig = PortletWindowConfig.fromId(entryPointInstance.getContextName() + "." + entryPointInstance.getDefName() + "!" + windowID);
        windowConfig.setContextPath(entryPointInstance.getContextName());

        // Retrieve the current portal URL.
        PortalRequestContext portalEnv = PortalRequestContext.getContext(jahiaContextRequest);
        PortalURL portalURL = portalEnv.getRequestedPortalURL();
        
        // Create the portlet window to render.
        PortletWindow window = new PortletWindowImpl(null,windowConfig, portalURL);

        return window;
    }

    /**
     * Get application list of entry definition
     *
     * @param appBean
     * @return
     * @throws JahiaException
     */
    public List<EntryPointDefinition> getAppEntryPointDefinitions(ApplicationBean appBean) throws JahiaException {
        // get the portlet registry
        PortletRegistryService portletRegistryService = PlutoServices.getServices().getPortletRegistryService();
        List<EntryPointDefinition> result = new ArrayList<EntryPointDefinition>();

        // get all portlet of the application bean
        try {
            List<? extends PortletDefinition> portletList = portletRegistryService.getPortletApplication(appBean.getContext()).getPortlets();
            for (PortletDefinition portlet : portletList) {
                PortletEntryPointDefinition portletEntryPointDefinition = new PortletEntryPointDefinition(appBean.getID(), appBean.getContext(), portlet);
                result.add(portletEntryPointDefinition);
            }
        } catch (PortletContainerException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    public void start() throws JahiaInitializationException {
        // do nothing
    }

    public void stop() {
        // do nothing
    }
}
