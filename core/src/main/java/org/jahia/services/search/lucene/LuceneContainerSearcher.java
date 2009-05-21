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
//
//
//

package org.jahia.services.search.lucene;

import java.util.Collections;

import org.apache.lucene.search.Filter;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.search.SearchHandler;
import org.jahia.services.search.SearchResult;
import org.jahia.services.search.SearchResultImpl;
import org.jahia.services.search.lucene.fs.LuceneSearchHandlerImpl;
import org.jahia.services.version.EntryLoadRequest;


/**
 * Handle containers search.
 * The result for this searcher is an instance of JahiaSearchResult
 * with a List of matching container ids.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class LuceneContainerSearcher extends ContainerSearcher {

    private Filter filter;

    /**
     * Constructor for one single container list search
     * The expected result is a JahiaSearchResult object containing a List of matching containers.
     * The List contained in the JahiaSearchResult is a List of matching ctn ids, not the containers.
     * No right check on container, only on field.
     *
     * @param ctnListID the container list id.
     * @param ctnListID
     * @param query       a valid Lucene search query
     * @param loadRequest
     */
    public LuceneContainerSearcher (int ctnListID, String query, EntryLoadRequest loadRequest) {
        super(ctnListID, query, loadRequest);
    }

    /**
     * Constructor for one single container list search
     * The expected result is a JahiaSearchResult object containing a List of matching containers.
     * The List contained in the JahiaSearchResult is a List of matching ctn ids, not the containers.
     * No right check on container, only on field.
     *
     * @param ctnListID
     * @param containerLevel
     * @param query
     * @param loadRequest
     */
    public LuceneContainerSearcher (int ctnListID, int containerLevel, String query,
                              EntryLoadRequest loadRequest) {
        super(ctnListID, containerLevel, query, loadRequest);
    }

    /**
     * Constructor for a single container list
     *
     * @param containerListName
     * @param params
     * @param query
     * @param loadRequest
     *
     * @throws org.jahia.exceptions.JahiaException
     */
    public LuceneContainerSearcher (String containerListName, ProcessingContext params, String query,
                              EntryLoadRequest loadRequest)
            throws JahiaException {
        super(containerListName, params, query, loadRequest);
    }


    /**
     * Constructor for searching containers of one Site or all Sites,
     * and of one definition or of any definition
     * If siteId = -1 -> search on all sites
     * If containerDefinitionName is null -> ignore container definition type
     * The expected result is a JahiaSearchResult object containing a List of matching containers.
     * The List contained in the JahiaSearchResult is a List of matching ctn ids, not the containers.
     * No right check on container, only on field.
     *
     * @param siteId
     * @param containerDefinitionName
     * @param query
     * @param loadRequest                                                   
     */
    public LuceneContainerSearcher (int siteId, String containerDefinitionName,
                              String query, EntryLoadRequest loadRequest) {
        super(siteId, containerDefinitionName, query, loadRequest);
    }

    public LuceneContainerSearcher (Integer[] siteIds, String containerDefinitionName,
                              String query, EntryLoadRequest loadRequest) {
        super(siteIds, containerDefinitionName, query, loadRequest);
    }
    
    public LuceneContainerSearcher (Integer[] siteIds, String[] containerDefinitionNames,
            String query, EntryLoadRequest loadRequest) {
        super(siteIds, containerDefinitionNames, query, loadRequest);        
    }

    /**
     * Lucene implementation
     *
     * @param query
     * @param searchHandlers
     * @param jParams
     * @return
     * @throws JahiaException
     */
    protected SearchResult doSearch(String query, String[] searchHandlers, ProcessingContext jParams)
            throws JahiaException {

        SearchResult result = new SearchResultImpl();
        ServicesRegistry sReg = ServicesRegistry.getInstance();
        SearchHandler searchHandler = null;
        for ( int i=0; i<searchHandlers.length; i++ ){
            searchHandler = sReg.getJahiaSearchService().getSearchManager()
                    .getSearchHandler(searchHandlers[i]);
            if ( searchHandler instanceof LuceneSearchHandlerImpl ){
                ((LuceneSearchHandlerImpl)searchHandler).search(query, Collections.<String>emptyList(),  result, filter);
            } else {
                searchHandler.search(query, result);
            }
        }
        return result;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

}
