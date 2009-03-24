/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.filemanagement.server.helper;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.util.nodes.JCRClientUtils;
import org.jahia.ajax.gwt.filemanagement.server.GWTFileManagerUploadServlet;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.aclmanagement.server.ACLHelper;

import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.SelectorType;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.captcha.CaptchaService;
import org.jahia.services.categories.Category;
import org.jahia.registries.ServicesRegistry;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.api.Constants;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.PortletEntryPointDefinition;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.utils.FileUtils;
import org.jahia.bin.Jahia;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.StringValue;
import org.apache.log4j.Logger;
import org.apache.pluto.descriptors.portlet.ExpirationCacheDD;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 20 juin 2008 - 12:49:42
 */
public class FileManagerWorker {

    public final static String SAVED_OPEN_PATHS = "org.jahia.preferences.filemanager.savedopenpaths.";
    public final static String OPEN_PATHS_SEPARATOR = "-_-";

    private static JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
    private static Logger logger = Logger.getLogger(FileManagerWorker.class);

    public static String[] splitOpenPathList(String concatPaths) {
        return concatPaths.split(OPEN_PATHS_SEPARATOR);
    }

    public static String concatOpenPathsList(List<String> paths) {
        if (paths == null || paths.size() == 0) {
            return null;
        }
        StringBuilder b = new StringBuilder(paths.get(0));
        for (int i = 1; i < paths.size(); i++) {
            b.append(OPEN_PATHS_SEPARATOR).append(paths.get(i));
        }
        return b.toString();
    }

    /**
     * Append the children corresponding to the open paths.
     *
     * @param node      the node to retrieve children of
     * @param openPaths the list of previously open paths
     * @param context   the processing context
     * @param nodeTypes node types to include
     * @param filters   the filters
     * @param mimeTypes the mime types
     * @param noFolders don't retrieve folders if true
     * @throws GWTJahiaServiceException sthg bad happened
     */
    private static void appendChildren(GWTJahiaNode node, String[] openPaths, ProcessingContext context, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException {
        logger.debug("appending children for node " + node.getPath());
        if (node.isFile()) {
            return;
        }
        List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>();
        List<GWTJahiaNode> lsResult = ls(node, nodeTypes, mimeTypes, filters, null, noFolders, context);
        boolean addAll = false;
        for (GWTJahiaNode aNode : lsResult) {
            for (String openPath : openPaths) {
                if (openPath.indexOf(aNode.getPath()) == 0) {
                    addAll = true;
                    logger.debug("found at least one child in the path");
                    break;
                }
            }
            if (addAll) {
                break;
            }
        }
        if (addAll) {
            for (GWTJahiaNode aNode : lsResult) {
                boolean enableRec = false;
                for (String openPath : openPaths) {
                    if (openPath.indexOf(aNode.getPath()) == 0) {
                        enableRec = true;
                        break;
                    }
                }
                logger.debug("adding " + aNode.getName());
                aNode.setParent(node);
                nodes.add(aNode);
                if (!aNode.isFile() && enableRec) {
                    logger.debug("enable recursion for " + aNode.getName());
                    appendChildren(aNode, openPaths, context, nodeTypes, mimeTypes, filters, noFolders);
                }
            }
            node.setChildren(nodes);
        } else {
            logger.debug(node.getPath() + " does not contain any more node to expand");
        }
    }

    public static List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders, ProcessingContext context) throws GWTJahiaServiceException {
        JahiaUser user = context.getUser();
        JCRNodeWrapper node = jcr.getFileNode(folder != null ? folder.getPath() : "/", user);

        if (node == null) {
            throw new GWTJahiaServiceException("Parent node is null");
        }
        if (node.isFile()) {
            throw new GWTJahiaServiceException("Can't list the children of a file");
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(user.getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        List<JCRNodeWrapper> list = node.getChildren();
        if (list == null) {
            throw new GWTJahiaServiceException("Children list is null");
        }
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        String[] nodeTypesToApply = getFiltersToApply(nodeTypes);
        String[] mimeTypesToMatch = getFiltersToApply(mimeTypes);
        String[] filtersToApply = getFiltersToApply(filters);
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        for (JCRNodeWrapper f : list) {
            try {
                if (f.isVisible() && (matchesNodeType(f, nodeTypesToApply) || (f.isNodeType(Constants.JAHIANT_FOLDER) || f.isNodeType(Constants.JAHIANT_VFSMOUNTPOINT)))) {
                    if ((f.isNodeType(Constants.JAHIANT_FOLDER) || f.isNodeType(Constants.JAHIANT_VFSMOUNTPOINT)) && noFolders) {
                        continue;
                    }
                    try {
                        if (f.isNodeType(Constants.JAHIANT_VIRTUALSITE)) {
                            if (!f.getProperty("j:name").getString().equals(context.getSiteKey())) {
                                continue;
                            }
                        }
                    } catch (RepositoryException e) {
                        logger.debug("cannot get site name " + f.getPath());
                    }
                    if (f.isCollection() || (matchesFilters(f.getFileContent().getContentType(), mimeTypesToMatch) && matchesFilters(f.getName(), filtersToApply))) {
                        GWTJahiaNode theNode = getGWTJahiaNode(f);
                        if (openPaths != null && openPaths.length() > 0) {
                            logger.debug("trying to append children");
                            appendChildren(theNode, splitOpenPathList(openPaths), context, nodeTypes, mimeTypes, filters, noFolders);
                        }
                        result.add(theNode);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        Collections.sort(result);
        return result;
    }

    private static String[] getFiltersToApply(String filter) {
        if (filter == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] filtersToApply = StringUtils.isNotEmpty(filter) ? StringUtils
                .split(filter, ',') : ArrayUtils.EMPTY_STRING_ARRAY;
        for (int i = 0; i < filtersToApply.length; i++) {
            filtersToApply[i] = StringUtils.trimToNull(filtersToApply[i]);
        }

        return filtersToApply;
    }

    private static boolean matchesFilters(String nodeName, String[] filters) {
        if (nodeName == null || filters.length == 0) {
            return true;
        }
        boolean matches = false;
        for (String wildcard : filters) {
            if (FilenameUtils.wildcardMatch(nodeName, wildcard,
                    IOCase.INSENSITIVE)) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    private static boolean matchesNodeType(Node node, String[] nodeTypes) {
        if (nodeTypes.length == 0) {
            return true;
        }
        for (String nodeType : nodeTypes) {
            try {
                if (node.isNodeType(nodeType)) {
                    return true;
                }
            } catch (RepositoryException e) {
                logger.error("can't get nodetype", e);
            }
        }
        return false;
    }

    public static List<GWTJahiaNode> retrieveRoot(String key, ProcessingContext jParams, String nodeTypes, String mimeTypes, String filters, String openPaths) throws GWTJahiaServiceException {
        List<GWTJahiaNode> userNodes = new ArrayList<GWTJahiaNode>();
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        logger.debug("open paths for getRoot : " + openPaths);
        if (key.startsWith("/")) {
            GWTJahiaNode root = getNode(key, jParams.getUser());
            if (root != null) userNodes.add(root);
        } else if (key.equals(JCRClientUtils.MY_REPOSITORY)) {
            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                GWTJahiaNode root = getNode(node.getPath() + "/files", jParams.getUser());
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }
        } else if (key.equals(JCRClientUtils.MY_EXTERNAL_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/mounts", jParams.getUser());
            if (root != null) {
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.SHARED_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/shared/files", jParams.getUser());
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.WEBSITE_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/files", jParams.getUser());
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.MY_MASHUP_REPOSITORY)) {
            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                GWTJahiaNode root = getNode(node.getPath() + "/mashups", jParams.getUser());
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }
        } else if (key.equals(JCRClientUtils.SHARED_MASHUP_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/shared/mashups", jParams.getUser());
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/mashups", jParams.getUser());
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.ALL_FILES)) {
            GWTJahiaNode root = getNode("/content/shared/files", jParams.getUser());
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }

            root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/files", jParams.getUser());
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }

            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                root = getNode(node.getPath() + "/files", jParams.getUser());
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }

            root = getNode("/content/mounts", jParams.getUser());
            if (root != null) userNodes.add(root);
        } else if (key.equals(JCRClientUtils.ALL_MASHUPS)) {
            GWTJahiaNode root = getNode("/content/shared/mashups", jParams.getUser());
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }
            root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/mashups", jParams.getUser());
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }
            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                root = getNode(node.getPath() + "/mashups", jParams.getUser());
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }
        } else if (key.equals(JCRClientUtils.GLOBAL_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/", jParams.getUser());
            if (root != null) {
                userNodes.add(root);
            }
        }

        for (GWTJahiaNode userNode : userNodes) {
            if (openPaths != null && openPaths.length() > 0) {
                appendChildren(userNode, splitOpenPathList(openPaths), jParams, nodeTypes, mimeTypes, filters, false);
            }
        }

        return userNodes;
    }

    public static List<GWTJahiaNode> search(String searchString, int limit, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(searchString, context);
            return executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    public static List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, ProcessingContext context) throws GWTJahiaServiceException {
        if (nodeTypes == null) nodeTypes = JCRClientUtils.FILE_NODETYPES;
        String[] nodeTypesToApply = getFiltersToApply(nodeTypes);
        String[] mimeTypesToMatch = getFiltersToApply(mimeTypes);
        String[] filtersToApply = getFiltersToApply(filters);
        try {
            Query q = createQuery(searchString, context);
            return executeQuery(q, nodeTypesToApply, mimeTypesToMatch, filtersToApply, context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    private static List<GWTJahiaNode> executeQuery(Query q, String[] nodeTypesToApply, String[] mimeTypesToMatch, String[] filtersToApply, ProcessingContext context) throws RepositoryException {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        QueryResult qr = q.execute();
        NodeIterator ni = qr.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (matchesNodeType(n, nodeTypesToApply) && n.isVisible()) {
                JCRNodeWrapper f = n; //jcr.getFileNode(n.getPath(), context.getUser());
                if ((filtersToApply.length == 0 && mimeTypesToMatch.length == 0)
                        || f.isCollection()
                        || (matchesFilters(f.getName(), filtersToApply) && matchesFilters(
                        f.getFileContent().getContentType(),
                        mimeTypesToMatch))) {
                    result.add(getGWTJahiaNode(f));
                }
            }
        }
        return result;
    }

    public static GWTJahiaNode saveSearch(String searchString, String name, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }
            Query q = createQuery(searchString, context);
            List<JCRNodeWrapper> users = jcr.getUserFolders(context.getSite().getSiteKey(), context.getUser());
            if (users.isEmpty()) {
                logger.error("no user folder");
                throw new GWTJahiaServiceException("No user folder to store query");
            }
            JCRNodeWrapper user = users.iterator().next();
            JCRNodeWrapper queryStore;
            if (!user.hasNode("savedSearch")) {
                queryStore = user.createCollection("savedSearch");
                user.save();
            } else {
                queryStore = jcr.getFileNode(user.getPath() + "/savedSearch", context.getUser());
            }
            String path = queryStore.getPath() + "/" + name;
            q.storeAsNode(path).getParent().save();
            return getGWTJahiaNode(jcr.getFileNode(path, context.getUser()));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("Could not store query");
        }
    }

    private static Query createQuery(String searchString, ProcessingContext context) throws RepositoryException {
        String s = "//element(*, jmix:hierarchyNode)[jcr:contains(@j:filename," + JCRContentUtils.stringToJCRSearchExp(searchString) + ")]";
        Query q = jcr.getQueryManager(context.getUser()).createQuery(s, Query.XPATH);
        return q;
    }

    public static List<GWTJahiaNode> getSavedSearch(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "//element(*, nt:query)";
            Query q = jcr.getQueryManager(context.getUser()).createQuery(s, Query.XPATH);
            return executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public static List<GWTJahiaNode> getMountpoints(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "//element(*, jnt:mountPoint)";
            Query q = jcr.getQueryManager(context.getUser()).createQuery(s, Query.XPATH);
            return executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public static GWTJahiaNode getNode(String path, JahiaUser user) {
        JCRNodeWrapper f = jcr.getFileNode(path, user);
        return getGWTJahiaNode(f);
    }

    public static GWTJahiaNode getNodeByUUID(String uuid, JahiaUser user) throws RepositoryException {
        JCRNodeWrapper f = jcr.getNodeByUUID(uuid, user);
        return getGWTJahiaNode(f);
    }

    public static GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper f) {
        List<String> list = f.getNodeTypes();
        List<String> inherited = new ArrayList<String>();
        for (String s : list) {
            try {
                ExtendedNodeType[] inh = NodeTypeRegistry.getInstance().getNodeType(s).getSupertypes();
                for (ExtendedNodeType extendedNodeType : inh) {
                    if (!inherited.contains(extendedNodeType.getName())) {
                        inherited.add(extendedNodeType.getName());
                    }
                }
            } catch (NoSuchNodeTypeException e) {
                logger.debug("NoSuchNodeTypeException", e);
            }
        }
        GWTJahiaNode n;

        // get uuid
        String uuid = null;
        try {
            uuid = f.getUUID();
        } catch (RepositoryException e) {
            logger.debug("Unable to get uuid for node " + f.getName(), e);
        }

        // get description
        String description = "";
        try {
            if (f.hasProperty("jcr:description")) {
                Value dValue = f.getProperty("jcr:description").getValue();
                if (dValue != null) {
                    description = dValue.getString();
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Unable to get description property for node " + f.getName(), e);
        }
        if (f.isFile() || f.isPortlet()) {
            n = new GWTJahiaNode(uuid, f.getName(), description, f.getProvider().decodeInternalName(f.getPath()), f.getUrl(), f.getLastModifiedAsDate(), f.getNodeTypes(), inherited, f.getFileContent().getContentLength(), new StringBuilder("icon-").append(FileUtils.getFileIcon(f.getName())).toString(), f.isWriteable(), f.isLockable(), f.isLocked(), f.getLockOwner());
            if (f.isPortlet()) {
                n.setExt("mashup");
            }
        } else {
            n = new GWTJahiaNode(uuid, f.getName(), description, f.getProvider().decodeInternalName(f.getPath()), f.getUrl(), f.getLastModifiedAsDate(), list, inherited, f.isWriteable(), f.isLockable(), f.isLocked(), f.getLockOwner());
            boolean hasChildren = false;
            boolean hasFolderChildren = false;
            if (f instanceof JCRMountPointNode) {
                hasChildren = true;
                hasFolderChildren = true;
            } else {
                for (JCRNodeWrapper w : f.getChildren()) {
                    try {
                        hasChildren = true;
                        if (w.isCollection() || w.isNodeType(Constants.JAHIANT_MOUNTPOINT)) {
                            hasFolderChildren = true;
                            break;
                        }
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            n.setHasChildren(hasChildren);
            n.setHasFolderChildren(hasFolderChildren);
        }


        List<String> names = f.getThumbnails();
        if (names.contains("thumbnail")) {
            n.setPreview(f.getThumbnailUrl("thumbnail"));
            n.setDisplayable(true);
        } else {
            StringBuilder buffer = new StringBuilder();
            ProcessingContext pBean = Jahia.getThreadParamBean() ;
            buffer.append(pBean.getScheme()).append("://").append(pBean.getServerName()).append(":").append(pBean.getServerPort()).append(Jahia.getContextPath());
            buffer.append("/engines/images/types/gwt/large/") ;
            buffer.append(n.getExt()).append(".png") ;
            n.setPreview(buffer.toString());
        }
        for (String name : names) {
            n.getThumbnailsMap().put(name, f.getThumbnailUrl(name));
        }
        try {
            if (f.hasProperty("j:height")) {
                n.setHeight(Long.valueOf(f.getProperty("j:height").getLong()).intValue());
            }
            if (f.hasProperty("j:width")) {
                n.setWidth(Long.valueOf(f.getProperty("j:width").getLong()).intValue());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        n.setNormalizedName(JCRStoreService.removeDiacritics(n.getName()));
        return n;
    }

    public static void setLock(List<String> paths, boolean locked, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper node = jcr.getFileNode(path, user);
            if (!node.hasPermission(JCRNodeWrapper.WRITE)) {
                missedPaths.add(new StringBuilder(node.getName()).append(": write access denied").toString());
            } else if (node.isLocked()) {
                if (!locked) {
                    if (!node.getLockOwner().equals(user.getUsername()) && !user.isRoot()) {
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

    public static void createFolder(String parentPath, String name, ProcessingContext context) throws GWTJahiaServiceException {
        createNode(parentPath, name, "jnt:folder", new ArrayList<GWTJahiaNodeProperty>(), context);
    }

    private static boolean getRecursedLocksAndFileUsages(JCRNodeWrapper nodeToDelete,
                                                         List<String> lockedNodes, String username) {
        if (nodeToDelete.isCollection()) {
            for (JCRNodeWrapper child : nodeToDelete.getChildren()) {
                if (child.isCollection()) {
                    getRecursedLocksAndFileUsages(child, lockedNodes, username);
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

    public static void deletePaths(List<String> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper nodeToDelete = jcr.getFileNode(path, user);
            if (!user.isRoot() && nodeToDelete.isLocked() && !nodeToDelete.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - locked by ").append(nodeToDelete.getLockOwner()).toString());
            }
            if (!nodeToDelete.hasPermission(JCRNodeWrapper.WRITE)) {
                missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString());
            } else if (!getRecursedLocksAndFileUsages(nodeToDelete, missedPaths, user.getUsername())) {
                try {
                    int status = nodeToDelete.deleteFile();
                    switch (status) {
                        case JCRNodeWrapper.OK:
                            nodeToDelete.saveSession();
                            break;
                        case JCRNodeWrapper.INVALID_FILE:
                            missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - INVALID FILE").toString());
                            break;
                        case JCRNodeWrapper.ACCESS_DENIED:
                            missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString());
                            break;
                        case JCRNodeWrapper.UNSUPPORTED_ERROR:
                            missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - UNSUPPORTED").toString());
                            break;
                        default:
                            missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - UNKNOWN ERROR").toString());
                    }
                } catch (AccessDeniedException e) {
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - ACCESS DENIED").toString());
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

    public static String getDownloadPath(String path, JahiaUser user) throws GWTJahiaServiceException {
        JCRNodeWrapper node = jcr.getFileNode(path, user);
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(user.getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getUrl();
    }

    public static String getAbsolutePath(String path, ParamBean jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node = jcr.getFileNode(path, jParams.getUser());
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(jParams.getUser().getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getAbsoluteWebdavUrl(jParams);
    }

    public static void copy(List<GWTJahiaNode> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (GWTJahiaNode aNode : paths) {
            JCRNodeWrapper node = jcr.getFileNode(aNode.getPath(), user);
            if (!node.hasPermission(JCRNodeWrapper.READ)) {
                missedPaths.add(new StringBuilder("User ").append(user.getUsername()).append(" has no read access to ").append(node.getName()).toString());
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be copied:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static void cut(List<GWTJahiaNode> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (GWTJahiaNode aNode : paths) {
            JCRNodeWrapper node = jcr.getFileNode(aNode.getPath(), user);
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

    public static void paste(final List<GWTJahiaNode> pathsToCopy, final String destinationPath, boolean cut, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (GWTJahiaNode aNode : pathsToCopy) {
            JCRNodeWrapper node = jcr.getFileNode(aNode.getPath(), user);
            String name = node.getName();
            if (node.hasPermission(JCRNodeWrapper.READ)) {
                JCRNodeWrapper dest = jcr.getFileNode(destinationPath, user);
                if (dest.isCollection()) {
                    String destPath = dest.getPath();
                    int i = 1;
                    JCRNodeWrapper target = jcr.getFileNode(dest.getPath() + "/" + name, user);

                    String basename = name;
                    int dot = basename.lastIndexOf('.');
                    String ext = "";
                    if (dot > 0) {
                        ext = basename.substring(dot);
                        basename = basename.substring(0, dot);
                    }
                    int und = basename.lastIndexOf('_');
                    if (und > -1 && basename.substring(und + 1).matches("[0-9]+")) {
                        basename = basename.substring(0, und);
                    }

                    while (target.getException() == null) {
                        name = basename + "_" + (i++) + ext;
                        target = jcr.getFileNode(dest.getPath() + "/" + name, user);
                    }
                    if (dest.isWriteable()) {
                        if (cut) {
                            try {
                                if (!node.moveFile(destPath, name)) {
                                    missedPaths.add(new StringBuilder("File ").append(name).append(" could not be moved in ").append(dest.getPath()).toString());
                                    continue;
                                }
                                dest.saveSession();
                            } catch (RepositoryException e) {
                                logger.error("Exception", e);
                                missedPaths.add(new StringBuilder("File ").append(name).append(" could not be moved in ").append(dest.getPath()).toString());
                            }
                        } else {
                            try {
                                if (!node.copyFile(destPath, name)) {
                                    missedPaths.add(new StringBuilder("File ").append(name).append(" could not be copied in ").append(dest.getPath()).toString());
                                    continue;
                                }
                                dest.saveSession();
                            } catch (RepositoryException e) {
                                logger.error("Exception", e);
                                missedPaths.add(new StringBuilder("File ").append(name).append(" could not be copied in ").append(dest.getPath()).toString());
                            }
                        }
                    } else {
                        missedPaths.add(new StringBuilder("File ").append(name).append(" could not be copied in ").append(dest.getPath()).toString());
                    }
                }
            } else {
                missedPaths.add(new StringBuilder("Source file ").append(name).append(" could not be read by ").append(user.getUsername()).append(" - ACCESS DENIED").toString());
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be pasted:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static void rename(String path, String newName, JahiaUser user) throws GWTJahiaServiceException {
        JCRNodeWrapper node = jcr.getFileNode(path, user);
        if (node.isLocked() && !node.getLockOwner().equals(user.getUsername())) {
            throw new GWTJahiaServiceException(new StringBuilder(node.getName()).append(" is locked by ").append(user.getUsername()).toString());
        } else if (!node.hasPermission(JCRNodeWrapper.WRITE)) {
            throw new GWTJahiaServiceException(new StringBuilder(node.getName()).append(" - ACCESS DENIED").toString());
        } else if (!node.renameFile(newName)) {
            throw new GWTJahiaServiceException(new StringBuilder("Could not rename file ").append(node.getName()).append(" into ").append(newName).toString());
        }
        try {
            node.saveSession();
        } catch (RepositoryException e) {
            logger.error("error", e);
            throw new GWTJahiaServiceException(new StringBuilder("Could not save file ").append(node.getName()).append(" into ").append(newName).toString());
        }
    }

    public static Map<String, GWTJahiaNodeProperty> getProperties(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper objectNode = jcr.getFileNode(path, jParams.getUser());
        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
        String propName = "null";
        try {
            PropertyIterator it = objectNode.getProperties();
            while (it.hasNext()) {
                Property prop = it.nextProperty();
                PropertyDefinition def = prop.getDefinition();
                propName = def.getName();
                // create the corresponding GWT bean
                GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                nodeProp.setName(propName);
                nodeProp.setMultiple(def.isMultiple());
                Value[] values;
                if (!def.isMultiple()) {
                    Value oneValue = prop.getValue();
                    values = new Value[]{oneValue};
                } else {
                    values = prop.getValues();
                }
                List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>(values.length);

                boolean stringValueIsNotEmpty = false;
                for (Value val : values) {
                    gwtValues.add(Utils.convertValue(val));
                    stringValueIsNotEmpty |= PropertyType.STRING == val
                            .getType()
                            && val.getString() != null
                            && val.getString().length() > 0;
                }
                if (stringValueIsNotEmpty
                        && SelectorType.CATEGORY == JCRContentUtils
                        .getPropertyDefSelector(def)) {
                    List<GWTJahiaNodePropertyValue> adjustedGwtValues = new ArrayList<GWTJahiaNodePropertyValue>(
                            values.length);
                    for (GWTJahiaNodePropertyValue jahiaNodePropertyValue : gwtValues) {
                        if (jahiaNodePropertyValue.getString() != null
                                && jahiaNodePropertyValue.getString().length() > 0) {
                            adjustedGwtValues
                                    .add(new GWTJahiaNodePropertyValue(
                                            Category
                                                    .getCategoryKey(jahiaNodePropertyValue
                                                    .getString()),
                                            jahiaNodePropertyValue.getType()));
                        } else {
                            adjustedGwtValues.add(jahiaNodePropertyValue);
                        }
                    }
                    gwtValues = adjustedGwtValues;
                }
                nodeProp.setValues(gwtValues);
                props.put(nodeProp.getName(), nodeProp);
            }
        } catch (RepositoryException e) {
            logger.error("Cannot access property " + propName + " of node " + objectNode.getName(), e);
        }
        return props;
    }

    /**
     * A batch-capable save properties method.
     *
     * @param nodes    the nodes to save the properties of
     * @param newProps the new properties
     * @param user     the current user
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          sthg bad happened
     */
    public static void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, JahiaUser user) throws GWTJahiaServiceException {
        for (GWTJahiaNode aNode : nodes) {
            JCRNodeWrapper objectNode = jcr.getFileNode(aNode.getPath(), user);
            setProperties(objectNode, newProps);
            try {
                objectNode.save();
            } catch (RepositoryException e) {
                logger.error("error", e);
                throw new GWTJahiaServiceException("Could not save file " + objectNode.getName());
            }
        }
    }

    private static void setProperties(Node objectNode, List<GWTJahiaNodeProperty> newProps) {
        for (GWTJahiaNodeProperty prop : newProps) {
            try {
                if (prop != null && !prop.getName().equals("*")) {
                    boolean isCategory = SelectorType.CATEGORY == JCRContentUtils
                            .getPropertyDefSelector(((ExtendedNodeType) objectNode
                                    .getDefinition().getDeclaringNodeType())
                                    .getPropertyDefinitionsAsMap().get(
                                    prop.getName()));
                    if (prop.isMultiple()) {
                        List<Value> values = new ArrayList<Value>();
                        for (GWTJahiaNodePropertyValue val : prop.getValues()) {
                            if (isCategory) {
                                values.addAll(getCategoryPathValues(val.getString()));
                            } else {
                                values.add(Utils.convertValue(val));
                            }
                        }
                        Value[] finalValues = new Value[values.size()];
                        values.toArray(finalValues);
                        objectNode.setProperty(prop.getName(), finalValues);
                    } else {
                        if (prop.getValues().size() > 0) {
                            GWTJahiaNodePropertyValue propValue = prop.getValues().get(0);
                            if (propValue.getType() == GWTJahiaNodePropertyType.ASYNC_UPLOAD) {
                                GWTFileManagerUploadServlet.Item i = GWTFileManagerUploadServlet.getItem(propValue.getString());
                                ExtendedNodeDefinition end = ((ExtendedNodeType) objectNode.getPrimaryNodeType()).getChildNodeDefinitionsAsMap().get(prop.getName());


                                if (end != null) {
                                    try {
                                        if (objectNode.hasNode(prop.getName())) {
                                            objectNode.getNode(prop.getName()).remove();
                                        }
                                        String s = end.getRequiredPrimaryTypesNames()[0];
                                        Node content = objectNode.addNode(prop.getName(), s.equals("nt:base") ? "jnt:resource" : s);

                                        content.setProperty(Constants.JCR_MIMETYPE, i.contentType);
                                        content.setProperty(Constants.JCR_DATA, i.file);
                                        content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                    } catch (Throwable e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                            } else {
                                if (isCategory) {
                                    List<Value> pathValues = getCategoryPathValues(propValue.getString());
                                    Value[] values = new Value[pathValues.size()];
                                    values = pathValues.toArray(values);
                                    objectNode.setProperty(prop.getName(), values);
                                } else {
                                    Value value = Utils.convertValue(propValue);
                                    objectNode.setProperty(prop.getName(), value);
                                }
                            }
                        } else if (objectNode.hasProperty(prop.getName())) {
                            objectNode.getProperty(prop.getName()).remove();
                        }
                    }

                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static List<Value> getCategoryPathValues(String value) {
        if (value == null || value.length() == 0) {
            return Collections.EMPTY_LIST;
        }

        List<Value> values = new LinkedList<Value>();
        String[] categories = StringUtils.split(value, ",");
        for (String categoryKey : categories) {
            try {
                values.add(new StringValue(Category.getCategoryPath(categoryKey.trim())));
            } catch (JahiaException e) {
                logger.warn(
                        "Unable to retrieve category path for category key '"
                                + categoryKey + "'. Cause: " + e.getMessage(),
                        e);
            }
        }
        return values;
    }

    public static GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, ProcessingContext context) throws GWTJahiaServiceException {
        if (jcr.getFileNode(parentPath + "/" + name, context.getUser()).isValid()) {
            throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
        }
        JCRNodeWrapper parentNode = jcr.getFileNode(parentPath, context.getUser());
        JCRNodeWrapper childNode = addNode(parentNode, name, nodeType, props);
        try {
            parentNode.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Folder creation failed");
        }
        return getGWTJahiaNode(childNode);
    }

    private static JCRNodeWrapper addNode(JCRNodeWrapper parentNode, String name, String nodeType, List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        if (!parentNode.hasPermission(JCRNodeWrapper.WRITE)) {
            throw new GWTJahiaServiceException(new StringBuilder(parentNode.getPath()).append(" - ACCESS DENIED").toString());
        }
        JCRNodeWrapper childNode = null;
        if (parentNode.isValid() && !parentNode.isFile() && parentNode.isWriteable()) {
            try {
                childNode = parentNode.addNode(name, nodeType);
                setProperties(childNode, props);
            } catch (Exception e) {
                logger.error("Exception", e);
                throw new GWTJahiaServiceException("Folder creation failed");
            }
        }
        if (childNode == null || !childNode.isValid()) {
            throw new GWTJahiaServiceException("Folder creation failed");
        }
        return childNode;
    }

    public static GWTJahiaNode unsecureCreateNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, ProcessingContext context) throws GWTJahiaServiceException {
        if (jcr.getFileNode(parentPath + "/" + name, context.getUser()).isValid()) {
            throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
        }
        JCRNodeWrapper parentNode = jcr.getFileNode(parentPath, context.getUser());
        JCRNodeWrapper childNode = unsecureAddNode(parentNode, name, nodeType, props);
        try {
            parentNode.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Folder creation failed");
        }
        return getGWTJahiaNode(childNode);
    }

    private static JCRNodeWrapper unsecureAddNode(JCRNodeWrapper parentNode, String name, String nodeType, List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        JCRNodeWrapper childNode = null;
        if (parentNode.isValid() && !parentNode.isFile()) {
            try {
                childNode = parentNode.addNode(name, nodeType);
                setProperties(childNode, props);
            } catch (Exception e) {
                logger.error("Exception", e);
                throw new GWTJahiaServiceException("Folder creation failed");
            }
        }
        if (childNode == null || !childNode.isValid()) {
            throw new GWTJahiaServiceException("Folder creation failed");
        }
        return childNode;
    }

    public static GWTJahiaNodeACL getACL(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node = jcr.getFileNode(path, jParams.getUser());
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

    public static void setACL(String path, GWTJahiaNodeACL acl, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node = jcr.getFileNode(path, jParams.getUser());
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

    public static List<GWTJahiaNodeUsage> getUsages(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node = jcr.getFileNode(path, jParams.getUser());
        List<UsageEntry> usages = node.findUsages(jParams, false);
        List<GWTJahiaNodeUsage> result = new ArrayList<GWTJahiaNodeUsage>();

        for (UsageEntry usage : usages) {
            result.add(new GWTJahiaNodeUsage(usage.getId(), usage.getVersion(), usage.getWorkflow(), usage.getExtendedWorkflowState(), usage.getLang(), usage.getPageTitle(), usage.getUrl()));
        }
        return result;
    }

    public static void zip(List<String> paths, String archiveName, JahiaUser user) throws GWTJahiaServiceException {
        if (!archiveName.endsWith(".zip") && !archiveName.endsWith(".ZIP")) {
            archiveName = new StringBuilder(archiveName).append(".zip").toString();
        }
        List<String> missedPaths = new ArrayList<String>();
        List<JCRNodeWrapper> nodesToZip = new ArrayList<JCRNodeWrapper>();
        for (String path : paths) {
            JCRNodeWrapper nodeToZip = jcr.getFileNode(path, user);
            if (nodeToZip.hasPermission(JCRNodeWrapper.READ)) {
                nodesToZip.add(nodeToZip);
            } else {
                missedPaths.add(nodeToZip.getName());
            }
        }
        if (nodesToZip.size() > 0) {
            String firstPath = nodesToZip.get(0).getPath();
            int index = firstPath.lastIndexOf("/");
            String parentPath;
            if (index > 0) {
                parentPath = firstPath.substring(0, index);
            } else {
                parentPath = "/";
            }
            JCRNodeWrapper parent = jcr.getFileNode(parentPath, user);
            if (parent.isWriteable()) {
                List<String> errorPaths = JCRZipTools.zipFiles(parent, archiveName, nodesToZip);
                if (errorPaths != null) {
                    errorPaths.addAll(missedPaths);
                    StringBuilder errors = new StringBuilder("The following files could not be zipped:");
                    for (String err : errorPaths) {
                        errors.append("\n").append(err);
                    }
                    throw new GWTJahiaServiceException(errors.toString());
                }
            } else {
                throw new GWTJahiaServiceException("Directory " + parent.getPath() + " is not writable.");
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be zipped:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static void unzip(List<String> paths, boolean removeArchive, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        List<JCRNodeWrapper> nodesToUnzip = new ArrayList<JCRNodeWrapper>();
        for (String path : paths) {
            JCRNodeWrapper nodeToUnzip = jcr.getFileNode(path, user);
            if (nodeToUnzip.hasPermission(JCRNodeWrapper.READ)) {
                nodesToUnzip.add(nodeToUnzip);
            } else {
                missedPaths.add(nodeToUnzip.getName());
            }
        }
        if (nodesToUnzip.size() > 0) {
            String firstPath = nodesToUnzip.get(0).getPath();
            int index = firstPath.lastIndexOf("/");
            String parentPath;
            if (index > 0) {
                parentPath = firstPath.substring(0, index);
            } else {
                parentPath = "/";
            }
            JCRNodeWrapper parent = jcr.getFileNode(parentPath, user);
            if (parent.isWriteable()) {
                for (JCRNodeWrapper nodeToUnzip : nodesToUnzip) {
                    try {
                        if (!JCRZipTools.unzipFile(nodeToUnzip, parent, user)) {
                            missedPaths.add(nodeToUnzip.getName());
                        } else if (removeArchive) {
                            if (nodeToUnzip.deleteFile() != JCRNodeWrapper.OK) {
                                logger.error("Issue when trying to delete original archive " + nodeToUnzip.getPath());
                            }
                        }
                    } catch (RepositoryException e) {
                        missedPaths.add(nodeToUnzip.getName());
                        logger.error(e, e);
                    }
                }
            } else {
                throw new GWTJahiaServiceException("Directory " + parent.getPath() + " is not writable.");
            }
            try {
                parent.saveSession();
            } catch (RepositoryException e) {
                logger.error("Could not save changes in " + parent.getPath(), e);
            }
        }
        if (missedPaths.size() > 0) {
            StringBuilder errors = new StringBuilder("The following files could not be unzipped:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static void mount(String parentPath, String name, String root, JahiaUser user) throws GWTJahiaServiceException {
        List<JCRNodeWrapper> userFolders = jcr.getUserFolders(null, user);
        if (userFolders.size() == 1) {
            JCRNodeWrapper parent = jcr.getFileNode("/content", user);
            JCRNodeWrapper mounts = jcr.getFileNode(parent.getPath() + "/mounts", user);
            if (!mounts.isValid()) {
                try {
                    mounts = parent.createCollection("mounts");
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                    throw new GWTJahiaServiceException("Could not create 'mounts' folder");
                }
            } else if (!mounts.isCollection()) {
                throw new GWTJahiaServiceException("A file already exists with name 'mounts'");
            }

            JCRMountPointNode childNode = null;
            if (mounts.isValid() && !mounts.isFile() && mounts.isWriteable()) {
                try {
                    childNode = (JCRMountPointNode) mounts.addNode(name, "jnt:vfsMountPoint");
                    childNode.setProperty("j:root", root);

                    boolean valid = childNode.checkValidity();
                    if (!valid) {
                        childNode.remove();
                        throw new GWTJahiaServiceException("Invalid path");
                    }
                } catch (RepositoryException e) {
                    logger.error("Exception", e);
                    throw new GWTJahiaServiceException("Folder creation failed");
                }
                try {
                    parent.save();
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                    throw new GWTJahiaServiceException("Folder creation failed");
                }
            }
            if (childNode == null || !childNode.isValid()) {
                throw new GWTJahiaServiceException("Folder creation failed");
            }
        }
    }

    public static List<GWTJahiaPortletDefinition> searchPortlets(ProcessingContext jParams) {
        List<GWTJahiaPortletDefinition> results = new ArrayList<GWTJahiaPortletDefinition>();
        try {
            List appList = ServicesRegistry.getInstance().getApplicationsManagerService().getApplications();
            for (Iterator iterator = appList.iterator(); iterator.hasNext();) {
                ApplicationBean appBean = (ApplicationBean) iterator.next();


                if (appBean.getACL().getPermission(null, null, jParams.getUser(), JahiaBaseACL.READ_RIGHTS, true, jParams.getSiteID())) {
                    List l = appBean.getEntryPointDefinitions();
                    for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {
                        EntryPointDefinition entryPointDefinition = (EntryPointDefinition) iterator1.next();
                        results.add(createGWTJahiaPortletDefinition(appBean, entryPointDefinition));
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    /**
     * Create a GWTJahiaPortletDefinition object from an applicationBean and an entryPointDefinition objects
     *
     * @param appBean
     * @param entryPointDefinition
     * @return
     * @throws JahiaException
     */
    private static GWTJahiaPortletDefinition createGWTJahiaPortletDefinition(ApplicationBean appBean, EntryPointDefinition entryPointDefinition) throws JahiaException {
        String portletType = null;
        int expTime = 0;
        String cacheScope = null;
        if (entryPointDefinition instanceof PortletEntryPointDefinition) {
            portletType = ((PortletEntryPointDefinition) entryPointDefinition).getInitParameter("portletType");
            ExpirationCacheDD dd = ((PortletEntryPointDefinition) entryPointDefinition).getPortletDefinition().getExpirationCacheDD();
            if (dd != null) {
                expTime = dd.getExpirationTime();
                cacheScope = dd.getScope();
            }
        }
        if (portletType == null) {
            portletType = "jnt:portlet";
        }
        GWTJahiaNodeACL gwtJahiaNodeACL = new GWTJahiaNodeACL(new ArrayList<GWTJahiaNodeACE>());
        gwtJahiaNodeACL.setAvailablePermissions(JCRPortletNode.getAvailablePermissions(appBean.getContext(), entryPointDefinition.getName()));
        return new GWTJahiaPortletDefinition(appBean.getContext(), entryPointDefinition.getName(), portletType, gwtJahiaNodeACL, entryPointDefinition.getDescription(), expTime, cacheScope);
    }

    /**
     * Create a  GWTJahiaNode object that represents a portlet instance.
     *
     * @param parentPath
     * @param gwtJahiaNewPortletInstance
     * @param context
     * @return
     * @throws GWTJahiaServiceException
     */
    public static GWTJahiaNode createPortletInstance(String parentPath, GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String name = gwtJahiaNewPortletInstance.getInstanceName();

            if (name == null) {
                name = gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName().replaceAll("/", "___") + Math.round(Math.random() * 1000000l);
            }

            if (jcr.getFileNode(parentPath + "/" + name, context.getUser()).isValid()) {
                throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
            }
            JCRNodeWrapper parentNode = jcr.getFileNode(parentPath, context.getUser());
            JCRPortletNode node = (JCRPortletNode) addNode(parentNode, name, gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getPortletType(), gwtJahiaNewPortletInstance.getProperties());
            node.setApplication(gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getContextName(), gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName());
            node.revokeAllPermissions();
            for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getModes().getAce()) {
                String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                if (!ace.isInherited()) {
                    node.changePermissions(user, ace.getPermissions());
                }
            }
            for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getRoles().getAce()) {
                String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                if (!ace.isInherited()) {
                    node.changePermissions(user, ace.getPermissions());
                }
            }

            try {
                parentNode.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException("Folder creation failed");
            }
            return getGWTJahiaNode(node);
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("error");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Create a portlet instance
     *
     * @param parentPath
     * @param instanceName
     * @param appName
     * @param entryPointName
     * @param gwtJahiaNodeProperties
     * @param context
     * @return
     * @throws GWTJahiaServiceException
     */
    public static GWTJahiaNode createPortletInstance(String parentPath, String instanceName, String appName, String entryPointName, List<GWTJahiaNodeProperty> gwtJahiaNodeProperties, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            // get RSS GWTJahiaPortletDefinition
            GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, entryPointName, context);
            if (gwtJahiaPortletDefinition == null) {
                logger.error("[" + appName + "," + entryPointName + "]" + " portlet defintion not found --> Aboard creating  portlet instance");
            }

            GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
            gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

            // add url property
            gwtJahiaNewPortletInstance.getProperties().addAll(gwtJahiaNodeProperties);
            gwtJahiaNewPortletInstance.getProperties().add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));

            GWTJahiaNodeACL acl = gwtJahiaPortletDefinition.getBaseAcl();

            // all modes for users of the current site
            GWTJahiaNodeACL modes = gwtJahiaNewPortletInstance.getModes();
            if (modes == null) {
                modes = new GWTJahiaNodeACL();
            }
            List<GWTJahiaNodeACE> modeAces = modes.getAce();
            if (modeAces == null) {
                modeAces = new ArrayList<GWTJahiaNodeACE>();
            }
            if (acl != null && acl.getAvailablePermissions() != null) {
                List<String> modesPermissions = acl.getAvailablePermissions().get(JCRClientUtils.MODES_ACL);
                modeAces.add(ACLHelper.createUsersGroupACE(modesPermissions, true, context));
            }
            modes.setAce(modeAces);
            gwtJahiaNewPortletInstance.setModes(modes);

            // all rodes for users of the current site
            GWTJahiaNodeACL roles = gwtJahiaNewPortletInstance.getRoles();
            if (roles == null) {
                roles = new GWTJahiaNodeACL();
            }
            List<GWTJahiaNodeACE> roleAces = roles.getAce();
            if (roleAces == null) {
                roleAces = new ArrayList<GWTJahiaNodeACE>();
            }
            if (acl != null && acl.getAvailablePermissions() != null) {
                List<String> rolesPermissions = acl.getAvailablePermissions().get(JCRClientUtils.ROLES_ACL);
                roleAces.add(ACLHelper.createUsersGroupACE(rolesPermissions, true, context));
            }
            roles.setAce(roleAces);
            gwtJahiaNewPortletInstance.setRoles(roles);

            // set name
            gwtJahiaNewPortletInstance.setInstanceName(instanceName);
            return createPortletInstance(parentPath, gwtJahiaNewPortletInstance, context);
        } catch (GWTJahiaServiceException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Unable to create an RSS portlet due to: ", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Create an instance of an RSS portlet
     *
     * @param parentPath
     * @param name
     * @param url
     * @param context
     * @return
     * @throws GWTJahiaServiceException
     */
    public static GWTJahiaNode createRSSPortletInstance(String parentPath, String name, String url, ProcessingContext context) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();

        // get RSS GWTJahiaPortletDefinition
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName("rss", "JahiaRSSPortlet", context);
        if (gwtJahiaPortletDefinition == null) {
            logger.error("RSS portlet defintion not found --> Aboard creating RSS portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("url", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, "rss", "JahiaRSSPortlet", gwtJahiaNodeProperties, context);
    }

     public static GWTJahiaNode createGoogleGadgetPortletInstance(String parentPath, String name,String script, ProcessingContext context) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();

        // get RSS GWTJahiaPortletDefinition
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName("googlegadget", "JahiaGoogleGadget", context);
        if (gwtJahiaPortletDefinition == null) {
            logger.error("RSS portlet defintion not found --> Aboard creating Google Gadget portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue("", GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("code", new GWTJahiaNodePropertyValue(script, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name,"googlegadget", "JahiaGoogleGadget", gwtJahiaNodeProperties, context);
    }


    /**
     * 
     * @param appName
     * @param entryPointName
     * @param context
     * @return
     */
    private static GWTJahiaPortletDefinition createJahiaGWTPortletDefinitionByName(String appName, String entryPointName, ProcessingContext context) {
        if (appName != null && entryPointName != null) {
            try {
                // TO DO: replace this part of the method by a more perfoming one
                List appList = ServicesRegistry.getInstance().getApplicationsManagerService().getApplications();
                for (Iterator iterator = appList.iterator(); iterator.hasNext();) {
                    ApplicationBean appBean = (ApplicationBean) iterator.next();
                    if (appBean.getACL().getPermission(null, null, context.getUser(), JahiaBaseACL.READ_RIGHTS, true, context.getSiteID())) {
                        List l = appBean.getEntryPointDefinitions();
                        for (Iterator iterator1 = l.iterator(); iterator1.hasNext();) {
                            EntryPointDefinition entryPointDefinition = (EntryPointDefinition) iterator1.next();
                            boolean foundEntryPointDefinition = appName.equalsIgnoreCase(appBean.getName()) && entryPointDefinition.getName().equalsIgnoreCase(entryPointName);
                            if (foundEntryPointDefinition) {
                                return createGWTJahiaPortletDefinition(appBean, entryPointDefinition);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        logger.error("Portlet definition not found: " + appName + "[Jahia context path:" + context.getContextPath() + "]");
        return null;
    }

    public static boolean checkCaptcha(ParamBean context, String captcha) {
        final String captchaId = context.getSessionState().getId();
        if (logger.isDebugEnabled()) logger.debug("j_captcha_response: " + captcha);
        boolean isResponseCorrect = false;
        try {
            isResponseCorrect = CaptchaService.getInstance().validateResponseForID(captchaId, captcha);
            if (logger.isDebugEnabled()) logger.debug("CAPTCHA - isResponseCorrect: " + isResponseCorrect);
        } catch (final Exception e) {
            //should not happen, may be thrown if the id is not valid
            logger.debug("Error when calling CaptchaService", e);
        }
        return isResponseCorrect;
    }


}
