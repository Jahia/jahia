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
//  JahiaSearchService
//  NK		25.01.2002 Integrate Lucene to implement search on container list.
//

package org.jahia.services.search;


import org.apache.lucene.search.Sort;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.engines.search.SearchViewHandler;
import org.jahia.engines.search.Hit;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaIndexingJobManager;
import org.jahia.hibernate.manager.JahiaSavedSearchManager;
import org.jahia.hibernate.manager.JahiaSavedSearchViewManager;
import org.jahia.hibernate.manager.JahiaServerPropertiesManager;
import org.jahia.hibernate.model.indexingjob.JahiaIndexingJob;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.pipelines.Pipeline;
import org.jahia.services.JahiaService;
import org.jahia.services.search.compass.CompassResourceConverter;
import org.jahia.services.search.compass.JahiaCompassHighlighter;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.services.search.savedsearch.JahiaSavedSearchView;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.jahia.services.usermanager.JahiaUser;
import org.compass.core.Compass;
import org.compass.core.Resource;
import org.compass.core.engine.SearchEngineHighlighter;

import java.util.*;

import com.sun.syndication.feed.synd.SyndEntry;


/**
 * Search Service.
 *
 * @author DJ Original implementation
 * @author NK Extended for lucene integration.
 */
public abstract class JahiaSearchService extends JahiaService {
    
    public static final Integer DEFAULT_SCORE_BOOST_MAP = new Integer(0);

    public abstract SearchManager getSearchManager();

    public abstract void setSearchManager(SearchManager searchManager);

    public abstract void setSavedSearchManager(JahiaSavedSearchManager savedSearchManager);
    
    public abstract void setSavedSearchViewManager(JahiaSavedSearchViewManager savedSearchViewManager);    

    public abstract SearchHandler createSearchHandler(int siteId);

    public abstract SearchHandler getSearchHandler(int siteId);

    public abstract JahiaIndexingJobManager getIndJobMgr();

    public abstract void setIndJobMgr(JahiaIndexingJobManager indJobMgr);

    public abstract JahiaServerPropertiesManager getServerPropMgr();

    public abstract void setServerPropMgr(JahiaServerPropertiesManager serverPropMgr);

    public abstract Compass getCompass();

    public abstract void setCompass(Compass compass);

    public abstract CompassResourceConverter getCompassResourceConverter();

    public abstract void setCompassResourceConverter(CompassResourceConverter compassResourceConverter);

    public abstract Properties getConfig();
    
    public abstract void setConfig(Properties config);
    
    public abstract Properties getIndexationConfig();
    
    public abstract void setIndexationConfig(Properties indexationConfig);
    
    public abstract Properties getFilterCacheConfig();
    
    public abstract void setFilterCacheConfig(Properties filterCacheConfig);    

    public abstract boolean isDisabled();
    
    public abstract boolean isLocalIndexing();    
    
    public abstract String getServerId();    

    public abstract Pipeline getSearchIndexProcessPipeline();

    public abstract void setSearchIndexProcessPipeline(Pipeline searchIndexProcessPipeline);

    /**
     * Synchronized indexation. Be care about bad performance
     *
     * @param job
     */
    public abstract void synchronizedIndexation(JahiaIndexingJob job);

    public abstract void recordIndexingJobAsDoneForServer( JahiaIndexingJob job );

    /**
     * Add a shutdownable instance that will be notified when this service will shutdown
     *
     * @param name
     * @param s
     */
    public abstract void addShutdownable(String name,Shutdownable s);

    /**
     * Remove a shutdownable instance
     * 
     * @param name
     */
    public abstract void removeShutdownable(String name);

    /**
     * Return the last indexingJob time
     *
     * @return
     */
    public abstract long getLastIndexingJobTime();

    /**
     * Set the last indexingJob time
     *
     * @param lastIndexingJobTime
     */
    public abstract void setLastIndexingJobTime(long lastIndexingJobTime);

    /**
     * Save an JahiaIndexingJob i queue
     *
     * @param job
     * @param ctx
     */
    public abstract void addIndexingJob(JahiaIndexingJob job, RuleEvaluationContext ctx);

    /**
     * Returns tht list of Indexing Job
     * @return
     */
    public abstract List<JahiaIndexingJob> getIndexingJobs();

    /**
     * Delete an indexing job
     * @param id
     */
    public abstract void deleteIndexingJob(String id);

    /**
     * Remove any document matching a field key from search index
     *
     * @param siteId
     * @param keyFieldName name of key field for which to remove all documents from index
     * @param keyFieldValue the key field value
     * @param user
     * @param ctx
     */
    public abstract void removeFromSearchEngine ( int siteId,
                                                  String keyFieldName,
                                                  String keyFieldValue,
                                                  JahiaUser user,
                                                  RuleEvaluationContext ctx);

    /**
     * Remove any document matching a field key from search index
     *
     * @param siteId
     * @param keyFieldName  name of key field for which to remove all documents from index
     * @param keyFieldValue the key field value
     * @param user
     * @param notifyCluster if true, a message is send to the other server nodes
     * @param allowQueuing if true,
     *        an indexation job is queued in an indexation queue instead of performed immediately
     * @param ctx
     */
    public abstract void removeFromSearchEngine(int siteId,
                                                String keyFieldName,
                                                String keyFieldValue,
                                                JahiaUser user,
                                                boolean notifyCluster,
                                                boolean allowQueuing, RuleEvaluationContext ctx);

    /**
     * Remove a contentObject from index
     *
     * @param objectKey
     * @param user
     * @param ctx
     */
    public abstract void removeContentObject( ObjectKey objectKey, JahiaUser user, RuleEvaluationContext ctx);

    /**
     * Remove a contentObject from index
     *
     * @param objectKey
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public abstract void removeContentObject(ObjectKey objectKey, JahiaUser user,
                                             boolean notifyCluster,
                                             boolean allowQueuing, RuleEvaluationContext ctx);

    /**
     * Remove a contentObject from index
     *
     * @param contentObject
     * @param user
     * @param ctx
     */
    public abstract void removeContentObject( ContentObject contentObject, JahiaUser user, RuleEvaluationContext ctx);

    /**
     * Remove a contentObject from index
     *
     * @param contentObject
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public abstract void removeContentObject(ContentObject contentObject, JahiaUser user,
                                             boolean notifyCluster, boolean allowQueuing, RuleEvaluationContext ctx);

    /**
     * Remove the whole index for a site. This is used when a site is deleted. Notice that
     * 
     *
     * @param siteId
     */
    public abstract void deleteIndexForSite(int siteId, JahiaUser user, boolean notifyCluster);    
    
    /**
     * Request indexing ContentObject
     *
     * @param objectKey
     * @param ctx
     */
    public abstract void indexContentObject( ObjectKey objectKey, JahiaUser user, RuleEvaluationContext ctx );

    /**
     * Request indexing ContentObject
     *
     * @param contentObject
     * @param user
     * @param ctx
     */
    public abstract void indexContentObject( ContentObject contentObject, JahiaUser user, RuleEvaluationContext ctx );

    /**
     * Request indexing ContentObject
     *
     * @param objectKey
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public abstract void indexContentObject( ObjectKey objectKey, JahiaUser user,
                                             boolean notifyCluster, boolean allowQueuing,
                                             RuleEvaluationContext ctx);

    /**
     * index a container
     * @param ctnId
     * @param user
     * @param ctx
     */
    public abstract void indexContainer( int ctnId, JahiaUser user, RuleEvaluationContext ctx );

    /**
     * index a container
     *
     * @param ctnId
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public abstract void indexContainer( int ctnId,
                                         JahiaUser user,
                                         boolean notifyCluster,
                                         boolean allowQueuing,
                                         RuleEvaluationContext ctx);

    /**
     * index page
     *
     * @param pageId
     * @param user
     * @param ctx
     */
    public abstract void indexPage( int pageId, JahiaUser user, RuleEvaluationContext ctx );

    /**
     * index page
     *
     * @param pageId
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public abstract void indexPage( int pageId,
                                    JahiaUser user,
                                    boolean notifyCluster,
                                    boolean allowQueuing,
                                    RuleEvaluationContext ctx);

    /**
     * index a containerList
     * @param ctnListId
     * @param user
     */
    public abstract void indexContainerList( int ctnListId, JahiaUser user );

    /**
     * index a containerList
     *
     * @param ctnListId
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     */
    public abstract  void indexContainerList(int ctnListId,
                                             JahiaUser user,
                                             boolean notifyCluster,
                                             boolean allowQueuing );

    /**
     * Re-index a field.
     *
     * @param fieldID
     * @param user
     * @param ctx
     */
    public abstract void indexField ( int fieldID, JahiaUser user, RuleEvaluationContext ctx);

    /**
     * Re-index a field.
     *
     * @param fieldID
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public abstract void indexField (int fieldID,
                                     JahiaUser user,
                                     boolean notifyCluster,
                                     boolean allowQueuing,
                                     RuleEvaluationContext ctx);

    /**
     * Returns a list of IndexableDocument to add to the SearchHandler.addDocument(doc)
     *
     * @param ctnId
     * @param user
     */
    public abstract List<IndexableDocument> getIndexableDocumentsForContainer(  int ctnId,
                                                             JahiaUser user );

    /**
     * Returns a list of IndexableDocument to add to the SearchHandler.addDocument(doc)
     *
     * @param pageId
     * @param user
     */
    public abstract List<IndexableDocument> getIndexableDocumentsForPage(  int pageId,
                                                        JahiaUser user );

    /**
     * Returns a list of IndexableDocument to add to the SearchHandler.addDocument(doc)
     *
     * @param fieldId
     * @param user
     * @param applyFileFieldIndexationRule if true, apply indexation rule for file field if needed. If false,
     *                                              extract and index the file field immediately
     * @return
     */
    public abstract List<IndexableDocument> getIndexableDocumentsForField(  int fieldId,
                                                         JahiaUser user,
                                                         boolean applyFileFieldIndexationRule);

    /**
     * Re-index a full site.
     * Should be called under particular situations ( time consuming ).
     *
     * @param siteId
     * @param user
     *
     * @return boolean false on error.
     */
    public abstract boolean indexSite (int siteId, JahiaUser user)
    throws JahiaException;

    /**
     * Perform an index optimization for a given site.
     *
     * @param siteID
     *
     * @return boolean false on error.
     */
    public abstract boolean optimizeIndex (int siteID);

    /**
     * Return the full path to search indexes root directory.
     *
     * @return String the site's index path, null if not exist.
     */
    public abstract String getSearchIndexRootDir ()
            throws JahiaException;

    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public abstract SearchResult search (String[] queries,
                                         String[] searchHandlers,
                                         ProcessingContext jParams)
            throws JahiaException;

    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param jParams
     * @param hitCollector
     * @return
     * @throws JahiaException
     */
    public abstract SearchResult search (String[] queries,
                                         String[] searchHandlers,
                                         ProcessingContext jParams,
                                         JahiaAbstractHitCollector hitCollector)
            throws JahiaException;

    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param languageCodes
     * @param jParams
     * @param hitCollector
     * @return
     * @throws JahiaException
     */
    public abstract SearchResult search (String[] queries,
                                         String[] searchHandlers,
                                         List<String> languageCodes,
                                         ProcessingContext jParams,
                                         JahiaAbstractHitCollector hitCollector)
            throws JahiaException;    
    
    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param jParams
     * @param searchResultBuilder
     * @param sort
     * @return
     * @throws JahiaException
     */
    public abstract SearchResult search(String[] queries,
                               String[] searchHandlers,
                               ProcessingContext jParams,
                               JahiaSearchResultBuilder searchResultBuilder,
                               Sort sort)
            throws JahiaException;
    
    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param languageCodes 
     * @param jParams
     * @param searchResultBuilder
     * @param sort
     * @return
     * @throws JahiaException
     */
    public abstract SearchResult search(String[] queries,
                               String[] searchHandlers,
                               List<String> languageCodes,                               
                               ProcessingContext jParams,
                               JahiaSearchResultBuilder searchResultBuilder,
                               Sort sort)
            throws JahiaException;
    
    //--------------------------------------------------------------------------
    /**
     * Return a List of matching pages.
     * Perform a search for a given query ( valid lucene query ).
     *
     * @return JahiaSearchResult, containing a List of matching page.
     *
     * @param siteId
     * @param queryString
     * @param jParams to check read access.
     * @param languageCodes language codes in which language to search.
     * @param searchResultBuilder
     * @return
     * @throws JahiaException
     */
    public abstract JahiaSearchResult search (int siteId,
                                              String queryString,
                                              ProcessingContext jParams,
                                              List<String> languageCodes,
                                              JahiaSearchResultBuilder searchResultBuilder)
    throws JahiaException;

    /**
     *
     * @param searchHandlers
     * @param queryString
     * @param jParams
     * @param languageCodes
     * @param searchResultBuilder
     * @return
     * @throws JahiaException
     */
    public abstract JahiaSearchResult search (   String[] searchHandlers,
                                                 String queryString,
                                                 ProcessingContext jParams,
                                                 List<String> languageCodes,
                                                 JahiaSearchResultBuilder searchResultBuilder)
            throws JahiaException;

    public abstract JahiaSearchResult search(String[] searchHandlers,
                                    String queryString, String jcrQueryString,
                                    ProcessingContext jParams,
                                    List<String> languageCodes,
                                    JahiaSearchResultBuilder searchResultBuilder)
            throws JahiaException;

    /**
     *
     * @param savedSearch
     * @param searchViewHandler
     * @param context
     * @throws JahiaException
     */
    public abstract void saveSearch(JahiaSavedSearch savedSearch,
                                    SearchViewHandler searchViewHandler,
                                    ProcessingContext context)
    throws JahiaException;

    /**
    *
    * @param savedSearch
    * @throws JahiaException
    */
   public abstract void saveSearch(JahiaSavedSearch savedSearch)
   throws JahiaException;

    /**
     * Delete the given saved search
     *
     * @param id
     * @throws JahiaException
     */
    public abstract void deleteSearch(int id)
    throws JahiaException;

    /**
     * Returns all saved searches
     *
     * @return
     * @throws JahiaException
     */
    public abstract List<JahiaSavedSearch> getSavedSearches()
    throws JahiaException;
    
    /**
     * Returns all saved searches for the specified user.
     * 
     * @param owner
     *            current user
     * @return all saved searches for the specified user
     * @throws JahiaException
     */
    public abstract List<JahiaSavedSearch> getSavedSearches(JahiaUser owner)
            throws JahiaException;
    
    /**
     * Returns the saved search for the given title
     *
     * @return JahiaSavedSearch
     * @throws JahiaException
     */
    public abstract JahiaSavedSearch getSavedSearch(String title) throws JahiaException;    

    /**
     * Returns the map of field grouping. It is used for query rewriting
     * @return
     */
    public abstract Map<String, Set<String>> getFieldsGrouping();

    /**
     * Returns the list of fields that can be copied to SearchHit object from raw search hit ( like lucene Field )
     * This is used to limit the size of memory.
     *  
     * @return
     */
    public abstract List<String> getFieldsToCopyToSearchHit();

    public abstract void setFieldsToCopyToSearchHit(List<String> fieldsToCopyToSearchHit);

    /**
     * Returns the list of fields that must not be used for highlighting in search result. Typically date fields.
     * @return
     */
    public abstract List<String> getFieldsToExcludeFromHighlighting();

    public abstract void setFieldsToExcludeFromHighlighting(List<String> fieldsToExcludeFromHighlighting);

    /**
     * Date rounding to keep the search index smaller
     * @return
     */
    public abstract int getDateRounding();

    public abstract void setDateRounding(int dateRounding);

    /**
     * Returns true if the service is configured to run with multiple indexing server
     *         false if there is only one indexing server, even in clustering
     *
     * @return
     */
    public abstract boolean isMultipleIndexingServer();

    public abstract void setMultipleIndexingServer(boolean multipleIndexingServer);

    /**
     * Return a CompassHighlighter instance
     * @param searchEngineHighlighter
     * @param resource
     * @return
     */
    public abstract JahiaCompassHighlighter getCompassHighlighter(
            SearchEngineHighlighter searchEngineHighlighter,
            Resource resource);

    public abstract Iterator<String> getTerms (final int siteID, final String query);

    /**
     * Performs a DMS search for files, matching the specified query.
     * 
     * @param jcrQuery
     *            file search query
     * @param user
     *            current user that executes this search query
     * @return {@link JahiaSearchResult} object containing results of the file
     *         search
     */
    public abstract JahiaSearchResult fileSearch(String jcrQuery, JahiaUser user);
    
    public abstract JahiaSavedSearchView getSavedSearchView(Integer searchMode,
            Integer savedSearchId, String contextId, String viewName,
            ProcessingContext ctx) throws JahiaException;

    public abstract void updateSavedSearchView(JahiaSavedSearchView view,
            ProcessingContext ctx) throws JahiaException;

    /**
     *
     * @param hit
     * @param jParams
     * @param serverURL
     * @return
     * @throws JahiaException
     */
    public abstract SyndEntry getSyndEntry(JahiaSearchHit hit, ParamBean jParams, String serverURL)
    throws JahiaException;

    /**
     *
     * @param hit
     * @param jParams
     * @param serverURL
     * @return
     * @throws JahiaException
     */
    public abstract SyndEntry getSyndEntry(Hit hit, ParamBean jParams, String serverURL)
    throws JahiaException;
    
    public abstract void initSearchFieldConfiguration(int siteId);


}
