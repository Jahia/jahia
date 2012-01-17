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

import javax.servlet.http.HttpServletRequest;

import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.JahiaUser;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * Base class for Jahia specific action controllers.
 * 
 * @author Sergiy Shyrkov
 */
public abstract class JahiaMultiActionController extends MultiActionController {

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

    private String requiredPermission;

    protected void checkUserAuthorized() throws JahiaForbiddenAccessException {
        JahiaControllerUtils.checkUserAuthorized(getCurrentUser(), getRequiredPermission());
    }

    protected void checkUserAuthorized(JCRNodeWrapper node) throws JahiaForbiddenAccessException {
        JahiaControllerUtils.checkUserAuthorized(node, getCurrentUser(), getRequiredPermission());
    }

    protected void checkUserLoggedIn() throws JahiaForbiddenAccessException {
        JahiaControllerUtils.checkUserLoggedIn(getCurrentUser());
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

    /**
     * Sets the permission, required to handle this action. <code>null</code> if no particular permission is required.
     * 
     * @param requiredPermission
     *            the permission, required to handle this action. <code>null</code> if no particular permission is required
     */
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

}
