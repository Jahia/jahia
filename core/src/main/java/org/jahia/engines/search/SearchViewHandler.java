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
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 f�vr. 2005
 * Time: 15:02:10
 * To change this template use File | Settings | File Templates.
 */
public interface SearchViewHandler {

    public static final String SEARCH_MODE = "searchMode";
    public static final int SEARCH_MODE_WEBSITE = 1;
    public static final int SEARCH_MODE_WEBDAV = 2;
    public static final int SEARCH_MODE_JCR = 3;

    /**
     * The searchViewHandler name
     *
     * @param name
     */
    public abstract void setName(String name);

    public abstract String getName();

    /**
     * Retrieves params from request
     *
     * @param jParams
     * @param engineMap
     */
    public abstract void init(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException;

    /**
     * handles search operations like search option update
     *
     * @param jParams
     * @param engineMap
     */
    public abstract void update(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException;

    /**
     * Returns the search result of the last performed query
     *
     * @return
     */
    public abstract JahiaSearchResult getSearchResult();

    /**
     * Executes search
     *
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public abstract JahiaSearchResult search(ProcessingContext jParams)  throws JahiaException;

    /**
     * Return the full query
     * @return
     */
    public abstract String getQuery();

    public abstract void setQuery(String query);

    /**
     * Return the saved search as string
     *
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public abstract String getSaveSearchDoc(ProcessingContext jParams) throws JahiaException;

    /**
     * Use the given savedSearch as initial state
     *
     * @param jParams
     * @param savedSearch
     * @throws JahiaException
     */
    public abstract void useSavedSearch(ProcessingContext jParams, JahiaSavedSearch savedSearch) throws JahiaException;

    public abstract int getSearchMode();

    public abstract void setSearchMode(int searchMode);

    public abstract boolean isSearchModeChanged();

    public abstract boolean isWebSiteSearch();

    public abstract List<JahiaSavedSearch> getSavedSearches() throws JahiaException;

}
