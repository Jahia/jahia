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

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.jahia.services.content.JCRNodeIteratorWrapper;

import javax.jcr.RepositoryException;
import javax.jcr.query.QueryResult;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for the JCR {@link QueryResult} adding facets and other methods.
 *
 * @author Thomas Draier
 */
public interface QueryResultWrapper extends QueryResult {

    List<FacetField> getFacetFields();

    List<FacetField> getFacetDates();

    List<RangeFacet> getRangeFacets();

    FacetField getFacetField(String name);

    FacetField getFacetDate(String name);

    RangeFacet getRangeFacet(String name);

    boolean isFacetFieldsEmpty(List<FacetField> facetFields);

    boolean isRangeFacetsEmpty(List<RangeFacet> rangeFacets);

    Map<String, Long> getFacetQuery();

    List<FacetField> getLimitingFacets();

    /**
     * Check if the queryResultWrapper contains any facet results
     *
     * @return true is queryResultWrapper doesn't contains any facet results
     */
    boolean isFacetResultsEmpty();

    long getApproxCount();

    @Override
    JCRNodeIteratorWrapper getNodes() throws RepositoryException;
}