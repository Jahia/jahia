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
package org.jahia.hibernate.dao;

import org.jahia.hibernate.model.indexingjob.*;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 *
 */
public class JahiaIndexingJobDAO extends AbstractGeneratorDAO {

    private byte[] lock = {};

    public List<JahiaIndexingJob> getIndexingJobs() {
        //getSession().setCacheMode(CacheMode.IGNORE);
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Set jobs = new HashSet(template.find("from JahiaIndexingJob as indexingJob order by indexingJob.date asc"));
        return new ArrayList<JahiaIndexingJob>(jobs);
    }

    public List<JahiaIndexingJob> getIndexingJobsAfter(long time,
                                     boolean includeOfSameTime) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer query = new StringBuffer("from JahiaIndexingJob as indexingJob where indexingJob.date ");
        if ( includeOfSameTime ){
            query.append(">= ?");
        } else {
            query.append("> ?");
        }
        query.append(" order by indexingJob.date asc ");
        return template.find(query.toString(), new Long(time));
    }

    public List<JahiaIndexingJob> getIndexingJobsBefore(long time,
                                      boolean includeOfSameTime) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer query = new StringBuffer("from JahiaIndexingJob as indexingJob where indexingJob.date ");
        if ( includeOfSameTime ){
            query.append("<= ?");
        } else {
            query.append("< ?");
        }
        query.append(" order by indexingJob.date asc ");
        return template.find(query.toString(), new Long(time));
    }

    /**
     * Returns the list of indexing jobs matching the given hour and minute of Day
     *
     * @param time
     * @param includeOfSameTime
     * @param hoursOfDay
     * @param serverId unique cluster serverId
     * @return
     */
    public List<JahiaIndexingJob> getIndexingJobs(long time,
                                boolean includeOfSameTime,
                                int hoursOfDay, int minutes,
                                String serverId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Integer hourInMinutes  =  new Integer(hoursOfDay * 60 + minutes);
        StringBuffer query = new StringBuffer("from JahiaIndexingJob as indexingJob where (indexingJob.enabledIndexingServers=? OR indexingJob.enabledIndexingServers=?) AND ((indexingJob.indexImmediately=? AND indexingJob.date ");
        if ( includeOfSameTime ){
            query.append(">= ?");
        } else {
            query.append("> ?");
        }
        query.append(" ) ");
        query.append(" OR ( indexingJob.scheduledFromTime1 < indexingJob.scheduledToTime1 AND indexingJob.scheduledFromTime1 <=? AND indexingJob.scheduledToTime1 >= ? ) ");
        query.append(" OR ( indexingJob.scheduledFromTime2 < indexingJob.scheduledToTime2 AND indexingJob.scheduledFromTime2 <=? AND indexingJob.scheduledToTime2 >= ? ) ");
        query.append(" OR ( indexingJob.scheduledFromTime3 < indexingJob.scheduledToTime2 AND indexingJob.scheduledFromTime2 <=? AND indexingJob.scheduledToTime3 >= ? ) ");
        query.append(" OR ( indexingJob.scheduledFromTime1 > indexingJob.scheduledToTime1 AND ( ? >= indexingJob.scheduledFromTime1 OR ? <= indexingJob.scheduledToTime1 ) ) ");
        query.append(" OR ( indexingJob.scheduledFromTime2 > indexingJob.scheduledToTime2 AND ( ? >= indexingJob.scheduledFromTime2 OR ? <= indexingJob.scheduledToTime2 ) )");
        query.append(" OR ( indexingJob.scheduledFromTime3 > indexingJob.scheduledToTime3 AND ( ? >= indexingJob.scheduledFromTime3 OR ? <= indexingJob.scheduledToTime3 ) ) )");
        query.append(" order by indexingJob.date asc ");
        return template.find(query.toString(), new Object[]{JahiaIndexingJob.EXCLUSIVE_INDEXING_SERVER_ALL,serverId,Boolean.TRUE,new Long(time),hourInMinutes,hourInMinutes,hourInMinutes,hourInMinutes,
                hourInMinutes,hourInMinutes,hourInMinutes,hourInMinutes,hourInMinutes,
                hourInMinutes,hourInMinutes,hourInMinutes});
    }

    public List<JahiaIndexingJob> getIndexingJobsForSite(Integer siteID) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Set jobs = new HashSet(template.find("from JahiaIndexingJob as indexingJob where indexingJob.siteId=?", siteID));
        return new ArrayList<JahiaIndexingJob>(jobs);
    }    
    
    public JahiaIndexingJob findByPK(String id) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        JahiaIndexingJob job = (JahiaIndexingJob)template.get(JahiaIndexingJob.class, id);
        return job;
    }

    public void deleteIndexingJobsBefore(long time,
                                         boolean includeOfSameTime) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(getIndexingJobsBefore(time,includeOfSameTime));
            template.flush();
        }
    }

    public void delete(String id) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(template.find("from JahiaIndexingJob h where h.id=?", id));
            template.flush();
        }
    }

    public void delete(Collection<String> ids) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(template.findByNamedParam("from JahiaIndexingJob h where h.id in (:jobIds)", "jobIds", ids));
            template.flush();
        }
    }    
    
    public void save(JahiaIndexingJob job) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.setCacheQueries(false);
            template.merge(job);
            template.flush();
        }
    }

    public void addProcessedServer(String indexJobId, JahiaIndexJobServer jobServer) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            JahiaIndexingJob job = this.findByPK(indexJobId);
            if ( job != null ){
                job.removeIndexJobServer(jobServer);
                job.addProcessedServer(jobServer);
            }
            template.merge(job);
            template.flush();
        }
    }

    public void addProcessedServer(JahiaIndexingJob job, JahiaIndexJobServer jobServer) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            job.addProcessedServer(jobServer);
            template.merge(job);
            template.flush();
        }            
    }

    public JahiaIndexJobServer getServerLastIndexingJob(String serverId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List jobs = template.find("from JahiaIndexJobServer as jobServer where jobServer.comp_id.serverId=? order by jobServer.date desc",
                new Object[]{serverId});
        if ( !jobs.isEmpty() ){
            return (JahiaIndexJobServer)jobs.get(0);
        }
        return null;
    }

}
