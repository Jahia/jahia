/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.owasp.encoder.Encode;
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
                buff.append(Encode.forHtml(renderContext.getUser().getName()));
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
