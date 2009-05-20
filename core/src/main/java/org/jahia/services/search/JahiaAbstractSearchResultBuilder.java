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
package org.jahia.services.search;

import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.compass.JahiaCompassHighlighter;
import org.jahia.services.search.compass.JahiaLuceneSearchEngineHighlighter;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;
import org.jahia.services.search.lucene.JahiaLuceneQueryParser;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.compass.core.CompassHighlighter;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.mapping.ResourceMapping;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Jahia Search result builder
 * 
 * @author NK
 */
public abstract class JahiaAbstractSearchResultBuilder implements
        JahiaSearchResultBuilder {
    private static Logger logger = Logger
            .getLogger(JahiaAbstractSearchResultBuilder.class);
    public final int GROUP_HITS_BY_PAGE = 1;

    public final int GROUP_HITS_BY_CONTAINER = 2;

    private JahiaAbstractHitCollector hitCollector;

    private boolean discardLuceneDoc = true;

    private Sort sorter;

    private SearchResult searchResult = new SearchResultImpl();

    private int maxHits = -1;

    /**
     * the default method used to build Search result from List of ParsedObject instance
     * 
     * @param parsedObjects
     *            Collection a collection of ParsedObject instance
     * @param jParams
     *            ProcessingContext
     * @param queriesAr
     *            The array of queries            
     * @return JahiaSearchResult
     */
    public abstract JahiaSearchResult buildResult(
            Collection<ParsedObject> parsedObjects, ProcessingContext jParams, String[] queriesAr);

    /**
     * Grouping results by object type {@link GROUP_HITS_BY_PAGE,GROUP_HITS_BY_CONTAINER} The map is the object key, the value is a List
     * of hits
     * 
     * @param objectType
     * @param jahiaSearchResult
     * @return
     */
    public abstract Map<Integer, List<JahiaSearchHit>> groupHitsByObject(
            int objectType, JahiaSearchResult jahiaSearchResult);

    public JahiaAbstractHitCollector getHitCollector() {
        return hitCollector;
    }

    public void setHitCollector(JahiaAbstractHitCollector hitCollector) {
        this.hitCollector = hitCollector;
    }

    /**
     * true if the hit matched lucene doc is discarded.
     * 
     * @return
     */
    public boolean isDiscardLuceneDoc() {
        return discardLuceneDoc;
    }

    public void setDiscardLuceneDoc(boolean discardLuceneDoc) {
        this.discardLuceneDoc = discardLuceneDoc;
    }

    /**
     * If not null this sorter will be passed to lucene search
     * 
     * @return
     */
    public Sort getSorter() {
        return sorter;
    }

    public void setSorter(Sort sorter) {
        this.sorter = sorter;
    }

    public SearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(SearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public int getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(int maxHits) {
        this.maxHits = maxHits;
    }

    /**
     * Returns the highlighter for that hit.
     * 
     * @return The highlighter.
     */
    protected CompassHighlighter getHighlighter(ParsedObject parsedObject) {
        CompassHighlighter highlighter = null;
        if (parsedObject != null) {
            SearchHit searchHit = parsedObject.getSearchHit();
            if (searchHit != null) {
                highlighter = searchHit.highlighter();
            }
        }
        return highlighter;
    }

    /**
     * For best highlighting, a query with only the free text search to highlight can be provided
     * 
     * @param queryForHighlighting
     *            , i.e "jahia* OR lucene"
     * @param defaultSearchFieldName
     *            , if null , use JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD
     * @param analyzerName
     *            , if null , use "default"
     * @return
     */
    protected CompassHighlighter getHighlighter(ParsedObject parsedObject,
            String queryForHighlighting, String defaultSearchFieldName,
            String analyzerName) {
        CompassHighlighter highlighter = null;
        if (parsedObject != null) {
            SearchHit searchHit = parsedObject.getSearchHit();
            if (searchHit != null) {
                highlighter = searchHit.highlighter();
                if (highlighter != null
                        && highlighter instanceof JahiaCompassHighlighter) {
                    JahiaCompassHighlighter jahiaCompassHighlighter = (JahiaCompassHighlighter) highlighter;
                    JahiaLuceneSearchEngineHighlighter lh = (JahiaLuceneSearchEngineHighlighter) jahiaCompassHighlighter
                            .getSearchEngineHighlighter();
                    if (analyzerName == null || "".equals(analyzerName.trim())) {
                        analyzerName = "default";
                    }
                    if (defaultSearchFieldName == null
                            || "".equals(defaultSearchFieldName.trim())) {
                        defaultSearchFieldName = JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD;
                    }
                    LuceneSearchEngineFactory luceneFactory = lh.getSearchEngine().getSearchEngineFactory();
                    Analyzer analyzer = luceneFactory.getAnalyzerManager()
                            .getAnalyzer(analyzerName);
                    ResourceMapping resourceMapping = luceneFactory.getMapping().getRootMappingByAlias("jahiaHighlighter");
                    QueryParser queryParser = new JahiaLuceneQueryParser(
                            defaultSearchFieldName, ServicesRegistry
                                    .getInstance().getJahiaSearchService()
                                    .getFieldsGrouping(), -1, analyzer, resourceMapping);
                    try {
                        Query q = queryParser.parse(queryForHighlighting);
                        lh.setSearchQuery(q);
                    } catch (Exception t) {
                        logger.debug("Error parsing lucene query", t);
                    }
                }
            }
        }
        return highlighter;
    }

}
