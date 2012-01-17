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
