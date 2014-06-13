/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.bin;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.api.Constants;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.JahiaAdminUser;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Base controller class for the test module which provides required data for the tests: sets the root user as the current one and stores
 * request and response objects into {@link ThreadLocal}.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class BaseTestController implements Controller {

    protected static ThreadLocal<HttpServletRequest> perThreadRequest = new ThreadLocal<HttpServletRequest>();
    protected static ThreadLocal<HttpServletResponse> perThreadResponse = new ThreadLocal<HttpServletResponse>();

    /**
     * Returns the <code>HttpServletRequest</code> object for the current call.
     * 
     * @return current {@link HttpServletRequest} object
     */
    public static final HttpServletRequest getThreadLocalRequest() {
        return perThreadRequest.get();
    }

    /**
     * Returns the <code>HttpServletResponse</code> object for the current call.
     * 
     * @return current {@link HttpServletResponse} object
     */
    public static final HttpServletResponse getThreadLocalResponse() {
        return perThreadResponse.get();
    }

    protected abstract void handleGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException;

    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (request.getMethod().equalsIgnoreCase("get")) {
            // set root as the current user
            JahiaUser oldUser = JCRSessionFactory.getInstance().getCurrentUser();
            JCRSessionFactory.getInstance().setCurrentUser(JahiaAdminUser.getAdminUser(0));

            // set English as session locale
            Locale oldLocale = (Locale) request.getSession(true).getAttribute(Constants.SESSION_LOCALE);
            request.getSession(true).setAttribute(Constants.SESSION_LOCALE, Locale.ENGLISH);

            // set current request / response into ThreadLocal
            perThreadRequest.set(request);
            perThreadResponse.set(response);
            try {
                handleGet(request, response);
            } finally {
                // reset current request / response in ThreadLocal
                perThreadRequest.set(null);
                perThreadResponse.set(null);

                // reset locale
                request.getSession(true).setAttribute(Constants.SESSION_LOCALE, oldLocale);

                // reset user
                JCRSessionFactory.getInstance().setCurrentUser(oldUser);
            }
        } else {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
        return null;
    }
}
