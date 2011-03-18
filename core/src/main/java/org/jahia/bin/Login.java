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

import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.params.valves.LoginEngineAuthValveImpl;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Login action controller.
 * User: toto
 * Date: Nov 17, 2009
 * Time: 1:47:38 PM
 */
public class Login implements Controller {
	
    private JahiaSitesBaseService sitesService;

    public static String getServletPath() {
        // TODO move this into configuration
        return "/cms/login";
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
        // Login done by parambean and auth-valve

        boolean restMode = Boolean.valueOf(request.getParameter("restMode"));
        boolean redirectActive = !restMode;
        if (redirectActive) {
            String redirectActiveStr = request.getParameter("redirectActive");
            if (redirectActiveStr != null) {
                redirectActive = Boolean.parseBoolean(redirectActiveStr);    
            }
        }

        String redirect = null;
        if (redirectActive) {
            redirect = request.getParameter("redirect");
            if (redirect == null || redirect.length() == 0) {
                JahiaSite site = sitesService.getDefaultSite();
                if (site == null) {
                	Iterator<JahiaSite> sites = sitesService.getSites();
                	site = sites.hasNext() ? sites.next() : null;
                }
				redirect = site != null ? request.getContextPath()
						+ "/cms/render/default/" + site.getDefaultLanguage()
						+ "/sites/" + site.getSiteKey() + "/home.html"
						: request.getContextPath() + "/welcome";
            }
        }

        String result = (String) request.getAttribute(LoginEngineAuthValveImpl.VALVE_RESULT);
        if (LoginEngineAuthValveImpl.OK.equals(result)) {

            // Create one session at login to initialize external user
            JCRSessionFactory.getInstance().getCurrentUserSession();

            if (redirectActive) {
                response.sendRedirect(response.encodeRedirectURL(redirect));
            } else {
                response.getWriter().append("OK");
            }
        } else {
            if (!restMode) {
                request.setAttribute("javax.servlet.error.request_uri", redirect);
                request.getRequestDispatcher("/errors/error_401.jsp").forward(request, response);
            } else {
                response.getWriter().append("unauthorized");
            }
        }
        return null;
    }

    public void setSitesService(JahiaSitesBaseService sitesService) {
        this.sitesService = sitesService;
    }

}
