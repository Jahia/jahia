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
package org.jahia.bin;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.springframework.web.servlet.mvc.Controller;

/**
 * Base class for Jahia specific action controllers.
 *
 * @author Sergiy Shyrkov
 */
public abstract class JahiaController implements Controller {

    /**
     * Simple utility method to retrieve an integer parameter from a request and throws an {@link JahiaBadRequestException} (results in a 400 error)
     * in case the parameter is not found.
     *
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @return an integer value of the given parameter
     * @throws JahiaBadRequestException
     *             in case the parameter is not found in the request or a parsing exception occurs
     */
    protected final static int getIntParameter(final HttpServletRequest request, final String name)
            throws JahiaBadRequestException {
        return JahiaControllerUtils.getIntParameter(request, name);
    }

    /**
     * Retrieves the integer value of the specified request parameter. If it does not exist, returns the provided default value.
     *
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @param defaultValue
     *            the default parameter value
     * @return the value of the specified request parameter. If it does not exist, returns the provided default value
     */
    protected final static int getIntParameter(final HttpServletRequest request, final String name,
            int defaultValue) {
        return JahiaControllerUtils.getIntParameter(request, name, defaultValue);
    }

    /**
     * Simple utility method to retrieve a parameter from a request and throws an {@link JahiaBadRequestException} (results in a 400 error)
     * in case the parameter is not found.
     *
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @return A String containing the value of the given parameter
     * @throws JahiaBadRequestException
     *             in case the parameter is not found in the request
     */
    protected final static String getParameter(final HttpServletRequest request, final String name)
            throws JahiaBadRequestException {
        return JahiaControllerUtils.getParameter(request, name);
    }

    /**
     * Retrieves the value of the specified request parameter. If it does not exist, returns the provided default value.
     *
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @param defaultValue
     *            the default parameter value
     * @return the value of the specified request parameter. If it does not exist, returns the provided default value
     */
    protected final static String getParameter(final HttpServletRequest request, final String name,
            String defaultValue) {
        return JahiaControllerUtils.getParameter(request, name, defaultValue);
    }

    private boolean requireAuthenticatedUser;

    private String requiredPermission;

    protected void checkUserAuthorized() throws JahiaForbiddenAccessException {
        if (getRequiredPermission() != null) {
            JahiaControllerUtils.checkUserAuthorized(getCurrentUser(), getRequiredPermission());
        }
    }

    protected void checkUserAuthorized(JCRNodeWrapper node) throws JahiaForbiddenAccessException {
        if (getRequiredPermission() != null) {
            JahiaControllerUtils.checkUserAuthorized(node, getCurrentUser(), getRequiredPermission());
        }
    }

    protected void checkUserLoggedIn() throws JahiaForbiddenAccessException {
        if (isRequireAuthenticatedUser()) {
            JahiaControllerUtils.checkUserLoggedIn(getCurrentUser());
        }
    }

    /**
     * Returns the current user.
     *
     * @return current user
     */
    protected JahiaUser getCurrentUser() {
        return JCRSessionFactory.getInstance().getCurrentUser();
    }

    /**
     * Returns the permission, required to handle this action. <code>null</code> if no particular permission is required.
     *
     * @return the permission, required to handle this action. <code>null</code> if no particular permission is required
     */
    protected String getRequiredPermission() {
        return requiredPermission;
    }

    protected boolean isRequireAuthenticatedUser() {
        return requireAuthenticatedUser;
    }

    /**
     * Returns <code>true</code> if the current user is a non-authenticated user.
     *
     * @return <code>true</code> if the current user is a non-authenticated user
     */
    protected boolean isUserGuest() {
        return JahiaUserManagerService.isGuest(getCurrentUser());
    }

    public void setRequireAuthenticatedUser(boolean requireAuthenticatedUser) {
        this.requireAuthenticatedUser = requireAuthenticatedUser;
    }

    /**
     * Sets the permission, required to handle this action. <code>null</code> if no particular permission is required.
     *
     * @param requiredPermission
     *            the permission, required to handle this action. <code>null</code> if no particular permission is required
     */
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = StringUtils.defaultIfBlank(requiredPermission, null);
    }
}
