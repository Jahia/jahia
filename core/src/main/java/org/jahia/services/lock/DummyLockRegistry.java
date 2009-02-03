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

package org.jahia.services.lock;

import org.jahia.services.usermanager.JahiaUser;

import java.util.*;

/**
 *
 * This is a dummy lock service that is used to never lock anything.
 * It can be used in place of default lock service as long as it is not completely working well.
 *
 * @version 1.0
 */
public class DummyLockRegistry extends LockService {

    /**
     * Default constructor
     */
    private DummyLockRegistry() {
        logger.debug("Lock registry has been instanciated");
    }

    private static DummyLockRegistry lockRegistryInstance;

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(DummyLockRegistry.class);

    /**
     * Return the unique registry instance. If the instance does not exist,
     * a new instance is created.
     *
     * @return The unique lock registry instance.
     */
    public synchronized static DummyLockRegistry getInstance() {
        if (lockRegistryInstance == null) {
            lockRegistryInstance = new DummyLockRegistry();
        }
        return lockRegistryInstance;
    }

    public void start() {}

    public void stop() {}

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
    public synchronized boolean acquire(LockKey lockKey, JahiaUser owner,
                                        String lockID, int timeout) {
        return true;
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
    public synchronized boolean isAcquireable(LockKey lockKey, JahiaUser owner,
                                              String lockID) {
        return true;
    }

    /**
     * Reserve a lock with a defined expiration delay after which the lock is
     * acquired to the user.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock identifier.
     * @param timeout Lock time out.
     * @param delay Lock delay...
     * @return True if the lock can be reserved, false otherwise.
     *
     * todo To implement
     */
    public synchronized boolean reserve(LockKey lockKey, JahiaUser owner,
                                        String lockID, int timeout, int delay) {
        return false;
    }

    /**
     * Release the acquired lock. The lock should be in the correct context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner
     * @param lockID The lock identifier.
     */
    public synchronized void release(LockKey lockKey, JahiaUser owner,
                                     String lockID) {
    }

    /**
     * Return a lock information from a lock stored in the registry.
     *
     * @param lockKey The lock key identifying the lock.
     * @return The lock attributes
     */
    public synchronized List getInfo(LockKey lockKey) {
        return new ArrayList();
    }

    /**
     * Get the remaining time from a lock stored in the registry.
     *
     * @param lockKey The lock key identifying the lock.
     * @param contextId
     * @return The lock tremaining time in second.
     */
    public synchronized Long getTimeRemaining(LockKey lockKey, String contextId) {
        return new Long(0);
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
    public synchronized void steal(LockKey lockKey, JahiaUser newOwner,
                                   String lockID) {
    }

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
    public synchronized void nuke(LockKey lockKey, JahiaUser owner, String lockID) {
    }

    /**
     * Return is the lock has been stolen or not.
     *
     * @param lockKey The lock key identifying the lock.
     * @param contextId
     * @return True is the lock has benn stolen, false otherwise.
     */
    public boolean isStealed(LockKey lockKey, String contextId) {
        return true;
    }

    /**
     * Return if a lock has already been acquired.
     *
     * @param lockKey The lock key identifying the lock.
     * @return True if the lock has already been acquired, false otherwise.
     */
    public synchronized boolean isAlreadyAcquired(LockKey lockKey) {
        return false;
    }

    /**
     * Define if a lock has been stolen in a specified context.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock has been stolen in the context, false otherwise.
     */
    public synchronized boolean isStealedInContext(LockKey lockKey, JahiaUser owner, String lockID) {
        return false;
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
    public synchronized boolean isAlreadyAcquiredInContext(LockKey lockKey,
                                                           JahiaUser owner, String lockID) {
        return false;
    }

    /**
     * Define if a lock can be released or not.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @param lockID The lock identifier.
     * @return True if the lock can be released, false otherwise.
     */
    public synchronized boolean canRelease(LockKey lockKey, JahiaUser owner,
                                           String lockID) {
        return true;
    }

    /**
     * Define is the Jahia user has admin rights on a specified lock.
     *
     * @param lockKey The lock key identifying the lock.
     * @param owner The lock owner.
     * @return True if the user has admin rights, false otherwise.
     */
    public synchronized boolean hasAdminRights(LockKey lockKey, JahiaUser owner) {
        return true;
    }

    public void purgeLockForContext(String contextId) {
    }

    /**
     * Purge all locks and lock prerequisites from memory and database,
     * effectively freeing all the objects. This is a powerful operation and
     * must be used with care as it might cause problems when people already
     * have open popups.
     */
    public void purgeLocks() {
    }

    public Set getContexts(LockKey lockKey) {
        return new HashSet();
    }

    public Map getLocksOnObject(LockKey lockKey) {
        return new HashMap();
    }


    public void setServerId(LockKey lockKey, String lockId) {
    }

    public void purgeLocksForServer() {
    }

    public boolean acquire(LockKey lockKey, JahiaUser owner, String lockID, int timeout, boolean setServerId) {
        return true;
    }
}