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

import org.jahia.params.ProcessingContext;
import org.jahia.services.search.*;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.XMLWriter;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;

import java.util.*;
import java.io.StringWriter;
import java.io.StringReader;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 8 f?vr. 2005
 * Time: 15:02:10
 * To change this template use File | Settings | File Templates.
 */
public abstract class AdvSearchDelegate {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (AdvSearchDelegate.class);

    protected AdvSearchViewHandler searchViewHandler;

    public static final String FREE_SEARCH = "freeSearch";
    public static final String ALL_WORD_SEARCH = "allWordSearch";
    public static final String EXACT_PHRASE_SEARCH = "exactPhraseSearch";
    public static final String ONE_OF_WORD_SEARCH = "oneOfWordSearch";
    public static final String WITHOUT_WORD_SEARCH = "withoutWordSearch";

    public static final String SEARCH_MODE = "searchMode";

    public static final String VALUE_ELEMENT = "value";

    protected String freeSearch = "";
    protected String allWord = "";
    protected String exactPhrase = "";
    protected String oneOfWord = "";
    protected String withoutWord = "";
    protected List<String> languageCodes = new ArrayList<String>();
    protected String query = "";

    protected JahiaSearchResult searchResult;
    protected JahiaSearchResultBuilder resultBuilder;
    protected SearchOptionsHandler searchOptionsHandler;

    public AdvSearchDelegate(AdvSearchViewHandler searchViewHandler){
        this.searchViewHandler = searchViewHandler;
        this.searchOptionsHandler = new SearchOptionsHandler(searchViewHandler);
    }

    public void init(ProcessingContext jParams, Map<String, Object> engineMap)
    throws JahiaException {

        if ( !this.getSearchViewHandler().isSearchModeChanged() ){
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
        this.searchOptionsHandler.handleActions(jParams, engineMap);
        this.buildQuery();
    }

    public SearchOptionsHandler getSearchOptionsHandler() {
        return searchOptionsHandler;
    }

    public void setSearchOptionsHandler(SearchOptionsHandler searchOptionsHandler) {
        this.searchOptionsHandler = searchOptionsHandler;
    }

    public abstract JahiaSearchResult search(ProcessingContext jParams)
    throws JahiaException;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public JahiaSearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(JahiaSearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public JahiaSearchResultBuilder getResultBuilder() {
        return resultBuilder;
    }

    public void setResultBuilder(JahiaSearchResultBuilder resultBuilder) {
        this.resultBuilder = resultBuilder;
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
            buffer.append(this.getFreeSearch());
            buffer.append(withoutWordString);
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

    public AdvSearchViewHandler getSearchViewHandler() {
        return searchViewHandler;
    }

    public void setSearchViewHandler(AdvSearchViewHandler searchViewHandler) {
        this.searchViewHandler = searchViewHandler;
    }

    public String getSaveSearchDoc(ProcessingContext jParams)
    throws JahiaException {

        String result = "";
        try {
            Document doc = DocumentHelper.createDocument();
            Element root = doc.addElement(JahiaSavedSearch.SEARCH_ELEMENT);

            root.addElement(SEARCH_MODE)
                    .addText(String.valueOf(this.getSearchViewHandler().getSearchMode()));
            root.addElement(FREE_SEARCH).addCDATA(this.freeSearch);
            root.addElement(ALL_WORD_SEARCH).addCDATA(this.allWord);
            root.addElement(EXACT_PHRASE_SEARCH).addCDATA(this.exactPhrase);
            root.addElement(ONE_OF_WORD_SEARCH).addCDATA(this.oneOfWord);
            root.addElement(WITHOUT_WORD_SEARCH).addCDATA(this.withoutWord);

            appendSaveSearchDoc(jParams,root);
            root.addElement(JahiaSavedSearch.QUERY_ELEMENT).addCDATA(JahiaSavedSearch.encodeCDATA_Tag(this.getQuery()));

            StringWriter out = new StringWriter(1024);
            XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());
            writer.setWriter(out);
            writer.write(doc);
            result = out.toString();
        } catch ( Exception t ){
            throw new JahiaException("Error occured when created the XML representation of the search",
                "Error occured when created the XML representation of the search",
                JahiaException.ENGINE_ERROR,JahiaException.ERROR_SEVERITY,t);
        }
        return result;
    }

    protected abstract void appendSaveSearchDoc(ProcessingContext jParams, Element root) throws JahiaException;

    protected abstract void useSaveSearchDoc(ProcessingContext jParams, Element root) throws JahiaException;

    public void useSavedSearch(ProcessingContext jParams,
                               JahiaSavedSearch savedSearch) {
        try
        {
            SAXReader reader = new SAXReader();
            Document document = reader.read(new StringReader(savedSearch.getSearch()));
            Element root = document.getRootElement();
            if (root != null)
            {
                Element el = root.element(FREE_SEARCH);
                if ( el != null ){
                    this.freeSearch = el.getText();
                }
                el = root.element(ALL_WORD_SEARCH);
                if ( el != null ){
                    this.allWord = el.getText();
                }
                el = root.element(EXACT_PHRASE_SEARCH);
                if ( el != null ){
                    this.exactPhrase = el.getText();
                }
                el = root.element(ONE_OF_WORD_SEARCH);
                if ( el != null ){
                    this.oneOfWord = el.getText();
                }
                el = root.element(WITHOUT_WORD_SEARCH);
                if ( el != null ){
                    this.withoutWord = el.getText();
                }
                this.useSaveSearchDoc(jParams,root);
            }
        }
        catch (Exception t){
            logger.debug("Error parsing JahiaSavedSearch xml",t);
        }
    }


    public List<JahiaSavedSearch> getSavedSearches(List<JahiaSavedSearch> allSavedSearches) {
        List<JahiaSavedSearch> list = new ArrayList<JahiaSavedSearch>();
        for (JahiaSavedSearch savedSearch : allSavedSearches){
            if ( this.getSearchViewHandler().getClass().getName()
                    .equals(savedSearch.getSearchViewHandlerClass()) ){
                try
                {
                    SAXReader reader = new SAXReader();
                    Document document = reader.read(new StringReader(savedSearch.getSearch()));
                    Element root = document.getRootElement();
                    Element el = root.element(SEARCH_MODE);
                    if (el != null)
                    {
                        int mode = Integer.parseInt(el.getText());
                        if ( mode == this.getSearchViewHandler().getSearchMode() ){
                            list.add(savedSearch);
                        }
                    }
                }
                catch (Exception t){
                    logger.debug("Error parsing JahiaSavedSearch xml",t);
                }
            }
        }
        return list;
    }

}
