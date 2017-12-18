/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.tools.OutWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing orphaned version history check and cleanup.
 *
 * @author Sergiy Shyrkov
 */
class OrphanedVersionHistoryChecker {

    private static final Logger logger = LoggerFactory.getLogger(OrphanedVersionHistoryChecker.class);

    private final boolean deleteOrphans;
    private final long maxOrphans;
    private final List<NodeId> nodesToCheck = new LinkedList<NodeId>();
    private final List<NodeId> orphans = new LinkedList<NodeId>();
    private final OutWrapper out;
    private final OrphanedVersionHistoryCheckStatus status;

    private boolean forceStop;
    private BundleDbPersistenceManager persistenceManager;

    OrphanedVersionHistoryChecker(OrphanedVersionHistoryCheckStatus status, long maxOrphans, boolean deleteOrphans, OutWrapper out) {
        this.status = status;
        this.maxOrphans = maxOrphans;
        this.deleteOrphans = deleteOrphans;
        this.out = out;
    }

    private void checkOrphaned(JCRSessionWrapper session) throws RepositoryException {
        try {
            long timer = System.currentTimeMillis();
            Map<NodeId, Boolean> existsReferencesToNodes = persistenceManager.existsReferencesToNodes(nodesToCheck);
            if (logger.isDebugEnabled()) {
                logger.debug("persistenceManager.existsReferencesToNodes took {} ms",
                        (System.currentTimeMillis() - timer));
            }
            status.checked += nodesToCheck.size();
            for (Map.Entry<NodeId, Boolean> ref : existsReferencesToNodes.entrySet()) {
                if (!ref.getValue()) {
                    status.orphaned++;
                    if (deleteOrphans) {
                        orphans.add(ref.getKey());
                    }
                    if (status.orphaned >= maxOrphans) {
                        out.echo("{} version histories checked and the limit of {}"
                                + " orphaned version histories is reached. Stopping checks.", status.checked, maxOrphans);
                        break;
                    }
                    if (deleteOrphans && status.orphaned > 0 && orphans.size() >= NodeVersionHistoryHelper.PURGE_HISTORY_CHUNK) {
                        delete(session);
                    }
                }
                if (forceStop) {
                    return;
                }
            }
        } catch (ItemStateException e) {
            logger.warn(e.getMessage(), e);
        } finally {
            nodesToCheck.clear();
            if (status.orphaned < maxOrphans) {
                out.echo(status.toString());
            }
        }
    }

    private void delete(JCRSessionWrapper session) {
        out.echo("Start deleting version history for {} nodes", orphans.size());
        try {
            long nb = status.deleted;
            long timer = System.currentTimeMillis();
            NodeVersionHistoryHelper.purgeVersionHistories(orphans, session, status);
            out.echo("deleted {} version histories in {} ms", status.deleted - nb, System.currentTimeMillis() - timer);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            out.echo("Error deleting version histories. Cause: {}", e.getMessage());
        } finally {
            orphans.clear();
        }
    }

    private Map<NodeId, NodeInfo> getAllNodeInfos(NodeId bigger) {
        int loadVersionBundleBatchSize = Integer.getInteger("org.jahia.services.history.loadVersionBundleBatchSize", 8000);
        Map<NodeId, NodeInfo> batch = Collections.emptyMap();
        try {
            batch = persistenceManager.getAllNodeInfos(bigger, loadVersionBundleBatchSize);
        } catch (ItemStateException e) {
            logger.error(e.getMessage(), e);
        }
        return batch;
    }

    private int getCheckOrphanedBatchSize() {
        int checkOrphanedBatchSize = Integer.getInteger("org.jahia.services.history.checkOrphanedBatchSize", 0);
        if (checkOrphanedBatchSize != 0) {
            return checkOrphanedBatchSize;
        }
        if (persistenceManager instanceof DerbyPersistenceManager) {
            return 100;
        } else if (persistenceManager.getStorageModel() == BundleDbPersistenceManager.SM_LONGLONG_KEYS) {
            return 500;
        } else {
            return 1000;
        }
    }

    void perform(JCRSessionWrapper session) throws RepositoryException {
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
            return;
        }

        persistenceManager = (BundleDbPersistenceManager) pm;

        traverse(session);

        if (forceStop) {
            out.echo("Request received to stop checking nodes.");
        } else if (deleteOrphans && orphans.size() > 0) {
            delete(session);
        }
    }

    void stop() {
        this.forceStop = true;
    }

    private void traverse(JCRSessionWrapper session) throws RepositoryException {
        int checkOrphanedBatchSize = getCheckOrphanedBatchSize();
        Map<NodeId, NodeInfo> batch = getAllNodeInfos(null);
        while (!batch.isEmpty()) {
            NodeId lastId = null;
            for (NodeInfo info : batch.values()) {
                lastId = info.getId();
                if (NameConstants.NT_VERSIONHISTORY.equals(info.getNodeTypeName())) {
                    nodesToCheck.add(info.getId());
                    if (nodesToCheck.size() >= checkOrphanedBatchSize) {
                        checkOrphaned(session);
                    }
                }
                if (status.orphaned >= maxOrphans) {
                    break;
                }
                if (forceStop) {
                    return;
                }
            }
            batch = status.orphaned < maxOrphans ? getAllNodeInfos(lastId) : Collections.<NodeId, NodeInfo> emptyMap();
        }
        if (nodesToCheck.size() > 0) {
            checkOrphaned(session);
        }
        if (deleteOrphans && orphans.size() > 0) {
            delete(session);
        }
    }
}