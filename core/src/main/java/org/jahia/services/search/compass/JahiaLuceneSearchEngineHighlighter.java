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

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.compass.core.engine.SearchEngineException;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHighlighter;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 janv. 2007
 * Time: 13:56:10
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLuceneSearchEngineHighlighter extends LuceneSearchEngineHighlighter {

    private static Logger logger =
            Logger.getLogger (JahiaLuceneSearchEngineHighlighter.class);

    private Query searchQuery;
    private boolean differentQueryForHighlighting = false;
    private LuceneSearchEngine searchEngine;
    private IndexReader indexReader;
    
    public JahiaLuceneSearchEngineHighlighter(
            LuceneSearchEngineQuery searchEngineQuery, IndexReader indexReader,
            LuceneSearchEngine searchEngine)
            throws SearchEngineException {
        super(searchEngineQuery.getQuery(), indexReader, searchEngine);
        this.searchEngine = searchEngine;
        this.indexReader = indexReader;
    }
    
    public JahiaLuceneSearchEngineHighlighter(
            LuceneSearchEngineQuery searchEngineQuery, IndexReader indexReader,
            LuceneSearchEngine searchEngine, Query query)
            throws SearchEngineException {
        super(searchEngineQuery.getQuery(), indexReader, searchEngine);
        this.searchQuery = query;
        this.searchEngine = searchEngine;
        this.indexReader = indexReader;
    }

    public Query getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(Query searchQuery) {
        this.searchQuery = searchQuery;
        if (searchEngine.getSearchEngineFactory().getHighlighterManager()
                .getDefaultHighlighterSettings().isRewriteQuery()) {
            try {
                this.searchQuery = this.searchQuery.rewrite(this.indexReader);
            } catch (Exception t) {
                logger.debug("Exception occured creating scorer", t);
            }
        }
        this.differentQueryForHighlighting = true;
    }

    public LuceneSearchEngine getSearchEngine() {
        return searchEngine;
    }

    public void setSearchEngine(LuceneSearchEngine searchEngine) {
        this.searchEngine = searchEngine;
    }

    protected Scorer createScorer(String propertyName) throws SearchEngineException {
        return this.searchQuery != null ? new QueryScorer(
                new LuceneQueryForHighlighting(this.searchQuery, propertyName,
                        this.differentQueryForHighlighting), null) : super
                .createScorer(propertyName);
    }

    public IndexReader getIndexReader() {
        return indexReader;
    }

    public void setIndexReader(IndexReader indexReader) {
        this.indexReader = indexReader;
    }

}
