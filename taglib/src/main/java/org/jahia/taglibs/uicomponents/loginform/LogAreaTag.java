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
package org.jahia.taglibs.uicomponents.loginform;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Tag responsible for rendering an HTML form element for login area.
 * 
 * @author Werner Assek
 */
@SuppressWarnings("serial")
public class LogAreaTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(LogAreaTag.class);
    
    private String name;

    private boolean doRedirect = true;

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        if (!jData.gui().isLogged()) {
            try {
                out.append("<form name=\"").append(name != null ? name : "loginForm").append("\"");
                if (getId() != null) {
                    out.append(" id=\"").append(getId()).append("\"");
                }
                out.append(" method=\"post\" action=\"").
                        append(jParams.composePageUrl(jParams.getPageID())).append("\">\n");
                out.append("<input type=\"hidden\" name=\""
                        + LoginEngineAuthValveImpl.LOGIN_TAG_PARAMETER
                        + "\" value=\"1\" />\n");
                if (doRedirect) {
                    out.append("<input type=\"hidden\" name=\""
                            + LoginEngineAuthValveImpl.DO_REDIRECT
                            + "\" value=\"true\" />\n");
                }
            } catch (IOException ioe) {
                logger.error("IO exception while trying to display login content login", ioe);
            } catch (JahiaException ex) {
                logger.error("Can not get login URL !", ex);
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (!jData.gui().isLogged()) {
            final JspWriter out = bodyContent.getEnclosingWriter();
            try {
                bodyContent.writeOut(out);
            } catch (IOException ioe) {
                logger.error("Error:", ioe);
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        if (!jData.gui().isLogged()) {
            try {
                out.println("</form>");
            } catch (IOException ioe) {
                logger.error("IO exception while trying to display login content login", ioe);
            }
        }

        resetState();
        
        return EVAL_PAGE;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDoRedirect(boolean doRedirect) {
        this.doRedirect = doRedirect;
    }

    @Override
    protected void resetState() {
        super.resetState();
        name = null;
        doRedirect = true;
        id = null;
    }
    
}
