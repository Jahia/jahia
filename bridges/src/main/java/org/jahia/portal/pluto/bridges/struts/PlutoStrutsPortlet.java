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
import org.apache.portals.bridges.struts.StrutsPortlet;
import org.apache.pluto.internal.impl.PortletConfigImpl;

public class PlutoStrutsPortlet extends StrutsPortlet{
 protected HttpServletRequest getHttpServletRequest(GenericPortlet portlet, PortletRequest request, PortletResponse response) {
     HttpServletRequest httpServletRequest =(HttpServletRequest) request.getAttribute("javax.portlet.request");
     return httpServletRequest;
 }
 protected HttpServletResponse getHttpServletResponse(GenericPortlet portlet, PortletRequest request, PortletResponse response) {
     HttpServletResponse httpServletResponse =(HttpServletResponse)request.getAttribute("javax.portlet.response");
     return httpServletResponse;
 }
 protected ServletContext getServletContext(GenericPortlet portlet, PortletRequest request, PortletResponse response) {
     PortletConfigImpl portletConfigImpl =(PortletConfigImpl)request.getAttribute("javax.portlet.config");
     ServletContext servletContext = portletConfigImpl.getServletConfig().getServletContext();
  return servletContext;
 }
}