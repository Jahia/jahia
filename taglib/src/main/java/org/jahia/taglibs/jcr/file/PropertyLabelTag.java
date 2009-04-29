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
package org.jahia.taglibs.jcr.file;

import org.apache.log4j.Logger;
import org.apache.struts.taglib.TagUtils;
import org.jahia.data.JahiaData;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 déc. 2007
 * Time: 18:21:57
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PropertyLabelTag extends TagSupport {
    
    private static final transient Logger logger = Logger
            .getLogger(PropertyLabelTag.class);
    
    private String name = "propertyDefinition";
    private String property;
    private String scope;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public int doStartTag() throws JspException {
        ExtendedItemDefinition itemDef = (ExtendedItemDefinition) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        ServletRequest request = pageContext.getRequest();
        JspWriter out = pageContext.getOut();

        JahiaData jData = (JahiaData) request.getAttribute(
            "org.jahia.data.JahiaData");

        String typelabel = itemDef.getLabel(jData.getProcessingContext().getLocale());

        try {
            out.print(typelabel);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return SKIP_BODY;
    }
}
