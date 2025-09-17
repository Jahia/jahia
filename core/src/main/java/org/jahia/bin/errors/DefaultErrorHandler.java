/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.errors;

import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

import java.io.IOException;
import java.io.StringWriter;

import javax.jcr.AccessDeniedException;
import javax.jcr.PathNotFoundException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.exceptions.*;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.render.AjaxRenderException;
import org.jahia.services.render.RenderException;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.settings.SettingsBean;

/**
 * Handler class for captured exceptions.
 *
 * @author Sergiy Shyrkov
 */
public class DefaultErrorHandler implements ErrorHandler {

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
        } else if (e instanceof AjaxRenderException) {
            code = SC_NOT_FOUND;
        } else if (e instanceof AccessDeniedException) {
            if (JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser())) {
                code = SC_UNAUTHORIZED;
            } else if (e instanceof RequiredActionModeException) {
                code = SC_METHOD_NOT_ALLOWED;
            } else {
                code = SC_FORBIDDEN;
            }
        } else if (e instanceof JahiaException) {
            code = ((JahiaException) e).getResponseErrorCode();
        } else if (e instanceof JahiaRuntimeException) {
            if (e instanceof JahiaBadRequestException) {
                code = SC_BAD_REQUEST;
            } else if (e instanceof JahiaUnauthorizedException) {
                code = SC_UNAUTHORIZED;
            } else if (e instanceof JahiaNotFoundException) {
                code = SC_NOT_FOUND;
            } else if (e instanceof JahiaServiceUnavailableException) {
                code = SC_SERVICE_UNAVAILABLE;
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

            if (SettingsBean.getInstance().isDevelopmentMode()) {
                StringWriter traceWriter = new StringWriter();
                traceWriter.append(StackTraceFilter.printStackTrace(e));
                traceWriter.flush();
                request.setAttribute("org.jahia.exception.trace", traceWriter
                        .getBuffer().toString());
            }

            if (!response.isCommitted()) {
                response.sendError(code, e.getMessage());
            }
            return true;
        }
        return false;
    }

}
