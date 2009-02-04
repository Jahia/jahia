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

import org.apache.struts.taglib.TagUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.jsp.JspException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Apr 8, 2008
 * Time: 5:52:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileNodeTypesTag extends AbstractJahiaTag {

    private String id = "type";

    private String baseType = null;

    private String name = "file";
    private String property;
    private String scope;

    private Iterator<ExtendedNodeType> typeIterator;

    @Override
    public int doAfterBody() throws JspException {
        if (!typeIterator.hasNext()) {
            return SKIP_BODY;
        }

        putNextItem();

        return EVAL_BODY_AGAIN;
    }

    @Override
    public int doEndTag() throws JspException {
        id = "type";
        baseType = null;

        typeIterator = null;

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        JCRNodeWrapper file = null;

        FileTag ft = (FileTag) findAncestorWithClass(this, FileTag.class);
        if (ft != null) {
            file = ft.getFile();
        } else {
            file = (JCRNodeWrapper) TagUtils.getInstance().lookup(pageContext, name, property, scope);
        }

        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();

        if (file != null) {
            for (String s : file.getNodeTypes()) {
                try {
                    ExtendedNodeType o = NodeTypeRegistry.getInstance().getNodeType(s);
                    if (baseType == null || o.isNodeType(baseType)) {
                        types.add(o);
                    }
                } catch (NoSuchNodeTypeException e) {
                    e.printStackTrace();
                }
            }
        }

        typeIterator = types.iterator();

        if (!typeIterator.hasNext()) {
            return SKIP_BODY;
        }

        putNextItem();

        return EVAL_BODY_INCLUDE;
    }

    private void putNextItem() throws JspException {
        pageContext.setAttribute(id, typeIterator.next());
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }
}
