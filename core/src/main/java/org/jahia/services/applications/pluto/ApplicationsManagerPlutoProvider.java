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
