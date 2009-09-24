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
package org.jahia.ajax.gwt.content.server.helper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.StringValue;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.aclmanagement.server.ACLHelper;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.*;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.ajax.gwt.definitions.server.ContentDefinitionHelper;
import org.jahia.ajax.gwt.utils.JahiaGWTUtils;
import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.PortletEntryPointDefinition;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.captcha.CaptchaService;
import org.jahia.services.categories.Category;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.SelectorType;
import org.jahia.services.content.nodetypes.initializers.Templates;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.XMLFormatDetectionHandler;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.UsageEntry;
import org.jahia.utils.FileUtils;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.*;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeDefinition;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.version.Version;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 20 juin 2008 - 12:49:42
 */
public class ContentManagerHelper {

    public final static String SAVED_OPEN_PATHS = "org.jahia.preferences.filemanager.savedopenpaths.";
    public final static String OPEN_PATHS_SEPARATOR = "-_-";

    private static JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
    private static JCRSessionFactory sessionFactory = jcr.getSessionFactory();
    private static JCRPublicationService publicationService = JCRPublicationService.getInstance();
    private static Logger logger = Logger.getLogger(ContentManagerHelper.class);
    private static final int UPLOAD_NEW_VERSION = 3;
    private static final int UPLOAD_AUTO_RENAME = 1;
    private static final int UPLOAD = 0;

    public static String[] splitOpenPathList(String concatPaths) {
        return concatPaths.split(OPEN_PATHS_SEPARATOR);
    }

    public static String concatOpenPathsList(List<String> paths) {
        if (paths == null || paths.size() == UPLOAD) {
            return null;
        }
        StringBuilder b = new StringBuilder(paths.get(UPLOAD));
        for (int i = UPLOAD_AUTO_RENAME; i < paths.size(); i++) {
            b.append(OPEN_PATHS_SEPARATOR).append(paths.get(i));
        }
        return b.toString();
    }

    /*
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
/*    private static void appendChildren(GWTJahiaNode node, String[] openPaths, ProcessingContext context, String nodeTypes, String mimeTypes, String filters, boolean noFolders) throws GWTJahiaServiceException {
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
    }*/

    public static List<GWTJahiaNode> ls(GWTJahiaNode folder, String nodeTypes, String mimeTypes, String filters, String openPaths, boolean noFolders, boolean viewTemplateAreas, ProcessingContext context) throws GWTJahiaServiceException {
        Locale locale = Jahia.getThreadParamBean().getCurrentLocale();

        String workspace = "default";
        JahiaUser user = context.getUser();
        JCRNodeWrapper node = null;
        try {
            node = sessionFactory.getThreadSession(user, workspace, locale).getNode(folder != null ? folder.getPath() : "/");
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
        boolean displayAllVirtualSites = nodeTypes != null && nodeTypes.contains(Constants.JAHIANT_VIRTUALSITE);
        for (JCRNodeWrapper f : list) {
            if (logger.isDebugEnabled()) {
                logger.debug(new StringBuilder("processing ").append(f.getPath()).toString());
            }
            if (f.isVisible() && (matchesNodeType(f, nodeTypesToApply) || (!noFolders && f.isCollection()))) {
                try {
                    // if we are not in the site manager and the current site does not match the virtual site node --> hide it
                	if (!displayAllVirtualSites && f.isNodeType(Constants.JAHIANT_VIRTUALSITE) && !f.getName().equals(context.getSiteKey())) {
                    	continue;
                    }
                } catch (RepositoryException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("cannot get site name " + f.getPath());
                    }
                }
                if (f.isCollection() || (matchesFilters(f.getFileContent().getContentType(), mimeTypesToMatch) && matchesFilters(f.getName(), filtersToApply))) {
                    GWTJahiaNode theNode = getGWTJahiaNode(f,true);
                    if (displayAllVirtualSites) {
                        try {
                            theNode.setPublicationInfo(getPublicationInfo(f.getPath(), null, false, context.getUser()));
                        } catch (GWTJahiaServiceException e) {
                			logger.error(e.getMessage(), e);
                		}
                    }
//                    if (openPaths != null && openPaths.length() > 0) {
//                        logger.debug("trying to append children");
//                        appendChildren(theNode, splitOpenPathList(openPaths), context, nodeTypes, mimeTypes, filters, noFolders);
//                    }
                    result.add(theNode);
                } else {
                	if (logger.isDebugEnabled()) {
                		logger.debug(new StringBuilder(f.getPath()).append(" did not match the filters or is not a collection"));
                	}
                }
            }
        }

        try {
            if (!displayAllVirtualSites && viewTemplateAreas && node.isNodeType("jmix:renderable") && node.hasProperty("j:defaultTemplate")) {
                Resource r = new Resource(node, "html", null, null);
                RenderContext renderContext = new RenderContext(((ParamBean) context).getRequest(), ((ParamBean) context).getResponse(), user);
                renderContext.setSite(context.getSite());
                renderContext.setIncludeSubModules(false);
                RenderService.getInstance().render(r, renderContext);
                List<String> l = r.getMissingResources();
                if (l != null) {
                    for (String s : l) {
                        if (!node.hasNode(s)) {
                            final GWTJahiaNode o = new GWTJahiaNode(null, s, "placeholder " + s, node.getProvider().decodeInternalName(node.getPath()) + "/" + s, null, null, null, null, null, null, node.isWriteable(), false, false, false, null, false);
                            o.setExt("icon-placeholder");
                            result.add(o);
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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
        for (int i = UPLOAD; i < filtersToApply.length; i++) {
            filtersToApply[i] = StringUtils.trimToNull(filtersToApply[i]);
        }

        return filtersToApply;
    }

    private static boolean matchesFilters(String nodeName, String[] filters) {
        if (nodeName == null || filters.length == UPLOAD) {
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
        if (nodeTypes.length == UPLOAD) {
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

        String workspace = "default";

        if (key.startsWith("/")) {
            GWTJahiaNode root = getNode(key, workspace, jParams);
            if (root != null) userNodes.add(root);
        } else if (key.equals(JCRClientUtils.MY_REPOSITORY)) {
            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                GWTJahiaNode root = getNode(node.getPath() + "/files", workspace, jParams);
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }
        } else if (key.equals(JCRClientUtils.USERS_REPOSITORY)) {
            try {
                NodeIterator ni = sessionFactory.getThreadSession(jParams.getUser(), workspace, jParams.getLocale()).getNode("/content/users").getNodes();
                while (ni.hasNext()) {
                    Node node = (Node) ni.next();
                    GWTJahiaNode jahiaNode = getGWTJahiaNode((JCRNodeWrapper) node.getNode("files"),true);
                    jahiaNode.setDisplayName(node.getName());
                    userNodes.add(jahiaNode);
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            Collections.sort(userNodes, new Comparator<GWTJahiaNode>() {
                public int compare(GWTJahiaNode o1, GWTJahiaNode o2) {
                    return o1.getDisplayName().compareTo(o2.getDisplayName());
                }
            });
        } else if (key.equals(JCRClientUtils.MY_EXTERNAL_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/mounts", workspace, jParams);
            if (root != null) {
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.SHARED_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/shared/files", workspace, jParams);
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.WEBSITE_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/files", workspace, jParams);
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.MY_MASHUP_REPOSITORY)) {
            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                GWTJahiaNode root = getNode(node.getPath() + "/mashups", workspace, jParams);
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }
        } else if (key.equals(JCRClientUtils.SHARED_MASHUP_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/shared/mashups", workspace, jParams);
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.WEBSITE_MASHUP_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/mashups", workspace, jParams);
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.ALL_FILES)) {
            GWTJahiaNode root = getNode("/content/shared/files", workspace, jParams);
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }

            root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/files", workspace, jParams);
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }

            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                root = getNode(node.getPath() + "/files", workspace, jParams);
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }

            root = getNode("/content/mounts", workspace, jParams);
            if (root != null) userNodes.add(root);
        } else if (key.equals(JCRClientUtils.ALL_MASHUPS)) {
            GWTJahiaNode root = getNode("/content/shared/mashups", workspace, jParams);
            if (root != null) {
                root.setDisplayName("shared");
                userNodes.add(root);
            }
            root = getNode("/content/sites/" + jParams.getSite().getSiteKey() + "/mashups", workspace, jParams);
            if (root != null) {
                root.setDisplayName(jParams.getSite().getSiteKey());
                userNodes.add(root);
            }
            for (JCRNodeWrapper node : jcr.getUserFolders(null, jParams.getUser())) {
                root = getNode(node.getPath() + "/mashups", workspace, jParams);
                if (root != null) {
                    root.setDisplayName(jParams.getUser().getName());
                    userNodes.add(root);
                }
                break; // one node should be enough
            }
        } else if (key.equals(JCRClientUtils.CATEGORY_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/categories", workspace, jParams);
            if (root != null) {
                root.setDisplayName("categories");
                userNodes.add(root);
            }

        } else if (key.equals(JCRClientUtils.PORTLET_DEFINITIONS_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/portletdefinitions", workspace, jParams);
            if (root != null) {
                root.setDisplayName("Portlet Definition");

                userNodes.add(root);

            }

        } else if (key.equals(JCRClientUtils.SITE_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/sites", workspace, jParams);
            if (root != null) {
                root.setDisplayName("sites");
                userNodes.add(root);
            }
        } else if (key.equals(JCRClientUtils.GLOBAL_REPOSITORY)) {
            GWTJahiaNode root = getNode("/content/", workspace, jParams);
            if (root != null) {
                userNodes.add(root);
            }
        }

//        for (GWTJahiaNode userNode : userNodes) {
//            if (openPaths != null && openPaths.length() > 0) {
//                appendChildren(userNode, splitOpenPathList(openPaths), jParams, nodeTypes, mimeTypes, filters, false);
//            }
//        }

        return userNodes;
    }

    public static List<GWTJahiaNode> search(String searchString, int limit, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(JahiaGWTUtils.formatQuery(searchString), context);
            return executeQuery(q, new String[UPLOAD], new String[UPLOAD], new String[UPLOAD], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    public static List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, ProcessingContext context) throws GWTJahiaServiceException {
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        String[] nodeTypesToApply = getFiltersToApply(nodeTypes);
        String[] mimeTypesToMatch = getFiltersToApply(mimeTypes);
        String[] filtersToApply = getFiltersToApply(filters);
        try {
            Query q = createQuery(JahiaGWTUtils.formatQuery(searchString), context);
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
        List<String> foundPaths = new ArrayList<String>();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (matchesNodeType(n, nodeTypesToApply) && n.isVisible()) {
                if ((filtersToApply.length == UPLOAD && mimeTypesToMatch.length == UPLOAD)
                        || n.isCollection()
                        || (matchesFilters(n.getName(), filtersToApply) && matchesFilters(n.getFileContent().getContentType(), mimeTypesToMatch))) {
                    String path = n.getPath();
                    if (!foundPaths.contains(path)) { // TODO dirty filter, please correct search/index issue (sometimes duplicate results)
                        foundPaths.add(path);
                        result.add(getGWTJahiaNode(n,true));
                    }
                }
            }
        }
        return result;
    }

    public static GWTJahiaNode saveSearch(String searchString, String name, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String workspace = "default";
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
            boolean createdSearchFolder = false;
            if (!user.hasNode("savedSearch")) {
                queryStore = user.createCollection("savedSearch");
                createdSearchFolder = true;
            } else {
                queryStore = sessionFactory.getThreadSession(context.getUser(), workspace, context.getLocale()).getNode(user.getPath() + "/savedSearch");
            }
            String path = queryStore.getPath() + "/" + name;
            if (checkExistence(path, context.getUser())) {
                throw new ExistingFileException("The node " + path + " alreadey exists.");
            }
            q.storeAsNode(path);
            if (createdSearchFolder) {
                user.save();
            } else {
                queryStore.save();
            }
            return getGWTJahiaNode(sessionFactory.getThreadSession(context.getUser(), workspace, context.getLocale()).getNode(path),true);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("Could not store query");
        }
    }

    public static GWTJahiaNode saveSearch(String searchString, String path, String name, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String workspace = "default";
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }

            JCRNodeWrapper parent = sessionFactory.getThreadSession(context.getUser(), workspace).getNode(path);
            name = findAvailableName(parent, name, context.getUser());
            Query q = createQuery(searchString, context);
            q.storeAsNode(path + "/" + name);
            parent.saveSession();

            return getGWTJahiaNode(sessionFactory.getThreadSession(context.getUser(), workspace, context.getLocale()).getNode(path+"/"+name),true);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("Could not store query");
        }
    }


    private static Query createQuery(String searchString, ProcessingContext context) throws RepositoryException {
        String s = "select * from [jmix:hierarchyNode] as h where contains(h.[j:nodename]," + JCRContentUtils.stringToJCRSearchExp(searchString) + ")";
        return jcr.getQueryManager(context.getUser()).createQuery(s, Query.JCR_SQL2);
    }

    public static List<GWTJahiaNode> getSavedSearch(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [nt:query]";
            Query q = jcr.getQueryManager(context.getUser()).createQuery(s, Query.JCR_SQL2);
            return executeQuery(q, new String[UPLOAD], new String[UPLOAD], new String[UPLOAD], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public static List<GWTJahiaNode> getMountpoints(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [jnt:mountPoint]";
            Query q = jcr.getQueryManager(context.getUser()).createQuery(s, Query.JCR_SQL2);
            return executeQuery(q, new String[UPLOAD], new String[UPLOAD], new String[UPLOAD], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    public static GWTJahiaNode getNode(String path, String workspace, ProcessingContext jParams) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode(sessionFactory.getThreadSession(jParams.getUser(), workspace, jParams.getLocale()).getNode(path),true);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public static GWTJahiaNode getParentNode(String path, String workspace, ProcessingContext jParams) throws GWTJahiaServiceException {
        try {
            return getGWTJahiaNode((JCRNodeWrapper) sessionFactory.getThreadSession(jParams.getUser(), workspace, jParams.getLocale()).getNode(path).getParent(),true);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
    }

    public static GWTJahiaNode getGWTJahiaNode(JCRNodeWrapper f,boolean versioned) {
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
            n = new GWTJahiaNode(uuid, f.getName(), description, f.getProvider().decodeInternalName(f.getPath()), f.getUrl(), f.getLastModifiedAsDate(), f.getNodeTypes(), inherited, aclContext, f.getProvider().getKey(), f.getFileContent().getContentLength(), f.isWriteable(), f.isWriteable(), f.isLockable(), f.isLocked(), f.getLockOwner(), f.isVersioned());
        } else {
            n = new GWTJahiaNode(uuid, f.getName(), description, f.getProvider().decodeInternalName(f.getPath()), f.getUrl(), f.getLastModifiedAsDate(), list, inherited, aclContext, f.getProvider().getKey(), f.isWriteable(), f.isWriteable(), f.isLockable(), f.isLocked(), f.getLockOwner(), f.isVersioned());

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
             if (f.hasProperty("j:defaultTemplate")) {
                n.setTemplate(f.getProperty("j:defaultTemplate").getValue().getString());   
             }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        n.setNormalizedName(JCRStoreService.removeDiacritics(n.getName()));
        if (versioned) {
            List<GWTJahiaNodeVersion> gwtJahiaNodeVersions = JCRVersioningHelper.getVersions(f, null);
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

    private static void setIcon(JCRNodeWrapper f, GWTJahiaNode n) {
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
                Map<String,String> map = ContentDefinitionHelper.getNodetypeIcons();
                for (String nt : map.keySet()) {
                    if (f.isNodeType(nt)) {
                        n.setExt("icon-"+map.get(nt));
                        break;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static void setLock(List<String> paths, boolean locked, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper node;
            try {
                node = sessionFactory.getThreadSession(user).getNode(path);
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
        if (missedPaths.size() > UPLOAD) {
            StringBuilder errors = new StringBuilder("The following files could not be ").append(locked ? "locked:" : "unlocked:");
            for (String missedPath : missedPaths) {
                errors.append("\n").append(missedPath);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static boolean checkExistence(String path, JahiaUser user) throws GWTJahiaServiceException {
        try {
            return jcr.checkExistence(path, user) != null;
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException("Error:\n" + e.toString());
        }
    }

    public static void createFolder(String parentPath, String name, ProcessingContext context) throws GWTJahiaServiceException {
        createNode(parentPath, name, "jnt:folder", new ArrayList<GWTJahiaNodeProperty>(), context);
    }

    private static boolean getRecursedLocksAndFileUsages(JCRNodeWrapper nodeToDelete, List<String> lockedNodes, String username) {
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

    public static void deletePaths(List<String> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (String path : paths) {
            JCRNodeWrapper nodeToDelete;
            try {
                nodeToDelete = sessionFactory.getThreadSession(user).getNode(path);
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
                } catch (ReferentialIntegrityException e) {
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath())
                            .append(" - is in use").toString());
                } catch (RepositoryException e) {
                    logger.error("error", e);
                    missedPaths.add(new StringBuilder(nodeToDelete.getPath()).append(" - UNSUPPORTED").toString());
                }
            }
        }
        if (missedPaths.size() > UPLOAD) {
            StringBuilder errors = new StringBuilder("The following files could not be deleted:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static String getDownloadPath(String path, JahiaUser user) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getThreadSession(user).getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(e.toString());
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(user.getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getUrl();
    }

    public static String getAbsolutePath(String path, ParamBean jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getThreadSession(jParams.getUser()).getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        if (!node.hasPermission(JCRNodeWrapper.READ)) {
            throw new GWTJahiaServiceException(new StringBuilder("User ").append(jParams.getUser().getUsername()).append(" has no read access to ").append(node.getName()).toString());
        }
        return node.getAbsoluteWebdavUrl(jParams);
    }

    public static void copy(List<GWTJahiaNode> paths, JahiaUser user) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();
        for (GWTJahiaNode aNode : paths) {
            JCRNodeWrapper node;
            try {
                node = sessionFactory.getThreadSession(user).getNode(aNode.getPath());
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(aNode.getDisplayName()).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (!node.hasPermission(JCRNodeWrapper.READ)) {
                missedPaths.add(new StringBuilder("User ").append(user.getUsername()).append(" has no read access to ").append(node.getName()).toString());
            }
        }
        if (missedPaths.size() > UPLOAD) {
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
            JCRNodeWrapper node;
            try {
                node = sessionFactory.getThreadSession(user).getNode(aNode.getPath());
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(aNode.getDisplayName()).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (!node.hasPermission(JCRNodeWrapper.WRITE)) {
                missedPaths.add(new StringBuilder("User ").append(user.getUsername()).append(" has no write access to ").append(node.getName()).toString());
            } else if (node.isLocked() && !node.getLockOwner().equals(user.getUsername())) {
                missedPaths.add(new StringBuilder(node.getName()).append(" is locked by ").append(user.getUsername()).toString());
            }
        }
        if (missedPaths.size() > UPLOAD) {
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
            try {
                final JCRSessionWrapper session = sessionFactory.getThreadSession(user);
                JCRNodeWrapper node = session.getNode(aNode.getPath());

                String name = node.getName();
                if (node.hasPermission(JCRNodeWrapper.READ)) {
                    JCRNodeWrapper dest = session.getNode(destinationPath);
                    if (dest.isCollection()) {
                        String destPath = dest.getPath();
                        name = findAvailableName(dest, name, user);
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
            } catch (RepositoryException e) {
                missedPaths.add(new StringBuilder("File cannot be read").toString());
            }
        }
        if (missedPaths.size() > UPLOAD) {
            StringBuilder errors = new StringBuilder("The following files could not be pasted:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    private static String findAvailableName(JCRNodeWrapper dest, String name, JahiaUser user) {
        int i = UPLOAD_AUTO_RENAME;

        String basename = name;
        int dot = basename.lastIndexOf('.');
        String ext = "";
        if (dot > UPLOAD) {
            ext = basename.substring(dot);
            basename = basename.substring(UPLOAD, dot);
        }
        int und = basename.lastIndexOf('_');
        if (und > -UPLOAD_AUTO_RENAME && basename.substring(und + UPLOAD_AUTO_RENAME).matches("[0-9]+")) {
            basename = basename.substring(UPLOAD, und);
        }

        JCRNodeWrapper target = null;
        do {
            try {
                target = sessionFactory.getThreadSession(user).getNode(dest.getPath() + "/" + name);
                name = basename + "_" + (i++) + ext;
            } catch (RepositoryException e) {
                break;
            }

        } while (true);

        return name;
    }

    public static void pasteReference(GWTJahiaNode aNode, String destinationPath, String name, JahiaUser user) throws GWTJahiaServiceException {
        pasteReferenceOnTopOf(aNode, destinationPath, name, user, false);
    }


    public static void pasteReferences(final List<GWTJahiaNode> pathsToCopy, String destinationPath, JahiaUser user) throws GWTJahiaServiceException {
        pasteReferencesOnTopOf(pathsToCopy, destinationPath, user, false);
    }

    private static void doPasteReference(JCRNodeWrapper dest, JCRNodeWrapper node, String name) throws RepositoryException, JahiaException {
        /*Property p = */
        if (dest.getPrimaryNodeTypeName().equals("jnt:members")) {
            if(node.getPrimaryNodeTypeName().equals("jnt:user")){
                Node member = dest.addNode(name, Constants.JAHIANT_MEMBER);
                member.setProperty("j:member", node.getUUID());
            } else if(node.getPrimaryNodeTypeName().equals("jnt:group")){
                Node node1 = node.getParent().getParent();
                int id = 0;
                if(node1!=null && node1.getPrimaryNodeType().getName().equals(Constants.JAHIANT_VIRTUALSITE)) {
                    id = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(node1.getName()).getID();
                }
                Node member = dest.addNode(name+"___"+ id, Constants.JAHIANT_MEMBER);
                member.setProperty("j:member", node.getUUID());
            }
        } else {
            Node reference = dest.addNode(name, "jnt:nodeReference");
            if (node.isNodeType("jnt:nodeReference")) {
                node = (JCRNodeWrapper) node.getProperty("j:node").getNode();
            }
            reference.setProperty("j:node", node.getUUID());
        }
    }

    public static void rename(String path, String newName, JahiaUser user) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getThreadSession(user).getNode(path);
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

    public static Map<String, GWTJahiaNodeProperty> getProperties(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper objectNode;
        try {
            objectNode = sessionFactory.getThreadSession(jParams.getUser(), null, jParams.getLocale()).getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>();
        String propName = "null";
        try {
            PropertyIterator it = objectNode.getProperties();
            while (it.hasNext()) {
                Property prop = it.nextProperty();
                PropertyDefinition def = prop.getDefinition();
                // definition can be null if the file is versionned
                if (def != null) {
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

                    for (Value val : values) {
                        gwtValues.add(Utils.convertValue(val,def.getRequiredType()));
                    }
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                } else {
                    logger.debug("The following property has been ignored "+prop.getName()+","+prop.getPath());
                }
            }
            NodeIterator ni = objectNode.getNodes();
            while (ni.hasNext()) {
                Node node = ni.nextNode();
                if (node.isNodeType(Constants.NT_RESOURCE)) {
                    NodeDefinition def = node.getDefinition();
                    propName = def.getName();
                    // create the corresponding GWT bean
                    GWTJahiaNodeProperty nodeProp = new GWTJahiaNodeProperty();
                    nodeProp.setName(propName);
                    List<GWTJahiaNodePropertyValue> gwtValues = new ArrayList<GWTJahiaNodePropertyValue>();
                    gwtValues.add(new GWTJahiaNodePropertyValue(node.getProperty(Constants.JCR_MIMETYPE).getString(), GWTJahiaNodePropertyType.ASYNC_UPLOAD));
                    nodeProp.setValues(gwtValues);
                    props.put(nodeProp.getName(), nodeProp);
                }
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
     * @throws org.jahia.ajax.gwt.client.service.GWTJahiaServiceException
     *          sthg bad happened
     */
    public static void saveProperties(List<GWTJahiaNode> nodes, List<GWTJahiaNodeProperty> newProps, ProcessingContext context) throws GWTJahiaServiceException {
        Locale locale = context.getCurrentLocale();
        String workspace = "default";
        JahiaUser user = context.getUser();

        for (GWTJahiaNode aNode : nodes) {
            JCRNodeWrapper objectNode;
            try {
                objectNode = sessionFactory.getThreadSession(user, workspace, locale).getNode(aNode.getPath());
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(aNode.getDisplayName()).append(" could not be accessed :\n").append(e.toString()).toString());
            }
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
                            values.add(Utils.convertValue(val));
                        }
                        Value[] finalValues = new Value[values.size()];
                        values.toArray(finalValues);
                        objectNode.setProperty(prop.getName(), finalValues);
                    } else {
                        if (prop.getValues().size() > UPLOAD) {
                            GWTJahiaNodePropertyValue propValue = prop.getValues().get(UPLOAD);
                            if (propValue.getType() == GWTJahiaNodePropertyType.ASYNC_UPLOAD) {
                                GWTFileManagerUploadServlet.Item i = GWTFileManagerUploadServlet.getItem(propValue.getString());
                                boolean clear = propValue.getString().equals("clear");
                                if (!clear && i == null) {
                                    continue;
                                }
                                ExtendedNodeDefinition end = ((ExtendedNodeType) objectNode.getPrimaryNodeType()).getChildNodeDefinitionsAsMap().get(prop.getName());

                                if (end != null) {
                                    try {
                                        if (objectNode.hasNode(prop.getName())) {
                                            objectNode.getNode(prop.getName()).remove();
                                        }

                                        if (!clear) {
                                            String s = end.getRequiredPrimaryTypeNames()[UPLOAD];
                                            Node content = objectNode.addNode(prop.getName(), s.equals("nt:base") ? "jnt:resource" : s);

                                            content.setProperty(Constants.JCR_MIMETYPE, i.contentType);
                                            content.setProperty(Constants.JCR_DATA, i.file);
                                            content.setProperty(Constants.JCR_LASTMODIFIED, new GregorianCalendar());
                                        }
                                    } catch (Throwable e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                }
                            } else {
                                if (propValue != null && propValue.getString() != null) {
                                    Value value = Utils.convertValue(propValue);
                                    objectNode.setProperty(prop.getName(), value);
                                } else {
                                    if (objectNode.hasProperty(prop.getName())) {
                                        objectNode.getProperty(prop.getName()).remove();
                                    }
                                }
                            }
                        } else if (objectNode.hasProperty(prop.getName())) {
                            objectNode.getProperty(prop.getName()).remove();
                        }
                    }

                }
            } catch (PathNotFoundException e) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Property with the name '"
                                + prop.getName() + "' not found on the node "
                                + objectNode.getPath() + ". Skipping.", e);
                    } else {
                        logger.info("Property with the name '" + prop.getName()
                                + "' not found on the node "
                                + objectNode.getPath() + ". Skipping.");
                    }
                } catch (RepositoryException re) {
                    logger.info("Property with the name '" + prop.getName()
                            + "' not found on the node " + objectNode
                            + ". Skipping.");
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private static List<Value> getCategoryPathValues(String value) {
        if (value == null || value.length() == UPLOAD) {
            return Collections.EMPTY_LIST;
        }
        List<Value> values = new LinkedList<Value>();
        String[] categories = StringUtils.split(value, ",");
        for (String categoryKey : categories) {
            try {
                values.add(new StringValue(Category.getCategoryPath(categoryKey.trim())));
            } catch (JahiaException e) {
                logger.warn("Unable to retrieve category path for category key '" + categoryKey + "'. Cause: " + e.getMessage(), e);
            }
        }
        return values;
    }

    public static GWTJahiaNode createNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, ProcessingContext context) throws GWTJahiaServiceException {
        JCRNodeWrapper parentNode;
        try {
            parentNode = sessionFactory.getThreadSession(context.getUser(), null, context.getLocale()).getNode(parentPath);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        if(name==null) {
            name = findAvailableName(parentNode, nodeType.replaceAll(":","_"), context.getUser());
        } else {
            name = findAvailableName(parentNode, name, context.getUser());
        }
        checkName(name);
        if (checkExistence(parentPath + "/" + name, context.getUser())) {
            throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
        }

        JCRNodeWrapper childNode = addNode(parentNode, name, nodeType, props);
        try {
            parentNode.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Node creation failed. Cause: " + e.getMessage());
        }
        return getGWTJahiaNode(childNode,true);
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
                throw new GWTJahiaServiceException("Node creation failed. Cause: " + e.getMessage());
            }
        }
        if (childNode == null || !childNode.isValid()) {
            throw new GWTJahiaServiceException("Node creation failed");
        }
        return childNode;
    }

    public static GWTJahiaNode unsecureCreateNode(String parentPath, String name, String nodeType, List<GWTJahiaNodeProperty> props, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            if (sessionFactory.getThreadSession(context.getUser()).getNode(parentPath + "/" + name).isValid()) {
                throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
            }
        } catch (PathNotFoundException e) {
            if(!e.getMessage().contains(name)) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(parentPath).append("/").append(name).append(" could not be accessed :\n").append(e.toString()).toString());
            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(parentPath).append("/").append(name).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        JCRNodeWrapper parentNode;
        try {
            parentNode = sessionFactory.getThreadSession(context.getUser()).getNode(parentPath);
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
        return getGWTJahiaNode(childNode,true);
    }

    private static JCRNodeWrapper unsecureAddNode(JCRNodeWrapper parentNode, String name, String nodeType, List<GWTJahiaNodeProperty> props) throws GWTJahiaServiceException {
        JCRNodeWrapper childNode = null;
        if (parentNode.isValid() && !parentNode.isFile()) {
            try {
                childNode = parentNode.addNode(name, nodeType);
                setProperties(childNode, props);
            } catch (Exception e) {
                logger.error("Exception", e);
                throw new GWTJahiaServiceException("Node creation failed");
            }
        }
        if (childNode == null || !childNode.isValid()) {
            throw new GWTJahiaServiceException("Node creation failed");
        }
        return childNode;
    }

    public static GWTJahiaNodeACL getACL(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getThreadSession(jParams.getUser()).getNode(path);
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
            ace.setPrincipalType(principal.charAt(UPLOAD));
            ace.setPrincipal(principal.substring(2));

            List<String[]> st = m.get(principal);
            Map<String, String> perms = new HashMap<String, String>();
            Map<String, String> inheritedPerms = new HashMap<String, String>();
            String inheritedFrom = null;
            for (String[] strings : st) {
                if (!path.equals(strings[UPLOAD])) {
                    inheritedFrom = strings[UPLOAD];
                    inheritedPerms.put(strings[2], strings[UPLOAD_AUTO_RENAME]);
                } else {
                    perms.put(strings[2], strings[UPLOAD_AUTO_RENAME]);
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
                    k = k.substring(k.indexOf(':') + UPLOAD_AUTO_RENAME);
                }
                labels.put(s, JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.rights.ManageRights." + k + ".label", jParams.getLocale(), k));
            }
        }
        acl.setPermissionLabels(labels);
        acl.setAclDependencies(new HashMap<String, List<String>>());
        return acl;
    }

    public static void setACL(String path, GWTJahiaNodeACL acl, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getThreadSession(jParams.getUser()).getNode(path);
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

    public static List<GWTJahiaNodeUsage> getUsages(String path, ProcessingContext jParams) throws GWTJahiaServiceException {
        JCRNodeWrapper node;
        try {
            node = sessionFactory.getThreadSession(jParams.getUser()).getNode(path);
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(new StringBuilder(path).append(" could not be accessed :\n").append(e.toString()).toString());
        }
        List<UsageEntry> usages = node.findUsages(jParams, false);
        List<GWTJahiaNodeUsage> result = new ArrayList<GWTJahiaNodeUsage>();

        for (UsageEntry usage : usages) {
            GWTJahiaNodeUsage nodeUsage = new GWTJahiaNodeUsage(usage.getId(), usage.getVersion(), usage.getWorkflow(), usage.getExtendedWorkflowState(), usage.getLang(), usage.getPageTitle(), usage.getUrl());
            nodeUsage.setVersionName(usage.getVersionName());
            result.add(nodeUsage);
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
            JCRNodeWrapper nodeToZip;
            try {
                nodeToZip = sessionFactory.getThreadSession(user).getNode(path);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(path).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (nodeToZip.hasPermission(JCRNodeWrapper.READ)) {
                nodesToZip.add(nodeToZip);
            } else {
                missedPaths.add(nodeToZip.getName());
            }
        }
        if (nodesToZip.size() > UPLOAD) {
            String firstPath = nodesToZip.get(UPLOAD).getPath();
            int index = firstPath.lastIndexOf("/");
            String parentPath;
            if (index > UPLOAD) {
                parentPath = firstPath.substring(UPLOAD, index);
            } else {
                parentPath = "/";
            }
            JCRNodeWrapper parent;
            try {
                parent = sessionFactory.getThreadSession(user).getNode(parentPath);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
            }
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
        if (missedPaths.size() > UPLOAD) {
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
            JCRNodeWrapper nodeToUnzip;
            try {
                nodeToUnzip = sessionFactory.getThreadSession(user).getNode(path);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                missedPaths.add(new StringBuilder(path).append(" could not be accessed : ").append(e.toString()).toString());
                continue;
            }
            if (nodeToUnzip.hasPermission(JCRNodeWrapper.READ)) {
                nodesToUnzip.add(nodeToUnzip);
            } else {
                missedPaths.add(nodeToUnzip.getName());
            }
        }
        if (nodesToUnzip.size() > UPLOAD) {
            String firstPath = nodesToUnzip.get(UPLOAD).getPath();
            int index = firstPath.lastIndexOf("/");
            String parentPath;
            if (index > UPLOAD) {
                parentPath = firstPath.substring(UPLOAD, index);
            } else {
                parentPath = "/";
            }
            JCRNodeWrapper parent;
            try {
                parent = sessionFactory.getThreadSession(user).getNode(parentPath);
            } catch (RepositoryException e) {
                logger.error(e.toString(), e);
                throw new GWTJahiaServiceException(new StringBuilder(parentPath).append(" could not be accessed :\n").append(e.toString()).toString());
            }
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
        if (missedPaths.size() > UPLOAD) {
            StringBuilder errors = new StringBuilder("The following files could not be unzipped:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }

    public static void mount(String name, String root, JahiaUser user) throws GWTJahiaServiceException {
        if (user.isAdminMember(UPLOAD)) {
            JCRSessionWrapper session = null;
            try {
                session = sessionFactory.getSystemSession(user.getName());
                JCRNodeWrapper parent = session.getNode("/content");
                JCRNodeWrapper mounts;
                try {
                    mounts = (JCRNodeWrapper) parent.getNode("mounts");
                } catch (PathNotFoundException nfe) {
                    try {
                        mounts = parent.addNode("mounts", "jnt:systemFolder");
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                        throw new GWTJahiaServiceException("Could not create 'mounts' folder");
                    }
                }

                JCRMountPointNode childNode = null;
                if (mounts.isValid() && !mounts.isFile()) {
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
                        throw new GWTJahiaServiceException("A system error happened");
                    }
                    try {
                        parent.save();
                    } catch (RepositoryException e) {
                        logger.error(e.getMessage(), e);
                        throw new GWTJahiaServiceException("A system error happened");
                    }
                }
                if (childNode == null || !childNode.isValid()) {
                    throw new GWTJahiaServiceException("A system error happened");
                }
            } catch (RepositoryException e) {
                logger.error("A system error happened", e);
                throw new GWTJahiaServiceException("A system error happened");
            } finally {
                if (session != null) {
                    session.logout();
                }
            }
        } else {
            throw new GWTJahiaServiceException("Only root can mount folders");
        }
    }

    public static List<GWTJahiaPortletDefinition> searchPortlets(String match, ProcessingContext jParams) {
        List<GWTJahiaPortletDefinition> results = new ArrayList<GWTJahiaPortletDefinition>();
        try {
        	List<ApplicationBean> appList = new LinkedList<ApplicationBean>();
        	if (match != null) {
        		ApplicationBean app = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(match);
        		if (app != null) {
        			appList.add(app);
        		} else {
        			logger.warn("Application not found for the UUID '" + match + "'");
        		}
        	} else {
        		appList.addAll(ServicesRegistry.getInstance().getApplicationsManagerService().getApplications());
        	}
        	for (ApplicationBean appBean : appList) {
                if (JCRContentUtils.hasPermission(jParams.getUser(), Constants.JCR_READ_RIGHTS, appBean.getID())) {
                    List<EntryPointDefinition> l = appBean.getEntryPointDefinitions();
                    for (EntryPointDefinition aL : l) {
                        results.add(createGWTJahiaPortletDefinition(jParams, appBean, aL));
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
     * @param appBean              the application bean
     * @param entryPointDefinition the entry point definition
     * @return the portlet definition
     * @throws JahiaException sthg bad happened
     */
    private static GWTJahiaPortletDefinition createGWTJahiaPortletDefinition(ProcessingContext jParams, ApplicationBean appBean, EntryPointDefinition entryPointDefinition) throws JahiaException {
        String portletType = null;
        int expTime = UPLOAD;
        String cacheScope = null;
        if (entryPointDefinition instanceof PortletEntryPointDefinition) {
            PortletEntryPointDefinition portletEntryPointDefinition = ((PortletEntryPointDefinition) entryPointDefinition);
            portletType = portletEntryPointDefinition.getInitParameter("portletType");
            expTime = portletEntryPointDefinition.getExpirationCache();
            cacheScope = portletEntryPointDefinition.getCacheScope();
        }
        if (portletType == null) {
            portletType = "jnt:portlet";
        }
        GWTJahiaNodeACL gwtJahiaNodeACL = new GWTJahiaNodeACL(new ArrayList<GWTJahiaNodeACE>());
        gwtJahiaNodeACL.setAvailablePermissions(JCRPortletNode.getAvailablePermissions(appBean.getContext(), entryPointDefinition.getName()));
        return new GWTJahiaPortletDefinition(appBean.getID(),appBean.getContext(), entryPointDefinition.getName(), entryPointDefinition.getDisplayName(jParams.getLocale()), portletType, gwtJahiaNodeACL, entryPointDefinition.getDescription(jParams.getLocale()), expTime, cacheScope);
    }

    /**
     * Create a  GWTJahiaNode object that represents a portlet instance.
     *
     * @param parentPath                 where to create the node
     * @param gwtJahiaNewPortletInstance the portlet instance
     * @param context                    the processing context
     * @return a node
     * @throws GWTJahiaServiceException sthg bad happened
     */
    public static GWTJahiaNode createPortletInstance(String parentPath, GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String name = gwtJahiaNewPortletInstance.getInstanceName();

            if (name == null) {
                name = gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName().replaceAll("/", "___") + Math.round(Math.random() * 1000000l);
            }
            checkName(name);
            if (checkExistence(parentPath + "/" + name, context.getUser())) {
                throw new GWTJahiaServiceException("A node already exists with name '" + name + "'");
            }
            JCRNodeWrapper parentNode = sessionFactory.getThreadSession(context.getUser()).getNode(parentPath);
            JCRPortletNode node = (JCRPortletNode) addNode(parentNode, name, gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getPortletType(), gwtJahiaNewPortletInstance.getProperties());

            node.setApplication(gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getApplicationId(), gwtJahiaNewPortletInstance.getGwtJahiaPortletDefinition().getDefinitionName());
            node.revokeAllPermissions();

            // set modes permissions
            if (gwtJahiaNewPortletInstance.getModes() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getModes().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
                        node.changePermissions(user, ace.getPermissions());
                    }
                }
            }

            // set roles permissions
            if (gwtJahiaNewPortletInstance.getRoles() != null) {
                for (GWTJahiaNodeACE ace : gwtJahiaNewPortletInstance.getRoles().getAce()) {
                    String user = ace.getPrincipalType() + ":" + ace.getPrincipal();
                    if (!ace.isInherited()) {
                        node.changePermissions(user, ace.getPermissions());
                    }
                }
            }
            node.changePermissions("g:users","rw");
            node.changePermissions("g:guest","r-");
            try {
                parentNode.save();
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
                throw new GWTJahiaServiceException("A system error happened");
            }
            return getGWTJahiaNode(node,true);
        } catch (RepositoryException e) {
            logger.error(e, e);
            throw new GWTJahiaServiceException("error");
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
        String prefix = Jahia.getContextPath();
        if (prefix.equals("/")) {
            prefix = "";
        } else {
            prefix = prefix.substring(1) + "/";
        }
        // get RSS GWTJahiaPortletDefinition
        final String appName = prefix + "rss";
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaRSSPortlet", context);
        if (gwtJahiaPortletDefinition == null) {
            logger.error("RSS portlet defintion not found --> Abort creating RSS portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("url", new GWTJahiaNodePropertyValue(url, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, appName, "JahiaRSSPortlet", gwtJahiaNodeProperties, context);
    }

    /**
     * Create a google gadget node
     *
     * @param parentPath
     * @param name
     * @param script
     * @param context
     * @return
     * @throws GWTJahiaServiceException
     */
    public static GWTJahiaNode createGoogleGadgetPortletInstance(String parentPath, String name, String script, ProcessingContext context) throws GWTJahiaServiceException {
        GWTJahiaNewPortletInstance gwtJahiaNewPortletInstance = new GWTJahiaNewPortletInstance();
        String prefix = Jahia.getContextPath();
        if (prefix.equals("/")) {
            prefix = "";
        } else {
            prefix = prefix.substring(1) + "/";
        }
        // get RSS GWTJahiaPortletDefinition
        final String appName = prefix + "googlegadget";
        // get RSS GWTJahiaPortletDefinition
        GWTJahiaPortletDefinition gwtJahiaPortletDefinition = createJahiaGWTPortletDefinitionByName(appName, "JahiaGoogleGadget", context);
        if (gwtJahiaPortletDefinition == null) {
            logger.error("Google gadget portlet defintion not found --> Abort creating Google Gadget portlet instance");
        }
        gwtJahiaNewPortletInstance.setGwtJahiaPortletDefinition(gwtJahiaPortletDefinition);

        // create portlet properties
        List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(name, GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("jcr:description", new GWTJahiaNodePropertyValue("", GWTJahiaNodePropertyType.STRING)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("j:expirationTime", new GWTJahiaNodePropertyValue("0", GWTJahiaNodePropertyType.LONG)));
        gwtJahiaNodeProperties.add(new GWTJahiaNodeProperty("code", new GWTJahiaNodePropertyValue(script, GWTJahiaNodePropertyType.STRING)));

        return createPortletInstance(parentPath, name, appName, "JahiaGoogleGadget", gwtJahiaNodeProperties, context);
    }

    /**
     * Uploda file depending on operation (add version, auto-rename or just upload)
     *
     * @param location
     * @param tmpName
     * @param operation
     * @param newName
     * @param ctx
     * @throws GWTJahiaServiceException
     */
    public static void uploadedFile(String location, String tmpName, int operation, String newName, ProcessingContext ctx) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper parent = sessionFactory.getThreadSession(ctx.getUser()).getNode(location);
            switch (operation) {
                case UPLOAD_NEW_VERSION:
                    JCRNodeWrapper node = (JCRNodeWrapper) parent.getNode(newName);
                    if (node == null) {
                        throw new GWTJahiaServiceException("Could'nt add a new version, file " + location + "/" + newName + "not found ");
                    }
                    addNewVersionFile(node, tmpName, ctx);
                    break;
                case UPLOAD_AUTO_RENAME:
                    newName = findAvailableName(parent, newName, ctx.getUser());
                case UPLOAD:
                    if (parent.hasNode(newName)) {
                        throw new GWTJahiaServiceException("file exists");
                    }
                default:
                    parent.uploadFile(newName, GWTFileManagerUploadServlet.getItem(tmpName).file, GWTFileManagerUploadServlet.getItem(tmpName).contentType);
                    break;
            }
            parent.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Activate versioning if nested and add a new version
     *
     * @param node
     * @param tmpName
     * @param ctx
     * @throws GWTJahiaServiceException
     */
    public static void addNewVersionFile(JCRNodeWrapper node, String tmpName, ProcessingContext ctx) throws GWTJahiaServiceException {
        try {
            if (node != null) {
                if (!node.isVersioned()) {
                    node.versionFile();
                    node.save();
                }
                node.checkout();
                node.getFileContent().uploadFile(GWTFileManagerUploadServlet.getItem(tmpName).file, GWTFileManagerUploadServlet.getItem(tmpName).contentType);
                node.save();
                node.checkpoint();

                logger.debug("Number of version: " + node.getVersions().size());

            } else {
                logger.error("Could not add version to a null file.");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Restore node version
     *
     * @param nodeUuid
     * @param versionUuid
     * @param ctx
     */
    public static void restoreNode(String nodeUuid, String versionUuid, ProcessingContext ctx) {
        try {
            JCRNodeWrapper node = (JCRNodeWrapper) sessionFactory.getThreadSession(ctx.getUser()).getNodeByUUID(nodeUuid);
            Version version = (Version) sessionFactory.getThreadSession(ctx.getUser()).getNodeByUUID(versionUuid);
            node.checkout();
            node.restore(version, true);
            node.checkpoint();

            // fluch caches: To do: flush only the nested cache
            ServicesRegistry.getInstance().getCacheService().flushAllCaches();


        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }


    /**
     * @param appName
     * @param entryPointName
     * @param context
     * @return
     */
    private static GWTJahiaPortletDefinition createJahiaGWTPortletDefinitionByName(String appName, String entryPointName, ProcessingContext context) {
        if (appName != null && entryPointName != null) {
            try {
                // TO DO: replace this part of the method by a more perfoming one
                List<ApplicationBean> appList = ServicesRegistry.getInstance().getApplicationsManagerService().getApplications();
                for (ApplicationBean anAppList : appList) {
                    if (JCRContentUtils.hasPermission(context.getUser(), Constants.JCR_READ_RIGHTS, anAppList.getID())) {
                        List<EntryPointDefinition> l = anAppList.getEntryPointDefinitions();
                        for (EntryPointDefinition aL : l) {
                            boolean foundEntryPointDefinition = appName.equalsIgnoreCase(anAppList.getName()) && aL.getName().equalsIgnoreCase(entryPointName);
                            if (foundEntryPointDefinition) {
                                return createGWTJahiaPortletDefinition(context, anAppList, aL);
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

    /**
     * Check captcha
     *
     * @param context
     * @param captcha
     * @return
     */
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

    /**
     * Check name
     *
     * @param name
     * @throws GWTJahiaServiceException
     */
    public static void checkName(String name) throws GWTJahiaServiceException {
        if (name.indexOf("*") > UPLOAD ||
                name.indexOf("/") > UPLOAD ||
                name.indexOf(":") > UPLOAD ||
                name.indexOf("\"") > UPLOAD) {
            throw new GWTJahiaServiceException("Invalid name : characters *,/,\",: cannot be used here");
        }
    }


    /**
     * Get rendered content
     *
     * @param path
     * @param workspace
     * @param locale
     * @param template
     * @param templateWrapper
     * @param editMode
     * @param ctx  @return   @throws GWTJahiaServiceException
     */
    public static String getRenderedContent(String path, String workspace, Locale locale, String template, String templateWrapper, boolean editMode, ParamBean ctx) throws GWTJahiaServiceException {
        String res = null;
        try {
            if (locale == null) {
                locale = ctx.getLocale();
            }
            JCRSessionWrapper session = sessionFactory.getThreadSession(ctx.getUser(), workspace, locale);
            JCRNodeWrapper node = session.getNode(path);
            Resource r = new Resource(node, "html", null, template);
            ctx.getRequest().setAttribute("mode", "edit");
            RenderContext renderContext = new RenderContext(ctx.getRequest(), ctx.getResponse(), ctx.getUser());
            renderContext.setSite(ctx.getSite());
            renderContext.setEditMode(editMode);
            renderContext.setMainResource(r);
            renderContext.setTemplateWrapper(templateWrapper);
            res = RenderService.getInstance().render(r, renderContext);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return res;

    }

    /**
	 * Check if a node in a given container has at least all the ACLs of the container.
	 *
	 * @param paramBean the ParamBean
	 * @param path the node path
	 * @return true if accessibility is ok, false otherwise
	 */
	public static boolean isFileAccessibleForCurrentContainer(ParamBean paramBean, String path) {
		try {
			// first let's check the JCR node ACLs
			JCRNodeWrapper node = sessionFactory.getThreadSession(paramBean.getUser()).getNode(path);
			Map<String, Map<String, String>> m = node.getActualAclEntries();
			List<String> jcrReadGroups = new ArrayList<String>();
			List<String> jcrReadUsers = new ArrayList<String>();
			logger.debug("JCR :");
			for (String key: m.keySet()) {
				// keys are g:groupName or u:userName
				String[] userKey = key.split(":");
				if (userKey.length == 2) {
					String username = userKey[1];
					logger.debug("  " + key);
					Map<String, String> perms = m.get(key);
					boolean canRead = false;
					for (Map.Entry<String, String> perm: perms.entrySet()) {
						logger.debug("    " + perm.getKey() + " - " + perm.getValue());
						if (perm.getKey().equals("jcr:read")) {
							if (perm.getValue().equalsIgnoreCase("GRANT")) {
								canRead = true;
								break;
							}
						}
					}
					if (canRead) {
						if (username.equals("guest")) { // if guest has acess, nothing left to check
							return true;
						}
						if ("g".equals(userKey[0])) {
							jcrReadGroups.add(username);
						} else {
							jcrReadUsers.add(username);
						}
					}
				}
			}

			// now let's retrieve the ACLs for the current container (from the engineMap)
			Map engineMap = (Map) paramBean.getSession().getAttribute(ProcessingContext.SESSION_JAHIA_ENGINEMAP);
			if (engineMap != null) {
				JahiaContainer theContainer = (JahiaContainer) engineMap.get("theContainer");
				JahiaBaseACL acl = theContainer.getACL();
				final Set<String> readGroups = new HashSet<String>();
				final Set<String> readUsers = new HashSet<String>();
				final List<String> groups= acl.getGroupnameList(null);
				final List<String> users = acl.getUsernameList(null);
				logger.debug("Container :");
				logger.debug("  Groups :");
				for (String group: groups) {
					logger.debug("    analyzing " + group);
					JahiaGroup g = ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(group);
					if (g != null && (acl.getPermission(g, JahiaBaseACL.READ_RIGHTS) || acl.getPermission(g, JahiaBaseACL.WRITE_RIGHTS) || acl.getPermission(g, JahiaBaseACL.ADMIN_RIGHTS))) {
						String theGroupName;
						if (group.indexOf(":") > -1) {
							theGroupName = group.split(":")[0];
						} else if (group.indexOf("}") > -1) {
							theGroupName = group.substring(group.indexOf("}")+1);
						} else {
							theGroupName = group;
						}
						readGroups.add(theGroupName);
						logger.debug("      " + theGroupName + " is supposed to read");
					} else {
						logger.debug("      " + group + " is not supposed to read");
					}
				}
				logger.debug("  Users :");
				for (String user: users) {
					logger.debug("    analyzing " + user);
					JahiaUser u = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUser(user);
					if (u != null && (acl.getPermission(u, JahiaBaseACL.READ_RIGHTS) || acl.getPermission(u, JahiaBaseACL.WRITE_RIGHTS) || acl.getPermission(u, JahiaBaseACL.ADMIN_RIGHTS))) {
						String theUserName;
						if (user.indexOf(":") > -1) {
							theUserName = user.split(":")[0];
						} else if (user.indexOf("}") > -1) {
							theUserName = user.substring(user.indexOf("}")+1);
						} else {
							theUserName = user;
						}
						readUsers.add(theUserName);
						logger.debug("      " + theUserName + " is supposed to read");
					} else {
						logger.debug("      " + user + " is not supposed to read");
					}
				}

				// then let's check if people that have read rights on the container also have them on the jcr node
				Set<String> nonReadGroups = new HashSet<String>();
				Set<String> nonReadUsers = new HashSet<String>();
				for (String group: readGroups) {
					if (!jcrReadGroups.contains(group)) {
						nonReadGroups.add(group);
					}
				}
				for (String user: readUsers) {
					if (jcrReadUsers.contains(user)) {
						nonReadUsers.add(user);
					}
				}

				// if both sets are empty, all clear
				return nonReadGroups.size() == 0 && nonReadUsers.size() == 0;
			}
		} catch (JahiaSessionExpirationException e) {
			logger.error(e.toString(), e);
		} catch (PathNotFoundException e) {
			logger.error(e.toString(), e);
		} catch (RepositoryException e) {
			logger.error(e.toString(), e);
		} catch (JahiaACLException e) {
			logger.error(e.toString(), e);
		}
		return Boolean.FALSE;
    }
    
    public static Map<String, String> getStoredPasswordsProviders(JahiaUser user) {
        Map<String, String> results = new HashMap<String,String>();
        results.put(null, user.getUsername());
        for (JCRStoreProvider provider : sessionFactory.getProviders().values()) {
            if ("storedPasswords".equals(provider.getAuthenticationType())) {
                results.put (provider.getKey(), user.getProperty("storedUsername_"+provider.getKey()));
            }
        }
        return results;
    }
 		
    public static void storePasswordForProvider(JahiaUser user, String providerKey, String username, String password) {
        if (username == null) {
            user.removeProperty("storedUsername_"+providerKey);
        } else {
            user.setProperty("storedUsername_"+providerKey, username);
        }
        if (password == null) {
            user.removeProperty("storedPassword_"+providerKey);
        } else {
            user.setProperty("storedPassword_"+providerKey, password);
        }
    }


    public static void importContent(ProcessingContext jParams, String parentPath, String fileKey) throws GWTJahiaServiceException {
        GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(fileKey);
        try {
            if ("application/zip".equals(item.contentType)) {

            } else if ("application/xml".equals(item.contentType)) {
                File tempFile = File.createTempFile("tmp", "");
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                IOUtils.copy(item.file, fileOutputStream);
                fileOutputStream.close();
                FileInputStream inputStream = new FileInputStream(tempFile);
                int format = ImportExportBaseService.getInstance().detectXmlFormat(inputStream);
                inputStream.close();

                try {
                    switch (format) {
                        case XMLFormatDetectionHandler.JCR_DOCVIEW: {
                            JCRSessionWrapper session = sessionFactory.getThreadSession(jParams.getUser());

                            session.importXML(parentPath, new FileInputStream(tempFile), ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW);
                            session.save();
                            break;
                        }

                        case XMLFormatDetectionHandler.USERS: {
                            ImportExportBaseService.getInstance().importUsers(tempFile);
                            break;
                        }
                        case XMLFormatDetectionHandler.CATEGORIES: {
                            Category cat = ServicesRegistry.getInstance().getCategoryService().getCategoryByPath(parentPath);
                            ImportExportBaseService.getInstance().importCategories(jParams, cat, new FileInputStream(tempFile));
                            break;
                        }                            
                    }
                } finally {
                    tempFile.delete();
                }
            }
        } catch (Exception e) {
            logger.error("Error when importing",e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public static void move(JahiaUser user, String sourcePath, String targetPath) throws RepositoryException, InvalidItemStateException, ItemExistsException {
        JCRSessionWrapper session = sessionFactory.getThreadSession(user);
        session.getWorkspace().move(sourcePath, targetPath);
    }

    public static void moveOnTopOf(JahiaUser user, String sourcePath, String targetPath) throws RepositoryException, InvalidItemStateException, ItemExistsException {
        JCRSessionWrapper session = sessionFactory.getThreadSession(user);
        final JCRNodeWrapper srcNode = session.getNode(sourcePath);
        final JCRNodeWrapper targetNode = session.getNode(targetPath);
        final JCRNodeWrapper targetParent = (JCRNodeWrapper) targetNode.getParent();
        if (srcNode.getParent().getPath().equals(targetParent.getPath())) {
            targetParent.orderBefore(srcNode.getName(), targetNode.getName());
        } else {
            String newname = findAvailableName(targetParent, srcNode.getName(), user);
            session.getWorkspace().move(sourcePath, targetParent.getPath()+ "/"+ newname);
            targetParent.orderBefore(newname, targetNode.getName());
        }
        targetParent.save();
        session.save();
    }

    public static void moveAtEnd(JahiaUser user, String sourcePath, String targetPath) throws RepositoryException, InvalidItemStateException, ItemExistsException {
        JCRSessionWrapper session = sessionFactory.getThreadSession(user);
        final JCRNodeWrapper srcNode = session.getNode(sourcePath);
        final JCRNodeWrapper targetNode = session.getNode(targetPath);
        if (srcNode.getParent().getPath().equals(targetNode.getPath())) {
            targetNode.orderBefore(srcNode.getName(), null);
        } else {
            String newname = findAvailableName(targetNode, srcNode.getName(), user);
            session.getWorkspace().move(sourcePath, targetNode.getPath()+ "/" + newname);
            targetNode.orderBefore(newname, null);
        }
        targetNode.save();
        session.save();
    }

    public static List<String[]> getTemplatesSet(String path, ProcessingContext ctx) throws GWTJahiaServiceException {
        List<String[]> templatesPath = new ArrayList<String[]>();
        try {
            JCRNodeWrapper node = sessionFactory.getThreadSession(ctx.getUser()).getNode(path);
            String def = null;
            if (node.hasProperty("j:defaultTemplate")) {
                templatesPath.add(new String[]{"--unset--","--unset--"});
                def = node.getProperty("j:defaultTemplate").getString();
            }

            SortedSet<Templates.Template> set = getTemplatesSet(node);
            for (Templates.Template s : set) {
                String tpl;
                if (s.getOwnerPackage() == null) {
                    tpl = "Default";
                } else {
                    tpl = s.getOwnerPackage().getName();
                }
                if (s.getKey().equals(def)) {
                    templatesPath.add(new String[]{s.getKey(),"* " + tpl +" : "+ s.getKey()});
                } else {
                    templatesPath.add(new String[]{s.getKey(),tpl +" : "+ s.getKey()});
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }

        return templatesPath;
    }

    private static SortedSet<Templates.Template> getTemplatesSet(JCRNodeWrapper node) throws RepositoryException {
        ExtendedNodeType nt = (ExtendedNodeType) node.getPrimaryNodeType();
        SortedSet<Templates.Template> set;
        if (node.getPrimaryNodeTypeName().equals("jnt:nodeReference")) {

            set = getTemplatesSet((JCRNodeWrapper) node.getProperty("j:node").getNode());
        } else if (node.isNodeType("jnt:contentList") || node.isNodeType("jnt:containerList")) {
            set = Templates.getTemplatesSet(nt);
            List<JCRNodeWrapper> l = node.getChildren();
            for (JCRNodeWrapper c : l) {
                set.addAll(getTemplatesSet(c));
            }
        } else {
            set = Templates.getTemplatesSet(nt);
        }
        return set;
    }

    public static void pasteReferenceOnTopOf(GWTJahiaNode aNode, String destinationPath, String name, JahiaUser user, boolean moveOnTop) throws GWTJahiaServiceException {
        try {
            final JCRNodeWrapper targetParent;
            JCRSessionWrapper session = sessionFactory.getThreadSession(user);
            JCRNodeWrapper targetNode = session.getNode(destinationPath);
            if(moveOnTop)
            targetParent = (JCRNodeWrapper) targetNode.getParent();
            else targetParent = targetNode;
            if (targetParent.isCollection() && targetParent.isWriteable()) {
                JCRNodeWrapper node = session.getNode(aNode.getPath());
                if (node.hasPermission(JCRNodeWrapper.READ)) {
                    name = findAvailableName(targetParent, name, user);
                    doPasteReference(targetParent, node, name);
                    if(moveOnTop)
                    targetParent.orderBefore(name, targetNode.getName());
                    targetParent.save();
                }
            }
        } catch (RepositoryException e) {
            logger.error("Exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        } catch (JahiaException e) {
            logger.error("Exception", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public static void pasteReferencesOnTopOf(List<GWTJahiaNode> pathsToCopy, String destinationPath, JahiaUser user, boolean moveOnTop) throws GWTJahiaServiceException {
        List<String> missedPaths = new ArrayList<String>();

        final JCRNodeWrapper targetParent;
        JCRNodeWrapper targetNode;
        try {
            targetNode = sessionFactory.getThreadSession(user).getNode(destinationPath);

            if(moveOnTop) {
                targetParent = (JCRNodeWrapper) targetNode.getParent();
            } else {
                targetParent = targetNode;
            }
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }

        for (GWTJahiaNode aNode : pathsToCopy) {
            JCRNodeWrapper node = jcr.getFileNode(aNode.getPath(), user);
            String name = node.getName();
            if (node.hasPermission(JCRNodeWrapper.READ)) {
                if (targetParent.isCollection()) {
                    try {
                        name = findAvailableName(targetParent, name, user);
                        if (targetParent.isWriteable()) {
                            doPasteReference(targetParent, node, name);
                            if(moveOnTop)
                            targetParent.orderBefore(node.getName(), targetNode.getName());
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
                missedPaths.add(new StringBuilder("Source file ").append(name).append(" could not be read by ").append(user.getUsername()).append(" - ACCESS DENIED").toString());
            }
        }
        try {
            targetParent.save();
        } catch (RepositoryException e) {
            throw new GWTJahiaServiceException(e.getMessage());
        }

        if (missedPaths.size() > UPLOAD) {
            StringBuilder errors = new StringBuilder("The following files could not have their reference pasted:");
            for (String err : missedPaths) {
                errors.append("\n").append(err);
            }
            throw new GWTJahiaServiceException(errors.toString());
        }
    }


    /**
     * Publish a node into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param path Path of the node to publish
     * @param languages Set of languages to publish if null publish all languages
     * @param user the user for obtaining the jcr session
     * @param publishParent Recursively publish the parents
     * @throws GWTJahiaServiceException in case of any RepositoryException
     */
    public static void publish(String path, Set<String> languages, JahiaUser user, boolean publishParent) throws GWTJahiaServiceException {
        try {
            publicationService.publish(path, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, languages, user, publishParent, false);
        } catch (RepositoryException e) {
            logger.error("repository exception",e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    /**
     * Publish a list of nodes into the live workspace.
     * Referenced nodes will also be published.
     * Parent node must be published, or will be published if publishParent is true.
     *
     * @param paths list of paths of the nodes to publish
     * @param languages Set of languages to publish if null publish all languages
     * @param user the user for obtaining the jcr session
     * @param publishParent Recursively publish the parents
     * @throws GWTJahiaServiceException in case of any RepositoryException
     */
    public static void publish(List<String> paths, Set<String> languages, JahiaUser user, boolean publishParent) throws GWTJahiaServiceException {
    	// TODO implement me!
    }

    /**
     * Get the publication status information for a particular path.
     * @param path to get publication info from
     * @param user the user for obtaining the jcr session
     * @return a GWTJahiaPublicationInfo object filled with the right status for the publication state of this path
     * @throws GWTJahiaServiceException in case of any RepositoryException
     */
    public static GWTJahiaPublicationInfo getPublicationInfo(String path, Set<String> languages, boolean includesReferences,JahiaUser user) throws GWTJahiaServiceException {
        try {
            PublicationInfo pubInfo = publicationService.getPublicationInfo(path,  user, languages, includesReferences);
            return convert(path, pubInfo);
        } catch (RepositoryException e) {
            logger.error("repository exception",e);
            throw new GWTJahiaServiceException(e.getMessage());
        }

    }

    private static GWTJahiaPublicationInfo convert(String path, PublicationInfo pubInfo) {
        List<GWTJahiaPublicationInfo> list = new ArrayList<GWTJahiaPublicationInfo>();

        GWTJahiaPublicationInfo gwtInfo = new GWTJahiaPublicationInfo(path, pubInfo.getStatus(), pubInfo.isCanPublish());
        for (String p : pubInfo.getReferences().keySet()) {
            PublicationInfo pi = pubInfo.getReferences().get(p);
            gwtInfo.add(convert(p, pi));
        }

        return  gwtInfo;
    }

    /**
     * Unpublish a node from live workspace.
     * Referenced Node will not be unpublished.
     *
     * @param path path of the node to unpublish
     * @param languages Set of languages to unpublish if null unpublish all languages
     * @param user the user for obtaining the jcr session
     * @throws GWTJahiaServiceException in case of any RepositoryException
     */
    public static void unpublish(String path, Set<String> languages, JahiaUser user) throws GWTJahiaServiceException {
        try {
            publicationService.unpublish(path, languages, user);
        } catch (RepositoryException e) {
            logger.error("repository exception",e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }
}
