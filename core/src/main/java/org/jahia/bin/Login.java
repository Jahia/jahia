/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.bin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.settings.SettingsBean;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Login action controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:38 PM
 */
public class Login implements Controller {
    
    // TODO move this into configuration
    private static final String CONTROLLER_MAPPING = "/login";
    
    public static String getMapping() {
        return CONTROLLER_MAPPING;
    }
    
    public static String getServletPath() {
        // TODO move this into configuration
        return "/cms" + CONTROLLER_MAPPING;
    }
    
    protected String getRedirectUrl(HttpServletRequest request, HttpServletResponse response) {
        return response.encodeRedirectURL(StringUtils.defaultIfEmpty(request.getParameter("redirect"),
                request.getContextPath() + "/welcome"));
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
                if (isAuthorizedRedirect(request, request.getParameter("failureRedirect"), false)) {
                    if ("bad_password".equals(result)) {
                        result = "unknown_user";
                    }
                    response.sendRedirect(request.getParameter("failureRedirect")+"?loginError="+result);
                } else {
                    if (request.getParameter("redirect") != null) {
                        request.setAttribute("javax.servlet.error.request_uri", request.getParameter("redirect"));
                    }
                    request.getRequestDispatcher("/errors/error_401.jsp").forward(request, response);
                }
            } else {
                response.getWriter().append("unauthorized");
            }
        }
        return null;
    }

    protected static boolean isAuthorizedRedirect(HttpServletRequest request, String redirectUrl, boolean authorizeNullRedirect) {
        if (redirectUrl == null) {
            return authorizeNullRedirect;
        }
        if (redirectUrl.contains("://")) {
            if (redirectUrl.startsWith("http://") || redirectUrl.startsWith("https://")) {
                String redirectUrlAfterProtocol = StringUtils.substringAfter(redirectUrl, "://");
                String urlBase = StringUtils.substringAfter(StringUtils.removeEnd(request.getRequestURL().toString(), request.getRequestURI().toString()), "://");
                if (redirectUrlAfterProtocol.startsWith(urlBase)) {
                    return true;
                }
                for (String authorizedRedirectHost : SettingsBean.getInstance().getAuthorizedRedirectHosts()) {
                    if (redirectUrlAfterProtocol.startsWith(authorizedRedirectHost)) {
                        return true;
                    }
                }
            }
            // Block non-HTTP URls, like ftp://...
            return false;
        }
        // Block any other absolute URLs, like mailto:some@mail.com or even http:\\www.somedomain.com, which works on Google Chrome
        // Second part of the test is to allow relative URLs that contain a colon in the end
        int indexOfColon = redirectUrl.indexOf(":");
        int indexOfSlash = redirectUrl.indexOf("/");
        if (indexOfColon >= 0 && (indexOfSlash < 0 || indexOfColon < indexOfSlash)) {
            return false;
        }
        // relative URL
        return true;
    }

}
