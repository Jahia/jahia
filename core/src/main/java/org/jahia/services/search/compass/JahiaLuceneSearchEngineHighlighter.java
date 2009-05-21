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
