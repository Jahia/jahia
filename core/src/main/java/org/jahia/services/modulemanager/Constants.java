/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.modulemanager;

import java.util.jar.Attributes;

/**
 * Module management related constants.
 *
 * @author Sergiy Shyrkov
 */
public final class Constants {

    private Constants() {}


    public static final  String ATTR_JAHIA_MODULE_TYPE = "Jahia-Module-Type";

    public static final  Attributes.Name ATTR_NAME_BUNDLE_NAME = new Attributes.Name(org.osgi.framework.Constants.BUNDLE_NAME);

    public static final  Attributes.Name ATTR_NAME_BUNDLE_SYMBOLIC_NAME = new Attributes.Name(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);

    public static final  Attributes.Name ATTR_NAME_BUNDLE_VERSION = new Attributes.Name(org.osgi.framework.Constants.BUNDLE_VERSION);

    public static final  Attributes.Name ATTR_NAME_FRAGMENT_HOST = new Attributes.Name(org.osgi.framework.Constants.FRAGMENT_HOST);

    public static final  Attributes.Name ATTR_NAME_GROUP_ID = new Attributes.Name("Jahia-GroupId");

    public static final  Attributes.Name ATTR_NAME_IMPL_TITLE = new Attributes.Name("Implementation-Title");

    public static final  Attributes.Name ATTR_NAME_IMPL_VERSION = new Attributes.Name("Implementation-Version");

    public static final  Attributes.Name ATTR_NAME_JAHIA_DEPENDS = new Attributes.Name("Jahia-Depends");

    public static final  Attributes.Name ATTR_NAME_JAHIA_MODULE_TYPE = new Attributes.Name(ATTR_JAHIA_MODULE_TYPE);

    public static final  Attributes.Name ATTR_NAME_JAHIA_PACKAGE_DESCRIPTION = new Attributes.Name("Jahia-Package-Description");

    public static final  Attributes.Name ATTR_NAME_JAHIA_PACKAGE_LICENSE = new Attributes.Name("Jahia-Package-License");

    public static final  Attributes.Name ATTR_NAME_JAHIA_PACKAGE_NAME = new Attributes.Name("Jahia-Package-Name");

    public static final Attributes.Name ATTR_NAME_JAHIA_PACKAGE_VERSION = new Attributes.Name("Jahia-Package-Version");

    public static final Attributes.Name ATTR_NAME_JAHIA_REQUIRED_VERSION = new Attributes.Name("Jahia-Required-Version");

    public static final Attributes.Name ATTR_NAME_PROVIDE_CAPABILITY = new Attributes.Name("Provide-Capability");

    public static final Attributes.Name ATTR_NAME_REQUIRE_CAPABILITY = new Attributes.Name("Require-Capability");

    public static final Attributes.Name ATTR_NAME_ROOT_FOLDER = new Attributes.Name("root-folder");

    public static final String BUNDLE_SERVICE_PROPERTY_CLUSTERED = "clustered";

    public static final String OSGI_CAPABILITY_SERVER = "com.jahia.server";

    public static final String OSGI_CAPABILITY_SERVER_VERSION = "version";

    public static final String OSGI_CAPABILITY_MODULE_DEPENDENCIES = "com.jahia.modules.dependencies";

    public static final String OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY = "moduleIdentifier";

    public static final String OSGI_CAPABILITY_MODULE_DEPENDENCIES_VERSION_KEY = "moduleVersion";

    public static final String URL_PROTOCOL_DX = "dx";

    public static final String DEPENDENCY_DELIMITER = "#"; // anything except comma or semicolon

    public static final String URL_PROTOCOL_MODULE_DEPENDENCIES = "legacydepends";
}
