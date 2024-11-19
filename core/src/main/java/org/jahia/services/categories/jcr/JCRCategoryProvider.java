/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.services.categories.jcr;

import org.jahia.services.content.JCRContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.api.Constants;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.categories.CategoryBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.*;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import java.util.*;

/**
 * This is the manager/provider class to create/get/remove categories within Jahia's JCR.
 *
 * @author Benjamin Papez
 *
 */
public class JCRCategoryProvider {

    private static final Logger logger = LoggerFactory.getLogger(JCRCategoryProvider.class);

    private transient JCRSessionFactory sessionFactory;

    /**
     * Setting the JCR store service (done via Spring)
     * @param sessionFactory
     */
    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Returns the folder node (of nt:folder type) holding all root categories
     * @return categories' root folder JCR node
     */
    public Node getCategoriesRoot() {
        Node rootNodeWrapper = null;
        try {
            rootNodeWrapper = getCategoriesRoot(sessionFactory
                    .getCurrentUserSession());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return rootNodeWrapper;
    }

    private Node getCategoriesRoot(JCRSessionWrapper jcrSessionWrapper) {
        JCRNodeWrapper rootNodeWrapper = null;
        try {
            rootNodeWrapper = jcrSessionWrapper.getNode(JCRContentUtils.getSystemSitePath()+"/categories");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return rootNodeWrapper;
    }

    /**
     * Get all root categories accessible by the given user
     * @param user accessing the repository
     * @return list of root categories (Category facade objects)
     */
    public List<Category> getRootCategories(JahiaUser user) {
        JCRNodeWrapper rootNodeWrapper = null;
        List<Category> rootCategories = new ArrayList<Category>();
        try {
            rootNodeWrapper = (JCRNodeWrapper) getCategoriesRoot();
            for (NodeIterator it = rootNodeWrapper.getNodes(); it.hasNext();) {
                JCRNodeWrapper rootCategoryNode = (JCRNodeWrapper) it.nextNode();
                if (rootCategoryNode.isNodeType(Constants.JAHIANT_CATEGORY)) {
                    rootCategories.add(new Category(
                            createCategoryBeanFromNode(rootCategoryNode)));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return rootCategories;
    }

    /**
     * This is the method that creates a new category in the system, with all the specified attributes.
     * @param key of category to be created
     * @param parentCategory
     * @return newly created category (Category facade object)
     * @throws JahiaException
     */
    public Category createCategory(String key, Category parentCategory) throws JahiaException {
        Category newCategory = null;
        try {
            JCRSessionWrapper jcrSessionWrapper = sessionFactory
                    .getCurrentUserSession();
            JCRNodeWrapper parentNodeWrapper = getParentNode(parentCategory,
                    jcrSessionWrapper);
            if (!Constants.EDIT_WORKSPACE.equals(parentNodeWrapper.getSession().getWorkspace().getName())) {
                parentNodeWrapper = jcrSessionWrapper.getNodeByIdentifier(parentNodeWrapper.getIdentifier());
            }

            jcrSessionWrapper.checkout(parentNodeWrapper);
            final JCRNodeWrapper wrapper = parentNodeWrapper.addNode(key,
                    Constants.JAHIANT_CATEGORY);
            jcrSessionWrapper.save();
            newCategory = new Category(createCategoryBeanFromNode(wrapper));
        } catch (ItemExistsException e) {
            throw new JahiaException("Category " + key
                    + " already exists", "Category " + key
                    + " already exists", JahiaException.DATA_ERROR,
                    JahiaException.ERROR_SEVERITY);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return newCategory;
    }

    /**
     * Get category by its ID
     * @param categoryUUID
     * @return category having the given UUID
     */
    public Category getCategoryByUUID(String categoryUUID) {
        Category categoryByUUID = null;
        try {
            JCRSessionWrapper session = sessionFactory
                    .getCurrentUserSession(Constants.LIVE_WORKSPACE);
            Node categoryNode = session.getNodeByUUID(categoryUUID);
            categoryByUUID = new Category(
                    createCategoryBeanFromNode(categoryNode));
        } catch (PathNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return categoryByUUID;
    }


    /**
     * Get category by path
     * @param categoryPath
     * @return
     */
    public Category getCategoryByPath(String categoryPath) {
        Category categoryByUUID = null;
        try {
            JCRSessionWrapper session = sessionFactory
                    .getCurrentUserSession(Constants.LIVE_WORKSPACE);
            Node categoryNode = session.getNode(categoryPath);
            categoryByUUID = new Category(
                    createCategoryBeanFromNode(categoryNode));
        } catch (PathNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return categoryByUUID;
    }

    /**
     * Get child category within a parent category (or null for root) by its key
     * @param categoryKey
     * @param parentCategory
     * @return the category matching the key
     */
    public Category getCategoryByKey(String categoryKey, Category parentCategory) {
        Category newCategory = null;
        try {
            JCRSessionWrapper jcrSessionWrapper = sessionFactory
                    .getCurrentUserSession(Constants.LIVE_WORKSPACE);
            JCRNodeWrapper parentNodeWrapper = null;
            if (parentCategory != null) {
                parentNodeWrapper = (JCRNodeWrapper) ((JCRCategory) parentCategory
                        .getJahiaCategory()).getCategoryNode();
            }
            if (parentNodeWrapper == null) {
                parentNodeWrapper = (JCRNodeWrapper) getCategoriesRoot(jcrSessionWrapper);
            }
            final JCRNodeWrapper wrapper = (JCRNodeWrapper) parentNodeWrapper
                    .getNode(categoryKey);
            newCategory = new Category(createCategoryBeanFromNode(wrapper));
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return newCategory;

    }

    /**
     * Find categories by its key anywhere in the tree
     * @param categoryKey
     * @return list of categories matching the key
     */
    public List<Category> findCategoryByKey(String categoryKey) {
        final List<Category> result = new ArrayList<Category>();
        try {
            Session session = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE);
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder("SELECT * FROM ["
                        + Constants.JAHIANT_CATEGORY + "] as cat WHERE localname(cat) = '");
                query.append(JCRContentUtils.sqlEncode(categoryKey));
                query.append("' ");
                if (logger.isDebugEnabled()) {
                    logger.debug(query.toString());
                }
                Query q = session.getWorkspace().getQueryManager().createQuery(
                        query.toString(), Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    Node categoryNode = ni.nextNode();
                    result.add(new Category(
                            createCategoryBeanFromNode(categoryNode)));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Find categories by its key prefix anywhere in the tree
     * @param categoryKey
     * @return list of categories matching the key
     */
    public List<Category> findCategoriesStartingByKey(String categoryKey) {
        final List<Category> result = new ArrayList<Category>();
        try {
            Session session = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE);
            if (session.getWorkspace().getQueryManager() != null) {
                StringBuilder query = new StringBuilder("SELECT * FROM ["
                        + Constants.JAHIANT_CATEGORY
                        + "] as cat WHERE localname(cat) LIKE '");
                query.append(JCRContentUtils.sqlEncode(categoryKey));
                query.append("%' ");
                query.append(" ORDER BY localname(cat)");
                if (logger.isDebugEnabled()) {
                    logger.debug(query.toString());
                }
                Query q = session.getWorkspace().getQueryManager().createQuery(
                        query.toString(), Query.JCR_SQL2);
                QueryResult qr = q.execute();
                NodeIterator ni = qr.getNodes();
                while (ni.hasNext()) {
                    Node categoryNode = ni.nextNode();
                    result.add(new Category(
                            createCategoryBeanFromNode(categoryNode)));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Find categories by its title prefix and language anywhere in the tree
     * @param titlePrefix
     * @param language
     * @return list of categories matching the title prefix and langauge
     */
    public List<Category> findCategoriesStartingByTitle(String titlePrefix,
            String language) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Find categories containing term in title and language anywhere in the tree
     * @param title
     * @param language
     * @return list of categories matching the title term and language
     */
    public List<Category> findCategoriesContainingTitleString(String title,
            String language) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Get the category title in all languages. The key in the map is the language code and the value is the title.
     * @param category
     * @return a Map with language code keys and title values
     * @throws JahiaException
     */
    public Map<String, String> getTitlesForCategory(Category category)
            throws JahiaException {
        Map<String, String> result = new HashMap<String, String>();
        try {
            Session session = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE);
            Node categoryNode = session.getNodeByIdentifier(category
                    .getJahiaCategory().getId());
            for (NodeIterator it = categoryNode
                    .getNodes("j:translation*"); it.hasNext();) {
                Node subNode = (Node) it.next();
                String languageCode = subNode.getProperty(
                        Constants.JCR_LANGUAGE).getString();
                String title = null;
                try {
                    title = subNode.getProperty(
                            Constants.JCR_TITLE)
                            .getString();
                } catch (PathNotFoundException ex) {
                    logger.debug(ex.getMessage(), ex);
                }
                result.put(languageCode, title);
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Get the category title in the specified language
     * @param category
     * @param locale
     * @return category title in the specified language
     * @throws JahiaException
     */
    public String getTitleForCategory(Category category, Locale locale)
            throws JahiaException {
        String title = null;
        try {
            Session session = sessionFactory.getCurrentUserSession(Constants.LIVE_WORKSPACE, locale);
            Node categoryNode = session.getNodeByIdentifier(category
                    .getJahiaCategory().getId());
            Property titleProperty = categoryNode
                    .getProperty(Constants.JCR_TITLE);
            if (titleProperty != null) {
                title = titleProperty.getString();
            }
        } catch (PathNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return title;
    }

    /**
     * Set category title in specified language
     * @param category
     * @param locale
     * @param title
     * @throws JahiaException
     */
    public void setTitleForCategory(Category category, Locale locale,
            String title) throws JahiaException {
        try {
            JCRSessionWrapper session = sessionFactory.getCurrentUserSession(null, locale);
            Node categoryNode = session.getNodeByIdentifier(category
                    .getJahiaCategory().getId());
            session.checkout(categoryNode);
            categoryNode.setProperty(Constants.JCR_TITLE, title);
            session.save();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Remove category title for specified language
     * @param category
     * @param locale
     * @throws JahiaException
     */
    public void removeTitleForCategory(Category category, Locale locale)
            throws JahiaException {
        try {
            JCRSessionWrapper session = sessionFactory.getCurrentUserSession(null, locale);
            Node categoryNode = session.getNodeByIdentifier(category
                    .getJahiaCategory().getId());
            session.checkout(categoryNode);
            categoryNode.getProperty(Constants.JCR_TITLE).remove();
            session.save();
        } catch (PathNotFoundException e) {
            logger.debug(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Remove the category from the repository
     * @param categoryBean
     * @throws JahiaException
     */
    public void removeCategory(CategoryBean categoryBean) throws JahiaException {
        try {
            JCRSessionWrapper jcrSessionWrapper = sessionFactory
                    .getCurrentUserSession();
            Node node = null;
            if (categoryBean.getId() == null) {
                List<Category> categories = findCategoryByKey(categoryBean.getKey());
                if (categories.size() == 1) {
                    node = jcrSessionWrapper.getNodeByUUID(categories.get(0).getID());
                } else {
                    String msg = "Category "
                            + categoryBean.getKey()
                            + (categories.size() == 0 ? "not found"
                                    : " exists multiple times and could not be deleted");
                    throw new JahiaException(msg, msg,
                            JahiaException.DATA_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }
            } else {
                node = jcrSessionWrapper.getNodeByUUID(categoryBean.getId());
            }
            if (node != null) {
                if (!node.getParent().isCheckedOut()) {
                    jcrSessionWrapper.checkout(node.getParent());
                }
                node.remove();
                jcrSessionWrapper.save();
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private CategoryBean createCategoryBeanFromNode(Node categoryNode) {
        CategoryBean categoryBean = new JCRCategory(categoryNode);
        return categoryBean;
    }

    /**
     * Find category by property name and value (not supported yet, as properties are dropped and will be replaced by category references)
     * @param propName
     * @param propValue
     * @param user
     * @return
     */
    public List<Category> findCategoriesByPropNameAndValue(String propName,
            String propValue, JahiaUser user) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    /**
     * Remove properties from category (not supported yet, as properties are dropped and will be replaced by category references)
     * @param categoryId
     */
    public void removeProperties(String categoryId) {
        logger.error("method nor supported yet");
    }

    /**
     * Get properties from category (not supported yet, as properties are dropped and will be replaced by category references)
     * @param categoryId
     * @return
     */
    public Properties getProperties(String categoryId) {
        logger.error("method nor supported yet");
        return null;
    }

    /**
     * Set properties (not supported yet, as properties are dropped and will be replaced by category references)
     * @param categoryId
     * @param properties
     */
    public void setProperties(String categoryId, Properties properties) {
        logger.error("method nor supported yet");
    }

    /**
     * Get all child categories from a parent category a user is able to see
     * @param parentCategory
     * @param user
     * @return all child category objects of a parent, which a user can see
     * @throws JahiaException
     */
    public List<Category> getCategoryChildCategories(Category parentCategory, JahiaUser user)
            throws JahiaException {
        List<Category> rootCategories = new ArrayList<Category>();
        try {
            JCRSessionWrapper jcrSessionWrapper = sessionFactory
                    .getCurrentUserSession(Constants.LIVE_WORKSPACE);
            JCRNodeWrapper parentNodeWrapper = getParentNode(parentCategory,
                    jcrSessionWrapper);
            for (NodeIterator iterator = parentNodeWrapper.getNodes(); iterator.hasNext();) {
                JCRNodeWrapper rootCategoryNode = (JCRNodeWrapper) iterator.nextNode();
                if (rootCategoryNode.isNodeType(Constants.JAHIANT_CATEGORY)) {
                    rootCategories.add(new Category(
                            createCategoryBeanFromNode(rootCategoryNode)));
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return rootCategories;
    }

    private JCRNodeWrapper getParentNode(Category parentCategory,
            JCRSessionWrapper jcrSessionWrapper) throws RepositoryException {
        JCRNodeWrapper parentNodeWrapper = null;
        if (parentCategory != null) {
            parentNodeWrapper = (JCRNodeWrapper) ((JCRCategory) parentCategory
                    .getJahiaCategory()).getCategoryNode();
        }
        if (parentNodeWrapper == null) {
            parentNodeWrapper = (JCRNodeWrapper) getCategoriesRoot(jcrSessionWrapper);
        }
        return parentNodeWrapper;
    }
}
