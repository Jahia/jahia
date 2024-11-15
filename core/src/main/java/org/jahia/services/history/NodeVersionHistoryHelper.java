/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.history;

import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.version.InternalVersionHistory;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.InternalXAVersionManager;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version history utility class for purging all version entries of the specified node.
 *
 * @author Sergiy Shyrkov
 */
public final class NodeVersionHistoryHelper {

    private static boolean checkingOrphans;

    private static boolean checkingUnused;

    private static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryHelper.class);

    private static OrphanedVersionHistoryChecker orphanedChecker;

    protected static final int PURGE_HISTORY_CHUNK = Integer.getInteger(
            "org.jahia.services.history.purgeVersionHistoryBatchSize", 100);

    private static final String DATA_NODE_NAME = "unusedVersionChecker";

    private static final String LAST_CHECKED_ID_PROPERTY = "lastCheckedNodeId";

    private static UnusedVersionChecker unusedChecker;

    /**
     * Triggers the process of orphaned version histories check. If the <code>deleteOrphans</code> is set to <code>true</code> also performs
     * the purge of found orphaned version histories.
     *
     * This method ensures that only one check process runs at a time.
     *
     * @param maxOrphans
     *            the maximum number of orphaned histories found at which the process is stopped
     * @param deleteOrphans
     *            if set to <code>true</code> performs the purge of found orphaned version histories; in case of <code>false</code> only the
     *            found orphaned version history count is reported, but no removal is done
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized OrphanedVersionHistoryCheckStatus checkOrphaned(final long maxOrphans,
            final boolean deleteOrphans, final Writer statusOut) throws RepositoryException {
        if (checkingOrphans) {
            throw new IllegalStateException("The version history is currently beeing checked for orphans."
                    + " Cannot start the second process.");
        }
        checkingOrphans = true;
        long timer = System.currentTimeMillis();
        final OrphanedVersionHistoryCheckStatus status = new OrphanedVersionHistoryCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} orphaned version history", deleteOrphans ? "deleting" : "checking");

        orphanedChecker = new OrphanedVersionHistoryChecker(status, maxOrphans, deleteOrphans, out);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    orphanedChecker.perform(session);
                    return null;
                }
            });
        } finally {
            checkingOrphans = false;
            orphanedChecker = null;
            out.echo("Done checking orphaned version history in {} ms. Status: {}",
                    (System.currentTimeMillis() - timer), status.toString());
        }

        return status;
    }

    /**
     * Triggers the process of unused versions check. If the <code>deleteUnused</code> is set to <code>true</code> also performs the purge
     * of found unused versions.
     *
     * This method ensures that only one check process runs at a time.
     *
     * @param maxUnused
     *            the maximum number of unused versions found at which the process is stopped
     * @param deleteUnused
     *            if set to <code>true</code> performs the purge of found unused versions; in case of <code>false</code> only the found
     *            unused versions count is reported, but no removal is done
     * @param purgeOlderThanTimestamp
     *            if positive value is provided checks that the unused versions are older than the specified date (timestamp in milliseconds
     *            UTC); if <code>0</code> value is provided all found unused versions are considered in the process
     * @param statusOut
     *            a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException
     *             in case of JCR errors
     */
    public static synchronized UnusedVersionCheckStatus checkUnused(final long maxUnused, final boolean deleteUnused,
            final long purgeOlderThanTimestamp, final Writer statusOut) throws RepositoryException {
        if (checkingUnused) {
            throw new IllegalStateException("Unused versions are currently beeing checked."
                    + " Cannot start the second process.");
        }
        checkingUnused = true;
        long timer = System.currentTimeMillis();
        final UnusedVersionCheckStatus status = new UnusedVersionCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} unused versions{}", deleteUnused ? "deleting" : "checking",
                purgeOlderThanTimestamp <= 0 ? "" : (" older than " + new Date(purgeOlderThanTimestamp)));

        unusedChecker = new UnusedVersionChecker(status, maxUnused, deleteUnused, out);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {

                @Override
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    JCRNodeWrapper root = session.getRootNode();

                    NodeId lastCheckedNodeId = null;
                    if (root.hasNode(DATA_NODE_NAME)) {
                        JCRNodeWrapper data = root.getNode(DATA_NODE_NAME);
                        String lastCheckedNodeUuid = data.getPropertyAsString(LAST_CHECKED_ID_PROPERTY);
                        lastCheckedNodeId = (lastCheckedNodeUuid == null ? null : NodeId.valueOf(lastCheckedNodeUuid));
                    }

                    lastCheckedNodeId = unusedChecker.perform(session, purgeOlderThanTimestamp, lastCheckedNodeId);

                    JCRNodeWrapper data;
                    if (root.hasNode(DATA_NODE_NAME)) {
                        data = root.getNode(DATA_NODE_NAME);
                    } else {
                        data = root.addNode(DATA_NODE_NAME, Constants.NT_UNSTRUCTURED);
                    }
                    data.setProperty(LAST_CHECKED_ID_PROPERTY, (lastCheckedNodeId == null ? null : lastCheckedNodeId.toString()));
                    session.save();

                    return null;
                }
            });
        } finally {
            checkingUnused = false;
            unusedChecker = null;
            out.echo("Done checking unused versions in {} ms. Status: {}", (System.currentTimeMillis() - timer),
                    status.toString());
        }

        return status;
    }

    /**
     * Forces stop of the orphaned version history check process if it is currently running.
     */
    public static void forceStopOrphanedCheck() {
        if (orphanedChecker != null) {
            orphanedChecker.stop();
        }
    }

    /**
     * Forces stop of the unused versions check process if it is currently running.
     */
    public static void forceStopUnusedCheck() {
        if (unusedChecker != null) {
            unusedChecker.stop();
        }
    }

    static void internalPurgeVersionHistories(List<InternalVersionHistory> histories, JCRSessionWrapper session,
            VersionHistoryCheckStatus status) throws VersionException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        int[] result = null;
        if (vm instanceof InternalVersionManagerImpl) {
            result = ((InternalVersionManagerImpl) vm).purgeVersions(providerSession, histories);
        } else if (vm instanceof InternalXAVersionManager) {
            result = ((InternalXAVersionManager) vm).purgeVersions(providerSession, histories);
        } else {
            logger.warn("Unknown implemmentation of the InternalVersionManager: {}.", vm.getClass().getName());
        }

        if (result != null) {
            if (!(status instanceof OrphanedVersionHistoryCheckStatus)) {
                status.checked += histories.size();
            }
            status.deleted += result[0];
            status.deletedVersionItems += result[1];
        }
    }

    private static VersionHistoryCheckStatus internalPurgeVersionHistories(final Set<String> nodeIdentifiers) {
        final VersionHistoryCheckStatus status = new VersionHistoryCheckStatus();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(session -> {
                SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
                InternalVersionManager vm = providerSession.getInternalVersionManager();

                List<InternalVersionHistory> histories = new LinkedList<>();
                for (String id : nodeIdentifiers) {
                    try {
                        histories.add(vm.getVersionHistoryOfNode(NodeId.valueOf(id)));
                    } catch (ItemNotFoundException e) {
                        // no history found
                    }
                }

                internalPurgeVersionHistories(histories, session, status);
                return Boolean.TRUE;
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return status;
    }

    /**
     * Returns <code>true</code> if the process for checking orphans is currently running.
     *
     * @return <code>true</code> if the process for checking orphans is currently running; <code>false</code> otherwise
     */
    public static boolean isCheckingOrphans() {
        return checkingOrphans;
    }

    /**
     * Returns <code>true</code> if the process for checking unused versions is currently running.
     *
     * @return <code>true</code> if the process for checking unused versions is currently running; <code>false</code> otherwise
     */
    public static boolean isCheckingUnused() {
        return checkingUnused;
    }

    static void purgeUnusedVersions(List<NodeId> unusedVersions, JCRSessionWrapper session,
            UnusedVersionCheckStatus status) throws PathNotFoundException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        int result = 0;
        if (vm instanceof InternalVersionManagerImpl) {
            result = ((InternalVersionManagerImpl) vm).purgeUnusedVersions(providerSession, unusedVersions);
        } else if (vm instanceof InternalXAVersionManager) {
            result = ((InternalXAVersionManager) vm).purgeUnusedVersions(providerSession, unusedVersions);
        } else {
            logger.warn("Unknown implemmentation of the InternalVersionManager: {}.", vm.getClass().getName());
        }

        status.deletedVersionItems += result;
    }

    static void purgeVersionHistories(List<NodeId> historyIds, JCRSessionWrapper session,
            VersionHistoryCheckStatus status) throws VersionException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        List<InternalVersionHistory> histories = new LinkedList<InternalVersionHistory>();
        for (NodeId id : historyIds) {
            try {
                histories.add(vm.getVersionHistory(id));
            } catch (ItemNotFoundException e) {
                // no history found
            }
        }

        internalPurgeVersionHistories(histories, session, status);
    }

    /**
     * Performs the removal of unused versions for the specified nodes. All unused versions are removed, no mater the "age" of the version.
     *
     * @param nodes an instance of {@link NodeIterator} for processing nodes
     * @param statusOut a writer to log current processing status into
     * @return the status object to indicate the result of the check
     * @throws RepositoryException in case of JCR errors
     */
    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(NodeIterator nodes, Writer statusOut)
            throws RepositoryException {
        OutWrapper out = new OutWrapper(logger, statusOut);
        out.echo("Start checking version history");

        Set<String> ids = new HashSet<>();
        VersionHistoryCheckStatus status = new VersionHistoryCheckStatus();
        while (nodes.hasNext()) {
            ids.add(nodes.nextNode().getIdentifier());
            if (ids.size() >= PURGE_HISTORY_CHUNK) {
                VersionHistoryCheckStatus chunkResult = internalPurgeVersionHistories(ids);
                ids.clear();
                status.merge(chunkResult);
                out.echo(status.toString());
            }
        }
        if (!ids.isEmpty()) {
            // purge the rest
            VersionHistoryCheckStatus chunkResult = internalPurgeVersionHistories(ids);
            ids.clear();
            status.merge(chunkResult);
            out.echo(status.toString());
        }

        return status;
    }

    /**
     * Performs the removal of unused versions for the specified nodes. All unused versions are removed, no mater the "age" of the version.
     *
     * @param nodeIdentifiers a set of node IDs to process
     * @return the status object to indicate the result of the check
     */
    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers) {
        return purgeVersionHistoryForNodes(nodeIdentifiers, (Writer) null);
    }

    /**
     * Performs the removal of unused versions for the specified nodes. All unused versions are removed, no mater the "age" of the version.
     *
     * @param nodeIdentifiers a set of node IDs to process
     * @param statusOut a writer to log current processing status into
     * @return the status object to indicate the result of the check
     */
    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers, Writer statusOut) {
        final OutWrapper out = new OutWrapper(logger, statusOut);
        out.echo("Start checking version history for {} nodes, using batch of {} nodes", nodeIdentifiers.size(), PURGE_HISTORY_CHUNK);

        Set<String> ids = new HashSet<>();
        VersionHistoryCheckStatus status = new VersionHistoryCheckStatus();
        for (String nodeIdentifier : nodeIdentifiers) {
            ids.add(nodeIdentifier);
            if (ids.size() >= PURGE_HISTORY_CHUNK) {
                VersionHistoryCheckStatus chunkResult = internalPurgeVersionHistories(ids);
                ids.clear();
                status.merge(chunkResult);
            }
        }
        if (!ids.isEmpty()) {
            // purge the rest
            VersionHistoryCheckStatus chunkResult = internalPurgeVersionHistories(ids);
            ids.clear();
            status.merge(chunkResult);
        }

        out.echo("Done checking version history for nodes. Version history status: {}", status.toString());
        return status;
    }

    private NodeVersionHistoryHelper() {
        super();
    }
}
