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

package org.jahia.security.license;



/**
 * This exception is thrown when the license file is not valid.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class InvalidLicenseException extends Exception
{

    //-------------------------------------------------------------------------
    public InvalidLicenseException (String msg)
    {
        super (msg + "\n");
    }

    //-------------------------------------------------------------------------
    public InvalidLicenseException ()
    {
        super ("Invalid license exception.");
    }

}

