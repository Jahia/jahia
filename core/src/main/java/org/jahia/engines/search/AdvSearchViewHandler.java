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

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.services.search.*;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.exceptions.JahiaException;

import java.util.*;

/**
 * Advanced search view handler implementation.
 * 
 * @author hollis
 */
public class AdvSearchViewHandler extends AbstractSearchViewHandler {

    private static Logger logger =
            Logger.getLogger (AdvSearchViewHandler.class);

    public static final String VALUE_ELEMENT = "value";

    public static final String OPERATION = "operation";
    public static final String SAVESEARCH_OPERATION = "saveSearch";

    public static final String TEMPLATE = "advsearchresult.jsp";

    protected boolean searchModeChanged = false;

    protected WebSiteSearchDelegate webSiteSearchDelegate;
    protected WebdavSearchDelegate webdavSearchDelegate;
    protected AdvSearchDelegate advSearchDelegate;

    public AdvSearchViewHandler(){
        this.webSiteSearchDelegate = new WebSiteSearchDelegate(this);
        this.webdavSearchDelegate = new WebdavSearchDelegate(this);
    }

    public WebSiteSearchDelegate getWebSiteSearchDelegate() {
        return webSiteSearchDelegate;
    }

    public WebdavSearchDelegate getWebdavSearchDelegate() {
        return webdavSearchDelegate;
    }

    public boolean isSearchModeChanged() {
        return searchModeChanged;
    }

    public void setSearchModeChanged(boolean searchModeChanged) {
        this.searchModeChanged = searchModeChanged;
    }

    public void init(ProcessingContext jParams, Map<String, Object> engineMap) throws JahiaException {

        screen = (String)engineMap.get("screen");
        if ( screen == null ){
            screen = Search_Engine.EXECUTE_SCREEN;
        }

        this.setSearchModeChanged(false);

        if ( Search_Engine.EXECUTE_SCREEN.equals(screen) ){

            String value = jParams.getParameter(SEARCH_MODE);
            if ( value != null ){
                try {
                    int oldSearchMode = this.searchMode;
                    this.searchMode = Integer.parseInt(value);
                    this.setSearchModeChanged( oldSearchMode != this.searchMode );
                } catch ( Exception t){
                }
            }
        }

        String fileName = null;
        String theTemplate = (String)engineMap.get(Search_Engine.ENGINE_OUTPUT_FILE_PARAM);
        if ((jParams != null) && (jParams.getPage () != null) &&
                (jParams.getPage ().getPageTemplate () != null)) {
            fileName = jParams.getPage ().getPageTemplate ().getSourcePath ();
            if (fileName.lastIndexOf ("/") != -1) {
                fileName = fileName.substring (0, fileName.lastIndexOf ("/") + 1) +
                        TEMPLATE;
                logger.debug ("Trying to redirect search result to : " + fileName);
                theTemplate = fileName;
            }
        }
        if ( theTemplate != null ){
            engineMap.put (Search_Engine.ENGINE_OUTPUT_FILE_PARAM, theTemplate);
        }            

        if ( this.isWebSiteSearch() ){
            this.advSearchDelegate = this.webSiteSearchDelegate;
        } else {
            this.advSearchDelegate = this.webdavSearchDelegate;
        }
        this.advSearchDelegate.init(jParams,engineMap);
    }

    /**
     * handles search operations like search option update
     *
     * @param jParams
     * @param engineMap
     */
    public void update(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException{
        this.advSearchDelegate.update(jParams,engineMap);
        List<String> languageCodes = (List<String>)engineMap.get("searchLanguageCodes");
        if ( languageCodes != null ){
            this.advSearchDelegate.setLanguageCodes(languageCodes);
        }
    }


    public JahiaSearchResult search(ProcessingContext jParams)
    throws JahiaException
    {
        return this.advSearchDelegate.search(jParams);
    }

    public JahiaSearchResult getSearchResult(){
        return this.advSearchDelegate.getSearchResult();
    }

    public String getQuery() {
        return this.advSearchDelegate.getQuery();
    }

    public void setQuery(String query) {
        this.advSearchDelegate.setQuery(query);
    }

    public void changeMode(int searchMode){
        this.searchMode = searchMode;
        this.searchModeChanged = true;
        if ( this.isWebSiteSearch() ){
            this.advSearchDelegate = this.webSiteSearchDelegate;
        } else {
            this.advSearchDelegate = this.webdavSearchDelegate;
        }
    }

    public JahiaSearchResultBuilder getResultBuilder() {
        return this.advSearchDelegate.getResultBuilder();
    }

    public void setResultBuilder(JahiaSearchResultBuilder resultBuilder) {
        this.advSearchDelegate.setResultBuilder(resultBuilder);
    }

    public String getFreeSearch() {
        return this.advSearchDelegate.getFreeSearch();
    }

    public void setFreeSearch(String freeSearch) {
        this.advSearchDelegate.setFreeSearch(freeSearch);
    }

    public String getAllWord() {
        return this.advSearchDelegate.getAllWord();
    }

    public void setAllWord(String allWord) {
        this.advSearchDelegate.setAllWord(allWord);
    }

    public String getExactPhrase() {
        return this.advSearchDelegate.getExactPhrase();
    }

    public void setExactPhrase(String exactPhrase) {
        this.advSearchDelegate.setExactPhrase(exactPhrase);
    }

    public String getOneOfWord() {
        return this.advSearchDelegate.getOneOfWord();
    }

    public void setOneOfWord(String oneOfWord) {
        this.advSearchDelegate.setOneOfWord(oneOfWord);
    }

    public String getWithoutWord() {
        return this.advSearchDelegate.getWithoutWord();
    }

    public void setWithoutWord(String withoutWord) {
        this.advSearchDelegate.setWithoutWord(withoutWord);
    }


    public String getSaveSearchDoc(ProcessingContext jParams)
    throws JahiaException {
        return this.advSearchDelegate.getSaveSearchDoc(jParams);
    }

    public void useSavedSearch(ProcessingContext jParams, JahiaSavedSearch savedSearch)
    throws JahiaException {
        this.advSearchDelegate.useSavedSearch(jParams, savedSearch);
    }

    public List<JahiaSavedSearch> getSavedSearches(List<JahiaSavedSearch> allSavedSearches){
        return this.advSearchDelegate.getSavedSearches(allSavedSearches);
    }

}
