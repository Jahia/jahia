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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util;

import org.jahia.ajax.gwt.client.messages.Messages;


/**
 * Utility class for opening engine popup windows.
 *
 * @author rfelden
 * @version 25 f�vr. 2008 - 18:46:08
 */
public class EngineOpener {

    /**
     * these params might be changed in case the window to open should have a different look
     */
    public final static String ENGINE_WINDOW_PARAMS = "menubar=no,location=no,resizable=yes,scrollbars=yes,status=yes,width=1020,height=730" ;
    public final static String ENGINE_FRAME_NAME = "enginePopupWindow" ;

    public static void openEngine(String url) {
        openEngine(ENGINE_FRAME_NAME, url) ;
    }

    public static void openEngine(String url, String windowName) {
        openEngine(windowName, url, ENGINE_WINDOW_PARAMS);
    }

    public static void openEngine(String url, String windowName, String windowParams) {
        if (closePreviousEngine(Messages
                .getNotEmptyResource(
                        "org.jahia.engines.confirmWindowClose",
                        "Opening a new engine window will close the currently opened one. Do you want to continue?"))) {
            open(url, windowName, windowParams);
        }
    }

    private static native boolean closePreviousEngine(String confirmationMessage) /*-{
        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        if ($wnd.jahia.engineWindow && !$wnd.jahia.engineWindow.closed) {
            if (!$wnd.confirm(confirmationMessage)) {
                return false;
            } 
            try {
                $wnd.jahia.engineWindow.close();
                if ($wnd.jahia.engineWindow && $wnd.jahia.engineWindow.closed) {
                    $wnd.jahia.engineWindow = null;
                }

            } catch (e) {
                $wnd.alert('error: ' + e);
            }
        }
        return true;
    }-*/;

    private static native void open(String url, String engineName, String params) /*-{
        if (!$wnd.jahia) {
            $wnd.jahia = new Object();
        }
        if ($wnd.jahia.engineWindow && !$wnd.jahia.engineWindow.closed) {
            engineName = engineName + new Date().getTime();
        }
        $wnd.jahia.engineWindow = $wnd.open(url, engineName, params);
        $wnd.jahia.engineWindow.focus();
    }-*/;
}
