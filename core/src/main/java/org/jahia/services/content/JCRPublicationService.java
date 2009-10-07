package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

import javax.jcr.*;
import javax.jcr.version.VersionManager;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.Version;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.*;

public class JCRPublicationService extends JahiaService {
    private static transient Logger logger = Logger.getLogger(JCRPublicationService.class);
    private JCRSessionFactory sessionFactory;
    private static JCRPublicationService instance;

    private JCRPublicationService() {
    }

    public JCRSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
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
                            logger.warn("Cannot get reference " + v.getString());
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
            publish(n, sourceWorkspace, destinationWorkspace, languages, system, allSubTree, new HashSet<String>());
        } finally {
            JahiaAccessManager.setPublication(null);
        }


//        session.save();
    }

    private void publish(final JCRNodeWrapper sourceNode, final String sourceWorkspace, final String destinationWorkspace, Set<String> languages,
                         boolean system, boolean allSubTree, Set<String> pathes) throws RepositoryException {
        pathes.add(sourceNode.getPath());


        JCRSessionWrapper destinationSession = getSessionFactory().getCurrentUserSession(destinationWorkspace);

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
                    publish(node, sourceWorkspace, destinationWorkspace, languages, system, false, pathes);
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
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                public Object doInJCR(final JCRSessionWrapper sourceSession) throws RepositoryException {
                    return JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback() {
                        public Object doInJCR(JCRSessionWrapper destinationSession) throws RepositoryException {
                            publish(toPublish, sourceNode.getSession(), destinationSession);
                            return null;
                        }
                    }, sourceSession.getUserID(), destinationWorkspace);
                }
            }, destinationSession.getUserID(), sourceWorkspace);
        } else {
            publish(toPublish, sourceNode.getSession(), destinationSession);
        }

    }

    private void publish(List<JCRNodeWrapper> toPublish, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper sourceNode = toPublish.iterator().next();
        try {
            sourceNode.getCorrespondingNodePath(destinationSession.getWorkspace().getName());
        } catch (ItemNotFoundException e) {
            cloneToLiveWorkspace(sourceNode, sourceSession, destinationSession);
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

                Node liveNode = destinationSession.getNode(destinationPath); // Live node exists - merge live node from source space

                Date pubDate = liveNode.getProperty("jcr:lastModified").getDate().getTime();
                Date stagingDate = node.getProperty("jcr:lastModified").getDate().getTime();
//                System.out.println("-live-> "+ pubDate + "  ,-stag-> "+ stagingDate);

                if (pubDate.getTime() == stagingDate.getTime()) {
//                    System.out.println("--- node " + path + " is uptodate");
                    continue;
                }

                logger.info("Merge : "+path);

                // Node has been modified, check in now
                Version newVersion = sourceVersionManager.checkin(path);

//                System.out.println("---BEFORE PUBLISH");
//                System.out.println("STAG "+logVersion(path, sourceVersionManager));
//                System.out.println("LIVE "+logVersion(destinationPath, destinationVersionManager));
//                System.out.println("-----------------");

                NodeIterator ni = destinationVersionManager.merge(destinationPath, node.getSession().getWorkspace().getName(), true, true);
                while (ni.hasNext()) {
                    Node failed = ni.nextNode();
//                    System.out.println("### FAILED : live node modified. Resolve merging. "+failed.getPath() + " : "+ failed.getBaseVersion().getName());
                    destinationVersionManager.doneMerge(failed.getPath(), newVersion);
                    destinationVersionManager.checkin(failed.getPath());

//                    System.out.println("---AFTER MERGE");
//                    System.out.println("STAG "+logVersion(path, sourceVersionManager));
//                    System.out.println("LIVE "+logVersion(destinationPath, destinationVersionManager));
//                    System.out.println("-----------------");

                    destinationVersionManager.merge(path, node.getSession().getWorkspace().getName(), false, true);

//                        System.out.println("-------------------------------------------------------------");
//                        System.out.println("-------------------------------------------------------------");
//                        System.out.println("---------------------------------MERGE FAIL------------------");
//                        System.out.println("-------------------------------------------------------------");
//                        System.out.println("-------------------------------------------------------------");
//                        System.out.println("---AFTER FAILED MERGE");
//                        System.out.println("STAG "+logVersion(path, sourceVersionManager));
//                        System.out.println("LIVE "+logVersion(destinationPath, destinationVersionManager));
//                        System.out.println("-----------------");
                }
                // do not do unnecessary checkout
//                sourceVersionManager.checkout(path);

//                System.out.println("---END MERGE");
//                System.out.println("STAG "+logVersion(path, sourceVersionManager));
//                System.out.println("LIVE "+logVersion(destinationPath, destinationVersionManager));
//                System.out.println("-----------------");

            } catch (ItemNotFoundException e) {
                // Item does not exist yet in live space
//                System.out.println("------------- not found yet ???????");
                node.getParent().getCorrespondingNodePath(destinationSession.getWorkspace().getName());
            }
        }
    }

    private String logVersion(String path, VersionManager versionManager) throws RepositoryException {
        StringBuffer sb = new StringBuffer();
        VersionIterator vi = versionManager.getVersionHistory(path).getAllLinearVersions();
        sb.append("Node at "+path + ", base:"+versionManager.getBaseVersion(path).getName() + " co:" +versionManager.isCheckedOut(path)+" , versions=");
        while (vi.hasNext()) {
            Version version = (Version) vi.next();
            sb.append(version.getName() + " (<- ");
            Version[] versions = version.getPredecessors();
            for (int i = 0; i < versions.length; i++) {
                Version version1 = versions[i];
                sb.append(version1.getName()+ " ");
            }
            sb.append(")");
        }
        return sb.toString();
    }

    private void cloneToLiveWorkspace(JCRNodeWrapper sourceNode, JCRSessionWrapper sourceSession, JCRSessionWrapper destinationSession) throws RepositoryException {
        JCRNodeWrapper parent = sourceNode.getParent();
        String destinationWorkspaceName = destinationSession.getWorkspace().getName();
        String destinationParentPath = null;
        do {
            try {
                destinationParentPath = parent.getCorrespondingNodePath(destinationWorkspaceName);
                break;
            } catch (ItemNotFoundException e) {
                sourceNode = parent;
                parent = sourceNode.getParent();
            }
        } while (true);

        VersionManager sourceVersionManager = sourceSession.getWorkspace().getVersionManager();

        recurseCheckin(sourceNode, sourceVersionManager);

//        System.out.println("---BEFORE CLONE");
//        System.out.println("STAG "+logVersion(sourceNode.getPath(), sourceVersionManager));
//        System.out.println("-----------------");

        VersionManager destinationVersionManager = destinationSession.getWorkspace().getVersionManager();
        if (!destinationVersionManager.isCheckedOut(destinationParentPath)) {
            destinationVersionManager.checkout(destinationParentPath);
//            System.out.println("---BEFORE CLONE, PARENT NODE");
//            System.out.println("LIVE "+logVersion(destinationParentPath, destinationVersionManager));
//            System.out.println("-----------------");
        }

        destinationSession.getWorkspace().clone(sourceSession.getWorkspace().getName(), sourceNode.getPath(), destinationParentPath+"/"+sourceNode.getName(), false);

        destinationVersionManager.checkin(destinationParentPath);

        // do not do unnecessary checkout
//        recurseCheckout(sourceNode, sourceVersionManager);

//        System.out.println("---END CLONE");
//        System.out.println("STAG "+logVersion(sourceNode.getParent().getPath(), sourceVersionManager));
//        System.out.println("LIVE "+logVersion(destinationParentPath, destinationVersionManager));
//        System.out.println("-----------------");

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

    private void recurseCheckout(Node node, VersionManager versionManager) throws RepositoryException {
        if (node.isNodeType("mix:versionable")) {
            versionManager.checkout(node.getPath());
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            Node sub = ni.nextNode();
            recurseCheckout(sub, versionManager);
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
            l.add(node);
            mergeToLiveWorkspace(l, sourceSession, destinationSession);
        } finally {
            JahiaAccessManager.setPublication(null);
        }
    }

    /**
     * @param path
     * @return
     * @throws javax.jcr.RepositoryException
     */
    public PublicationInfo getPublicationInfo(String path, Set<String> languages, boolean includesReferences) throws RepositoryException {
        return getPublicationInfo(path, languages, includesReferences, new HashSet<String>());
    }

    public PublicationInfo getPublicationInfo(String path, Set<String> languages, boolean includesReferences, Set<String> pathes)
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

        if (includesReferences) {
            List<JCRNodeWrapper> toPublish = new ArrayList<JCRNodeWrapper>();
            List<JCRNodeWrapper> blocked = new ArrayList<JCRNodeWrapper>();
            List<JCRNodeWrapper> referencedNodes = new ArrayList<JCRNodeWrapper>();

            getBlockedAndReferencesList(stageNode, toPublish, blocked, referencedNodes, languages, false);
            for (JCRNodeWrapper referencedNode : referencedNodes) {
                if (!pathes.contains(referencedNode.getPath())) {
                    info.addReference(referencedNode.getPath(), getPublicationInfo(referencedNode.getPath(), languages, true, pathes));
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
                info.setStatus(PublicationInfo.NOT_PUBLISHED);
            }
        } else {
            if (stageNode.getLastModifiedAsDate() == null) {
                logger.error("Null modifieddate for staged node " + stageNode.getPath());
                info.setStatus(PublicationInfo.MODIFIED);
            } else {
                long s = stageNode.getLastModifiedAsDate().getTime();
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

    public void start() throws JahiaInitializationException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void stop() throws JahiaException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}