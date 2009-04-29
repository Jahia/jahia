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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jahia.blogs.model.BlogCategory;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;


/**
 * Action used to get a post's categories from the Jahia content repository.
 * Compliant with MovableType API's getPostCategories method.
 *
 * @author Xavier Lawrence
 */
public class GetPostCategoriesAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetPostCategoriesAction.class);
    
    private String postID;
    
    /** Creates a new instance of GetPostCategories */
    public GetPostCategoriesAction(String postID, String userName, String passWord) {
        this.postID = postID;
        super.userName = userName;
        super.password = passWord;
    }
    
    /**
     * Retrieves a given post.
     *
     * @return A Map containing the post information
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        // First check that the user is registered to this site.
        super.checkLogin();
        
        // Load the Container and check the structure
        final JahiaContainer postContainer = super.getContainer(
                Integer.parseInt(postID));
        
        if (postContainer == null) {
            throw new JahiaException("Post: "+postID+
                    " does not exist", "Container: "+postID+ " does not exist",
                    JahiaException.ENTRY_NOT_FOUND,
                    JahiaException.WARNING_SEVERITY);
        }
        
        log.debug("Working on container: "+postContainer.getID());
        
        // Prepare the return object
        List cats = this.fromSet(Category.getObjectCategories(postContainer.
                getContentContainer().getObjectKey()));

        log.debug("Post categories are: "+ cats);
        return cats;
    }
    
    /**
     * Transforms a Set of category IDs into a List of IDs
     */
    protected List fromSet(Set categories) {
        
        if (categories == null) return new ArrayList(0);
        
        List cats = new ArrayList(categories.size());
        Iterator ite = categories.iterator();
        while (ite.hasNext()) {
            Map catInfo = new HashMap(3);
            Category cat = (Category)ite.next();
            String catName = cat.getTitle(jParams.getLocale());
            
            if (catName == null || catName.length() < 1) {
                catName = cat.getKey();
            }
            
            catInfo.put(BlogCategory.MT_CATEGORY_ID, cat.getKey());
            catInfo.put(BlogCategory.MT_CATEGORY_NAME, catName);
            catInfo.put(BlogCategory.MT_IS_PRIMARY, Boolean.FALSE);
            
            cats.add(catInfo);
        }
        return cats;
    }
}
