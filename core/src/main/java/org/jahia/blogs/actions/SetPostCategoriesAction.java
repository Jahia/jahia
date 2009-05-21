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
