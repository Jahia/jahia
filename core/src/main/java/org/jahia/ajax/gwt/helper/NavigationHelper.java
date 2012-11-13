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
import org.jahia.utils.i18n.JahiaResourceBundle;
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
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.parent.node.is.null",uiLocale));
        }
        if (node.isFile()) {
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.can.t.list.the.children.of.a.file",uiLocale));
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
            throw new GWTJahiaServiceException(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.children.list.is.null", uiLocale));
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
        }

        // do filter
        return matchesFilters(node.getFileContent().getContentType(), filters);
    }

    protected static boolean matchesNodeType(JCRNodeWrapper node, List<String> nodeTypes) {
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
                            if (!node.isExpandOnLoad() && !node.isFile()) {
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
                path = path.replace("$moduleversion", ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(site.getSiteKey()).getVersion().toString());
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
                displayName = JahiaResourceBundle
                        .getJahiaInternalResource("label.personalFolder", uiLocale, "label.personalFolder");
            }
            if (path.startsWith("/")) {
                if (path.endsWith("/*")) {
//                    NodeIterator ni =
//                            currentUserSession.getNode(StringUtils.substringBeforeLast(path, "/*")).getNodes();
                    getMatchingChilds(nodeTypes, mimeTypes, filters, fields, currentUserSession.getNode(StringUtils.substringBeforeLast(path, "/*")), userNodes ,checkSubChild, displayHiddenTypes, hiddenTypes,hiddenRegex,false, uiLocale);
//                    while (ni.hasNext()) {
//                        GWTJahiaNode node = getGWTJahiaNode((JCRNodeWrapper) ni.next(), fields);
//                        if (displayName != "") {
//                            node.setDisplayName(JCRContentUtils.unescapeLocalNodeName(displayName));
//                        }
//                        userNodes.add(node);
//                    }
                } else {
                    GWTJahiaNode root = getNode(path, fields, uiLocale, currentUserSession);
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
            return getGWTJahiaNode(currentUserSession.getNode(path), fields);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
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

    public GWTJahiaNode getNode(String path, List<String> fields, Locale uiLocale, JCRSessionWrapper currentUserSession)
            throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(currentUserSession.getNode(path), fields, uiLocale);
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

    public String getAbsolutePath(String path, JCRSessionWrapper currentUserSession, final HttpServletRequest request, Locale uiLocale)
            throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = currentUserSession.getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(
                    new StringBuilder(path).append(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
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
                        new StringBuilder(path).append(JahiaResourceBundle.getJahiaInternalResource("label.gwt.error.could.not.be.accessed", uiLocale)).append(e.toString()).toString());
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
        return nodeHelper.getGWTJahiaNode(node, fields, null);
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
