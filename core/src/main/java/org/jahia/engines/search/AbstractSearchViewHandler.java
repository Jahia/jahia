/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
