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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.struts.taglib.TagUtils;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 déc. 2007
 * Time: 18:22:36
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class NodeTypeNameTag extends TagSupport {
    private String name = "type";
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
        ExtendedNodeType nodeType = (ExtendedNodeType) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        try {
            pageContext.getOut().print(nodeType.getName().replace(':','_'));
        } catch (IOException e) {
            throw new JspTagException(e);
        }
        return SKIP_BODY;

    }
}