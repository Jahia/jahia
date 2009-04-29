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
package org.jahia.ajax.gwt.client.util.layoutmanager;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 28 nov. 2007
 * Time: 15:55:34
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPageUserPropertyHelper {
    private static String BOX_STATUS_FULLSCREEN = "fullscreen";
    private static String BOX_STATUS_MAXIMIZED = "maximized";
    private static String BOX_STATUS_NORMAL = "normalState";
    private static String BOX_STATUS_MINIMIZED = "minimized";
    private static String BOX_STATUS_CLOSED = "closed";


    public static String getStatusNormaleValue() {
        return BOX_STATUS_NORMAL;
    }

    public static String getStatusMaximizedValue() {
        return BOX_STATUS_MAXIMIZED;
    }

    public static String getStatusFullscreenValue() {
        return BOX_STATUS_FULLSCREEN;
    }

    public static String getStatusClosedValue() {
        return BOX_STATUS_CLOSED;
    }

    public static String getStatusMinimizedValue() {
        return BOX_STATUS_MINIMIZED;
    }

    public static boolean isStatusMaximized(String value) {
        if (value == null) {
            return false;
        } else {
            return value.equalsIgnoreCase(BOX_STATUS_MAXIMIZED);
        }
    }

    public static boolean isStatusNormal(String value) {
        if (value == null) {
            return false;
        } else {
            return value.equalsIgnoreCase(BOX_STATUS_NORMAL);
        }
    }

    public static boolean isStatusFullScreen(String value) {
        if (value == null) {
            return false;
        } else {
            return value.equalsIgnoreCase(BOX_STATUS_FULLSCREEN);
        }
    }

    public static boolean isStatusMinimized(String value) {
        if (value == null) {
            return false;
        } else {
            return value.equalsIgnoreCase(BOX_STATUS_MINIMIZED);
        }
    }

    public static boolean isStatusClosed(String value) {
        if (value == null) {
            return false;
        } else {
            return value.equalsIgnoreCase(BOX_STATUS_CLOSED);
        }
    }


}
