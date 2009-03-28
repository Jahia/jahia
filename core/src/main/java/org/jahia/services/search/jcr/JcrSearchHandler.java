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
    public void notifyIndexUpdate() {
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
