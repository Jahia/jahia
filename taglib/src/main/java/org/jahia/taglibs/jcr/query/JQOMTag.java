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
package org.jahia.taglibs.jcr.query;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.jahia.taglibs.query.QueryDefinitionTag;

/**
 * Tag implementation for exposing a result of a QueryObjectModel query into the template
 * scope.
 */
@SuppressWarnings("serial")
public class JQOMTag extends QueryDefinitionTag {
    private static final Logger logger = Logger.getLogger(JQOMTag.class);

    private String qomBeanName;
    private long limit;
    private long offset;

    public int doEndTag() throws JspException {
        try {
            QueryObjectModel queryModel = qomBeanName == null ? getQueryObjectModel() : (QueryObjectModel) pageContext.getAttribute(qomBeanName, PageContext.REQUEST_SCOPE);
            pageContext.setAttribute(getVar(), findQueryResultByQOM(queryModel), getScope());
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        } finally {
            resetState();
        }
        
        return EVAL_PAGE;
    }

    /**
     * Executes the query of the provided QueryObjectModel.
     * 
     * @param queryModel
     *            a QueryObjectModel to perform the JCR query
     * @return the {@link QueryResult} instance with the results of
     *         the query
     * @throws RepositoryException 
     * @throws InvalidQueryException 
     */
    private QueryResult findQueryResultByQOM(QueryObjectModel queryModel) throws InvalidQueryException, RepositoryException {
        QueryResult queryResult = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Find node by qom [ " + queryModel.getStatement() + " ]");
        }
        if (limit > 0) {
            queryModel.setLimit(limit);
        }
        if (offset > 0) {
            queryModel.setOffset(offset);
        }
        // execute query
        queryResult = queryModel.execute();
        if (logger.isDebugEnabled()) {
            logger.debug("Query[" + queryModel.getStatement() + "] --> found [" + queryResult + "] values.");
        }

        return queryResult;
    }

    @Override
    protected void resetState() {
        qomBeanName = null;
        limit = 0;
        offset = 0;
        super.resetState();
    }

    public void setQomBeanName(String qomBeanName) {
        this.qomBeanName = qomBeanName;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
    
    public void setOffset(long offset) {
        this.offset = offset;
    }
}