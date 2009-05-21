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
