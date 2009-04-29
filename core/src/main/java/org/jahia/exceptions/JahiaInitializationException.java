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



package org.jahia.exceptions;



/**
 * This exception is used when an initialization has failed inside of Jahia.
 * It's mainly used by the services initialization.s
 *
 * @author Khue ng
 * @author  Fulco Houkes
 *
 * @version 1.1
 */
public class JahiaInitializationException extends JahiaException
{

    /**
     * DefaultConstructor
     */
    public JahiaInitializationException(String message)
    {
        super ("Initialization error.", message, INITIALIZATION_ERROR,
               CRITICAL_SEVERITY);
    }

    /**
     * Embeddedable exception constructor, use this if you are rethrowing
     * an existing exception.
     */
    public JahiaInitializationException(String message, Throwable t)
    {
        super ("Initialization error.", message, INITIALIZATION_ERROR,
               CRITICAL_SEVERITY, t);
    }
} // end Class JahiaInitializationException
