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
//        if (!node.isNodeType("jmix:publication")) {
//            if (!node.isCheckedOut()) {
//                node.checkout();
//            }
//            node.addMixin("jmix:publication");
//            session.save();
//        }
        if (node.isLockable()) {
            node.lockAndStoreToken("validation"," " +key+" ");
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
                    node.unlock("validation"," " +key+" ");
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
     * @param uuid                 UUID of the node to publish
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
        LinkedHashSet<String> allIds = new LinkedHashSet<String>();

        for (PublicationInfo publicationInfo : publicationInfos) {
            allIds.addAll(publicationInfo.getAllUuids(false, false));
            for (PublicationInfo subtree : publicationInfo.getAllReferences()) {
                allIds.addAll(subtree.getAllUuids(false, false));
            }
        }
        publish(new ArrayList<String>(allIds), sourceWorkspace, destinationWorkspace, comments);
    }

    public void publish(final List<String> uuids, final String sourceWorkspace,
                        final String destinationWorkspace, final List<String> comments) throws RepositoryException {
        if (uuids.isEmpty())
            return;

        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        final String username = user != null ? user.getUsername() : null;

        JCRTemplate.getInstance().doExecute(true, username, sourceWorkspace, null, new JCRCallback<Object>() {
            public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                JCRTemplate.getInstance().doExecute(true, username, destinationWorkspace, new JCRCallback<Object>() {
                    public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                        publish(uuids, sourceSession, destinationSession, comments);
                        return null;
                    }
                });

                return null;
            }
        });
    }

    private void publish(final List<String> uuidsToPublish, JCRSessionWrapper sourceSession,
                         JCRSessionWrapper destinationSession, final List<String> comments)
            throws RepositoryException {
        final Calendar calendar = new GregorianCalendar();
//        uuids.add(publicationInfo.getRoot().getUuid());

        final JCRNodeWrapper sourceNode = sourceSession.getNodeByUUID(uuidsToPublish.get(0));

        final String destinationWorkspace = destinationSession.getWorkspace().getName();

        try {
            sourceNode.getParent().getCorrespondingNodePath(destinationWorkspace);
        } catch (ItemNotFoundException e) {
            if (!destinationSession.isSystem()) {
                final String parentPath = sourceNode.getParent().getPath();
                JCRTemplate.getInstance().doExecute(true, sourceNode.getUser().getUsername(),
                        sourceNode.getSession().getWorkspace().getName(), null, new JCRCallback<Object>() {
                            public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                                return JCRTemplate.getInstance()
                                        .doExecute(true, sourceNode.getUser().getUsername(), destinationWorkspace,
                                                new JCRCallback<Object>() {
                                                    public Object doInJCR(final JCRSessionWrapper destinationSession)
                                                            throws RepositoryException {
                                                        cloneParents(sourceSession.getNode(parentPath), sourceSession,
                                                                destinationSession);
                                                        sourceSession.save();
                                                        destinationSession.save();
                                                        return null;
                                                    }
                                                });
                            }
                        });
            } else {
                cloneParents(sourceNode.getParent(), sourceNode.getSession(), destinationSession);
            }
        }

//        final List<String> uuidsToPublish = publicationInfo.getAllPublishableUuids();

//        for (PublicationInfo subtree : publicationInfo.getAllReferences()) {
//            try {
//                if (!uuids.contains(subtree.getRoot().getUuid()) &&
//                        !uuidsToPublish.contains(subtree.getRoot().getUuid())) {
//                    publish(subtree, sourceSession, destinationSession, uuids, comments);
//                }
//            } catch (Exception e) {
//                logger.warn("Cannot publish node at : " + subtree.getRoot().getUuid(), e);
//            }
//        }

        List<JCRNodeWrapper> toPublish = new ArrayList<JCRNodeWrapper>();
        for (String uuid : uuidsToPublish) {
            toPublish.add(sourceSession.getNodeByUUID(uuid));
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
                    try {
                        JCRNodeWrapper destNode = destinationSession
                                .getNode(jcrNodeWrapper.getCorrespondingNodePath(destinationWorkspace));
                        if (!destinationVersionManager.isCheckedOut(destNode.getPath())) {
                            destinationVersionManager.checkout(destNode.getPath());
                        }
                        destNode.setProperty("j:published", Boolean.TRUE);
                    } catch (ItemNotFoundException e) {
                    }
                }
            }
            sourceNode.getSession().save();
            destinationSession.save();
        }

//        final List<String> prunedSourcePath = new ArrayList<String>();
//        for (JCRNodeWrapper node : pruneSourceNodes) {
//            prunedSourcePath.add(node.getIdentifier());
//        }

        JCRObservationManager.setEventsDisabled(Boolean.TRUE);
        try {
//            JCRNodeWrapper destNode = destinationSession.getNode(sourceNode.getPath());
//            ArrayList<JCRNodeWrapper> pruneDestNodes = new ArrayList<JCRNodeWrapper>();
//            getBlockedAndReferencesList(destNode, new ArrayList<JCRNodeWrapper>(), pruneDestNodes, new ArrayList<JCRNodeWrapper>(), languages, allSubTree);
//            final List<String> prunedDestPath = new ArrayList<String>();
//            for (JCRNodeWrapper node : pruneDestNodes) {
//                prunedDestPath.add(node.getPath());
//            }

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
                    nodeWrapper.remove();
                } catch (InvalidItemStateException e) {
                    logger.warn("Already deleted : "+nodeWrapper.getPath());
                }                
            }
            for (String s : toDelete) {
                try {
                    JCRNodeWrapper node = destinationSession.getNodeByIdentifier(s);
                    node.remove();
                } catch (ItemNotFoundException e) {
                    logger.warn("Already deleted : "+s);
                } catch (InvalidItemStateException e) {
                    logger.warn("Already deleted : "+s);
                }
            }
            sourceSession.save();
            destinationSession.save();

            mergeToDestinationWorkspace(toPublish, uuidsToPublish, sourceNode.getSession(), destinationSession,
                    calendar);
//        } catch (PathNotFoundException e) {
//            cloneToDestinationWorkspace(toPublish, uuidsToPublish, sourceNode.getSession(),
//                    destinationSession, calendar);
        } finally {
            JCRObservationManager.setEventsDisabled(null);
        }

        if (loggingService.isEnabled()) {
            // now let's output the publication information to the logging service.
            for (JCRNodeWrapper publishedNode : toPublish) {
                String userID = sourceSession.getUserID();
                if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
                    userID = userID.substring(JahiaLoginModule.SYSTEM.length());
                }
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
    
    private void cloneParents(JCRNodeWrapper node, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        String path;
        try {
            path = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        } catch (ItemNotFoundException e) {
            cloneParents(node.getParent(), sourceSession, destinationSession);
            path = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
            try {
                // Check if node still does not exist in target space - if it has been cloned with parent, return
                node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                return;
            } catch (ItemNotFoundException ee) {
            }
        }
        JCRNodeWrapper destinationNode = doClone(node, null, sourceSession, destinationSession);
        if (node.getParent().isNodeType("mix:versionable")) {
            checkpoint(destinationSession, destinationSession.getNode(path),
                    destinationSession.getWorkspace().getVersionManager());
        }
    }

    private void mergeToDestinationWorkspace(final List<JCRNodeWrapper> toPublish, final List<String> uuids,
                                             final JCRSessionWrapper sourceSession,
                                             final JCRSessionWrapper destinationSession, Calendar calendar)
            throws RepositoryException {
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();
        final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

        for (final JCRNodeWrapper node : toPublish) {
            if (node.hasProperty("jcr:mergeFailed")) {
                Value[] failed = node.getProperty("jcr:mergeFailed").getValues();

                for (Value value : failed) {
                    logger.warn("-- Failed merge waiting : " + node.getPath() + " / " + value.getString());
                }
                continue;
            }
        }

        if (toPublish.isEmpty()) {
            return;
        }

        for (JCRNodeWrapper node : toPublish) {
            if (node.isNodeType("jmix:lastPublished")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Setting last published for {}", node.getPath());
                }
//            if (!sourceSession.getWorkspace().getName().equals(LIVE_WORKSPACE)) {
                VersionManager versionManager = node.getSession().getWorkspace().getVersionManager();
                if (!versionManager.isCheckedOut(node.getPath())) {
                    versionManager.checkout(node.getPath());
                }
                node.setProperty("j:lastPublished", calendar);

                String userID = destinationSession.getUserID();
                if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
                    userID = userID.substring(JahiaLoginModule.SYSTEM.length());
                }
                node.setProperty("j:lastPublishedBy", userID);
            }
//            }
        }

        sourceSession.save();

        for (JCRNodeWrapper node : toPublish) {
            // Node has been modified, check in now
            if (node.isNodeType("mix:versionable")) {
                sourceVersionManager.checkpoint(node.getPath());
            }
        }
        for (final JCRNodeWrapper node : toPublish) {
            try {
                if (!node.isNodeType("mix:versionable")) {
                    destinationSession.getNode(node.getCorrespondingNodePath(destinationSession.getWorkspace().getName())).update(sourceSession.getWorkspace().getName());
                    continue;
                }

                final String path = node.getPath();
                String destinationPath =
                        node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());

                // Item exists at "destinationPath" in live space, update it

                JCRNodeWrapper destinationNode = destinationSession
                        .getNode(destinationPath); // Live node exists - merge live node from source space

                // force conflict
                if (!destinationVersionManager.isCheckedOut(destinationNode.getPath())) {
                    destinationVersionManager.checkout(destinationNode.getPath());
                }

                final String oldPath = handleSharedMove(sourceSession, node, node.getPath());

                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Merge node : " + path + " source v=" + node.getBaseVersion().getName() + " , dest node v=" +
                                destinationSession.getNode(destinationPath).getBaseVersion().getName());
                }

                if (!node.getPath().equals(destinationPath)) {
                    try {
                        destinationVersionManager
                                .checkout(StringUtils.substringBeforeLast(destinationPath, "/")); // previous parent
                        String newParentPath = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                        destinationVersionManager.checkout(newParentPath); // new parent
                        recurseCheckout(destinationSession.getNode(destinationPath), null,
                                destinationVersionManager); // node and sub nodes

                        destinationSession.move(destinationPath, newParentPath + "/" + node.getName());
                        destinationSession.save();

                        destinationPath = newParentPath + "/" + node.getName();
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

//                recurseCheckin(destinationSession.getNode(destinationPath), pruneNodes, destinationVersionManager);
                if (destinationNode.isNodeType("mix:versionable") && destinationNode.isCheckedOut() &&
                        !destinationNode.hasProperty("jcr:mergeFailed")) {
                    destinationVersionManager.checkpoint(destinationPath);
                }

                destinationSession.save();
                NodeIterator ni = destinationVersionManager
                        .merge(destinationPath, node.getSession().getWorkspace().getName(), true, true);

                if (ni.hasNext()) {
                    while (ni.hasNext()) {
                        Node failed = ni.nextNode();
                        if (!destinationVersionManager.isCheckedOut(failed.getPath())) {
                            destinationVersionManager.checkout(failed.getPath());
                        }

                        JCRNodeWrapper destNode = destinationSession.getNode(failed.getPath());

                        ConflictResolver resolver = new ConflictResolver(node, destNode);
                        resolver.setUuids(uuids);
                        try {
                            resolver.applyDifferences();

                            if (!resolver.getUnresolvedDifferences().isEmpty()) {
                                logger.warn("Unresolved conflicts : " + resolver.getUnresolvedDifferences());
                            }
                        } catch (RepositoryException e) {
                            logger.error("Error when merging differences",e);
                        }
                        destinationVersionManager
                                .doneMerge(failed.getPath(), sourceVersionManager.getBaseVersion(path));
                    }
//                    if (!sourceSession.getWorkspace().getName().equals(LIVE_WORKSPACE)) {
                    recurseCheckpoint(destinationSession, destinationNode, uuids,
                            destinationVersionManager, calendar);
//                        node.update(destinationSession.getWorkspace().getName()); // do not update live in reverse publish
//                    }
                }

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
            } catch (ItemNotFoundException e) {
                // Item does not exist yet in live space
                JCRNodeWrapper destinationNode =
                        doClone(node, uuids, sourceSession, destinationSession);
                if (node.getParent().isNodeType("mix:versionable")) {
                    destinationVersionManager.checkpoint(
                            node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName()));
                }
                recurseCheckpoint(destinationSession, destinationNode, null, destinationVersionManager, calendar);
            }
        }
    }

    JCRNodeWrapper doClone(JCRNodeWrapper sourceNode, List<String> uuidsToPublish, JCRSessionWrapper sourceSession,
                           JCRSessionWrapper destinationSession) throws RepositoryException {
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
            cloneParents(sourceNode.getParent(), sourceSession, destinationSession);
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
            Set<String> denied = new HashSet<String>();
            NodeIterator it = sourceNode.getNodes();
            while (it.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) it.next();
                if (nodeWrapper.isVersioned()) {
                    denied.add(nodeWrapper.getPath());
                }
            }
            JahiaAccessManager.setDeniedPaths(denied);

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
                logger.warn("Cloning existing node "+sourceNode.getPath());
                if (sourceNode.isNodeType("mix:shareable")) {
                    // Shareable node - todo : check if we need to move or clone

                    String oldPath = handleSharedMove(sourceSession, sourceNode, destinationPath);

                    // Clone the node node in live space
                    destinationSession.getWorkspace()
                            .clone(destinationWorkspaceName, correspondingNodePath, destinationPath, false);

                    if (oldPath != null) {
                        try {
                            JCRNodeWrapper node = destinationSession.getNode(oldPath);
                            recurseCheckout(node, null, destinationVersionManager);
                            JCRNodeWrapper oldParent = node.getParent();
                            if (!destinationVersionManager.isCheckedOut(oldParent.getPath())) {
                                destinationVersionManager.checkout(oldParent.getPath());
                            }
                            node.remove();
                            node.getSession().save();
                        } catch (RepositoryException e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                } else {
                    // Non shareable node has been moved
                    destinationVersionManager
                            .checkout(StringUtils.substringBeforeLast(correspondingNodePath, "/")); // previous parent
                    destinationVersionManager.checkout(destinationParentPath); // new parent
                    recurseCheckout(destinationSession.getNode(correspondingNodePath), null,
                            destinationVersionManager); // node and sub nodes

                    destinationSession.move(correspondingNodePath, destinationPath);
                    destinationSession.save();
//                    destinationSession.getWorkspace()
//                            .clone(sourceSession.getWorkspace().getName(), sourceNodePath, destinationPath, true);
//                    destinationVersionManager.checkin(destinationParentPath);
                }
            } catch (ItemNotFoundException e) {
                destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNodePath, destinationPath, false);
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
                            destinationSession.getNode( (destinationParentPath.equals("/") ? "" : destinationParentPath ) + "/" + currentNode.getName());
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
            destinationNode = destinationSession.getNode(sourceNode.getCorrespondingNodePath(destinationWorkspaceName));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return destinationNode;
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
            node.setProperty(FULLPATH, node.getPath());
        }
        session.save();
        Version version = versionManager.checkpoint(node.getPath());
        if (logger.isDebugEnabled()) {
            logger.debug("Checkin node " + node.getPath() + " in workspace " + session.getWorkspace().getName() +
                " with new version " + version.getName() + " base version is " +
                versionManager.getBaseVersion(node.getPath()).getName());
        }
    }

    private void recurseCheckpoint(Session session, JCRNodeWrapper node, List<String> uuidsToPublish,
                                   VersionManager versionManager, Calendar calendar) throws RepositoryException {
        if (node.isNodeType("mix:versionable") && versionManager.isCheckedOut(node.getPath()) &&
                !node.hasProperty("jcr:mergeFailed")) {
            checkpoint(session, node, versionManager);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper sub = (JCRNodeWrapper) ni.nextNode();
            if (uuidsToPublish == null || uuidsToPublish.contains(sub.getIdentifier())) {
                recurseCheckpoint(session, sub, uuidsToPublish, versionManager, calendar);
            }
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
     * @param uuids      uuids of the node to unpublish
     * @param languages
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(final List<String> uuids, final Set<String> languages) throws RepositoryException {
        final String username;
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        } else {
            username = null;
        }
        
        final Set<String> ignoredNodes = new HashSet<String>();
        
        JCRTemplate.getInstance().doExecute(true, username, EDIT_WORKSPACE, null, new JCRCallback<Object>() {
            public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                Set<String> translationLanguages = new HashSet<String>();
                boolean first = true;
                VersionManager vm = sourceSession.getWorkspace().getVersionManager();
                List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>(uuids.size()); 
                for (String uuid : uuids) {
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
                            
                            if (languages != null){
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
                            logger.info("Node {} does not exist in the default workspace any longer."+
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
                        for (Iterator<JCRNodeWrapper> it = nodes.iterator(); it.hasNext();) {
                            if (ignoredNodes.contains(it.next().getPath())) {
                                it.remove();
                            }
                        }
                    }
                }
                for (ListIterator<JCRNodeWrapper> it = nodes.listIterator(nodes.size()); it.hasPrevious();) {
                    JCRNodeWrapper node = it.previous();
                    unpublish(node, languages, ignoredNodes);
                    sourceSession.save();
                }
                return null;
            }
        });

        JCRTemplate.getInstance().doExecute(true, username, LIVE_WORKSPACE, new JCRCallback<Object>() {
            public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                for (ListIterator<String> it = uuids.listIterator(uuids.size()); it.hasPrevious();) {
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
        if (loggingService.isEnabled()) { 
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

        if (info != null && JCRContentUtils.isNotJcrUuid(uuid)) {
            info.setStatus(PublicationInfo.PUBLISHED);
            return info;
        }
        
        if (info == null) {
            info = new PublicationInfoNode(node.getIdentifier(), node.getPath());
            info.setSubtreeProcessed(allsubtree);
            infosMap.put(uuid, info);
        
            if (node.hasProperty("j:deletedChildren")) {
                JCRPropertyWrapper p = node.getProperty("j:deletedChildren");
                Value[] values = p.getValues();
                for (Value value : values) {
                    try {
                        JCRNodeWrapper deletedNode = destinationSession.getNodeByUUID(value.getString());
                        PublicationInfoNode deletedInfo = new PublicationInfoNode(deletedNode.getIdentifier(), deletedNode.getPath());
                        deletedInfo.setStatus(PublicationInfo.DELETED);
                        info.addChild(deletedInfo);
                    } catch (ItemNotFoundException e) {
                        logger.debug("Cannot find deleted subnode of "+node.getPath() + " : " + value.getString()+", we keep the reference until next publication to be sure to erase it from the live workspace.");
                    }
                }
            }
    
            info.setStatus(getStatus(node, destinationSession, languages));
            if (info.getStatus() == PublicationInfo.CONFLICT) {
                if(languages == null || languages.isEmpty()) {
                    info.setCanPublish(true,"");
                } else {
                    for (String language : languages) {
                        info.setCanPublish(false,language);
                    }
                }
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
    
            // todo : performance problem on permission check
    //        info.setCanPublish(stageNode.hasPermission(JCRNodeWrapper.WRITE_LIVE));
            if(languages == null || languages.isEmpty()) {
                info.setCanPublish(true,"");
            } else {
                for (String language : languages) {
                    info.setCanPublish(canPublish(node, language),language);
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
                
                boolean supportsPublication = sourceSession.getProviderSession(n.getProvider()).getRepository().getDescriptorValue(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED).getBoolean();
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
                    hasProperty = ((ExtendedPropertyDefinition)property.getDefinition()).isInternationalized();
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
                if (modProp == null || pubProp == null || liveModProp == null) {
                    logger.warn(node.getPath() + " : Some property is null [last modified / last published / last modified (live)]: " + modProp + "/" + pubProp + "/" +
                            liveModProp);
                    status = PublicationInfo.MODIFIED;
                } else {
                    long mod = modProp.getTime();
                    long pub = pubProp.getTime();
                    if (mod > pub) {
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

    private boolean canPublish(JCRNodeWrapper node, String language) {
        boolean b;
        b = node.hasPermission("jcr:all_default");
        if (b) {
            return b;
        }
        b = node.hasPermission("jcr:write_default");
        if (b) {
            return b;
        }
        b = node.hasPermission("jcr:modifyProperties_default");
        if (b) {
            return b;
        }
        b = node.hasPermission("jcr:modifyProperties_default_" + language);
        if (b) {
            return b;
        }
        b = node.hasPermission("jcr:addChildNodes_default");
        if (b) {
            return b;
        }
        b = node.hasPermission("jcr:removeNode_default");
        if (b) {
            return b;
        }
        b = node.hasPermission("jcr:removeChildNodes_default");
        return b;
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
                                    logger.warn("Cannot get reference " + v.getString() + " from node " + node.getPath());
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Cannot get weak reference {} from node {}", v.getString(), node.getPath());
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
                            logger.warn("Cannot get reference " + p.getString());
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