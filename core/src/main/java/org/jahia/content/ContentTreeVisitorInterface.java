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
 * <p>Title: Define the interface for Content Tree Visitor.</p>
 * <p>Description: Define the interface for Content Tree Visitor.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */

public interface ContentTreeVisitorInterface {

    /**
     * Returns the internal user used to check rights access
     * @return
     */
    public abstract JahiaUser getUser();

    /**
     * Set the internal user used to check rights access
     * @param user
     */
    public abstract void setUser(JahiaUser user);

    /**
     * Returns the internal EntryLoadRequest used to retrieve's Content Object Childs.
     * @return
     */
    public abstract EntryLoadRequest getEntryLoadRequest();

    /**
     * Set the EntryLoadRequest used to retrieve's Content Object Childs.
     * @param loadRequest
     */
    public abstract void setEntryLoadRequest(EntryLoadRequest loadRequest);

    /**
     * Returns the internal OperationMode used to retrieve Content Object Childs.
     * @return
     */
    public abstract String getOperationMode();

    /**
     * Set the internal OperationMode used to retrieve's Content Object Childs.
     * @param operationMode
     */
    public abstract void setOperationMode(String operationMode);

    /**
     * Return the internal ContentTree used to traverse the Content Tree
     *
     * @return
     */
    public abstract ContentTree getContentTree();

    /**
     * Returns the descending page level from the root page.
     * @return
     */
    public abstract int getDescendingPageLevel();

    /**
     * Set the descending page level.
     */
    public void setDescendingPageLevel(int level);

    /**
     * Returns true if the ContentTree should iterate on childs
     *
     * @return
     */
    public abstract boolean withChildsContent();

    /**
     * Returns a ContentTreeStatus implementation for a given ContentObject
     *
     * @param contentObject
     * @param currentPageLevel
     * @return
     * @throws JahiaException
     */
    public abstract ContentTreeStatusInterface getContentTreeStatus(ContentObject contentObject,
            int currentPageLevel)
    throws JahiaException;

    /**
     * Returns an array list of childs for a given ContentObject
     *
     * @param contentObject
     * @param currentPageLevel
     * @return
     */
    public abstract List<? extends ContentObject> getChilds(ContentObject contentObject,
                                        int currentPageLevel)
    throws JahiaException;

    /**
     * Called before processing the current content object's childs
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public abstract void processContentObjectBeforeChilds(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException;

    /**
     * Called after processing the current object's childs when traversing the tree
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public abstract void processContentObjectAfterChilds(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException;

    /**
     * Called after the childs has been processed and the ContentTreeStatus has been
     * set with continueAfterChilds = false. usually, we should simply do nothing with
     * the contentObject, as requested by one of this child.
     *
     * @param contentObject
     * @param currentPageLevel
     * @throws JahiaException
     */
    public abstract void stopProcessContentObjectAfterChilds(
        ContentObject contentObject, int currentPageLevel)
    throws JahiaException;

    /**
     * Process the Last Content page Field on which, we reached the page level limit when
     * descending in subtree.
     *
     * @param contentObject the last content page field on which we reach the page level limit.
     * @param currentPageLevel
     * @throws JahiaException
     */
    public abstract void processLastContentPageField(
            ContentObject contentObject, int currentPageLevel)
    throws JahiaException;

}