/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
