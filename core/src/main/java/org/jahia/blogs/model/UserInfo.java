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
package org.jahia.blogs.model;
import java.io.Serializable;

/**
 * Simple model representing an authenticated blog user according to the 
 * Blogger API.
 *
 * @author Xavier Lawrence
 */
public interface UserInfo extends Serializable {       
    
    public static final String USER_ID = "userid";
    public static final String NICKNAME = "nickname";
    public static final String FIRSTNAME = "firstname";
    public static final String LASTNAME = "lastname";
    public static final String EMAIL = "email";
    public static final String URL = "url";
        
    public String getUserID();
    public String getNickName();
    public String getFirstName();
    public String getLastName();
    public String getUrl();
    public String getEmail();
}
