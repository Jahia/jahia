/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
