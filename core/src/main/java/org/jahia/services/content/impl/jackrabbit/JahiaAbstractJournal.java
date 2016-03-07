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
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.journal.*;
import org.apache.jackrabbit.core.util.XAReentrantWriterPreferenceReadWriteLock;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.VersioningLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Base journal implementation.
 * Fork from org.apache.jackrabbit.core.journal.AbstractJournal, extends it but override all sync / lockAndSync mechanism.
 * Use additional parameter in doSync to setup maximum sync time before returning, and maxLockedSyncTime field.
 */
public abstract class JahiaAbstractJournal extends AbstractJournal {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(JahiaAbstractJournal.class);

    /**
     * Map of registered consumers.
     */
    private final Map<String, RecordConsumer> consumers = new HashMap<String, RecordConsumer>();

    /**
     * Journal lock, allowing multiple readers (synchronizing their contents)
     * but only one writer (appending a new entry).
     */
    private final XAReentrantWriterPreferenceReadWriteLock rwLock = new XAReentrantWriterPreferenceReadWriteLock();

    /**
     * Internal version manager.
     */
    private InternalVersionManagerImpl internalVersionManager;

    /**
     * Maximum time we keep lock when syncing, before unlocking
     */
    private int maxLockedSyncTime = 30000;

    /**
     * {@inheritDoc}
     */
    public void register(RecordConsumer consumer) throws JournalException {
        super.register(consumer);
        synchronized (consumers) {
            String consumerId = consumer.getId();
            if (consumers.containsKey(consumerId)) {
                String msg = "Record consumer with identifier '"
                        + consumerId + "' already registered.";
                throw new JournalException(msg);
            }
            consumers.put(consumerId, consumer);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean unregister(RecordConsumer consumer) {
        super.unregister(consumer);
        synchronized (consumers) {
            String consumerId = consumer.getId();
            return consumers.remove(consumerId) != null;
        }
    }

    /**
     * Return the minimal revision of all registered consumers.
     */
    private long getMinimalRevision() {
        long minimalRevision = Long.MAX_VALUE;

        synchronized (consumers) {
            for (RecordConsumer consumer : consumers.values()) {
                if (consumer.getRevision() < minimalRevision) {
                    minimalRevision = consumer.getRevision();
                }
            }
        }
        return minimalRevision;
    }


    /**
     * {@inheritDoc}
     */
    public void sync(boolean startup) throws JournalException {
        for (;;) {
            if (internalVersionManager != null) {
                VersioningLock.ReadLock lock =
                        internalVersionManager.acquireReadLock();
                try {
                    internalSync(startup);
                } finally {
                    lock.release();
                }
            } else {
                internalSync(startup);
            }
            // startup sync already done, don't do it again
            startup = false;
            if (syncAgainOnNewRecords()) {
                // sync again if there are more records available
                RecordIterator it = getRecords(getMinimalRevision());
                try {
                    if (it.hasNext()) {
                        continue;
                    }
                } finally {
                    it.close();
                }
            }
            break;
        }
    }

    private void internalSync(boolean startup) throws JournalException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(this + ".internalSync("+startup + "): Trying to acquire read lock ");
            }
            rwLock.readLock().acquire();
            if (log.isDebugEnabled()) {
                log.debug(this + ".internalSync(" + startup + "): Read lock acquired");
            }
        } catch (InterruptedException e) {
            String msg = "Unable to acquire read lock.";
            throw new JournalException(msg, e);
        }
        try {
            doSync(getMinimalRevision(), startup);
        } finally {
            rwLock.readLock().release();
            if (log.isDebugEnabled()) {
                log.debug(this + ".internalSync("+startup + "): Read lock released");
            }
        }
    }

    /**
     *
     * Synchronize contents from journal. May be overridden by subclasses.
     *
     * @param startRevision start point (exclusive)
     * @throws JournalException if an error occurs
     */
    protected void doSync(long startRevision) throws JournalException {
        doSync(startRevision, -1L);
    }

    /**
     *
     * Synchronize contents from journal. May be overridden by subclasses.
     *
     * @param startRevision start point (exclusive)
     * @throws JournalException if an error occurs
     */
    protected boolean doSync(long startRevision, long maxTime) throws JournalException {
        RecordIterator iterator = getRecords(startRevision);
        long startTime = System.currentTimeMillis();
        boolean timeOut = false;

        long stopRevision = Long.MIN_VALUE;

        try {
            while (!timeOut && iterator.hasNext()) {
                Record record = iterator.nextRecord();
                if (record.getJournalId().equals(getId())) {
                    log.debug("Record with revision '" + record.getRevision()
                            + "' created by this journal, skipped.");
                } else {
                    RecordConsumer consumer = getConsumer(record.getProducerId());
                    if (consumer != null) {
                        consumer.consume(record);
                    }
                }
                stopRevision = record.getRevision();

                if (maxTime > -1 && (System.currentTimeMillis() - startTime) > maxTime) {
                    timeOut = true;
                }
            }
        } catch (IllegalStateException e) {
            log.error("Could not synchronize to revision: " + (stopRevision + 1) + " due illegal state of RecordConsumer.");
        } finally {
            iterator.close();
        }

        if (stopRevision > 0) {
            for (RecordConsumer consumer : consumers.values()) {
                consumer.setRevision(stopRevision);
            }
            log.debug("Synchronized from revision " + startRevision + " to revision: " + stopRevision);
        }

        return !timeOut;
    }

    /**
     * Lock the journal revision, disallowing changes from other sources until
     * {@link #unlock} has been called, and synchronizes to the latest change.
     *
     * @throws JournalException if an error occurs
     */
    public void lockAndSync() throws JournalException {
        if (internalVersionManager != null) {
            VersioningLock.ReadLock lock =
                    internalVersionManager.acquireReadLock();
            try {
                internalLockAndSync();
            } finally {
                lock.release();
            }
        } else {
            internalLockAndSync();
        }
    }

    private void internalLockAndSync() throws JournalException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(this + ".internalLockAndSync: Trying to acquire writeLock " + rwLock.writeLock());
            }
            rwLock.writeLock().acquire();
            if (log.isDebugEnabled()) {
                log.debug(this + ".internalLockAndSync: writeLock " +rwLock.writeLock()+" acquired");
            }
        } catch (InterruptedException e) {
            String msg = "Unable to acquire write lock.";
            throw new JournalException(msg, e);
        }

        boolean succeeded = false;

        try {
            // QA-8338 : check maximum lock time after every sync entry and loop until synchronization is over
            while (!succeeded) {
                // first sync before locking
                doSync(getMinimalRevision());

                // lock
                doLock();
                try {
                    // and sync
                    succeeded = doSync(getMinimalRevision(), maxLockedSyncTime);
                } finally {
                    if (!succeeded) {
                        doUnlock(false);
                    }
                }
            }
        } finally {
            if (!succeeded) {
                rwLock.writeLock().release();
                if (log.isDebugEnabled()) {
                    log.debug(this + ".internalLockAndSync : writeLock "+rwLock.writeLock()+"released - unsuccessful sync");
                }
            }
        }
    }

    /**
     * Unlock the journal revision.
     *
     * @param successful flag indicating whether the update process was
     *                   successful
     */
    public void unlock(boolean successful) {
        try {
            doUnlock(successful);
        } finally {
            //Should not happen that a RuntimeException will be thrown in subCode, but it's safer
            //to release the rwLock in finally block.
            rwLock.writeLock().release();
            if (log.isDebugEnabled()) {
                log.debug(this + ".unlock : writeLock "+rwLock.writeLock()+" released - Successful? " + successful);
            }
        }
    }

    /**
     * Set the version manager.
     */
    public void setInternalVersionManager(InternalVersionManagerImpl internalVersionManager) {
        this.internalVersionManager = internalVersionManager;
    }

    public int getMaxLockedSyncTime() {
        return maxLockedSyncTime;
    }

    public void setMaxLockedSyncTime(int maxLockedSyncTime) {
        this.maxLockedSyncTime = maxLockedSyncTime;
    }
}
