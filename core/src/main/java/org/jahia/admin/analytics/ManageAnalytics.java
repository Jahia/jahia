/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.analytics.GoogleAnalyticsService;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.SitesSettings;
import org.jahia.utils.i18n.JahiaResourceBundle;
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
    private String jahiaProfileName = "";
    boolean trackingEnabled = false;

    /**
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label", jParams.getLocale());
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
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

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
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
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

        //To change body of created methods use File | Settings | File Templates.
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
            jahiaProfileName = StringUtils.left(JahiaTools.getStrParameter(request, "jahiaGAprofile", "").trim(), 100);

            // set as request attribute
            request.setAttribute("gaUserAccount", gaUserAccount);
            request.setAttribute("gaProfile", gaProfile);
            request.setAttribute("gaLogin", gaLogin);
            request.setAttribute("trackingEnabled", trackingEnabled);
            request.setAttribute("trackedUrls", trackedUrls);
            request.setAttribute("jahiaProfileName", jahiaProfileName);


            // check validity of parameters
            final Map<String, Boolean> gaValidity = validateParameters();
            final boolean profileAvailable = checkJahiaProfileAvaibility(request, jahiaProfileName);
            if (!gaValidity.get("emptyField")) {
                if (gaValidity.get("credentialsOK")) {
                    if ((gaValidity.get("validAccount")) && gaValidity.get("validProfile")) {
                        if (saveMode.equals("new") && profileAvailable) {
                            try {
                                saveAnayliticsParameters("new", request, response, session);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }

                        } else if (saveMode.equals("new") && !profileAvailable) {
                            request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaProfileName + " is already in use");
                            try {
                                displayAdd(request, response, session, "new");
                            } catch (Exception e) {
                                logger.error(e, e);
                            }

                        } else if (saveMode.equals("edit")) {
                            try {
                                saveAnayliticsParameters("edit", request, response, session);
                            } catch (Exception e) {
                                logger.error(e, e);
                            }
                        }


                    } else if (!gaValidity.get("validAccount")) {
                        logger.info("account  unavailable");
                        if (!profileAvailable)
                            request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaProfileName + " is already in use");
                        request.setAttribute("gaError", "account  unavailable");
                        try {
                            displayAdd(request, response, session, "new");
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    } else {
                        logger.info("profile  unavailable");
                        if (!profileAvailable) {
                            request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaProfileName + " is already in use");
                        }
                        request.setAttribute("gaError", "profile  unavailable");
                        try {
                            displayAdd(request, response, session, "new");
                        } catch (Exception e) {
                            logger.error(e, e);
                        }
                    }
                } else {
                    logger.info("bad credentials");
                    if (!profileAvailable) {
                        request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaProfileName + " is already in use");
                    }
                    request.setAttribute("gaError", "bad credentials");
                    try {
                        displayAdd(request, response, session, "new");
                    } catch (Exception e) {
                        logger.error(e, e);
                    }
                }

            } else {
                logger.debug("an empty field is detected");
                if (!profileAvailable) {
                    request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaProfileName + " is already in use");
                }
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
     * The profil already exist
     *
     * @param request
     * @param jahiaProfileName
     * @return
     */
    private boolean checkJahiaProfileAvaibility(HttpServletRequest request, String jahiaProfileName) {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            boolean exist = jData.getProcessingContext().getSite().getProperty(SitesSettings.getJahiaProfileNameKey(jahiaProfileName), null) != null;
            if (exist) {
                logger.debug("Profile [" + jahiaProfileName + "] exists");
                return false;
            }
        }
        return true;
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

        GoogleAnalyticsService analyticsService = GoogleAnalyticsService.getInstance();
        if (!emptyField) {
            if (credentialsOK = analyticsService.checkCredential(jahiaProfileName, gaLogin, gaPassword)) {
                if (validAccount = analyticsService.checkAccount(jahiaProfileName, gaUserAccount)) {
                    validProfile = analyticsService.checkProfile(jahiaProfileName, gaUserAccount, gaProfile);
                }
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
            Iterator it = ((site.getSettings()).keySet()).iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.startsWith("jahiaGAprofile")) {
                    String profile = site.getSettings().getProperty(key);
                    boolean newTrackingEnabled = Boolean.valueOf(request.getParameter(profile + "TrackingEnabled") != null);
                    String newTrackedUrls = "virtual";
                    if (StringUtils.left(JahiaTools.getStrParameter(request, profile + "TrackedUrls", "real").trim(), 100).equals("real")) {
                        newTrackedUrls = "real";
                    }
                    site.getSettings().setProperty(SitesSettings.getTrackingEnabledKey(profile), String.valueOf(newTrackingEnabled));
                    site.getSettings().setProperty(SitesSettings.getTrackedUrlKey(profile), newTrackedUrls);
                }
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
        ProcessingContext jParams = null;

        if (jData != null) {
            jParams = jData.getProcessingContext();
            site = jParams.getSite();

            final List<String> propertiesToBeRemoved = new ArrayList<String>();
            propertiesToBeRemoved.add(SitesSettings.getUserAccountPropertyKey(profile));
            propertiesToBeRemoved.add(SitesSettings.getProfileKey(profile));
            propertiesToBeRemoved.add(SitesSettings.getLoginKey(profile));
            propertiesToBeRemoved.add(SitesSettings.getPasswordKey(profile));
            propertiesToBeRemoved.add(SitesSettings.getTrackingEnabledKey(profile));
            propertiesToBeRemoved.add(SitesSettings.getTrackedUrlKey(profile));
            propertiesToBeRemoved.add(SitesSettings.getJahiaProfileNameKey(profile));


            try {
                servicesRegistry.getJahiaSitesService().removeSiteProperties(site, propertiesToBeRemoved);
                servicesRegistry.getCacheService().flushAllCaches();
            } catch (JahiaException e) {
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


    /**
     * Save as site properties
     *
     * @param mode
     * @param request
     * @param response
     * @param session
     * @throws IOException
     * @throws ServletException
     */
    private void saveAnayliticsParameters(String mode, HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpSession session) throws IOException, ServletException {
        if (servicesRegistry != null) {
            if (mode.equals("new")) {
                site.getSettings().setProperty(SitesSettings.getJahiaProfileNameKey(jahiaProfileName), jahiaProfileName);
            }
            site.getSettings().setProperty(SitesSettings.getUserAccountPropertyKey(jahiaProfileName), gaUserAccount);
            site.getSettings().setProperty(SitesSettings.getProfileKey(jahiaProfileName), gaProfile);
            site.getSettings().setProperty(SitesSettings.getLoginKey(jahiaProfileName), gaLogin);
            site.getSettings().setProperty(SitesSettings.getPasswordKey(jahiaProfileName), gaPassword);
            site.getSettings().setProperty(SitesSettings.getTrackingEnabledKey(jahiaProfileName), String.valueOf(trackingEnabled));
            site.getSettings().setProperty(SitesSettings.getTrackedUrlKey(jahiaProfileName), trackedUrls);

            jahiaSitesService = servicesRegistry.getJahiaSitesService();
            try {
                jahiaSitesService.updateSite(site);
                displayAnalyticsParams(request, response, session);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }

    }


}

