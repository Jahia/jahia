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
