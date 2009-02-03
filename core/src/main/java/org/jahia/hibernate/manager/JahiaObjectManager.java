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
package org.jahia.hibernate.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.TimeBasedPublishingJahiaObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaObjectDAO;
import org.jahia.hibernate.dao.JahiaRetentionRuleDAO;
import org.jahia.hibernate.model.JahiaObject;
import org.jahia.hibernate.model.JahiaObjectPK;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 11:42:48
 * To change this template use File | Settings | File Templates.
 */
public class JahiaObjectManager {
// ------------------------------ FIELDS ------------------------------

    private Log log = LogFactory.getLog(JahiaObjectManager.class);

    public static final String CACHE_NAME = "JahiaObjectManagerCache";
    public static final String CACHE_KEY_PREFIX = "JahiaObject_";
    public static final String OBJECTDELEGATE_KEY_PREFIX = "JahiaObjectDelegate_";

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger (JahiaObjectManager.class);

    private JahiaObjectDAO dao = null;

    private JahiaRetentionRuleDAO retentionRuleDao = null;

    private CacheService cacheService = null;

    private Cache cache = null;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public List getJahiaObjects() {
        List list = dao.getJahiaObjects();
        return fillList(list);
    }

    /**
     * Convenient method to execute a query
     *
     * @param query
     * @param params
     * @return
     */
    public <E> List<E> executeQuery(String query, Object params[]) {
        return dao.executeQuery(query, params);
    }

    public JahiaObjectDAO getJahiaObjectDAO() {
        return this.dao;
    }

    public void setJahiaObjectDAO(JahiaObjectDAO dao) {
        this.dao = dao;
    }

    public JahiaRetentionRuleDAO getJahiaRetentionRuleDAO() {
        return retentionRuleDao;
    }

    public void setJahiaRetentionRuleDAO(JahiaRetentionRuleDAO retentionRuleDao) {
        this.retentionRuleDao = retentionRuleDao;
    }

// ------------------------- OTHER METHODS --------------------------

    private List fillList(List list) {
        FastArrayList retList = new FastArrayList(list.size());
        JahiaObject hibJahiaObject = null;
        org.jahia.content.JahiaObject jahiaObject;
        for (Iterator it = list.iterator(); it.hasNext();) {
            hibJahiaObject = (JahiaObject) it.next();
            try {
                jahiaObject = hibJahiaObject.toJahiaObject();
                if ( jahiaObject != null ){
                    retList.add(jahiaObject);
                }
            } catch (Exception e) {
                log.debug("Error loading JahiaObject",e);
            }
        }
        retList.setFast(true);
        return retList;
    }

    public List findDisplayableJahiaObjectIds(String type) {
        List retList = null;
        List result = null;
        if(cache == null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        if(cache!=null) {
            result = (List) cache.get(type);
        }
        if(result == null) {
            retList = dao.findDisplayableObjects(type);
            result = new ArrayList();
            Iterator iterator = retList.iterator();
            Integer id = null;
            while (iterator.hasNext()) {
                id = (Integer) iterator.next();
                result.add(id);
            }
            if(cache!=null) {
                cache.put(type,result);
            }
        }
        return result;
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
        List jObjsList = dao.findInconsistentObjects(checkTime, maxElapsedTime);
        if (jObjsList == null ){
            return new ArrayList();
        }
        List retList = new ArrayList();
        Iterator it = jObjsList.iterator();
        JahiaObject jObj = null;
        JahiaObjectDelegate jahiaObjectDelegate = null;
        while ( it.hasNext() ){
            jObj = (JahiaObject)it.next();
            try {
                jahiaObjectDelegate = convertJahiaObjectToJahiaObjectDelegate(jObj.toJahiaObject().getObjectKey(),jObj);
                if ( jahiaObjectDelegate != null ){
                    addJahiaObjectDelegateInCache(jahiaObjectDelegate);
                    retList.add(jahiaObjectDelegate);
                }
            } catch ( Exception t ){
            }
        }
        return retList;
    }

    public org.jahia.content.JahiaObject getJahiaObject(ObjectKey key) {

        org.jahia.content.JahiaObject jahiaObject = null;
        if(cache==null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        String entryKey = CACHE_KEY_PREFIX + key;
        if(cache!=null) {
            jahiaObject = (org.jahia.content.JahiaObject) cache.get(entryKey);
        }
        if(jahiaObject==null) {
            JahiaObject hibJahiaObject = dao.findByPK(new JahiaObjectPK(key.getType(),new Integer(key.getIDInType())));
            if ( hibJahiaObject != null ){
                try {
                    jahiaObject = hibJahiaObject.toJahiaObject();
                } catch ( Exception t){
                    log.debug("Error loading JahiaObject", t);
                }
                if(jahiaObject!=null){
                    cache.put(entryKey,jahiaObject);
                }
            }
        }
        return jahiaObject;
    }

    public void delete(ObjectKey key) {
        dao.delete(new JahiaObjectPK(key.getType(),new Integer(key.getIDInType())));
        this.flushCache(new JahiaObjectPK(key.getType(),new Integer(key.getIDInType())));
    }

    public JahiaObjectDelegate getJahiaObjectDelegate(ObjectKey objectKey){
        JahiaObject hibJahiaObject = null;
        JahiaObjectDelegate jahiaObjectDelegate = null;
        if(cache==null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        String entryKey = OBJECTDELEGATE_KEY_PREFIX + objectKey;
        if(cache!=null) {
            jahiaObjectDelegate = (JahiaObjectDelegate) cache.get(entryKey);
        }
        if(jahiaObjectDelegate==null) {
            try {
                hibJahiaObject = dao.findByPK(new JahiaObjectPK(objectKey.getType(),
                                                                new Integer(objectKey.getIDInType())));
                jahiaObjectDelegate = convertJahiaObjectToJahiaObjectDelegate(objectKey, hibJahiaObject);
                if(cache != null) {
                    cache.put(entryKey,jahiaObjectDelegate);
                }
            } catch (Exception t) {
            }
        }
        try {
            if ( jahiaObjectDelegate != null ){
                return (JahiaObjectDelegate)jahiaObjectDelegate.clone();
            }
        } catch ( Exception t ) {
            logger.error("Exception occured cloning object 1",t);
        }
        return null;
    }

    public JahiaObjectDelegate getJahiaObjectDelegate(ObjectKey objectKey, int batchSize){
        JahiaObject hibJahiaObject = null;
        JahiaObjectDelegate jahiaObjectDelegate = null;
        if(cache==null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        String entryKey = OBJECTDELEGATE_KEY_PREFIX + objectKey;
        if(cache!=null) {
            jahiaObjectDelegate = (JahiaObjectDelegate) cache.get(entryKey);
        }
        if(jahiaObjectDelegate==null) {
            try {
                List objs = dao.loadJahiaObjects(new JahiaObjectPK(objectKey.getType(),
                        new Integer(objectKey.getIDInType())),batchSize);
                if ( objs != null && !objs.isEmpty() ){
                    Iterator it = objs.iterator();
                    JahiaObjectDelegate jobjd;
                    while ( it.hasNext() ){
                        hibJahiaObject = (JahiaObject)it.next();
                        jobjd = convertJahiaObjectToJahiaObjectDelegate(ObjectKey
                                .getInstance(hibJahiaObject.getComp_id().getKey()), hibJahiaObject);
                        if(cache != null) {
                            entryKey = OBJECTDELEGATE_KEY_PREFIX + jobjd.getObjectKey();
                            cache.put(entryKey,jobjd);
                        }
                        if ( jahiaObjectDelegate == null && jobjd.getObjectKey().equals(objectKey) ){
                            jahiaObjectDelegate = jobjd;
                        }
                    }
                }
            } catch (Exception t) {
                logger.debug("Exception loading JahiaObjects",t);
            }
        }
        try {
            if ( jahiaObjectDelegate != null ){
                return (JahiaObjectDelegate)jahiaObjectDelegate.clone();
            }
        } catch ( Exception t ) {
            logger.error("Exception occured cloning object",t);
        }
        return null;
    }

    public void addJahiaObjectDelegateInCache(JahiaObjectDelegate jahiaObjectDelegate){
        if ( jahiaObjectDelegate == null ){
            return;
        }
        try {
            jahiaObjectDelegate = (JahiaObjectDelegate)jahiaObjectDelegate.clone();
        } catch ( Exception t ){
            logger.error("Exception occured cloning object",t);
            return;
        }
        if(cache==null) {
            try {
                cache = cacheService.createCacheInstance(CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error creating cache", e);
            }
        }
        String entryKey = OBJECTDELEGATE_KEY_PREFIX + jahiaObjectDelegate.getObjectKey();
        if(cache!=null && !cache.containsKey(entryKey)) {
            cache.put(entryKey,jahiaObjectDelegate);
        }
    }

    /**
     * Returns the JahiaObjectDelegate instance that owns the given rule
     * @param ruleId
     * @return jahiaObjectDelegate
     */
    public Collection getJahiaObjectDelegateByRuleId(int ruleId){
        Collection hibJahiaObjects = dao.findByRetentionRule(ruleId);
        List res = new ArrayList();
        try {
            for (Iterator iterator = hibJahiaObjects.iterator(); iterator.hasNext();) {
                JahiaObject hibJahiaObject = (JahiaObject) iterator.next();
                JahiaObjectDelegate delegate = convertJahiaObjectToJahiaObjectDelegate(hibJahiaObject.getComp_id().toObjectKey(),
                    hibJahiaObject);
                addJahiaObjectDelegateInCache(delegate);
                res.add(delegate);
            }
            return res;
        } catch ( Exception t){
            logger.debug("Error converting JahiaObject to JahiaObjectDelegate",t);
        }
        return res;
    }

    public void save(JahiaObjectDelegate delegate){
        JahiaObject hibJahiaObject = null;

        try {
            hibJahiaObject = dao.findByPK(new JahiaObjectPK(delegate.getObjectKey().getType(),
                    new Integer(delegate.getObjectKey().getIDInType())));
        } catch ( Exception t ){
        }
        boolean timeBPStateChanged = false;
        if ( hibJahiaObject != null ){
            timeBPStateChanged = ( hibJahiaObject.getTimeBPState().intValue() != delegate.getTimeBPState().intValue()  );
            hibJahiaObject.setTimeBPState(delegate.getTimeBPState());
            //timeBPStateChanged = ( timeBPStateChanged || hibJahiaObject.getValidFromDate().longValue() != delegate.getValidFromDate().longValue() );
            hibJahiaObject.setValidFromDate(delegate.getValidFromDate());
            //timeBPStateChanged = ( timeBPStateChanged || hibJahiaObject.getValidToDate().longValue() != delegate.getValidToDate().longValue() );
            hibJahiaObject.setValidToDate(delegate.getValidToDate());
            if ( delegate.getSiteId().intValue()>0 ){
                hibJahiaObject.setSiteId(delegate.getSiteId());
            } else {
                //hibJahiaObject.setSite(null);
            }
            if ( delegate.getRule()!=null ){
                hibJahiaObject.setRetentionRule(retentionRuleDao.findByPK(delegate.getRule().getId()));
            } else {
                hibJahiaObject.setRetentionRule(null);
            }
        }
        this.dao.save(hibJahiaObject);
        this.flushCache(hibJahiaObject.getComp_id());
        if ( this.cache != null ){
            try {
                //commented due to performance reasons: JAHIA-2638
                //this.cache.put(CACHE_KEY_PREFIX + delegate.getObjectKey(),hibJahiaObject.toJahiaObject());
                this.cache.put(OBJECTDELEGATE_KEY_PREFIX + delegate.getObjectKey(),delegate);
            } catch ( Exception t ){
            }
        }
        if ( timeBPStateChanged ){
            try {
                TimeBasedPublishingJahiaObject contentObject = (TimeBasedPublishingJahiaObject)
                        hibJahiaObject.toJahiaObject();
                contentObject.notifyStateChanged();
                ProcessingContext context = Jahia.getThreadParamBean();
                if ( context == null ){
                    if ( hibJahiaObject.getSiteId() != null ){
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
                                .getSite(hibJahiaObject.getSiteId().intValue());
                        if ( site != null ){
                            JahiaUser user = getAdminUser(site.getID());
                            context =
                                    new ProcessingContext(org.jahia.settings.SettingsBean.getInstance(),
                                            System.currentTimeMillis(),
                                            site,user,site.getHomeContentPage());
                        }
                    }
                }
                if ( context != null ) {
                    ContentObject curContentObject = (ContentObject)hibJahiaObject.toJahiaObject();

                    if (logger.isDebugEnabled()) logger.debug("Time publishing triggering invalidation of "+curContentObject);

                    JahiaEvent objectUpdatedEvent = new JahiaEvent(this, context, curContentObject);
                    ServicesRegistry.getInstance ().getJahiaEventService ()
                        .fireContentObjectUpdated(objectUpdatedEvent);                    
                }
            } catch ( Exception t){
            }
        }
    }


    private JahiaObjectDelegate convertJahiaObjectToJahiaObjectDelegate(ObjectKey objectKey,
                                                                        JahiaObject hibJahiaObject){
        JahiaObjectDelegate delegate = new JahiaObjectDelegate();
        delegate.setObjectKey(objectKey);
        if (hibJahiaObject == null ){
            return delegate;
        }
        delegate.setTimeBPState(hibJahiaObject.getTimeBPState());
        if ( hibJahiaObject.getSiteId() != null ){
            delegate.setSiteId(hibJahiaObject.getSiteId());
        }
        if (hibJahiaObject.getValidFromDate()!= null){
            delegate.setValidFromDate(hibJahiaObject.getValidFromDate());
        }
        if (hibJahiaObject.getValidToDate()!= null){
            delegate.setValidToDate(hibJahiaObject.getValidToDate());
        }
        if ( hibJahiaObject.getRetentionRule() != null ){
            try {
                delegate.setRule(hibJahiaObject.getRetentionRule().getRetentionRule());
            } catch ( Exception t){
            }
        }
        return delegate;
    }

    public void flushCacheByObjectKey(ObjectKey key) {
        flushCache(new JahiaObjectPK(key.getType(),
                new Integer(Integer.parseInt(key.getIDInType()))));
    }

    public void flushCache(JahiaObjectPK key) {
        if(cache != null) {
            synchronized(cache){
                try {
                    cache.remove(CACHE_KEY_PREFIX+key.toObjectKey());
                    cache.remove(OBJECTDELEGATE_KEY_PREFIX+key.toObjectKey());
                } catch (ClassNotFoundException e) {
                    log.warn("Error removing from cache", e);
                }
                cache.remove(key.getType());
                cache.flush();
            }
        }
    }

    protected JahiaUser getAdminUser(int siteId){
        JahiaGroup adminGroup = ServicesRegistry.getInstance().getJahiaGroupManagerService()
                .lookupGroup(siteId, JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME);
        Set members = adminGroup.getRecursiveUserMembers();
        if ( members.iterator().hasNext() ){
            return (JahiaUser)members.iterator().next();
        }
        return null;
    }

}


