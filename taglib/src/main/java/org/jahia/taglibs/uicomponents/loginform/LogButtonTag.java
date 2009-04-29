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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * @author Werner Assek
 */
@SuppressWarnings("serial")
public class LogButtonTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(LogButtonTag.class);

    private String labelKey;
    public int tabIndex = 4;

    public String getLabelKey() {
        return labelKey;
    }

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public int doEndTag() throws JspException {
        final LogAreaTag logAreaTag = (LogAreaTag) findAncestorWithClass(this, LogAreaTag.class);
        if (logAreaTag == null) {
            logger.error("login-button tag must be used as a child of tag login-area");
            tabIndex = 4;
            labelKey = null;
            return EVAL_PAGE;
        }

        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (!jData.gui().isLogged()) {
            try {
                String label = getMessage(getLabelKey(), "Connect");

                String css = "";
                if (cssClassName != null && cssClassName.length() > 0) {
                    css = " class=\"" + cssClassName + "\"";
                }

                final StringBuffer buff = new StringBuffer();
                buff.append("<input type=\"submit\" name=\"login\" tabindex=\"").append(tabIndex).
                        append("\" value=\"").append(label).append("\" ").append(css).append(" />\n");
                out.print(buff.toString());

            } catch (IOException ioe) {
                logger.error("IO exception while trying to display login content login", ioe);
            }
        }
        tabIndex = 4;
        labelKey = null;
        return EVAL_PAGE;
    }
}
