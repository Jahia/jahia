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