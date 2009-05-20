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
package org.jahia.registries.locks;

import java.util.HashMap;
import java.util.Map;

import org.jahia.utils.JahiaConsole;


/**
 *  .... add comments here ....
 *
 * @auhtor  Fulco Houkes
 * @version 1.0
 */
public class JahiaLocksRegistry
{
    private static JahiaLocksRegistry mObject;

    /**
     * @associates JahiaLock
     */
    private Map<String, JahiaLock> mRegistry;


    //-------------------------------------------------------------------------
    /** Default constructor
     */
    protected JahiaLocksRegistry () {
        mRegistry = new HashMap<String, JahiaLock>();
        JahiaConsole.println ("JahiaLocksRegistry.constructor",
                "=---= Lock registry has been instanciated =---=");
    }


    //-------------------------------------------------------------------------
    /** Return the unique registry instance. If the instance does not exist,
     *  a new instance is created.
     *
     * #return
     *      Return the unique registry instance. Return null is the registry
     *      could not be instanciated.
     */
    public static JahiaLocksRegistry getInstance() {
        if (mObject == null) {
            mObject = new JahiaLocksRegistry();
        }
        return mObject;
    }

    //-------------------------------------------------------------------------
    /** Set the lock data. The lock is created if the specified lock name is not
     *  used. If the lock already exits, the lock's data are updated.
     *
     * @param   lockName
     *      The lock name. This name must be unique among the registry.
     * @param   lockData
     *      The additional (user-defined) lock data.
     *
     * @return
     *      Return true if the lock could be added to the registry. False if the
     *      lock name is already used.
     */
    public synchronized boolean setLock (String lockName, Map<String, Object> lockData,
            int timeout)
    {
        if (lockName == null) {
            return false;
        }

        JahiaLock lock = (JahiaLock)mRegistry.get (lockName);
        if (lock == null) {
            lock = new JahiaLock (lockName, lockData, timeout);
            mRegistry.put (lockName, lock);

        } else {
            lock.setLockData (lockData);
            lock.resetTimeout();
        }
        return true;
    }



    //-------------------------------------------------------------------------
    /** Get the specified lock.
     *
     * @param   lockName
     *      The lock name.
     *
     * @return
     *      Return the lock's data Map. If the lock could not befound,
     *      null will be returned.
     */
    public synchronized Map<String, Object> getLock (String lockName)
    {
        if (lockName != null) {
            JahiaLock lock = mRegistry.get (lockName);
            if (lock != null) {
                return lock.getLockData();
            }
        }
        return null;
    }


    //-------------------------------------------------------------------------
    /** Remove the specified lock. Does not consider as an error if the lock
     *  is not present in the registry.
     *
     * @param   lockName
     *      The lock name.
     */
    public synchronized void removeLock (String lockName)
    {
        if (lockName != null) {
            mRegistry.remove (lockName);
        }
    }


    //-------------------------------------------------------------------------
    /** check if the specified lock name is already in use.
     *
     * @param   lockName
     *      The lock name.
     *
     * @return
     *      Return true if the lock name is already in use, otherwise return
     *      false.
     */
    public final boolean doesLockExist (String lockName)
    {
        return (getLock (lockName) != null);
    }

    //-------------------------------------------------------------------------
    /**
     *
     */
    public synchronized boolean isLockValid (String lockName)
    {
        if (lockName == null) {
            return false;
        }

        JahiaLock lock = (JahiaLock)mRegistry.get (lockName);
        return lock.isValid();
    }

    //-------------------------------------------------------------------------
    /**
     *
     */
    public synchronized boolean resetLockTimeout (String lockName)
    {
        if (lockName == null) {
            return false;
        }

        JahiaLock lock = (JahiaLock)mRegistry.get (lockName);
        lock.resetTimeout();
        return true;
    }

}
