/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.jackrabbit.commons.iterator.NodeIteratorAdapter;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;

import javax.jcr.*;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import java.util.*;

/**
 * This is an adapter to support JCR query result functionality for multiple providers.
 *
 * @author Thomas Draier
 */
public class MultipleQueryResultAdapter implements QueryResult {

    private static MultipleQueryResultAdapter EMPTY = new MultipleQueryResultAdapter();
    
    private List<QueryResultWrapper> queryResults;
    
    /**
     * Decorates the provided list of query results, if needed.
     * @param queryResults the query results to be wrapped
     * @return decorated list of query results 
     */
    public static QueryResult decorate(List<QueryResultWrapper> queryResults) {
        if (queryResults == null || queryResults.isEmpty()) {
            return EMPTY;
        } else if (queryResults.size() == 1) { 
            return queryResults.get(0); 
        } else {
            return new MultipleQueryResultAdapter(queryResults);
        }
    }
    
    /**
     * Wrapped query results that comes from different store
     */
    private MultipleQueryResultAdapter() {
        super();
        queryResults = Collections.emptyList();
    }

    /**
     * Wrapped query results that comes from different store
     *
     * @param queryResults
     */
    private MultipleQueryResultAdapter(List<QueryResultWrapper> queryResults) {
        this.queryResults = queryResults;
    }

    public String[] getColumnNames() throws RepositoryException {
        return !queryResults.isEmpty() ? queryResults.get(0).getColumnNames() : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public RowIterator getRows() throws RepositoryException {
        RowIterator resultRowIterator = RowIteratorAdapter.EMPTY;
        if (!queryResults.isEmpty()) {
            List<RowIterator> rowIterators = new ArrayList<RowIterator>();
            for (final QueryResultWrapper queryResult : queryResults) {
                rowIterators.add(queryResult.getRows());
            }
            resultRowIterator = new MultipleRowIterator(rowIterators);
        }
        return resultRowIterator;
    }

    public NodeIterator getNodes() throws RepositoryException {
        NodeIterator nodeIterator = NodeIteratorAdapter.EMPTY;

        if (!queryResults.isEmpty()) {
            List<NodeIterator> nodeIterators = new ArrayList<NodeIterator>();
            for (QueryResult queryResult : queryResults) {
                nodeIterators.add(queryResult.getNodes());
            }
            nodeIterator = new MultipleNodeIterator(nodeIterators);
        }
        return nodeIterator;
    }

    public String[] getSelectorNames() throws RepositoryException {
        return !queryResults.isEmpty() ? queryResults.get(0).getSelectorNames() : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public class MultipleRowIterator extends MultipleIterator<RowIterator> implements RowIterator {

        public MultipleRowIterator(List<RowIterator> iterators) {
            super(iterators);
        }

        @Override
        public Row nextRow() {
            return (Row) next();
        }
    }

    public class MultipleNodeIterator extends MultipleIterator<NodeIterator> implements NodeIterator {

        public MultipleNodeIterator(List<NodeIterator> iterators) {
            super(iterators);
        }

        @Override
        public Node nextNode() {
            return (Node) next();
        }
    }
}