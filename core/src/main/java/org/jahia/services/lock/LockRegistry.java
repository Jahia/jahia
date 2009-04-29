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
package org.jahia.services.lock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaLockManager;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.usermanager.JahiaUser;

/**
 *
 * <p>Title: Jahia locking system implementation.</p>
 * <p>Description:
 * This registry store the Jahia lock defined in the engine or administration
 * procedure.
 *
 * todo Make this lock registry persistent for Jahia load balancing system.
 *
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2003)</p>
 * <p>Company: Jahia Solutions S�rl</p>
 * @author MAP
 * @version 1.0
 */
public class LockRegistry extends LockService {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(LockRegistry.class);

    public final static String OWNER = "owner";
    public final static String ID = "id";
    public final static String STEALED = "stealed";
    public final static String TIME_REMAINING = "timeRemaining";
    public final static String TIMEOUT = "timeout";

    private static LockRegistry lockRegistryInstance;

    private JahiaLockManager manager;
    private Cache<ObjectKey, Map<String, Set<Lock>>> lockAlreadyAcquiredMap;
    private CacheService cacheService;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    private String serverId;

    public void setLockManager(JahiaLockManager manager) {
        this.manager = manager;
    }

    /**
     * Return the unique registry instance. If the instance does not exist,
     * a new instance is created.
     *
     * @return The unique lock registry instance.
     */
    public synchronized static LockRegistry getInstance () {
        if (lockRegistryInstance == null) {
            lockRegistryInstance = new LockRegistry();
        }
        return lockRegistryInstance;
    }

    public void start() throws JahiaInitializationException {
        lockAlreadyAcquiredMap = cacheService.createCacheInstance("LockAlreadyAcquiredMap");
        LockPrerequisites.getInstance().setCacheService(cacheService);
        purgeLocksForServer();
    }

    public void stop() {}

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    /**
     * Acquire a lock if the appropriate prerequisites are complete.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock ID
         * @param timeout The period in second during which the lock is valid. For a
     * non expiration time lock use the constant NO_EXPIRATION_TIME.
     * @return True if the lock was acquired, false otherwise.
     */
    public boolean acquire (final LockKey lockKey,
                                         final JahiaUser owner,
                                         final String lockID,
                                         final int timeout) {
        return acquire(lockKey, owner, lockID, timeout, true);
    }

    /**
     * Acquire a lock if the appropriate prerequisites are complete.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock ID
         * @param timeout The period in second during which the lock is valid. For a
     * non expiration time lock use the constant NO_EXPIRATION_TIME.
     * @return True if the lock was acquired, false otherwise.
     */
    public boolean acquire (final LockKey lockKey,
                                         final JahiaUser owner,
                                         final String lockID,
                                         final int timeout,
                                         final boolean setServerId) {
        if (LockPrerequisites.getInstance().isLockAcquirable(lockKey, owner,
            lockID, false)) {
            Lock lock = getLock(lockKey, lockID);
            if (lock == null) {
                lock = new Lock(owner, lockID, timeout);
            } else {
                lock.resetTimeout(timeout);
                lock.setStealed(false);
            }
            putLock(lockKey, lock, setServerId);

            return true;
        }
        return false;
    }

    public void setServerId(LockKey lockKey, String lockId) {
        Lock lock = getLock(lockKey, lockId);
        if (lock != null) {
            putLock(lockKey, lock);
        }
    }

    /**
     * Test if a lock is acquireable if the appropriate prerequisites are
     * complete.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock ID
     * @return true if the lock is acquireable, false otherwise.
     */
    public boolean isAcquireable (final LockKey lockKey,
                                               final JahiaUser owner,
                                               final String lockID) {
        return LockPrerequisites.getInstance().isLockAcquirable(lockKey, owner, lockID, true);
    }

    /**
     * Release the acquired lock. The lock should be in the correct context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock identifier.
     */
    public void release (final LockKey lockKey,
                                      final JahiaUser owner,
                                      final String lockID) {
        if (canRelease(lockKey, owner, lockID)) {
            removeLock(lockKey, lockID);
            LockPrerequisites.getInstance().resetPrerequisite(lockKey);
        }
    }

    /**
     * Return a lock information from a lock stored in the registry.
     *
     * @param lockKey The lock key identifying the lock.
     * @return The lock attributes
     */
    public List<Map<String, Serializable>> getInfo (final LockKey lockKey) {
        List<Map<String, Serializable>> results = new ArrayList<Map<String, Serializable>>();
        for (Lock lock : getLocks(lockKey)) {
            lock = getLock(lockKey, lock.getID());
            final Map<String, Serializable> lockInfo = new HashMap<String, Serializable>();
            lockInfo.put(OWNER, lock.getOwner());
            lockInfo.put(ID, lock.getID());
            lockInfo.put(STEALED, Boolean.valueOf(lock.isStealed()));
            lockInfo.put(TIME_REMAINING, getTimeRemaining(lockKey, lock.getID()));
            lockInfo.put(TIMEOUT, new Long(lock.getTimeout()));
            results.add(lockInfo);
        }
        return results;
    }

    /**
     * Get the remaining time from a lock stored in the registry.
     *
     * @param lockKey The lock key identifying the lock.
     * @return The lock tremaining time in second.
     */
    public Long getTimeRemaining (LockKey lockKey, String lockID) {
        Lock lock = getLock(lockKey, lockID);
        if (lock == null) {
            return null;
        }
        return new Long(lock.getTimeRemaining());
    }

    /**
     * Change the lock context meaning that it can be stolen. Actually we admit
     * that only user with administration priviliges has the right to perform
     * this operation.
     *
     * @param lockKey The lock key identifying the lock.
     * @param newOwner The new lock owner.
     * @param lockID The new lock identifier.
     */
    public void steal (final LockKey lockKey,
                                    final JahiaUser newOwner,
                                    final String lockID) {
        if (hasAdminRights(lockKey, newOwner)) {
            synchronized (this) {
                for (Lock lock : getLocks(lockKey)) {
                    lock.setStealed(true);
                    putLock(lockKey, lock);
                }

                LockPrerequisites.getInstance().resetPrerequisite(lockKey);
            }
        }
    }

    /**
	 * Force to remove the lock from the registry meaning that it can be broken
	 * (nuked). Actually we admit that only user with administration priviliges
	 * has the right to perform this operation.
	 * 
	 * @param lockKey
	 *            The lock key identifying the lock.
	 * @param owner
	 *            The lock owner.
	 * @param lockID
	 *            The lock identifier.
	 * 
	 * Sorry, should be called "break" but it is a reserved word ;)
	 */
    public void nuke (final LockKey lockKey,
                                   final JahiaUser owner,
                                   final String lockID) {
        if (hasAdminRights(lockKey, owner)) {
            synchronized (this) {
                removeLock(lockKey, lockID);
                LockPrerequisites.getInstance().resetPrerequisite(lockKey);
            }
        }
    }

    /**
	 * Return is the lock has been stolen or not.
	 * 
	 * @param lockKey
	 *            The lock key identifying the lock.
	 * @param contextId
	 * @return True is the lock has benn stolen, false otherwise.
	 */
    public boolean isStealed(final LockKey lockKey,
                             final String contextId) {
        Lock lock = getLock(lockKey, contextId);
        if (lock == null) {
            return false;
        }
        return lock.isStealed();
    }

    /**
     * Return if a lock has already been acquired.
     *
     * @param lockKey The lock key identifying the lock.
     * @return True if the lock has already been acquired, false otherwise.
     */
    public boolean isAlreadyAcquired (final LockKey lockKey) {
        List<Lock> locks = getLocks(lockKey);
        if (locks.isEmpty()) {
            return false; // By §convention
        }

        boolean alreadyAcquired = false;

        for (Lock lock : locks) {
            if (lock.hasExpired()) {
                removeLock(lockKey, lock.getID());
            } else {
                lock = getLock(lockKey, lock.getID());
                if (!lock.isStealed()) {
                    alreadyAcquired = true;
                }
            }
        }
        return alreadyAcquired;
    }

    /**
     * Define if a lock has been stolen in a specified context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
         * @return True if the lock has been stolen in the context, false otherwise.
     */
    public boolean isStealedInContext (final LockKey lockKey,
                                                    final JahiaUser owner,
                                                    final String lockID) {
        return (isStealed(lockKey, lockID));
    }

    /**
     * Define if a lock has already been acquired in a specified context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock has been already acquired in the context,
     * false otherwise.
     */
    public boolean isAlreadyAcquiredInContext (final LockKey lockKey,
                                                            final JahiaUser owner,
                                                            final String lockID) {
        Lock lock = getLock(lockKey, lockID);
        if (lock == null) {
            return false; // By convention
        }
        if (lock.hasExpired()) {
            removeLock(lockKey, lockID);
            LockPrerequisites.getInstance().resetPrerequisite(lockKey);
            return false;
        }
        if (lock.isStealed()) {
            return false;
        }

        return lock.getOwner().getUserKey().equals(owner.getUserKey()) && lock.getID().equals(lockID);
    }

    public Set<String> getContexts(final LockKey lockKey) {
        Map<String, Set<Lock>> allLocks = (Map<String, Set<Lock>>) lockAlreadyAcquiredMap.get(lockKey.getObjectKey());

        if (allLocks == null) {
            allLocks = getLocks(lockKey.getName(), lockKey.getId());
            lockAlreadyAcquiredMap.put(lockKey.getObjectKey(), allLocks);
        }

        Set<Lock> locks = allLocks.get(lockKey.getAction());

        if (locks == null || locks.isEmpty()) {
            return new HashSet<String>();
        }
        Set<String> res = new HashSet<String>();
        for (Lock lock : locks) {
            if (lock.hasExpired()) {
                removeLock(lockKey, lock.getID());
            } else {
                lock = getLock(lockKey, lock.getID());
                if (lock != null && !lock.isStealed()) {
                    res.add(lock.getID());
                }
            }
        }
        return res;
    }

    public Map<String, Set<Lock>> getLocksOnObject(final LockKey lockKey) {
        Map<String, Set<Lock>> allLocks = (Map<String, Set<Lock>>) lockAlreadyAcquiredMap.get(lockKey.getObjectKey());

        if (allLocks == null) {
            allLocks = getLocks(lockKey.getName(), lockKey.getId());
            lockAlreadyAcquiredMap.put(lockKey.getObjectKey(), allLocks);
        }

        return allLocks;
    }

    /**
     * Define if a lock can be released or not.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock can be released, false otherwise.
     */
    public  boolean canRelease (final LockKey lockKey,
                                            final JahiaUser owner,
                                            final String lockID) {
        Lock lock = getLock(lockKey, lockID);
        if (lock == null) {
            return true; // By convention
        }
        if (lock.hasExpired()) {
            synchronized (this) {
                removeLock(lockKey, lockID);
                LockPrerequisites.getInstance().resetPrerequisite(lockKey);
            }
            return true;
        }

        return lock.getOwner().getUserKey().equals(owner.getUserKey()) && lock.getID().equals(lockID);
    }

    /**
     * Define is the Jahia user has admin rights on a specified lock.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @return True if the user has admin rights, false otherwise.
     */
    public boolean hasAdminRights (final LockKey lockKey,
                                                final JahiaUser owner) {
        if (lockKey == null) return false;
        ObjectKey objectKey = lockKey.getObjectKey();
        // Try to get the content object from lock key if exists...
        if (objectKey != null) {
            try {
                ContentObject contentObject = (ContentObject) JahiaObject.
                                              getInstance(objectKey);
                if (!lockKey.getType().startsWith(LockKey.WORKFLOW_ACTION) && contentObject.checkAdminAccess(owner)) {
                    return true;
                }
                if (owner.isAdminMember(contentObject.getSiteID())) {
                    return true;
                }
            } catch (ClassNotFoundException cnfe) {
                logger.debug("Object '" + lockKey.getType() + "' not found !",
                             cnfe);
            }
        }
        // ... is the user root or site admin ?
        else if (owner.isAdminMember(0)) {
            return true;
        }
        return false;
    }

    /**
     * Purge all locks and lock prerequisites from memory and database,
     * effectively freeing all the objects. This is a powerful operation and
     * must be used with care as it might cause problems when people already
     * have open popups.
     */
    public synchronized void purgeLocks() {
        LockPrerequisites.getInstance().flush();
        removeAllLocks();
    }

    public synchronized void purgeLockForContext(String contextId) {
        manager.purgeLockForContext(contextId);
        LockPrerequisites.getInstance().flush();
    }

    public synchronized void purgeLocksForServer() {
        manager.purgeLockForServer(serverId);
        LockPrerequisites.getInstance().flush();
    }

    /**
     * Default constructor
     */
    protected LockRegistry () {
        logger.debug("Lock registry has been instanciated");
    }

    private void putLock (LockKey lockKey, Lock lock) {
        putLock(lockKey, lock, true);
    }

    private void putLock (LockKey lockKey, Lock lock, boolean useServer) {
        manager.save(lockKey, lock, useServer ? serverId : null);

        Map<String, Set<Lock>> allLocks = (Map<String, Set<Lock>>) lockAlreadyAcquiredMap.get(lockKey.getObjectKey());
        if (allLocks != null) {
            Set<Lock> s = allLocks.get(lockKey.getAction());
            if (s == null) {
                s = new HashSet<Lock>();
                allLocks.put(lockKey.getAction(), s);
            }
            s.add(lock);
            lockAlreadyAcquiredMap.put(lockKey.getObjectKey(), allLocks);
        }
    }

    private List<Lock> getLocks (LockKey lockKey) {
        return manager.getLocks(lockKey);
    }

    public Set<LockKey> getLockKeys (String action) {
        return manager.getLockKeys(action);
    }

    private Map<String, Set<Lock>> getLocks (String name, int id) {
        return manager.getLocks(name, id);
    }

    private Lock getLock (LockKey lockKey, String contextId) {
        return manager.getLock(lockKey, contextId);
    }

    private void removeLock (LockKey lockKey, String contextId) {
        manager.remove(lockKey, contextId);
    }

    private void removeAllLocks() {
        manager.removeAllLocks();
    }
}