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
package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.logging.MetricsLoggingService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import java.util.*;

import static org.jahia.api.Constants.*;

/**
 * This is a Jahia service, which offers functionality to publish, unpublish or get publication info of JCR nodes
 *
 * @author toto
 */
public class JCRPublicationService extends JahiaService {

    private static class Holder {
        static final JCRPublicationService INSTANCE = new JCRPublicationService();
    }

    private static transient Logger logger = LoggerFactory.getLogger(JCRPublicationService.class);

    private int batchSize;

    private JCRSessionFactory sessionFactory;
    private MetricsLoggingService loggingService;

    private Set<String> propertiesToSkipForReferences = Collections.emptySet();

    private Set<String> referencedNodeTypesToSkip = Collections.emptySet();

    private boolean skipAllReferenceProperties;

    private JCRPublicationService() {
        super();
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
        return Holder.INSTANCE;
    }

    public boolean hasIndependantPublication(JCRNodeWrapper node) throws RepositoryException {
        return node.isNodeType("jmix:publication"); // todo : do we want to add this as a configurable in admin ?
        // currently it has to be set in definitions files
    }

    public void lockForPublication(final List<String> publicationInfo, final String workspace,
                                   final String key) throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecuteWithSystemSessionAsUser(getSessionFactory().getCurrentUserSession(workspace).getUser(),
                        workspace, null, new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                for (String id : publicationInfo) {
                                    doLock(id, session, key);
                                }
                                return null;
                            }
                        }
                );
    }

    private void doLock(String id, JCRSessionWrapper session, String key)
            throws RepositoryException {
        try {
            JCRNodeWrapper node = session.getNodeByUUID(id);
            if (node.isLockable()) {
                node.lockAndStoreToken("validation", " " + key + " ");
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Item does not exist anymore : " + id);
        }
    }

    public void unlockForPublication(final List<String> publicationInfo, final String workspace,
                                     final String key) throws RepositoryException {
        JCRTemplate.getInstance()
                .doExecuteWithSystemSessionAsUser(getSessionFactory().getCurrentUserSession(workspace).getUser(),
                        workspace, null, new JCRCallback<Object>() {
                            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                                for (String id : publicationInfo) {
                                    doUnlock(id, session, key);
                                }
                                return null;
                            }
                        }
                );
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
            allIds.addAll(publicationInfo.getAllUuids(false, false, false));
            for (PublicationInfo subtree : publicationInfo.getAllReferences()) {
                allIds.addAll(subtree.getAllUuids(false, false, false));
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
        publish(uuids, sourceWorkspace, destinationWorkspace, checkPermissions, true, comments);
    }

    public void publish(final List<String> uuids, final String sourceWorkspace,
                        final String destinationWorkspace, boolean checkPermissions, final boolean updateMetadata, final List<String> comments) throws RepositoryException {
        if (uuids.isEmpty())
            return;

        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();

        final Set<String> checkedUuids = new LinkedHashSet<String>();
        if (checkPermissions) {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            for (String uuid : uuids) {
                try {
                    if (session.getNodeByIdentifier(uuid).hasPermission("publish")) {
                        checkedUuids.add(uuid);
                    }
                } catch (javax.jcr.ItemNotFoundException e) {
                    logger.debug("Impossible to publish missing node", e);
                }
            }
        } else {
            checkedUuids.addAll(uuids);
        }

        if (!checkedUuids.isEmpty()) {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, sourceWorkspace, null, new JCRCallback<Object>() {
                public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                    JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, destinationWorkspace, null, new JCRCallback<Object>() {
                        public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                            sourceSession.setSkipValidation(true);
                            destinationSession.setSkipValidation(true);
                            publish(checkedUuids, sourceSession, destinationSession, updateMetadata, comments);
                            return null;
                        }
                    });

                    return null;
                }
            });
            // Refresh this user sessions after publication
            if (sessionFactory.getCurrentUser() != null) {
                sessionFactory.getCurrentUserSession(sourceWorkspace).refresh(false);
                sessionFactory.getCurrentUserSession(destinationWorkspace).refresh(false);
            }
        }
    }

    private void publish(final Set<String> uuidsToPublish, JCRSessionWrapper sourceSession,
                         JCRSessionWrapper destinationSession, boolean updateMetadata, final List<String> comments)
            throws RepositoryException {
        int totalCount = uuidsToPublish.size();
        if (batchSize < 0 || totalCount <= batchSize) {
            // no limit on the batch size
            long startTime = System.currentTimeMillis();
            doPublish(uuidsToPublish, sourceSession, destinationSession, updateMetadata, comments);
            logger.info("Published {} nodes in {} ms", totalCount, System.currentTimeMillis() - startTime);
        } else {
            logger.info("Publishing {} nodes in batches of {}", totalCount, batchSize);
            long startTime = System.currentTimeMillis();
            Set<String> batch = new LinkedHashSet<String>(batchSize);
            int batchIndex = 1;
            int batchTotalCount = (int) Math.ceil((double) totalCount / (double) batchSize);
            while (uuidsToPublish.size() > batchSize) {
                int batchCount = 0;
                for (Iterator<String> iterator = uuidsToPublish.iterator(); iterator.hasNext();) {
                    batch.add(iterator.next());
                    iterator.remove();
                    batchCount++;
                    if (batchCount >= batchSize) {
                        break;
                    }
                }

                logger.info("Processing batch {}/{}", batchIndex++, batchTotalCount);

                doPublish(batch, sourceSession, destinationSession, updateMetadata, comments);

                batch.clear();
            }

            if (uuidsToPublish.size() > 0) {
                doPublish(uuidsToPublish, sourceSession, destinationSession, updateMetadata, comments);
            }

            logger.info("Batch-published {} nodes in {} ms", totalCount, System.currentTimeMillis() - startTime);
        }
    }

    private void doPublish(final Set<String> uuidsToPublish, JCRSessionWrapper sourceSession,
                           JCRSessionWrapper destinationSession, boolean updateMetadata, final List<String> comments)
            throws RepositoryException {
        final Calendar calendar = new GregorianCalendar();
//        uuids.add(publicationInfo.getRoot().getUuid());

        final String destinationWorkspace = destinationSession.getWorkspace().getName();

        Set<JCRNodeWrapper> toPublish = new LinkedHashSet<JCRNodeWrapper>();
        for (String uuid : uuidsToPublish) {
            try {
                JCRNodeWrapper node = sourceSession.getNodeByUUID(uuid);
                if (!node.isNodeType("jmix:nolive") && !toPublish.contains(node) && supportsPublication(sourceSession, node)) {
                    toPublish.add(node);
                }
            } catch (javax.jcr.ItemNotFoundException e) {
                logger.debug("Impossible to publish missing node", e);
            }
        }

        String userID = destinationSession.getUserID();
        if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
            userID = userID.substring(JahiaLoginModule.SYSTEM.length());
        }

        VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
        Map<String,Value> previousValue = new HashMap<>();
        if (updateMetadata && destinationSession.getWorkspace().getName().equals(LIVE_WORKSPACE)) {
            for (JCRNodeWrapper jcrNodeWrapper : toPublish) {
                String nodePath = jcrNodeWrapper.getPath();
                if (logger.isDebugEnabled()) {
                    logger.debug("Publishing node {}", nodePath);
                }

                final boolean hasLastPublishedMixin = jcrNodeWrapper.isNodeType(Constants.JAHIAMIX_LASTPUBLISHED);
                if (hasLastPublishedMixin) {
                    previousValue.put(nodePath + "/" + Constants.PUBLISHED, jcrNodeWrapper.hasProperty(Constants.PUBLISHED) ? jcrNodeWrapper.getProperty(Constants.PUBLISHED).getValue() : null);
                    previousValue.put(nodePath + "/" + Constants.LASTPUBLISHED,  jcrNodeWrapper.hasProperty(Constants.LASTPUBLISHED) ? jcrNodeWrapper.getProperty(Constants.LASTPUBLISHED).getValue() : null);
                    previousValue.put(nodePath + "/" + Constants.LASTPUBLISHEDBY,  jcrNodeWrapper.hasProperty(Constants.LASTPUBLISHEDBY) ? jcrNodeWrapper.getProperty(Constants.LASTPUBLISHEDBY).getValue() : null);
                    jcrNodeWrapper.setProperty(Constants.PUBLISHED, Boolean.TRUE);
                    jcrNodeWrapper.setProperty(Constants.LASTPUBLISHED, calendar);
                    jcrNodeWrapper.setProperty(Constants.LASTPUBLISHEDBY, userID);
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

        JCRObservationManager.setEventListenersAvailableDuringPublishOnly(Boolean.TRUE);
        try {
            List<String> toDelete = new ArrayList<String>();
            List<JCRNodeWrapper> toDeleteOnSource = new ArrayList<JCRNodeWrapper>();
            for (Iterator<JCRNodeWrapper> lit = toPublish.iterator(); lit.hasNext(); ) {
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
                    addRemovedLabel(nodeWrapper, nodeWrapper.getSession().getWorkspace().getName() + "_removed_at_" + JCRVersionService.DATE_FORMAT.print(calendar.getTime().getTime()));
                    nodeWrapper.remove();
                } catch (InvalidItemStateException e) {
                    logger.warn("Already deleted : " + nodeWrapper.getPath());
                }
            }
            for (String s : toDelete) {
                try {
                    JCRNodeWrapper node = destinationSession.getNodeByIdentifier(s);
                    addRemovedLabel(node, node.getSession().getWorkspace().getName() + "_removed_at_" + JCRVersionService.DATE_FORMAT.print(calendar.getTime().getTime()));
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
                    CloneResult cloneResult = ensureNodeInDestinationWorkspace(sourceNode, destinationSession,
                            toCheckpoint);
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
        } catch (RepositoryException e) {
            // Restore previous status
            for (Map.Entry<String, Value> entry : previousValue.entrySet()) {
                try {
                    ((JCRPropertyWrapper) sourceSession.getItem(entry.getKey())).setValue(entry.getValue());
                } catch (PathNotFoundException ex) {
                    // node is removed during publication
                }
            }
            if (sourceSession.hasPendingChanges()) {
                sourceSession.save();
            }
            throw e;
        } finally {
            JCRObservationManager.setEventListenersAvailableDuringPublishOnly(null);
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
        // Refresh this user system sessions after publication
        sourceSession.refresh(false);
        destinationSession.refresh(false);
    }

    private CloneResult ensureNodeInDestinationWorkspace(final JCRNodeWrapper node,
                                                         JCRSessionWrapper destinationSession, final Set<JCRNodeWrapper> toCheckpoint) throws AccessDeniedException,
            NoSuchWorkspaceException, RepositoryException {
        if (!destinationSession.isSystem()) {
            final String nodePath = node.getPath();
            final String destinationWorkspace = destinationSession.getWorkspace().getName();
            return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(node.getUser().getJahiaUser(),
                    node.getSession().getWorkspace().getName(), null, new JCRCallback<CloneResult>() {
                        public CloneResult doInJCR(final JCRSessionWrapper sourceSession)
                                throws RepositoryException {
                            return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(
                                    node.getUser().getJahiaUser(), destinationWorkspace, null,
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
                                    }
                            );
                        }
                    }
            );
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

    private void mergeToDestinationWorkspace(final Set<JCRNodeWrapper> toPublish, final Set<String> uuids,
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
                conflictResolver.setToCheckpoint(toCheckpoint);
                conflictResolver.applyDifferences();
                continue;
            }

            final String path = node.getPath();
            String destinationPath =
                    null;
            try {
                destinationPath = node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
            } catch (ItemNotFoundException e) {
                return;
            }

            // Item exists at "destinationPath" in live space, update it

            JCRNodeWrapper destinationNode = destinationSession
                    .getNode(destinationPath); // Live node exists - merge live node from source space

            destinationSession.checkout(destinationNode);

            final String oldPath = handleSharedMove(sourceSession, node, node.getPath());

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Merge node : " + path + " source v=" + node.getBaseVersion().getName() + " , dest node v=" +
                                destinationNode.getBaseVersion().getName()
                );
            }
            String expectedDestinationPath = null;
            try {
                String parentDestinationPath = node.getParent().getPath().equals("/") ? "" : node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                expectedDestinationPath = parentDestinationPath + "/" + node.getName();
            } catch (ItemNotFoundException e) {
                // Not found - parent is not yet published
            }
            if (expectedDestinationPath == null || !expectedDestinationPath.equals(destinationPath)) {
                try {
                    destinationSession.checkout(destinationNode.getParent()); // previous parent
                    JCRNodeWrapper nodeParent = node.getParent();
                    String newParentPath = null;
                    try {
                        newParentPath = nodeParent.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                    } catch (ItemNotFoundException e) {
                        ensureNodeInDestinationWorkspace(nodeParent, destinationSession, toCheckpoint);
                        newParentPath = nodeParent.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                    }
                    JCRNodeWrapper destinationNewParent = destinationSession.getNode(newParentPath);
                    destinationSession.checkout(destinationNewParent); // new parent
                    recurseCheckout(destinationNode, null, destinationSession); // node and sub nodes

                    String newDestinationPath = newParentPath + "/" + node.getName();
                    destinationSession.move(destinationPath, newDestinationPath);
                    destinationSession.save();

                    destinationPath = newDestinationPath;
                    destinationNode = destinationSession.getNode(destinationPath);

                    if (destinationNewParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                        NodeIterator ni = node.getParent().getNodes();
                        boolean found = false;
                        while (ni.hasNext()) {
                            JCRNodeWrapper currentNode = (JCRNodeWrapper) ni.next();
                            if (!found && currentNode.getIdentifier().equals(node.getIdentifier())) {
                                found = true;
                            } else if (found) {
                                try {
                                    destinationSession.getNode(newParentPath + "/" + currentNode.getName());
                                    destinationNewParent.orderBefore(node.getName(), currentNode.getName());
                                    destinationNewParent.getSession().save();
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
                    recurseCheckout(snode, null, destinationSession);
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
            if (!parentCloneResult.includedUuids.contains(sourceNode.getParent().getIdentifier())) {
                return cloneResult;
            }
            cloneResult.includedUuids.addAll(parentCloneResult.includedUuids);
            destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
        }

        JCRNodeWrapper destinationParent = destinationSession.getNode(destinationParentPath);
        if (destinationParent.hasNode(sourceNode.getName())) {
            logger.error("Node " + sourceNode.getName() + " is in conflict, already exist under " + destinationParent.getPath() +
                    " - cannot publish !");
            return cloneResult;
        }

        try {
            Set<String> deniedPaths = new HashSet<String>();
            Set<String> included = new HashSet<String>();
            getDeniedPath(sourceNode, deniedPaths, included);
            cloneResult.includedUuids.addAll(included);
            JahiaAccessManager.setDeniedPaths(deniedPaths);

            destinationSession.checkout(destinationParent);
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
                destinationSession.checkout(destinationSession.getNodeByIdentifier(sourceNode.getIdentifier()));
                destinationSession.checkout(destinationParent); // new parent
                recurseCheckout(destinationSession.getNode(correspondingNodePath), null,
                        destinationSession); // node and sub nodes

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
                if (sourceNode.getDefinition().getDeclaringNodeType().isMixin() && !destinationParent.isNodeType(sourceNode.getDefinition().getDeclaringNodeType().getName())) {
                    destinationParent.addMixin(sourceNode.getDefinition().getDeclaringNodeType().getName());
                    destinationSession.save();
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
            if (nodeWrapper.isVersioned() || nodeWrapper.isNodeType("jmix:nolive") || sourceNode.getProvider() != nodeWrapper.getProvider()) {
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
                sourceSession.checkout(sourceNode);
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

    private void recurseCheckout(Node node, List<String> prune, JCRSessionWrapper session)
            throws RepositoryException {
        session.checkout(node);
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            if (prune == null || !prune.contains(sub.getIdentifier())) {
                recurseCheckout(sub, prune, session);
            }
        }
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param uuids uuids of the node to unpublish
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(final List<String> uuids) throws RepositoryException {
        unpublish(uuids, true);
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param uuids uuids of the node to unpublish
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(final List<String> uuids, boolean checkPermissions) throws RepositoryException {
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();

        final List<String> checkedUuids = new ArrayList<String>();
        if (checkPermissions) {
            JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
            for (String uuid : uuids) {
                if (uuid != null && session.getNodeByIdentifier(uuid).hasPermission("publish")) {
                    checkedUuids.add(uuid);
                }
            }
        } else {
            checkedUuids.addAll(uuids);
        }

        JCRCallback<Object> callback = new JCRCallback<Object>() {
            public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                for (ListIterator<String> it = checkedUuids.listIterator(checkedUuids.size()); it.hasPrevious(); ) {
                    String uuid = it.previous();
                    if (uuid != null) {
                        JCRNodeWrapper destNode = sourceSession.getNodeByIdentifier(uuid);
                        destNode.setProperty("j:published", false);
                        boolean doLogging = loggingService.isEnabled();
                        if (doLogging) {
                            Integer operationType = JCRObservationManager.getCurrentOperationType();
                            if (operationType != null && operationType == JCRObservationManager.IMPORT) {
                                doLogging = false;
                            }
                        }
                        if (doLogging) {
                            String userID = destNode.getSession().getUserID();
                            if ((userID != null) && (userID.startsWith(JahiaLoginModule.SYSTEM))) {
                                userID = userID.substring(JahiaLoginModule.SYSTEM.length());
                            }
                            loggingService.logContentEvent(userID, "", "", destNode.getIdentifier(), destNode.getPath(),
                                    destNode.getPrimaryNodeTypeName(), "unpublishedNode",
                                    destNode.getSession().getWorkspace().getName());
                        }
                    }
                }
                sourceSession.save();
                return null;
            }
        };
        JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, EDIT_WORKSPACE, null, callback);

        JCRObservationManager.setEventListenersAvailableDuringPublishOnly(Boolean.TRUE);
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user, LIVE_WORKSPACE, null, callback);
        } finally {
            JCRObservationManager.setEventListenersAvailableDuringPublishOnly(Boolean.FALSE);
        }
    }

    public List<PublicationInfo> getPublicationInfos(List<String> uuids, Set<String> languages,
                                                     boolean includesReferences, boolean includesSubnodes,
                                                     boolean allsubtree, final String sourceWorkspace,
                                                     final String destinationWorkspace) throws RepositoryException {
        List<PublicationInfo> infos = new ArrayList<PublicationInfo>();

        Set<String> allUuids = new LinkedHashSet<String>();

        for (String uuid : uuids) {
            if (!allUuids.contains(uuid)) {
                final List<PublicationInfo> publicationInfos =
                        getPublicationInfo(uuid, languages, includesReferences, includesSubnodes, allsubtree,
                                sourceWorkspace, destinationWorkspace);
                for (PublicationInfo publicationInfo : publicationInfos) {
                    infos.add(publicationInfo);
                    allUuids.addAll(publicationInfo.getAllUuids());
                }
            }
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
        tree.setRoot(getPublicationInfo(stageNode, languages, includesReferences, includesSubnodes, allsubtree,
                sourceSession, destinationSession, new HashMap<String, PublicationInfoNode>(), infos, tree));
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
     * @param sourceSession      source session (default workspace)
     * @param destinationSession destination session (live workspace)
     * @param infosMap           a Set of uuids, which don't need to be checked or have already been checked
     * @param infos              contains all publication infos
     * @param currentPublicationInfo processed publicationInfo
     * @return the <code>PublicationInfo</code> for the requested node(s)
     * @throws RepositoryException
     */
    private PublicationInfoNode getPublicationInfo(JCRNodeWrapper node, Set<String> languages,
                                                   boolean includesReferences, boolean includesSubnodes,
                                                   boolean allsubtree, final JCRSessionWrapper sourceSession,
                                                   final JCRSessionWrapper destinationSession,
                                                   Map<String, PublicationInfoNode> infosMap,
                                                   List<PublicationInfo> infos, PublicationInfo currentPublicationInfo) throws
            RepositoryException {

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
                            if (logger.isDebugEnabled()) {
                                logger.debug("Cannot find deleted subnode of " + node.getPath() + " : " + value.getString() + ", we keep the reference until next publication to be sure to erase it from the live workspace.");
                            }
                        }
                    }
                } catch (PathNotFoundException e) {
                    logger.warn("property j:deletedChildren has been found on node " + node.getPath() + " but was not here");
                }
            }

            if(node.hasProperty(Constants.WORKINPROGRESS) && node.getProperty(Constants.WORKINPROGRESS).getBoolean()
                    && !node.isMarkedForDeletion() && (!node.isNodeType(Constants.JAHIANT_TRANSLATION) || !node.getParent().isMarkedForDeletion())) {
                info.setWorkInProgress(true);
            }

            info.setStatus(getStatus(node, destinationSession, languages, infosMap.keySet()));

            if (allsubtree) {
                if (currentPublicationInfo.hasLiveNode() == null) {
                    try {
                        node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                        currentPublicationInfo.setHasLiveNode(true);
                    } catch (ItemNotFoundException e) {
                        currentPublicationInfo.setHasLiveNode(false);
                    }
                }

                if (info.getStatus() == PublicationInfo.PUBLISHED && !currentPublicationInfo.hasLiveNode()) {
                    info.setStatus(PublicationInfo.NOT_PUBLISHED);
                }
            }
            // If in conflict we still need to have the translation nodes has they are part of the node to make it valid
            // in case we manage to resolve the conflict on publication
            if (info.getStatus() == PublicationInfo.CONFLICT) {
                return info;
            }
            if (node.hasProperty("j:lockTypes")) {
                try {
                    Value[] lockTypes = node.getProperty("j:lockTypes").getValues();
                    for (Value lockType : lockTypes) {
                        if (lockType.getString().endsWith(":validation")) {
                            info.setLocked(true);
                        }
                    }
                } catch (PathNotFoundException e) {
                    // Property has been removed
                }
            }
        }
        if (info.getStatus() == PublicationInfo.CONFLICT) {
            return info;
        }
        if (includesReferences || includesSubnodes) {
            if (includesReferences) {
                getReferences(node, languages, includesReferences, includesSubnodes, sourceSession, destinationSession,
                        infosMap, infos, info);
            }
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();

                if (!supportsPublication(sourceSession, n)) continue;

                if (info.getStatus() == PublicationInfo.MARKED_FOR_DELETION || info.getStatus() == PublicationInfo.DELETED) {
                    info.addChild(getPublicationInfo(n, languages, includesReferences, true, true,
                            sourceSession, destinationSession, infosMap, infos, currentPublicationInfo));
                } else if (languages != null && n.isNodeType("jnt:translation")) {
                    String translationLanguage = n.getProperty("jcr:language").getString();
                    if (languages.contains(translationLanguage)) {
                        PublicationInfoNode child =
                                getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                                        sourceSession, destinationSession, infosMap, infos, currentPublicationInfo);
                        info.addChild(child);
                        if(child.isWorkInProgress()) {
                            info.getChildren().clear();
                            info.getReferences().clear();
                            info.addChild(child);
                            info.setWorkInProgress(true);
                            break;
                        }
                    }
                } else {
                    boolean hasIndependantPublication = hasIndependantPublication(n);
                    if (allsubtree && hasIndependantPublication) {
                        PublicationInfo newinfo = new PublicationInfo();
                        infos.add(newinfo);
                        newinfo.setRoot(getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree, sourceSession,
                                destinationSession, infosMap, infos, newinfo));
                    }
                    if (!hasIndependantPublication) {
                        if (n.isNodeType("jmix:lastPublished")) {
                            info.addChild(getPublicationInfo(n, languages, includesReferences, includesSubnodes, allsubtree,
                                    sourceSession, destinationSession, infosMap, infos, currentPublicationInfo));
                        } else if (includesReferences) {
                            getReferences(n, languages, includesReferences, includesSubnodes, sourceSession, destinationSession, infosMap,
                                    infos, info);
                        }
                    }
                }
            }
        }
        return info;
    }

    /**
     * Get the publication status of a specific node
     */
    public int getStatus(JCRNodeWrapper node, JCRSessionWrapper destinationSession, Set<String> languages) throws RepositoryException {
        return getStatus(node, destinationSession, languages, Collections.<String>emptySet());
    }

    /**
     * Get the publication status of a specific node, taking into account nodes being published at the same time (used for conflict detection)
     */
    public int getStatus(JCRNodeWrapper node, JCRSessionWrapper destinationSession, Set<String> languages, Set<String> includedUuids) throws RepositoryException {
        int status;
        if (!node.checkLanguageValidity(languages)) {
            status = PublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE;
            for (String language : languages) {
                Locale locale = LanguageCodeConverters.getLocaleFromCode(language);
                if (node.checkI18nAndMandatoryPropertiesForLocale(locale)) {
                    status = PublicationInfo.MANDATORY_LANGUAGE_VALID;
                }
            }
        } else if (!node.hasProperty("j:published")) {
            // Node has never been published, check for potential conflict in live
            if (checkConflict(node, destinationSession, includedUuids) == PublicationInfo.CONFLICT) {
                return PublicationInfo.CONFLICT;
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
        } else if (node.hasProperty("j:published") && !node.getProperty("j:published").getBoolean()) {
            status = PublicationInfo.UNPUBLISHED;
        } else {
            if (node.hasProperty("jcr:mergeFailed")) {
                status = PublicationInfo.CONFLICT;
            } else if (node.getLastModifiedAsDate() == null) {
                // No modification date - node is published
                status = PublicationInfo.PUBLISHED;
            } else {
                if (node.isNodeType("jmix:markedForDeletion")) {
                    status = PublicationInfo.MARKED_FOR_DELETION;
                } else {
                    Date pubProp = node.getLastPublishedAsDate();
                    if (pubProp == null) {
                        destinationSession.getNodeByUUID(node.getIdentifier()).getLastModifiedAsDate();
                    }
                    Date modProp = pubProp != null ? node.getLastModifiedAsDate() : null;
                    if (modProp == null) {
                        logger.debug(
                                "Unable to check publication status for node {}."
                                        + " One of properties [last published or last modified (live) / last modified] is null."
                                        + " Considering node as modified.", node.getPath()
                        );
                        status = PublicationInfo.NOT_PUBLISHED;
                    } else {
                        if (modProp.after(pubProp)) {
                            if (node.hasProperty(FULLPATH) && !node.getCanonicalPath().equals(node.getProperty(FULLPATH).getString())) {
                                // Check conflict in case of renamed / moved node
                                if (checkConflict(node, destinationSession, includedUuids) == PublicationInfo.CONFLICT) {
                                    return PublicationInfo.CONFLICT;
                                }
                            }
                            status = PublicationInfo.MODIFIED;
                        } else {
                            status = PublicationInfo.PUBLISHED;
                        }
                    }
                }
            }
        }
        return status;
    }

    private int checkConflict(JCRNodeWrapper node, JCRSessionWrapper destinationSession, Set<String> includedUuids) throws RepositoryException {
        try {
            try {
                JCRNodeWrapper parent = node.getParent();
                JCRNodeWrapper n = destinationSession.getNodeByIdentifier(parent.getIdentifier()).getNode(node.getName());
                if (n.getIdentifier().equals(node.getIdentifier())) {
                    return 0;
                } else if (includedUuids.contains(parent.getIdentifier()) && parent.hasProperty("j:deletedChildren")) {
                    //find if the live node has to be deleted
                    JCRPropertyWrapper p = parent.getProperty("j:deletedChildren");
                    Value[] values = p.getValues();
                    for (Value value : values) {
                        if(n.getIdentifier().equals(value.getString())) {
                            return 0;
                        }
                    }
                }
            } catch (UnsupportedRepositoryOperationException e) {
            }
            // Conflict , a node exists in live that has not been deleted properly in default, or has just been created in live!
            return PublicationInfo.CONFLICT;
        } catch (ItemNotFoundException e) {
        } catch (PathNotFoundException e) {
        }
        return 0;
    }

    private void getReferences(JCRNodeWrapper node, Set<String> languages, boolean includesReferences,
                               boolean includesSubnodes, JCRSessionWrapper sourceSession,
                               JCRSessionWrapper destinationSession, Map<String, PublicationInfoNode> infosMap,
                               List<PublicationInfo> infos, PublicationInfoNode info) throws RepositoryException {
        if (skipAllReferenceProperties) {
            return;
        }
        List<ExtendedPropertyDefinition> defs = node.getReferenceProperties();
        for (ExtendedPropertyDefinition def : defs) {
            String propName = def.getName();
            if (propertiesToSkipForReferences.contains(propName) || propName.startsWith("jcr:")
                    || !node.hasProperty(propName)) {
                continue;
            }
            Property p = node.getProperty(def.getName());
            if (def.isMultiple()) {
                Value[] vs = p.getValues();
                for (Value v : vs) {
                    try {
                        JCRNodeWrapper ref = node.getSession().getNodeByUUID(v.getString());
                        if (!skipReferencedNodeType(ref) && supportsPublication(sourceSession, ref)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Calculating publication status for the reference property {}", propName);
                            }
                            PublicationInfo refInfo = new PublicationInfo();
                            info.addReference(refInfo);
                            refInfo.setRoot(getPublicationInfo(ref, languages, includesReferences,
                                    includesSubnodes, false, sourceSession, destinationSession, infosMap, infos, refInfo));
                        }
                    } catch (ItemNotFoundException e) {
                        if (def.getRequiredType() == PropertyType.REFERENCE) {
                            logger.warn("Cannot get reference " + p.getName() + " = " + v.getString() + " from node "
                                    + node.getPath());
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Cannot get reference " + p.getName() + " = " + v.getString()
                                        + " from node " + node.getPath());
                            }
                        }

                    }
                }
            } else {
                try {
                    JCRNodeWrapper ref = (JCRNodeWrapper) p.getNode();

                    if (!supportsPublication(sourceSession, ref)) continue;

                    if (!skipReferencedNodeType(ref)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Calculating publication status for the reference property {}", propName);
                        }
                        PublicationInfo refInfo = new PublicationInfo();
                        info.addReference(refInfo);
                        refInfo.setRoot(getPublicationInfo(ref, languages, includesReferences,
                                includesSubnodes, false, sourceSession, destinationSession, infosMap, infos, refInfo));
                    }
                } catch (ItemNotFoundException e) {
                    if (def.getRequiredType() == PropertyType.REFERENCE) {
                        logger.warn("Cannot get reference " + p.getName() + " = " + p.getString() + " from node "
                                + node.getPath());
                    } else {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Cannot get reference " + p.getName() + " = " + p.getString() + " from node "
                                    + node.getPath());
                        }
                    }
                }
            }
        }
    }

    public static boolean supportsPublication(JCRSessionWrapper sourceSession, JCRNodeWrapper node)
            throws RepositoryException {
        JCRStoreProvider provider = node.getProvider();
        if (provider.isDefault()) {
            return true;
        }
        Value workspaceManagement = sourceSession.getProviderSession(node.getProvider()).getRepository()
                .getDescriptorValue(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED);
        if (workspaceManagement == null) {
            return false;
        }
        Value writeSupported = node.getSession().getProviderSession(node.getProvider()).getRepository()
                .getDescriptorValue(Repository.WRITE_SUPPORTED);
        if (writeSupported == null) {
            return false;
        }
        return workspaceManagement.getBoolean() && writeSupported.getBoolean();
    }

    private boolean skipReferencedNodeType(JCRNodeWrapper ref) throws RepositoryException {
        for (String ntName : referencedNodeTypesToSkip) {
            if (ref.isNodeType(ntName)) {
                return true;
            }
        }
        return false;
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

    public void setPropertiesToSkipForReferences(String propertiesToSkipForReferences) {
        this.propertiesToSkipForReferences = tokenize(propertiesToSkipForReferences);
        this.skipAllReferenceProperties = propertiesToSkipForReferences.contains(".*");
    }

    public void setReferencedNodeTypesToSkip(String referencedNodeTypesToSkip) {
        this.referencedNodeTypesToSkip = tokenize(referencedNodeTypesToSkip);
    }

    public void addReferencedNodeTypesToSkip(String referencedNodeTypesToSkip) {
        this.referencedNodeTypesToSkip.addAll(tokenize(referencedNodeTypesToSkip));
    }

    public void addPropertiesToSkipForReferences(String propertiesToSkipForReferences) {
        this.propertiesToSkipForReferences.addAll(tokenize(propertiesToSkipForReferences));
    }

    private static Set<String> tokenize(String input) {
        Set<String> result;
        String[] tokens = StringUtils.split(input, " ,");
        if (tokens == null || tokens.length == 0) {
            result = Collections.emptySet();
        } else {
            result = new LinkedHashSet<String>();
            result.addAll(Arrays.asList(tokens));
        }

        return result;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

}