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
package org.apache.jackrabbit.core.query.lucene;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;

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
    private List<RangeFacet> rangeFacets = null;    

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
    
    public List<RangeFacet> getRangeFacets() {
        return rangeFacets;
    }    
    
    public void setRangeFacets(List<RangeFacet> rangeFacets) {
        this.rangeFacets = rangeFacets;
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
    
    public RangeFacet getRangeFacet(String name) {
        if (rangeFacets == null)
            return null;
        for (RangeFacet f : rangeFacets)
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
