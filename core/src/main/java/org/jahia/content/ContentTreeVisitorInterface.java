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