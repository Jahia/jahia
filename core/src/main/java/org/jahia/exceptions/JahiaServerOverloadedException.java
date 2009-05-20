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
package org.jahia.exceptions;

/**
 * This exception is raised when a session is requested which is null, this
 * will be considered as a session expiration in most of the cases.
 *
 * @author  Fulco Houkes
 * @version 1.0
 */
public class JahiaServerOverloadedException extends JahiaException
{
    boolean duringFirstRequest = false;
    int suggestedRetryTime;
    //-------------------------------------------------------------------------
    /** Default constructor
     */
    public JahiaServerOverloadedException(boolean isDuringFirstRequest, int aSuggestedRetryTime)
    {
        super ("503 - server overloaded", "503 - Server Overloaded",
               UNAVAILABLE_ERROR , ERROR_SEVERITY);
        duringFirstRequest = isDuringFirstRequest;
        suggestedRetryTime = aSuggestedRetryTime;
    }
    
    public int getSuggestedRetryTime() {
        return suggestedRetryTime;
    }

    public boolean isDuringFirstRequest() {
        return duringFirstRequest;
    }
}
