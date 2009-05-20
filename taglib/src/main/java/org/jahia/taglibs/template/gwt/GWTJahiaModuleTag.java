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
package org.jahia.taglibs.template.gwt;

import org.jahia.ajax.gwt.client.core.JahiaType;
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
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GWTJahiaModuleTag.class);
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
            logger.error(e, e);
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
