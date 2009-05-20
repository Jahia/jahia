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

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 déc. 2007
 * Time: 17:56:01
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class FileTag extends AbstractJahiaTag {
    private String name;
    private String property;
    private String scope;

    private JCRNodeWrapper file;

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

    public JCRNodeWrapper getFile() {
        return file;
    }

    public int doStartTag() throws JspException {
        file = (JCRNodeWrapper) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        name = null;
        property = null;
        scope = null;

        file = null;

        return EVAL_PAGE;
    }
}
