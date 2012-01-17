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

 package org.jahia.services.categories;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.content.CategoryKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * <p>Title: Category object</p>
 * <p>Description: This Category class is used to associate any objects within
 * Jahia, be they Content Objects, Categories or others. It also provides
 * static methods to manipulate Categories in the persistence back-end.
 * This class is self-sufficient for most operations as it uses the category
 * service back-end to perform most of it's work.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class Category extends JahiaObject {

    private static final long serialVersionUID = 3389053914999712807L;

    public static final String PATH_DELIMITER = "/";

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger (Category.class);

    private CategoryBean categoryBean = null;
    protected static CategoryService categoryService;

    public Category (CategoryBean categoryBean) {
        super (new CategoryKey (categoryBean.getId ()));
        this.categoryBean = categoryBean;
    }

    /**
     * New category constructor.
     *
     * @param key            the unique key name for the new category
     * @param parentCategory the parent category for this new category,
     *                       it may be null to create an un-associated category
     *
     * @return the newly created category
     *
     * @throws JahiaException if there was an error while communicating with
     *                        the database
     */
    static public Category createCategory (String key, Category parentCategory)
            throws JahiaException {

        Category category = categoryService.addCategory (key,
                parentCategory);
        return category;
    }

    static public Node getCategoriesRoot ()
            throws JahiaException {
        return getCategoriesRoot(JCRSessionFactory.getInstance().getCurrentUser());
    }

    /**
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     * @return the root category object that corresponds to the start point
     *         of the category tree.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    static public Node getCategoriesRoot (JahiaUser p)
            throws JahiaException {
        Node categoriesRoot = categoryService.
                getCategoriesRoot();
        String uuid = null;
        try {
            uuid = categoriesRoot.getUUID();
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return categoriesRoot;
    }

    /**
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     * @return the root category object that corresponds to the start point
     *         of the category tree.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    static public List<Category> getRootCategories (JahiaUser p)
            throws JahiaException {
        List<Category> rootCategories = categoryService.
                getRootCategories(p);

        return rootCategories;
    }    
    
    /**
     * @param key the key for the category to retrieve
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     *
     * @return the category corresponding to the key if it exists in the
     *         database
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public static Category getCategory (String key, JahiaUser p)
            throws JahiaException {
        Category category = null;
        List<Category> catList = categoryService.getCategory(key);
        if (catList.size() > 0) {
            category = catList.get(0);
        }
        return category;
    }

    public static Category getCategory (String key)
            throws JahiaException {
        return getCategory(key,JCRSessionFactory.getInstance().getCurrentUser());
    }

    public static List<Category> getCategoriesWithKeyPrefix(final String keyPrefix,
                                                  final String rootCategoryKey,
                                                  final JahiaUser p) throws JahiaException {
        final List<Category> tmp = categoryService.getCategoryStartingByKeyPrefix(keyPrefix);
        final List<Category> validCategories;
        if (rootCategoryKey == null) {
            validCategories = null;
        } else {
            validCategories = new ArrayList<Category>();
            getAllChildrenCategoriesFrom(Category.getCategory(rootCategoryKey), p, validCategories);
        }
        final List<Category> result = new ArrayList<Category>();
        if (tmp == null) return result;
        for (final Category cat : tmp) {
            if (p == null) {
                if (rootCategoryKey == null || validCategories.contains(cat)) {
                    result.add(cat);
                }
            }
        }
        return result;
    }

    public static List<Category> getCategoriesWithtitlePrefix(final String titlePrefix,
                                                    final String rootCategoryKey,
                                                    final String languageCode,
                                                    final JahiaUser p)
            throws JahiaException {
        final List<Category> tmp = categoryService.getCategoryStartingByTitlePrefix(titlePrefix, languageCode);
        List<Category> validCategories;
        if (rootCategoryKey == null) {
            validCategories = null;
        } else {
            validCategories = new ArrayList<Category>();
            getAllChildrenCategoriesFrom(Category.getCategory(rootCategoryKey), p, validCategories);
        }
        final List<Category> result = new ArrayList<Category>();
        if (tmp == null) return result;
        for (final Category cat : tmp) {
            if (p == null) {
                if (rootCategoryKey == null || validCategories.contains(cat)) {
                    result.add(cat);
                }
            }
        }
        return result;
    }

    public static List<Category> getCategoriesContainingStringInTitle(final String string,
                                                            final String rootCategoryKey,
                                                            final String languageCode,
                                                            final JahiaUser p)
            throws JahiaException {
        final List<Category> tmp = categoryService.getCategoriesContainingStringInTitle(string, languageCode);
        List<Category> validCategories;
        if (rootCategoryKey == null) {
            validCategories = null;
        } else {
            validCategories = new ArrayList<Category>();
            getAllChildrenCategoriesFrom(Category.getCategory(rootCategoryKey), p, validCategories);
        }
        final List<Category> result = new ArrayList<Category>();
        if (tmp == null) return result;
        for (final Category cat : tmp) {
            if (p == null) {
                if (rootCategoryKey == null || validCategories.contains(cat)) {
                    result.add(cat);
                }
            }
        }
        return result;
    }

    public static void getAllChildrenCategoriesFrom(final Category startCategory, final JahiaUser p, List<Category> result)
            throws JahiaException {
        final List<Category> childCategories = startCategory.getChildCategories(p);
        for (final Category curChildCategory : childCategories) {
            result.add(curChildCategory);
            getAllChildrenCategoriesFrom(curChildCategory, p, result);
        }
    }

    /**
     * @param categoryUUID the category ID for the category to retrieve
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     *
     * @return the category corresponding to the key if it exists in the
     *         database
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    static public Category getCategoryByUUID (String categoryUUID, JahiaUser p)
            throws JahiaException {
        Category category = categoryService.getCategoryByUUID (categoryUUID);
        if (p == null) {
            return category;
        }
        return category;
    }

    /**
     * @param categoryUUID the category ID for the category to retrieve for the current principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     *
     * @return the category corresponding to the key if it exists in the
     *         database
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    static public Category getCategoryByUUID (String categoryUUID)
            throws JahiaException {
        return getCategoryByUUID(categoryUUID,JCRSessionFactory.getInstance().getCurrentUser());
    }

    /**
     * @param categoryPath the category path for the category to retrieve
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     *
     * @return the category corresponding to the key if it exists in the
     *         database
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    static public Category getCategoryByPath (String categoryPath, JahiaUser p)
            throws JahiaException {
        Category category = categoryService.getCategoryByPath(categoryPath);
        if (p == null) {
            return category;
        }
        return category;
    }


    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the ObjectKey passed described.
     *
     * @param objectKey an ObjectKey instance for the object we want to retrieve
     *                  an instance of.
     *
     * @return a Category class instance that corresponds to the given
     * object key.
     */
    static public JahiaObject getChildInstance (ObjectKey objectKey, JahiaUser p) {
        try {
            Category category = categoryService.
                    getCategoryByUUID (objectKey.getIDInType());
            if (p == null) {
                return category;
            }
            return category;
        } catch (JahiaException je) {
            logger.error ("Error while trying to load category from object key " +
                    objectKey.toString (), je);
            return null;
        }
    }

    static public JahiaObject getChildInstance (ObjectKey objectKey) {
        return getChildInstance(objectKey,JCRSessionFactory.getInstance().getCurrentUser());
    }

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
    static public Set<Category> getObjectCategories (ObjectKey objectKey)
            throws JahiaException {
        return categoryService.getObjectCategories (
                objectKey);
    }

    /**
     * Finds categories by specifying property name and
     * property value. These will be used to build LIKE SQL queries to find
     * all the categories that contain strings that match the properties. It
     * is allowed to use "%" characters here to perform partial String matching
     * instead of full String matching.
     *
     * @param propName  a property name that will be used to
     *                  retrieve categories by their ID. Use "%" character for partial matching.
     * @param propValue a partial property value that will be used to
     *                  retrieve categories by their ID. Use "%" character for partial matching.
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     *
     * @return an List containing Integer objects that are the category
     *         IDs that correspond to the matched properties.
     */
    static public List<Category> findCategoriesByPropNameAndValue (String propName,
                                                         String propValue, JahiaUser p) {
        List<Category> foundCategories = categoryService.findCategoriesByPropNameAndValue (propName, propValue, p);

        return foundCategories;
    }

    public static Category getChildInstance (String IDInType, JahiaUser p) {
        try {
            return getCategoryByUUID (IDInType, p);
        } catch (JahiaException je) {
            logger.debug ("Error retrieving container instance for id : " + IDInType, je);
        }
        return null;
    }

    public static Category getChildInstance (String IDInType) {
        return getChildInstance(IDInType,JCRSessionFactory.getInstance().getCurrentUser());
    }

    /**
     * @return the unique key name identifying this category
     */
    public String getKey () {
        if (this.categoryBean == null) {
            return null;
        }
        return categoryBean.getKey ();
    }

    public String getID () {
        if (this.categoryBean == null) {
            return null;
        }
        return categoryBean.getId ();
    }

    protected CategoryBean getCategoryBean () {
        return categoryBean;
    }

    /**
     * @return a CategoryKey that is an instance of an ObjectKey for this
     *         category and may be used for associating with this category
     */
    public ObjectKey getObjectKey () {
        if (categoryBean == null) {
            return null;
        }
        if (categoryBean.getId () == null || categoryBean.getId ().length() == 0) {
            return null;
        }
        return new CategoryKey (categoryBean.getId ());
    }

    /**
     * Retrieves the list of child categories for this category and at the same
     * time checking permissions for the principal.
     * @param p the principal for which to check the permission
     * @return the list contains the Category objects that the principal has
     * the permission to view.
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<Category> getChildCategories (JahiaUser p)
            throws JahiaException {
        return categoryService.getCategoryChildCategories(this, p);
    }

    /**
     * Retrieves the list of child category keys for this category. 
     *
     * @return an List containing Cagegory classes
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<Category> getChildCategories ()
            throws JahiaException {
        return getChildCategories(JCRSessionFactory.getInstance().getCurrentUser());
    }

    /**
     * Retrieves the list of parent categories for this category, checking the
     * rights if a principal is specified.
     * @param p the Principal for which to retrieve the category, checking rights
     * to make sure he has access to it. If this object is null, then no rights
     * check will be performed.
     * @return the list contains Category objects that correspond to the parent
     * categories for this category
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<Category> getParentCategories (JahiaUser p)
            throws JahiaException {
        List<ObjectKey> parentKeys = getParentObjectKeys ();
        List<Category> parentCategories = new ArrayList<Category>();
        Iterator<ObjectKey> parentKeyIter = parentKeys.iterator ();
        while (parentKeyIter.hasNext ()) {
            ObjectKey curKey = parentKeyIter.next ();
            if (curKey instanceof CategoryKey) {
                Category curParentCategory = (Category) Category.getChildInstance (curKey, p);
                parentCategories.add (0, curParentCategory);
                
                parentKeys = curParentCategory.getParentObjectKeys();
                parentKeyIter = parentKeys.iterator ();
            }
        }
        return parentCategories;
    }

    public List<Category> getParentCategories ()
            throws JahiaException {
        return getParentCategories(JCRSessionFactory.getInstance().getCurrentUser());
    }

    /**
     * Retrieves the list of child object keys for this category. This may be
     * a mixture of any type of ObjectKey sub-classes, such as CategoryKey or
     * ContentObjectKey classes
     *
     * @return an List containing ObjectKey classes or sub-classes
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<ObjectKey> getChildObjectKeys ()
            throws JahiaException {
        return categoryService.
                getCategoryChildKeys (this);
    }

    /**
     * Retrieves the list of child category keys for this category.
     *
     * @return an List containing Cagegory classes
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     * @param recursive If true, include recursively childs category of childs  either
     * @return
     * @throws JahiaException
     */
    public List<Category> getChildCategories (boolean recursive)
            throws JahiaException {
        List<Category> childs = categoryService.
                getCategoryChildCategories (this, JCRSessionFactory.getInstance().getCurrentUser());
        if (!recursive){
            return childs;
        } else {
            List<Category> keysList = new ArrayList<Category>();
            for (Category cat : childs){
                List<Category> subChilds = cat.getChildCategories(recursive);
                keysList.addAll(subChilds);
            }
            childs.addAll(keysList);
        }
        return childs;
    }

    /**
     * Retrieves the list of parent object keys for this category. This should
     * normally be Categories, unless there has been some manual associations
     * done.
     * @return an List containing ObjectKey classes or sub-classes
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<ObjectKey> getParentObjectKeys()
            throws JahiaException {
        return categoryService.getCategoryParentKeys(this);
    }



    /**
     * Returns the title in the specified language if it exists, or null if
     * it doesn't exist
     *
     * @param locale the locale which specifies the language for which to
     *               retrieve the title.
     *
     * @return a String containing the category title in the specified language,
     *         or null if it doesn't exist.
     */
    public String getTitle (Locale locale) {
        try {
            return categoryService.
                    getTitleForCategory (this, locale);
        } catch (JahiaException je) {
            logger.error (
                    "Error while trying to retrieve title for category " + this.getObjectKey ()
                    .toString (),
                    je);
            return null;
        }
    }

    /**
     * Returns the title in the specified language if it exists, or null if
     * it doesn't exist
     *
     * @param locale the locale which specifies the language for which to
     *               retrieve the title.
     *
     * @return a String containing the category title in the specified language,
     *         or the default value if null or empty.
     */
    /**
     *
     * @param locale
     * @param defaultValue
     * @return
     */
    public String getTitle (Locale locale, String defaultValue) {
        String result = null;
        try {
            result = categoryService.
                    getTitleForCategory (this, locale);
        } catch (JahiaException je) {
            logger.error (
                    "Error while trying to retrieve title for category " + this.getObjectKey ()
                    .toString (),
                    je);
        }
        if ( result == null || "".equals(result.trim()) ){
            return defaultValue;
        }
        return result;
    }

    /**
     * Sets a title for this category in a given locale
     *
     * @param locale the locale in which we want to set the title
     * @param title  the String containing the title for the specified locale.
     */
    public void setTitle (Locale locale, String title) {
        try {
            categoryService.setTitleForCategory (this,
                    locale, title);
        } catch (JahiaException je) {
            logger.error (
                    "Error while trying to set title " + title + " for category " + this.getObjectKey ()
                    .toString (),
                    je);
        }
    }

    /**
     * Removes a title for this category in a given locale
     *
     * @param locale the locale for which we want to remove the title
     */
    public void removeTitle (Locale locale) {
        try {
            categoryService.removeTitleForCategory (this,
                    locale);
        } catch (JahiaException je) {
            logger.error (
                    "Error while trying to remove title for category " + this.getObjectKey ()
                    .toString (),
                    je);
        }
    }

    /**
     * Deletes this category and all associated data (but does not recursively
     * delete categories !), notably associations with child and parent objects,
     * titles in all languages, and finally the category itself.
     *
     * @throws JahiaException thrown if there was an error while removing any
     *                        of the associations, titles or the category itself from the database.
     * todo ACLs are not deleted here, we must add this
     */
    public void delete ()
            throws JahiaException {
        categoryService.removeCategory (this);
    }

    /**
     * @return the properties for this category
     */
    public Properties getProperties () {
        return categoryService.getProperties(categoryBean.getId());
    }

    /**
     * Sets a whole new set of properties for this category. Warning, all
     * previously existing properties are erased by this method.
     *
     * @param newProperties the full set of properties to set for this
     *                      category.
     */
    public void setProperties (Properties newProperties) {
        categoryService.setProperties(categoryBean.getId(), newProperties);
    }

    /**
     * @param propertyName the name of the property for which to retrieve the
     *                     value
     *
     * @return a value for the property if it exists in this category
     */
    public String getProperty (String propertyName) {
        if (getProperties () != null) {
            return getProperties ().getProperty (propertyName);
        } else {
            return null;
        }
    }

    /**
     * Sets a single property. This will override any previously existing
     * value for this propertyName.
     *
     * @param propertyName  the name of the property to set
     * @param propertyValue the value to set for this property
     */
    public void setProperty (String propertyName, String propertyValue) {
        if (getProperties () != null) {
            getProperties ().setProperty (propertyName, propertyValue);
            setProperties (getProperties());
        } else {
            Properties properties = new Properties ();
            properties.setProperty (propertyName, propertyValue);
            setProperties (properties);
        }
    }

    /**
     * Removes a property. This implementation will directly remove this
     * property from the database backend.
     *
     * @param propertyName the name of the property to remove.
     */
    public void removeProperty (String propertyName) {
        if (getProperties () != null) {
            getProperties ().remove (propertyName);
            setProperties (getProperties());
        }
    }

    public CategoryBean getJahiaCategory() {
        return categoryBean;
    }

    /**
     * Returns the path representation of the form <code>/root/catKey1/catKeyB/thisCategoryKey</code>
     *
     * @return
     * @throws JahiaException
     */
    public String getCategoryPath() throws JahiaException {
        
        return categoryBean.getPath();

        /*

        List<Category> parentCategories = this.getParentCategories(p);
        StringBuilder path = new StringBuilder(32);
        for (Category category : parentCategories) {
            path.append(PATH_DELIMITER).append(category.getKey());
        }
        path.append(PATH_DELIMITER).append(this.getKey());

        return path.toString();*/
    }

    /**
     * The last category node for a category path of the form /root/catKey1/catKeyB
     *
     * @param path
     * @return
     */
    public static Category getLastCategoryNode(String path) throws JahiaException {
        if (path == null || "".equals(path.trim()) || !path.startsWith(PATH_DELIMITER)){
            return null;
        }
        if (path.endsWith(PATH_DELIMITER + "*")){
            path = path.substring(0,path.length()-2);
        } else if (path.endsWith(PATH_DELIMITER)){
            path = path.substring(0,path.length()-1);
        }
        int pos = path.lastIndexOf(PATH_DELIMITER);
        if (pos == -1){
            return null;
        }
        String catKey = path.substring(pos+1);
        return Category.getCategory(catKey);
    }

    /**
     * Returns the category key from the full category path.
     * 
     * @param categoryPath
     *            the full category path
     * @return the category key from the full category path
     */
    public static String getCategoryKey(String categoryPath) {
        return StringUtils.substringAfterLast(categoryPath, PATH_DELIMITER);
    }
    
    /**
     * Returns the path representation of the form <code>/root/catKey1/catKeyB/thisCategoryKey</code>
     *
     * @return
     * @throws JahiaException
     */
    public static String getCategoryPath(String categoryKey) throws JahiaException {
        return getCategory(categoryKey).getCategoryPath();
    }

   @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}