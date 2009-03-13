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

// $Id$
//
//  ManageLogs
//
//  01.04.2001  MJ  added in jahia.
//
//

package org.jahia.admin.audit;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.JahiaListenersRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.audit.JahiaAuditLogManagerService;
import org.jahia.services.audit.LoggingEventListener;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.properties.PropertiesManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Calendar;


/**
 * desc: This class provides the business methods for audit
 * log management, from the JahiaAdministration servlet.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Mikhael Janson
 * @author Alexandre Kraft
 * @version 1.0
 */
public class ManageLogs extends AbstractAdministrationModule {
    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageLogs.class);

    private static final String     CLASS_NAME              =  JahiaAdministration.CLASS_NAME;
    private static final String     JSP_PATH                =  JahiaAdministration.JSP_PATH;
    private static final String     LOGGING_EVENT_LISTENER  = "org.jahia.services.audit.LoggingEventListener";

    private static JahiaAuditLogManagerService  lMgr;
    private static JahiaListenersRegistry       lReg;



    /**
    * Default constructor.
    *
    * @param   request       Servlet request.
    * @param   response      Servlet response.
    */
    public void service( HttpServletRequest    request,
                       HttpServletResponse   response )

    throws Exception
    {
        lReg  =  JahiaListenersRegistry.getInstance();

        ServicesRegistry       sReg  =  ServicesRegistry.getInstance();
        if (sReg != null)      lMgr  =  sReg.getJahiaAuditLogManagerService();

        userRequestDispatcher( request, response, request.getSession() );
    } // end constructor



    /**
     * This method serves as a dispatcher for user requests.
     *
     * @param   req      Servlet request.
     * @param   res      Servlet response.
     * @param   sess     Servlet session for the current user.
     */
    private void userRequestDispatcher( HttpServletRequest    req,
                                        HttpServletResponse   res,
                                        HttpSession           sess )
    throws IOException, ServletException
    {

        String op =  req.getParameter("sub");

        if(op.equals("settings"))            displaySettings( req, res, sess );
        else if(op.equals("view"))           displayView( req, res, sess );
        else if(op.equals("flushconfirm"))   displayFlush( req, res, sess );
        else if(op.equals("manage"))         processSettings( req, res, sess );
        else if(op.equals("flush"))          processFlush( req, res, sess );
        else if(op.equals("enable"))         processEnable( req, res, sess );
        else if(op.equals("disable"))        processDisable( req, res, sess );
        else if(op.equals("analytics"))      displayAnalytics( req, res, sess );

    } // userRequestDispatcher

    private void displayAnalytics(HttpServletRequest req, HttpServletResponse res, HttpSession sess) {
        try {
            JahiaAdministration.doRedirect( req, res, sess, JSP_PATH + "gwtanalytics.jsp" );
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ServletException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    /**
     * Display a UI for audit log management, using doRedirect().
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void displaySettings( HttpServletRequest   req,
                                  HttpServletResponse  res,
                                  HttpSession          sess )
    throws IOException, ServletException
    {

        String   jahiaDisplayMessage =  (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        boolean  logEnabled          =  true;
        String   maxLogs             =  "";

        if(lReg != null) {

            // get log config and set default values...
            logEnabled = (lReg.getListenerByClassName(LOGGING_EVENT_LISTENER) != null) ;

            if(logEnabled) {
                maxLogs  = org.jahia.settings.SettingsBean.getInstance().readJahiaPropertiesFile().getProperty("jahiaMaxLogs");

                if(maxLogs == null)             maxLogs             = "0";
            }

            if(jahiaDisplayMessage == null)     jahiaDisplayMessage = Jahia.COPYRIGHT;

            // set request attributes...
            req.setAttribute("logEnabled",           Boolean.valueOf(logEnabled));
            req.setAttribute("maxLogs",              maxLogs);
            req.setAttribute("jahiaDisplayMessage",  jahiaDisplayMessage);

            JahiaAdministration.doRedirect( req, res, sess, JSP_PATH + "manage_logs.jsp" );

        } else {
            // back to menu in case of problems with the Managers...
          JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
          ProcessingContext jParams = null;
          if (jData != null) {
              jParams = jData.getProcessingContext();
          }
            jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.noListenersRegistryInstance.label",
                                               jParams.getLocale());
            sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            JahiaAdministration.displayMenu( req, res, sess );
        }

    } // end displaySettings



    /**
     * Display full audit log, using doRedirect().
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void displayView( HttpServletRequest   req,
                              HttpServletResponse  res,
                              HttpSession          sess )
    throws IOException, ServletException
    {
        //todo ib for test purpose
        displayAnalytics( req, res, sess );

        String   jahiaDisplayMessage = (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");

        JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        if (lMgr !=null) {

            Integer  viewLastDays    =  (Integer) sess.getAttribute("viewLastDays");
            if( viewLastDays == null ) {
                viewLastDays = new Integer(5);
                sess.setAttribute("viewLastDays", viewLastDays);
            }
            String newViewLastDays = req.getParameter("viewLastDays");
            if ((newViewLastDays != null) && (!"".equals(newViewLastDays))) {
                try {
                    viewLastDays = Integer.valueOf(newViewLastDays);
                    sess.setAttribute("viewLastDays", viewLastDays);
                } catch (NumberFormatException nfe) {
                    logger.warn("Invalid number value "+newViewLastDays+", ignoring...");
                }
            }

            // get logs
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, - viewLastDays.intValue());
            List logData = lMgr.getLog(calendar.getTimeInMillis(), jParams);

            // set default values...
            if(jahiaDisplayMessage == null) jahiaDisplayMessage = Jahia.COPYRIGHT;

            // set session and request attributes...
            Boolean  keeprecentlogs =  (Boolean) sess.getAttribute("keeprecentlogs");
            if( keeprecentlogs == null ) sess.setAttribute("keeprecentlogs", Boolean.TRUE);

            Integer  maxlogsdays    =  (Integer) sess.getAttribute("maxlogsdays");
            if( maxlogsdays == null )    sess.setAttribute("maxlogsdays", Integer.valueOf(60));

            req.setAttribute("logData", logData);
            sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  jahiaDisplayMessage);
        //    JahiaAdministration.doRedirect( req, res, sess, JSP_PATH + "logs.jsp" ); //todo ib for test purpose

        } else {

            // back to menu in case of problems with the Managers...
            jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.noAuditLogManagerInstance.label",
                                               jParams.getLocale());
            sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            JahiaAdministration.displayMenu( req, res, sess );
        }

    } // end displayView



    /**
     * Enable Audit Event logging
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void processEnable( HttpServletRequest   req,
                                HttpServletResponse  res,
                                HttpSession          sess )
    throws IOException, ServletException
    {
        String   jahiaDisplayMessage = (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");


            // set default values...
            if(jahiaDisplayMessage == null)     jahiaDisplayMessage = Jahia.COPYRIGHT;


            if(lReg != null) {

                // get log config
                JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
                ProcessingContext jParams = null;
                if (jData != null) {
                    jParams = jData.getProcessingContext();
                }
                try {
                    if( lReg.addListener(new LoggingEventListener()) ) {
                      jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.auditLoggingEnabled.label",
                                                                                 jParams.getLocale());
                    } else {
                      jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.auditLoggingNotEnabled.label",
                                                                                 jParams.getLocale());
                    }
                } catch (JahiaException e) {
                    throw new ServletException("Error starting logging event listener", e);
                }
            }

            // set request attributes...
            sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  jahiaDisplayMessage);
            displaySettings( req, res, sess );

    } // end processEnable



    /**
     * Disable Audit Event logging
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void processDisable( HttpServletRequest   req,
                                 HttpServletResponse  res,
                                 HttpSession          sess )
    throws IOException, ServletException
    {
        String   jahiaDisplayMessage = (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");

            // set default values...
            if(jahiaDisplayMessage == null) {  jahiaDisplayMessage = Jahia.COPYRIGHT;  }

            if(lReg != null) {

                // get log config
                //JahiaEventListenerInterface theListener = (lReg.getListener(LOGGING_EVENT_LISTENER));
                JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
                ProcessingContext jParams = null;
                if (jData != null) {
                    jParams = jData.getProcessingContext();
                }
                if( lReg.removeListenerByClassName(LOGGING_EVENT_LISTENER) ) {
                  jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.auditLoggingDisabled.label",
                                                     jParams.getLocale());
                } else {
                  jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.auditLoggingNotDisabled.label",
                                                     jParams.getLocale());
                }
            }

            // set request attributes...
            sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  jahiaDisplayMessage);
            displaySettings( req, res, sess );

    } // end processDisable



    /**
     * Update the maxLog property...
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void processSettings( HttpServletRequest   req,
                                  HttpServletResponse  res,
                                  HttpSession          sess )
    throws IOException, ServletException
    {
        String   jahiaDisplayMessage = (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");

            // get request parameters...
            String               maxLogs  =  req.getParameter("maxlogs");
            if (maxLogs == null) maxLogs  =  "500";

            // make sure we've got a valid number
            int maxLogsInt;
            JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
            ProcessingContext jParams = null;
            if (jData != null) {
                jParams = jData.getProcessingContext();
            }
            try {
                maxLogsInt = Integer.parseInt(maxLogs.trim());

                // update the config property...
                PropertiesManager propertiesManager =  new PropertiesManager( Jahia.getJahiaPropertiesFileName() );
                propertiesManager.setProperty( "jahiaMaxLogs", Integer.toString(maxLogsInt) );
                propertiesManager.storeProperties();

                // if needed, delete superfluous log entries

                if (lMgr !=null) {
                    //logger.debug("Enforce max logs: "+maxLogsInt);
                    //int result = lMgr.enforceMaxLogs(maxLogsInt);
                    // for performance reason, it's better to delete all rows
                    logger.debug("Delete all logs entries");
                    int result = lMgr.deleteAllLogs();
                    jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.logEntriesDeleted.label",
                                                       jParams.getLocale());
                    jahiaDisplayMessage =  result + " " + jahiaDisplayMessage;

                } else {

                  jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.noAuditLogManagerInstance.label",
                                                     jParams.getLocale());
                  sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
                    JahiaAdministration.displayMenu( req, res, sess );   // back to menu in case of problems with the Managers...
                }
            }
            catch(NumberFormatException nfe) {
                jahiaDisplayMessage =  "Invalid value entered for maximum log size";
            }

            // set request attributes...
            sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  jahiaDisplayMessage);
            displaySettings( req, res, sess );

    } // end processSettings



    /**
     * Display a confirmation request for flushing all audit log entries, using doRedirect().
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void displayFlush( HttpServletRequest   req,
                               HttpServletResponse  res,
                               HttpSession          sess )
    throws IOException, ServletException
    {

        String       jahiaDisplayMessage  =  (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        String       keeprecent           =  (String)  req.getParameter("keeprecent");
        StringBuffer buf                  =  new StringBuffer();
        String       jsp_template         =  JSP_PATH + "logs_flushconfirm.jsp";

            // set request attributes...
            Boolean keeprecentlogs =  Boolean.valueOf(keeprecent);
            sess.setAttribute("keeprecentlogs", keeprecentlogs );
            JahiaData jData = (JahiaData) req.getAttribute("org.jahia.data.JahiaData");
            ProcessingContext jParams = null;
            if (jData != null) {
                jParams = jData.getProcessingContext();
            }
            if( !keeprecentlogs.booleanValue() ) {
                req.setAttribute("engineMessage", "");
                JahiaAdministration.doRedirect( req, res, sess, jsp_template );

            } else {
                try {
                    String  maxLogsDaysStr = (String) req.getParameter("maxlogsdays");
                    Integer maxlogsdays = new Integer(maxLogsDaysStr.trim());

                    if(maxlogsdays.intValue() < 1) {
                      String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.numberDaysInvalid.label",
                                                         jParams.getLocale());
                      req.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                        sess.setAttribute("maxlogsdays", null);
                        displayView( req, res, sess );
                    } else {
                        sess.setAttribute("maxlogsdays", maxlogsdays);
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.engineMessage.olderThan.label",
                                                           jParams.getLocale());
                        buf.append(" ").append(dspMsg).append(" ");
                        buf.append(maxlogsdays.toString());
                        dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.engineMessage.days.label",
                                                           jParams.getLocale());
                        buf.append(" ").append(dspMsg);
                        sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  jahiaDisplayMessage);
                        req.setAttribute("engineMessage", buf.toString());
                        JahiaAdministration.doRedirect( req, res, sess, jsp_template );
                    }
                }
                catch (ClassCastException cce) {
                  String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestNotFulfilled.label",
                                                     jParams.getLocale());
                  req.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    displayView( req, res, sess );
                }
                catch (NumberFormatException nfe) {
                  String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.numberDaysInvalid.label",
                                                     jParams.getLocale());
                  req.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    displayView( req, res, sess );
                }
            }
    } // end displayFlush



    /**
     * Remove all audit log entries.
     *
     * @param   req       the HttpServletRequest object
     * @param   res       the HttpServletResponse object
     * @param   sess      the HttpSession object
     */
    private void processFlush( HttpServletRequest   req,
                               HttpServletResponse  res,
                               HttpSession          sess )
    throws IOException, ServletException
    {

        String   jahiaDisplayMessage  =  (String)  sess.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        Boolean  keeprecentlogs       =  (Boolean) sess.getAttribute("keeprecentlogs");
        Integer  maxlogsdays          =  (keeprecentlogs.booleanValue()) ? (Integer) sess.getAttribute("maxlogsdays") : null ;

        if (lMgr !=null) {

            // flush logs
            lMgr.flushLogs( ((JahiaUser) sess.getAttribute(ProcessingContext.SESSION_USER)), maxlogsdays );
        }

        // set request attributes...
        sess.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  jahiaDisplayMessage);
        displayView( req, res, sess );

    } // end processFlush


} // end class ManageLogs
