/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
