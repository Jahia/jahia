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

