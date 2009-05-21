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
package org.jahia.hibernate.dao;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.DetachedCriteria;
import org.jahia.hibernate.model.JahiaAuditLog;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 15 avr. 2005
 * Time: 14:40:44
 * To change this template use File | Settings | File Templates.
 */
public class JahiaAuditLogDAO extends AbstractGeneratorDAO {
    public void save(final JahiaAuditLog log) {
        if (log.getId() == null) {
            log.setId(getNextInteger(log));
        }
        HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        hibernateTemplate.execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Transaction transaction = session.beginTransaction();
                try {
                    session.save(log);
                    transaction.commit();
                } catch (Exception t) {
                    transaction.rollback();
                }
                return null;
            }
        });
    }

    public List<JahiaAuditLog> getLogs(Integer objectType, Integer objectID,
            List<Integer[]> childrenObjectList) {

        List<JahiaAuditLog> retList = Collections.emptyList();

        if (objectType != null && objectID != null) {
            HibernateTemplate template = getHibernateTemplate();
            template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
            template.setCacheQueries(false);

            // limit the OR clauses (use limit for IN)
            int maxClauses = org.jahia.settings.SettingsBean.getInstance().getDBMaxElementsForInClause();

            if (childrenObjectList.size() <= maxClauses) {
                StringBuilder buffer = new StringBuilder(childrenObjectList
                        .size() * 35 + 128).append(
                        "from JahiaAuditLog l where l.objecttype=").append(
                        objectType).append(" and l.objectid=").append(objectID);
                for (Integer[] integers : childrenObjectList) {
                    buffer.append(" or l.objecttype=").append(integers[0])
                            .append(" and l.objectid=").append(integers[1]);
                }
                buffer.append(" order by l.time desc");
                retList = template.find(buffer.toString());
            } else {
                Set<Integer> ids = new HashSet<Integer>();
                for (int i = 0; i < (childrenObjectList.size() / maxClauses) + 1; i++) {
                    List<Integer[]> chunkedList = childrenObjectList.subList(i
                            * maxClauses, Math.min((i + 1) * maxClauses,
                            childrenObjectList.size()));

                    if (chunkedList.size() > 0) {
                        StringBuilder buffer = new StringBuilder(
                                maxClauses * 35 + 128)
                                .append("select l.id from JahiaAuditLog l where ");
                        if (i == 0) {
                            buffer.append("l.objecttype=").append(objectType)
                                    .append(" and l.objectid=")
                                    .append(objectID).append(" or");
                        }
                        boolean first = true;
                        for (Integer[] integers : chunkedList) {
                            if (!first) {
                                buffer.append(" or");
                            } else {
                                first = false;
                            }
                            buffer.append(" l.objecttype=").append(integers[0])
                                    .append(" and l.objectid=").append(
                                            integers[1]);
                        }
                        ids.addAll(template.find(buffer.toString()));
                    }
                }
                if (ids.size() > 0) {
                    List<Integer> idList = new LinkedList<Integer>(ids);
                    // check if there are too many results
                    if (ids.size() > maxClauses) {
                        Collections.sort(idList);
                        // lets take 'maxClauses' latest
                        idList = idList.subList(idList.size() - maxClauses,
                                idList.size());
                    }

                    retList = getSession().createQuery(
                            "from JahiaAuditLog l where l.id in (:ids)"
                                    + " order by l.time desc")
                            .setParameterList("ids", idList)
                            .setCacheable(false).list();
                }
            }
        }

        return retList;
    }

    public List<JahiaAuditLog> getLogs(long fromDate) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_NEVER);
        template.setCacheQueries(false);
        return template.find("from JahiaAuditLog l where (l.time > ?)"
                + " order by l.time desc, l.site desc", new Long(fromDate));
    }

    public int flushLogs(Integer objectType, Integer objectID,
            List<Integer[]> childrenObjectList) {
        int count = 0;

        if (objectType != null && objectID != null) {
            // limit the OR clauses (use limit for IN)
            int maxClauses = org.jahia.settings.SettingsBean.getInstance().getDBMaxElementsForInClause();

            if (childrenObjectList.size() <= maxClauses) {
                final StringBuilder buffer = new StringBuilder(
                        childrenObjectList.size() * 35 + 128).append(
                        "delete from JahiaAuditLog where objecttype=").append(
                        objectType).append(" and objectid=").append(objectID);

                for (Integer[] integers : childrenObjectList) {
                    buffer.append(" or objecttype=").append(integers[0])
                            .append(" and objectid=").append(integers[1]);
                }
                count = this.getSession().createQuery(buffer.toString())
                        .setCacheable(false).executeUpdate();
            } else {
                for (int i = 0; i < (childrenObjectList.size() / maxClauses) + 1; i++) {
                    List<Integer[]> chunkedList = childrenObjectList.subList(i
                            * maxClauses, Math.min((i + 1) * maxClauses,
                            childrenObjectList.size()));

                    if (chunkedList.size() > 0) {
                        StringBuilder buffer = new StringBuilder(chunkedList
                                .size() * 35 + 128)
                                .append("delete from JahiaAuditLog where ");

                        if (i == 0) {
                            buffer.append(" objecttype=").append(objectType)
                                    .append(" and objectid=").append(objectID)
                                    .append(" or ");
                        }
                        
                        boolean first = true;
                        for (Integer[] integers : chunkedList) {
                            if (!first) {
                                buffer.append(" or ");
                            } else {
                                first = false;
                            }
                            buffer.append("objecttype=").append(integers[0])
                                    .append(" and objectid=").append(
                                            integers[1]);
                        }
                        count += this.getSession().createQuery(
                                buffer.toString()).setCacheable(false)
                                .executeUpdate();
                    }
                }
            }
        }

        return count;
    }

    public void flushLogs(final Long oldestEntryTime) {
        this.getSession().createQuery(
                "delete from JahiaAuditLog where time < ?").setParameter(0,
                oldestEntryTime).executeUpdate();
    }

    public void flushSiteLogs(String siteKey) {
        this.getSession().createQuery(
                "delete from JahiaAuditLog where site = ?").setString(0,
                siteKey).executeUpdate();
    }

    public int enforceMaxLogs(int maxLogs) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.setCacheQueries(false);
        List<Long> ret = template.find("select count(l.id) from JahiaAuditLog l");
        int numRows = ret.get(0).intValue();
        int numDeletes = numRows - maxLogs;
        // if rows need to be deleted, get the highest ID to be deleted
        if (numDeletes > 0) {
            for (int i = 0; i < numDeletes; i++) {
                deleteOldestRow();
            }
        }
        return numDeletes;
    }

    public int deleteAllLogs() {
        return this.getSession().createQuery("delete from JahiaAuditLog")
                .executeUpdate();
    }

    public void deleteOldestRow() {
        this.getSession().createQuery(
                "delete from JahiaAuditLog where id = min(id)").executeUpdate();
    }

    public List<Object[]> executeCriteria(DetachedCriteria criteria, int maxResultSet){
        Criteria executableCriteria = criteria.getExecutableCriteria(this.getSession());
        executableCriteria.setMaxResults(maxResultSet);
        return executableCriteria.list();
    }

    public <E> List<E> executeNamedQuery(String queryName, Map<String, Object> parameters){
        Query query = this.getSession().getNamedQuery(queryName);
        if ( parameters != null ){
            Iterator<String> it = parameters.keySet().iterator();
            String key = null;
            Object value = null;
            while (it.hasNext()){
                key = (String)it.next();
                value = parameters.get(key);
                if ( value instanceof Collection ){
                    query.setParameterList(key,(Collection<?>)value);
                } else {
                    query.setParameter(key,value);
                }
            }
        }
        return query.list();
    }

}
