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

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.params.ProcessingContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * @author Werner Assek
 */
public class LogAreaTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(LogAreaTag.class);
    public static final String LOGIN_TAG_PARAMETER = "loginFromTag";

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        if (!jData.gui().isLogged()) {
            try {
                final StringBuffer buff = new StringBuffer();
                buff.append("<form name=\"loginForm\" method=\"post\" action=\"").
                        append(jParams.composePageUrl(jParams.getPageID())).append("\">\n");
                buff.append("<input type=\"hidden\" name=\"" + LOGIN_TAG_PARAMETER + "\" value=\"1\" />\n");
                out.print(buff.toString());

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

        return EVAL_PAGE;
    }
}
