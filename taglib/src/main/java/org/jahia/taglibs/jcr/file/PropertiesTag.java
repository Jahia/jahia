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

package org.jahia.taglibs.jcr.file;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.struts.taglib.TagUtils;
import org.jahia.services.content.nodetypes.ExtendedItemDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 18 déc. 2007
 * Time: 18:19:44
 * To change this template use File | Settings | File Templates.
 */
public class PropertiesTag extends BodyTagSupport {
    private String name = "type";
    private String property;
    private String scope;

    private String id = "propertyDefinition";

    private Iterator it;

    public int doStartTag() throws JspException {
        ExtendedNodeType nodetype = (ExtendedNodeType) TagUtils.getInstance().lookup(pageContext, name, property, scope);

        List l = nodetype.getDeclaredItems();

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
