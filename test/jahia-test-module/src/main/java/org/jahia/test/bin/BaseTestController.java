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
            JCRSessionFactory.getInstance().setCurrentUser(JahiaAdminUser.getAdminUser(null));

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
