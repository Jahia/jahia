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
 package org.jahia.params;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaSiteNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 5, 2005
 * Time: 3:46:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProcessingContextFactoryImpl implements ProcessingContextFactory {

    public ParamBean getContext(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext servletContext)
            throws JahiaException, JahiaSiteNotFoundException, JahiaPageNotFoundException {
        final URLGenerator urlGenerator = new ServletURLGeneratorImpl(request, response);
        final long startTime = System.currentTimeMillis();
        // get the main http method...
        final String requestMethod = request.getMethod();
        int intRequestMethod = 0;

        if (requestMethod.equals("GET")) {
            intRequestMethod = ProcessingContext.GET_METHOD;
        } else if (requestMethod.equals("POST")) {
            intRequestMethod = ProcessingContext.POST_METHOD;
        }
        final ParamBean paramBean = new ParamBean(request, response, servletContext,
                org.jahia.settings.SettingsBean.getInstance(), startTime,
                intRequestMethod);
        paramBean.setUrlGenerator(urlGenerator);
        return paramBean;
    }

    public ParamBean getContext(final HttpServletRequest request,
                                final HttpServletResponse response,
                                final ServletContext servletContext,
                                final String extraURLParams)
            throws JahiaException, JahiaSiteNotFoundException, JahiaPageNotFoundException {
        final URLGenerator urlGenerator = new ServletURLGeneratorImpl(request, response);
        final long startTime = System.currentTimeMillis();
        // get the main http method...
        final String requestMethod = request.getMethod();
        int intRequestMethod = 0;

        if (requestMethod.equals("GET")) {
            intRequestMethod = ProcessingContext.GET_METHOD;
        } else if (requestMethod.equals("POST")) {
            intRequestMethod = ProcessingContext.POST_METHOD;
        }
        final ParamBean paramBean = new ParamBean(request, response, servletContext,
                org.jahia.settings.SettingsBean.getInstance(), startTime,
                intRequestMethod, extraURLParams);
        paramBean.setUrlGenerator(urlGenerator);
        return paramBean;
    }

    public ProcessingContext getContext(SessionState sessionState) {
        if (sessionState == null) {
            // todo Generate a new session ID in a meaningful way.
            String id = "internal_session_id";
            sessionState = new BasicSessionState(id);
        }
        final URLGenerator urlGenerator = new BasicURLGeneratorImpl();
        final ProcessingContext processingContext = new ProcessingContext();
        processingContext.setUrlGenerator(urlGenerator);
        processingContext.setSessionState(sessionState);
        return processingContext;
    }
}
