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

import org.jahia.exceptions.JahiaException;


/**
 * All the license exception are derived from this class.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class LicenseException extends JahiaException
{

    //-------------------------------------------------------------------------
    /**
     * Default constructor
     *
     * @param   message
     *      Error message.
     */
    public LicenseException (String message)
    {
        super (message, message, LICENSE_ERROR, FATAL_SEVERITY);
    }

}

