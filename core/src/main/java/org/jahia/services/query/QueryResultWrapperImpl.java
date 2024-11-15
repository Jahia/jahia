/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.commons.iterator.RowIteratorAdapter;
import org.apache.jackrabbit.core.query.lucene.CountRow;
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
    private RowIterator aggregatedCountRow = null;

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
            if (QueryWrapper.isCount(getColumnNames())) {
                resultRowIterator = getAggregatedCount();
            } else {
                List<RowIterator> rowIterators = new ArrayList<RowIterator>();
                for (final QueryResultAdapter queryResult : queryResults) {
                    rowIterators.add(queryResult.getRows());
                }
                resultRowIterator = new MultipleRowIterator(rowIterators, limit);
            }
        }
        return resultRowIterator;
    }

    private RowIterator getAggregatedCount() throws RepositoryException {
        if (aggregatedCountRow == null) {
            long aggregatedCount = 0;
            boolean aggregatedApproxLimitReached = false;

            for (final QueryResultAdapter queryResult : queryResults) {
                RowIterator queryResultRowIterator = queryResult.getRows();

                if (queryResultRowIterator != null && queryResultRowIterator.hasNext()) {
                    QueryResultAdapter.RowDecorator row = (QueryResultAdapter.RowDecorator) queryResultRowIterator.nextRow();

                    if (row != null && row.getRawRow() instanceof CountRow) {
                        aggregatedCount += row.getValue(StringUtils.EMPTY).getLong();
                        aggregatedApproxLimitReached = aggregatedApproxLimitReached || row.getValue(CountRow.APPROX_LIMIT_REACHED).getBoolean();
                    }
                }
            }

            aggregatedCountRow = new RowIteratorAdapter(Collections.singletonList(new CountRow(aggregatedCount, aggregatedApproxLimitReached)));
        }

        return aggregatedCountRow;
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
