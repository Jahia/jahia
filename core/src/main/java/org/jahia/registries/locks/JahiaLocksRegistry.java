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
