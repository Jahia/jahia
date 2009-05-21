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
//
//
//

package org.jahia.services.search;

import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;


/**
 * Handle pages search.
 * The result for this searcher is an instance of JahiaSearchResult and is a List of matching pages.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class PageSearcher extends JahiaSearcher {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (PageSearcher.class);

    private String[] searchHandlers = new String[]{};
    private List<String> languageCodes = new ArrayList<String> ();
    private JahiaSearchResultBuilder searchResultBuilder = new PageSearchResultBuilderImpl();
    public JahiaSearchResultBuilder getSearchResultBuilder() {
        return searchResultBuilder;
    }

    public void setSearchResultBuilder(JahiaSearchResultBuilder searchResultBuilder) {
        this.searchResultBuilder = searchResultBuilder;
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     */
    public PageSearcher (String[] searchHandlers) {
        this(searchHandlers, new ArrayList<String>());
    }

    //--------------------------------------------------------------------------
    /**
     * Constructor
     */
    public PageSearcher (String[] searchHandlers, List<String> languageCodes) {
        if (languageCodes != null) {
            this.languageCodes = languageCodes;
        }
        if ( searchHandlers != null ){
            this.searchHandlers = searchHandlers;
        }
    }

    public String[] getSearchHandlers() {
        return searchHandlers;
    }

    public void setSearchHandlers(String[] searchHandlers) {
        this.searchHandlers = searchHandlers;
    }

    //--------------------------------------------------------------------------
    /**
     * Perform the search for a given query as String.
     * The expected result is a JahiaSearchResult object containing a List of matching pages.
     *
     * @param query
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public JahiaSearchResult search (String query,
                                     ProcessingContext jParams)
            throws JahiaException {
        return search(query, null, jParams);
    }

    //--------------------------------------------------------------------------
    /**
     * Perform the search for a given query as String.
     * The expected result is a JahiaSearchResult object containing a List of matching pages.
     *
     * @param query
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public JahiaSearchResult search (String query, String jcrQuery,
                                     ProcessingContext jParams)
            throws JahiaException {

        // Must set the query first.
        setQuery (query);

        // Perform the search.

        JahiaSearchResult result = new JahiaSearchResult (searchResultBuilder);

        if (getQuery () == null || "".equals(getQuery().trim()) || "()".equals(getQuery().trim()))
            return result;

        List<String> queries = new ArrayList<String>();
        queries.add(this.getQuery());
        
        String[] searchHandlers = this.getSearchHandlers();
        String searchHandlerName = null;
        JahiaSite site = null;
        String queryStr = null;
        for ( int i=0; i<searchHandlers.length; i++ ){
            searchHandlerName = searchHandlers[i];
            site = null;
            try {
                site = ServicesRegistry.getInstance().getJahiaSitesService()
                        .getSiteByKey(searchHandlerName);
            } catch ( Exception t ){
            }
            if (site != null){
                queryStr = SearchTools
                        .getWorkflowAndLanguageCodeSearchQuery(this.getLanguageCodes(),
                        jParams.getEntryLoadRequest());
                queries.add(queryStr);
            }
        }

        String[] queriesAr = (String[])queries.toArray(new String[queries.size()]);
        try {
            List<ParsedObject> allParsedObject = new LinkedList<ParsedObject>();

            SearchResult searchResult = ServicesRegistry.getInstance()
                    .getJahiaSearchService().search(queriesAr,
                            this.getSearchHandlers(), this.getLanguageCodes(), jParams,
                            this.searchResultBuilder.getHitCollector());

            List<ParsedObject> parsedObjects = SearchTools.getParsedObjects(searchResult);
            if ( parsedObjects == null ){
                parsedObjects = new LinkedList<ParsedObject>();
            }
            allParsedObject.addAll(parsedObjects);

            if (jcrQuery != null && !"".equals(jcrQuery.trim())) {
                SearchHandler searchHandler = ServicesRegistry.getInstance()
                        .getJahiaSearchService().getSearchManager().getSearchHandler(JahiaSearchBaseService.WEBDAV_SEARCH);
                if ( searchHandler != null ){
                    try {

                        SearchResult sr = searchHandler.search(jcrQuery);
                        allParsedObject.addAll(SearchTools.getParsedObjects(sr));
                    } catch (Exception t) {
                        logger.error (t);
                    }
                }

            }

            List<String> searchHandlersList = Arrays.asList(this.getSearchHandlers());
            if ( searchHandlersList.contains(JahiaSearchBaseService.WEBDAV_SEARCH) ){
                WebDavSearchResultBuilderImpl searchBuilder = new WebDavSearchResultBuilderImpl();
                result = searchBuilder.buildResult(allParsedObject, jParams, queriesAr);
            } else {
                result = searchResultBuilder.buildResult(allParsedObject, jParams, queriesAr);
            }
            // Store the result.
            setResult (result);

        } catch (Exception t) {
            logger.error ("Error",t);
        }
        return result;
    }

    /**
     * @return the list of languages code to search.
     */
    public List<String> getLanguageCodes () {
        return this.languageCodes;
    }
}
