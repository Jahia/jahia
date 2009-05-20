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
package org.jahia.ajax.gwt.client.util;

import java.util.List;
import java.util.ArrayList;

/**
 * User: ktlili
 * Date: 4 sept. 2008
 * Time: 18:24:36
 */
public abstract class MySettingsConstants {
    public static final String REQUEST_KEY_PREFIX = "mysettings-user-";
    public static final String SEPARATOR = "#";
    public static final String USER_PROPERTY_PREFIX = REQUEST_KEY_PREFIX + "property" + SEPARATOR;

    public static String EMAIL = "email";
    public static String LAST_NAME = "lastname";
    public static String FIRST_NAME = "firstname";
    public static String ORG = "organization";
    public static String EMAIL_NOTIFICATION = "emailNotificationsDisabled";
    public static String PREFERED_LANGUAGE = "preferredLanguage";

    public static List<String> getJahiaUSerProperties() {
        List<String> list = new ArrayList<String>();
        list.add(EMAIL);
        list.add(LAST_NAME);
        list.add(FIRST_NAME);
        list.add(EMAIL_NOTIFICATION);
        list.add(PREFERED_LANGUAGE);
        return list;

    }
}
