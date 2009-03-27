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

 package org.jahia.content;

import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

/**
 *
 * <p>Title: Abstract Content Tree Visitor </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public abstract class AbstractContentTreeVisitor implements ContentTreeVisitorInterface {

    private int descendingPageLevel = 0;
    private boolean withChildsContent = true;
    private JahiaUser user;
    private EntryLoadRequest loadRequest;
    private String operationMode;

    protected AbstractContentTreeVisitor(JahiaUser user,
                                          EntryLoadRequest loadRequest,
                                          String operationMode){
        this.user = user;
        this.loadRequest = loadRequest;
        this.operationMode = operationMode;
    }

    /**
     * Returns the internal user used to check rights access
     * @return
     */
    public JahiaUser getUser(){
        return this.user;
    }

    /**
     * Set the internal user used to check rights access
     * @param user
     */
    public void setUser(JahiaUser user){
        this.user = user;
    }

    /**
     * Returns the internal EntryLoadRequest used to retrieve's Content Object Childs.
     * @return
     */
    public EntryLoadRequest getEntryLoadRequest(){
        return this.loadRequest;
    }

    /**
     * Set the EntryLoadRequest used to retrieve's Content Object Childs.
     * @param loadRequest
     */
    public void setEntryLoadRequest(EntryLoadRequest loadRequest){
        this.loadRequest = loadRequest;
    }

    /**
     * Returns the internal OperationMode used to retrieve Content Object Childs.
     * @return
     */
    public String getOperationMode(){
        return this.operationMode;
    }

    /**
     * Set the internal OperationMode used to retrieve's Content Object Childs.
     * @param operationMode
     */
    public void setOperationMode(String operationMode){
        this.operationMode = operationMode;
    }

    /**
     * Returns the descending page level from the root page.
     * @return
     */
    public int getDescendingPageLevel(){
        return this.descendingPageLevel;
    }

    /**
     * Set the descending page level.
     */
    public void setDescendingPageLevel(int level){
        this.descendingPageLevel = level;
    }

    /**
     * Return the internal ContentTree used to traverse the Content Tree
     *
     * @return
     */
    public abstract ContentTree getContentTree();

    /**
     * Returns true if the ContentTree should iterate on childs
     *
     * @return
     */
    public boolean withChildsContent(){
        return this.withChildsContent;
    }

    /**
     * Set if the ContentTree should iterate on childs
     *
     *
     * @param value
     * @return
     */
    public void withChildsContent(boolean value){
        this.withChildsContent = value;
    }

    /**
     * Returns a ContentTreeStatus implementation for a given ContentObject
     *
     * @param contentObject
     * @return
     */
    public abstract ContentTreeStatusInterface getContentTreeStatus(ContentObject contentObject,
            int currentPageLevel) throws JahiaException;

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

        return this.getContentTree().getContentChilds(contentObject,user,
                loadRequest,operationMode);
    }

    /**
     * Called before processing the current content object's childs
     *
     * @param contentObject
     * @param contentStatusInterface
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processContentObjectBeforeChilds(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException{
        // do nothing by default
    }

    /**
     * Called after processing the current object's childs when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processContentObjectAfterChilds(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException{
        // do nothing by default
    }

    /**
     * Called after the childs has been processed and the ContentTreeStatus has been
     * set with continueAfterChilds = false. usually, we should simply do nothing with
     * the contentObject, as requested by one of this child.
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void stopProcessContentObjectAfterChilds(
        ContentObject contentObject, int currentPageLevel)
    throws JahiaException{
        // do nothing by default
    }

    /**
     * Process the Last Content page Field on which, we reached the page level limit when
     * descending in subtree.
     *
     * @param contentObject the last content page field on which we reach the page level limit.
     * @param currentPageLevel
     * @throws JahiaException
     */
    public void processLastContentPageField(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException{
        // do nothing by default
    }

}