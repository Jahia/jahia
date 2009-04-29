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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jahia.content.ContentObject;
import org.jahia.content.ObjectKey;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 *
 * <p>Title: Pool of AuthoringContentTreeVisitor </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class AuthoringContentTreeVisitorPool {

    private int permission;
    private boolean iterateAllVisitors = false;
    private JahiaUser user;
    private EntryLoadRequest loadRequest;
    private String operationMode;

    // Map of visitors
    protected Map<ObjectKey, AuthoringContentTreeVisitor> visitors = new HashMap<ObjectKey, AuthoringContentTreeVisitor>();


    /**
     *
     * @param user
     * @param permission
     * @param loadRequest
     * @param operationMode
     */
    public AuthoringContentTreeVisitorPool( JahiaUser user,
                                            int permission,
                                            boolean iterateAllVisitors,
                                            EntryLoadRequest loadRequest,
                                            String operationMode){

        this.user = user;
        this.permission = permission;
        this.iterateAllVisitors = iterateAllVisitors;
        EntryLoadRequest stagedLoadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE,0,
                loadRequest.getLocales(),false);
        this.loadRequest = stagedLoadRequest;
        this.operationMode = operationMode;
    }

    /**
     * Start processing the pool of visitors
     *
     */
    public void visite()
    throws JahiaException{
        for ( AuthoringContentTreeVisitor visitor : visitors.values() ){
            visitor.visite();
            if ( !this.iterateAllVisitors && visitor.getContents().size()>0 ){
                break;
            }
        }
    }

    /**
     * Add a new visitor to the pool
     */
    public void addVisitor(AuthoringContentTreeVisitor visitor){
        if ( visitor != null ){
            this.visitors.put(visitor.getContentTree().getRootContentObject()
                              .getObjectKey(),visitor);
        }
    }

    /**
     *
     * @param contentObject
     * @param iterateAll
     */
    public void addVisitor(ContentObject contentObject, boolean iterateAll){
        if ( contentObject != null ){
            AuthoringContentTreeVisitor visitor =
                    AuthoringContentTreeVisitor.getInstance(contentObject,user,
                    permission,iterateAll,loadRequest,operationMode);
            this.visitors.put(contentObject.getObjectKey(),visitor);
        }
    }

    /**
     * Remove a visitor from the pool by looking at the object key which
     * should be the key of the root object of the visitor
     */
    public void removeVisitor(ObjectKey objectKey){
        if ( objectKey != null ){
            this.visitors.remove(objectKey);
        }
    }

    /**
     * Return a visitor for a given object key
     *
     * @param objectKey
     * @return
     */
    public AuthoringContentTreeVisitor getVisitor(ObjectKey objectKey){
        if ( objectKey != null ){
           return (AuthoringContentTreeVisitor)this.visitors.get(objectKey);
        }
        return null;
    }

    /**
     * Return true if at least one of the visitor match at least one content object
     *
     * @return
     */
    public boolean hasFoundContent(){
        for ( AuthoringContentTreeVisitor visitor : visitors.values() ){
            if ( visitor.getContents().size()>0 ){
                return true;
            }
        }
        return false;
    }
}