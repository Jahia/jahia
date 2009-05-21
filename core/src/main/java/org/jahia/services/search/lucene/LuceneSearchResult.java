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
 package org.jahia.services.search.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.compass.core.CompassHighlighter;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.lucene.LuceneResource;
import org.compass.core.lucene.engine.LuceneSearchEngine;
import org.compass.core.lucene.engine.LuceneSearchEngineHighlighter;
import org.compass.core.lucene.engine.LuceneSearchEngineQuery;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.SearchHit;
import org.jahia.services.search.SearchResultImpl;
import org.jahia.services.search.compass.JahiaLuceneSearchEngineHighlighter;
import org.jahia.services.search.compass.LuceneResourceForHighLighting;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 17:48:29
 * To change this template use File | Settings | File Templates.
 */
public class LuceneSearchResult extends SearchResultImpl {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LuceneSearchResult.class);

    private LuceneSearchEngine searchEngine;
    private LuceneSearchEngineHighlighter highlighter = null;

    /**
     * Without ACL and Time Based Publishing Check
     *
     * @param indexReader
     * @param query
     * @param searchEngine
     */
    public LuceneSearchResult(  IndexReader indexReader,
                                Query query,
                                LuceneSearchEngine searchEngine ){
        this(indexReader,query,searchEngine,false);
    }

    /**
     *
     * @param indexReader
     * @param query
     * @param searchEngine
     * @param checkAccess if true, apply ACL and Time Based Publishing check
     */
    public LuceneSearchResult(  IndexReader indexReader,
                                Query query,
                                LuceneSearchEngine searchEngine,
                                boolean checkAccess ){
        super(checkAccess);
        this.searchEngine = searchEngine;

        if ( searchEngine != null ){
            try {
                highlighter = new JahiaLuceneSearchEngineHighlighter(
                        new LuceneSearchEngineQuery(searchEngine, query),
                        indexReader, searchEngine);
            } catch ( Exception t ){
                logger.debug(t);
            }
        }
    }

    /**
     * Returns an highlighter for the hits.
     */
    public SearchEngineHighlighter getHighlighter(){
        return highlighter;
    }

    /**
     * Returns the highlighter that maps the n'th hit.
     *
     * @param index
     *            The n'th hit.
     * @return The highlighter.
     */
    public CompassHighlighter highlighter(int index) {
        LuceneSearchHit searchHit = (LuceneSearchHit)this.results().get(index);
        if ( searchHit != null ){
            Document doc = searchHit.getDocument();
            if ( doc == null ){
                doc = new Document();
            }
            LuceneResource res = new LuceneResource(doc, searchHit
                    .getDocNumber(), searchEngine.getSearchEngineFactory());
            return ServicesRegistry.getInstance().getJahiaSearchService()
                    .getCompassHighlighter(getHighlighter(), res);
        }
        return null;
    }

    /**
     *
     * @param searchHit
     * @return
     */
    public CompassHighlighter highlighter(SearchHit searchHit){
        if ( searchHit != null ){
            LuceneSearchHit luceneSearchHit = (LuceneSearchHit)searchHit;
            Document doc = luceneSearchHit.getDocument();
            if ( doc == null ){
                doc = new Document();
            }
            LuceneResource res = new LuceneResourceForHighLighting(doc,luceneSearchHit.getDocNumber(),searchEngine.getSearchEngineFactory());
            return ServicesRegistry.getInstance().getJahiaSearchService().getCompassHighlighter(getHighlighter(), res);
        }
        return null;
    }

}
