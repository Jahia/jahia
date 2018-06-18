/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.search;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.search.SearchResponse.FacetedResult;

/**
 * Search related utility methods.
 *
 * @author Sergiy Shyrkov
 */
public final class SearchUtils {

    private static final String ATTR_SEARCH_RESPONSE = SearchResponse.class.getName();

    /**
     * Retrieves the faceted result, which corresponds to the supplied facet definition ID. This call also executes the search, if it was
     * not executed in this request yet and the request contains the search criteria.
     * 
     * @param facetDefinitionId the facet definition ID to retrieve results for
     * @param ctx current rendering context
     * @return faceted result, which corresponds to the supplied facet definition ID, or <code>null</code> if there is no information about
     *         that facet (or no result for that facet is present)
     */
    public static FacetedResult getFacetedResult(String facetDefinitionId, RenderContext ctx) {
        SearchResponse searchResponse = getSearchResponse(ctx);
        return searchResponse != null ? searchResponse.getFacetedResult(facetDefinitionId) : null;
    }

    /**
     * Obtains the search response object for the current search, performing a search if not yet done for current request. If no search
     * criteria is found in current request parameters, null is returned.
     * 
     * @param ctx current rendering context
     * @return the search response object or <code>null</code> if there are no search criteria in the current request
     */
    public static SearchResponse getSearchResponse(RenderContext ctx) {
        SearchCriteria criteria = SearchCriteriaFactory.getInstance(ctx);
        return criteria != null ? ServicesRegistry.getInstance().getSearchService().search(criteria, ctx) : null;
    }

    /**
     * Retrieves the stored search response object for the specified criteria.
     * 
     * @param searchCriteria the search criteria object
     * @param request current HTTP request
     * @return the stored search response from the request according to the search criteria or <code>null</code> if the search response
     *         object is not stored yet for that search criteria
     */
    static SearchResponse getStoredSearchResponse(SearchCriteria searchCriteria, HttpServletRequest request) {
        Map<Integer, SearchResponse> searchResponses = getStoredSearchResponses(request, false);
        return searchResponses != null ? searchResponses.get(searchCriteria.hashCode()) : null;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, SearchResponse> getStoredSearchResponses(HttpServletRequest request,
            boolean createIfMissing) {
        Map<Integer, SearchResponse> responses = (Map<Integer, SearchResponse>) request
                .getAttribute(ATTR_SEARCH_RESPONSE);
        if (responses == null && createIfMissing) {
            responses = new HashMap<>();
            request.setAttribute(ATTR_SEARCH_RESPONSE, responses);
        }
        return responses;
    }

    /**
     * Store the provided search response into the request.
     * 
     * @param searchCriteria the search criteria object
     * @param response to store
     * @param request current HTTP request
     */
    static void storeSearchResponse(SearchCriteria searchCriteria, SearchResponse response,
            HttpServletRequest request) {
        getStoredSearchResponses(request, true).put(searchCriteria.hashCode(), response);
    }
}
