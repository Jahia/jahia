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
 package org.jahia.services.search.indexingscheduler;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.search.JahiaSearchConfigConstant;
import org.jahia.utils.JahiaTools;
import org.quartz.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 oct. 2005
 * Time: 15:45:19
 * To change this template use File | Settings | File Templates.
 */
public class IndexingJobConsummer implements StatefulJob {

    public static final String JOB_NAME = "IndexingJobConsummer";
    public static final String JOB_GROUP_NAME = "IndexingJobConsummer";
    public static final String TRIGGER_NAME = "IndexingJobConsummerTrigger";
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(IndexingJobConsummer.class);

    public IndexingJobConsummer() {
    }

    public synchronized void execute(JobExecutionContext context)
            throws JobExecutionException {
        try {
            Date now = new Date();
            try {
                long indexingJobMaxLifeTime = JahiaTools.getTimeAsLong(
                        ServicesRegistry.getInstance()
                        .getJahiaSearchService().getIndexationConfig()
                        .getProperty(JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_MAX_LIFE_TIME,"1d"),"1d").longValue();

                ServicesRegistry.getInstance()
                        .getJahiaSearchService().getIndJobMgr()
                        .deleteIndexingJobsBefore(now.getTime()-indexingJobMaxLifeTime,true);
            } catch ( Exception t ){
                logger.debug("Exception deleting old indexing job",t);
            }
        } catch ( Exception t ){
            logger.debug("Exception occured deleting expired jobs", t);
        }
    }
}
