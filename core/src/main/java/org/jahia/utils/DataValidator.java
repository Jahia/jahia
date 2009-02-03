/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
