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
package org.jahia.services.search.jcr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.jackrabbit.JcrConstants;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Sort;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.search.SearchHandlerImpl;
import org.jahia.services.search.SearchHitImpl;
import org.jahia.services.search.SearchIndexer;
import org.jahia.services.search.SearchManager;
import org.jahia.services.search.SearchResult;
import org.jahia.services.search.SearchResultImpl;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;

/**
 * Implementation of the search handler for the JCR.
 *
 * @author hollis
 */
public class JcrSearchHandler extends SearchHandlerImpl {

    private static Logger logger = Logger.getLogger(JcrSearchHandler.class);

    public JcrSearchHandler() {
        super();
    }

    public JcrSearchHandler(Properties config) {
        super(config);
    }

    public JcrSearchHandler(SearchManager searchManager, Properties config) {
        super(searchManager, config);
    }

    @Override
    public SearchIndexer getIndexer() {
        return null;
    }

    /**
     * This method should be called, once the config and searchManager are set
     * to allow internal initialisation.
     *
     * @throws Exception
     */
    @Override
    public void init() throws Exception {
        // do nothing
    }

    @Override
    public void notifyIndexUpdate(boolean synchronizedIndexing) {
        // nothing to do yet.
    }

    @Override
    public SearchResult search(String query) {
        SearchResult result = new SearchResultImpl();
        search(query, result);
        return result;
    }

    @Override
    public void search(String queryStatement, SearchResult collector) {

        try {

            ParamBean jParams = (ParamBean) Jahia.getThreadParamBean();

            if (queryStatement == null || queryStatement.length() == 0) {
                return;
            }

            QueryManager qm = JCRStoreService.getInstance().getQueryManager(jParams.getUser());
            Query query = qm.createQuery(queryStatement, Query.XPATH);

            QueryResult queryResult = query.execute();
            RowIterator it = queryResult.getRows();

            SearchResult searchResult = new SearchResultImpl(false);
            while (it.hasNext()) {
                Row row = it.nextRow();
                String path = row.getValue(JcrConstants.JCR_PATH).getString();
                SearchHitImpl searchHit = new SearchHitImpl(path);

                List<Object> list = new LinkedList<Object>();
                list.add(path);
                searchHit.getFields().put("uri", list);

                searchHit.setScore((float) (row.getValue(JcrConstants.JCR_SCORE)
                        .getDouble() / 1000.));
                
                // this is Jackrabbit specific, so if other implementations
                // throw exceptions, we have to do a check here 
                Value excerpt = row.getValue("rep:excerpt(jcr:content)");
                if (excerpt != null) {
                    searchHit.setExcerpt(excerpt.getString());
                }
                
                searchResult.add(searchHit);
                searchHit.setSearchResult(searchResult);
            }

            collector.results().addAll(searchResult.results());
        } catch (Exception t) {
            logger.error("Exception in document search. Cause: " + t.getMessage(), t);
        }
    }

    @Override
    public void search(String query, List<String> languageCodes, SearchResult collector,
                       JahiaAbstractHitCollector hitCollector) {
        this.search(query, collector);
    }

    public void shutdown() {
        // do nothing
    }

    @Override
    public void search(String query, List<String> languageCodes, SearchResult collector, Sort sort) {
        throw new UnsupportedOperationException(
                "Method search(String, SearchResult, Sort) is not supported by this implementation");
    }

    @Override
    public void search(String query, List<String> languageCodes, SearchResult collector, Sort sort,
                       IndexReader reader) {
        throw new UnsupportedOperationException(
                "Method search(String, SearchResult, Sort, IndexReader) is not supported by this implementation");
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, JahiaAbstractHitCollector hitCollector) {
        throw new UnsupportedOperationException(
                "Method search(String, SearchResult, Sort) is not supported by this implementation");
    }

    public void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, Sort sort, IndexReader reader) {
        throw new UnsupportedOperationException(
                "Method search(String, SearchResult, Sort) is not supported by this implementation");
    }
        

    @Override
    public Iterator<String> getTerms(String query) {
        throw new UnsupportedOperationException(
        "Method getTerms(String) is not supported by this implementation");
    }
}
