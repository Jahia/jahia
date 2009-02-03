/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.actions.DispatchAction;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ProcessingContext;

/**
 * Base class for Ajax actions that are using method dispatching.
 * 
 * @author Sergiy Shyrkov
 */
public class AjaxDispatchAction extends DispatchAction {

    /**
     * Method handles all types of exceptions that can occur during processing
     * of an Ajax action depending on the exception type.
     * 
     * @param e
     *            the exception, occurred during processing
     * @param request
     *            current request object
     * @param response
     *            current response object
     * @throws IOException
     *             propagates the original exception if it is an instance or
     *             subclass of {@link IOException}
     * @throws ServletException
     *             propagates the original exception if it is an instance or
     *             subclass of {@link ServletException} or wraps the original
     *             exception into ServletException to propagate it further
     */
    protected static final void handleException(Exception e,
            HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        AjaxAction.handleException(e, request, response);
    }

    /**
     * Set appropriate headers to disable browser response cache.
     * 
     * @param response
     *            current response object
     */
    protected static void setNoCacheHeaders(HttpServletResponse response) {
        AjaxAction.setNoCacheHeaders(response);
    }

    /**
     * Simple utility method to retreive a parameter from a request and throws
     * an {@link JahiaBadRequestException} (results in a 400 error) in case the
     * parameter is not found.
     * 
     * @param request
     *            The current HttpServletRequest
     * @param name
     *            The parameter name
     * @return A String containing the value of the given parameter
     * @throws JahiaBadRequestException
     *             in case the parameter is not found in the request
     */
    protected final String getParameter(final HttpServletRequest request,
            final String name) throws JahiaBadRequestException {
        return AjaxAction.getParameter(request, name);
    }

    public ProcessingContext retrieveProcessingContext(
            HttpServletRequest request, HttpServletResponse response,
            String parameters, boolean forceValidUser)
            throws JahiaBadRequestException, JahiaUnauthorizedException,
            ServletException, IOException {

        return AjaxAction.retrieveProcessingContext(request, response,
                getServlet().getServletContext(), parameters, forceValidUser);
    }
}
