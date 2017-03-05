/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.sites;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Holds constants for site's settings.
 */
public final class SitesSettings {

    /**
     * These checks settings are used for BigText fields
     */
    public static final String HTML_MARKUP_FILTERING_ENABLED = "j:doTagFiltering";

    public static final String HTML_MARKUP_FILTERING_TAGS = "j:filteredTags";

    public static final String WCAG_COMPLIANCE_CHECKING_ENABLED = "j:wcagCompliance";
    
    public static final Set<String> HTML_SETTINGS = new HashSet<String>(Arrays.asList(HTML_MARKUP_FILTERING_ENABLED, HTML_MARKUP_FILTERING_TAGS, WCAG_COMPLIANCE_CHECKING_ENABLED)); 

    /**
     * Language settings
     */
    public static final String MIX_LANGUAGES_ACTIVE = "j:mixLanguage";

    public static final String INACTIVE_LIVE_LANGUAGES = "j:inactiveLiveLanguages"; 

    public static final String INACTIVE_LANGUAGES = "j:inactiveLanguages"; 

    public static final String MANDATORY_LANGUAGES = "j:mandatoryLanguages";
    
    public static final String DEFAULT_LANGUAGE = "j:defaultLanguage";

    public static final String LANGUAGES = "j:languages"; 

}
