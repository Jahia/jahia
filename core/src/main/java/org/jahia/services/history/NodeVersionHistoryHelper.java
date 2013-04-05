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

import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.jcr.ItemNotFoundException;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.version.VersionException;

import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.id.NodeId;
import org.apache.jackrabbit.core.version.InternalVersionHistory;
import org.apache.jackrabbit.core.version.InternalVersionManager;
import org.apache.jackrabbit.core.version.InternalVersionManagerImpl;
import org.apache.jackrabbit.core.version.InternalXAVersionManager;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Version history utility class for purging all version entries of the specified node.
 * 
 * @author Sergiy Shyrkov
 */
public final class NodeVersionHistoryHelper {

    private static OrphanedVersionHistoryChecker checker;

<<<<<<< .working
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
                    new Long[] { checked, orphaned, deletedVersionItems, deleted }).getMessage();
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
            return echo(MessageFormatter.format(format, arg1).getMessage());
        }

        public OutWrapper echo(String format, Object arg1, Object arg2) {
            return echo(MessageFormatter.format(format, arg1, arg2).getMessage());
        }

        public OutWrapper echo(String format, Object arg1, Object arg2, Object arg3) {
            return echo(MessageFormatter.arrayFormat(format, new Object[] { arg1, arg2, arg3 }).getMessage());
        }

        public OutWrapper echo(String format, Object[] args) {
            return echo(MessageFormatter.format(format, args).getMessage());
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
                            deletedVersionItems, deleted }).getMessage();
        }
    }

=======
>>>>>>> .merge-right.r45342
    private static boolean checkingOrphans;

    static final Logger logger = LoggerFactory.getLogger(NodeVersionHistoryHelper.class);

    protected static final int PURGE_HISTORY_CHUNK = 100;

    public static synchronized OrhpanedVersionHistoryCheckStatus checkOrphaned(final String versionStorageStartPath,
            final long maxOrphans, final boolean deleteOrphans, final Writer statusOut) throws RepositoryException {
        if (checkingOrphans) {
            throw new IllegalStateException("The version history is currently beeing checked for orphans."
                    + " Cannot start the second process.");
        }
        checkingOrphans = true;
        long timer = System.currentTimeMillis();
        final OrhpanedVersionHistoryCheckStatus status = new OrhpanedVersionHistoryCheckStatus();

        final OutWrapper out = new OutWrapper(logger, statusOut);

        out.echo("Start {} orphaned version history", deleteOrphans ? "deleting" : "checking");

        checker = new OrphanedVersionHistoryChecker(status, maxOrphans, deleteOrphans, out);

        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    checker.perform(session);
                    return null;
                }
            });
        } finally {
            checkingOrphans = false;
            checker = null;
            out.echo("Done checking orphaned version history in {} ms. Status: {}",
                    (System.currentTimeMillis() - timer), status.toString());
        }

        return status;
    }

    public static void forceStop() {
        if (checker != null) {
            checker.stop();
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
            if (!(status instanceof OrhpanedVersionHistoryCheckStatus)) {
                status.checked += histories.size();
            }
            status.deleted += result[0];
            status.deletedVersionItems += result[1];
        }
    }

    public static boolean isCheckingOrphans() {
        return checkingOrphans;
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

    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(NodeIterator nodes, Writer statusOut)
            throws RepositoryException {
        long total = nodes.getSize();
        OutWrapper out = new OutWrapper(logger, statusOut);
        if (total > 0) {
            out.echo("Start checking version history for {} nodes", total);
        }
        Set<String> ids = new HashSet<String>();
        VersionHistoryCheckStatus status = null;
        if (nodes.getSize() <= PURGE_HISTORY_CHUNK) {
            for (; nodes.hasNext();) {
                ids.add(nodes.nextNode().getIdentifier());
            }
            status = purgeVersionHistoryForNodes(ids, out);
            out.echo(status.toString());
        } else {
            status = new VersionHistoryCheckStatus();
            for (; nodes.hasNext();) {
                ids.add(nodes.nextNode().getIdentifier());
                if (ids.size() >= PURGE_HISTORY_CHUNK) {
                    // purge a chunk
                    VersionHistoryCheckStatus result = purgeVersionHistoryForNodes(ids, out);
                    ids.clear();
                    status.checked += result.checked;
                    status.deleted += result.deleted;
                    status.deletedVersionItems += result.deletedVersionItems;
                    out.echo(status.toString());
                }
            }
            if (ids.size() > 0) {
                // purge the rest
                VersionHistoryCheckStatus result = purgeVersionHistoryForNodes(ids, out);
                ids.clear();
                status.checked += result.checked;
                status.deleted += result.deleted;
                status.deletedVersionItems += result.deletedVersionItems;
                out.echo(status.toString());
            }
        }

        return status;
    }

    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers) {
        return purgeVersionHistoryForNodes(nodeIdentifiers, (Writer) null);
    }

    static void purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers, JCRSessionWrapper session,
            VersionHistoryCheckStatus status) throws VersionException, RepositoryException {
        SessionImpl providerSession = (SessionImpl) session.getProviderSession(session.getNode("/").getProvider());
        InternalVersionManager vm = providerSession.getInternalVersionManager();

        List<InternalVersionHistory> histories = new LinkedList<InternalVersionHistory>();
        for (String id : nodeIdentifiers) {
            try {
                histories.add(vm.getVersionHistoryOfNode(NodeId.valueOf(id)));
            } catch (ItemNotFoundException e) {
                // no history found
            }
        }

        internalPurgeVersionHistories(histories, session, status);
    }

    public static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers,
            Writer statusOut) {
        final OutWrapper out = new OutWrapper(logger, statusOut);
        out.echo("Start checking version history for {} nodes", nodeIdentifiers.size());

        final VersionHistoryCheckStatus status = purgeVersionHistoryForNodes(nodeIdentifiers, out);
        
        out.echo("Done checking version history for nodes. Version histrory status: {}", status.toString());

        return status;
    }

    private static VersionHistoryCheckStatus purgeVersionHistoryForNodes(final Set<String> nodeIdentifiers,
            OutWrapper out) {
        final VersionHistoryCheckStatus status = new VersionHistoryCheckStatus();
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    purgeVersionHistoryForNodes(nodeIdentifiers, session, status);
                    return Boolean.TRUE;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return status;
    }

    private NodeVersionHistoryHelper() {
        super();
    }
}
