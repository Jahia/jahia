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

 package org.jahia.engines.categories;

import java.util.*;
import java.security.Principal;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.jahia.content.ObjectKey;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.categories.Category;
import org.jahia.utils.GUITreeTools;
import org.jahia.content.ContentDefinition;

/**
 * <p>Title: Category management sub engine</p> <p>Description: </p> <p>Copyright: Copyright (c)
 * 2002</p> <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class ManageCategories {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageCategories.class);

    private static ManageCategories singletonInstance = null;
    private static final String JSP_FILE =
            "/jsp/jahia/engines/categories/manage_categories.jsp";

    private static final String SELECTEDCATEGORIES_ENGINEMAPKEY = "selectedCategories";
    private static final String DEFAULTCATEGORIES_SESSIONKEYPREFIX = "defaultCategories_";
    private static final String CATEGORYTREE_ENGINEMAPKEY = "categoryTree";
    private static final String FLATCATEGORYLIST_ENGINEMAPKEY = "flatCategoryList";

    private static final String CATEGORYPREFIX_HTMLPARAMETER = "category_";

    private ManageCategories () {
        logger.debug (
                "***** Starting " + ManageCategories.class.getName () + " engine *****");
    }

    /**
     * @return the singleton instance of the object
     */
    public static synchronized ManageCategories getInstance () {
        if (singletonInstance == null) {
            singletonInstance = new ManageCategories ();
        }
        return singletonInstance;
    }

    public boolean handleActions (ProcessingContext jParams, int mode, Map engineMap,
                                  ObjectKey objectKey, ContentDefinition contentDefinition,
                                  boolean useDefaults)
            throws JahiaException, JahiaSessionExpirationException {
        switch (mode) {
            case (JahiaEngine.LOAD_MODE):
                return loadCategories (jParams, engineMap, objectKey, contentDefinition,
                        useDefaults);
            case (JahiaEngine.UPDATE_MODE):
                return updateCategories (jParams, engineMap, objectKey, contentDefinition,
                        useDefaults);
            case (JahiaEngine.SAVE_MODE):
                return saveCategories (jParams, engineMap, objectKey, contentDefinition,
                        useDefaults);
        }
        return false;
    }

    private boolean loadCategories (ProcessingContext jParams, Map engineMap,
                                    ObjectKey objectKey, ContentDefinition contentDefinition,
                                    boolean useDefaults)
            throws JahiaException {

        logger.debug ("Loading categories for object key " + objectKey);

        SessionState session = jParams.getSessionState ();

        List selectedCategories = (List) engineMap.get (
                SELECTEDCATEGORIES_ENGINEMAPKEY);

        JTree tree = null;

        if (jParams.getParameterMap ().containsKey ("guitree")) {
            // we only load the tree from the session if we were doing
            // control operations on it.
            tree = (JTree) engineMap.get (CATEGORYTREE_ENGINEMAPKEY);
        } else {
            engineMap.remove (CATEGORYTREE_ENGINEMAPKEY);
        }
        if (tree == null) {
            // Root Node
            DefaultMutableTreeNode top =
                    new DefaultMutableTreeNode (Category.getRootCategory (jParams.getUser()), true);
            DefaultTreeModel treeModel = new DefaultTreeModel (top, true);
            tree = new JTree (treeModel);
            buildCategoryTree (top, Category.getRootCategory (jParams.getUser()), jParams.getUser());
            engineMap.put (CATEGORYTREE_ENGINEMAPKEY, tree);
            if (selectedCategories == null) {
                selectedCategories = new ArrayList();
                Set existingCategories = Category.getObjectCategories (objectKey);
                Iterator existingCategoriesIter = existingCategories.iterator ();
                while (existingCategoriesIter.hasNext ()) {
                    Category curCategory = (Category) existingCategoriesIter.next ();
                    selectedCategories.add (curCategory.getKey ());
                }
                engineMap.put (SELECTEDCATEGORIES_ENGINEMAPKEY, selectedCategories);
            }
            if ((selectedCategories.size () == 0) && (useDefaults)) {
                // this mechanism allows templates to set the currently
                // selected categories into the session and we can use these
                // values to initialize the selected categories.
                selectedCategories = (List) session.getAttribute (
                        DEFAULTCATEGORIES_SESSIONKEYPREFIX + contentDefinition.getObjectKey ());
                logger.debug (
                        "Looking for default selected categories in session key " +
                        DEFAULTCATEGORIES_SESSIONKEYPREFIX +
                        contentDefinition.getObjectKey ());
                if (selectedCategories != null) {
                    logger.debug (
                            "Found default categories " + selectedCategories +
                            "for session key " +
                            DEFAULTCATEGORIES_SESSIONKEYPREFIX +
                            contentDefinition.getObjectKey ());
                } else {
                    selectedCategories = new ArrayList();
                }
                engineMap.put (SELECTEDCATEGORIES_ENGINEMAPKEY, selectedCategories);
            }
        }

        // first let's make sure all the selected categories have their
        // path expanded.
        expandSelectedCategories (tree,
                (DefaultMutableTreeNode) tree.getModel ().getRoot (),
                selectedCategories);

        // now let's modify any expansions according to the user's request.
        GUITreeTools.updateGUITree (tree, jParams);

        List flatCategoryList = null;
        if (tree != null) {
            DefaultMutableTreeNode rootNode =
                    (DefaultMutableTreeNode) tree.getModel ().getRoot ();
            if (rootNode != null) {
                flatCategoryList = GUITreeTools.getFlatTree (tree, rootNode);
            }
        }

        engineMap.put (FLATCATEGORYLIST_ENGINEMAPKEY, flatCategoryList);

        engineMap.put("fieldsEditCallingEngineName","ManageCategories");
        engineMap.put("ManageCategories"+"."+"fieldForm",ServicesRegistry.getInstance ().
                getJahiaFetcherService ().fetchServlet ((ParamBean)jParams, JSP_FILE));
        return true;
    }

    private boolean updateCategories (ProcessingContext jParams, Map engineMap,
                                      ObjectKey objectKey, ContentDefinition contentDefinition,
                                      boolean useDefaults) {
        logger.debug ("Processing categories update");

        JTree tree = (JTree) engineMap.get (CATEGORYTREE_ENGINEMAPKEY);

        Map parameterMap = jParams.getParameterMap ();
        Iterator paramNameIter = parameterMap.keySet ().iterator ();
        List newSelectedCategories = new ArrayList();
        while (paramNameIter.hasNext ()) {
            String curParamName = (String) paramNameIter.next ();
            if (curParamName.startsWith (CATEGORYPREFIX_HTMLPARAMETER)) {
                String curCategoryName = curParamName.substring (
                        CATEGORYPREFIX_HTMLPARAMETER.length ());
                newSelectedCategories.add (curCategoryName);
                logger.debug ("Submitted category key : " + curCategoryName);
            }
        }

        List oldSelectedCategories = (List) engineMap.get (
                SELECTEDCATEGORIES_ENGINEMAPKEY);
        Iterator oldSelectedIter = oldSelectedCategories.iterator ();
        while (oldSelectedIter.hasNext ()) {
            String curOldSelectedCategoryKey = (String) oldSelectedIter.next ();
            if (newSelectedCategories.contains (curOldSelectedCategoryKey)) {
                // nothing to do here
            } else {
                DefaultMutableTreeNode categoryNode = findNodeByCategoryKey (tree,
                        curOldSelectedCategoryKey);
                if (!tree.isVisible (new TreePath (categoryNode.getPath ()))) {
                    newSelectedCategories.add (curOldSelectedCategoryKey);
                }
            }
        }
        engineMap.put (SELECTEDCATEGORIES_ENGINEMAPKEY, newSelectedCategories);
        return true;
    }

    private boolean saveCategories (ProcessingContext jParams, Map engineMap,
                                    ObjectKey objectKey, ContentDefinition contentDefinition,
                                    boolean useDefaults)
            throws JahiaException {
        logger.debug ("Saving categories selection");
        List selectedCategories = (List) engineMap.get (
                SELECTEDCATEGORIES_ENGINEMAPKEY);

        // selectedCategories will be null here if we never went into the
        // categories tab

        if ((useDefaults) && (selectedCategories == null)) {
            // this mechanism allows templates to set the currently
            // selected categories into the session and we can use these
            // values to initialize the selected categories.
            selectedCategories = (List) jParams.getSessionState ().
                    getAttribute (
                            DEFAULTCATEGORIES_SESSIONKEYPREFIX +
                    contentDefinition.getObjectKey ());
            logger.debug (
                    "Looking for default selected categories in session key " +
                    DEFAULTCATEGORIES_SESSIONKEYPREFIX +
                    contentDefinition.getObjectKey ());
            if (selectedCategories != null) {
                logger.debug ("Found default categories " +
                        selectedCategories + "for session key " +
                        DEFAULTCATEGORIES_SESSIONKEYPREFIX +
                        contentDefinition.getObjectKey ());
            }
        }

        if (selectedCategories == null) {
            return false;
        }
        boolean updated = false;
        // now we must not only add the selected categories for the current
        // object but we must also remove any categories that were not selected
        // because this means the user has removed them.

        // so first let's remove the categories.
        Set existingCategories = Category.getObjectCategories (objectKey);
        Iterator existingCategoriesIter = existingCategories.iterator ();
        while (existingCategoriesIter.hasNext ()) {
            Category curCategory = (Category) existingCategoriesIter.next ();
            if (!selectedCategories.contains (curCategory.getKey ())) {
                // remove association with object from category.
                curCategory.removeChildObjectKey (objectKey);
                updated = true;
            }
        }

        // now we can add the current object key to all the selected
        // categories.
        Iterator selectedCategoriesIter = selectedCategories.iterator ();
        while (selectedCategoriesIter.hasNext ()) {
            String curCategoryKey = (String) selectedCategoriesIter.next ();
            Category curCategory = Category.getCategory (curCategoryKey, jParams.getUser());
            curCategory.addChildObjectKey (objectKey);
            updated = true;
        }

        engineMap.remove (SELECTEDCATEGORIES_ENGINEMAPKEY);

        return updated;
    }

    private void buildCategoryTree (MutableTreeNode curNode, Category currentCategory, Principal p)
            throws JahiaException {
        List childCategories = currentCategory.getChildCategories (p);
        Iterator childIter = childCategories.iterator ();
        while (childIter.hasNext ()) {
            Category curChildCategory = (Category) childIter.next ();
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode (curChildCategory);
            curNode.insert (newNode, 0);
            buildCategoryTree (newNode, curChildCategory, p);
        }
    }

    private void expandSelectedCategories (JTree tree, DefaultMutableTreeNode node,
                                           List selectedCategories) {
        Category curCategory = (Category) node.getUserObject ();
        if (selectedCategories.contains (curCategory.getKey ())) {
            tree.expandPath (new TreePath (node.getPath ()));
        }
        Iterator children = new EnumerationIterator(node.children ());
        DefaultMutableTreeNode childNode = null;
        while (children.hasNext ()) {
            childNode = (DefaultMutableTreeNode) children.next ();
            expandSelectedCategories (tree, childNode, selectedCategories);
        }
    }

    private DefaultMutableTreeNode findNodeByCategoryKey (JTree tree, String categoryKey) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel ().getRoot ();
        Iterator depthNodeEnum = new EnumerationIterator(root.depthFirstEnumeration ());
        while (depthNodeEnum.hasNext ()) {
            DefaultMutableTreeNode curNode = (DefaultMutableTreeNode) depthNodeEnum.next ();
            Category curCategory = (Category) curNode.getUserObject ();
            if (curCategory.getKey ().equals (categoryKey)) {
                return curNode;
            }
        }
        return null;
    }

}
