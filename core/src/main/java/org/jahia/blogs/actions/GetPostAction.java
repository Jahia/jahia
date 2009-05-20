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

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.categories.Category;

/**
 * Action used to get a user's post information from the Jahia content repository.
 * Compliant with Blogger API's and MetaWeblog API's getPost method.
 *
 * @author Xavier Lawrence
 */
public class GetPostAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(GetPostAction.class);
    
    private String postID;
    private boolean meta;
    
    /** 
     * Creates a new instance of GetPostAction (Blogger API)
     */
    public GetPostAction(String appKey, String postID,
            String userName, String password) {
        super.appKey = appKey;
        super.userName = userName;
        super.password = password;
        this.postID = postID;
        meta = false;
    }
    
    /** 
     * Creates a new instance of GetPostAction (MetaWeblog API)
     */
    public GetPostAction(String postID,
            String userName, String password) {
        super.userName = userName;
        super.password = password;
        this.postID = postID;
        meta = true;
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
        Map postInfo;
        if (meta) {
           Set set = Category.getObjectCategories(postContainer.
                   getContentContainer().getObjectKey());
            postInfo = super.createMetaPostInfo(postContainer, set);
        } else {
            postInfo = super.createPostInfo(postContainer);
        }
        
        log.debug("Post info is: "+ postInfo);
        return postInfo;
    } 
}
