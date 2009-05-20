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
    public List<? extends ContentObject> getChilds(ContentObject contentObject,
                               int currentPageLevel)
    throws JahiaException {

        return super.getChilds(contentObject,currentPageLevel);
    }

}