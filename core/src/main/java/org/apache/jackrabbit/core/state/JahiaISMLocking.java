/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.core.state;

import org.apache.jackrabbit.core.id.ItemId;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default item state locking strategy. The default strategy is simply to use
 * a single coarse-grained read-write lock over the entire workspace.
 */
public class JahiaISMLocking implements ISMLocking {

    private final ReentrantReadWriteLock lock;
    private final Lock rl;
    private final Lock wl;

    /**
     * The read lock instance used by readers to release the acquired lock.
     */
    private final ReadLock readLock = new ReadLock() {

        public void release() {
            rl.unlock();
        }
    };

    /**
     * The write lock instance used by writers to release or downgrade the
     * acquired lock.
     */
    private final WriteLock writeLock = new WriteLock() {

        public void release() {
            wl.unlock();
        }

        public ReadLock downgrade() {
            rl.lock();
            wl.unlock();
            return readLock;
        }
    };
    
    /**
     * Initializes an instance of this class.
     */
    public JahiaISMLocking() {
        this(false);
    }

    /**
     * Initializes an instance of this class.
     *
     * @param fair {@code true} if this lock should use a fair ordering policy
     */
    public JahiaISMLocking(boolean fair) {
        super();
        lock = new ReentrantReadWriteLock(fair);
        rl = lock.readLock();
        wl = lock.writeLock();
    }

    /**
     * Increments the reader count and returns the acquired read lock once
     * there are no more writers or the current writer shares the thread id
     * with this reader.
     */
    public ReadLock acquireReadLock(ItemId id)
            throws InterruptedException {
        rl.lock();
        return readLock;
    }

    /**
     * Increments the writer count, sets the writer identifier and returns
     * the acquired write lock once there are no other active readers or
     * writers or the current writer shares the thread id with this writer.
     */
    public WriteLock acquireWriteLock(ChangeLog changeLog)
            throws InterruptedException {
        wl.lock();
        return writeLock;
    }

}
