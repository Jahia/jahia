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

package org.jahia.services.content;

import static org.jahia.api.Constants.*;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import java.util.*;

/**
 * This is a Jahia service, which offers functionality to publish, unpublish or get publication info of JCR nodes
 *
 * @author toto
 */
public class JCRPublicationService extends JahiaService {
    private static transient Logger logger = LoggerFactory.getLogger(JCRPublicationService.class);
    private JCRSessionFactory sessionFactory;
    private static JCRPublicationService instance;
    private MetricsLoggingService loggingService;

    private JCRPublicationService() {
    }

    /**
     * Get the JCR session factory
     *
     * @return The JCR session factory
     */
    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Set the JCR session factory
     *
     * @param sessionFactory The JCR session factory
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Get the singleton instance of the JCRPublicationService
     *
     * @return the singleton instance of the JCRPublicationService
     */
    public static JCRPublicationService getInstance() {
        if (instance == null) {
            synchronized (JCRPublicationService.class) {
                if (instance == null) {
                    instance = new JCRPublicationService();
                }
            }
        }
        return instance;
    }

    public boolean hasIndependantPublication(JCRNodeWrapper node) throws RepositoryException {
        return node.isNodeType("jmix:publication"); // todo : do we want to add this as a configurable in admin ?
        // currently it has to be set in definitions files
    }

    public void lockForPublication(final List<String> publicationInfo, final String workspace,
                                   final String key) throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(true, getSessionFactory().getCurrentUserSession(workspace).getUser().getUsername(),
                        workspace, null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        for (String id : publicationInfo) {
                            doLock(id, session, key);
                        }
                        return null;
                    }
                });
    }

    private void doLock(String id, JCRSessionWrapper session, String key)
            throws RepositoryException {
        JCRNodeWrapper node = session.getNodeByUUID(id);
        if (node.isLockable()) {
            node.lockAndStoreToken("validation", " " + key + " ");
        }
    }

    public void unlockForPublication(final List<String> publicationInfo, final String workspace,
                                     final String key) throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecute(true, getSessionFactory().getCurrentUserSession(workspace).getUser().getUsername(),
                        workspace, null, new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        for (String id : publicationInfo) {
                            doUnlock(id, session, key);
                        }
                        return null;
                    }
                });
    }

    private void doUnlock(String id, JCRSessionWrapper session, String key)
            throws RepositoryException {
        try {
            JCRNodeWrapper node = session.getNodeByUUID(id);
            if (node.isLocked()) {
                try {
                    node.unlock("validation", " " + key + " ");
                } catch (LockException e) {
                }
            }
        } catch (ItemNotFoundException nfe) {
            // Item has been deleted, ignore
        }
    }

    /**
     * Publish a node sub-tree from default into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param uuid UUID of the node to publish
     * @throws javax.jcr.RepositoryException in case of error
     */
    public void publishByMainId(final String uuid) throws RepositoryException {
        publishByMainId(uuid, EDIT_WORKSPACE, LIVE_WORKSPACE, null, true, null);
    }

    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param uuid                 Uuid of the node to publish
     * @param sourceWorkspace      the source workspace of the publication
     * @param destinationWorkspace the destination workspace of the publication
     * @param languages            set of languages you wish to publish
     * @param allSubTree
     * @param comments             an optional List<String> of comments that will be used to log the publication in the metrics
     *                             service.
     * @throws javax.jcr.RepositoryException in case of error
     */
    public void publishByMainId(final String uuid, final String sourceWorkspace, final String destinationWorkspace,
                                final Set<String> languages, final boolean allSubTree, List<String> comments)
            throws RepositoryException {
        List<PublicationInfo> tree =
                getPublicationInfo(uuid, languages, true, true, allSubTree, sourceWorkspace, destinationWorkspace);
        publishByInfoList(tree, sourceWorkspace, destinationWorkspace, comments);
    }

    public void publishByInfoList(final List<PublicationInfo> publicationInfos, final String sourceWorkspace,
                                  final String destinationWorkspace, final List<String> comments) throws RepositoryException {
        publishByInfoList(publicationInfos, sourceWorkspace, destinationWorkspace, true, comments);
    }

    public void publishByInfoList(final List<PublicationInfo> publicationInfos, final String sourceWorkspace,
                                  final String destinationWorkspace, boolean checkPermissions, final List<String> comments) throws RepositoryException {
        LinkedHashSet<String> allIds = new LinkedHashSet<String>();

        for (PublicationInfo publicationInfo : publicationInfos) {
            allIds.addAll(publicationInfo.getAllUuids(false, false));
            for (PublicationInfo subtree : publicationInfo.getAllReferences()) {
                allIds.addAll(subtree.getAllUuids(false, false));
            }
        }
        publish(new ArrayList<String>(allIds), sourceWorkspace, destinationWorkspace, checkPermissions, comments);
    }

    public void publish(final List<String> uuids, final String sourceWorkspace,
                        final String destinationWorkspace, final List<String> comments) throws RepositoryException {
        publish(uuids, sourceWorkspace, destinationWorkspace, true, comments);
    }

    public void publish(final List<String> uuids, final String sourceWorkspace,
                        final String destinationWorkspace, boolean checkPermissions, final List<String> comments) throws RepositoryException {
        if (uuids.isEmpty())
            return;

        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        final String username = user != null ? user.getUsername() : null;

        final List<String> checkedUuids = new ArrayList<String>();
        if (checkPermissions) {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            for (String uuid : uuids) {
                if (session.getNodeByIdentifier(uuid).hasPermission("publish")) {
                    checkedUuids.add(uuid);
                }
            }
        } else {
            checkedUuids.addAll(uuids);
        }

        if (!checkedUuids.isEmpty()) {
            JCRTemplate.getInstance().doExecute(true, username, sourceWorkspace, null, new JCRCallback<Object>() {
                public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                    JCRTemplate.getInstance().doExecute(true, username, destinationWorkspace, new JCRCallback<Object>() {
                        public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                            publish(checkedUuids, sourceSession, destinationSession, comments);
                            return null;
                        }
                    });

                    return null;
                }
            });
        }
    }

    private void publish(final List<String> uuidsToPublish, JCRSessionWrapper sourceSession,
                         JCRSessionWrapper destinationSession, final List<String> comments)
            throws RepositoryException {
        final Calendar calendar = new GregorianCalendar();
//        uuids.add(publicationInfo.getRoot().getUuid());

        final String destinationWorkspace = destinationSession.getWorkspace().getName();

        List<JCRNodeWrapper> toPublish = new ArrayList<JCRNodeWrapper>();
        for (String uuid : uuidsToPublish) {
            JCRNodeWrapper node = sourceSession.getNodeByUUID(uuid);
            if (!node.isNodeType("jmix:nolive")) {
                toPublish.add(node);
            }
        }

        String userID = destinationSession.getUserID();
        if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
            userID = userID.substring(JahiaLoginModule.SYSTEM.length());
        }

        VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();
        VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
        if (destinationSession.getWorkspace().getName().equals(LIVE_WORKSPACE)) {
            for (JCRNodeWrapper jcrNodeWrapper : toPublish) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Publishing node {}", jcrNodeWrapper.getPath());
                }
                if (jcrNodeWrapper.isNodeType("jmix:lastPublished") && (!jcrNodeWrapper.hasProperty("j:published") ||
                        !jcrNodeWrapper.getProperty("j:published").getBoolean())) {
                    if (!sourceVersionManager.isCheckedOut(jcrNodeWrapper.getPath())) {
                        sourceVersionManager.checkout(jcrNodeWrapper.getPath());
                    }
                    jcrNodeWrapper.setProperty("j:published", Boolean.TRUE);
                    jcrNodeWrapper.setProperty("j:lastPublished", calendar);
                    jcrNodeWrapper.setProperty("j:lastPublishedBy", userID);

                    try {
                        JCRNodeWrapper destNode = destinationSession
                                .getNode(jcrNodeWrapper.getCorrespondingNodePath(destinationWorkspace));
                        if (!destinationVersionManager.isCheckedOut(destNode.getPath())) {
                            destinationVersionManager.checkout(destNode.getPath());
                        }
                        destNode.setProperty("j:published", Boolean.TRUE);
                    } catch (ItemNotFoundException e) {
                    }
                } else if (jcrNodeWrapper.isNodeType("jmix:lastPublished")) {
                    jcrNodeWrapper.setProperty("j:published", Boolean.TRUE);
                    jcrNodeWrapper.setProperty("j:lastPublished", calendar);
                    jcrNodeWrapper.setProperty("j:lastPublishedBy", userID);
                }
            }
            if (sourceSession.hasPendingChanges()) {
                sourceSession.save();
            }
            if (destinationSession.hasPendingChanges()) {
                destinationSession.save();
            }
        }

        Set<JCRNodeWrapper> toCheckpoint = new HashSet<JCRNodeWrapper>();

        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        try {
            List<String> toDelete = new ArrayList<String>();
            List<JCRNodeWrapper> toDeleteOnSource = new ArrayList<JCRNodeWrapper>();
            for (ListIterator<JCRNodeWrapper> lit = toPublish.listIterator(); lit.hasNext(); ) {
                JCRNodeWrapper nodeWrapper = lit.next();
                if (nodeWrapper.hasProperty("j:deletedChildren")) {
                    JCRPropertyWrapper property = nodeWrapper.getProperty("j:deletedChildren");
                    Value[] values = property.getValues();
                    for (Value value : values) {
                        toDelete.add(value.getString());
                    }
                    property.remove();
                    nodeWrapper.removeMixin("jmix:deletedChildren");
                }
                if (nodeWrapper.isNodeType(JAHIAMIX_MARKED_FOR_DELETION_ROOT)) {
                    nodeWrapper.unmarkForDeletion();

                    toDeleteOnSource.add(nodeWrapper);
                    toDelete.add(nodeWrapper.getIdentifier());

                    lit.remove();
                } else {
                    for (JCRNodeWrapper nodeToDelete : toDeleteOnSource) {
                        if (nodeWrapper.getPath().startsWith(nodeToDelete.getPath())) {
                            lit.remove();
                            break;
                        }
                    }
                }
            }

            for (JCRNodeWrapper nodeWrapper : toDeleteOnSource) {
                try {
                    addRemovedLabel(nodeWrapper, nodeWrapper.getSession().getWorkspace().getName() + "_removed_at_" + Constants.DATE_FORMAT.format(calendar.getTime()));
                    nodeWrapper.remove();
                } catch (InvalidItemStateException e) {
                    logger.warn("Already deleted : " + nodeWrapper.getPath());
                }
            }
            for (String s : toDelete) {
                try {
                    JCRNodeWrapper node = destinationSession.getNodeByIdentifier(s);
                    addRemovedLabel(node, node.getSession().getWorkspace().getName() + "_removed_at_" + Constants.DATE_FORMAT.format(calendar.getTime()));
                    node.remove();
                } catch (ItemNotFoundException e) {
                    logger.warn("Already deleted : " + s);
                } catch (InvalidItemStateException e) {
                    logger.warn("Already deleted : " + s);
                }
            }
            sourceSession.save();
            destinationSession.save();


            Set<String> allCloned = new HashSet<String>();
            for (JCRNodeWrapper sourceNode : toPublish) {
                try {
                    sourceNode.getCorrespondingNodePath(destinationWorkspace);
                } catch (ItemNotFoundException e) {
                    CloneResult cloneResult = ensureNodeInDestinationWorkspace(sourceNode, destinationSession, toCheckpoint);
                    allCloned.addAll(cloneResult.includedUuids);
                }
            }

            uuidsToPublish.removeAll(allCloned);
            for (String includedUuid : allCloned) {
                toPublish.remove(sourceSession.getNodeByIdentifier(includedUuid));
            }

            mergeToDestinationWorkspace(toPublish, uuidsToPublish, sourceSession, destinationSession,
                    calendar, toCheckpoint);

            for (JCRNodeWrapper nodeWrapper : toCheckpoint) {
                checkpoint(destinationSession, nodeWrapper, destinationVersionManager);
            }
        } finally {
            JCRObservationManager.setEventsDisabled(null);
        }

        boolean doLogging = loggingService.isEnabled();
        if (doLogging) {
            Integer operationType = JCRObservationManager.getCurrentOperationType();
            if (operationType != null && operationType == JCRObservationManager.IMPORT) {
                doLogging = false;
            }
        }
        if (doLogging) {
            // now let's output the publication information to the logging service.
            for (JCRNodeWrapper publishedNode : toPublish) {
                StringBuilder commentBuf = null;
                if (comments != null && comments.size() > 0) {
                    commentBuf = new StringBuilder();
                    for (String comment : comments) {
                        commentBuf.append(comment);
                    }
                }
                loggingService.logContentEvent(userID, "", "", publishedNode.getIdentifier(), publishedNode.getPath(),
                        publishedNode.getPrimaryNodeTypeName(), "publishedNode", sourceSession.getWorkspace().getName(),
                        destinationSession.getWorkspace().getName(), commentBuf != null ? commentBuf.toString() : "");
            }
        }
    }

    private CloneResult ensureNodeInDestinationWorkspace(final JCRNodeWrapper node,
                                                         JCRSessionWrapper destinationSession, final Set<JCRNodeWrapper> toCheckpoint) throws AccessDeniedException,
            NoSuchWorkspaceException, RepositoryException {
        if (!destinationSession.isSystem()) {
            final String nodePath = node.getPath();
            final String destinationWorkspace = destinationSession.getWorkspace().getName();
            return JCRTemplate.getInstance().doExecute(true, node.getUser().getUsername(),
                    node.getSession().getWorkspace().getName(), null, new JCRCallback<CloneResult>() {
                public CloneResult doInJCR(final JCRSessionWrapper sourceSession)
                        throws RepositoryException {
                    return JCRTemplate.getInstance().doExecute(true,
                            node.getUser().getUsername(), destinationWorkspace,
                            new JCRCallback<CloneResult>() {
                                public CloneResult doInJCR(
                                        final JCRSessionWrapper destinationSession)
                                        throws RepositoryException {
                                    CloneResult cloneResult = cloneParents(sourceSession.getNode(nodePath),
                                            sourceSession, destinationSession, toCheckpoint);
                                    sourceSession.save();
                                    destinationSession.save();
                                    return cloneResult;
                                }
                            });
                }
            });
        } else {
            return cloneParents(node, node.getSession(), destinationSession, toCheckpoint);
        }
    }

    private CloneResult cloneParents(JCRNodeWrapper node, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession, Set<JCRNodeWrapper> toCheckpoint) throws RepositoryException {
        CloneResult cloneResult = null;
        JCRNodeWrapper parent = node.getParent();
        try {
            parent.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        } catch (ItemNotFoundException e) {
            cloneResult = cloneParents(parent, sourceSession, destinationSession, toCheckpoint);

            try {
                // Check if node still does not exist in target space - if it has been cloned with parent, return
                node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                return cloneResult;
            } catch (ItemNotFoundException ee) {
            }
        }
        CloneResult subCloneResult = doClone(node, sourceSession, destinationSession, toCheckpoint);
        if (cloneResult != null) {
            subCloneResult.includedUuids.addAll(cloneResult.includedUuids);
        }
        return subCloneResult;
    }

    private void mergeToDestinationWorkspace(final List<JCRNodeWrapper> toPublish, final List<String> uuids,
                                             final JCRSessionWrapper sourceSession,
                                             final JCRSessionWrapper destinationSession, Calendar calendar, Set<JCRNodeWrapper> toCheckpoint)
            throws RepositoryException {
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();
        final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

        if (toPublish.isEmpty()) {
            return;
        }

        for (final JCRNodeWrapper node : toPublish) {
            if (!node.isNodeType("mix:versionable")) {
                ConflictResolver conflictResolver = new ConflictResolver(node, destinationSession.getNode(node.getCorrespondingNodePath(destinationSession.getWorkspace().getName())));
                conflictResolver.setUuids(uuids);
                conflictResolver.setToCheckpoint(toCheckpoint);
                conflictResolver.applyDifferences();
                continue;
            }

            final String path = node.getPath();
            String destinationPath =
                    node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());

            // Item exists at "destinationPath" in live space, update it

            JCRNodeWrapper destinationNode = destinationSession
                    .getNode(destinationPath); // Live node exists - merge live node from source space

            if (!destinationVersionManager.isCheckedOut(destinationNode.getPath())) {
                destinationVersionManager.checkout(destinationNode.getPath());
            }

            final String oldPath = handleSharedMove(sourceSession, node, node.getPath());

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Merge node : " + path + " source v=" + node.getBaseVersion().getName() + " , dest node v=" +
                                destinationNode.getBaseVersion().getName());
            }

            if (!node.getPath().equals(destinationPath)) {
                try {
                    destinationVersionManager
                            .checkout(StringUtils.substringBeforeLast(destinationPath, "/")); // previous parent
                    JCRNodeWrapper nodeParent = node.getParent();
                    String newParentPath = null;
                    try {
                        newParentPath = nodeParent.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                    } catch (ItemNotFoundException e) {
                        ensureNodeInDestinationWorkspace(nodeParent, destinationSession, toCheckpoint);
                        newParentPath = nodeParent.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                    }
                    destinationVersionManager.checkout(newParentPath); // new parent
                    recurseCheckout(destinationNode, null, destinationVersionManager); // node and sub nodes

                    String newDestinationPath = newParentPath + "/" + node.getName();
                    destinationSession.move(destinationPath, newDestinationPath);
                    destinationSession.save();

                    destinationPath = newDestinationPath;
                    destinationNode = destinationSession.getNode(destinationPath);

                    JCRNodeWrapper destinationParent = destinationSession.getNode(newParentPath);
                    if (destinationParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                        NodeIterator ni = node.getParent().getNodes();
                        boolean found = false;
                        while (ni.hasNext()) {
                            JCRNodeWrapper currentNode = (JCRNodeWrapper) ni.next();
                            if (!found && currentNode.getIdentifier().equals(node.getIdentifier())) {
                                found = true;
                            } else if (found) {
                                try {
                                    destinationSession.getNode(newParentPath + "/" + currentNode.getName());
                                    destinationParent.orderBefore(node.getName(), currentNode.getName());
                                    destinationParent.getSession().save();
                                    break;
                                } catch (PathNotFoundException e1) {

                                }
                            }
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            destinationSession.save();

            checkpoint(sourceSession, node, sourceVersionManager);
            ConflictResolver resolver = new ConflictResolver(node, destinationNode);
            resolver.setUuids(uuids);
            resolver.setToCheckpoint(toCheckpoint);
            try {
                resolver.applyDifferences();

                if (!resolver.getUnresolvedDifferences().isEmpty()) {
                    logger.warn("Unresolved conflicts : " + resolver.getUnresolvedDifferences());
                }
            } catch (RepositoryException e) {
                logger.error("Error when merging differences", e);
            }

            ((JCRWorkspaceWrapper.VersionManagerWrapper) destinationVersionManager).addPredecessor(destinationPath, sourceVersionManager.getBaseVersion(path));
            toCheckpoint.add(destinationNode);

            if (oldPath != null) {
                try {
                    JCRNodeWrapper snode = destinationSession.getNode(oldPath);
                    recurseCheckout(snode, null, destinationVersionManager);
                    JCRNodeWrapper oldParent = snode.getParent();
                    oldParent.checkout();
                    snode.remove();
                    snode.getSession().save();
                } catch (PathNotFoundException e) {
                    // already removed
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Merge node end : " + path + " source v=" +
                        sourceSession.getNode(path).getBaseVersion().getName() + " , dest node v=" +
                        destinationSession.getNode(destinationPath).getBaseVersion().getName());
            }
        }
    }

    class CloneResult {
        JCRNodeWrapper root;
        Set<String> includedUuids;
    }

    CloneResult doClone(JCRNodeWrapper sourceNode, JCRSessionWrapper sourceSession,
                        JCRSessionWrapper destinationSession, Set<JCRNodeWrapper> toCheckpoint) throws RepositoryException {
        CloneResult cloneResult = new CloneResult();
        cloneResult.includedUuids = new HashSet<String>();

        JCRNodeWrapper parent = sourceNode.getParent();
//                destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
        final String sourceNodePath =
                sourceNode.getIndex() > 1 ? sourceNode.getPath() + "[" + sourceNode.getIndex() + "]" :
                        sourceNode.getPath();
        if (logger.isDebugEnabled()) {
            logger.debug("Cloning node : " + sourceNodePath + " parent path " + parent.getPath());
        }
        final String destinationWorkspaceName = destinationSession.getWorkspace().getName();
        String destinationParentPath = null;
        try {
            destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
        } catch (ItemNotFoundException e) {
            CloneResult parentCloneResult = cloneParents(sourceNode.getParent(), sourceSession, destinationSession, toCheckpoint);
            cloneResult.includedUuids.addAll(parentCloneResult.includedUuids);
            destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
        }

        JCRNodeWrapper destinationParent = destinationSession.getNode(destinationParentPath);
        if (destinationParent.hasNode(sourceNode.getName())) {
            logger.error("Node " + sourceNode.getName() + " already exist under " + destinationParent.getPath() +
                    " - live node is going to be removed !");
            destinationParent.checkout();
            destinationParent.getNode(sourceNode.getName()).remove();
            destinationSession.save();
        }

        final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

        try {
            Set<String> deniedPaths = new HashSet<String>();
            Set<String> included = new HashSet<String>();
            getDeniedPath(sourceNode, deniedPaths, included);
            cloneResult.includedUuids.addAll(included);
            JahiaAccessManager.setDeniedPaths(deniedPaths);

            if (!destinationVersionManager.isCheckedOut(destinationParentPath)) {
                destinationVersionManager.checkout(destinationParentPath);
            }
            String destinationPath;
            if (destinationParentPath.equals("/")) {
                destinationPath = "/" + sourceNode.getName();
            } else {
                destinationPath = destinationParentPath + "/" + sourceNode.getName();
            }

            try {
                String correspondingNodePath = sourceNode.getCorrespondingNodePath(destinationWorkspaceName);
                logger.warn("Cloning existing node " + sourceNode.getPath());
                // Node has been moved
                destinationVersionManager
                        .checkout(StringUtils.substringBeforeLast(correspondingNodePath, "/")); // previous parent
                destinationVersionManager.checkout(destinationParentPath); // new parent
                recurseCheckout(destinationSession.getNode(correspondingNodePath), null,
                        destinationVersionManager); // node and sub nodes

                destinationSession.move(correspondingNodePath, destinationPath);
                destinationSession.save();
            } catch (ItemNotFoundException e) {
                // Always checkpoint before first clone
                for (String s : included) {
                    JCRNodeWrapper n = sourceSession.getNodeByIdentifier(s);
                    if (n.isNodeType("mix:versionable")) {
                        checkpoint(sourceSession, n, sourceSession.getWorkspace().getVersionManager());
                    }
                }
                destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNodePath, destinationPath, false);
                for (String s : included) {
                    JCRNodeWrapper n = destinationSession.getNodeByIdentifier(s);
                    if (n.isNodeType("mix:versionable")) {
                        toCheckpoint.add(n);
                    }
                }
                JCRNodeWrapper n = destinationSession.getNode(sourceNode.getCorrespondingNodePath(destinationWorkspaceName));
                try {
                    if (n.getParent().isNodeType("mix:versionable")) {
                        toCheckpoint.add(n.getParent());
                    }
                } catch (ItemNotFoundException e1) {
                }
            }
            if (destinationParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                NodeIterator ni = sourceNode.getParent().getNodes();
                boolean found = false;
                while (ni.hasNext()) {
                    JCRNodeWrapper currentNode = (JCRNodeWrapper) ni.next();
                    if (!found && currentNode.getIdentifier().equals(sourceNode.getIdentifier())) {
                        found = true;
                    } else if (found) {
                        try {
                            destinationSession.getNode((destinationParentPath.equals("/") ? "" : destinationParentPath) + "/" + currentNode.getName());
                            destinationParent.orderBefore(sourceNode.getName(), currentNode.getName());
                            destinationParent.getSession().save();
                            break;
                        } catch (PathNotFoundException e1) {

                        }
                    }
                }
            }
        } finally {
            JahiaAccessManager.setDeniedPaths(null);
        }
        JCRNodeWrapper destinationNode = null;
        try {
            cloneResult.root = destinationSession.getNode(sourceNode.getCorrespondingNodePath(destinationWorkspaceName));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return cloneResult;
    }

    private void getDeniedPath(JCRNodeWrapper sourceNode, Set<String> deniedPaths, Set<String> includedUuids) throws RepositoryException {
        includedUuids.add(sourceNode.getIdentifier());
        NodeIterator it = sourceNode.getNodes();
        while (it.hasNext()) {
            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) it.next();
            if (nodeWrapper.isVersioned() || nodeWrapper.isNodeType("jmix:nolive")) {
                deniedPaths.add(nodeWrapper.getPath());
            } else {
                getDeniedPath(nodeWrapper, deniedPaths, includedUuids);
            }
        }
    }

    private String handleSharedMove(JCRSessionWrapper sourceSession, JCRNodeWrapper sourceNode, String destinationPath)
            throws RepositoryException {
        String oldPath = null;
        if (sourceNode.hasProperty("j:movedFrom")) {
            Property movedFrom = sourceNode.getProperty("j:movedFrom");
            List<Value> values = new ArrayList<Value>(Arrays.asList(movedFrom.getValues()));
            for (Value value : values) {
                String v = value.getString();
                if (v.endsWith(":::" + destinationPath)) {
                    oldPath = StringUtils.substringBefore(v, ":::");
                    values.remove(value);
                    break;
                }
            }
            if (oldPath != null) {
                sourceNode.checkout();
                if (values.isEmpty()) {
                    movedFrom.remove();
                } else {
                    movedFrom.setValue(values.toArray(new Value[values.size()]));
                }
                sourceSession.save();
            }
        }
        return oldPath;
    }

    private void checkpoint(Session session, JCRNodeWrapper node, VersionManager versionManager)
            throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Checkin node " + node.getPath() + " in workspace " + session.getWorkspace().getName() +
                    " with current version " + versionManager.getBaseVersion(node.getPath()).getName());
        }
        if (node.isNodeType(JAHIAMIX_NODENAMEINFO)) {
            boolean doUpdate = true;
            String nodePath = node.getPath();
            if (node.hasProperty(FULLPATH)) {
                Value fp = node.getProperty(FULLPATH).getValue();
                doUpdate = fp == null || !StringUtils.equals(fp.getString(), nodePath);
            }
            if (doUpdate) {
                node.setProperty(FULLPATH, node.getPath());
            }
        }
        session.save();
        Version version = versionManager.checkpoint(node.getPath());
        if (logger.isDebugEnabled()) {
            logger.debug("Checkin node " + node.getPath() + " in workspace " + session.getWorkspace().getName() +
                    " with new version " + version.getName() + " base version is " +
                    versionManager.getBaseVersion(node.getPath()).getName());
        }
    }

    private void recurseCheckout(Node node, List<String> prune, VersionManager versionManager)
            throws RepositoryException {
        if (!versionManager.isCheckedOut(node.getPath())) {
            versionManager.checkout(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            if (prune == null || !prune.contains(sub.getIdentifier())) {
                recurseCheckout(sub, prune, versionManager);
            }
        }
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param uuids     uuids of the node to unpublish
     * @param languages
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(final List<String> uuids, final Set<String> languages) throws RepositoryException {
        unpublish(uuids, languages, true);
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param uuids     uuids of the node to unpublish
     * @param languages
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(final List<String> uuids, final Set<String> languages, boolean checkPermissions) throws RepositoryException {
        final String username;
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        } else {
            username = null;
        }

        final List<String> checkedUuids = new ArrayList<String>();
        if (checkPermissions) {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            for (String uuid : uuids) {
                if (session.getNodeByIdentifier(uuid).hasPermission("publish")) {
                    checkedUuids.add(uuid);
                }
            }
        } else {
            checkedUuids.addAll(uuids);
        }

        final Set<String> ignoredNodes = new HashSet<String>();

        JCRTemplate.getInstance().doExecute(true, username, EDIT_WORKSPACE, null, new JCRCallback<Object>() {
            public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                Set<String> translationLanguages = new HashSet<String>();
                boolean first = true;
                VersionManager vm = sourceSession.getWorkspace().getVersionManager();
                List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>(checkedUuids.size());
                for (String uuid : checkedUuids) {
                    try {
                        JCRNodeWrapper node = sourceSession.getNodeByIdentifier(uuid);
                        if (first && !node.isNodeType(Constants.JAHIAMIX_PUBLICATION)) {
                            if (!vm.isCheckedOut(node.getPath())) {
                                vm.checkout(node.getPath());
                            }
                            node.addMixin(Constants.JAHIAMIX_PUBLICATION);
                            first = false;
                        }
                        if (!node.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                            nodes.add(node);

                            if (languages != null) {
                                NodeIterator ni = node.getNodes("j:translation*");
                                if (!ni.hasNext()) {
                                    ignoredNodes.add(node.getPath());
                                }
                                while (ni.hasNext()) {
                                    JCRNodeWrapper i18n = (JCRNodeWrapper) ni.next();
                                    if (i18n.hasProperty("j:published") && i18n.getProperty("j:published").getBoolean()) {
                                        translationLanguages.add(i18n.getProperty("jcr:language").getString());
                                    }
                                }
                            }
                        }
                    } catch (ItemNotFoundException e) {
                        if (logger.isInfoEnabled()) {
                            logger.info("Node {} does not exist in the default workspace any longer." +
                                    " Skipping unpublishing it.", uuid);
                        }
                    }
                }
                // if not all languages are unpublished now, then also do not unpublish the
                // nodes having no translations
                if (languages != null) {
                    if (languages.containsAll(translationLanguages)) {
                        ignoredNodes.clear();
                    } else {
                        for (Iterator<JCRNodeWrapper> it = nodes.iterator(); it.hasNext(); ) {
                            if (ignoredNodes.contains(it.next().getPath())) {
                                it.remove();
                            }
                        }
                    }
                }
                for (ListIterator<JCRNodeWrapper> it = nodes.listIterator(nodes.size()); it.hasPrevious(); ) {
                    JCRNodeWrapper node = it.previous();
                    unpublish(node, languages, ignoredNodes);
                    sourceSession.save();
                }
                return null;
            }
        });

        JCRTemplate.getInstance().doExecute(true, username, LIVE_WORKSPACE, new JCRCallback<Object>() {
            public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                for (ListIterator<String> it = uuids.listIterator(uuids.size()); it.hasPrevious(); ) {
                    String uuid = it.previous();
                    JCRNodeWrapper destNode = destinationSession.getNodeByIdentifier(uuid);
                    if (!destNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                        unpublish(destNode, languages, ignoredNodes);
                    }
                    destinationSession.save();
                }
                return null;
            }
        });

    }

    private void unpublish(JCRNodeWrapper node, Set<String> languages, Set<String> nodesNotUnpublished) throws RepositoryException {
        boolean allLanguagesUnpublished = true;
        NodeIterator ni = node.getNodes("j:translation*");
        // if there are nodes not unpublished then we have to keep the ones without translations
        if (!nodesNotUnpublished.isEmpty() && !ni.hasNext()) {
            allLanguagesUnpublished = false;
        }
        while (ni.hasNext()) {
            JCRNodeWrapper i18n = (JCRNodeWrapper) ni.next();
            if (languages == null || languages.contains(i18n.getProperty("jcr:language").getString())) {
                if (!i18n.isCheckedOut()) {
                    i18n.checkout();
                }
                i18n.setProperty("j:published", false);
            } else if (i18n.hasProperty("j:published") && i18n.getProperty("j:published").getBoolean()) {
                allLanguagesUnpublished = false;
            }
        }
        if (allLanguagesUnpublished) {
            boolean allChildrenUnpublished = true;
            for (String nodeNotUnpublished : nodesNotUnpublished) {
                if (nodeNotUnpublished.startsWith(node.getPath())) {
                    allChildrenUnpublished = false;
                    break;
                }
            }
            if (allChildrenUnpublished) {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }
                node.setProperty("j:published", false);
            } else {
                nodesNotUnpublished.add(node.getPath());
            }
        } else {
            nodesNotUnpublished.add(node.getPath());
        }
        boolean doLogging = loggingService.isEnabled();
        if (doLogging) {
            Integer operationType = JCRObservationManager.getCurrentOperationType();
            if (operationType != null && operationType == JCRObservationManager.IMPORT) {
                doLogging = false;
            }
        }
        if (doLogging) {
            String userID = node.getSession().getUserID();
            if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
                userID = userID.substring(JahiaLoginModule.SYSTEM.length());
            }
            loggingService
                    .logContentEvent(userID, "", "", node.getIdentifier(), node.getPath(), node.getPrimaryNodeTypeName(),
                            "unpublishedNode", node.getSession().getWorkspace().getName());
        }
    }

    public List<PublicationInfo> getPublicationInfos(List<String> uuids, Set<String> languages,
                                                     boolean includesReferences, boolean includesSubnodes,
                                                     boolean allsubtree, final String sourceWorkspace,
                                                     final String destinationWorkspace, boolean checkForUnpublication) throws RepositoryException {
        List<PublicationInfo> infos = new ArrayList<PublicationInfo>();

        List<String> allUuids = new ArrayList<String>();

        for (String uuid : uuids) {
            if (!allUuids.contains(uuid)) {
                final List<PublicationInfo> publicationInfos =
                        getPublicationInfo(uuid, languages, includesReferences, includesSubnodes, allsubtree,
                                sourceWorkspace, destinationWorkspace);
                for (PublicationInfo publicationInfo : publicationInfos) {
                    if (publicationInfo.needPublication(null)) {
                        infos.add(publicationInfo);
                        allUuids.addAll(publicationInfo.getAllUuids());
                    } else if (checkForUnpublication && publicationInfo.isUnpublicationPossible(null)) {
                        infos.add(publicationInfo);
                        allUuids.addAll(publicationInfo.getAllUuids());
                    }
                }
            }
        }
        for (PublicationInfo info : infos) {
            info.clearInternalAndPublishedReferences(uuids);
        }
        return infos;
    }

    /**
     * Gets the publication info for the current node and if acquired also for referenced nodes and subnodes.
     * The returned <code>PublicationInfo</code> has the publication info for the current node (NOT_PUBLISHED, PUBLISHED, MODIFIED, UNPUBLISHABLE)
     * and if requested you will be able to get the infos also for the subnodes and the referenced nodes.
     * As language dependent data is always stored in subnodes you need to set includesSubnodes to true, if you also specify a list of languages.
     *
     * @param uuid               The uuid of the node to get publication info
     * @param languages          Languages list to use for publication info, or null for all languages (only appplied if includesSubnodes is true)
     * @param includesReferences If true include info for referenced nodes
     * @param includesSubnodes   If true include info for subnodes
     * @return the <code>PublicationInfo</code> for the requested node(s)
     * @throws RepositoryException
     */
    public List<PublicationInfo> getPublicationInfo(String uuid, Set<String> languages, boolean includesReferences,
                                                    boolean includesSubnodes, boolean allsubtree,
                                                    final String sourceWorkspace, final String destinationWorkspace)
            throws RepositoryException {
        final JCRSessionWrapper sourceSession = sessionFactory.getCurrentUserSession(sourceWorkspace);
        final JCRSessionWrapper destinationSession = sessionFactory.getCurrentUserSession(destinationWorkspace);
        return getPublicationInfo(uuid, languages, includesReferences, includesSubnodes, allsubtree, sourceSession, destinationSession);
    }

    public List<PublicationInfo> getPublicationInfo(String uuid, Set<String> languages, boolean includesReferences,
                                                    boolean includesSubnodes, boolean allsubtree,
                                                    final JCRSessionWrapper sourceSession, final JCRSessionWrapper destinationSession)
            throws RepositoryException {
        JCRNodeWrapper stageNode;
        try {
            stageNode = sourceSession.getNodeByUUID(uuid);
        } catch (ItemNotFoundException e) {
            logger.warn("ItemNotFoundException for {} in workspace {}", uuid, sourceSession.getWorkspace().getName());
            throw e;
        }
        List<PublicationInfo> infos = new ArrayList<PublicationInfo>();
        PublicationInfo tree = new PublicationInfo();
        infos.add(tree);
        PublicationInfoNode root =
                getPublicationInfo(stageNode, languages, includesReferences, includesSubnodes, allsubtree,
                        sourceSession, destinationSession, new HashMap<String, PublicationInfoNode>(), infos);
        tree.setRoot(root);
        return infos;
    }


    /**
     * Gets the publication info for the current node and if acquired also for referenced nodes and subnodes.
     * The returned <code>PublicationInfo</code> has the publication info for the current node (NOT_PUBLISHED, PUBLISHED, MODIFIED, UNPUBLISHABLE)
     * and if requested you will be able to get the infos also for the subnodes and the referenced nodes.
     * As language dependent data is always stored in subnodes you need to set includesSubnodes to true, if you also specify a list of languages.
     *
     * @param languages          Languages list to use for publication info, or null for all languages (only appplied if includesSubnodes is true)
     * @param includesReferences If true include info for referenced nodes
     * @param includesSubnodes   If true include info for subnodes
     * @param sourceSession
     * @param destinationSession
     * @param infosMap           a Set of uuids, which don't need to be checked or have already been checked
     * @return the <code>PublicationInfo</code> for the requested node(s)
     * @throws RepositoryException
     */
    private PublicationInfoNode getPublicationInfo(JCRNodeWrapper node, Set<String> languages,
                                                   boolean includesReferences, boolean includesSubnodes,
                                                   boolean allsubtree, final JCRSessionWrapper sourceSession,
                                                   final JCRSessionWrapper destinationSession,
                                                   Map<String, PublicationInfoNode> infosMap,
                                                   List<PublicationInfo> infos) throws RepositoryException {

        PublicationInfoNode info = null;
        final String uuid = node.getIdentifier();
        info = infosMap.get(uuid);
        if (info != null && (!allsubtree || info.isSubtreeProcessed())) {
            return info;
        }

        if (info != null && uuid == null) {
            info.setStatus(PublicationInfo.PUBLISHED);
            return info;
        }

        if (info == null) {
            info = new PublicationInfoNode(node.getIdentifier(), node.getPath());

            if (node.isNodeType("jmix:nolive")) {
                info.setStatus(PublicationInfo.PUBLISHED);
                return info;
            }

            info.setSubtreeProcessed(allsubtree);
            infosMap.put(uuid, info);

            if (node.hasProperty("j:deletedChildren")) {
                try {
                    JCRPropertyWrapper p = node.getProperty("j:deletedChildren");
                    Value[] values = p.getValues();
                    for (Value value : values) {
                        try {
                            JCRNodeWrapper deletedNode = destinationSession.getNodeByUUID(value.getString());
                            PublicationInfoNode deletedInfo = new PublicationInfoNode(deletedNode.getIdentifier(), deletedNode.getPath());
                            deletedInfo.setStatus(PublicationInfo.DELETED);
                            info.addChild(deletedInfo);
                        } catch (ItemNotFoundException e) {
                            if (logger.isDebugEnabled())  {
                                logger.debug("Cannot find deleted subnode of "+node.getPath() + " : " + value.getString()+", we keep the reference until next publication to be sure to erase it from the live workspace.");
                            }
                        }
                    }
                }catch (PathNotFoundException e) {
                    logger.warn("property j:deletedChildren has been found on node " + node.getPath() + " but was not here");
                }
            }

            info.setStatus(getStatus(node, destinationSession, languages));
            if (info.getStatus() == PublicationInfo.CONFLICT) {
                return info;
            }
            if (node.hasProperty(JAHIA_LOCKTYPES)) {
                Value[] lockTypes = node.getProperty(JAHIA_LOCKTYPES).getValues();
                for (Value lockType : lockTypes) {
                    if (lockType.getString().endsWith(":validation")) {
                        info.setLocked(true);
                    }
                }
            }
        }

        if (includesReferences || includesSubnodes) {
            if (includesReferences) {
                getReferences(node, languages, includesReferences, includesSubnodes, sourceSession, destinationSession,
                        infosMap, infos, info);
            }
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();

                Value descriptorValue = sourceSession.getProviderSession(n.getProvider()).getRepository().getDescriptorValue(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED);
                if (descriptorValue == null) {
                    continue;
                }
                boolean supportsPublication = descriptorValue.getBoolean();
                if (!supportsPublication) {
                    continue;
                }

                if (languages != null && n.isNodeType("jnt:translation")) {
                    String translationLanguage = n.getProperty("jcr:language").getString();
                    if (languages.contains(translationLanguage)) {
                        PublicationInfoNode child =
                                getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                                        sourceSession, destinationSession, infosMap, infos);
                        info.addChild(child);
                    }
                } else {
                    boolean hasIndependantPublication = hasIndependantPublication(n);
                    if (allsubtree && hasIndependantPublication) {
                        PublicationInfo newinfo = new PublicationInfo();
                        infos.add(newinfo);
                        newinfo.setRoot(getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree, sourceSession,
                                destinationSession, infosMap, infos));
                    }
                    if (!hasIndependantPublication) {
                        if (n.isNodeType("jmix:lastPublished")) {
                            PublicationInfoNode child = getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                                    sourceSession, destinationSession, infosMap, infos);
                            info.addChild(child);
                        } else {
                            getReferences(n, languages, includesReferences, includesSubnodes, sourceSession, destinationSession, infosMap,
                                    infos, info);
                        }
                    }
                }
            }
        }
        return info;
    }

    public int getStatus(JCRNodeWrapper node, JCRSessionWrapper destinationSession, Set<String> languages) throws RepositoryException {
        int status;
        JCRNodeWrapper publishedNode = null;
        try {
            publishedNode = destinationSession.getNodeByUUID(node.getIdentifier());
        } catch (ItemNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("No live node for staging node " + node.getPath());
            }
        }

        if (!node.checkLanguageValidity(languages)) {
            status = PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE;
            for (String language : languages) {
                Locale locale = LanguageCodeConverters.getLocaleFromCode(language);
                if (node.checkI18nAndMandatoryPropertiesForLocale(locale)) {
                    status = PublicationInfo.MANDATORY_LANGUAGE_VALID;
                }
            }
        } else if (node.hasProperty("j:published") && !node.getProperty("j:published").getBoolean()) {
            status = PublicationInfo.UNPUBLISHED;
        } else if (publishedNode == null) {
            try {
                try {
                    destinationSession.getNodeByUUID(node.getParent().getUUID()).getNode(node.getName());
                } catch (UnsupportedRepositoryOperationException e) {
                }
                // Conflict , a node exists in live !
                status = PublicationInfo.CONFLICT;
                return status;
            } catch (ItemNotFoundException e) {
            } catch (PathNotFoundException e) {
            }

            status = PublicationInfo.NOT_PUBLISHED;
            if (node.isNodeType(JAHIANT_TRANSLATION)) {
                boolean hasProperty = false;
                final PropertyIterator iterator = node.getProperties();
                while (iterator.hasNext() && !hasProperty) {
                    Property property = (Property) iterator.next();
                    hasProperty = ((ExtendedPropertyDefinition) property.getDefinition()).isInternationalized();
                }
                if (!hasProperty) {
                    status = PublicationInfo.PUBLISHED;
                }
            }
        } else {
            if (node.hasProperty("jcr:mergeFailed") || publishedNode.hasProperty("jcr:mergeFailed")) {
                status = PublicationInfo.CONFLICT;
            } else if (node.getLastModifiedAsDate() == null) {
                // No modification date - node is published
                status = PublicationInfo.PUBLISHED;
            } else {
                Date modProp = node.getLastModifiedAsDate();
                Date pubProp = node.getLastPublishedAsDate();
                Date liveModProp = publishedNode.getLastModifiedAsDate();
                if (pubProp == null) {
                    pubProp = liveModProp;
                }
                if (modProp == null || pubProp == null) {
                    logger.warn(node.getPath() + " : Some property is null [last modified / last published / last modified (live)]: " + modProp + "/" + pubProp + "/" +
                            liveModProp);
                    status = PublicationInfo.MODIFIED;
                } else {
                    if (modProp.after(pubProp)) {
                        status = PublicationInfo.MODIFIED;
                    } else {
                        status = PublicationInfo.PUBLISHED;
                    }
                }
                if (node.isNodeType("jmix:markedForDeletion")) {
                    status = PublicationInfo.MARKED_FOR_DELETION;
                }
            }
        }
        return status;
    }

    private void getReferences(JCRNodeWrapper node, Set<String> languages, boolean includesReferences,
                               boolean includesSubnodes, JCRSessionWrapper sourceSession,
                               JCRSessionWrapper destinationSession, Map<String, PublicationInfoNode> infosMap,
                               List<PublicationInfo> infos, PublicationInfoNode info) throws RepositoryException {
        List<ExtendedPropertyDefinition> defs = node.getReferenceProperties();
        for (ExtendedPropertyDefinition def : defs) {
            if (!def.getName().equals("*") && node.hasProperty(def.getName())) {
                Property p = node.getProperty(def.getName());
                PropertyDefinition definition = p.getDefinition();
                if (definition != null && (definition.getRequiredType() == PropertyType.REFERENCE ||
                        definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) &&
                        !p.getName().startsWith("jcr:") && !p.getName().equals("j:templateNode") && !p.getName().equals("j:sourceTemplate")) {
                    if (definition.isMultiple()) {
                        Value[] vs = p.getValues();
                        for (Value v : vs) {
                            try {
                                JCRNodeWrapper ref = node.getSession().getNodeByUUID(v.getString());
                                if (!ref.isNodeType("jnt:page")) {
                                    PublicationInfoNode n =
                                            getPublicationInfo(ref, languages, includesReferences, includesSubnodes, false,
                                                    sourceSession, destinationSession, infosMap, infos);
                                    info.addReference(new PublicationInfo(n));
                                }
                            } catch (ItemNotFoundException e) {
                                if (definition.getRequiredType() == PropertyType.REFERENCE) {
                                    logger.warn("Cannot get reference " + p.getName() + " = " + v.getString() + " from node " + node.getPath());
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Cannot get reference " + p.getName() + " = " + v.getString() + " from node " + node.getPath());
                                    }
                                }

                            }
                        }
                    } else {
                        try {
                            JCRNodeWrapper ref = (JCRNodeWrapper) p.getNode();
                            if (!ref.isNodeType("jnt:page")) {
                                PublicationInfoNode n =
                                        getPublicationInfo(ref, languages, includesReferences, includesSubnodes, false,
                                                sourceSession, destinationSession, infosMap, infos);
                                info.addReference(new PublicationInfo(n));
                            }
                        } catch (ItemNotFoundException e) {
                            if (definition.getRequiredType() == PropertyType.REFERENCE) {
                                logger.warn("Cannot get reference " + p.getName() + " = " + p.getString() + " from node " + node.getPath());
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Cannot get reference " + p.getName() + " = " + p.getString() + " from node " + node.getPath());
                                }
                            }
                        }
                    }
                }
            }
        }
//        PropertyIterator pi = node.getProperties();
//        while (pi.hasNext()) {
//            Property p = pi.nextProperty();
//        }
    }

    protected void addRemovedLabel(JCRNodeWrapper node, final String label) throws RepositoryException {
        if (node.isVersioned()) {
            node.getVersionHistory().addVersionLabel(node.getBaseVersion().getName(), label, false);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper child = (JCRNodeWrapper) ni.next();
            addRemovedLabel(child, label);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void start() throws JahiaInitializationException {
        // nothing to do
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws JahiaException {
        // nothing to do
    }


    public void print(VersionHistory vh) throws RepositoryException {
        Version root = vh.getRootVersion();

        print(root, 0);
    }

    public void print(Version v, int indent) throws RepositoryException {
        System.out.print(StringUtils.leftPad("", indent) + "---- " + v.getName());
        Version[] preds = v.getPredecessors();
        System.out.print("(");
        for (Version pred : preds) {
            System.out.print(" " + pred.getName());
        }
        System.out.print(")");
        System.out.println("");
        Version[] succ = v.getSuccessors();
        for (Version version : succ) {
            print(version, indent + 2);
        }
    }
}