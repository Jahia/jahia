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
