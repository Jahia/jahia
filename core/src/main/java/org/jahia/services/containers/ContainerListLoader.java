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
package org.jahia.services.containers;

import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentFieldKey;
import org.jahia.content.CoreFilterNames;
import org.jahia.content.ObjectKey;
import org.jahia.data.containers.*;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.search.*;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.OrderedBitSet;

import java.io.Serializable;
import java.util.*;

/**
 * this class is used to effectively loads Container List applying filtering, searching and sorting if needed
 *
 * User: hollis
 * Date: 10 mars 2008
 * Time: 16:15:44
 * To change this template use File | Settings | File Templates.
 */
public class ContainerListLoader implements Serializable {

    private static final long serialVersionUID = -2110579553569562647L;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerListLoader.class);

    private transient Boolean isValid;
    private transient Map<String, ContainerSearcher> containerSearchersMap;
    private transient Map<String, ContainerFilters> containerFiltersMap;
    private transient Map<String, ContainerSorterInterface> containerSortersMap;

    protected ContainerListLoader(){
        containerSearchersMap = new HashMap<String, ContainerSearcher>();
        containerFiltersMap = new HashMap<String, ContainerFilters>();
        containerSortersMap = new HashMap<String, ContainerSorterInterface>();
        isValid = Boolean.TRUE;
    }

    /**
     *
     * @param context
     * @param cList
     * @return
     * @throws JahiaException
     */
    public static ContainerListLoader getInstance(  final ProcessingContext context,
                                                    final JahiaContainerList cList)
    throws JahiaException {

        if (cList == null || cList.getID()==-1 || context==null){
            return new ContainerListLoader();
        }

        ContainerListLoader cLoader = new ContainerListLoader();
        cLoader.initMaps();

        return cLoader;
    }

    public boolean isValid(){
        return (this.isValid != null && this.isValid.booleanValue());
    }

    void initMaps(){
        if (this.containerFiltersMap == null){
            this.containerFiltersMap = new HashMap<String, ContainerFilters>();
        }
        if (this.containerSearchersMap == null){
            this.containerSearchersMap = new HashMap<String, ContainerSearcher>();
        }
        if (this.containerSortersMap == null){
            this.containerSortersMap = new HashMap<String, ContainerSorterInterface>();
        }
    }

    /**
     * Apply searching, filtering and sorting on the given container list.
     *
     * @param loaderContext
     * @return List of matching ctn ids, sorted and filtered.
     */

    public List<Integer> doContainerFilterSearchSort(final ContainerListLoaderContext loaderContext) {

        JahiaContainerList cList = loaderContext.getCList();
        EntryLoadRequest loadVersion = loaderContext.getLoadVersion();

        try {
            loaderContext.setLoadingUseSingleSearchQuery(Boolean.valueOf(this.isLoadingUseSingleSearchQuery(loaderContext)));
            BitSet resultBitSet = null;
            BitSet filterBitSet = null;
            JahiaSearchResult searchResult = null;
            ContainerFilters cFilters = null;

            // Apply container list search if needed.
            final ContainerSearcher cSearcher = getCtnListSearcher(loaderContext);
            String contextID = "";
            if (cSearcher != null) {
                searchResult = (JahiaSearchResult) cSearcher.getResult();
                if ( cSearcher.getContextID() != null && !"".equals(cSearcher.getContextID()) ){
                    contextID += "_" + cSearcher.getContextID();
                }
            }
            if (searchResult != null) {
                if (searchResult.bits() != null) {
                    resultBitSet = (BitSet) searchResult.bits().clone();
                }
            }

            // Apply container list filtering if needed.
            if ((searchResult == null) ||
                    !(searchResult != null && searchResult.getHitCount() == 0)) {
                cFilters = getCtnListFilters(loaderContext);
                if (cFilters != null) {
                    if ( cFilters.getContextID() != null && !"".equals(cFilters.getContextID()) ){
                        contextID += "_" + cFilters.getContextID();
                    }
                    if (cFilters.bits() != null) {
                        filterBitSet = (BitSet) cFilters.bits().clone();
                    }
                }
            }
            if (resultBitSet != null) {
                if (filterBitSet != null) {
                    resultBitSet.and(filterBitSet);
                    if (resultBitSet instanceof OrderedBitSet) {
                        ((OrderedBitSet) resultBitSet).setOrdered(false);
                    }
                }
            } else if (filterBitSet != null) {
                resultBitSet = filterBitSet;
            }

            if (resultBitSet != null && resultBitSet.length() == 0) {
                return Collections.emptyList();
            }

            if (resultBitSet instanceof OrderedBitSet){
                OrderedBitSet orderedBitSet = (OrderedBitSet)resultBitSet;
                if (orderedBitSet.isOrdered()){
                    List<Integer> orderedBits = orderedBitSet.getOrderedBits();
                    List<Integer>result = new ArrayList<Integer>();
                    result.addAll(orderedBits);
                    return result;
                }
            }

            // Apply Sorting if needed.
            final ContainerSorterInterface sorter = getContainerSorter(loaderContext,resultBitSet);
            if (sorter != null && sorter.result() != null) {
                return sorter.result();
            } else {
                // it's a fake sorter or sorting failed
                // so return the list of matching ctn ids, without sorting
                if (!loaderContext.getLoadingUseSingleSearchQuery().booleanValue()) {
                    cList.setIsContainersLoaded(false);
                }    
                try {
                    JahiaContainersService jcService = ServicesRegistry.getInstance()
                            .getJahiaContainersService();
                    if (resultBitSet == null) {
                        return jcService.getctnidsInList(cList.getID(), loadVersion);
                    } else {
                        return jcService.getCtnIds(
                                        resultBitSet, loadVersion);
                    }
                } catch (Exception t) {
                    logger.warn("Error retrieving container ids", t);
                }
            }

        } catch (Exception t) {
            logger.warn(t, t);
        }
        return null; // on any error return null List, so Jahia will return all
        // containers without search, filtering or sorting of any sort.
    }

    //--------------------------------------------------------------------------

    /**
     * Apply container search if needed
     *
     * @param loaderContext
     * @return
     * @throws JahiaException
     */
    private ContainerSearcher getCtnListSearcher(ContainerListLoaderContext loaderContext)
            throws JahiaException {

        ProcessingContext context = loaderContext.getContext();
        JahiaContainerList cList = loaderContext.getCList();

        int clistID = cList.getID();
        String containerListName = "truc";
        if (cList.getID() != -1) {
            containerListName = cList.getDefinition().getName();
        }

        ContainerSearcher cSearcher = cList.getQueryBean() != null ? cList.getQueryBean().getSearcher() : null;

        String key = clistID + "_"
                + context.getPageID() + "_" + containerListName +
                "_search_handler" + "_" + context.getEntryLoadRequest().toString();
        boolean fakeSearcher = false;
        if (cSearcher == null || !cSearcher.isQueryValid()){
            // create a fake searcher
            String contextID = "";
            if (cSearcher!=null){
                contextID = cSearcher.getContextID();
            }
            cSearcher = new ContainerSearcher(clistID, "", context.getEntryLoadRequest());
            cSearcher.setContextID(contextID);
            cSearcher.setUpdateStatus();
            if (cSearcher.getContextID() != null
                    && !"".equals(cSearcher.getContextID())){
                key += "_" + cSearcher.getContextID();
            }
            fakeSearcher = true;
        } else {
            ContainerSearcher cachedContainerSearcher = null;
            if (cSearcher.getContextID() != null
                    && !"".equals(cSearcher.getContextID())){
                key += "_" + cSearcher.getContextID();
            }
            cachedContainerSearcher = (ContainerSearcher) this.containerSearchersMap.get(key);

            boolean doNewSearch = true;

            ContainerSearcher customSearcher = null;
            if (!cSearcher.isSiteModeSearching()) {
                customSearcher = new ContainerSearcher(clistID,
                        cSearcher.getContainerLevel(),
                        cSearcher.getQuery(),
                        cSearcher.getEntryLoadRequest());
                customSearcher.setContextID(cSearcher.getContextID());
                customSearcher.setCacheTime(cSearcher.getCacheTime());
                customSearcher.setLastSearchTime(cSearcher.getLastSearchTime());
                customSearcher.setLanguageCodes(cSearcher.getLanguageCodes());
                customSearcher.setSearchResultBuilder(cSearcher.getSearchResultBuilder());
            } else {
                customSearcher = cSearcher;
            }
            if ( cachedContainerSearcher != null ){
                customSearcher.setLastSearchTime(cachedContainerSearcher.getLastSearchTime());
            }

            // detect pagination
            String pagination = context.getParameter("ctnlistpagination_" + containerListName);
            if ( "true".equals(pagination) ){
                // on pagination, do not perform search
                doNewSearch = false;
            } else {
                if (cachedContainerSearcher != null
                    && (cachedContainerSearcher.getQuery () != null)
                        && cachedContainerSearcher.getQuery ().equals (customSearcher.getQuery())
                    && cachedContainerSearcher.getLanguageCodes ().contains (context.getLocale ().
                    toString ())) {
                    // check if the container list has been changed the last time the search was performed
                    long lastSearchTime = cachedContainerSearcher.getLastSearchTime ();

                    if ( System.currentTimeMillis()-lastSearchTime<
                            customSearcher.getCacheTime() ){
                        ContainersChangeEventListener listener = (
                                ContainersChangeEventListener) JahiaListenersRegistry.
                                getInstance ()
                                .getListenerByClassName (ContainersChangeEventListener.class.getName ());
                        if (listener != null) {
                            long lastCtnListChangeTime = listener.getContainerLastChangeTime();
                            if (lastCtnListChangeTime <= lastSearchTime) {
                                cSearcher = cachedContainerSearcher;
                                cSearcher.resetUpdateStatus (); // to indicate no new search has been launched.
                                logger.debug ("Container Searcher found in session with same query and ctnlList did not change -> no need to run search again.");
                                doNewSearch = false;
                            }
                        }
                    }
                }
            }
            if ( !doNewSearch ){
                // test for search result validity
                JahiaSearchResult searchResult = (JahiaSearchResult)((JahiaSearcher)customSearcher).getResult();
                if ( searchResult != null && !searchResult.isValid() ){
                    doNewSearch = true;
                }
            }
            if (doNewSearch) {
                customSearcher.search(customSearcher.getQuery(), context);
                logger.debug("Container Searcher launched new search");
                cSearcher = customSearcher;
            } else if (loaderContext.getLoadingUseSingleSearchQuery().booleanValue()) {
                // as we use cached results, we need to force reloading the containers
                cList.setIsContainersLoaded(false);
            }
        }
        if (!fakeSearcher){
            this.containerSearchersMap.put(key,cSearcher);
        } else if (this.containerSearchersMap != null){
            this.containerSearchersMap.remove(key);            
        }

        return cSearcher;
    }

    //--------------------------------------------------------------------------

    /**
     * Apply container filtering if needed
     *
     * @param loaderContext
     * @return
     * @throws JahiaException
     */
    private ContainerFilters getCtnListFilters(ContainerListLoaderContext loaderContext)
            throws JahiaException {

        ProcessingContext context = loaderContext.getContext();
        JahiaContainerList cList = loaderContext.getCList();

        int clistID = cList.getID();

        String containerListName = cList.getID() != -1 ? cList.getDefinition().getName() : "truc";

        if (logger.isDebugEnabled()) {
            logger.debug("Started for container list : " + containerListName + "[" + clistID + "]");
        }

        ContainerFilters cFilters = cList.getQueryBean() != null ? cList.getQueryBean().getFilter() : null;

        String key = clistID + "_"
                    + context.getPageID() + "_" + containerListName +
                "_filter_handler" + "_" + context.getEntryLoadRequest().toString();

        boolean fakeFilter = false;
        if (cFilters == null || !cFilters.isQueryValid()) {
            String contextID = "";
            if(cFilters !=null){
                contextID = cFilters.getContextID();
            }
            // create a fake filter
            cFilters = new ContainerFilters(clistID, new ArrayList<ContainerFilterInterface>());
            cFilters.setContextID(contextID);
            cFilters.setUpdateStatus();
            fakeFilter = true;
        } else {
            if ( cFilters.getContextID() != null
                && !"".equals(cFilters.getContextID())){
                key += "_" + cFilters.getContextID();
            }
            ContainerFilters cachedContainerFilters = (ContainerFilters) this.containerFiltersMap.get(key);
            boolean doNewFiltering = true;

            // We need to create a container filter handler with the correct container list ID!
            cFilters.setCtnListID(clistID);

            if (cachedContainerFilters != null
                    && (cachedContainerFilters.getQuery() != null)
                    && cachedContainerFilters.getQuery().equals(
                            cFilters.getQuery())
                    && (cachedContainerFilters.getQueryParameters() != null)
                    && cachedContainerFilters.getQueryParameters().equals(
                            cFilters.getQueryParameters())
            ) {
                // check if containers has changed the last time the filtering was performed
                final long lastFilteringTime = cachedContainerFilters.
                        getLastFilteringTime();
                final ContainersChangeEventListener listener = (ContainersChangeEventListener) JahiaListenersRegistry.
                        getInstance().getListenerByClassName(ContainersChangeEventListener.class.getName());
                if (listener != null) {
                    final long lastCtnChangeTime = listener.getContainerLastChangeTime();
                    if (lastCtnChangeTime <= lastFilteringTime) {
                        cFilters = cachedContainerFilters;
                        cFilters.resetUpdateStatus(); // to indicate no new filtering has been launched.
                        logger.debug("Container Filters found in session with same query and ctnlList did not change -> no need to run filtering again.");
                        doNewFiltering = false;
                    }
                }
            }

            if (doNewFiltering) {
                cFilters.doFilter();
                logger.debug("Container Filters launched new filtering");
            } else if (loaderContext.getLoadingUseSingleSearchQuery().booleanValue()) {
                // as we use cached results, we need to force reloading the containers
                cList.setIsContainersLoaded(false);
            }
        }
        
        if (!fakeFilter) {
            this.containerFiltersMap.put(key, cFilters);
        } else if (this.containerFiltersMap != null) {
            this.containerFiltersMap.remove(key);
        }
        return cFilters;
    }

    //--------------------------------------------------------------------------

    /**
     * Apply container sorting if needed
     *
     * @param loaderContext
     * @param resultBitSet      the filtering and search result as BitSet
     * @return ContainerSorterBean,  the container sort handler
     */
    private ContainerSorterInterface getContainerSorter(ContainerListLoaderContext loaderContext,
                                                        final BitSet resultBitSet)
            throws JahiaException {

        ProcessingContext context = loaderContext.getContext();
        JahiaContainerList cList = loaderContext.getCList();

        ContainerSorterInterface cachedSorter = null;

        String containerListName = "truc";
        if (cList.getID() != -1) {
            containerListName = cList.getDefinition().getName();
        }
        ContainerSorterInterface sorter = cList.getQueryBean() != null ? cList.getQueryBean().getSorter() : null;
        
        String key = cList.getID() + "_"
                    + context.getPageID() + "_" + containerListName + "_sort_handler" +
                    "_" + context.getEntryLoadRequest().toString();
        if ( sorter != null ){
            if ( sorter.getContextID() != null && !"".equals(sorter.getContextID()) ){
                key += sorter.getContextID();
            }
            cachedSorter = (ContainerSorterInterface) this.containerSortersMap.get(key);
        } else if (this.containerSortersMap != null){
            this.containerSortersMap.remove(key);
        }
        boolean doNewSorting = false;
        if (sorter == null || !sorter.isValid()) {
            final String property = cList.getProperty("automatic_sort_handler");
            final boolean useOptimizedMode = ("true".equals(cList.getProperty("automatic_sort_useOptimizedMode")) 
                    || cList.getProperty("automatic_sort_useOptimizedMode")==null);
            final boolean ignoreOptimizedMode = ("true".equals(cList.getProperty("automatic_sort_ignoreOptimizedMode")));
            if (property != null && !"".equals(property.trim())) {
                final String[] paramValues = property.split(";");
                final boolean isMetadata = Boolean.valueOf(paramValues[3]).booleanValue();
                String fieldName = paramValues[0];
                List<Integer> fieldDefIDs = ServicesRegistry.getInstance().getJahiaFieldService()
                        .loadFieldDefinitionIds(fieldName,isMetadata);
                if (fieldDefIDs != null && !fieldDefIDs.isEmpty()){
                    try {
                        JahiaFieldDefinition definition = (JahiaFieldDefinition)
                                JahiaFieldDefinition.getChildInstance(String.valueOf(fieldDefIDs.get(0).intValue()));
                        fieldName = definition.getCtnType();
                    } catch ( Throwable t ){
                        logger.debug(t, t);
                        return null;
                    }
                }
                if (isMetadata) {
                    sorter = new ContainerMetadataSorterBean(cList.getID(), fieldName,
                            Boolean.valueOf(paramValues[2]).booleanValue(), context, context.getEntryLoadRequest());
                    if (ignoreOptimizedMode || !useOptimizedMode){
                        ((ContainerMetadataSorterBean)sorter).setOptimizedMode(false);
                    }
                } else {
                    sorter = new ContainerSorterBean(cList.getID(), fieldName,
                            Boolean.valueOf(paramValues[2]).booleanValue(), context.getEntryLoadRequest());
                    if (ignoreOptimizedMode || !useOptimizedMode){
                        ((ContainerSorterBean)sorter).setOptimizedMode(false);
                    }
                }
                if ("desc".equals(paramValues[1])) {
                    sorter.setDescOrdering();
                }
                doNewSorting = true;
            }
        } else {
            doNewSorting = true;
        }

        if (doNewSorting) {

            logger.debug("Found sort hanlder on field(s) [" +
                    sorter.getSortingFieldNames() + "]");

            // We need to create a container sort handler with the correct container list ID!
            sorter.setCtnListID(cList.getID());

            if (cachedSorter != null
                    && cachedSorter.getEntryLoadRequest().toString().equals(sorter.getEntryLoadRequest().toString())
                    && cachedSorter.isValid()
                    && Arrays.equals(cachedSorter.getSortingFieldNames(), sorter.getSortingFieldNames())
                    && (cachedSorter.isAscOrdering() == sorter.isAscOrdering())
                    ) {
                // check if containers has changed since the last time the sorting was performed
                final long lastSortingTime = cachedSorter.getLastSortingTime();
                final ContainersChangeEventListener listener = (ContainersChangeEventListener) JahiaListenersRegistry.
                        getInstance().getListenerByClassName(ContainersChangeEventListener.class.getName());
                if (listener != null) {
                    final long lastCtnChangeTime = listener.getContainerLastChangeTime();
                    if (lastCtnChangeTime <= lastSortingTime) {
                        sorter = cachedSorter;
                        sorter.resetUpdateStatus(); // to indicate no new sorting has been launched.
                        logger.debug("Container Sorter found in session with same sorting field and ctnlList did not change -> no need to run sorting again.");
                        doNewSorting = false;
                    }
                }
            }

            if (doNewSorting) {
                sorter.doSort(resultBitSet);
                logger.debug("Container Sorter launched new sort");
            } else if (loaderContext.getLoadingUseSingleSearchQuery().booleanValue()) {
                // as we use cached results, we need to force reloading the containers
                cList.setIsContainersLoaded(false);
            }

        }
        if (sorter != null){
            this.containerSortersMap.put(key,sorter);
        } else if (this.containerSortersMap != null){
            this.containerSortersMap.remove(key);
        }
        return sorter;
    }

    /**
     * 
     * @param loaderContext
     * @return
     * @throws JahiaException
     */
    private boolean isLoadingUseSingleSearchQuery(ContainerListLoaderContext loaderContext) throws JahiaException {

        JahiaContainerList cList = loaderContext.getCList();

        ContainerSearcher cSearcher = null;
        ContainerFilters cFilters = null;
        ContainerSorterInterface sorter = null;
        final BitSet facetedFilter = cList.getQueryBean() != null
                && cList.getQueryBean().getQueryContext().getFacetFilterQueryParamName() != null ? ServicesRegistry
                .getInstance().getJahiaFacetingService().applyFacetFilters(
                        null, loaderContext.getContext().getParameter(
                                cList.getQueryBean().getQueryContext().getFacetFilterQueryParamName()),
                        cList.getQueryBean().getQueryContext(), loaderContext.getContext()) : null;
                        
        if ( cList.getQueryBean() != null ){
            cList.getQueryBean().getQueryContext().setFacetedFilterResult(facetedFilter);
            cSearcher = cList.getQueryBean().getSearcher();
            cFilters = cList.getQueryBean().getFilter();
            sorter = cList.getQueryBean().getSorter();
        }
        if (cSearcher != null && cFilters == null && sorter == null){
            return true;
        } else if(cFilters != null && sorter == null && cSearcher == null){
            if (cFilters.getContainerFilters().size()==1){
                ContainerFilterInterface filter = (ContainerFilterInterface)cFilters.getContainerFilters().get(0);
                if (filter instanceof ContainerSearcherToFilterAdapter){
                    ContainerSearcherToFilterAdapter containerSearcher = (ContainerSearcherToFilterAdapter)filter;
                    final JahiaContainerList containerList = cList;
                    final ContainerListLoaderContext finalLoaderContext = loaderContext;
                    final int maxHits = getEffectiveMaxHits(containerSearcher.getSearcher()
                            .getSearchResultBuilder().getMaxHits(),containerList.getMaxSize());
                    containerSearcher.getSearcher()
                            .getSearchResultBuilder().setSearchResult(
                                new SearchResultImpl(false){
                                    public boolean add(SearchHit hit){
                                        return processHit(finalLoaderContext, containerList, hit, maxHits, this, facetedFilter);
                                    }
                                }
                    );
                    // deactivate any proxy
                    containerList.setIsContainersLoaded(true);
                    containerList.clearContainers();
                    // set the collector to null so that this is the passed SearchResult will be used as Hit Collector
                    containerSearcher.getSearcher().getSearchResultBuilder().setHitCollector(null);
                    return true;
                } else if (filter instanceof ContainerChainedFilter){
                    ContainerChainedFilter chainedFilter = (ContainerChainedFilter)filter;
                    if (chainedFilter.getChain().length==1){
                        filter = chainedFilter.getChain()[0];
                        if (filter instanceof ContainerSearcherToFilterAdapter){
                            ContainerSearcherToFilterAdapter containerSearcher = (ContainerSearcherToFilterAdapter)filter;
                            final JahiaContainerList containerList = cList;
                            final ContainerListLoaderContext finalLoaderContext = loaderContext;
                            final int maxHits = getEffectiveMaxHits(containerSearcher.getSearcher()
                                    .getSearchResultBuilder().getMaxHits(),containerList.getMaxSize());
                            containerSearcher.getSearcher()
                                    .getSearchResultBuilder().setSearchResult(
                                        new SearchResultImpl(false){
                                            public boolean add(SearchHit hit){
                                                return processHit(finalLoaderContext, containerList, hit, maxHits, this, facetedFilter);
                                            }
                                        }
                            );
                            // deactivate any proxy
                            containerList.setIsContainersLoaded(true);
                            containerList.clearContainers();
                            // set the collector to null so that this is the passed SearchResult will be used as Hit Collector
                            containerSearcher.getSearcher().getSearchResultBuilder().setHitCollector(null);
                            return true;
                        }
                    }
                }
            }
        } else if (sorter != null && cSearcher == null && cFilters == null){
            if (sorter instanceof ContainerLuceneSorterBean){
                ContainerLuceneSorterBean luceneSorterBean = (ContainerLuceneSorterBean)sorter;
                //if (luceneSorterBean.getContainerSearcher().getSearchResultBuilder().getSorter()!= null){
                    final JahiaContainerList containerList = cList;
                    final ContainerListLoaderContext finalLoaderContext = loaderContext;
                    final int maxHits = getEffectiveMaxHits(luceneSorterBean.getContainerSearcher()
                        .getSearchResultBuilder().getMaxHits(),containerList.getMaxSize());
                    luceneSorterBean.getContainerSearcher()
                            .getSearchResultBuilder().setSearchResult(
                                new SearchResultImpl(false){
                                    public boolean add(SearchHit hit){
                                        return processHit(finalLoaderContext, containerList, hit, maxHits, this, facetedFilter);
                                    }
                                }
                    );
                    // deactivate any proxy
                    containerList.setIsContainersLoaded(true);
                    containerList.clearContainers();
                    // set the collector to null so that this is the passed SearchResult will be used as Hit Collector
                    luceneSorterBean.getContainerSearcher().getSearchResultBuilder().setHitCollector(null);
                    return true;
                //}
            }
        }
        return false;
    }

    private static boolean processHit(ContainerListLoaderContext loaderContext, JahiaContainerList containerList,
            SearchHit hit, int maxHits, SearchResult searchResult, BitSet facetedFilter) {
        boolean result = true;

        int nbContainers = searchResult.results().size();
        // init the container list pagination
        JahiaContainerListPagination cListPagination = null;
        try {
            Integer lastEditedItemIntId = (Integer) loaderContext.getContext().getAttribute(
                    "ContextualContainerList_" + String.valueOf(containerList.getID()));
            int lastEditedItemId = 0;
            if (lastEditedItemIntId != null) {
                lastEditedItemId = lastEditedItemIntId.intValue();
            }
            List<Integer> ctnIds = new ArrayList<Integer>();
            if (lastEditedItemId > 0) {
                List<JahiaContainer> ctns = containerList.getContainersList();
                if (ctns != null && !ctns.isEmpty()) {
                    for (JahiaContainer ctn : ctns) {
                        ctnIds.add(new Integer(ctn.getID()));
                    }
                }
            }

            // init the container list pagination
            cListPagination = containerList.getCtnListPagination(false);
            if (cListPagination != null) {
                cListPagination = new JahiaContainerListPagination(containerList, loaderContext.getContext(),
                        cListPagination.getWindowSize(), ctnIds, lastEditedItemId, loaderContext.getListViewId());
            } else {
                cListPagination = new JahiaContainerListPagination(containerList, loaderContext.getContext(), -1,
                        ctnIds, lastEditedItemId, loaderContext.getListViewId());
            }
            containerList.setCtnListPagination(cListPagination);
        } catch (Exception t) {
            logger.debug("Error checking container list pagination", t);
            return false;
        }
        int endPos = cListPagination.getSize();

        if (cListPagination.isValid()) {
            endPos = cListPagination.getLastItemIndex();
        }

        if ((nbContainers >= containerList.getMaxSize())
                || (nbContainers > endPos
                        + org.jahia.settings.SettingsBean.getInstance().getPreloadedItemsForPagination())) {
            return false;
        }

        if (nbContainers == maxHits || (nbContainers > 0 && nbContainers == containerList.getMaxSize())) {
            return false;
        }
        try {
            String ctnKeyVal = hit.getValue(JahiaSearchConstant.OBJECT_KEY);
            String parentID = hit.getValue(JahiaSearchConstant.PARENT_ID);
            String aclID = hit.getValue(JahiaSearchConstant.ACL_ID);
            ObjectKey objectKey = null;
            if (ctnKeyVal != null) {
                objectKey = ObjectKey.getInstance(ctnKeyVal);
                if (objectKey instanceof ContentFieldKey) {
                    objectKey = new ContentContainerKey(Integer.parseInt(parentID));
                }
                if (facetedFilter == null || facetedFilter.get(objectKey.getIdInType())) {
                    JahiaContainer jahiaContainer = loadContainer(objectKey.getIdInType(), Integer.parseInt(aclID),
                            loaderContext);
                    if (jahiaContainer != null && jahiaContainer.getID() > 0) {
                        containerList.addContainer(jahiaContainer);
                        containerList.setFullSize(containerList.getFullSize() + 1);
                        searchResult.results().add(hit);
                        nbContainers++;
                        endPos = cListPagination.getSize();

                        if (cListPagination.isValid()) {
                            endPos = cListPagination.getLastItemIndex();
                        }
                        if ((nbContainers >= containerList.getMaxSize())
                                || (nbContainers > endPos
                                        + org.jahia.settings.SettingsBean.getInstance()
                                                .getPreloadedItemsForPagination())) {
                            return false;
                        }
                        if (nbContainers == maxHits || (nbContainers > 0 && nbContainers == containerList.getMaxSize())) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception t) {
            logger.debug(t, t);
        }
        return result;
    }

    /**
     *
     * @param filterDefinedMaxHits
     * @param containerListMaxSize
     * @return
     */
    private int getEffectiveMaxHits(int filterDefinedMaxHits, int containerListMaxSize) {
        int maxHits = containerListMaxSize;
        if (maxHits<0){
            return filterDefinedMaxHits;
        } else if (filterDefinedMaxHits>0 && filterDefinedMaxHits<maxHits){
            maxHits = filterDefinedMaxHits;
        }
        return maxHits;
    }

    /**
     * Load a JahiaContainer applying all check
     * @param ctnID
     * @param aclID
     * @return
     * @throws JahiaException
     */
    private static JahiaContainer loadContainer(int ctnID,
                                         int aclID,
                                         ContainerListLoaderContext loaderContext)
    throws JahiaException {

        ProcessingContext context = loaderContext.getContext();
        EntryLoadRequest loadVersion = loaderContext.getLoadVersion();

        final JahiaContainersService jahiaContainersService = ServicesRegistry.getInstance().getJahiaContainersService();
        final TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance().getTimeBasedPublishingService();
        JahiaContainer container = null;

        // start check for correct rights.
        if (context != null) { // no jParams, can't check for rights

            try {
                // Check for expired container
                boolean disableTimeBasedPublishingFilter = context.isFilterDisabled(CoreFilterNames.
                        TIME_BASED_PUBLISHING_FILTER);

                JahiaAcl acl = null;
                if (aclID == -1){
                    return null;
                }
                acl = ServicesRegistry.getInstance().getJahiaACLManagerService().lookupACL(aclID);
                if (acl!= null && acl.getPermission(context.getUser(), JahiaBaseACL.READ_RIGHTS)) {
                    if ( !disableTimeBasedPublishingFilter ){
                        if ( ParamBean.NORMAL.equals(context.getOperationMode()) ){
                            if (!tbpServ.isValid(new ContentContainerKey(ctnID),
                                    context.getUser(),context.getEntryLoadRequest(),context.getOperationMode(),
                                    (Date)null)){
                                return null;
                            }
                        } else if ( ParamBean.PREVIEW.equals(context.getOperationMode()) ){
                            if (!tbpServ.isValid(new ContentContainerKey(ctnID),
                                    context.getUser(),context.getEntryLoadRequest(),context.getOperationMode(),
                                    AdvPreviewSettings.getThreadLocaleInstance())){
                                return null;
                            }
                        }
                    }
                    try {
                        container = jahiaContainersService.loadContainer(ctnID,
                                loaderContext.getLoadFlag(), context,
                                loadVersion, loaderContext.getCachedFieldsInContainer(),
                                loaderContext.getCachedContainerListsFromContainers(),
                                loaderContext.getCachedContainerListsFromContainers());
                    } catch (Exception t) {
                        String errorMsg = "Error loading container [" + ctnID + "]";
                        if (loadVersion != null) {
                            errorMsg += " loadVersion=" + loadVersion.toString();
                        }
                        logger.debug(errorMsg, t);

                    }
                }
            } catch (Exception t) {
                logger.error(t, t);
            }
        }
        return container;
    }

}
