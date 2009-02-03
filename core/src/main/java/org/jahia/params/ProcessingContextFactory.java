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
