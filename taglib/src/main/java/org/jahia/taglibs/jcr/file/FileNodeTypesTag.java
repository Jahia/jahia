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
@SuppressWarnings("serial")
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
