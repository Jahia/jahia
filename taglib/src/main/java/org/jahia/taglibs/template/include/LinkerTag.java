/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.include;

import org.slf4j.Logger;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.UUID;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class LinkerTag extends TagSupport {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(LinkerTag.class);

    private String mixinType;

    private String property = "j:bindedComponent";

    public void setMixinType(String mixinType) {
        this.mixinType = mixinType;
    }

    public void setProperty(String propertyName) {
        this.property = propertyName;
    }

    public int doEndTag() throws JspException {
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            if (renderContext.isEditMode() && /*editable &&*/ !Boolean.TRUE.equals(renderContext.getRequest().getAttribute("inWrapper"))) {
                Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);

                String currentPath = currentResource.getNode().getPath();

                StringBuffer buffer = new StringBuffer();

                buffer.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ")
                        .append("id=\"module")
                        .append(UUID.randomUUID().toString())
                        .append("\" type=\"")
                        .append("linker")
                        .append("\" ");

                buffer
                        .append(" path=\"").append(currentPath)
                        .append("\" property=\"").append(property);
                if (mixinType != null) {
                    buffer
                            .append("\" mixinType=\"").append(mixinType);
                }
                buffer.append("\" ");

                buffer.append("></div>");

                pageContext.getOut().print(buffer.toString());
            }
        } catch (Exception e) {
            throw new JspException(e);
        } finally {
            property = null;
            mixinType = null;
        }
        return EVAL_PAGE;
    }


}