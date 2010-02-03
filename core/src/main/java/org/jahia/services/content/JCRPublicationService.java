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

import javax.jcr.*;
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

    /**
     * Search in all sub nodes and properties the referenced nodes and get the list of nodes where the publication
     * should stop ( currently sub pages, sub folders and sub files ).
     *
     * @param start          The root node where to start the search
     * @param toPublish      Empty list, will be filled with the list of sub nodes that will be part of the publication
     * @param pruneNodes     Empty list, will be filled with the list of sub nodes that should not be part of the publication
     * @param referencedNode Empty list, will be filled with the list of nodes referenced in the sub tree
     * @param languages      Languages list to publish, or null for all languages
     * @param allSubTree     Do not prune on sub nodes, includes all sub tree
     */
    private void getBlockedAndReferencesList(JCRNodeWrapper start, List<JCRNodeWrapper> toPublish, List<JCRNodeWrapper> pruneNodes, List<JCRNodeWrapper> referencedNode, Set<String> languages, boolean allSubTree) throws RepositoryException {
        toPublish.add(start);
        String lang = null;
        if (start.isNodeType("jnt:translation")) {
            lang = start.getProperty("jcr:language").getString();
        }

        PropertyIterator pi = start.getProperties();
        while (pi.hasNext()) {
            Property p = pi.nextProperty();
            PropertyDefinition definition = p.getDefinition();
            if (lang != null && p.getName().endsWith("_" + lang)) {
                String name = p.getName().substring(0, p.getName().length() - lang.length() - 1);
                definition = ((JCRNodeWrapper) start.getParent()).getApplicablePropertyDefinition(name);
            }
            if ((definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE) && !p.getName().startsWith(
                    "jcr:")) {
                if (definition.isMultiple()) {
                    Value[] vs = p.getValues();
                    for (Value v : vs) {
                        try {
                            JCRNodeWrapper ref = start.getSession().getNodeByUUID(v.getString());
                            if (!referencedNode.contains(ref.getPath())) {
                                if (!ref.isNodeType("jnt:page")) {
                                    referencedNode.add(ref);
                                }
                            }
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
                        if (!referencedNode.contains(ref.getPath())) {
                            if (!ref.isNodeType("jnt:page")) {
                                referencedNode.add(ref);
                            }
                        }
                    } catch (ItemNotFoundException e) {
                        logger.warn("Cannot get reference " + p.getString());
                    }
                }
            }
        }
        NodeIterator ni = start.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (!allSubTree && n.isNodeType("jmix:publication")) {
                pruneNodes.add(n);
            } else if (languages != null && n.isNodeType("jnt:translation")) {
                String translationLanguage = n.getProperty("jcr:language").getString();
                if (languages.contains(translationLanguage)) {
                    getBlockedAndReferencesList(n, toPublish, pruneNodes, referencedNode, languages, allSubTree);
                } else {
                    pruneNodes.add(n);
                }
            } else if (n.isNodeType("jmix:lastPublished")) {
                getBlockedAndReferencesList(n, toPublish, pruneNodes, referencedNode, languages, allSubTree);
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
     * @param system               use system session or not
     * @param allSubTree
     * @throws javax.jcr.RepositoryException in case of error
     */
    public void publish(String path, String sourceWorkspace, String destinationWorkspace, Set<String> languages,
                        boolean system, boolean allSubTree) throws RepositoryException {
        JCRSessionWrapper session = getSessionFactory().getCurrentUserSession(sourceWorkspace);
        JCRNodeWrapper n = session.getNode(path);

        for (ExtendedNodeType type : n.getMixinNodeTypes()) {
            if (type.getName().equals("jmix:publication")) {
                if (!n.isCheckedOut()) {
                    n.checkout();
                }
                n.removeMixin("jmix:publication");
                session.save();
            }
        }
        publish(n, sourceWorkspace, destinationWorkspace, languages, system, allSubTree, false, new HashSet<String>());
//        session.save();
    }

    private void publish(final JCRNodeWrapper sourceNode, final String sourceWorkspace, final String destinationWorkspace, Set<String> languages,
                         boolean system, boolean allSubTree, boolean publishParent, Set<String> pathes) throws RepositoryException {
        pathes.add(sourceNode.getPath());

        JCRSessionWrapper destinationSession = getSessionFactory().getCurrentUserSession(destinationWorkspace);

        try {
            sourceNode.getParent().getCorrespondingNodePath(destinationWorkspace);
        } catch (ItemNotFoundException e) {
            if (publishParent) {
                String parentPath = sourceNode.getParent().getPath();
                if (!pathes.contains(parentPath)) {
                    publish(sourceNode.getParent(), sourceWorkspace, destinationWorkspace, languages, system, false, true, pathes);
                }
            } else {
                return;
            }
        }

        final List<JCRNodeWrapper> pruneNodes = new ArrayList<JCRNodeWrapper>();
        final List<JCRNodeWrapper> referencedNodes = new ArrayList<JCRNodeWrapper>();
        final List<JCRNodeWrapper> toPublish = new ArrayList<JCRNodeWrapper>();

        getBlockedAndReferencesList(sourceNode, toPublish, pruneNodes, referencedNodes, languages, allSubTree);
//        for (Node s : toPublish) {
//            s.setProperty("j:publishedLanguage")
//        }

        for (JCRNodeWrapper node : referencedNodes) {
            try {
                if (!pathes.contains(node.getPath())) {
                    publish(node, sourceWorkspace, destinationWorkspace, languages, system, false, true, pathes);
                }
            } catch (AccessDeniedException e) {
                logger.warn("Cannot publish node at : " + node);
            }
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
        system = true;
        if (system) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(destinationSession.getUserID(), sourceWorkspace, new JCRCallback<Object>() {
                public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                    return JCRTemplate.getInstance().doExecuteWithSystemSession(sourceSession.getUserID(), destinationWorkspace, new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper destinationSession) throws RepositoryException {
                            publish(toPublish, pruneNodes, sourceNode.getSession(), destinationSession);
                            return null;
                        }
                    });
                }
            });
        } else {
            publish(toPublish, pruneNodes, sourceNode.getSession(), destinationSession);
        }
    }

    private void publish(List<JCRNodeWrapper> toPublish, List<JCRNodeWrapper> pruneNodes, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper sourceNode = toPublish.iterator().next();

        List<String> prunedPath = new ArrayList<String>();
        if (pruneNodes != null) {
            for (JCRNodeWrapper node : pruneNodes) {
                prunedPath.add(node.getPath());
            }
        }

        try {
            destinationSession.getNode(sourceNode.getPath());
            mergeToDestinationWorkspace(toPublish, prunedPath, sourceSession, destinationSession);
        } catch (PathNotFoundException e) {
            cloneToDestinationWorkspace(toPublish.iterator().next(), prunedPath, sourceSession, destinationSession);
        }
    }

    void mergeToDestinationWorkspace(final List<JCRNodeWrapper> toPublish, final List<String> pruneNodes, final JCRSessionWrapper sourceSession, final JCRSessionWrapper destinationSession) throws RepositoryException {
        Calendar c = new GregorianCalendar();
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();
        final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

        List<JCRNodeWrapper> modified = new ArrayList<JCRNodeWrapper>();
        for (final JCRNodeWrapper node : toPublish) {
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
        for (JCRNodeWrapper node : modified) {
            System.out.println("-- setting last published : "+node.getPath());
//            if (!sourceSession.getWorkspace().getName().equals("live")) {
                if (!node.isCheckedOut()) {
                    node.checkout();
                }
                node.setProperty("j:lastPublished", c);
                node.setProperty("j:lastPublishedBy", destinationSession.getUserID());
//            }
        }
        if (modified.isEmpty()) {
            return;
        }

        sourceSession.save();

        for (JCRNodeWrapper node : modified) {
            // Node has been modified, check in now
            sourceVersionManager.checkin(node.getPath());
        }
        
        for (final JCRNodeWrapper node : modified) {
            try {

                if (node.hasProperty("jcr:mergeFailed")) {
                    Value[] failed = node.getProperty("jcr:mergeFailed").getValues();

                    for (Value value : failed) {
                        System.out.println("-- failed merge waiting : "+value.getString());
                    }
                    continue;
                }

                final String path = node.getPath();
                final String destinationPath = node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());

                // Item exists at "destinationPath" in live space, update it

                Node destinationNode = destinationSession.getNode(destinationPath); // Live node exists - merge live node from source space

                final String oldPath = handleSharedMove(sourceSession, node, node.getPath());

                logger.info("Merge node : " + path + " source v=" + node.getBaseVersion().getName() +
                        " , dest node v=" + destinationSession.getNode(destinationPath).getBaseVersion().getName());

                recurseCheckin(destinationSession.getNode(destinationPath), pruneNodes, destinationVersionManager);

                NodeIterator ni = destinationVersionManager.merge(destinationPath, node.getSession().getWorkspace().getName(), true, true);

                if (ni.hasNext()) {
                    while (ni.hasNext()) {
                        logger.info("Merge conflict");
                        // Conflict : node has been modified in live. Resolve conflict.
                        Node failed = ni.nextNode();
                        destinationVersionManager.checkout(failed.getPath());

                        JCRNodeWrapper destNode = destinationSession.getNode(failed.getPath());

                        ConflictResolver resolver = new ConflictResolver(node, destNode);
                        resolver.setPrunedPaths(pruneNodes);
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
                    recurseCheckin(destinationNode, pruneNodes, destinationVersionManager);
                    node.update(destinationSession.getWorkspace().getName());
                }

                if (oldPath != null) {
                    try {
                        JCRNodeWrapper snode = destinationSession.getNode(oldPath);
                        recurseCheckout(snode,null,destinationVersionManager);
                        JCRNodeWrapper oldParent = node.getParent();
                        oldParent.checkout();
                        node.remove();
                        node.getRealNode().getSession().save();
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                }

                logger.info("Merge node end : " + path + " source v=" + sourceSession.getNode(path).getBaseVersion().getName() +
                        " , dest node v=" + destinationSession.getNode(destinationPath).getBaseVersion().getName());


            } catch (ItemNotFoundException e) {
                // Item does not exist yet in live space
//                node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
            }
        }
    }

    void cloneToDestinationWorkspace(JCRNodeWrapper sourceNode, List<String> pruneNodes, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        final VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();

        recurseSetPublicationDate(sourceNode, pruneNodes, new GregorianCalendar(), sourceSession.getUserID());
        sourceSession.save();
        recurseCheckin(sourceNode, pruneNodes, sourceVersionManager);

        doClone(sourceNode, pruneNodes, sourceSession, destinationSession);

        VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
//        destinationVersionManager.checkin(sourceNode.getParent().getPath());
    }

    void doClone(JCRNodeWrapper sourceNode, List<String> deniedPath, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper parent = sourceNode.getParent();
//                destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);

        final String destinationWorkspaceName = destinationSession.getWorkspace().getName();
        final String destinationParentPath = parent.getPath();

        logger.info("Cloning node : " + sourceNode.getPath() + " source v=" + sourceNode.getBaseVersion().getName() +
                " , source parent v=" + sourceNode.getParent().getBaseVersion().getName() +
                " , dest parent v=" + destinationSession.getNode(destinationParentPath).getBaseVersion().getName());

        try {
            JahiaAccessManager.setDeniedPathes(deniedPath);

            final VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
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
                            node.getRealNode().getSession().save();
                        } catch (RepositoryException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    // Non shareable node has been moved
                    destinationVersionManager.checkout(StringUtils.substringBeforeLast(correspondingNodePath, "/")); // previous parent
                    destinationVersionManager.checkout(destinationParentPath); // new parent
                    recurseCheckout(destinationSession.getNode(correspondingNodePath), null, destinationVersionManager); // node and sub nodes

                    destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNode.getPath(), destinationPath, true);
//                    destinationVersionManager.checkin(destinationParentPath);
                }
            } catch (ItemNotFoundException e) {
                destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNode.getPath(), destinationPath, false);
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
                            destinationParent.getRealNode().getSession().save();
                            break;
                        } catch (PathNotFoundException e1) {

                        }
                    }
                }
            }
        } finally {
            JahiaAccessManager.setDeniedPathes(null);
        }

        logger.info("Cloning node end : " + sourceNode.getPath() + " source v=" + sourceNode.getBaseVersion().getName() +
                " , dest node v=" + destinationSession.getNode(sourceNode.getPath()).getBaseVersion().getName() +
                " , source parent v=" + sourceNode.getBaseVersion().getName() +
                " , dest parent v=" + destinationSession.getNode(destinationParentPath).getBaseVersion().getName());
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

    private void recurseSetPublicationDate(Node node, List<String> prune, Calendar c, String userID) throws RepositoryException {
        if (node.isNodeType("jmix:lastPublished")) {
            System.out.println("-- setting last published/clone : "+node.getPath());
            if (!node.isCheckedOut()) {
                node.checkout();
            }
            node.setProperty("j:lastPublished", c);
            node.setProperty("j:lastPublishedBy", userID);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            if (prune == null || !prune.contains(sub.getPath())) {
                recurseSetPublicationDate(sub, prune, c, userID);
            }
        }
    }

    private void recurseCheckin(Node node, List<String> prune, VersionManager versionManager) throws RepositoryException {
        if (node.isNodeType("mix:versionable") && node.isCheckedOut() && !node.hasProperty("jcr:mergeFailed")) {
            versionManager.checkin(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            if (prune == null || !prune.contains(sub.getPath())) {
                recurseCheckin(sub, prune, versionManager);
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
            if (prune == null || !prune.contains(sub.getPath())) {
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
        if (languages != null) {
            NodeIterator ni = node.getNodes("jnt:translation");
            while (ni.hasNext()) {
                JCRNodeWrapper i18n = (JCRNodeWrapper) ni.next();
                if (languages.contains(i18n.getProperty("jcr:language").getString())) {
                    if (!i18n.isCheckedOut()) {
                        i18n.checkout();
                    }
                    i18n.setProperty("j:published", false);
                }
            }
        }
    }

    /**
     * Gets the publication info for the current node and if acquired also for referenced nodes and subnodes.
     * The returned <code>PublicationInfo</code> has the publication info for the current node (NOT_PUBLISHED, PUBLISHED, MODIFIED, UNPUBLISHABLE)
     * and if requested you will be able to get the infos also for the subnodes and the referenced nodes.
     * As language dependent data is always stored in subnodes you need to set includesSubnodes to true, if you also specify a list of languages.
     *
     * @param path               The path of the node to get publication info
     * @param languages          Languages list to use for publication info, or null for all languages (only appplied if includesSubnodes is true)
     * @param includesReferences If true include info for referenced nodes
     * @param includesSubnodes   If true include info for subnodes
     * @return the <code>PublicationInfo</code> for the requested node(s)
     * @throws RepositoryException
     */
    public PublicationInfo getPublicationInfo(String path, Set<String> languages, boolean includesReferences, boolean includesSubnodes) throws RepositoryException {
        return getPublicationInfo(path, languages, includesReferences, includesSubnodes, new HashSet<String>());
    }

    /**
     * Gets the publication info for the current node and if acquired also for referenced nodes and subnodes.
     * The returned <code>PublicationInfo</code> has the publication info for the current node (NOT_PUBLISHED, PUBLISHED, MODIFIED, UNPUBLISHABLE)
     * and if requested you will be able to get the infos also for the subnodes and the referenced nodes.
     * As language dependent data is always stored in subnodes you need to set includesSubnodes to true, if you also specify a list of languages.
     *
     * @param path               The path of the node to get publication info
     * @param languages          Languages list to use for publication info, or null for all languages (only appplied if includesSubnodes is true)
     * @param includesReferences If true include info for referenced nodes
     * @param includesSubnodes   If true include info for subnodes
     * @param pathes             a Set of pathes, which don't need to be checked or have already been checked
     * @return the <code>PublicationInfo</code> for the requested node(s)
     * @throws RepositoryException
     */
    public PublicationInfo getPublicationInfo(String path, Set<String> languages, boolean includesReferences, boolean includesSubnodes, Set<String> pathes)
            throws RepositoryException {
        pathes.add(path);

        JCRSessionWrapper session = getSessionFactory().getCurrentUserSession();
        JCRSessionWrapper liveSession = getSessionFactory().getCurrentUserSession(Constants.LIVE_WORKSPACE);
        PublicationInfo info = new PublicationInfo();

        JCRNodeWrapper publishedNode = null;
        try {
            publishedNode = liveSession.getNode(path);
        } catch (PathNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e);
            }
        }

        JCRNodeWrapper stageNode = session.getNode(path);

        if (includesReferences || includesSubnodes) {
            List<JCRNodeWrapper> toPublish = new ArrayList<JCRNodeWrapper>();
            List<JCRNodeWrapper> blocked = new ArrayList<JCRNodeWrapper>();
            List<JCRNodeWrapper> referencedNodes = new ArrayList<JCRNodeWrapper>();

            getBlockedAndReferencesList(stageNode, toPublish, blocked, referencedNodes, languages, false);

            if (includesReferences) {
                for (JCRNodeWrapper referencedNode : referencedNodes) {
                    if (!pathes.contains(referencedNode.getPath())) {
                        info.addReference(referencedNode.getPath(), getPublicationInfo(referencedNode.getPath(), languages, true, false, pathes));
                    }
                }
            }
            if (includesSubnodes) {
                toPublish.remove(0);
                for (JCRNodeWrapper sub : toPublish) {
                    if (!pathes.contains(sub.getPath())) {
                        info.addSubnode(sub.getPath(), getPublicationInfo(sub.getPath(), languages, false, false, pathes));
                    }
                }
            }
        }
        if (stageNode.hasProperty("j:published") && !stageNode.getProperty("j:published").getBoolean()) {
            info.setStatus(PublicationInfo.UNPUBLISHED);
        } else if (publishedNode == null) {
            // node has not been published yet, check if parent is published
            try {
                liveSession.getNode(stageNode.getParent().getPath());
                info.setStatus(PublicationInfo.NOT_PUBLISHED);
            } catch (AccessDeniedException e) {
                info.setStatus(PublicationInfo.UNPUBLISHABLE);
            } catch (PathNotFoundException e) {
                info.setStatus(PublicationInfo.UNPUBLISHABLE);
            }
        } else {
            if (stageNode.hasProperty("jcr:mergeFailed") || publishedNode.hasProperty("jcr:mergeFailed")) {
                info.setStatus(PublicationInfo.CONFLICT);                
            }
            if (stageNode.getLastModifiedAsDate() == null) {
                logger.error("Null modifieddate for staged node " + stageNode.getPath());
                info.setStatus(PublicationInfo.MODIFIED);
            } else {
                Date modProp = stageNode.getLastModifiedAsDate();
                Date pubProp = stageNode.getLastPublishedAsDate();
                Date liveModProp = publishedNode.getLastModifiedAsDate();
                if (modProp == null || pubProp == null || liveModProp == null) {
                    logger.warn(path + " : Some property is null : "+modProp + "/"+pubProp + "/"+ liveModProp);
                    info.setStatus(PublicationInfo.MODIFIED);
                } else {
                    long mod = modProp.getTime();
                    long pub = pubProp.getTime();
                    long liveMod = liveModProp.getTime();
//                    if (publishedNode.isCheckedOut()) {
//                        info.setStatus(PublicationInfo.LIVE_MODIFIED);
//                    } else if (stageNode.isCheckedOut()) {
//                        info.setStatus(PublicationInfo.MODIFIED);
//                    } else {
//                        info.setStatus(PublicationInfo.PUBLISHED);
//                    }
                    if (liveMod > pub) {
                        info.setStatus(PublicationInfo.LIVE_MODIFIED);
                    } else if (mod > pub) {
                        info.setStatus(PublicationInfo.MODIFIED);
                    } else {
                        info.setStatus(PublicationInfo.PUBLISHED);
                    }
                }
            }
        }
        info.setCanPublish(stageNode.hasPermission(JCRNodeWrapper.WRITE_LIVE));

        return info;
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