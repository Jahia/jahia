/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.bin;

import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.CookieAuthConfig;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Logout controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:45 PM
 */
public class Logout implements Controller {

    private CookieAuthConfig cookieAuthConfig;

    public void setCookieAuthConfig(CookieAuthConfig cookieAuthConfig) {
        this.cookieAuthConfig = cookieAuthConfig;
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

        if (cookieAuthConfig.isActivated()) {
            removeAuthCookie(request, response);
        }
        Locale uiLocale = (Locale) request.getSession().getAttribute(ProcessingContext.SESSION_UI_LOCALE);
        Locale locale = (Locale) request.getSession().getAttribute(ProcessingContext.SESSION_LOCALE);
        
        request.getSession().invalidate();

        request.getSession().setAttribute(ProcessingContext.SESSION_UI_LOCALE, uiLocale);
        request.getSession().setAttribute(ProcessingContext.SESSION_LOCALE, locale);
        
        String redirectActiveStr = request.getParameter("redirectActive");
        boolean redirectActive = true;
        if (redirectActiveStr != null) {
            redirectActive = Boolean.parseBoolean(redirectActiveStr);
        }

        String redirect = request.getParameter("redirect");
        if (redirect == null) {
            redirect = request.getHeader("referer");
        }

        if (redirectActive) {
            response.sendRedirect(StringUtils.isEmpty(redirect) ? "/" : redirect);
        }
        return null;
    }

    private void removeAuthCookie(HttpServletRequest request, HttpServletResponse response) {
        // now let's destroy the cookie authentication if there was one
        // set for this user.
        JahiaUser curUser = JCRSessionFactory.getInstance().getCurrentUser();
        String cookieAuthKey = curUser.getProperty(cookieAuthConfig.getUserPropertyName());
        if (cookieAuthKey != null) {
            Cookie authCookie = new Cookie(cookieAuthConfig.getCookieName(), cookieAuthKey);
            authCookie.setPath(StringUtils.isNotEmpty(request.getContextPath()) ? request.getContextPath() : "/");
            authCookie.setMaxAge(0); // means we want it deleted now !
            response.addCookie(authCookie);
            curUser.removeProperty(cookieAuthConfig.getUserPropertyName());
        }
    }

    public static String getLogoutServletPath() {
        // TODO move this into configuration
        return "/cms/logout";
    }

}
