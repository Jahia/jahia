/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.query;

import javax.jcr.query.qom.Selector;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * Tag used to create a Query Object Model Selector.
 * The selector is assigned to the parent QueryObjectModel taken from the parent QueryDefinitionTag
 * <code>parentQueryDefinitionTag.setSource(selector)</code>
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 */
@SuppressWarnings("serial")
public class SelectorTag extends QueryDefinitionDependentTag {

    private static Logger logger = Logger.getLogger(SelectorTag.class);

    private String nodeTypeName;
    public int doStartTag() throws JspException {
        QueryDefinitionTag queryDefinitionTag = getQueryDefinitionTag();
        
        if (StringUtils.isEmpty(nodeTypeName)) {
            throw new JspTagException("Attribute nodeTypeName is required for tag " + this.getClass().getName());
        }
        nodeTypeName = nodeTypeName.trim();
        if (getSelectorName() == null) {
            setSelectorName(nodeTypeName);
        }

        try {
            Selector selector = queryDefinitionTag.getQueryFactory().selector(nodeTypeName, getSelectorName());
            if (selector != null) {
                queryDefinitionTag.setSource(selector);
            }
        } catch (Exception t) {
            logger.debug("Exception occurred creating Selector Node", t);
            throw new JspException("Exception occurred creating Selector Node", t);
        }

        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        resetState();
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        nodeTypeName = null;
        super.resetState();
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }
}