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
