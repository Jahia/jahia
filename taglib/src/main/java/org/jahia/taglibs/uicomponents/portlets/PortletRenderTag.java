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

package org.jahia.taglibs.uicomponents.portlets;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.content.JCRNodeWrapper;

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
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PortletRenderTag.class);

    private Node portletNode;
    private int windowId;

    public int doStartTag() {
        try {
            if(!(portletNode instanceof JCRNodeWrapper)){
                logger.error("portletNode must be an instance of JCRNodeWrapper");
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
            drawPortlet(new JCRPortletNode((JCRNodeWrapper) portletNode), windowId, pageContext.getOut(), pageContext.getServletContext());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }


    @Override
    public int doEndTag() throws JspException {
        super.doEndTag();
        portletNode = null;
        windowId=-1;
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
