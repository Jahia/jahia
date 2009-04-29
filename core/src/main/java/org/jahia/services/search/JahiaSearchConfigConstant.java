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
//
//
//

package org.jahia.services.search;

/**
 * Define constants for Search service configuration
 */
public final class JahiaSearchConfigConstant {

    /**
     * the root directory for search indexes
     */
    public static final String SEARCH_INDEX_ROOT_DIR                    = "searchIndexRootDir";
    
    /**
     * setting to disable indexing (with true) 
     */
    public static final String SEARCH_INDEXING_DISABLED                    = "indexingDisabled";    

    /**
     * 1: index, 0: do not index ,just read ( I.E when sharing a same index directory in a clustered environment )
     */
    public static final String SEARCH_LOCAL_INDEXING                    = "localIndexing";

    /**
     * 1: multiple indexing server, 0: one single indexing server
     */
    public static final String SEARCH_MULTIPLE_INDEXING_SERVER          = "multipleIndexingServer";

    /**
     * the latenty time at search service startup before starting indexation
     */
    public static final String SEARCH_INDEXING_STARTUP_LATENCY_TIME     = "indexingStartupLatencyTime";

    /**
     * the interval time between each scan of indexing jobs history ( loading them and process them if needed )
     */
    public static final String SEARCH_INDEXING_JOB_WATCH_INTERVAL       = "indexingJobWatchInterval";

    /**
     * the delayed time before processing an indexing job. An indexing job will be
     * processed only after this delay time in regard to it creation time
     */
    public static final String SEARCH_INDEXING_JOB_EXECUTION_DELAY_TIME = "indexingJobExecutionDelayTime";

    /**
     * Define the max size of the insert job map. This map is used to control the minimal interval
     * between two insert of an indexing order for a same object
     */
    public static final String SEARCH_INDEXING_JOB_INSERT_MAP_MAXSIZE = "indexingJobInsertMapMaxSize";

    /**
     * The batch size used to handle indexing jobs at a time
     */
    public static final String SEARCH_INDEXING_JOB_BATCH_SIZE = "indexingJobBatchSize";

    /**
     * the maximum life time of indexing job history. 15min for demo purpose,
     * but should be i.e 1 day with several indexing servers in cluster
     */
    public static final String SEARCH_INDEXING_JOB_MAX_LIFE_TIME        = "indexingJobMaxLifeTime";

    /**
     * default delay after which field score boost are re-evaluated ( Values are reloaded from Field definitions )
     */
    public static final String SEARCH_SCORE_BOOST_REFRESH_DELAY_TIME = "scoreBoostRefreshDelayTime";

    /**
     * the default view handler for search engine ( search form processing )
     */
    public static final String SEARCH_DEFAULT_VIEW_HANDLER              = "org.jahia.engines.search.searchViewHandler.default";

    /**
     * the default search handler class ( search and indexation )
     */
    public static final String SEARCH_DEFAULT_SEARCH_HANDLER_CLASS      = "defaultSearchHandlerClass";

    /**
     * The date rounding ( in minutes ) to keep the search index smaller
     */
    public static final String DATE_ROUNDING = "dateRounding";
    
    /**
     * server ID (cluster.node.serverId in jahia.properties) of the cluster node, which is doing the indexing of the used index
     */
    public static final String SEARCH_INDEXER_SERVER_ID = "searchIndexerServerId";    

    /**
     * server ID (cluster.node.serverId in jahia.properties) of the cluster node, which is doing the indexing of the used index
     */
    public static final String SYNCHRONIZED_INDEXATION_WAIT_DELAY = "synxhronizedIndexationWaitDelay";    

    
    public static final String ENABLE_FILE_INDEXING = "enableJcrSearch";
    
    public static final String METADATA_SCOREBOOST = "org.jahia.services.search.scoreBoost.metadata";
    public static final String ANALYZER_FOR_NUMERICS = "org.jahia.services.search.analyzerForNumerics";
    public static final String ANALYZER_FOR_KEYWORDS = "org.jahia.services.search.analyzerForKeywords";
}
