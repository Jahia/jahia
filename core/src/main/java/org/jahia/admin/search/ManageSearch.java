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
//  ManageSearch
//
//  02.04.2002  NK  added in jahia.
//

package org.jahia.admin.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.JahiaSiteIndexingJob;
import org.jahia.services.search.SearchHandler;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.admin.AbstractAdministrationModule;


/**
 * This class is used by the administration to manage the search engine.
 *
 * Copyright:    Copyright (c) 2001
 * Company:      Jahia
 *
 * @author <Khue Nguyen> <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 * @version 1.0
 */
public class ManageSearch extends AbstractAdministrationModule {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageSearch.class.getName ());

    private static final String HTTP_FILES = "/engines";
    private static final String JSP = "/admin/search/main.jsp?includejsp=";

    private ServicesRegistry sReg;
    private JahiaSitesService siteServ;
    private JahiaSearchService searchServ;

    private JahiaSite site;
    private JahiaUser user;
    private Map values;

    private String operation = ""; // current operation

    private static byte[] mLock = new byte[1];


    /**
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service(HttpServletRequest request,
                         HttpServletResponse response)
            throws Exception {
        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }

        sReg = ServicesRegistry.getInstance ();

        if (sReg == null) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.serviceRegistyNotAvailable.label",
                    jParams.getLocale());
            displayError (request, response, dspMsg);
            return;
        }

        siteServ = sReg.getJahiaSitesService ();
        searchServ = sReg.getJahiaSearchService ();

        if (siteServ == null || searchServ == null) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.requestServiceNotAvailable.label",
                    jParams.getLocale());
            displayError (request, response, dspMsg);
            return;
        }

        userRequestDispatcher (request, response, request.getSession());
    }


    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void userRequestDispatcher (HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws IOException, ServletException {

        // check if the user has really admin access to this site...
        user = (JahiaUser)session.getAttribute (ProcessingContext.SESSION_USER);
        site = (JahiaSite)session.getAttribute (ProcessingContext.SESSION_SITE);
        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }

        if (site == null || user == null || sReg == null) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.requestInfoNotAvailable.label",
                    jParams.getLocale());
            displayError (request, response, dspMsg);
            return;
        }

        // restore data stored in session
        values = (Map)session.getAttribute ("org.jahia.adminsearch.values." + site.getSiteKey ());
        if (values == null) {
            Map values = new HashMap();
            session.setAttribute ("org.jahia.adminsearch.values." + site.getSiteKey (), values);
            this.values = values;
        }

        try {


            operation = request.getParameter ("sub");

            if (operation.equals ("display")) {
                displayMain (request, response, session);
            } else if (operation.equals ("chooseoperation")) {
                processChooseOperation (request, response, session);
            } else if (operation.equals ("doindex")) {
                processDoIndex (request, response, session);
            } else if (operation.equals ("dooptimize")) {
                processDoOptimize (request, response, session);
            }

        } catch (Exception t) {
            logger.error(t.getMessage(), t);
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.processingRequestError.label",
                    jParams.getLocale());
            displayError (request, response, dspMsg);
        }
    }

    /**
     * Forward the servlet request and servlet response objects, using the request
     * dispatcher (from the ServletContext). Note: please be careful, use only
     * context relative path.
     *
     * @param       request             servlet request.
     * @param       response            servlet response.
     * @param       target              target, context-relative path.
     * @exception   IOException         an I/O exception occured during the process.
     * @exception   ServletException    a servlet exception occured during the process.
     */
    private void doRedirect (HttpServletRequest request,
                             HttpServletResponse response,
                             String target)
            throws IOException, ServletException {
        try {
            request.setAttribute ("currentSite", site);
            request.setAttribute ("user", user);

            // check current operation
            if (request.getAttribute ("operation") == null) {
                request.setAttribute ("operation", operation);
            }

            // check null warning msg
            if (request.getAttribute ("warningMsg") == null) {
                request.setAttribute ("warningMsg", "");
            }

            // check null jsp bottom message, and fill in if necessary...
            if (request.getAttribute ("msg") == null) {
                request.setAttribute ("msg", Jahia.COPYRIGHT);
            }

            if (request.getAttribute ("focus") == null) {
                request.setAttribute ("focus", "-none-");
            }

            // check null configuration step title, and fill in if necessary...
            if (request.getAttribute ("title") == null) {
                request.setAttribute ("title", "Manage Search Engine");
            }

            // set input values on request...
            request.setAttribute ("values", values);

            // add http files path in request...
            request.setAttribute ("url", request.getContextPath () + HTTP_FILES);
            request.setAttribute("URL", request.getContextPath() + HTTP_FILES + "/");

            // get browser entity...
            String userAgent = request.getHeader ("user-agent");

            Integer navigator = (userAgent.indexOf ("IE"))
                    != -1 ? new Integer (0) : new Integer (1);
            request.setAttribute ("navigator", navigator);

            boolean isLynx = false;

            if (userAgent.indexOf ("Lynx") != -1) {
                isLynx = true;
            }
            request.setAttribute ("isLynx", Boolean.valueOf(isLynx));

            // set browser response content type
            HttpSession session = request.getSession ();
            JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
            ProcessingContext jParams = null;
            if (jData != null) {
                jParams = jData.getProcessingContext ();
            }
            Locale currentLocale = (Locale)session.getAttribute (ProcessingContext.SESSION_LOCALE);
            if (currentLocale == null) {
                currentLocale = jParams.getLocale ();
            }

            String contentTypeStr = "text/html;charset=";
            contentTypeStr = contentTypeStr + "ISO8859-1";
            request.setAttribute ("content-type", contentTypeStr);

            // redirect!
            JahiaAdministration.doRedirect(request, response, session, target);

        } catch (IOException ie) {
            logger.error (ie.getMessage ());
        } catch (ServletException se) {
            logger.error (se.getMessage ());
        }
    }

    /**
     * Display the search engine main admin page.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void displayMain (HttpServletRequest request,
                              HttpServletResponse response,
                              HttpSession session)
            throws IOException, ServletException, JahiaException {

        //String refreshURL =  request.getScheme() + "://" + request.getServerName()
        //            + (request.getServerPort()!=80?":" + request.getServerPort():"");
        String refreshURL = JahiaAdministration.composeActionURL(request,response,"search","&sub=display");

        response.setHeader("Refresh", "20;" + refreshURL);

                // set the default action
        request.setAttribute ("subAction", "chooseoperation");

        handleDisplayOperation(request,response,session);

        // set the default step
        request.setAttribute ("go", "change");

        // default values
        String operation = "doindex";

        String val = null;
        val = (String)values.get ("operation");
        if (val != null) {
            operation = val;
        }

        // check if the index exists yet for the current site.
        SearchHandler searchHandler = searchServ.getSearchHandler(site.getID ());
        boolean indexExists = (searchHandler != null);

        values.put ("operation", operation);

        request.setAttribute ("operation", operation);
        request.setAttribute ("indexExists", Boolean.valueOf(indexExists));
        doRedirect (request, response, JSP + "operations");
    }

    private void handleDisplayOperation (   HttpServletRequest request,
                                            HttpServletResponse response,
                                            HttpSession session)
            throws IOException, ServletException, JahiaException {

        String val = null;
        val = request.getParameter("go");
        String jobSiteKey = request.getParameter("jobSiteKey");
        String serverId = request.getParameter("serverId");
        if ( jobSiteKey != null && serverId != null ){
            JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(jobSiteKey);
            String currentStatus = site.getSettings().getProperty(serverId + "_"
                    + JahiaSiteIndexingJob.INTERRUPT_STATUS,"");
            Properties newSettings = new Properties();
            if ("abortJob".equals(val)) {
                if (!BackgroundJob.STATUS_ABORTED.equals(currentStatus)){
                    newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                        JahiaSiteIndexingJob.INTERRUPT_STATUS_ABORT_REQUESTED);
                    ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
                }
            } else if ("interruptJob".equals(val)){
                if (!BackgroundJob.STATUS_INTERRUPTED.equals(currentStatus)
                    && !BackgroundJob.STATUS_ABORTED.equals(currentStatus)){
                    newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                            JahiaSiteIndexingJob.INTERRUPT_STATUS_INTERRUPT_REQUESTED);
                    ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
                }
            } else if ("resumeJob".equals(val)){
                if (!JahiaSiteIndexingJob.INTERRUPT_STATUS_RESUMED.equals(currentStatus)
                    && !BackgroundJob.STATUS_ABORTED.equals(currentStatus)){
                    newSettings.setProperty(serverId + "_" + JahiaSiteIndexingJob.INTERRUPT_STATUS,
                            JahiaSiteIndexingJob.INTERRUPT_STATUS_RESUME_REQUESTED);
                    ServicesRegistry.getInstance().getJahiaSitesService().updateSiteProperties(site, newSettings);
                }
            }
        }
    }

    /**
     * Process the form choose operations.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void processChooseOperation (HttpServletRequest request,
                                         HttpServletResponse response,
                                         HttpSession session)
            throws IOException, ServletException, JahiaException {

        // get submitted data
        String operation = request.getParameter ("operation");
        if (operation == null)
            displayMain (request, response, session);

        values.put ("operation", operation);

        if (operation.equals ("doindex")) {
            displayIndexForm (request, response, session);
        } else if (operation.equals ("dooptimize")) {
            displayOptimizeForm (request, response, session);
        } else {
            displayMain (request, response, session);
        }
    }

    /**
     * Index the site.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void processDoIndex (HttpServletRequest request,
                                 HttpServletResponse response,
                                 HttpSession session)
            throws IOException, ServletException, JahiaException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        if (!user.isAdminMember(site.getID ())) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.mustLogRootAdmin.label",
                    jParams.getLocale());
            displayError (request, response, dspMsg);
            return;
        }

        //JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");

        synchronized (mLock) {

            // get submitted data
            String go = request.getParameter ("go");
            if (go == null)
                displayMain (request, response, session);

            if (go.equals ("ok")) {

                // process the indexation of the full site.
                boolean result = searchServ.indexSite (site.getID (), jData.getProcessingContext().getUser());
                searchServ.optimizeIndex (site.getID ());

                if (!result) {
                    request.setAttribute("result", Boolean.valueOf(result));

                    // set the default action
                    request.setAttribute("subAction", "display");
                    // set the default step
                    request.setAttribute("go", "change");

                    doRedirect(request, response, JSP + "indexresult");
                } else {
                    displayMain (request, response, session);                    
                }
            } else {
                displayMain (request, response, session);
            }
        }
    }

    /**
     * Display the indexation form.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void displayIndexForm (HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session)
            throws IOException, ServletException, JahiaException {

        // set the default action
        request.setAttribute ("subAction", "doindex");
        // set the default event
        request.setAttribute ("go", "change");

        doRedirect (request, response, JSP + "index");
    }

    /**
     * Display the optimization form.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void displayOptimizeForm (HttpServletRequest request,
                                      HttpServletResponse response,
                                      HttpSession session)
            throws IOException, ServletException, JahiaException {

        // set the default action
        request.setAttribute ("subAction", "dooptimize");
        // set the default event
        request.setAttribute ("go", "change");

        doRedirect (request, response, JSP + "optimize");
    }

    /**
     * Optimize the index.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void processDoOptimize (HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException, JahiaException {

        // get submitted data
        String go = request.getParameter ("go");
        if (go == null)
            displayMain (request, response, session);

        if (go.equals ("ok")) {
            // process the indexation of the full site.
            boolean result = searchServ.optimizeIndex (site.getID ());

            request.setAttribute ("result", Boolean.valueOf(result));

            // set the default action
            request.setAttribute ("subAction", "display");
            // set the default step
            request.setAttribute ("go", "change");

            doRedirect (request, response, JSP + "optimizeresult");

        } else {
            displayMain (request, response, session);
        }
    }

    /**
     * Forward to error page.
     *
     * @param   request     Servlet request.
     * @param   response    Servlet response.
     * @param   msg         the warning msg.
     */
    private void displayError (HttpServletRequest request,
                               HttpServletResponse response,
                               String msg)
            throws IOException, ServletException {
        request.setAttribute ("warningMsg", msg);
        doRedirect (request, response, JSP + "error");
    }


}