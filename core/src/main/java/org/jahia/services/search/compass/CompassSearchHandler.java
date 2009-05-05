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
 package org.jahia.services.search.compass;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Sort;
import org.compass.core.*;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.*;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;

import java.util.*;


/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 18:47:47
 * To change this template use File | Settings | File Templates.
 */
public class CompassSearchHandler extends SearchHandlerImpl {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (CompassSearchHandler.class);

    private SearchIndexer indexer = null;

    private Compass compass = null;

    public CompassSearchHandler(){
        super();
    }

    public CompassSearchHandler(Properties config){
        super(config);
    }

    public CompassSearchHandler(SearchManager searchManager, Properties config){
        super(searchManager, config);
    }

    /**
     * This method should be called, once the config and searchManager are set to allow
     * internal initialisation.
     *
     * @throws Exception
     */
    public void init() throws Exception {
        compass = ServicesRegistry.getInstance()
                .getJahiaSearchService().getCompass();
        String readOnly = this.getConfig().getProperty("readOnly");
        if ( readOnly != null ){
            this.setReadOnly("true".equalsIgnoreCase(readOnly));
        }
        if ( !this.getReadOnly() ){
            this.setIndexer(new CompassSearchIndexer(true,new Properties()));
            this.getIndexer().setSearchHandler(this);
            this.getIndexer().start();
        }
    }

    public SearchResult search(String query){
        SearchResult result = new SearchResultImpl();
        search(query, result);
        return result;
    }

    public void search(final String query, List<String> languageCodes, final SearchResult collector, int maxHitsByPage, int maxPages){
        search(query,collector);    
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, Sort sort) {
        throw new UnsupportedOperationException();
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, Sort sort, IndexReader reader) {
        throw new UnsupportedOperationException();
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, Sort sort, IndexReader reader) {
        throw new UnsupportedOperationException();
    }    
    
    public void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, JahiaAbstractHitCollector hitCollector) {
        if (filterQueries != null && filterQueries.length > 0) {
            for (int i = 0, size = filterQueries.length; i < size; i++) {
                query += query + " AND (" + filterQueries[i] + ")";
            }
        }        
        search(query, languageCodes, collector, hitCollector);        
    }
    
    public void search(final String query, List<String> languageCodes, final SearchResult collector, JahiaAbstractHitCollector hitCollector) {
        try {
            CompassTemplate template = new CompassTemplate(compass);
            StringBuffer queryBuffer = new StringBuffer("(");
            queryBuffer.append(query);
            queryBuffer.append(") AND ");
            queryBuffer.append(CompassSearchIndexer.SEARCH_HANDLER_NAME);
            queryBuffer.append(":");
            queryBuffer.append(getName());
            CompassHits hits = template.find(queryBuffer.toString());
            SearchResult result = getSearchResult(hits.detach());
            collector.results().addAll(result.results());
        } catch ( Exception t ){
            logger.debug("Exception occured when performing search", t);
        }
    }

    public void search(final String query, final SearchResult collector){
        search(query, Collections.<String>emptyList(), collector, (Sort)null);
    }

    public SearchIndexer getIndexer(){
        return this.indexer;
    }

    public void setIndexer(SearchIndexer indexer){
        this.indexer = indexer;
    }

    public void shutdown(){
        if ( this.indexer != null ){
            this.indexer.shutdown();
        }
    }

    /**
     *
     * @param hits
     * @return
     * @throws Exception
     */
    protected SearchResult getSearchResult(CompassDetachedHits hits) throws Exception {

       SearchResultImpl searchResult = new CompassSearchResultImpl(hits);
       if ( hits == null || hits.length()==0 ){
           return searchResult;
       }
       int size = hits.length();
       for ( int i=0; i<size; i++ ){
           CompassHit hit = hits.hit(i);

           SearchHitImpl searchHit = new SearchHitImpl(hit);
           searchHit.setSearchResult(searchResult);
           searchHit.setScore(hits.score(i));
           Resource res = hit.getResource();
           Map<String, List<Object>> fieldsMap = new HashMap<String, List<Object>>();
           for ( Property property : res.getProperties() ){
               List<Object> list = new ArrayList<Object>();
               String name = property.getName();
               for ( String val : res.getValues(name) ){
                   list.add(val);
               }
               fieldsMap.put(name,list);
           }
           searchHit.setFields(fieldsMap);
           searchResult.add(searchHit);
       }
       return searchResult;
    }

    public void notifyIndexUpdate(boolean synchronizedIndexing){
        // nothing to do yet.
    }

    public Iterator<String> getTerms(final String query) {
        return null;
    }

}
