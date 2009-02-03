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
