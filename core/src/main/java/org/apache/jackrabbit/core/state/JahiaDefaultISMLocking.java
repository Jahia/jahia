/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import static org.apache.jackrabbit.core.TransactionContext.getCurrentThreadId;
import static org.apache.jackrabbit.core.TransactionContext.isSameThreadId;

import org.apache.jackrabbit.core.id.ItemId;

/**
 * DX Default item state locking strategy, which additionally to {@link DefaultISMLocking} includes a fix [QA-9444].
 * The default strategy is simply to use a single coarse-grained read-write lock over the entire workspace.
 */
public class JahiaDefaultISMLocking implements ISMLocking {

    /**
     * The read lock instance used by readers to release the acquired lock.
     */
    private final ReadLock readLock = new ReadLock() {
        public void release() {
            releaseReadLock();
        }
    };

    /**
     * The write lock instance used by writers to release or downgrade the
     * acquired lock.
     */
    private final WriteLock writeLock = new WriteLock() {
        public void release() {
            releaseWriteLock(false);
        }
        public ReadLock downgrade() {
            releaseWriteLock(true);
            return readLock;
        }
    };

    /**
     * Flag for determining whether this locking strategy should give
     * preference to writers or not. If writers are preferred (which
     * is the default setting), then all readers will get blocked whenever
     * there's a writer waiting for the lock.
     */
    private boolean writerPreference = true;

    /**
     * Number of writer threads waiting. While greater than zero, no new
     * (unrelated) readers are allowed to proceed.
     */
    private int writersWaiting = 0;

    /**
     * The thread identifier of the current writer, or <code>null</code> if
     * no write is in progress. A thread with the same identifier (i.e. the
     * same thread or another thread in the same transaction) can re-acquire
     * read or write locks without limitation, while all other readers and
     * writers remain blocked. Note that a downgraded write lock still retains
     * the writer thread identifier, which allows related threads to reacquire
     * read or write locks even when there are concurrent writers waiting.
     */
    private Object writerId = null;

    /**
     * Number of acquired write locks. All the concurrent write locks are
     * guaranteed to share the same thread identifier (see {@link #writerId}).
     */
    private int writerCount = 0;

    /**
     * Number of acquired read locks.
     */
    private int readerCount = 0;

    /**
     * Returns the writer preference status of this locking strategy.
     *
     * @return writer preference
     */
    public boolean isWriterPreference() {
        return writerPreference;
    }

    /**
     * Sets the writer preference status of this locking strategy.
     *
     * @param preference writer preference
     */
    public void setWriterPreference(boolean preference) {
        this.writerPreference = preference;
    }

    /**
     * Increments the reader count and returns the acquired read lock once
     * there are no more writers or the current writer shares the thread id
     * with this reader.
     */
    public synchronized ReadLock acquireReadLock(ItemId id)
            throws InterruptedException {
        Object currentId = getCurrentThreadId();
        while (writerId != null
                ? ((writerCount > 0 || writerPreference && writersWaiting > 0) && !isSameThreadId(writerId, currentId))
                : (writerPreference && writersWaiting > 0)) {
            wait();
        }

        readerCount++;
        return readLock;
    }

    /**
     * Decrements the reader count and notifies all pending threads if the
     * lock is now available. Used by the {@link #readLock} instance.
     */
    private synchronized void releaseReadLock() {
        readerCount--;
        if (readerCount == 0 && writerCount == 0) {
            writerId = null;
            notifyAll();
        }
    }

    /**
     * Increments the writer count, sets the writer identifier and returns
     * the acquired write lock once there are no other active readers or
     * writers or the current writer shares the thread id with this writer.
     */
    public synchronized WriteLock acquireWriteLock(ChangeLog changeLog)
            throws InterruptedException {
        Object currentId = getCurrentThreadId();

        writersWaiting++;
        try {
            while (writerId != null
                    ? !isSameThreadId(writerId, currentId) : readerCount > 0) {
                wait();
            }
        } finally {
            writersWaiting--;
        }

        if (writerCount++ == 0) {
            writerId = currentId;
        }
        return writeLock;
    }

    /**
     * Decrements the writer count (and possibly clears the writer identifier)
     * and notifies all pending threads if the lock is now available. If the
     * downgrade argument is true, then the reader count is incremented before
     * notifying any pending threads. Used by the {@link #writeLock} instance.
     */
    private synchronized void releaseWriteLock(boolean downgrade) {
        writerCount--;
        if (downgrade) {
            readerCount++;
        }
        if (writerCount == 0) {
            if (readerCount == 0) {
                writerId = null;
            }
            notifyAll();
        }
    }

}
