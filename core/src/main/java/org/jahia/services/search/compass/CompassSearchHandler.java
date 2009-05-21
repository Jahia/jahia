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
