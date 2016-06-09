/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
public interface Constants {
    
    String ATTR_JAHIA_MODULE_TYPE = "Jahia-Module-Type";
    
    Attributes.Name ATTR_NAME_BUNDLE_NAME = new Attributes.Name(org.osgi.framework.Constants.BUNDLE_NAME);

    Attributes.Name ATTR_NAME_BUNDLE_SYMBOLIC_NAME = new Attributes.Name(org.osgi.framework.Constants.BUNDLE_SYMBOLICNAME);
    
    Attributes.Name ATTR_NAME_BUNDLE_VERSION = new Attributes.Name(org.osgi.framework.Constants.BUNDLE_VERSION);

    Attributes.Name ATTR_NAME_FRAGMENT_HOST = new Attributes.Name(org.osgi.framework.Constants.FRAGMENT_HOST);

    Attributes.Name ATTR_NAME_GROUP_ID = new Attributes.Name("Jahia-GroupId");

    Attributes.Name ATTR_NAME_IMPL_TITLE = new Attributes.Name("Implementation-Title");

    Attributes.Name ATTR_NAME_IMPL_VERSION = new Attributes.Name("Implementation-Version");

    Attributes.Name ATTR_NAME_JAHIA_DEPENDS = new Attributes.Name("Jahia-Depends");

    Attributes.Name ATTR_NAME_JAHIA_MODULE_TYPE = new Attributes.Name(ATTR_JAHIA_MODULE_TYPE);
    
    Attributes.Name ATTR_NAME_PROVIDE_CAPABILITY = new Attributes.Name("Provide-Capability");
    
    Attributes.Name ATTR_NAME_REQUIRE_CAPABILITY = new Attributes.Name("Require-Capability");

    Attributes.Name ATTR_NAME_ROOT_FOLDER = new Attributes.Name("root-folder");

    String BUNDLE_SERVICE_PROPERTY_CLUSTERED = "clustered";
    
    String OSGI_CAPABILITY_MODULE_DEPENDENCIES = "com.jahia.modules.dependencies";
    
    String OSGI_CAPABILITY_MODULE_DEPENDENCIES_KEY = "moduleIdentifier";
    
    String URL_PROTOCOL_DX = "dx";
    
    String URL_PROTOCOL_MODULE_DEPENDENCIES = "legacydepends";
}
