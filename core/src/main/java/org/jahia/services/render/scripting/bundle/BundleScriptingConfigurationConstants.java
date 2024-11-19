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
package org.jahia.services.render.scripting.bundle;

/**
 * A class gathering constants used for bundles providing configuration targeted at providing new scripting language
 * support.
 *
 * @author Christophe Laprun
 */
public class BundleScriptingConfigurationConstants {
    /**
     * OSGi header specifying whether the module provides any views so that it can be skipped by DX when looking for
     * views. A {@code no} value for this header specifies that the module doesn't provide any view and will
     * therefore be skipped when DX scans modules for views. Any other value is ignored and will result in the module
     * being scanned for views. This header is useful to prevent any form of view-related inspection of your module
     * in order to speed-up deployment and/or DX startup.
     */
    public static final String JAHIA_MODULE_HAS_VIEWS = "Jahia-Module-Has-Views";
    /**
     * OSGi header specifying a comma-separated list of script languages for which this module provides views. Script
     * languages in this case are identified by engine name, language name and/or file extension. This header is
     * mandatory for modules wishing to provide views for non-default scripting languages as DX will not scan bundles
     * for non-default scripting languages views by default. It is therefore important for a module to specify which
     * scripting languages it provides views for so that it is properly scanned and its views, registered. This header
     * is not needed if you only provide views using scripting languages supported by default.
     * <p>
     * The following example specifies that the bundle provides views for the Thymeleaf and PHP scripting languages:
     * <pre>
     * {@code <Jahia-Module-Scripting-Views>thymeleaf,php</Jahia-Module-Scripting-Views>}
     * </pre>
     */
    public static final String JAHIA_MODULE_SCRIPTING_VIEWS = "Jahia-Module-Scripting-Views";
    /**
     * OSGi header to allow modules providing new scripting languages to control the order of how views are resolved by
     * DX by controlling the priority of file extensions associated with specific view technologies.
     * <p>
     * Its value should consist of a space-separated list of {@code extension=priority} pairs specifying the new
     * ordering of extensions.
     * <p>
     * The following example specifies that the foo extension will have a priority of 1000 while the bar extension will
     * have a priority of 250, thus making it of higher priority.
     * <pre>
     * {@code <Jahia-Scripting-Extensions-Priorities>foo=1000 bar=250</Jahia-Scripting-Extensions-Priorities>}
     * </pre>
     */
    public static final String JAHIA_SCRIPTING_EXTENSIONS_PRIORITIES = "Jahia-Scripting-Extensions-Priorities";

    private BundleScriptingConfigurationConstants() {
    }
}
