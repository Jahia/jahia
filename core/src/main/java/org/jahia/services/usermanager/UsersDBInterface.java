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
package org.jahia.services.usermanager;


/**
 * .. add class description here ..
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public interface UsersDBInterface {

    /** user database table name */
    public static final String JAHIA_USERS = "jahia_users";

    /**
     * user unique identification number, used only in this database
     * user management system
     */
    public static final String FIELD_USER_ID_USERS = "id_jahia_users";

    /** username column name */
    public static final String FIELD_USER_NAME_USERS = "name_jahia_users";

    /** password column name */
    public static final String FIELD_PASSWORD_USERS = "password_jahia_users";

    /** user unique identification key column name */
    public static final String FIELD_USER_KEY_USERS = "key_jahia_users";

}

