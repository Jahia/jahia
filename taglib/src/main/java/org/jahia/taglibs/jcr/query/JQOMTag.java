/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.QueryObjectModel;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

import org.slf4j.Logger;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.taglibs.query.QueryDefinitionTag;

/**
 * Tag implementation for exposing a result of a QueryObjectModel query into the template
 * scope.
 */
@SuppressWarnings("serial")
public class JQOMTag extends QueryDefinitionTag {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JQOMTag.class);

    public int doEndTag() throws JspException {
        try {
            QueryObjectModel queryModel = getQueryObjectModel();
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
        // execute query
        long x = System.currentTimeMillis();
        queryResult = queryModel.execute();
        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Query {} --> found {} values in {} ms.",
                    new Object[] { queryModel.getStatement(),
                            JCRContentUtils.size(queryResult.getNodes()),
                            System.currentTimeMillis() - x });
        }

        return queryResult;
    }

}