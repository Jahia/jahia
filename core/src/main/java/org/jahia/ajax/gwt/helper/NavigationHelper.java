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
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRMountPointNode;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.services.content.decorator.JCRVersionHistory;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.utils.FileUtils;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
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
    private RenderService renderService;
    private JCRSessionFactory sessionFactory;

    private PublicationHelper publication;

    public Map<String, String> nodetypeIcons;

    public void setJcrService(JCRStoreService jcrService) {
        this.jcrService = jcrService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setRenderService(RenderService renderService) {
        this.renderService = renderService;
    }

    public void setPublication(PublicationHelper publication) {
        this.publication = publication;
    }

    public void setNodetypeIcons(Map<String, String> nodetypeIcons) {
        this.nodetypeIcons = nodetypeIcons;
    }


    /**
     * Check if note must be expanded
     *
     * @param parentNode
     * @param openPaths
     * @return
     */
    private boolean expandOnLoad(GWTJahiaNode parentNode, List<String> openPaths) {
        // check if parentNode is expanded
        boolean expand = false;
        if (openPaths != null) {
            for (String openPath : openPaths) {
                if (openPath.indexOf(parentNode.getPath()) == 0) {
                    expand = true;

                    break;
                }
            }
        }
        return expand;
    }


    /**
     * like ls unix command on the folder
     *
     * @param folder
     * @param nodeTypes
     * @param mimeTypes
     * @param filters
     * @param noFolders
     * @param viewTemplateAreas
     * @param context
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, boolean noFolders, boolean viewTemplateAreas, ProcessingContext context) throws GWTJahiaServiceException {
        Locale locale = Jahia.getThreadParamBean().getCurrentLocale();


        String workspace = "default";
        JahiaUser user = context.getUser();
        JCRNodeWrapper node = null;
        try {
            node = sessionFactory.getCurrentUserSession(workspace, locale).getNode(folder != null ? folder.getPath() : "/");
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
        boolean displayTags = nodeTypes != null && nodeTypes.contains(JCRClientUtils.TAG_NODETYPES);
        for (JCRNodeWrapper f : list) {
            if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("processing ").append(f.getPath()).toString());
            }
            // in case of a folder, it allows to know if the node is selectable
            boolean matchNodeType = matchesNodeType(f, nodeTypesToApply);
            if (f.isVisible() && (matchNodeType || (!noFolders && f.isCollection()))) {
                // in case of a folder, it allows to know if the node is selectable
                boolean matchFilters = matchesMimeTypeFilters(f.isFile(),f.getFileContent().getContentType(), mimeTypesToMatch) && matchesFilters(f.getName(), filtersToApply);
                if (f.isCollection() || matchFilters) {
                    GWTJahiaNode theNode = getGWTJahiaNode(f, true);
                    theNode.setMatchFilters(matchNodeType && matchFilters);
                        try {
                            theNode.setPublicationInfo(publication.getPublicationInfo(f.getPath(), null, false));
                        } catch (GWTJahiaServiceException e) {
                            logger.error(e.getMessage(), e);
                        }
                    if (displayTags) {
                        try {
                            theNode.set("count", f.getWeakReferences().getSize());
                        } catch (RepositoryException e) {
                            logger.warn("Unable to count node references for node: " + f.getPath(), e);
                        }
                    }

                    result.add(theNode);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug(new StringBuilder(f.getPath()).append(" did not match the filters or is not a collection"));
                    }
                }
            }
        }

//        try {
//            if (!displayAllVirtualSites && viewTemplateAreas && node.isNodeType("jmix:renderable") && node.hasProperty("j:template")) {
//                Resource r = new Resource(node, "html", null, null);
//                RenderContext renderContext = new RenderContext(((ParamBean) context).getRequest(), ((ParamBean) context).getResponse(), user);
//                renderContext.setSite(context.getSite());
//                renderContext.setSiteNode(JCRSessionFactory.getInstance().getCurrentUserSession(workspace, locale).getNode("/sites/" + context.getSite().getSiteKey()));
//                renderContext.setIncludeSubModules(false);
//            renderContext.setMainResource(r);
//                renderService.render(r, renderContext);
//                List<String> l = r.getMissingResources();
//                if (l != null) {
//                    for (String s : l) {
//                        if (!node.hasNode(s)) {
//                            final GWTJahiaNode o = new GWTJahiaNode(null, s, "placeholder " + s, node.getProvider().decodeInternalName(node.getPath()) + "/" + s, null, null, null, null, null, null, node.isWriteable(), false, false, false, null, false);
//                            o.setExt("icon-placeholder");
//                            result.add(o);
//                        }
//                    }
//                }
//            }
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        } catch (RenderException e) {
//            logger.error(e.getMessage(), e);
//        }

        return result;
    }

    public String[] getFiltersToApply(String filter) {
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

    public boolean matchesFilters(String nodeName, String[] filters) {
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

    public boolean matchesMimeTypeFilters(boolean isFile, String mimeType, String[] filters) {
        // no filters
        if(filters == null || filters.length ==0 ){
            return true;
        }

        // there are filters, but not a file
        if (!isFile) {
            return false;
        }

        // do filter
        return matchesFilters(mimeType, filters);
    }

    public boolean matchesNodeType(Node node, String[] nodeTypes) {
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

    public List<GWTJahiaNode> retrieveRoot(String repositoryKey, ProcessingContext jParams, String nodeTypes, String mimeTypes, String filters, List<String> selectedNodes, List<String> openPaths,
                                           boolean forceCreate, ContentManagerHelper contentManager) throws GWTJahiaServiceException {
        List<GWTJahiaNode> userNodes = new ArrayList<GWTJahiaNode>();
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        logger.debug("open paths for getRoot : " + openPaths);

        String workspace = "default";
        String[] keys = repositoryKey.split(";");
        for (String key : keys) {
            if (key.startsWith("/")) {
                GWTJahiaNode root = getNode(key, workspace, jParams);
                if (root != null) {
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.MY_REPOSITORY)) {
                for (JCRNodeWrapper node : jcrService.getUserFolders(null, jParams.getUser())) {
                    GWTJahiaNode root = getNode(node.getPath() + "/files", workspace, jParams);
                    if (root != null) {
                        root.setDisplayName(jParams.getUser().getName());
                        userNodes.add(root);
                    }
                    break; // one node should be enough
                }
            } else if (key.equals(JCRClientUtils.USERS_REPOSITORY)) {
                try {
                    NodeIterator ni = sessionFactory.getCurrentUserSession(workspace, jParams.getLocale()).getNode(
                            "/users").getNodes();
                    while (ni.hasNext()) {
                        Node node = (Node) ni.next();
                        GWTJahiaNode jahiaNode = getGWTJahiaNode((JCRNodeWrapper) node.getNode("files"), true);
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
                GWTJahiaNode root = getNode("/mounts", workspace, jParams);
                if (root != null) {
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.SHARED_REPOSITORY)) {
                GWTJahiaNode root = getNode("/shared/files", workspace, jParams);
                if (root != null) {
                    root.setDisplayName("shared");
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.WEBSITE_REPOSITORY)) {
                GWTJahiaNode root = getNode("/sites/" + jParams.getSite().getSiteKey() + "/files", workspace,
                        jParams);
                if (root != null) {
                    root.setDisplayName(jParams.getSite().getSiteKey());
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.MY_MASHUP_REPOSITORY)) {
                for (JCRNodeWrapper node : jcrService.getUserFolders(null, jParams.getUser())) {
                    GWTJahiaNode root = getNode(node.getPath() + "/mashups", workspace, jParams);
                    if (root != null) {
                        root.setDisplayName(jParams.getUser().getName());
                        userNodes.add(root);
                    }
                    break; // one node should be enough
                }
            } else if (key.equals(JCRClientUtils.SHARED_MASHUP_REPOSITORY)) {
                GWTJahiaNode root = getNode("/shared/mashups", workspace, jParams);
                if (root != null) {
                    root.setDisplayName("shared");
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY)) {
                GWTJahiaNode root = getNode("/sites/" + jParams.getSite().getSiteKey() + "/mashups", workspace,
                        jParams);
                if (root != null) {
                    root.setDisplayName(jParams.getSite().getSiteKey());
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.ALL_FILES)) {
                addShareSiteUserFolder(jParams, userNodes, workspace, "/files");
                GWTJahiaNode root = getNode("/mounts", workspace, jParams);
                if (root != null) {
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.ALL_CONTENT)) {
                addShareSiteUserFolder(jParams, userNodes, workspace, "/contents");
            } else if (key.equals(JCRClientUtils.ALL_MASHUPS)) {
                addShareSiteUserFolder(jParams, userNodes, workspace, "/mashups");
            } else if (key.equals(JCRClientUtils.CATEGORY_REPOSITORY)) {
                GWTJahiaNode root = getNode("/categories", workspace, jParams);
                if (root != null) {
                    root.setDisplayName("categories");
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.TAG_REPOSITORY)) {
                GWTJahiaNode root = getNode("/sites/" + jParams.getSite().getSiteKey() + "/tags", workspace, jParams);
                if (root != null) {
                    root.setDisplayName("tags");
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.PORTLET_DEFINITIONS_REPOSITORY)) {
                GWTJahiaNode root = getNode("/portletdefinitions", workspace, jParams);
                if (root != null) {
                    root.setDisplayName("Portlet Definition");

                    userNodes.add(root);

                }

            } else if (key.equals(JCRClientUtils.SITE_REPOSITORY)) {
                GWTJahiaNode root = getNode("/sites", workspace, jParams);
                if (root != null) {
                    root.setDisplayName("sites");
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.GLOBAL_REPOSITORY)) {
                GWTJahiaNode root = getNode("/", workspace, jParams);
                if (root != null) {
                    userNodes.add(root);
                }
            } else if (key.equals(JCRClientUtils.REUSABLE_COMPONENTS_REPOSITORY)) {
                GWTJahiaNode root = null;
                final String s = nodeTypes.replaceAll(":", "_");
                try {
                    root = getNode("/reusableComponents/"+ s, workspace, jParams);
                } catch (GWTJahiaServiceException e) {
                    if(forceCreate) {
                        contentManager.createNode("/reusableComponents", s,"jnt:reusableComponentsFolder", null, new ArrayList<GWTJahiaNodeProperty>(), jParams);
                        root = getNode("/reusableComponents/"+ s, workspace, jParams);
                    }
                }
                if (root != null) {
                    root.setDisplayName("reusableComponents");
                    userNodes.add(root);
                }
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
                    for (int i = 0; i< allNodes.size(); i++) {
                        GWTJahiaNode node = allNodes.get(i);
                        if (openPath.startsWith(node.getPath()) && !node.isExpandOnLoad() && !node.isFile()) {
                            node.setExpandOnLoad(true);
                            List<GWTJahiaNode> list = ls(node, nodeTypes, mimeTypes, filters, true, false,jParams);
                            for (int j = 0; j< list.size(); j++) {
                                node.insert(list.get(j),j);
                                allNodes.add(list.get(j));
                            }
                        }
                        if (selectedNodes != null && selectedNodes.contains(node.getPath())) {
                            node.setSelectedOnLoad(true);
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return userNodes;
    }

    private void addShareSiteUserFolder(ProcessingContext jParams, List<GWTJahiaNode> userNodes, String workspace, String folderName) throws GWTJahiaServiceException {
        GWTJahiaNode root = null;
        try {
            root = getNode("/shared"+ folderName, workspace, jParams);
            root.setDisplayName("shared");
            userNodes.add(root);
        } catch (GWTJahiaServiceException e) {
            e.printStackTrace();
        }

        try {
            root = getNode("/sites/" + jParams.getSite().getSiteKey() + folderName, workspace, jParams);
            root.setDisplayName(jParams.getSite().getSiteKey());
            userNodes.add(root);
        } catch (GWTJahiaServiceException e) {
            e.printStackTrace();
        }

        for (JCRNodeWrapper node : jcrService.getUserFolders(null, jParams.getUser())) {
            try {
                root = getNode(node.getPath() + folderName, workspace, jParams);
                root.setDisplayName(jParams.getUser().getName());
                userNodes.add(root);
            } catch (GWTJahiaServiceException e) {
                e.printStackTrace();
            }
            break; // one node should be enough
        }
    }

    public List<GWTJahiaNode> getMountpoints(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [jnt:mountPoint]";
            Query q = sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Return a node if existing exception otherwise
     *
     * @param path      the path to test an dget the node if existing
     * @param workspace the workspace
     * @param jParams   the jparams
     * @return the existing node
     * @throws GWTJahiaServiceException it node does not exist
     */
    public GWTJahiaNode getNode(String path, String workspace, ProcessingContext jParams) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(sessionFactory.getCurrentUserSession(workspace, jParams.getLocale()).getNode(path), true);
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public GWTJahiaNode getParentNode(String path, String workspace, ProcessingContext jParams) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode((JCRNodeWrapper) sessionFactory.getCurrentUserSession(workspace, jParams.getLocale()).getNode(path).getParent(), true);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public String getDownloadPath(String path, JahiaUser user) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getCurrentUserSession().getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(e.toString());
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(user.getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getUrl();
    }

    public String getAbsolutePath(String path, ParamBean jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getCurrentUserSession().getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(jParams.getUser().getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getAbsoluteWebdavUrl(jParams);
    }

    public List<GWTJahiaNodeUsage> getUsages(String path) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getCurrentUserSession().getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        List<GWTJahiaNodeUsage> result = new ArrayList<GWTJahiaNodeUsage>();
        try {
            NodeIterator usages = node.getSharedSet();
            while (usages.hasNext()) {
                JCRNodeWrapper usage = (JCRNodeWrapper) usages.next();
                result.add(new GWTJahiaNodeUsage(usage.getIdentifier(), usage.getPath()));

            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
        }

        try {
            PropertyIterator references = node.getReferences();
            while (references.hasNext()) {
                JCRPropertyWrapper reference = (JCRPropertyWrapper) references.next();
                Node refNode = reference.getNode();
                result.add(new GWTJahiaNodeUsage(refNode.getIdentifier(),refNode.getPath()));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    public List<GWTJahiaNode> executeQuery(Query q, String[] nodeTypesToApply, String[] mimeTypesToMatch, String[] filtersToApply, ProcessingContext context) throws RepositoryException {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        QueryResult qr = q.execute();
        NodeIterator ni = qr.getNodes();
        List<String> foundPaths = new ArrayList<String>();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (matchesNodeType(n, nodeTypesToApply) && n.isVisible()) {
                // use for pickers 
                boolean matchFilter = (filtersToApply.length == 0 && mimeTypesToMatch.length == 0) || matchesFilters(n.getName(), filtersToApply) && matchesMimeTypeFilters(n.isFile(),n.getFileContent().getContentType(), mimeTypesToMatch);
                if (n.isCollection() || matchFilter) {
                    String path = n.getPath();
                    if (!foundPaths.contains(path)) { // TODO dirty filter, please correct search/index issue (sometimes duplicate results)
                        foundPaths.add(path);
                        GWTJahiaNode node = getGWTJahiaNode(n, true);
                        node.setMatchFilters(matchFilter);
                        result.add(node);
                    }
                }
            }
        }
        return result;
    }

    public GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper f, boolean versioned) {
        List<String> inherited = new ArrayList<String>();
        List<String> list = null;
        try {
            list = f.getNodeTypes();
            for (String s : list) {
                ExtendedNodeType[] inh = NodeTypeRegistry.getInstance().getNodeType(s).getSupertypes();
                for (ExtendedNodeType extendedNodeType : inh) {
                    if (!inherited.contains(extendedNodeType.getName())) {
                        inherited.add(extendedNodeType.getName());
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

        String aclContext = "sharedOnly";
        Node i = f;
        try {
            while (!i.isNode() || !i.isNodeType("jnt:virtualsite")) {
                i = i.getParent();
            }
            aclContext = "site:" + i.getName();
        } catch (RepositoryException e) {
        }

        if (f.isFile() || f.isPortlet()) {
            n = new GWTJahiaNode(uuid, f.getName(), description, f.getProvider().decodeInternalName(f.getPath()), f.getUrl(), f.getLastModifiedAsDate(), list, inherited, aclContext, f.getProvider().getKey(), f.getFileContent().getContentLength(), f.isWriteable(), f.isWriteable(), f.isLockable(), f.isLocked(), f.getLockOwner(), f.isVersioned());
        } else {
            n = new GWTJahiaNode(uuid, f.getName(), description, f.getProvider().decodeInternalName(f.getPath()), f.getUrl(), f.getLastModifiedAsDate(), list, inherited, aclContext, f.getProvider().getKey(), f.isWriteable(), f.isWriteable(), f.isLockable(), f.isLocked(), f.getLockOwner(), f.isVersioned());

            n.setCollection(f.isCollection());

            if (sessionFactory.getMountPoints().containsKey(f.getPath())) {
                n.setDeleteable(false);
            }

            boolean hasChildren = false;
            boolean hasFolderChildren = false;
            if (f instanceof JCRMountPointNode) {
                hasChildren = true;
                hasFolderChildren = true;
            } else {
                // TODO consider current node types, mime types and filters when checking for children
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
        n.setLastModifiedBy(f.getModificationUser());
        n.setCreated(f.getCreationDateAsDate());
        n.setCreatedBy(f.getCreationUser());
        n.setLastPublished(f.getLastPublishedAsDate());
        n.setLastPublishedBy(f.getPublicationUser());


        try {
            if (f.hasProperty("j:tags")) {
                StringBuilder b = new StringBuilder();
                Value[] values = f.getProperty("j:tags").getValues();
                for (Value value : values) {
                    Node node = ((JCRValueWrapper) value).getNode();
                    if (node != null) {
                        b.append(", ");
                        b.append(node.getName());
                    }
                }
                if (b.length()>0) {
                    n.setTags(b.substring(2));
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error when getting tags", e);
        }

        setIcon(f, n);

        List<String> names = f.getThumbnails();
        if (names.contains("thumbnail")) {
            n.setPreview(f.getThumbnailUrl("thumbnail"));
            n.setDisplayable(true);
        } else {
            StringBuilder buffer = new StringBuilder();
            ProcessingContext pBean = Jahia.getThreadParamBean();
            buffer.append(pBean.getScheme()).append("://").append(pBean.getServerName()).append(":").append(pBean.getServerPort()).append(Jahia.getContextPath());
            buffer.append("/engines/images/types/gwt/large/");
            buffer.append(n.getExt()).append(".png");
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
            // add node template
            if (f.hasProperty("j:template")) {
                n.setTemplate(f.getProperty("j:template").getValue().getString());
            }

            if(f.hasProperty("jcr:title")) {
                n.setDisplayName(f.getProperty("jcr:title").getString());
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        n.setNormalizedName(removeDiacritics(n.getName()));
        if (versioned) {
            List<GWTJahiaNodeVersion> gwtJahiaNodeVersions = getVersions(f, null);
            if (gwtJahiaNodeVersions != null && gwtJahiaNodeVersions.size() > 0) {
                n.setVersions(gwtJahiaNodeVersions);
            }
        }
        try {
            if (f.isNodeType("jnt:nodeReference") && f.hasProperty("j:node")) {
                n.setReferencedNode(getGWTJahiaNode((JCRNodeWrapper) f.getProperty("j:node").getNode(), versioned));
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return n;
    }

    public void setIcon(JCRNodeWrapper f, GWTJahiaNode n) {
        try {
            if (f.isFile()) {
                n.setExt(new StringBuilder("icon-").append(FileUtils.getFileIcon(f.getName())).toString());
            } else if (f.isPortlet()) {
                n.setPortlet(true);
                try {
                    JCRPortletNode portletNode = new JCRPortletNode(f);
                    if (portletNode.getContextName().equalsIgnoreCase("/rss")) {
                        n.setExt("icon-rss");
                    } else {
                        n.setExt("icon-portlet");
                    }
                } catch (RepositoryException e) {
                    n.setExt("icon-portlet");
                }
            } else {
                Map<String, String> map = getNodetypeIcons();
                for (String nt : map.keySet()) {
                    if (f.isNodeType(nt)) {
                        n.setExt("icon-" + map.get(nt));
                        break;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Get list of version as gwt bean list
     *
     * @param node
     * @param jParams
     * @return
     */
    public List<GWTJahiaNodeVersion> getVersions(JCRNodeWrapper node, ProcessingContext jParams) {
        List<GWTJahiaNodeVersion> versions = new ArrayList<GWTJahiaNodeVersion>();
        try {
            if (node.isVersioned()) {
                VersionHistory vh = node.getVersionHistory();
                VersionIterator vi = vh.getAllVersions();
                while (vi.hasNext()) {
                    Version v = vi.nextVersion();
                    if (!v.getName().equals("jcr:rootVersion")) {
                        JCRNodeWrapper orig = ((JCRVersionHistory) v.getContainingHistory()).getNode();
                        GWTJahiaNode n = getGWTJahiaNode(orig, false);
                        n.setUrl(orig.getUrl() + "?v=" + v.getName());
                        GWTJahiaNodeVersion jahiaNodeVersion = new GWTJahiaNodeVersion(v.getUUID(), v.getName(), v.getCreated().getTime());
                        jahiaNodeVersion.setNode(n);
                        versions.add(jahiaNodeVersion);
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e, e);
        }
        return versions;
    }

    public String removeDiacritics(String name) {
        if (name == null) return null;
        StringBuffer sb = new StringBuffer(name.length());
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= '\u0080') {
                if (c >= '\u00C0' && c < '\u00C6') sb.append('A');
                else if (c == '\u00C6') sb.append("AE");
                else if (c == '\u00C7') sb.append('C');
                else if (c >= '\u00C8' && c < '\u00CC') sb.append('E');
                else if (c >= '\u00CC' && c < '\u00D0') sb.append('I');
                else if (c == '\u00D0') sb.append('D');
                else if (c == '\u00D1') sb.append('N');
                else if (c >= '\u00D2' && c < '\u00D7') sb.append('O');
                else if (c == '\u00D7') sb.append('x');
                else if (c == '\u00D8') sb.append('O');
                else if (c >= '\u00D9' && c < '\u00DD') sb.append('U');
                else if (c == '\u00DD') sb.append('Y');
                else if (c == '\u00DF') sb.append("SS");
                else if (c >= '\u00E0' && c < '\u00E6') sb.append('a');
                else if (c == '\u00E6') sb.append("ae");
                else if (c == '\u00E7') sb.append('c');
                else if (c >= '\u00E8' && c < '\u00EC') sb.append('e');
                else if (c >= '\u00EC' && c < '\u00F0') sb.append('i');
                else if (c == '\u00F0') sb.append('d');
                else if (c == '\u00F1') sb.append('n');
                else if (c >= '\u00F2' && c < '\u00FF') sb.append('o');
                else if (c == '\u00F7') sb.append('/');
                else if (c == '\u00F8') sb.append('o');
                else if (c >= '\u00F9' && c < '\u00FF') sb.append('u');
                else if (c == '\u00FD') sb.append('y');
                else if (c == '\u00FF') sb.append("y");
                else if (c == '\u0152') sb.append("OE");
                else if (c == '\u0153') sb.append("oe");
                else sb.append('_');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public Map<String, String> getNodetypeIcons() {
        return nodetypeIcons;
    }


}
