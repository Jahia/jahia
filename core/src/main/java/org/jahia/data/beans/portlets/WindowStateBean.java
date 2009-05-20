/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.data.beans.portlets;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class WindowStateBean {
    private String name;
    private ProcessingContext processingContext;
    private PortletWindowBean portletWindowBean;

    public WindowStateBean (ProcessingContext processingContext, PortletWindowBean portletWindowBean) {
        this.processingContext = processingContext;
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
        PortalRequestContext portalEnv = PortalRequestContext.getContext(
                (HttpServletRequest) ((ParamBean) processingContext).getRequest());

        PortalURL portalUrl =  portalEnv.createPortalURL();
        String portletWindowID = portletWindowBean.getPortletWindow().getId().getStringId();
        portalUrl.setWindowState(portletWindowID, new WindowState(name));
        // URL should look like this : http://localhost:8080/pluto/portal//Test%20Page/__ws0x3testsuite0x2TestPortlet1!764587357%7C0_normal?
        return "";
    }

}
