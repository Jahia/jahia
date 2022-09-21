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
package org.jahia.services.sites;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * Holds constants for site's settings.
 */
public final class SitesSettings {
    public static final String SERVER_NAME = "j:serverName";
    public static final String SERVER_NAME_ALIASES = "j:serverNameAliases";

    public static final String DEFAULT_SITE = "j:defaultSite";
    
    /**
     * These checks settings are used for BigText fields
     */
    public static final String HTML_MARKUP_FILTERING_ENABLED = "j:doTagFiltering";

    public static final String HTML_MARKUP_FILTERING_TAGS = "j:filteredTags";

    public static final String WCAG_COMPLIANCE_CHECKING_ENABLED = "j:wcagCompliance";
    
    public static final Set<String> HTML_SETTINGS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(HTML_MARKUP_FILTERING_ENABLED, HTML_MARKUP_FILTERING_TAGS, WCAG_COMPLIANCE_CHECKING_ENABLED))); 

    /**
     * Language settings
     */
    public static final String MIX_LANGUAGES_ACTIVE = "j:mixLanguage";

    public static final String INACTIVE_LIVE_LANGUAGES = "j:inactiveLiveLanguages"; 

    public static final String INACTIVE_LANGUAGES = "j:inactiveLanguages"; 

    public static final String MANDATORY_LANGUAGES = "j:mandatoryLanguages";
    
    public static final String DEFAULT_LANGUAGE = "j:defaultLanguage";

    public static final String LANGUAGES = "j:languages";
    
    public static final String ALLOWS_UNLISTED_LANGUAGES = "j:allowsUnlistedLanguages";
    
    /**
     * Module/template settings
     */
    public static final String INSTALLED_MODULES = "j:installedModules";
    
    public static final String TEMPLATES_SET = "j:templatesSet";
    
    private SitesSettings() {
    }
}
