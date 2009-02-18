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

package org.jahia.taglibs.utility.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.templatestructure.TemplateTag;
import org.jahia.ajax.gwt.client.util.ResourceBundle;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;

/**
 * Create a resource bundle dictionary
 */
public class GWTResourceBundleDictionaryTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(GWTResourceBundleDictionaryTag.class);

    private String moduleType = ResourceBundle.RESOURCE_BUNDLE_MODULE_TYPE;
    private String elementId = ResourceBundle.APPLICATION_RESOURCE_ELEMENT_ID;
    private boolean enableGwt = true ;

    /**
     * Return the resource bundle module type.
     * By default uses <code>ResourceBundle.RESOURCE_BUNDLE_MODULE_TYPE</code>
     *
     * @return
     */
    public String getModuleType() {
        return moduleType;
    }

    public void setModuleType(String moduleType) {
        this.moduleType = moduleType;
    }

    /**
     * Return the resource bundle element id.
     * By default uses <code>ResourceBundle.APPLICATION_RESOURCE_ELEMENT_ID</code>
     *
     * @return
     */
    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        Tag tag = findAncestorWithClass(this, TemplateTag.class) ;
        if (tag != null && tag instanceof TemplateTag) {
            if(!isLogged() && !((TemplateTag) tag).enableGwtForGuest()) {
                enableGwt = false ;
                return SKIP_BODY ;
            }
        }
        try {
            // add jahia parameter dictonary
            StringBuffer buf = new StringBuffer();
            if (getModuleType() != null && getElementId() != null) {
                buf.append("<script type='text/javascript'>\n");
                buf.append("var " + getModuleType().toLowerCase() + "_rb_" + getElementId().toLowerCase() + " = {");
                out.print(buf.toString());
                return EVAL_BODY_INCLUDE;
            } else {
                logger.error("module type or elementId are not defined.");
            }
        } catch (IOException e) {
            logger.error(e, e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        if (!enableGwt) {
            return EVAL_PAGE ;
        }
        final JspWriter out = pageContext.getOut();
        // print output
        try {
            if (getModuleType() != null && getElementId() != null) {
                out.append("};");
                out.append("\n</script>\n");
            }
            // validate
            if (getElementId() == null) {
                out.print("<!-- gwt-resourceBundleDictionary: 'elementId' must be not null-->");
                return EVAL_PAGE;
            }
        } catch (IOException e) {
            logger.error(e, e);
        }

        /**
         *
         */
        pageContext.getRequest().removeAttribute("org.jahia.ajax.gwt.rb");
        moduleType = null;
        elementId = null;
        enableGwt = true ;
        return EVAL_PAGE;
    }

}