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
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;

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
public class QueryResultWrapperImpl implements QueryResultWrapper {

    private static QueryResultWrapperImpl EMPTY = new QueryResultWrapperImpl();
    
    private List<QueryResultAdapter> queryResults;
    private long limit;

    /**
     * Decorates the provided list of query results, if needed.
     * @param queryResults the query results to be wrapped
     * @return decorated list of query results 
     */
    public static QueryResultWrapper wrap(List<QueryResultAdapter> queryResults, long limit) {
        if (queryResults == null || queryResults.isEmpty()) {
            return EMPTY;
        } else {
            return new QueryResultWrapperImpl(queryResults, limit);
        }
    }
    
    /**
     * Wrapped query results that comes from different store
     */
    private QueryResultWrapperImpl() {
        super();
        queryResults = Collections.emptyList();
    }

    /**
     * Wrapped query results that comes from different store
     *
     * @param queryResults
     */
    private QueryResultWrapperImpl(List<QueryResultAdapter> queryResults, long limit) {
        this.queryResults = queryResults;
        this.limit = limit;
    }

    public String[] getColumnNames() throws RepositoryException {
        return !queryResults.isEmpty() ? queryResults.get(0).getColumnNames() : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public RowIterator getRows() throws RepositoryException {
        RowIterator resultRowIterator = RowIteratorAdapter.EMPTY;
        if (!queryResults.isEmpty()) {
            List<RowIterator> rowIterators = new ArrayList<RowIterator>();
            for (final QueryResultAdapter queryResult : queryResults) {
                rowIterators.add(queryResult.getRows());
            }
            resultRowIterator = new MultipleRowIterator(rowIterators, limit);
        }
        return resultRowIterator;
    }

    public NodeIterator getNodes() throws RepositoryException {
        NodeIterator nodeIterator = NodeIteratorAdapter.EMPTY;

        if (!queryResults.isEmpty()) {
            List<NodeIterator> nodeIterators = new ArrayList<NodeIterator>();
            for (QueryResultAdapter queryResult : queryResults) {
                nodeIterators.add(queryResult.getNodes());
            }
            nodeIterator = new MultipleNodeIterator(nodeIterators, limit);
        }
        return nodeIterator;
    }

    public String[] getSelectorNames() throws RepositoryException {
        return !queryResults.isEmpty() ? queryResults.get(0).getSelectorNames() : ArrayUtils.EMPTY_STRING_ARRAY;
    }

    public class MultipleRowIterator extends MultipleIterator<RowIterator> implements RowIterator {

        public MultipleRowIterator(List<RowIterator> iterators, long limit) {
            super(iterators, limit);
        }

        @Override
        public Row nextRow() {
            return (Row) next();
        }
    }

    public class MultipleNodeIterator extends MultipleIterator<NodeIterator> implements NodeIterator {

        public MultipleNodeIterator(List<NodeIterator> iterators, long limit) {
            super(iterators, limit);
        }

        @Override
        public Node nextNode() {
            return (Node) next();
        }
    }

    @Override
    public List<FacetField> getFacetFields() {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getFacetFields() != null) {
                return queryResult.getFacetFields();
            }
        }
        return null;
    }

    @Override
    public List<FacetField> getFacetDates() {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getFacetDates() != null) {
                return queryResult.getFacetDates();
            }
        }
        return null;
    }

    @Override
    public List<RangeFacet> getRangeFacets() {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getRangeFacets() != null) {
                return queryResult.getRangeFacets();
            }
        }
        return null;
    }

    @Override
    public FacetField getFacetField(String name) {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getFacetField(name) != null) {
                return queryResult.getFacetField(name);
            }
        }
        return null;
    }

    @Override
    public FacetField getFacetDate(String name) {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getFacetDate(name) != null) {
                return queryResult.getFacetDate(name);
            }
        }
        return null;
    }

    @Override
    public RangeFacet getRangeFacet(String name) {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getRangeFacet(name) != null) {
                return queryResult.getRangeFacet(name);
            }
        }
        return null;
    }

    @Override
    public Map<String, Long> getFacetQuery() {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getFacetQuery() != null) {
                return queryResult.getFacetQuery();
            }
        }
        return null;
    }

    @Override
    public List<FacetField> getLimitingFacets() {
        for (QueryResultAdapter queryResult : queryResults) {
            if (queryResult.getLimitingFacets() != null) {
                return queryResult.getLimitingFacets();
            }
        }
        return null;
    }

    @Override
    public boolean isFacetFieldsEmpty(List<FacetField> facetFields) {
        if (facetFields.isEmpty()){
            return true;
        }else{
            for (FacetField facetField: facetFields) {
                if (facetField.getValueCount() != 0)
                    return false;
            }
        }
        return true;
    }


    public boolean isFacetResultsEmpty(){
        return (this.getFacetFields() == null || isFacetFieldsEmpty(this.getFacetFields())) &&
                (this.getFacetDates() == null || isFacetFieldsEmpty(this.getFacetFields())) &&
                (this.getFacetQuery() == null || this.getFacetQuery().isEmpty());
    }

    @Override
    public long getApproxCount() {
        int result = 0;
        for (QueryResultAdapter queryResult : queryResults) {
            result += queryResult.getApproxCount();
        }
        return result;
    }
}