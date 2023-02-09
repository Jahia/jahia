/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
     * @param jcrPortletNode the corresponding portlet JCR node
     * @param windowId the window ID
     * @param out the current JSP writer instance
     * @param servletContext  the Servlet context instance
     * @throws JahiaException in case of DX specific error
     * @throws IOException in case of an I/O error
     * @throws RepositoryException in case of JCR-related errors
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
