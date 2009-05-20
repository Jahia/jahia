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
package org.jahia.services.acl;


/**
 * This interface is used to share
 *
 * @author Fulco Houkes
 * @author MAP
 * @version 1.1
 */
public interface ACLInfo {

    /** User entry type constant */
    public static final int USER_TYPE_ENTRY = 1;

    /** group entry type constant */
    public static final int GROUP_TYPE_ENTRY = 2;

    /** IP entry type constant */
    public static final int IP_TYPE_ENTRY = 3;

    /** Inheritance different states on the DB acl table ("jahia_acl") field */
    public static final int INHERITANCE = 0;
    public static final int NO_INHERITANCE = 1;
}

