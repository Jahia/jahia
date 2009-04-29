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
 * This exception is used when an IP address is invalid.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class InvalidIPNumberException extends Exception
{
    /** Invalid IP number */
    private String mIPNumber = null;


    //-------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public InvalidIPNumberException () {
        super ("Invalid IP Address.");
    }


    //-------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param   number
     *      String representation of the bad IP range.
     */
    public InvalidIPNumberException (String number) {
        super ("Invalid IP Address ["+number+"]");
        mIPNumber = number;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the invalid IP number string representation.
     *
     * @return
     *      Return the invalid IP string representation, might be
     *      <code>null</code>.
     */
    public String getIPNumber () {
        return mIPNumber;
    }
}
