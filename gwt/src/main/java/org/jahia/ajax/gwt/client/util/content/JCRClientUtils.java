/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.content;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rfelden
 * Date: 12 déc. 2008 - 11:03:23
 */
public final class JCRClientUtils {

    public static final String FOLDER_NODETYPES = "nt:folder,jnt:mountPoint";
    public static final String FILE_NODETYPES = "nt:file,nt:folder,jnt:mountPoint,jnt:portlet";
    public static final String PORTLET_NODETYPES = "nt:folder,jnt:mountPoint,jnt:portlet";
    public static final String CATEGORY_NODETYPES = "jnt:category";
    public static final String PORTLET_DEFINITIONS_NODETYPES = "jnt:portletDefinitions";    
    public static final String SITE_NODETYPES = "jnt:virtualsite,jnt:page";
    public static final String PAGE_NODETYPES = "jnt:page";
    public static final String TAG_NODETYPES = "jnt:tag";

    public final static String MY_REPOSITORY = "myRepository";
    public final static String USERS_REPOSITORY = "usersRepository";
    public final static String MY_EXTERNAL_REPOSITORY = "myExternalRepository";
    public final static String SHARED_REPOSITORY = "sharedRepository";
    public final static String WEBSITE_REPOSITORY = "websiteRepository";

    public final static String ALL_FILES = "files";

    public final static String MY_MASHUP_REPOSITORY = "myMashupRepository";
    public final static String SHARED_MASHUP_REPOSITORY = "sharedMashupRepository";
    public final static String WEBSITE_MASHUP_REPOSITORY = "websiteMashupRepository";

    public final static String CATEGORY_REPOSITORY = "categoryRepository";
    public final static String TAG_REPOSITORY = "tagRepository";

    public final static String PORTLET_DEFINITIONS_REPOSITORY = "portletDefinitionRepository";

    public final static String SITE_REPOSITORY = "siteRepository";

    public final static String AUTHORIZATIONS_ACL = "default";
    public final static String MODES_ACL = "modes";
    public final static String ROLES_ACL = "roles";

    public final static String ALL_MASHUPS = "mashups";

    public final static String ALL_CONTENT = "contents";

    public final static String GLOBAL_REPOSITORY = "globalRepository";
    
    public static final String REUSABLE_COMPONENTS_REPOSITORY = "resuableComponentsRepository";

    public final static String PRINCIPAL_ROLES_MAPPING = "principalRolesMapping";
    public final static String AUTHORIZATIONS = "authorizations";
    public final static String USAGE = "usage";
    public final static String VERSIONING = "versioning";
    
    public static short FILE_TABLE = 1;
    public static short THUMB_VIEW = 2;
    public static short DETAILED_THUMB_VIEW = 3;

    public static String cleanUpFilename(String name) {
        name = name.replaceAll("([\\*:/\\\\<>|?\"])", " ");
        name = name.trim();
        return name;
    }

    public static List<String> getPathesList(List<GWTJahiaNode> l) {
        List<String> res = new ArrayList<String>();
        for (GWTJahiaNode node : l) {
            res.add(node.getPath());
        }
        return res;
    }
}
