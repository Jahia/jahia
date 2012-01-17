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
    private HttpServletRequest httpServletRequest;
    private PortletWindowBean portletWindowBean;

    public WindowStateBean (HttpServletRequest httpServletRequest, PortletWindowBean portletWindowBean) {
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
        portalUrl.setWindowState(portletWindowID, new WindowState(name));
        // URL should look like this : http://localhost:8080/pluto/portal//Test%20Page/__ws0x3testsuite0x2TestPortlet1!764587357%7C0_normal?
        return "";
    }

}
