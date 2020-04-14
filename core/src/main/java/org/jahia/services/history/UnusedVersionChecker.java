/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.history;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.persistence.PersistenceManager;
import org.apache.jackrabbit.core.persistence.pool.BundleDbPersistenceManager;
import org.apache.jackrabbit.core.persistence.pool.DerbyPersistenceManager;
import org.apache.jackrabbit.core.persistence.util.NodeInfo;
import org.apache.jackrabbit.core.state.ItemStateException;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.InternalXAVersionManager;
import org.apache.jackrabbit.spi.commons.name.NameConstants;
import org.apache.jackrabbit.util.ISO8601;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing unused version check and cleanup.
 *
 * @author Sergiy Shyrkov
 */
class UnusedVersionChecker {

    private static final Logger logger = LoggerFactory.getLogger(UnusedVersionChecker.class);

    private final UnusedVersionCheckStatus status;
    private final long maxUnused;
    private final boolean deleteUnused;
    private final OutWrapper out;
    private final List<NodeId> nodesToCheck = new LinkedList<NodeId>();
    private final List<NodeId> unused = new LinkedList<NodeId>();

    private boolean forceStop;
    private BundleDbPersistenceManager persistenceManager;

    UnusedVersionChecker(UnusedVersionCheckStatus status, long maxUnused, boolean deleteUnused, OutWrapper out) {
        this.status = status;
        this.maxUnused = maxUnused;
        this.deleteUnused = deleteUnused;
        this.out = out;
    }

    private NodeId checkUnused(JCRSessionWrapper session) throws RepositoryException {
        NodeId lastCheckedNodeId = null;
        try {
            long timer = System.currentTimeMillis();
            Map<NodeId, Boolean> existsReferencesToNodes = persistenceManager.existsReferencesToNodes(nodesToCheck);
            if (logger.isDebugEnabled()) {
                logger.debug("persistenceManager.existsReferencesToNodes took {} ms", (System.currentTimeMillis() - timer));
            }
            int purgeVersionsBatchSize = Integer.getInteger("org.jahia.services.history.purgeVersionsBatchSize", 100);
            for (Map.Entry<NodeId, Boolean> ref : existsReferencesToNodes.entrySet()) {
                lastCheckedNodeId = ref.getKey();
                status.checked++;
                nodesToCheck.remove(ref.getKey());
                if (!ref.getValue()) {
                    status.orphaned++;
                    if (deleteUnused) {
                        unused.add(ref.getKey());
                    }
                    if (status.orphaned >= maxUnused) {
                        out.echo("{} versions checked and the limit of {} versions is reached. Stopping checks.", status.checked, maxUnused);
                        break;
                    }
                    if (deleteUnused && status.orphaned > 0 && unused.size() >= purgeVersionsBatchSize) {
                        delete(session);
                    }
                }
                if (forceStop) {
                    return lastCheckedNodeId;
                }
            }
        } catch (ItemStateException e) {
            logger.warn(e.getMessage(), e);
        } finally {
            if (status.orphaned < maxUnused) {
                out.echo(status.toString());
            }
        }
        return lastCheckedNodeId;
    }

    private void delete(JCRSessionWrapper session) {
        out.echo("Start deleting {} versions", unused.size());
        try {
            long nb = status.deletedVersionItems;
            long timer = System.currentTimeMillis();
            NodeVersionHistoryHelper.purgeUnusedVersions(unused, session, status);
            out.echo("deleted {} version items in {} ms", status.deletedVersionItems - nb, System.currentTimeMillis()
                    - timer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            out.echo("Error deleting version histories. Cause: {}", e.getMessage());
        } finally {
            unused.clear();
        }
    }

    private Map<NodeId, NodeInfo> getAllNodeInfos(NodeId bigger, int maxCount) {
        Map<NodeId, NodeInfo> batch = Collections.emptyMap();
        try {
            batch = persistenceManager.getAllNodeInfos(bigger, maxCount);
        } catch (ItemStateException e) {
            logger.error(e.getMessage(), e);
        }
        return batch;
    }

    private int getCheckUnusedBatchSize() {
        int checkUnusedBatchSize = Integer.getInteger("org.jahia.services.history.checkUnusedBatchSize", 0);
        if (checkUnusedBatchSize != 0) {
            return checkUnusedBatchSize;
        }
        if (persistenceManager instanceof DerbyPersistenceManager) {
            return 100;
        } else if (persistenceManager.getStorageModel() == BundleDbPersistenceManager.SM_LONGLONG_KEYS) {
            return 500;
        } else {
            return 1000;
        }
    }

    private boolean isOlder(NodeInfo info, long purgeOlderThanTimestamp) {
        if (purgeOlderThanTimestamp <= 0) {
            return true;
        }
        Calendar created = null;
        if (info.getCreated() != null) {
            try {
                created = ISO8601.parse(info.getCreated());
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.warn("Error parsing creation date " + info.getCreated() + " for node " + info.getId(), e);
                } else {
                    logger.warn("Error parsing creation date " + info.getCreated() + " for node " + info.getId());
                }
            }
        }

        return created != null && created.getTimeInMillis() < purgeOlderThanTimestamp;
    }

    private boolean isRootVersion(NodeInfo info) {
        List<NodeId> predecessors = info.getReferences().get(NameConstants.JCR_PREDECESSORS);
        return predecessors == null || predecessors.isEmpty();
    }

    NodeId perform(JCRSessionWrapper session, long purgeOlderThanTimestamp) throws RepositoryException {
        return perform(session, purgeOlderThanTimestamp, null);
    }

    NodeId perform(JCRSessionWrapper session, long purgeOlderThanTimestamp, NodeId lastCheckedNodeId) throws RepositoryException {

        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        PersistenceManager pm = null;
        if (vm instanceof InternalVersionManagerImpl) {
            pm = ((InternalVersionManagerImpl) vm).getPersistenceManager();
        } else if (vm instanceof InternalXAVersionManager) {
            pm = ((InternalXAVersionManager) vm).getPersistenceManager();
        } else {
            logger.warn("Unknown implemmentation of the InternalVersionManager: {}.", vm.getClass().getName());
        }
        if (pm == null || !(pm instanceof BundleDbPersistenceManager)) {
            out.echo("The provided PersistenceManager {} is not an instance"
                    + " of BundleDbPersistenceManager. Unable to proceed.", pm);
            return lastCheckedNodeId;
        }

        persistenceManager = (BundleDbPersistenceManager) pm;

        lastCheckedNodeId = traverse(session, purgeOlderThanTimestamp, lastCheckedNodeId);

        if (forceStop) {
            out.echo("Request received to stop checking nodes.");
        } else if (deleteUnused && unused.size() > 0) {
            delete(session);
        }

        return lastCheckedNodeId;
    }

    void stop() {
        this.forceStop = true;
    }

    private NodeId traverse(JCRSessionWrapper session, long purgeOlderThanTimestamp, NodeId lastCheckedNodeId) throws RepositoryException {

        int nodeInfosBatchSize = Integer.getInteger("org.jahia.services.history.loadVersionBundleBatchSize", 8000);
        int checkUnusedBatchSize = getCheckUnusedBatchSize();
        NodeId lastReadNodeId = lastCheckedNodeId;
        boolean endReadingNodesSequence = false;

        batches: do {
            Map<NodeId, NodeInfo> batch = getAllNodeInfos(lastReadNodeId, nodeInfosBatchSize);
            if (batch.size() < nodeInfosBatchSize) {
                // There was not enough nodes to fill the entire batch, which means we've reached the end of the nodes sequence when reading them.
                endReadingNodesSequence = true;
            }
            for (NodeInfo info : batch.values()) {
                lastReadNodeId = info.getId();
                if (forceStop) {
                    return lastCheckedNodeId;
                }
                if (!NameConstants.NT_VERSION.equals(info.getNodeTypeName())) {
                    continue;
                }
                if (isRootVersion(info)) {
                    continue;
                }
                if (!isOlder(info, purgeOlderThanTimestamp)) {
                    continue;
                }
                nodesToCheck.add(info.getId());
                if (nodesToCheck.size() >= checkUnusedBatchSize) {
                    lastCheckedNodeId = checkUnused(session);
                    if (status.orphaned >= maxUnused) {
                        break batches;
                    }
                }
            }
        } while (!endReadingNodesSequence);

        if (!nodesToCheck.isEmpty() && status.orphaned < maxUnused) {
            lastCheckedNodeId = checkUnused(session);
        }
        if (deleteUnused && unused.size() > 0) {
            delete(session);
        }

        if (endReadingNodesSequence && nodesToCheck.isEmpty()) {
            // We've reached the end of the nodes sequence when checking them: return null to indicate the need to start from the very beginning next time.
            return null;
        } else {
            return lastCheckedNodeId;
        }
    }
}