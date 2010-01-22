package org.jahia.services.content;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.*;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import javax.jcr.version.Version;
import javax.jcr.nodetype.PropertyDefinition;
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
     * @return The JCR session factory
     */
    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Set the JCR session factory
     * @param sessionFactory The JCR session factory
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    /**
     * Get the singleton instance of the JCRPublicationService
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

        try {
            JahiaAccessManager.setPublication(Boolean.TRUE);
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
        } finally {
            JahiaAccessManager.setPublication(null);
        }


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
        Calendar c = new GregorianCalendar();
        for (JCRNodeWrapper jcrNodeWrapper : toPublish) {
            if (!jcrNodeWrapper.isCheckedOut()) {
                jcrNodeWrapper.checkout();
            }
            if (!jcrNodeWrapper.hasProperty("j:published") || !jcrNodeWrapper.getProperty("j:published").getBoolean()) {
                jcrNodeWrapper.setProperty("j:published", Boolean.TRUE);
            }
            jcrNodeWrapper.setProperty("j:lastPublished", c);
            jcrNodeWrapper.setProperty("j:lastPublishedBy", destinationSession.getUserID());
        }
        sourceNode.getSession().save();
        system = true;
        if (system) {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                    return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                        public Object doInJCR(JCRSessionWrapper destinationSession) throws RepositoryException {
                            publish(toPublish, pruneNodes, sourceNode.getSession(), destinationSession);
                            return null;
                        }
                    }, sourceSession.getUserID(), destinationWorkspace);
                }
            }, destinationSession.getUserID(), sourceWorkspace);
        } else {
            publish(toPublish, pruneNodes, sourceNode.getSession(), destinationSession);
        }
        JCRObservationManager.consume(destinationSession);
    }

    private void publish(List<JCRNodeWrapper> toPublish, List<JCRNodeWrapper> pruneNodes, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper sourceNode = toPublish.iterator().next();
        try {
//            sourceNode.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
            destinationSession.getNode(sourceNode.getPath());
        } catch (PathNotFoundException e) {
            cloneToLiveWorkspace(toPublish, pruneNodes, sourceSession, destinationSession);
            return;
        }
        mergeToLiveWorkspace(toPublish, sourceSession, destinationSession);
    }

    private void mergeToLiveWorkspace(List<JCRNodeWrapper> toPublish, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        for (JCRNodeWrapper node : toPublish) {
            try {
                String path = node.getPath();
                String destinationPath = node.getCorrespondingNodePath(destinationSession.getWorkspace().getName());

                // Item exists at "destinationPath" in live space, update it
                VersionManager sourceVersionManager = node.getSession().getWorkspace().getVersionManager();

                VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();

                logger.info("Merge : "+path);

                String oldPath = handleSharedMove(sourceSession, node, node.getPath());

                // Node has been modified, check in now
                Version newVersion = sourceVersionManager.checkin(path);

                NodeIterator ni = destinationVersionManager.merge(destinationPath, node.getSession().getWorkspace().getName(), true, true);

                while (ni.hasNext()) {
                    // Conflict : node has been modified in live. Resolve conflict without doing anything
                    Node failed = ni.nextNode();
                    destinationVersionManager.checkout(failed.getPath());
                    destinationVersionManager.doneMerge(failed.getPath(), newVersion);
                    destinationVersionManager.checkin(failed.getPath());
                    // Then retry the merge (should be ok now)
                    path = failed.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
                    destinationVersionManager.merge(path, node.getSession().getWorkspace().getName(), false, true);
                    // Update the staging node to the new merged version.
                    node.update(destinationSession.getWorkspace().getName());
                }

                // todo : handle shareable node move
//                if (oldPath != null) {
//                    try {
//                        JCRNodeWrapper snode = destinationSession.getNode(oldPath);
//                        snode.checkout();
//                        JCRNodeWrapper oldParent = snode.getParent();
//                        oldParent.checkout();
//                        snode.remove();
//                        destinationSession.save();
//                    } catch (RepositoryException e) {
//                        e.printStackTrace();
//                    }
//                }
            } catch (ItemNotFoundException e) {
                // Item does not exist yet in live space
//                node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
            }
        }
    }

    private void cloneToLiveWorkspace(List<JCRNodeWrapper> toPublish, List<JCRNodeWrapper> pruneNodes, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper sourceNode = toPublish.iterator().next();

        JCRNodeWrapper parent = sourceNode.getParent();
        String destinationWorkspaceName = destinationSession.getWorkspace().getName();
        String destinationParentPath = null;
//                destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
        destinationParentPath = parent.getPath();

        VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();


        for (JCRNodeWrapper node : toPublish) {
            if (node.isNodeType("mix:versionable")) {
                sourceVersionManager.checkin(node.getPath());
            }
        }
//        recurseCheckin(sourceNode, sourceVersionManager);

//        System.out.println("---BEFORE CLONE");
//        System.out.println("STAG "+logVersion(sourceNode.getPath(), sourceVersionManager));
//        System.out.println("-----------------");

        VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
        if (!destinationVersionManager.isCheckedOut(destinationParentPath)) {
            destinationVersionManager.checkout(destinationParentPath);
        }
        List<String> deniedPath = new ArrayList<String>();
        for (JCRNodeWrapper node : pruneNodes) {
            deniedPath.add(node.getPath());
        }
        String destinationPath;
        if (destinationParentPath.equals("/")) {
            destinationPath = "/" + sourceNode.getName();
        } else {
            destinationPath = destinationParentPath + "/" + sourceNode.getName();
        }
        try {
            JahiaAccessManager.setDeniedPathes(deniedPath);
            try {
                String correspondingNodePath = sourceNode.getCorrespondingNodePath(destinationWorkspaceName);
                if (sourceNode.isNodeType("mix:shareable")) {
                    // Shareable node - todo : check if we need to move or clone

                    String oldPath = handleSharedMove(sourceSession, sourceNode, destinationPath);

                    // Clone the node node in live space
                    destinationSession.getWorkspace().clone(destinationWorkspaceName, correspondingNodePath, destinationPath, false);

                    // todo : handle shareable node move
//                    if (oldPath != null) {
//                        try {
//                            JCRNodeWrapper node = destinationSession.getNode(oldPath);
//                            node.checkout();
//                            JCRNodeWrapper oldParent = node.getParent();
//                            oldParent.checkout();
//                            node.remove();
//                            destinationSession.save();
//                        } catch (RepositoryException e) {
//                            e.printStackTrace();
//                        }
//                    }
                } else {
                    // Non shareable node has been moved
                    destinationVersionManager.checkout(StringUtils.substringBeforeLast(correspondingNodePath,"/"));
                    destinationVersionManager.checkout(correspondingNodePath);
                    destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNode.getPath(), destinationPath, true);
                    destinationVersionManager.checkin(StringUtils.substringBeforeLast(correspondingNodePath,"/"));
                }
            } catch (ItemNotFoundException e) {
                destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNode.getPath(), destinationPath, false);
            }
        } finally {
            JahiaAccessManager.setDeniedPathes(null);
        }

        destinationVersionManager.checkin(destinationParentPath);
    }

    private String handleSharedMove(JCRSessionWrapper sourceSession, JCRNodeWrapper sourceNode, String destinationPath) throws RepositoryException {
        String oldPath = null;
        if (sourceNode.hasProperty("j:movedFrom")) {
            Property movedFrom = sourceNode.getProperty("j:movedFrom");
            List<Value> values = new ArrayList<Value>(Arrays.asList(movedFrom.getValues()));
            for (Value value : values) {
                String v = value.getString();
                if (v.endsWith(":::" + destinationPath)) {
                    oldPath = StringUtils.substringBefore(v,":::");
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

    private void recurseCheckin(Node node, VersionManager versionManager) throws RepositoryException {
        if (node.isNodeType("mix:versionable")) {
            versionManager.checkin(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            recurseCheckin(sub, versionManager);
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
        try {
            List<JCRNodeWrapper> l = new ArrayList<JCRNodeWrapper>();
            JahiaAccessManager.setPublication(Boolean.TRUE);
            if (!vm.isCheckedOut(node.getPath())) {
                vm.checkout(node.getPath());
            }
            node.setProperty("j:published", false);
            if (!node.isNodeType("jmix:publication")) {
                node.addMixin("jmix:publication");
            }
            l.add(node);
            if (languages != null) {
                NodeIterator ni = node.getNodes("jnt:translation");
                while (ni.hasNext()) {
                    JCRNodeWrapper i18n = (JCRNodeWrapper) ni.next();
                    if (languages.contains(i18n.getProperty("jcr:language").getString())) {
                        i18n.setProperty("j:published", false);
                        l.add(i18n);
                    }
                }
            }
            sourceSession.save();
//            l.add(node);
            mergeToLiveWorkspace(l, sourceSession, destinationSession);
        } finally {
            JahiaAccessManager.setPublication(null);
        }
    }

    /**
     * Gets the publication info for the current node and if acquired also for referenced nodes and subnodes. 
     * The returned <code>PublicationInfo</code> has the publication info for the current node (NOT_PUBLISHED, PUBLISHED, MODIFIED, UNPUBLISHABLE) 
     * and if requested you will be able to get the infos also for the subnodes and the referenced nodes.
     * As language dependent data is always stored in subnodes you need to set includesSubnodes to true, if you also specify a list of languages.   
     * @param path The path of the node to get publication info
     * @param languages Languages list to use for publication info, or null for all languages (only appplied if includesSubnodes is true)
     * @param includesReferences If true include info for referenced nodes
     * @param includesSubnodes If true include info for subnodes
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
     * @param path The path of the node to get publication info
     * @param languages Languages list to use for publication info, or null for all languages (only appplied if includesSubnodes is true)
     * @param includesReferences If true include info for referenced nodes
     * @param includesSubnodes If true include info for subnodes
     * @param pathes a Set of pathes, which don't need to be checked or have already been checked
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
        if (publishedNode == null) {
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
            if (stageNode.getLastModifiedAsDate() == null) {
                logger.error("Null modifieddate for staged node " + stageNode.getPath());
                info.setStatus(PublicationInfo.MODIFIED);
            } else {
                long s = stageNode.getLastModifiedAsDate().getTime();
//                long p = stageNode.getLastPublishedAsDate().getTime();
                long p = publishedNode.getLastModifiedAsDate().getTime();
                if (s > p) {
                    info.setStatus(PublicationInfo.MODIFIED);
                } else {
                    info.setStatus(PublicationInfo.PUBLISHED);
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
        System.out.print(StringUtils.leftPad("", indent)+"---- "+ v.getName());
        Version[] preds = v.getPredecessors();
        System.out.print("(");
        for (Version pred : preds) {
            System.out.print(" "+pred.getName());
        }
        System.out.print(")");
        System.out.println("");
        Version[] succ = v.getSuccessors();
        for (Version version : succ) {
            print(version, indent + 2);
        }
    }
}