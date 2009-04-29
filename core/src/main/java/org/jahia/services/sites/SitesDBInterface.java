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
 * This interface holds constants defining the site database table name, and
 * all the column names.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public interface SitesDBInterface {

    /** <code>jahia_sites_users</code> database table name */
    public static final String JAHIA_SITES_USERS = "jahia_sites_users";

    /** account ID column name */
    public static final String FIELD_ACCOUNT_ID_SITES_USERS = "id_jahia_accounts";

    /** user key column name */
    public static final String FIELD_USER_KEY_SITES_USERS = "key_jahia_users";

    /** site key column name */
    public static final String FIELD_SITE_KEY_SITES_USERS = "key_jahia_sites";


    /** <code>jahia_sites</code> database table name */
    public static final String JAHIA_SITES = "jahia_sites";

    /**  */
    public static final String FIELD_TITLE_SITES = "title_jahia_sites";

    /**  */
    public static final String FIELD_SERVERNAME_SITES = "servername_jahia_sites";

    /**  */
    public static final String FIELD_KEY_SITES = "key_jahia_sites";

    /**  */
    public static final String FIELD_ACTIVE_SITES = "active_jahia_sites";

    /**  */
    public static final String FIELD_DEFAULTPAGE_ID_SITES =
            "defaultpageid_jahia_sites";

    /**  */
    public static final String FIELD_DEFAULT_TEMPLATE_ID_SITES =
            "defaulttemplateid_jahia_sites";

    /**  */
    public static final String FIELD_TPL_DEPLOY_MODE_SITES =
            "tpl_deploymode_jahia_sites";

    /**  */
    public static final String FIELD_WEBAPPS_DEPLOY_MODE_SITES =
            "webapps_deploymode_jahia_sites";

    /**  */
    public static final String FIELD_ACL_ID_SITES = "rights_jahia_sites";

    /**  */
    public static final String FIELD_SITE_DESCRIPTION_SITES = "descr_jahia_sites";

}
