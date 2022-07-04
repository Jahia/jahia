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
package org.jahia.services.content.impl.jackrabbit;

import org.apache.jackrabbit.core.journal.*;
import org.apache.jackrabbit.core.util.db.*;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * JahiaDatabaseJournal is a copy of DatabaseJournal, but extends JahiaAbstractJournal to use lockAndSync timeouts
 */
public class JahiaDatabaseJournal extends JahiaAbstractJournal implements DatabaseAware {

    /**
     * Default journal table name, used to check schema completeness.
     */
    private static final String DEFAULT_JOURNAL_TABLE = "JOURNAL";

    /**
     * Local revisions table name, used to check schema completeness.
     */
    private static final String LOCAL_REVISIONS_TABLE = "LOCAL_REVISIONS";
    public static final int DEFAULT_JANITOR_SLEEP = 60 * 60 * 24;

    /**
     * Logger.
     */
    static Logger log = LoggerFactory.getLogger(JahiaDatabaseJournal.class);

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
    ConnectionHelper conHelper;

    /**
     * Auto commit level.
     */
    private int lockLevel;

    /**
     * Locked revision.
     */
    private long lockedRevision;

    /**
     * Whether the revision table janitor thread is enabled.
     */
    private boolean janitorEnabled = false;

    /**
     * The sleep time of the revision table janitor in seconds, 1 day default.
     */
    int janitorSleep = DEFAULT_JANITOR_SLEEP;

    /**
     * Indicates when the next run of the janitor is scheduled.
     * The first run is scheduled by default at 03:00 hours.
     */
    Calendar janitorNextRun;

    private Thread janitorThread;

    /**
     * Whether the schema check must be done during initialization.
     */
    private boolean schemaCheckEnabled = true;

    /**
     * The instance that manages the local revision.
     */
    private DatabaseRevision databaseRevision;

    /**
     * SQL statement returning all revisions within a range.
     */
    protected String selectRevisionsStmtSQL;

    /**
     * SQL statement updating the global revision.
     */
    protected String updateGlobalStmtSQL;

    /**
     * SQL statement returning the global revision.
     */
    protected String selectGlobalStmtSQL;

    /**
     * SQL statement appending a new record.
     */
    protected String insertRevisionStmtSQL;

    /**
     * SQL statement returning the minimum of the local revisions.
     */
    protected String selectMinLocalRevisionStmtSQL;

    /**
     * SQL statement returning the minimum of the journal revisions
     */
    protected String selectMinJournalStmtSQL;

    /**
     * SQL statement removing a set of revisions with from the journal table.
     */
    protected String cleanRevisionStmtSQL;

    /**
     * SQL statement returning the local revision of this cluster node.
     */
    protected String getLocalRevisionStmtSQL;

    /**
     * SQL statement for inserting the local revision of this cluster node.
     */
    protected String insertLocalRevisionStmtSQL;

    /**
     * SQL statement for updating the local revision of this cluster node.
     */
    protected String updateLocalRevisionStmtSQL;

    /**
     * Schema object prefix, bean property.
     */
    protected String schemaObjectPrefix;

    /**
     * Maximum number of revisions to remove during clean-up of old revisions
     */
    protected long janitorBatchLimit = 10000L;

    /**
     * The repositories {@link ConnectionFactory}.
     */
    private ConnectionFactory connectionFactory;

    /**
     * Default constructor
     */
    public JahiaDatabaseJournal() {
        databaseType = "default";
        schemaObjectPrefix = "";
        setJanitorFirstRunHourOfDay(3);
    }

    /**
     * {@inheritDoc}
     */
    public void setConnectionFactory(ConnectionFactory connnectionFactory) {
        this.connectionFactory = connnectionFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(String id, NamespaceResolver resolver)
            throws JournalException {

        super.init(id, resolver);

        init();

        try {
            conHelper = createConnectionHelper(getDataSource());

            // make sure schemaObjectPrefix consists of legal name characters only
            schemaObjectPrefix = conHelper.prepareDbIdentifier(schemaObjectPrefix);

            // check if schema objects exist and create them if necessary
            if (isSchemaCheckEnabled()) {
                createCheckSchemaOperation().run();
            }

            // Make sure that the LOCAL_REVISIONS table exists (see JCR-1087)
            if (isSchemaCheckEnabled()) {
                checkLocalRevisionSchema();
            }

            buildSQLStatements();
            initInstanceRevisionAndJanitor();
        } catch (Exception e) {
            String msg = "Unable to create connection.";
            throw new JournalException(msg, e);
        }
        log.info("DatabaseJournal initialized.");
    }

    private DataSource getDataSource() throws RepositoryException, SQLException {
        if (getDataSourceName() == null || "".equals(getDataSourceName())) {
            return connectionFactory.getDataSource(getDriver(), getUrl(), getUser(), getPassword());
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
        return new ConnectionHelper(dataSrc, false);
    }

    /**
     * This method is called from {@link #init(String, NamespaceResolver)} after the
     * {@link #createConnectionHelper(DataSource)} method, and returns a default {@link CheckSchemaOperation}.
     * Subclasses can overrride this implementation to get a customized implementation.
     *
     * @return a new {@link CheckSchemaOperation} instance
     */
    protected CheckSchemaOperation createCheckSchemaOperation() {
        InputStream in = DatabaseJournal.class.getResourceAsStream(databaseType + ".ddl");
        return new CheckSchemaOperation(conHelper, in, schemaObjectPrefix + DEFAULT_JOURNAL_TABLE).addVariableReplacement(
                CheckSchemaOperation.SCHEMA_OBJECT_PREFIX_VARIABLE, schemaObjectPrefix);
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
    @SuppressWarnings("java:S1141")
    protected void init() throws JournalException {
        if (driver == null && dataSourceName == null) {
            String msg = "Driver not specified.";
            throw new JournalException(msg);
        }
        if (url == null && dataSourceName == null) {
            String msg = "Connection URL not specified.";
            throw new JournalException(msg);
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
        if (databaseType == null && url !=null) {
            try {
                databaseType = getDatabaseTypeFromURL(url);
            } catch (IllegalArgumentException e) {
                String msg = "Unable to derive database type from URL: " + e.getMessage();
                throw new JournalException(msg, e);
            }
        }
    }

    /**
     * Initialize the instance revision manager and the janitor thread.
     *
     * @throws JournalException on error
     */
    protected void initInstanceRevisionAndJanitor() throws JournalException {
        databaseRevision = new DatabaseRevision();

        // Get the local file revision from disk (upgrade; see JCR-1087)
        long localFileRevision = 0L;
        if (getRevision() != null) {
            InstanceRevision currentFileRevision = new FileRevision(new File(getRevision()), true);
            localFileRevision = currentFileRevision.get();
            currentFileRevision.close();
        }

        // set batch limit for deleting revisions
        if (SettingsBean.getInstance().getDbJournalJanitorBatchLimit() > 0) {
            janitorBatchLimit = SettingsBean.getInstance().getDbJournalJanitorBatchLimit();
        }

        // set hour of day
        setJanitorFirstRunHourOfDay(SettingsBean.getInstance().getDbJournalJanitorHourOfDay());

        // Now write the localFileRevision (or 0 if it does not exist) to the LOCAL_REVISIONS
        // table, but only if the LOCAL_REVISIONS table has no entry yet for this cluster node
        long localRevision = databaseRevision.init(localFileRevision);
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
    public RecordIterator getRecords(long startRevision) throws JournalException {
        try {
            return new DatabaseRecordIterator(conHelper.exec(selectRevisionsStmtSQL, new Object[]{startRevision}, false, 0),
                                              getResolver(), getNamePathResolver());
        } catch (SQLException e) {
            throw new JournalException("Unable to return record iterator.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public RecordIterator getRecords() throws JournalException {
        try {
            return new DatabaseRecordIterator(conHelper.exec(selectRevisionsStmtSQL, new Object[]{Long.MIN_VALUE}, false, 0),
                                              getResolver(), getNamePathResolver());
        } catch (SQLException e) {
            throw new JournalException("Unable to return record iterator.", e);
        }
    }

    /**
     * Synchronize contents from journal. May be overridden by subclasses.
     * Do the initial sync in batchMode, since some databases (PSQL) when
     * not in transactional mode, load all results in memory which causes
     * out of memory. See JCR-2832
     *
     * @param startRevision start point (exclusive)
     * @param startup       indicates if the cluster node is syncing on startup
     *                      or does a normal sync.
     * @throws JournalException if an error occurs
     */
    @Override
    protected void doSync(long startRevision, boolean startup) throws JournalException {
        if (!startup) {
            // if the cluster node is not starting do a normal sync
            doSync(startRevision);
        } else {
            try {
                startBatch();
                try {
                    doSync(startRevision);
                } finally {
                    endBatch(true);
                }
            } catch (SQLException e) {
                throw new JournalException("Couldn't sync the cluster node", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This journal is locked by incrementing the current value in the table
     * named <code>GLOBAL_REVISION</code>, which effectively write-locks this
     * table. The updated value is then saved away and remembered in the
     * appended record, because a save may entail multiple appends (JCR-884).
     */
    protected void doLock() throws JournalException {
        ResultSet rs = null;
        boolean succeeded = false;

        try {
            startBatch();
        } catch (SQLException e) {
            throw new JournalException("Unable to set autocommit to false.", e);
        }

        try {
            conHelper.exec(updateGlobalStmtSQL);
            if (log.isDebugEnabled()) {
                log.debug("{}.doLock : About to lock global revision table.", this);
            }
            rs = conHelper.exec(selectGlobalStmtSQL, null, false, 0);
            if (!rs.next()) {
                throw new JournalException("No revision available.");
            }
            lockedRevision = rs.getLong(1);
            if (log.isDebugEnabled()) {
                log.debug("{}.doLock : Global revision table locked: {}", this, lockedRevision);
            }
            succeeded = true;
        } catch (SQLException e) {
            throw new JournalException("Unable to lock global revision table.", e);
        } finally {
            DbUtility.close(rs);
            if (!succeeded) {
                doUnlock(false);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    protected void doUnlock(boolean successful) {
        endBatch(successful);
    }

    private void startBatch() throws SQLException {
        if (lockLevel++ == 0) {
            conHelper.startBatch();
            if (log.isDebugEnabled()) {
                log.debug("{}.startBatch : DatabaseJournal batch started.", this);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("{}.startBatch : incremented lockLevel: {}", this, (lockLevel - 1));
            }
        }
    }

    private void endBatch(boolean successful) {
        if (--lockLevel == 0) {
            try {
                conHelper.endBatch(successful);
                if (log.isDebugEnabled()) {
                    log.debug("{}.endBatch : DatabaseJournal batch ended. Successful? {}", this, successful);
                }
            } catch (SQLException e) {
                log.error("failed to end batch", e);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("{}.endBatch : startBatch decremented lockLevel: {} Successful? {}",
                          this, lockLevel, successful);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Save away the locked revision inside the newly appended record.
     */
    @Override
    protected void appending(AppendRecord record) {
        log.info("Creating revision: {}", lockedRevision);
        record.setRevision(lockedRevision);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * We have already saved away the revision for this record.
     */
    protected void append(AppendRecord record, InputStream in, int length)
            throws JournalException {

        try {
            conHelper.exec(insertRevisionStmtSQL, record.getRevision(), getId(), record.getProducerId(),
                           new StreamWrapper(in, length));

        } catch (SQLException e) {
            String msg = "Unable to append revision " + lockedRevision + ".";
            throw new JournalException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (janitorThread != null) {
            janitorThread.interrupt();
        }
    }

    /**
     * Checks if the local revision schema objects exist and creates them if they
     * don't exist yet.
     *
     * @throws IOException if an error occurs
     * @throws SQLException if an SQL exception occurs
     */
    private void checkLocalRevisionSchema() throws IOException, SQLException {
        InputStream localRevisionDDLStream = null;
        boolean done = false;
        try (InputStream in = DatabaseJournal.class.getResourceAsStream(databaseType + ".ddl")) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            String sql = reader.readLine();
            while (sql != null) {
                // Skip comments and empty lines, and select only the statement to create the LOCAL_REVISIONS
                // table.
                if (!sql.startsWith("#") && sql.length() > 0 && sql.contains(LOCAL_REVISIONS_TABLE)) {
                    localRevisionDDLStream = new ByteArrayInputStream(sql.getBytes(StandardCharsets.UTF_8));
                    break;
                }
                // read next sql stmt
                sql = reader.readLine();
            }
            done = true;
        } catch (IOException e) {
            if (!done) {
                throw e;
            }
        }
        // Run the schema check for the single table
        new CheckSchemaOperation(conHelper, localRevisionDDLStream, schemaObjectPrefix
                + LOCAL_REVISIONS_TABLE).addVariableReplacement(
                CheckSchemaOperation.SCHEMA_OBJECT_PREFIX_VARIABLE, schemaObjectPrefix).run();
    }

    /**
     * Builds the SQL statements. May be overridden by subclasses to allow
     * different table and/or column names.
     */
    protected void buildSQLStatements() {
        selectRevisionsStmtSQL =
                "select REVISION_ID, JOURNAL_ID, PRODUCER_ID, REVISION_DATA from "
                        + schemaObjectPrefix + DEFAULT_JOURNAL_TABLE + " where REVISION_ID > ? order by REVISION_ID";
        updateGlobalStmtSQL =
                "update " + schemaObjectPrefix + "GLOBAL_REVISION"
                        + " set REVISION_ID = REVISION_ID + 1";
        selectGlobalStmtSQL =
                "select REVISION_ID from "
                        + schemaObjectPrefix + "GLOBAL_REVISION";
        insertRevisionStmtSQL =
                "insert into " + schemaObjectPrefix + DEFAULT_JOURNAL_TABLE
                        + " (REVISION_ID, JOURNAL_ID, PRODUCER_ID, REVISION_DATA) "
                        + "values (?,?,?,?)";
        selectMinLocalRevisionStmtSQL =
                "select MIN(REVISION_ID) from " + schemaObjectPrefix + LOCAL_REVISIONS_TABLE;
        selectMinJournalStmtSQL =
                "select MIN(REVISION_ID) from " + schemaObjectPrefix + DEFAULT_JOURNAL_TABLE;
        cleanRevisionStmtSQL =
                "delete from " + schemaObjectPrefix + "JOURNAL where REVISION_ID < ? ORDER BY REVISION_ID LIMIT ?";
        getLocalRevisionStmtSQL =
                "select REVISION_ID from " + schemaObjectPrefix + LOCAL_REVISIONS_TABLE
                        + " where JOURNAL_ID = ?";
        insertLocalRevisionStmtSQL =
                "insert into " + schemaObjectPrefix + LOCAL_REVISIONS_TABLE
                        + " (REVISION_ID, JOURNAL_ID) values (?,?)";
        updateLocalRevisionStmtSQL =
                "update " + schemaObjectPrefix + LOCAL_REVISIONS_TABLE
                        + " set REVISION_ID = ? where JOURNAL_ID = ?";
    }

    /**
     * Bean getters
     */
    public String getDriver() {
        return driver;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Get the database type.
     *
     * @return the database type
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * Get the database type.
     *
     * @return the database type
     * @deprecated This method is deprecated; {@link #getDatabaseType} should be used instead.
     */
    @Deprecated
    public String getSchema() {
        return getDatabaseType();
    }

    public String getSchemaObjectPrefix() {
        return schemaObjectPrefix;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean getJanitorEnabled() {
        return janitorEnabled;
    }

    public int getJanitorSleep() {
        return janitorSleep;
    }

    public int getJanitorFirstRunHourOfDay() {
        return janitorNextRun.get(Calendar.HOUR_OF_DAY);
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
        janitorNextRun = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (janitorNextRun.get(Calendar.HOUR_OF_DAY) >= hourOfDay) {
            janitorNextRun.add(Calendar.DAY_OF_MONTH, 1);
        }
        janitorNextRun.set(Calendar.HOUR_OF_DAY, hourOfDay);
        janitorNextRun.set(Calendar.MINUTE, 0);
        janitorNextRun.set(Calendar.SECOND, 0);
        janitorNextRun.set(Calendar.MILLISECOND, 0);
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    /**
     * @return whether the schema check is enabled
     */
    public final boolean isSchemaCheckEnabled() {
        return schemaCheckEnabled;
    }

    /**
     * @param enabled set whether the schema check is enabled
     */
    public final void setSchemaCheckEnabled(boolean enabled) {
        schemaCheckEnabled = enabled;
    }

    /**
     * This class manages the local revision of the cluster node. It
     * persists the local revision in the LOCAL_REVISIONS table in the
     * clustering database.
     */
    public class DatabaseRevision implements InstanceRevision {

        /**
         * The cached local revision of this cluster node.
         */
        private long localRevision;

        /**
         * Indicates whether the init method has been called.
         */
        private boolean initialized = false;

        /**
         * Checks whether there's a local revision value in the database for this
         * cluster node. If not, it writes the given default revision to the database.
         *
         * @param revision the default value for the local revision counter
         * @return the local revision
         * @throws JournalException on error
         */
        protected synchronized long init(long revision) throws JournalException {
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
                initialized = true;
                return revision;

            } catch (SQLException e) {
                throw new JournalException("Failed to initialize local revision", e);
            } finally {
                DbUtility.close(rs);
            }
        }

        public synchronized long get() {
            if (!initialized) {
                throw new IllegalStateException("instance has not yet been initialized");
            }
            return localRevision;
        }

        public synchronized void set(long localRevision) throws JournalException {

            if (!initialized) {
                throw new IllegalStateException("instance has not yet been initialized");
            }

            if (this.localRevision == localRevision) {
                if (log.isDebugEnabled()) {
                    log.debug("{}.set : Local revision already has value {}, will do nothing", this, localRevision);
                }
                return;
            }

            // Update the cached value and the table with local revisions.
            try {
                if (log.isDebugEnabled()) {
                    log.debug(
                            "{}.set : Attempting to update local revision table with revision {} and journal ID {} with connection helper {}",
                            this, localRevision, getId(), conHelper);
                }
                conHelper.exec(updateLocalRevisionStmtSQL, localRevision, getId());
                this.localRevision = localRevision;
                if (log.isDebugEnabled()) {
                    log.debug("{}.set : Local revision table updated with revision {} and journal ID {} with connection helper {}",
                              this, localRevision, getId(), conHelper);
                }
            } catch (SQLException e) {
                throw new JournalException("Failed to update local revision.", e);
            }
        }

        public void close() {
            // nothing to do
        }
    }

    protected long getLockedRevision() {
        return lockedRevision;
    }

    /**
     * Class for maintaining the revision table. This is only useful if all
     * JR information except the search index is in the database (i.e., node types
     * etc). In that case, revision data can safely be thrown away from the JOURNAL table.
     */
    public class RevisionTableJanitor implements Runnable {

        private String getUTCTime(Calendar c) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ssX");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.format(c.getTime());
        }

        /**
         * {@inheritDoc}
         */
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    log.info("Next clean-up run scheduled at {}", getUTCTime(janitorNextRun));
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
                // Clean up if necessary:
                if (cleanUp) {
                    minRevision = rs.getLong(1);
                    DbUtility.close(rs); // close rs connection before recursing
                    doCleanUpOldRevisions(minRevision);
                    log.info("Cleaned old revisions up to revision {}.", minRevision);
                }

            } catch (Exception e) {
                log.warn("Failed to clean up old revisions.", e);
            } finally {
                DbUtility.close(rs); // close in case of exception
            }
        }

        private void doCleanUpOldRevisions(long minRevision) throws SQLException {
            // batch clean-up
            conHelper.exec(cleanRevisionStmtSQL, minRevision, janitorBatchLimit);

            // check if there are more to delete
            ResultSet rs = null;
            try {
                long nextRevision = 0;
                rs = conHelper.exec(selectMinJournalStmtSQL, null, false, 0);
                if (rs.next()) {
                    nextRevision = rs.getLong(1);
                    DbUtility.close(rs); // close rs connection before recursing
                    if (nextRevision < minRevision) {
                        log.debug("Cleaning next {} revisions...", janitorBatchLimit);
                        doCleanUpOldRevisions(minRevision);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to clean up old revisions.", e);
            } finally {
                DbUtility.close(rs); // close in case of exception
            }
        }
    }

}
