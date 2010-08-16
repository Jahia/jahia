/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.admin.analytics;

import org.jahia.services.analytics.GoogleAnalyticsProfile;
import org.jahia.services.analytics.GoogleAnalyticsService;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.License;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;

import org.jahia.utils.JahiaTools;

import org.jahia.admin.AbstractAdministrationModule;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;


/**
 * @author Ibrahim Elghandour
 */

public class ManageAnalytics extends AbstractAdministrationModule {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageAnalytics.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private static ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private static JahiaSitesService jahiaSitesService = servicesRegistry.getJahiaSitesService();

    private License coreLicense;

    private String gaUserAccount = "";
    private String gaProfile = "";
    private String gaLogin = "";
    private String gaPassword = "";
    private String trackedUrls = "";
    boolean trackingEnabled = false;

    /**
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = getMessage("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label");
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect(request, response, request.getSession(), JSP_PATH + "menu.jsp");
            return;
        }
        userRequestDispatcher(request, response, request.getSession());
    } // end constructor


    //-------------------------------------------------------------------------

    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     * @author Alexandre Kraft
     */
    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        String operation = request.getParameter("sub");


        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && servicesRegistry != null) {
            // set the new site id to administrate...
            request.setAttribute("site", site);
            String profile = request.getParameter("profile");
            if (operation.equals("display")) {
                displayAnalyticsParams(request, response, session);
            } else if (operation.equals("new")) {
                displayAdd(request, response, session, operation);
            } else if (operation.equals("add")) {
                processAdd(request, response, session, "new");
            } else if (operation.equals("displayEdit")) {
                displayEdit(request, response, session, profile);
            } else if (operation.equals("saveEdit")) {
                processAdd(request, response, session, "edit");
            } else if (operation.equals("commit")) {
                commitChanges(request, response, session);
            } else if (operation.equals("delete")) {
                commitDelete(request, response, session, profile);
            } else {
                displayAnalyticsParams(request, response, session);
            }

        } else {
            String dspMsg = getMessage("message.generalError");
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session, JSP_PATH + "menu.jsp");
        }
    } // userRequestDispatcher


    /**
     * display edit view
     *
     * @param request
     * @param response
     * @param session
     * @param profile
     * @throws IOException
     * @throws ServletException
     */
    private void displayEdit(HttpServletRequest request, HttpServletResponse response, HttpSession session, String profile) throws IOException, ServletException {
        request.setAttribute("profile", profile);
        JahiaAdministration.doRedirect(request,
                response,
                session,
                JSP_PATH + "manage_analytics_form.jsp");

        
    }


    /**
     * Process add
     *
     * @param request
     * @param response
     * @param session
     * @param saveMode
     */
    private void processAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session, String saveMode) {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;

        if (jData != null) {
            jParams = jData.getProcessingContext();
            site = jParams.getSite();
            gaUserAccount = StringUtils.left(JahiaTools.getStrParameter(request, "gaUserAccount", "").trim(), 100);
            gaProfile = StringUtils.left(JahiaTools.getStrParameter(request, "gaProfile", "").trim(), 100);
            gaLogin = StringUtils.left(JahiaTools.getStrParameter(request, "gaLogin", "").trim(), 100);
            gaPassword = StringUtils.left(JahiaTools.getStrParameter(request, "gaPassword", "").trim(), 100);
            trackingEnabled = Boolean.valueOf(request.getParameter("trackingEnabled") != null);
            trackedUrls = StringUtils.left(JahiaTools.getStrParameter(request, "trackedUrls", "").trim(), 100);

            // set as request attribute
            request.setAttribute("gaUserAccount", gaUserAccount);
            request.setAttribute("gaProfile", gaProfile);
            request.setAttribute("gaLogin", gaLogin);
            request.setAttribute("trackingEnabled", trackingEnabled);
            request.setAttribute("trackedUrls", trackedUrls);
            // check validity of parameters
            final Map<String, Boolean> gaValidity = validateParameters();
            if (!gaValidity.get("emptyField")) {
                if (gaValidity.get("credentialsOK")) {
                    if ((gaValidity.get("validAccount")) && gaValidity.get("validProfile")) {
                        try {
                            site.setGoogleAnalyticsProfile(trackedUrls,true,gaPassword,gaLogin,gaProfile,gaUserAccount);
                            commitChanges(request, response, session);
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    } else if (!gaValidity.get("validAccount")) {
                        logger.info("account  unavailable");
                        try {
                            displayAdd(request, response, session, "new");
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    } else {
                        logger.info("profile  unavailable");
                        request.setAttribute("gaError", "profile  unavailable");
                        try {
                            displayAdd(request, response, session, "new");
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    }
                } else {
                    logger.info("bad credentials");
                    request.setAttribute("gaError", "bad credentials");
                    try {
                        displayAdd(request, response, session, "new");
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }

            } else {
                logger.debug("an empty field is detected");
                request.setAttribute("gaError", "an empty field is detected");
                try {
                    displayAdd(request, response, session, "new");
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }

    }


    /**
     * Check is the parameters are valide
     *
     * @return
     */
    private Map<String, Boolean> validateParameters() {
        Map<String, Boolean> validity = new HashMap<String, Boolean>();

        boolean emptyField = (gaLogin.length() == 0) || (gaPassword.length() == 0) || (gaUserAccount.length() == 0) || (gaProfile.length() == 0);
        boolean credentialsOK = false;
        boolean validAccount = false;
        boolean validProfile = false;

        GoogleAnalyticsService analyticsService = ServicesRegistry.getInstance().getGoogleAnalyticsService();
        if (!emptyField) {
            if (credentialsOK = analyticsService.checkCredential(gaLogin, gaPassword)) {
                validAccount = true;
                validProfile = true;
            }
        }
        validity.put("credentialsOK", credentialsOK);
        validity.put("validAccount", validAccount);
        validity.put("validProfile", validProfile);
        validity.put("emptyField", emptyField);
        return validity;
    }


    /**
     * Save google analytics profile
     *
     * @param request
     * @param response
     * @param session
     */
    private void commitChanges(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;

        // todo limit the number the enabled profiles 

        if (jData != null) {
            jParams = jData.getProcessingContext();
            site = jParams.getSite();

            GoogleAnalyticsProfile googleAnalyticsProfile = site.getGoogleAnalytics();
            if (googleAnalyticsProfile != null) {
                String profile = googleAnalyticsProfile.getProfile();

                // get tracked rl
                String newTrackedUrls = "virtual";
                if (StringUtils.left(JahiaTools.getStrParameter(request, profile + "TrackedUrls", "real").trim(), 100).equals("real")) {
                    newTrackedUrls = "real";
                }
                googleAnalyticsProfile.setTypeUrl(newTrackedUrls);
            }
            jahiaSitesService = servicesRegistry.getJahiaSitesService();
            try {
                jahiaSitesService.updateSite(site);
                displayAnalyticsParams(request, response, session);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

    }

    /**
     * Display add view
     *
     * @param request
     * @param response
     * @param session
     * @param operation
     * @throws IOException
     * @throws ServletException
     */
    private void displayAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session, String operation) throws IOException, ServletException {
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_analytics_form.jsp");
    }

    /**
     * Commit delete
     *
     * @param request
     * @param response
     * @param session
     * @param profile
     * @throws IOException
     * @throws ServletException
     */
    private void commitDelete(HttpServletRequest request, HttpServletResponse response, HttpSession session, String profile) throws IOException, ServletException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            ProcessingContext jParams = jData.getProcessingContext();
            site = jParams.getSite();

            jahiaSitesService = servicesRegistry.getJahiaSitesService();
            try {
                site.getGoogleAnalytics().setToDelete(true);
                jahiaSitesService.updateSite(site);
                site.getGoogleAnalytics().delete();
                displayAnalyticsParams(request, response, session);
            } catch (Exception e) {
                logger.error(e, e);
            }
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "manage_analytics.jsp");
        }
    }

    /**
     * Display analitytics parameter
     *
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void displayAnalyticsParams(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_analytics.jsp");
    }

}

