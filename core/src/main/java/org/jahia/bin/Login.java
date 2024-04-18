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
package org.jahia.bin;

import org.apache.commons.lang.StringUtils;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.seo.urlrewrite.SessionidRemovalResponseWrapper;
import org.jahia.settings.SettingsBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

/**
 * Login action controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:38 PM
 */
public class Login implements Controller {

    // TODO move this into configuration
    private static final String CONTROLLER_MAPPING = "/login";
    private static final String LOGIN_ERR_PARAM_NAME = "loginError";
    private static final String FAIL_REDIRECT_PARAM = "failureRedirect";
    private static final Pattern ERR_PARAM_NOT_ALONE_PATTERN = Pattern.compile("\\?" + LOGIN_ERR_PARAM_NAME + "=([^&]+)\\&");
    private static final Pattern ERR_PARAM_ALONE_PATTERN = Pattern.compile("\\?" + LOGIN_ERR_PARAM_NAME + "=([^&]+)");
    private static final Pattern ERR_PARAM_END_PATTERN = Pattern.compile("\\&" + LOGIN_ERR_PARAM_NAME + "=([^&]+)");

    public static String getMapping() {
        return CONTROLLER_MAPPING;
    }

    public static String getServletPath() {
        // TODO move this into configuration
        return "/cms" + CONTROLLER_MAPPING;
    }

    protected String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        // Method only called when the login is successful
        String redirect = StringUtils.defaultIfEmpty(request.getParameter("redirect"),
                request.getContextPath() + "/welcome");
        redirect = response.encodeRedirectURL(Login.removeErrorParameter(redirect));
        if (SettingsBean.getInstance().isDisableJsessionIdParameter()) {
            redirect = SessionidRemovalResponseWrapper.removeJsessionId(redirect);
        }
        return redirect;
    }

    /**
     * Process the request and return a ModelAndView object which the DispatcherServlet
     * will render. A <code>null</code> return value is not an error: It indicates that
     * this object completed request processing itself, thus there is no ModelAndView
     * to render.
     *
     * @param request  current HTTP request
     * @param response current HTTP response
     * @return a ModelAndView to render, or <code>null</code> if handled directly
     * @throws Exception in case of errors
     */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {

        boolean restMode = Boolean.valueOf(request.getParameter("restMode"));
        boolean redirectActive = !restMode;
        if (redirectActive) {
            String redirectActiveStr = request.getParameter("redirectActive");
            if (redirectActiveStr != null) {
                redirectActive = Boolean.parseBoolean(redirectActiveStr);
            }
        }

        String result = (String) request.getAttribute(LoginEngineAuthValveImpl.VALVE_RESULT);
        if (LoginEngineAuthValveImpl.OK.equals(result)) {
            // Create one session at login to initialize external user
            JCRSessionFactory.getInstance().getCurrentUserSession();

            if (redirectActive && isAuthorizedRedirect(request, request.getParameter("redirect"), true)) {
                response.sendRedirect(getRedirectUrl(request, response));
            } else {
                response.getWriter().append("OK");
            }
        } else {
            if (!restMode) {
                if (isAuthorizedRedirect(request, request.getParameter(FAIL_REDIRECT_PARAM), false)) {
                    if ("bad_password".equals(result)) {
                        result = "unknown_user";
                    }
                    //[QA-7582] FIXME: we need to sanitize the URL a dirty quick fix to avoid working on jsp templates that depend on this Servlet
                    String failureRedirectUrl = sanitizeRedirectURL(request);
                    StringBuilder url = new StringBuilder(64);
                    url.append(failureRedirectUrl);
                    if (failureRedirectUrl.indexOf('?') == -1) {
                        url.append("?" + LOGIN_ERR_PARAM_NAME + "=").append(result);
                    } else if (failureRedirectUrl.indexOf("?" + LOGIN_ERR_PARAM_NAME + "=") == -1
                            && failureRedirectUrl.indexOf("&loginError=") == -1) {
                        url.append("&" + LOGIN_ERR_PARAM_NAME + "=").append(result);
                    }
                    response.sendRedirect(url.toString());
                } else {
                    if (request.getParameter("redirect") != null) {
                        request.setAttribute("javax.servlet.error.request_uri", request.getParameter("redirect"));
                    }
                    String theme = "jahia-anthracite";
                    String pathToCheck = "/errors/" + theme + "/error_401.jsp";
                    if (request.getServletContext().getResource(pathToCheck) != null) {
                        request.getRequestDispatcher("/errors/" + theme + "/error_401.jsp").forward(request, response);
                        return null;
                    }
                    request.getRequestDispatcher("/errors/error_401.jsp").forward(request, response);
                }
            } else {
                response.getWriter().append("unauthorized");
            }
        }
        return null;
    }

    private String sanitizeRedirectURL(HttpServletRequest request) {
        String failureRedirectUrl = request.getParameter(FAIL_REDIRECT_PARAM);
        if (StringUtils.isNotEmpty(failureRedirectUrl) && failureRedirectUrl.indexOf(LOGIN_ERR_PARAM_NAME) > -1) {
            // remove failure redirect param before we add it again
            String[] urlParts = StringUtils.split(failureRedirectUrl, "\\?");
            StringBuilder sanitizedUrlSb = new StringBuilder(urlParts[0]);
            if (urlParts.length > 1) {
                String[] urlParams = StringUtils.split(urlParts[1], "&");
                StringBuilder paramSb = new StringBuilder();
                for (int i = 0; i < urlParams.length; i++) {
                    if (urlParams[i].indexOf(LOGIN_ERR_PARAM_NAME) == -1) {
                        paramSb.append(urlParams[i]);
                    }
                }
                if (StringUtils.isNotEmpty(paramSb.toString())) {
                    sanitizedUrlSb.append("?").append(paramSb.toString());
                }

            }

            failureRedirectUrl = sanitizedUrlSb.toString();
        }
        return failureRedirectUrl;
    }

    protected static boolean isAuthorizedRedirect(HttpServletRequest request, String redirectUrl, boolean authorizeNullRedirect) {
        if (redirectUrl == null) {
            return authorizeNullRedirect;
        }

        try {
            URI currentUri = new URI(request.getRequestURL().toString());
            URI redirectUri = new URI(redirectUrl);

            boolean isProtocolRelativeUrl = redirectUrl.startsWith("//");
            if (redirectUri.isAbsolute() || isProtocolRelativeUrl) {
                for (String authorizedRedirectHost : SettingsBean.getInstance().getAuthorizedRedirectHosts()) {
                    if (redirectUri.getHost().equalsIgnoreCase(authorizedRedirectHost)) {
                        return true;
                    }
                }

                // Check if the host (domain) of the redirect URL is the same as the current URL
                return currentUri.getHost().equalsIgnoreCase(redirectUri.getHost()) && currentUri.getPort() == redirectUri.getPort();
            } else {
                // Relative URL, consider it as same domain
                return true;
            }
        } catch (URISyntaxException e) {
            // Handle invalid URLs
            return false;
        }
    }

    // Only protected for test purposes
    protected static String removeErrorParameter(String redirect) {
        redirect = ERR_PARAM_NOT_ALONE_PATTERN.matcher(redirect).replaceAll("\\?");
        redirect = ERR_PARAM_ALONE_PATTERN.matcher(redirect).replaceAll("");
        redirect = ERR_PARAM_END_PATTERN.matcher(redirect).replaceAll("");
        return redirect;
    }
}
