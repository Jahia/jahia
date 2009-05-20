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

import org.jahia.params.ProcessingContext;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.*;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.exceptions.JahiaException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 f�vr. 2005
 * Time: 15:02:10
 * To change this template use File | Settings | File Templates.
 */
public class SearchViewHandlerImpl extends AbstractSearchViewHandler {

    private String freeSearch = "";
    private String allWord = "";
    private String exactPhrase = "";
    private String oneOfWord = "";
    private String withoutWord = "";
    private List<String> languageCodes = new ArrayList<String>();
    private JahiaSearchResultBuilder resultBuilder = null;
    private List<String> domains = new ArrayList<String>();
    private List<String> refineSearchAttributes;

    public static final String FREE_SEARCH = "freeSearch";
    public static final String ALL_WORD_SEARCH = "allWordSearch";
    public static final String EXACT_PHRASE_SEARCH = "exactPhraseSearch";
    public static final String ONE_OF_WORD_SEARCH = "oneOfWordSearch";
    public static final String WITHOUT_WORD_SEARCH = "withoutWordSearch";
    public static final String SEARCH_DOMAIN = "searchDomain";

    public void init(ProcessingContext jParams, Map<String, Object> engineMap){

        String value = jParams.getParameter(FREE_SEARCH);
        if ( value != null ){
            this.freeSearch = value;
        }

        value = jParams.getParameter(ALL_WORD_SEARCH);
        if ( value != null ){
            this.allWord = value;
        }

        value = jParams.getParameter(EXACT_PHRASE_SEARCH);
        if ( value != null ){
            this.exactPhrase = value;
        }

        value = jParams.getParameter(ONE_OF_WORD_SEARCH);
        if ( value != null ){
            this.oneOfWord = value;
        }

        value = jParams.getParameter(WITHOUT_WORD_SEARCH);
        if ( value != null ){
            this.withoutWord = value;
        }

        String[] values = jParams.getParameterValues(SEARCH_DOMAIN);
        if ( values != null ){
            this.domains = new ArrayList<String>();
            for ( int i=0 ;i<values.length; i++ ){
                this.domains.add(values[i]);
            }
        }

        languageCodes = (List<String>)engineMap.get("searchLanguageCodes");
        if ( languageCodes == null ){
            languageCodes = new ArrayList<String>();
        }


        screen = (String)engineMap.get("screen");
        if ( screen == null ){
            screen = "execute";
        }

        buildQuery();
    }

    /**
     * handles search operations like search option update
     *
     * @param jParams
     * @param engineMap
     */
    public void update(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException{
        List<String> l  = (List<String>)engineMap.get(Search_Engine.SEARCH_REFINE_ATTRIBUTE);
        this.refineSearchAttributes = new ArrayList<String>();
        if ( l != null ){
            this.refineSearchAttributes.addAll(l);
        }
    }

    public JahiaSearchResult search(ProcessingContext jParams) throws JahiaException {
        if ( screen.equals("execute") ){

            if (resultBuilder == null) {
                boolean oneHitPerPage =
                        ("true".equals(jParams.
                        getParameter(PageSearchResultBuilderImpl.ONLY_ONE_HIT_BY_PAGE)));
                resultBuilder =
                        new PageSearchResultBuilderImpl(oneHitPerPage);
            }

            List<String> ar = new ArrayList<String>();
            if ( this.domains.contains("anywhere") ){
                Map<String, SearchHandler> searchHandlers = ServicesRegistry.getInstance()
                        .getJahiaSearchService().getSearchManager().getSearchHandlers();
                for ( String name : searchHandlers.keySet() ){
                    if ( !name.equals(JahiaSearchBaseService.WEBDAV_SEARCH) ){
                        ar.add(name);
                    }
                }
            } else if ( this.domains.size()==0 ){
                SearchHandler searchHandler = ServicesRegistry.getInstance()
                        .getJahiaSearchService().getSearchHandler(jParams.getSiteID());
                if ( searchHandler != null ){
                    ar.add(searchHandler.getName());
                }
            } else {
                ar.addAll(domains);
            }
            String[] searchHandlers = new String[ar.size()];
            ar.toArray(searchHandlers);

            String refineSearchQuery = Search_Engine.buildSearchRefineQuery(this.refineSearchAttributes);
            StringBuffer queryBuffer = new StringBuffer("(").append(query).append(")");
            if ( refineSearchQuery != null && !"".equals(refineSearchQuery.trim()) ){
                queryBuffer.append(" AND ( ").append(refineSearchQuery).append(" )");
            }
            String startPageNodeQuery = Search_Engine.buildStartPageNodeQuery(jParams.getParameter("startPageNode"),
                    jParams);
            if ( startPageNodeQuery != null && !"".equals(startPageNodeQuery.trim()) ){
                queryBuffer.append(" AND (").append(startPageNodeQuery).append(") ");
            }

            searchResult = ServicesRegistry
                .getInstance ().getJahiaSearchService ()
                .search(searchHandlers, queryBuffer.toString(), "", jParams, languageCodes, resultBuilder);
        }
        if (searchResult == null) {
            searchResult = new JahiaSearchResult(new
                    PageSearchResultBuilderImpl());
        }
        return searchResult;
    }

    public void buildQuery(){

        String withoutWordString = "";
        if ( !"".equals(this.getWithoutWord()) ){
            StringBuffer buff = new StringBuffer();
            StringTokenizer tokenizer = new StringTokenizer(this.getWithoutWord().trim()," ");
            String token = null;
            while ( tokenizer.hasMoreElements() ){
                token = tokenizer.nextToken();
                buff.append(" -");
                buff.append(token);
            }
            withoutWordString = buff.toString();
        }

        StringBuffer buffer = new StringBuffer();
        if ( !"".equals(this.getFreeSearch().trim()) ){
            buffer.append("(");
            buffer.append(this.getFreeSearch());
            buffer.append(withoutWordString);
            buffer.append(")");
        }
        if ( !"".equals(this.getAllWord().trim()) ){
            StringTokenizer tokenizer = new StringTokenizer(this.getAllWord().trim()," ");
            String token = null;
            if ( tokenizer.hasMoreElements() ) {
                if ( buffer.length()>0 ){
                    buffer.append(" AND ");
                }
                StringBuffer searchQueryBuffer = new StringBuffer("(");
                while ( tokenizer.hasMoreElements() ){
                    token = tokenizer.nextToken();
                    searchQueryBuffer.append(" +");
                    searchQueryBuffer.append(token);
                }
                buffer.append(searchQueryBuffer.toString());
                buffer.append(withoutWordString);
                buffer.append(" )");
            }
        }
        if ( !"".equals(this.getExactPhrase().trim()) ){
            if ( buffer.length()>0 ){
                buffer.append(" AND ");
            }
            buffer.append("(");
            buffer.append('"' + this.getExactPhrase().trim() + '"');
            buffer.append(withoutWordString);
            buffer.append(" )");
        }
        if ( !"".equals(this.getOneOfWord().trim()) ){
            if ( buffer.length()>0 ){
                buffer.append(" AND ");
            }
            buffer.append(" (");
            buffer.append(withoutWordString);
            buffer.append(this.getOneOfWord());
            buffer.append(") ");
        }
        query = buffer.toString();
        if ( query.length() == 0){
            query = withoutWordString;
        }
    }

    public String getFreeSearch() {
        return freeSearch;
    }

    public void setFreeSearch(String freeSearch) {
        this.freeSearch = freeSearch;
    }


    public String getAllWord() {
        return allWord;
    }

    public void setAllWord(String allWord) {
        this.allWord = allWord;
    }

    public String getExactPhrase() {
        return exactPhrase;
    }

    public void setExactPhrase(String exactPhrase) {
        this.exactPhrase = exactPhrase;
    }

    public String getOneOfWord() {
        return oneOfWord;
    }

    public void setOneOfWord(String oneOfWord) {
        this.oneOfWord = oneOfWord;
    }

    public String getWithoutWord() {
        return withoutWord;
    }

    public void setWithoutWord(String withoutWord) {
        this.withoutWord = withoutWord;
    }

    public List<String> getLanguageCodes() {
        return languageCodes;
    }

    public void setLanguageCodes(List<String> languageCodes) {
        this.languageCodes = languageCodes;
    }

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<String> getRefineSearchAttributes() {
        return refineSearchAttributes;
    }

    public void setRefineSearchAttributes(List<String> refineSearchAttributes) {
        this.refineSearchAttributes = refineSearchAttributes;
    }

    public JahiaSearchResultBuilder getResultBuilder() {
        return resultBuilder;
    }

    public void setResultBuilder(JahiaSearchResultBuilder resultBuilder) {
        this.resultBuilder = resultBuilder;
    }

    public String getSaveSearchDoc(ProcessingContext jParams)
    throws JahiaException {
        return ""; // not supported
    }

    public void useSavedSearch(ProcessingContext jParams, JahiaSavedSearch savedSearch)
    throws JahiaException {
        // not supported
    }

    public List<JahiaSavedSearch> getSavedSearches(List<JahiaSavedSearch> allSavedSearches){
        return new ArrayList<JahiaSavedSearch>(); // not supported
    }

    public void setSearchMode(int searchMode) {
        // not supported
    }

    public boolean isSearchModeChanged(){
        return false;
    }

}
