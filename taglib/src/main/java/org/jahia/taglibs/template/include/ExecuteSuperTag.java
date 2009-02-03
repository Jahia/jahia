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
