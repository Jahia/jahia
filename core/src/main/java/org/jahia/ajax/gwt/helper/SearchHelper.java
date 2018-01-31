/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.helper;

import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.apache.lucene.queryParser.QueryParser;
import org.jahia.ajax.gwt.client.data.GWTJahiaSearchQuery;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.search.SearchCriteria;
import org.jahia.services.search.SearchCriteria.Term.SearchFields;
import org.jahia.services.search.jcr.JahiaJCRSearchProvider;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import java.util.*;

/**
 * Search utility class.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 2:23:56 PM
 */
public class SearchHelper {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(SearchHelper.class);

    private JCRStoreService jcrService;

    private NavigationHelper navigation;
    private ContentManagerHelper contentManager;

    private JahiaJCRSearchProvider jcrSearchProvider;

    public void setJcrService(JCRStoreService jcrService) {
        this.jcrService = jcrService;
    }

    public void setNavigation(NavigationHelper navigation) {
        this.navigation = navigation;
    }

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    /**
     * Search for searchString in the name f the node
     *
     *
     * @param searchString
     * @param limit
     * @param site
     *@param currentUserSession  @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> search(String searchString, int limit, JCRSiteNode site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(formatQuery(searchString), currentUserSession);
            return navigation.executeQuery(q, null,null,null, (site != null ? Arrays.asList(site.getSiteKey()): null));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Search by Serach bean (used by the advanced search)
     *
     *
     *
     *
     * @param search
     * @param limit
     * @param offset
     * @param showOnlyNodesWithTemplates
     * @param site
     *@param currentUserSession  @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> search(GWTJahiaSearchQuery search, int limit, int offset, boolean showOnlyNodesWithTemplates, JCRSiteNode site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(search, limit, offset, currentUserSession);
            if (logger.isDebugEnabled()) {
                logger.debug("Executing query: " + q.getStatement());
            }
            return navigation.executeQuery(q, search.getNodeTypes(), search.getMimeTypes(), search.getFilters(), Arrays.asList(GWTJahiaNode.ICON, "jcr:created", "jcr:createdBy",
                    GWTJahiaNode.TAGS, GWTJahiaNode.CHILDREN_INFO, "j:view", "j:width", "j:height", GWTJahiaNode.PUBLICATION_INFO, GWTJahiaNode.PRIMARY_TYPE_LABEL, GWTJahiaNode.PERMISSIONS), search.getSites(), showOnlyNodesWithTemplates);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Search for searchString and filters in the name f the node
     *
     *
     * @param searchString
     * @param limit
     * @param nodeTypes
     * @param mimeTypes
     * @param filters
     * @param site
     *@param currentUserSession  @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaNode> search(String searchString, int limit, List<String> nodeTypes, List<String> mimeTypes, List<String> filters, JCRSiteNode site, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = createQuery(formatQuery(searchString), currentUserSession);
            return navigation.executeQuery(q, nodeTypes, mimeTypes, filters, (site != null ? Arrays.asList(site.getSiteKey()): null));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Search for searchString and filters in the name f the node
     *
     *  @param searchString
     * @param limit
     * @param offset
     * @param nodeTypes
     * @param mimeTypes
     * @param filters
     * @param currentUserSession  @return      @throws GWTJahiaServiceException
     * */
    public List<GWTJahiaNode> searchSQL(String searchString, int limit, int offset, List<String> nodeTypes, List<String> mimeTypes,
                                        List<String> filters, List<String> fields, JCRSessionWrapper currentUserSession) throws GWTJahiaServiceException {
        try {
            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery(searchString,Query.JCR_SQL2);
            q.setLimit(limit);
            q.setOffset(offset);
            return navigation.executeQuery(q, nodeTypes, mimeTypes, filters,fields, null, false);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<GWTJahiaNode>();
    }

    /**
     * Get saved search
     *
     *
     *
     * @param site
     * @param currentUserSession
     * @return
     */
    public List<GWTJahiaNode> getSavedSearch(JCRSiteNode site, JCRSessionWrapper currentUserSession) {
        List<GWTJahiaNode> result = new ArrayList<GWTJahiaNode>();
        JCRNodeWrapper user;
        try {
            user = currentUserSession.getUserNode();
        } catch (Exception e) {
            logger.error("no user folder for site " + site.getSiteKey() + " and user " + currentUserSession.getUser().getName());
            return result;
        }
        try {
            String s = "select * from [nt:query] as q where isdescendantnode(q, ['" + JCRContentUtils.sqlEncode(user.getPath()) + "'])";
            Query q = currentUserSession.getWorkspace().getQueryManager().createQuery(s, Query.JCR_SQL2);
            return navigation.executeQuery(q, null,null,null,null);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }


    /**
     * Save search
     *
     * @param searchString
     * @param name
     * @param site
     * @param currentUserSession
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode saveSearch(String searchString, String name, JCRSiteNode site, JCRSessionWrapper currentUserSession, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.could.not.store.query.with.null.name",uiLocale));
            }
            Query q = createQuery(searchString, currentUserSession);
            JCRNodeWrapper user;
            try {
                user = currentUserSession.getUserNode();
            } catch (Exception e) {
                logger.error("no user folder for site " + site.getSiteKey() + " and user " + currentUserSession.getUser().getName());
                throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.no.user.folder.to.store.query",uiLocale));
            }

            JCRNodeWrapper queryStore;
            if (!user.hasNode("savedSearch")) {
                currentUserSession.checkout(user);
                queryStore = user.createCollection("savedSearch");
            } else {
                queryStore = currentUserSession.getNode(user.getPath() + "/savedSearch");
                currentUserSession.checkout(queryStore);
            }
            String path = queryStore.getPath() + "/" + name;
            if (contentManager.checkExistence(path, currentUserSession, uiLocale)) {
                throw new ExistingFileException("The node " + path + " alreadey exists.");
            }
            q.storeAsNode(path);
            user.getSession().save();
            return navigation.getGWTJahiaNode(currentUserSession.getNode(path));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.could.not.store.query",uiLocale));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.could.not.store.query",uiLocale));
        }
    }

    /**
     * Save search
     *
     *
     * @param search
     * @param path
     * @param name
     * @param session
     * @param uiLocale
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaNode saveSearch(GWTJahiaSearchQuery search, String path, String name, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            if (name == null) {
                throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.could.not.store.query.with.null.name",uiLocale));
            }

            JCRNodeWrapper parent = null;
            if (path == null) {
                parent = session.getUserNode();
            } else {
                parent = session.getNode(path);
            }

            final String saveSearchPath = parent.getPath() + "/" + contentManager.findAvailableName(parent, name);
            parent.checkout();
            logger.debug("Save search path: " + saveSearchPath);
            Query q = createQuery(search, session);

            q.storeAsNode(saveSearchPath);

            session.save();

            return navigation.getGWTJahiaNode(session.getNode(saveSearchPath));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.could.not.store.query",uiLocale));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(Messages.getInternal("label.gwt.error.could.not.store.query",uiLocale));
        }
    }


    /**
     * Add "*" at beginning and end of query if not present in original search string.
     * Ex: *query   -->   *query
     * query*   -->   query*
     * query    -->   *query*
     *
     * @param rawQuery the raw query string
     * @return formatted query string
     */
    public static String formatQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.length() == 0) {
            return "";
        } else if (rawQuery.startsWith("*") || rawQuery.endsWith("*")) {
            return rawQuery;
        } else {
            return new StringBuilder("*").append(rawQuery).append("*").toString();
        }
    }

    /**
     * @param jcrSearchProvider the jcrSearchProvider to set
     */
    public void setJcrSearchProvider(JahiaJCRSearchProvider jcrSearchProvider) {
        this.jcrSearchProvider = jcrSearchProvider;
    }

    /**
     * Creates the {@link Query} instance from the provided search criteria.
     *
     * @param searchString
     * @param session
     * @return
     * @throws RepositoryException in case of JCR-related errors
     */
    public Query createQuery(String searchString, JCRSessionWrapper session) throws RepositoryException {
        SearchCriteria criteria = new SearchCriteria();
        criteria.getTerms().get(0).setTerm(QueryParser.escape(searchString));
        return jcrSearchProvider.buildQuery(criteria, session);
    }

    /**
     * Create JCR query
     *
     * @param gwtQuery
     * @param session
     * @return
     * @throws InvalidQueryException
     * @throws RepositoryException in case of JCR-related errors
     */
    private Query createQuery(GWTJahiaSearchQuery gwtQuery, JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        return createQuery(gwtQuery, 0, 0, session);
    }

    /**
     * Creates the {@link Query} instance from the provided search criteria.
     *
     * @param gwtQuery the search criteria bean
     * @param session  current JCR session
     * @return the {@link Query} instance, created from the provided search criteria
     * @throws RepositoryException in case of JCR-related errors
     * @throws InvalidQueryException
     */
    private Query createQuery(final GWTJahiaSearchQuery gwtQuery, int limit, int offset, JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        SearchCriteria criteria = new SearchCriteria();
        if (offset > 0) {
            criteria.setOffset(offset);
        }
        if (limit > 0) {
            criteria.setLimit(limit);
        }

        // page path
        if (gwtQuery.getPages() != null && !gwtQuery.getPages().isEmpty()) {
            criteria.getPagePath().setValue(gwtQuery.getPages().get(0).getPath());
            criteria.getPagePath().setIncludeChildren(true);
        }

        // nodeType
        if (gwtQuery.getNodeTypes() != null && gwtQuery.getNodeTypes().size() == 1) {
            criteria.setNodeType(gwtQuery.getNodeTypes().get(0));
            gwtQuery.setNodeTypes(new LinkedList<String>());
        }        
        
        // language
        if (gwtQuery.getLanguage() != null && gwtQuery.getLanguage().getLanguage() != null) {
            criteria.getLanguages().setValue(gwtQuery.getLanguage().getLanguage());
        }

//        // category
//        if (gwtQuery.getCategories() != null && !gwtQuery.getCategories().isEmpty()) {
//            criteria.getLanguages().setValue(gwtQuery.getLanguage().getLanguage());
//        }
//
        // query string
        if (gwtQuery.getQuery() != null && gwtQuery.getQuery().length() > 0) {
            criteria.getTerms().get(0).setTerm(QueryParser.escape(gwtQuery.getQuery()));
            SearchFields fields = criteria.getTerms().get(0).getFields();
            fields.setSiteContent(gwtQuery.isInContents());
            fields.setFilename(gwtQuery.isInName());
            fields.setFileContent(gwtQuery.isInFiles());
            fields.setTitle(gwtQuery.isInMetadatas());
            fields.setDescription(gwtQuery.isInMetadatas());
            fields.setKeywords(gwtQuery.isInMetadatas());
            fields.setTags(gwtQuery.isInTags());
        }

        Date startDate = null;
        SearchCriteria.DateValue creationDate = new SearchCriteria.DateValue();
        creationDate.setType(SearchCriteria.DateValue.Type.RANGE);
        criteria.setCreated(creationDate);
        SearchCriteria.DateValue lastModifiedDate = new SearchCriteria.DateValue();
        lastModifiedDate.setType(SearchCriteria.DateValue.Type.RANGE);
        criteria.setLastModified(lastModifiedDate);
        final SearchCriteria.NodeProperty lastPublishedProp = criteria.getProperties().get(Constants.JAHIAMIX_LASTPUBLISHED).get(Constants.LASTPUBLISHED);
        SearchCriteria.DateValue lastPublished = lastPublishedProp.getDateValue();
        lastPublishedProp.setType(SearchCriteria.NodeProperty.Type.DATE);
        lastPublishedProp.setName(Constants.LASTPUBLISHED);
        lastPublished.setType(SearchCriteria.DateValue.Type.RANGE);

        if (gwtQuery.getTimeInDays() != null) {
            // compute startDate
            int timeInDays = Integer.parseInt(gwtQuery.getTimeInDays());
            Calendar cal = Calendar.getInstance();
            if (timeInDays < 30) {
                cal.add(Calendar.DATE,-timeInDays);
            } else if (timeInDays < 365) {
                cal.add(Calendar.MONTH, -(timeInDays / 30));
            } else {
                cal.add(Calendar.YEAR, -(timeInDays / 365));
            }
            startDate = cal.getTime();
        }

        if (gwtQuery.getEndLastModifiedDate() != null) {
            lastModifiedDate.setToAsDate(gwtQuery.getEndLastModifiedDate());
            if (startDate != null) {
                lastModifiedDate.setFromAsDate(startDate);
            }
        } else if (gwtQuery.getEndCreatedDate() != null) {
            creationDate.setToAsDate(gwtQuery.getEndCreatedDate());
            if (startDate != null) {
                creationDate.setFromAsDate(startDate);
            }
         } else if (gwtQuery.getEndPublishedDate() != null) {
            lastPublished.setToAsDate(gwtQuery.getEndPublishedDate());
            if (startDate != null) {
                lastPublished.setFromAsDate(startDate);
            }
        }

        if (gwtQuery.getStartCreatedDate() != null) {
            creationDate.setFromAsDate(gwtQuery.getStartCreatedDate());
            criteria.setCreated(creationDate);
        }

        if (gwtQuery.getStartLastModifiedDate() != null ) {
            lastModifiedDate.setFromAsDate(gwtQuery.getStartLastModifiedDate());
            criteria.setLastModified(lastModifiedDate);
        }

        if (gwtQuery.getStartPublishedDate() != null) {
            lastPublished.setFromAsDate(gwtQuery.getStartPublishedDate());
        }

        if (gwtQuery.getOriginSiteUuid() != null) {
            String siteKey = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<String>() {
                public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    try {
                        JCRNodeWrapper nodeWrapper = session.getNodeByIdentifier(gwtQuery.getOriginSiteUuid());
                        return nodeWrapper.getName();
                    } catch (ItemNotFoundException e) {
                        logger.error("Unable for find site node by UUID: " + gwtQuery.getOriginSiteUuid(), e);
                    }
                    return null;
                }
            });
            if (siteKey != null) {
                criteria.setOriginSiteKey(siteKey);
            }
            
        }

        if (gwtQuery.getSites() != null) {
            SearchCriteria.CommaSeparatedMultipleValue sites = new SearchCriteria.CommaSeparatedMultipleValue();
            sites.setValues(gwtQuery.getSites().toArray(new String[gwtQuery.getSites().size()]));
            criteria.setSites(sites);
        }

        if (gwtQuery.getBasePath() != null) {
            SearchCriteria.HierarchicalValue filePath = new SearchCriteria.HierarchicalValue();
            filePath.setValue(gwtQuery.getBasePath());
            filePath.setIncludeChildren(true);
            criteria.setFilePath(filePath);
        }

        return jcrSearchProvider.buildQuery(criteria, session);
    }
}
