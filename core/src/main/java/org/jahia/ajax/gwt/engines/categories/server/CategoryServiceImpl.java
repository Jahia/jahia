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
package org.jahia.ajax.gwt.engines.categories.server;

import org.jahia.ajax.gwt.client.service.category.CategoryService;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.ajax.gwt.aclmanagement.server.ACLHelper;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryNode;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaCategoryTitle;
import org.jahia.ajax.gwt.client.data.category.GWTJahiaNodeProperty;
import org.jahia.services.usermanager.*;
import org.jahia.services.categories.Category;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.ACLResource;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.comparator.NumericStringComparator;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.gui.GuiBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.data.events.JahiaEvent;


import java.util.*;

/**
 * User: ktlili
 * Date: 15 sept. 2008
 * Time: 17:25:18
 */
@SuppressWarnings("serial")
public class CategoryServiceImpl extends JahiaRemoteService implements CategoryService {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(CategoryServiceImpl.class);

    public List<GWTJahiaCategoryNode> lsInit(final String rootKey, final List<GWTJahiaCategoryNode> selectedCategories, final String categoryLocale) throws GWTJahiaServiceException {
        try {
            final JahiaUser currentUser = getRemoteJahiaUser();
            Category rootCategory;
            boolean rootKeySupplied = (rootKey != null && rootKey.trim().length() > 0);
            if (!rootKeySupplied) {
                rootCategory = Category.getRootCategory(currentUser);
            } else {
                rootCategory = Category.getCategory(rootKey, currentUser);
            }

            List<String> selectedPaths = new ArrayList<String>();
            for (GWTJahiaCategoryNode node : selectedCategories) {
                selectedPaths.add(node.getPath());
            }

            List<GWTJahiaCategoryNode> result = new ArrayList<GWTJahiaCategoryNode>(1);

            if ("root".equals(rootCategory.getKey())) { // don't retrieve root category, get its children instead
                final TreeSet<Category> sortedChildrenCategories = new TreeSet<Category>(new NumericStringComparator<Category>());
                sortedChildrenCategories.addAll(rootCategory.getChildCategories());
                for (Category child : sortedChildrenCategories) {
                    List<Category> parents = child.getParentCategories();
                    String parentKey = null;
                    if (parents != null && parents.size() > 0) {
                        parentKey = parents.get(0).getKey();
                    }
                    GWTJahiaCategoryNode rootNode = createGWTJahiaCategoryNode(parentKey, child, false, categoryLocale);
                    // This code is comment due to GXT/GWT stack overflow
                    // addChildrenToCategory(rootNode, selectedPaths, currentUser, categoryLocale);
                    result.add(rootNode);
                }
            } else {
                List<Category> parents = rootCategory.getParentCategories();
                String parentKey = null;
                if (parents != null && parents.size() > 0) {
                    parentKey = parents.get(0).getKey();
                }
                GWTJahiaCategoryNode rootNode = createGWTJahiaCategoryNode(parentKey, rootCategory, false, categoryLocale);
                //  // This code is comment due to GXT/GWT stack overflow 
                // addChildrenToCategory(rootNode, selectedPaths, currentUser, categoryLocale);
                result.add(rootNode);
            }
            return result;
        } catch (Exception e) {
            logger.error(e.toString(), e);
            throw new GWTJahiaServiceException(e.toString());
        }
    }

    /**
     * Add children to categories
     * @param node
     * @param pathsToAdd
     * @param currentUser
     * @param locale
     * @throws JahiaException
     */
    private void addChildrenToCategory(final GWTJahiaCategoryNode node, final List<String> pathsToAdd, final JahiaUser currentUser, final String locale) throws JahiaException {
        Category cat = Category.getCategory(node.getKey(), currentUser);
        final TreeSet<Category> childrenCategories = new TreeSet<Category>(new NumericStringComparator<Category>());
        childrenCategories.addAll(cat.getChildCategories());
        for (Category childCategory : childrenCategories) {
//            if (pathsToAdd.contains(childCategory.getCategoryPath((Principal) null))) {
                GWTJahiaCategoryNode childNode = createGWTJahiaCategoryNode(node.getKey(), childCategory, false, locale);
                addChildrenToCategory(childNode, pathsToAdd, currentUser, locale);
                node.add(childNode);
                childNode.setParent(node);
//            }
        }
    }

    public List<GWTJahiaCategoryNode> ls(GWTJahiaCategoryNode gwtJahiaCategoryNode, String categoryLocale) throws GWTJahiaServiceException {
        List<GWTJahiaCategoryNode> gwtJahiaNodes = new ArrayList<GWTJahiaCategoryNode>();
        try {
            final JahiaUser currentUser = getRemoteJahiaUser();
            logger.debug("Get categories for user " + currentUser);
            final Category parentCategory;
            List<Category> childrenCategories;
            if (gwtJahiaCategoryNode == null) {
                // load root
                parentCategory = Category.getRootCategory(currentUser);
                childrenCategories = new ArrayList<Category>();
                childrenCategories.add(parentCategory);
                //childrenCategories = parentCategory.getChildCategories(currentUser);
            } else {
                logger.debug("load category with key[" + gwtJahiaCategoryNode.getKey() + "].");
                parentCategory = Category.getCategory(gwtJahiaCategoryNode.getKey(), currentUser);
                if (parentCategory == null) {
                    return new ArrayList<GWTJahiaCategoryNode>();
                }
                childrenCategories = parentCategory.getChildCategories(currentUser);
            }
            String parentKey = "";
            if (parentCategory != null) {
                parentKey = parentCategory.getKey();
            }

            // sort categories
            final TreeSet<Category> sortedChildrenCategories = new TreeSet<Category>(new NumericStringComparator<Category>());
            if (childrenCategories != null) {
                sortedChildrenCategories.addAll(childrenCategories);
                Iterator<Category> it = sortedChildrenCategories.iterator();
                // add category children
                for (int i = 0; i < sortedChildrenCategories.size(); i++) {
                    final Category cat = it.next();
                    if (cat != null) {
                        GWTJahiaCategoryNode gwtJahiaNode = createGWTJahiaCategoryNode(parentKey, cat, false, categoryLocale);
                        logger.debug(gwtJahiaNode.getName() + "," + gwtJahiaNode.getKey() + "," + gwtJahiaNode.getPath());
                        gwtJahiaNodes.add(gwtJahiaNode);
                    }
                }
            } else {
                logger.debug("no children categories for current user.");
            }
        } catch (Exception e) {
            logger.error("Unable to load children categories of [" + gwtJahiaCategoryNode + "]due to", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return gwtJahiaNodes;
    }

    public List<GWTJahiaCategoryNode> getCategories(List<String> keys) throws GWTJahiaServiceException {
        List<GWTJahiaCategoryNode> res = new ArrayList<GWTJahiaCategoryNode>();
        for (String key : keys) {
            try {
                key = key.trim();
                Category cat = Category.getCategory(key);
                GWTJahiaCategoryNode gwtJahiaNode = createGWTJahiaCategoryNode("", cat, false, null);
                res.add(gwtJahiaNode);
            } catch (JahiaException e) {
                logger.error("Error retrieveing categories: " + keys
                        + ". Cause: " + e.getMessage(), e);
            }


        }
        return res;
    }


    /**
     * Save properties
     *
     * @param gwtJahiaCategoryNode
     * @param gwtJahiaNodeProperties
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaCategoryNode saveProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> gwtJahiaNodeProperties) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            if (gwtJahiaNodeProperties != null && !gwtJahiaNodeProperties.isEmpty()) {
                final Category category = Category.getCategory(gwtJahiaCategoryNode.getKey(), currentUser);
                if (category != null) {
                    for (GWTJahiaNodeProperty gwtJahiaNodeProperty : gwtJahiaNodeProperties) {
                        String propName = gwtJahiaNodeProperty.getName();
                        if (propName != null) {
                            String propValue = gwtJahiaNodeProperty.getValue();
                            category.setProperty(propName, propValue);
                            logger.debug("add property[" + propName + "," + propValue + "] to category['" + gwtJahiaCategoryNode.getKey() + "']");
                        } else {
                            String message = "There is a property without name.";
                            logger.error(message);
                            throw new GWTJahiaServiceException(message);
                        }

                        // add property to bean
                        gwtJahiaCategoryNode.addCategoryProperties(gwtJahiaNodeProperty);

                    }
                } else {
                    String message = "Category with key['" + gwtJahiaCategoryNode.getKey() + "'] not found";
                    logger.error(message);
                    throw new GWTJahiaServiceException(message);
                }

            } else {
                String message = "Category Properties list is null or empty.";
                throw new GWTJahiaServiceException(message);
            }

        } catch (JahiaException e) {
            logger.error("Unable to save properties for category [" + gwtJahiaCategoryNode + "]due to", e);
        }
        return gwtJahiaCategoryNode;
    }

    /**
     * Remove properties
     *
     * @param gwtJahiaCategoryNode
     * @param gwtJahiaNodeProperties
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaCategoryNode removeProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode, List<GWTJahiaNodeProperty> gwtJahiaNodeProperties) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            if (gwtJahiaNodeProperties != null && !gwtJahiaNodeProperties.isEmpty()) {
                final Category category = Category.getCategory(gwtJahiaCategoryNode.getKey(), currentUser);
                if (category != null) {
                    for (GWTJahiaNodeProperty gwtJahiaNodeProperty : gwtJahiaNodeProperties) {
                        String propName = gwtJahiaNodeProperty.getName();
                        if (propName != null) {
                            category.removeProperty(propName);
                            gwtJahiaCategoryNode.removeCategoryProperties(gwtJahiaNodeProperty);
                        }
                    }
                }
            }

        } catch (JahiaException e) {
            logger.error("Unable to remove properties for category [" + gwtJahiaCategoryNode + "]due to", e);
        }
        return gwtJahiaCategoryNode;
    }

    /**
     * Load properties
     *
     * @param gwtJahiaCategoryNode
     * @return
     * @throws GWTJahiaServiceException
     */
    public GWTJahiaCategoryNode loadProperties(GWTJahiaCategoryNode gwtJahiaCategoryNode) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            final Category category = Category.getCategory(gwtJahiaCategoryNode.getKey(), currentUser);
            return createGWTJahiaCategoryNode(gwtJahiaCategoryNode.getParentKey(), category, true, null);

        } catch (JahiaException e) {
            logger.error("Unable to load properties for category [" + gwtJahiaCategoryNode + "]due to", e);
        }

        return gwtJahiaCategoryNode;
    }

    /**
     * Update category Indo
     *
     * @param gwtJahiaCategoryNode
     * @throws GWTJahiaServiceException
     */
    public void updateCategoryInfo(GWTJahiaCategoryNode gwtJahiaCategoryNode) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            final Category category = Category.getCategory(gwtJahiaCategoryNode.getKey(), currentUser);
            if (ACLResource.checkReadAccess(category, category, currentUser)) {
                updateInfo(gwtJahiaCategoryNode, category);
            } else {
                throw new GWTJahiaServiceException(getLocaleJahiaAdminResource("org.jahia.admin.components.ManageComponents.noRightToEdit.label"));
            }

        } catch (JahiaException e) {
            logger.error("Unable to update category info due to", e);
        }

    }


    /**
     * Paste
     *
     * @param newParentJahiaCategoryNode
     * @param cut
     * @throws GWTJahiaServiceException
     */
    public void paste(List<GWTJahiaCategoryNode> copiedNode, GWTJahiaCategoryNode newParentJahiaCategoryNode, boolean cut) throws GWTJahiaServiceException {
        if (copiedNode == null) {
            logger.debug("Copied node is not set ---> cancle paste action");
            return;
        }
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            if (newParentJahiaCategoryNode != null) {
                Category newParentCat = Category.getCategory(newParentJahiaCategoryNode.getKey(), currentUser);
                for (GWTJahiaCategoryNode categoryNode : copiedNode) {
                    Category categoryToMove = Category.getCategory(categoryNode.getKey(), currentUser);
                    if (categoryToMove != null) {
                        String newParentPath = newParentCat.getCategoryPath(currentUser);
                        List<Category> oldParentCategories = categoryToMove.getParentCategories(currentUser);
                        // paste categories if it's not root and if newParent is not a child of the catgeories to move
                        if ((oldParentCategories != null && !oldParentCategories.isEmpty()) && newParentPath.indexOf(categoryToMove.getCategoryPath(currentUser)) != 0) {
                            if (cut) {
                                // remove from old parent
                                Category parentCat = Category.getCategory(categoryNode.getParentKey(), currentUser);
                                parentCat.removeChildObjectKey(categoryToMove.getObjectKey());
                            }
                            // link to new parent
                            newParentCat.addChildObjectKey(categoryToMove.getObjectKey());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Unable to paste category", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }


    /**
     * Create category
     *
     * @param gwtParentCategory
     * @throws GWTJahiaServiceException
     */
    public void createCategory(GWTJahiaCategoryNode gwtParentCategory, GWTJahiaCategoryNode newCategory) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            // test if category exists
            if (Category.getCategory(newCategory.getKey()) != null) {
                throw new GWTJahiaServiceException(getLocaleJahiaAdminResource("org.jahia.admin.categories.ManageCategories.editCategory.categoryAlreadyExists.label"));
            }

            final Category parentCategory = Category.getCategory(gwtParentCategory.getKey(), currentUser);

            // create category
            Category newCat = Category.createCategory(newCategory.getKey(), parentCategory);

            // update info
            updateInfo(newCategory, newCat);

        } catch (JahiaException e) {
            logger.error("Unable to create child category for [" + gwtParentCategory + "]", e);
        }

    }

    public List<GWTJahiaCategoryNode> searchByTitle(String title, String rootCategroyKey, int limit) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes = new ArrayList<GWTJahiaCategoryNode>();
        try {
            List<Category> categories = Category.getCategoriesContainingStringInTitle(title, null, retrieveParamBean().getLocale().toString(), currentUser);

            for (int i = 0; i < categories.size() && i < limit; i++) {
                Category category = categories.get(i);
                List<Category> parentCategories = category.getParentCategories();
                String parentKey = null;
                if (parentCategories != null && !parentCategories.isEmpty()) {
                    parentKey = parentCategories.get(0).getKey();
                }
                gwtJahiaCategoryNodes.add(createGWTJahiaCategoryNode(parentKey, category, true, null));
            }

        } catch (Exception e) {
            logger.error("Unable to find categories by title due to", e);
        }
        return gwtJahiaCategoryNodes;
    }


    /**
     * Search categories by properties
     *
     * @param gwtJahiaNodeProperty
     * @param limit
     * @return
     * @throws GWTJahiaServiceException
     */
    public List<GWTJahiaCategoryNode> search(GWTJahiaNodeProperty gwtJahiaNodeProperty, int limit) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes = new ArrayList<GWTJahiaCategoryNode>();
        try {
            List<Category> categories = Category.findCategoriesByPropNameAndValue(gwtJahiaNodeProperty.getName(), gwtJahiaNodeProperty.getValue(), currentUser);
            for (int i = 0; i < categories.size(); i++) {
                Category category = categories.get(i);
                List<Category> parentCategories = category.getParentCategories();
                String parentKey = null;
                if (parentCategories != null && !parentCategories.isEmpty()) {
                    parentKey = parentCategories.get(0).getKey();
                }
                gwtJahiaCategoryNodes.add(createGWTJahiaCategoryNode(parentKey, category, true, null));
            }

        } catch (Exception e) {
            logger.error("Unable to find categories by property due to", e);
        }
        return gwtJahiaCategoryNodes;
    }

    /**
     * Delete category
     *
     * @param gwtJahiaCategoryNodes
     * @throws GWTJahiaServiceException
     */
    public void deleteCategory(List<GWTJahiaCategoryNode> gwtJahiaCategoryNodes) throws GWTJahiaServiceException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        try {
            for (GWTJahiaCategoryNode gwtJahiaCategoryNode : gwtJahiaCategoryNodes) {
                final Category category = Category.getCategory(gwtJahiaCategoryNode.getKey(), currentUser);
                recursiveDeleteCategory(category);
            }
        } catch (JahiaException e) {
            logger.error("Unable to delete category[" + gwtJahiaCategoryNodes + "] due to", e);
        }
    }


    /**
     * Set acl of a category
     *
     * @param node
     * @param acl
     */
    public void setACL(GWTJahiaCategoryNode node, GWTJahiaNodeACL acl) {
        try {
            Category cat = Category.getCategory(node.getKey());

            JahiaBaseACL baseACL = cat.getACL();
            final JahiaBaseACL baseACL1 = ACLHelper.saveACL(acl, baseACL, false);
            ServicesRegistry.getInstance().getJahiaEventService().fireSetRights(new JahiaEvent(cat, retrieveParamBean(), baseACL1));
        } catch (JahiaException e) {
            logger.error("Unable to set ACL for category [" + node + "] due to:", e);
        }

    }


    /**
     * Get acl of a category
     *
     * @param gwtJahiaCategoryNode
     * @return
     */
    public GWTJahiaNodeACL getACL(GWTJahiaCategoryNode gwtJahiaCategoryNode) {
        try {
            Category cat = Category.getCategory(gwtJahiaCategoryNode.getKey());

            JahiaBaseACL baseACL = cat.getACL();

            GWTJahiaNodeACL gwtacl = ACLHelper.getGWTJahiaNodeACL(baseACL, cat, cat, false, retrieveParamBean());
            return gwtacl;
        } catch (JahiaException e) {
            logger.error("Error retrieveing ACL for " + gwtJahiaCategoryNode
                    + ". Cause: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Update info
     *
     * @param gwtJahiaCategoryNode
     * @param category
     */
    private void updateInfo(GWTJahiaCategoryNode gwtJahiaCategoryNode, Category category) {
        List<GWTJahiaCategoryTitle> gwtCategoryTitles = gwtJahiaCategoryNode.getCategoryTitles();
        logger.debug("Try to update " + gwtCategoryTitles.size() + "categories");
        for (GWTJahiaCategoryTitle categoryTitle : gwtCategoryTitles) {
            if (categoryTitle.getTitleValue() != null && !categoryTitle.getTitleValue().equalsIgnoreCase("")) {
                category.setTitle(LanguageCodeConverters.languageCodeToLocale(categoryTitle.getLocale()), categoryTitle.getTitleValue());
            }
        }
    }

    /**
     * Delete categories and sub categories
     *
     * @param currentCategory
     * @throws JahiaException
     */
    private void recursiveDeleteCategory(Category currentCategory) throws JahiaException {
        List<Category> childCategories = currentCategory.getChildCategories(null);
        for (Category curChildCategory : childCategories) {
            recursiveDeleteCategory(curChildCategory);
        }

        //  do not remove 'root' category
        List<Category> parentCategories = currentCategory.getParentCategories();
        if (parentCategories == null || parentCategories.size() == 0) {
            return;
        }
        currentCategory.delete();
    }


    /**
     * Create a gwt category node from a category bean
     *
     * @param category
     * @param loadProperties
     * @param categoryLocale
     * @return
     * @throws JahiaException
     */
    private GWTJahiaCategoryNode createGWTJahiaCategoryNode(String parentKey, Category category, boolean loadProperties, String categoryLocale) throws JahiaException {
        final JahiaUser currentUser = getRemoteJahiaUser();
        GWTJahiaCategoryNode gwtJahiaNode = new GWTJahiaCategoryNode();

        Locale engineLocale = getEngineLocale();
        if (categoryLocale != null) {
            engineLocale = LanguageCodeConverters.languageCodeToLocale(categoryLocale);
        }
        String name = category.getTitle(engineLocale);
        String extendedName;
        if (name == null || name.length() == 0) {
            name = "(" + category.getKey() + ")";
            extendedName = "(" + category.getKey() + ")";
        }else{
            extendedName = "(" + category.getKey() + ") "+category.getTitle(engineLocale);
        }
        gwtJahiaNode.setCategoryId("" + category.getJahiaCategory().getId());
        gwtJahiaNode.setParentKey(parentKey);
        gwtJahiaNode.setName(name);
        gwtJahiaNode.setExtendedName(extendedName);
        gwtJahiaNode.setKey(category.getKey());
        gwtJahiaNode.setPath(category.getCategoryPath(getRemoteJahiaUser()));

        // acl
        gwtJahiaNode.setRead(ACLResource.checkReadAccess(category, category, currentUser));
        gwtJahiaNode.setWriteable(ACLResource.checkWriteAccess(category, category, currentUser));
        gwtJahiaNode.setAdmin(ACLResource.checkAdminAccess(category, category, currentUser));

        if (retrieveParamBean() != null) {
            final GuiBean gui = new GuiBean(retrieveParamBean());
            gwtJahiaNode.setACLLink(gui.drawUpdateCategoryUrl(category));
        } else {
            logger.error("Param bean not found");
        }


        // load titles
        List<GWTJahiaCategoryTitle> gwtCategoryTitles = new ArrayList<GWTJahiaCategoryTitle>();
        JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        List<String> allLanguageCodes = listManager.getAllSitesLanguages();
        for (String curLanguageCode : allLanguageCodes) {
            GWTJahiaCategoryTitle gwtCategoryTitle = new GWTJahiaCategoryTitle();
            gwtCategoryTitle.setLocale(curLanguageCode);
            String titleValue = category.getTitle(LanguageCodeConverters.languageCodeToLocale(curLanguageCode));
            if (titleValue == null) {
                titleValue = "";
            }
            gwtCategoryTitle.setTitleValue(titleValue);
            // add it to liste
            gwtCategoryTitles.add(gwtCategoryTitle);
        }
        gwtJahiaNode.setCategoryTitles(gwtCategoryTitles);


        // load  properties
        if (loadProperties) {
            List<GWTJahiaNodeProperty> gwtJahiaNodeProperties = new ArrayList<GWTJahiaNodeProperty>();
            Properties properties = category.getProperties();
            if (properties != null) {
                Enumeration<?> propEnum = properties.propertyNames();
                while (propEnum.hasMoreElements()) {
                    String propName = propEnum.nextElement().toString();
                    String proValue = properties.getProperty(propName);
                    GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
                    gwtJahiaNodeProperty.setName(propName);
                    gwtJahiaNodeProperty.setValue(proValue);
                    gwtJahiaNodeProperties.add(gwtJahiaNodeProperty);
                }
            }
            gwtJahiaNode.setCategoryProperties(gwtJahiaNodeProperties);
        }

        return gwtJahiaNode;
    }
}
