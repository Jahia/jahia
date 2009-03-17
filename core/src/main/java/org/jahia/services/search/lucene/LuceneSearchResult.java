/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
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
