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
package org.jahia.services.events;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.cluster.*;
import org.apache.jackrabbit.core.journal.Journal;
import org.apache.jackrabbit.core.journal.JournalException;
import org.apache.jackrabbit.core.journal.Record;
import org.apache.jackrabbit.core.journal.RecordIterator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.DefaultEventListener;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private SessionFactory hibernateSessionFactory;
    private String lastProcessedRevisionFilePath;

    /**
     * Polls the underlying journal for events of the type ChangeLogRecord that happened after a given revision, on a given workspace.
     *
     * @param revision  starting revision
     * @param workspace the workspace name
     * @return the list of collected {@link ChangeLogRecord}s
     */
    private int processChangeLogRecords(long revision, final String workspace, ProcessRecordCallback callback) {
        ClusterNode cn = SpringJackrabbitRepository.getInstance().getClusterNode();
        if (cn == null) {
            return 0;
        }

        logger.info("Getting journal change log records starting with revision {} for workspace {}", revision,
                workspace);

        Journal journal = cn.getJournal();
        ClusterRecordDeserializer deserializer = new ClusterRecordDeserializer();
        RecordIterator records = null;
        final AtomicInteger processedRecords = new AtomicInteger();
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
                            callback.processRecord(record);
                            processedRecords.incrementAndGet();
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

            logger.info("Found {} journal change log records for workspace {}", processedRecords.get(), workspace);
        } catch (JournalException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (records != null) {
                records.close();
            }
        }
        return processedRecords.get();
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
     *
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
                propManager.setProperty(propertyKey, revision);
                propManager.storeProperties();

                logger.info("Remembered last processed journal revision as {}", revision);
            }
        }
    }

    /**
     * Reads the JCR events directly from the JCR journal and passes them to the specified listener.
     *
     * @param listener the listener to call with the read events
     * @param key      the key is associate to the last processed journal revision value, it allow to identify the property
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
            Integer processedRecords = JCRTemplate.getInstance().doExecuteWithSystemSession(session -> replayMissedEvents(startRevision, listener, session));

            if (processedRecords != null && processedRecords > 0) {
                logger.info("Done replaying {} missed JCR journal revisions for listener {} in {} ms",
                        processedRecords, listener, System.currentTimeMillis() - startTime);
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
        SessionImpl jrSession = (SessionImpl) session.getRootNode().getRealNode().getSession();

        return processChangeLogRecords(startRevision,
                StringUtils.defaultString(listener.getWorkspace(), Constants.EDIT_WORKSPACE), new ProcessRecordCallback() {
                    @Override
                    void processRecord(ChangeLogRecord r) {
                        EventIterator evtIterator = getEventIterator(jrSession, r, listener.getEventTypes());

                        try {
                            listener.onEvent(evtIterator);

                            logger.info("Processed {} event(s) (revision: {}) by listener {}",
                                    evtIterator.getSize(), r.getRevision(), listener);
                        } catch (Exception e) {
                            logger.error("Error replaying JCR events (revision: " + r.getRevision() + ") by listener " + listener,
                                    e);
                        }
                    }
                });
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;

        lastProcessedRevisionFilePath = new File(
                new File(settingsBean.getJahiaVarDiskPath(), LAST_PROCESSED_JOURNAL_REVISION_FOLDER),
                LAST_PROCESSED_JOURNAL_REVISION_FILE).getPath();
    }

    public void setHibernateSessionFactory(SessionFactory hibernateSessionFactory) {
        this.hibernateSessionFactory = hibernateSessionFactory;
    }

    /**
     * Process records callback.
     */
    private abstract static class ProcessRecordCallback {
        abstract void processRecord(ChangeLogRecord r);
    }


    public Long getGlobalRevision() {
        try (Session session = hibernateSessionFactory.openSession()) {
            return queryGlobalRevision(session);
        }
    }

    public String getNodeId() {
        return System.getProperty("cluster.node.serverId");
    }

    public Long getLocalRevision() {
        String currentNodeServerId = System.getProperty("cluster.node.serverId");
        if (!StringUtils.isEmpty(currentNodeServerId)) {
            try (Session session = hibernateSessionFactory.openSession()) {
                return queryLocalRevision(session, currentNodeServerId);
            }
        } else {
            logger.warn("Unable to query localRevision, cluster.node.serverId system property not found");
            return null;
        }
    }

    public Map<String, Long> getRevisions() {
        try (Session session = hibernateSessionFactory.openSession()) {
            return queryAllLocalRevisions(session);
        }
    }

    public Boolean isClusterSync() {
        try (Session session = hibernateSessionFactory.openSession()) {
            Long globalRevision = queryGlobalRevision(session);
            if (globalRevision == null) {
                throw new IllegalStateException("Unable to check if cluster is sync, globalRevision not found");
            }

            List<Long> revisions = new ArrayList<>(queryAllLocalRevisions(session).values());
            revisions.add(globalRevision);

            return revisions.stream().distinct().count() <= 1;
        }
    }

    private Long queryLocalRevision(Session session, String journalId) {
        NativeQuery<?> query = session.createSQLQuery("SELECT JOURNAL_ID, REVISION_ID FROM JR_J_LOCAL_REVISIONS WHERE JOURNAL_ID = :journalId");
        query.setParameter("journalId", journalId);
        Object result = query.uniqueResult();
        if (result instanceof Object[]) {
            return ((Number) ((Object[])result)[1]).longValue();
        }
        return null;
    }

    private Map<String, Long> queryAllLocalRevisions(Session session) {
        NativeQuery<?> query = session.createSQLQuery("SELECT JOURNAL_ID, REVISION_ID FROM JR_J_LOCAL_REVISIONS");
        List<?> results = query.getResultList();
        if (results != null) {
            return results.stream()
                    .filter(obj -> obj instanceof Object[])
                    .map(obj -> ((Object[]) obj))
                    .collect(Collectors.toMap(obj -> obj[0].toString(), obj -> ((Number) obj[1]).longValue()));
        }

        return Collections.emptyMap();
    }

    private Long queryGlobalRevision(Session session) {
        NativeQuery<?> query = session.createSQLQuery("SELECT REVISION_ID FROM JR_J_GLOBAL_REVISION");
        Object result = query.uniqueResult();
        if (result instanceof Number) {
            return ((Number) result).longValue();
        }
        return null;
    }

}
