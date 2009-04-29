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
package org.jahia.taglibs.template.include;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;

/**
 * Tag handler for executing a JSP with the same name as the current one from
 * the parent template set. Consider this tag as an equivalent of the Java
 * constructor overriding:
 * 
 * <pre>
 * public class Child extends Parent {
 * 
 *     public Child() {
 *         super();
 *         // initialization of this instance
 *     }
 * }
 * </pre>
 * 
 * This tag does the same as the call <code>super()</code> in the example
 * above.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class ExecuteSuperTag extends IncludeTag {

    private boolean found;

    @Override
    public int doEndTag() throws JspException {
        return found ? super.doEndTag() : EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        found = false;
        HttpServletRequest request = (HttpServletRequest) pageContext
                .getRequest();
        String jspPath = (String) (request
                .getAttribute("javax.servlet.include.servlet_path") != null ? request
                .getAttribute("javax.servlet.include.servlet_path")
                : request.getServletPath());

        if (jspPath != null) {
            String path = getTemplatePathResolver().lookupOverridden(jspPath);
            if (path != null) {
                this.url = path;
                found = true;
            }
        }

        return found ? super.doStartTag() : SKIP_BODY;
    }

}
