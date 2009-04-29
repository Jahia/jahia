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
// $Id$
//


package org.jahia.exceptions;


/**
 * This exception is raised when a page object is requested and is not
 * present in the database.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class JahiaPageNotFoundException extends JahiaException
{
    private int mPageID;

    //-------------------------------------------------------------------------
    /** Default constructor
     *
     * @param   pageID  The not-found requested page ID.
     */
    public JahiaPageNotFoundException (int pageID)
    {
        super ("404 Not found - Page:" + pageID,
               "404 error - page ["+pageID+"] could not be found in the database or is no longer accessible.",
               PAGE_ERROR, ERROR_SEVERITY);

        mPageID = pageID;
    }

    //-------------------------------------------------------------------------
    /** Constructor
     *
     * @param   pageIDStr  The not-found requested page ID string.
     */

    public JahiaPageNotFoundException (String pageIDStr)
    {
        super ("404 Not found - Page:" + pageIDStr,
               "404 error - page ["+pageIDStr+"] could not be found in the database or is no longer accessible.",
               PAGE_ERROR, ERROR_SEVERITY);

        try {
            mPageID = Integer.parseInt (pageIDStr);
        }
        catch (NumberFormatException ex) {
            mPageID = -1;
        }
    }

    public JahiaPageNotFoundException (int pageID, String languageCode, String operationMode)
    {
        super ("404 Not found - Page:" + pageID,
               "404 error - page ["+pageID+"] could not be found for language ["+languageCode+"] in ["+operationMode+"] mode .",
               PAGE_ERROR, ERROR_SEVERITY);

        mPageID = pageID;
    }

    //-------------------------------------------------------------------------
    /** Return the ID of the not found page.
     */
    public final int getPageID () {
        return mPageID;
    }

}
