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

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.bin.Jahia;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.QueryResultAdapter;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import java.security.Principal;
import java.util.Locale;

/**
 * Tag implementation for exposing a result of SQL-2 JCR query into the template scope.
 */
public class JCRSQLTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 4183406665401018247L;
    private static final Logger logger = Logger.getLogger(JCRSQLTag.class);
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private String statement;
    private long limit;

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }

    public int doStartTag() throws JspException {
        try {
            pageContext.setAttribute(var, findQueryResult(getUser(), statement), scope);
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Find Node iterator by principal and query expression.
     *
     * @param p
     *            the principal
     * @param query
     *            a query expression to perform the query
     * @return the {@link javax.jcr.NodeIterator} instance with the results of the query;
     *         returns empty iterator if nothing is found
     * @throws RepositoryException 
     * @throws InvalidQueryException 
     */
    private QueryResult findQueryResult(Principal p, String query) throws InvalidQueryException, RepositoryException {
        QueryResult queryResult = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Find node by " + getQueryLanguage() + "[ " + query + " ]");
        }
        if (p instanceof JahiaGroup) {
            throw new UnsupportedOperationException("method not implemented for JahiaGroup");
            
        }

        String workspace = null;
        Locale locale = Jahia.getThreadParamBean().getCurrentLocale();
        Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
        if (currentResource != null) {
            workspace = currentResource.getWorkspace();
            locale = currentResource.getLocale();
        }
        Query q = JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getWorkspace()
                .getQueryManager().createQuery(query, getQueryLanguage());
        if (limit > 0) {
            q.setLimit(limit);
        }
        // execute query
        queryResult = q.execute();
        if (logger.isDebugEnabled()) {
            logger.debug(getQueryLanguage() + "[" + query + "] --> found [" + queryResult + "] values.");
        }

        return queryResult != null ? queryResult : new QueryResultAdapter();
    }

    @Override
    protected void resetState() {
        super.resetState();
        scope = PageContext.PAGE_SCOPE;
        statement = null;
        var = null;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setVar(String var) {
        this.var = var;
    }

    protected void setStatement(String statement) {
        this.statement = statement;
    }

    public void setSql(String sql) {
        setStatement(sql);
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }
    
    /**
     * Returns the type of the query language.
     * 
     * @return the type of the query language
     */
    protected String getQueryLanguage() {
        return Query.JCR_SQL2;
    }
}