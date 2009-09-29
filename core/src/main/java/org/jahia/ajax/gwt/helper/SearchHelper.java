package org.jahia.ajax.gwt.helper;

import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.ajax.gwt.client.util.content.JCRClientUtils;
import org.jahia.ajax.gwt.utils.JahiaGWTUtils;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreService;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
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
    private static Logger logger = Logger.getLogger(SearchHelper.class);

    private JCRStoreService jcrService;
    private JCRSessionFactory sessionFactory;

    private NavigationHelper navigation;
    private ContentManagerHelper contentManager;

    public void setJcrService(JCRStoreService jcrService) {
        this.jcrService = jcrService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public List<GWTJahiaNode> search(String searchString, int limit, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(JahiaGWTUtils.formatQuery(searchString), context);
            return navigation.executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    public List<GWTJahiaNode> search(String searchString, int limit, String nodeTypes, String mimeTypes, String filters, ProcessingContext context) throws GWTJahiaServiceException {
        if (nodeTypes == null) {
            nodeTypes = JCRClientUtils.FILE_NODETYPES;
        }
        String[] nodeTypesToApply = navigation.getFiltersToApply(nodeTypes);
        String[] mimeTypesToMatch = navigation.getFiltersToApply(mimeTypes);
        String[] filtersToApply = navigation.getFiltersToApply(filters);
        try {
            Query q = createQuery(JahiaGWTUtils.formatQuery(searchString), context);
            return navigation.executeQuery(q, nodeTypesToApply, mimeTypesToMatch, filtersToApply, context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    public List<GWTJahiaNode> getSavedSearch(ProcessingContext context) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        try {
            String s = "select * from [nt:query]";
            Query q = sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return navigation.executeQuery(q, new String[0], new String[0], new String[0], context);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    Query createQuery(String searchString, ProcessingContext context) throws RepositoryException {
        String s = "select * from [jmix:hierarchyNode] as h where contains(h.[j:nodename]," + JCRContentUtils.stringToJCRSearchExp(searchString) + ")";
        return sessionFactory.getCurrentUserSession().getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
    }

    public GWTJahiaNode saveSearch(String searchString, String name, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String workspace = "default";
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }
            Query q = createQuery(searchString, context);
            List<JCRNodeWrapper> users = jcrService.getUserFolders(context.getSite().getSiteKey(), context.getUser());
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
            if (contentManager.checkExistence(path)) {
                throw new ExistingFileException("The node " + path + " alreadey exists.");
            }
            q.storeAsNode(path);
            if (createdSearchFolder) {
                user.save();
            } else {
                queryStore.save();
            }
            return navigation.getGWTJahiaNode(sessionFactory.getCurrentUserSession(workspace, context.getLocale()).getNode(path), true);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("Could not store query");
        }
    }

    public GWTJahiaNode saveSearch(String searchString, String path, String name, ProcessingContext context) throws GWTJahiaServiceException {
        try {
            String workspace = "default";
            if (name == null) {
                throw new GWTJahiaServiceException("Could not store query with null name");
            }

            JCRNodeWrapper parent = sessionFactory.getCurrentUserSession(workspace).getNode(path);
            name = contentManager.findAvailableName(parent, name);
            Query q = createQuery(searchString, context);
            q.storeAsNode(path + "/" + name);
            parent.saveSession();

            return navigation.getGWTJahiaNode(sessionFactory.getCurrentUserSession(workspace, context.getLocale()).getNode(path + "/" + name), true);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException("Could not store query");
        } catch (Exception e) {
            e.printStackTrace();
            throw new GWTJahiaServiceException("Could not store query");
        }
    }
}
