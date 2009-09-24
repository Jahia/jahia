package org.jahia.services.content;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.jaas.JahiaLoginModule;
import org.jahia.services.JahiaService;
import org.jahia.services.content.nodetypes.ExtendedPropertyType;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JCRPublicationService extends JahiaService {
    private static transient Logger logger = Logger.getLogger(JCRPublicationService.class);
    private JCRSessionFactory sessionFactory;
    private static JCRPublicationService instance;

    public JCRPublicationService() {
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
     * @param pruneNodes     Empty list, will be filled with the list of sub nodes that should not be part of the publication
     * @param referencedNode Empty list, will be filled with the list of nodes referenced in the sub tree
     * @param languages      The list of languages to publish, null to publish all
     * @throws javax.jcr.RepositoryException in case of error
     */
    private void getBlockedAndReferencesList(JCRNodeWrapper start, Set<String> pruneNodes, Set<String> referencedNode,
                                             Set<String> languages) throws RepositoryException {
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
                            Node ref = start.getSession().getNodeByUUID(v.getString());
                            if (!referencedNode.contains(ref.getPath())) {
                                if (!ref.isNodeType("jnt:page")) {
                                    referencedNode.add(ref.getPath());
                                }
//                                getBlockedAndReferencesList(ref, pruneNodes, referencedNode, languages);
                            }
                        } catch (ItemNotFoundException e) {
                            logger.warn("Cannot get reference " + v.getString());
                        }
                    }
                } else {
                    try {
                        Node ref = p.getNode();
                        if (!referencedNode.contains(ref.getPath())) {
                            if (!ref.isNodeType("jnt:page")) {
                                referencedNode.add(ref.getPath());
                            }
//                            getBlockedAndReferencesList(p.getNode(), pruneNodes, referencedNode, languages);
                        }
                    } catch (ItemNotFoundException e) {
                        logger.warn("Cannot get reference " + p.getString());
                    }
                }
            } else if ((definition.getRequiredType() == PropertyType.REFERENCE || definition.getRequiredType() == ExtendedPropertyType.WEAKREFERENCE)) {
                System.out.println("-->" + p.getName());
            }
        }
        NodeIterator ni = start.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (n.isNodeType("jnt:page") || n.isNodeType("jnt:folder") || n.isNodeType("jnt:file")) {
                pruneNodes.add(n.getPath());
            } else if (languages != null && n.isNodeType("jnt:translation")) {
                String translationLanguage = n.getProperty("jcr:language").getString();
                if (languages.contains(translationLanguage)) {
                    getBlockedAndReferencesList(n, pruneNodes, referencedNode, languages);
                } else {
                    pruneNodes.add(n.getPath());
                }
            } else if (!n.getName().equals("j:acl")) {
                getBlockedAndReferencesList(n, pruneNodes, referencedNode, languages);
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
     * @param user                 the user
     * @param publishParent        Recursively publish the parents
     * @param system               use system session or not
     * @throws javax.jcr.RepositoryException in case of error
     */
    public void publish(String path, String sourceWorkspace, String destinationWorkspace, Set<String> languages,
                        JahiaUser user, boolean publishParent, boolean system) throws RepositoryException {
        publish(path, sourceWorkspace, destinationWorkspace, languages, user, publishParent, system, new HashSet<String>());
    }

    private void publish(String path, String sourceWorkspace, String destinationWorkspace, Set<String> languages,
                         JahiaUser user, boolean publishParent, boolean system, Set<String> pathes) throws RepositoryException {
        pathes.add(path);

        JCRSessionWrapper sourceSession = getSessionFactory().getThreadSession(user, sourceWorkspace);
        JCRNodeWrapper sourceNode = sourceSession.getNode(path);

        String parentPath = sourceNode.getParent().getPath();
        JCRSessionWrapper destinationSession = getSessionFactory().getThreadSession(user,
                                                                                                    destinationWorkspace);
        try {
            destinationSession.getNode(parentPath);
        } catch (PathNotFoundException e) {
            if (publishParent) {
                if (!pathes.contains(parentPath)) {
                    publish(parentPath, sourceWorkspace, destinationWorkspace, languages, user, true, system, pathes);
                }
            } else {
                return;
            }
        }

        Set<String> blocked = new HashSet<String>();
        Set<String> referencedNodes = new HashSet<String>();

        getBlockedAndReferencesList(sourceNode, blocked, referencedNodes, languages);

        for (String node : referencedNodes) {
            try {
                if (!pathes.contains(node)) {
                    publish(node, sourceWorkspace, destinationWorkspace, languages, user, true, system, pathes);
                }
            } catch (AccessDeniedException e) {
                logger.warn("Cannot publish node at : " + node);
            }
        }

        List<String> deniedPathes = new ArrayList<String>();
        for (String node : blocked) {
            deniedPathes.add(node);
        }

        JCRSessionWrapper liveSessionForPublish;
        if (system) {
            liveSessionForPublish = getSessionFactory().login(JahiaLoginModule.getSystemCredentials(user.getUsername(),
                                                                                                                    deniedPathes),
                                                                              destinationWorkspace);
        } else {
            liveSessionForPublish = getSessionFactory().login(JahiaLoginModule.getCredentials(user.getUsername(),
                                                                                                              deniedPathes),
                                                                              destinationWorkspace);
        }
        try {
            Node liveNode = liveSessionForPublish.getNode(path);
            liveNode.update(sourceWorkspace);
        } catch (PathNotFoundException e) {
            try {
                liveSessionForPublish.getWorkspace().clone(sourceWorkspace, path, path, true);
            } catch (RepositoryException ee) {
                ee.printStackTrace();
            }
        } finally {
            liveSessionForPublish.logout();
        }
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param path      path of the node to unpublish
     * @param languages
     * @param user
     * @throws javax.jcr.RepositoryException
     */
    public void unpublish(String path, Set<String> languages, JahiaUser user) throws RepositoryException {
        JCRSessionWrapper session = getSessionFactory().getThreadSession(user);
        JCRNodeWrapper w = session.getNode(path);

        String parentPath = w.getParent().getPath();
        JCRSessionWrapper liveSession = getSessionFactory().getThreadSession(user, Constants.LIVE_WORKSPACE);
        final JCRNodeWrapper parentNode = liveSession.getNode(parentPath);
        final JCRNodeWrapper node = liveSession.getNode(path);
        node.remove();
        parentNode.save();
        liveSession.logout();
    }

    /**
     * @param path
     * @param user
     * @return
     * @throws javax.jcr.RepositoryException
     */
    public PublicationInfo getPublicationInfo(String path, JahiaUser user, Set<String> languages, boolean includesReferences) throws RepositoryException {
        return getPublicationInfo(path, user, languages, includesReferences, new HashSet<String>());
    }

    public PublicationInfo getPublicationInfo(String path, JahiaUser user, Set<String> languages, boolean includesReferences, Set<String> pathes)
            throws RepositoryException {
        pathes.add(path);

        JCRSessionWrapper session = getSessionFactory().getThreadSession(user);
        JCRSessionWrapper liveSession = getSessionFactory().getThreadSession(user, Constants.LIVE_WORKSPACE);
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
            Set<String> blocked = new HashSet<String>();
            Set<String> referencedNodes = new HashSet<String>();

            getBlockedAndReferencesList(stageNode, blocked, referencedNodes, languages);
            for (String referencedNode : referencedNodes) {
                if (!pathes.contains(referencedNode)) {
                    info.addReference(referencedNode, getPublicationInfo(referencedNode, user, languages, true, pathes));
                }
            }
        }
        if (publishedNode == null) {
            // node has not been published yet, check if parent is published
            try {
                liveSession.getNode(stageNode.getParent().getPath());
                info.setStatus(PublicationInfo.UNPUBLISHED);
            } catch (AccessDeniedException e) {
                info.setStatus(PublicationInfo.UNPUBLISHABLE);
            } catch (PathNotFoundException e) {
                info.setStatus(PublicationInfo.UNPUBLISHABLE);
            }
        } else {
            if (stageNode.getLastModifiedAsDate() == null) {
                logger.error("Null modifieddate for staged node " + stageNode.getPath());
                info.setStatus(PublicationInfo.MODIFIED);
            } else if (publishedNode.getLastPublishedAsDate() == null) {
                logger.error("Null lastPublishDate for published node " + stageNode.getPath());
                info.setStatus(PublicationInfo.MODIFIED);
            } else {
                long s = stageNode.getLastModifiedAsDate().getTime();
                long p = publishedNode.getLastPublishedAsDate().getTime();
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