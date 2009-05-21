/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.layoutmanager;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 28 nov. 2007
 * Time: 15:55:34
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPropertyHelper {
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
