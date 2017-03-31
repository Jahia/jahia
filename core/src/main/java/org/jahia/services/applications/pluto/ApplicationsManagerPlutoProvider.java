/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
import org.slf4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

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
        try {
            PortletWindow window = new PortletWindowImpl(null,windowConfig, portalURL);

            return window;
        } catch (Exception e) {
            return null;
        }
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
            logger.error(e.getMessage());
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
