/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.engines.search;

import java.util.List;
import java.util.Map;

import org.jahia.data.search.JahiaSearchResult;
import org.jahia.engines.calendar.CalendarHandler;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.expressions.ExpressionEvaluationUtils;
import org.jahia.services.expressions.SearchExpressionContext;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;


/**
 * Base class for different search view handler implementations.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class AbstractSearchViewHandler implements SearchViewHandler {

    protected String name = "";

    protected String query = "";

    protected String screen = "";
    
    protected int searchMode = SEARCH_MODE_WEBSITE;

    protected JahiaSearchResult searchResult;

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#getName()
     */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#getQuery()
     */
    public String getQuery() {
        return query;
    }

    public List<JahiaSavedSearch> getSavedSearches() throws JahiaException {
        return ServicesRegistry.getInstance().getJahiaSearchService()
                .getSavedSearches();
    }

    public String getSaveSearchDoc(ProcessingContext params)
            throws JahiaException {
        return null;
    }

    public String getScreen() {
        return screen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#getSearchMode()
     */
    public int getSearchMode() {
        return searchMode;
    }

    public JahiaSearchResult getSearchResult() {
        return searchResult;
    }

    public void init(ProcessingContext params, Map<String, Object> engineMap)
            throws JahiaException {
        // do nothing
    }

    public boolean isSearchModeChanged() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#isWebSiteSearch()
     */
    public boolean isWebSiteSearch() {
        return SEARCH_MODE_WEBDAV == searchMode;
    }

    public JahiaSearchResult search(ProcessingContext params)
            throws JahiaException {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#setQuery(java.lang.String)
     */
    public void setQuery(String query) {
        this.query = query;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jahia.engines.search.SearchViewHandler#setSearchMode(int)
     */
    public void setSearchMode(int searchMode) {
        this.searchMode = searchMode;
    }

    public void update(ProcessingContext params, Map<String, Object> engineMap)
            throws JahiaException {
        // do nothing
    }

    public void useSavedSearch(ProcessingContext params,
            JahiaSavedSearch savedSearch) throws JahiaException {
        // do nothing
    }
    
    /**
     * Evaluate search query and Expression
     * @param query
     * @return
     * @throws JahiaException
     */
    protected String evaluateQuery(String query, ProcessingContext context) throws JahiaException {
        SearchExpressionContext expContext = new SearchExpressionContext(CalendarHandler.DEFAULT_DATE_FORMAT,"UTC",context);
        return (String)ExpressionEvaluationUtils.doEvaluate(query,String.class,expContext);
    }    
}
