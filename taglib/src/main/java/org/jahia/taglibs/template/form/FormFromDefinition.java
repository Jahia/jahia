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

package org.jahia.taglibs.template.form;

import org.jahia.taglibs.template.gwt.GWTJahiaModuleTag;
import org.jahia.ajax.gwt.templates.commons.client.module.JahiaType;

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