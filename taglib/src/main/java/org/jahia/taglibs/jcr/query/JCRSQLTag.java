/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.jcr.query;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

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
                JCRSessionFactory.getInstance().setCurrentUser(JahiaUserManagerService.getInstance().lookupRootUser().getJahiaUser());
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
            logger.debug(getQueryLanguage() + " [" + statement + "] executed in " + (System.currentTimeMillis() - startTime) +" ms --> found [" + queryResult.getRows().getSize() + "] values.");
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
