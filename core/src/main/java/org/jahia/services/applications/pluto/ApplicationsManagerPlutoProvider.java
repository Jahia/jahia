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
package org.jahia.services.applications.pluto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.descriptors.portlet.ExpirationCacheDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.internal.impl.PortletContextImpl;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.data.applications.PortletEntryPointDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.params.ParamBean;
import org.jahia.services.applications.ApplicationsManagerProvider;
import org.springframework.web.context.ServletContextAware;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 15 juil. 2008
 * Time: 15:54:24
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationsManagerPlutoProvider implements ApplicationsManagerProvider, ServletContextAware {

    private PortletContainer portletContainer;
    private ServletContext servletContext;

    public ApplicationsManagerPlutoProvider() {

    }

    public EntryPointInstance createEntryPointInstance(EntryPointDefinition entryPointDefinition) throws JahiaException {        
        final EntryPointInstance instance = new EntryPointInstance(null, entryPointDefinition.getContext(), entryPointDefinition.getName());
        if (entryPointDefinition instanceof PortletEntryPointDefinition) {
            PortletEntryPointDefinition portletEntryPointDefinition = (PortletEntryPointDefinition) entryPointDefinition;            
            final ExpirationCacheDD expirationCacheDD = portletEntryPointDefinition.getPortletDefinition().getExpirationCacheDD();
            if(expirationCacheDD!=null){
                instance.setExpirationTime(expirationCacheDD.getExpirationTime());
                instance.setCacheScope(expirationCacheDD.getScope());
            }
        }
        return instance;
    }

    public PortletWindow getPortletWindow(EntryPointInstance entryPointInstance, String windowID, ParamBean jParams) {

        JahiaContextRequest jahiaContextRequest = new JahiaContextRequest(jParams, jParams.getRealRequest());

        PortalRequestContext portalContext = new PortalRequestContext(jParams.getContext(), jahiaContextRequest, jParams.getResponse());

        PortletWindowConfig windowConfig =
            PortletWindowConfig.fromId(entryPointInstance.getContextName() + "."+entryPointInstance.getDefName() + "!" + windowID);
        windowConfig.setContextPath(entryPointInstance.getContextName());
        // Retrieve the current portal URL.
        PortalRequestContext portalEnv = PortalRequestContext.getContext(
                (HttpServletRequest) jahiaContextRequest);
        PortalURL portalURL = portalEnv.getRequestedPortalURL();

        // Create the portlet window to render.
        PortletWindow window = new PortletWindowImpl(windowConfig, portalURL);

        return window;  //To change body of implemented methods use File | Settings | File Templates.
    }


    public List getAppEntryPointDefinitions(ApplicationBean appBean) throws JahiaException {
        List result = new ArrayList();
        Iterator portletContextIterator = getPortletContainer().getOptionalContainerServices().getPortletRegistryService().getRegisteredPortletApplications();
        while (portletContextIterator.hasNext()) {
            PortletContextImpl portletContext = (PortletContextImpl) portletContextIterator.next();
            if (portletContext.getApplicationId().equals(appBean.getContext())) {
                List<PortletDD> portletList = portletContext.getPortletApplicationDefinition().getPortlets();
                for (PortletDD portlet: portletList) {
                    PortletEntryPointDefinition portletEntryPointDefinition = new PortletEntryPointDefinition(appBean.getID(), appBean.getContext(), portlet);
                    result.add(portletEntryPointDefinition);
                }
            }
        }
        return result;
    }

    public void start() throws JahiaInitializationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;

    }

    private PortletContainer getPortletContainer() {
        // Retrieve the portlet container from servlet context.
        portletContainer = (PortletContainer)
                servletContext.getAttribute(AttributeKeys.PORTLET_CONTAINER);
        return portletContainer;
    }
}
