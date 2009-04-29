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

/**
 * User: ktlili
 * Date: 4 mars 2008
 * Time: 15:12:01
 */
public class Constants {
    public final static String UID_SEPARATOR = "-uid";

    // this is a long number (milliseconds for a week) to reckon an id that would stay unique for a week
    // (cache flushes every 4 hours by default so this might seem too much, but shit happens)
    public static final int MODULO = 604800000;

    public static final int MODULO_DAY = 86400000 ;

    public static final int MODULO_HOUR = 3600000 ;

}
