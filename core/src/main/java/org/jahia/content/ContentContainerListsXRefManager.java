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
    public Set<Integer> getAbsoluteContainerListPageIDs(int containerListID)
        throws JahiaException {
        ContentContainerListKey containerListKey = new ContentContainerListKey(containerListID);

        Set<ObjectKey> objectRefs = CrossReferenceManager.getInstance().getObjectXRefs(containerListKey);
        Set<Integer> pageIDs = new TreeSet<Integer>();
        if (objectRefs == null) {
            return pageIDs;
        }
        Iterator<ObjectKey> objectRefIter = objectRefs.iterator();
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
    public Set<ContentContainerListKey> getAbsoluteContainerListsFromPageID(int pageID)
        throws JahiaException {

        ContentPageKey pageKey = new ContentPageKey(pageID);

        Set<ObjectKey> objectRefs = CrossReferenceManager.getInstance().getReverseObjectXRefs(pageKey);
        Set<ContentContainerListKey> containerListKeys = new TreeSet<ContentContainerListKey>();
        if (objectRefs == null) {
            return containerListKeys;
        }
        Iterator<ObjectKey> objectRefIter = objectRefs.iterator();
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