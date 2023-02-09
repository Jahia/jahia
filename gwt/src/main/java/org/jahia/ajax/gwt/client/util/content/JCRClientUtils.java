/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
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
