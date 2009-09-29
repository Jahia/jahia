package org.jahia.ajax.gwt.content.server.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.utils.JahiaGWTUtils;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreService;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:23:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class SearchHelper {
    private static JCRStoreService jcr = ServicesRegistry.getInstance().getJCRStoreService();
    private static JCRSessionFactory sessionFactory = jcr.getSessionFactory();

    private static Logger logger = Logger.getLogger(SearchHelper.class);

    public static List<GWTJahiaNode> search(String searchString, int limit, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(JahiaGWTUtils.formatQuery(searchString), context);
            return executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    public static List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, ProcessingContext context) throws GWTJahiaServiceException {
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        String[] nodeTypesToApply = NavigationHelper.getFiltersToApply(nodeTypes);
        String[] mimeTypesToMatch = NavigationHelper.getFiltersToApply(mimeTypes);
        String[] filtersToApply = NavigationHelper.getFiltersToApply(filters);
        try {
            Query q = createQuery(JahiaGWTUtils.formatQuery(searchString), context);
            return executeQuery(q, nodeTypesToApply, mimeTypesToMatch, filtersToApply, context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    public static List<GWTJahiaNode> executeQuery(Query q, String[] nodeTypesToApply, String[] mimeTypesToMatch, String[] filtersToApply, ProcessingContext context) throws RepositoryException {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        QueryResult qr = q.execute();
        NodeIterator ni = qr.getNodes();
        List<String> foundPaths = new ArrayList<String>();
        while (ni.hasNext()) {
            JCRNodeWrapper n = (JCRNodeWrapper) ni.nextNode();
            if (NavigationHelper.matchesNodeType(n, nodeTypesToApply) && n.isVisible()) {
                if ((filtersToApply.length == 0 && mimeTypesToMatch.length == 0)
                        || n.isCollection()
                        || (NavigationHelper.matchesFilters(n.getName(), filtersToApply) && NavigationHelper.matchesFilters(n.getFileContent().getContentType(), mimeTypesToMatch))) {
                    String path = n.getPath();
                    if (!foundPaths.contains(path)) { // TODO dirty filter, please correct search/index issue (sometimes duplicate results)
                        foundPaths.add(path);
                        result.add(NavigationHelper.getGWTJahiaNode(n, true));
                    }
                }
            }
        }
        return result;
    }

    public static List<GWTJahiaNode> getSavedSearch(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [nt:query]";
            Query q = sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    static Query createQuery(String searchString, ProcessingContext context) throws RepositoryException {
        String s = "select * from [jmix:hierarchyNode] as h where contains(h.[j:nodename]," + JCRContentUtils.stringToJCRSearchExp(searchString) + ")";
        return sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
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
                queryStore = sessionFactory.getCurrentUserSession(workspace, context.getLocale()).getNode(user.getPath() + "/savedSearch");
            }
            String path = queryStore.getPath() + "/" + name;
            if (ContentManagerHelper.checkExistence(path)) {
                throw new ExistingFileException("The node " + path + " alreadey exists.");
            }
            q.storeAsNode(path);
            if (createdSearchFolder) {
                user.save();
            } else {
                queryStore.save();
            }
            return NavigationHelper.getGWTJahiaNode(sessionFactory.getCurrentUserSession(workspace, context.getLocale()).getNode(path), true);
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

            JCRNodeWrapper parent = sessionFactory.getCurrentUserSession(workspace).getNode(path);
            name = ContentManagerHelper.findAvailableName(parent, name);
            Query q = createQuery(searchString, context);
            q.storeAsNode(path + "/" + name);
            parent.saveSession();

            return NavigationHelper.getGWTJahiaNode(sessionFactory.getCurrentUserSession(workspace, context.getLocale()).getNode(path + "/" + name), true);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("Could not store query");
        }
    }
}
