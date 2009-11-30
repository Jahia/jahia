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
package org.jahia.ajax.gwt.client.util;

import com.google.gwt.core.client.GWT;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;

/**
 * Created by Jahia.
 * User: ktlili
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
    public static String getAbsolutleURL(String url) {
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
     * REqrite url (ie. used for richtext)
     *
     * @param url
     * @return
     */
    public static String rewrite(String url) {
        if (url == null) {
            return null;
        }else if(url.indexOf("/##mode##/##lang##/") > 0){
            // already rewited
            return url;
        } else {

            // absolute url are not processed
            String[] splittedUrl = url.split("/");
            /*splittedUrl[0] = "http:"
            splittedUrl[1] = ""
            splittedUrl[2] = "www.example.com" */
            if (splittedUrl.length > 2) {
                if (splittedUrl[1].length() == 0) {
                    return url;
                }
            }

            // at this level, we are sur that its a relative url
            String prefix = "";
            if(JahiaGWTParameters.getServletPath() != null && JahiaGWTParameters.getServletPath().length()> 0){
                prefix = JahiaGWTParameters.getServletPath();
            }
            if(JahiaGWTParameters.getContextPath() != null && JahiaGWTParameters.getContextPath().length()> 0){
                prefix = prefix+JahiaGWTParameters.getContextPath();
            }
            prefix = prefix+"/##mode##/##lang##";
            if (url.charAt(0) == '/') {
                return prefix + url;
            } else {
                return prefix + "/" + url;      
            }
        }
    }

    /**
     * Get window urle
     *
     * @return
     */
    public static native String getWindowUrl() /*-{
    return $wnd.location.href;

}-*/;
}
