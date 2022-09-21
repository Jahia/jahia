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

import org.apache.jackrabbit.core.journal.JournalException;
import org.apache.jackrabbit.core.journal.ReadRecord;
import org.apache.jackrabbit.core.journal.Record;
import org.apache.jackrabbit.core.journal.RecordIterator;
import org.apache.jackrabbit.spi.commons.conversion.NamePathResolver;
import org.apache.jackrabbit.spi.commons.namespace.NamespaceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * DatabaseRecordIterator implementation. Local copy of org.apache.jackrabbit.core.journal.DatabaseRecordIterator.
 */
public class DatabaseRecordIterator  implements RecordIterator {
    public static final int REVISION_COLUMN = 1;
    public static final int JOURNAL_ID_COLUMN = 2;
    public static final int PRODUCER_ID_COLUMN = 3;
    public static final int DATA_BLOB_COLUMN = 4;
    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(DatabaseRecordIterator.class);

    /**
     * Underlying result set.
     */
    private final ResultSet rs;

    /**
     * Namespace resolver.
     */
    private final NamespaceResolver resolver;

    /**
     * Name and Path resolver.
     */
    private final NamePathResolver npResolver;

    /**
     * Current record.
     */
    private ReadRecord record;

    /**
     * Last record returned.
     */
    private ReadRecord lastRecord;

    /**
     * Flag indicating whether EOF was reached.
     */
    private boolean isEOF;

    /**
     * Create a new instance of this class.
     * @param rs the result set to iterate over
     * @param resolver a namespace resolver
     * @param npResolver a name path resolver
     */
    public DatabaseRecordIterator(ResultSet rs, NamespaceResolver resolver, NamePathResolver npResolver) {
        this.rs = rs;
        this.resolver = resolver;
        this.npResolver = npResolver;
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        try {
            if (!isEOF && record == null) {
                fetchRecord();
            }
            return !isEOF;
        } catch (SQLException e) {
            String msg = "Error while moving to next record.";
            log.error(msg, e);
            return false;
        }
    }

    /**
     * Return the next record. If there are no more records, throws
     * a <code>NoSuchElementException</code>. If an error occurs,
     * throws a <code>JournalException</code>.
     *
     * @return next record
     * @throws NoSuchElementException if there are no more records
     * @throws JournalException if another error occurs
     */
    @SuppressWarnings("java:S1130")
    public Record nextRecord() throws NoSuchElementException, JournalException {
        if (!hasNext()) {
            String msg = "No current record.";
            throw new NoSuchElementException(msg);
        }
        close(lastRecord);
        lastRecord = record;
        record = null;

        return lastRecord;
    }

    /**
     * {@inheritDoc}
     */
    public void close() {
        if (lastRecord != null) {
            close(lastRecord);
            lastRecord = null;
        }
        try {
            rs.close();
        } catch (SQLException e) {
            String msg = "Error while closing result set: " + e.getMessage();
            log.warn(msg);
        }
    }

    /**
     * Fetch the next record.
     */
    private void fetchRecord() throws SQLException {
        if (rs.next()) {
            long revision = rs.getLong(REVISION_COLUMN);
            String journalId = rs.getString(JOURNAL_ID_COLUMN);
            String producerId = rs.getString(PRODUCER_ID_COLUMN);
            DataInputStream dataIn = new DataInputStream(rs.getBinaryStream(DATA_BLOB_COLUMN));
            record = new ReadRecord(journalId, producerId, revision, dataIn, 0, resolver, npResolver);
        } else {
            isEOF = true;
        }
    }

    /**
     * Close a record.
     *
     * @param record record
     */
    private static void close(ReadRecord record) {
        if (record != null) {
            try {
                record.close();
            } catch (IOException e) {
                String msg = "Error while closing record.";
                log.warn(msg, e);
            }
        }
    }

}
