/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.history;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.NodeImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.WorkspaceImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.version.InternalVersionHistory;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.spi.Name;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

/**
 * Version history utility class for purging all version entries of the specified node.
 * 
 * @author Sergiy Shyrkov
 */
public final class NodeVersionHistoryHelper {

    public static class OrhpanedVersionHistoryCheckStatus extends VersionHistoryCheckStatus {

        long limit;

        long orphaned;

        public long getLimit() {
            return limit;
        }

        public long getOrphaned() {
            return orphaned;
        }

        @Override
        public String toString() {
            return MessageFormatter.arrayFormat("{} version histories checked. {} orphans found. "
                    + "{} version items deleted. {} complete version histories deleted.",
                    new Long[] { checked, orphaned, deletedVersionItems, deleted });
        }
    }

    static class OutWrapper {
        private Logger log;
        private Writer out;

        OutWrapper(Logger logger, Writer out) {
            this.log = logger;
            this.out = out;
        }

        public OutWrapper echo(String message) {
            log.info(message);
            out(message);
            return this;
        }

        public OutWrapper echo(String format, Object arg1) {
            return echo(MessageFormatter.format(format, arg1));
        }

        public OutWrapper echo(String format, Object arg1, Object arg2) {
            return echo(MessageFormatter.format(format, arg1, arg2));
        }

        public OutWrapper echo(String format, Object arg1, Object arg2, Object arg3) {
            return echo(MessageFormatter.arrayFormat(format, new Object[] { arg1, arg2, arg3 }));
        }

        public OutWrapper echo(String format, Object[] args) {
            return echo(MessageFormatter.format(format, args));
        }

        private void out(String message) {
            if (out != null) {
                try {
                    out.append(message).append("\n").flush();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public static class VersionHistoryCheckStatus {

        long checked;

        long deleted;

        long deletedVersionItems;

        public long getChecked() {
            return checked;
        }

        public long getDeleted() {
            return deleted;
        }

        public long getDeletedVersionItems() {
            return deletedVersionItems;
        }

        @Override
        public String toString() {
            return MessageFormatter.arrayFormat(
                    "{} version histories checked. {} version items deleted."
                            + " {} complete version histories deleted.", new Long[] { checked,
                            deletedVersionItems, deleted });
        }
    }

    private static boolean checkingOrphans;

    private static boolean forceStop;

    private static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryHelper.class);

    public static String checkOrphaned(Node vhNode, JCRSessionWrapper session)
            throws RepositoryException {
        String targetId = vhNode.hasProperty("jcr:versionableUuid") ? vhNode.getProperty(
                "jcr:versionableUuid").getString() : null;
        if (targetId != null && !nodeExists(targetId, session)) {
            NodeImpl realNode = (NodeImpl) ((JCRNodeWrapper) vhNode).getRealNode();

            if (!((WorkspaceImpl) realNode.getSession().getWorkspace()).getItemStateManager()
                    .hasNodeReferences(realNode.getNodeId())) {
                return targetId;
            }
        }

        return null;
    }

    public static long checkOrphaned(NodeIterator it, JCRSessionWrapper session, Set<String> ids)
            throws RepositoryException {
        long checkedCount = 0;
        while (it.hasNext()) {
            Node vhNode = it.nextNode();
            checkedCount++;
            String source = checkOrphaned(vhNode, session);
            if (source != null) {
                ids.add(source);
            }
        }
        return checkedCount;
    }

    public static synchronized OrhpanedVersionHistoryCheckStatus checkOrphaned(
            final String versionStorageStartPath, final long maxOrphans,
            final boolean deleteOrphans, final Writer statusOut) throws RepositoryException {
        if (checkingOrphans) {
            throw new IllegalStateException(
                    "The version history is currently beeing checked for orphans."
                            + " Cannot start the second process.");
        }
        checkingOrphans = true;
        long timer = System.currentTimeMillis();
        final OrhpanedVersionHistoryCheckStatus status = new OrhpanedVersionHistoryCheckStatus();
        final String startPath = StringUtils.defaultIfEmpty(versionStorageStartPath,
                "/jcr:system/jcr:versionStorage");

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} orphaned version history under {}", deleteOrphans ? "deleting"
                : "checking", startPath);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Long>() {
                final Set<String> orphans = new HashSet<String>();

                private void check(JCRNodeWrapper node, JCRSessionWrapper session)
                        throws RepositoryException {
                    for (NodeIterator ni = node.getNodes(); ni.hasNext();) {
                        JCRNodeWrapper child = (JCRNodeWrapper) ni.nextNode();
                        if (child.isNodeType("nt:versionHistory")) {
                            String nodeUuid = checkOrphaned(child, session);
                            if (nodeUuid != null) {
                                status.orphaned++;
                                if (deleteOrphans) {
                                    orphans.add(nodeUuid);
                                }
                            }
                            status.checked++;
                            if (status.checked % 1000 == 0) {
                                out.echo(status.toString());
                            }
                            if (status.orphaned >= maxOrphans) {
                                out.echo(
                                        "{} version histories checked and the limit of {}"
                                                + " orphaned version histories is reached. Stopping checks.",
                                        status.checked, maxOrphans);
                                break;
                            }
                            if (deleteOrphans && status.orphaned > 0 && orphans.size() >= 200) {
                                delete(session);
                            }
                        } else if (child.isNodeType("rep:versionStorage")) {
                            check(child, session);
                        }
                        if (status.orphaned >= maxOrphans) {
                            return;
                        }
                        if (forceStop) {
                            return;
                        }
                    }
                }

                private void delete(JCRSessionWrapper session) {
                    out.echo("Start deleting version history for {} nodes", orphans.size());
                    try {
                        long nb = purgeVersionHistoryForNodes(orphans, session, status);
                        status.deleted += nb;
                        out.echo("deleted {} version histories", nb);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        out.echo("Error deleting version histories. Cause: {}", e.getMessage());
                    } finally {
                        orphans.clear();
                    }
                }

                public Long doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    check(session.getNode(startPath), session);
                    if (forceStop) {
                        out.echo("Request received to stop checking nodes.");
                    } else if (deleteOrphans && orphans.size() > 0) {
                        delete(session);
                    }
                    out.echo(status.toString());

                    return status.checked;
                }
            });

        } finally {
            checkingOrphans = false;
            forceStop = false;
            out.echo("Done checking orphaned version history in {} ms. Status: {}",
                    (System.currentTimeMillis() - timer), status.toString());
        }

        return status;
    }

    public static void forceStop() {
        forceStop = true;
    }

    public static boolean isCheckingOrphans() {
        return checkingOrphans;
    }

    private static boolean nodeExists(String id, JCRSessionWrapper session)
            throws RepositoryException {
        try {
            session.getNodeByIdentifier(id);
            return true;
        } catch (ItemNotFoundException e) {
            return false;
        }
    }

    private static boolean purgeVersionHistoryForNode(String nodeIdentifier,
            JCRSessionWrapper sessionWrapper, VersionHistoryCheckStatus status)
            throws PathNotFoundException, RepositoryException {
        long timer = System.currentTimeMillis();
        SessionImpl session = (SessionImpl) sessionWrapper.getProviderSession(sessionWrapper
                .getNode("/").getProvider());
        InternalVersionManager vm = session.getInternalVersionManager();

        if (logger.isDebugEnabled()) {
            logger.debug("Start purging version history for node {}", nodeIdentifier);
        }
        InternalVersionHistory history = null;
        try {
            history = vm.getVersionHistoryOfNode(NodeId.valueOf(nodeIdentifier));
        } catch (ItemNotFoundException e) {
            // no history found
            return false;
        }

        Name[] versionNames = history.getVersionNames();

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} versions", versionNames.length);
        }

        boolean deleted = true;
        if (versionNames.length == 1 && versionNames[0].getLocalName().equals("rootVersion")) {
            if (status != null && (status instanceof OrhpanedVersionHistoryCheckStatus)) {
                vm.removeVersion(session, history, versionNames[0]);
                if (status != null) {
                    status.deletedVersionItems++;
                }
            } else {
                deleted = false;
            }
        } else {
            for (Name v : versionNames) {
                if (v.getLocalName().equals("rootVersion")) {
                    continue;
                }
                try {
                    vm.removeVersion(session, history, v);
                    if (status != null) {
                        status.deletedVersionItems++;
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("removed version {}", v.getLocalName());
                    }
                } catch (ReferentialIntegrityException e) {
                    deleted = false;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Skipping still referenced version {}", v.getLocalName());
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            if (deleted) {
                logger.debug("Purged version history for node {} in {} ms", nodeIdentifier,
                        String.valueOf(System.currentTimeMillis() - timer));
            } else {
                logger.debug("Version history for node {} was not deleted"
                        + " as the version items are still referenced", nodeIdentifier);
            }
        }

        return deleted;
    }

    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(NodeIterator nodes,
            Writer statusOut) throws RepositoryException {
        Set<String> ids = new HashSet<String>();
        for (; nodes.hasNext();) {
            ids.add(nodes.nextNode().getIdentifier());
        }

        return purgeVersionHistoryForNodes(ids, statusOut);
    }

    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(
            final Set<String> nodeIdentifiers) {
        return purgeVersionHistoryForNodes(nodeIdentifiers, null);

    }

    public static long purgeVersionHistoryForNodes(Set<String> nodeIdentifiers,
            JCRSessionWrapper session, VersionHistoryCheckStatus status) throws RepositoryException {
        long deleted = 0;
        for (String id : nodeIdentifiers) {
            deleted += purgeVersionHistoryForNode(id, session, status) ? 1 : 0;
            if (forceStop) {
                status.deleted += deleted;
                return deleted;
            }
        }
        status.deleted += deleted;
        return deleted;
    }

    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(
            final Set<String> nodeIdentifiers, Writer statusOut) {
        final OutWrapper out = new OutWrapper(logger, statusOut);
        out.echo("Start checking version history for {} nodes", nodeIdentifiers.size());
        final VersionHistoryCheckStatus status = new VersionHistoryCheckStatus();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    int deletedItems = 0;
                    for (String id : nodeIdentifiers) {
                        status.checked++;
                        long oldDeletedItems = status.deletedVersionItems;
                        status.deleted += purgeVersionHistoryForNode(id, session, status) ? 1 : 0;
                        deletedItems += (status.deletedVersionItems - oldDeletedItems); 
                        if (status.checked % 200 == 0 || deletedItems > 200) {
                            out.echo(status.toString());
                            deletedItems = 0;
                        }
                    }
                    return true;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        out.echo("Done checking version history for nodes. Version histrory status: {}",
                status.toString());

        return status;
    }

    private NodeVersionHistoryHelper() {
        super();
    }
}
