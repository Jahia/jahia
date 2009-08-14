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
 package org.jahia.services.categories;

import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.data.fields.FieldTypes;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.hibernate.manager.JahiaFieldsDataManager;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.categories.jcr.JCRCategoryProvider;
import org.jahia.services.usermanager.JahiaUser;

import java.util.*;

import javax.jcr.Node;

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

    final private static org.apache.log4j.Logger logger = org.apache.log4j.Logger
            .getLogger(CategoryServiceImpl.class);

    private static CategoryServiceImpl singletonInstance;

    private JCRCategoryProvider categoryProvider;
    private JahiaFieldsDataManager fieldsDataManager;
    // we use a cache in a special way. Basically we use it to synchronize
    // last modification dates on all the nodes of the cluster. For sync
    // messages we use the flush() event.
    // Cache that holds the last modification information on all categories
    public static final String CATEGORY_LASTMODIF_STATUS_CACHE = "CategoryLastModifStatusCache";
    private Cache<?, ?> lastModifCache = null;
    private Date lastModifDate = null;

    private static final String CATEGORY_LINKTYPE = "category";
    private static final String CATEGORY_CHILD_PREFIX = "Category_%";

    private CacheService cacheService;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setCategoryProvider(JCRCategoryProvider categoryProvider) {
        this.categoryProvider = categoryProvider;
    }

    protected CategoryServiceImpl() {
    }

    /**
     * Return the unique service instance. If the instance does not exist, a new instance is created.
     * 
     * @return The unique service instance.
     */
    public synchronized static CategoryServiceImpl getInstance() {
        if (singletonInstance == null) {
            singletonInstance = new CategoryServiceImpl();
        }
        return singletonInstance;
    }

    public void start() throws JahiaInitializationException {
        try {
            lastModifCache = cacheService
                    .createCacheInstance(CATEGORY_LASTMODIF_STATUS_CACHE);
            Category.categoryService = this;
        } catch (JahiaException je) {
            logger.error("Error while checking existence of root category", je);
            throw new JahiaInitializationException(
                    "Error while checking existence of root category", je);
        }
    }

    public void stop() {
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
        final List<Object[]> list = fieldsDataManager.findMetadataObjectKeyByFieldValuesAndType(parentCategory.getKey(), FieldTypes.CATEGORY);
        list.addAll(fieldsDataManager.findMetadataObjectKeyByFieldValuesAndType("%$$$"+parentCategory.getKey(), FieldTypes.CATEGORY));
        list.addAll(fieldsDataManager.findMetadataObjectKeyByFieldValuesAndType(parentCategory.getKey()+"$$$%", FieldTypes.CATEGORY));
        list.addAll(fieldsDataManager.findMetadataObjectKeyByFieldValuesAndType("%$$$"+parentCategory.getKey()+"$$$%", FieldTypes.CATEGORY));

        List<ObjectKey> returnList = new ArrayList<ObjectKey>();
        for (int i = 0; i < list.size(); i++) {
            Object[] jahiaFieldsData = list.get(i);
            ContentObjectKey objectKey = null;
            final int metadataOwnerId = ((Integer) jahiaFieldsData[0]).intValue();
            final String metadataOwnerType = (String) jahiaFieldsData[1];
            if(metadataOwnerId >0 && metadataOwnerType.equals(ContentContainerKey.CONTAINER_TYPE))
                objectKey = new ContentContainerKey(metadataOwnerId);
            else if(metadataOwnerId >0 && metadataOwnerType.equals(ContentPageKey.PAGE_TYPE))
                objectKey = new ContentPageKey(metadataOwnerId);
            else if(metadataOwnerId >0 && metadataOwnerType.equals(ContentContainerListKey.CONTAINERLIST_TYPE))
                objectKey = new ContentContainerListKey(metadataOwnerId);
            if(!returnList.contains(objectKey))
                returnList.add(objectKey);
        }
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
        lastModifCache.flush();
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
        lastModifCache.flush();
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
            lastModifCache.flush();
        } else if (childKey instanceof ContentObjectKey) {
            try {
                ContentObject object = (ContentObject) ContentObject
                        .getInstance(childKey);
                JahiaField metadata = object.getMetadataAsJahiaField(
                        "defaultCategory", Jahia.getThreadParamBean());
                if (metadata != null) {
                    String value = metadata.getValue();
                    if (value.length() > 0) {
                        value += JahiaField.MULTIPLE_VALUES_SEP;
                    }
                    value += parentCategory.getKey();
                    metadata.setValue(value);
                    metadata.save(Jahia.getThreadParamBean());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            lastModifCache.flush();
        } else if (childKey instanceof ContentObjectKey) {
            try {
                ContentObject object = (ContentObject) ContentObject
                        .getInstance(childKey);
                JahiaField metadata = object.getMetadataAsJahiaField(
                        "defaultCategory", Jahia.getThreadParamBean());
                if (metadata != null) {
                    String value = metadata.getValue();
                    String newValue = "";
                    StringTokenizer st = new StringTokenizer(value,
                            JahiaField.MULTIPLE_VALUES_SEP);
                    while (st.hasMoreTokens()) {
                        String key = st.nextToken();
                        if (!key.equals(parentCategory.getKey())) {
                            newValue += key + JahiaField.MULTIPLE_VALUES_SEP;
                        }
                    }
                    if (newValue.length() > 0) {
                        newValue = newValue.substring(newValue.length() - 3);
                    }
                    metadata.setValue(newValue);
                    metadata.save(Jahia.getThreadParamBean());
                }
            } catch (Exception e) {
                e.printStackTrace();
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
        } else {
            try {
                ContentObject object = (ContentObject) ContentObject
                        .getInstance(objectKey);
                JahiaField metadata = object.getMetadataAsJahiaField(
                        "defaultCategory", Jahia.getThreadParamBean());
                if (metadata != null) {
                    String value = metadata.getValue();
                    StringTokenizer st = new StringTokenizer(value,
                            JahiaField.MULTIPLE_VALUES_SEP);
                    while (st.hasMoreTokens()) {
                        String key = st.nextToken();
                        List<Category> cat = getCategory(key);
                        if (cat.size() > 0) {
                            categorySet.add(cat.get(0));
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
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

    public Date getLastModificationDate() {
        return lastModifDate;
    }

    public void setLastModificationDate() {
        lastModifCache.flush();
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

    /**
     * This method is called each time the cache flushes its items.
     * 
     * @param cacheName
     *            the name of the cache which flushed its items.
     */
    public void onCacheFlush(String cacheName) {
        if (CATEGORY_LASTMODIF_STATUS_CACHE.equals(cacheName)) {
            lastModifDate = new Date();
        }
    }

    public void onCachePut(String cacheName, Object entryKey, Object entryValue) {
        // do nothing;
    }

    public void setFieldsDataManager(JahiaFieldsDataManager fieldsDataManager) {
        this.fieldsDataManager = fieldsDataManager;
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
