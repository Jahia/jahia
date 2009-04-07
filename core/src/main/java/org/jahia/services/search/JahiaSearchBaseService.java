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
//  JahiaSearchService
//  NK		25.01.2002 Implementation based on Lucene engine.
//
//
package org.jahia.services.search;


import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Sort;
import org.compass.core.Compass;
import org.compass.core.Resource;
import org.compass.core.Property.Index;
import org.compass.core.config.CompassConfiguration;
import org.compass.core.engine.SearchEngineHighlighter;
import org.compass.core.engine.naming.StaticPropertyPath;
import org.compass.core.lucene.engine.LuceneSearchEngineFactory;
import org.compass.core.mapping.CompassMapping;
import org.compass.core.mapping.Mapping;
import org.compass.core.mapping.ResourceMapping;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.mapping.rsem.RawResourceMapping;
import org.compass.core.mapping.rsem.RawResourcePropertyMapping;
import org.compass.core.spi.InternalCompass;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContentContainerFacade;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaContentFieldFacade;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.data.fields.LoadFlags;
import org.jahia.data.search.JahiaSearchResult;
import org.jahia.data.search.JahiaSearchHit;
import org.jahia.engines.search.*;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.cache.JahiaBatchingClusterCacheHibernateProvider;
import org.jahia.hibernate.manager.*;
import org.jahia.hibernate.model.indexingjob.*;
import org.jahia.params.BasicURLGeneratorImpl;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.Pipeline;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.cluster.ClusterListener;
import org.jahia.services.cluster.ClusterMessage;
import org.jahia.services.cluster.ClusterService;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.content.automation.RulesListener;
import org.jahia.services.content.automation.URLService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.JahiaFieldService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.search.compass.CompassResourceConverter;
import org.jahia.services.search.compass.JahiaCompassHighlighter;
import org.jahia.services.search.indexingscheduler.*;
import org.jahia.services.search.jcr.JcrSearchHandler;
import org.jahia.services.search.lucene.IndexUpdatedMessage;
import org.jahia.services.search.lucene.JahiaAbstractHitCollector;
import org.jahia.services.search.lucene.SynchronizedIndexationRequestMessage;
import org.jahia.services.search.lucene.SynchronizedIndexationResponseMessage;
import org.jahia.services.search.lucene.fs.JahiaIndexSearcher;
import org.jahia.services.search.lucene.fs.LuceneQueryRequest;
import org.jahia.services.search.lucene.fs.LuceneSearchHandlerImpl;
import org.jahia.services.search.savedsearch.JahiaSavedSearch;
import org.jahia.services.search.savedsearch.JahiaSavedSearchView;
import org.jahia.services.search.savedsearch.JahiaSavedSearchViewSettings;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaTools;
import org.jgroups.Address;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.SimpleTrigger;

import java.io.File;
import java.security.Principal;
import java.util.*;
import java.util.concurrent.Semaphore;
import javax.jcr.PropertyType;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndContent;


/**
 * Search Service based on Lucene engine.
 * This service is a little unique, since the indexing is done by a background
 * thread that executes the indexing orders. This is done because indexing is
 * a slow task and there is no reason that the user should wait on it.
 *
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @version 1.0
 */
public class JahiaSearchBaseService extends JahiaSearchService
        implements SearchIndexationPipeline, ClusterListener, Runnable  {

    private static Logger logger = Logger
            .getLogger(JahiaSearchBaseService.class);

    /**
     * The unique instance of this service *
     */
    protected static JahiaSearchBaseService theObject;
    
    private Byte[] fieldScoreLock = new Byte[0];
    private Semaphore fieldScoreSemaphore = new Semaphore(1);

    /**
     * Constants *
     */
    private static final String searchIndexesDir = "search_indexes";

    public static final String WEBDAV_SEARCH = "webdavsearch";
    
    /**
     * The indexes root path *
     */
    private String searchIndexesDiskPath = "";

    private Properties config = new Properties();
    
    private Properties indexationConfig = new Properties();    
    
    private Properties filterCacheConfig = new Properties();    

    private SearchManager searchManager;

    private JahiaSavedSearchManager savedSearchManager;
    
    private JahiaSavedSearchViewManager savedSearchViewManager;    

    private Boolean indexationDisabled;
    
    private Compass compass;

    private CompassResourceConverter compassResourceConverter;

    private Pipeline searchIndexProcessPipeline;

    private JahiaIndexingJobManager indJobMgr = null;

    private boolean localIndexing = true;

    private boolean multipleIndexingServer = false;

    private JahiaServerPropertiesManager serverPropMgr = null;

    private Long lastIndexingJobTime = new Long(0);

    private Thread backgroundIndexingThread;

    private Thread IndexingJobConsummerThread;

    private IndexingJobConsummerStarter indexingJobConsummerStarter;

    private String serverId;

    private LRUMap indexingJobInserts;

    private long indexingJobInsertMinInterval = 0;

    private int indexingJobInserMapMaxSize = 5000;

    private long searchScoreBoostRefreshDelayTime = 30000;

    private int dateRounding = 5;

    private long synchronizedIndexationWaitDelay = 5000;

    private Map<String, Shutdownable> shutdownables = new HashMap<String, Shutdownable>();

    private Properties clusterNodeSettings;

    private JahiaSitesService sitesService;
    private SchedulerService schedulerService;
    private ClusterService clusterService;
    private JahiaContainersService containersService;
    private JahiaGroupManagerService groupManagerService;
    private JahiaFieldService fieldService;

    private List<Integer> loadedSiteSearchHandlers = new ArrayList<Integer>();

    private Map<String, Set<String>> fieldsGrouping = null;
    private long fieldsScoreBoostLastUpdateTime;
    private List<String> fieldsToCopyToSearchHit = new ArrayList<String>();

    private Map<String, JahiaSavedSearchViewSettings> savedSearchViewSettings = Collections.emptyMap();

    private List<String> fieldsToExcludeFromHighlighting = new ArrayList<String>();

    private List<SynchronizedIndexationTask> synchronizedIndexationTask = new ArrayList<SynchronizedIndexationTask>();

    private static ThreadLocal<Set<LuceneQueryRequest>> threadLocalOpenLuceneQueryRequest = new ThreadLocal<Set<LuceneQueryRequest>>();
    private static ThreadLocal<Set<Searcher>> threadLocalSearcher = new ThreadLocal<Set<Searcher>>();

    //-------------------------------------------------------------------------
    //
    // Constructors, global start, stop, inits services methods
    //
    //-------------------------------------------------------------------------

    /**
     * Constructor
     * Client should always call getInstance() method
     */
    protected JahiaSearchBaseService() {
    }

    /**
     * Returns the unique instance of this service.
     */
    public static JahiaSearchBaseService getInstance() {
        if (theObject == null) {
            synchronized (JahiaSearchBaseService.class) {
                if (theObject == null) {
                    theObject = new JahiaSearchBaseService();
                }                
            }
        }
        return theObject;
    }

    public void setClusterNodeSettings(Properties clusterNodeSettings) {
        this.clusterNodeSettings = clusterNodeSettings;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }
    
    public void setContainersService(JahiaContainersService containersService) {
        this.containersService = containersService;
    }

    public void setGroupManagerService(JahiaGroupManagerService groupManagerService) {
        this.groupManagerService = groupManagerService;
    }

    public void setFieldService(JahiaFieldService fieldService) {
        this.fieldService = fieldService;
    }

    /**
     * Initialization
     */
    public void start() throws JahiaInitializationException {

        configureSystemProperties();

        String val = getIndexationConfig().getProperty(JahiaSearchConfigConstant.SEARCH_INDEX_ROOT_DIR);
        if (val != null) {
            val = JahiaTools.convertContexted (val,
                    SettingsBean.getInstance().getPathResolver());
            File f = new File(val);
            if ( f.isAbsolute() ){
                searchIndexesDiskPath = val;
            } else {
                if (val.startsWith("/")) {
                    searchIndexesDiskPath = Jahia.getStaticServletConfig()
                        .getServletContext().getRealPath(val);
                } else {
                    searchIndexesDiskPath = val;
                }
            }
        } else {
            // the default
            searchIndexesDiskPath = settingsBean.getJahiaVarDiskPath()
                    + File.separator
                    + searchIndexesDir;
        }
        logger.debug("Search index Root Dir: " + searchIndexesDiskPath);

        File f = new File(searchIndexesDiskPath);
        // Create lucene search index repository if not exists.
        if (!f.isDirectory()) {
            f.mkdir();
        }

        val = getIndexationConfig().getProperty(JahiaSearchConfigConstant.SEARCH_LOCAL_INDEXING);
        localIndexing = !((val != null && "0".equals(val.trim())));
        if (!localIndexing) {
            logger
                    .info("This node will not perfom search indexation, just search.");
        }

        val = getIndexationConfig().getProperty(JahiaSearchConfigConstant.SEARCH_MULTIPLE_INDEXING_SERVER);
        this.multipleIndexingServer = (val != null && "1".equals(val.trim()));

        try {
            Properties props = (Properties) this.config.clone();
            props.setProperty("indexDirectory", f.getAbsolutePath());
            searchManager = new SearchManager(props,this);
        } catch (Exception e) {
            logger.error("Exception creating Search Manager", e);
            throw new JahiaInitializationException("Exception creating Search Manager", e);
        }

        this.indexingJobInsertMinInterval = JahiaTools.getTimeAsLong(getIndexationConfig().getProperty(
                 JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_EXECUTION_DELAY_TIME),"600").longValue()/2;
        try {
            this.indexingJobInserMapMaxSize = Integer.parseInt(getIndexationConfig().getProperty(
                 JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_INSERT_MAP_MAXSIZE,"5000"));
        } catch ( Throwable t ){
            logger.debug("Wrong value for indexing job insert map max size, use default " + this.indexingJobInserMapMaxSize,t);
        }
        this.indexingJobInserts = new LRUMap(this.indexingJobInserMapMaxSize);
        this.searchScoreBoostRefreshDelayTime = JahiaTools.getTimeAsLong(getIndexationConfig().getProperty(
                 JahiaSearchConfigConstant.SEARCH_SCORE_BOOST_REFRESH_DELAY_TIME,"0"),"0").longValue();
        if ( this.searchScoreBoostRefreshDelayTime > 0 && this.searchScoreBoostRefreshDelayTime < 5000 ){
            this.searchScoreBoostRefreshDelayTime = 5000;
        }

        // init fields score boost
//        This is now triggered from the TemplateService         
//        initSearchFieldConfiguration(); 

        val = getIndexationConfig().getProperty(JahiaSearchConfigConstant.DATE_ROUNDING);
        try {
            this.dateRounding = Integer.parseInt(val);
            if ( this.dateRounding < 0 ){
                this.dateRounding = 5;
            }
        } catch (Exception e) {
            this.dateRounding = 5;
        }

        this.synchronizedIndexationWaitDelay = JahiaTools.getTimeAsLong(getIndexationConfig().getProperty(
                 JahiaSearchConfigConstant.SYNCHRONIZED_INDEXATION_WAIT_DELAY,"5s"),"5s").longValue();

        try {
            if ( settingsBean.isProcessingServer() ){
                startIndexingJobConsummer();
            }
        } catch (Exception e) {
            logger.error("Exception starting IndexingJobConsummer", e);
            throw new JahiaInitializationException("Exception starting indexingJobConsummer", e);
        }
        backgroundIndexingThread = new Thread(theObject,
            "Background content indexing");
        backgroundIndexingThread.setDaemon(true);
        backgroundIndexingThread.start(); // start background thread

        clusterService.addListener(this);

        logger.debug("Initialized");

    }

    public synchronized void startIndexingJobConsummer()
            throws JahiaException {
        if (this.isDisabled()) {
            return;
        }
        this.indexingJobConsummerStarter = new IndexingJobConsummerStarter();
        this.IndexingJobConsummerThread = new Thread(this.indexingJobConsummerStarter, "Processing server indexing consumer");
        this.IndexingJobConsummerThread.setDaemon(true);
        this.IndexingJobConsummerThread.start();
    }

    public synchronized void stop()
            throws JahiaException {

        logger.info("Shutting down search service...");

        this.localIndexing = false;
        this.indexationDisabled = Boolean.TRUE;
        if (backgroundIndexingThread != null) {
            backgroundIndexingThread.interrupt();
        }
        if ( this.indexingJobConsummerStarter != null ){
            this.indexingJobConsummerStarter.shutdown();
        }
        for (Shutdownable s : this.shutdownables.values()){
            try {
                s.shutdown();
            } catch ( Exception t ){
                logger.info("Error shuting down service",t);
            }
        }

        logger.info("Shutting down search service done...");

    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }    
    
    public Properties getIndexationConfig() {
        return indexationConfig;
    }

    public void setIndexationConfig(Properties indexationConfig) {
        this.indexationConfig = indexationConfig;
    }    
    
    public Properties getFilterCacheConfig() {
        return filterCacheConfig;
    }

    public void setFilterCacheConfig(Properties filterCacheConfig) {
        this.filterCacheConfig = filterCacheConfig;
    }

    private void configureSystemProperties() {

        if (this.getIndexationConfig() != null) {

            for (Map.Entry<Object, Object> entry : this.getIndexationConfig().entrySet()) {
                String key = (String)entry.getKey();
                if (key.startsWith("org.apache.lucene")) {
                    String val  = (String)entry.getValue();
                    if (val != null && !"".equals(val.trim())) {
                        System.setProperty(key, val);
                    }
                }
            }
        }
    }

    /**
     * Return the last indexingJob time
     *
     * @return
     */
    public long getLastIndexingJobTime() {
        synchronized (lastIndexingJobTime){
            return lastIndexingJobTime.longValue();
        }
    }

    /**
     * Set the last indexingJob time
     *
     * @param lastIndexingJobTime
     */
    public synchronized void setLastIndexingJobTime(long lastIndexingJobTime) {
        synchronized(this.lastIndexingJobTime){
            this.lastIndexingJobTime = new Long(lastIndexingJobTime);
        }
    }

    //-------------------------------------------------------------------------
    //
    // Runnable interface
    //
    //-------------------------------------------------------------------------

    /**
     * Synchronized indexation. Be care about bad performance
     *
     * @param job
     */
    public void synchronizedIndexation(JahiaIndexingJob job){
        if (this.isDisabled() || !this.localIndexing){
            return;
        }
        final List<RemovableDocument> toRemove = new ArrayList<RemovableDocument>();
        final List<IndexableDocument> toAdd = new ArrayList<IndexableDocument>();
        int siteID = job.getSiteId().intValue();
        if ( job.isValid() ){
            JahiaUser user = getAdminUser(siteID);
            if (job instanceof JahiaDeleteIndexJob) {
                deleteIndexForSite(job.getSiteId(), user, false);
            } else {
                job.prepareBatchIndexation(toRemove, toAdd, user);
                final SearchHandler searchHandler = getSearchHandler(siteID);
                if (searchHandler != null) {
                    searchHandler.synchronizedBatchIndexing(toRemove, toAdd);
                }
            }
        }
    }

    public void run(){
        serverId = clusterNodeSettings.getProperty("serverId");
        boolean processedStartupLatencyTime = false;
        SpringContextSingleton instance = SpringContextSingleton.getInstance();
        Session session = null;
        while ( !this.isDisabled() && this.localIndexing ){
            if ( ! processedStartupLatencyTime ){
                try {
                    Thread.sleep(JahiaTools.getTimeAsLong(
                            this.getIndexationConfig().getProperty(
                                    JahiaSearchConfigConstant.SEARCH_INDEXING_STARTUP_LATENCY_TIME),"5m").longValue());
                    processedStartupLatencyTime = true;
                } catch ( InterruptedException inte ){
                }
            } else {
                try {
                    Thread.sleep(JahiaTools.getTimeAsLong(
                            this.getIndexationConfig().getProperty(
                                    JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_WATCH_INTERVAL),"20s").longValue());
                } catch ( InterruptedException inte ){
                }
            }
            if ( this.isDisabled() || !this.localIndexing ){
                continue;
            }
            try {
                if (!instance.isInitialized()) {
                    continue;
                }

                this.wakeupSearchIndexer();
                this.launchSiteReindexation();

                SessionFactory factory = (SessionFactory)instance.getContext().getBean("sessionFactory");
                List<JahiaIndexingJob> jobsList = null;
                if ( getLastIndexingJobTime()==0 ){
                    JahiaIndexJobServer indexJobServer = indJobMgr.getServerLastIndexedJob(serverId);
                    if ( indexJobServer != null ){
                        setLastIndexingJobTime(indexJobServer.getDate().longValue());
                    }
                }
                long time = getLastIndexingJobTime() - 5000;
                Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
                int minutes = cal.get(Calendar.MINUTE);
                List<JahiaIndexingJob> avJobsList = indJobMgr.getIndexingJobs(time,true,hourOfDay,minutes,serverId);
                if ( this.multipleIndexingServer ){
                    jobsList = IndexingJobTools.resolveIndexingJobs(avJobsList, serverId);
                } else {
                    jobsList = IndexingJobTools.resolveIndexingJobs(avJobsList, null);
                }
                if ( jobsList == null || jobsList.isEmpty() ){
                    this.notifySiteIndexingJob();
                }
                session = factory.openSession();
                Map<Integer, JahiaUser> cachedAdminUsers = new HashMap<Integer, JahiaUser>();
                if (jobsList.size() > 0) {
                    Map<Integer, List<RemovableDocument>> sitesToRemove = new HashMap<Integer, List<RemovableDocument>>();
                    Map<Integer, List<IndexableDocument>> sitesToAdd = new HashMap<Integer, List<IndexableDocument>>();
                    for (JahiaIndexingJob job : jobsList) {
                        List<RemovableDocument> toRemove = new ArrayList<RemovableDocument>();
                        List<IndexableDocument> toAdd = new ArrayList<IndexableDocument>();
                        Integer siteID = job.getSiteId();
                        if (job.isValid()) {
                            JahiaUser user = (JahiaUser) cachedAdminUsers
                                    .get(siteID);
                            if (user == null) {
                                user = getAdminUser(siteID.intValue());
                                if (user != null) {
                                    cachedAdminUsers.put(siteID, user);
                                }
                            }
                            if (job instanceof JahiaDeleteIndexJob) {
                                deleteIndexForSite(job.getSiteId(), user, false);
                            } else {
                                job.prepareBatchIndexation(toRemove, toAdd,
                                        user);

                                List<RemovableDocument> removeDocsList = sitesToRemove
                                        .get(job.getSiteId());
                                if (removeDocsList == null) {
                                    removeDocsList = new ArrayList<RemovableDocument>();
                                    sitesToRemove.put(job.getSiteId(),
                                            removeDocsList);
                                }
                                removeDocsList.addAll(toRemove);

                                List<IndexableDocument> addDocsList = sitesToAdd
                                        .get(job.getSiteId());
                                if (addDocsList == null) {
                                    addDocsList = new ArrayList<IndexableDocument>();
                                    sitesToAdd
                                            .put(job.getSiteId(), addDocsList);
                                }
                                addDocsList.addAll(toAdd);
                            }
                        }
                    }
                    for (Integer siteId : sitesToRemove.keySet()){

                        List<RemovableDocument> toRemove = sitesToRemove.get(siteId);
                        List<IndexableDocument> toAdd = sitesToAdd.get(siteId);
                        if ( toRemove == null ){
                            toRemove = new ArrayList<RemovableDocument>();
                        }
                        if ( toAdd == null ){
                            toAdd = new ArrayList<IndexableDocument>();
                        }
                        SearchHandler searchHandler = getSearchHandler(siteId.intValue());
                        if ( searchHandler != null ){
                            SearchIndexer searchIndexer = searchHandler.getIndexer();
                            if ( searchIndexer != null ){
                                int count = 100;
                                while (count>=100){
                                    searchIndexer.wakeUp();
                                    count = searchIndexer.getBufferedDocs();
                                    if ( count>=100 ){
                                        try {
                                            Thread.sleep(5000);
                                        } catch ( InterruptedException inte ){
                                        }
                                    } else {
                                        notifySiteIndexingJobOfBatchingIndexation(toRemove,toAdd);
                                        searchHandler.batchIndexing(toRemove,toAdd);
                                        continue;
                                    }
                                }
                            }
                        }
                    }

                    time = 0;
                    for (JahiaIndexingJob job : jobsList) {
                        try {
                            time = job.getDate().longValue();
                            setLastIndexingJobTime(time);
                            if ( this.multipleIndexingServer ) {
                                JahiaIndexJobServer jobServer = new JahiaIndexJobServer(new JahiaIndexJobServerPK(job.getId(),serverId),job.getDate());
                                indJobMgr.saveIndexJobServer(jobServer);
                            } else {
                                indJobMgr.delete(job.getId());
                            }
                        } catch ( Exception t ){
                            logger.debug("Exception saving JahiaIndexationJob", t);
                        }
                    }
                }
            } catch ( Exception t ){
                logger.debug("Exception executing JahiaIndexationJobs", t);
            } finally {
                if ( session != null && session.isOpen() ) {
                    try {
                        session.close();
                    } catch ( HibernateException ex ){
                        logger.debug("Error closing Hibernate session",ex);
                    }
                }
            }
        }
    }

    protected void notifySiteIndexingJob() {
        try {
            List<JobExecutionContext> executingJobs = ServicesRegistry.getInstance().getSchedulerService()
                    .getCurrentlyExecutingRamJobs();
            if ( executingJobs == null ){
                return;
            }
            for (JobExecutionContext jobExContext : executingJobs){
                Job job = jobExContext.getJobInstance();
                if ( job != null && job instanceof JahiaSiteIndexingJob ){
                    int siteId = ((JahiaSiteIndexingJob)job).getSiteID();
                    SearchHandler searchHandler = this.getSearchHandler(siteId);
                    if ( searchHandler != null ){
                        SearchIndexer searchIndexer = searchHandler.getIndexer();
                        if ( searchIndexer != null && searchIndexer.getBufferedDocs() < 50){
                            ((JahiaSiteIndexingJob)job).notifyJob();
                        }
                    }

                }
            }
        } catch ( Exception t ){
            logger.debug("Error notifying jahiasite indexing jobs",t);
        }
    }

    protected void notifySiteIndexingJobOfBatchingIndexation(List<RemovableDocument> toRemove, List<IndexableDocument> toAdd) {
        try {
            List<JobExecutionContext> executingJobs = ServicesRegistry.getInstance().getSchedulerService()
                    .getCurrentlyExecutingRamJobs();
            if ( executingJobs == null ){
                return;
            }
            for ( JobExecutionContext jobExContext : executingJobs){
                Job job = jobExContext.getJobInstance();
                if ( job != null && job instanceof JahiaSiteIndexingJob ){
                    ((JahiaSiteIndexingJob)job).addConcurrentJobs(toRemove,false);
                    ((JahiaSiteIndexingJob)job).addConcurrentJobs(toAdd,true);
                }
            }
        } catch ( Exception t ){
            logger.debug("Error notifying jahiasite indexing jobs",t);
        }
    }

    protected void wakeupSearchIndexer() {
        for (SearchHandler searchHandler : this.getSearchManager().getSearchHandlers().values() ){
            searchHandler.getIndexer();
            SearchIndexer indexer = (SearchIndexer)searchHandler.getIndexer();
            if ( indexer != null ){
                indexer.wakeUp();
            }
        }
    }

    //-------------------------------------------------------------------------
    //
    // Getter and setter methods
    //
    //-------------------------------------------------------------------------

    public JahiaIndexingJobManager getIndJobMgr() {
        return indJobMgr;
    }

    public void setIndJobMgr(JahiaIndexingJobManager indJobMgr) {
        this.indJobMgr = indJobMgr;
    }

    public Compass getCompass() {
        return compass;
    }

    public void setCompass(Compass compass) {
        this.compass = compass;
        if ( this.compass != null ){
            this.compass.getSearchEngineIndexManager().stop();
        }
    }

    public CompassResourceConverter getCompassResourceConverter() {
        return compassResourceConverter;
    }

    public void setCompassResourceConverter(CompassResourceConverter compassResourceConverter) {
        this.compassResourceConverter = compassResourceConverter;
    }

    public Pipeline getSearchIndexProcessPipeline() {
        return searchIndexProcessPipeline;
    }

    public void setSearchIndexProcessPipeline(Pipeline searchIndexProcessPipeline) {
        this.searchIndexProcessPipeline = searchIndexProcessPipeline;
        if (this.searchIndexProcessPipeline != null) {
            try {
                this.searchIndexProcessPipeline.initialize();
            } catch (Exception t) {
                logger.debug("Exception occured on pipeline.initialize() call", t);
            }
        }
    }

    /**
     * Return the full path to search indexes root directory.
     *
     * @return String the site's index path, null if not exist.
     */
    public String getSearchIndexRootDir()
            throws JahiaException {
        return searchIndexesDiskPath;
    }

    public boolean isDisabled() {
        if (indexationDisabled == null) {
            indexationDisabled = Boolean.parseBoolean(getIndexationConfig().getProperty(JahiaSearchConfigConstant.SEARCH_INDEXING_DISABLED));
        }
        return indexationDisabled.booleanValue();
    }

    public JahiaServerPropertiesManager getServerPropMgr() {
        return serverPropMgr;
    }

    public void setServerPropMgr(JahiaServerPropertiesManager serverPropMgr) {
        this.serverPropMgr = serverPropMgr;
    }

    public int getDateRounding() {
        return dateRounding;
    }

    public void setDateRounding(int dateRounding) {
        this.dateRounding = dateRounding;
    }

    public boolean isMultipleIndexingServer() {
        return multipleIndexingServer;
    }

    public void setMultipleIndexingServer(boolean multipleIndexingServer) {
        this.multipleIndexingServer = multipleIndexingServer;
    }

    //-------------------------------------------------------------------------
    //
    // SearchManager , Search Handlers methods
    //
    //-------------------------------------------------------------------------

    public SearchManager getSearchManager() {
        return this.searchManager;
    }

    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
    }

    public void setSavedSearchManager(JahiaSavedSearchManager savedSearchManager) {
        this.savedSearchManager = savedSearchManager;
    }
    
    public void setSavedSearchViewManager(JahiaSavedSearchViewManager savedSearchViewManager) {
        this.savedSearchViewManager = savedSearchViewManager;
    }    

    public SearchHandler createSearchHandler(int siteId) {
        SearchHandler searchHandler = null;
        try {
            JahiaSite site = sitesService.getSite(siteId);
            searchHandler = searchManager.getSearchHandler(site.getSiteKey());
            if (searchHandler == null) {
                Properties config = (Properties)this.getConfig().clone();
                config.putAll(this.getIndexationConfig());
                config.setProperty("indexDirectory", this.getSearchIndexRootDir()
                        + File.separator + site.getSiteKey());
                if ( this.localIndexing ){
                    config.setProperty("readOnly", "false");
                } else {
                    config.setProperty("readOnly", "true");
                }
                searchHandler = searchManager.createSearchHandler(site.getSiteKey(),
                        site.getServerName(), site.getID(), config);
                if ( searchHandler != null ){
                    searchHandler.setAllSearchFieldName(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD);
                }

                // initialize search handler
                searchHandler = getFileSearchHandler();
            }

        } catch (Exception t) {
            logger.warn("Error creating search handler for site :" + siteId, t);
        }
        return searchHandler;
    }

    public SearchHandler getSearchHandler(int siteId) {
        SearchHandler searchHandler = null;
        try {
            JahiaSite site = sitesService.getSite(siteId);
            if (site != null) {
                searchHandler = searchManager.getSearchHandler(site.getSiteKey());
                if ( searchHandler == null ){
                    synchronized(loadedSiteSearchHandlers){
                        boolean alreadyLoaded = loadedSiteSearchHandlers.contains(new Integer(siteId));
                        if ( !alreadyLoaded ){
                            this.createSearchHandler(siteId);
                            loadedSiteSearchHandlers.add(new Integer(siteId));
                            searchHandler = searchManager.getSearchHandler(site.getSiteKey());
                        }
                    }
                }
            }
        } catch (Exception t) {
            logger.warn("Error retrieving search handler for site :" + siteId, t);
        }
        return searchHandler;
    }

    //-------------------------------------------------------------------------
    //
    // Adding and removing content to/from search index
    //
    //-------------------------------------------------------------------------

    /**
     * Save an JahiaIndexingJob in queue
     *
     * @param job
     */
    public void addIndexingJob(JahiaIndexingJob job, RuleEvaluationContext ctx) {
        if (job == null) {
            return;
        }
        synchronized(indexingJobInserts){
            Long lastInsertTime = null;
            StringBuffer key =new StringBuffer(100);
            String keyAsString = null;
            long diff = 0;
            if ( job instanceof JahiaFieldIndexingJob ){
                job.setEnabledIndexingServers(serverId);
                key.append(job.getClassName())
                    .append(((JahiaFieldIndexingJob)job).getFieldId());
                keyAsString = key.toString();
                lastInsertTime = (Long) indexingJobInserts.get(keyAsString);
            } else if ( job instanceof JahiaContainerIndexingJob ) {
                key.append(job.getClassName())
                    .append(((JahiaContainerIndexingJob)job).getCtnId());
                keyAsString = key.toString();
                lastInsertTime = (Long) indexingJobInserts.get(keyAsString);
            } else if ( job instanceof JahiaContainerListIndexingJob ) {
                key.append(job.getClassName())
                    .append(((JahiaContainerListIndexingJob)job).getCtnListId());
                keyAsString = key.toString();
                lastInsertTime = (Long) indexingJobInserts.get(keyAsString);
            } else if ( job instanceof JahiaPageIndexingJob ) {
                key.append(job.getClassName())
                    .append(((JahiaPageIndexingJob)job).getPageId());
                keyAsString = key.toString();
                lastInsertTime = (Long) indexingJobInserts.get(keyAsString);
            } else if ( job instanceof JahiaRemoveFromIndexJob ){
                key.append(key.append(job.getClassName())
                    .append(((JahiaRemoveFromIndexJob)job).getKeyFieldName())
                    .append(((JahiaRemoveFromIndexJob)job).getKeyFieldValue())
                    .append(job.getSiteId()));
                keyAsString = key.toString();
                lastInsertTime = (Long) indexingJobInserts.get(keyAsString);
            } else if ( job instanceof JahiaDeleteIndexJob ){
                key.append(key.append(job.getClassName())
                        .append(job.getSiteId()));
                keyAsString = key.toString();
                lastInsertTime = (Long) indexingJobInserts.get(keyAsString);                
            } else {
                indexingJobInserts = new LRUMap(this.indexingJobInserMapMaxSize);
            }
            if ( lastInsertTime != null ){
                diff = job.getDate().longValue()-lastInsertTime.longValue();
                if (diff < this.indexingJobInsertMinInterval) {
                    // skip inserting the job
                    //logger.info("Skip inserting indexingJob ");
                    return;
                } else {
                    indexingJobInserts.put(keyAsString,job.getDate());
                }
            } else if ( keyAsString != null ){
                indexingJobInserts.put(keyAsString,job.getDate());
            }
        }

        IndexationRuleInterface rule = ServicesRegistry.getInstance().getJahiaSearchIndexationService()
                .evaluateContentIndexationRules(ctx);
        if ( rule != null ){
            if ( rule.getIndexationMode()==IndexationRuleInterface.DONT_INDEX ){
                return;
            } else {
                job.setRuleId(new Integer(rule.getId()));
                if ( rule.getIndexationMode() == IndexationRuleInterface.SCHEDULED_INDEXATION ){
                    job.setIndexImmediately(Boolean.FALSE);
                    if (rule.getDailyIndexationTimes() != null && !rule.getDailyIndexationTimes().isEmpty()){
                        Iterator<TimeRange> it = rule.getDailyIndexationTimes().iterator();
                        int count = 0;
                        TimeRange timeRange = null;
                        while (count<3 && it.hasNext()){
                            timeRange = it.next();
                            if ( count == 0 ){
                                job.setScheduledFromTime1(new Integer(timeRange.getStartHour()*60
                                        +timeRange.getStartMinute()));
                                job.setScheduledToTime1(new Integer(timeRange.getEndHour()*60
                                        +timeRange.getEndMinute()));
                            } else if ( count == 1 ){
                                job.setScheduledFromTime2(new Integer(timeRange.getStartHour()*60
                                        +timeRange.getStartMinute()));
                                job.setScheduledToTime2(new Integer(timeRange.getEndHour()*60
                                        +timeRange.getEndMinute()));
                            } else if ( count == 2 ){
                                job.setScheduledFromTime3(new Integer(timeRange.getStartHour()*60
                                        +timeRange.getStartMinute()));
                                job.setScheduledToTime3(new Integer(timeRange.getEndHour()*60
                                        +timeRange.getEndMinute()));
                            }
                            count++;
                        }
                    } else {
                        // schedule to be indexed at any hours ( as soon as possible )
                        job.setScheduledFromTime1(new Integer(0));
                        job.setScheduledToTime1(new Integer(23*60+60));
                    }
                } else {
                    // immediate indexation
                    job.setIndexImmediately(Boolean.TRUE);
                }
            }
        } else {
            // schedule to be indexed at any hours ( as soon as possible )
            job.setIndexImmediately(Boolean.FALSE);
            job.setScheduledFromTime1(new Integer(0));
            job.setScheduledToTime1(new Integer(23*60+60));
        }
        if (job.getIndexImmediately().booleanValue()){
            triggerImmediateExecutionOnAllNodes(job);
        } else {
            indJobMgr.save(job);
         }
    }

    private void triggerImmediateExecutionOnAllNodes (JahiaIndexingJob job) {
        boolean notifyCluster = true;
        String indexingServerID = getIndexationConfig().getProperty(JahiaSearchConfigConstant.SEARCH_INDEXER_SERVER_ID);
        if (this.localIndexing && !this.multipleIndexingServer){
            notifyCluster = false;
        }
        if (notifyCluster){
            SynchronizedIndexationRequestMessage msg = new SynchronizedIndexationRequestMessage(job);
            SearchClusterMessage clusterMsg = new SearchClusterMessage(msg);
            if ( indexingServerID != null && !"".equals(indexingServerID.trim())
                    && !this.serverId.equals(indexingServerID) ) {
                SynchronizedIndexationTask task = new SynchronizedIndexationTask(job,indexingServerID,
                        this.synchronizedIndexationWaitDelay);
                synchronized(this.synchronizedIndexationTask){
                    this.synchronizedIndexationTask.add(task);
                    ServicesRegistry.getInstance().getCacheService().syncClusterNow();
                    JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
                    this.clusterService.sendMessage(clusterMsg);
                }
                task.startWait();
            } else {
                ServicesRegistry.getInstance().getCacheService().syncClusterNow();
                JahiaBatchingClusterCacheHibernateProvider.syncClusterNow();
                this.clusterService.sendMessage(clusterMsg);
            }
        }
        if (this.localIndexing){
            ServicesRegistry.getInstance().getJahiaSearchService().synchronizedIndexation(job);
            if (this.multipleIndexingServer){
                indJobMgr.save(job);
                ServicesRegistry.getInstance().getJahiaSearchService()
                        .recordIndexingJobAsDoneForServer(job);
            }
        }

    }
    
    public void recordIndexingJobAsDoneForServer( JahiaIndexingJob job ){
        try {
            long time = job.getDate().longValue();
            setLastIndexingJobTime(time);
            if ( this.multipleIndexingServer ) {
                JahiaIndexJobServer jobServer = new JahiaIndexJobServer(new JahiaIndexJobServerPK(job.getId(),serverId),
                        job.getDate());
                indJobMgr.saveIndexJobServer(jobServer);
            } else {
                indJobMgr.delete(job.getId());
            }
        } catch ( Exception t ){
            logger.debug("Exception saving JahiaIndexationJob", t);
        }
    }

    /**
     * Returns tht list of Indexing Job
     *
     * @return
     */
    public List<JahiaIndexingJob> getIndexingJobs() {
        return indJobMgr.getIndexingJobs();
    }

    /**
     * Delete an indexing job
     *
     * @param id
     */
    public void deleteIndexingJob(String id) {
        indJobMgr.delete(id);
    }

    /**
     * Remove any document matching a field key from search index
     *
     * @param siteId
     * @param keyFieldName  name of key field for which to remove all documents from index
     * @param keyFieldValue the key field value
     * @param user
     * @param ctx
     */
    public void removeFromSearchEngine(int siteId,
                                       String keyFieldName,
                                       String keyFieldValue,
                                       JahiaUser user,
                                       RuleEvaluationContext ctx) {
        removeFromSearchEngine(siteId, keyFieldName, keyFieldValue, user, true, true, ctx);
    }

    /*
    * Remove any document matching a field key from search index
    *
    * @param siteId
    * @param keyFieldName  name of key field for which to remove all documents from index
    * @param keyFieldValue the key field value
    * @param user
    * @param notifyCluster if true, a message is send to the other server nodes
    * @param allowQueuing  if true,
    *                      an indexation job is queued in an indexation queue instead of performed immediately
    * @param ctx The Rule indexation Evaluation Context. Only used if allowQueuing is true
    */
   public void removeFromSearchEngine(int siteId,
                                      String keyFieldName,
                                      String keyFieldValue,
                                      JahiaUser user,
                                      boolean notifyCluster,
                                      boolean allowQueuing,
                                      RuleEvaluationContext ctx) {

       if (this.isDisabled()) {
           return;
       }

       if (keyFieldName == null ||
               keyFieldValue == null ||
               "".equals(keyFieldName.trim()))
           return;


       JahiaRemoveFromIndexJob job = null;
       if (allowQueuing || (!allowQueuing && notifyCluster)) {
           job = new JahiaRemoveFromIndexJob(siteId, keyFieldName, keyFieldValue,
                   System.currentTimeMillis());
       }

       if (!allowQueuing) {
           if (notifyCluster) {
               SearchClusterMessage msg = new SearchClusterMessage(job);
               clusterService.sendMessage(msg);
           }
           if ( !this.localIndexing ){
               return;
           }
           RemovableDocumentImpl doc =
                   new RemovableDocumentImpl(keyFieldName, keyFieldValue);
           SearchHandler searchHandler = this.getSearchHandler(siteId);
           if (searchHandler != null) {
               searchHandler.removeDocument(doc);
           }
       } else {
           this.addIndexingJob(job,ctx);
       }
   }

    /**
     * Remove a contentObject from index
     *
     * @param objectKey
     * @param user
     * @param ctx
     */
    public void removeContentObject(ObjectKey objectKey, JahiaUser user, RuleEvaluationContext ctx) {
        removeContentObject(objectKey, user, false, true,ctx);
    }

    /**
     * Remove a contentObject from index
     *
     * @param objectKey
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public void removeContentObject(ObjectKey objectKey, JahiaUser user,
                                    boolean notifyCluster,
                                    boolean allowQueuing,
                                    RuleEvaluationContext ctx) {
        if (this.isDisabled()) {
            return;
        }

        try {
            ContentObject contentObject = null;
            if (ctx != null){
                contentObject = ctx.getContentObject();
            }
            if (contentObject == null){
                contentObject = ContentObject.getContentObjectInstance(objectKey);
                if (ctx!=null){
                    ctx.setContentObject(contentObject);
                }
            }
            removeContentObject(contentObject, user, notifyCluster, allowQueuing, ctx);
        } catch (Exception t) {
            logger.debug("Exception occured when trying to remove content object " + objectKey, t);
        }
    }

    /**
     * Remove a contentObject from index
     *
     * @param contentObject
     * @param user
     * @param ctx
     */
    public void removeContentObject(ContentObject contentObject, JahiaUser user, RuleEvaluationContext ctx) {
        removeContentObject(contentObject, user, false, true, ctx);
    }

    /**
     * Remove a contentObject from index
     *
     * @param contentObject
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public void removeContentObject(ContentObject contentObject, JahiaUser user,
                                    boolean notifyCluster, boolean allowQueuing,
                                    RuleEvaluationContext ctx) {
        if (this.isDisabled()) {
            return;
        }

        if (contentObject == null) {
            return;
        }
        try {
            if (contentObject instanceof ContentContainer) {
                removeFromSearchEngine(contentObject.getSiteID(),
                        JahiaSearchConstant.OBJECT_KEY, contentObject.getObjectKey().getKey(),
                        user, notifyCluster, allowQueuing, ctx);
            } else if (contentObject instanceof ContentPage) {
                removeFromSearchEngine(contentObject.getSiteID(),
                        JahiaSearchConstant.OBJECT_KEY, contentObject.getObjectKey().getKey(),
                        user, notifyCluster, allowQueuing, ctx);
            } else if (contentObject instanceof ContentContainerList) {
                removeFromSearchEngine(contentObject.getSiteID(),
                        JahiaSearchConstant.OBJECT_KEY, contentObject.getObjectKey().getKey(), user,
                        notifyCluster, allowQueuing, ctx);
            } else if (contentObject instanceof ContentField) {
                removeFromSearchEngine(contentObject.getSiteID(),
                        JahiaSearchConstant.OBJECT_KEY, contentObject.getObjectKey().getKey(), user,
                        notifyCluster, allowQueuing, ctx);
            }
        } catch (Exception t) {
            logger.debug("Exception occured when trying to remove content object "
                    + contentObject.getObjectKey(), t);
        }
    }

    /**
     * Request indexing ContentObject
     *
     * @param objectKey
     * @param user
     * @param ctx
     */
    public void indexContentObject(ObjectKey objectKey, JahiaUser user, RuleEvaluationContext ctx) {
        indexContentObject(objectKey, user, false, true,ctx);
    }

    /**
     * Request indexing ContentObject
     *
     * @param objectKey
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public void indexContentObject(ObjectKey objectKey, JahiaUser user,
                                   boolean notifyCluster, boolean allowQueuing,
                                   RuleEvaluationContext ctx) {
        if (this.isDisabled()) {
            return;
        }

        try {
            ContentObject contentObject = null;
            if (ctx != null){
                contentObject = ctx.getContentObject();
            }
            if (contentObject == null){
                contentObject = ContentObject.getContentObjectInstance(objectKey);
                if (ctx!=null){
                    ctx.setContentObject(contentObject);
                }
            }
            indexContentObject(contentObject, user, notifyCluster, allowQueuing,ctx);
        } catch (Exception t) {
            logger.debug("Exception occured when trying to index content object " + objectKey, t);
        }
    }

    /**
     * Request indexing ContentObject.
     *
     * @param contentObject
     * @param user
     * @param ctx
     */
    public void indexContentObject(ContentObject contentObject,
                                   JahiaUser user, RuleEvaluationContext ctx) {
        indexContentObject(contentObject, user, false, true, ctx);
    }

    /**
     * Request indexing ContentObject.
     *
     * @param contentObject
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public void indexContentObject(ContentObject contentObject,
                                   JahiaUser user,
                                   boolean notifyCluster, boolean allowQueuing, RuleEvaluationContext ctx) {
        if (this.isDisabled()) {
            return;
        }

        if (contentObject == null) {
            return;
        }
        if (contentObject instanceof ContentContainer) {
            indexContainer(contentObject.getID(), user, notifyCluster, allowQueuing,ctx);
        } else if (contentObject instanceof ContentPage) {
            indexPage(contentObject.getID(), user, notifyCluster, allowQueuing, ctx);
        } else if (contentObject instanceof ContentContainerList) {
            //@todo index container list (metadatas ?)
        } else if (contentObject instanceof ContentField) {
            boolean isMetadata = ((ContentField)contentObject).isMetadata();
            if (isMetadata) {
                try {
                    ContentObject  metadataOwner = ContentObject.getContentObjectFromMetadata(contentObject.getObjectKey());
                    if (metadataOwner != null) {
                        ctx.setObjectKey(metadataOwner.getObjectKey());
                        ctx.setContentObject(metadataOwner);
                        indexContentObject(metadataOwner, user, notifyCluster, allowQueuing, ctx);
                    }
                } catch (Exception t) {
                    logger.debug("Exception occured when trying to index metadata owner", t);
                }
            } else {
                int ctnId = ((ContentField) contentObject).getContainerID();
                if (ctnId > 0) {
                    try {
                        ContentContainer contentContainer = ContentContainer.getContainer(ctnId);
                        ctx.setObjectKey(contentContainer.getObjectKey());
                        ctx.setContentObject(contentContainer);
                        this.indexContainer(ctnId, user, notifyCluster, allowQueuing, ctx);
                    } catch ( Exception t ){
                        logger.debug("Exception occured logging container indexation",t);
                    }
                }
            }
        }
    }

    /**
     * index a containerList
     *
     * @param ctnListId
     * @param user
     */
    public void indexContainerList(int ctnListId,
                                   JahiaUser user) {
        indexContainerList(ctnListId, user, false, true);
    }

    /**
     * index a containerList
     *
     * @param ctnListId
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     */
    public void indexContainerList(int ctnListId,
                                   JahiaUser user,
                                   boolean notifyCluster,
                                   boolean allowQueuing) {
        if (this.isDisabled()) {
            return;
        }
        //@todo
    }

    /**
     * index a container
     *
     * @param ctnId
     * @param user
     * @param ctx
     */
    public void indexContainer(int ctnId, JahiaUser user, RuleEvaluationContext ctx) {
        indexContainer(ctnId, user, false, true, ctx);
    }

    /**
     * index a container
     *
     * @param ctnId
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx indexation rule evaluation context, only used if allowqueuing is true
     */
    public void indexContainer(int ctnId,
                               JahiaUser user,
                               boolean notifyCluster,
                               boolean allowQueuing,
                               RuleEvaluationContext ctx) {

        if (this.isDisabled()) {
            return;
        }

        try {
            ContentContainer contentContainer = ContentContainer.getContainer(ctnId);
            if (contentContainer == null) {
                return;
            }
            JahiaContainerIndexingJob indJob = null;
            if (allowQueuing || (!allowQueuing && notifyCluster)) {
                indJob =
                        new JahiaContainerIndexingJob(ctnId, System.currentTimeMillis());

            }
            if (!allowQueuing) {
                if (notifyCluster) {
                    SearchClusterMessage msg = new SearchClusterMessage(indJob);
                    clusterService.sendMessage(msg);
                }
                if (! this.localIndexing ){
                    return;
                }
                indJob = new JahiaContainerIndexingJob(ctnId, System.currentTimeMillis());
                List<IndexableDocument> toAdd = new ArrayList<IndexableDocument>();
                List<RemovableDocument> toRemove = new ArrayList<RemovableDocument>();
                indJob.prepareBatchIndexation(toRemove,toAdd,user);
                JahiaSite site = sitesService.getSite(contentContainer.getSiteID());
                this.getSearchHandler(site.getID()).batchIndexing(toRemove,toAdd);
            } else {
                this.addIndexingJob(indJob,ctx);
            }
        } catch (Exception t) {
            logger.warn("Error indexing container", t);
        }
    }

    /**
     * Returns a list of IndexableDocument to add to the SearchHandler.addDocument(doc)
     *
     * @param ctnId
     * @param user
     */
    public List<IndexableDocument> getIndexableDocumentsForContainer(  int ctnId,
                                                    JahiaUser user ) {
        List<IndexableDocument> docs = new ArrayList<IndexableDocument>();
        try {
            ContentContainer contentContainer = ContentContainer.getContainer(ctnId);
            if (contentContainer == null) {
                return docs;
            }
            JahiaSite site = sitesService.getSite(contentContainer.getSiteID());

            // init the JahiaContentContainer
            ProcessingContext jParams = new ProcessingContext(SettingsBean.getInstance(),
                    System.currentTimeMillis(), site, user,
                    ContentPage.getPage(contentContainer.getPageID()), ProcessingContext.EDIT);
            jParams.setUrlGenerator(new BasicURLGeneratorImpl());
            JahiaContentContainerFacade containerFacade =
                    new JahiaContentContainerFacade(ctnId, LoadFlags.ALL, jParams,
                            site.getLanguageSettingsAsLocales(true), false, false);
            // remove first
            docs.add(new RemovableDocumentImpl(JahiaSearchConstant.OBJECT_KEY,
                    contentContainer.getObjectKey().getKey()));

            Iterator<JahiaContainer> it = containerFacade.getContainers();
            JahiaContainer container = null;
            boolean isMarkedForDelete = contentContainer.isMarkedForDelete();
            List<IndexableDocument> newDocs = null;
            while (it.hasNext()) {
                container = it.next();

                if (isMarkedForDelete
                        && container.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    // ignore marked for delete
                    continue;
                }
                newDocs = this.getIndexableDocuments(container, jParams);
                if ( newDocs != null ){
                    docs.addAll(newDocs);
                }
                if (!isMarkedForDelete &&
                        container.getWorkflowState() < EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    if (!containerFacade.existsEntry(container.getWorkflowState(),
                            container.getLanguageCode())) {
                        container.setVersionID(0);
                        container.setWorkflowState(EntryLoadRequest.STAGING_WORKFLOW_STATE);
                        newDocs = this.getIndexableDocuments(container, jParams);
                        if ( newDocs != null ){
                            docs.addAll(newDocs);
                        }
                    }
                }
            }
        } catch (JahiaPageNotFoundException t) {
            logger.info("Page has been deleted, cannot index container "+ctnId);
            docs.clear();
        } catch (Exception t) {
            logger.warn("Error getting indexable documents for container", t);
            docs.clear();
        }
        return docs;
    }

    /**
     * index page
     *
     * @param pageId
     * @param user
     * @param ctx
     */
    public void indexPage(int pageId, JahiaUser user, RuleEvaluationContext ctx) {
        indexPage(pageId, user, false, true, ctx);
    }

    /**
     * index page
     *
     * @param pageId
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public void indexPage(int pageId,
                          JahiaUser user,
                          boolean notifyCluster,
                          boolean allowQueuing,
                          RuleEvaluationContext ctx) {

        if (this.isDisabled()) {
            return;
        }

        try {
            ContentPage contentPage = ContentPage.getPage(pageId);
            if (contentPage == null) {
                return;
            }
            JahiaPageIndexingJob indJob = null;
            if (allowQueuing || (!allowQueuing && notifyCluster)) {
                indJob =
                        new JahiaPageIndexingJob(pageId, System.currentTimeMillis());


            }
            if (!allowQueuing) {
                if (notifyCluster) {
                    SearchClusterMessage msg = new SearchClusterMessage(indJob);
                    clusterService.sendMessage(msg);
                }
                if (! this.localIndexing ){
                    return;
                }
                JahiaSite site = sitesService.getSite(contentPage.getSiteID());
                List<IndexableDocument> docs = getIndexableDocumentsForPage(pageId,user);
                for (IndexableDocument doc : docs){
                    this.getSearchHandler(site.getID())
                            .addDocument(doc);
                }
            } else {
                this.addIndexingJob(indJob,ctx);
            }
        } catch (JahiaPageNotFoundException jpnfe) {
            // ignore
        } catch (Exception t) {
            logger.warn("Error indexing page", t);
        }
    }

    /**
     * Returns a list of IndexableDocument to add to the SearchHandler.addDocument(doc)
     *
     * @param pageId
     * @param user
     */
    public List<IndexableDocument> getIndexableDocumentsForPage(  int pageId,
                                               JahiaUser user ) {
        List<IndexableDocument> docs = new ArrayList<IndexableDocument>();

        try {
            ContentPage contentPage = ContentPage.getPage(pageId);
            if (contentPage == null) {
                return docs;
            }
            // remove first
            docs.add(new RemovableDocumentImpl(JahiaSearchConstant.OBJECT_KEY,
                    contentPage.getObjectKey().getKey()));

            ProcessingContext jParams = Jahia.getThreadParamBean();
            if ( jParams == null ){
                jParams = new ProcessingContext(SettingsBean.getInstance(),
                    System.currentTimeMillis(), contentPage.getSite(), user, contentPage.getSite().getHomeContentPage(), ProcessingContext.EDIT);
            }
            Set<ContentObjectEntryState> entryStates = contentPage.getActiveAndStagingEntryStates();
            Map<String, ContentObjectEntryState> stagedEntries = new HashMap<String, ContentObjectEntryState>();
            for (ContentObjectEntryState entryState : entryStates) {
                if (entryState.getWorkflowState() >
                        EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    stagedEntries.put(entryState.getLanguageCode(), entryState);
                }
            }
            for (ContentObjectEntryState entryState : entryStates) {
                EntryLoadRequest loadRequest = new EntryLoadRequest(entryState, false);
                if (entryState.getWorkflowState() > EntryLoadRequest.ACTIVE_WORKFLOW_STATE
                        && entryState.getVersionID() == -1) {
                    // ignore marked for delete
                    continue;
                }
                JahiaPage page = contentPage.getPage(loadRequest, ParamBean.EDIT, user);

                List<IndexableDocument> newDocs = this.getIndexableDocuments(page, loadRequest, jParams);
                if ( newDocs != null ){
                    docs.addAll(newDocs);
                }
                ContentObjectEntryState stagingEntryState = 
                        stagedEntries.get(entryState.getLanguageCode());
                if (page != null && stagingEntryState == null) {
                    stagingEntryState = new ContentObjectEntryState(
                            EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                            entryState.getLanguageCode());
                    loadRequest = new EntryLoadRequest(stagingEntryState, false);
                    newDocs = this.getIndexableDocuments(page, loadRequest, jParams);
                    if ( newDocs != null ){
                        docs.addAll(newDocs);
                    }
                }
            }
        } catch (JahiaPageNotFoundException jpnfe) {
            // ignore
            docs.clear();
        } catch (Exception t) {
            logger.warn("Error getting indexing documents for page", t);
            docs.clear();
        }
        return docs;
    }

    /**
     * Re-index a field.
     *
     * @param fieldID
     * @param user
     * @param ctx
     */
    public void indexField(int fieldID, JahiaUser user, RuleEvaluationContext ctx) {
        indexField(fieldID, user, false, true, ctx);
    }

    /**
     * Re-index a field.
     *
     * @param fieldID
     * @param user
     * @param notifyCluster
     * @param allowQueuing
     * @param ctx
     */
    public void indexField(int fieldID,
                           JahiaUser user,
                           boolean notifyCluster,
                           boolean allowQueuing,
                           RuleEvaluationContext ctx) {

        if (this.isDisabled()) {
            return;
        }
        try {
            // init the JahiaContentFieldFacade
            ContentField contentField = ContentField.getField(fieldID);
            if (contentField == null) {
                return;
            }
            JahiaFieldIndexingJob indJob = null;
            if (allowQueuing || (!allowQueuing && notifyCluster)) {
                indJob =
                        new JahiaFieldIndexingJob(fieldID, System.currentTimeMillis());

            }
            if (!allowQueuing) {
                if (notifyCluster) {
                    SearchClusterMessage msg = new SearchClusterMessage(indJob);
                    clusterService.sendMessage(msg);
                }
                if (! this.localIndexing ){
                    return;
                }
                JahiaSite site = sitesService.getSite(contentField.getSiteID());
                List<IndexableDocument> docs = getIndexableDocumentsForField(fieldID,user,false);
                for (IndexableDocument doc : docs ){
                    this.getSearchHandler(site.getID())
                            .addDocument(doc);
                }
            } else {
                this.addIndexingJob(indJob,ctx);
            }
        } catch (Exception t) {
            logger.warn("Error indexing field", t);
        }
    }

    /**
     * Returns a list of IndexableDocument to add to the SearchHandler.addDocument(doc)
     *
     * @param fieldId
     * @param applyFileFieldIndexationRule if true, apply indexation rule for file field if needed. If false,
     *                                              extract and index the file field immediately
     * @return
     */
    public List<IndexableDocument> getIndexableDocumentsForField(  int fieldId,
                                                JahiaUser user,
                                                boolean applyFileFieldIndexationRule) {
        List<IndexableDocument> docs = new ArrayList<IndexableDocument>();
        try {
            // init the JahiaContentFieldFacade
            ContentField contentField = ContentField.getField(fieldId);
            if (contentField == null) {
                return docs;
            }
            // remove first
            docs.add(new RemovableDocumentImpl(JahiaSearchConstant.FIELD_FIELDID,
                    String.valueOf(contentField.getID())));
            
            boolean isMarkedForDelete = false;
            JahiaSite site = sitesService.getSite(contentField.getSiteID());

            ProcessingContext jParams = new ProcessingContext(SettingsBean.getInstance(),
                    System.currentTimeMillis(), site, user,
                    ContentPage.getPage(contentField.getPageID()), ProcessingContext.EDIT);
            jParams.setUrlGenerator(new BasicURLGeneratorImpl());            
            List<Locale> localeList = site.getLanguageSettingsAsLocales(true);

            JahiaContentFieldFacade jahiaContentFieldFacade =
                    new JahiaContentFieldFacade(fieldId, LoadFlags.TEXTS, jParams,
                            localeList, false);
            Iterator<JahiaField> it = jahiaContentFieldFacade.getFields();
            JahiaField aField = null;
            while (it.hasNext()) {
                aField = it.next();
                isMarkedForDelete = contentField.isMarkedForDelete(aField.getLanguageCode());
                if (isMarkedForDelete && aField.getWorkflowState()
                        > EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    // ignore marked for delete
                    continue;
                }
                List<IndexableDocument> newDocs = this.getIndexableDocuments(aField, applyFileFieldIndexationRule, jParams);
                if ( newDocs != null ){
                    docs.addAll(newDocs);
                }
                if (!isMarkedForDelete && aField.getWorkflowState()
                        < EntryLoadRequest.STAGING_WORKFLOW_STATE
                        && !jahiaContentFieldFacade
                        .existsEntry(EntryLoadRequest.STAGING_WORKFLOW_STATE, aField.getLanguageCode())) {
                    aField.setVersionID(0);
                    aField.setWorkflowState(EntryLoadRequest.STAGING_WORKFLOW_STATE);
                    newDocs = this.getIndexableDocuments(aField, applyFileFieldIndexationRule, jParams);
                    if ( newDocs != null ){
                        docs.addAll(newDocs);
                    }
                }
            }
        } catch (Exception t) {
            logger.warn("Error getting indexable document for field", t);
            docs.clear();
        }
        return docs;
    }

    /**
     * Add a container to search engine.
     * With updated container, you should remove it first from search engine before
     * adding it again.
     *
     * @param container
     * @param context
     */
    protected void indexContainer(JahiaContainer container,
                                  ProcessingContext context) {
        if (this.isDisabled()) {
            return;
        }

        if (container == null)
            return;
        List<IndexableDocument> newDocs = null;
        try {
            newDocs = getIndexableDocuments(container, context);
            if (newDocs != null) {
                for (IndexableDocument doc : newDocs) {
                    this.getSearchHandler(container.getSiteID()).addDocument(
                            doc);
                }
            }
        } catch (Exception t) {
            logger.debug("Error adding container id=" + container.getID()
                    + " to search index", t);
        } finally {
        }
    }

    /**
     * Return an indexable document for a given container.
     *
     * @param container
     * @param context
     */
    protected List<IndexableDocument> getIndexableDocuments(JahiaContainer container,
                                  ProcessingContext context) {
        if (container == null)
            return null;
        List<IndexableDocument> docs = null;
        try {
            Map<String, Object> contextMap = new HashMap<String, Object>();
            contextMap.put(SOURCE_OBJECT, container);
            contextMap.put(PROCESSING_CONTEXT, context);
            this.searchIndexProcessPipeline.invoke(contextMap);
            docs = (List<IndexableDocument>) contextMap.get(INDEXABLE_DOCUMENTS);
        } catch (Exception t) {
            logger.debug("Error getting IndexableDocument from container id="
                    + container.getID(), t);
        }
        return docs;
    }

    /**
     * Index the page
     *
     * @param page
     * @param loadRequest
     * @param context
     */
    protected void indexPage(JahiaPage page,
                             EntryLoadRequest loadRequest,
                             ProcessingContext context) {
        if (this.isDisabled()) {
            return;
        }

        if (page == null)
            return;
        try {
            List<IndexableDocument> docs = getIndexableDocuments(page,loadRequest,context);
            if (docs != null) {
                for (IndexableDocument doc : docs) {
                    this.getSearchHandler(page.getJahiaID()).addDocument(doc);
                }
            }
        } catch (Exception t) {
            logger.debug("Error adding page id=" + page.getID()
                    + " to search index", t);
        }
    }

    /**
     * Return an indexable document for a given page.
     *
     * @param page
     * @param loadRequest
     * @param context
     */
    protected List<IndexableDocument> getIndexableDocuments(JahiaPage page,
                             EntryLoadRequest loadRequest,
                             ProcessingContext context) {
        if (page == null)
            return null;
        List<IndexableDocument> docs = null;
        try {
            Map<String, Object> contextMap = new HashMap<String, Object>();
            contextMap.put(SOURCE_OBJECT, page);
            contextMap.put(LOAD_REQUEST, loadRequest);
            contextMap.put(PROCESSING_CONTEXT, context);
            this.searchIndexProcessPipeline.invoke(contextMap);
            docs = (List<IndexableDocument>) contextMap.get(INDEXABLE_DOCUMENTS);
        } catch (Exception t) {
            logger.debug("Error getting IndexableDocument from page id="
                    + page.getID(), t);
        }
        return docs;
    }

    /**
     * Return an indexable document for a given field.
     *
     * @param aField
     * @param context
     * @param applyFileFieldIndexationRule if true, apply indexation rule for file field if needed. If false,
     *                                              extract and index the file field immediately
     */
    protected List<IndexableDocument> getIndexableDocuments(JahiaField aField,
                                                     boolean applyFileFieldIndexationRule,
                                                     ProcessingContext context) {
        if (aField == null) {
            return null;
        }

        List<IndexableDocument> docs = null;
        try {
            Map<String, Object> contextMap = new HashMap<String, Object>();
            contextMap.put(SOURCE_OBJECT, aField);
            contextMap.put(PROCESSING_CONTEXT, context);
            contextMap.put(APPLY_FILE_FIELD_INDEXATION_RULE, new Boolean(applyFileFieldIndexationRule));
            this.searchIndexProcessPipeline.invoke(contextMap);
            docs = (List<IndexableDocument>) contextMap.get(INDEXABLE_DOCUMENTS);
        } catch (Exception t) {
            logger.debug("Error adding field id=" + aField.getID()
                    + " to search index", t);
        }
        return docs;
    }

    //-------------------------------------------------------------------------
    //
    // Full site re-indexation methods
    //
    //-------------------------------------------------------------------------

    protected void launchSiteReindexation(){
        if ( this.isDisabled() || !this.localIndexing ){
            return;
        }
        try {
            Iterator<JahiaSite> sites = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
            SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
            while ( sites.hasNext() ){
                JahiaUser user = null;
                JahiaSite site = sites.next();
                String value = site.getSettings().getProperty(serverId + "_" +
                        BackgroundJob.JOB_STATUS,"");
                try {
                    String userKey = site.getSettings().getProperty(
                           serverId + "_" + BackgroundJob.JOB_USERKEY);
                    if ( userKey == null ){
                        continue;
                    }
                    user = ServicesRegistry.getInstance().getJahiaUserManagerService()
                            .lookupUserByKey(userKey);
                } catch ( Exception t ){
                    continue;
                }

                String jobName = site.getSettings().getProperty(serverId + "_"
                        + JahiaSiteIndexingJob.SITE_INDEXATION_JOBNAME,"");
                String interruptStatus = site.getSettings().getProperty(serverId + "_" +
                        JahiaSiteIndexingJob.INTERRUPT_STATUS,"");
                if ( BackgroundJob.STATUS_ABORTED.equals(interruptStatus) ){
                    continue;
                } else if ( BackgroundJob.STATUS_INTERRUPTED.equals(interruptStatus) ){
                    continue;
                } else if ( JahiaSiteIndexingJob.INTERRUPT_STATUS_ABORT_REQUESTED.equals(interruptStatus) ){
                    try {
                        ServicesRegistry.getInstance().getSchedulerService().interruptJob(jobName,
                            JahiaSiteIndexingJob.JOB_GROUP_NAME);
                    } catch ( Exception t ){
                        logger.debug("Exception occured when interrupting indexing job " + jobName,t);
                    }
                    Properties newSettings = new Properties();
                    newSettings.setProperty(serverId + "_" +
                            JahiaSiteIndexingJob.INTERRUPT_STATUS,BackgroundJob.STATUS_ABORTED);
                    newSettings.setProperty(serverId + "_" +
                            BackgroundJob.RESULT,BackgroundJob.STATUS_ABORTED);
                    newSettings.setProperty(serverId + "_" +
                            BackgroundJob.JOB_STATUS,BackgroundJob.STATUS_ABORTED);
                    ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
                    
                    List<String> settingsToRemove = new ArrayList<String>();
                    settingsToRemove.add(serverId + "_" + JahiaSiteIndexingJob.LAST_INDEXED_PAGE);
                    ServicesRegistry.getInstance().getJahiaSitesService().removeSiteProperties(site, settingsToRemove);
                    continue;
                }
                if ( BackgroundJob.STATUS_POOLED.equals(value)
                        || BackgroundJob.STATUS_RUNNING.equals(value)
                        || BackgroundJob.STATUS_WAITING.equals(value)){
                    JobDetail jobDetail = schedulerServ.getJobDetail(jobName,JahiaSiteIndexingJob.JOB_GROUP_NAME,true);
                    if ( jobDetail == null ){
                        // have to start reindexation
                        String startingPage = site.getSettings().getProperty(serverId + "_" +
                                JahiaSiteIndexingJob.LAST_INDEXED_PAGE,"0");
                        this.indexSite(site.getID(),user,Integer.parseInt(startingPage));
                    }
                } else if ( BackgroundJob.STATUS_INTERRUPTED.equals(value)
                        && JahiaSiteIndexingJob.INTERRUPT_STATUS_RESUME_REQUESTED.equals(interruptStatus) ){
                    //JobDetail jobDetail = schedulerServ.getJobDetail(jobName,JahiaSiteIndexingJob.JOB_GROUP_NAME,true);
                    //if ( jobDetail == null ){
                        // have to start reindexation
                        String startingPage = site.getSettings().getProperty(serverId + "_" +
                                JahiaSiteIndexingJob.LAST_INDEXED_PAGE,"0");
                        this.indexSite(site.getID(),user,Integer.parseInt(startingPage));
                    //}
                }
            }

        } catch ( Exception t ){
            logger.debug("Error checking site reindexation",t);
        }
    }

    /**
     * Site indexation.
     * Should be called under particular situations ( time consuming ).
     *
     *
     * @param siteId
     * @param user
     * @return
     * @throws JahiaException
     */
    public boolean indexSite(int siteId, JahiaUser user)
            throws JahiaException {
        return this.indexSite(siteId,user,0);
    }

    /**
     * Site indexation.
     * Should be called under particular situations ( time consuming ).
     *
     * @param siteId
     * @param user
     * @return boolean false on error.
     */
    public boolean indexSite(int siteId, JahiaUser user, int startingPage)
            throws JahiaException {

        if (this.isDisabled()) {
            return false;
        }

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(siteId);
        Properties settings = site.getSettings();
        Properties newSettings = new Properties();        
        if ( this.localIndexing ){
            String jobName = settings.getProperty(serverId + "_" + JahiaSiteIndexingJob.SITE_INDEXATION_JOBNAME,"");
            SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
            schedulerServ.deleteRamJob(jobName, JahiaSiteIndexingJob.JOB_GROUP_NAME);
            
            if ( startingPage != 0 ){
                newSettings.setProperty(serverId + "_"
                        + JahiaSiteIndexingJob.LAST_INDEXED_PAGE,String.valueOf(startingPage));
            } else {
                List<String> settingsToRemove = new ArrayList<String>();
                settingsToRemove.add(serverId + "_" + JahiaSiteIndexingJob.LAST_INDEXED_PAGE);
                ServicesRegistry.getInstance().getJahiaSitesService().removeSiteProperties(site, settingsToRemove);                
            }
            
            newSettings.setProperty(serverId + "_" + BackgroundJob.JOB_STATUS,
                    BackgroundJob.STATUS_WAITING);

            ProcessingContext jParams = Jahia.getThreadParamBean();
            boolean setMissingValue = false;
            if ( jParams == null ){
                setMissingValue = true;
                jParams = new ProcessingContext(SettingsBean.getInstance(),
                    System.currentTimeMillis(), site, user, site.getHomeContentPage(), ProcessingContext.EDIT);
            }
            JobDetail jobDetail = BackgroundJob.createJahiaJob("Site re-indexation "+ site.getSiteKey(),
                    JahiaSiteIndexingJob.class, jParams);
            if ( setMissingValue ){
                jobDetail.getJobDataMap().put(BackgroundJob.JOB_CURRENT_LOCALE, Locale.getDefault().toString());
                jobDetail.getJobDataMap().put(BackgroundJob.JOB_SCHEME, "");
                jobDetail.getJobDataMap().put(BackgroundJob.JOB_SERVERNAME, "");
                jobDetail.getJobDataMap().put(BackgroundJob.JOB_PARAMETER_MAP, jParams.getParameterMap());
                jobDetail.getJobDataMap().put(BackgroundJob.JOB_SERVERPORT, "0");
                jobDetail.getJobDataMap().put(BackgroundJob.JOB_OPMODE, ParamBean.EDIT);
            }

            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.SITE_INDEXATION_JOBNAME,
                    jobDetail.getName());
            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.JOB_USERKEY,
                    user.getUserKey());
            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,"");
            ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
            /*
            JobDetail jobDetail =
                new JobDetail(JahiaSiteIndexingJob.JOB_NAME_PREFIX + siteId,
                        JahiaSiteIndexingJob.JOB_GROUP_NAME, JahiaSiteIndexingJob.class);*/
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_SITEKEY, site.getSiteKey());
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_USERKEY, user.getUserKey());
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_TYPE,JahiaSiteIndexingJob.SITE_INDEXATION_JOB_TYPE);
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_SCHEDULED,  System.currentTimeMillis()); //scheduled now
            jobDetail.getJobDataMap().put(BackgroundJob.JOB_STATUS, BackgroundJob.STATUS_WAITING);

            SimpleTrigger trigger = new SimpleTrigger(JahiaSiteIndexingJob.TRIGGER_NAME_PREFIX + siteId,
                    SchedulerService.SCHEDULED_TRIGGER_GROUP);
            trigger.setVolatility(true);
            schedulerServ.unscheduleRamJob(jobDetail);
            schedulerServ.deleteRamJob(jobDetail.getName(), JahiaSiteIndexingJob.JOB_GROUP_NAME);
            schedulerServ.scheduleRamJob(jobDetail, trigger);
        } else {
            newSettings.setProperty(serverId + "_" + BackgroundJob.JOB_STATUS,
                    BackgroundJob.STATUS_POOLED);
            newSettings.setProperty(serverId + "_"
                    + BackgroundJob.JOB_USERKEY,user.getUserKey());
            newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,"");
            ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
        }
        return true;

        /*
        boolean result = false;
        // start the chrono...
        long startTime = JahiaChrono.getInstance().start();

        // remove all object with siteId
        this.removeFromSearchEngine(siteId, JahiaSearchConstant.JAHIA_ID,
                NumberPadding.pad(siteId), user);

        // index containers
        result = indexeSiteContainers(siteId, user);

        if (!result) {
            return result;
        }

        // index pages
        result = indexeSitePages(siteId, user);

        if (!result) {
            return result;
        }

        // index containerlists
        result = indexeSiteContainerLists(siteId, user);

        if (!result) {
            return result;
        }

        logger.info("indexing time ["
                + JahiaChrono.getInstance().read(startTime) +
                "ms]");

        this.indexFileRepository(siteId,user);

        return result;
        */
    }

    public boolean indexeSiteContainers(int siteId, JahiaUser user)
            throws JahiaException {

        for (Integer Id : containersService.getCtnIds(siteId)) {
            try {
                RuleEvaluationContext ctx = new RuleEvaluationContext(ContentContainerKey.getInstance(ContentContainerKey.CONTAINER_TYPE +
                        "_"+Id.toString()),null,Jahia.getThreadParamBean(),user);            
                this.indexContainer(Id.intValue(), user, ctx);
            } catch ( Exception t ){
                logger.debug("Error on calling index container service",t);
            }
        }
        return true;
    }

    public boolean indexeSiteContainerLists(int siteId, JahiaUser user) {
        // @todo, what to index for container list. Metadata ?
        return true;
    }

    /**
     * Perform an index optimization for a given site.
     *
     * @param siteID
     * @return boolean false on error.
     */
    public boolean optimizeIndex(int siteID) {
        return true;
    }

    //-------------------------------------------------------------------------
    //
    // Search methods
    //
    //-------------------------------------------------------------------------
    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public SearchResult search(String[] queries,
                               String[] searchHandlers,
                               ProcessingContext jParams)
            throws JahiaException {
        return search(queries, searchHandlers, jParams, null);
    }

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
    public SearchResult search(String[] queries,
                               String[] searchHandlers,
                               ProcessingContext jParams,
                               JahiaAbstractHitCollector hitCollector)
            throws JahiaException {
        return search(queries, searchHandlers, Collections.<String>emptyList(), jParams, new SearchResultImpl(), null, hitCollector);
    }

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
    public SearchResult search(String[] queries,
                               String[] searchHandlers,
                               List<String> languageCodes,
                               ProcessingContext jParams,
                               JahiaAbstractHitCollector hitCollector)
            throws JahiaException {
        return search(queries, searchHandlers, languageCodes, jParams, new SearchResultImpl(), null, hitCollector);
    }    
    
    public SearchResult search(String[] queries, String[] searchHandlers,
            ProcessingContext jParams,
            JahiaSearchResultBuilder searchResultBuilder, Sort sort)
            throws JahiaException {
        return search(queries, searchHandlers,
                Collections.<String> emptyList(), jParams, searchResultBuilder
                        .getSearchResult(), sort, null);
    }

    public SearchResult search(String[] queries, String[] searchHandlers,
            List<String> languageCodes, ProcessingContext jParams,
            JahiaSearchResultBuilder searchResultBuilder, Sort sort)
            throws JahiaException {
        return search(queries, searchHandlers, languageCodes, jParams,
                searchResultBuilder.getSearchResult(), sort, null);
    }    

    /**
     * search on multiple search handlers
     *
     * @param queries
     * @param searchHandlers
     * @param jParams
     * @param sort
     * @return
     * @throws JahiaException
     */
    private SearchResult search(String[] queries,
                               String[] searchHandlers,
                               List<String> languageCodes,
                               ProcessingContext jParams,
                               SearchResult result,
                               Sort sort, 
                               JahiaAbstractHitCollector hitCollector)
            throws JahiaException {
        logger.debug("Started");
        if (jParams != null){
            if (Jahia.getThreadParamBean()==null){
                // for right access, we need to ParamBean
                Jahia.setThreadParamBean(jParams);
            }
        }
        if (searchHandlers.length == 1){
            SearchHandler searchHandler = this.getSearchManager().getSearchHandler(searchHandlers[0]);
            if (searchHandler == null){
                return result;
            }
            if (queries.length == 1) {
                if (hitCollector == null) {
                    searchHandler.search(queries[0], languageCodes, result, sort);
                } else {
                    searchHandler.search(queries[0], languageCodes, result, hitCollector);
                }
            } else {
                List<String> queryList = new ArrayList<String>(Arrays.asList(queries));
                String query = (String)queryList.remove(0);
                if (hitCollector == null) {
                    searchHandler.search(query, languageCodes, result, (String[]) queryList
                            .toArray(new String[queryList.size()]), sort, null);
                } else {
                    searchHandler.search(query, languageCodes, result, (String[]) queryList
                            .toArray(new String[queryList.size()]),
                            hitCollector);
                }
            }
        } else {
        MultiReader reader = null;
        Set<LuceneQueryRequest> openRequests = new HashSet<LuceneQueryRequest>();
        try {
            LuceneSearchHandlerImpl currentSiteSearchHandler = null;
            LuceneSearchHandlerImpl searchHandler = null;
            List<IndexReader> indexReadersList = new ArrayList<IndexReader>();
            for (int i = 0; i < searchHandlers.length; i++) {
                searchHandler = (LuceneSearchHandlerImpl) this
                        .getSearchManager().getSearchHandler(searchHandlers[i]);
                if (searchHandler.getName().equals(jParams.getSiteKey())){
                    currentSiteSearchHandler = searchHandler;
                }
                LuceneQueryRequest req = new LuceneQueryRequest(searchHandler
                        .getCoreSearcher());
                JahiaIndexSearcher searcher = req.getSearcher();
                openRequests.add(req);
                try {
                    indexReadersList.add(searcher.getReader());
                } catch (Exception t) {
                    logger.debug(t);
                }
            }

            addReferenceToOpenLuceneQueryRequests(openRequests);

            IndexReader[] readers = new IndexReader[] {};
            readers = (IndexReader[]) indexReadersList.toArray(readers);

            reader = new MultiReader(readers);

            if (currentSiteSearchHandler != null){
                searchHandler = currentSiteSearchHandler;                
            }
            if (queries.length == 1) {
                searchHandler.search(queries[0], languageCodes, result, null, sort, reader, hitCollector);
            } else {
                List<String> queryList = new ArrayList<String>(Arrays.asList(queries));
                String query = (String)queryList.remove(0);
                searchHandler.search(query, languageCodes, result, (String[]) queryList
                        .toArray(new String[queryList.size()]), null, sort, reader, hitCollector);
            }
        } catch (Exception t) {
            logger.warn(t);
        } finally {
// Removed as closing the multi-reader also closes the underlying readers, which should not
// be closed as they are shared by all threads. Closing the request will simply decrement 
// reference, what should be enough. For now not closing the multi-reader is not causing
// memory leaks, but it perhaps could in future.            
//            if ( reader != null ){
//                try {
//                    reader.close();
//                } catch ( Exception t ){
//                }
//            }
            // Removed as releasing references to opened lucene query request is done
            // in finally clause of Jahia.service() method
            /*
            for (Iterator it = openRequests.iterator(); it.hasNext();) {
                ((LuceneQueryRequest)it.next()).close();
            }*/
        }
        }
        return result;
    }

    /**
     * Return a List of matching pages.
     * Perform a search for a given query ( valid lucene query ).
     *
     * @param siteId
     * @param queryString
     * @param jParams             to check read access.
     * @param languageCodes       language codes in which language to search.
     * @param searchResultBuilder
     * @return
     * @throws JahiaException
     */
    public JahiaSearchResult search(int siteId,
                                    String queryString,
                                    ProcessingContext jParams,
                                    List<String> languageCodes,
                                    JahiaSearchResultBuilder searchResultBuilder)
            throws JahiaException {

        SearchHandler searchHandler = this.getSearchHandler(siteId);
        JahiaSearchResult result = null;
        if (searchHandler != null) {
            result = search(new String[]{searchHandler.getName()},
                    queryString, jParams, languageCodes, searchResultBuilder);
        }
        if (result == null) {
            result = executeURLModificationRules(new JahiaSearchResult(searchResultBuilder), jParams);
        }
        return result;
    }

    /**
     * @param searchHandlers
     * @param queryString
     * @param jParams
     * @param languageCodes
     * @param searchResultBuilder
     * @return
     * @throws JahiaException
     */
    public JahiaSearchResult search(String[] searchHandlers,
                                    String queryString,
                                    ProcessingContext jParams,
                                    List<String> languageCodes,
                                    JahiaSearchResultBuilder searchResultBuilder)
            throws JahiaException {
        return search(searchHandlers, queryString, null, jParams,languageCodes, searchResultBuilder);
    }
    /**
     * @param searchHandlers
     * @param queryString
     * @param jParams
     * @param languageCodes
     * @param searchResultBuilder
     * @return
     * @throws JahiaException
     */
    public JahiaSearchResult search(String[] searchHandlers,
                                    String queryString, String jcrQueryString,
                                    ProcessingContext jParams,
                                    List<String> languageCodes,
                                    JahiaSearchResultBuilder searchResultBuilder)
            throws JahiaException {

        PageSearcher pageSearcher = new PageSearcher(searchHandlers,
                languageCodes);
        pageSearcher.setSearchResultBuilder(searchResultBuilder);
        JahiaSearchResult result = pageSearcher.search(queryString, jcrQueryString, jParams);

        if (result == null) {
            result = executeURLModificationRules(new JahiaSearchResult(searchResultBuilder), jParams);
        }
        return result;
    }
    
    protected static JahiaSearchResult executeURLModificationRules(
            JahiaSearchResult searchResult, ProcessingContext jParams) {
        Map<String, Object> globals = new HashMap<String, Object>();
        globals.put("processingContext", jParams);
        globals.put("urlService", URLService.getInstance());        
        RulesListener.getInstance("jahia").executeRules((Collection<?>)searchResult.results(),
                globals);
        return searchResult;
    }

    public void addShutdownable(String name,Shutdownable s){
        this.shutdownables.put(name,s);
    }

    /**
     *
     * @param savedSearch
     * @param searchViewHandler
     * @param context
     * @throws JahiaException
     */
    public void saveSearch(JahiaSavedSearch savedSearch,
                           SearchViewHandler searchViewHandler,
                           ProcessingContext context)
    throws JahiaException {
        try {
            if ( savedSearch.getId().intValue() == -1){
                String search = searchViewHandler.getSaveSearchDoc(context);
                savedSearch.setSearch(search);
                savedSearch.setOwnerKey(context.getUser().getUserKey());
                savedSearch.setCreationDate(new Long(System.currentTimeMillis()));
                savedSearch.setSearchViewHandlerClass(searchViewHandler.getClass().getName());
                savedSearch.setSideId(context.getSiteID());
                JahiaBaseACL acl = new JahiaBaseACL();
                acl.create(0);
                savedSearch.setAcl(acl);
            } else {
                String search = searchViewHandler.getSaveSearchDoc(context);
                savedSearch.setSearch(search);
            }
            savedSearchManager.saveSearch(savedSearch);
        } catch ( Exception e ){
            throw new JahiaException("Error saving search","Error saving search",
                    JahiaException.DATA_ERROR,JahiaException.ERROR_SEVERITY, e);
        }
    }

   public void saveSearch(JahiaSavedSearch savedSearch) throws JahiaException {
        savedSearchManager.saveSearch(savedSearch);
    }

    /**
     * Delete the given saved search
     * 
     * @param id
     * @throws JahiaException
     */
    public void deleteSearch(int id)
    throws JahiaException {
        savedSearchManager.deleteSearch(new Integer(id));
    }

    /**
     * Returns all saved searchs
     *
     * @return
     * @throws JahiaException
     */
    public List<JahiaSavedSearch> getSavedSearches()
    throws JahiaException {
        return savedSearchManager.getSearches();
    }
    
    @Override
    public List<JahiaSavedSearch> getSavedSearches(JahiaUser owner) throws JahiaException {
        return savedSearchManager.getSearches(owner.getUserKey());
    }

    /**
     * Returns the saved search for the given title
     *
     * @return JahiaSavedSearch
     * @throws JahiaException
     */
    public JahiaSavedSearch getSavedSearch(String title) throws JahiaException {
        return savedSearchManager.getSavedSearch(title);
    }    
    

    public void removeShutdownable(String name){
        this.shutdownables.remove(name);
    }

    public class IndexingJobConsummerStarter implements Runnable {

        private boolean shutdowned = false;

        public IndexingJobConsummerStarter() {
            logger.info("Starting IndexingJobConsummer ");
        }

        public void run() {
            JobDetail jobDetail = null;
            try {
                jobDetail = schedulerService.getJobDetail(IndexingJobConsummer.JOB_NAME,
                        SchedulerService.SYSTEM_JOB_GROUP);
            } catch (Exception t) {
            }
            while (!shutdowned && jobDetail == null) {
                try {
                    Thread.sleep(5000L);
                } catch (Exception t) {
                }
                try {
                    jobDetail = schedulerService.getJobDetail(IndexingJobConsummer.JOB_NAME,
                            SchedulerService.SYSTEM_JOB_GROUP);
                } catch (Exception t) {
                }
                if (jobDetail == null) {
                    long startTime = System.currentTimeMillis() + 20000L;
                    jobDetail =
                            new JobDetail(IndexingJobConsummer.JOB_NAME,
                                    SchedulerService.SYSTEM_JOB_GROUP, IndexingJobConsummer.class);
                    SimpleTrigger trigger =
                            new SimpleTrigger(IndexingJobConsummer.TRIGGER_NAME,
                                    SchedulerService.REPEATED_TRIGGER_GROUP, new Date(startTime),
                                    null, SimpleTrigger.REPEAT_INDEFINITELY, 30L * 60L * 1000L);
                    jobDetail.setDurability(false);
                    jobDetail.setRequestsRecovery(false);
                    jobDetail.setVolatility(true);
                    trigger.setVolatility(true);
                    try {
                        schedulerService.scheduleRamJob(jobDetail, trigger);
                    } catch (Exception t) {
                        logger.debug("Exception scheduling IndexingJobConsummerJob", t);
                    }
                }
            }
        }

        public void shutdown(){
            this.shutdowned = true;
        }
    }

    public void messageReceived(ClusterMessage clusterMessage) {
        if (clusterMessage instanceof SearchClusterMessage) {
            Object msg = clusterMessage.getObject();
            if (msg instanceof IndexUpdatedMessage){
                IndexUpdatedMessage indexUpdatedMsg = (IndexUpdatedMessage) clusterMessage.getObject();
                if (indexUpdatedMsg.getServerId().equals(getIndexationConfig()
                            .getProperty(JahiaSearchConfigConstant.SEARCH_INDEXER_SERVER_ID))) {
                    SearchHandler searchHandler = this.getSearchManager()
                            .getSearchHandler(
                                    indexUpdatedMsg.getSearchHandlerName());
                    if (searchHandler instanceof LuceneSearchHandlerImpl) {
                        try {
                            ((LuceneSearchHandlerImpl) searchHandler)
                                    .getCoreSearcher().getSearcher(true, false,
                                            null);
                        } catch (Exception e) {
                            logger.warn("Failed to reopen index searcher/reader", e);
                        }
                    }
                }
            } else if (msg instanceof SynchronizedIndexationRequestMessage){
                SynchronizedIndexationRequestMessage synchIndexMsg =
                        (SynchronizedIndexationRequestMessage) clusterMessage.getObject();
                this.synchronizedIndexation(synchIndexMsg);
            } else if (msg instanceof SynchronizedIndexationResponseMessage){
                SynchronizedIndexationResponseMessage synchIndexMsg =
                        (SynchronizedIndexationResponseMessage) clusterMessage.getObject();
                this.notifySynchronizedIndexationTasks(synchIndexMsg);
            } else if ( msg instanceof JahiaIndexingJob ){
                if (localIndexing) {
                    JahiaIndexingJob job = (JahiaIndexingJob) msg;
                    job.execute(null);
                }
            }
        }
    }

    private void synchronizedIndexation(SynchronizedIndexationRequestMessage msg){
        JahiaIndexingJob indexingJob = msg.getIndexingJob();
        this.synchronizedIndexation(indexingJob);
        SynchronizedIndexationResponseMessage responseMsg = new SynchronizedIndexationResponseMessage(indexingJob,
                this.serverId);
        SearchClusterMessage clusterMsg = new SearchClusterMessage(responseMsg);
        this.clusterService.sendMessage(clusterMsg);
    }

    private void notifySynchronizedIndexationTasks(SynchronizedIndexationResponseMessage msg){
        synchronized(this.synchronizedIndexationTask){
            List<SynchronizedIndexationTask> l = new ArrayList<SynchronizedIndexationTask>();
            for (SynchronizedIndexationTask task : synchronizedIndexationTask){
                task.messageReceived(msg);
                if (!task.isTaskIsPending()){
                    l.add(task);
                }
            }
            this.synchronizedIndexationTask = l;
        }
    }

    public void memberJoined(Address address) {
        //To change body of implemented methods use File | Settings | File Templates.
        logger.info("Joined member " + address.toString());
    }

    public void memberLeft(Address address) {
        //To change body of implemented methods use File | Settings | File Templates.
        logger.info("Left member " + address.toString());

    }

    public List<String> getFieldsToCopyToSearchHit() {
        return fieldsToCopyToSearchHit;
    }

    public void setFieldsToCopyToSearchHit(List<String> fieldsToCopyToSearchHit) {
        this.fieldsToCopyToSearchHit = fieldsToCopyToSearchHit;
    }

    public List<String> getFieldsToExcludeFromHighlighting() {
        return fieldsToExcludeFromHighlighting;
    }

    public void setFieldsToExcludeFromHighlighting(List<String> fieldsToExcludeFromHighlighting) {
        this.fieldsToExcludeFromHighlighting = fieldsToExcludeFromHighlighting;
    }

    public Map<String, Set<String>> getFieldsGrouping() {
        if (fieldsGrouping == null || this.searchScoreBoostRefreshDelayTime != 0) {
            checkFieldsGrouping();            
        }
        return fieldsGrouping;
    }

    public void setSavedSearchViewSettings(Map<String, JahiaSavedSearchViewSettings> savedSearchViewSettings) {
        this.savedSearchViewSettings = savedSearchViewSettings;
    }

    private void checkFieldsGrouping(){
        long now = System.currentTimeMillis();
        boolean semaphoreAcquired = false;
        if (now - this.fieldsScoreBoostLastUpdateTime > this.searchScoreBoostRefreshDelayTime) {
            try {
                if (fieldsGrouping == null || fieldsGrouping.isEmpty()) {
                    fieldScoreSemaphore.acquire();
                    semaphoreAcquired = true;
                    if (fieldsGrouping == null || fieldsGrouping.isEmpty()) {
                        initSearchFieldConfiguration(0);
                        this.fieldsScoreBoostLastUpdateTime = System.currentTimeMillis();
                    }
                } else {
                    if (fieldScoreSemaphore.tryAcquire()) {
                        semaphoreAcquired = true;
                        initSearchFieldConfiguration(0);
                        this.fieldsScoreBoostLastUpdateTime = System.currentTimeMillis();
                    }
                }
            } catch (InterruptedException e) {
                logger.info("FieldScoreBoost-Semaphore interrupted", e);
            } finally {
                if (semaphoreAcquired) {
                    fieldScoreSemaphore.release();
                }
            }
        }
    }

    /**
     * Return a JahiaCompassHighlighter instance
     * @param searchEngineHighlighter
     * @param resource
     * @return
     */
    public JahiaCompassHighlighter getCompassHighlighter(
            SearchEngineHighlighter searchEngineHighlighter,
            Resource resource){
        JahiaCompassHighlighter highlighter =
                new JahiaCompassHighlighter(searchEngineHighlighter,resource);
        return highlighter;
    }

    /**
     * Re-index the whole file repository (slide) of a site
     *
     * @param siteId
     * @param user
     * @throws JahiaException
     */
    public void indexFileRepository(int siteId, JahiaUser user)
    throws JahiaException {
        if (! this.localIndexing || !Boolean.parseBoolean(getConfig().getProperty(JahiaSearchConfigConstant.ENABLE_FILE_INDEXING))){
            return;
        }
    }

    /**
     *
     * @param jahiaSearchHit
     * @param jParams
     * @param serverURL
     * @return
     * @throws JahiaException
     */
    public SyndEntry getSyndEntry(JahiaSearchHit jahiaSearchHit, ParamBean jParams, String serverURL)
    throws JahiaException {

        Hit hit = null;
        if (jahiaSearchHit.getType()==JahiaSearchHit.WEBDAVFILE_TYPE){
            hit = new FileHit(jahiaSearchHit,jParams);
        } else if (jahiaSearchHit.getType()==JahiaSearchHit.PAGE_TYPE) {
            hit = new PageHit(jahiaSearchHit,jParams);
        } else if (jahiaSearchHit.getType()==JahiaSearchHit.CONTAINER_TYPE) {
            try {
                ContentContainer contentContainer = ContentContainer
                        .getContainer(jahiaSearchHit.getSearchHitObjectKey().getIdInType());
                JahiaContainer jahiaContainer = contentContainer.getJahiaContainer(jParams,jParams.getEntryLoadRequest());
                if (jahiaContainer != null){
                    hit = new ContainerHit(jahiaSearchHit,jahiaContainer,jParams);
                }
            } catch ( Throwable t ){
                logger.debug("Exception occured retrieving JahiaContainer from hit "
                        + jahiaSearchHit.getSearchHitObjectKey());
            }
        }
        return getSyndEntry(hit, jParams, serverURL);
    }

    /**
     *
     * @param hit
     * @param jParams
     * @param serverURL
     * @return
     * @throws JahiaException
     */
    public SyndEntry getSyndEntry(Hit hit, ParamBean jParams, String serverURL)
    throws JahiaException {
        if (hit == null || jParams == null){
            return null;
        }
        SyndContentImpl description = new SyndContentImpl();
        description.setType("text/html");
        SyndEntry entry = new SyndEntryImpl();
        entry.setTitle(hit.getTitle());
        entry.setLink(serverURL+hit.getLink());
        String highlightedText = hit.getSummary();
        if (isEmtyOrNullString(highlightedText)){
            highlightedText = "";
        }
        description.setValue(JahiaTools.stripNonValidXMLCharacters(highlightedText));
        entry.setDescription(description);
        if (!isEmtyOrNullString(hit.getCreatedBy())){
            entry.setAuthor(hit.getCreatedBy());
        }
        if (hit.getCreated() != null){
            entry.setPublishedDate(hit.getCreated());
        }
        if (hit.getLastModified()!= null){
            entry.setUpdatedDate(hit.getLastModified());
        } else if (hit.getCreated()!= null){
            entry.setUpdatedDate(hit.getCreated());
        }
        List<String> contributors = new ArrayList<String>();
        if (!isEmtyOrNullString(hit.getLastModifiedBy())){
            contributors.add(hit.getLastModifiedBy());
        } else if (!isEmtyOrNullString(hit.getCreatedBy())){
            contributors.add(hit.getCreatedBy());
        }
        entry.setContributors(contributors);
        String content = hit.getContent();
        if (!isEmtyOrNullString(content)){
            List<SyndContent> contents = new ArrayList<SyndContent>();
            SyndContentImpl syndContent = new SyndContentImpl();
            if (content.length()>1000){
                content = content.substring(0,1000);
            }
            syndContent.setValue(JahiaTools.stripNonValidXMLCharacters(content));
            contents.add(syndContent);
            entry.setContents(contents);
        }
        return entry;
    }

    private boolean isEmtyOrNullString(String str){
        return str == null || "".equals(str.trim());
    }

    private JahiaUser getAdminUser(int siteId){
        JahiaGroup adminGroup = groupManagerService.lookupGroup(siteId, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
        Set<Principal> members = adminGroup.getRecursiveUserMembers();
        if ( members.iterator().hasNext() ){
            return (JahiaUser)members.iterator().next();
        }
        return null;
    }

    public void initSearchFieldConfiguration(int siteId) {

        String value = config.getProperty(JahiaSearchConfigConstant.METADATA_SCOREBOOST, "1.0");
        float metadataScoreBoost = NumberUtils.toFloat(value, 1.0f);

        String numericAnalyzer = getIndexationConfig()
                .getProperty(JahiaSearchConfigConstant.ANALYZER_FOR_NUMERICS);
        String keywordAnalyzer = getIndexationConfig()
                .getProperty(JahiaSearchConfigConstant.ANALYZER_FOR_KEYWORDS);
        CompassConfiguration compassConfig = getCompass().getConfig();
        LuceneSearchEngineFactory searchEngineFactory = (LuceneSearchEngineFactory) ((InternalCompass) getCompass())
                .getSearchEngineFactory();
        CompassMapping compassMapping = searchEngineFactory.getMapping();
        ResourceMapping[] compassMappings = compassMapping.getRootMappings();
        NodeTypeRegistry ntRegistry = NodeTypeRegistry.getInstance();
        boolean compassConfigChanged = false;
        
        Map<String, Set<String>> newFieldsGrouping = siteId > 0 && fieldsGrouping != null ? new HashMap<String, Set<String>>(fieldsGrouping) : new HashMap<String, Set<String>>();
        
        try {
            if (newFieldsGrouping.isEmpty()) {
                addDefaultGroups(newFieldsGrouping, 0);
                if (siteId == 0) {
                    for (int currentSiteId : sitesService.getSiteIds()) {
                        addDefaultGroups(newFieldsGrouping, currentSiteId);
                    }
                }
            }
            if (siteId > 0) {
                if (fieldsGrouping != null) {
                    for (String key : fieldsGrouping.keySet()) {
                        if (key.startsWith(siteId + "_")) {
                            newFieldsGrouping.remove(key);
                        }
                    }
                }
                addDefaultGroups(newFieldsGrouping, siteId);
            } 

            for (Integer currentID : siteId > 0 ? fieldService.getAllFieldDefinitionIDs(siteId) : fieldService.getAllFieldDefinitionIDs()) {
                JahiaFieldDefinition def = fieldService
                        .loadFieldDefinition(currentID.intValue());
                float scoreBoost = 0;
                String analyzer = null;
                int indexMode = ExtendedPropertyDefinition.INDEXED_NO;
                if (def != null) {
                    String ntDefinition = def.getCtnType();
                    boolean isDefault = true;
                    ExtendedPropertyDefinition propertyDef = null;
                    if (ntDefinition != null) {
                        int index = ntDefinition.indexOf(" ");
                        String nodeType = ntDefinition.substring(0, index);
                        String propertyName = ntDefinition.substring(index + 1);

                        propertyDef = ntRegistry
                                .getNodeType(nodeType).getPropertyDefinition(
                                        propertyName);
                    }
                    if (propertyDef != null) {                    
                        scoreBoost = (float) propertyDef.getScoreboost();
                        analyzer = propertyDef.getAnalyzer();
                        indexMode = propertyDef.getIndex();
                        if (analyzer != null || scoreBoost != 1
                                || indexMode != 1) {
                            isDefault = false;
                        }
                        if (analyzer == null) {
                                if (propertyDef.getRequiredType() == PropertyType.DATE
                                        || propertyDef.getRequiredType() == PropertyType.BOOLEAN
                                        || propertyDef.getRequiredType() == PropertyType.DOUBLE || propertyDef
                                        .getRequiredType() == PropertyType.LONG) {
                            analyzer = numericAnalyzer;
                            indexMode = ExtendedPropertyDefinition.INDEXED_UNTOKENIZED;
                            }
                        }
                    } 
                    
                    String name;
                    boolean isMetadata = def.getIsMetadata();
                    String lowerCaseName = def.getName().toLowerCase();
                    
                    if (isMetadata) {
                        name = JahiaSearchConstant.METADATA_PREFIX
                                + lowerCaseName;
                    } else {
                        if (def.getCtnType() != null) {
                            lowerCaseName = def.getCtnType().replace(':', '_')
                                    .toLowerCase();
                            String key = JahiaSearchConstant.CONTAINER_FIELD_PREFIX + lowerCaseName.split(" ")[0];
                            lowerCaseName = lowerCaseName.replace(' ', '_');
                            name = JahiaSearchConstant.CONTAINER_FIELD_PREFIX
                                    + lowerCaseName;
                            addToFieldsGroup(newFieldsGrouping, key, name, def
                                    .getJahiaID());
                        } else {
                            name = JahiaSearchConstant.CONTAINER_FIELD_PREFIX
                                + lowerCaseName;
                        }
                    }

                    if (isMetadata && (scoreBoost == 0 || scoreBoost == 1)) {
                        scoreBoost = metadataScoreBoost;
                    }

                    if (analyzer != null
                            || scoreBoost != 1
                            || indexMode != ExtendedPropertyDefinition.INDEXED_TOKENIZED) {
                        if (updateCompassMappings(compassMappings, name,
                                isDefault, scoreBoost, analyzer, indexMode)) {
                            if (!compassConfigChanged) {
                                compassConfigChanged = true;
                            }
                        }
                        for (String aliasName : def.getAliasNames()){
                            if (updateCompassMappings(compassMappings, JahiaSearchConstant.CONTAINER_FIELD_ALIAS_PREFIX
                                    + aliasName.toLowerCase(),
                                    isDefault, scoreBoost, analyzer, indexMode)) {
                                if (!compassConfigChanged) {
                                    compassConfigChanged = true;
                                }
                            }                            
                        }
                    }
                    
                    if (propertyDef != null) {
                        if (propertyDef.isFacetable()) {
                            if (updateCompassMappings(
                                    compassMappings,
                                    JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX
                                            + lowerCaseName,
                                    isDefault,
                                    scoreBoost,
                                    keywordAnalyzer,
                                    ExtendedPropertyDefinition.INDEXED_UNTOKENIZED)) {
                                if (!compassConfigChanged) {
                                    compassConfigChanged = true;
                                }
                            }
                        }
                        if (propertyDef.isSortable()) {
                            if (updateCompassMappings(
                                    compassMappings,
                                    JahiaSearchConstant.CONTAINER_FIELD_FACET_PREFIX
                                            + lowerCaseName,
                                    isDefault,
                                    scoreBoost,
                                    keywordAnalyzer,
                                    ExtendedPropertyDefinition.INDEXED_UNTOKENIZED)) {
                                if (!compassConfigChanged) {
                                    compassConfigChanged = true;
                                }
                            }
                        }
                    }

                    if (indexMode != ExtendedPropertyDefinition.INDEXED_NO 
                            && (def.getItemDefinition() != null
                            && def.getType() != FieldTypes.DATE
                            && def.getType() != FieldTypes.BOOLEAN
                            && def.getType() != FieldTypes.FLOAT
                            && def.getType() != FieldTypes.INTEGER
                            && def.getType() != FieldTypes.COLOR
                            && def.getType() != FieldTypes.APPLICATION && 
                               (propertyDef == null || !Boolean.FALSE
                                    .equals(propertyDef.getFulltextSearchable())))
                            || (propertyDef != null && Boolean.TRUE
                                    .equals(propertyDef.getFulltextSearchable()))) {
                        String key = isMetadata ? JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD
                                : JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD;

                        addToFieldsGroup(newFieldsGrouping, key, name, def
                                .getJahiaID());
                        addToFieldsGroup(newFieldsGrouping,
                                JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD,
                                name, def.getJahiaID());
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(
                    "Error creating Lucene field groups and score boost data",
                    e);
        } finally {
            if (compassConfigChanged) {
                for (ResourceMapping resourceMapping : compassMappings) {
                    if (resourceMapping instanceof RawResourceMapping) {
                        ((RawResourceMapping) resourceMapping).postProcess();
                    }
                    compassConfig.removeMappingByAlias(resourceMapping.getAlias());
                    compassConfig.addResourceMapping(resourceMapping);
                }
                getCompass().rebuild();
            }
        }

        synchronized (fieldScoreLock) {
            fieldsGrouping = newFieldsGrouping;
        }
    }
    
    private void addDefaultGroups (Map<String, Set<String>> newFieldGrouping, int siteId) {
        Set<String> l = new HashSet<String>();
        l.add(JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE);
        l.add(JahiaSearchConstant.TITLE);                    
        newFieldGrouping.put((siteId > 0 ? siteId + "_" : "") + JahiaSearchConstant.ALL_FULLTEXT_SEARCH_FIELD, l);
        
        l = new HashSet<String>();
        l.add(JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE);
        l.add(JahiaSearchConstant.TITLE);
        newFieldGrouping.put((siteId > 0 ? siteId + "_" : "") + JahiaSearchConstant.CONTENT_FULLTEXT_SEARCH_FIELD, l);
        
        l = new HashSet<String>();
        l.add(JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD_FOR_QUERY_REWRITE);
        newFieldGrouping.put((siteId > 0 ? siteId + "_" : "") + JahiaSearchConstant.METADATA_FULLTEXT_SEARCH_FIELD, l);
    }

    private boolean updateCompassMappings(ResourceMapping[] compassMappings,
            String fieldName, boolean isDefault, float scoreBoost,
            String analyzer, int indexMode) {
        boolean mappingChanged = false;
        for (ResourceMapping compassMapping : compassMappings) {
            Mapping propertyMapping = compassMapping.getMapping(fieldName);
            boolean newMapping = false;
            if (propertyMapping == null) {
                newMapping = true;
                mappingChanged = true;
                propertyMapping = new RawResourcePropertyMapping();
                propertyMapping.setName(fieldName);
                propertyMapping.setPath(new StaticPropertyPath(fieldName));
            }
            if (propertyMapping instanceof ResourcePropertyMapping) {
                RawResourcePropertyMapping rawMapping = (RawResourcePropertyMapping) propertyMapping;
                rawMapping.setOverrideByName(true);
                if (rawMapping.getAnalyzer() == null && analyzer != null
                        || (rawMapping.getAnalyzer() != null && !rawMapping.getAnalyzer().equals(analyzer))
                        || rawMapping.getBoost() != scoreBoost
                        || !rawMapping.getIndex().equals(getIndexMode(indexMode))) {
                    rawMapping.setAnalyzer(analyzer);
                    rawMapping.setBoost(scoreBoost);
                    rawMapping.setIndex(getIndexMode(indexMode));
                    mappingChanged = true;
                }
                if (newMapping) {
                    compassMapping.addMapping(propertyMapping);
                }
            }
        }
        return mappingChanged;
    }

    private Index getIndexMode(int indexMode) {
        Index index = null;
        switch (indexMode) {
        case ExtendedPropertyDefinition.INDEXED_NO:
            index = Index.NO;
            break;
        case ExtendedPropertyDefinition.INDEXED_UNTOKENIZED:
            index = Index.UN_TOKENIZED;
            break;
        default:
            index = Index.TOKENIZED;
            break;

        }
        return index;
    }

    private void addToFieldsGroup(Map<String, Set<String>> fieldGroups, String groupName,
            String value, int siteId) {
        String key = groupName;    
        for (int i = 0; key != null && i < 2; i++) {
            Set<String> l = fieldGroups.get(key);
            if (l == null) {
                l = new HashSet<String>();
                fieldGroups.put(key, l);
            }
            if (!l.contains(value)) {
                l.add(value);
            }
            if (siteId > 0) {
                key = siteId + "_" + groupName;
            } else {
                for (Map.Entry<String, Set<String>> entry : fieldGroups.entrySet()) {
                    if (entry.getKey().endsWith("_" + groupName)) {
                        l = entry.getValue();
                        if (!l.contains(value)) {
                            l.add(value);
                        }
                    }
                }
                key = null;
            }
        }
    }

    @Override
    public JahiaSearchResult fileSearch(String query, JahiaUser user) {
        return new WebDavSearchResultBuilderImpl().buildResult(
                getFileSearchHandler().search(query), user, new String[]{query});
    }

    protected SearchHandler getFileSearchHandler() {
        SearchHandler srcHandler = this.getSearchManager().getSearchHandler(
                WEBDAV_SEARCH);
        if (null == srcHandler) {
            try {
                srcHandler = this.getSearchManager().createSearchHandler(WEBDAV_SEARCH,
                        "JCR content search handler", 0,
                        JcrSearchHandler.class.getName(), config);
            } catch (Exception e) {
                logger.error("Unable to create JCR content search handler", e);
                throw new RuntimeException(
                        "Unable to create JCR content search handler", e);
            }
        }

        return srcHandler;
    }


    @Override
    public JahiaSavedSearchView getSavedSearchView(Integer searchMode,
            Integer savedSearchId, String contextId, String viewName,
            ProcessingContext ctx) throws JahiaException {

        JahiaSavedSearchView view = null;
        String userKey = ctx.getUser().getUsername();

        // guest users and administrators do not have persisted private profiles
        // administrators are able to update the shared profile (for all
        // guest users and the default for others)
        if (JahiaUserManagerService.GUEST_USERNAME.equals(ctx.getUser()
                .getUsername())
                || ctx.getUser().isAdminMember(ctx.getSiteID())) {
            userKey = "";
        }

        // for guest user we first check the session
        if (JahiaUserManagerService.isGuest(ctx.getUser())) {
            JahiaSavedSearchView testView = new JahiaSavedSearchView(Integer
                    .valueOf(searchMode), Integer.valueOf(savedSearchId),
                    contextId, userKey);
            view = (JahiaSavedSearchView) ctx.getSessionState().getAttribute(
                    testView.getHashCodeAsString());
        }

        // get from the database
        if (view == null) {
            view = savedSearchViewManager.getView(searchMode, savedSearchId,
                    contextId, userKey, false);
        }

        // nothing found? --> get the default from the
        // applicationcontext-savesearch.xml
        if (view == null) {
            view = new JahiaSavedSearchView(searchMode, savedSearchId,
                    contextId, userKey);
            view.setName(viewName);
            view.setSettings(getJahiaSavedSearchViewSettings(viewName));
        }

        if (userKey.length() > 0) {
            view.setUserKey(ctx.getUser().getUsername());
        }

        return view;
    }    
    
    private JahiaSavedSearchViewSettings getJahiaSavedSearchViewSettings(String viewName) {
        JahiaSavedSearchViewSettings settings = savedSearchViewSettings
        .get(savedSearchViewSettings.containsKey(viewName) ? viewName
                : "default");
        if (settings == null) {
            throw new IllegalStateException(
                    "No view settings configured in the applicationcontext-savesearch.xml file"
                            + " for key '" + viewName
                            + "' or at least for key 'default'.");
        }
        
        return settings.clone();
    } 
    
    @Override
    public void updateSavedSearchView(JahiaSavedSearchView view,
            ProcessingContext ctx) throws JahiaException {

        // guest users are allowed to store settings only in the session
        if (JahiaUserManagerService.isGuest(ctx.getUser())) {
            ctx.getSessionState()
                    .setAttribute(view.getHashCodeAsString(), view);
        } else {
            // administrators do not have persisted private profiles
            // rather they are able to update the shared profile (for all
            // guest users and the default for others)
            if (ctx.getUser().isAdminMember(ctx.getSiteID())) {
                view.setUserKey("");
            }
            savedSearchViewManager.saveView(view);
        }
    }

    public Iterator<String> getTerms (final int siteID, final String query) {
        final SearchHandler handler = getSearchHandler(siteID);
        return handler.getTerms(query);
    }

    /**
     * Add reference to open LuceneQueryRequest instances that need to be released by calling
     * the method <code>closeAllOpenLuceneQueryRequests</code>
     * in a finally clause at the end of the execution of current thread
     *
     * @param openLuceneQueryReqs
     */
    public static void addReferenceToOpenLuceneQueryRequests(Set<LuceneQueryRequest> openLuceneQueryReqs){
        if ( openLuceneQueryReqs == null || openLuceneQueryReqs.isEmpty() ){
            return;
        }
        Set<LuceneQueryRequest> reqsSet = threadLocalOpenLuceneQueryRequest.get();
        if (reqsSet == null) {
            reqsSet = new HashSet<LuceneQueryRequest>();
            threadLocalOpenLuceneQueryRequest.set(reqsSet);
        }
        reqsSet.addAll(openLuceneQueryReqs);
    }

    /**
     * Add reference to open LuceneQueryRequest instance that needs to be
     * released by calling the method
     * <code>closeAllOpenLuceneQueryRequests</code> in a finally clause at the
     * end of the execution of current thread
     * 
     * @param openLuceneQueryReq
     */
    public static void addReferenceToOpenLuceneQueryRequest(LuceneQueryRequest openLuceneQueryReq){
        if ( openLuceneQueryReq == null ){
            return;
        }
        Set<LuceneQueryRequest>reqsSet = threadLocalOpenLuceneQueryRequest.get();
        if (reqsSet == null) {
            reqsSet = new HashSet<LuceneQueryRequest>();
            threadLocalOpenLuceneQueryRequest.set(reqsSet);
        }
        reqsSet.add(openLuceneQueryReq);
    }

    /**
     * This method need to be called in the finally clause of a running Thread
     * to ensure all references to open Lucene Request are released.
     */
    public static void closeAllOpenLuceneQueryRequests(){
        Set<LuceneQueryRequest> reqsSet = threadLocalOpenLuceneQueryRequest.get();
        if (reqsSet != null) {
            for (LuceneQueryRequest request : reqsSet) {
                request.close();
            }
            reqsSet.clear();
        }
    }


    /**
     * Add referrence to an Open Searcher that needs to be released by calling
     * the method <code>removeAllReferenceToOpenSearchers</code> in a finally
     * clause at the end of the execution of current thread
     * 
     * @param searcher
     */
    public static void addReferenceToOpenSearcher(Searcher searcher){
        if (searcher == null) {
            return;
        }
        Set<Searcher> searchers = threadLocalSearcher.get();
        if (searchers == null) {
            searchers = new HashSet<Searcher>();
            threadLocalSearcher.set(searchers);
        }
        searchers.add(searcher);
    }

    /**
     * This method need to be called in the finally clause of a running Thread
     * to ensure all references to open Searchers are released.
     */
    public static void closeAllOpenSearchers(){
        Set<Searcher> searchers = threadLocalSearcher.get();
        if (searchers != null) {
            for (Searcher searcher : searchers) {
                try {
                    searcher.close();
                } catch (Exception t) {
                    logger.warn("cannot close IndexSearcher", t);
                }
            }
            searchers.clear();
        }
    }

    public static void closeAllOpenLuceneQueryRequestOrSearcher(){
        closeAllOpenLuceneQueryRequests();
        closeAllOpenSearchers();
    }

    public void deleteIndexForSite(int siteId, JahiaUser user,
            boolean notifyCluster) {

        // We delete All pending indexing jobs
        getIndJobMgr().deleteIndexingJobsForSite(siteId);        

        try {
            if (notifyCluster) {
                JahiaDeleteIndexJob job = new JahiaDeleteIndexJob(siteId, System
                        .currentTimeMillis());
                triggerImmediateExecutionOnAllNodes(job);
            } else {
                SearchHandler searchHandler = this.getSearchHandler(siteId);
                searchHandler.shutdown();
                loadedSiteSearchHandlers.remove(new Integer(siteId));
                
                JahiaSite site = sitesService.getSite(siteId);
                searchManager.unregisterHandler(site.getSiteKey());
                
                if (!this.localIndexing) {
                    return;
                }

                File f = new File(this.getSearchIndexRootDir() + File.separator
                        + site.getSiteKey());
                if (f.exists()) {
                    logger.debug(" site[" + site.getID()
                            + "] Process delete search index ");
                    JahiaTools.deleteFile(f);
                }
                
            }
        } catch (JahiaException ex) {
            logger.warn(
                    "Error when trying to delete the search index for site-Id: "
                            + siteId, ex);
        }
    }
}
