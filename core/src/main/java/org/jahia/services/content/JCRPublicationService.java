/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.slf4j.Logger;
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
    private static transient Logger logger = org.slf4j.LoggerFactory.getLogger(JCRPublicationService.class);
    private JCRSessionFactory sessionFactory;
    private JCRVersionService jcrVersionService;
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

    public void setJcrVersionService(JCRVersionService jcrVersionService) {
        this.jcrVersionService = jcrVersionService;
    }

    public void setLoggingService(MetricsLoggingService loggingService) {
        this.loggingService = loggingService;
    }

    /**
     * Get the singleton instance of the JCRPublicationService
     *
     * @return the singleton instance of the JCRPublicationService
     */
    public synchronized static JCRPublicationService getInstance() {
        if (instance == null) {
            instance = new JCRPublicationService();
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
            node.lockAndStoreToken(key);
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
        JCRNodeWrapper node = session.getNodeByUUID(id);
        if (node.isLocked()) {
            try {
                node.unlock(key);
            } catch (LockException e) {
            }
        }
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
            allIds.addAll(publicationInfo.getAllUuids());
            for (PublicationInfo subtree : publicationInfo.getAllReferences()) {
                allIds.addAll(subtree.getAllUuids());
            }
        }
        publish(new ArrayList<String>(allIds), sourceWorkspace, destinationWorkspace, comments);
    }

    public void publish(final List<String> uuids, final String sourceWorkspace,
                        final String destinationWorkspace, final List<String> comments) throws RepositoryException {
        final String username;
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        } else {
            username = null;
        }
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
        if (destinationSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
            for (JCRNodeWrapper jcrNodeWrapper : toPublish) {
                logger.debug("Publishing node " + jcrNodeWrapper.getPath());
                if (jcrNodeWrapper.isNodeType("jmix:publication") && (!jcrNodeWrapper.hasProperty("j:published") ||
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

            mergeToDestinationWorkspace(toPublish, uuidsToPublish, sourceNode.getSession(), destinationSession,
                    calendar);
//        } catch (PathNotFoundException e) {
//            cloneToDestinationWorkspace(toPublish, uuidsToPublish, sourceNode.getSession(),
//                    destinationSession, calendar);
        } finally {
            JCRObservationManager.setEventsDisabled(null);
        }

        // now let's output the publication information to the logging service.
        for (JCRNodeWrapper publishedNode : toPublish) {
            String userID = sourceSession.getUserID();
            if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
                userID = userID.substring(JahiaLoginModule.SYSTEM.length());
            }
            StringBuffer commentBuf = new StringBuffer();
            if (comments != null) {
                for (String comment : comments) {
                    commentBuf.append(comment);
                }
            }
            loggingService.logContentEvent(userID, "", "", publishedNode.getIdentifier(), publishedNode.getPath(),
                    publishedNode.getPrimaryNodeTypeName(), "publishedNode", sourceSession.getWorkspace().getName(),
                    destinationSession.getWorkspace().getName(), commentBuf.toString());
        }
    }

    private void cloneParents(JCRNodeWrapper node, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        String path;
        try {
            path = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        } catch (ItemNotFoundException e) {
            cloneParents(node.getParent(), sourceSession, destinationSession);
            path = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        }
        JCRNodeWrapper destinationNode = doClone(node, null, sourceSession, destinationSession);
        if (node.getParent().isNodeType("mix:versionable")) {
            checkin(destinationSession, destinationSession.getNode(path),
                    destinationSession.getWorkspace().getVersionManager());
        }
    }

    private void mergeToDestinationWorkspace(final List<JCRNodeWrapper> toPublish, final List<String> uuids,
                                             final JCRSessionWrapper sourceSession,
                                             final JCRSessionWrapper destinationSession, Calendar calendar)
            throws RepositoryException {
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();
        final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

        List<JCRNodeWrapper> modified = new ArrayList<JCRNodeWrapper>();
        for (final JCRNodeWrapper node : toPublish) {
            if (node.hasProperty("jcr:mergeFailed")) {
                Value[] failed = node.getProperty("jcr:mergeFailed").getValues();

                for (Value value : failed) {
                    System.out.println("-- failed merge waiting : " + node.getPath() + " / " + value.getString());
                }
                continue;
            }

            if (!node.hasProperty("j:lastPublished")) {
                modified.add(node);
            } else if (node.isNodeType("jmix:lastPublished")) {
                Date modificationDate = node.getProperty("jcr:lastModified").getDate().getTime();
                Date publicationDate = node.getProperty("j:lastPublished").getDate().getTime();
                if (publicationDate.getTime() < modificationDate.getTime()) {
                    modified.add(node);
                }
            }
        }

        if (modified.isEmpty()) {
            return;
        }

        for (JCRNodeWrapper node : modified) {
            if (node.isNodeType("jmix:lastPublished")) {
                logger.debug("Setting last published : " + node.getPath());
//            if (!sourceSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
                VersionManager versionManager = node.getSession().getWorkspace().getVersionManager();
                if (!versionManager.isCheckedOut(node.getPath())) {
                    versionManager.checkout(node.getPath());
                }
                node.setProperty("j:lastPublished", calendar);
                node.setProperty("j:lastPublishedBy", destinationSession.getUserID());
            }
//            }
        }

        sourceSession.save();

        for (JCRNodeWrapper node : modified) {
            // Node has been modified, check in now
            if (node.isNodeType("mix:versionable")) {
                sourceVersionManager.checkin(node.getPath());
            }
        }
        for (final JCRNodeWrapper node : modified) {
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
                destinationVersionManager.checkout(destinationNode.getPath());

                final String oldPath = handleSharedMove(sourceSession, node, node.getPath());

                logger.debug(
                        "Merge node : " + path + " source v=" + node.getBaseVersion().getName() + " , dest node v=" +
                                destinationSession.getNode(destinationPath).getBaseVersion().getName());

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
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

//                recurseCheckin(destinationSession.getNode(destinationPath), pruneNodes, destinationVersionManager);
                if (destinationNode.isNodeType("mix:versionable") && destinationNode.isCheckedOut() &&
                        !destinationNode.hasProperty("jcr:mergeFailed")) {
                    destinationVersionManager.checkin(destinationPath);
                }

                destinationSession.save();
                NodeIterator ni = destinationVersionManager
                        .merge(destinationPath, node.getSession().getWorkspace().getName(), true, true);

                if (ni.hasNext()) {
                    while (ni.hasNext()) {
                        Node failed = ni.nextNode();
                        destinationVersionManager.checkout(failed.getPath());

                        JCRNodeWrapper destNode = destinationSession.getNode(failed.getPath());

                        ConflictResolver resolver = new ConflictResolver(node, destNode);
                        resolver.setUuids(uuids);
                        try {
                            resolver.applyDifferences();

                            if (!resolver.getUnresolvedDifferences().isEmpty()) {
                                logger.warn("Unresolved conflicts : " + resolver.getUnresolvedDifferences());
                            }
                            destinationVersionManager
                                    .doneMerge(failed.getPath(), sourceVersionManager.getBaseVersion(path));
                        } catch (RepositoryException e) {
                            logger.error("Error when merging differences",e);
                        }
                    }
//                    if (!sourceSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
                    recurseCheckin(destinationSession, destinationNode, uuids,
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
                        e.printStackTrace();
                    }
                }

                logger.debug("Merge node end : " + path + " source v=" +
                        sourceSession.getNode(path).getBaseVersion().getName() + " , dest node v=" +
                        destinationSession.getNode(destinationPath).getBaseVersion().getName());


            } catch (ItemNotFoundException e) {
                // Item does not exist yet in live space
                JCRNodeWrapper destinationNode =
                        doClone(node, uuids, sourceSession, destinationSession);
                if (node.getParent().isNodeType("mix:versionable")) {
                    destinationVersionManager.checkin(
                            node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName()));
                }
                recurseCheckin(destinationSession, destinationNode, null, destinationVersionManager, calendar);
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
        logger.debug("Cloning node : " + sourceNodePath + " parent path " + parent.getPath());
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
                            destinationVersionManager.checkout(oldParent.getPath());
                            node.remove();
                            node.getSession().save();
                        } catch (RepositoryException e) {
                            e.printStackTrace();
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
                            destinationSession.getNode(destinationParentPath + "/" + currentNode.getName());
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
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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

    private void checkin(Session session, JCRNodeWrapper node, VersionManager versionManager)
            throws RepositoryException {
        logger.debug("Checkin node " + node.getPath() + " in workspace " + session.getWorkspace().getName() +
                " with current version " + versionManager.getBaseVersion(node.getPath()).getName());
        session.save();
        Version version = versionManager.checkin(node.getPath());
        logger.debug("Checkin node " + node.getPath() + " in workspace " + session.getWorkspace().getName() +
                " with new version " + version.getName() + " base version is " +
                versionManager.getBaseVersion(node.getPath()).getName());
    }

    private void recurseCheckin(Session session, JCRNodeWrapper node, List<String> uuidsToPublish,
                                VersionManager versionManager, Calendar calendar) throws RepositoryException {
        if (node.isNodeType("mix:versionable") && versionManager.isCheckedOut(node.getPath()) &&
                !node.hasProperty("jcr:mergeFailed")) {
            checkin(session, node, versionManager);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper sub = (JCRNodeWrapper) ni.nextNode();
            if (uuidsToPublish == null || uuidsToPublish.contains(sub.getIdentifier())) {
                recurseCheckin(session, sub, uuidsToPublish, versionManager, calendar);
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
     * @param uuid      uuid of the node to unpublish
     * @param languages
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(String uuid, Set<String> languages) throws RepositoryException {
        JCRSessionWrapper sourceSession = getSessionFactory().getCurrentUserSession();
        JCRSessionWrapper destinationSession = getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);
        JCRNodeWrapper node = sourceSession.getNodeByUUID(uuid);
        VersionManager vm = sourceSession.getWorkspace().getVersionManager();
        if (!vm.isCheckedOut(node.getPath())) {
            vm.checkout(node.getPath());
        }
        unpublish(node, languages);
        sourceSession.save();

        JCRNodeWrapper destNode = destinationSession.getNode(node.getCorrespondingNodePath(Constants.LIVE_WORKSPACE));
        unpublish(destNode, languages);
        destinationSession.save();
    }

    private void unpublish(JCRNodeWrapper node, Set<String> languages) throws RepositoryException {
        if (!node.isCheckedOut()) {
            node.checkout();
        }
        node.setProperty("j:published", false);
        if (!node.isNodeType("jmix:publication")) {
            node.addMixin("jmix:publication");
        }
        NodeIterator ni = node.getNodes("j:translation*");
        while (ni.hasNext()) {
            JCRNodeWrapper i18n = (JCRNodeWrapper) ni.next();
            if (languages == null || languages.contains(i18n.getProperty("jcr:language").getString())) {
                if (!i18n.isCheckedOut()) {
                    i18n.checkout();
                }
                i18n.setProperty("j:published", false);
            }
        }
        String userID = node.getSession().getUserID();
        if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
            userID = userID.substring(JahiaLoginModule.SYSTEM.length());
        }
        loggingService
                .logContentEvent(userID, "", "", node.getIdentifier(), node.getPath(), node.getPrimaryNodeTypeName(),
                        "unpublishedNode", node.getSession().getWorkspace().getName());
    }

    public List<PublicationInfo> getPublicationInfos(List<String> uuids, Set<String> languages,
                                                     boolean includesReferences, boolean includesSubnodes,
                                                     boolean allsubtree, final String sourceWorkspace,
                                                     final String destinationWorkspace) throws RepositoryException {
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

        JCRNodeWrapper stageNode = sourceSession.getNodeByUUID(uuid);
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

        final String uuid = node.getIdentifier();
        if (infosMap.containsKey(uuid)) {
            return infosMap.get(uuid);
        }

        PublicationInfoNode info = new PublicationInfoNode(node.getIdentifier(), node.getPath());
        infosMap.put(uuid, info);

        JCRNodeWrapper publishedNode = null;
        try {
            publishedNode = destinationSession.getNodeByUUID(uuid);
        } catch (ItemNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("No live node for staging node " + node.getPath());
            }
        }
        if (!node.checkLanguageValidity(languages)) {
            info.setStatus(PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE);
            for (String language : languages) {
                Locale locale = LanguageCodeConverters.getLocaleFromCode(language);
                if (node.checkI18nAndMandatoryPropertiesForLocale(locale)) {
                    info.setStatus(PublicationInfo.MANDATORY_LANGUAGE_VALID);
                }
            }
        } else if (node.hasProperty("j:published") && !node.getProperty("j:published").getBoolean()) {
            info.setStatus(PublicationInfo.UNPUBLISHED);
        } else if (publishedNode == null) {
            try {
                try {
                    destinationSession.getNodeByUUID(node.getParent().getUUID()).getNode(node.getName());
                } catch (UnsupportedRepositoryOperationException e) {
                }
                // Conflict , a node exists in live !
                info.setStatus(PublicationInfo.CONFLICT);
                info.setCanPublish(false);
                return info;
            } catch (ItemNotFoundException e) {
            } catch (PathNotFoundException e) {
            }

            info.setStatus(PublicationInfo.NOT_PUBLISHED);
            if (node.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                boolean hasProperty = false;
                final PropertyIterator iterator = node.getProperties();
                while (iterator.hasNext() && !hasProperty) {
                    Property property = (Property) iterator.next();
                    hasProperty = ((ExtendedPropertyDefinition)property.getDefinition()).isInternationalized();
                }
                if (!hasProperty) {
                    info.setStatus(PublicationInfo.PUBLISHED);
                }
            }
        } else {
            if (node.hasProperty("jcr:mergeFailed") || publishedNode.hasProperty("jcr:mergeFailed")) {
                info.setStatus(PublicationInfo.CONFLICT);
            } else if (node.getLastModifiedAsDate() == null) {
                // No modification date - node is published
                info.setStatus(PublicationInfo.PUBLISHED);
            } else {
                Date modProp = node.getLastModifiedAsDate();
                Date pubProp = node.getLastPublishedAsDate();
                Date liveModProp = publishedNode.getLastModifiedAsDate();
                if (modProp == null || pubProp == null || liveModProp == null) {
                    logger.warn(info.getUuid() + " : Some property is null : " + modProp + "/" + pubProp + "/" +
                            liveModProp);
                    info.setStatus(PublicationInfo.MODIFIED);
                } else {
                    long mod = modProp.getTime();
                    long pub = pubProp.getTime();
                    if (mod > pub) {
                        info.setStatus(PublicationInfo.MODIFIED);
                    } else {
                        info.setStatus(PublicationInfo.PUBLISHED);
                    }
                }
            }
        }

        if (node.hasProperty(Constants.JAHIA_LOCKTYPES)) {
            Value[] lockTypes = node.getProperty(Constants.JAHIA_LOCKTYPES).getValues();
            for (Value lockType : lockTypes) {
                if (StringUtils.substringAfter(lockType.getString(), ":").startsWith("publication-")) {
                    info.setLocked(true);
                }
            }
        }

        // todo : performance problem on permission check
//        info.setCanPublish(stageNode.hasPermission(JCRNodeWrapper.WRITE_LIVE));
        info.setCanPublish(true);

        if (includesReferences || includesSubnodes) {
            if (includesReferences) {
                getReferences(node, languages, includesReferences, includesSubnodes, sourceSession, destinationSession,
                        infosMap, infos, info);
            }
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                boolean hasIndependantPublication = hasIndependantPublication(n);
                if (allsubtree && hasIndependantPublication) {
                    PublicationInfo newinfo = new PublicationInfo();
                    infos.add(newinfo);
                    newinfo.setRoot(getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                            sourceSession, destinationSession, infosMap, infos));
                }
                if (!hasIndependantPublication) {
                    if (languages != null && n.isNodeType("mix:language")) {
                        String translationLanguage = n.getProperty("jcr:language").getString();
                        if (languages.contains(translationLanguage)) {
                            PublicationInfoNode child =
                                    getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                                            sourceSession, destinationSession, infosMap, infos);
                            info.addChild(child);
                        }
                    } else if (n.isNodeType("jmix:lastPublished")) {
                        PublicationInfoNode child =
                                getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                                        sourceSession, destinationSession, infosMap, infos);
                        info.addChild(child);
                    } else {
                        getReferences(n, languages, includesReferences, includesSubnodes, sourceSession,
                                destinationSession, infosMap, infos, info);
                    }
                }
            }
        }
        return info;
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
                        !p.getName().startsWith("jcr:")) {
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
                                    logger.debug("Cannot get weak reference " + v.getString() + " from node " + node.getPath());
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
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * {@inheritDoc}
     */
    public void stop() throws JahiaException {
        //To change body of implemented methods use File | Settings | File Templates.
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