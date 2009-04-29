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

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.JahiaData;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class LogRememberMeTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(LogRememberMeTag.class);

    private String labelKey;
    private String labelCssClassName;
    public int tabIndex = 3;

    public void setLabelKey(String labelKey) {
        this.labelKey = labelKey;
    }

    public void setLabelCssClassName(String labelCssClassName) {
        this.labelCssClassName = labelCssClassName;
    }

    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    public int doStartTag() {
        final LogAreaTag logAreaTag = (LogAreaTag) findAncestorWithClass(this, LogAreaTag.class);
        if (logAreaTag == null) {
            logger.error("login-rememberme tag must be used as a child of tag login-area");
            return SKIP_BODY;
        }
        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (!jData.gui().isLogged()) {
            try {
                final String label = getMessage(labelKey, "Remember me");

                String css = "";
                if (cssClassName != null && !cssClassName.equals("")) {
                    css = " class=\"" + cssClassName + "\"";
                }
                final StringBuffer buff = new StringBuffer();
                buff.append("<label for=\"useCookie\" class=\"").append(labelCssClassName).append("\">").
                        append(label).append(":</label>\n");
                buff.append("<input type=\"checkbox\" name=\"useCookie\" tabindex=\"").
                        append(tabIndex).append("\"");
                buff.append(css);
                buff.append(" />\n");
                out.print(buff.toString());
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        labelCssClassName = null;
        labelKey = null;
        tabIndex = 3;
        return EVAL_PAGE;
    }
}
