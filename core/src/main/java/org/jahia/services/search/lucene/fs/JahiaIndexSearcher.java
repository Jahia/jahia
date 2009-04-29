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
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.services.search.lucene.fs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.CachingWrapperFilter;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.Directory;

public class JahiaIndexSearcher extends Searcher {
    private static Logger log = Logger.getLogger(JahiaIndexSearcher.class
            .getName());

    private final String name;
    private final IndexSearcher searcher;
    private final IndexReader reader;
    private final boolean closeReader;

    private final boolean cachingEnabled;
    private final SearcherCache filterCache;

    private final SearcherCache[] cacheList;
    private static final SearcherCache[] noCaches = new SearcherCache[0];

    /** Creates a searcher searching the index in the named directory. */
    public JahiaIndexSearcher(LuceneCoreSearcher core, String name,
            String path, boolean enableCache) throws IOException {
        this(core, name, IndexReader.open(path), true, enableCache);
    }

    /** Creates a searcher searching the index in the provided directory. */
    public JahiaIndexSearcher(LuceneCoreSearcher core, String name,
            Directory directory, boolean enableCache) throws IOException {
        this(core, name, IndexReader.open(directory), true, enableCache);
    }

    /** Creates a searcher searching the provided index. */
    public JahiaIndexSearcher(LuceneCoreSearcher core, String name,
            IndexReader r, boolean enableCache) {
        this(core, name, r, false, enableCache);
    }

    private JahiaIndexSearcher(LuceneCoreSearcher core, String name,
            IndexReader r, boolean closeReader, boolean enableCache) {
        this.name = "Searcher@" + Integer.toHexString(hashCode())
                + (name != null ? " " + name : "");

        log.info("Opening " + this.name);

        reader = r;
        searcher = new IndexSearcher(r);
        this.closeReader = closeReader;

        cachingEnabled = enableCache;
        if (cachingEnabled) {
            List<SearcherCache> clist = new ArrayList<SearcherCache>();
            filterCache = CacheConfig.getConfig(core.getFilterCacheConfig())
                    .newInstance();
            if (filterCache != null)
                clist.add(filterCache);

            cacheList = clist.toArray(new SearcherCache[clist.size()]);
        } else {
            filterCache = null;
            cacheList = noCaches;
        }
    }

    public void close() throws IOException {
        // unregister first, so no management actions are tried on a closing searcher.

        try {
            searcher.close();
        } finally {
            if (closeReader)
                reader.close();
        }
    }

    public int maxDoc() throws IOException {
        return searcher.maxDoc();
    }

    public TopDocs search(Weight weight, Filter filter, int i)
            throws IOException {
        return searcher.search(weight, filter, i);
    }

    public void search(Weight weight, Filter filter, HitCollector hitCollector)
            throws IOException {
        searcher.search(weight, filter, hitCollector);
    }

    public Query rewrite(Query original) throws IOException {
        return searcher.rewrite(original);
    }

    public Explanation explain(Weight weight, int i) throws IOException {
        return searcher.explain(weight, i);
    }

    public TopFieldDocs search(Weight weight, Filter filter, int i, Sort sort)
            throws IOException {
        return searcher.search(weight, filter, i, sort);
    }

    /**
     * Retrieve the {@link Document} instance corresponding to the document id.
     */
    public Document doc(int i) throws IOException {
        return doc(i, (Set<Fieldable>) null);
    }

    /**
     * Retrieve a {@link Document} using a {@link org.apache.lucene.document.FieldSelector} This method does not currently use the Solr
     * document cache.
     * 
     * @see IndexReader#document(int, FieldSelector)
     */
    public Document doc(int n, FieldSelector fieldSelector) throws IOException {
        return searcher.getIndexReader().document(n, fieldSelector);
    }

    /**
     * Retrieve the {@link Document} instance corresponding to the document id.
     * 
     * Note: The document will have all fields accessable, but if a field filter is provided, only the provided fields will be loaded (the
     * remainder will be available lazily).
     */
    public Document doc(int i, Set<Fieldable> fields) throws IOException {
        return searcher.getIndexReader().document(i);
    }

    public void setSimilarity(Similarity similarity) {
        searcher.setSimilarity(similarity);
    }

    public Similarity getSimilarity() {
        return searcher.getSimilarity();
    }

    public int docFreq(Term term) throws IOException {
        return searcher.docFreq(term);
    }

    /** Direct access to the IndexReader used by this searcher */
    public IndexReader getReader() {
        return reader;
    }

    /**
     * Compute and cache the DocSet that matches a query. The normal usage is expected to be cacheDocSet(myQuery, null,false) meaning that
     * Solr will determine if the Query warrants caching, and if so, will compute the DocSet that matches the Query and cache it. If the
     * answer to the query is already cached, nothing further will be done.
     * <p>
     * If the optionalAnswer DocSet is provided, it should *not* be modified after this call.
     * 
     * @param query
     *                the lucene query that will act as the key
     * @param optionalAnswer
     *                the DocSet to be cached - if null, it will be computed.
     * @param mustCache
     *                if true, a best effort will be made to cache this entry. if false, heuristics may be used to determine if it should be
     *                cached.
     */
    public void cacheFilter(Query query, Filter optionalAnswer,
            boolean mustCache) throws IOException {
        // Even if the cache is null, still compute the DocSet as it may serve to warm the Lucene
        // or OS disk cache.
        if (optionalAnswer != null) {
            if (filterCache != null) {
                filterCache.put(query, optionalAnswer);
            }
            return;
        }

        // Throw away the result, relying on the fact that getDocSet
        // will currently always cache what it found. If getDocSet() starts
        // using heuristics about what to cache, and mustCache==true, (or if we
        // want this method to start using heuristics too) then
        // this needs to change.
        getFilter(query);
    }

    /**
     * Returns the set of document ids matching a query. This method is cache-aware and attempts to retrieve the answer from the cache if
     * possible. If the answer was not cached, it may have been inserted into the cache as a result of this call. This method can handle
     * negative queries.
     * <p>
     * The DocSet returned should <b>not</b> be modified.
     */
    public Filter getFilter(Query query) throws IOException {
        // Get the absolute value (positive version) of this query. If we
        // get back the same reference, we know it's positive.
        if (filterCache != null) {
            Filter filter = (Filter) filterCache.get(query);
            if (filter != null) {
                return filter;
            }
        }

        Filter filter = new CachingWrapperFilter(new QueryWrapperFilter(query));
        filter.bits(this.getReader());
        
        if (filterCache != null) {
            // cache negative queries as positive
            filterCache.put(query, filter);
        }

        return filter;
    }

    /**
     * Warm this searcher based on an old one (primarily for auto-cache warming).
     */
    public void warm(JahiaIndexSearcher old) throws IOException {
        // Make sure this is first! filters can help queryResults execute!
        boolean logme = log.isInfoEnabled();

        // warm the caches in order...
        for (int i = 0; i < cacheList.length; i++) {
            if (logme)
                log.info("autowarming " + this + " from " + old + "\n\t"
                        + old.cacheList[i]);
            this.cacheList[i].warm(this, old.cacheList[i]);
            if (logme)
                log.info("autowarming result for " + this + "\n\t"
                        + this.cacheList[i]);
        }
    }
    
    /** Register sub-objects such as caches
     */
    public void register() {
      for (SearcherCache cache : cacheList) {
        cache.setState(SearcherCache.State.LIVE);
      }
    }    
}