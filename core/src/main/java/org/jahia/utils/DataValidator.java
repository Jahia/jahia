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
//
//  DataValidator
//  MJ      08.01.2001
//
//

package org.jahia.utils;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.services.mail.MailService;



/**
 * Utility class for data validation.
 *
 * @author MJ
 * @deprecated use {@link MailService#isValidEmailAddress(String, boolean)} instead
 */


public class DataValidator
{
    
    private static RE emailRE;
    
    static {
        try {
            emailRE = new RE("^[\\w\\-\\._=\\!#\\*\\+]+@((([\\w\\-]+\\.)+[a-zA-Z]{2,3})|localhost)$");
        } catch (RESyntaxException e) {
	        String errorMsg = "RE exception : " + e.getMessage();
	        JahiaConsole.println( "DataValidator", errorMsg );
        }
    }

    /**
     * Checks whether an email address is valid.
     *
     * @param email The email address to check.
     * @return      Whether the email is valid.
     */
    public static boolean isValidEmail (String email)
    {
        return (email != null && emailRE.match(email));
    }

}
