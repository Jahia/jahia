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

package org.jahia.portal.pluto.bridges.struts;

import org.apache.portals.bridges.struts.StrutsPortlet;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * We also had to subclass the StrutsPortlet class because it is not compliant with the new Portlet API 2.0 way
 * of doing things. Ideally the Struts bridge should be updated to use proper portlet request dispatchers instead of
 * using servlet context dispatchers (if possible of course).
 *
 * @author loom
 *         Date: Jun 22, 2009
 *         Time: 1:13:56 PM
 */
public class PlutoStrutsPortlet extends StrutsPortlet {

    @Override
    protected void processRequest(PortletRequest portletRequest, PortletResponse portletResponse, String defaultPage, String requestType) throws PortletException, IOException {
        HttpServletRequest req = getHttpServletRequest(this, portletRequest, portletResponse);
        if ( req.getAttribute("javax.portlet.config") == null )
        {
            req.setAttribute("javax.portlet.config", getPortletConfig());
        }
        if ( req.getAttribute("javax.portlet.request") == null )
        {
            req.setAttribute("javax.portlet.request", portletRequest);
        }
        if ( req.getAttribute("javax.portlet.response") == null )
        {
            req.setAttribute("javax.portlet.response", portletResponse);
        }
        super.processRequest(portletRequest, portletResponse, defaultPage, requestType);
    }
}
