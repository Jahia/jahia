package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.core.query.lucene.FacetHandler;
import org.apache.jackrabbit.core.query.lucene.FacetRow;
import org.apache.jackrabbit.core.query.lucene.join.SimpleQueryResult;
import org.apache.solr.client.solrj.response.FacetField;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import java.util.List;
import java.util.Map;

/**
 * Simple query result implementation with facets.
 */
public class FacetedQueryResult extends JahiaSimpleQueryResult implements QueryResult {
    private FacetRow facetRow;


    public FacetedQueryResult(String[] columnNames, String[] selectorNames, RowIterator rowIterator,
                              FacetRow facetRow) {
        super(columnNames, selectorNames, rowIterator);
        this.facetRow = facetRow;
    }


    public Map<String, Long> getFacetQuery() {
        return facetRow.getFacetQuery();
    }

    public List<FacetField> getFacetFields() {
        return facetRow.getFacetFields();
    }

    public List<FacetField> getLimitingFacets() {
        return facetRow.getLimitingFacets();
    }

    public List<FacetField> getFacetDates() {
        return facetRow.getFacetDates();
    }

    public FacetField getFacetField(String name) {
        return facetRow.getFacetField(name);
    }

    public FacetField getFacetDate(String name) {
        return facetRow.getFacetDate(name);
    }
}
