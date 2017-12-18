/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
