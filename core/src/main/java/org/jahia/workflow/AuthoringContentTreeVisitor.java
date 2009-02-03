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

 package org.jahia.workflow;

import java.util.ArrayList;
import java.util.List;

import org.jahia.content.AbstractContentTreeVisitor;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentTree;
import org.jahia.content.ContentTreeStatus;
import org.jahia.content.ContentTreeStatusInterface;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 *
 * <p>Title: A Visitor used to check if a user has any write right ( Authoring )
 *           on any Content Object of a given page</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class AuthoringContentTreeVisitor extends AbstractContentTreeVisitor {

    public static final int READ_RIGHTS     = JahiaBaseACL.READ_RIGHTS;
    public static final int WRITE_RIGHTS    = JahiaBaseACL.WRITE_RIGHTS;
    public static final int ADMIN_RIGHTS    = JahiaBaseACL.ADMIN_RIGHTS;

    protected ContentTree contentTree;
    private int permission;
    private boolean iterateAll = false;

    // List of contents , the user's has authoring rights
    protected List<ObjectKey> contents;


    public static AuthoringContentTreeVisitor getInstance(
                                        ContentObject rootContentObject,
                                        JahiaUser user,
                                        int permission,
                                        boolean iterateAll,
                                        EntryLoadRequest loadRequest,
                                        String operationMode){

        AuthoringContentTreeVisitor instance
                = new AuthoringContentTreeVisitor(rootContentObject,user,
                permission,iterateAll,loadRequest,operationMode);
        return instance;
    }

    /**
     *
     * @param rootContentObject
     * @param user
     * @param permission
     * @param loadRequest
     * @param operationMode
     * @param jParams
     */
    public AuthoringContentTreeVisitor( ContentObject rootContentObject,
                                        JahiaUser user,
                                        int permission,
                                        boolean iterateAll,
                                        EntryLoadRequest loadRequest,
                                        String operationMode){

        super(user,loadRequest,operationMode);

        EntryLoadRequest stagedLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,
                loadRequest.getLocales(),false);
        this.setEntryLoadRequest(stagedLoadRequest);

        contentTree = new ContentTree(rootContentObject);
        this.permission = permission;
        this.iterateAll = iterateAll;
        this.contents = new ArrayList<ObjectKey>();
        this.setDescendingPageLevel(0); // never descending in pages childs.
    }

    /**
     * Start visiting the tree
     *
     */
    public void visite()
    throws JahiaException{
        getContentTree().iterate(this);
    }
    /**
     * Return the internal ContentTree used to traverse the Content Tree
     *
     * @return
     */
    public ContentTree getContentTree(){
        return this.contentTree;
    }

    /**
     * Returns a ContentTreeStatus implementation for a given ContentObject
     *
     * @param contentObject
     * @return
     */
    public ContentTreeStatusInterface getContentTreeStatus(ContentObject contentObject,
            int currentPageLevel)
    throws JahiaException{
        ContentTreeStatus contentTreeStatus = new ContentTreeStatus();
        return (ContentTreeStatusInterface)contentTreeStatus;
    }

    /**
     * process the current content object when traversing the tree
     *
     * @param contentObject
     * @param contentTreeStatus
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processContentObjectBeforeChilds(ContentObject contentObject,
            ContentTreeStatusInterface contentTreeStatus, int currentPageLevel)
    throws JahiaException{
        if (contentObject.checkAccess(this.getUser(),permission,false)){
            this.contents.add(contentObject.getObjectKey());
            if ( !this.iterateAll ){
                // stop iterate further
                contentTreeStatus.setContinueWithNextContentObject(false);
                contentTreeStatus.setContinueWithChilds(false);
            }
        }
    }

    /**
     * Return the array list of ContentObjects the user has access
     * @return
     */
    public List<ObjectKey> getContents(){
        return this.contents;
    }

    /**
     * Returns an array list of childs for a given ContentObject
     *
     * @param contentObject
     * @param currentPageLevel
     * @return
     */
    public List<ContentObject> getChilds(ContentObject contentObject,
                               int currentPageLevel)
    throws JahiaException {

        return super.getChilds(contentObject,currentPageLevel);
    }

}