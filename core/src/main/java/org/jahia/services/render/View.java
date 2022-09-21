/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.render.scripting.Script;

import java.util.Properties;

/**
 * Resource view that is used for rendering and is "executed" by the appropriate {@link Script}.
 * 
 * @author Thomas Draier
 */
public interface View {
    String VISIBLE_KEY = "visible";
    String VISIBLE_FALSE = "false";
    String VISIBLE_STUDIO_ONLY = "studioOnly";
    String TYPE_KEY = "type";
    String DEFAULT_VIEW_KEY = "default";

    String getKey();

    JahiaTemplatesPackage getModule();

    String getModuleVersion();

    String getDisplayName();

    String getFileExtension();

    String getPath();    

    /**
     * Return printable information about the script : type, localization, file, .. in order to help
     * template developer to find the original source of the script
     *
     * @return printable information about the script : type, localization, file, .. in order to help
     * template developer to find the original source of the script
     */
    String getInfo();

    /**
     * Return properties of the template
     *
     * @return properties of the template
     */
    Properties getProperties();

    /**
     * Return default properties of the node type template
     *
     * @return
     */
    Properties getDefaultProperties();
}
