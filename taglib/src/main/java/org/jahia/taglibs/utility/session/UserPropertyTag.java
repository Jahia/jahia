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
@SuppressWarnings("serial")
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
