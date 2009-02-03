/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.sites;


/**
 * Holds constants for site's settings.
 */
public final class SitesSettings {

    //--------------------------------------------------------------------------
    /** Home page settings */
    public static final String USER_DEFAULT_HOMEPAGE_DEF
            = "user_default_homepage_def";
    public static final String GROUP_DEFAULT_HOMEPAGE_DEF
            = "group_default_homepage_def";
    public static final String USER_DEFAULT_HOMEPAGE_DEF_ACTIVE
            = "user_default_homepage_def_active";
    public static final String GROUP_DEFAULT_HOMEPAGE_DEF_ACTIVE
            = "group_default_homepage_def_active";
    public static final String USER_DEFAULT_HOMEPAGE_DEF_ATCREATION
            = "user_default_homepage_def_atcreation";
    public static final String GROUP_DEFAULT_HOMEPAGE_DEF_ATCREATION
            = "group_default_homepage_def_atcreation";
    public static final String VERSIONING_ENABLED = "versioning_enabled";

    public static final String STAGING_ENABLED = "staging_enabled";

    /** These checks settings are used for BigText fields */
    public static final String HTML_CLEANUP_ENABLED = "html_cleanup_enabled";

    public static final String HTML_MARKUP_FILTERING_ENABLED = "html_markup_filtering_enabled";
    
    public static final String HTML_MARKUP_FILTERING_TAGS = "html_markup_filtering_tags";
    
    public static final String URL_INTEGRITY_CHECKING_ENABLED = "url_integrity_checking_enabled";
    
    public static final String WAI_COMPLIANCE_CHECKING_ENABLED = "wai_compliance_checking_enabled";

    /** Language settings */
    public static final String MIX_LANGUAGES_ACTIVE = "mix_languages_active";

    public static final String TEMPLATE_PACKAGE_NAME = "templatePackageName";
    
    public static final String FILE_LOCK_ON_PUBLICATION = "fileLockOnPublication";
}
