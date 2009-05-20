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

import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaSiteNotFoundException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Mar 5, 2005
 * Time: 2:13:58 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ProcessingContextFactory {

    /**
     * Get a processing context for a servlet request context. This will usually construct
     * a usual ParamBean object.
     * @param request
     * @param response
     * @param servletContext
     */
    public ParamBean getContext(HttpServletRequest request,
                                HttpServletResponse response,
                                ServletContext servletContext)
            throws JahiaException, JahiaSiteNotFoundException, JahiaPageNotFoundException;

    /**
     * Get a processing context for a servlet request context. This will usually construct
     * a usual ParamBean object.
     * @param request
     * @param response
     * @param servletContext
     * @param extraURLParams This String will be appended to the PathInfo when constructing the ParamBean
     */
    public ParamBean getContext(HttpServletRequest request,
                                HttpServletResponse response,
                                ServletContext servletContext,
                                String extraURLParams)
            throws JahiaException, JahiaSiteNotFoundException, JahiaPageNotFoundException;


    /**
     * Get a processing context for a non-servlet context. This is mostly used by test cases
     * but can also be used in other setups where Jahia is not interacting through HTTP
     * interfaces.
     * @param sessionState a session state object that contains the data related to the
     * current session. May be null if no session state exists for the moment.
     */
    public ProcessingContext getContext(SessionState sessionState);

}
