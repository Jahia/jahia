package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.usermanager.JahiaUser;

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
    private static transient Logger logger = Logger.getLogger(JCRPublicationService.class);
    private JCRSessionFactory sessionFactory;
    private JCRVersionService jcrVersionService;
    private static JCRPublicationService instance;

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

    public void lockForPublication(final List<PublicationInfo> publicationInfo, final String workspace, final String key) throws RepositoryException {
        JCRTemplate.getInstance().doExecute(true, getSessionFactory().getCurrentUserSession(workspace).getUser().getUsername(), workspace, null,new JCRCallback() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                for (PublicationInfo info : publicationInfo) {
                    doLock(info, session, key);
                }
                return null;
            }
        });
    }

    private void doLock(PublicationInfo publicationInfo, JCRSessionWrapper session, String key) throws RepositoryException {
        JCRNodeWrapper node = session.getNodeByUUID(publicationInfo.getRoot().getUuid());
        if (!node.isNodeType("jmix:publication")) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            node.addMixin("jmix:publication");
            session.save();
        }

        for (String uuid : publicationInfo.getAllUuids()) {
            node = session.getNodeByUUID(uuid);
            if (node.isLockable()) {
                node.lockAndStoreToken(key);
            }
        }
        for (PublicationInfo tree : publicationInfo.getAllReferences()) {
            for (String uuid : tree.getAllUuids()) {
                node = session.getNodeByUUID(uuid);
                if (node.isLockable()) {
                    node.lockAndStoreToken(key);
                }
            }
        }
    }

    public void unlockForPublication(final List<PublicationInfo> publicationInfo, final String workspace, final String key) throws RepositoryException {
//        final List<String> toRelock = new ArrayList<String>();
        JCRTemplate.getInstance().doExecute(true, getSessionFactory().getCurrentUserSession(workspace).getUser().getUsername(), workspace, null,new JCRCallback() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
//                Collections.reverse(toUnlock);
                for (PublicationInfo info : publicationInfo) {
                    doUnlock(info, session, key);
                }
                return null;
            }
        });
//        return toRelock;
    }

    private void doUnlock(PublicationInfo publicationInfo, JCRSessionWrapper session, String key) throws RepositoryException {
        List<String> allUuids = publicationInfo.getAllUuids();
        Collections.reverse(allUuids);
        for (String uuid : allUuids) {
            JCRNodeWrapper node = session.getNodeByUUID(uuid);
            if (node.isLocked()) {
                try {
                    node.unlock(key);
                } catch (LockException e) {
                }
            }
        }
        for (PublicationInfo tree : publicationInfo.getAllReferences()) {
            allUuids = tree.getAllUuids();
            Collections.reverse(allUuids);
            for (String uuid : allUuids) {
                JCRNodeWrapper node = session.getNodeByUUID(uuid);
                if (node.isLocked()) {
                    try {
                        node.unlock(key);
                    } catch (LockException e) {
                    }
                }
            }
        }
    }

    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param path                 Path of the node to publish
     * @param sourceWorkspace      the source workspace of the publication
     * @param destinationWorkspace the destination workspace of the publication
     * @param languages            set of languages you wish to publish
     * @param allSubTree
     * @throws javax.jcr.RepositoryException in case of error
     */
    public void publish(final String uuid, final String sourceWorkspace, final String destinationWorkspace, final Set<String> languages,
                        final boolean allSubTree) throws RepositoryException {
        if (sessionFactory.getCurrentUser() == null) {
            return;
        }
        List<PublicationInfo> tree = getPublicationInfo(uuid, languages, true, true, allSubTree, sourceWorkspace, destinationWorkspace);
        publish(tree, sourceWorkspace, destinationWorkspace);
    }

    public void publish(final List<PublicationInfo> publicationInfos, final String sourceWorkspace, final String destinationWorkspace) throws RepositoryException {
        final String username;
        final JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
        if (user != null) {
            username = user.getUsername();
        } else {
            username = null;
        }
        JCRTemplate.getInstance().doExecute(true, username, sourceWorkspace, null,  new JCRCallback() {
            public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                JCRTemplate.getInstance().doExecute(true, username, destinationWorkspace, new JCRCallback() {
                    public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                        for (PublicationInfo publicationInfo : publicationInfos) {
                            JCRNodeWrapper n = sourceSession.getNodeByUUID(publicationInfo.getRoot().getUuid());

                            for (ExtendedNodeType type : n.getMixinNodeTypes()) {
                                if (type.getName().equals("jmix:publication")) {
                                    if (!n.isCheckedOut()) {
                                        n.checkout();
                                    }
                                    n.removeMixin("jmix:publication");
                                    sourceSession.save();
                                }
                            }

                            publish(publicationInfo, sourceSession, destinationSession, new HashSet<String>());
                        }
                        return null;
                    }
                });

                return null;
            }
        });
    }

    private void publish(final PublicationInfo publicationInfo, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession, Set<String> uuids) throws RepositoryException {
        final Calendar calendar = new GregorianCalendar();
        uuids.add(publicationInfo.getRoot().getUuid());

        final JCRNodeWrapper sourceNode = sourceSession.getNodeByUUID(publicationInfo.getRoot().getUuid());

        final String destinationWorkspace = destinationSession.getWorkspace().getName();

        try {
            sourceNode.getParent().getCorrespondingNodePath(destinationWorkspace);
        } catch (ItemNotFoundException e) {
            if (!destinationSession.isSystem()) {
                final String parentPath = sourceNode.getParent().getPath();
                JCRTemplate.getInstance().doExecute(true, sourceNode.getUser().getUsername(), sourceNode.getSession().getWorkspace().getName(), null,  new JCRCallback() {
                    public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                        return JCRTemplate.getInstance().doExecute(true, sourceNode.getUser().getUsername(), destinationWorkspace, new JCRCallback() {
                            public Object doInJCR(final JCRSessionWrapper destinationSession) throws RepositoryException {
                                cloneParents(sourceSession.getNode(parentPath), sourceSession, destinationSession, calendar);
                                sourceSession.save();
                                destinationSession.save();
                                return null;
                            }
                        });
                    }
                });
            } else {
                cloneParents(sourceNode.getParent(), sourceNode.getSession(), destinationSession, calendar);
            }
        }

        final List<String> uuidsToPublish = publicationInfo.getAllUuids();

        for (PublicationInfo subtree : publicationInfo.getAllReferences()) {
            try {
                if (!uuids.contains(subtree.getRoot().getUuid()) && !uuidsToPublish.contains(subtree.getRoot().getUuid()) ) {
                    publish(subtree, sourceSession, destinationSession, uuids);
                }
            } catch (Exception e) {
                logger.warn("Cannot publish node at : " + subtree.getRoot().getUuid(),e);
            }
        }

        List<JCRNodeWrapper> toPublish = new ArrayList<JCRNodeWrapper>();
        for (String uuid : uuidsToPublish) {
            toPublish.add(sourceSession.getNodeByUUID(uuid));
        }

        if (destinationSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
            for (JCRNodeWrapper jcrNodeWrapper : toPublish) {
                if (!jcrNodeWrapper.hasProperty("j:published") || !jcrNodeWrapper.getProperty("j:published").getBoolean()) {
                    if (!jcrNodeWrapper.isCheckedOut()) {
                        jcrNodeWrapper.checkout();
                    }
                    jcrNodeWrapper.setProperty("j:published", Boolean.TRUE);
                    try {
                        JCRNodeWrapper destNode = destinationSession.getNode(jcrNodeWrapper.getCorrespondingNodePath(destinationWorkspace));
                        if (!destNode.isCheckedOut()) {
                            destNode.checkout();
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

        try {
            JCRNodeWrapper destNode = destinationSession.getNode(sourceNode.getPath());
//            ArrayList<JCRNodeWrapper> pruneDestNodes = new ArrayList<JCRNodeWrapper>();
//            getBlockedAndReferencesList(destNode, new ArrayList<JCRNodeWrapper>(), pruneDestNodes, new ArrayList<JCRNodeWrapper>(), languages, allSubTree);
//            final List<String> prunedDestPath = new ArrayList<String>();
//            for (JCRNodeWrapper node : pruneDestNodes) {
//                prunedDestPath.add(node.getPath());
//            }

            mergeToDestinationWorkspace(toPublish, uuidsToPublish, sourceNode.getSession(), destinationSession, calendar);
        } catch (PathNotFoundException e) {
            cloneToDestinationWorkspace(toPublish.iterator().next(), uuidsToPublish, sourceNode.getSession(), destinationSession, calendar);
        }
    }

    private void cloneParents(JCRNodeWrapper node, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession, Calendar calendar) throws RepositoryException {
        String path;
        try {
            path = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        } catch (ItemNotFoundException e) {
            cloneParents(node.getParent(), sourceSession, destinationSession, calendar);
            path = node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        }
        JCRNodeWrapper destinationNode = doClone(node, null, sourceSession, destinationSession);
        checkin(destinationSession, destinationSession.getNode(path), destinationSession.getWorkspace().getVersionManager(), calendar);
    }

    void mergeToDestinationWorkspace(final List<JCRNodeWrapper> toPublish, final List<String> uuidsToPublish,final JCRSessionWrapper sourceSession, final JCRSessionWrapper destinationSession, Calendar calendar) throws RepositoryException {
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();
        final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

        List<JCRNodeWrapper> modified = new ArrayList<JCRNodeWrapper>();
        for (final JCRNodeWrapper node : toPublish) {
            if (node.hasProperty("jcr:mergeFailed")) {
                Value[] failed = node.getProperty("jcr:mergeFailed").getValues();

                for (Value value : failed) {
                    System.out.println("-- failed merge waiting : "+node.getPath() + " / "+value.getString());
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
            logger.debug("Setting last published : "+node.getPath());
//            if (!sourceSession.getWorkspace().getName().equals("live")) {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }
                node.setProperty("j:lastPublished", calendar);
                node.setProperty("j:lastPublishedBy", destinationSession.getUserID());

            jcrVersionService.setNodeCheckinDate(node, calendar);
//            }
        }

        sourceSession.save();

        for (JCRNodeWrapper node : modified) {
            // Node has been modified, check in now
            sourceVersionManager.checkin(node.getPath());
        }

        for (final JCRNodeWrapper node : modified) {
            try {
                final String path = node.getPath();
                final String destinationPath = node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());

                // Item exists at "destinationPath" in live space, update it

                JCRNodeWrapper destinationNode = destinationSession.getNode(destinationPath); // Live node exists - merge live node from source space

                // force conflict
                destinationNode.checkout();

                final String oldPath = handleSharedMove(sourceSession, node, node.getPath());

                logger.info("Merge node : " + path + " source v=" + node.getBaseVersion().getName() +
                        " , dest node v=" + destinationSession.getNode(destinationPath).getBaseVersion().getName());

//                recurseCheckin(destinationSession.getNode(destinationPath), pruneNodes, destinationVersionManager);
                if (destinationNode.isNodeType("mix:versionable") && destinationNode.isCheckedOut() && !destinationNode.hasProperty("jcr:mergeFailed")) {
                    destinationVersionManager.checkin(destinationPath);
                }
                destinationSession.save();
                NodeIterator ni = destinationVersionManager.merge(destinationPath, node.getSession().getWorkspace().getName(), true, true);

                if (ni.hasNext()) {
                    while (ni.hasNext()) {
                        Node failed = ni.nextNode();
                        destinationVersionManager.checkout(failed.getPath());

                        JCRNodeWrapper destNode = destinationSession.getNode(failed.getPath());

                        ConflictResolver resolver = new ConflictResolver(node, destNode);
                        resolver.setUuidsToPublish(uuidsToPublish);
                        try {
                            resolver.applyDifferences();

                            if (!resolver.getUnresolvedDifferences().isEmpty()) {
                                logger.warn("Unresolved conflicts : "+resolver.getUnresolvedDifferences());
                            } else {
                                destinationVersionManager.doneMerge(failed.getPath(), sourceVersionManager.getBaseVersion(path));
                            }
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                    }
//                    if (!sourceSession.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE)) {
                    recurseCheckin(destinationSession, destinationNode, uuidsToPublish, destinationVersionManager, calendar);
//                        node.update(destinationSession.getWorkspace().getName()); // do not update live in reverse publish
//                    }
                }

                if (oldPath != null) {
                    try {
                        JCRNodeWrapper snode = destinationSession.getNode(oldPath);
                        recurseCheckout(snode,null,destinationVersionManager);
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

                logger.info("Merge node end : " + path + " source v=" + sourceSession.getNode(path).getBaseVersion().getName() +
                        " , dest node v=" + destinationSession.getNode(destinationPath).getBaseVersion().getName());


            } catch (ItemNotFoundException e) {
                // Item does not exist yet in live space
                JCRNodeWrapper destinationNode = doClone(node, uuidsToPublish, sourceSession, destinationSession);
                destinationVersionManager.checkin(node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName()));
                recurseCheckin(destinationSession, destinationNode, null, destinationVersionManager, calendar);
            }
        }
    }

    void cloneToDestinationWorkspace(JCRNodeWrapper sourceNode, List<String> uuidsToPublish, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession, Calendar calendar) throws RepositoryException {
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();

        recurseSetPublicationDate(sourceNode, uuidsToPublish, calendar, sourceSession.getUserID());
        sourceSession.save();
        checkin(sourceSession, sourceNode, sourceVersionManager, calendar);
        recurseCheckin(sourceSession, sourceNode, uuidsToPublish, sourceVersionManager, calendar);

        JCRNodeWrapper destinationNode = doClone(sourceNode, uuidsToPublish, sourceSession, destinationSession);

        VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
        if (sourceNode.getParent().isVersioned()) {
            destinationVersionManager.checkin(sourceNode.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName()));
        }
        recurseCheckin(destinationSession, destinationNode, null, destinationVersionManager, calendar);
    }

    JCRNodeWrapper doClone(JCRNodeWrapper sourceNode, List<String> uuidsToPublish, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper parent = sourceNode.getParent();
//                destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);

        final String destinationWorkspaceName = destinationSession.getWorkspace().getName();
        final String destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
        final String sourceNodePath = sourceNode.getIndex() > 1 ? sourceNode.getPath() + "[" + sourceNode.getIndex() + "]": sourceNode.getPath();
        logger.info("Cloning node : " + sourceNodePath);

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
                if (sourceNode.isNodeType("mix:shareable")) {
                    // Shareable node - todo : check if we need to move or clone

                    String oldPath = handleSharedMove(sourceSession, sourceNode, destinationPath);

                    // Clone the node node in live space
                    destinationSession.getWorkspace().clone(destinationWorkspaceName, correspondingNodePath, destinationPath, false);

                    if (oldPath != null) {
                        try {
                            JCRNodeWrapper node = destinationSession.getNode(oldPath);
                            recurseCheckout(node,null,destinationVersionManager);
                            JCRNodeWrapper oldParent = node.getParent();
                            oldParent.checkout();
                            node.remove();
                            node.getSession().save();
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Non shareable node has been moved
                    destinationVersionManager.checkout(StringUtils.substringBeforeLast(correspondingNodePath, "/")); // previous parent
                    destinationVersionManager.checkout(destinationParentPath); // new parent
                    recurseCheckout(destinationSession.getNode(correspondingNodePath), null, destinationVersionManager); // node and sub nodes

                    destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNodePath, destinationPath, true);
//                    destinationVersionManager.checkin(destinationParentPath);
                }
            } catch (ItemNotFoundException e) {
                destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNodePath, destinationPath, false);
            }
            JCRNodeWrapper destinationParent = destinationSession.getNode(destinationParentPath);
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
        JCRNodeWrapper destinationNode = destinationSession.getNode(sourceNode.getCorrespondingNodePath(destinationWorkspaceName));
        if (uuidsToPublish != null && sourceNode.getNodes().hasNext()) {
            NodeIterator it = sourceNode.getNodes();
            while (it.hasNext()) {
                JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) it.next();
                if (uuidsToPublish.contains(nodeWrapper.getIdentifier()) && nodeWrapper.isVersioned()) {
                    try {
                        // Check if the child has a corresponding path - if not, clone it
                        nodeWrapper.getCorrespondingNodePath(destinationWorkspaceName);
                        // Check if parent node has a child with that name - if not, clone it (shareable)
                        if (!destinationNode.hasNode(nodeWrapper.getName())) {
                            doClone(nodeWrapper, uuidsToPublish, sourceSession, destinationSession);
                        }
                    } catch (ItemNotFoundException e) {
                        doClone(nodeWrapper, uuidsToPublish, sourceSession, destinationSession);
                    }
                }
            }
//            destinationNode.checkin();
//        } else {
//            destinationNode.checkpoint();
        }
        return destinationNode;
    }

    private String handleSharedMove(JCRSessionWrapper sourceSession, JCRNodeWrapper sourceNode, String destinationPath) throws RepositoryException {
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

    private void recurseSetPublicationDate(Node node, List<String> uuidsToPublish, Calendar c, String userID) throws RepositoryException {
        if (node.isNodeType("jmix:lastPublished")) {
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            node.setProperty("j:lastPublished", c);
            node.setProperty("j:lastPublishedBy", userID);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            if (uuidsToPublish == null || uuidsToPublish.contains(sub.getIdentifier())) {
                recurseSetPublicationDate(sub, uuidsToPublish, c, userID);
            }
        }
    }

    private void checkin(Session session, JCRNodeWrapper node, VersionManager versionManager, Calendar calendar) throws RepositoryException {
        jcrVersionService.setNodeCheckinDate(node, calendar);
        session.save();
        versionManager.checkin(node.getPath());
    }

    private void recurseCheckin(Session session, JCRNodeWrapper node, List<String> uuidsToPublish, VersionManager versionManager, Calendar calendar) throws RepositoryException {
        if (node.isNodeType("mix:versionable") && node.isCheckedOut() && !node.hasProperty("jcr:mergeFailed")) {
            checkin(session, node, versionManager, calendar);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper sub = (JCRNodeWrapper) ni.nextNode();
            if (uuidsToPublish == null || uuidsToPublish.contains(sub.getIdentifier())) {
                recurseCheckin(session, sub, uuidsToPublish, versionManager, calendar);
            }
        }
    }

    private void recurseCheckout(Node node, List<String> prune, VersionManager versionManager) throws RepositoryException {
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
     * @param path      path of the node to unpublish
     * @param languages
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(String path, Set<String> languages) throws RepositoryException {
        JCRSessionWrapper sourceSession = getSessionFactory().getCurrentUserSession();
        JCRSessionWrapper destinationSession = getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);
        JCRNodeWrapper node = sourceSession.getNode(path);
        VersionManager vm = sourceSession.getWorkspace().getVersionManager();
        List<JCRNodeWrapper> l = new ArrayList<JCRNodeWrapper>();
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
    }

    public List<PublicationInfo> getPublicationInfos(List<String> uuids, Set<String> languages, boolean includesReferences, boolean includesSubnodes, boolean allsubtree,
                                              final String sourceWorkspace, final String destinationWorkspace)  throws RepositoryException {
        List<PublicationInfo> infos = new ArrayList<PublicationInfo>();

        List<String> allUuids = new ArrayList<String>();

        for (String uuid : uuids) {
            if (!allUuids.contains(uuid)) {
                final List<PublicationInfo> publicationInfos = getPublicationInfo(uuid, languages, includesReferences, includesSubnodes, allsubtree, sourceWorkspace, destinationWorkspace);
                for (PublicationInfo publicationInfo : publicationInfos) {
                    if (publicationInfo.needPublication()) {
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
    public List<PublicationInfo> getPublicationInfo(String uuid, Set<String> languages, boolean includesReferences, boolean includesSubnodes, boolean allsubtree,
                                              final String sourceWorkspace, final String destinationWorkspace) throws RepositoryException {
        final JCRSessionWrapper sourceSession = sessionFactory.getCurrentUserSession(sourceWorkspace);
        final JCRSessionWrapper destinationSession = sessionFactory.getCurrentUserSession(destinationWorkspace);

        JCRNodeWrapper stageNode = sourceSession.getNodeByUUID(uuid);
        List<PublicationInfo> infos = new ArrayList<PublicationInfo>();
        PublicationInfo tree = new PublicationInfo(uuid, stageNode.getPath());
        infos.add(tree);
        getPublicationInfo(stageNode, tree.getRoot(), languages, includesReferences, includesSubnodes, allsubtree,
                sourceSession, destinationSession, new HashSet<String>(), infos);
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
     * @param uuids              a Set of uuids, which don't need to be checked or have already been checked
     * @return the <code>PublicationInfo</code> for the requested node(s)
     * @throws RepositoryException
     */
    private void getPublicationInfo(JCRNodeWrapper node, PublicationInfo.PublicationNode info, Set<String> languages,
                                    boolean includesReferences, boolean includesSubnodes, boolean allsubtree, final JCRSessionWrapper sourceSession,
                                    final JCRSessionWrapper destinationSession, Set<String> uuids, List<PublicationInfo> infos)
            throws RepositoryException {
        if (uuids.contains(info.getUuid())) {
            return;
        }
        uuids.add(info.getUuid());

//        JCRNodeWrapper stageNode = null;
//        try {
//            stageNode = sourceSession.getNodeByUUID(info.getUuid());
//        } catch (ItemNotFoundException e) {
//            stageNode = destinationSession.getNodeByUUID(info.getUuid());
//            info.setPath(stageNode.getPath());
//            info.setStatus(PublicationInfo.LIVE_ONLY);
//            return;
//        }
//        info.setPath(stageNode.getPath());
        JCRNodeWrapper publishedNode = null;
        try {
            publishedNode = destinationSession.getNodeByUUID(node.getIdentifier());
        } catch (ItemNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("No live node for staging node " + node.getPath());
            }
        }

        if (node.hasProperty("j:published") && !node.getProperty("j:published").getBoolean()) {
            info.setStatus(PublicationInfo.UNPUBLISHED);
        } else if (publishedNode == null) {
                info.setStatus(PublicationInfo.NOT_PUBLISHED);
        } else {
            if (node.hasProperty("jcr:mergeFailed") || publishedNode.hasProperty("jcr:mergeFailed")) {
                info.setStatus(PublicationInfo.CONFLICT);
            } else if (node.getLastModifiedAsDate() == null) {
                logger.error("Null modifieddate for staged node " + node.getPath());
                info.setStatus(PublicationInfo.MODIFIED);
            } else {
                Date modProp = node.getLastModifiedAsDate();
                Date pubProp = node.getLastPublishedAsDate();
                Date liveModProp = publishedNode.getLastModifiedAsDate();
                if (modProp == null || pubProp == null || liveModProp == null) {
                    logger.warn(info.getUuid() + " : Some property is null : "+modProp + "/"+pubProp + "/"+ liveModProp);
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
        // todo : performance problem on permission check
//        info.setCanPublish(stageNode.hasPermission(JCRNodeWrapper.WRITE_LIVE));
        info.setCanPublish(true);

        if (includesReferences || includesSubnodes) {
            String lang = null;
            if (node.isNodeType("jnt:translation")) {
                lang = node.getProperty("jcr:language").getString();
            }

            if (includesReferences) {
                PropertyIterator pi = node.getProperties();
                while (pi.hasNext()) {
                    Property p = pi.nextProperty();
                    PropertyDefinition definition = p.getDefinition();
                    if (lang != null && p.getName().endsWith("_" + lang)) {
                        String name = p.getName().substring(0, p.getName().length() - lang.length() - 1);
                        definition = ((JCRNodeWrapper) node.getParent()).getApplicablePropertyDefinition(name);
                    }
                    if (definition!=null &&
                            (definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE)
                            && !p.getName().startsWith("jcr:")) {
                        if (definition.isMultiple()) {
                            Value[] vs = p.getValues();
                            for (Value v : vs) {
                                try {
                                    JCRNodeWrapper ref = node.getSession().getNodeByUUID(v.getString());
//                            if (!referencedNode.contains(ref)) {
                                    if (!ref.isNodeType("jnt:page")) {
                                        getPublicationInfo(ref, info.addReference(ref.getUUID(), ref.getPath()).getRoot(), languages, includesReferences, includesSubnodes, false, sourceSession, destinationSession, uuids, infos);
                                    }
//                            }
                                } catch (ItemNotFoundException e) {
                                    if (definition.getRequiredType() == PropertyType.REFERENCE) {
                                        logger.warn("Cannot get reference " + v.getString());
                                    } else {
                                        logger.debug("Cannot get reference " + v.getString());
                                    }

                                }
                            }
                        } else {
                            try {
                                JCRNodeWrapper ref = (JCRNodeWrapper) p.getNode();
//                        if (!referencedNode.contains(ref)) {
                                if (!ref.isNodeType("jnt:page")) {
                                    getPublicationInfo(ref, info.addReference(ref.getUUID(), ref.getPath()).getRoot(), languages, includesReferences, includesSubnodes, false, sourceSession, destinationSession, uuids, infos);
                                }
//                        }
                            } catch (ItemNotFoundException e) {
                                logger.warn("Cannot get reference " + p.getString());
                            }
                        }
                    }
                }
            }
            NodeIterator ni = node.getNodes();
            while (ni.hasNext()) {
                JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                PublicationInfo.PublicationNode subinfo = info;
                if (allsubtree && hasIndependantPublication(n)) {
                    PublicationInfo newinfo = new PublicationInfo(n.getUUID(), n.getPath());
                    infos.add(newinfo);
                    subinfo = newinfo.getRoot();
                }
                if (allsubtree || !hasIndependantPublication(n)) {
                    if (languages != null && n.isNodeType("mix:language")) {
                        String translationLanguage = n.getProperty("jcr:language").getString();
                        if (languages.contains(translationLanguage)) {
                            getPublicationInfo(n, subinfo.addChild(n.getUUID(), n.getPath()), languages, includesReferences, includesSubnodes, allsubtree, sourceSession, destinationSession, uuids, infos);
                        }
                    } else if (n.isNodeType("jmix:lastPublished")) {
                        getPublicationInfo(n, subinfo.addChild(n.getUUID(), n.getPath()), languages, includesReferences, includesSubnodes, allsubtree, sourceSession, destinationSession, uuids, infos);
                    }
                }
            }
        }
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