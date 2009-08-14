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
package org.jahia.taglibs.template.category;

import java.util.Date;
import java.util.Enumeration;

import javax.jcr.Node;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import org.jahia.data.JahiaData;
import org.jahia.data.beans.CategoryBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * <p>Title: The category tag gives access to a category, it's children
 * objects as well as it's sub-tree.</p>
 * <p>Description: This tag can be used to display information about a
 * specific category, or to build navigation trees for categories, or to
 * display children of a specific category.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 *
 *
 * @jsp:tag name="category" body-content="empty"
 * description="Retrieves a category from the backend and gives access to the category, its children
 * objects as well as its sub-tree.
 *
 * <p><attriInfo>This tag can be used to display information about a
 * specific category, or to build navigation trees for categories, or to
 * display children of a specific category.
 *
 *
 * <p><b>Background :</b> Categories allow users to classify any content object, making it possible to then browse
 * them using a different hierarchy than the page navigation hierarchy.
 *
 * <p>In order to browse a category, this
 * usually involves looking at a specific root category and listing all the objects that are related
 * to the category, such as sub-categories or other objects that are available in that category.
 *
 * <p>An example of classification is a content book list that needs to categorize the books. In this
 * case the content manager would either re-use or input a classification tree and then use it to
 * classify the books. Users of the system can  then browse using the categories to view books that
 * are related to each other, or view books that are in the same categories as the currently viewed book.
 *
 * <p> One of the main open questions is where to start category browsing. We might have different
 * entry points depending on the type of objects we are looking for in the categories.
 * For example a category entry point might be different if we are looking at web applications
 * or content objects, and the same goes for templates, etc...
 *
 *
 * <p><b>Example 1 :</b> Retrieving the root category
 *
 * <p>First, we will show is how to retrieve the root category, which always exists :
 *
 * <p>&lt;content:category key=\"root\" id=\"rootCategory\" subTreeID=\"categoryTree\" /&gt;
 *
 * <p>The above tag is quite powerful. Basically what it does is :
 *
 * <ul>
 * <li>retrieves the root category in the backend
 *
 * <li>stores the corresponding CategoryBean object under the \�rootCategory\� pageContext attribute
 *
 * <li>stores the subtree for the root category in the pageContext attribute categoryTree.
 * The class type for the tree object is <a href='http://java.sun.com/j2se/1.4.2/docs/api/javax/swing/JTree.html' target='tagFrame'>JTree</a>,
 * which may be used with the
 * <a href='../jahia-html/tree.html' target='tagFrame'>&lt;jahiaHtml:tree&gt;</a> tag for displaying.
 *
 * </ul>
 *
 * <p><b>Example 2 :</b>
 * <p>
 * &lt;content:category key=\"&lt;%=selectedCategory.getKey()%&gt;\" id=\"currentCategory\" /&gt;
 *
 * <p><b>Example 3 :</b>
 * <p>
 * &lt;content:category key=\"root\" id=\"rootCategory\" subTreeID=\"categoryTree\" /&gt;
 *
 * </attriInfo>"
 */

@SuppressWarnings("serial")
public class CategoryTag extends AbstractJahiaTag {

    private static final String TREEID_DATE_POSTFIX = "_gentime";

    private String key = null;
    private String name = null;
    private String childCategoriesID = null;
    private String subTreeID;
    private String selectedCategoryKeyName = null;

    /**
     * @jsp:attribute name="id" required="false" rtexprvalue="true"
     * description="the name of the pageContext attribute under which to
     * store a corresponding CategoryBean object
     * <p><attriInfo>
     * </attriInfo>"
     */


    /**
     * Empty constructor. Does nothing.
     */
    public CategoryTag () {
    }

    /**
     * Sets the key of the category that this tag will load. Note that if a
     * name attribute was specified too it will override this setter.
     * @param key a String corresponding to the key of the category to access
     * through this tag.
     *
     * @jsp:attribute name="key" required="false" rtexprvalue="true"
     * description="Sets the key of the category that this tag will load.
     *
     * <p><attriInfo>Note that if a \"name\" attribute was also specified, it will override this setter.
     *
     * <p>If both the \"name\" and \"key \" attributes aren't set, the Root Category is picked by default.
     * </attriInfo>"
     */
    public void setKey (String key) {
        this.key = key;
    }

    /**
     * Sets the name of the pageContext attribute that contains a String containing
     * the key of the category to use with this tag.
     * @param name the name of the pageContext attribute that contains a
     * String containing the key of the category to use with this tag.
     *
     * @jsp:attribute name="name" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute that contains a String containing.
     * the key of the category to use with this tag.
     *
     * <p><attriInfo>Note that if a \"key\" attribute is also specified, this setter takes precedence.
     *
     * <p>If both the \"name\" and \"key \" attributes aren't set, the Root Category is picked by default.
     * </attriInfo>"
     */
    public void setName (String name) {
        this.name = name;
    }

    /**
     * Sets the name of the pageContext attribute to use to store the ArrayList
     * of Category object that represent the child categories of the category
     * loaded by this tag.
     * @param childCategoriesID the name of the pageContext attribute
     *
     * @jsp:attribute name="childCategoriesID" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute to use to store the ArrayList
     * of Category object that represent the child categories of the category
     * loaded by this tag.
     *
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setChildCategoriesID (String childCategoriesID) {
        this.childCategoriesID = childCategoriesID;
    }

    /**
     * Sets the name of the pageContext attribute to use to store the JTree
     * instance that contains the sub-tree of the specified category. Note that
     * this tag will also first try to load a JTree pageContext attribute by
     * that same name, so that operations of tree modifications stored in the
     * session can be preserved. This means that the scope used to store the
     * JTree in the pageContext is a SESSION_SCOPE.
     * @param subTreeID the name of the pageContext attribute
     *
     * @jsp:attribute name="subTreeID" required="false" rtexprvalue="true"
     * description="Sets the name of the pageContext attribute to use to store the JTree
     * instance that contains the sub-tree of the specified category.
     *
     * <p><attriInfo> Note that this tag will also first try to load a JTree pageContext attribute by
     * that same name, so that operations of tree modifications stored in the
     * session can be preserved. This means that the scope used to store the
     * JTree in the pageContext is a SESSION_SCOPE.
     * <p>Note the JTree object is reloaded if a newer version, than the one stored in the session, is detected.
     * </attriInfo>"
     */
    public void setSubTreeID (String subTreeID) {
        this.subTreeID = subTreeID;
    }

    /**
     * The name of a pageContext attribute that contains a category key to
     * select in the current tree.
     * @param selectedCategoryKeyName the name of the page context attribute
     * key that contains a category key to select by default in the tree.
     *
     * @jsp:attribute name="selectedCategoryKeyName" required="false" rtexprvalue="true"
     * description="The name of a pageContext attribute that contains a category key to
     * select by default in the current tree.
     *
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setSelectedCategoryKeyName (String selectedCategoryKeyName) {
        this.selectedCategoryKeyName = selectedCategoryKeyName;
    }

    public int doStartTag ()
        throws JspException {
        Category curCategory = null;
        String categoryKey = null;

        ServletRequest request = pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute(
            "org.jahia.data.JahiaData");
        JahiaUser p = null;
        if (jData != null) {
            p = jData.getProcessingContext().getUser();
        }

        if (name != null) {
            categoryKey = (String) pageContext.getAttribute(name);
        } else if (key != null) {
            categoryKey = key;
        }

        try {
            if (categoryKey != null) {
                curCategory = Category.getCategory(key, p);
                // this can happen if the user has no rights on the starting category                
                if (curCategory == null) {
                    return SKIP_BODY;
                }
            } 
        } catch (JahiaException je) {
            if (categoryKey != null) {
                throw new JspException(
                    "Error while trying to access category with key [" +
                    categoryKey + "]", je);
            } else {
                throw new JspException(
                    "Error while trying to access root category", je);
            }
        }

        JTree tree = null;
        if (subTreeID != null) {
            tree = (JTree) pageContext.findAttribute(subTreeID);
            // here we must test the validity of the tree depending if there
            // were modifications to the categories after this object was
            // created.
            Date genDate = (Date) pageContext.findAttribute(subTreeID + TREEID_DATE_POSTFIX);
            if (tree != null) {
                Date lastCategoryModifDate = Category.getLastModificationDate();
                if ((genDate != null) && (lastCategoryModifDate != null)) {
                    long genTime = genDate.getTime();
                    long lastCategoryModifTime = lastCategoryModifDate.getTime();
                    if (genTime < lastCategoryModifTime) {
                        pageContext.removeAttribute(subTreeID,
                            PageContext.SESSION_SCOPE);
                        pageContext.removeAttribute(subTreeID +
                            TREEID_DATE_POSTFIX, PageContext.SESSION_SCOPE);
                        tree = null;
                    }
                }
            }
        }

        if (tree == null) {
            tree = buildJTree(curCategory, p);
        }

        // now let's handle default selection of a category.
        String selectedCategoryKey = null;

        if (selectedCategoryKeyName != null) {
            selectedCategoryKey = (String) pageContext.findAttribute(
            selectedCategoryKeyName);
        }
        if (selectedCategoryKey != null) {
            if (tree.getSelectionCount() != 0) {
                tree.clearSelection();
            }
            // now we must find the path for the category key we want to
            // select.
            DefaultMutableTreeNode curNode =
                (DefaultMutableTreeNode) tree.getModel().getRoot();
            selectCategoryInTree(tree, selectedCategoryKey, curNode);
        } else {
            // no selection was enforced, we do nothing in case there was
            // a previous selection.
        }

        if (subTreeID != null) {
            pageContext.setAttribute(subTreeID, tree, PageContext.SESSION_SCOPE);
            pageContext.setAttribute(subTreeID + TREEID_DATE_POSTFIX, new Date(), PageContext.SESSION_SCOPE);
        }

        if (childCategoriesID != null) {
            try {
                pageContext.setAttribute(childCategoriesID,
                                         curCategory.getChildCategories(p));
            } catch (JahiaException je) {
                throw new JspException(
                    "Error while retrieving child categories for category [" +
                    curCategory + "]", je);
            }
        }

        if (getId() != null) {
            pageContext.setAttribute(getId(),
                                     new CategoryBean(curCategory, jData.getProcessingContext()));
        }
        return EVAL_BODY_BUFFERED;
    }

    private void selectCategoryInTree (JTree tree, String selectedCategoryKey,
                                       DefaultMutableTreeNode curNode) {
        Category category = (Category) curNode.getUserObject();
        if (category.getKey().equals(selectedCategoryKey)) {
            TreePath selectionPath = new TreePath(curNode.getPath());
            tree.setSelectionPath(selectionPath);
            return;
        }
        Enumeration<?> childNodeEnum = curNode.children();
        while (childNodeEnum.hasMoreElements()) {
            DefaultMutableTreeNode curChildNode = (DefaultMutableTreeNode)
                                                  childNodeEnum.nextElement();
            selectCategoryInTree(tree, selectedCategoryKey, curChildNode);
        }
    }

    // loops through the next elements
    public int doAfterBody ()
        throws JspException {
        // gets the current container list
        return SKIP_BODY;
    }

    public int doEndTag ()
        throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        super.doEndTag();
        key = null;
        name = null;
        childCategoriesID = null;
        return EVAL_PAGE;
    }

    private JTree buildJTree(Category category, JahiaUser p)
            throws JspException {
        JTree tree = null;
        // Root Node
        try {
            DefaultMutableTreeNode top = new DefaultMutableTreeNode(
                    category != null ? category : Category.getCategoriesRoot(p),
                    true);
            DefaultTreeModel treeModel = new DefaultTreeModel(top, true);
            tree = new JTree(treeModel);
            if (category != null) {
                buildCategoryTree(top, category, p);
            } else {
                for (Category rootCategory : Category.getRootCategories(p)) {
                    buildCategoryTree(top, rootCategory, p);
                }
            }

        } catch (JahiaException je) {
            throw new JspException("Error while building category tree for "
                    + category, je);
        }
        return tree;
    }

    private void buildCategoryTree (MutableTreeNode curNode,
                                    Category currentCategory,
                                    JahiaUser p)
        throws JahiaException {
        if ( currentCategory != null ){
            for (Category curChildCategory : currentCategory.getChildCategories(p)) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(
                        curChildCategory);
                curNode.insert(newNode, 0);
                buildCategoryTree(newNode, curChildCategory, p);
            }
        }
    }

    public String getSelectedCategoryKeyName () {
        return selectedCategoryKeyName;
}

}
