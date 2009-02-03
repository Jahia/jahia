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

//
//
//

package org.jahia.services.search;

import org.jahia.data.search.JahiaSearchResult;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;

import java.util.*;


/**
 * Handle containers search.
 * The result for this searcher is an instance of JahiaSearchResult
 * with a List of matching container ids.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */

public class ContainerSearcher extends JahiaSearcher {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ContainerSearcher.class);

    private int ctnListID = 0;

    private Integer[] siteIds = new Integer[]{};

    private String[] containerDefinitionNames;

    private boolean siteModeSearching = false;

    private long lastSearchTime = -1;

    private boolean updated = false;

    private int containerLevel = 0;

    private EntryLoadRequest loadRequest = EntryLoadRequest.CURRENT;

    private List<String> languageCodes = new ArrayList<String> ();

    private JahiaSearchResultBuilder searchResultBuilder = new ContainerSearchResultBuilderImpl();

    private String contextID = "";

    private long cacheTime = 30000;
    
    private boolean cacheQueryResultsInBackend = false;

    /**
     * Constructor for one single container list search
     * The expected result is a JahiaSearchResult object containing a List of matching containers.
     * The List contained in the JahiaSearchResult is a List of matching ctn ids, not the containers.
     * No right check on container, only on field.
     *
     * @param ctnListID the container list id.
     * @param query       a valid Lucene search query
     * @param loadRequest
     */
    public ContainerSearcher (int ctnListID, String query, EntryLoadRequest loadRequest) {
        this(ctnListID, 0, query, loadRequest);
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
    public ContainerSearcher (int ctnListID, int containerLevel, String query,
                              EntryLoadRequest loadRequest) {
        this.setCtnListID(ctnListID);
        this.setContainerLevel(containerLevel);
        if (loadRequest != null) {
            this.setLoadRequest(loadRequest);
        }
        this.setQuery(query);
    }

    /**
     * Constructor for a single container list
     *
     * @param containerListName
     * @param params
     * @param query
     * @param loadRequest
     *
     * @throws JahiaException
     */
    public ContainerSearcher (String containerListName, ProcessingContext params, String query,
                              EntryLoadRequest loadRequest)
            throws JahiaException {
        if (containerListName != null) {
            int clistID = ServicesRegistry.getInstance ().getJahiaContainersService ().
                    getContainerListID (containerListName, params.getPage ().getID ());
            if (clistID != -1) {
                this.setCtnListID(clistID);
            }
        }
        this.setQuery(query);
        if (loadRequest != null) {
            this.setLoadRequest(loadRequest);
        }
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
    public ContainerSearcher (int siteId, String containerDefinitionName,
                              String query, EntryLoadRequest loadRequest) {
        int[] siteIds = null;
        if (siteId != -1){
            siteIds = new int[]{siteId};
        }
        String[] containerDefinitionNames = null;
        if (containerDefinitionName != null && !"".equals(containerDefinitionName.trim())){
            containerDefinitionNames = new String[]{containerDefinitionName};
        }
        if (siteIds != null && siteIds.length>0){
            this.setSiteIds(new Integer[siteIds.length]);
            for (int i=0; i<siteIds.length; i++){
                this.getSiteIds()[i]=new Integer(siteIds[i]);
            }
        }
        this.setContainerDefinitionNames(containerDefinitionNames);
        this.setSiteModeSearching(true);

        if (loadRequest != null) {
            this.setLoadRequest(loadRequest);
        }
        this.setQuery(query);
    }

    /**
     * Constructor for searching containers of one Site or all Sites,
     * and of one definition or of any definition
     * The expected result is a JahiaSearchResult object containing a List of matching containers.
     * The List contained in the JahiaSearchResult is a List of matching ctn ids, not the containers.
     * No right check on container, only on field.
     *
     * @param siteIds all sites are allowed if null or empty
     * @param containerDefinitionNames all definitions are allowed if null or empty
     * @param query
     * @param loadRequest
     */
    public ContainerSearcher (int[] siteIds, String[] containerDefinitionNames,
                              String query, EntryLoadRequest loadRequest) {

        if (siteIds != null && siteIds.length>0){
            this.setSiteIds(new Integer[siteIds.length]);
            for (int i=0; i<siteIds.length; i++){
                this.getSiteIds()[i]=new Integer(siteIds[i]);
            }
        }
        this.setContainerDefinitionNames(containerDefinitionNames);
        this.setSiteModeSearching(true);

        if (loadRequest != null) {
            this.setLoadRequest(loadRequest);
        }
        this.setQuery(query);
    }

    
    public ContainerSearcher (Integer[] siteIds, String containerDefinitionName,
                              String query, EntryLoadRequest loadRequest) {
        if ( siteIds != null ){
            this.setSiteIds(siteIds);
        }
        this.setContainerDefinitionName(containerDefinitionName);
        this.setSiteModeSearching(true);

        if (loadRequest != null) {
            this.setLoadRequest(loadRequest);
        }
        this.setQuery(query);
    }

    public ContainerSearcher (Integer[] siteIds, String[] containerDefinitionNames,
                              String query, EntryLoadRequest loadRequest) {
        if ( siteIds != null ){
            this.setSiteIds(siteIds);
        }
        this.setContainerDefinitionNames(containerDefinitionNames);
        this.setSiteModeSearching(true);

        if (loadRequest != null) {
            this.setLoadRequest(loadRequest);
        }
        this.setQuery(query);
    }

    public JahiaSearchResultBuilder getSearchResultBuilder() {
        return searchResultBuilder;
    }

    public void setSearchResultBuilder(JahiaSearchResultBuilder searchResultBuilder) {
        this.searchResultBuilder = searchResultBuilder;
    }

    public Integer[] getSiteIds() {
        return siteIds;
    }

    public void setSiteIds(Integer[] siteIds) {
        this.siteIds = siteIds;
    }

    /**
     * Return true if the search is done on an entire site ( or all sites )
     * false, if the search is done on one container list ( using ctnListId )
     *
     * @return
     */
    public boolean isSiteModeSearching () {
        return this.siteModeSearching;
    }

    /**
     * Return the container list id.
     *
     * @return int ctnListID, the container list id.
     */
    public int getCtnListID () {
        return this.ctnListID;
    }

    /**
     * Return the site Id.
     * @deprecated use getSiteIds()
     */
    public int getSiteId () {
        if (this.siteIds==null||this.siteIds.length==0){
            return -1;
        }
        return this.siteIds[0].intValue();
    }

    /**
     * Return the container definition name.
     * @deprecated, use getContainerDefinitionNames()
     */
    public String getContainerDefinitionName () {
        if (this.containerDefinitionNames == null || this.containerDefinitionNames.length==0){
            return null;
        }
        return this.containerDefinitionNames[0];
    }

    /**
     * Return the container definition name.
     */
    public String[] getContainerDefinitionNames() {
        return this.containerDefinitionNames;
    }

    /**
     * Return the last search running time.
     *
     * @return int ctnListID, the container list id. -1 if never performed yet
     */
    public long getLastSearchTime () {
        return this.lastSearchTime;
    }

    /**
     * Return the update status. Each time the doFilter method is called, this update status is set to true.
     *
     * @return boolean, the internal updated status value.
     */
    public boolean getUpdateStatus () {
        return this.updated;
    }

    /**
     * Set the update status to true.
     */
    public void setUpdateStatus () {
        this.updated = true;
    }

    /**
     * You can reset the internal update status by setting it to false
     */
    public void resetUpdateStatus () {
        this.updated = false;
    }

    /**
     * @return container list level.
     */
    public int getContainerLevel () {
        return this.containerLevel;
    }

    /**
     * @return the list of languages code to search.
     */
    public List<String> getLanguageCodes () {
        if ( this.languageCodes == null || this.languageCodes.isEmpty() ){
            List<Locale> locales = loadRequest.getLocales ();
            List<String> result = new ArrayList<String> ();
            for (Locale locale : locales) {
                result.add (locale.toString ());
            }
            this.languageCodes = result;
        }
        return this.languageCodes;
    }

    public EntryLoadRequest getEntryLoadRequest () {
        return this.loadRequest;
    }

    /**
     * Return a List of matching containers.
     *
     * @param query
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public JahiaSearchResult search (String query, ProcessingContext jParams)
    throws JahiaException {
        JahiaSearchResult result = new JahiaSearchResult (this.getSearchResultBuilder());
        // Must set the query first.
        setQuery (query);

        if (getQuery () == null || "".equals(getQuery().trim()) || "()".equals(getQuery().trim()))
            return result;
        
        List<String> queryList = new ArrayList<String>();
        // if query results must be cached, then first entry in list is empty, because the next entries will be treated as cached filters 
        if (isCacheQueryResultsInBackend()) {
            queryList.add("");
        }
        queryList.add(query);
        
        StringBuffer buff = new StringBuffer (1024);
        try {
            ServicesRegistry sReg = ServicesRegistry.getInstance();
            buff.append(SearchTools
                    .getWorkflowAndLanguageCodeSearchQuery(this.getLanguageCodes(),
                    this.loadRequest));

            buff.append(" AND ").append(JahiaSearchConstant.CONTENT_TYPE)
                    .append(":(").append(JahiaSearchConstant.CONTAINER_TYPE)
                    .append(" ").append(JahiaSearchConstant.FIELD_TYPE).append(
                            ")");

            if (!isSiteModeSearching ()) {
                buff.append(" AND ").append(JahiaSearchConstant.PARENT_ID)
                        .append(":").append(NumberPadding.pad(getCtnListID()))
                        .append(" ");
            } else {
                if (siteIds != null && siteIds.length>0) {
                    buff.append(" AND ").append(JahiaSearchConstant.JAHIA_ID)
                            .append(":(");
                    for(int i=0; i<siteIds.length; i++){
                        buff.append(NumberPadding.pad(siteIds[i].intValue()));
                        if (i+1 < siteIds.length) {
                          buff.append(" ");
                        }  
                    }
                    buff.append(")");
                }

                if (containerDefinitionNames != null
                        && containerDefinitionNames.length>0) {
                    buff.append(" AND ").append(
                            JahiaSearchConstant.DEFINITION_NAME).append(":(");                    
                    for(int i=0; i<containerDefinitionNames.length; i++){
                        buff.append (containerDefinitionNames[i]);
                        if (i + 1 < containerDefinitionNames.length) {
                            buff.append(" ");
                        }
                    }
                    buff.append(")");
                }
            }
            String filterQuery = buff.toString();
            queryList.add(filterQuery);
            if (logger.isDebugEnabled()) {
                logger.debug("Query is : " + query + " AND " + filterQuery);
            }

            List<String> searchAr = new ArrayList<String>();
            SearchHandler searchHandler = null;
            if ( this.isSiteModeSearching() && this.getSiteIds() != null && this.getSiteIds().length>0 ) {
                List<Integer> siteIdsList = Arrays.asList(this.getSiteIds());
                Iterator<JahiaSite> sites = sReg.getJahiaSitesService().getSites();
                while ( sites.hasNext() ){
                    JahiaSite site = sites.next();
                    if ( siteIdsList.contains(new Integer(site.getID())) ){
                        searchHandler = sReg.getJahiaSearchService().getSearchHandler(site.getID());
                        if ( searchHandler != null ){
                            searchAr.add(searchHandler.getName());
                        }
                    }
                }
            } else if (this.isSiteModeSearching() ){
                Integer[] siteIds = ServicesRegistry.getInstance().getJahiaSitesService().getSiteIds();
                if ( siteIds != null ){
                    for (int i=0; i<siteIds.length; i++){
                        searchHandler = sReg.getJahiaSearchService().getSearchHandler(siteIds[i].intValue());
                        if ( searchHandler != null ){
                            searchAr.add(searchHandler.getName());
                        }
                    }
                }
            } else {
                searchHandler = sReg.getJahiaSearchService().getSearchHandler(jParams.getJahiaID());
                if ( searchHandler != null ){
                    searchAr.add(searchHandler.getName());
                }
            }
            String[] searchHandlers = new String[searchAr.size()];
            searchAr.toArray(searchHandlers);

            SearchResult searchResult = null;
            String[] queries = (String[]) queryList.toArray(new String[queryList.size()]);
            if (this.getSearchResultBuilder().getSorter() != null
                    || this.getSearchResultBuilder().getHitCollector() == null) {
                searchResult = sReg.getJahiaSearchService().search(queries, searchHandlers,
                        this.getLanguageCodes(), jParams,
                        this.getSearchResultBuilder(),
                        this.getSearchResultBuilder().getSorter());
            } else {
                searchResult = sReg.getJahiaSearchService().search(queries, searchHandlers,
                        this.getLanguageCodes(), jParams,
                        this.getSearchResultBuilder().getHitCollector());
            }

            List<ParsedObject> parsedObjects = SearchTools.getParsedObjects(searchResult);
            if ( parsedObjects == null ){
                parsedObjects = new LinkedList<ParsedObject>();
            }
            result = this.searchResultBuilder.buildResult(parsedObjects, jParams, queries);
            if (logger.isDebugEnabled()) {
                logger.debug("jahia result : " + result.getHitCount());
            }
        } catch (Exception t) {
            logger.error (t);
        } finally {
            buff = null;
        }

        // Store the result.
        setResult (result);

        // Set search time
        TimeZone tz = TimeZone.getTimeZone ("UTC");
        Calendar cal = Calendar.getInstance (tz);
        Date nowDate = cal.getTime ();
        this.lastSearchTime = nowDate.getTime ();

        this.updated = true;

        return result;
    }


    public String getContextID(){
        return this.contextID;
    }

    public void setContextID(String contextID){
        this.contextID = contextID;
    }

    /**
     * This is the allowed cache time. default is 15 sec.
     * 
     * @return
     */
    public long getCacheTime() {
        return cacheTime;
    }

    public void setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
    }

    /**
     * May be overwrited by sub classes
     *
     * @param query
     * @param searchHandlers
     * @param jParams
     * @return
     * @throws JahiaException
     */
    protected SearchResult doSearch(String query, String[] searchHandlers, ProcessingContext jParams)
            throws JahiaException {
        ServicesRegistry sReg = ServicesRegistry.getInstance();
        return sReg.getJahiaSearchService()
                   .search(new String[]{query}, searchHandlers, jParams);
    }

    public EntryLoadRequest getLoadRequest() {
        return loadRequest;
    }

    public void setLoadRequest(EntryLoadRequest loadRequest) {
        this.loadRequest = loadRequest;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public void setCtnListID(int ctnListID) {
        this.ctnListID = ctnListID;
    }

    /**
     * @deprecated use setSiteIds()
     * @param siteId
     */
    public void setSiteId(int siteId) {
        if (siteId == -1){
            this.siteIds = null;
        } else {
            this.siteIds = new Integer[]{new Integer(siteId)};
        }
    }

    /**
     * @deprecated use setContainerDefinitionNames()
     * @param containerDefinitionName
     */
    public void setContainerDefinitionName(String containerDefinitionName) {
        this.containerDefinitionNames = null;
        if (containerDefinitionName != null && containerDefinitionName.trim().length() > 0){
            this.containerDefinitionNames = new String[]{containerDefinitionName};
        }
    }

    public void setContainerDefinitionNames(String[] containerDefinitionNames) {
        this.containerDefinitionNames = containerDefinitionNames;
    }

    public void setSiteModeSearching(boolean siteModeSearching) {
        this.siteModeSearching = siteModeSearching;
    }

    public void setLastSearchTime(long lastSearchTime) {
        this.lastSearchTime = lastSearchTime;
    }

    public void setContainerLevel(int containerLevel) {
        this.containerLevel = containerLevel;
    }

    public void setLanguageCodes(List<String> languageCodes) {
        this.languageCodes = languageCodes;
    }

    public boolean isQueryValid () {
        return (this.getQuery() != null && this.getQuery().trim ().length() > 0);
    }

    public boolean isCacheQueryResultsInBackend() {
        return cacheQueryResultsInBackend;
    }

    public void setCacheQueryResultsInBackend(boolean cacheQueryResultsInBackend) {
        this.cacheQueryResultsInBackend = cacheQueryResultsInBackend;
    }

}
