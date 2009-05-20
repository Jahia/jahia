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
import org.jahia.services.search.JahiaSearchConstant;
import org.jahia.services.search.JahiaSearchConfigConstant;
import org.jahia.content.*;
import org.jahia.hibernate.model.indexingjob.*;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 17 oct. 2005
 * Time: 15:45:19
 * To change this template use File | Settings | File Templates.
 */
public class IndexingJobTools {

    public static List<JahiaIndexingJob> resolveIndexingJobs(List<JahiaIndexingJob> allIndexingJobs, String excludeServerId) {
        if (allIndexingJobs.isEmpty()) {
            return allIndexingJobs;
        }

        long now = System.currentTimeMillis();
        long indexingJobDelayedTime = 0;
        int batchSize = 100;
        try {
            indexingJobDelayedTime = Long.parseLong(ServicesRegistry.getInstance()
                    .getJahiaSearchService().getIndexationConfig()
                    .getProperty(JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_EXECUTION_DELAY_TIME));
        } catch ( Exception t) {
            indexingJobDelayedTime = 30000;
        }
        try {
            batchSize = Integer.parseInt(ServicesRegistry.getInstance()
                    .getJahiaSearchService().getIndexationConfig()
                    .getProperty(JahiaSearchConfigConstant.SEARCH_INDEXING_JOB_BATCH_SIZE));
        } catch ( Exception t) {
            batchSize = 100;
        }
        Map<Object, JahiaIndexingJob> map = new HashMap<Object, JahiaIndexingJob>();
        List<String> bypassedIds = new ArrayList<String>();
        for (JahiaIndexingJob lastJob : allIndexingJobs) {
            boolean alreadyProcessed = false;
            if ( lastJob instanceof JahiaFieldIndexingJob ){
                JahiaFieldIndexingJob fieldIndexingJob = (JahiaFieldIndexingJob)lastJob;
                Object key = ContentFieldKey.toObjectKeyString(fieldIndexingJob.getFieldId().intValue());
                JahiaIndexingJob job = (JahiaIndexingJob)map.get(key);
                if ( job == null ){
                    map.put(key,lastJob);
                } else if ( lastJob.getDate().longValue()>job.getDate().longValue() ){
                    map.put(key,lastJob);
                    deleteJobFromPersistence(job);
                    bypassedIds.add(job.getId());
                } else {
                    deleteJobFromPersistence(lastJob);
                    bypassedIds.add(lastJob.getId());
                }
            } else if ( lastJob instanceof JahiaContainerIndexingJob ) {
                JahiaContainerIndexingJob containerIndexingJob = (JahiaContainerIndexingJob)lastJob;
                Object key = ContentContainerKey.toObjectKeyString(containerIndexingJob.getCtnId().intValue());
                JahiaIndexingJob job = (JahiaIndexingJob)map.get(key);
                if ( job == null ){
                    map.put(key,lastJob);
                } else if ( lastJob.getDate().longValue()>job.getDate().longValue() ){
                    map.put(key,lastJob);
                    deleteJobFromPersistence(job);
                    bypassedIds.add(job.getId());
                } else {
                    deleteJobFromPersistence(lastJob);
                    bypassedIds.add(lastJob.getId());
                }
            } else if ( lastJob instanceof JahiaContainerListIndexingJob ) {
                JahiaContainerListIndexingJob containerListIndexingJob = (JahiaContainerListIndexingJob)lastJob;
                Object key = (new ContentContainerListKey(containerListIndexingJob.getCtnListId())).getKey();
                JahiaIndexingJob job = (JahiaIndexingJob)map.get(key);
                if ( job == null ){
                    map.put(key,lastJob);
                } else if ( lastJob.getDate().longValue()>job.getDate().longValue() ){
                    map.put(key,lastJob);
                    deleteJobFromPersistence(job);
                    bypassedIds.add(job.getId());
                } else {
                    deleteJobFromPersistence(lastJob);
                    bypassedIds.add(lastJob.getId());
                }
            } else if ( lastJob instanceof JahiaPageIndexingJob ) {
                JahiaPageIndexingJob pageIndexingJob = (JahiaPageIndexingJob)lastJob;
                Object key = ContentPageKey.toObjectKeyString(pageIndexingJob.getPageId().intValue());
                JahiaIndexingJob job = (JahiaIndexingJob)map.get(key);
                if ( job == null ){
                    map.put(key,lastJob);
                } else if ( lastJob.getDate().longValue()>job.getDate().longValue() ){
                    map.put(key,lastJob);
                    deleteJobFromPersistence(job);
                    bypassedIds.add(job.getId());
                } else {
                    deleteJobFromPersistence(lastJob);
                    bypassedIds.add(lastJob.getId());
                }
            } else if ( lastJob instanceof JahiaRemoveFromIndexJob ){
                JahiaRemoveFromIndexJob removeFromIndexJob = (JahiaRemoveFromIndexJob)lastJob;
                if ( JahiaSearchConstant.FIELD_FIELDID
                        .equalsIgnoreCase(removeFromIndexJob.getKeyFieldName()) ){
                    // it's a field
                    Object key = ContentFieldKey.toObjectKeyString(removeFromIndexJob.getKeyFieldValue());
                    JahiaIndexingJob job = (JahiaIndexingJob)map.get(key);
                    if ( job == null ){
                        map.put(key,lastJob);
                    } else if ( lastJob.getDate().longValue()>job.getDate().longValue() ){
                        map.put(key,lastJob);
                        deleteJobFromPersistence(job);
                        bypassedIds.add(job.getId());
                    } else {
                        deleteJobFromPersistence(lastJob);
                        bypassedIds.add(lastJob.getId());
                    }
                } else if ( JahiaSearchConstant.OBJECT_KEY
                        .equalsIgnoreCase(removeFromIndexJob.getKeyFieldName()) ){
                    try {
                        // it's a page, a container or a container list
                        Object key = ContentObjectKey.getInstance(removeFromIndexJob.getKeyFieldValue()).getKey();
                        JahiaIndexingJob job = (JahiaIndexingJob)map.get(key);
                        if ( job == null ){
                            map.put(key,lastJob);
                        } else if ( lastJob.getDate().longValue()>job.getDate().longValue() ){
                            map.put(key,lastJob);
                            deleteJobFromPersistence(job);
                            bypassedIds.add(job.getId());
                        } else {
                            deleteJobFromPersistence(lastJob);
                            bypassedIds.add(lastJob.getId());
                        }                        
                    } catch ( Exception t ){
                        // should not happens
                        deleteJobFromPersistence(lastJob);
                        bypassedIds.add(lastJob.getId());
                        continue;
                    }
                } else {
                    // we cannot resolve or optimize further
                    // so just put it in the map
                    map.put(lastJob.getId(),lastJob);
                }
            } else {
                // we cannot resolve or optimize further
                // so just put it in the map
                map.put(lastJob.getId(),lastJob);
            }
            if ( excludeServerId != null && !lastJob.getProcessedServers().isEmpty() ){
                for (JahiaIndexJobServer jobServer : lastJob.getProcessedServers() ){
                    if ( jobServer.getComp_id().getServerId().equals(excludeServerId)
                            && jobServer.getDate().longValue() == lastJob.getDate().longValue() ){
                        bypassedIds.add(lastJob.getId());
                        alreadyProcessed = true;
                        break;
                    }
                }
            }
            if ( alreadyProcessed ){
                continue;
            }
            if ( (now - lastJob.getDate().longValue()) < indexingJobDelayedTime ){
                bypassedIds.add(lastJob.getId());
            }
        }
        if ( bypassedIds.size() == 0 ){
            if ( allIndexingJobs.size()> batchSize ){
                return allIndexingJobs.subList(0,batchSize);
            }
        }
        List<JahiaIndexingJob> result = new ArrayList<JahiaIndexingJob>();
        for (JahiaIndexingJob job : allIndexingJobs ){
            if (!bypassedIds.contains(job.getId()) ){
                result.add(job);
                if ( result.size() > batchSize ){
                    return result;
                }
            }
        }
        return result;
    }

    public static void deleteJobFromPersistence(JahiaIndexingJob job){
        ServicesRegistry.getInstance()
                .getJahiaSearchService().deleteIndexingJob(job.getId());
    }
}
