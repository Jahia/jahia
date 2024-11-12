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
package org.apache.jackrabbit.core.query;

import org.apache.jackrabbit.core.query.lucene.FacetRow;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;

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

    public FacetedQueryResult(String[] columnNames, String[] selectorNames,
            RowIterator rowIterator, FacetRow facetRow, long approxCount) {
        super(columnNames, selectorNames, rowIterator, approxCount);
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

    public List<RangeFacet> getRangeFacets() {
        return facetRow.getRangeFacets();
    }

    public FacetField getFacetField(String name) {
        return facetRow.getFacetField(name);
    }

    public FacetField getFacetDate(String name) {
        return facetRow.getFacetDate(name);
    }

    public RangeFacet getRangeFacet(String name) {
        return facetRow.getRangeFacet(name);
    }
}
