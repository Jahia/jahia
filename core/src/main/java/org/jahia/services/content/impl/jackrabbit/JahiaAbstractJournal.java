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
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.journal.*;
import org.apache.jackrabbit.core.util.XAReentrantWriterPreferenceReadWriteLock;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.VersioningLock;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.hazelcast.HazelcastCP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Base journal implementation.
 * <p>
 * Fork from {@link AbstractJournal}, extends it but override all sync / lockAndSync mechanism.
 * Use additional parameter in doSync to setup maximum sync time before returning, and maxLockedSyncTime field.
 */
public abstract class JahiaAbstractJournal extends AbstractJournal {

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(JahiaAbstractJournal.class);
    public static final int RETRY_COUNT = 10;
    public static final int MAX_LOCK_TIME_INT = 500;
    public static final long MAX_LOCK_TIME_LONG = 500L;
    public static final int DEFAULT_MAX_LOCK_TIME = 30000;

    /**
     * Map of registered consumers.
     */
    private final Map<String, RecordConsumer> consumers = new HashMap<>();

    /**
     * Journal lock, allowing multiple readers (synchronizing their contents)
     * but only one writer (appending a new entry).
     */
    private final XAReentrantWriterPreferenceReadWriteLock rwLock = new XAReentrantWriterPreferenceReadWriteLock();
    private final Random random = new Random();

    /**
     * Internal version manager.
     */
    private InternalVersionManagerImpl internalVersionManager;

    /**
     * Maximum time we keep lock when syncing, before unlocking
     */
    private int maxLockedSyncTime = DEFAULT_MAX_LOCK_TIME;

    private HazelcastCP lockService;

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @SuppressWarnings("java:S2177")
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
    @Override
    @SuppressWarnings({"java:S135","java:S3626"})
    public void sync(boolean startup) throws JournalException {
        log.debug("Synchronize to the latest change. Startup: {}", startup);
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

    @SuppressWarnings({"java:S2142","java:S2177"})
    private void internalSync(boolean startup) throws JournalException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("{}.internalSync({}): Trying to acquire read lock ", this, startup);
            }
            rwLock.readLock().acquire();
            if (log.isDebugEnabled()) {
                log.debug("{}.internalSync({}): Read lock acquired", this, startup);
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
                log.debug("{}.internalSync({}): Read lock released", this, startup);
            }
        }
    }

    /**
     * Synchronize contents from journal. May be overridden by subclasses.
     *
     * @param startRevision start point (exclusive)
     * @throws JournalException if an error occurs
     */
    @Override
    protected void doSync(long startRevision) throws JournalException {
        doSync(startRevision, -1L);
    }

    /**
     * Synchronize contents from journal. May be overridden by subclasses.
     *
     * @param startRevision start point (exclusive)
     * @throws JournalException if an error occurs
     */
    protected boolean doSync(long startRevision, long maxTime) throws JournalException {
        log.debug("Synchronize contents from journal. StartRevision: {}", startRevision);
        RecordIterator iterator = getRecords(startRevision);
        long startTime = System.currentTimeMillis();
        long stopRevision = Long.MIN_VALUE;

        boolean timeOut = false;
        try {
            while (!timeOut && iterator.hasNext()) {
                Record record = iterator.nextRecord();
                if (record.getJournalId().equals(getId())) {
                    log.debug("Record with revision '{}}' created by this journal, skipped.", record.getRevision());
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
            log.error("Could not synchronize to revision: {} due illegal state of RecordConsumer.", (stopRevision + 1), e);
        } finally {
            iterator.close();
        }

        if (stopRevision > 0) {
            for (RecordConsumer consumer : consumers.values()) {
                consumer.setRevision(stopRevision);
            }
            log.debug("Synchronized from revision {} to revision: {}", startRevision, stopRevision);
        }

        return !timeOut;
    }

    /**
     * Lock the journal revision, disallowing changes from other sources until
     * {@link #unlock} has been called, and synchronizes to the latest change.
     *
     * @throws JournalException if an error occurs
     */
    @Override
    public void lockAndSync() throws JournalException {
        log.debug("Lock the journal revision and synchronize to the latest change.");
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

    @SuppressWarnings({"java:S3776","java:S2177"})
    private void internalLockAndSync() throws JournalException {
        acquireWriteLock();

        boolean succeeded = false;
        int tryCount = 0;
        try {
            // QA-8338 : check maximum lock time after every sync entry and loop until synchronization is over
            while (!succeeded) {
                if (tryCount > 0) {
                    try {
                        log.info("Wait ... ");
                        Thread.sleep(MAX_LOCK_TIME_LONG + random.nextInt(MAX_LOCK_TIME_INT));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                // first sync before locking
                doSync(getMinimalRevision());

                // lock
                long hazelcast = lockCluster();
                doLock();
                long lockedRevision = getLockedRevision();
                log.debug("Locked revision {}, from hz {}", lockedRevision, hazelcast);
                try {
                    if (lockedRevision <= hazelcast) {
                        log.warn("Invalid revision {} from database, must be greater than {}, wait and retry", lockedRevision, hazelcast);
                        if (tryCount++ > RETRY_COUNT) {
                            throw new JournalException("Invalid revision {} from database, must be greater than {}");
                        }
                    } else {
                        // and sync
                        succeeded = doSync(getMinimalRevision(), maxLockedSyncTime);
                        tryCount = 0;
                    }
                } finally {
                    if (!succeeded) {
                        doUnlock(false);
                        unlockCluster(null);
                    }
                }
            }
        } finally {
            if (!succeeded) {
                rwLock.writeLock().release();
                unlockCluster(null);
                if (log.isDebugEnabled()) {
                    log.debug("{}.internalLockAndSync : writeLock {} released - unsuccessful sync", this, rwLock.writeLock());
                }
            }
        }
    }

    @SuppressWarnings({"java:S2142"})
    private void acquireWriteLock() throws JournalException {
        try {
            if (log.isDebugEnabled()) {
                log.debug("{}.internalLockAndSync: Trying to acquire writeLock {}", this, rwLock.writeLock());
            }
            rwLock.writeLock().acquire();
            if (log.isDebugEnabled()) {
                log.debug("{}.internalLockAndSync: writeLock {} acquired", this, rwLock.writeLock());
            }
        } catch (InterruptedException e) {
            String msg = "Unable to acquire write lock.";
            throw new JournalException(msg, e);
        }
    }

    protected abstract long getLockedRevision();

    /**
     * Unlock the journal revision.
     *
     * @param successful flag indicating whether the update process was
     *                   successful
     */
    @Override
    public void unlock(boolean successful) {
        try {
            doUnlock(successful);
            unlockCluster(successful ? getLockedRevision() : null);
        } finally {
            //Should not happen that a RuntimeException will be thrown in subCode, but it's safer
            //to release the rwLock in finally block.
            rwLock.writeLock().release();
            if (log.isDebugEnabled()) {
                log.debug("{}.unlock : writeLock {} released - Successful? {}", this, rwLock.writeLock(), successful);
            }
        }
    }

    private long lockCluster() {
        HazelcastCP hcp = getLockService();
        if (hcp != null) {
            hcp.lock("journalLock");
            Long globalRevision = hcp.getAtomic("globalRevision");
            if (globalRevision != null) {
                return globalRevision;
            }
        }
        return 0L;
    }

    private void unlockCluster(Long newGlobalRevision) {
        HazelcastCP hcp = getLockService();
        if (hcp != null) {
            if (newGlobalRevision != null) {
                log.debug("Updating global revision before releasing lock = {}", newGlobalRevision);
                hcp.setAtomic("globalRevision", newGlobalRevision);
            }
            hcp.unlock("journalLock");
        }
    }

    private HazelcastCP getLockService() {
        if (lockService == null && FrameworkService.getInstance().isStarted()) {
            lockService = BundleUtils.getOsgiService(HazelcastCP.class, null);
        }
        return lockService;
    }

    /**
     * Set the version manager.
     */
    @Override
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
