/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.*;

/**
 * URL related utility functions.
 * @author ktlili
 * Date: 5 nov. 2007
 * Time: 09:02:11
 */
public class URL {

    private static final String GWT_SCRIPT_PATH = "/gwt/";

    public static String getServerBaseURL() {
        String absoluteURLContext = GWT.getModuleBaseURL();
        String[] splittedUrl = absoluteURLContext.split("/");
        /*  url: http://www.example.com/jahia/...
        --->
        splittedUrl[0] = "http:"
        splittedUrl[1] = ""
        splittedUrl[2] = "www.example.com"  */
        if (splittedUrl.length > 2) {
            return splittedUrl[0] + "//" + splittedUrl[2];
        } else {
            return null;
        }
    }

    /**
     * Get Jahia context
     *
     * @return
     */
    public static String getJahiaContext() {
        String contextPath = "";
        String baseUrl = GWT.getModuleBaseURL();
        String serverBaseUrl = getServerBaseURL();
        baseUrl = baseUrl.substring(serverBaseUrl.length());
        int suffixPosition = baseUrl.indexOf(GWT_SCRIPT_PATH);
        if (suffixPosition != -1) {
            contextPath = baseUrl.substring(0, suffixPosition);
        }
        return contextPath;
    }

    /**
     * Get absolute url
     *
     * @param url
     * @return
     */
    public static String getAbsoluteURL(String url) {
        String server = getServerBaseURL();
        return server + url;
    }

    /**
     * Get relative url
     *
     * @return
     */
    public static String getRelativeURL() {
        return getWindowUrl().replaceAll(getJahiaContext(), "");
    }

    /**
     * Get queryString
     *
     * @return
     */
    public static String getQueryString() {
        String windowUrl = getWindowUrl();
        int separatorIndex = windowUrl.indexOf('?');
        if (separatorIndex > 0) {
            return windowUrl.substring(separatorIndex);
        }
        return null;
    }

    /**
     * Return as example /jahia/cms if the url is like http//www.mysite.com/jahia/cms/edit/....
     *
     * @return
     */
    public static String getContextServletPath() {
        return JahiaGWTParameters.getContextPath() + JahiaGWTParameters.getServletPath();
    }

    /**
     * Rewrite url (ie. used for richtext)
     *
     * @param url
     * @return
     */
    public static String rewrite(final String jahiaContextPath,final String jahiaServletPath, final String url) {
        if (url == null) {
            return null;
        } else if (url.indexOf("/{mode}/{lang}/") > 0) {
            // already rewited
            return url;
        } else {

            // absolute url are not processed
            if (isAbsoluteUrl(url)) {
                return url;
            }

            // return url like /jahia/cms/##mode##/##lang##"/content/sites/ACME/home.html
            return jahiaContextPath+ jahiaServletPath+ "/{mode}/{lang}" + url;
        }
    }

    /**
     * Chech if the url is absolute one
     *
     * @param url
     * @return
     */
    private static boolean isAbsoluteUrl(String url) {
        String[] splittedUrl = url.split("/");
        /*splittedUrl[0] = "http:"
        splittedUrl[1] = ""
        splittedUrl[2] = "www.example.com" */
        if (splittedUrl.length > 2) {
            if (splittedUrl[1].length() == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get window urle
     *
     * @return
     */
    public static native String getWindowUrl() /*-{
    return $wnd.location.href;

}-*/;

    public static String replacePlaceholders(String value, final GWTJahiaNode selectedNode) {
        if (value.contains("$context")) {
            value = value.replace("$context", JahiaGWTParameters.getContextPath());
        }
        if (value.contains("$siteuuid")) {
            value = value.replace("$siteuuid", JahiaGWTParameters.getSiteUUID());
        }
        if (value.contains("$base-url")) {
            value = value.replace("$base-url", JahiaGWTParameters.getBaseUrl());
        }
        if (value.contains("$site-path")) {
            value = value.replace("$site-path", JahiaGWTParameters.getSiteNode().getPath());
        }
        if (value.contains("$site-key")) {
            value = value.replace("$site-key", JahiaGWTParameters.getSiteNode().getSiteKey());
        }
        if (value.contains("$site-servername")) {
            value = value.replace("$site-servername", ((String) JahiaGWTParameters.getSiteNode().get("j:serverName")));
        }
        if (value.contains("$site-homepage-path")) {
            String home = JahiaGWTParameters.getSiteNode().get(GWTJahiaNode.HOMEPAGE_PATH);
            if (home != null) {
                value = value.replace("$site-homepage-path", home);
            }
        }
        if (value.contains("$lang")) {
            value = value.replace("$lang", JahiaGWTParameters.getLanguage());
        }
        if (value.contains("$ui-lang(")) {
            // handle pattern like: $uiLang([fr,en],en)
            int uiLangIndex = value.indexOf("$ui-lang(");
            int startLangIndex = uiLangIndex + "$ui-lang(".length();
            int endLangIndex = value.indexOf(")", startLangIndex);

            String paramStr = value.substring(startLangIndex, endLangIndex);
            String[] params = paramStr.split(",");

            Set<String> acceptedLangs = new HashSet<String>();
            String defaultLang = params[params.length - 1].trim();

            for (int i = 0; i < (params.length - 1); i++) {
                String param = params[i].trim();
                if (param.startsWith("[")) {
                    param = param.substring(1);
                }
                if (param.endsWith("]")) {
                    param = param.substring(0, param.length() - 1);
                }
                acceptedLangs.add(param);
            }

            String finalLang = acceptedLangs.contains(JahiaGWTParameters.getUILanguage()) ? JahiaGWTParameters.getUILanguage() : defaultLang;
            value = value.replace(value.substring(uiLangIndex, endLangIndex + 1), finalLang);
        }
        if (value.contains("$nodepathnoescape") && selectedNode != null) {
            value = value.replace("$nodepathnoescape", selectedNode.getPath());
        }
        if (value.contains("$nodepath") && selectedNode != null) {
            value = value.replace("$nodepath", com.google.gwt.http.client.URL.encodeQueryString(selectedNode.getPath()));
        }
        if (value.contains("$workspace")) {
            value = value.replace("$workspace", JahiaGWTParameters.getWorkspace());
        }
        if (value.contains("$location-path")) {
            value = value.replace("$location-path", com.google.gwt.http.client.URL.encodeQueryString(Window.Location.getPath()));
        }
        if (value.contains("$location-hash")) {
            value = value.replace("$location-hash", com.google.gwt.http.client.URL.encodeQueryString(Window.Location.getHash()));
        }
        if (value.contains("$dx-version")) {
            value = value.replace("$dx-version", JahiaGWTParameters.getDxVersion());
        }
        return value;
    }

    /**
     * Appends a dummy parameter with a current timestamp to the provided URL. Used when rendering images to force server side reload.
     *
     * @param url
     *            the URL of the image to be modified
     * @return an adjusted URL of the image with timestamp parameter added
     */
    public static String appendTimestamp(String url) {
        if (url == null || url.length() == 0) {
            return url;
        }

        return url + (url.contains("?") ? "&refresh=" : "?refresh=") + System.currentTimeMillis();
    }
}
