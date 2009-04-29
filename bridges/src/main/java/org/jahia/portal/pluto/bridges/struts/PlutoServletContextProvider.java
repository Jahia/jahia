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
package org.jahia.portal.pluto.bridges.struts;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest ;
import javax.servlet.http.HttpServletResponse;
import org.apache.portals.bridges.common.ServletContextProvider;
public class PlutoServletContextProvider implements ServletContextProvider{
    public ServletContext getServletContext(GenericPortlet portlet){
     //Empty place holder
     System.err.println("Please use PlutoStrutsPortlet instead of StrutsPortlet");
     return null;
    }
    public HttpServletRequest getHttpServletRequest(GenericPortlet portlet, PortletRequest request){
     //Empty place holder
     System.err.println("Please use PlutoStrutsPortlet instead of StrutsPortlet");
     return null;
    }
    public HttpServletResponse getHttpServletResponse(GenericPortlet portlet, PortletResponse response){
     //Empty place holder
     System.err.println("Please use PlutoStrutsPortlet instead of StrutsPortlet");
     return null;
    }
}