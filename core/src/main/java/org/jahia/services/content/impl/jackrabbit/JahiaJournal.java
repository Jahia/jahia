/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.cluster.JahiaClusterNode;
import org.apache.jackrabbit.core.cluster.NodeLevelLockableJournal;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.journal.*;
import org.apache.jackrabbit.core.util.db.*;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.*;

/**
 * Cluster Journal based on node-level database locking
 *
 * @see <a href="https://jira.jahia.org/browse/BACKLOG-4520">BACKLOG-4520</a>
 * @deprecated Use {@link JahiaDatabaseJournal} instead
 */
@Deprecated
public class JahiaJournal extends AbstractJournal implements DatabaseAware, NodeLevelLockableJournal {

    /**
     * Map of registered consumers.
     */
    private final Map<String, RecordConsumer> consumers = new HashMap<>();

    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(JahiaJournal.class);

    /**
     * Driver name, bean property.
     */
    private String driver;

    /**
     * Connection URL, bean property.
     */
    private String url;

    /**
     * Database type, bean property.
     */
    private String databaseType;

    /**
     * User name, bean property.
     */
    private String user;

    /**
     * Password, bean property.
     */
    private String password;

    /**
     * DataSource logical name, bean property.
     */
    private String dataSourceName;

    /**
     * The connection helper
     */
    private ConnectionHelper conHelper;

    /**
     * Time to lock in ms
     */
    private int sleepTimeWaitingForLock = 500;

    private int numberOfRetries = 3;

    /**
     * Whether the revision table janitor thread is enabled.
     */
    private boolean janitorEnabled = false;

    /**
     * The sleep time of the revision table janitor in seconds, 1 day default.
     */
    private int janitorSleep = 60 * 60 * 24;

    /**
     * Indicates when the next run of the janitor is scheduled.
     * The first run is scheduled by default at 03:00 hours.
     */
    private Calendar janitorNextRun;

    private Thread janitorThread;

    /**
     * The instance that manages the local revision.
     */
    private DatabaseRevision databaseRevision;

    /**
     * SQL statement returning all revisions within a range.
     */
    private String selectRevisionsStmtSQL;

    /**
     * SQL statement updating the global revision.
     */
    private String updateGlobalStmtSQL;

    /**
     * SQL statement returning the global revision.
     */
    private String selectGlobalStmtSQL;

    /**
     * SQL statement appending a new record.
     */
    private String insertRevisionStmtSQL;

    /**
     * SQL statement returning the minimum of the local revisions.
     */
    private String selectMinLocalRevisionStmtSQL;

    /**
     * SQL statement removing a set of revisions with from the journal table.
     */
    private String cleanRevisionStmtSQL;

    /**
     * SQL statement returning the local revision of this cluster node.
     */
    private String getLocalRevisionStmtSQL;

    /**
     * SQL statement for inserting the local revision of this cluster node.
     */
    private String insertLocalRevisionStmtSQL;

    /**
     * SQL statement for updating the local revision of this cluster node.
     */
    private String updateLocalRevisionStmtSQL;

    /**
     * SQL statement for updating the local revision of this cluster node.
     */

    private String insertLockSQL;

    /**
     * SQL statement for updating the local revision of this cluster node.
     */
    private String deleteLockSQL;

    /**
     * Schema object prefix, bean property.
     */
    private String schemaObjectPrefix;

    /**
     * The repositories {@link ConnectionFactory}.
     */
    private ConnectionFactory connectionFactory;

    public JahiaJournal() {
        databaseType = "default";
        schemaObjectPrefix = "";
        setJanitorFirstRunHourOfDay(3);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnectionFactory(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(String id, NamespaceResolver resolver) throws JournalException {
        super.init(id, resolver);
        initDatabaseType();
        try {
            conHelper = createConnectionHelper(getDataSource());
            schemaObjectPrefix = conHelper.prepareDbIdentifier(schemaObjectPrefix); // make sure schemaObjectPrefix consists of legal name characters only
            buildSQLStatements();
            initInstanceRevisionAndJanitor();
        } catch (Exception e) {
            throw new JournalException("Error when initalizing the journal", e);
        }
        log.info("JahiaJournal initialized.");
    }

    /**
     * Completes initialization of this database journal. Base implementation
     * checks whether the required bean properties <code>driver</code> and
     * <code>url</code> have been specified and optionally deduces a valid
     * database type. Should be overridden by subclasses that use a different way to
     * create a connection and therefore require other arguments.
     *
     * @throws JournalException if initialization fails
     */
    protected void initDatabaseType() throws JournalException {
        if (driver == null && dataSourceName == null) {
            throw new JournalException("Driver not specified.");
        }
        if (url == null && dataSourceName == null) {
            throw new JournalException("Connection URL not specified.");
        }
        if (dataSourceName != null) {
            try {
                String configuredDatabaseType = connectionFactory.getDataBaseType(dataSourceName);
                try (InputStream resourceStream = DatabaseJournal.class.getResourceAsStream(configuredDatabaseType + ".ddl")) {
                    if (resourceStream != null) {
                        setDatabaseType(configuredDatabaseType);
                    }
                } catch (IOException e) {
                    log.warn("Ignored exception on resource close", e);
                }
            } catch (RepositoryException e) {
                throw new JournalException("failed to get database type", e);
            }
        }
        if (databaseType == null) {
            try {
                databaseType = getDatabaseTypeFromURL(url);
            } catch (IllegalArgumentException e) {
                String msg = "Unable to derive database type from URL: " + e.getMessage();
                throw new JournalException(msg);
            }
        }
    }

    private DataSource getDataSource() throws RepositoryException, SQLException {
        if (StringUtils.isEmpty(dataSourceName)) {
            return connectionFactory.getDataSource(driver, url, user, password);
        } else {
            return connectionFactory.getDataSource(dataSourceName);
        }
    }

    /**
     * This method is called from the {@link #init(String, NamespaceResolver)} method of this class and
     * returns a {@link ConnectionHelper} instance which is assigned to the {@code conHelper} field.
     * Subclasses may override it to return a specialized connection helper.
     *
     * @param dataSrc the {@link DataSource} of this persistence manager
     * @return a {@link ConnectionHelper}
     * @throws Exception on error
     */
    protected ConnectionHelper createConnectionHelper(DataSource dataSrc) throws Exception {
        // we will create a helper regarding the db type and this can work if the db type is known
        // otherwise the default is returned
        ConnectionHelper helper = null;
        if ("oracle".equalsIgnoreCase(databaseType)) {
            helper = new OracleConnectionHelper(dataSrc, false);
            ((OracleConnectionHelper) helper).init();
        } else if ("postgresql".equalsIgnoreCase(databaseType)) {
            helper = new PostgreSQLConnectionHelper(dataSrc, false);
        } else if ("derby".equalsIgnoreCase(databaseType)) {
            helper = new DerbyConnectionHelper(dataSrc, false);
        } else {
            helper = new ConnectionHelper(dataSrc, false);
        }

        return helper;
    }

    /**
     * Initialize the instance revision manager and the janitor thread.
     *
     * @throws JournalException on error
     */
    protected void initInstanceRevisionAndJanitor() throws JournalException{
        // Get the local file revision from disk (upgrade; see JCR-1087)
        long localFileRevision = 0L;
        if (getRevision() != null) {
            InstanceRevision currentFileRevision = new FileRevision(new File(getRevision()), true);
            try {
                localFileRevision = currentFileRevision.get();
            } finally {
                currentFileRevision.close();
            }
        }

        // Now write the localFileRevision (or 0 if it does not exist) to the LOCAL_REVISIONS
        // table, but only if the LOCAL_REVISIONS table has no entry yet for this cluster node
        databaseRevision = new DatabaseRevision(localFileRevision);
        long localRevision = databaseRevision.get();

        log.info("Initialized local revision to {}", localRevision);

        // Start the clean-up thread if necessary.
        if (janitorEnabled) {
            janitorThread = new Thread(new RevisionTableJanitor(), "Jackrabbit-ClusterRevisionJanitor");
            janitorThread.setDaemon(true);
            janitorThread.start();
            log.info("Cluster revision janitor thread started; first run scheduled at {}", janitorNextRun.getTime());
        } else {
            log.info("Cluster revision janitor thread not started");
        }
    }

    /* (non-Javadoc)
     * @see org.apache.jackrabbit.core.journal.Journal#getInstanceRevision()
     */
    @Override
    public InstanceRevision getInstanceRevision() throws JournalException {
        return databaseRevision;
    }

    /**
     * Derive a database type from a JDBC connection URL. This simply treats the given URL
     * as delimeted by colons and takes the 2nd field.
     *
     * @param url JDBC connection URL
     * @return the database type
     * @throws IllegalArgumentException if the JDBC connection URL is invalid
     */
    private static String getDatabaseTypeFromURL(String url) {
        int start = url.indexOf(':');
        if (start != -1) {
            int end = url.indexOf(':', start + 1);
            if (end != -1) {
                return url.substring(start + 1, end);
            }
        }
        throw new IllegalArgumentException(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordIterator getRecords(long startRevision) throws JournalException {
        try {
            return new DatabaseRecordIterator(conHelper.exec(selectRevisionsStmtSQL, new Object[]{startRevision}, false, 0), getResolver(), getNamePathResolver());
        } catch (SQLException e) {
            throw new JournalException("Unable to return record iterator.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecordIterator getRecords() throws JournalException {
        return getRecords(Long.MIN_VALUE);
    }

    /**
     * Synchronize contents from journal. May be overridden by subclasses.
     * Do the initial sync in batchMode, since some databases (PSQL) when
     * not in transactional mode, load all results in memory which causes
     * out of memory. See JCR-2832
     * At normal runtime, transaction must not be used as it will lead to
     * deadlocks. Out of memory issue should not happen as the
     * synchronization is done too often at runtime to have results sets
     * big enough.
     *
     * @param startRevision start point (exclusive)
     *
     * @param startup indicates if the cluster node is syncing on startup
     *                or does a normal sync.
     * @throws JournalException if an error occurs
     */
    @Override
    protected void doSync(long startRevision, boolean startup) throws JournalException {
        if (!startup) {
            // if the cluster node is not starting do a normal sync
            doSync(startRevision);
        } else {
            startBatch();
            boolean success = false;
            try {
                doSync(startRevision);
                success = true;
            } finally {
                endBatch(success);
            }
        }
    }

    @Override
    protected void doLock() throws JournalException {
        // Do nothing
    }

    @Override
    protected void doUnlock(boolean successful) {
        // Do nothing
    }

    @Override
    public void lockNodes(Set<NodeId> ids) throws JournalException {
        RetryOnExceptionStrategy retryStrategy = new RetryOnExceptionStrategy(numberOfRetries, sleepTimeWaitingForLock);
        while (retryStrategy.canRetry()) {
            try {
                internalLockNodes(ids);
                return;
            } catch (SQLIntegrityConstraintViolationException e) {
                // The meaning of the exception in this case is inability to insert a record into JR_J_LOCKS table
                // due to primary key constraint violation, which means a node is currently locked by a concurrent process.
                // We should wait until the lock is released.
                log.debug("Cannot lock {} . Reason: {}", StringUtils.join(ids, ","), e.getMessage());
                try {
                    retryStrategy.onErrorOccured();
                } catch (RetryStrategyException rsex) {
                    throw new JournalException(rsex.getMessage(), rsex);
                }
                sync(false);
            }
        }
    }

    private void internalLockNodes(Set<NodeId> ids) throws JournalException, SQLIntegrityConstraintViolationException {

        // Inserting IDs in a predefined order helps avoid potential database deadlock
        // when one process inserts id1 then id2, and another one inserts id2 then id1 simultaneously.
        ids = new TreeSet<>(ids);

        boolean success = false;
        startBatch();

        try {
            for (NodeId id : ids) {
                log.debug("Lock {}", id);
                conHelper.exec(insertLockSQL, id.toString(), getId());
            }
            success = true;
        } catch (SQLIntegrityConstraintViolationException sicvEx) {
            // The meaning of the exception in this case is inability to insert a record into JR_J_LOCKS table
            // due to primary key constraint violation, which means a node is currently locked by a concurrent process.
            // Let the invoker deal with this specific case.
            throw sicvEx;
        } catch (SQLException e) {
            throw new JournalException(e.getMessage(), e);
        } finally {
            endBatch(success);
        }
    }

    public void unlockNodes(Set<NodeId> ids) throws JournalException {
        try {
            for (NodeId id : ids) {
                log.debug("Unlock {}", id);
                conHelper.exec(deleteLockSQL, new Object[]{id.toString()}, false, 0);
            }
        } catch (SQLException e) {
            throw new JournalException("Unable to unlock nodes.", e);
        }
    }

    private void startBatch() throws JournalException {
        try {
            conHelper.startBatch();
        } catch (SQLException e) {
            throw new JournalException("Unable to start batch and set autocommit to false.", e);
        }
    }

    private void endBatch(boolean successful) throws JournalException {
        try {
            conHelper.endBatch(successful);
        } catch (SQLException e) {
            throw new JournalException("failed to end batch", e);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * We have already saved away the revision for this record.
     */
    @Override
    protected void append(AppendRecord record, InputStream in, int length)
            throws JournalException {
        ResultSet rs = null;
        boolean succeeded = false;

        startBatch();

        try {
            conHelper.exec(updateGlobalStmtSQL);
            rs = conHelper.exec(selectGlobalStmtSQL, null, false, 0);
            if (!rs.next()) {
                throw new JournalException("No revision available.");
            }
            long lockedRevision = rs.getLong(1);
            record.setRevision(lockedRevision);
            conHelper.exec(insertRevisionStmtSQL, lockedRevision, getId(), record.getProducerId(),
                    new StreamWrapper(in, length));
            succeeded = true;
        } catch (SQLException e) {
            throw new JournalException("Unable to lock global revision table.", e);
        } finally {
            endBatch(succeeded);
            DbUtility.close(rs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if (janitorThread != null) {
            janitorThread.interrupt();
        }
    }

    /**
     * Builds the SQL statements. May be overridden by subclasses to allow
     * different table and/or column names.
     */
    protected void buildSQLStatements() {
        selectRevisionsStmtSQL =
                "select REVISION_ID, JOURNAL_ID, PRODUCER_ID, REVISION_DATA from "
                        + schemaObjectPrefix + "JOURNAL where REVISION_ID > ? order by REVISION_ID";
        updateGlobalStmtSQL =
                "update " + schemaObjectPrefix + "GLOBAL_REVISION"
                        + " set REVISION_ID = REVISION_ID + 1";
        selectGlobalStmtSQL =
                "select REVISION_ID from "
                        + schemaObjectPrefix + "GLOBAL_REVISION";
        insertRevisionStmtSQL =
                "insert into " + schemaObjectPrefix + "JOURNAL"
                        + " (REVISION_ID, JOURNAL_ID, PRODUCER_ID, REVISION_DATA) "
                        + "values (?,?,?,?)";
        selectMinLocalRevisionStmtSQL =
                "select MIN(REVISION_ID) from " + schemaObjectPrefix + "LOCAL_REVISIONS";
        cleanRevisionStmtSQL =
                "delete from " + schemaObjectPrefix + "JOURNAL " + "where REVISION_ID < ?";
        getLocalRevisionStmtSQL =
                "select REVISION_ID from " + schemaObjectPrefix + "LOCAL_REVISIONS "
                        + "where JOURNAL_ID = ?";
        insertLocalRevisionStmtSQL =
                "insert into " + schemaObjectPrefix + "LOCAL_REVISIONS "
                        + "(REVISION_ID, JOURNAL_ID) values (?,?)";
        updateLocalRevisionStmtSQL =
                "update " + schemaObjectPrefix + "LOCAL_REVISIONS "
                        + "set REVISION_ID = ? where JOURNAL_ID = ?";
        insertLockSQL =
                "insert into " + schemaObjectPrefix + "LOCKS " + "(NODE_ID, JOURNAL_ID) values (?,?)";
        deleteLockSQL =
                "delete from " + schemaObjectPrefix + "LOCKS where NODE_ID = ?";

    }

    /**
     * Bean setters
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Set the database type.
     *
     * @param databaseType the database type
     */
    public void setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
    }

    /**
     * Set the database type.
     *
     * @param databaseType the database type
     * @deprecated This method is deprecated; {@link #setDatabaseType} should be used instead.
     */
    @Deprecated
    public void setSchema(String databaseType) {
        setDatabaseType(databaseType);
    }

    public void setSchemaObjectPrefix(String schemaObjectPrefix) {
        this.schemaObjectPrefix = schemaObjectPrefix.toUpperCase();
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setJanitorEnabled(boolean enabled) {
        this.janitorEnabled = enabled;
    }

    public void setJanitorSleep(int sleep) {
        this.janitorSleep = sleep;
    }

    public void setJanitorFirstRunHourOfDay(int hourOfDay) {
        janitorNextRun = Calendar.getInstance();
        if (janitorNextRun.get(Calendar.HOUR_OF_DAY) >= hourOfDay) {
            janitorNextRun.add(Calendar.DAY_OF_MONTH, 1);
        }
        janitorNextRun.set(Calendar.HOUR_OF_DAY, hourOfDay);
        janitorNextRun.set(Calendar.MINUTE, 0);
        janitorNextRun.set(Calendar.SECOND, 0);
        janitorNextRun.set(Calendar.MILLISECOND, 0);
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * @deprecated Schema check is no longer supported.
     */
    @Deprecated
    public final void setSchemaCheckEnabled(boolean enabled) {
    }

    public void setSleepTimeWaitingForLock(int sleepTimeWaitingForLock) {
        this.sleepTimeWaitingForLock = sleepTimeWaitingForLock;
    }

    public void setNumberOfRetries(int numberOfRetries) {
        this.numberOfRetries = numberOfRetries;
    }

    /**
     * Synchronize contents from journal.
     * Explicitly call reallySetRevision() on JahiaClusterNode consumer.
     *
     * @param startRevision start point (exclusive)
     * @throws JournalException if an error occurs
     */
    @Override
    protected void doSync(long startRevision) throws JournalException {
        log.debug("Getting revisions from {}", startRevision);
        RecordIterator iterator = getRecords(startRevision);
        long stopRevision = Long.MIN_VALUE;
        try {
            while (iterator.hasNext()) {
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
            }
        } catch (IllegalStateException e) {
            log.error("Could not synchronize to revision: {} due illegal state of RecordConsumer.", (stopRevision + 1));
        } finally {
            iterator.close();
        }

        if (stopRevision > 0) {
            for (RecordConsumer consumer : consumers.values()) {
                if (consumer instanceof JahiaClusterNode) {
                    ((JahiaClusterNode) consumer).reallySetRevision(stopRevision);
                } else {
                    consumer.setRevision(stopRevision);
                }
            }

            log.debug("Synchronized from revision {} to revision: {}",startRevision, stopRevision);
        }
    }

    /**
     * Override register to have a local copy of consumers
     */
    @Override
    public void register(RecordConsumer consumer) throws JournalException {
        synchronized (consumers) {
            String consumerId = consumer.getId();
            if (consumers.containsKey(consumerId)) {
                String msg = "Record consumer with identifier '"
                        + consumerId + "' already registered.";
                throw new JournalException(msg);
            }
            consumers.put(consumerId, consumer);
        }
        super.register(consumer);
    }

    /**
     * Override unregister to have a local copy of consumers
     */
    @Override
    public boolean unregister(RecordConsumer consumer) {
        synchronized (consumers) {
            String consumerId = consumer.getId();
            consumers.remove(consumerId);
        }
        return super.unregister(consumer);
    }

    /**
     * This class manages the local revision of the cluster node. It
     * persists the local revision in the LOCAL_REVISIONS table in the
     * clustering database.
     */
    private class DatabaseRevision implements InstanceRevision {

        /**
         * The cached local revision of this cluster node.
         */
        private long localRevision;

        /**
         * Checks whether there's a local revision value in the database for this
         * cluster node. If not, it writes the given default revision to the database.
         *
         * @param revision the default value for the local revision counter
         * @return the local revision
         * @throws JournalException on error
         */
        public DatabaseRevision(long revision) throws JournalException {

            ResultSet rs = null;
            try {
                // Check whether there is an entry in the database.
                rs = conHelper.exec(getLocalRevisionStmtSQL, new Object[]{getId()}, false, 0);
                boolean exists = rs.next();
                boolean needUpdate = false;
                if (exists) {
                    long dbRevision = rs.getLong(1);
                    if (dbRevision < revision) {
                        // supplied revision is higher; will update the database value
                        needUpdate = true;
                    } else {
                        revision = dbRevision;
                    }
                }

                // Insert the given revision in the database
                if (!exists) {
                    conHelper.exec(insertLocalRevisionStmtSQL, revision, getId());
                } else if (needUpdate) {
                    // update the revision in the database
                    conHelper.exec(updateLocalRevisionStmtSQL, revision, getId());
                }

                // Set the cached local revision and return
                localRevision = revision;
            } catch (SQLException e) {
                throw new JournalException("Failed to initialize local revision", e);
            } finally {
                DbUtility.close(rs);
            }
        }

        @Override
        public synchronized long get() {
            return localRevision;
        }

        @Override
        public synchronized void set(long localRevision) throws JournalException {
            // Update the cached value and the table with local revisions.
            try {
                conHelper.exec(updateLocalRevisionStmtSQL, localRevision, getId());
                this.localRevision = localRevision;
            } catch (SQLException e) {
                throw new JournalException("Failed to update local revision.", e);
            }
        }

        @Override
        public void close() {
            // nothing to do
        }
    }

    /**
     * Class for maintaining the revision table. This is only useful if all
     * JR information except the search index is in the database (i.e., node types
     * etc). In that case, revision data can safely be thrown away from the JOURNAL table.
     */
    private class RevisionTableJanitor implements Runnable {

        /**
         * {@inheritDoc}
         */
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    log.info("Next clean-up run scheduled at {}", janitorNextRun.getTime());
                    long sleepTime = janitorNextRun.getTimeInMillis() - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        Thread.sleep(sleepTime);
                    }
                    cleanUpOldRevisions();
                    janitorNextRun.add(Calendar.SECOND, janitorSleep);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            log.info("Interrupted: stopping clean-up task.");
        }

        /**
         * Cleans old revisions from the clustering table.
         */
        protected void cleanUpOldRevisions() {
            ResultSet rs = null;
            try {
                long minRevision = 0;
                rs = conHelper.exec(selectMinLocalRevisionStmtSQL, null, false, 0);
                boolean cleanUp = rs.next();
                if (cleanUp) {
                    minRevision = rs.getLong(1);
                }

                // Clean up if necessary:
                if (cleanUp) {
                    conHelper.exec(cleanRevisionStmtSQL, minRevision);
                    log.info("Cleaned old revisions up to revision {}.", minRevision);
                }

            } catch (Exception e) {
                log.warn("Failed to clean up old revisions.", e);
            } finally {
                DbUtility.close(rs);
            }
        }
    }
}
