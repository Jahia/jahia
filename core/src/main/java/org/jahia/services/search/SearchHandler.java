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
    public abstract void notifyIndexUpdate();

    public abstract Iterator<String> getTerms(final String query);

}
