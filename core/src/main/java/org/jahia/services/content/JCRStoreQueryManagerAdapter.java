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

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;

import org.apache.jackrabbit.core.query.QueryManagerImpl;
import org.jahia.query.QueryService;


/**
 * This is an adapter to support Jackrabbit's query manager functionality.
 *
 * @author toto
 */
public class JCRStoreQueryManagerAdapter implements QueryManager {

    private QueryManagerImpl queryManager;
    private QueryObjectModelFactoryAdapter qomFactory;
    private Locale currentLocale;

    /**
     *
     * @param queryManager
     */
    public JCRStoreQueryManagerAdapter(QueryManagerImpl queryManager) {
        this.queryManager = queryManager;
        qomFactory = new QueryObjectModelFactoryAdapter(this.queryManager.getQOMFactory());
    }

    public QueryObjectModelFactory getQOMFactory() {
        return qomFactory;
    }

    public Query createQuery(String statement, String language)
    throws InvalidQueryException, RepositoryException {
        Query query = this.queryManager.createQuery(statement,language);
        if (Query.JCR_SQL2.equals(statement)
                && query instanceof QueryObjectModel) {
            query = QueryService.getInstance().modifyAndOptimizeQuery(
                    (QueryObjectModel) query, getCurrentLocale());
        }
        return query;
    }

    public Query getQuery(Node node)
    throws InvalidQueryException, RepositoryException {
        return this.queryManager.getQuery(node);
    }

    public String[] getSupportedQueryLanguages() throws RepositoryException {
        return this.queryManager.getSupportedQueryLanguages();
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(Locale currentLocale) {
        this.currentLocale = currentLocale;
    }

}
