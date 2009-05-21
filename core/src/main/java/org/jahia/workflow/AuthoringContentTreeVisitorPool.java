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