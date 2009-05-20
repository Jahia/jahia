/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.blogs.actions;

import org.jahia.blogs.model.BlogCategory;

import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;

import org.jahia.services.categories.Category;

import org.jahia.exceptions.JahiaException;

import org.apache.log4j.Logger;

import java.util.*;
import java.security.Principal;

/**
 * Action used to obtain all the categories of a given blog.
 * Compliant with MetaWeblog API's getCategories method.
 *
 * @author Xavier Lawrence
 */
public class GetCategoriesAction extends AbstractAction {

    // log4j logger
    static Logger log = Logger.getLogger(GetCategoriesAction.class);

    private String blogID;
    private boolean meta;

    /** Creates a new instance of getCategories */
    public GetCategoriesAction(String blogID, String userName,
                               String password, boolean meta) {

        super.userName = userName;
        super.password = password;
        this.blogID = blogID;
        this.meta = meta;
    }

    /**
     * Retrieves the categories of the blog.
     *
     * @return A Map containing the categories
     */
    public Object execute() throws JahiaException {

        // Create commmon resources
        super.init();

        ContentPage blogContentPage = super.changePage(Integer.parseInt(blogID));

        // First check that the user is registered to this site.
        super.checkLogin();

        JahiaPage blogPage = blogContentPage.getPage(jParams);

        Category rootCategory = Category.getRootCategory(jParams.getUser());
        List categories = buildCategoryTree(new ArrayList(),
                rootCategory, jParams.getUser());

        Object categoryResult;
        if (meta) {
            categoryResult = new HashMap();
        } else {
            categoryResult = new ArrayList();
        }

        // Contains all the category keys
        Iterator ite = categories.iterator();
        while (ite.hasNext()) {
            Category cat = Category.getCategory((String)ite.next(), jParams.getUser());

            Map categoryDef = new HashMap(3);

            String catDescription = cat.getTitle(jParams.getLocale());
            if (catDescription == null || catDescription.length() < 1) {
                catDescription = cat.getKey();
            }

            if (meta) {
                categoryDef.put(BlogCategory.DESCRIPTION, catDescription);
                categoryDef.put(BlogCategory.HTML_URL, getCategoryURL(blogPage, cat));
                categoryDef.put(BlogCategory.RSS_URL, getCategoryURL(blogPage, cat));
                ((Map)categoryResult).put(cat.getKey(), categoryDef);

            } else {
                categoryDef.put(BlogCategory.MT_CATEGORY_ID, cat.getKey());
                categoryDef.put(BlogCategory.MT_CATEGORY_NAME, catDescription);
                ((List)categoryResult).add(categoryDef);
            }
        }

        return categoryResult;
    }

    /**
     * Build the category tree for a given root category
     * @param categories List of current category
     * @param parentCategory The current category being processed
     *
     * @throws JahiaException If something goes wrong
     */
    protected List buildCategoryTree (List categories,
                                           Category parentCategory, Principal p) throws JahiaException {

        if ( parentCategory != null ){
            List childCategories = parentCategory.getChildCategories(p);
            Iterator childIter = childCategories.iterator();
            while (childIter.hasNext()) {
                Category curChildCategory = (Category) childIter.next();
                categories.add(curChildCategory.getKey());
                buildCategoryTree(categories, curChildCategory, p);
            }
        }
        return categories;
    }

    /**
     * Constructs the URL of a Category
     * @param page The page where the Category should be applied
     * @param category The Category to construct the URL
     * 
     * @return A String representing the URL
     * @throws JahiaException If something goes wrong
     */
    protected String getCategoryURL(JahiaPage page, Category category)
    throws JahiaException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(super.getPageURL(page));
        buffer.append("?category=");
        buffer.append(category.getKey());
        return buffer.toString();
    }
}
