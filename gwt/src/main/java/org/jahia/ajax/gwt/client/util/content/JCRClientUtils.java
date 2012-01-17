/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util.content;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: rfelden
 * Date: 12 déc. 2008 - 11:03:23
 */
public final class JCRClientUtils {

    public static final List<String> FOLDER_NODETYPES =  Arrays.asList("nt:folder","jnt:mountPoint");
    public static final List<String> CONTENTLIST_NODETYPES =  Arrays.asList("jnt:contentList");
    public static final List<String> PORTLETLIST_NODETYPES =  Arrays.asList("jnt:portletFolder");
    public static final List<String> FILE_NODETYPES = Arrays.asList("nt:file");
    public static final List<String> PORTLET_NODETYPES = Arrays.asList("jnt:portlet");
    public static final List<String> CATEGORY_NODETYPES =  Arrays.asList("jnt:category");
    public static final List<String> SITE_NODETYPES = Arrays.asList( "jnt:virtualsite","jnt:page");
    public static final List<String> PAGE_NODETYPES =  Arrays.asList("jnt:page");
    public static final List<String> TAG_NODETYPES =  Arrays.asList("jnt:tag");
    public static final List<String> CONTENT_NODETYPES =  Arrays.asList("jnt:content");
    public static final List<String> MANUALLY_ORDERABLE_NODETYPES =  Arrays.asList("jnt:content", "jmix:manuallyOrderable");

    public final static String MY_REPOSITORY = "myRepository";
    public final static String USERS_REPOSITORY = "usersRepository";
    public final static String MY_EXTERNAL_REPOSITORY = "myExternalRepository";
    public final static String SHARED_REPOSITORY = "sharedRepository";
    public final static String WEBSITE_REPOSITORY = "websiteRepository";
    public final static String REMOTEPUBLICATIONS_REPOSITORY = "remotePublicationsRepository";

    public final static String ALL_FILES = "files";

    public final static String MY_MASHUP_REPOSITORY = "myPortletRepository";
    public final static String SHARED_MASHUP_REPOSITORY = "sharedPortletRepository";
    public final static String WEBSITE_MASHUP_REPOSITORY = "websitePortletRepository";

    public final static String CATEGORY_REPOSITORY = "categoryRepository";
    public final static String TAG_REPOSITORY = "tagRepository";
    public final static String ROLE_REPOSITORY = "roleRepository";
    public final static String SITE_ROLE_REPOSITORY = "siteRoleRepository";

    public final static String PORTLET_DEFINITIONS_REPOSITORY = "portletDefinitionRepository";

    public final static String SITE_REPOSITORY = "siteRepository";

    public final static String TEMPLATES_REPOSITORY = "templatesRepository";

    public final static String PORTLET_MODES_ROLES = "modes";
    public final static String PORTLET_ROLES = "roles";

    public final static String ALL_MASHUPS = "portlets";

    public final static String ALL_CONTENT = "contents";

    public final static String GLOBAL_REPOSITORY = "globalRepository";
    
    public final static String INFO = "info";
    public final static String PRINCIPAL_ROLES_MAPPING = "principalRolesMapping";
    public final static String AUTHORIZATIONS = "authorizations";
    public final static String USAGE = "usage";
    public final static String VERSIONING = "versioning";
    
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
