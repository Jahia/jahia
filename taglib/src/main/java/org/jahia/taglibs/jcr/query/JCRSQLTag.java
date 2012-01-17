/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.jcr.query;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.jcr.JCRUserManagerProvider;
import org.jahia.taglibs.jcr.AbstractJCRTag;

/**
 * Tag implementation for exposing a result of SQL-2 JCR query into the template scope.
 */
public class JCRSQLTag extends AbstractJCRTag {
    private static final long serialVersionUID = 4183406665401018247L;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JCRSQLTag.class);
    private boolean useRootUser = false;
    private int scope = PageContext.PAGE_SCOPE;
    private String var;
    private String statement;
    private long limit;
    private long offset;
    
    public int doEndTag() throws JspException {
        QueryResult result = null;
        JahiaUser userToReset = null;
        try {
            if (isUseRootUser()) {
                userToReset = JCRSessionFactory.getInstance().getCurrentUser();
                JCRSessionFactory.getInstance().setCurrentUser(JCRUserManagerProvider.getInstance().lookupRootUser());
            }    
            result = executeQuery(getJCRSession());
            
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        } finally {
            if (userToReset != null)  {
                JCRSessionFactory.getInstance().setCurrentUser(userToReset);
            }
        }
        pageContext.setAttribute(var, result, scope);
        resetState();
        return EVAL_PAGE;
    }

    /**
     * Executes the query.
     *
     * @return the QueryResult instance with the results of the query
     * @throws RepositoryException in case of JCR errors 
     * @throws InvalidQueryException in case of bad query statement
     */
    private QueryResult executeQuery(JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        long startTime = System.currentTimeMillis();
        QueryResult queryResult = null;
        if (logger.isDebugEnabled()) {
            logger.debug("Executing " + getQueryLanguage() + " query: " + statement);
        }

        Query q = session.getWorkspace().getQueryManager().createQuery(statement, getQueryLanguage());
        if (limit > 0) {
            q.setLimit(limit);
        }
        if (offset > 0) {
            q.setOffset(offset);
        }
        // execute query
        queryResult = q.execute();
        if (logger.isDebugEnabled()) {
            logger.debug(getQueryLanguage() + "[" + statement + "] executed in " + (System.currentTimeMillis() - startTime) +" ms --> found [" + queryResult + "] values.");
        }

        return queryResult;
    }
    
    @Override
    protected void resetState() {
        scope = PageContext.PAGE_SCOPE;
        statement = null;
        var = null;
        limit = 0;
        offset = 0;
        useRootUser = false; 
        super.resetState();
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

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public boolean isUseRootUser() {
        return useRootUser;
    }

    public void setUseRootUser(boolean useSystemUserSession) {
        this.useRootUser = useSystemUserSession;
    }
}