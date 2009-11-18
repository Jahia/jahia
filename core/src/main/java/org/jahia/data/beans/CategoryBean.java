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
package org.jahia.data.beans;

import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.content.comparators.ObjectTypeDispatcherComparator;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;

import java.util.*;

/**
 * <p>Title: A wrapper JavaBean compliant class that uses the current
 * request context to display values back to the output. </p>
 * <p>Description: This class encapsulates a ProcessingContext and a Category class
 * to provide helper methods for template developers to access using Struts
 * or JSTL accessors to beans.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class CategoryBean extends AbstractJahiaObjectBean {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CategoryBean.class);

    private ProcessingContext processingContext;
    private Category category;

    static {
        registerType(Category.class.getName(), CategoryBean.class.getName());
    }

    /**
     * Empty constructor to follow JavaBean compliance rules
     */
    public CategoryBean() {
    }

    /**
     * Constructor for wrapper
     *
     * @param category          the category to wrap
     * @param processingContext the processingContextgContext containing the request context to use
     *                          for this wrapper
     */
    public CategoryBean(Category category, ProcessingContext processingContext) {
        this.category = category;
        this.processingContext = processingContext;
    }

    /**
     * Static instantiator called from the getInstance() method of the
     * AbstractJahiaObjectBean class.
     *
     * @param jahiaObject       the JahiaObject instance to build this wrapper for
     * @param processingContext the ProcessingContext instance representing the request
     *                          context to build this wrapper for.
     * @return an instance of an AbstractJahiaObjectBean descendant corresponding
     *         to the JahiaObject type and request context
     */
    public static AbstractJahiaObjectBean getChildInstance(JahiaObject
            jahiaObject,
                                                           ProcessingContext processingContext) {
        return new CategoryBean((Category) jahiaObject, processingContext);
    }

    /**
     * @return the enclosed ProcessingContext instance
     */
    public ProcessingContext getParamBean() {
        return processingContext;
    }

    /**
     * @return the enclosed Category instance
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Retrieves a sorted list of children, filtered by the type that was
     * specified. The type corresponds to the recognized JahiaObject types
     * that are used in descendant classes of ObjectKeys. Examples of types
     * are : ContentPage, Category, JahiaContainer, JahiaContainerList,
     * ContentField, etc...
     *
     * @param type a String specifying the type by which to filter the
     *             JahiaObject
     * @return an List containing JahiaObjects of the specified types,
     *         and sorted according to the type specific comparators.
     */
    public List<AbstractJahiaObjectBean> getChildsOfType(String type) {
        List<AbstractJahiaObjectBean> filteredChildsOfType = new ArrayList<AbstractJahiaObjectBean>();
        List<JahiaObject> fullChildsOfType;
        try {
            fullChildsOfType = getSortedChildJahiaObjects(type);
        } catch (Exception t) {
            logger.error("Error while retrieving child objects", t);
            return filteredChildsOfType;
        }
        Iterator<JahiaObject> childsIter = fullChildsOfType.iterator();
        /**
         * todo this code is ugly, can we do this a cleaner way ? We would
         * probably need some kind of common interface for checks on content
         * objects to clean it up.
         */
        while (childsIter.hasNext()) {
            JahiaObject curJahiaObject = childsIter.next();
            if (curJahiaObject instanceof ContentObject) {
                ContentObject curContentObject = (ContentObject) curJahiaObject;

                /**
                 * todo this code is ugly, because it mixes the new and the
                 * old backend. This is a problem because it means the old
                 * backend offers functions that the new one doesn't yet !
                 * Plus the other problem is that we hard code the different
                 * content objects here, making it very difficult to add new
                 * ones or remove them. We should probably have on ContentObjects
                 * something similar to the ContentPage.getPage() method which
                 * we use below.
                 */
                boolean acceptObject = false;
                try {
                    if (curContentObject instanceof ContentField) {
                        int fieldID = curContentObject.getID();
                        JahiaField jahiaField = ServicesRegistry.getInstance().
                                getJahiaFieldService().
                                loadField(fieldID,
                                        LoadFlags.ALL, processingContext,
                                        processingContext.getEntryLoadRequest());

                        if (jahiaField != null) {
                            acceptObject = true;
                        }
                    } else if (curContentObject instanceof ContentPage) {
                        ContentPage contentPage = (ContentPage) curContentObject;
                        JahiaPage jahiaPage = contentPage.getPage(processingContext.getEntryLoadRequest(), processingContext.getOperationMode(), processingContext.getUser());
                        if (jahiaPage != null) {
                            acceptObject = true;
                        }
                    }
                } catch (JahiaException je) {
                    // this is not necessarily an error. We might have an
                    // exception telling us we don't have access to the
                    // object because of the mode or something.
                    /*
                    logger.debug(
                        "Error while checking availability of content object " +
                        curContentObject.getObjectKey() +
                        ", not adding to children of category " +
                        getCategory().getKey(), je);
                    */
                    acceptObject = false;
                }

                if (acceptObject) {
                    try {
                        AbstractJahiaObjectBean curObjectBean =
                                AbstractJahiaObjectBean.
                                        getInstance(curContentObject, processingContext);
                        filteredChildsOfType.add(curObjectBean);
                    } catch (ClassNotFoundException cnfe) {
                        logger.error("Class not found while converting category child object to beans", cnfe);
                    }
                }
            }
        }

        return filteredChildsOfType;
    }

    /**
     * Retrieves a sorted list of children, filtered by the type that was
     * specified. The type corresponds to the recognized JahiaObject types
     * that are used in descendant classes of ObjectKeys. Examples of types
     * are : ContentPage, Category, JahiaContainer, JahiaContainerList,
     * ContentField, etc...
     * Note : this method does not do any checks against ACLs or operation
     * modes. Use the getChildsOfType() method if you need these checks (if
     * you're not sure you should probably be suing getChildsOfType).
     *
     * @param type a String specifying the type by which to filter the
     *             JahiaObject
     * @return an List containing JahiaObjects of the specified types,
     *         and sorted according to the type specific comparators.
     */
    public List<AbstractJahiaObjectBean> getChildsOfTypeNoChecks(String type) {
        List<AbstractJahiaObjectBean> childsOfType = new ArrayList<AbstractJahiaObjectBean>();
        try {
            List<JahiaObject> childJahiaObjects = getSortedChildJahiaObjects(type);

            childsOfType = jahiaObjectToBeans(childJahiaObjects);
        } catch (ClassNotFoundException cnfe) {
            logger.error("Error while retrieving category children of type [" +
                    type + "]", cnfe);
        } catch (JahiaException je) {
            logger.error("Error while retrieving category children of type [" +
                    type + "]", je);
        }
        return childsOfType;
    }

    private List<AbstractJahiaObjectBean> jahiaObjectToBeans(List<JahiaObject> childJahiaObjects)
            throws ClassNotFoundException {
        List<AbstractJahiaObjectBean> beanList = new ArrayList<AbstractJahiaObjectBean>();
        Iterator<JahiaObject> sortedJahiaObjectIter = childJahiaObjects.iterator();
        while (sortedJahiaObjectIter.hasNext()) {
            JahiaObject curObject = sortedJahiaObjectIter.
                    next();
            AbstractJahiaObjectBean curObjectBean = AbstractJahiaObjectBean.
                    getInstance(curObject, processingContext);
            beanList.add(curObjectBean);
        }
        return beanList;
    }

    private List<JahiaObject> getSortedChildJahiaObjects(String type)
            throws ClassNotFoundException, JahiaException {
        List<JahiaObject> childJahiaObjects = new ArrayList<JahiaObject>();
        for (ObjectKey curObjectKey : category.getChildObjectKeys()) {
            if (curObjectKey.getType().equals(type)) {
                // we have found a matching type object.
                JahiaObject curObject = JahiaObject.getInstance(
                        curObjectKey);
                childJahiaObjects.add(curObject);
            }
        }
        /**
         * todo for the moment this is hardcoded but we should introduce
         * a builder that uses configuration files to build standard
         * comparators
         */
        ObjectTypeDispatcherComparator comparator = new
                ObjectTypeDispatcherComparator();
        Collections.sort(childJahiaObjects, comparator);
        return childJahiaObjects;
    }

    /**
     * @return an List of CategoryBean objects that are the children of
     *         the current category. May return an empty array if there are no
     *         children but never returns null.
     */
    public List<CategoryBean> getChildCategoryBeans() {
        List<CategoryBean> childCategoryBeans = new ArrayList<CategoryBean>();
        try {
            for (Category curCategory : category.getChildCategories(processingContext.getUser())) {
                CategoryBean curCategoryBean = new CategoryBean(curCategory,
                        processingContext);
                childCategoryBeans.add(curCategoryBean);
            }
        } catch (JahiaException je) {
            logger.error("Error while retrieving child categories of category " +
                    category, je);
        }
        return childCategoryBeans;
    }

    /**
     * @return the title of the category for the current locale accessed through
     *         the ProcessingContext.getLocale() method.
     */
    public String getTitle() {
        final String title = category.getTitle(processingContext.getLocale());
        if (title != null) {
            return title;
        } else {
            return category.getKey();
        }
    }

    /**
     * @return the full set of properties
     */
    public Properties getProperties() {
        return category.getProperties();
    }

    public static Set<CategoryBean> getCategoryBeans(final Set<Category> categories) {
        final Set<CategoryBean> result = new HashSet<CategoryBean>();
        for (Category cat : categories) {
            result.add(new CategoryBean(cat, Jahia.getThreadParamBean()));
        }
        return result;
    }
    
    /**
     * Returns the category key.
     * 
     * @return the category key
     */
    public String getKey() {
        return category.getKey();
    }

}