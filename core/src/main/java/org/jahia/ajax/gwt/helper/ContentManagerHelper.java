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

package org.jahia.ajax.gwt.helper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaContentHistoryEntry;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.history.ContentHistoryService;
import org.jahia.services.history.HistoryEntry;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import javax.jcr.*;
import javax.jcr.security.Privilege;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author rfelden
 * @version 20 juin 2008 - 12:49:42
 */
public class ContentManagerHelper {

    private static final String MODULE_SKELETONS = "WEB-INF/etc/repository/${type}.xml,WEB-INF/etc/repository/${type}-*.xml,modules/**/META-INF/${type}-skeleton.xml,modules/**/META-INF/${type}-skeleton-*.xml";

// ------------------------------ FIELDS ------------------------------

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ContentManagerHelper.class);

    private JahiaSitesService sitesService;
    private ContentHistoryService contentHistoryService;

    private NavigationHelper navigation;
    private PropertiesHelper properties;
    private VersioningHelper versioning;

// --------------------- GETTER / SETTER METHODS ---------------------

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

    public JCRNodeWrapper addNode(JCRNodeWrapper parentNode, String name, String nodeType, List<String> mixin,
                                  List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        if (!parentNode.hasPermission(Privilege.JCR_ADD_CHILD_NODES)) {
            throw new GWTJahiaServiceException(
                    new StringBuilder(parentNode.getPath()).append(" - ACCESS DENIED").toString());
        }
        JCRNodeWrapper childNode = null;
        if (!parentNode.isFile() && parentNode.hasPermission("jcr:addChildNodes") && !parentNode.isLocked()) {
            try {
                if (!parentNode.isCheckedOut()) {
                    parentNode.checkout();
                }
                childNode = parentNode.addNode(name, nodeType);
                if (mixin != null) {
                    for (String m : mixin) {
                        childNode.addMixin(m);
                    }
                }
                properties.setProperties(childNode, props);
            } catch (Exception e) {
                logger.error("Exception", e);
                throw new GWTJahiaServiceException("Node creation failed. Cause: " + e.getMessage());
            }
        }
        if (childNode == null) {
            throw new GWTJahiaServiceException("Node creation failed");
        }
        return childNode;
    }

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin,
                                   List<GWTJahiaNodeProperty> props, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper parentNode;
        final JCRSessionWrapper jcrSessionWrapper;
        try {
            jcrSessionWrapper = currentUserSession;
            parentNode = jcrSessionWrapper.getNode(parentPath);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        String nodeName = name;

        if (nodeName == null) {
            nodeName = findAvailableName(parentNode, nodeType.substring(nodeType.lastIndexOf(":") + 1)
            );
        } else {
            nodeName = findAvailableName(parentNode, nodeName);
        }
        checkName(nodeName);
        if (checkExistence(parentPath + "/" + nodeName, currentUserSession)) {
            throw new GWTJahiaServiceException("A node already exists with name '" + nodeName + "'");
        }

        JCRNodeWrapper childNode = addNode(parentNode, nodeName, nodeType, mixin, props);
        return navigation.getGWTJahiaNode(childNode);
    }

    public String generateNameFromTitle(List<GWTJahiaNodeProperty> props) {
        String nodeName = null;
        for (GWTJahiaNodeProperty property : props) {
            if (property != null) {
                final List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
                if (property.getName().equals("jcr:title") && propertyValues != null && propertyValues.size() > 0 &&
                        propertyValues.get(0).getString() != null) {
                    nodeName = JCRContentUtils.generateNodeName(propertyValues.get(0).getString(), 32);
                }
            } else {
                logger.error("found a null property");
            }
        }
        return nodeName;
    }

// -------------------------- OTHER METHODS --------------------------

    public void createFolder(String parentPath, String name, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper parentNode;
        final JCRSessionWrapper jcrSessionWrapper;
        try {
            jcrSessionWrapper = currentUserSession;
            parentNode = jcrSessionWrapper.getNode(parentPath);
            if (parentNode.isNodeType("jnt:folder")) {
                createNode(parentPath, name, "jnt:folder", null, new ArrayList<GWTJahiaNodeProperty>(),
                        currentUserSession);
            } else {
                createNode(parentPath, name, "jnt:contentList", null, new ArrayList<GWTJahiaNodeProperty>(),
                        currentUserSession);
            }

            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public String findAvailableName(JCRNodeWrapper dest, String name)
            throws GWTJahiaServiceException {
        return JCRContentUtils.findAvailableNodeName(dest, name);
    }

    /**
     * Check name
     *
     * @param name
     * @throws GWTJahiaServiceException
     */
    public void checkName(String name) throws GWTJahiaServiceException {
        if (name.indexOf("*") > 0 || name.indexOf("/") > 0 || name.indexOf(":") > 0 || name.indexOf("\"") > 0) {
            throw new GWTJahiaServiceException("Invalid name : characters *,/,\",: cannot be used here");
        }
    }

    public boolean checkExistence(String path, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        boolean exists = false;
        try {
            currentUserSession.getNode(path);
            exists = true;
        } catch (PathNotFoundException e) {
            exists = false;
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException("Error:\n" + e.toString());
        }
        return exists;
    }

    public void move(String sourcePath, String targetPath, JCRSessionWrapper currentUserSession)
            throws RepositoryException, InvalidItemStateException, ItemExistsException {
        JCRSessionWrapper session = currentUserSession;
        session.move(sourcePath, targetPath);
        session.save();
    }

    /**
     * Remove deleted children and reorder
     *
     * @param newChildrenList
     */
    public void updateChildren(final GWTJahiaNode parentNode, final List<GWTJahiaNode> newChildrenList,
                               final JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            if (newChildrenList == null) {
                return;
            }

            final JCRNodeWrapper targetNode = currentUserSession.getNode(parentNode.getPath());

            if (!targetNode.isCheckedOut()) {
                currentUserSession.checkout(targetNode);
            }


            // remove deleted children
            NodeIterator oldChildrenNodes = targetNode.getNodes();
            while (oldChildrenNodes.hasNext()) {
                JCRNodeWrapper currentChildNode = (JCRNodeWrapper) oldChildrenNodes.nextNode();

                GWTJahiaNode comparingGWTJahiaNode = new GWTJahiaNode();
                comparingGWTJahiaNode.setPath(currentChildNode.getPath());

                // node has been deleted
                if (!newChildrenList.contains(comparingGWTJahiaNode) && currentChildNode.isNodeType("jnt:content")) {
                    currentChildNode.remove();
                }

            }

            // reorder existing ones
            if (newChildrenList != null) {
                for (GWTJahiaNode childNode : newChildrenList) {
                    targetNode.orderBefore(childNode.getName(), null);
                }
            }
            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void moveAtEnd(String sourcePath, String targetPath, JCRSessionWrapper currentUserSession)
            throws RepositoryException, InvalidItemStateException, ItemExistsException, GWTJahiaServiceException {
        JCRSessionWrapper session = currentUserSession;
        final JCRNodeWrapper srcNode = session.getNode(sourcePath);
        final JCRNodeWrapper targetNode = session.getNode(targetPath);
        if (!targetNode.isCheckedOut()) {
            targetNode.checkout();
        }

        if (srcNode.getParent().getPath().equals(targetNode.getPath())) {
            if (targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetNode.orderBefore(srcNode.getName(), null);
            }
        } else {
            if (!srcNode.isCheckedOut()) {
                srcNode.checkout();
            }
            if (!srcNode.getParent().isCheckedOut()) {
                srcNode.getParent().checkout();
            }
            String newname = findAvailableName(targetNode, srcNode.getName());
            session.move(sourcePath, targetNode.getPath() + "/" + newname);
            if (targetNode.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetNode.orderBefore(newname, null);
            }
        }
        session.save();
    }

    public void moveOnTopOf(String sourcePath, String targetPath, JCRSessionWrapper currentUserSession)
            throws RepositoryException, InvalidItemStateException, ItemExistsException, GWTJahiaServiceException {
        JCRSessionWrapper session = currentUserSession;
        final JCRNodeWrapper srcNode = session.getNode(sourcePath);
        final JCRNodeWrapper targetNode = session.getNode(targetPath);
        final JCRNodeWrapper targetParent = (JCRNodeWrapper) targetNode.getParent();
        if (!targetParent.isCheckedOut()) {
            targetParent.checkout();
        }
        if (srcNode.getParent().getPath().equals(targetParent.getPath())) {
            if (targetParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetParent.orderBefore(srcNode.getName(), targetNode.getName());
            }
        } else {
            if (!srcNode.isCheckedOut()) {
                srcNode.checkout();
            }
            if (!srcNode.getParent().isCheckedOut()) {
                srcNode.getParent().checkout();
            }
            String newname = findAvailableName(targetParent, srcNode.getName());
            session.move(sourcePath, targetParent.getPath() + "/" + newname);
            if (targetParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                targetParent.orderBefore(newname, targetNode.getName());
            }
        }
        session.save();
    }

    public void checkWriteable(List<String> paths, JahiaUser user, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String aNode : paths) {
            JCRNodeWrapper node;
            try {
                node = currentUserSession.getNode(aNode);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(aNode).append(" could not be accessed : ")
                        .append(e.toString()).toString());
                continue;
            }
            if (!node.hasPermission(Privilege.JCR_ADD_CHILD_NODES)) {
                missedPaths.add(new StringBuilder("User ").append(user.getUsername()).append(" has no write access to ")
                        .append(node.getName()).toString());
            } else if (node.isLocked() && !node.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(node.getName()).append(" is locked by ")
                        .append(user.getUsername()).toString());
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be cut:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public List<GWTJahiaNode> copy(List<String> pathsToCopy, String destinationPath, String newName, boolean moveOnTop,
                                   boolean cut, boolean reference, boolean templateToPage,
                                   JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        List<GWTJahiaNode> res = new ArrayList<GWTJahiaNode>();

        List<String> missedPaths = new ArrayList<String>();

        final JCRNodeWrapper targetParent;
        JCRNodeWrapper targetNode;
        try {
            targetNode = currentUserSession.getNode(destinationPath);

            if (moveOnTop) {
                targetParent = (JCRNodeWrapper) targetNode.getParent();
            } else {
                targetParent = targetNode;
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }

        try {
            for (String aNode : pathsToCopy) {
                JCRNodeWrapper node = currentUserSession.getNode(aNode);
                String name = newName != null ? newName : node.getName();
                try {
                    name = findAvailableName(targetParent, name);
                    if (targetParent.hasPermission("jcr:addChildNodes") && !targetParent.isLocked()) {
                        final JCRNodeWrapper copy = doPaste(targetParent, node, name, cut, reference);

                        if (moveOnTop && targetParent.getPrimaryNodeType().hasOrderableChildNodes()) {
                            targetParent.orderBefore(name, targetNode.getName());
                        }
                        currentUserSession.save();
                        res.add(navigation.getGWTJahiaNode(copy));
                    } else {
                        missedPaths
                                .add(new StringBuilder("File ").append(name).append(" could not be referenced in ")
                                        .append(targetParent.getPath()).toString());
                    }
                } catch (RepositoryException e) {
                    logger.error("Exception", e);
                    missedPaths.add(new StringBuilder("File ").append(name).append(" could not be referenced in ")
                            .append(targetParent.getPath()).toString());
                } catch (JahiaException e) {
                    logger.error("Exception", e);
                    missedPaths.add(new StringBuilder("File ").append(name).append(" could not be referenced in ")
                            .append(targetParent.getPath()).toString());
                }
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }

        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not have their reference pasted:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
        return res;
    }

    private JCRNodeWrapper doPaste(JCRNodeWrapper targetNode, JCRNodeWrapper node, String name, boolean cut,
                                   boolean reference) throws RepositoryException, JahiaException {
        targetNode.checkout();
        if (cut) {
            node.checkout();
            node.getParent().checkout();
            targetNode.getSession().move(node.getPath(), targetNode.getPath() + "/" + name);
        } else if (reference) {
            /*Property p = */
            if (!targetNode.isCheckedOut()) {
                targetNode.checkout();
            }
            if (targetNode.getPrimaryNodeTypeName().equals("jnt:members")) {
                if (node.getPrimaryNodeTypeName().equals("jnt:user")) {
                    Node member = targetNode.addNode(name, Constants.JAHIANT_MEMBER);
                    member.setProperty("j:member", node.getUUID());
                } else if (node.getPrimaryNodeTypeName().equals("jnt:group")) {
                    Node node1 = node.getParent().getParent();
                    int id = 0;
                    if (node1 != null && node1.getPrimaryNodeType().getName().equals(Constants.JAHIANT_VIRTUALSITE)) {
                        id = sitesService.getSiteByKey(node1.getName()).getID();
                    }
                    name += ("___" + id);
                    Node member = targetNode.addNode(name, Constants.JAHIANT_MEMBER);
                    member.setProperty("j:member", node.getUUID());
                }
            } else {
                Node ref = targetNode.addNode(name, "jnt:contentReference");
                ref.setProperty("j:node", node.getUUID());
            }
        } else {
            node.copy(targetNode, name, true);
        }
        return targetNode.getNode(name);
    }

    public void deletePaths(List<String> paths, JahiaUser user, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper nodeToDelete;
            try {
                nodeToDelete = currentUserSession.getNode(path);
            } catch (RepositoryException e) {
                missedPaths.add(new StringBuilder(path).append(" could not be accessed : ")
                        .append(e.toString()).toString());
                continue;
            }
            if (!user.isRoot() && nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - locked by ")
                        .append(nodeToDelete.getLockOwner()).toString());
            }
            if (!nodeToDelete.hasPermission(Privilege.JCR_REMOVE_NODE)) {
                missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString());
            } else if (!getRecursedLocksAndFileUsages(nodeToDelete, missedPaths, user.getUsername())) {
                try {
                    if (!nodeToDelete.getParent().isCheckedOut()) {
                        nodeToDelete.getParent().checkout();
                    }

                    nodeToDelete.remove();
                    nodeToDelete.saveSession();
                } catch (AccessDeniedException e) {
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString());
                } catch (ReferentialIntegrityException e) {
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - is in use").toString());
                } catch (RepositoryException e) {
                    logger.error("error", e);
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - UNSUPPORTED").toString());
                }
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(JahiaResourceBundle.getJahiaInternalResource("label.error.nodes.not.deleted", currentUserSession.getLocale()));
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public void rename(String path, String newName, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        try {
            if (node.isLocked() && !node.getLockOwner().equals(currentUserSession.getUser().getUsername())) {
                throw new GWTJahiaServiceException(new StringBuilder(node.getName()).append(" is locked by ")
                        .append(currentUserSession.getUser().getUsername()).toString());
            } else if (!node.hasPermission(Privilege.JCR_WRITE)) {
                throw new GWTJahiaServiceException(
                        new StringBuilder(node.getName()).append(" - ACCESS DENIED").toString());
            } else if (!node.rename(newName)) {
                throw new GWTJahiaServiceException(
                        new StringBuilder("Could not rename file ").append(node.getName()).append(" into ")
                                .append(newName).toString());
            }
        } catch (ItemExistsException e) {
            throw new GWTJahiaServiceException(new StringBuilder(newName).append(" already exists").toString());
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder("Could not rename file ").append(node.getName()).append(" into ")
                            .append(newName).toString());
        }
        try {
            node.saveSession();
        } catch (RepositoryException e) {
            logger.error("error", e);
            throw new GWTJahiaServiceException(
                    new StringBuilder("Could not save file ").append(node.getName()).append(" into ")
                            .append(newName).toString());
        }
    }

    public void importContent(String parentPath, String fileKey, boolean asynchronously) throws GWTJahiaServiceException {
        try {
            if (!asynchronously) {
                ImportJob.importContent(parentPath, fileKey);
            } else {
                // let's schedule an import job.
                GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(fileKey);

                JobDetail jobDetail = BackgroundJob.createJahiaJob("Import file " + FilenameUtils.getName(item.getOriginalFileName()), ImportJob.class);
                JobDataMap jobDataMap;
                jobDataMap = jobDetail.getJobDataMap();

                jobDataMap.put(ImportJob.DESTINATION_PARENT_PATH, parentPath);
                jobDataMap.put(ImportJob.FILE_KEY, fileKey);
                jobDataMap.put(ImportJob.FILENAME, FilenameUtils.getName(item.getOriginalFileName()));

                ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);

            }
        } catch (Exception e) {
            logger.error("Error when importing", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNodeACL getACL(String path, boolean newAcl, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        Map<String, List<String[]>> m = node.getAclEntries();

        GWTJahiaNodeACL acl = new GWTJahiaNodeACL();

        try {
            Map<String, List<JCRNodeWrapper>> roles = node.getAvailableRoles();
            Map<String, List<String>> dependencies = new HashMap<String, List<String>>();

            Map<String, List<String>> availablePermissions = new HashMap<String, List<String>>();
            Set<String> allAvailablePermissions = new HashSet<String>();
            Map<String, String> labels = new HashMap<String, String>();

            for (Map.Entry<String, List<JCRNodeWrapper>> entry : roles.entrySet()) {
                availablePermissions.put(entry.getKey(), new ArrayList<String>());
                for (JCRNodeWrapper nodeWrapper : entry.getValue()) {
                    allAvailablePermissions.add(nodeWrapper.getName());
                    availablePermissions.get(entry.getKey()).add(nodeWrapper.getName());
                    if (nodeWrapper.hasProperty("jcr:title")) {
                        labels.put(nodeWrapper.getName(), nodeWrapper.getProperty("jcr:title").getString());
                    } else {
                        labels.put(nodeWrapper.getName(), nodeWrapper.getName());
                    }
                    List<String> d = new ArrayList<String>();
                    if (nodeWrapper.hasProperty("j:dependencies")) {
                        for (Value value : nodeWrapper.getProperty("j:dependencies").getValues()) {
                            d.add(((JCRValueWrapper) value).getNode().getName());
                        }
                    }
                    dependencies.put(nodeWrapper.getName(), d);
                }
            }
            acl.setAvailablePermissions(availablePermissions);
            acl.setPermissionLabels(labels);
            acl.setPermissionsDependencies(dependencies);

            List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();
            Map<String, GWTJahiaNodeACE> map = new HashMap<String, GWTJahiaNodeACE>();

            for (Iterator<String> iterator = m.keySet().iterator(); iterator.hasNext();) {
                String principal = iterator.next();
                GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
                map.put(principal, ace);
                ace.setPrincipalType(principal.charAt(0));
                ace.setPrincipal(principal.substring(2));

                List<String[]> st = m.get(principal);
                Map<String, Boolean> perms = new HashMap<String, Boolean>();
                Map<String, Boolean> inheritedPerms = new HashMap<String, Boolean>();
                String inheritedFrom = null;
                for (String[] strings : st) {
                    String pathFrom = strings[0];
                    String aclType = strings[1];
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
                if (!CollectionUtils.intersection(allAvailablePermissions, inheritedPerms.keySet()).isEmpty() ||
                        !CollectionUtils.intersection(allAvailablePermissions, perms.keySet()).isEmpty()) {
                    aces.add(ace);
                    ace.setInheritedFrom(inheritedFrom);
                    ace.setInheritedPermissions(inheritedPerms);
                    ace.setPermissions(perms);
                    ace.setInherited(perms.isEmpty());
                }
            }

            boolean aclInheritanceBreak = node.getAclInheritanceBreak();
            acl.setBreakAllInheritance(aclInheritanceBreak);

            if (aclInheritanceBreak) {
                m = node.getParent().getAclEntries();
                for (Iterator<String> iterator = m.keySet().iterator(); iterator.hasNext();) {
                    String principal = iterator.next();
                    GWTJahiaNodeACE ace = map.get(principal);
                    if (ace == null) {
                        ace = new GWTJahiaNodeACE();
                        map.put(principal, ace);
                        aces.add(ace);
                        ace.setPrincipalType(principal.charAt(0));
                        ace.setPrincipal(principal.substring(2));
                        ace.setPermissions(new HashMap<String, Boolean>());
                    }
                    Map<String, Boolean> inheritedPerms = new HashMap<String, Boolean>();

                    List<String[]> st = m.get(principal);
                    String inheritedFrom = null;
                    for (String[] strings : st) {
                        String pathFrom = strings[0];
                        String aclType = strings[1];
                        String role = strings[2];
                        if (!inheritedPerms.containsKey(role)) {
                            if (inheritedFrom == null || inheritedFrom.length() < pathFrom.length()) {
                                inheritedFrom = pathFrom;
                            }
                            inheritedPerms.put(role, aclType.equals("GRANT"));
                        }
                    }

                    ace.setInheritedFrom(inheritedFrom);
                    ace.setInheritedPermissions(inheritedPerms);
                }
            }
            acl.setAce(aces);

        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return acl;
    }

    public void setACL(String path, GWTJahiaNodeACL acl, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
            if (!node.isCheckedOut()) {
                currentUserSession.checkout(node);
            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }

        try {
            node.revokeAllRoles();
            for (GWTJahiaNodeACE ace : acl.getAce()) {
                String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                for (Map.Entry<String, Boolean> entry : ace.getPermissions().entrySet()) {
                    if (!entry.getValue().equals(ace.getInheritedPermissions().get(entry.getKey()))) {
                        if (entry.getValue().equals(Boolean.TRUE) && !Boolean.TRUE.equals(ace.getInheritedPermissions().get(entry.getKey()))) {
                            node.grantRoles(user, Collections.singleton(entry.getKey()));
                        } else if (entry.getValue().equals(Boolean.FALSE) && Boolean.TRUE.equals(ace.getInheritedPermissions().get(entry.getKey()))) {
                            node.denyRoles(user, Collections.singleton(entry.getKey()));
                        }
                    }
                }
            }
            node.setAclInheritanceBreak(acl.isBreakAllInheritance());
            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error("error", e);
            throw new GWTJahiaServiceException("Could not save file " + node.getName());
        }
    }

    private boolean getRecursedLocksAndFileUsages(JCRNodeWrapper nodeToDelete, List<String> lockedNodes,
                                                  String username) throws GWTJahiaServiceException {
        try {
            for (NodeIterator iterator = nodeToDelete.getNodes(); iterator.hasNext();) {
                JCRNodeWrapper child = (JCRNodeWrapper) iterator.next();
                getRecursedLocksAndFileUsages(child, lockedNodes, username);
                if (lockedNodes.size() >= 10) {
                    // do not check further
                    return true;
                }
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
        if (nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(username)) {
            lockedNodes.add(new StringBuilder(nodeToDelete.getPath()).append(" - locked by ")
                    .append(nodeToDelete.getLockOwner()).toString());
        }
        return !lockedNodes.isEmpty();
    }

    public void setLock(List<String> paths, boolean locked, JCRSessionWrapper currentUserSession)
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
                missedPaths.add(new StringBuilder(path).append(" could not be accessed : ")
                        .append(e.toString()).toString());
                continue;
            }
        }
        for (JCRNodeWrapper node : nodes) {
            if (!node.hasPermission(Privilege.JCR_LOCK_MANAGEMENT)) {
                missedPaths.add(new StringBuilder(node.getName()).append(": write access denied").toString());
            } else if (node.isLocked()) {
                if (!locked) {
                    if (user.isAdminMember(0)) {
                        try {
                            node.clearAllLocks();
                        } catch (RepositoryException e) {
                            logger.error(e.toString(), e);
                            missedPaths
                                    .add(new StringBuilder(node.getName()).append(": repository exception").toString());
                        }
                    } else if (node.getLockOwner() != null && !node.getLockOwner().equals(user.getUsername()) &&
                            !user.isRoot()) {
                        missedPaths.add(new StringBuilder(node.getName()).append(": locked by ")
                                .append(node.getLockOwner()).toString());
                    } else {
                        try {
                            node.unlock();
                        } catch (RepositoryException e) {
                            logger.error(e.toString(), e);
                            missedPaths
                                    .add(new StringBuilder(node.getName()).append(": repository exception").toString());
                        }
                    }
                } else {
                    if (node.getLockOwner() != null && !node.getLockOwner().equals(user.getUsername())) {
                        missedPaths.add(new StringBuilder(node.getName()).append(": locked by ")
                                .append(node.getLockOwner()).toString());
                    }
                }
            } else {
                if (locked) {
                    try {
                        if (!node.lockAndStoreToken("user")) {
                            missedPaths
                                    .add(new StringBuilder(node.getName()).append(": repository exception").toString());
                        }
                    } catch (RepositoryException e) {
                        logger.error(e.toString(), e);
                        missedPaths.add(new StringBuilder(node.getName()).append(": repository exception").toString());
                    }
                } else {
                    // already unlocked
                }
            }
        }
        try {
            currentUserSession.save();
        } catch (RepositoryException e) {
            logger.error("error", e);
            throw new GWTJahiaServiceException("Could not save session");
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors =
                    new StringBuilder("The following files could not be ").append(locked ? "locked:" : "unlocked:");
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
            if (!subNode.isNodeType("jnt:page") && subNode.isNodeType("jnt:content")) {
                addSub(nodes, subNode);
            }
        }
    }

    /**
     * Uploda file depending on operation (add version, auto-rename or just upload)
     *
     * @param location
     * @param tmpName
     * @param operation
     * @param newName
     * @param currentUserSession
     * @throws GWTJahiaServiceException
     */
    public void uploadedFile(String location, String tmpName, int operation, String newName,
                             JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper parent = currentUserSession.getNode(location);
            switch (operation) {
                case 3:
                    JCRNodeWrapper node = (JCRNodeWrapper) parent.getNode(newName);
                    if (node == null) {
                        throw new GWTJahiaServiceException(
                                "Could'nt add a new version, file " + location + "/" + newName + "not found ");
                    }
                    versioning.addNewVersionFile(node, tmpName);
                    break;
                case 1:
                    newName = findAvailableName(parent, newName);
                case 0:
                    if (parent.hasNode(newName)) {
                        throw new GWTJahiaServiceException("file exists");
                    }
                default:
                    GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(tmpName);
                    FileInputStream is = null;
                    try {
                        is = item.getStream();
                        parent.uploadFile(newName, is, item.getContentType());
                    } catch (FileNotFoundException e) {
                        logger.error(e.getMessage(), e);
                    } finally {
                        IOUtils.closeQuietly(is);
                        item.dispose();
                    }
                    break;
            }
            parent.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deployTemplates(final String templatesPath, final String sitePath, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        try {
            ServicesRegistry.getInstance().getJahiaTemplateManagerService().deployTemplates(templatesPath, sitePath, currentUserSession.getUser().getUsername());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNode createTemplateSet(String key, String baseSet, String siteType, JCRSessionWrapper session) throws GWTJahiaServiceException {
        boolean isTemplatesSet = "templatesSet".equals(siteType);
        if (baseSet == null) {
            String shortName = JCRContentUtils.generateNodeName(key, 50);

            try {
                JCRNodeWrapper templateSet = session.getNode("/templateSets").addNode(shortName, "jnt:virtualsite");
                session.save();
                templateSet.setProperty("j:siteType", siteType);
                templateSet.setProperty("j:installedModules", new Value[]{session.getValueFactory().createValue(shortName)});

                String skeletons = MODULE_SKELETONS.replace("${type}", siteType);
                JCRContentUtils.importSkeletons(skeletons, "/templateSets/" + shortName, session);
                if (isTemplatesSet) {
                    templateSet.getNode("templates/base").setProperty("j:view", shortName);
                }
                session.save();
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().createModule(shortName, isTemplatesSet);

                return navigation.getGWTJahiaNode(templateSet);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            try {
                List<GWTJahiaNode> result = copy(Arrays.asList("/templateSets/" + baseSet), "/templateSets", key, false, false, false, false, session);
                siteType = session.getNode("/templateSets/" + baseSet).getProperty("j:siteType").getValue().getString();
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().createModule(key, isTemplatesSet);
                return result.get(0);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public GWTJahiaNode generateWar(String moduleName, JCRSessionWrapper session) {
        try {
            ServicesRegistry.getInstance().getJahiaTemplateManagerService().regenerateImportFile(moduleName);
            File f = File.createTempFile("templateSet", ".war");
            File templateDir = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath(), moduleName);

            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(f));
            zip(templateDir, templateDir, zos);
            zos.close();

            JCRNodeWrapper privateFolder = session.getNode("/users/" + session.getUser().getName() + "/files/private");

            if (!privateFolder.hasNode("templates-sets")) {
                if (!privateFolder.isCheckedOut()) {
                    privateFolder.checkout();
                }
                privateFolder.addNode("templates-sets", Constants.JAHIANT_FOLDER);
            }
            JCRNodeWrapper parent = privateFolder.getNode("templates-sets");
            if (!parent.isCheckedOut()) {
                parent.checkout();
            }
            JCRNodeWrapper res = parent.uploadFile(moduleName + ".war", new FileInputStream(f), "application/x-zip");
            session.save();

            f.delete();

            return navigation.getGWTJahiaNode(res);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    private void zip(File dir, File rootDir, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                ZipEntry ze = new ZipEntry(file.getPath().substring(rootDir.getPath().length() + 1).replace("\\", "/"));
                zos.putNextEntry(ze);
                final FileInputStream input = new FileInputStream(file);
                IOUtils.copy(input, zos);
                input.close();
            }

            if (file.isDirectory()) {
                ZipEntry ze = new ZipEntry(file.getPath().substring(rootDir.getPath().length() + 1).replace("\\", "/") + "/");
                zos.putNextEntry(ze);
                zip(file, rootDir, zos);
            }
        }
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
                return -o1.compareTo(o2);
            }
        });

        return result;
    }

    private GWTJahiaContentHistoryEntry convertToGWTJahiaContentHistoryEntry(HistoryEntry historyEntry) {
        String languageCode = null;
        if (historyEntry.getLocale() != null) {
            languageCode = historyEntry.getLocale().toString();
        }
        GWTJahiaContentHistoryEntry result = new GWTJahiaContentHistoryEntry(historyEntry.getDate(), historyEntry.getAction(), historyEntry.getPropertyName(), historyEntry.getUserKey(), historyEntry.getPath(), historyEntry.getMessage(), languageCode);
        return result;
    }

    public void deleteReferences(String path, JahiaUser user, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        JCRNodeWrapper referencedNode = null;
        try {
            referencedNode = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            missedPaths.add(new StringBuilder(path).append(" could not be accessed : ").append(
                    e.toString()).toString());
        }
        if (referencedNode != null) {
            if (!user.isRoot() && referencedNode.isLocked() && !referencedNode.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(referencedNode.getPath()).append(" - locked by ").append(
                        referencedNode.getLockOwner()).toString());
            }
            if (!referencedNode.hasPermission(Privilege.JCR_REMOVE_NODE)) {
                missedPaths.add(new StringBuilder(referencedNode.getPath()).append(" - ACCESS DENIED").toString());
            } else if (!getRecursedLocksAndFileUsages(referencedNode, missedPaths, user.getUsername())) {
                try {
                    String referenceToRemove = referencedNode.getIdentifier();
                    PropertyIterator r = referencedNode.getReferences();
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
                                missedPaths.add(new StringBuilder(referencedNode.getPath()).append(
                                        " - is use in a mandatory property of ").append(
                                        reference.getParent().getPath()).toString());
                            }
                        }
                    }
                    currentUserSession.save();
                } catch (AccessDeniedException e) {
                    missedPaths.add(new StringBuilder(referencedNode.getPath()).append(" - ACCESS DENIED").toString());
                } catch (ReferentialIntegrityException e) {
                    missedPaths.add(new StringBuilder(referencedNode.getPath()).append(" - is in use").toString());
                } catch (RepositoryException e) {
                    logger.error("error", e);
                    missedPaths.add(new StringBuilder(referencedNode.getPath()).append(" - UNSUPPORTED").toString());
                }
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder(JahiaResourceBundle.getJahiaInternalResource(
                    "label.error.nodes.not.deleted", currentUserSession.getLocale()));
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

}
