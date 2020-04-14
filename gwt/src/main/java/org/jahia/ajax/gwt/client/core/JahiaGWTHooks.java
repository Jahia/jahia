/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.core;

import org.jahia.ajax.gwt.client.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Global class to provide hooks in GWT application
 * A hook is a function that will be call from "window.top.jahiaGwtHook" object
 */
public class JahiaGWTHooks {

    /**
     * Check if the hook associated with the key exists
     * @param key
     * @return true if the hook exists
     */
    public static native boolean hasHook(String key) /*-{
        return  $wnd.top.jahiaGwtHook !== undefined && $wnd.top.jahiaGwtHook[key] !== undefined;
    }-*/;

    /**
     * Call the hook define by key with associated params
     * @param key
     * @param customParams
     */
    public static void callHook(String key, Map<String, Object> customParams) {
        Map<String, Object> params = new HashMap<>();
        // Provide the current GWT context
        JahiaGWTParameters.getJahiaParamDictionary().keySet().forEach(keyEntry -> params.put(keyEntry, JahiaGWTParameters.getJahiaParamDictionary().get(keyEntry)));

        // add custom params
        params.putAll(customParams);

        // call the hook
        callHook(key, JsonUtils.serialize(params).getJavaScriptObject());
    }

    private static native void callHook(String key, Object param) /*-{
        if ( $wnd.top.jahiaGwtHook !== undefined && $wnd.top.jahiaGwtHook[key] !== undefined) {
            $wnd.top.jahiaGwtHook[key](param);
        } else {
            console.warn('unable to find GWT hook for key ' + key + ' in window.top');
        }
    }-*/;

}
