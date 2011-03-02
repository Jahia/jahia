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

package org.jahia.bin.errors;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
import javax.jcr.AccessDeniedException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaNotFoundException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.usermanager.JahiaUserManagerService;

/**
 * Handler class for captured exceptions.
 * 
 * @author Sergiy Shyrkov
 */
public class DefaultErrorHandler implements ErrorHandler {

    private static final Pattern ERROR_CODE_PATTERN = Pattern
            .compile("\\d\\d\\d");

    /**
     * Tries to parse the integer response error code from the exception
     * message. In case of failure, returns <code>0</code>.
     * 
     * @param t
     *            the catched exception
     * @return the integer response error code parsed from the exception
     *         message. In case of failure, returns <code>0</code>
     */
    private static int getErrorCode(Throwable t) {

        int code = SC_INTERNAL_SERVER_ERROR;
        if (t != null && StringUtils.isNotEmpty(t.getMessage())) {
            String errorString = null;
            Matcher m = ERROR_CODE_PATTERN.matcher(t.getMessage());
            if (m.find()) {
                errorString = m.group();
                try {
                    code = Integer.parseInt(errorString);
                } catch (NumberFormatException e) {
                    // ignore it
                }
            }
        }

        return code >= 400 && code < 600 ? code : SC_INTERNAL_SERVER_ERROR;
    }

    /**
     * Returns an instance of the error handler, configured in the
     * <code>applicationcontext-basejahiaconfig.xml</code> file.
     * 
     * @return instance of the error handler class (a subclass of
     *         {@link DefaultErrorHandler})
     */
    public static DefaultErrorHandler getInstance() {
        return (DefaultErrorHandler) SpringContextSingleton.getBean("org.jahia.bin.errors.handler");
    }

    /**
     * Method handles all types of exceptions that can occur during processing
     * depending on the exception type.
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
    public boolean handle(Throwable e, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        int code = SC_INTERNAL_SERVER_ERROR;
        if (e instanceof ServletException) {
            e = ((ServletException) e).getRootCause();
        } if (e instanceof RenderException && e.getCause() != null) {
            e = e.getCause();
        }

        if (e instanceof PathNotFoundException) {
        	code = SC_NOT_FOUND;
        } else if (e instanceof TemplateNotFoundException) {
        	code = SC_NOT_FOUND;
        } else if (e instanceof AccessDeniedException) {
            if (JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser())) {
                code = SC_UNAUTHORIZED;
            } else {
                code = SC_FORBIDDEN;
            }
        } else if (e instanceof JahiaException) {
            if (e instanceof JahiaSessionExpirationException) {
                code = SC_BAD_REQUEST;
            } else if (e instanceof JahiaForbiddenAccessException) {
                code = SC_FORBIDDEN;
            } else {
                code = getErrorCode(e);
            }
        } else if (e instanceof JahiaRuntimeException) {
            if (e instanceof JahiaBadRequestException) {
                code = SC_BAD_REQUEST;
            } else if (e instanceof JahiaUnauthorizedException) {
                code = SC_UNAUTHORIZED;
            } else if (e instanceof JahiaNotFoundException) {
                code = SC_NOT_FOUND;
            } else {
//                throw (JahiaRuntimeException) e;
            }
        } else if (e instanceof ClassNotFoundException) {
            code = SC_BAD_REQUEST;
        } else if (e instanceof IOException) {
//            throw (IOException) e;
        } else if (e instanceof ServletException) {
//            throw (ServletException) e;
        } else {
//            throw new ServletException(e);
        }

        if (code != 0) {
            request.setAttribute("org.jahia.exception", e);

            StringWriter traceWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(traceWriter));
            traceWriter.flush();
            request.setAttribute("org.jahia.exception.trace", traceWriter
                    .getBuffer().toString());

            // for backward compatibility
            request.setAttribute("org.jahia.exception.Message", e.getMessage());
            request.setAttribute("org.jahia.exception.StackTrace", traceWriter
                    .getBuffer().toString());

            if (!response.isCommitted()) {
                // set proper error code (and use the trick, because Tomcat will not use custom error page for 503 error)
                response.sendError(code != SC_SERVICE_UNAVAILABLE ? code : SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
            return true;
        }
        return false;
    }

}
