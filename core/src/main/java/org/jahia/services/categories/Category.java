/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

 package org.jahia.services.categories;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections.FastArrayList;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jahia.bin.Jahia;
import org.jahia.content.CategoryKey;
import org.jahia.content.ContentObjectKey;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.PropertiesInterface;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaCategoryPropertiesManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaCategory;
import org.jahia.services.acl.ACLResource;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.ParentACLFinder;

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

public class Category extends JahiaObject implements ACLResourceInterface,
        ParentACLFinder, PropertiesInterface {

    private static final long serialVersionUID = 3389053914999712807L;

    public static final String PATH_DELIMITER = "/";

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (Category.class);

    private JahiaCategory categoryBean = null;
    private Properties properties = null;
    private boolean propertiesLoaded = false;
    protected static CategoryService categoryService;

    public Category (JahiaCategory categoryBean) {
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
        JahiaCategory categoryBean = new JahiaCategory ();
        categoryBean.setKey (key);
        JahiaBaseACL newAcl = new JahiaBaseACL ();
        // the category ACLs are root ACLs.
        newAcl.create (0);
        categoryBean.setJahiaAcl(newAcl.getACL());
        Category category = new Category (categoryBean);
        categoryService.addCategory (
                category, parentCategory);
        return category;
    }

    static public Category getRootCategory ()
            throws JahiaException {
        return getRootCategory(Jahia.getThreadParamBean().getUser());
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
    static public Category getRootCategory (Principal p)
            throws JahiaException {
        Category category = categoryService.
                getRootCategory ();
        if (p == null) {
            return category;
        }
        if (ACLResource.checkReadAccess(category, category, p)) {
            return category;
        } else {
            return null;
        }
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
    static public Category getCategory (String key, Principal p)
            throws JahiaException {
        Category category = categoryService.getCategory (
                key);
        if (p == null) {
            return category;
        }
        if (ACLResource.checkReadAccess(category, category, p)) {
            return category;
        } else {
            return null;
        }
    }

    static public Category getCategory (String key)
            throws JahiaException {
        return getCategory(key,Jahia.getThreadParamBean().getUser());
    }

    public static List<Category> getCategoriesWithKeyPrefix(final String keyPrefix,
                                                  final String rootCategoryKey,
                                                  final Principal p) throws JahiaException {
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
            if (p == null || ACLResource.checkReadAccess(cat, cat, p)) {
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
                                                    final Principal p)
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
            if (p == null || ACLResource.checkReadAccess(cat, cat, p)) {
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
                                                            final Principal p)
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
            if (p == null || ACLResource.checkReadAccess(cat, cat, p)) {
                if (rootCategoryKey == null || validCategories.contains(cat)) {
                    result.add(cat);
                }
            }
        }
        return result;
    }

    public static void getAllChildrenCategoriesFrom(final Category startCategory, final Principal p, List<Category> result)
            throws JahiaException {
        final List<Category> childCategories = startCategory.getChildCategories(p);
        for (final Category curChildCategory : childCategories) {
            result.add(curChildCategory);
            getAllChildrenCategoriesFrom(curChildCategory, p, result);
        }
    }

    /**
     * @param categoryID the category ID for the category to retrieve
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
    static public Category getCategory (int categoryID, Principal p)
            throws JahiaException {
        Category category = categoryService.getCategory (categoryID);
        if (p == null) {
            return category;
        }
        if (ACLResource.checkReadAccess(category, category, p)) {
            return category;
        } else {
            return null;
        }
    }

    static public Category getCategory (int categoryID)
            throws JahiaException {
        return getCategory(categoryID,Jahia.getThreadParamBean().getUser());
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
    static public JahiaObject getChildInstance (ObjectKey objectKey, Principal p) {
        try {
            Category category = categoryService.
                    getCategory (objectKey.getIdInType ());
            if (p == null) {
                return category;
            }
            if (ACLResource.checkReadAccess(category, category, p)) {
                return category;
            } else {
                return null;
            }
        } catch (JahiaException je) {
            logger.error ("Error while trying to load category from object key " +
                    objectKey.toString (), je);
            return null;
        }
    }

    static public JahiaObject getChildInstance (ObjectKey objectKey) {
        return getChildInstance(objectKey,Jahia.getThreadParamBean().getUser());
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
                                                         String propValue, Principal p) {
        List<Category> foundCategories = new FastArrayList (53);
        try {
            JahiaCategoryPropertiesManager propertiesManager = (JahiaCategoryPropertiesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaCategoryPropertiesManager.class.getName());
            List<Integer> categoryIDs = propertiesManager.findCategoryIDsByPropNameAndValue (propName, propValue);
            for (Integer curCategoryID : categoryIDs) {
                Category curCategory = getCategory (curCategoryID.intValue (), p);
                if(curCategory!=null ) // User may not have rights
                foundCategories.add (curCategory);
            }
        } catch (JahiaException je) {
            logger.error (
                    "Error while trying to find categories by property name " + propName + "and property value " + propValue,
                    je);
        }
        ((FastArrayList)foundCategories).setFast(true);
        return foundCategories;
    }

    static public List<Category> findCategoriesByPropNameAndValue (String propName,
                                                         String propValue) {
        return findCategoriesByPropNameAndValue(propName, propValue,Jahia.getThreadParamBean().getUser());
    }

    /**
     * Returns the date of last modification of *any* category. This is useful
     * notably to invalidate trees of categories if any category was modified.
     * @return Date the date of the last modification on any category.
     */
    static public Date getLastModificationDate() {
        return categoryService.getLastModificationDate();
    }

    public static Category getChildInstance (String IDInType, Principal p) {
        try {
            return getCategory (Integer.parseInt (IDInType), p);
        } catch (JahiaException je) {
            logger.debug ("Error retrieving container instance for id : " + IDInType, je);
        }
        return null;
    }

    public static Category getChildInstance (String IDInType) {
        return getChildInstance(IDInType,Jahia.getThreadParamBean().getUser());
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
        if (categoryBean.getId () == 0) {
            return null;
        }
        return new CategoryKey (categoryBean.getId ());
    }

    /**
     * Retrieves the list of child categories for this category
     *
     * @return the list contains Category objects that correspond to the child
     *         categories for this category.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    /*
    public List getChildCategories ()
            throws JahiaException {
        return getChildCategories(null);
    }
    */

    /**
     * Retrieves the list of child categories for this category and at the same
     * time checking permissions for the principal.
     * @param p the principal for which to check the permission
     * @return the list contains the Category objects that the principal has
     * the permission to view.
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<Category> getChildCategories (Principal p)
            throws JahiaException {
        List<CategoryKey> childKeys = getChildCategoryKeys();
        List<Category> childCategories = new ArrayList<Category>();
        Iterator<CategoryKey> childKeyIter = childKeys.iterator ();
        while (childKeyIter.hasNext ()) {
            ObjectKey curKey = childKeyIter.next ();
            Category curChildCategory = (Category) Category.getChildInstance (curKey, p);
            if (curChildCategory != null) {
                childCategories.add (curChildCategory);
            }
        }
        return childCategories;
    }

    public List<Category> getChildCategories ()
            throws JahiaException {
        return getChildCategories(Jahia.getThreadParamBean().getUser());
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
    public List<Category> getParentCategories (Principal p)
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
        return getParentCategories(Jahia.getThreadParamBean().getUser());
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
     * @return an List containing CagegoryKey classes
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<CategoryKey> getChildCategoryKeys ()
            throws JahiaException {
        return categoryService.
                getCategoryChildCategories (this);
    }     
    
    /**
     * Retrieves the list of child category keys for this category.
     *
     * @return an List containing CagegoryKey classes
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     * @param recursive If true, include recursively childs category of childs  either
     * @return
     * @throws JahiaException
     */
    public List<CategoryKey> getChildCategoryKeys (boolean recursive)
            throws JahiaException {
        List<CategoryKey> childs = categoryService.
                getCategoryChildCategories (this);
        if (!recursive){
            return childs;
        } else {
            List<CategoryKey> keysList = new ArrayList<CategoryKey>();
            for (CategoryKey catKey : childs){
                Category cat = (Category)Category.getChildInstance(catKey);
                List<CategoryKey> subChilds = cat.getChildCategoryKeys(recursive);
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
     * Retrieves the list of child content objects for this category
     *
     * @return the list contains ContentObject objects that correspond to the
     *         child content objects associated with this category.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database
     */
    public List<JahiaObject> getChildContentObjects ()
            throws JahiaException {
        List<ObjectKey> childKeys = getChildObjectKeys ();
        List<JahiaObject> childContentObjects = new ArrayList<JahiaObject>();
        for (ObjectKey curKey : childKeys) {
            if (curKey instanceof ContentObjectKey) {
                try {
                    JahiaObject curChildCategory = JahiaObject.getInstance (
                            curKey);
                    childContentObjects.add (curChildCategory);
                } catch (ClassNotFoundException cnfe) {
                    logger.error (
                            "Error while loading content object for object key " +
                            curKey, cnfe);
                }
            }
        }
        return childContentObjects;
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
     * Deletes this category and all associated data (but does not recursively
     * delete categories !), notably associations with child and parent objects,
     * titles in all languages, and finally the category itself.
     *
     * @throws JahiaException thrown if there was an error while removing any
     *                        of the associations, titles or the category itself from the database.
     */
    public void delete ()
            throws JahiaException {
        JahiaCategoryPropertiesManager propertiesManager = (JahiaCategoryPropertiesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaCategoryPropertiesManager.class.getName());
        propertiesManager.removeProperties (categoryBean.getId ());
        categoryService.removeCategory (this);
    }

    /**
     * Associates a child object key with this category. This method does
     * nothing if the child object key already exists
     *
     * @param childObjectKey the object key to add as a child of this category
     *
     * @throws JahiaException thrown in case there was a problem communicating
     *                        with the database
     */
    public void addChildObjectKey (ObjectKey childObjectKey)
            throws JahiaException {
        categoryService.addObjectKeyToCategory (this,
                childObjectKey);
    }

    /**
     * Removes a child object key from this category. This method does nothing
     * if the object key was not a child of the category.
     *
     * @param childObjectKey the object key to remove from the child list of
     *                       this category.
     *
     * @throws JahiaException thrown in case there was a problem communicating
     *                        with the database
     */
    public void removeChildObjectKey (ObjectKey childObjectKey)
            throws JahiaException {
        categoryService.removeObjectKeyFromCategory (
                this, childObjectKey);
    }

    /**
     * @return the properties for this category
     */
    public Properties getProperties () {
        if (propertiesLoaded) {
            return properties;
        } else {
            JahiaCategoryPropertiesManager propertiesManager = (JahiaCategoryPropertiesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaCategoryPropertiesManager.class.getName());
            properties = propertiesManager.getProperties(categoryBean.getId());
            if (properties != null && !properties.isEmpty()) {
                propertiesLoaded = true;
            }
            return properties;
        }
    }

    /**
     * Sets a whole new set of properties for this category. Warning, all
     * previously existing properties are erased by this method.
     *
     * @param newProperties the full set of properties to set for this
     *                      category.
     */
    public void setProperties (Properties newProperties) {
        properties = newProperties;
        JahiaCategoryPropertiesManager propertiesManager = (JahiaCategoryPropertiesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaCategoryPropertiesManager.class.getName());
        propertiesManager.setProperties(categoryBean.getId(), properties);
        categoryService.setLastModificationDate();
        propertiesLoaded = true;
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
            setProperties (properties);
        } else {
            properties = new Properties ();
            propertiesLoaded = true;
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
            setProperties (properties);
        }
    }

    public JahiaCategory getJahiaCategory() {
        return categoryBean;
    }

    /**
     * Return the parent category of the category passed in parameter.
     *
     * @param aclResource ACLResourceInterface
     * @return ACLResourceInterface
     */
    public ACLResourceInterface getParent(ACLResourceInterface aclResource) {
        if (!(aclResource instanceof Category)) {
            return null;
        }
        Category category = (Category) aclResource;
        try {
            List<Category> parentCategories = category.getParentCategories(null);
            if (parentCategories == null || parentCategories.isEmpty()) {
                return null;
            }
            return parentCategories.get(0);
        } catch (JahiaException e) {
            return null;
        }
    }

    /**
     * Returns the ACL
     *
     * @return Return the ACL.
     */
    public JahiaBaseACL getACL() {
        int aclID = getAclID();
        if (aclID == -1) {
            return null;
        }
        try {
            return new JahiaBaseACL(aclID);
        } catch (JahiaException je) {
            logger.error("Error while retrieving ACL " + aclID, je);
            return null;
        }
    }

    /**
     * Returns the acl id
     *
     * @return int the acl id. Return -1 if not found
     */
    public int getAclID() {
        if (this.categoryBean == null) {
            return -1;
        }
        return categoryBean.getAclID();
    }

    /**
     * Returns the path representation of the form <code>/root/catKey1/catKeyB/thisCategoryKey</code>
     *
     * @param p
     * @return
     * @throws JahiaException
     */
    public String getCategoryPath(Principal p) throws JahiaException {
        List<Category> parentCategories = this.getParentCategories(p);
        StringBuilder path = new StringBuilder(32);
        for (Category category : parentCategories) {
            path.append(PATH_DELIMITER).append(category.getKey());
        }
        path.append(PATH_DELIMITER).append(this.getKey());

        return path.toString();
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
     * @param p
     * @return
     * @throws JahiaException
     */
    public static String getCategoryPath(String categoryKey) throws JahiaException {
        return getCategory(categoryKey).getCategoryPath((Principal) null);
    }

   @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}