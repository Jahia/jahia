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
