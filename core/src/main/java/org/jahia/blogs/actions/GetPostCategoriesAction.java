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
