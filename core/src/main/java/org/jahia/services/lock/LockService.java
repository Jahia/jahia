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

import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class LockService extends JahiaService {

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
    public abstract boolean acquire (LockKey lockKey, JahiaUser owner,
                                     String lockID, int timeout);

    public abstract boolean acquire (LockKey lockKey, JahiaUser owner,
                                     String lockID, int timeout, boolean setServerId);

    public abstract void setServerId(LockKey lockKey, String lockId);


    /**
     * Test if a lock is acquireable if the appropriate prerequisites are
     * complete.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock ID
     * @return true if the lock is acquireable, false otherwise.
     */
    public abstract boolean isAcquireable (LockKey lockKey, JahiaUser owner,
                                           String lockID);

    /**
     * Release the acquired lock. The lock should be in the correct context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock identifier.
     */
    public abstract void release(LockKey lockKey, JahiaUser owner,
                                 String lockID);

    /**
     * Return a lock information from a lock stored in the registry.
     *
     * @param lockKey The lock key identifying the lock.
     * @return The lock attributes
     */
    public abstract List<Map<String, Serializable>> getInfo(LockKey lockKey);

    /**
     * Get the remaining time from a lock stored in the registry.
     *
     * @param lockKey The lock key identifying the lock.
     * @param contextId
     * @return The lock tremaining time in second.
     */
    public abstract Long getTimeRemaining(LockKey lockKey, String contextId);

    /**
     * Change the lock context meaning that it can be stolen. Actually we admit
     * that only user with administration priviliges has the right to perform
     * this operation.
     *
     * @param lockKey The lock key identifying the lock.
     * @param newOwner The new lock owner.
     * @param lockID The new lock identifier.
     */
    public abstract void steal(LockKey lockKey, JahiaUser newOwner,
                               String lockID);

    /**
     * Force to remove the lock from the registry meaning that it can be broken
     * (nuked). Actually we admit that only user with administration priviliges
     * has the right to perform this operation.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     *
     * Sorry, should be called "break" but it is a reserved word ;)
     */
    public abstract void nuke(LockKey lockKey, JahiaUser owner, String lockID);

    /**
     * Return is the lock has been stolen or not.
     *
     * @param lockKey The lock key identifying the lock.
     * @param contextId
     * @return True is the lock has benn stolen, false otherwise.
     */
    public abstract boolean isStealed(LockKey lockKey, String contextId);

    /**
     * Return if a lock has already been acquired.
     *
     * @param lockKey The lock key identifying the lock.
     * @return True if the lock has already been acquired, false otherwise.
     */
    public abstract boolean isAlreadyAcquired(LockKey lockKey);

    /**
     * Define if a lock has been stolen in a specified context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock has been stolen in the context, false otherwise.
     */
    public abstract boolean isStealedInContext(LockKey lockKey, JahiaUser owner, String lockID);

    /**
     * Define if a lock has already been acquired in a specified context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock has been already acquired in the context,
     * false otherwise.
     */
    public abstract boolean isAlreadyAcquiredInContext(LockKey lockKey,
                                                       JahiaUser owner, String lockID);

    /**
     * Define if a lock can be released or not.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock can be released, false otherwise.
     */
    public abstract boolean canRelease(LockKey lockKey, JahiaUser owner,
                                       String lockID);

    /**
     * Define is the Jahia user has admin rights on a specified lock.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @return True if the user has admin rights, false otherwise.
     */
    public abstract boolean hasAdminRights(LockKey lockKey, JahiaUser owner);

    public abstract void purgeLockForContext(String contextId);

    public abstract void purgeLocksForServer();

    /**
     * Purge all locks and lock prerequisites from memory and database,
     * effectively freeing all the objects. This is a powerful operation and
     * must be used with care as it might cause problems when people already
     * have open popups.
     */
    public abstract void purgeLocks();

    public abstract Set<String> getContexts(LockKey lockKey);

    public abstract Map<String, Set<Lock>> getLocksOnObject(LockKey lockKey);

}