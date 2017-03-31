/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.lang.ArrayUtils;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.MultipleIterator;
import org.jahia.services.content.MultipleNodeIterator;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This is an adapter to support JCR query result functionality for multiple providers.
 *
 * @author Thomas Draier
 */
public final class QueryResultWrapperImpl implements QueryResultWrapper {

    private final static QueryResultWrapperImpl EMPTY = new QueryResultWrapperImpl();
    
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

    public JCRNodeIteratorWrapper getNodes() throws RepositoryException {
        JCRNodeIteratorWrapper nodeIterator = JCRNodeIteratorWrapper.EMPTY;

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

    /**
     * Iterator aggregating multiple row iterators
     */
    public static class MultipleRowIterator extends MultipleIterator<RowIterator> implements RowIterator {

        public MultipleRowIterator(List<RowIterator> iterators, long limit) {
            super(iterators, limit);
        }

        @Override
        public Row nextRow() {
            return (Row) next();
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

    @Override
    public boolean isRangeFacetsEmpty(List<RangeFacet> rangeFacets) {
        if (rangeFacets.isEmpty()){
            return true;
        }else{
            for (RangeFacet facetField: rangeFacets) {
                List<RangeFacet.Count> counts = facetField.getCounts();
                for (RangeFacet.Count count : counts) {
                    if (count.getCount() != 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isFacetResultsEmpty(){
        return (this.getFacetFields() == null || isFacetFieldsEmpty(this.getFacetFields())) &&
                (this.getFacetDates() == null || isFacetFieldsEmpty(this.getFacetDates())) &&
                (this.getRangeFacets() == null || isRangeFacetsEmpty(this.getRangeFacets())) &&
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