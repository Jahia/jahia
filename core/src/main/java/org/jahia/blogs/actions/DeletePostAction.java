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
