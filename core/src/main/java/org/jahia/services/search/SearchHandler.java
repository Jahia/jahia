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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Sort;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;

import java.util.*;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 15 f�vr. 2005
 * Time: 15:33:30
 * To change this template use File | Settings | File Templates.
 */
public abstract class SearchHandler implements Shutdownable {

    /**
     * This method should be called, once the config and searchManager are set to allow
     * internal initialisation.
     *
     * @throws Exception
     */
    public abstract void init() throws Exception;

    public abstract Properties getConfig();

    public abstract void setConfig(Properties config);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getTitle();

    public abstract void setTitle(String title);
    
    public abstract int getSiteId();

    public abstract void setSiteId(int siteId);

    public abstract SearchResult search(String query);

    public abstract void search(String query, SearchResult collector);
    
    public abstract void search(String query, List<String> languageCodes, SearchResult collector, JahiaAbstractHitCollector hitCollector);
    
    public abstract void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, JahiaAbstractHitCollector hitCollector);    

    public abstract void search(String query, List<String> languageCodes, SearchResult collector, Sort sort);

    public abstract void search(String query, List<String> languageCodes, SearchResult collector, Sort sort, IndexReader reader);
    
    public abstract void search(String query, List<String> languageCodes, SearchResult collector, String[] filterQueries, Sort sort, IndexReader reader);    

    public abstract SearchIndexer getIndexer();

    public abstract SearchManager getSearchManager();

    public abstract void setSearchManager(SearchManager searchManager);

    public abstract void registerListerer(String name, SearchEventListener listener);

    public abstract void unregisterListerer(String name);

    public abstract void addDocument(IndexableDocument doc);

    public abstract void removeDocument(RemovableDocument doc);

    public abstract void batchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd);

    public abstract void synchronizedBatchIndexing(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd);

    public abstract boolean getReadOnly();

    public abstract void setReadOnly(boolean readOnly);

    public abstract String getAllSearchFieldName();

    public abstract void setAllSearchFieldName(String allSearchFieldName);

    /**
     *
     * @param ev
     * @param methodName a SearchListener method name
     */
    public abstract void notify(SearchEvent ev,
                                String methodName);

    /**
     * used to notify the search handler that the index has been changed
     */
    public abstract void notifyIndexUpdate(boolean waitForNewSearcher);

    public abstract Iterator<String> getTerms(final String query);

}
