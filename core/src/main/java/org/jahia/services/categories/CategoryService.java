/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Node;
import java.util.*;

/**
 * <p>Title: Category management service</p>
 * <p>Description: Category management service, allows browsing of categories as
 * well as navigation and manipulating categories and associated objects.
 * This service should not be used directly but rather the Category class
 * should be used to manipulate categories and associations.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public abstract class CategoryService extends JahiaService {

    /**
     * @return the root node object that corresponds to the start point
     *         of the category tree.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract Node getCategoriesRoot ()
            throws JahiaException;

    /**
     * @param user the user name executing the operation
     *  
     * @return the root category objects (namespaces)
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract List<Category> getRootCategories(JahiaUser user)
            throws JahiaException;    
    
    /**
     * @param key the key for the category to retrieve
     *
     * @return the list of categories corresponding to the key if it exists in the
     *         database
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract List<Category> getCategory (String key)
            throws JahiaException;

    /**
     * @param categoryUUID the identifier of the category to retrieve from the
     *                     persistent storage
     *
     * @return the category corresponding to the identifier if it exists in
     *         the database, or null otherwise
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract Category getCategoryByUUID (String categoryUUID)
            throws JahiaException;


    /**
     * @param categoryPath the identifier of the category to retrieve from the
     *                     persistent storage
     *
     * @return the category corresponding to the identifier if it exists in
     *         the database, or null otherwise
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract Category getCategoryByPath (String categoryPath)
            throws JahiaException;

    /**
     * Retrieves the child object keys that are associated with the given
     * parent category. The child objects might be refering to content objects
     * or other objects such as sub-categories.
     *
     * @param parentCategory the category for which to retrieve the child
     *                       object key list
     *
     * @return a list of ObjectKey objects that could be anything including
     *         CategoryKey objects.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract List<ObjectKey> getCategoryChildKeys (Category parentCategory)
            throws JahiaException;
    
    /**
     * Retrieves the child category keys that are associated with the given
     * parent category. 
     *
     * @param parentCategory the category for which to retrieve the sub-categories
     *
     * @return a list of Category objects.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract List<Category> getCategoryChildCategories (Category parentCategory, JahiaUser user)
            throws JahiaException;    

    /**
     * Retrieves the parent object keys that are associated with the given
     * child category. The parent objects should only be other categories,
     * but in rare cases they might be other objects.
     * @param childCategory the category for which to retrieve the parent
     * object key list.
     * @return a list of ObjectKey objects that could be anything including
     *         CategoryKey objects.
     * @throws JahiaException JahiaException thrown if there was a problem
     * communicating with the database
     */
    public abstract List<ObjectKey> getCategoryParentKeys (Category childCategory)
            throws JahiaException;

    /**
     * Add a new category under a specified category. This method performs
     * both the operation of creating a new category and associating it with
     * the parentCategory.
     *
     * @param newCategory    the new category to add
     * @param parentCategory the parent category which will contain as a
     *                       child the newCategory.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract Category addCategory (String key,
                                      Category parentCategory)
            throws JahiaException;

    /**
     * Removes a category and all the associations with it, including all the
     * title translations.
     *
     * @param category the category to be removed
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract void removeCategory (Category category)
            throws JahiaException;

    /**
     * Associate a category with an object key
     *
     * @param parentCategory the category that will be associated with the
     *                       object key
     * @param childKey       the object key that will become a "child" of the
     *                       specified category.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract void addObjectKeyToCategory (Category parentCategory,
                                                 ObjectKey childKey)
            throws JahiaException;

    /**
     * Remove an association between a category and an object key
     *
     * @param parentCategory the category currently involved in this association
     * @param childKey       the category child object that we want to remove from
     *                       this association
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract void removeObjectKeyFromCategory (Category parentCategory,
                                                      ObjectKey childKey)
            throws JahiaException;

    /**
     * Returns a set of categories with which this object is associated.
     *
     * @param objectKey the object key for the object for which to retrieve the
     *                  categories
     *
     * @return a Set of Category objects.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database.
     */
    public abstract Set<Category> getObjectCategories (ObjectKey objectKey)
            throws JahiaException;

    /**
     * Retrieves all titles in all locales for a category. Note that this
     * is a raw retrieval.
     *
     * @param category the category for which to retrieve the title
     *
     * @return a String containing the titles for the category in the specified
     *         locale if found, or null if not found. No resolving is done in the
     *         "parent" locales by this method.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract Map<String, String> getTitlesForCategory (Category category)
            throws JahiaException;

    /**
     * Retrieves a title in a certain locale for a category. Note that this
     * is a raw retrieval, no locale resolution is done by this method.
     *
     * @param category the category for which to retrieve the title
     * @param locale   the locale for which to retrieve the title
     *
     * @return a String containing the title for the category in the specified
     *         locale if found, or null if not found. No resolving is done in the
     *         "parent" locales by this method.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract String getTitleForCategory (Category category,
                                                Locale locale)
            throws JahiaException;

    /**
     * Set a title for a category in a given locale. If the title existed
     * previously it is overwritten, if it didn't exist it is created.
     *
     * @param category the category for which to set the title
     * @param locale   the locale for which to set the title
     * @param title    the title in the given locale
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract void setTitleForCategory (Category category, Locale locale,
                                              String title)
            throws JahiaException;

    /**
     * Remove a title for a category in a given locale. This operation does
     * nothing if the title didn't exist for the specified locale
     *
     * @param category the category for which to remove the title
     * @param locale   the locale in which to remove the title
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public abstract void removeTitleForCategory (Category category,
                                                 Locale locale)
            throws JahiaException;

    /**
     * Retrieves the list of categories which have a categoryKey that starts with a given prefix.
     *
     * @param keyPrefix the key prefix to search for
     * @return the categories corresponding to the key prefix or an empty list if none was found
     * @throws JahiaException thrown if there was a problem communicating with the database
     */
    public abstract List<Category> getCategoryStartingByKeyPrefix(final String keyPrefix) throws JahiaException;

    /**
     * Retrieves the list of categories which have a title that starts with a given prefix.
     *
     * @param titlePrefix  the title prefix to search for
     * @param languageCode the current language
     * @return the categories corresponding to the title prefix or an empty list if none was found
     * @throws JahiaException thrown if there was a problem communicating with the database
     */
    public abstract List<Category> getCategoryStartingByTitlePrefix(final String titlePrefix,
                                                          final String languageCode) throws JahiaException;

    /**
     * Retrieves the list of categories which have a title that contains a given String.
     *
     * @param string       the string to search for
     * @param languageCode the current language
     * @return the categories corresponding to the title prefix or an empty list if none was found
     * @throws JahiaException thrown if there was a problem communicating with the database
     */
    public abstract List<Category> getCategoriesContainingStringInTitle(final String string,
                                                              final String languageCode) throws JahiaException;

    
    public abstract List<Category> findCategoriesByPropNameAndValue(String propName, String propValue, JahiaUser user);

    public abstract void removeProperties(String categoryId);

    public abstract Properties getProperties(String categoryId);

    public abstract void setProperties(String categoryId, Properties properties);
}