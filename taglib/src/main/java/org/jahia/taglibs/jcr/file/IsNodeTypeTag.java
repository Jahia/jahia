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

import org.apache.struts.taglib.TagUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 8, 2008
 * Time: 5:20:36 PM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class IsNodeTypeTag extends AbstractJahiaTag {
    private String name = "file";
    private String property;
    private String scope;

    private String type;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int doStartTag() throws JspException {
        JCRNodeWrapper file = null;

        FileTag ft = (FileTag) findAncestorWithClass(this, FileTag.class);
        if (ft != null) {
            file = ft.getFile();
        } else {
            file = (JCRNodeWrapper) TagUtils.getInstance().lookup(pageContext, name, property, scope);
        }

        try {
            if (file != null && file.isNodeType(type)) {
                return EVAL_BODY_INCLUDE;
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        name = "file";
        property = null;
        scope = null;
        type = null;

        return EVAL_PAGE;
    }
}
