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
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jahia.blogs.model.BlogCategory;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;

/**
 * Action used to set a post's categories pings into the Jahia content repository.
 * Compliant with MovableType API's setPostCategories method.
 *
 * @author Xavier Lawrence
 */
public class SetPostCategoriesAction extends AbstractAction {
    
        // log4j logger
    static Logger log = Logger.getLogger(SetPostCategoriesAction.class);
    
    private String postID;
    private List categories;
    
    /** Creates a new instance of SetPostCategories */
    public SetPostCategoriesAction(String postID, String userName, 
            String passWord, List categories) {
        this.postID = postID;
        this.categories = categories;
        super.userName = userName;
        super.password = passWord;
    }
    
    /**
     */
    public Object execute() throws JahiaException {
    
         // Create commmon resources
        super.init();
        
        // First check that the user is registered to this site.
        super.checkLogin();
        
        // Load the Container and check the structure
        final JahiaContainer postContainer = super.getContainer(Integer.
                parseInt(postID), jParams.getLocale().toString());
        
        log.debug("Working on container: "+postContainer.getID());
        
        // Prepare the return object ans set the categories for the given post
        List categoryKeys = new ArrayList(categories.size());
        for (int i=0; i<categories.size(); i++) {
            categoryKeys.add(((Map)categories.get(i)).get(
                    BlogCategory.MT_CATEGORY_ID));
        }
        
        super.setCategories(categoryKeys, postContainer);

        log.debug("Post categories are: "+ categories);
        return Boolean.TRUE;
    } 
}
