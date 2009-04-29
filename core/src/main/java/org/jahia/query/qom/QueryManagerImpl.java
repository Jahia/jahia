/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.query.qom;

import org.jahia.params.ProcessingContext;
import org.jahia.query.QueryService;

import javax.jcr.query.Query;
import javax.jcr.query.InvalidQueryException;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;
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