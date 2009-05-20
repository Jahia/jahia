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
package org.jahia.ajax.gwt.client.util.nodes;

/**
 * User: rfelden
 * Date: 12 déc. 2008 - 11:03:23
 */
public final class JCRClientUtils {

    public static final String FOLDER_NODETYPES = "nt:folder,jnt:mountPoint";
    public static final String FILE_NODETYPES = "nt:file,nt:folder,jnt:mountPoint,jnt:portlet";
    public static final String PORTLET_NODETYPES = "nt:folder,jnt:mountPoint,jnt:portlet";

    public final static String MY_REPOSITORY = "myRepository";
    public final static String USERS_REPOSITORY = "usersRepository";
    public final static String MY_EXTERNAL_REPOSITORY = "myExternalRepository";
    public final static String SHARED_REPOSITORY = "sharedRepository";
    public final static String WEBSITE_REPOSITORY = "websiteRepository";

    public final static String ALL_FILES = "files";

    public final static String MY_MASHUP_REPOSITORY = "myMashupRepository";
    public final static String SHARED_MASHUP_REPOSITORY = "sharedMashupRepository";
    public final static String WEBSITE_MASHUP_REPOSITORY = "websiteMashupRepository";

    public final static String AUTHORIZATIONS_ACL = "default";
    public final static String MODES_ACL = "modes";
    public final static String ROLES_ACL = "roles";

    public final static String ALL_MASHUPS = "mashups";

    public final static String GLOBAL_REPOSITORY = "globalRepository";
    public static short FILE_TABLE = 1;
    public static short THUMB_VIEW = 2;
    public static short DETAILED_THUMB_VIEW = 3;

    public static String cleanUpFilename(String name) {
        name = name.replaceAll("([\\*:/\\\\<>|?\"])", " ");
        name = name.trim();
        return name;
    }

}
