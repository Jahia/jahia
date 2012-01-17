/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.template.gwt;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.DynamicAttributes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: jahia
 * Date: 2 avr. 2008
 * Time: 14:43:29
 */
@SuppressWarnings("serial")
public class GWTJahiaModuleTag extends AbstractJahiaTag implements DynamicAttributes {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GWTJahiaModuleTag.class);
    private transient Map<String, Object> attributes = new HashMap<String, Object>();

    private String id;
    private String jahiaType;
    private boolean templateUsage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJahiaType() {
        return jahiaType;
    }

    public void setJahiaType(String jahiaType) {
        this.jahiaType = jahiaType;
    }

    public void setTemplateUsage(boolean templateUsage) {
        this.templateUsage = templateUsage;
    }

    public int doStartTag() throws JspException {

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        final JspWriter out = pageContext.getOut();
        // print output
        try {
         
            // validate
            if (id == null) {
                out.print("<!-- gwt-jahiamodule: 'id' must be not null-->");
                return EVAL_PAGE;
            }

            // print the place holder
            out.print(GWTIncluder.generateJahiaModulePlaceHolder(templateUsage,getCssClassName(),jahiaType,id,attributes));

        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

        id = null;
        jahiaType = null;
        attributes = new HashMap<String, Object>();
        return EVAL_PAGE;
    }



    public void setDynamicAttribute(String uri, String localName, Object value) throws JspException {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(localName, value);
    }



}
