/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import static org.jahia.api.Constants.SESSION_USER;
import static org.jahia.api.Constants.UI_THEME;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.subject.Subject;
import org.jahia.api.Constants;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.springframework.core.io.Resource;

/**
 * Miscellaneous request/response handling methods.
 *
 * @author Sergiy Shyrkov
 */
public final class WebUtils {

    /**
     * Does a URL encoding of the <code>path</code>. The characters that don't need encoding are those defined 'unreserved' in section 2.3
     * of the 'URI generic syntax' RFC 2396. Not the entire path string is escaped, but every individual part (i.e. the slashes are not
     * escaped).
     *
     * @param path
     *            the path to encode
     * @return the escaped path
     * @throws NullPointerException
     *             if <code>path</code> is <code>null</code>.
     * @see Text#escapePath(String)
     */
    public static String escapePath(String path) {
        return path != null ? Text.escapePath(path) : null;
    }

    /**
     * Get authenticated subject by performing login using basic authentication credentials, if provided in the current request.
     * then check if this subject has the specified role.
     *
     * @param request current HTTP request
     * @param role the role to be checked
     * @return true if the authenticated subject has the role, false otherwise
     */
    public static boolean authenticatedSubjectHasRole(HttpServletRequest request, String role) {
        Subject subject = SecurityUtils.getSubject();
        if (subject == null) {
            // we have no subject in the current content
            return false;
        }
        if (!subject.isAuthenticated()) {
            String[] authData = getBasicAuthData(request);
            if (authData != null) {
                subject.login(new UsernamePasswordToken(authData[0], authData[1]));
            }
        }

        return subject.hasRole(role);
    }

    /**
     * Returns the username and password pair from provided request if it contains authorization header of type BASIC.
     *
     * @param request current HTTP request
     * @return the username/password pair from the current request header or <code>null</code> if the request does not contain such
     *         information
     */
    public static String[] getBasicAuthData(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null) {
            String[] authStr = header.split(" ");
            if (authStr.length >= 2 && authStr[0].equalsIgnoreCase(HttpServletRequest.BASIC_AUTH)) {
                String decoded = Base64.decodeToString(authStr[1]);
                String[] tokens = decoded.split(":");
                if (tokens.length >= 2) {
                    return new String[] { tokens[0], tokens[1] };
                }
            }
        }
        return null;
    }

    /**
     * Returns the value for the X-UA-Compatible tag, based on the configuration and current UI theme.
     *
     * @param request current HTTP request
     * @return the value for the X-UA-Compatible tag, based on the configuration and current UI theme
     */
    public static String getInternetExplorerCompatibility(HttpServletRequest request) {
        String compatibility = SettingsBean.getInstance().getInternetExplorerCompatibility();
        if (request != null && "IE=10".equals(compatibility)) {
            String theme = getUITheme(request);
            if (theme != null && !"default".equals(theme)) {
                // force IE11 compatibility for non-default theme
                compatibility = "IE=11";
            }
        }
        return compatibility;
    }

    /**
     * Returns the InputStream for the specified servlet context resource.
     *
     * @param path
     *            the resource path (context relative)
     * @return an InputStream for the specified resource content or <code>null</code> if the corresponding resource does not exist
     * @throws IOException
     *             in case of a problem reading resource content
     */
    public static InputStream getResourceAsStream(String path) throws IOException {
        InputStream is = null;
        path = path.length() > 0 && path.charAt(0) != '/' ? "/" + path : path;
        if (path.startsWith("/modules/")) {
            String module = StringUtils.substringAfter(path, "/modules/");
            String remainingPath = StringUtils.substringAfter(module, "/");
            module = StringUtils.substringBefore(module, "/");
            JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                    .getTemplatePackageById(module);
            if (pack != null) {
                Resource r = pack.getResource(remainingPath);
                if (r != null) {
                    is = r.getInputStream();
                }
            }
        } else {
            is = JahiaContextLoaderListener.getServletContext().getResourceAsStream(path);
        }

        return is;
    }

    /**
     * Loads the content of the specified servlet context resource as text.
     *
     * @param path
     *            the resource path (context relative)
     * @return the text of the specified resource content or <code>null</code> if the corresponding resource does not exist
     * @throws IOException
     *             in case of a problem reading resource content
     */
    public static String getResourceAsString(String path) throws IOException {
        InputStream is = getResourceAsStream(path);
        String content = null;
        if (is != null) {
            try {
                content = IOUtils.toString(is);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }

        return content;
    }

    /**
     * Returns the value of the currently active UI theme, considering the theme request parameter, session scope attribute, user
     * preferred theme property and finally the globally configured theme.
     *
     * @param request current HTTP request
     * @return the value for of the currently active UI theme
     */
    public static String getUITheme(HttpServletRequest request) {
        String theme = request.getParameter(UI_THEME);
        if (theme == null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                theme = (String) session.getAttribute(Constants.UI_THEME);
                if (theme == null) {
                    JahiaUser jahiaUser = (JahiaUser) session.getAttribute(SESSION_USER);
                    if (jahiaUser != null) {
                        theme = jahiaUser.getProperty(UI_THEME);
                        if (theme == null) {
                            theme = "jahia-anthracite";
                        }
                    }
                }
            }
        }
        return theme;
    }

    /**
     * Loads the content of the servlet context resource as text looking up the specified paths until the first resource is found.
     *
     * @param lookupPaths
     *            the resource paths to lookup (context relative)
     * @return the text of the specified resource content or <code>null</code> if the corresponding resource does not exist
     * @throws IOException
     *             in case of a problem reading resource content
     */
    public static String lookupResourceAsString(String... lookupPaths) throws IOException {
        String text = null;
        for (String path : lookupPaths) {
            text = getResourceAsString(path);
            if (text != null) {
                break;
            }
        }
        return text;
    }

    /**
     * Sets proper response headers to cache current response for the specified number of seconds.
     *
     * @param expiresSeconds the expiration in seconds
     * @param response
     *            current response object
     */
    public static void setCacheHeaders(long expiresSeconds, HttpServletResponse response) {
        response.setHeader("Cache-Control", "public, max-age=" + expiresSeconds);
        response.setDateHeader("Expires", System.currentTimeMillis() + expiresSeconds * 1000L);
    }

    /**
     * Sets proper response headers to prevent caching of this response.
     *
     * @param response
     *            current response object
     */
    public static void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control",
                "no-cache, no-store, must-revalidate, proxy-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 295075800000L);
    }

    /**
     * Sets proper response headers in case of file download using the provided file name.
     *
     * @param response
     *            current response
     * @param fileName
     *            the file name to use in the response header
     */
    public static void setFileDownloadHeaders(HttpServletResponse response, String fileName) {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
    }

    /**
     * Decodes a <code>application/x-www-form-urlencoded</code> string using the character encoding, configured in jahia.properties.
     *
     * @param url
     *            the string to be decoded
     * @return the decoded string
     */
    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url, SettingsBean.getInstance().getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Initializes an instance of this class.
     */
    private WebUtils() {
        super();
    }

}
