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
