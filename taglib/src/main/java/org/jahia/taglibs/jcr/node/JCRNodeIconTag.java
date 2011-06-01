/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.jcr.node;

import org.apache.jackrabbit.core.nodetype.EffectiveNodeType;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/1/11
 * Time: 14:25
 * To change this template use File | Settings | File Templates.
 */
public class JCRNodeIconTag extends AbstractJCRTag {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeTypeTag.class);

    private JCRNodeWrapper node;
    private ExtendedNodeType type;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setType(ExtendedNodeType type) {
        this.type = type;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public int doStartTag() throws JspException {
        try {
            if (node != null) {
                pageContext.setAttribute(var, JCRContentUtils.getIcon(node), scope);
            } else if (type != null) {
                pageContext.setAttribute(var, JCRContentUtils.getIcon(type), scope);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        resetState();
        return super.doEndTag();
    }

    @Override
    protected void resetState() {
        node = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        super.resetState();
    }

}
