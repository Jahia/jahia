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

import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 19 déc. 2007
 * Time: 10:58:43
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class NodeTypeTag extends AbstractJahiaTag {
    private String ntname = "file";

    private String id = "type";

    public String getNtname() {
        return ntname;
    }

    public void setNtname(String ntname) {
        this.ntname = ntname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int doStartTag() throws JspException {
        try {
            NodeType type = NodeTypeRegistry.getInstance().getNodeType(ntname);
            pageContext.setAttribute(id, type);
        } catch (NoSuchNodeTypeException e) {
            return SKIP_BODY;
        }

        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        ntname = null;
        id = "type";

        return EVAL_PAGE;
    }


}
