/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content;

import org.jahia.services.query.QueryWrapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.QueryManager;
import javax.jcr.query.qom.QueryObjectModelFactory;

/**
 * Extension of the QueryManager interface, with wrapped return types
 */
public interface QueryManagerWrapper extends QueryManager {

    /**
     * Creates a new query by specifying the query <code>statement</code> itself
     * and the <code>language</code> in which the query is stated. The
     * <code>language</code> must be a string from among those returned by
     * QueryManager.getSupportedQueryLanguages().
     *
     * @param statement a <code>String</code>
     * @param language  a <code>String</code>
     * @return a <code>Query</code> object
     * @throws javax.jcr.query.InvalidQueryException if the query statement is syntactically
     *                               invalid or the specified language is not supported.
     * @throws javax.jcr.RepositoryException   if another error occurs.
     */
    public QueryWrapper createQuery(String statement, String language) throws InvalidQueryException, RepositoryException;

    /**
     * Creates a new query by specifying the query <code>statement</code> in xpath and
     * in SQL2. XPath will be used against jackrabbit where SQL2 can be used as a fallback statement
     * in other providers, which may not support xpath.
     *
     * QueryManager.getSupportedQueryLanguages().
     *
     * @param statement a <code>String</code>
     * @param language  a <code>String</code>
     * @param sqlFallbackStatement  a <code>String</code>
     * @return a <code>Query</code> object
     * @throws javax.jcr.query.InvalidQueryException if the query statement is syntactically
     *                               invalid or the specified language is not supported.
     * @throws javax.jcr.RepositoryException   if another error occurs.
     */
    public QueryWrapper createDualQuery(String statement, String language, String sqlFallbackStatement) throws InvalidQueryException, RepositoryException;

    /**
     * Returns a <code>QueryObjectModelFactory</code> with which a JCR-JQOM
     * query can be built programmatically.
     *
     * @return a <code>QueryObjectModelFactory</code> object
     * @since JCR 2.0
     */
    public QueryObjectModelFactory getQOMFactory();

    /**
     * Retrieves an existing persistent query.
     * <p>
     * Persistent queries are created by first using {@link
     * QueryManager#createQuery} to create a <code>Query</code> object and then
     * calling <code>Query.save</code> to persist the query to a location in the
     * workspace.
     *
     * @param node a persisted query (that is, a node of type
     *             <code>nt:query</code>).
     * @return a <code>Query</code> object.
     * @throws InvalidQueryException If <code>node</code> is not a valid
     *                               persisted query (that is, a node of type <code>nt:query</code>).
     * @throws RepositoryException   if another error occurs
     */
    public QueryWrapper getQuery(Node node) throws InvalidQueryException, RepositoryException;



}
