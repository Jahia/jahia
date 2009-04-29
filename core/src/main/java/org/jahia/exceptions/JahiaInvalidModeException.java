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
// $Id: JahiaForbiddenAccessException.java 14745 2006-07-20 14:58:38Z shuber $
//


package org.jahia.exceptions;


/**
 * This exception is raised when the user tries to access a resource, for which
 * the mode is not valid (e.g. edit mode on a read-only cluster node)
 */
public class JahiaInvalidModeException extends JahiaException
{

    //-------------------------------------------------------------------------
    /** Default constructor
     */
    public JahiaInvalidModeException ()
    {
        super ("403 Invalid mode", "403 Invalid mode",
                UNAVAILABLE_ERROR, ERROR_SEVERITY);
    }
}
