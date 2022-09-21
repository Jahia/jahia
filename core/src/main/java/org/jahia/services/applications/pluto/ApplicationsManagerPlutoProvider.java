/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
