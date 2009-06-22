package org.jahia.portal.pluto.bridges.struts;

import org.apache.portals.bridges.common.ServletContextProvider;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.container.driver.DriverPortletContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletContext;

/**
 *
 * This class is reliant on the PlutoStrutsPortletServlet being setup in the web.xml file instead of the default
 * implementation.
 *
 * @author loom
 *         Date: Jun 19, 2009
 *         Time: 9:59:50 AM
 */
public class PlutoServletContextProvider implements ServletContextProvider {

    public ServletContext getServletContext(GenericPortlet genericPortlet) {
        return PlutoStrutsPortletServlet.getStaticServletContext();
    }

    public HttpServletRequest getHttpServletRequest(GenericPortlet genericPortlet, PortletRequest portletRequest) {
        return PlutoStrutsPortletServlet.getHttpServletRequest();
    }

    public HttpServletResponse getHttpServletResponse(GenericPortlet genericPortlet, PortletResponse portletResponse) {
        return PlutoStrutsPortletServlet.getHttpServletResponse();
    }
}
