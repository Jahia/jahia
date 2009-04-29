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
package org.jahia.data.containers;

import org.jahia.bin.Jahia;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.search.ContainerSearchResultBuilderImpl;
import org.jahia.services.search.ContainerSearcher;
import org.jahia.services.version.EntryLoadRequest;

import java.util.BitSet;
import java.util.Map;

/**
 * This class is used to adapt a ContainerSearcher instance as a ContainerFilterInterface
 *
 * User: hollis
 * Date: 9 nov. 2007
 * Time: 09:28:23
 * To change this template use File | Settings | File Templates.
 */
public class ContainerSearcherToFilterAdapter implements ContainerFilterInterface, MergeableFilter {

    private ContainerSearcher searcher;

    public ContainerSearcherToFilterAdapter(ContainerSearcher searcher) {
        this.searcher = searcher;
        ContainerSearchResultBuilderImpl searchResultBuilder = (ContainerSearchResultBuilderImpl)
                this.searcher.getSearchResultBuilder();
        searchResultBuilder.setCheckParentPageIntegrity(false);
    }

    public BitSet doFilter(int ctnListID)
    throws JahiaException {
        searcher.setCtnListID(ctnListID);
        JahiaSearchResult searchResult = searcher.search (searcher.getQuery(), Jahia.getThreadParamBean());
        return searchResult.bits();
    }

    public String getSelect(int ctnListID, int filterId, Map<String, Object> parameters) {
        EntryLoadRequest loadRequest = searcher.getLoadRequest();
        return searcher.getQuery() + getCacheTimeQueryPostFix() + (loadRequest != null ? loadRequest.toString():"");
    }

    public void setContainerFilters(ContainerFilters containerFilters) {
        // do nothing
    }

    private String getCacheTimeQueryPostFix(){
        String postFix = String.valueOf(searcher.getCacheTime());
        if (System.currentTimeMillis()-searcher.getLastSearchTime()<
                            searcher.getCacheTime()){
            // force cache invalidation
            postFix = String.valueOf(System.currentTimeMillis());
        }
        return postFix;
    }

    public BitSet doFilterBySite(int siteId, String containerDefinitionName, int listId)
    throws JahiaException {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return doFilterBySite(siteIds,containerDefinitionNames,listId);
    }

    public BitSet doFilterBySite(Integer[] siteIds, String[] containerDefinitionNames, int listId)
    throws JahiaException {
        searcher.setSiteIds(siteIds);
        searcher.setContainerDefinitionNames(containerDefinitionNames);
        searcher.setCtnListID(listId);
        searcher.setSiteModeSearching(true);
        JahiaSearchResult searchResult = searcher.search(searcher.getQuery(), Jahia.getThreadParamBean());
        return searchResult.bits();
    }

    public String getSelectBySiteID(int siteId,
            String containerDefinitionName, int filterId, Map<String, Object> parameters) {
        Integer[] siteIds = null;
        if (siteId != -1){
            siteIds = new Integer[]{new Integer(siteId)};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        return getSelectBySiteID(siteIds,containerDefinitionNames, filterId, parameters);
    }

    public String getSelectBySiteID(Integer[] siteIds,
            String[] containerDefinitionNames, int filterId, Map<String, Object> parameters) {
        searcher.setSiteIds(siteIds);
        searcher.setContainerDefinitionNames(containerDefinitionNames);
        EntryLoadRequest loadRequest = searcher.getLoadRequest();
        return searcher.getQuery() + getCacheTimeQueryPostFix() + (loadRequest != null ? loadRequest.toString():"");
    }

    public ContainerSearcher getSearcher() {
        return searcher;
    }

    public void setSearcher(ContainerSearcher searcher) {
        this.searcher = searcher;
    }

    /**
     * A filter may be capable of merging with another filter instance for optimization.
     *
     * @param filter
     * @return true if the merging is performed, false if no merging was performed.
     *
     */
    public boolean mergeAnd(ContainerFilterInterface filter) {
        if (!(filter instanceof ContainerSearcherToFilterAdapter)){
            return false;
        }
        ContainerSearcherToFilterAdapter filterBean = (ContainerSearcherToFilterAdapter)filter;
        StringBuffer queryBuffer = new StringBuffer();
        queryBuffer.append(this.getSearcher().getQuery()).append(" AND ");
        queryBuffer.append(filterBean.getSearcher().getQuery());
        this.searcher.setQuery(queryBuffer.toString());
        return true;
    }

    /**
     * A filter may be capable of merging with another filter instance for optimization.
     *
     * @param filter
     * @return true if the merging is performed, false if no merging was performed.
     *
     */
    public boolean mergeOr(ContainerFilterInterface filter) {
        if (!(filter instanceof ContainerSearcherToFilterAdapter)){
            return false;
        }
        ContainerSearcherToFilterAdapter filterBean = (ContainerSearcherToFilterAdapter)filter;
        StringBuffer queryBuffer = new StringBuffer();
        queryBuffer.append("(").append(this.getSearcher().getQuery()).append(") ").append(" OR ");
        queryBuffer.append("(").append(filterBean.getSearcher().getQuery()).append(")");
        this.searcher.setQuery(queryBuffer.toString());
        return true;
    }

}
