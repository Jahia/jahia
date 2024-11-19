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
package org.jahia.services.query;

import javax.jcr.RepositoryException;
import javax.jcr.ValueFactory;
import javax.jcr.query.qom.Column;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.Ordering;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Source;

import org.jahia.services.JahiaService;
import org.jahia.services.content.JCRSessionWrapper;

/**
 * Jahia's query service is based on the JCR QueryObjectModelFactory and thus supports all kinds of
 * complex queries specified in JSR-283 (Content Repository for Java� Technology API 2.0)
 *
 * Queries can be created with the API by using the QueryObjectModel.
 * Jahia will also provide a query builder user interface.
 * It is also possible to use SQL-2 and the deprecated XPATH language.
 *
 * As Jahia can plug-in multiple repositories via the universal content hub (UCH), the queries can be
 * converted to other languages, like the EntropySoft connector query language.
 *
 * The query service provides methods to modify and optimize the queries to support and make use of Jahia's
 * internal data model implementation.
 *
 * @author Benjamin Papez
 */
public abstract class QueryService extends JahiaService {
    /**
     * Entry point to request a ValueFactory instance
     *
     * @return ValueFactory
     */
    public abstract ValueFactory getValueFactory();

    /**
     * Modifies the query to adapt to Jahia's internal datamodel implementation, which creates jnt:translation subnodes per locale (language).
     * Furthermore Jahia may make modifications because of performance optimization reasons.
     *
     * @param qom the source query object model
     * @param qomFactory query object model factory to use
     * @param session the current JCR session used for the query
     * @return the modified and optimized query object model
     * @throws RepositoryException if the operation fails
     */
    public abstract QueryObjectModel modifyAndOptimizeQuery(QueryObjectModel qom, QueryObjectModelFactory qomFactory, JCRSessionWrapper session) throws RepositoryException;

    /**
     * Modifies the query to adapt to Jahia's internal datamodel implementation, which creates jnt:translation subnodes per locale (language).
     * Furthermore Jahia may make modifications because of performance optimization reasons.
     *
     * @param source the QOM Source object to use
     * @param constraint the QOM root constraint object to use
     * @param orderings the QOM ordering objects to use
     * @param columns the QOM column objects to use
     * @param qomFactory query object model factory to use
     * @param session the current JCR session used for the query
     * @return the created modified and optimized query object model
     * @throws RepositoryException if the operation fails
     */
    public abstract QueryObjectModel modifyAndOptimizeQuery(Source source, Constraint constraint, Ordering[] orderings,
            Column[] columns, QueryObjectModelFactory qomFactory, JCRSessionWrapper session) throws RepositoryException;

}
