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
 package org.jahia.services.categories;

import org.jahia.content.CategoryKey;
import org.jahia.content.ObjectKey;
import org.jahia.content.ObjectLink;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.categories.jcr.JCRCategoryProvider;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Node;
import java.util.*;

/**
 * <p>
 * Title: Category management service database implementation
 * </p>
 * <p>
 * Description: Category management service database implementation, allows browsing of categories as well as navigation and manipulating
 * categories and associated objects. This service should not be used directly but rather the Category class should be used to manipulate
 * categories and associations.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2002
 * </p>
 * <p>
 * Company: Jahia Ltd
 * </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class CategoryServiceImpl extends CategoryService {

    private static final String CATEGORY_LINKTYPE = "category";
    private static final String CATEGORY_CHILD_PREFIX = "Category_%";

    private static volatile CategoryServiceImpl singletonInstance;

    private JCRCategoryProvider categoryProvider;

    protected CategoryServiceImpl() {
    }

    /**
     * Return the unique service instance. If the instance does not exist, a new instance is created.
     *
     * @return The unique service instance.
     */
    public static CategoryServiceImpl getInstance() {
        if (singletonInstance == null) {
            synchronized (CategoryServiceImpl.class) {
                if (singletonInstance == null) {
                    singletonInstance = new CategoryServiceImpl();
                }
            }
        }
        return singletonInstance;
    }

    public void setCategoryProvider(JCRCategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    public void start() throws JahiaInitializationException {
        Category.categoryService = this;
    }

    public void stop() {
        // do nothing
    }

    public Node getCategoriesRoot() throws JahiaException {
        return categoryProvider.getCategoriesRoot();
    }

    public List<Category> getRootCategories(JahiaUser user) throws JahiaException {
        return categoryProvider.getRootCategories(user);
    }

    public List<Category> getCategory(String key) throws JahiaException {
        return categoryProvider.findCategoryByKey(key);
    }

    public Category getCategory(String key, Category parentCategory) throws JahiaException {
        return categoryProvider.getCategoryByKey(key, parentCategory);
    }

    public Category getCategoryByUUID(String categoryUUID) throws JahiaException {
        return categoryProvider.getCategoryByUUID(categoryUUID);
    }

    public Category getCategoryByPath(String categoryPath) throws JahiaException {
        return categoryProvider.getCategoryByPath(categoryPath);
    }

    private List<ObjectLink> getCategoryChildLinks(Category parentCategory)
            throws JahiaException {
        return ObjectLink.findByTypeAndLeftObjectKey(CATEGORY_LINKTYPE,
                parentCategory.getObjectKey());
    }

    private List<ObjectLink> getCategoryParentLinks(Category childCategory)
            throws JahiaException {
        return ObjectLink.findByTypeAndRightObjectKey(CATEGORY_LINKTYPE,
                childCategory.getObjectKey());
    }

    public List<Category> getCategoryChildCategories(Category parentCategory, JahiaUser user)
            throws JahiaException {
        return categoryProvider.getCategoryChildCategories(parentCategory, user);
    }

    public List<ObjectKey> getCategoryChildKeys(Category parentCategory)
            throws JahiaException {
        List<ObjectKey> returnList = new ArrayList<ObjectKey>();
        return returnList;
    }

    public List<ObjectKey> getCategoryParentKeys(Category parentCategory)
            throws JahiaException {
        List<ObjectLink> links = getCategoryParentLinks(parentCategory);
        List<ObjectKey> leftObjectKeys = new ArrayList<ObjectKey>();
        for (ObjectLink curLink : links) {
            leftObjectKeys.add(curLink.getLeftObjectKey());
        }
        return leftObjectKeys;
    }

    public Category addCategory(String key, Category parentCategory)
            throws JahiaException {
        Category newCategory = categoryProvider.createCategory(key, parentCategory);
        return newCategory;
    }

    public void removeCategory(Category category) throws JahiaException {
        // first we must remove all the child object associations with the category
        List<ObjectLink> catChilds = getCategoryChildLinks(category);
        for (ObjectLink curLink : catChilds) {
            curLink.remove();
        }
        // first we must remove all the parent object associations with the category
        List<ObjectLink> catParents = getCategoryParentLinks(category);
        for (ObjectLink curLink : catParents) {
            curLink.remove();
        }
        // now we can remove the category
        categoryProvider.removeCategory(category.getJahiaCategory());
    }

    public void addObjectKeyToCategory(Category parentCategory,
            ObjectKey childKey) throws JahiaException {
        if (childKey instanceof CategoryKey) {
            List<ObjectLink> resultList = ObjectLink.findByTypeAndLeftAndRightObjectKeys(
                    CATEGORY_LINKTYPE, parentCategory.getObjectKey(), childKey);
            if (!resultList.isEmpty()) {
                return;
            }

            ObjectLink.createLink(parentCategory.getObjectKey(), childKey,
                    CATEGORY_LINKTYPE, new HashMap<String, String>());
        }
    }

    public void removeObjectKeyFromCategory(Category parentCategory,
            ObjectKey childKey) throws JahiaException {
        if (childKey instanceof CategoryKey) {
            List<ObjectLink> resultList = ObjectLink.findByTypeAndLeftAndRightObjectKeys(
                    CATEGORY_LINKTYPE, parentCategory.getObjectKey(), childKey);
            if (resultList.isEmpty()) {
                // not found, we do nothing...
                return;
            }

            // we now remove all results, but we expect only one...
            for (ObjectLink curLink : resultList) {
                curLink.remove();
            }
        }
    }

    public Set<Category> getObjectCategories(ObjectKey objectKey)
            throws JahiaException {
        Set<Category> categorySet = new HashSet<Category>();
        if (objectKey instanceof CategoryKey) {
            List<ObjectLink> links = ObjectLink.findByTypeAndRightAndLikeLeftObjectKey(
                    CATEGORY_LINKTYPE, objectKey, CATEGORY_CHILD_PREFIX);
            for (ObjectLink curLink : links) {
                CategoryKey curCatKey = (CategoryKey) curLink
                        .getLeftObjectKey();
                Category curCategory = getCategoryByUUID(curCatKey.getIDInType());
                if (curCategory != null) {
                    categorySet.add(curCategory);
                }
            }
        }
        return categorySet;
    }

    public Map<String, String> getTitlesForCategory(Category category)
            throws JahiaException {
        return categoryProvider.getTitlesForCategory(category);
    }

    public String getTitleForCategory(Category category, Locale locale)
            throws JahiaException {
        return categoryProvider.getTitleForCategory(category, locale);
    }

    public void setTitleForCategory(Category category, Locale locale,
            String title) throws JahiaException {
        categoryProvider.setTitleForCategory(category, locale, title);
    }

    public void removeTitleForCategory(Category category, Locale locale)
            throws JahiaException {
        categoryProvider.removeTitleForCategory(category, locale);
    }

    public List<Category> getCategoryStartingByKeyPrefix(final String keyPrefix)
            throws JahiaException {
        return categoryProvider.findCategoriesStartingByKey(keyPrefix);
    }

    public List<Category> getCategoryStartingByTitlePrefix(
            final String titlePrefix, final String languageCode)
            throws JahiaException {
        return categoryProvider.findCategoriesStartingByTitle(titlePrefix, languageCode);
    }

    public List<Category> getCategoriesContainingStringInTitle(
            final String string, final String languageCode)
            throws JahiaException {
        return categoryProvider.findCategoriesContainingTitleString(string, languageCode);
    }

    public List<Category> findCategoriesByPropNameAndValue(String propName, String propValue, JahiaUser user) {
        return categoryProvider.findCategoriesByPropNameAndValue(propName, propValue, user);
    }

    public void removeProperties(String categoryId) {
        categoryProvider.removeProperties(categoryId);
    }

    public Properties getProperties(String categoryId) {
        return categoryProvider.getProperties(categoryId);
    }

    public void setProperties(String categoryId, Properties properties) {
        categoryProvider.setProperties(categoryId, properties);
    }
}
