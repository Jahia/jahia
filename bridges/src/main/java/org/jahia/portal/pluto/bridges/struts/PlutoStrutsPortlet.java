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
