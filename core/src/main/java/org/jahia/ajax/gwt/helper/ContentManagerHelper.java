/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaContentHistoryEntry;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.UploadedPendingFile;
import org.jahia.api.Constants;
import org.jahia.bin.SessionNamedDataStorage;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.history.ContentHistoryService;
import org.jahia.services.history.HistoryEntry;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.importexport.ReferencesHelper;
import org.jahia.services.importexport.validation.*;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.SourceControlManagement;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.visibility.VisibilityService;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.osgi.framework.BundleException;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.query.Query;
import javax.jcr.security.Privilege;
import javax.jcr.version.VersionException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.jahia.api.Constants.JAHIAMIX_MARKED_FOR_DELETION_ROOT;

/**
 * @author rfelden
 */
public class ContentManagerHelper {

    private static final List<String> COPIED_NODE_FIELDS = Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.TAGS, GWTJahiaNode.CHILDREN_INFO, "j:view", "j:width", "j:height", GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.PERMISSIONS);

    private static final List<String> NEW_NODE_FIELDS = Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.TAGS, GWTJahiaNode.CHILDREN_INFO, "j:view", "j:width", "j:height", GWTJahiaNode.LOCKS_INFO, GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO);

    private static Logger logger = LoggerFactory.getLogger(ContentManagerHelper.class);

    private JahiaSitesService sitesService;
    private ContentHistoryService contentHistoryService;
    private JahiaTemplateManagerService templateManagerService;

    private NavigationHelper navigation;
    private PropertiesHelper properties;
    private VersioningHelper versioning;

    private SessionNamedDataStorage<UploadedPendingFile> fileStorage;

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setProperties(PropertiesHelper properties) {
        this.properties = properties;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setVersioning(VersioningHelper versioning) {
        this.versioning = versioning;
    }

    public void setContentHistoryService(ContentHistoryService contentHistoryService) {
        this.contentHistoryService = contentHistoryService;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setFileStorage(SessionNamedDataStorage<UploadedPendingFile> fileStorage) {
        this.fileStorage = fileStorage;
    }

    /**
     * @deprecated : This method is used only to maintain compatibility with existing modules. Do not use it !
     */
    @Deprecated
    public JCRNodeWrapper addNode(JCRNodeWrapper parentNode, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, Locale uiLocale) throws GWTJahiaServiceException {
        return addNode(parentNode, name, nodeType, mixin, props, uiLocale, null);
    }

    public JCRNodeWrapper addNode(JCRNodeWrapper parentNode, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        if (!parentNode.hasPermission(Privilege.JCR_ADD_CHILD_NODES)) {
            throw new GWTJahiaServiceException(parentNode.getPath() + " - ACCESS DENIED");
        }
        JCRNodeWrapper childNode;
        try {
            parentNode.getSession().checkout(parentNode);
            childNode = parentNode.addNode(name, nodeType);
            if (mixin != null) {
                for (String m : mixin) {
                    childNode.addMixin(m);
                }
            }
            properties.setProperties(childNode, props, httpSessionID);
        } catch (Exception e) {
            logger.error("Exception", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.get.node", uiLocale, e.getLocalizedMessage()));
        }
        if (childNode == null) {
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.node.creation.failed", uiLocale));
        }
        return childNode;
    }

    /**
     * @deprecated : This method is used only to maintain compatibility with existing modules. Do not use it !
     */
    @Deprecated
    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, JCRSessionWrapper
            currentUserSession, Locale uiLocale, Map<String, String> parentNodesType, boolean forceCreation)  throws GWTJahiaServiceException {
        return createNode(parentPath, name, nodeType, mixin, props, currentUserSession, uiLocale, parentNodesType, forceCreation, null);
    }

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, JCRSessionWrapper currentUserSession, Locale uiLocale, Map<String, String> parentNodesType, boolean forceCreation, String httpSessionID)
            throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper parentNode = ensureParent(parentPath, currentUserSession, uiLocale, parentNodesType);

            String nodeName;

            if (name == null) {
                nodeName = findAvailableName(parentNode, nodeType.substring(nodeType.lastIndexOf(':') + 1));
            } else {
                nodeName = JCRContentUtils.escapeLocalNodeName(name);

                if (!forceCreation && checkExistence(parentPath + "/" + nodeName, currentUserSession, uiLocale)) {
                    throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.node.already.exists.with.name", uiLocale, nodeName));
                }
                nodeName = findAvailableName(parentNode, nodeName);
            }

            JCRNodeWrapper childNode = addNode(parentNode, nodeName, nodeType, mixin, props, uiLocale, httpSessionID);
            return navigation.getGWTJahiaNode(currentUserSession.getNode(childNode.getPath()), NEW_NODE_FIELDS);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(parentPath + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
        }
    }

    private JCRNodeWrapper ensureParent(String parentPath, JCRSessionWrapper currentUserSession, Locale uiLocale,
            Map<String, String> parentNodesType) throws RepositoryException, GWTJahiaServiceException,
            PathNotFoundException, NoSuchNodeTypeException, LockException, VersionException,
            ConstraintViolationException {
        JCRNodeWrapper parentNode;
        try {
            parentNode = currentUserSession.getNode(parentPath);
        } catch (PathNotFoundException e) {
            if (parentNodesType != null) {
                // we can create the intermediate parents
                String[] pathElements = StringUtils.split(parentPath, '/');
                JCRNodeWrapper current = currentUserSession.getRootNode();
                for (String pathElement : pathElements) {
                    try {
                        current = current.getNode(pathElement);
                    } catch (PathNotFoundException pnfe) {
                        String currentPath = current.getPath() + "/" + pathElement;
                        if (!parentNodesType.containsKey(currentPath)) {
                            throw new GWTJahiaServiceException(currentPath + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale));
                        }
                        current = current.addNode(pathElement, parentNodesType.get(currentPath));
                    }
                }
                parentNode = current;
            } else {
                throw e;
            }
        }
        return parentNode;
    }

    public String generateNameFromTitle(List<GWTJahiaNodeProperty> props) {
        String nodeName = null;
        for (GWTJahiaNodeProperty property : props) {
            if (property != null) {
                final List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
                if (property.getName().equals("jcr:title") && propertyValues != null && propertyValues.size() > 0 &&
                        propertyValues.get(0).getString() != null) {
                    nodeName = JCRContentUtils.generateNodeName(propertyValues.get(0).getString());
                }
            } else {
                logger.error("found a null property");
            }
        }
        return nodeName;
    }

    public GWTJahiaNode createFolder(String parentPath, String name, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID)
            throws GWTJahiaServiceException {
        JCRNodeWrapper parentNode;
        GWTJahiaNode newNode = null;
        final JCRSessionWrapper jcrSessionWrapper;
        try {
            jcrSessionWrapper = currentUserSession;
            parentNode = jcrSessionWrapper.getNode(parentPath);
            newNode = createNode(parentPath, name, parentNode.isNodeType("jnt:folder") ? "jnt:folder" : "jnt:contentList", null, null, currentUserSession, uiLocale, null, true, httpSessionID);
            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(parentPath + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
        }
        return newNode;
    }

    public String findAvailableName(JCRNodeWrapper dest, String name) {
        return JCRContentUtils.findAvailableNodeName(dest, name);
    }

    public boolean checkExistence(String path, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            return currentUserSession.nodeExists(JCRContentUtils.escapeNodePath(path));
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error", uiLocale, e.toString()));
        }
    }

    public void move(String sourcePath, String targetPath, JCRSessionWrapper currentUserSession) throws RepositoryException {
        currentUserSession.move(sourcePath, targetPath);
        currentUserSession.save();
    }

    public void moveAtEnd(String sourcePath, String targetPath, JCRSessionWrapper currentUserSession) throws RepositoryException, GWTJahiaServiceException {
        final JCRNodeWrapper srcNode = currentUserSession.getNode(sourcePath);
        final JCRNodeWrapper targetNode = currentUserSession.getNode(targetPath);
        currentUserSession.checkout(targetNode);

        if (srcNode.getParent().getPath().equals(targetNode.getPath())) {
            if (targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetNode.orderBefore(srcNode.getName(), null);
            }
        } else {
            currentUserSession.checkout(srcNode);
            currentUserSession.checkout(srcNode.getParent());
            String newname = findAvailableName(targetNode, srcNode.getName());
            currentUserSession.move(sourcePath, targetNode.getPath() + "/" + newname);
            if (targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetNode.orderBefore(newname, null);
            }
        }
        currentUserSession.save();
    }

    public void moveOnTopOf(String sourcePath, String targetPath, JCRSessionWrapper currentUserSession) throws RepositoryException, GWTJahiaServiceException {
        final JCRNodeWrapper srcNode = currentUserSession.getNode(sourcePath);
        final JCRNodeWrapper targetNode = currentUserSession.getNode(targetPath);
        final JCRNodeWrapper targetParent = targetNode.getParent();
        currentUserSession.checkout(targetParent);
        if (srcNode.getParent().getPath().equals(targetParent.getPath())) {
            if (targetParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetParent.orderBefore(srcNode.getName(), targetNode.getName());
            }
        } else {
            currentUserSession.checkout(srcNode);
            currentUserSession.checkout(srcNode.getParent());
            String newname = findAvailableName(targetParent, srcNode.getName());
            currentUserSession.move(sourcePath, targetParent.getPath() + "/" + newname);
            if (targetParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetParent.orderBefore(newname, targetNode.getName());
            }
        }
        currentUserSession.save();
    }

    public void checkWriteable(List<String> paths, JahiaUser user, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        try {
            List<String> missedPaths = new ArrayList<String>();
            for (String aNode : paths) {
                JCRNodeWrapper node;
                try {
                    node = currentUserSession.getNode(aNode);
                } catch (RepositoryException e) {
                    logger.error(e.toString(), e);
                    missedPaths.add(aNode + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
                    continue;
                }
                if (!node.hasPermission(Privilege.JCR_ADD_CHILD_NODES)) {
                    missedPaths.add(Messages.getInternalWithArguments("label.gwt.error.has.no.write.access.to", uiLocale, user.getUsername(), node.getName()));
                } else if (node.isLocked() && !node.getLockOwner().equals(user.getUsername())) {
                    missedPaths.add(node.getName() + Messages.getInternal("label.gwt.error.locked.by", uiLocale) + user.getUsername());
                }
            }
            if (missedPaths.size() > 0) {
                StringBuilder errors = new StringBuilder(Messages.getInternal("label.gwt.error.following.files.could.not.be.cut", uiLocale));
                for (String err : missedPaths) {
                    errors.append("\n").append(err);
                }
                throw new GWTJahiaServiceException(errors.toString());
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e);
        }
    }

    public List<GWTJahiaNode> copy(final List<String> pathsToCopy, final String destinationPath, final String newName,
                                   final boolean moveOnTop, final boolean cut, final boolean reference, boolean allLanguages,
                                   JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        return copy(pathsToCopy, destinationPath, newName, moveOnTop, cut, reference, null, allLanguages, currentUserSession,
                currentUserSession.getLocale());
    }

    public List<GWTJahiaNode> copy(final List<String> pathsToCopy, final String destinationPath, final String newName, final boolean moveOnTop,
                                   final boolean cut, final boolean reference,final List<String> childNodeTypesToSkip, boolean allLanguages,
                                   JCRSessionWrapper currentUserSession, final Locale uiLocale) throws GWTJahiaServiceException {
        final List<String> missedPaths = new ArrayList<String>();
        final List<GWTJahiaNode> res = new ArrayList<GWTJahiaNode>();

        // perform a check to prevent pasting content to itself or its children
        for (Iterator<String> iterator = pathsToCopy.iterator(); iterator.hasNext(); ) {
            String toCopy = iterator.next();
            if (destinationPath.equals(toCopy) || destinationPath.startsWith(toCopy + "/")) {
                    missedPaths.add(Messages.getInternalWithArguments("failure.paste.cannot.paste", uiLocale,
                            "Content {0} cannot be pasted into {1}", toCopy, destinationPath));
                iterator.remove();
            }
        }
        if (!missedPaths.isEmpty() && pathsToCopy.isEmpty()) {
            throw new GWTJahiaServiceException(StringUtils.join(missedPaths, "\n"));
        }

        try {
            JCRCallback<List<String>> callback = new JCRCallback<List<String>>() {
                public List<String> doInJCR(JCRSessionWrapper session) throws RepositoryException {

                    List<String> res = new ArrayList<String>();


                    final JCRNodeWrapper targetParent;
                    JCRNodeWrapper targetNode;

                    targetNode = session.getNode(destinationPath);

                    if (moveOnTop) {
                        targetParent = targetNode.getParent();
                    } else {
                        targetParent = targetNode;
                    }

                    for (String aNode : pathsToCopy) {
                        JCRNodeWrapper node = session.getNode(aNode);
                        String name = newName != null ? newName : node.getName();
                        try {
                            name = findAvailableName(targetParent, name);
                            if (targetParent.hasPermission("jcr:addChildNodes") && !targetParent.isLocked()) {
                                final JCRNodeWrapper copy = doPaste(targetParent, node, name, cut, reference, childNodeTypesToSkip);

                                if (moveOnTop && targetParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                                    targetParent.orderBefore(name, targetNode.getName());
                                }
                                session.save();
                                res.add(copy.getIdentifier());
                            } else {
                                missedPaths.add("File " + name + " could not be referenced in " + targetParent.getPath());
                            }
                        } catch (RepositoryException e) {
                            logger.error("Exception", e);
                            if(cut)
                            {
                                missedPaths.add(Messages.getInternalWithArguments("failure.cut.cannot.cut",
                                        uiLocale,
                                        name, targetParent.getPath() , node.getPath(), session.getUser().getName()));
                            }
                            else {
                                missedPaths.add("File " + name + " could not be referenced in " + targetParent.getPath());
                            }
                        } catch (JahiaException e) {
                            logger.error("Exception", e);
                            missedPaths.add("File " + name + " could not be referenced in " + targetParent.getPath());
                        }
                    }

                    return res;
                }
            };

            List<String> uuids;
            if (allLanguages) {
                uuids = JCRTemplate.getInstance().doExecute(currentUserSession.getUser(), currentUserSession.getWorkspace().getName(), null, callback);
            } else {
                uuids = callback.doInJCR(currentUserSession);
            }

            if (missedPaths.size() > 0) {
                StringBuilder errors = new StringBuilder();
                if(cut)
                {
                    for (String err : missedPaths) {
                        errors.append("\n").append(err);
                    }
                }
                else {
                    errors.append("The following files could not have their reference pasted:");
                    for (String err : missedPaths) {
                        errors.append("\n").append(err);
                    }
                }
                throw new GWTJahiaServiceException(errors.toString());
            }

            for (String uuid : uuids) {
                res.add(navigation.getGWTJahiaNode(currentUserSession.getNodeByUUID(uuid), COPIED_NODE_FIELDS));
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e);
        }
        return res;
    }

    private JCRNodeWrapper doPaste(JCRNodeWrapper targetNode, JCRNodeWrapper node, String name, boolean cut,
                                   boolean reference, List<String> childNodeTypesToSkip) throws RepositoryException, JahiaException {
        targetNode.checkout();
        if (cut) {
            node.getSession().checkout(node);
            node.getParent().getSession().checkout(node.getParent());
            targetNode.getSession().move(node.getPath(), targetNode.getPath() + "/" + name);
        } else if (reference) {
            targetNode.getSession().checkout(targetNode);
            if (targetNode.getPrimaryNodeTypeName().equals("jnt:members")) {
                if (node.getPrimaryNodeTypeName().equals("jnt:user")) {
                    Node member = targetNode.addNode(name, Constants.JAHIANT_MEMBER);
                    member.setProperty("j:member", node.getIdentifier());
                } else if (node.getPrimaryNodeTypeName().equals("jnt:group")) {
                    Node node1 = node.getParent().getParent();
                    String id = "systemsite";
                    if (node1 != null && node1.getPrimaryNodeType().getName().equals(Constants.JAHIANT_VIRTUALSITE)) {
                        id = sitesService.getSiteByKey(node1.getName()).getSiteKey();
                    }
                    name += ("___" + id);
                    Node member = targetNode.addNode(name, Constants.JAHIANT_MEMBER);
                    member.setProperty("j:member", node.getIdentifier());
                }
            } else {
                Node ref = targetNode.addNode(name, "jnt:contentReference");
                ref.setProperty(Constants.NODE, node.getIdentifier());
            }
        } else {
            JCRSiteNode sourceSite = node.getResolveSite();
            JCRSiteNode targetSite = targetNode.getResolveSite();
            if (!sourceSite.equals(targetSite)) {
                JCRSessionWrapper session = node.getSession();
                Query q = session.getWorkspace().getQueryManager().createQuery("select * from [jnt:template] as t where isdescendantnode(t, ['" + JCRContentUtils.sqlEncode(sourceSite.getPath()) + "/templates'])", Query.JCR_SQL2);
                NodeIterator ni = q.execute().getNodes();
                while (ni.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) ni.next();
                    try {
                        session.getUuidMapping().put(next.getIdentifier(), session.getNode(targetSite.getPath() + StringUtils.substringAfter(next.getPath(), sourceSite.getPath())).getIdentifier());
                    } catch (RepositoryException e) {
                        logger.debug("No matching template for copy", e);
                    }
                }
            }
            if (childNodeTypesToSkip == null) {
                node.copy(targetNode, name, true, null, SettingsBean.getInstance().getImportMaxBatch());
            } else {
                String newName = JCRContentUtils.findAvailableNodeName(targetNode, name);
                JCRNodeWrapper newNode = targetNode.addNode(newName, node.getPrimaryNodeTypeName());
                for (ExtendedNodeType mixin : node.getMixinNodeTypes()) {
                    if (!Constants.forbiddenMixinToCopy.contains(mixin.getName())) {
                        newNode.addMixin(mixin.getName());
                    }
                }
                Map<String, List<String>> references = new HashMap<String, List<String>>();
                node.copyProperties(newNode, references);
                ReferencesHelper.resolveCrossReferences(node.getSession(), references, false);

                for (JCRNodeWrapper childNode : node.getNodes()) {
                    childNode.copy(newNode, childNode.getName(), true, childNodeTypesToSkip, SettingsBean.getInstance().getImportMaxBatch());
                }
            }
        }
        return targetNode.getNode(name);
    }

    public void deletePaths(List<String> paths, boolean permanentlyDelete, String comment, JahiaUser user, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper nodeToDelete = null;
            try {
                nodeToDelete = currentUserSession.getNode(path);
                if (!currentUserSession.getUserNode().isRoot() && nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(user.getUsername())) {
                    if (nodeToDelete.isNodeType(JAHIAMIX_MARKED_FOR_DELETION_ROOT) && nodeToDelete.hasPermission(Privilege.JCR_REMOVE_NODE)) {
                        nodeToDelete.unmarkForDeletion();
                    } else {
                        missedPaths.add(nodeToDelete.getPath() + " - locked by " + nodeToDelete.getLockOwner());
                    }
                }
                if (!nodeToDelete.hasPermission(Privilege.JCR_REMOVE_NODE)) {
                    missedPaths.add(nodeToDelete.getPath() + " - ACCESS DENIED");
                } else if (!getRecursedLocksAndFileUsages(nodeToDelete, missedPaths, user.getUsername())) {
                    if (!permanentlyDelete && supportsMarkingForDeletion(nodeToDelete)) {
                        nodeToDelete.markForDeletion(comment);
                    } else {
                        nodeToDelete.getParent().getSession().checkout(nodeToDelete.getParent());
                        nodeToDelete.remove();
                    }

                    nodeToDelete.saveSession();
                }
            } catch (PathNotFoundException e) {
                missedPaths.add(path + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
            } catch (AccessDeniedException e) {
                missedPaths.add((nodeToDelete != null ? nodeToDelete.getPath() : "") + " - ACCESS DENIED");
            } catch (ReferentialIntegrityException e) {
                missedPaths.add((nodeToDelete != null ? nodeToDelete.getPath() : "") + " - is in use");
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException(e);
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(Messages.getInternal("label.error.nodes.not.deleted", uiLocale, "The following nodes could not be deleted:"));
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public void undeletePaths(List<String> paths, JahiaUser user, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper nodeToUndelete = null;
            try {
                nodeToUndelete = currentUserSession.getNode(path);
                if (!nodeToUndelete.hasPermission(Privilege.JCR_REMOVE_NODE)) {
                    missedPaths.add(nodeToUndelete.getPath() + " - ACCESS DENIED");
                } else {
                    nodeToUndelete.unmarkForDeletion();
                    nodeToUndelete.saveSession();
                }
            } catch (PathNotFoundException e) {
                missedPaths.add(path + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
            } catch (AccessDeniedException e) {
                missedPaths.add((nodeToUndelete != null ? nodeToUndelete.getPath() : "") + " - ACCESS DENIED");
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException(e);
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(Messages.getInternal("label.error.nodes.not.deleted", uiLocale, "The following nodes could not be undeleted:"));
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    private boolean supportsMarkingForDeletion(JCRNodeWrapper nodeToDelete) throws RepositoryException {
        return nodeToDelete.canMarkForDeletion();
    }

    public GWTJahiaNode rename(String path, String newName, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(path + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
        }
        try {
            if (node.isLocked() && !node.getLockOwner().equals(currentUserSession.getUser().getName())) {
                throw new GWTJahiaServiceException(node.getName() + Messages.getInternal("label.gwt.error.locked.by", uiLocale) + currentUserSession.getUser().getName());
            } else if (!node.hasPermission(Privilege.JCR_WRITE)) {
                throw new GWTJahiaServiceException(node.getName() + " - ACCESS DENIED");
            } else if (!node.rename(JCRContentUtils.escapeLocalNodeName(newName))) {
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.rename", uiLocale, node.getName(), newName));
            }
        } catch (ItemExistsException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.already.exists", uiLocale, newName));
        } catch (ConstraintViolationException e) {
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.rename",
                    uiLocale, node.getName(), newName)
                    + ". "
                    + Messages.getInternal("label.cause", uiLocale)
                    + ": "
                    + e.getLocalizedMessage());
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.rename", uiLocale, node.getName(), newName));
        }
        try {
            node.saveSession();
            return navigation.getGWTJahiaNode(currentUserSession.getNode(node.getPath()), NEW_NODE_FIELDS);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not save file " + node.getName() + " into " + newName);
        }
    }

    public void importContent(String parentPath, String fileKey, JCRSessionWrapper session, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        importContent(parentPath, fileKey, false, session, uiLocale, httpSessionID);
    }

    public void importContent(String parentPath, String fileKey, boolean replaceContent, JCRSessionWrapper session, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {

        try {

            UploadedPendingFile item = fileStorage.getRequired(httpSessionID, fileKey);
            try {

                ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
                JCRNodeWrapper parent = session.getNode(parentPath);
                JCRSiteNode resolveSite = parent.getResolveSite();
                String detectedContentType = ImportExportBaseService.detectImportContentType(item.getContentType(), fileKey);
                InputStream itemStream = null;
                ValidationResults results;
                try {
                    itemStream = item.getContentStream();
                    results = importExport.validateImportFile(session, itemStream, detectedContentType, resolveSite != null ? resolveSite.getInstalledModules() : null);
                } finally {
                    IOUtils.closeQuietly(itemStream);
                }

                if (results.isSuccessful()) {
                    try {
                        // First let's copy the file in the JCR
                        JCRNodeWrapper privateFilesFolder = JCRContentUtils.getInstance().getUserPrivateFilesFolder(session);
                        String importFilename = "import" + Math.random() * 1000;
                        itemStream = item.getContentStream();
                        JCRNodeWrapper jcrNodeWrapper = privateFilesFolder.uploadFile(importFilename, itemStream, detectedContentType);
                        session.save();
                        // let's schedule an import job.
                        JobDetail jobDetail = BackgroundJob.createJahiaJob(Messages.getInternal("import.file", uiLocale, "Import file") + " " + FilenameUtils.getName(fileKey), ImportJob.class);
                        JobDataMap jobDataMap;
                        jobDataMap = jobDetail.getJobDataMap();
                        jobDataMap.put(ImportJob.DESTINATION_PARENT_PATH, parentPath);
                        jobDataMap.put(ImportJob.URI, jcrNodeWrapper.getPath());
                        jobDataMap.put(ImportJob.FILENAME, FilenameUtils.getName(fileKey));
                        jobDataMap.put(ImportJob.REPLACE_CONTENT, replaceContent);
                        ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
                    } finally {
                        IOUtils.closeQuietly(itemStream);
                    }
                } else {
                    StringBuilder buffer = new StringBuilder();
                    for (ValidationResult result : results.getResults()) {
                        if (!result.isSuccessful()) {
                            if (result instanceof MissingModulesValidationResult) {
                                MissingModulesValidationResult missingModule = ((MissingModulesValidationResult) result);
                                if (missingModule.isTargetTemplateSetPresent()) {
                                    buffer.append(Messages.getInternalWithArguments("failure.import.missingTemplateSet", uiLocale, missingModule.getTargetTemplateSet()));
                                }
                                if (!missingModule.getMissingModules().isEmpty()) {
                                    buffer.append(Messages.getInternalWithArguments("failure.import.missingModules", uiLocale, missingModule.getMissingModules().size())).append(missingModule.getMissingModules());
                                }
                            } else if (result instanceof MissingNodetypesValidationResult) {
                                buffer.append(Messages.getInternalWithArguments("failure.import.missingNodetypes", uiLocale, ((MissingNodetypesValidationResult) result).getMissingNodetypes(), ((MissingNodetypesValidationResult) result).getMissingMixins()));
                            } else if (result instanceof MissingTemplatesValidationResult) {
                                MissingTemplatesValidationResult missingTemplates = ((MissingTemplatesValidationResult) result);
                                buffer.append(Messages.getInternalWithArguments("failure.import.missingTemplates", uiLocale, missingTemplates.getMissingTemplates().size())).append(missingTemplates.getMissingTemplates().keySet());
                            }
                        }
                    }
                    throw new GWTJahiaServiceException(buffer.toString());
                }
            } finally {
                item.close();
            }
        } catch (GWTJahiaServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Error when importing", e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.could.not.import", uiLocale, e.getLocalizedMessage()));
        }
    }

    public GWTJahiaNodeACL getACL(String path, boolean newAcl, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(path + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
        }
        Map<String, List<String[]>> m = node.getAclEntries();

        GWTJahiaNodeACL acl = new GWTJahiaNodeACL();

        try {
            Map<String, List<JCRNodeWrapper>> roles = node.getAvailableRoles();
            Map<String, List<String>> dependencies = new HashMap<String, List<String>>();

            Map<String, List<String>> availablePermissions = new HashMap<String, List<String>>();
            Set<String> allAvailablePermissions = new HashSet<String>();
            Map<String, String> labels = new HashMap<String, String>();
            Map<String, String> tooltips = new HashMap<String, String>();

            for (Map.Entry<String, List<JCRNodeWrapper>> entry : roles.entrySet()) {
                availablePermissions.put(entry.getKey(), new ArrayList<String>());
                for (JCRNodeWrapper nodeWrapper : entry.getValue()) {
                    String nodeName = nodeWrapper.getName();
                    allAvailablePermissions.add(nodeName);
                    availablePermissions.get(entry.getKey()).add(nodeName);
                    String label = nodeWrapper.getDisplayableName();
                    labels.put(nodeName, StringUtils.isEmpty(label)?nodeName:label);
                    if (nodeWrapper.hasProperty("jcr:description")) {
                        tooltips.put(nodeName, nodeWrapper.getProperty("jcr:description").getString());
                    }
                    List<String> d = new ArrayList<String>();
                    if (nodeWrapper.hasProperty("j:dependencies")) {
                        for (Value value : nodeWrapper.getProperty("j:dependencies").getValues()) {
                            d.add(((JCRValueWrapper) value).getNode().getName());
                        }
                    }
                    dependencies.put(nodeName, d);
                }
            }
            acl.setAvailableRoles(availablePermissions);
            acl.setRolesLabels(labels);
            acl.setRolesTooltips(tooltips);
            acl.setRoleDependencies(dependencies);

            List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();
            Map<String, GWTJahiaNodeACE> map = new HashMap<String, GWTJahiaNodeACE>();

            JahiaGroupManagerService groupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            for (String principal : m.keySet()) {
                GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
                ace.setPrincipalType(principal.charAt(0));
                ace.setPrincipal(principal.substring(2)); // we set this even if we can't lookup the principal
                if (ace.getPrincipalType() == 'g') {
                    JCRGroupNode g = groupManagerService.lookupGroup(node.getResolveSite().getSiteKey(), ace.getPrincipal());
                    if (g == null) {
                        g = groupManagerService.lookupGroup(null,ace.getPrincipal());
                    }
                    if (g != null) {
                        ace.setHidden(g.isHidden());
                        ace.setPrincipalKey(g.getPath());
                        String groupName = g.getDisplayableName(uiLocale);
                        ace.setPrincipalDisplayName(groupName);
                    } else {
                        continue;
                    }
                } else {
                    JCRUserNode u = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(ace.getPrincipal(), node.getResolveSite().getName());
                    if (u != null) {
                        ace.setPrincipalKey(u.getPath());
                        String userName = u.getDisplayableName(uiLocale);
                        ace.setPrincipalDisplayName(userName);
                    } else {
                        continue;
                    }
                }

                map.put(principal, ace);

                List<String[]> st = m.get(principal);
                Map<String, Boolean> perms = new HashMap<String, Boolean>();
                Map<String, Boolean> inheritedPerms = new HashMap<String, Boolean>();
                String inheritedFrom = null;
                for (String[] strings : st) {
                    String pathFrom = strings[0];
                    String aclType = strings[1];
                    if (aclType.equals("GRANT") || aclType.equals("DENY")) {
                        String role = strings[2];
                        if (!newAcl && path.equals(pathFrom)) {
                            perms.put(role, aclType.equals("GRANT"));
                        } else if (!inheritedPerms.containsKey(role)) {
                            if (inheritedFrom == null || inheritedFrom.length() < pathFrom.length()) {
                                inheritedFrom = pathFrom;
                            }
                            inheritedPerms.put(role, aclType.equals("GRANT"));
                        }
                    }
                }
                if (!CollectionUtils.intersection(allAvailablePermissions, inheritedPerms.keySet()).isEmpty() ||
                        !CollectionUtils.intersection(allAvailablePermissions, perms.keySet()).isEmpty()) {
                    aces.add(ace);
                    ace.setInheritedFrom(inheritedFrom);
                    ace.setInheritedRoles(inheritedPerms);
                    ace.setRoles(perms);
                    ace.setInherited(perms.isEmpty());
                }
            }

            boolean aclInheritanceBreak = node.getAclInheritanceBreak();
            acl.setBreakAllInheritance(aclInheritanceBreak);

            if (aclInheritanceBreak) {
                m = node.getParent().getAclEntries();
                for (String principal : m.keySet()) {
                    GWTJahiaNodeACE ace = map.get(principal);
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        map.put(principal, ace);
                        aces.add(ace);
                        ace.setPrincipalType(principal.charAt(0));
                        ace.setPrincipal(principal.substring(2)); // we set this even if we can't lookup the principal
                        if (ace.getPrincipalType() == 'g') {
                            JCRGroupNode g = groupManagerService.lookupGroup(node.getResolveSite().getSiteKey(),
                                    ace.getPrincipal());
                            if (g == null) {
                                g = groupManagerService.lookupGroup(null,ace.getPrincipal());
                            }
                            if (g != null) {
                                ace.setHidden(g.isHidden());
                                String groupName = g.getDisplayableName(uiLocale);
                                ace.setPrincipalKey(g.getPath());
                                ace.setPrincipalDisplayName(groupName);
                            }
                        } else {
                            JCRUserNode u = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(ace.getPrincipal(), node.getResolveSite().getName());
                            if (u != null) {
                                ace.setPrincipalKey(u.getPath());
                                String userName = u.getDisplayableName(uiLocale);
                                ace.setPrincipalDisplayName(userName);
                            }
                        }
                        ace.setRoles(new HashMap<String, Boolean>());
                    }
                    Map<String, Boolean> inheritedPerms = new HashMap<String, Boolean>();

                    List<String[]> st = m.get(principal);
                    String inheritedFrom = null;
                    for (String[] strings : st) {
                        String pathFrom = strings[0];
                        String aclType = strings[1];
                        if (aclType.equals("GRANT") || aclType.equals("DENY")) {
                            String role = strings[2];
                            if (!inheritedPerms.containsKey(role)) {
                                if (inheritedFrom == null || inheritedFrom.length() < pathFrom.length()) {
                                    inheritedFrom = pathFrom;
                                }
                                inheritedPerms.put(role, aclType.equals("GRANT"));
                            }
                        }
                    }

                    ace.setInheritedFrom(inheritedFrom);
                    ace.setInheritedRoles(inheritedPerms);
                }
            }
            acl.setAce(aces);

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return acl;
    }

    public void setACL(String uuid, GWTJahiaNodeACL acl, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNodeByUUID(uuid);
            if (!node.isCheckedOut()) {
                currentUserSession.checkout(node);
            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(uuid + " could not be accessed :\n" + e.toString());
        }
        GWTJahiaNodeACL oldAcl = getACL(node.getPath(), false, currentUserSession, null);
        if (oldAcl.equals(acl)) {
            return;
        }
        try {
            Map<String, GWTJahiaNodeACE> oldPrincipals = new HashMap<String, GWTJahiaNodeACE>();
            for (GWTJahiaNodeACE ace : oldAcl.getAce()) {
                if (!ace.getRoles().isEmpty()) {
                    oldPrincipals.put(ace.getPrincipalType() + ":" + ace.getPrincipal(), ace);
                }
            }
            for (GWTJahiaNodeACE ace : acl.getAce()) {
                String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                if (!ace.getRoles().isEmpty()) {
                    Map<String, String> perms = new HashMap<String, String>();
                    GWTJahiaNodeACE oldAce = oldPrincipals.get(user);
                    if (!ace.equals(oldAce)) {
                        for (Map.Entry<String, Boolean> entry : ace.getRoles().entrySet()) {
                            if (entry.getValue().equals(Boolean.TRUE) && (!Boolean.TRUE.equals(ace.getInheritedRoles().get(entry.getKey())) || acl.isBreakAllInheritance())) {
                                perms.put(entry.getKey(), "GRANT");
                            } else if (entry.getValue().equals(Boolean.FALSE) && (Boolean.TRUE.equals(ace.getInheritedRoles().get(entry.getKey())) || acl.isBreakAllInheritance())) {
                                perms.put(entry.getKey(), "DENY");
                            } else {
                                perms.put(entry.getKey(), "REMOVE");
                            }
                        }
                        for (Map.Entry<String, Boolean> entry : ace.getInheritedRoles().entrySet()) {
                            if (!entry.getValue() && !perms.containsKey(entry.getKey())) {
                                perms.put(entry.getKey(), "REMOVE");
                            }
                        }
                        node.changeRoles(user, perms);
                    }
                }
            }
            node.setAclInheritanceBreak(acl.isBreakAllInheritance());
        } catch (RepositoryException e) {
            logger.error("Error while saving acl on node "+node.getPath(), e);
            throw new GWTJahiaServiceException("Could not save acl on node " + node.getPath());
        }
    }

    private boolean getRecursedLocksAndFileUsages(JCRNodeWrapper nodeToDelete, List<String> lockedNodes,
                                                  String username) throws GWTJahiaServiceException {
        try {
            for (NodeIterator iterator = nodeToDelete.getNodes(); iterator.hasNext(); ) {
                JCRNodeWrapper child = (JCRNodeWrapper) iterator.next();
                getRecursedLocksAndFileUsages(child, lockedNodes, username);
                if (lockedNodes.size() >= 10) {
                    // do not check further
                    return true;
                }
            }
            if (nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(username)) {
                Set<JCRNodeLockType> lockTypes = JCRContentUtils.getLockTypes(nodeToDelete.getLockInfos());
                if (lockTypes.size() != 1 || !lockTypes.contains(JCRNodeLockType.DELETION)) {
                    lockedNodes.add(nodeToDelete.getPath() + " - locked by " + nodeToDelete.getLockOwner());
                }
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException("Cannot get lock information", e);
        }
        return !lockedNodes.isEmpty();
    }

    public void clearAllLocks(String path, boolean processChildNodes, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            if (currentUserSession.getUserNode().isRoot()) {
                JCRContentUtils.clearAllLocks(path, processChildNodes, currentUserSession.getWorkspace().getName());
            } else {
                JCRNodeWrapper node = currentUserSession.getNode(path);
                Map<String, List<String>> lockInfos = node.getLockInfos();
                List<String> locks = lockInfos.get(null);
                if (locks != null) {
                    for (String lock : locks) {
                        if (StringUtils.substringBefore(lock, ":").equals(currentUserSession.getUserID())) {
                            node.unlock(StringUtils.substringAfter(lock, ":"));
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("Repository error when clearing all locks on node " + path, e);
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.when.clearing.all.locks.on.node", uiLocale, path, currentUserSession.getUser().getUserKey()));
        }
    }

    public void setLock(List<String> paths, boolean toLock, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JahiaUser user = currentUserSession.getUser();
        List<String> missedPaths = new ArrayList<String>();
        List<JCRNodeWrapper> nodes = new ArrayList<JCRNodeWrapper>();
        for (String path : paths) {
            JCRNodeWrapper node;
            try {
                node = currentUserSession.getNode(path);
                addSub(nodes, node);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(path + " could not be accessed : " + e.toString());
            }
        }
        for (JCRNodeWrapper node : nodes) {
            try {
                if (!node.hasPermission(Privilege.JCR_LOCK_MANAGEMENT)) {
                    missedPaths.add(node.getName() + ": write access denied");
                } else if (node.getLockedLocales().contains(currentUserSession.getLocale()) || (node.getLockedLocales().isEmpty() && node.isLocked())) {
                    if (!toLock) {
                        try {
                            node.unlock("user");
                        } catch (LockException e) {
                            logger.error(e.toString(), e);
                            missedPaths.add(node.getName() + ": repository exception");
                        }
                    } else {
                        String lockOwner = node.getLockOwner();
                        if (lockOwner != null && !lockOwner.equals(user.getName())) {
                            missedPaths.add(node.getName() + ": locked by " + lockOwner);
                        }
                    }
                } else {
                    if (toLock) {
                        if (!node.lockAndStoreToken("user")) {
                            missedPaths.add(node.getName() + ": repository exception");
                        }
                    } else if (node.isLocked()) {
                        node.unlock("user");
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(node.getName() + ": repository exception");
            }
        }
        try {
            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not save session");
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors =
                    new StringBuilder("The following files could not be ").append(toLock ? "locked:" : "unlocked:");
            for (String missedPath : missedPaths) {
                errors.append("\n").append(missedPath);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    private void addSub(List<JCRNodeWrapper> nodes, JCRNodeWrapper node) throws RepositoryException {
        if (!nodes.contains(node)) {
            nodes.add(node);
        }
        NodeIterator ni = node.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper subNode = (JCRNodeWrapper) ni.next();
            if (subNode.isNodeType("jnt:content")) {
                addSub(nodes, subNode);
            }
        }
    }

    /**
     * Upload file depending on operation (add version, auto-rename or just upload)
     *
     * @param location
     * @param tmpName
     * @param operation
     * @param newName
     * @param currentUserSession
     * @throws GWTJahiaServiceException
     */
    public void uploadedFile(String location, String tmpName, int operation, String newName, JCRSessionWrapper currentUserSession, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper parent = currentUserSession.getNode(location);
            if (2 == operation) {
                JCRNodeWrapper node = parent.getNode(newName);
                if (node == null) {
                    throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.new.version.file.not.found", uiLocale, location, newName));
                }
                versioning.addNewVersionFile(node, tmpName, httpSessionID);
            } else {
                if (1 == operation) {
                    newName = findAvailableName(parent, newName);
                }
                if (parent.hasNode(newName)) {
                    throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.file.exists", uiLocale, newName));
                }
                UploadedPendingFile item = fileStorage.getRequired(httpSessionID, tmpName);
                try {
                    InputStream is = null;
                    try {
                        is = item.getContentStream();
                        parent.uploadFile(newName, is, JCRContentUtils.getMimeType(newName, item.getContentType()));
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                } finally {
                    item.close();
                    fileStorage.remove(httpSessionID, tmpName);
                }
            }
            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void markConflictAsResolved(String moduleId, GWTJahiaNode node, JCRSessionWrapper session) throws IOException, RepositoryException, BundleException {
        JahiaTemplatesPackage templatePackage = templateManagerService.getTemplatePackageById(moduleId);
        SourceControlManagement sourceControl = templatePackage.getSourceControl();
        if (sourceControl != null) {
            String path = node.getPath();
            path = path.substring(path.indexOf("/sources/") + "/sources".length());
            String sourcesFolderPath = templatePackage.getSourcesFolder().getAbsolutePath();
            sourceControl.markConflictAsResolved(new File(sourcesFolderPath + path));
        }
    }

    public GWTJahiaNode sendToSourceControl(String moduleId, String scmURI, String scmType, JCRSessionWrapper session) throws IOException, RepositoryException {
        templateManagerService.sendToSourceControl(moduleId, scmURI, scmType, session);
        return navigation.getGWTJahiaNode(session.getNode("/modules/" + moduleId), GWTJahiaNode.DEFAULT_SITE_FIELDS);
    }

    public List<GWTJahiaContentHistoryEntry> getContentHistory(JCRSessionWrapper session, String nodeIdentifier, int offset, int limit) throws RepositoryException {
        JCRNodeWrapper node = session.getNodeByIdentifier(nodeIdentifier);
        List<HistoryEntry> historyEntryList = contentHistoryService.getNodeHistory(node, true);
        List<GWTJahiaContentHistoryEntry> result = new ArrayList<GWTJahiaContentHistoryEntry>();
        for (HistoryEntry historyEntry : historyEntryList) {
            result.add(convertToGWTJahiaContentHistoryEntry(historyEntry));
        }
        Collections.sort(result, new Comparator<GWTJahiaContentHistoryEntry>() {
            public int compare(GWTJahiaContentHistoryEntry o1, GWTJahiaContentHistoryEntry o2) {
                return o2.compareTo(o1);
            }
        });

        return result;
    }

    private GWTJahiaContentHistoryEntry convertToGWTJahiaContentHistoryEntry(HistoryEntry historyEntry) {
        String languageCode = null;
        if (historyEntry.getLocale() != null) {
            languageCode = historyEntry.getLocale().toString();
        }
        return new GWTJahiaContentHistoryEntry(historyEntry.getDate() > 0 ? new Date(historyEntry.getDate()) : null, historyEntry.getAction(), historyEntry.getPropertyName(), historyEntry.getUserKey(), historyEntry.getPath(), historyEntry.getMessage(), languageCode);
    }

    public void deleteReferences(String path, JahiaUser user, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        JCRNodeWrapper referencedNode;
        try {
            referencedNode = currentUserSession.getNode(path);
            if (referencedNode != null) {
                if (!currentUserSession.getUserNode().isRoot() && referencedNode.isLocked() && !referencedNode.getLockOwner().equals(user.getUsername())) {
                    missedPaths.add(referencedNode.getPath() + " - locked by " + referencedNode.getLockOwner());
                }
                if (!referencedNode.hasPermission(Privilege.JCR_REMOVE_NODE)) {
                    missedPaths.add(referencedNode.getPath() + " - ACCESS DENIED");
                } else if (!getRecursedLocksAndFileUsages(referencedNode, missedPaths, user.getUsername())) {
                    try {
                        String referenceToRemove = referencedNode.getIdentifier();
                        PropertyIterator r = referencedNode.getWeakReferences();
                        while (r.hasNext()) {
                            JCRPropertyWrapper reference = (JCRPropertyWrapper) r.next();
                            if (!reference.getPath().startsWith("/referencesKeeper")) {
                                if (!reference.getDefinition().isMandatory()) {
                                    if (reference.getDefinition().isMultiple()) {
                                        Value[] values = reference.getValues();
                                        if (values.length > 1) {
                                            Value[] valuesCopy = new Value[values.length - 1];
                                            int i = 0;
                                            for (Value value : values) {
                                                if (!value.getString().equals(referenceToRemove)) {
                                                    valuesCopy[i++] = value;
                                                }
                                            }
                                            reference.setValue(valuesCopy);
                                        } else {
                                            reference.remove();
                                        }
                                    } else {
                                        reference.remove();
                                    }
                                } else {
                                    missedPaths.add(referencedNode.getPath() + " - is use in a mandatory property of " + reference.getParent().getPath());
                                }
                            }
                        }
                        currentUserSession.save();
                    } catch (AccessDeniedException e) {
                        missedPaths.add(referencedNode.getPath() + " - ACCESS DENIED");
                    } catch (ReferentialIntegrityException e) {
                        missedPaths.add(referencedNode.getPath() + " - is in use");
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                        missedPaths.add(referencedNode.getPath() + " - UNSUPPORTED");
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            missedPaths.add(path + Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale) + e.toString());
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(Messages.getInternal(
                    "label.error.nodes.not.deleted", uiLocale));
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }


    public void saveVisibilityConditions(GWTJahiaNode node, List<GWTJahiaNode> conditions, JCRSessionWrapper session, Locale uiLocale, String httpSessionID) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper parent = session.getNode(node.getPath());

            if (!conditions.isEmpty() && !parent.hasNode(VisibilityService.NODE_NAME)) {
                parent.addNode(VisibilityService.NODE_NAME, "jnt:conditionalVisibility");
            }

            String path = node.getPath() + "/" + VisibilityService.NODE_NAME;

            for (GWTJahiaNode condition : conditions) {
                List<GWTJahiaNodeProperty> props = condition.<List<GWTJahiaNodeProperty>>get("gwtproperties");
                if (condition.get("new-node") != null) {
                    GWTJahiaNode n = createNode(path, condition.getName(), condition.getNodeTypes().get(0), new ArrayList<String>(), props, session, uiLocale, null, true, httpSessionID);
                    condition.setUUID(n.getUUID());
                    condition.setPath(n.getPath());
                } else {
                    JCRNodeWrapper jcrCondition = session.getNode(condition.getPath());
                    properties.setProperties(jcrCondition, props, httpSessionID);
                }
            }
            try {
                session.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.occur.when.trying.to.save.the.node", uiLocale, node.getPath() + " (saveVisibilityConditions)"));
            }
            for (GWTJahiaNode condition : conditions) {
                if (condition.get("node-removed") != null) {
                    JCRNodeWrapper jcrCondition = session.getNode(condition.getPath());
                    jcrCondition.remove();
                }
            }

            if (parent.hasNode(VisibilityService.NODE_NAME)) {
                JCRNodeWrapper wrapper = parent.getNode(VisibilityService.NODE_NAME);
                if (node.get("node-visibility-forceMatchAllConditions") != null) {
                    wrapper.setProperty("j:forceMatchAllConditions", (Boolean) node.get("node-visibility-forceMatchAllConditions"));
                }
                if (!wrapper.hasNodes()) {
                    wrapper.markForDeletion("");
                }
            }
            session.save();
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(e);
        }
    }
}
