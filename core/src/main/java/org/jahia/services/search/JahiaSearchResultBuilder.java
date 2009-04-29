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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jahia.data.search.JahiaSearchHit;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;
import org.apache.lucene.search.Sort;

/**
 * Jahia Search result builder
 *
 * @author NK
 */
public interface JahiaSearchResultBuilder
{

    public final int GROUP_HITS_BY_PAGE = 1;

    public final int GROUP_HITS_BY_CONTAINER = 2;

    /**
     * the default method used to build Search result from List of ParsedObject instance
     *
     * @param parsedObjects Collection a collection of ParsedObject instance
     * @param jParams ProcessingContext
     * @param queriesArray The array of queries (also filtered) 
     * @return JahiaSearchResult
     */
    public abstract JahiaSearchResult buildResult(Collection<ParsedObject> parsedObjects,
                                                  ProcessingContext jParams, String[] queriesArray);

    /**
     * Grouping results by object type {@link GROUP_HITS_BY_PAGE,GROUP_HITS_BY_CONTAINER}
     * The map is the object key, the value is a List of hits
     *
     * @param objectType
     * @param jahiaSearchResult
     * @return
     */
    public abstract Map<Integer, List<JahiaSearchHit>> groupHitsByObject(int objectType,
                                          JahiaSearchResult jahiaSearchResult);

    /**
     * The an optional hit collector
     *
     * @return
     */
    public abstract JahiaAbstractHitCollector getHitCollector();

    /**
     * true if the hit matched lucene doc is discarded.
     * @return
     */
    public boolean isDiscardLuceneDoc();

    public void setHitCollector(JahiaAbstractHitCollector hitCollector);

    public void setDiscardLuceneDoc(boolean discardLuceneDoc);

    /**
     * If not null this sorter will be passed to lucene search
     *
     * @return
     */
    public Sort getSorter();

    public void setSorter(Sort sorter);

    /**
     * A search result builder can provide its own SearchResult that will be used to collect search Hit at low level
      * @return
     */
    public SearchResult getSearchResult();

    public void setSearchResult(SearchResult searchResult);

    /**
     * 
     * @return
     */
    public int getMaxHits();

    public void setMaxHits(int maxHits);

}

