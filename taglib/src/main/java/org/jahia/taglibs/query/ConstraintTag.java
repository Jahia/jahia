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

import javax.jcr.ValueFactory;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.Constraint;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
import javax.servlet.jsp.JspException;

import org.jahia.taglibs.AbstractJahiaTag;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 7 nov. 2007
 * Time: 15:36:14
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public abstract class ConstraintTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ConstraintTag.class);

    private QueryDefinitionTag queryModelDefTag = null;
    private ConstraintTag compoundConstraintTag = null;
    private QueryObjectModelFactory queryFactory = null;
    private ValueFactory valueFactory = null;

    public int doStartTag() {
        queryModelDefTag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
        if (queryModelDefTag == null) {
            return SKIP_BODY;
        }
        this.queryFactory = queryModelDefTag.getQueryFactory();
        this.valueFactory = queryModelDefTag.getValueFactory();
        if (this.queryFactory == null || this.valueFactory == null){
            logger.warn("QueryFactory or ValueFactory is null!");
            return SKIP_BODY;
        }
        compoundConstraintTag = (ConstraintTag) findAncestorWithClass(this,
                ConstraintTag.class);

        return EVAL_BODY_BUFFERED;
    }

    public int doEndTag() throws JspException {
        try {
            Constraint c = getConstraint();
            if ( c != null ){
                if ( compoundConstraintTag == null ){
                    queryModelDefTag.andConstraint(c);
                } else {
                    compoundConstraintTag.addChildConstraint(c);
                }
            }
        } catch ( Exception t ){
            logger.debug(t);
            throw new JspException("Error with constraint",t);
        }
        queryModelDefTag = null;
        compoundConstraintTag = null;
        queryFactory = null;
        valueFactory = null;
        return EVAL_PAGE;
    }

    public QueryDefinitionTag getQueryModelDefTag() {
        return queryModelDefTag;
    }

    public void setQueryModelDefTag(QueryDefinitionTag queryModelDefTag) {
        this.queryModelDefTag = queryModelDefTag;
    }

    public QueryObjectModelFactory getQueryFactory(){
        return this.queryFactory;
    }

    public ValueFactory getValueFactory() {
        return valueFactory;
    }

    public abstract Constraint getConstraint() throws Exception;

    public void addChildConstraint(Constraint constraint){
        // by default do nothing
    }

}
