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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jahia.content.TimeBasedPublishingState;
import org.jahia.hibernate.model.JahiaObject;
import org.jahia.hibernate.model.JahiaObjectPK;
import org.springframework.orm.hibernate3.HibernateTemplate;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:54:13
 * To change this template use File | Settings | File Templates.
 */
public class JahiaObjectDAO extends AbstractGeneratorDAO implements TimeBasedPublishingState {

    public List getJahiaObjects() {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaObject");
    }

    /**
     * Convenient method to execute a query
     *
     * @param query
     * @param params
     * @return
     */
    public <E> List<E> executeQuery(String query, Object params[]) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        if ( params != null ){
            return template.find(query,params);
        } else {
            return template.find(query);
        }
    }

    public JahiaObject findByPK(JahiaObjectPK key) {
        if ( key == null || key.getId().intValue()<=0 ){
            return null;
        }
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return (JahiaObject) template.get(JahiaObject.class, key);
    }

    public List loadJahiaObjects(JahiaObjectPK key, int batchSize) {
        if ( key == null || key.getId().intValue()<=0 ){
            return null;
        }
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        StringBuffer buffer = new StringBuffer(1024);
        buffer.append("select jobj from JahiaObject jobj");
        buffer.append(" left join fetch jobj.retentionRule retRule ");
        buffer.append(" where jobj.comp_id.id>=? AND jobj.comp_id.id<=? AND jobj.comp_id.type=?");
        List jahiaObjs = template.find(buffer.toString(),
                                new Object[]{new Integer(key.getId().intValue()-batchSize),
                                        new Integer(key.getId().intValue()+batchSize),key.getType()});
        /*
        if ( !jahiaObjs.isEmpty() ){
            List retRuleIds = template.find("select jobj.comp_id.id, jobj.retentionRule.id from JahiaObject jobj where jobj.comp_id.id>=? AND jobj.comp_id.id<=? AND jobj.comp_id.type=?",
                                new Object[]{key.getId(),new Integer(key.getId().intValue()+batchSize),key.getType()});
            Iterator it = retRuleIds.iterator();
            if ( it.hasNext() ){
                StringBuffer query = new StringBuffer(1024);
                query.append("from JahiaRetentionRule rule where rule.id=");
                Object[] objs = null;
                Map objectsMap = new HashMap();
                Integer objId = null;
                Integer ruleId = null;
                Iterator objIt = null;
                JahiaObject jahiaObj = null;
                while ( it.hasNext() ){
                    objs = (Object[])it.next();
                    objId = (Integer)objs[0];
                    ruleId = (Integer)objs[1];
                    if ( ruleId != null ){
                        objIt = jahiaObjs.iterator();
                        while ( objIt.hasNext() ){
                            jahiaObj = (JahiaObject)objIt.next();
                            if ( jahiaObj.getComp_id().getId().intValue() == objId.intValue() ){
                                objectsMap.put(ruleId,jahiaObj);
                                break;
                            }
                        }
                        query.append(String.valueOf(ruleId.intValue()));
                        if ( it.hasNext() ){
                            query.append(" or rule.id=");
                        }
                    }
                }
                List retRules = template.find(query.toString());
                it = retRules.iterator();
                List results = new ArrayList();
                JahiaRetentionRule rule = null;
                while ( it.hasNext() ){
                    rule = (JahiaRetentionRule)it.next();
                    jahiaObj = (JahiaObject)objectsMap.get(rule.getId());
                    if ( jahiaObj != null ){
                        jahiaObj.setRetentionRule(rule);
                    }
                }
            }
        }
        */
        return jahiaObjs;
    }
    /*
    public List loadJahiaObjects(JahiaObjectPK key, int batchSize) {
        if ( key == null || key.getId().intValue()<=0 ){
            return null;
        }
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        List jahiaObjs = template.find("from JahiaObject jobj where jobj.comp_id.id>=? AND jobj.comp_id.id<=? AND jobj.comp_id.type=?",
                                new Object[]{key.getId(),new Integer(key.getId().intValue()+batchSize),key.getType()});

        if ( !jahiaObjs.isEmpty() ){
            List retRuleIds = template.find("select jobj.comp_id.id, jobj.retentionRule.id from JahiaObject jobj where jobj.comp_id.id>=? AND jobj.comp_id.id<=? AND jobj.comp_id.type=?",
                                new Object[]{key.getId(),new Integer(key.getId().intValue()+batchSize),key.getType()});
            Iterator it = retRuleIds.iterator();
            if ( it.hasNext() ){
                StringBuffer query = new StringBuffer(1024);
                query.append("from JahiaRetentionRule rule where rule.id=");
                Object[] objs = null;
                Map objectsMap = new HashMap();
                Integer objId = null;
                Integer ruleId = null;
                Iterator objIt = null;
                JahiaObject jahiaObj = null;
                while ( it.hasNext() ){
                    objs = (Object[])it.next();
                    objId = (Integer)objs[0];
                    ruleId = (Integer)objs[1];
                    if ( ruleId != null ){
                        objIt = jahiaObjs.iterator();
                        while ( objIt.hasNext() ){
                            jahiaObj = (JahiaObject)objIt.next();
                            if ( jahiaObj.getComp_id().getId().intValue() == objId.intValue() ){
                                objectsMap.put(ruleId,jahiaObj);
                                break;
                            }
                        }
                        query.append(String.valueOf(ruleId.intValue()));
                        if ( it.hasNext() ){
                            query.append(" or rule.id=");
                        }
                    }
                }
                List retRules = template.find(query.toString());
                it = retRules.iterator();
                List results = new ArrayList();
                JahiaRetentionRule rule = null;
                while ( it.hasNext() ){
                    rule = (JahiaRetentionRule)it.next();
                    jahiaObj = (JahiaObject)objectsMap.get(rule.getId());
                    if ( jahiaObj != null ){
                        jahiaObj.setRetentionRule(rule);
                    }
                }
            }
        }
        return jahiaObjs;
    }*/

    public Collection findByRetentionRule(int ruleId) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find("from JahiaObject jobj where jobj.retentionRule.id=?",new Integer(ruleId));
    }

    public List findDisplayableObjects(String type) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        return template.find(new StringBuffer().append("Select jobj.comp_id.id from JahiaObject jobj where jobj.timeBPState =").append(TimeBasedPublishingState.IS_VALID_STATE).append(" and jobj.comp_id.type=?").toString(),new Object[]{type});
    }

    /**
     * return the list of inconsistent object in regards to the publishing state.
     *
     * @param checkTime the publication or expiration time
     * @param maxElapsedTime the elapsed time max above which the state should be considered inconsistent
     *
     * @return
     */
    public List findInconsistentObjects(long checkTime, long maxElapsedTime) {
        HibernateTemplate template = getHibernateTemplate();
        template.setCacheQueries(true);
        Long checkTimeLong = new Long(checkTime);
        Object[] params = {new Integer(TimeBasedPublishingState.IS_VALID_STATE),checkTimeLong,new Long(1),new Long(checkTime-1),
                           new Integer(TimeBasedPublishingState.IS_VALID_STATE),checkTimeLong,new Long(0),checkTimeLong};
        List objL = template.find(new StringBuffer().append("from JahiaObject jobj where (jobj.timeBPState = ?")
                .append(" and (jobj.validFromDate > ? ")
                .append(" or (jobj.validToDate between ? and ?))) or (jobj.timeBPState != ? and ")
                .append(" jobj.validFromDate < ? and (jobj.validToDate = ? or jobj.validToDate > ?))").toString(),
                params);
        List result = new ArrayList();
        Iterator iterator =objL.iterator();
        JahiaObject jObj = null;
        long validFrom = 0;
        long validTo = 0;
        while ( iterator.hasNext() ){
            jObj = (JahiaObject)iterator.next();
            validFrom = jObj.getValidFromDate().longValue();
            validTo = jObj.getValidToDate().longValue();
            if ( (jObj.getTimeBPState().intValue() == TimeBasedPublishingState.IS_VALID_STATE
                && (validFrom-checkTime>maxElapsedTime
                || (validTo>0 && checkTime-validTo>maxElapsedTime) ))
                || (jObj.getTimeBPState().intValue() != TimeBasedPublishingState.IS_VALID_STATE
                && (validFrom>0 && (checkTime-validFrom>maxElapsedTime)
                || validTo-checkTime>maxElapsedTime )) ){
                result.add(jObj);
            }
        }
        return result;
    }

    /**
     * Create a new entry in database
     * @param type
     * @param id
     * @param siteId
     */
    public void create(String type, Integer id, Integer siteId) {
        JahiaObject jahiaObject = new JahiaObject();
        jahiaObject.setComp_id(new JahiaObjectPK(type,id));
        jahiaObject.setSiteId(siteId);
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.saveOrUpdate(jahiaObject);
        template.flush();
    }

    public void delete(JahiaObjectPK key) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        String query = "from JahiaObject obj where obj.comp_id.id=? AND obj.comp_id.type=?";
        template.deleteAll(template.find(query, new Object[]{key.getId(),key.getType()}));
        template.flush();
    }

    public void save(JahiaObject jahiaObject) {
        HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.merge(jahiaObject);
        template.flush();
    }

    public void deleteAllFromSite(Integer siteID) {
        String queryString = "from JahiaObject c where c.siteId=? ";
        final HibernateTemplate template = getHibernateTemplate();
        template.setFlushMode(HibernateTemplate.FLUSH_AUTO);
        template.deleteAll(template.find(queryString,siteID));
        template.flush();
    }
}
