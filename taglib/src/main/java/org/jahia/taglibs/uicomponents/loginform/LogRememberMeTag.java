/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
