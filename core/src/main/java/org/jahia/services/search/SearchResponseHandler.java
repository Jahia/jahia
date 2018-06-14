/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.services.search;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Response handler to store search requests.
 * This handler stores each search results depending of the criteria.
 * The search responses are stored as a Request Attribute.
 * 
 * @author Sergiy Shyrkov
 */
class SearchResponseHandler {

    private static final String ATTR_SEARCH_RESPONSE = SearchResponse.class.getName();

    private HttpServletRequest request;

    private Integer searchCriteriaHashCode;

    /**
     * Constructor for SearchResponseHandler
     * @param searchCriteriaHashCode search criteria hashcode
     * @param request request where to store the search results
     */
    SearchResponseHandler(Integer searchCriteriaHashCode, HttpServletRequest request) {
        this.searchCriteriaHashCode = searchCriteriaHashCode;
        this.request = request;
    }

    /**
     * @return the stored search response from the request according to the search criteria
     */
    SearchResponse getStoredSearchResponse() {
        Map<Integer, SearchResponse> searchResponses = getStoredSearchResponses();
        return searchResponses != null ? searchResponses.get(searchCriteriaHashCode) : null;
    }

    @SuppressWarnings("unchecked")
    private Map<Integer, SearchResponse> getStoredSearchResponses() {
        return (Map<Integer, SearchResponse>) request.getAttribute(ATTR_SEARCH_RESPONSE);
    }

    /**
     * Store the provided search response into the request
     * @param response to store
     */
    void store(SearchResponse response) {
        Map<Integer, SearchResponse> searchResponses = getStoredSearchResponses();
        if (searchResponses == null) {
            searchResponses = new HashMap<>();
            storeResponse(response, searchResponses);
            request.setAttribute(ATTR_SEARCH_RESPONSE, searchResponses);
        } else {
            storeResponse(response, searchResponses);
        }
    }

    private void storeResponse(SearchResponse response, Map<Integer, SearchResponse> searchResponses) {
        searchResponses.put(searchCriteriaHashCode, response);
    }

}
