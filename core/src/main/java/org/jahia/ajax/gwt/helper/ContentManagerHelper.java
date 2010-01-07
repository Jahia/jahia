/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import com.ibm.icu.text.Normalizer;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.*;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.*;

import java.util.*;

import com.octo.captcha.service.image.ImageCaptchaService;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 20 juin 2008 - 12:49:42
 */
public class ContentManagerHelper {
// ------------------------------ FIELDS ------------------------------

    private static Logger logger = Logger.getLogger(ContentManagerHelper.class);

    private JCRStoreService jcrService;
    private JCRSessionFactory sessionFactory;
    private JahiaSitesService sitesService;
    private ImportExportBaseService importExport;
    private ImageCaptchaService captchaService;

    private NavigationHelper navigation;
    private PropertiesHelper properties;
    private VersioningHelper versioning;

// --------------------- GETTER / SETTER METHODS ---------------------

    public void setCaptchaService(ImageCaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    public void setImportExport(ImportExportBaseService importExport) {
        this.importExport = importExport;
    }

    public void setJcrService(JCRStoreService jcrService) {
        this.jcrService = jcrService;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setProperties(PropertiesHelper properties) {
        this.properties = properties;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setSitesService(JahiaSitesService sitesService) {
        this.sitesService = sitesService;
    }

    public void setVersioning(VersioningHelper versioning) {
        this.versioning = versioning;
    }

    public JCRNodeWrapper addNode(JCRNodeWrapper parentNode, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        if (!parentNode.hasPermission(JCRNodeWrapper.WRITE)) {
            throw new GWTJahiaServiceException(new StringBuilder(parentNode.getPath()).append(" - ACCESS DENIED").toString());
        }
        JCRNodeWrapper childNode = null;
        if (!parentNode.isFile() && parentNode.isWriteable()) {
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

    private JCRNodeWrapper unsecureAddNode(JCRNodeWrapper parentNode, String name, String nodeType, List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        JCRNodeWrapper childNode = null;
        if (!parentNode.isFile()) {
            try {
                if (!parentNode.isCheckedOut()) {
                    parentNode.checkout();
                }
                childNode = parentNode.addNode(name, nodeType);
                properties.setProperties(childNode, props);
            } catch (Exception e) {
                logger.error("Exception", e);
                throw new GWTJahiaServiceException("Node creation failed");
            }
        }
        if (childNode == null) {
            throw new GWTJahiaServiceException("Node creation failed");
        }
        return childNode;
    }

    public GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props, ProcessingContext context) throws GWTJahiaServiceException {
        JCRNodeWrapper parentNode;
        final JCRSessionWrapper jcrSessionWrapper;
        try {
            jcrSessionWrapper = sessionFactory.getCurrentUserSession(null, context.getLocale());
            parentNode = jcrSessionWrapper.getNode(parentPath);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        String nodeName = name;
        if (name == null) {
            for (GWTJahiaNodeProperty property : props) {
                if (property != null) {
                    final List<GWTJahiaNodePropertyValue> propertyValues = property.getValues();
                    if (property.getName().equals("jcr:title") && propertyValues != null && propertyValues.size() > 0) {
                        nodeName = propertyValues.get(0).getString();
                        final char[] chars = Normalizer.normalize(nodeName, Normalizer.NFKD).toCharArray();
                        final char[] newChars = new char[chars.length];
                        int j = 0;
                        for (char aChar : chars) {
                            if (CharUtils.isAsciiAlphanumeric(aChar) || aChar == 32) {
                                newChars[j++] = aChar;
                            }
                        }
                        nodeName = new String(newChars, 0, j).trim().replaceAll(" ", "-").toLowerCase();
                        if (nodeName.length() > 32) {
                            nodeName = nodeName.substring(0, 32);
                        }
                    }
                }else{
                    logger.error("found a null property");
                }
            }
        }

        if (nodeName == null) {
            nodeName = findAvailableName(parentNode, nodeType.substring(nodeType.lastIndexOf(":") + 1));
        } else {
            nodeName = findAvailableName(parentNode, nodeName);
        }
        checkName(nodeName);
        if (checkExistence(parentPath + "/" + nodeName)) {
            throw new GWTJahiaServiceException("A node already exists with name '" + nodeName + "'");
        }

        JCRNodeWrapper childNode = addNode(parentNode, nodeName, nodeType, mixin, props);
        try {
            jcrSessionWrapper.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Node creation failed. Cause: " + e.getMessage());
        }
        return navigation.getGWTJahiaNode(childNode, true);
    }

    public GWTJahiaNode unsecureCreateNode(String parentPath, String name, String nodeType, List<String> mixin, List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        try {
            sessionFactory.getCurrentUserSession().getNode(parentPath + "/" + name);
            throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
        } catch (PathNotFoundException e) {
            if (!e.getMessage().contains(name)) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(parentPath).append("/").append(name).append(" could not be accessed :\n").append(e.toString()).toString());
            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(parentPath).append("/").append(name).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        JCRNodeWrapper parentNode;
        try {
            parentNode = sessionFactory.getCurrentUserSession().getNode(parentPath);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        JCRNodeWrapper childNode = unsecureAddNode(parentNode, name, nodeType, props);
        try {
            parentNode.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Node creation failed");
        }
        return navigation.getGWTJahiaNode(childNode, true);
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * Check captcha
     *
     * @param context
     * @param captcha
     * @return
     */
    public boolean checkCaptcha(ParamBean context, String captcha) {
        final String captchaId = context.getSessionState().getId();
        if (logger.isDebugEnabled()) logger.debug("j_captcha_response: " + captcha);
        boolean isResponseCorrect = false;
        try {
            isResponseCorrect = this.captchaService.validateResponseForID(captchaId, captcha);
            if (logger.isDebugEnabled()) logger.debug("CAPTCHA - isResponseCorrect: " + isResponseCorrect);
        } catch (final Exception e) {
            //should not happen, may be thrown if the id is not valid
            logger.debug("Error when calling CaptchaService", e);
        }
        return isResponseCorrect;
    }

    public void createFolder(String parentPath, String name, ProcessingContext context) throws GWTJahiaServiceException {
        createNode(parentPath, name, "jnt:folder", null, new ArrayList<GWTJahiaNodeProperty>(), context);
    }

    public String findAvailableName(JCRNodeWrapper dest, String name) throws GWTJahiaServiceException {
        try {
            return JCRContentUtils.findAvailableNodeName(dest, name, sessionFactory.getCurrentUserSession());
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Check name
     *
     * @param name
     * @throws GWTJahiaServiceException
     */
    public void checkName(String name) throws GWTJahiaServiceException {
        if (name.indexOf("*") > 0 ||
                name.indexOf("/") > 0 ||
                name.indexOf(":") > 0 ||
                name.indexOf("\"") > 0) {
            throw new GWTJahiaServiceException("Invalid name : characters *,/,\",: cannot be used here");
        }
    }

    public boolean checkExistence(String path) throws GWTJahiaServiceException {
        boolean exists = false;
        try {
            sessionFactory.getCurrentUserSession().getNode(path);
            exists = true;
        } catch (PathNotFoundException e) {
            exists = false;
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException("Error:\n" + e.toString());
        }
        return exists;
    }

    public void move(String sourcePath, String targetPath) throws RepositoryException, InvalidItemStateException, ItemExistsException {
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession();
        session.move(sourcePath, targetPath);
        session.save();
    }

    public void moveAtEnd(String sourcePath, String targetPath) throws RepositoryException, InvalidItemStateException, ItemExistsException, GWTJahiaServiceException {
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession();
        final JCRNodeWrapper srcNode = session.getNode(sourcePath);
        final JCRNodeWrapper targetNode = session.getNode(targetPath);
        if (!targetNode.isCheckedOut()) {
            targetNode.checkout();
        }

        if (srcNode.getParent().getPath().equals(targetNode.getPath())) {
            targetNode.orderBefore(srcNode.getName(), null);
        } else {
            if (!srcNode.isCheckedOut()) {
                srcNode.checkout();
            }
            if (!srcNode.getParent().isCheckedOut()) {
                srcNode.getParent().checkout();
            }
            String newname = findAvailableName(targetNode, srcNode.getName());
            session.move(sourcePath, targetNode.getPath() + "/" + newname);
            targetNode.orderBefore(newname, null);
        }
        session.save();
    }

    public void moveOnTopOf(JahiaUser user, String sourcePath, String targetPath) throws RepositoryException, InvalidItemStateException, ItemExistsException, GWTJahiaServiceException {
        JCRSessionWrapper session = sessionFactory.getCurrentUserSession();
        final JCRNodeWrapper srcNode = session.getNode(sourcePath);
        final JCRNodeWrapper targetNode = session.getNode(targetPath);
        final JCRNodeWrapper targetParent = (JCRNodeWrapper) targetNode.getParent();
        if (!targetParent.isCheckedOut()) {
            targetParent.checkout();
        }
        if (srcNode.getParent().getPath().equals(targetParent.getPath())) {
            targetParent.orderBefore(srcNode.getName(), targetNode.getName());
        } else {
            if (!srcNode.isCheckedOut()) {
                srcNode.checkout();
            }
            if (!srcNode.getParent().isCheckedOut()) {
                srcNode.getParent().checkout();
            }
            String newname = findAvailableName(targetParent, srcNode.getName());
            session.move(sourcePath, targetParent.getPath() + "/" + newname);
            targetParent.orderBefore(newname, targetNode.getName());
        }
        session.save();
    }

    public void checkWriteable(List<String> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String aNode : paths) {
            JCRNodeWrapper node;
            try {
                node = sessionFactory.getCurrentUserSession().getNode(aNode);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(aNode).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (!node.hasPermission(JCRNodeWrapper.WRITE)) {
                missedPaths.add(new StringBuilder("User ").append(user.getUsername()).append(" has no write access to ").append(node.getName()).toString());
            } else if (node.isLocked() && !node.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(node.getName()).append(" is locked by ").append(user.getUsername()).toString());
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

    public void pasteOnTopOf(List<String> pathsToCopy, String destinationPath, String newName, boolean moveOnTop, boolean cut, boolean reference) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();

        final JCRNodeWrapper targetParent;
        JCRNodeWrapper targetNode;
        try {
            targetNode = sessionFactory.getCurrentUserSession().getNode(destinationPath);

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
                JCRNodeWrapper node = jcrService.getSessionFactory().getCurrentUserSession().getNode(aNode);
                String name = newName != null ? newName : node.getName();
                if (node.hasPermission(JCRNodeWrapper.READ)) {
                    if (targetParent.isCollection()) {
                        try {
                            name = findAvailableName(targetParent, name);
                            if (targetParent.isWriteable()) {
                                doPaste(targetParent, node, name, cut, reference);
                                if (moveOnTop)
                                    targetParent.orderBefore(name, targetNode.getName());
                            } else {
                                missedPaths.add(new StringBuilder("File ").append(name).append(" could not be referenced in ").append(targetParent.getPath()).toString());
                            }
                        } catch (RepositoryException e) {
                            logger.error("Exception", e);
                            missedPaths.add(new StringBuilder("File ").append(name).append(" could not be referenced in ").append(targetParent.getPath()).toString());
                        } catch (JahiaException e) {
                            logger.error("Exception", e);
                            missedPaths.add(new StringBuilder("File ").append(name).append(" could not be referenced in ").append(targetParent.getPath()).toString());
                        }
                    }
                } else {
                    missedPaths.add(new StringBuilder("Source file ").append(name).append(" could not be read ").append(" - ACCESS DENIED").toString());
                }
            }
            targetParent.save();
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
    }

    private void doPaste(JCRNodeWrapper targetNode, JCRNodeWrapper node, String name, boolean cut, boolean reference) throws RepositoryException, JahiaException {
        if (cut) {
            node.checkout();
            targetNode.checkout();
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
                    Node member = targetNode.addNode(name + "___" + id, Constants.JAHIANT_MEMBER);
                    member.setProperty("j:member", node.getUUID());
                }
            } else {
                targetNode.clone(node, name);
            }

        } else {
            node.copyFile(targetNode.getPath(), name);
        }
    }

    public void deletePaths(List<String> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper nodeToDelete;
            try {
                nodeToDelete = sessionFactory.getCurrentUserSession().getNode(path);
            } catch (RepositoryException e) {
                missedPaths.add(new StringBuilder(path).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (!user.isRoot() && nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - locked by ").append(nodeToDelete.getLockOwner()).toString());
            }
            if (!nodeToDelete.hasPermission(JCRNodeWrapper.WRITE)) {
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
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath())
                            .append(" - is in use").toString());
                } catch (RepositoryException e) {
                    logger.error("error", e);
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - UNSUPPORTED").toString());
                }
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be deleted:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public void rename(String path, String newName, JahiaUser user) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getCurrentUserSession().getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        try {
            if (node.isLocked() && !node.getLockOwner().equals(user.getUsername())) {
                throw new GWTJahiaServiceException(new StringBuilder(node.getName()).append(" is locked by ").append(user.getUsername()).toString());
            } else if (!node.hasPermission(JCRNodeWrapper.WRITE)) {
                throw new GWTJahiaServiceException(new StringBuilder(node.getName()).append(" - ACCESS DENIED").toString());
            } else if (!node.renameFile(newName)) {
                throw new GWTJahiaServiceException(new StringBuilder("Could not rename file ").append(node.getName()).append(" into ").append(newName).toString());
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(new StringBuilder("Could not rename file ").append(node.getName()).append(" into ").append(newName).toString());
        }
        try {
            node.saveSession();
        } catch (RepositoryException e) {
            logger.error("error", e);
            throw new GWTJahiaServiceException(new StringBuilder("Could not save file ").append(node.getName()).append(" into ").append(newName).toString());
        }
    }

    public void importContent(String parentPath, String fileKey) throws GWTJahiaServiceException {
        GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(fileKey);
        try {
            if ("application/zip".equals(item.contentType)) {
                importExport.importZip(parentPath, item.file, false);
                item.file.delete();
            } else if ("application/xml".equals(item.contentType) || "text/xml".equals(item.contentType)) {
                importExport.importXML(parentPath, item.fileStream, false);
            }
        } catch (Exception e) {
            logger.error("Error when importing", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNodeACL getACL(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getCurrentUserSession().getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        Map<String, List<String[]>> m = node.getAclEntries();

        GWTJahiaNodeACL acl = new GWTJahiaNodeACL();
        List<GWTJahiaNodeACE> aces = new ArrayList<GWTJahiaNodeACE>();

        for (Iterator<String> iterator = m.keySet().iterator(); iterator.hasNext();) {
            String principal = iterator.next();
            GWTJahiaNodeACE ace = new GWTJahiaNodeACE();
            ace.setPrincipalType(principal.charAt(0));
            ace.setPrincipal(principal.substring(2));

            List<String[]> st = m.get(principal);
            Map<String, String> perms = new HashMap<String, String>();
            Map<String, String> inheritedPerms = new HashMap<String, String>();
            String inheritedFrom = null;
            for (String[] strings : st) {
                if (!path.equals(strings[0])) {
                    inheritedFrom = strings[0];
                    inheritedPerms.put(strings[2], strings[1]);
                } else {
                    perms.put(strings[2], strings[1]);
                }
            }

            ace.setInheritedFrom(inheritedFrom);
            ace.setInheritedPermissions(inheritedPerms);
            ace.setPermissions(perms);
            ace.setInherited(perms.isEmpty());

            aces.add(ace);
        }
        acl.setAce(aces);
        acl.setAvailablePermissions(new HashMap<String, List<String>>(node.getAvailablePermissions()));
        Map<String, String> labels = new HashMap<String, String>();
        for (List<String> list : acl.getAvailablePermissions().values()) {
            for (String s : list) {
                String k = s;
                if (k.contains(":")) {
                    k = k.substring(k.indexOf(':') + 1);
                }
                labels.put(s, JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.rights.ManageRights." + k + ".label", jParams.getLocale(), k));
            }
        }
        acl.setPermissionLabels(labels);
        acl.setAclDependencies(new HashMap<String, List<String>>());
        return acl;
    }

    public void setACL(String path, GWTJahiaNodeACL acl) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getCurrentUserSession().getNode(path);
            if (!node.isCheckedOut()) {
                node.checkout();
            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }

        node.revokeAllPermissions();
        for (GWTJahiaNodeACE ace : acl.getAce()) {
            String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
            if (!ace.isInherited()) {
                node.changePermissions(user, ace.getPermissions());
            }
        }
        try {
            node.save();
        } catch (RepositoryException e) {
            logger.error("error", e);
            throw new GWTJahiaServiceException("Could not save file " + node.getName());
        }
    }

    private boolean getRecursedLocksAndFileUsages(JCRNodeWrapper nodeToDelete, List<String> lockedNodes, String username) {
        if (nodeToDelete.isCollection()) {
            for (JCRNodeWrapper child : nodeToDelete.getChildren()) {
                getRecursedLocksAndFileUsages(child, lockedNodes, username);
                if (lockedNodes.size() >= 10) {
                    // do not check further
                    return true;
                }
            }
        }
        if (nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(username)) {
            lockedNodes.add(new StringBuilder(nodeToDelete.getPath()).append(
                    " - locked by ").append(nodeToDelete.getLockOwner()).toString());
        }
        List<UsageEntry> list = nodeToDelete.findUsages(true);
        for (UsageEntry usageEntry : list) {
            lockedNodes.add(new StringBuilder(nodeToDelete.getPath()).append(
                    " - used on page \"").append(usageEntry.getPageTitle()).append("\"").toString());
        }

        return !lockedNodes.isEmpty();
    }

    public void setLock(List<String> paths, boolean locked, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper node;
            try {
                node = sessionFactory.getCurrentUserSession().getNode(path);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(path).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (!node.hasPermission(JCRNodeWrapper.WRITE)) {
                missedPaths.add(new StringBuilder(node.getName()).append(": write access denied").toString());
            } else if (node.isLocked()) {
                if (!locked) {
                    if (node.getLockOwner() != null && !node.getLockOwner().equals(user.getUsername()) && !user.isRoot()) {
                        missedPaths.add(new StringBuilder(node.getName()).append(": locked by ").append(node.getLockOwner()).toString());
                    } else {
                        if (!node.forceUnlock()) {
                            missedPaths.add(new StringBuilder(node.getName()).append(": repository exception").toString());
                        }
                    }
                } else {
                    // already locked
                }
            } else {
                if (locked) {
                    if (!node.lockAndStoreToken()) {
                        missedPaths.add(new StringBuilder(node.getName()).append(": repository exception").toString());
                    }
                    try {
                        node.saveSession();
                    } catch (RepositoryException e) {
                        logger.error("error", e);
                        throw new GWTJahiaServiceException("Could not save file " + node.getName());
                    }
                } else {
                    // already unlocked
                }
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be ").append(locked ? "locked:" : "unlocked:");
            for (String missedPath : missedPaths) {
                errors.append("\n").append(missedPath);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    /**
     * Uploda file depending on operation (add version, auto-rename or just upload)
     *
     * @param location
     * @param tmpName
     * @param operation
     * @param newName
     * @throws GWTJahiaServiceException
     */
    public void uploadedFile(String location, String tmpName, int operation, String newName) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper parent = sessionFactory.getCurrentUserSession().getNode(location);
            switch (operation) {
                case 3:
                    JCRNodeWrapper node = (JCRNodeWrapper) parent.getNode(newName);
                    if (node == null) {
                        throw new GWTJahiaServiceException("Could'nt add a new version, file " + location + "/" + newName + "not found ");
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
                    parent.uploadFile(newName, GWTFileManagerUploadServlet.getItem(tmpName).fileStream, GWTFileManagerUploadServlet.getItem(tmpName).contentType);
                    break;
            }
            parent.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
