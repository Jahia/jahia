/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.taglibs.uicomponents.portlets;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * Custom tag for rendering a specified portlet.
 * User: ktlili
 * Date: Jul 10, 2009
 * Time: 10:46:15 AM
 */
public class PortletRenderTag extends AbstractJahiaTag {
    private static final long serialVersionUID = -7527784423504794081L;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PortletRenderTag.class);

    private Node portletNode;
    private int windowId;

    public int doStartTag() {
        try {
            if (!(portletNode instanceof JCRPortletNode)) {
                logger.error("portletNode must be an instance of JCRPortletNode");
                return SKIP_BODY;
            }
            if (windowId <= 0) {
                Integer globalId = (Integer) pageContext.getAttribute(this
                        .getClass().getName(), PageContext.REQUEST_SCOPE);
                if (globalId != null) {
                    globalId++;
                } else {
                    globalId = 1;
                }
                pageContext.setAttribute(this.getClass().getName(), globalId,
                        PageContext.REQUEST_SCOPE);
                windowId = globalId;
            }
            drawPortlet((JCRPortletNode) portletNode, windowId, pageContext.getOut(), pageContext.getServletContext());

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return SKIP_BODY;
    }


    @Override
    public int doEndTag() throws JspException {
        super.doEndTag();
        portletNode = null;
        windowId = -1;
        return EVAL_PAGE;
    }

    public Node getPortletNode() {
        return portletNode;
    }

    public void setPortletNode(Node portletNode) {
        this.portletNode = portletNode;
    }

    public int getWindowId() {
        return windowId;
    }

    public void setWindowId(int windowId) {
        this.windowId = windowId;
    }

    /**
     * draw portlet node
     *
     * @param jcrPortletNode
     * @return
     */
    public void drawPortlet(JCRPortletNode jcrPortletNode, int windowId, final JspWriter out, ServletContext servletContext) throws JahiaException, IOException, RepositoryException {

        String appID = null;
        try {
            appID = jcrPortletNode.getUUID();
        } catch (RepositoryException e) {
            throw new JahiaException("Error rendering portlet", "Error rendering portlet",
                    JahiaException.APPLICATION_ERROR, JahiaException.ERROR_SEVERITY, e);
        }

        logger.debug("Dispatching to portlet for appID=" + appID + "...");

        String portletOutput = ServicesRegistry.getInstance().getApplicationsDispatchService().getAppOutput(windowId, appID, getRenderContext().getUser(), getRenderContext().getRequest(), getRenderContext().getResponse(), servletContext, jcrPortletNode.getSession().getWorkspace().getName());

        // remove <html> tags that can break the page
        if (portletOutput != null) {
            try {
                portletOutput = (new RE("</?html>", RE.MATCH_CASEINDEPENDENT)).subst(portletOutput, "");
            } catch (RESyntaxException e) {
                logger.debug(".getValue, exception : " + e.toString());
            }
        } else {
            portletOutput = "";
        }
        out.print(portletOutput);
    }

}
