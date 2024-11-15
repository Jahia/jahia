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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.seo.urlrewrite;

import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.regex.Pattern;

/**
 * Simple filter that removes the ;jsessionid string
 */
public class SessionidRemovalResponseWrapper extends HttpServletResponseWrapper {
    private HttpServletRequest request;

    private static Pattern cleanPattern = Pattern.compile(";"+SettingsBean.getInstance().getJsessionIdParameterName()+"=[^\\?#]*");

    public SessionidRemovalResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    @Override
    public String encodeURL(String url) {
        return clean(super.encodeURL(url));
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    /**
     * Remove all jsession parameter if set in the settings, or replace it by a macro marker if we are in a rendered page
     * macro in that case)
     * @param url
     * @return
     */
    private String clean(String url) {
        if (SettingsBean.getInstance().isDisableJsessionIdParameter() || isInRender()) {
            // Remove the jsessionid= part
            url = removeJsessionId(url);

            // If we are in jahia page, and jsession ids are not disabled, replace by a macro
            if (isInRender() && !SettingsBean.getInstance().isDisableJsessionIdParameter() && !url.contains("##sessionid##")) {
                url += "##sessionid##";
            }
        }
        return url;
    }

    public static String removeJsessionId(String url) {
        String s = ";" + SettingsBean.getInstance().getJsessionIdParameterName();
        if (url.contains(s)) {
            url = cleanPattern.matcher(url).replaceFirst("");
        }
        return url;
    }

    private boolean isInRender() {
        return request.getAttribute("currentNode") != null;
    }

}
