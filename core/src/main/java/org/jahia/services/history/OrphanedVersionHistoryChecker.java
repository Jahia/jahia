/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for performing orphaned version history check and cleanup.
 * 
 * @author Sergiy Shyrkov
 */
class OrphanedVersionHistoryChecker {

    private static int loadVersionBundleBatchSize;

    private static final Logger logger = LoggerFactory.getLogger(OrphanedVersionHistoryChecker.class);

    private int checkOrphanedBatchSize;

    private final boolean deleteOrphans;

    private boolean forceStop;

    private final long maxOrphans;

    private final List<NodeId> nodesToCheck = new LinkedList<NodeId>();

    private final List<NodeId> orphans = new LinkedList<NodeId>();

    private final OutWrapper out;

    private BundleDbPersistenceManager persistenceManager;

    private final OrphanedVersionHistoryCheckStatus status;

    OrphanedVersionHistoryChecker(OrphanedVersionHistoryCheckStatus status, long maxOrphans, boolean deleteOrphans,
            OutWrapper out) {
        super();
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
        Map<NodeId, NodeInfo> batch = Collections.emptyMap();
        try {
            batch = persistenceManager.getAllNodeInfos(bigger, loadVersionBundleBatchSize);
        } catch (ItemStateException e) {
            logger.error(e.getMessage(), e);
        }
        return batch;
    }

    private void initBatchSizeLimits() {
        checkOrphanedBatchSize = Integer.getInteger("org.jahia.services.history.checkOrphanedBatchSize", 0);
        if (checkOrphanedBatchSize == 0) {
            if (persistenceManager instanceof DerbyPersistenceManager) {
                checkOrphanedBatchSize = 100;
            } else if (persistenceManager.getStorageModel() == BundleDbPersistenceManager.SM_LONGLONG_KEYS) {
                checkOrphanedBatchSize = 500;
            } else {
                checkOrphanedBatchSize = 1000;
            }
        }

        loadVersionBundleBatchSize = Integer.getInteger("org.jahia.services.history.loadVersionBundleBatchSize", 8000);
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

        initBatchSizeLimits();

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