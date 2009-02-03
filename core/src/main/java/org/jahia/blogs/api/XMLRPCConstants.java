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

package org.jahia.blogs.api;

/**
 * Simple interface providing error codes and corresponding messages.
 *
 * @author Xavier Lawrence
 */
public interface XMLRPCConstants {
    
    public static final int AUTHORIZATION_EXCEPTION = 0001;
    public static final String AUTHORIZATION_EXCEPTION_MSG =
            "Invalid Username and/or Password";
    
    public static final int UNKNOWN_EXCEPTION = 1000;
    public static final String UNKNOWN_EXCEPTION_MSG =
            "An error occured processing your request";
    
    public static final int BLOGGERAPI_DISABLED = 1000;
    public static final String BLOGGERAPI_DISABLED_MSG =
            "You have not enabled Blogger API support for your weblog";
    
    public static final int UNSUPPORTED_EXCEPTION = 1001;
    public static final String UNSUPPORTED_EXCEPTION_MSG =
            "Unsupported method - Jahia does not support this method";
    
    public static final int INVALID_POSTID = 2000;
    public static final String INVALID_POSTID_MSG =
            "The entry postID you submitted is invalid";
    
    public static final int NOBLOGS_EXCEPTION = 3000;
    public static final String NOBLOGS_EXCEPTION_MSG =
            "There are no blogs defined for your user";
    
    public static final int UPLOAD_DENIED_EXCEPTION = 4000;
    public static final String UPLOAD_DENIED_EXCEPTION_MSG =
            "Upload denied";
}
