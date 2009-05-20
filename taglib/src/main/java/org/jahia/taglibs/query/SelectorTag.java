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
package org.jahia.taglibs.query;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Selector;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.jsp.JspException;

/**
 * Tag used to create a Query Object Model Selector.
 * The selector is assigned to the parent QueryObjectModel taken from the parent QueryDefinitionTag
 * <code>parentQueryDefinitionTag.setSource(selector)</code>
 *
 * User: hollis
 * Date: 8 nov. 2007
 * Time: 13:08:23
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class SelectorTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(SelectorTag.class);

    private QueryDefinitionTag queryDefinitionTag = null;

    private String nodeTypeName;
    private String selectorName;

    public int doStartTag() throws JspException {
        queryDefinitionTag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
        if (queryDefinitionTag == null) {
            return SKIP_BODY;
        }
        if ( nodeTypeName != null && !"".equals(nodeTypeName.trim())
                && selectorName != null && !"".equals(selectorName.trim()) ){
            try {
                Selector selector = queryDefinitionTag.getQueryFactory().selector(nodeTypeName, selectorName);
                if (selector != null){
                    queryDefinitionTag.setSource(selector);
                }
            } catch ( Exception t ){
                logger.debug("Exception occured creating Selector Node",t);
                throw new JspException("Exception occured creating Selector Node",t);
            }
        }
        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        queryDefinitionTag = null;
        nodeTypeName = null;
        selectorName = null;
        return EVAL_PAGE;
    }

    /**
     * The node Type name
     *
     * @return
     */
    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }

    /**
     * The selector name
     *
     * @return
     */
    public String getSelectorName() {
        return selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }

}