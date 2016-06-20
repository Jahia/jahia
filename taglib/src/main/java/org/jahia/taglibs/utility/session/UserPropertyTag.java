/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.taglibs.utility.session;

import java.io.IOException;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;

import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.slf4j.Logger;

/**
 * Simple tags that prints out the value of the specified property for currently logged in user.
 * If the property is not specified, the output will be the username.
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class UserPropertyTag extends AbstractJCRTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(UserPropertyTag.class);
    private String propertyName = null;

    public int doStartTag() throws JspException {
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            final StringBuilder buff = new StringBuilder();

            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append("<div class=\"");
                buff.append(cssClassName);
                buff.append("\">");
            }
            if (propertyName != null) {
                buff.append(getJCRSession().getUserNode().getPropertyAsString(propertyName));
            } else {
                buff.append(renderContext.getUser().getName());
            }
            if (cssClassName != null && cssClassName.length() > 0) {
                buff.append("</div>");
            }

            final JspWriter out = pageContext.getOut();
            out.print(buff.toString());

        } catch (final IOException e) {
            logger.error("IOException in UserPropertyTag", e);
        } catch (RepositoryException e) {
            logger.error("RepositoryException in UserPropertyTag", e);
        }

        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        propertyName = null;
        super.resetState();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
