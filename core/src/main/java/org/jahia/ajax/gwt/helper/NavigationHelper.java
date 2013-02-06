/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.queryParser.ParseException;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeVersion;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRQueryNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.*;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:16:27 PM
 */
public class NavigationHelper {
    private static Logger logger = LoggerFactory.getLogger(NavigationHelper.class);

    public final static String SAVED_OPEN_PATHS = "org.jahia.contentmanager.savedopenpaths.";
    public final static String SELECTED_PATH = "org.jahia.contentmanager.selectedpath.";

    private Set<String> ignoreInUsages = Collections.emptySet();
    
    private NodeHelper nodeHelper;


    /**
     * like ls unix command on the folder
     *
     *
     * @param gwtParentNode
     * @param nodeTypes
     * @param mimeTypes
     * @param nameFilters
     * @param fields
     * @param currentUserSession @return
     * @param uiLocale
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> ls(GWTJahiaNode gwtParentNode, List<String> nodeTypes, List<String> mimeTypes,
                                 List<String> nameFilters, List<String> fields, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        return ls(gwtParentNode, nodeTypes, mimeTypes, nameFilters, fields, false, false, null, null, currentUserSession,
                false, uiLocale);
    }

    public List<GWTJahiaNode> ls(GWTJahiaNode gwtParentNode, List<String> nodeTypes, List<String> mimeTypes,
                                 List<String> nameFilters, List<String> fields, boolean checkSubChild,
                                 boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex,
                                 JCRSessionWrapper currentUserSession, boolean showOnlyNodesWithTemplates, Locale uiLocale) throws GWTJahiaServiceException {
        JCRNodeWrapper node = null;
        try {
            node = currentUserSession.getNode(gwtParentNode != null ? gwtParentNode.getPath() : "/");
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
        }

        if (node == null) {
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.parent.node.is.null",uiLocale));
        }
        final List<GWTJahiaNode> gwtNodeChildren = new ArrayList<GWTJahiaNode>();
        try {
            getMatchingChilds(nodeTypes, mimeTypes, nameFilters, fields, node, gwtNodeChildren, checkSubChild,
                    displayHiddenTypes, hiddenTypes, hiddenRegex, showOnlyNodesWithTemplates, uiLocale);

            return gwtNodeChildren;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    private void getMatchingChilds(List<String> nodeTypes, List<String> mimeTypes, List<String> nameFilters,
                                   List<String> fields, JCRNodeWrapper node, List<GWTJahiaNode> gwtNodeChildren,
                                   boolean checkSubChild, boolean displayHiddenTypes, List<String> hiddenTypes,
                                   String hiddenRegex, boolean showOnlyNodesWithTemplates, Locale uiLocale) throws RepositoryException, GWTJahiaServiceException {
        final NodeIterator nodesIterator;
        if(node instanceof JCRQueryNode) {
            final Query q = node.getSession().getWorkspace().getQueryManager().getQuery(node.getRealNode());
            List<GWTJahiaNode> gwtJahiaNodes = executeQuery(q, nodeTypes, mimeTypes, nameFilters, fields, null, showOnlyNodesWithTemplates);
            gwtNodeChildren.addAll(gwtJahiaNodes);
            return;
        } else {
            nodesIterator = node.getNodes();
        }

        boolean hasOrderableChildren = node.getPrimaryNodeType().hasOrderableChildNodes();

        if (nodesIterator == null) {
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.children.list.is.null", uiLocale));
        }

        int i = 1;
        while (nodesIterator.hasNext()) {
            JCRNodeWrapper childNode = (JCRNodeWrapper) nodesIterator.nextNode();
            if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("processing ").append(childNode.getPath()).toString());
            }
            if(showOnlyNodesWithTemplates && !JCRContentUtils.isADisplayableNode(childNode, new RenderContext(null, null, node.getSession().getUser()))){
                continue;
            }
            // in case of a folder, it allows to know if the node is selectable
            boolean hiddenNode = false;
            if (!displayHiddenTypes) {
                if (hiddenTypes != null) {
                    for (String type : hiddenTypes) {
                        if (childNode.getNodeTypes().contains(type)) {
                            hiddenNode = true;
                        }
                    }
                }
                if (hiddenRegex != null && childNode.getName().matches(hiddenRegex)) {
                    hiddenNode = true;
                }
                if (childNode.getNodeTypes().contains(Constants.JAHIAMIX_HIDDEN_NODE)) {
                    hiddenNode = true;
                }
            }
            boolean matchVisibilityFilter = !hiddenNode;
            boolean matchNodeType = matchesNodeType(childNode, nodeTypes);
            if (logger.isDebugEnabled()) {
                logger.debug("----------");
                if(nodeTypes != null) {
                	for (String s : nodeTypes) {
                		logger.debug(
                            "Node " + childNode.getPath() + " match with " + s + "? " + childNode.isNodeType(s) + "[" +
                                    matchNodeType + "]");
                	}
                }
                logger.debug("----------"); 
            }
            boolean mimeTypeFilter = matchesMimeTypeFilters(childNode, mimeTypes);
            boolean nameFilter = matchesFilters(childNode.getName(), nameFilters);
            boolean hasNodes = false;
            if (!mimeTypeFilter) { // we do not need to check for sub-nodes if the mimeTypeFilter is already true
                try {
                    hasNodes = childNode.getNodes().hasNext();
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            // collection condition is available only if the parent node is not a nt:query. Else, the node has to match the node type condition
            if (matchVisibilityFilter && matchNodeType && (mimeTypeFilter || hasNodes) && nameFilter) {
                GWTJahiaNode gwtChildNode = getGWTJahiaNode(childNode, fields);
                gwtChildNode.setMatchFilters(matchNodeType && mimeTypeFilter);
                if (hasOrderableChildren) {
                    gwtChildNode.set("index", new Integer(i++));
                }
                NodeIterator ni = childNode.getNodes();
                gwtChildNode.setHasChildren(false);
                while (ni.hasNext()) {
                    Node n = ni.nextNode();
                    if (matchesNodeType(n, nodeTypes) && matchesFilters(n.getName(), nameFilters)) {
                        gwtChildNode.setHasChildren(true);
                        break;
                    }
                }
                gwtNodeChildren.add(gwtChildNode);
            } else if (checkSubChild && childNode.hasNodes()) {
                getMatchingChilds(nodeTypes, mimeTypes, nameFilters, fields, childNode, gwtNodeChildren, checkSubChild,
                        displayHiddenTypes, hiddenTypes, hiddenRegex, showOnlyNodesWithTemplates, uiLocale);
            }
        }
    }

    protected static boolean matchesFilters(String nodeName, List<String> filters) {
        if (CollectionUtils.isEmpty(filters)) {
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

    protected static boolean matchesMimeTypeFilters(JCRNodeWrapper node, List<String> filters) {
        // no filters
        if (CollectionUtils.isEmpty(filters)) {
            return true;
        }

        // there are filters, but not a file
        if (!node.isFile()) {
            return true;
        } else {
            return matchesFilters(node.getFileContent().getContentType(), filters);
        }
    }

    protected static boolean matchesNodeType(Node node, List<String> nodeTypes) {
        if (CollectionUtils.isEmpty(nodeTypes)) {
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

    public List<GWTJahiaNode> retrieveRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes,
                                           List<String> filters, List<String> fields, List<String> selectedNodes,
                                           List<String> openPaths, JCRSiteNode site,
                                           JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        return retrieveRoot(paths, nodeTypes, mimeTypes, filters, fields, selectedNodes, openPaths, site,
                currentUserSession, uiLocale, false, false, null, null);
    }

    public List<GWTJahiaNode> retrieveRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes,
                                           List<String> filters, List<String> fields, List<String> selectedNodes,
                                           List<String> openPaths, final JCRSiteNode site,
                                           JCRSessionWrapper currentUserSession, Locale uiLocale, boolean checkSubChild, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex)
            throws GWTJahiaServiceException {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("open paths for getRoot : " + openPaths);
            }

            final List<GWTJahiaNode> userNodes = retrieveRoot(paths, nodeTypes, mimeTypes, filters, fields, site, uiLocale, currentUserSession, checkSubChild, displayHiddenTypes, hiddenTypes, hiddenRegex);
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
                            if (!node.isExpandOnLoad()) {
                                boolean matchPath;
                                if (openPath.endsWith("*")) {
                                    String p = StringUtils.substringBeforeLast(openPath, "*");
                                    matchPath = node.getPath().startsWith(p) || p.startsWith(node.getPath());
                                } else {
                                    matchPath = openPath.startsWith(node.getPath());
                                }
                                if (matchPath) {
                                    node.setExpandOnLoad(true);
                                    List<GWTJahiaNode> list = ls(node, nodeTypes, mimeTypes, filters, fields, checkSubChild,
                                            displayHiddenTypes, hiddenTypes, hiddenRegex, currentUserSession, false, uiLocale);
                                    for (int j = 0; j < list.size(); j++) {
                                        node.insert(list.get(j), j);
                                        allNodes.add(list.get(j));
                                    }
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
            for (GWTJahiaNode userNode : userNodes) {
                userNode.set("isRootNode", Boolean.TRUE);
            }
            return userNodes;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public List<GWTJahiaNode> retrieveRoot(List<String> paths, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, List<String> fields, final JCRSiteNode site, Locale uiLocale, JCRSessionWrapper currentUserSession, boolean checkSubChild, boolean displayHiddenTypes, List<String> hiddenTypes, String hiddenRegex) throws RepositoryException, GWTJahiaServiceException {
        final List<GWTJahiaNode> userNodes = new ArrayList<GWTJahiaNode>();

        for (String path : paths) {
            // replace $user and $site by the right values
            String displayName = "";
            if (site != null && path.contains("$site/") || path.endsWith("$site")) {
                path = path.replace("$site", site.getPath());
//                if (!path.endsWith("*")) {
//                    displayName = site.getSiteKey();
//                }
            }
            if (path.contains("$siteKey/")) {
                path = path.replace("$siteKey", site.getSiteKey());
            }
            if (path.contains("$moduleversion/") || path.endsWith("$moduleversion")) {
                String moduleName = path.split("/")[2];
                path = path.replace("$moduleversion", ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(moduleName).getVersion().toString());
            }
            if (path.contains("$systemsite/") || path.endsWith("$systemsite")) {
                String systemSiteKey = JCRContentUtils.getSystemSitePath();
                path = path.replace("$systemsite", systemSiteKey);
//                try {
//                    displayName = site.getSession().getNode(path).getDisplayableName();
//                } catch (PathNotFoundException e) {
//                    displayName = StringUtils.substringAfterLast(systemSiteKey, "/");
//                }
            }
            if (site != null && path.contains("$sites")) {
                JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                    public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                        final JCRNodeWrapper parent = site.getParent();
                        NodeIterator nodes = parent.getNodes();
                        while (nodes.hasNext()) {
                            JCRNodeWrapper nodeWrapper = (JCRNodeWrapper) nodes.next();
                            userNodes.add(getGWTJahiaNode(nodeWrapper));
                        }
                        return null;
                    }
                });
            }
            if (path.contains("$user")) {
                path = path.replace("$user", currentUserSession.getUser().getLocalPath());
                displayName = Messages.getInternal("label.personalFolder", uiLocale, "label.personalFolder");
            }
            if (path.startsWith("/")) {
                if (path.endsWith("/*")) {
//                    NodeIterator ni =
//                            currentUserSession.getNode(StringUtils.substringBeforeLast(path, "/*")).getNodes();
                    try {
                        getMatchingChilds(nodeTypes, mimeTypes, filters, fields, currentUserSession.getNode(StringUtils.substringBeforeLast(path, "/*")), userNodes ,checkSubChild, displayHiddenTypes, hiddenTypes,hiddenRegex,false, uiLocale);
                    } catch (PathNotFoundException e) {
                        // do nothing is the path is not found
                    }
//                    while (ni.hasNext()) {
//                        GWTJahiaNode node = getGWTJahiaNode((JCRNodeWrapper) ni.next(), fields);
//                        if (displayName != "") {
//                            node.setDisplayName(JCRContentUtils.unescapeLocalNodeName(displayName));
//                        }
//                        userNodes.add(node);
//                    }
                } else {
                    GWTJahiaNode root = getNode(path, fields, currentUserSession, uiLocale);
                    if (root != null) {
                        if (!StringUtils.isEmpty(displayName)) {
                            root.setDisplayName(JCRContentUtils.unescapeLocalNodeName(displayName));
                        }
                        userNodes.add(root);
                    }
                }
            }
        }
        return userNodes;
    }

    public List<GWTJahiaNode> getMountpoints(JCRSessionWrapper currentUserSession) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [jnt:mountPoint]";
            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return executeQuery(q, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Return a node if existing exception otherwise
     *
     *
     * @param path               the path to test and get the node if it exists
     * @param fields list of additional fields to be loaded for the node
     * @param currentUserSession
     * @param uiLocale
     * @return the existing node
     * @throws GWTJahiaServiceException it node does not exist
     */
    public GWTJahiaNode getNode(String path, List<String> fields, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(currentUserSession.getNode(path), fields, uiLocale);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
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

    /**
     * Return a node if existing exception otherwise
     *
     *
     * @param path               the path to test and get the node if it exists
     * @param fields list of additional fields to be loaded for the node
     * @param currentUserSession
     * @param uiLocale
     * @return the existing node
     * @throws GWTJahiaServiceException it node does not exist
     */
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

    public String getAbsolutePath(String path, JCRSessionWrapper currentUserSession, final HttpServletRequest request, Locale uiLocale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
        }
        return node.getAbsoluteWebdavUrl(request);
    }

    public List<GWTJahiaNodeUsage> getUsages(List<String> paths, JCRSessionWrapper currentUserSession, Locale uiLocale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        List<GWTJahiaNodeUsage> result = new ArrayList<GWTJahiaNodeUsage>();
        for (String path : paths) {
            try {
                node = currentUserSession.getNode(path);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(
                        new StringBuilder(path).append(Messages.getInternal("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
            }
            try {
                NodeIterator usages = node.getSharedSet();
                while (usages.hasNext()) {
                    JCRNodeWrapper usage = (JCRNodeWrapper) usages.next();
                    if (!usage.getPath().equals(node.getPath()) && !usage.getPath().startsWith("/"+Constants.JCR_SYSTEM)) {
                        addUsage(result, usage, "shared");
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
            }

            try {
                fillUsages(result, node.getWeakReferences());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return result;
    }

    private void fillUsages(List<GWTJahiaNodeUsage> result, PropertyIterator r) throws RepositoryException {
        while (r.hasNext()) {
            JCRPropertyWrapper reference = (JCRPropertyWrapper) r.next();
            if (!reference.getPath().startsWith("/referencesKeeper")) {
                JCRNodeWrapper refNode = reference.getParent();
                if (!refNode.getPath().startsWith("/" + Constants.JCR_SYSTEM)) {
                    if (!ignoreInUsages.isEmpty()) {
                        String nt = refNode.getPrimaryNodeTypeName() + ".";
                        if (ignoreInUsages.contains(nt + "*")
                                || ignoreInUsages.contains(nt + reference.getName())) {
                            continue;
                        }
                    }
                    addUsage(result, refNode, "reference");
                }
            }
        }
    }

    private void addUsage(List<GWTJahiaNodeUsage> result, JCRNodeWrapper refNode, String type)
            throws RepositoryException {
        JCRNodeWrapper parent = refNode.isNodeType("jnt:page") ? refNode : lookUpParentPageNode(refNode);
        String language = null;
        if (refNode.isNodeType(Constants.JAHIANT_TRANSLATION)) {
            language = refNode.getProperty("jcr:language").getString();
            refNode = refNode.getParent();
        }
        GWTJahiaNodeUsage usage = new GWTJahiaNodeUsage(refNode.getIdentifier(), refNode.getPath());
        usage.setNodeName(refNode.getName());
        usage.setPagePath(parent != null ? parent.getPath() : refNode.getPath());
        usage.setNodeTitle(refNode.hasProperty("jcr:title") ? refNode.getPropertyAsString("jcr:title") : "");
        if (parent != null) {
            usage.setPageTitle(parent.getPropertyAsString("jcr:title"));
        } else {
            String title = refNode.getPropertyAsString("jcr:title");
            if (title == null) {
                title = refNode.getName();
            }
            title += " (" + refNode.getPrimaryNodeType().getName() + ")";
            usage.setPageTitle(title);
        }

        usage.setLanguage(language);
        usage.setType(type);
        result.add(usage);
    }

    /**
     * Get nodes by category
     *
     * @param path
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> getNodesByCategory(String path, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        final List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();

        try {
            PropertyIterator references = node.getWeakReferences();
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
                        result.add(getGWTJahiaNode(currentNode, Arrays.asList(GWTJahiaNode.ICON, GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.NAME)));
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


    public List<GWTJahiaNode> executeQuery(Query q, List<String> nodeTypesToApply, List<String> mimeTypesToMatch,
                                           List<String> filtersToApply, List<String> sites) throws RepositoryException {
        return executeQuery(q, nodeTypesToApply, mimeTypesToMatch, filtersToApply, GWTJahiaNode.DEFAULT_FIELDS, sites, false);
    }

    public List<GWTJahiaNode> executeQuery(Query q, List<String> nodeTypesToApply, List<String> mimeTypesToMatch,
                                           List<String> filtersToApply, List<String> fields, List<String> sites, boolean showOnlyNodesWithTemplates)
            throws RepositoryException {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        Set<String> addedIds = new HashSet<String>();
        NodeIterator ni = null;
        try {
            ni = q.execute().getNodes();
        } catch (RepositoryException e) {
            if (e.getMessage() != null && e.getMessage().contains(ParseException.class.getName())) {
                logger.warn(e.getMessage());
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage(), e);
                }
            } else {
                throw e;
            }
        }

        while (ni != null && ni.hasNext()) {
            try {
                JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
                if (n.isNodeType(Constants.JAHIANT_TRANSLATION)
                        || Constants.JCR_CONTENT.equals(n.getName())) {
                    n = n.getParent();
                }
                if (!n.isNodeType(Constants.NT_FROZENNODE) &&
                        (CollectionUtils.isEmpty(nodeTypesToApply) || matchesNodeType(n, nodeTypesToApply))) {
                    // use for pickers
                    boolean hasNodes = false;
                    try {
                        hasNodes = n.getNodes().hasNext();
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                    }
                    boolean matchFilter =
                            matchesFilters(n.getName(), filtersToApply) && matchesMimeTypeFilters(n, mimeTypesToMatch);
                    if(showOnlyNodesWithTemplates && !JCRContentUtils.isADisplayableNode(n, new RenderContext(null, null, n.getSession().getUser()))){
                        continue;
                    }
                    boolean siteCheck = (sites == null || sites.contains(n.getResolveSite().getSiteKey()));

                    if (siteCheck && (matchFilter || hasNodes) && addedIds.add(n.getIdentifier())) {
                        GWTJahiaNode node = getGWTJahiaNode(n, fields);
                        node.setMatchFilters(matchFilter);
                        result.add(node);
                    }
                }
            } catch (Exception e) {
                logger.warn("Error resolving search hit", e);
            }
        }
        return result;
    }

    public GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node) {
        return nodeHelper.getGWTJahiaNode(node);
    }

    public GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node, List<String> fields) {
<<<<<<< .working
        return nodeHelper.getGWTJahiaNode(node, fields, null);
=======
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
        n.setName(JCRContentUtils.unescapeLocalNodeName(node.getName()));
        try {
            n.setCanMarkForDeletion(node.canMarkForDeletion());
        } catch (RepositoryException e) {
            logger.error("Unable to check if the node " + node.getPath()
                    + " supports marking for deletion. Cause: " + e.getMessage(), e);
        }
        try {
            n.set("everPublished", Boolean.valueOf(node.hasProperty("j:published")));
        } catch (RepositoryException e) {
            logger.warn(
                    "Unable to check existence of the j:published property on node "
                            + node.getPath() + ". Cause: " + e.getMessage(), e);
        }
        try {
            if (node.getPath().equals("/")) {
                n.setDisplayName("root");
                n.setName("root");
            } else {
                n.setDisplayName(WordUtils.abbreviate(JCRContentUtils.unescapeLocalNodeName(node.getDisplayableName()),70,90,"..."));
            }
        } catch (Exception e) {
            logger.error("Error when getting name", e);
        }
        n.setNormalizedName(removeDiacritics(n.getName()));
        
        n.setDescription(description);
        n.setPath(node.getPath());
        n.setUrl(node.getUrl());
        n.setNodeTypes(nodeTypes);
        n.setInheritedNodeTypes(inheritedTypes);
        n.setProviderKey(node.getProvider().getKey());

        if (fields.contains(GWTJahiaNode.PERMISSIONS)) {
        	try{
        		if(node.getParent().isNodeType(Constants.JAHIAMIX_AUTOSPLITFOLDERS)) { 
        			//reload the node when it is splittype that all pathes are correct, to load the permissios
        	        node = JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID(node.getIdentifier());
        		}
        	}catch(Exception ex) {
        		logger.warn("reload of node " + node.getName() + " on path " + node.getPath() + " failed", ex);
        	}
            BitSet bs = node.getPermissionsAsBitSet();
            GWTBitSet gwtBs = new GWTBitSet(bs.size());

            for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1)) {
                gwtBs.set(i);
            }

            n.setPermissions(gwtBs);

            try {
                boolean hasAcl = node.hasNode("j:acl") && node.getNode("j:acl").hasNodes();
                n.setHasAcl(hasAcl);
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (fields.contains(GWTJahiaNode.LOCKS_INFO)) {
            n.setLockable(node.isLockable());
            try {
                String username = node.getSession().getUser().getUsername();
                n.setLocked(JCRContentUtils.isLockedAndCannotBeEdited(node));
                Map<String, List<String>> infos = node.getLockInfos();
                Map<String, List<String>> results = new HashMap<String, List<String>>(infos.size());
                if (!infos.isEmpty()) {
                    for (Map.Entry<String, List<String>> entry : infos.entrySet()) {
                        for (String s : entry.getValue()) {
                            JCRNodeLockType type = JCRContentUtils.getLockType(s);
                            if (!results.containsKey(entry.getKey())) {
                                results.put(entry.getKey(), new LinkedList<String>());
                            }
                            results.get(entry.getKey())
                                    .add(JCRNodeLockType.USER.equals(type)
                                            && StringUtils.isNotBlank(s) ? StringUtils
                                            .substringBefore(s, ":") : ("label.locked.by." + type
                                            .toString().toLowerCase()));
                        }
                    }
                }
                n.setLockInfos(results);
                if (node.getSession().getLocale() != null) {
                    String l = node.getSession().getLocale().toString();
                    n.setCanLock(infos.isEmpty() || !infos.containsKey(l));
                    n.setCanUnlock(infos.containsKey(null) && infos.get(null).contains(username+":user") && (infos.size() == 1 || infos.containsKey(l) && infos.get(l).contains(username+":user")));
                } else {
                    n.setCanLock(infos.isEmpty());
                    n.setCanUnlock(infos.containsKey(null) && infos.get(null).contains(username+":user"));
                }
            } catch (RepositoryException e) {
                logger.error("Error when getting lock", e);
            }
        }

        if (fields.contains(GWTJahiaNode.VISIBILITY_INFO)) {
            Map<JCRNodeWrapper, Boolean> conditionMatchesDetails = visibilityService.getConditionMatchesDetails(node);
            Map<GWTJahiaNode,ModelData> visibilityInfo = new HashMap<GWTJahiaNode, ModelData>();
            for (Map.Entry<JCRNodeWrapper, Boolean> entry : conditionMatchesDetails.entrySet()) {
                ModelData data = new BaseModelData();
                data.set("matches",entry.getValue());
                VisibilityConditionRule visibilityConditionRule = null;
                try {
                    visibilityConditionRule = visibilityService.getConditions().get(
                            entry.getKey().getPrimaryNodeTypeName());
                    data.set("xtemplate", visibilityConditionRule.getGWTDisplayTemplate(node.getSession().getLocale()));
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
                if(visibilityConditionRule!=null) {
                    visibilityInfo.put(getGWTJahiaNode(entry.getKey(),visibilityConditionRule.getRequiredFieldNamesForTemplate()), data);
                }
            }
            n.setVisibilityInfo(visibilityInfo);
            n.setVisible(visibilityService.matchesConditions(node));
        }

        n.setThumbnailsMap(new HashMap<String, String>());
        n.setVersioned(node.isVersioned());
        n.setLanguageCode(node.getLanguage());
        try {
            JCRSiteNode site = node.getResolveSite();
            if (site != null) {
                n.setSiteUUID(site.getUUID());
                n.setAclContext("site:" + site.getName());
                n.setSiteKey(site.getSiteKey());
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
            n.setReference(node.isNodeType("jmix:nodeReference"));
        } catch (RepositoryException e1) {
            logger.error("Error checking node type", e1);
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
                    logger.error(e.getMessage(), e);
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

        if (node.isPortlet()) {
            n.setPortlet(true);
        }

        // icons
        if (fields.contains(GWTJahiaNode.ICON)) {
            try {
                n.setIcon(JCRContentUtils.getIcon(node));
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
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
                n.set("count", JCRContentUtils.size(node.getWeakReferences()));
            } catch (RepositoryException e) {
                logger.warn("Unable to count node references for node");
            }
        }

        boolean supportsPublication = false;
        try {
            supportsPublication = node.getSession().getProviderSession(node.getProvider()).getRepository().getDescriptorValue(Repository.OPTION_WORKSPACE_MANAGEMENT_SUPPORTED).getBoolean();
            n.set("supportsPublication", Boolean.valueOf(supportsPublication));
        } catch (Exception e) {
            logger.error("Cannot get repository infos",e);
        }
        try {
            if (fields.contains(GWTJahiaNode.PUBLICATION_INFO) && supportsPublication && node.getSession().getLocale() != null) {
                n.setAggregatedPublicationInfos(publication.getAggregatedPublicationInfosByLanguage(node,
                        Collections.singleton(node.getSession().getLocale().toString()), node.getSession()));
            }
        } catch (UnsupportedRepositoryOperationException e) {
            // do nothing
            logger.debug(e.getMessage());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (GWTJahiaServiceException e) {
            logger.error(e.getMessage(), e);
        }

        if (fields.contains(GWTJahiaNode.PUBLICATION_INFOS) && supportsPublication) {
            try {
                JCRSiteNode siteNode = node.getResolveSite();
                if (siteNode != null) {
                    JCRSessionWrapper session = node.getSession();

                    n.setAggregatedPublicationInfos(publication.getAggregatedPublicationInfosByLanguage(node,
                            siteNode.getLanguages(), session));
                    n.setFullPublicationInfos(publication.getFullPublicationInfosByLanguage(Arrays.asList(node.getIdentifier()), siteNode.getLanguages(),
                                        session, false));
                }
            } catch (UnsupportedRepositoryOperationException e) {
                // do nothing
                logger.debug(e.getMessage());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (GWTJahiaServiceException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (fields.contains(GWTJahiaNode.WORKFLOW_INFO) || fields.contains(GWTJahiaNode.PUBLICATION_INFO)) {
            try {
                n.setWorkflowInfo(
                        workflow.getWorkflowInfo(n.getPath(), node.getSession(), node.getSession().getLocale()));
            } catch (UnsupportedRepositoryOperationException e) {
//                 do nothing
                logger.debug(e.getMessage());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (GWTJahiaServiceException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (fields.contains(GWTJahiaNode.WORKFLOW_INFOS)) {
            try {
                JCRSiteNode node1 = node.getResolveSite();
                if (node1 != null) {
                    Map<String, GWTJahiaWorkflowInfo> infoMap = new HashMap<String, GWTJahiaWorkflowInfo>();
                    JCRSessionWrapper session = node.getSession();
                    for (String code : node1.getLanguages()) {
                        Locale locale = LanguageCodeConverters.languageCodeToLocale(code);
                        JCRSessionWrapper localeSession =
                                sessionFactory.getCurrentUserSession(session.getWorkspace().getName(), locale);
                        GWTJahiaWorkflowInfo info = workflow.getWorkflowInfo(n.getPath(), localeSession, locale);
                        infoMap.put(code, info);
                    }
                    n.setWorkflowInfos(infoMap);
                }
            } catch (UnsupportedRepositoryOperationException e) {
//                 do nothing
                logger.debug(e.getMessage());
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            } catch (GWTJahiaServiceException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (fields.contains(GWTJahiaNode.AVAILABLE_WORKKFLOWS)) {
            try {
                if (node.hasProperty(GWTJahiaNode.AVAILABLE_WORKKFLOWS)) {
                    final JCRPropertyWrapper property = node.getProperty(GWTJahiaNode.AVAILABLE_WORKKFLOWS);
                    Value[] values = null;
                    if (property.isMultiple()) {
                        values = property.getValues();
                    } else {
                        values = new Value[]{property.getValue()};
                    }
                    List<String> vals = new LinkedList<String>();
                    if (values != null) {
                        for (Value value : values) {
                            if (value != null) {
                                vals.add(value.getString());
                            }
                        }
                    }
                    n.set(GWTJahiaNode.AVAILABLE_WORKKFLOWS, StringUtils.join(vals, ", "));
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node " + node.getPath());
            }
        }

        if (fields.contains(GWTJahiaNode.PRIMARY_TYPE_LABEL)) {
            try {
                n.set(GWTJahiaNode.PRIMARY_TYPE_LABEL, node.getPrimaryNodeType().getLabel(node.getSession().getLocale()));
            } catch (RepositoryException e) {
                logger.error("Cannot get property " + GWTJahiaNode.PRIMARY_TYPE_LABEL + " on node " + node.getPath());
            }
        }

        if (n.isFile() && nodeTypes.contains("jmix:image")) {
            fields = new LinkedList<String>(fields);
            if (!fields.contains("j:height")) {
                fields.add("j:height");
            }
            if (!fields.contains("j:width")) {
                fields.add("j:width");
            }
        }

        if (fields.contains(GWTJahiaNode.SITE_LANGUAGES)) {
            try {
                n.set(GWTJahiaNode.SITE_LANGUAGES, languages.getLanguages(node.getResolveSite(), node.getSession().getUser(), node.getSession().getLocale()));
            } catch (RepositoryException e) {
                logger.error("Cannot get sites languages");
            }
        }

        if (fields.contains(GWTJahiaNode.SUBNODES_CONSTRAINTS_INFO)) {
            // reference types
            try {
                String cons = ConstraintsHelper.getConstraints(node);
                if (cons != null) {
                    n.set("referenceTypes", ConstraintsHelper.getReferenceTypes(cons, null));
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node " + node.getPath());
            }
        }

        if (fields.contains(GWTJahiaNode.DEFAULT_LANGUAGE)) {
            try {
                if (node.hasProperty(GWTJahiaNode.DEFAULT_LANGUAGE)) {
                    Locale locale = LanguageCodeConverters.languageCodeToLocale(node.getProperty(GWTJahiaNode.DEFAULT_LANGUAGE).getString());
                    n.set(GWTJahiaNode.DEFAULT_LANGUAGE, languages.getCurrentLang(locale));
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node " + node.getPath());
            }
        }

        if (fields.contains(GWTJahiaNode.HOMEPAGE_PATH) && (node instanceof JCRSiteNode)) {
            try {
                if (((JCRSiteNode) node).getHome() != null) {
                 n.set(GWTJahiaNode.HOMEPAGE_PATH, ((JCRSiteNode) node).getHome().getPath());
                }
            } catch (RepositoryException e) {
                logger.error("Cannot get property " + GWTJahiaNode.AVAILABLE_WORKKFLOWS + " on node " + node.getPath());
            }
        }

        // properties
        for (String field : fields) {
            if (!GWTJahiaNode.RESERVED_FIELDS.contains(field)) {
                try {
                    if (node.hasProperty(field)) {
//                        n.set(StringUtils.substringAfter(propName, ":"), node.getProperty(propName).getString());
                        final JCRPropertyWrapper property = node.getProperty(field);
                        if (property.isMultiple()) {
                            Value[] values = property.getValues();
                            List<Object> l = new ArrayList<Object>();
                            for (Value value : values) {
                                l.add(getPropertyValue(value, node.getSession()));
                            }
                            n.set(field, l);
                        } else {
                            Value value = property.getValue();
                            n.set(field, getPropertyValue(value, node.getSession()));
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error("Cannot get property " + field + " on node " + node.getPath());
                }
            }
        }

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
                JCRNodeWrapper referencedNode = (JCRNodeWrapper) node.getProperty("j:node").getNode();
                n.setReferencedNode(n.getUUID().equals(referencedNode.getIdentifier()) ? n : getGWTJahiaNode(referencedNode));
            }
        } catch (ItemNotFoundException e) {
            logger.debug(e.getMessage(), e);
        }
        catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        // sort
        try {
            if (node.getPrimaryNodeType().hasOrderableChildNodes()) {
                n.set("hasOrderableChildNodes", Boolean.TRUE);
                n.setSortField("index");
            } else {
                n.setSortField(GWTJahiaNode.NAME);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        // constraints
        try {
            n.setChildConstraints(ConstraintsHelper.getConstraints(node));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        
        // WCAG checks
        try {
            n.setWCAGComplianceCheckEnabled(node.getResolveSite().hasProperty(
                    SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED)
                    && node.getResolveSite()
                            .getProperty(SitesSettings.WCAG_COMPLIANCE_CHECKING_ENABLED)
                            .getBoolean());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            if (fields.contains("j:versionInfo") && node.hasNode("j:versionInfo")) {
                n.set("j:versionInfo", node.getNode("j:versionInfo").getProperty("j:version").getString());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return n;
>>>>>>> .merge-right.r44642
    }

    public GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper node, List<String> fields, Locale uiLocale) {
        return nodeHelper.getGWTJahiaNode(node, fields, uiLocale);
    }

    /**
     * Get list of version as GWT bean list
     *
     * @param node
     * @return
     */
    public List<GWTJahiaNodeVersion> getVersions(final JCRNodeWrapper node) throws RepositoryException {
        return getVersions(node, false);
    }

    /**
     * Get list of version that have been published as gwt bean list
     *
     * @param node
     * @return
     */
    public List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node, boolean publishedOnly) throws RepositoryException {
        return nodeHelper.getVersions(node, publishedOnly);
    }

    /**
     * Get node url depending
     *
     *
     * @param workspace
     * @param locale
     */
    public String getNodeURL(String servlet, JCRNodeWrapper node, Date versionDate, String versionLabel, final String workspace,
                             final Locale locale) throws RepositoryException {
        return NodeHelper.getNodeURL(servlet, node, versionDate, versionLabel, workspace, locale);
    }

    public void setIgnoreInUsages(Set<String> ignoreInUsages) {
    	this.ignoreInUsages = ignoreInUsages;
    }

    public void setNodeHelper(NodeHelper nodeHelper) {
        this.nodeHelper = nodeHelper;
    }

}
