/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.securityfilter.core;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.modulemanager.util.PropertiesValues;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class ParserHelper {
    private ParserHelper() {
    }

    public static Set<String> buildSet(PropertiesValues nodeValues, String prop) {
        if (nodeValues.getProperty(prop) == null) {
            return Collections.emptySet();
        }
        return new LinkedHashSet<>(Arrays.asList(StringUtils.split(nodeValues.getProperty(prop), ", ")));
    }

    /**
     * Utility function to check request origin.
     * Source from Tomcat implementation.
     * https://github.com/apache/tomcat/blob/3e6abeeec3f88fad7825880a5a963e1be34f6204/java/org/apache/tomcat/util/http/RequestUtil.java
     */
    public static boolean isSameOrigin(HttpServletRequest request, String origin) {
        return isSameOrigin(request.getScheme(), request.getServerName(), request.getServerPort(), origin);
    }

    public static boolean isSameOriginFromHeader(HttpServletRequest request, String origin) {
        String requestOrigin = request.getHeader("Origin");
        if (requestOrigin == null) {
            // Get the referer in case origin is not available
            requestOrigin = request.getHeader("Referer");
            // find 3rd "/" in the request
            int delimiterPos = StringUtils.ordinalIndexOf(requestOrigin, "/", 3);
            if (delimiterPos > 0) {
                requestOrigin = StringUtils.substring(requestOrigin, 0, delimiterPos);
            }
        }
        if (requestOrigin != null) {
            // In case of hosted or same, compare to the request
            if (origin.equals("hosted") || origin.equals("same")) {
                return isSameOrigin(request, requestOrigin);
            } else {
                return isSameOrigin(origin, requestOrigin);
            }
        }
        return false;
    }

    public static boolean isSameOrigin(String originToCheck, String origin) {
        try {
            URL originURL = new URL(originToCheck);
            int port = originURL.getPort() == -1 ? originURL.getDefaultPort() : originURL.getPort();
            return isSameOrigin(originURL.getProtocol(), originURL.getHost(), port, origin);
        } catch (MalformedURLException e) {
            // Unable to parse the URL
        }
        return false;
    }

    private static boolean isSameOrigin(String scheme, String host, int port, String origin) {
        // Build scheme://host:port from request
        StringBuilder target = new StringBuilder();
        if (scheme == null) {
            return false;
        } else {
            scheme = scheme.toLowerCase(Locale.ENGLISH);
        }
        target.append(scheme);
        target.append("://");

        if (host == null) {
            return false;
        }
        target.append(host);

        // Origin may or may not include the (default) port.
        // At this point target doesn't include a port.
        if (target.length() == origin.length()) {
            // origin and target can only be equal if both are using default
            // ports. Therefore only append the port to the target if a
            // non-default port is used.
            if (("http".equals(scheme) || "ws".equals(scheme)) && port != 80 ||
                    ("https".equals(scheme) || "wss".equals(scheme)) && port != 443) {
                target.append(':');
                target.append(port);
            }
        } else {
            // origin and target can only be equal if:
            // a) origin includes an explicit default port
            // b) origin is using a non-default port
            // Either way, add the port to the target so it can be compared
            target.append(':');
            target.append(port);
        }


        // Both scheme and host are case-insensitive but the CORS spec states
        // this check should be case-sensitive
        return origin.equals(target.toString());
    }
}
