package org.apache.jackrabbit.core.query.lucene;

import org.apache.solr.client.solrj.response.FacetField;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.query.Row;
import java.util.List;
import java.util.Map;

/**
 * Fake result row, holding facet information
 */
public class FacetRow implements Row {
    private Map<String, Long> facetQuery = null;
    private List<FacetField> facetFields = null;
    private List<FacetField> limitingFacets = null;
    private List<FacetField> facetDates = null;

    public FacetRow() {
    }

    public Map<String, Long> getFacetQuery() {
        return facetQuery;
    }

    public void setFacetQuery(Map<String, Long> facetQuery) {
        this.facetQuery = facetQuery;
    }

    public List<FacetField> getFacetFields() {
        return facetFields;
    }

    public void setFacetFields(List<FacetField> facetFields) {
        this.facetFields = facetFields;
    }

    public List<FacetField> getLimitingFacets() {
        return limitingFacets;
    }

    public void setLimitingFacets(List<FacetField> limitingFacets) {
        this.limitingFacets = limitingFacets;
    }

    public List<FacetField> getFacetDates() {
        return facetDates;
    }

    public void setFacetDates(List<FacetField> facetDates) {
        this.facetDates = facetDates;
    }

    /**
     * get
     *
     * @param name the name of the
     * @return the FacetField by name or null if it does not exist
     */
    public FacetField getFacetField(String name) {
        if (facetFields == null)
            return null;
        for (FacetField f : facetFields) {
            if (f.getName().equals(name))
                return f;
        }
        return null;
    }

    public FacetField getFacetDate(String name) {
        if (facetDates == null)
            return null;
        for (FacetField f : facetDates)
            if (f.getName().equals(name))
                return f;
        return null;
    }

    public Value[] getValues() throws RepositoryException {
        return new Value[0];
    }

    public Value getValue(String columnName) throws ItemNotFoundException, RepositoryException {
        return null;
    }

    public Node getNode() throws RepositoryException {
        return null;
    }

    public Node getNode(String selectorName) throws RepositoryException {
        return null;
    }

    public String getPath() throws RepositoryException {
        return null;
    }

    public String getPath(String selectorName) throws RepositoryException {
        return null;
    }

    public double getScore() throws RepositoryException {
        return 0;
    }

    public double getScore(String selectorName) throws RepositoryException {
        return 0;
    }
}
