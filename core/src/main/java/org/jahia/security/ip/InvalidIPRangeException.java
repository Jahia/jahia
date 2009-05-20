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
package org.jahia.security.ip;



/**
 * This exception is used when an IP range is invalid.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class InvalidIPRangeException extends Exception
{
    /** Invalid IP range */
    private String mIPRange = null;

    //-------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public InvalidIPRangeException () {
        super ("Invalid IP range.");
    }

    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   range
     *      String representation of the bad IP range.
     */
    public InvalidIPRangeException (String range) {
        super ("Invalid IP range ["+range+"]");
        mIPRange = range;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the invalid IP range.
     *
     * @return
     *      Return the invalid IP range, might be <code>null</code>.
     */
    public String getIPRange () {
        return mIPRange;
    }
}
