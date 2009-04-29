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
/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package org.jahia.hibernate.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.hibernate.dao.JahiaIndexingJobDAO;
import org.jahia.hibernate.dao.JahiaIndexJobServerDAO;
import org.jahia.hibernate.model.indexingjob.JahiaIndexingJob;
import org.jahia.hibernate.model.indexingjob.JahiaIndexJobServer;

import java.util.*;

/**
 *
 */
public class JahiaIndexingJobManager {
// ------------------------------ FIELDS ------------------------------

    private JahiaIndexingJobDAO dao = null;
    private JahiaIndexJobServerDAO indexJobServerDao = null;

    private Log log = LogFactory.getLog(JahiaIndexingJobManager.class);

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setJahiaIndexingJobDAO(JahiaIndexingJobDAO dao) {
        this.dao = dao;
    }

    public void setJahiaIndexJobServerDAO(JahiaIndexJobServerDAO indexJobServerDao) {
        this.indexJobServerDao = indexJobServerDao;
    }

    public List getServerLastIndexingTime() {
        List indexingJobs = dao.getIndexingJobs();
        loadServers(indexingJobs);
        return indexingJobs;
    }

    public JahiaIndexJobServer getServerLastIndexedJob(String serverId) {
        return dao.getServerLastIndexingJob(serverId);
    }

    public List<JahiaIndexingJob> getIndexingJobs() {
        List<JahiaIndexingJob> indexingJobs = dao.getIndexingJobs();
        loadServers(indexingJobs);
        return indexingJobs;
    }

    public List getIndexingJobsAfter(long time, boolean includeOfSameTime) {
        List indexingJobs = dao.getIndexingJobsAfter(time, includeOfSameTime);
        loadServers(indexingJobs);
        return indexingJobs;
    }

    public List getIndexingJobsBefore(long time, boolean includeOfSameTime) {
        List indexingJobs = dao.getIndexingJobsBefore(time, includeOfSameTime);
        loadServers(indexingJobs);
        return indexingJobs;
    }

    public List<JahiaIndexingJob> getIndexingJobs(long time, boolean includeOfSameTime,int hoursOfDay, int minutes, String serverId) {
        List<JahiaIndexingJob> indexingJobs = dao.getIndexingJobs(time,includeOfSameTime,hoursOfDay,minutes,serverId);
        loadServers(indexingJobs);
        return indexingJobs;
    }

// -------------------------- OTHER METHODS --------------------------

    public JahiaIndexingJob getIndexingJobById(String id) {
        if (id == null ) {
            return null;
        }
        JahiaIndexingJob job = dao.findByPK(id);
        if ( job != null ){
            List servers = indexJobServerDao.getIndexingJobServersByIndexingJobId(id);
            if ( servers != null ){
                job.getProcessedServers().addAll(servers);
            }
        }
        return job;
    }

    public void deleteIndexingJobsBefore(long time, boolean includeOfSameTime) {
        indexJobServerDao.deleteIndexJobServersBefore(time,includeOfSameTime);
        dao.deleteIndexingJobsBefore(time,includeOfSameTime);
    }

    public void delete(String id) {
        indexJobServerDao.deleteByIndexingJobId(id);
        dao.delete(id);
    }

    public void deleteIndexingJobsForSite(int siteID) {
        List<JahiaIndexingJob> indexingJobs = dao.getIndexingJobsForSite(siteID);
        Set<String> jobIds = new HashSet<String>(indexingJobs.size());
        for (JahiaIndexingJob job : indexingJobs) {
            jobIds.add(job.getId());
        }
        if (!jobIds.isEmpty()) {
            indexJobServerDao.deleteByIndexingJobId(jobIds);
            dao.delete(jobIds);
        }
    }    
    
    public void save(JahiaIndexingJob job) {
        try {
            dao.save(job);
            indexJobServerDao.deleteByIndexingJobId(job.getId());
            Iterator it = job.getProcessedServers().iterator();
            JahiaIndexJobServer indexJobServer = null;
            while ( it.hasNext() ){
                indexJobServer = (JahiaIndexJobServer)it.next();
                indexJobServerDao.save(indexJobServer);
            }
        } catch (Exception e) {
            log.info("Error saving JahiaIndexingJob ", e);
        }
    }

    public void addProcessedServer(JahiaIndexingJob job, JahiaIndexJobServer jobServer) {
        try {
            job.getProcessedServers().add(jobServer);
            indexJobServerDao.save(jobServer);
        } catch (Exception e) {
            log.info("Error saving JahiaIndexingJob ", e);
        }
    }

    public void saveIndexJobServer(JahiaIndexJobServer jobServer) {
        try {
            indexJobServerDao.save(jobServer);
        } catch (Exception e) {
            log.info("Error saving JahiaIndexingJob ", e);
        }
    }

    protected void loadServers(List indexingJobs){
        List serversList = indexJobServerDao.getIndexingJobServers();
        Iterator it = serversList.iterator();
        Map serversMap = new HashMap();
        List servers = null;
        JahiaIndexJobServer jobServer = null;
        while ( it.hasNext() ){
            jobServer = (JahiaIndexJobServer)it.next();
            servers = (List)serversMap.get(jobServer.getComp_id().getIndexingJobId());
            if ( servers == null ){
                servers = new ArrayList();
                serversMap.put(jobServer.getComp_id().getIndexingJobId(),servers);
            }
            servers.add(jobServer);
        }
        it = indexingJobs.iterator();
        JahiaIndexingJob indexingJob = null;
        while ( it.hasNext() ){
            indexingJob = (JahiaIndexingJob)it.next();
            servers = (List)serversMap.get(indexingJob.getId());
            if ( servers != null ){
                indexingJob.getProcessedServers().addAll(servers);
            }
        }
    }
}

