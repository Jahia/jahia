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
 package org.jahia.data.beans.portlets;

import javax.portlet.PortletMode;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.url.PortalURL;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class PortletModeBean {

    private String name;
    private HttpServletRequest httpServletRequest;
    private PortletWindowBean portletWindowBean;

    public PortletModeBean (HttpServletRequest httpServletRequest, PortletWindowBean portletWindowBean) {
        this.httpServletRequest = httpServletRequest;
        this.portletWindowBean = portletWindowBean;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getURL () {
        // Retrieve the portal environment.
        PortalRequestContext portalEnv = PortalRequestContext.getContext(httpServletRequest);

        PortalURL portalUrl =  portalEnv.createPortalURL();
        String portletWindowID = portletWindowBean.getPortletWindow().getId().getStringId();
        portalUrl.setPortletMode(portletWindowID, new PortletMode(name));
        // URL should be like this (taken from Pluto RI 286 portal : http://localhost:8080/pluto/portal//Test%20Page/__pm0x3testsuite0x2TestPortlet1!764587357%7C0_edit? )
        return portalUrl.toURL(false);
    }

}
