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

package org.jahia.taglibs.utility.session;

import org.jahia.data.JahiaData;
import org.jahia.operations.valves.SkeletonAggregatorValve;
import org.jahia.taglibs.AbstractJahiaTag;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;

/**
 * Simple tags that prints out the user name of the currently logged user.
 *
 * @author Xavier Lawrence
 */
public class UserPropertyTag extends AbstractJahiaTag {

    private static final transient Logger logger = Logger.getLogger(UserPropertyTag.class);
    private String propertyName = null;

    public int doStartTag() throws JspException {
        try {
            final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final StringBuffer buff = new StringBuffer();

            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append("<div class=\"");
                buff.append(cssClassName);
                buff.append("\">");
            }
            if (propertyName != null) {
                buff.append("<!-- cache:vars var=\"")
                        .append(SkeletonAggregatorValve.ESI_VARIABLE_USER).append(".").append(propertyName)
                        .append("\" -->")
                        .append(jData.getProcessingContext().getUser().getProperty(propertyName))
                        .append("<!-- /cache:vars -->");
            } else {
                buff.append("<!-- cache:vars var=\"")
                        .append(SkeletonAggregatorValve.ESI_VARIABLE_USERNAME)
                        .append("\" -->")
                        .append(jData.getGui().drawUsername(true))
                        .append("<!-- /cache:vars -->");
            }
            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append("</div>");
            }

            final JspWriter out = pageContext.getOut();
            out.print(buff.toString());

        } catch (final IOException e) {
            logger.error("IOException in UserPropertyTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        cssClassName = null;
        return EVAL_PAGE;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
