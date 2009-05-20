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

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.jsp.JspException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Tag iterates over all available node types.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class NodeTypesTag extends AbstractJahiaTag {

    private String id = "type";

    private Iterator<?> typeIterator;

    private String baseType = null;

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
        if (baseType == null) {
            typeIterator = NodeTypeRegistry.getInstance().getAllNodeTypes();
        } else {
            ExtendedNodeType base = null;
            try {
                base = NodeTypeRegistry.getInstance().getNodeType(baseType);
            } catch (NoSuchNodeTypeException e) {
                return SKIP_BODY;
            }
            typeIterator = Arrays.asList(base.getMixinSubtypes()).iterator();
        }

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

    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }
}
