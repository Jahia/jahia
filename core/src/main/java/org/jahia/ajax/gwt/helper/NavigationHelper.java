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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.FileUtils;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import java.net.MalformedURLException;
import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:16:27 PM
 */
public class NavigationHelper {
    private static Logger logger = Logger.getLogger(NavigationHelper.class);

    public final static String SAVED_OPEN_PATHS = "org.jahia.contentmanager.savedopenpaths.";
    public final static String SELECTED_PATH = "org.jahia.contentmanager.selectedpath.";

    private JCRStoreService jcrService;
    private JCRSessionFactory sessionFactory;
    private JCRVersionService jcrVersionService;

    private PublicationHelper publication;


    public void setJcrService(JCRStoreService jcrService) {
        this.jcrService = jcrService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setPublication(PublicationHelper publication) {
        this.publication = publication;
    }

    public void setJcrVersionService(JCRVersionService jcrVersionService) {
        this.jcrVersionService = jcrVersionService;
    }

    /**
     * like ls unix command on the folder
     *
     * @param gwtParentNode
     * @param nodeTypes
     * @param mimeTypes
     * @param nameFilters
     * @param fields
     * @param currentUserSession @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> ls(GWTJahiaNode gwtParentNode, String nodeTypes, String mimeTypes, String nameFilters,
                                 List<String> fields, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node = null;
        try {
            node = currentUserSession.getNode(gwtParentNode != null ? gwtParentNode.getPath() : "/");
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
        }

        if (node == null) {
            throw new GWTJahiaServiceException("Parent node is null");
        }
        if (node.isFile()) {
            throw new GWTJahiaServiceException("Can't list the children of a file");
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(
                    new StringBuilder("User ").append(currentUserSession.getUser().getUsername())
                            .append(" has no read access to ").append(node.getName()).toString());
        }
        try {
            final NodeIterator nodesIterator = node.getNodes();

            if (nodesIterator == null) {
                throw new GWTJahiaServiceException("Children list is null");
            }
            if (nodeTypes == null) {
                nodeTypes = JCRClientUtils.FILE_NODETYPES;
            }
            String[] nodeTypesToApply = getFiltersToApply(nodeTypes);
            String[] mimeTypesToMatch = getFiltersToApply(mimeTypes);
            String[] namefiltersToApply = getFiltersToApply(nameFilters);

            final List<GWTJahiaNode> gwtNodeChildren = new ArrayList<GWTJahiaNode>();

            while (nodesIterator.hasNext()) {
                JCRNodeWrapper childNode = (JCRNodeWrapper) nodesIterator.nextNode();
                if (logger.isDebugEnabled()) {
                    logger.debug(new StringBuilder("processing ").append(childNode.getPath()).toString());
                }

                // in case of a folder, it allows to know if the node is selectable
                boolean matchVisibilityFilter = childNode.isVisible();
                boolean matchNodeType = matchesNodeType(childNode, nodeTypesToApply);
                if (logger.isDebugEnabled()) {
                    logger.debug("----------");
                    for (String s : nodeTypesToApply) {
                        logger.debug(
                                "Node " + childNode.getPath() + " match with " + s + "? " + childNode.isNodeType(s) +
                                        "[" + matchNodeType + "]");
                    }
                    logger.debug("----------");
                }
                boolean mimeTypeFilter = matchesMimeTypeFilters(childNode, mimeTypesToMatch);
                boolean nameFilter = matchesFilters(childNode.getName(), namefiltersToApply);
                boolean hasNodes = false;
                try {
                    hasNodes = childNode.getNodes().hasNext();
                } catch (RepositoryException e) {
                    logger.error(e, e);
                }
                // collection condition is available only if the parent node is not a nt:query. Else, the node has to matcch the node type condition
                if (matchVisibilityFilter && matchNodeType && (mimeTypeFilter || hasNodes) && nameFilter) {
                    GWTJahiaNode gwtChildNode = getGWTJahiaNode(childNode, fields);
                    gwtChildNode.setMatchFilters(matchNodeType && mimeTypeFilter);
                    gwtNodeChildren.add(gwtChildNode);
                }
            }

            return gwtNodeChildren;
        } catch (RepositoryException e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public String[] getFiltersToApply(String filter) {
        if (filter == null) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        String[] filtersToApply =
                StringUtils.isNotEmpty(filter) ? StringUtils.split(filter, ',') : ArrayUtils.EMPTY_STRING_ARRAY;
        for (int i = 0; i < filtersToApply.length; i++) {
            filtersToApply[i] = StringUtils.trimToNull(filtersToApply[i]);
        }

        return filtersToApply;
    }

    public boolean matchesFilters(String nodeName, String[] filters) {
        if (nodeName == null || filters.length == 0) {
            return true;
        }
        boolean matches = false;
        for (String wildcard : filters) {
            if (FilenameUtils.wildcardMatch(nodeName, wildcard, IOCase.INSENSITIVE)) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    public boolean matchesMimeTypeFilters(JCRNodeWrapper node, String[] filters) {
        // no filters
        if (filters == null || filters.length == 0) {
            return true;
        }

        // there are filters, but not a file
        if (!node.isFile()) {
            return false;
        }

        // do filter
        return matchesFilters(node.getFileContent().getContentType(), filters);
    }

    public boolean matchesNodeType(JCRNodeWrapper node, String[] nodeTypes) {
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

    public List<GWTJahiaNode> retrieveRoot(String repositoryKey, String nodeTypes, String mimeTypes, String filters,
                                           List<String> fields, List<String> selectedNodes, List<String> openPaths,
                                           JCRSiteNode site, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        List<GWTJahiaNode> userNodes = new ArrayList<GWTJahiaNode>();
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        logger.debug("open paths for getRoot : " + openPaths);

        String[] keys = repositoryKey.split(";");
        for (String key : keys) {
            try {
                if (key.startsWith("/")) {
                    GWTJahiaNode root = getNode(key, fields, currentUserSession);
                    if (root != null) {
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.MY_REPOSITORY)) {
                    JCRNodeWrapper node = jcrService.getUserFolder(currentUserSession.getUser());
                    GWTJahiaNode root = getNode(node.getPath() + "/files", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName(currentUserSession.getUser().getName());
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.USERS_REPOSITORY)) {
                    try {
                        NodeIterator ni = currentUserSession.getNode("/users").getNodes();
                        while (ni.hasNext()) {
                            Node node = (Node) ni.next();
                            GWTJahiaNode jahiaNode = getGWTJahiaNode((JCRNodeWrapper) node.getNode("files"), fields);
                            jahiaNode.setDisplayName(node.getName());
                            userNodes.add(jahiaNode);
                        }
                    } catch (RepositoryException e) {
                        // e.printStackTrace();
                    }
                    Collections.sort(userNodes, new Comparator<GWTJahiaNode>() {
                        public int compare(GWTJahiaNode o1, GWTJahiaNode o2) {
                            return o1.getDisplayName().compareTo(o2.getDisplayName());
                        }
                    });
                } else if (key.equals(JCRClientUtils.MY_EXTERNAL_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/mounts", fields, currentUserSession);
                    if (root != null) {
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.SHARED_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/shared/files", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName("shared");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.WEBSITE_REPOSITORY)) {
                    GWTJahiaNode root = getGWTJahiaNode(site.getNode("files"), fields);
                    if (root != null) {
                        root.setDisplayName(site.getSiteKey());
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.MY_MASHUP_REPOSITORY)) {
                    JCRNodeWrapper node = jcrService.getUserFolder(currentUserSession.getUser());
                    GWTJahiaNode root = getNode(node.getPath() + "/mashups", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName(currentUserSession.getUser().getName());
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.SHARED_MASHUP_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/shared/mashups", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName("shared");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY)) {
                    GWTJahiaNode root = getGWTJahiaNode(site.getNode("mashups"), fields);
                    if (root != null) {
                        root.setDisplayName(site.getSiteKey());
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.ALL_FILES)) {
                    addShareSiteUserFolder(userNodes, "files", site, fields, currentUserSession);
                    GWTJahiaNode root = getNode("/mounts", fields, currentUserSession);
                    if (root != null) {
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.ALL_CONTENT)) {
                    addShareSiteUserFolder(userNodes, "contents", site, fields, currentUserSession);
                } else if (key.equals(JCRClientUtils.ALL_MASHUPS)) {
                    addShareSiteUserFolder(userNodes, "mashups", site, fields, currentUserSession);
                } else if (key.equals(JCRClientUtils.CATEGORY_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/categories", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName("categories");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.TAG_REPOSITORY)) {
                    GWTJahiaNode root = getGWTJahiaNode(site.getNode("tags"), fields);
                    if (root != null) {
                        root.setDisplayName("tags");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.ROLE_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/roles", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName("roles");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.SITE_ROLE_REPOSITORY)) {
                    GWTJahiaNode root = getGWTJahiaNode(site.getNode("roles"), fields);
                    if (root != null) {
                        root.setDisplayName("roles");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.PORTLET_DEFINITIONS_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/portletdefinitions", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName("Portlet Definition");

                        userNodes.add(root);

                    }
                } else if (key.equals(JCRClientUtils.SITE_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/sites", fields, currentUserSession);
                    if (root != null) {
                        List<GWTJahiaNode> list = ls(root, "jnt:virtualsite", null, null, fields, currentUserSession);
                        userNodes.addAll(list);
                    }
                } else if (key.equals(JCRClientUtils.TEMPLATES_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/templatesSet", fields, currentUserSession);
                    if (root != null) {
                        List<GWTJahiaNode> list = ls(root, "jnt:virtualsite", null, null, fields, currentUserSession);
                        userNodes.addAll(list);
                    }
                } else if (key.equals(JCRClientUtils.REMOTEPUBLICATIONS_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/remotePublications", fields, currentUserSession);
                    if (root != null) {
                        root.setDisplayName("remotePublications");
                        userNodes.add(root);
                    }
                } else if (key.equals(JCRClientUtils.GLOBAL_REPOSITORY)) {
                    GWTJahiaNode root = getNode("/", fields, currentUserSession);
                    if (root != null) {
                        userNodes.add(root);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        List<GWTJahiaNode> allNodes = new ArrayList<GWTJahiaNode>(userNodes);
        if (selectedNodes != null) {
            if (openPaths == null) {
                openPaths = selectedNodes;
            } else {
                openPaths.addAll(selectedNodes);
            }
        }
        if (openPaths != null) {
            for (String openPath : new HashSet<String>(openPaths)) {
                try {
                    for (int i = 0; i < allNodes.size(); i++) {
                        GWTJahiaNode node = allNodes.get(i);
                        if (openPath.startsWith(node.getPath()) && !node.isExpandOnLoad() && !node.isFile()) {
                            node.setExpandOnLoad(true);
                            List<GWTJahiaNode> list =
                                    ls(node, nodeTypes, mimeTypes, filters, fields, currentUserSession);
                            for (int j = 0; j < list.size(); j++) {
                                node.insert(list.get(j), j);
                                allNodes.add(list.get(j));
                            }
                        }
                        if (selectedNodes != null && selectedNodes.contains(node.getPath())) {
                            node.setSelectedOnLoad(true);
                        }
                    }
                } catch (Throwable e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return userNodes;
    }

    private void addShareSiteUserFolder(List<GWTJahiaNode> userNodes, String folderName, JCRSiteNode site,
                                        List<String> fields, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        GWTJahiaNode root = null;
        try {
            root = getNode("/shared/" + folderName, fields, currentUserSession);
            root.setDisplayName("shared");
            userNodes.add(root);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            root = getGWTJahiaNode(site.getNode(folderName));
            root.setDisplayName(site.getSiteKey());
            userNodes.add(root);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            JCRNodeWrapper node = jcrService.getUserFolder(currentUserSession.getUser());
            root = getGWTJahiaNode(node.getNode(folderName));
            root.setDisplayName(currentUserSession.getUser().getName());
            userNodes.add(root);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<GWTJahiaNode> getMountpoints(JCRSessionWrapper currentUserSession) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [jnt:mountPoint]";
            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return executeQuery(q, new String[0], new String[0], new String[0]);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Return a node if existing exception otherwise
     *
     * @param path               the path to test an dget the node if existing
     * @param currentUserSession @return the existing node
     * @throws GWTJahiaServiceException it node does not exist
     */
    public GWTJahiaNode getNode(String path, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(currentUserSession.getNode(path));
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    /**
     * Get tag node
     *
     * @param name
     * @param site
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode getTagNode(String name, JCRSiteNode site) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = site.getNode("tags");
            if (name == null) {
                return getGWTJahiaNode(node);
            }
            if (node.hasNode(name)) {
                return getGWTJahiaNode(node.getNode(name));
            }
            return null;
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public JCRNodeWrapper getTagsNode(JCRSiteNode site) throws GWTJahiaServiceException {
        try {
            return site.getNode("tags");
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public GWTJahiaNode getNode(String path, List<String> fields, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(currentUserSession.getNode(path), fields);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public GWTJahiaNode getParentNode(String path, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(currentUserSession.getNode(path).getParent());
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public String getDownloadPath(String path, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(e.toString());
        }
        return node.getUrl();
    }

    public String getAbsolutePath(String path, ParamBean jParams, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(
                    new StringBuilder("User ").append(currentUserSession.getUser().getUsername())
                            .append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getAbsoluteWebdavUrl(jParams);
    }

    public List<GWTJahiaNodeUsage> getUsages(String path, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        List<GWTJahiaNodeUsage> result = new ArrayList<GWTJahiaNodeUsage>();
        try {
            NodeIterator usages = node.getSharedSet();
            while (usages.hasNext()) {
                JCRNodeWrapper usage = (JCRNodeWrapper) usages.next();
                JCRNodeWrapper parent = lookUpParentPageNode(usage);

                result.add(new GWTJahiaNodeUsage(usage.getIdentifier(), usage.getPath(),
                        parent == null ? "" : parent.getPath() + ".html"));

            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
        }

        try {
            PropertyIterator references = node.getReferences();
            while (references.hasNext()) {
                JCRPropertyWrapper reference = (JCRPropertyWrapper) references.next();
                JCRNodeWrapper refNode = (JCRNodeWrapper) reference.getNode();
                JCRNodeWrapper parent = lookUpParentPageNode(refNode);
                result.add(new GWTJahiaNodeUsage(refNode.getIdentifier(), refNode.getPath(),
                        parent == null ? "" : parent.getPath() + ".html"));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    /**
     * Get nodes by category
     *
     * @param path
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> getNodesByCategory(String path, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        final List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();

        try {
            PropertyIterator references = node.getReferences();
            Set<String> alreadyIncludedIdentifiers = new HashSet<String>();
            while (references.hasNext()) {
                JCRPropertyWrapper reference = (JCRPropertyWrapper) references.next();
                JCRNodeWrapper currentNode = reference.getParent();
                // avoid duplicate and category nodes
                if (!currentNode.isNodeType(Constants.JAHIANT_CATEGORY)) {
                    if (currentNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
                        currentNode = currentNode.getParent();
                    }
                    if (!alreadyIncludedIdentifiers.contains(currentNode.getIdentifier())) {
                        result.add(getGWTJahiaNode(currentNode));
                        alreadyIncludedIdentifiers.add(currentNode.getIdentifier());
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    private JCRNodeWrapper lookUpParentPageNode(JCRNodeWrapper usage) throws RepositoryException {
        return lookUpParentNode(usage, "jnt:page");
    }

    private JCRNodeWrapper lookUpParentNode(JCRNodeWrapper usage, String nodeType) throws RepositoryException {
        if (nodeType == null) {
            return null;
        }
        // look for parent page url
        boolean pageParentFound = false;
        JCRNodeWrapper parentNode = usage.getParent();
        while (!pageParentFound) {
            if (parentNode.isNodeType(nodeType)) {
                return parentNode;
            } else {
                // case of root
                if (parentNode.getPath().lastIndexOf("/") == 0) {
                    return null;
                }

                // update parent node
                parentNode = parentNode.getParent();
            }

        }

        return null;
    }


    public List<GWTJahiaNode> executeQuery(Query q, String[] nodeTypesToApply, String[] mimeTypesToMatch,
                                           String[] filtersToApply) throws RepositoryException {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        QueryResult qr = q.execute();
        NodeIterator ni = qr.getNodes();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (matchesNodeType(n, nodeTypesToApply) && n.isVisible()) {
                // use for pickers
                boolean isFile = n.isFile();
                boolean hasNodes = false;
                try {
                    hasNodes = n.getNodes().hasNext();
                } catch (RepositoryException e) {
                    logger.error(e, e);
                }
                boolean matchFilter = (filtersToApply.length == 0 && mimeTypesToMatch.length == 0) ||
                        matchesFilters(n.getName(), filtersToApply) && matchesMimeTypeFilters(n, mimeTypesToMatch);
                if (matchFilter || hasNodes) {
                    GWTJahiaNode node = getGWTJahiaNode(n);
                    node.setMatchFilters(matchFilter);
                    result.add(node);
                }
            }
        }
        return result;
    }

    public GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node) {
        return getGWTJahiaNode(node, GWTJahiaNode.DEFAULT_FIELDS);
    }

    public GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node, List<String> fields) {
        if (fields == null) {
            fields = Collections.emptyList();
        }
        List<String> inheritedTypes = new ArrayList<String>();
        List<String> nodeTypes = null;
        try {
            nodeTypes = node.getNodeTypes();
            for (String s : nodeTypes) {
                ExtendedNodeType[] inh = NodeTypeRegistry.getInstance().getNodeType(s).getSupertypes();
                for (ExtendedNodeType extendedNodeType : inh) {
                    if (!inheritedTypes.contains(extendedNodeType.getName())) {
                        inheritedTypes.add(extendedNodeType.getName());
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Error when getting nodetypes", e);
        }
        GWTJahiaNode n;

        // get uuid
        String uuid = null;
        try {
            uuid = node.getIdentifier();
        } catch (RepositoryException e) {
            logger.debug("Unable to get uuid for node " + node.getName(), e);
        }

        // get description
        String description = "";
        try {
            if (node.hasProperty("jcr:description")) {
                Value dValue = node.getProperty("jcr:description").getValue();
                if (dValue != null) {
                    description = dValue.getString();
                }
            }
        } catch (RepositoryException e) {
            logger.debug("Unable to get description property for node " + node.getName(), e);
        }

        n = new GWTJahiaNode();
        n.setUUID(uuid);
        n.setName(node.getName());
        n.setDisplayName(node.getName());
        n.setDescription(description);
        n.setPath(node.getPath());
        n.setUrl(node.getUrl());
        n.setNodeTypes(nodeTypes);
        n.setInheritedNodeTypes(inheritedTypes);
        n.setProviderKey(node.getProvider().getKey());
        n.setWriteable(node.isWriteable());
        n.setDeleteable(node.isWriteable());
        n.setLockable(node.isLockable());
        n.setLocked(node.isLocked());
        n.setLockOwner(node.getLockOwner());
        n.setThumbnailsMap(new HashMap<String, String>());
        n.setVersioned(node.isVersioned());
        n.setLanguageCode(node.getLanguage());
        try {
            JCRSiteNode site = node.resolveSite();
            if (site != null) {
                n.setSiteUUID(site.getUUID());
                n.setAclContext("site:" + site.getName());
            } else {
                n.setAclContext("sharedOnly");
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting sitekey", e);
        }
        if (node.isFile()) {
            n.setSize(node.getFileContent().getContentLength());

        }
        n.setFile(node.isFile());

        n.setIsShared(false);
        try {
            if (node.isNodeType("mix:shareable") && node.getSharedSet().getSize() > 1) {
                n.setIsShared(true);
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting shares", e);
        }

        try {
            List<Locale> locales = node.getLockedLocales();
            for (Locale locale : locales) {
                n.setLanguageLocked(locale.toString(), true);
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting locks", e);
        }

        if (sessionFactory.getMountPoints().containsKey(node.getPath())) {
            n.setDeleteable(false);
        }
        if (fields.contains(GWTJahiaNode.CHILDREN_INFO)) {
            boolean hasChildren = false;
            if (node instanceof JCRMountPointNode) {
                hasChildren = true;
            } else if (!node.isFile()) {
                try {
                    final NodeIterator nodesIterator = node.getNodes();
                    if (nodesIterator.hasNext()) {
                        hasChildren = true;
                    }
                } catch (RepositoryException e) {
                    logger.error(e, e);
                }
            }
            n.setHasChildren(hasChildren);
        }

        if (fields.contains(GWTJahiaNode.TAGS)) {
            try {
                if (node.hasProperty("j:tags")) {
                    StringBuilder b = new StringBuilder();
                    Value[] values = node.getProperty("j:tags").getValues();
                    for (Value value : values) {
                        Node tag = ((JCRValueWrapper) value).getNode();
                        if (tag != null) {
                            b.append(", ");
                            b.append(tag.getName());
                        }
                    }
                    if (b.length() > 0) {
                        n.setTags(b.substring(2));
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Error when getting tags", e);
            }
        }

        // icons
        if (fields.contains(GWTJahiaNode.ICON)) {
            setIcon(node, n);
        }

        // thumbnails
        List<String> names = node.getThumbnails();
        if (names.contains("thumbnail")) {
            n.setPreview(node.getThumbnailUrl("thumbnail"));
            n.setDisplayable(true);
        }
        for (String name : names) {
            n.getThumbnailsMap().put(name, node.getThumbnailUrl(name));
        }

        //count
        if (fields.contains(GWTJahiaNode.COUNT)) {
            try {
                n.set("count", JCRContentUtils.size(node.getWeakReferences())+JCRContentUtils.size(node.getReferences()));
            } catch (RepositoryException e) {
                logger.warn("Unable to count node references for node");
            }
        }

        if (fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
            try {
                n.setPublicationInfo(publication.getPublicationInfo(node.getIdentifier(),
                        Collections.singleton(node.getSession().getLocale().toString()), false,
                        node.getSession()));
            } catch (UnsupportedRepositoryOperationException e) {
                // do nothing
                logger.debug(e.getMessage());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (GWTJahiaServiceException e) {
                logger.error(e.getMessage(), e);
            }
        }

        // properties
        for (String field : fields) {
            if (!GWTJahiaNode.RESERVED_FIELDS.contains(field)) {
                try {
                    if (node.hasProperty(field)) {
//                        n.set(StringUtils.substringAfter(propName, ":"), node.getProperty(propName).getString());
                        final JCRPropertyWrapper property = node.getProperty(field);
                        if (property.getType() == PropertyType.DATE) {
                            n.set(field, property.getDate().getTime());
                        } else {
                            n.set(field, property.getString());
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot get property " + field + " on node " + node.getPath());
                }
            }
        }

        try {
            if (node.hasProperty("jcr:title")) {
                n.setDisplayName(node.getProperty("jcr:title").getString());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        n.setNormalizedName(removeDiacritics(n.getName()));

        // versions
        if (fields.contains(GWTJahiaNode.VERSIONS) && node.isVersioned()) {
            try {
                n.setCurrentVersion(node.getBaseVersion().getName());
                List<GWTJahiaNodeVersion> gwtJahiaNodeVersions = getVersions(node);
                if (gwtJahiaNodeVersions != null && gwtJahiaNodeVersions.size() > 0) {
                    n.setVersions(gwtJahiaNodeVersions);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        // references
        try {
            if (node.isNodeType("jmix:nodeReference") && node.hasProperty("j:node")) {
                n.setReferencedNode(getGWTJahiaNode((JCRNodeWrapper) node.getProperty("j:node").getNode()));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return n;
    }

    public void setIcon(JCRNodeWrapper f, GWTJahiaNode n) {
        try {
            String folder = getIconsFolder(f.getPrimaryNodeType());
            if (f.isFile()) {
                n.setIcon(folder + "jnt_file_" + FileUtils.getFileIcon(f.getName()));
            } else if (f.isPortlet()) {
                n.setPortlet(true);
                try {
                    JCRPortletNode portletNode = new JCRPortletNode(f);
                    if (portletNode.getContextName().equalsIgnoreCase("/rss")) {
                        n.setIcon(folder + "jnt_portlet_rss");
                    } else {
                        n.setIcon(folder + "jnt_portlet");
                    }
                } catch (RepositoryException e) {
                    n.setIcon(folder + "jnt_portlet");
                }
            } else if (f.isNodeType("jnt:page") && f.getParent().isNodeType("jnt:templatesFolder")) {
                n.setIcon(folder + "jnt_page_template");
            } else {
                final ExtendedNodeType type = f.getPrimaryNodeType();
                String icon = getIcon(type);
                n.setIcon(icon);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getIcon(ExtendedNodeType type) throws RepositoryException {
        String icon = getIconsFolder(type) + type.getName().replace(':', '_');
        if (check(icon)) {
            return icon;
        }
        for (ExtendedNodeType nodeType : type.getSupertypes()) {
            icon = getIconsFolder(nodeType) + nodeType.getName().replace(':', '_');
            if (check(icon)) {
                return icon;
            }
        }
        return null;
    }

    private Map<String, Boolean> iconsPresence = new HashMap();

    public boolean check(String icon) {
        try {
            if (!iconsPresence.containsKey(icon)) {
                iconsPresence.put(icon, Jahia.getStaticServletConfig().getServletContext().getResource("/templates/" + icon + ".png") != null);
            }
            return iconsPresence.get(icon);
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    public String getIconsFolder(final ExtendedNodeType primaryNodeType) throws RepositoryException {
        String folder = primaryNodeType.getSystemId();
        if (folder.startsWith("system-")) {
            folder = "default";
        } else {
            final JahiaTemplatesPackage aPackage =
                    ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(folder);
            if (aPackage != null) {
                folder = aPackage.getRootFolder();
            } else {
                folder = "default"; // todo handle portlets 
            }
        }
        folder += "/icons/";
        return folder;
    }

    /**
     * Get list of version as gwt bean list
     *
     * @param node
     * @return
     */
    public List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node) throws RepositoryException {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        VersionHistory vh = node.getVersionHistory();
        VersionIterator vi = vh.getAllVersions();
        while (vi.hasNext()) {
            Version v = vi.nextVersion();
            if (!v.getName().equals("jcr:rootVersion")) {
//                        JCRNodeWrapper orig = ((JCRVersionHistory) v.getContainingHistory()).getNode();
                GWTJahiaNode n = getGWTJahiaNode(node);
                n.setUrl(node.getUrl() + "?v=" + v.getName());
                GWTJahiaNodeVersion jahiaNodeVersion =
                        new GWTJahiaNodeVersion(v.getUUID(), v.getName(), v.getCreated().getTime(), null);
                jahiaNodeVersion.setNode(n);

                versions.add(jahiaNodeVersion);
            }
        }
        return versions;
    }

    /**
     * Get list of version that have been published as gwt bean list
     *
     * @param node
     * @return
     */
    public List<GWTJahiaNodeVersion> getPublishedVersions(JCRNodeWrapper node) throws RepositoryException {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        List<VersionInfo> versionInfos = jcrVersionService.getVersionInfos(node.getSession(), node);
        for (VersionInfo versionInfo : versionInfos) {
            Version v = versionInfo.getVersion();
            GWTJahiaNode n = getGWTJahiaNode(node);
            n.setUrl(node.getUrl() + "?v=" + versionInfo.getCheckinDate().getTime().getTime());
            GWTJahiaNodeVersion jahiaNodeVersion =
                    new GWTJahiaNodeVersion(v.getUUID(), v.getName(), v.getCreated().getTime(),
                            versionInfo.getCheckinDate().getTime());
            jahiaNodeVersion.setNode(n);

            versions.add(jahiaNodeVersion);
        }
        return versions;
    }

    public String removeDiacritics(String name) {
        if (name == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= '\u0080') {
                if (c >= '\u00C0' && c < '\u00C6') {
                    sb.append('A');
                } else if (c == '\u00C6') {
                    sb.append("AE");
                } else if (c == '\u00C7') {
                    sb.append('C');
                } else if (c >= '\u00C8' && c < '\u00CC') {
                    sb.append('E');
                } else if (c >= '\u00CC' && c < '\u00D0') {
                    sb.append('I');
                } else if (c == '\u00D0') {
                    sb.append('D');
                } else if (c == '\u00D1') {
                    sb.append('N');
                } else if (c >= '\u00D2' && c < '\u00D7') {
                    sb.append('O');
                } else if (c == '\u00D7') {
                    sb.append('x');
                } else if (c == '\u00D8') {
                    sb.append('O');
                } else if (c >= '\u00D9' && c < '\u00DD') {
                    sb.append('U');
                } else if (c == '\u00DD') {
                    sb.append('Y');
                } else if (c == '\u00DF') {
                    sb.append("SS");
                } else if (c >= '\u00E0' && c < '\u00E6') {
                    sb.append('a');
                } else if (c == '\u00E6') {
                    sb.append("ae");
                } else if (c == '\u00E7') {
                    sb.append('c');
                } else if (c >= '\u00E8' && c < '\u00EC') {
                    sb.append('e');
                } else if (c >= '\u00EC' && c < '\u00F0') {
                    sb.append('i');
                } else if (c == '\u00F0') {
                    sb.append('d');
                } else if (c == '\u00F1') {
                    sb.append('n');
                } else if (c >= '\u00F2' && c < '\u00FF') {
                    sb.append('o');
                } else if (c == '\u00F7') {
                    sb.append('/');
                } else if (c == '\u00F8') {
                    sb.append('o');
                } else if (c >= '\u00F9' && c < '\u00FF') {
                    sb.append('u');
                } else if (c == '\u00FD') {
                    sb.append('y');
                } else if (c == '\u00FF') {
                    sb.append("y");
                } else if (c == '\u0152') {
                    sb.append("OE");
                } else if (c == '\u0153') {
                    sb.append("oe");
                } else {
                    sb.append('_');
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

//    public Map<String, String> getNodetypeIcons() {
//        return nodetypeIcons;
//    }

    /**
     * Get children node as list: ToDo: remove this method and call directly getNodes() when nested for better performance
     *
     * @return
     */
    public List<JCRNodeWrapper> getNodesAsList(Node node) {
        try {
            if (node == null) {
                return null;
            }
            NodeIterator nodesIterator = node.getNodes();
            final List<JCRNodeWrapper> list = new ArrayList<JCRNodeWrapper>();
            while (nodesIterator.hasNext()) {
                list.add((JCRNodeWrapper) nodesIterator.next());
            }
            return list;
        } catch (RepositoryException e) {
            logger.error(e, e);
            return null;
        }
    }


}
