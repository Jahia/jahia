/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.render.scripting;

import org.jahia.utils.StringResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.services.render.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Enumeration;

/**
 * This class uses the standard request dispatcher to execute a JSP / Quercus script or any file handled by the
 * application server.
 *
 * The view path will be used as a resource path on which the request will be dispatched.
 *
 * @author toto
 */
public class RequestDispatcherScript implements Script {

    private static final Logger logger = LoggerFactory.getLogger(RequestDispatcherScript.class);

    protected static void dumpRequestAttributes(HttpServletRequest request) {
        // Let's enumerate request attribute to see what we are exposing.
        @SuppressWarnings("rawtypes")
        Enumeration attributeNamesEnum = request.getAttributeNames();
        while (attributeNamesEnum.hasMoreElements()) {
            String currentAttributeName = (String) attributeNamesEnum.nextElement();
            String currentAttributeValue = request.getAttribute(currentAttributeName).toString();
            if (currentAttributeValue.length() < 80) {
                logger.debug("Request attribute " + currentAttributeName + "=" + currentAttributeValue);
            } else {
                logger.debug("Request attribute " + currentAttributeName + "=" + currentAttributeValue.substring(0,
                        80) + " (first 80 chars)");
            }
        }
    }

    private View view;

    /**
     * Builds the script object
     *
     */
    public RequestDispatcherScript(View view) {
        this.view = view;
    }

    /**
     * Execute the script and return the result as a string
     *
     * @param resource resource to display
     * @param context
     * @return the rendered resource
     * @throws org.jahia.services.render.RenderException
     */
    public String execute(Resource resource, RenderContext context) throws RenderException {
        if (view == null) {
            throw new RenderException("View not found for : " + resource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("View '" + view + "' resolved for resource: " + resource);
            }
        }

        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();
        RequestDispatcher rd = request.getRequestDispatcher(view.getPath());

        Object oldModule = request.getAttribute("currentModule");
        request.setAttribute("currentModule", view.getModule());

        if (logger.isDebugEnabled()) {
            dumpRequestAttributes(request);
        }
        
        StringResponseWrapper wrapper = new StringResponseWrapper(response);
        try {
            rd.include(request, wrapper);
        } catch (ServletException e) {
            while (e.getRootCause() instanceof ServletException) {
                e = (ServletException) e.getRootCause();
            }
            if (e.getRootCause() instanceof RenderException) {
                throw (RenderException)e.getRootCause();
            }
            throw new RenderException(e.getRootCause() != null ? e.getRootCause() : e);
        } catch (IOException e) {
            throw new RenderException(e);
        } finally {
            request.setAttribute("currentModule", oldModule);
        }
        try {
            return wrapper.getString();
        } catch (IOException e) {
            throw new RenderException(e);
        }
    }

    /**
     * Provides access to the view associated with this script
     *
     * @return the View instance that will be executed
     */
    public View getView() {
        return view;
    }

}
