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

import java.util.Set;
import java.util.TreeSet;

import org.jahia.exceptions.JahiaException;

/**
 * Title:        Jahia
 * Description:  The purpose of this class is to provide a cross reference
 * system between pages and pages, in order to know which pages reference
 * a specific page . This is necessary since currently this information is defined
 * only by template developpers and is useful for the HTML cache system when a
 * container modification is done. This class allows lookups such as : "which
 * pages access this page ?"
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 * @author Serge Huber
 * @version 1.0
 */

public class ContentPageXRefManager {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContentPageXRefManager.class);

    private static ContentPageXRefManager theObject = null;

    protected ContentPageXRefManager() {
        logger.debug("Initializing...");
    }

    /**
     * Returns a singleton instance of this service
     * Note : this is a synchronized access method so it might affect performance
     * if a lot of threads must access it simultaneously.
     * @returns the singleton instance.
     */
    public static synchronized ContentPageXRefManager getInstance()
    {
        if (theObject == null) {
            theObject = new ContentPageXRefManager();
        }
        return theObject;
    } // end getInstance

    /**
     * Retrieves a list of pageIDs that contain references to a specific
     * page
     * @param targetPageID the target pageID for which to send xrefs.
     * @returns a Set of Integers containing pageIDs that reference the target
     * page.
     * @throws JahiaException is thrown in case there was a problem
     * communicating with the database that stores the link persistently
     */
    public Set<Integer> getPageIDs(int targetPageID)
        throws JahiaException {

        ContentPageKey pageKey = new ContentPageKey(targetPageID);

        Set<ObjectKey> objectRefs = CrossReferenceManager.getInstance().getObjectXRefs(pageKey);
        Set<Integer> pageIDs = new TreeSet<Integer>();
        if (objectRefs == null) {
            logger.debug("No references found for page " + targetPageID);
            return pageIDs;
        }
        for (ObjectKey ref : objectRefs) {
            ObjectKey refKey = (ObjectKey) ref;
            if (ContentPageKey.PAGE_TYPE.equals(refKey.getType())) {
                ContentPageKey pageRefKey = (ContentPageKey) refKey;
                Integer pageID = new Integer(pageRefKey.getPageID());
                pageIDs.add(pageID);
            } else {
                logger.debug("Expected page type in cross reference list, ignoring value...  ");
            }
        }
        return pageIDs;
    }


    public boolean isPageXRefByPage(int targetPageID, int refPageID) {
        ContentPageKey refPageKey = new ContentPageKey(refPageID);
        ContentPageKey targetPageKey = new ContentPageKey(targetPageID);
        return CrossReferenceManager.getInstance().isXRefExisting(refPageKey, targetPageKey);
    }

    /**
     * Adds or updates a cross reference between an target and
     * a page reference
     * @param targetPageID the target page ID for which to add a xref
     * @param refPageID the page that has the xref to the target page
     * @throws JahiaException is thrown in case there was a problem
     * communicating with the database that stores the link persistently
     */
    public void setPageID(int targetPageID, int refPageID)
    throws JahiaException {
        /*
        JahiaConsole.println("PageXRefManager.setPageID",
                             "Target=" + Integer.toString(targetPageID) +
                             ", refPageID=" + Integer.toString(refPageID));
        */
        //JahiaConsole.println("PageXRefManager","siteName =" + siteName);
        //JahiaConsole.println("PageXRefManager","targetPageID =" + targetPageID);
        //JahiaConsole.println("PageXRefManager","refPageID =" + refPageID);
        ContentPageKey pageKey = new ContentPageKey(targetPageID);
        ContentPageKey refPageKey = new ContentPageKey(refPageID);
        CrossReferenceManager.getInstance().setObjectXRef(pageKey, refPageKey);
    }

    /**
     * Removes all the cross references for a specified page
     * @param targetPageID the target page ID for which to remove the xrefs
     * @throws JahiaException is thrown in case there was a problem
     * communicating with the database that stores the link persistently
     */
    public void removePage(int targetPageID)
        throws JahiaException {
        ContentPageKey pageKey = new ContentPageKey(targetPageID);
        CrossReferenceManager.getInstance().removeObjectXRefs(pageKey);
    }

    public void removeAllPageLinks(int targetPageID)
        throws JahiaException {
        ContentPageKey pageKey = new ContentPageKey(targetPageID);
        CrossReferenceManager.getInstance().removeAllObjectXRefs(pageKey);
    }

    /**
     * Removes a specific page cross reference for a target page
     * @param targetPageID the identifier for the page that is the target of
     * the reference
     * @param referencePageID the page identifier that is referencing the
     * target page
     * @throws JahiaException is thrown in case there was a problem
     * communicating with the database that stores the link persistently
     */
    public void removePagePageID(int targetPageID, int referencePageID)
        throws JahiaException {
        ContentPageKey pageKey = new ContentPageKey(targetPageID);
        ContentPageKey referencePageKey = new ContentPageKey(referencePageID);
        CrossReferenceManager.getInstance().removeObjectXRef(pageKey, referencePageKey);
    }

}
