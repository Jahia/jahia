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

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jahia.content.ContentContainerKey;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.StateModificationContext;

/**
 * Action used to delete a post from the Jahia content repository. Compliant with
 * Blogger API's and MetaWeblog API's deletePost method.
 *
 * @author Xavier Lawrence
 */
public class DeletePostAction extends AbstractAction {
    
    // log4j logger
    static Logger log = Logger.getLogger(DeletePostAction.class);
    
    private String postID;
    private boolean publish;
    
    /** 
     * Creates a new instance of DeletePostAction (Blogger API)
     */
    public DeletePostAction(String appKey, String postID, String userName,
            String password, boolean publish) {
        
        super.appKey = appKey;
        super.userName = userName;
        super.password = password;
        this.postID = postID;
        this.publish = publish;
    }
    
    /** 
     * Creates a new instance of DeletePostAction (MetaWeblog API)
     */
    public DeletePostAction(String postID, String userName,
            String password, boolean publish) {
        
        super.userName = userName;
        super.password = password;
        this.postID = postID;
        this.publish = publish;
    }
    
    /**
     * Deletes a given post.
     *
     * @return True if the deletion was succesfull
     */
    public Object execute() throws JahiaException {
        
        // Create commmon resources
        super.init();
        
        // Load the Container and check the structure
        final JahiaContainer postContainer = super.getContainer(
                Integer.parseInt(postID), "en");
        
        super.changePage(postContainer.getPageID());

        // First check that the user is registered to this site.
        final JahiaUser user = super.checkLogin();

        if (!postContainer.checkWriteAccess(user)) {
            throw new JahiaException(
                    "You do not have write access to Post: "+postID,
                    "You do not have write access to Container: "+postID,
                    JahiaException.ACL_ERROR,
                    JahiaException.WARNING_SEVERITY);
        }
        
        log.debug("About to delete container: "+postContainer.getID());
        
        // we only need to remove the shared language since this will
        // automatically mark all sub languages for deletion too...
        Set<String> curLanguageCodes = new HashSet<String>();
        curLanguageCodes.add(ContentObject.SHARED_LANGUAGE);
        curLanguageCodes.add(jParams.getLocale().toString());
                
        StateModificationContext stateModifContext =
                new StateModificationContext(new ContentContainerKey(
                postContainer.getID()), curLanguageCodes, true);
        stateModifContext.pushAllLanguages(true);
        
        containerService.markContainerLanguageForDeletion(postContainer.getID(),
                user, ContentObject.SHARED_LANGUAGE, stateModifContext);
        
        if (publish) {
            super.activateContainer(postContainer.getID(), user);
        }
        
        super.flushPageCacheThatDisplayContainer(postContainer);
        
        log.debug("Post: "+postContainer.getID()+ " deleted");
        return Boolean.TRUE;
    }
}
