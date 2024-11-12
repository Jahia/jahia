/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
