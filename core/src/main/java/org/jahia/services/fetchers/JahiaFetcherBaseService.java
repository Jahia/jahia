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

//
//  JahiaFetcherBaseServices
//  EV      11.01.2001
//
//
//  fetchServlet( jParams, servletPath )
//

package org.jahia.services.fetchers;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.services.applications.ServletIncludeRequestWrapper;
import org.jahia.services.applications.ServletIncludeResponseWrapper;

/**
  * Class JahiaFetcherBaseService
  *
  */
public class JahiaFetcherBaseService extends JahiaFetcherService {

    private static JahiaFetcherBaseService theObject = null;

    /***
        * constructor
        *
        */
    protected JahiaFetcherBaseService() {
    } // end constructor

    /***
        * returns an instance of the fetcher service
        *
        */
    public static synchronized JahiaFetcherBaseService getInstance() {
        if (theObject == null) {
            theObject = new JahiaFetcherBaseService();
        }
        return theObject;
    } // end getInstance

    public void start() {}

    public void stop() {}

    /***
        * fetches a servlet or JSP output
        *
        * @param        jParams             a ProcessingContext object (with request and response)
        * @param        servletPath         the servlet / jsp context path
        * @return       the servlet / jsp output
        *
        */
    public String fetchServlet( ParamBean jParams, String servletPath ) {

        ServletIncludeResponseWrapper responseWrapper = null;

        try {

            logger.debug("Dispatching to servlet path = [" + servletPath + "]");

            RequestDispatcher sessionDispatcher = jParams.getContext().getRequestDispatcher( servletPath );

            if (sessionDispatcher == null) {
                String errorMsg = "Error in fetchServlet : sessionDispatcher is null - error getting request dispatcher";
                JahiaException je = new JahiaException("Error while fetching a servlet output",
                        errorMsg, JahiaException.SERVLET_ERROR, JahiaException.WARNING_SEVERITY );
                logger.debug(errorMsg, je);
                return "";
            }

            // Let's initialize the wrapper, that includes our specific PrintWriter, allowing us to redirect
            // output to a string object.
            String forceEncoding = null;
            forceEncoding = org.jahia.settings.SettingsBean.getInstance().getDefaultResponseBodyEncoding();
            responseWrapper = new ServletIncludeResponseWrapper(jParams.getResponse(), false, forceEncoding);

            if (responseWrapper == null) {
                String errorMsg = "Error in fetchServlet : responseWrapper is null - error getting response warpper";
                JahiaException je = new JahiaException(   "Error while fetching a servlet output",
                                        errorMsg, JahiaException.SERVLET_ERROR, JahiaException.WARNING_SEVERITY );
                logger.debug(errorMsg, je);
                return "";
            }

            ServletIncludeRequestWrapper requestWrapper = new ServletIncludeRequestWrapper(jParams.getRealRequest(), jParams);

            // Let's now call the servlet and retrieve the result of it's processing
            sessionDispatcher.include(requestWrapper, responseWrapper);

        } catch (ServletException se) {
            String generatedOutput = "";
            try {
                generatedOutput = responseWrapper.getStringBuffer();
            } catch (IOException ioe) {
                logger.error("Error while retrieving generated output", ioe);
            }
            logger.error(generatedOutput, se);
            logger.error(generatedOutput, se.getRootCause());
            return "";
        } catch (IOException ioe) {
            String generatedOutput = "";
            try {
                generatedOutput = responseWrapper.getStringBuffer();
            } catch (IOException ioe2) {
                logger.error("Error while retrieving generated output", ioe2);
            }
            logger.debug(generatedOutput, ioe);
            return "";
        } catch (Exception t) {
            String generatedOutput = "";
            try {
                generatedOutput = responseWrapper.getStringBuffer();
            } catch (IOException ioe) {
                logger.error("Error while retrieving generated output", ioe);
            }
            logger.debug(generatedOutput, t);
            return "";
        }
        String generatedOutput = "";
        try {
            generatedOutput = responseWrapper.getStringBuffer();
        } catch (IOException ioe) {
            logger.error("Error while retrieving generated output", ioe);
        }
        return generatedOutput;
    } // end fetchServlet

    private static Logger logger = Logger.getLogger(JahiaFetcherBaseService.class);

} // end JahiaFetcherBaseService
