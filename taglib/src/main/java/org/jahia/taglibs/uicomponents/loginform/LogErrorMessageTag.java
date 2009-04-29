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
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class LogErrorMessageTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(LogErrorMessageTag.class);

    private String invalidUsernamePasswordKey = "invalidUsernamePassword";
    private String valueID;

    public void setInvalidUsernamePasswordKey(String invalidUsernamePasswordKey) {
        this.invalidUsernamePasswordKey = invalidUsernamePasswordKey;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public int doStartTag() {
        final LogAreaTag logAreaTag = (LogAreaTag) findAncestorWithClass(this, LogAreaTag.class);
        if (logAreaTag == null) {
            logger.error("login-errormessage tag must be used as a child of tag login-area");
            return SKIP_BODY;
        }

        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        final String valveResult = (String) jParams.getAttribute(LoginEngineAuthValveImpl.VALVE_RESULT);
        if (!jData.gui().isLogged() && valveResult != null) {
            try {
                final StringBuffer buff = new StringBuffer();
                if (!LoginEngineAuthValveImpl.OK.equals(valveResult)) {
                    final String wrongLogin = getMessage(invalidUsernamePasswordKey, "Invalid username or password");
                    if (cssClassName != null && cssClassName.length() > 0) {
                        buff.append("<span class=\"").append(cssClassName).append("\">");
                        buff.append(wrongLogin);
                        buff.append("</span>\n");
                    } else {
                        buff.append(wrongLogin);
                    }
                }

                if (valueID == null || valueID.length() == 0) {
                    out.print(buff.toString());
                } else {
                    if (buff.length() > 0) pageContext.setAttribute(valueID, buff.toString());
                }
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
        return SKIP_BODY;
    }


    public int doEndTag() {
        cssClassName = null;
        invalidUsernamePasswordKey = "invalidUsernamePassword";
        valueID = null;
        return EVAL_PAGE;
    }
}
