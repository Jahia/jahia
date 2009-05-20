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
package org.jahia.taglibs.template.form;

import org.jahia.taglibs.template.gwt.GWTJahiaModuleTag;
import org.jahia.ajax.gwt.client.core.JahiaType;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * <template:gwtJahiaModule isTemplate="false" jahiaType="form" id="form" nodeType="jnt:mainContentContainer"
 * action="createNode" target="${currentPage.JCRPath}/maincontent">
 * </template:gwtJahiaModule>
 *
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class FormFromDefinition extends GWTJahiaModuleTag {

    private String nodeType;
    private String target;
    private static final String CREATE_NODE = "createNode";

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int doStartTag() throws JspException {
        super.setTemplateUsage(true);
        super.setJahiaType(JahiaType.FORM);
        super.setId("FormFromDefinition" + nodeType + System.currentTimeMillis());
        super.setDynamicAttribute("action", "action", CREATE_NODE);

        if (nodeType.length() > 0)
            super.setDynamicAttribute("nodeType", "nodeType", nodeType);

        if (target.length() > 0)
            super.setDynamicAttribute("target", "target", target);

        return super.doStartTag();
    }


    public int doEndTag() throws JspException {
        final int result = super.doEndTag();

        nodeType = null;
        target = null;
        return result;
    }
}