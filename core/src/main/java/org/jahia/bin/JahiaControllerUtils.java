/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for Jahia specific action controllers.
 * 
 * @author Sergiy Shyrkov
 */
public final class JahiaControllerUtils {

    private static final Logger logger = LoggerFactory.getLogger(JahiaControllerUtils.class);

    public static void checkUserAuthorized(JahiaUser user, String permissions)
            throws JahiaForbiddenAccessException {
        checkUserAuthorized(null, user, permissions);
    }

    public static void checkUserAuthorized(JCRNodeWrapper node, JahiaUser user, String permissions)
            throws JahiaForbiddenAccessException {
        try {
            if (JahiaUserManagerService.isGuest(user)) {
                throw new JahiaUnauthorizedException(
                        "You need to authenticate yourself to use this service");
            } else if (node != null ? !hasRequiredPermission(node, user, permissions)
                    : !hasRequiredPermission(user, permissions)) {
                throw new JahiaForbiddenAccessException(
                        "You have not enough permissions to use this service");
            }
        } catch (RepositoryException e) {
            logger.error("Cannot get permission", e);
            throw new JahiaForbiddenAccessException(
                    "You have not enough permissions to use this service");
        }
    }

    public static void checkUserLoggedIn(JahiaUser user) throws JahiaForbiddenAccessException {
        if (JahiaUserManagerService.isGuest(user)) {
            throw new JahiaUnauthorizedException(
                    "You need to authenticate yourself to use this service");
        }
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
    public final static String getParameter(final HttpServletRequest request, final String name)
            throws JahiaBadRequestException {
        final String value = request.getParameter(name);
        if (value == null) {
            throw new JahiaBadRequestException("Missing required '" + name
                    + "' parameter in request.");
        }
        return value;
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
    public final static String getParameter(final HttpServletRequest request, final String name,
            String defaultValue) {
        final String value = request.getParameter(name);
        return value != null ? value : defaultValue;
    }

    public static boolean hasRequiredPermission(JahiaUser user, String permissions)
            throws RepositoryException {
        return StringUtils.isNotEmpty(permissions) ? hasRequiredPermission(JCRSessionFactory
                .getInstance().getCurrentUserSession().getRootNode(), user, permissions) : true;
    }

    public static boolean hasRequiredPermission(JCRNodeWrapper node, JahiaUser user,
            String permissions) throws RepositoryException {
        if (StringUtils.isEmpty(permissions)) {
            return true;
        }

        boolean hasPermission = false;

        if (permissions.contains("|") || permissions.contains("+")) {
            // we have multiple permissions
            if (permissions.contains("|") && permissions.contains("+")) {
                throw new IllegalArgumentException("Unsupported permission format '" + permissions
                        + "'");
            }
            boolean andOperator = permissions.contains("+");
            String[] parsedPermissions = permissions.split(andOperator ? "\\+" : "\\|");
            hasPermission = andOperator;
            for (String perm : parsedPermissions) {
                hasPermission = node.hasPermission(perm.trim());
                if (andOperator) {
                    if (!hasPermission) {
                        break;
                    }
                } else if (hasPermission) {
                    break;
                }
            }
        } else {
            hasPermission = node.hasPermission(permissions);
        }
        return hasPermission;
    }

    /**
     * Initializes an instance of this class.
     */
    private JahiaControllerUtils() {
        super();
    }
}
