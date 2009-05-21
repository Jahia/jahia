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

