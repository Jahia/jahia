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
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.dao.JahiaLockDAO;
import org.jahia.hibernate.model.JahiaLock;
import org.jahia.hibernate.model.JahiaLockPK;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.lock.Lock;
import org.jahia.services.lock.LockKey;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.orm.ObjectRetrievalFailureException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Rincevent
 * Date: 19 avr. 2005
 * Time: 17:24:49
 * To change this template use File | Settings | File Templates.
 */
public class JahiaLockManager {
    private Log log = LogFactory.getLog(JahiaLockManager.class);
    public static final String LOCK_CACHE_NAME = "JahiaLockManagerCache";
    private JahiaLockDAO dao = null;
    private Cache<String, Lock> fast = null;
    private CacheService cacheService = null;

    public void setJahiaLockDAO(JahiaLockDAO dao) {
        this.dao = dao;
    }

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void save(LockKey lockKey, Lock lock, String serverId) {
        JahiaLockPK pk = new JahiaLockPK(lockKey.getName(), new Integer(lockKey.getId()), lockKey.getAction(), lock.getID());
        final JahiaLock jahiaLock = new JahiaLock();
        jahiaLock.setComp_id(pk);
        jahiaLock.setOwner(lock.getOwner().getUserKey());
        jahiaLock.setStolen(Boolean.toString(lock.isStealed()));
        jahiaLock.setServerId(serverId);
        long secondsTimeout = lock.getTimeout() / 1000;
        jahiaLock.setTimeout(new Integer(new Long(secondsTimeout).intValue()));
        jahiaLock.setExpirationDate(new Long(lock.getExpirationDate()));
        dao.merge(jahiaLock);
        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(LOCK_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error initializing cache", e);
            }
        }
        if (fast != null) {
            fast.put(lockKey+lock.getID(), lock);
        }
    }

    public List<Lock> getLocks(LockKey lockKey) {
        List<Lock> results = new ArrayList<Lock>();
        try {
            List<JahiaLock> locks = dao.findByLockKey(lockKey.getName(), new Integer(lockKey.getId()), lockKey.getAction());
            for (JahiaLock jahiaLock : locks) {
                JahiaUser owner = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(jahiaLock.getOwner());
                results.add(new Lock(owner, jahiaLock.getComp_id().getContextID(), jahiaLock.getTimeout().intValue(),
                        jahiaLock.getExpirationDate().longValue(), Boolean.getBoolean(jahiaLock.getStolen())));
            }
        } catch (ObjectRetrievalFailureException e) {
            log.debug("There are no locks for lockKey: " + lockKey, e);            
        } catch (Exception e) {
            log.warn("Error getting locks", e);
        }
        return results;
    }

    public Set<LockKey> getLockKeys(String action) {
        Set<LockKey> results = new HashSet<LockKey>();
        try {
            for (Object[] r : dao.findKeysByLockAction(action)) {
                LockKey k = LockKey.composeLockKey(action+"_"+r[0], ((Integer)r[1]).intValue());
                results.add(k);
            }
        } catch (ObjectRetrievalFailureException e) {
            log.debug("There are no locks for action: " + action, e);            
        } catch (Exception e) {
            log.warn("Error getting lock keys", e);
        }
        return results;
    }

    public Map<String, Set<Lock>> getLocks(String name, int id) {
        Map<String, Set<Lock>> results = new HashMap<String, Set<Lock>>();
        try {
            for (JahiaLock jahiaLock : dao.findByLockNameAndId(name, new Integer(id))) {
                LockKey k = LockKey.composeLockKey(jahiaLock.getComp_id().getAction()+"_"+name, id);
                Set<Lock> s = results.get(k.getAction());
                if (s == null) {
                    s = new HashSet<Lock>();
                    results.put(k.getAction(),s);
                }
                JahiaUser owner = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(jahiaLock.getOwner());
                s.add(new Lock(owner, jahiaLock.getComp_id().getContextID(), jahiaLock.getTimeout().intValue(),
                        jahiaLock.getExpirationDate().longValue(), Boolean.getBoolean(jahiaLock.getStolen())));
            }
        } catch (ObjectRetrievalFailureException e) {
            log.debug("There are no locks for name: " + name
                    + " id: " + id, e);            
        } catch (Exception e) {
            log.warn("Error getting locks", e);
        }
        return results;
    }

//    public Map getLocks(String prefix, String path) {
//        Map results = new HashMap();
//        try {
//            List locks = dao.findByLockPrefixAndPath(prefix, path);
//            for (Iterator iterator = locks.iterator(); iterator.hasNext();) {
//                JahiaLock jahiaLock = (JahiaLock) iterator.next();
//                LockKey k = LockKey.composeLockKey(jahiaLock.getComp_id().getAction()+"_"+jahiaLock.getComp_id().getName(),
//                        jahiaLock.getComp_id().getTargetID().intValue(), -1);
//                Set s = (Set) results.get(k.getAction());
//                if (s == null) {
//                    s = new HashSet();
//                    results.put(k.getAction(),s);
//                }
//                JahiaUser owner = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(jahiaLock.getOwner());
//                s.add(new Lock(owner, jahiaLock.getComp_id().getContextID(), jahiaLock.getTimeout().intValue(),
//                        jahiaLock.getExpirationDate().longValue(), Boolean.getBoolean(jahiaLock.getStolen())));
//            }
//        } catch (Exception e) {
//
//        }
//        return results;
//    }
//
    public Lock getLock(LockKey lockKey, String contextId) {
        Lock lock = null;
        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(LOCK_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error initializing cache", e);
            }
        }
        if (fast == null || !fast.containsKey(lockKey+contextId)) {
            try {
                JahiaLock jahiaLock = dao.findByPK(new JahiaLockPK(lockKey.getName(), new Integer(lockKey.getId()), lockKey.getAction(), contextId));
                JahiaUser owner = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(jahiaLock.getOwner());
                lock = new Lock(owner, contextId, jahiaLock.getTimeout().intValue(),
                        jahiaLock.getExpirationDate().longValue(), Boolean.getBoolean(jahiaLock.getStolen()));
            } catch (ObjectRetrievalFailureException e) {
                log.debug("There is no lock for lockKey: " + lockKey
                        + " contextId: " + contextId, e);
            } catch (Exception e) {
                log.warn("Error getting lock", e);
            }
            if (fast != null)
                fast.put(lockKey+contextId, lock);
        } else {
            lock = (Lock) fast.get(lockKey+contextId);
        }
        return lock;
    }

    public void remove(LockKey lockKey, String contextId) {
        try {
            dao.delete(dao.findByPK(new JahiaLockPK(lockKey.getName(), new Integer(lockKey.getId()), lockKey.getAction(), contextId)));
            if (fast == null) {
                try {
                    fast = cacheService.createCacheInstance(LOCK_CACHE_NAME);
                } catch (JahiaInitializationException e) {
                    log.error("Error initializing cache", e);
                }
            }
            if (fast != null)
                fast.remove(lockKey+contextId);
        } catch (ObjectRetrievalFailureException e) { // nothing to delete
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void purgeLockForContext(String contextId) {
        dao.purgeLockForContext(contextId);
        if (fast != null)
            fast.flush();
    }

    public void purgeLockForServer(String serverId) {
        dao.purgeLockForServer(serverId);
        if (fast != null)
            fast.flush();
    }

    public void removeAllLocks() {
        dao.deleteAllLocks();
        if (fast == null) {
            try {
                fast = cacheService.createCacheInstance(LOCK_CACHE_NAME);
            } catch (JahiaInitializationException e) {
                log.error("Error initializing cache", e);
            }
        }
        if (fast != null)
            fast.flush();
    }
}
