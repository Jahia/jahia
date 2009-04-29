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

import javax.jcr.query.QueryResult;
import org.apache.jackrabbit.spi.commons.query.jsr283.qom.QueryObjectModel;

/**
 * This interface is used by the QueryObjectModelImpl class to delegate the execution of its method <code>execute()</code>
 *
 * User: hollis
 * Date: 14 nov. 2008
 * Time: 16:06:39
 * To change this template use File | Settings | File Templates.
 */
public interface QueryExecute {

    /**
     *
     * @return
     * @throws javax.jcr.RepositoryException
     */
    public QueryResult execute(QueryObjectModel qom) throws javax.jcr.RepositoryException;

}
