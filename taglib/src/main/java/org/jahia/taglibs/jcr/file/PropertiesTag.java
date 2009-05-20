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
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 déc. 2007
 * Time: 18:19:44
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class PropertiesTag extends AbstractJahiaTag {
    private String name = "type";
    private String property;
    private String scope;

    private String id = "propertyDefinition";

    private Iterator<?> it;

    public int doStartTag() throws JspException {
        ExtendedNodeType nodetype = (ExtendedNodeType) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        List<ExtendedItemDefinition> l = nodetype.getDeclaredItems();

        it = l.iterator();

        if (!it.hasNext()) {
            return SKIP_BODY;
        }

        putNextItem();

        return EVAL_BODY_INCLUDE;
    }


    public int doAfterBody() throws JspException {
        if (!it.hasNext()) {
            return SKIP_BODY;
        }

        putNextItem();

        return EVAL_BODY_AGAIN;
    }

    private void putNextItem() {
        ExtendedItemDefinition item = (ExtendedItemDefinition) it.next();
        pageContext.setAttribute(id, item);
    }

    public int doEndTag() throws JspException {
        name = "type";
        property = null;
        scope = null;
        id = "propertyDefinition";

        return EVAL_PAGE;
    }
}
