/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
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
