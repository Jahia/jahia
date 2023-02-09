/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
@Deprecated
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
