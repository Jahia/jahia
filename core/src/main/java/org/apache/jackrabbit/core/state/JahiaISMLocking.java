/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.core.state;

import static org.apache.jackrabbit.core.TransactionContext.getCurrentThreadId;
import static org.apache.jackrabbit.core.TransactionContext.isSameThreadId;

import org.apache.jackrabbit.core.id.ItemId;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Default item state locking strategy. The default strategy is simply to use
 * a single coarse-grained read-write lock over the entire workspace.
 */
public class JahiaISMLocking implements ISMLocking {

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock rl = lock.readLock();
    private final Lock wl = lock.readLock();

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
