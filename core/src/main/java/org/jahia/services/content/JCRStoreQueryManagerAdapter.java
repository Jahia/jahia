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
package org.jahia.services.content;

import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModelFactory;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 13 nov. 2008
 * Time: 15:16:18
 * To change this template use File | Settings | File Templates.
 */
public class JCRStoreQueryManagerAdapter implements QueryManager {

    private org.apache.jackrabbit.core.query.QueryManagerImpl queryManager;
    private QueryObjectModelFactoryAdapter qomFactory;

    /**
     *
     * @param queryManager
     */
    public JCRStoreQueryManagerAdapter(org.apache.jackrabbit.core.query.QueryManagerImpl queryManager) {
        this.queryManager = queryManager;
        qomFactory = new QueryObjectModelFactoryAdapter(this.queryManager.getQOMFactory());
    }

    public QueryObjectModelFactory getQOMFactory() {
        return qomFactory;
    }

    public Query createQuery(java.lang.String s, java.lang.String s1)
    throws javax.jcr.query.InvalidQueryException, javax.jcr.RepositoryException {
        return this.queryManager.createQuery(s,s1);
    }

    public Query getQuery(javax.jcr.Node node)
    throws javax.jcr.query.InvalidQueryException, javax.jcr.RepositoryException {
        return this.queryManager.getQuery(node);
    }

    public String[] getSupportedQueryLanguages() throws javax.jcr.RepositoryException {
        return this.queryManager.getSupportedQueryLanguages();
    }

}
