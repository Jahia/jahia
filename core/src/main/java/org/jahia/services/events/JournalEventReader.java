/**
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms & Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.services.events;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.cluster.*;
import org.apache.jackrabbit.core.journal.Journal;
import org.apache.jackrabbit.core.journal.JournalException;
import org.apache.jackrabbit.core.journal.Record;
import org.apache.jackrabbit.core.journal.RecordIterator;
import org.jahia.api.Constants;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.impl.jackrabbit.SpringJackrabbitRepository;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.properties.PropertiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.observation.EventIterator;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Service that reads the JCR events directly from the JCR journal and passes them to the specified listeners.
 * 
 * @author Sergiy Shyrkov
 */
public class JournalEventReader {

    private static final String LAST_PROCESSED_JOURNAL_REVISION_PROPERTY = "lastProcessedJournalRevision";
    private static final String LAST_PROCESSED_JOURNAL_REVISION_FOLDER = "journal-event-reader";
    private static final String LAST_PROCESSED_JOURNAL_REVISION_FILE = "org.jahia.utils.journal-event-reader.properties";

    private static final Logger logger = LoggerFactory.getLogger(JournalEventReader.class);

    private SettingsBean settingsBean;
    private String lastProcessedRevisionFilePath;

    /**
     * Polls the underlying journal for events of the type ChangeLogRecord that happened after a given revision, on a given workspace.
     *
     * @param revision starting revision
     * @param workspace the workspace name
     * @return the list of collected {@link ChangeLogRecord}s
     */
    private List<ChangeLogRecord> getChangeLogRecords(long revision, final String workspace) {
        ClusterNode cn = SpringJackrabbitRepository.getInstance().getClusterNode();
        if (cn == null) {
            return Collections.emptyList();
        }

        logger.info("Getting journal change log records starting with revision {} for workspace {}", revision,
                workspace);

        Journal journal = cn.getJournal();
        final List<ChangeLogRecord> changeLogRecords = new LinkedList<>();
        ClusterRecordDeserializer deserializer = new ClusterRecordDeserializer();
        RecordIterator records = null;
        try {
            records = journal.getRecords(revision);
            while (records.hasNext()) {
                Record record = records.nextRecord();
                ClusterRecord r = null;
                try {
                    r = deserializer.deserialize(record);
                } catch (JournalException e) {
                    logger.error("Unable to read revision '" + record.getRevision() + "'.", e);
                }
                if (r == null) {
                    continue;
                }
                r.process(new ClusterRecordProcessor() {
                    public void process(ChangeLogRecord record) {
                        String eventW = record.getWorkspace();
                        if (eventW != null ? eventW.equals(workspace) : workspace == null) {
                            changeLogRecords.add(record);
                        }
                    }

                    public void process(LockRecord record) {
                        // ignore
                    }

                    public void process(NamespaceRecord record) {
                        // ignore
                    }

                    public void process(NodeTypeRecord record) {
                        // ignore
                    }

                    public void process(PrivilegeRecord record) {
                        // ignore
                    }

                    public void process(WorkspaceRecord record) {
                        // ignore
                    }
                });
            }

            logger.info("Found {} journal change log records for workspace {}", changeLogRecords.size(), workspace);
        } catch (JournalException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (records != null) {
                records.close();
            }
        }
        return changeLogRecords;
    }

    private EventIterator getEventIterator(SessionImpl session, ChangeLogRecord record, int eventTypes) {
        return new FilteredEventIterator(session, record.getEvents().iterator(), record.getTimestamp(),
                record.getUserData(), eventTypes);
    }

    private long readStartRevision(String key) {
        long startRevision = 0;
        String value = new PropertiesManager(lastProcessedRevisionFilePath)
                .getProperty(getPropertyKey(key));
        if (StringUtils.isNotEmpty(value)) {
            startRevision = Long.parseLong(value.trim());
        }

        return startRevision;
    }

    private String getPropertyKey(String key) {
        return LAST_PROCESSED_JOURNAL_REVISION_PROPERTY + "." + key;
    }

    private boolean isEnabled() {
        return settingsBean.isClusterActivated() && settingsBean.isProcessingServer();
    }

    /**
     * Stores in a dedicated properties file the last processed journal revision.
     * @param key the key is associate to the last processed journal revision value, it allow to identify the property
     */
    public void rememberLastProcessedJournalRevision(String key) {
        if (!isEnabled()) {
            return;
        }
        ClusterNode cn = SpringJackrabbitRepository.getInstance().getClusterNode();
        if (cn != null) {
            String revision = String.valueOf(cn.getRevision());
            PropertiesManager propManager = new PropertiesManager(lastProcessedRevisionFilePath);
            String propertyKey = getPropertyKey(key);
            if (!StringUtils.equals(propManager.getProperty(propertyKey), revision)) {
                propManager.setProperty(propertyKey, String.valueOf(revision));
                propManager.storeProperties();

                logger.info("Remembered last processed journal revision as {}", revision);
            }
        }
    }

    /**
     * Reads the JCR events directly from the JCR journal and passes them to the specified listener.
     * 
     * @param listener the listener to call with the read events
     * @param key the key is associate to the last processed journal revision value, it allow to identify the property
     */
    public void replayMissedEvents(DefaultEventListener listener, String key) {
        if (!isEnabled()) {
            return;
        }

        long startTime = System.currentTimeMillis();

        long startRevision = readStartRevision(key);
        if (startRevision <= 0) {
            // won't process anything as we cannot detect the last processed revision
            return;
        }

        logger.info("Checking for missed JCR events to be replayed for listener {} starting from revision {}", listener,
                startRevision);

        try {
            Integer processedRecords = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Integer>() {
                @Override
                public Integer doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    return replayMissedEvents(startRevision, listener, session);
                }
            });

            if (processedRecords != null && processedRecords.intValue() > 0) {
                logger.info("Done replaying {} missed JCR journal revisions for listener {} in {} ms",
                        new Object[] { processedRecords, listener, System.currentTimeMillis() - startTime });
            } else {
                logger.info(
                        "Done checking missed JCR events for listener {} in {} ms. No records to replay were found.",
                        listener, System.currentTimeMillis() - startTime);
            }
        } catch (Exception e) {
            logger.error("Error replaying missed JCR events by listener " + listener, e);
        }
    }

    private int replayMissedEvents(long startRevision, DefaultEventListener listener, JCRSessionWrapper session)
            throws RepositoryException {
        List<ChangeLogRecord> changeLogRecords = getChangeLogRecords(startRevision,
                StringUtils.defaultString(listener.getWorkspace(), Constants.EDIT_WORKSPACE));

        if (changeLogRecords.isEmpty()) {
            return 0;
        }

        SessionImpl jrSession = (SessionImpl) session.getRootNode().getRealNode().getSession();

        int processedRecords = 0;
        for (ChangeLogRecord r : changeLogRecords) {
            EventIterator evtIterator = getEventIterator(jrSession, r, listener.getEventTypes());
            processedRecords++;

            try {
                listener.onEvent(evtIterator);

                logger.info("Processed {} event(s) (revision: {}) by listener {}",
                        new Object[] { evtIterator.getSize(), r.getRevision(), listener });
            } catch (Exception e) {
                logger.error("Error replaying JCR events (revision: " + r.getRevision() + ") by listener " + listener,
                        e);
            }
        }

        return processedRecords;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;

        lastProcessedRevisionFilePath = new File(
                new File(settingsBean.getJahiaVarDiskPath(), LAST_PROCESSED_JOURNAL_REVISION_FOLDER),
                LAST_PROCESSED_JOURNAL_REVISION_FILE).getPath();
    }
}
