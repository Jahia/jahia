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

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.jahia.exceptions.JahiaException;

/**
 * Title:        Jahia
 * Description:  The purpose of this class is to provide a cross reference
 * system between container and pages, in order to know which pages reference
 * a container. This is necessary since currently this information is defined
 * only by template developpers and is useful for the HTML cache system when a
 * container modification is done. This class allows lookups such as : "which
 * pages access this container via the absolute container reference system?"
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 * @author Serge Huber
 * @version 1.0
 */

public class ContentContainerListsXRefManager {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentContainerListsXRefManager.class);

    private static ContentContainerListsXRefManager theObject = null;

    protected ContentContainerListsXRefManager() {
        logger.debug("Initializing...");
    }

    /**
     * Returns a singleton instance of this service
     * Note : this is a synchronized access method so it might affect performance
     * if a lot of threads must access it simultaneously.
     * @returns the singleton instance.
     */
    public static synchronized ContentContainerListsXRefManager getInstance()
    {
        if (theObject == null) {
            theObject = new ContentContainerListsXRefManager();
        }
        return theObject;
    } // end getInstance

    /**
     * Retrieves a list of pageIDs that contain references to a specific
     * absolute container list.
     * @param containerListID the identifier of the container list
     * @returns a Set of Integers containing pageIDs
     * @throws JahiaException in the case there was a problem retrieving the
     * references from the database.
     */
    public Set getAbsoluteContainerListPageIDs(int containerListID)
        throws JahiaException {
        ContentContainerListKey containerListKey = new ContentContainerListKey(containerListID);

        Set objectRefs = CrossReferenceManager.getInstance().getObjectXRefs(containerListKey);
        Set pageIDs = new TreeSet();
        if (objectRefs == null) {
            return pageIDs;
        }
        Iterator objectRefIter = objectRefs.iterator();
        while (objectRefIter.hasNext()) {
            Object ref = objectRefIter.next();
            if (ref instanceof ObjectKey) {
                ObjectKey refKey = (ObjectKey) ref;
                if (ContentPageKey.PAGE_TYPE.equals(refKey.getType())) {
                    ContentPageKey pageRefKey = (ContentPageKey) refKey;
                    Integer pageID = new Integer(pageRefKey.getPageID());
                    pageIDs.add(pageID);
                } else {
                    logger.debug("Expected page type in cross reference list, ignoring value...  ");
                }
            } else {
                logger.debug("Invalid key object in cross reference list, ignoring... ");
            }
        }
        return pageIDs;
    }

    /**
     * Retrieves a set of container list keys that are used on the page as
     * absolute container list references.
     * @param pageID the identifier of the page
     * @return a Set containing ContainerListKey objects that identify the
     * containers lists that are used on the page.
     * @throws JahiaException in case there was a problem retrieving the
     * references from the database
     */
    public Set getAbsoluteContainerListsFromPageID(int pageID)
        throws JahiaException {

        ContentPageKey pageKey = new ContentPageKey(pageID);

        Set objectRefs = CrossReferenceManager.getInstance().getReverseObjectXRefs(pageKey);
        Set containerListKeys = new TreeSet();
        if (objectRefs == null) {
            return containerListKeys;
        }
        Iterator objectRefIter = objectRefs.iterator();
        while (objectRefIter.hasNext()) {
            Object source = objectRefIter.next();
            if (source instanceof ObjectKey) {
                ObjectKey sourceKey = (ObjectKey) source;
                if (ContentContainerListKey.CONTAINERLIST_TYPE.equals(sourceKey.getType())) {
                    ContentContainerListKey containerListKey = (ContentContainerListKey) sourceKey;
                    containerListKeys.add(containerListKey);
                } else {
                    logger.debug("Expected page type in cross reference list, ignoring value...  ");
                }
            } else {
                logger.debug("Invalid key object in cross reference list, ignoring... ");
            }
        }
        return containerListKeys;

    }

    /**
     * Adds or updates a cross reference between an absolute  container list and
     * a page
     * @param containerListID the identifier of the container list
     * @param referencePageID the page identifier
     * @throws JahiaException in the case there was a problem storing the
     * references in the database
     */
    public void setAbsoluteContainerListPageID(int containerListID,
                                               int referencePageID)
        throws JahiaException {
        ContentContainerListKey containerListKey = new ContentContainerListKey(containerListID);
        ContentPageKey pageKey = new ContentPageKey(referencePageID);
        CrossReferenceManager.getInstance().setObjectXRef(containerListKey, pageKey);
    }

    /**
     * Removes all the cross references for a specified absolute container list
     * @param containerListID the identifier of the container list
     * the containerList
     * @throws JahiaException is thrown in case there was a problem
     * communicating with the database that stores the link persistently
     */
    public void removeAbsoluteContainerList(int containerListID)
        throws JahiaException {
        ContentContainerListKey containerListKey = new ContentContainerListKey(containerListID);
        CrossReferenceManager.getInstance().removeObjectXRefs(containerListKey);
    }

    /**
     * Removes a specific page cross reference for an absolute container list
     * @param containerListID the identifier for the container list addressed
     * absolutely
     * @param referencePageID the page identifier that is referencing the
     * container list absolutely
     * @throws JahiaException is thrown in case there was a problem
     * communicating with the database that stores the link persistently
     */
    public void removeAbsoluteContainerListPageID(int containerListID,
                                                  int referencePageID)
        throws JahiaException {
        ContentContainerListKey containerListKey = new ContentContainerListKey(containerListID);
        ContentPageKey pageKey = new ContentPageKey(referencePageID);
        CrossReferenceManager.getInstance().removeObjectXRef(containerListKey, pageKey);
    }

}