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
package org.jahia.hibernate.dao;

import org.hibernate.CacheMode;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.jahia.hibernate.model.indexingjob.JahiaIndexJobServer;
import org.jahia.hibernate.model.indexingjob.JahiaIndexJobServerPK;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

/**
 *
 */
public class JahiaIndexJobServerDAO extends AbstractGeneratorDAO {

    private byte[] lock = {};

    public List getIndexingJobServers() {
        getSession().setCacheMode(CacheMode.IGNORE);
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        Set jobs = new HashSet(template.find("from JahiaIndexJobServer as indexJobServer"));
        return new ArrayList(jobs);
    }

    public List getIndexJobServersAfter(long time,
                                        boolean includeOfSameTime) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        StringBuffer query = new StringBuffer("from JahiaIndexJobServer as indexJobServer where indexJobServer.date ");
        if ( includeOfSameTime ){
            query.append(">= ?");
        } else {
            query.append("> ?");
        }
        query.append(" order by indexJobServer.date asc ");
        return template.find(query.toString(), new Long(time));
    }

    public List getIndexJobServersBefore(long time,
                                         boolean includeOfSameTime) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        StringBuffer query = new StringBuffer("from JahiaIndexJobServer as indexJobServer where indexJobServer.date ");
        if ( includeOfSameTime ){
            query.append("<= ?");
        } else {
            query.append("< ?");
        }
        query.append(" order by indexJobServer.date asc ");
        return template.find(query.toString(), new Long(time));
    }

    public List getIndexingJobServersByServerId(String serverId) {
        getSession().setCacheMode(CacheMode.IGNORE);
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        Set servers = new HashSet(template.find("from JahiaIndexJobServer as indexJobServer where indexJobServer.comp_id.serverId=?",serverId));
        return new ArrayList(servers);
    }

    public List getIndexingJobServersByIndexingJobId(String indexingJobId) {
        getSession().setCacheMode(CacheMode.IGNORE);
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        Set servers = new HashSet(template.find("from JahiaIndexJobServer as indexJobServer where indexJobServer.comp_id.indexingJobId=?",indexingJobId));
        return new ArrayList(servers);
    }

    public JahiaIndexJobServer findByPK(JahiaIndexJobServerPK comp_id){
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(false);
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        return (JahiaIndexJobServer)template.get(JahiaIndexJobServer.class, comp_id);
    }

    public void delete(JahiaIndexJobServerPK comp_id) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(template.find("from JahiaIndexJobServer obj where obj.comp_id=?", comp_id));
            template.flush();
        }
    }

    public void deleteByServerId(String serverId) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(template.find("from JahiaIndexJobServer obj where obj.comp_id.serverId=?", serverId));
            template.flush();
        }
    }

    public void deleteByIndexingJobId(String indexingJobId) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(template.find("from JahiaIndexJobServer obj where obj.comp_id.indexingJobId=?", indexingJobId));
            template.flush();
        }
    }
    
    public void deleteByIndexingJobId(Collection<String> indexingJobIds) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(template.findByNamedParam("from JahiaIndexJobServer obj where obj.comp_id.indexingJobId in (:jobIds)", "jobIds", indexingJobIds));
            template.flush();
        }
    }    

    public void deleteIndexJobServersBefore(long time,
                                             boolean includeOfSameTime) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.deleteAll(getIndexJobServersBefore(time,includeOfSameTime));
            template.flush();
        }
    }

    public void save(JahiaIndexJobServer indexJobServer) {
        synchronized(lock){
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
            template.setCacheQueries(false);
            template.merge(indexJobServer);
            template.flush();
        }
    }

}
