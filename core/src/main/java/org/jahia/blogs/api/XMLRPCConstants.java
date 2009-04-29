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
