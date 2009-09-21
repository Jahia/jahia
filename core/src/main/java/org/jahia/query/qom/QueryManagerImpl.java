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
package org.jahia.query.qom;

import org.jahia.params.ProcessingContext;
import org.jahia.query.QueryService;

import javax.jcr.query.Query;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.RepositoryException;
import javax.jcr.Node;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 14 nov. 2008
 * Time: 16:18:02
 * To change this template use File | Settings | File Templates.
 */
public abstract class QueryManagerImpl implements javax.jcr.query.QueryManager {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(QueryManagerImpl.class);

    private ProcessingContext context;
    private Properties properties;

    public QueryManagerImpl(ProcessingContext context) {
        this.context = context;
    }

    public QueryManagerImpl(ProcessingContext context, Properties properties) {
        this.context = context;
        this.properties = properties;
    }

    public QueryObjectModelFactory getQOMFactory() {
        try {
            return QueryService.getInstance().getQueryObjectModelFactory(getQueryExecute(), this.context, this.properties);
        } catch (Throwable t){
            logger.debug("Cannot instanciate the QueryObjectModelFactory",t);
        }
        return null;
    }

    /**
     *
     * @return
     */
    public abstract QueryExecute getQueryExecute();

    /**
     *
     * @param statement
     * @param language
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public abstract Query createQuery(String statement, String language) throws InvalidQueryException,
            RepositoryException;

    /**
     *
     * @param node
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException
     */
    public abstract Query getQuery(Node node) throws InvalidQueryException, RepositoryException;

    /**
     *
     * @return
     * @throws RepositoryException
     */
    public abstract String[] getSupportedQueryLanguages() throws RepositoryException;

}