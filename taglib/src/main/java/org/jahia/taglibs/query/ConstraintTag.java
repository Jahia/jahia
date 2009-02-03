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
