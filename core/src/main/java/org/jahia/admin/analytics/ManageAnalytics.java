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

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;

import org.jahia.utils.JahiaTools;

import org.jahia.analytics.reports.JAnalytics;
import org.jahia.analytics.reports.JAnalyticsException;
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
    private ServicesRegistry sReg;
    private static JahiaSitesService sMgr;

    private License coreLicense;

    String gaUserAccount = "";
    String gaProfile = "";
    String gaLogin = "";
    String gaPassword = "";
    String trackedUrls = "";
    String jahiaGAprofile = "";
    boolean trackingEnabled = false;
    int profileCnt = 0;

    /**
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                           HttpServletResponse response)
            throws Exception {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                    jParams.getLocale());
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

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && sReg != null) {
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

    private void displayEdit(HttpServletRequest request, HttpServletResponse response, HttpSession session, String profile) throws IOException, ServletException {
        request.setAttribute("profile", profile);
        JahiaAdministration.doRedirect(request,
                response,
                session,
                JSP_PATH + "manage_analytics_form.jsp");

        //To change body of created methods use File | Settings | File Templates.
    }


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
            jahiaGAprofile = StringUtils.left(JahiaTools.getStrParameter(request, "jahiaGAprofile", "").trim(), 100);

            Map<String, String> gaValidity = gaSettingsValidity(); // ga settings' availability
            boolean profileAvailable = profileISavailable(request, jahiaGAprofile);// check jahia profile availability)
            if (!Boolean.valueOf(gaValidity.get("emptyField"))) {
                if (Boolean.valueOf(gaValidity.get("credentialsOK"))) {
                    if ((Boolean.valueOf(gaValidity.get("validAccount"))) && (Boolean.valueOf(gaValidity.get("validProfile")))) {
                        if (saveMode.equals("new") && profileAvailable) {
                            try {
                                saveSite("new", request, response, session);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (ServletException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        } else if (saveMode.equals("new") && !profileAvailable) {
                            request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaGAprofile + " is already in use");
                            try {
                                displayAdd(request, response, session, "new");
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (ServletException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }

                        } else if (saveMode.equals("edit")) {
                            try {
                                saveSite("edit", request, response, session);
                            } catch (IOException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            } catch (ServletException e) {
                                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                            }
                        }


                    } else if (!Boolean.valueOf(gaValidity.get("validAccount"))) {
                        logger.info("account  unavailable");
                        if (!profileAvailable)
                            request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaGAprofile + " is already in use");
                        request.setAttribute("gaError", "account  unavailable");
                        try {
                            displayAdd(request, response, session, "new");
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (ServletException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    } else {
                        logger.info("profile  unavailable");
                        if (!profileAvailable)
                            request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaGAprofile + " is already in use");
                        request.setAttribute("gaError", "profile  unavailable");
                        try {
                            displayAdd(request, response, session, "new");
                        } catch (IOException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        } catch (ServletException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    }
                } else {
                    logger.info("bad credentials");
                    if (!profileAvailable)
                        request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaGAprofile + " is already in use");
                    request.setAttribute("gaError", "bad credentials");
                    try {
                        displayAdd(request, response, session, "new");
                    } catch (IOException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (ServletException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }

            } else {
                logger.info("an empty field is detected");
                if (!profileAvailable)
                    request.setAttribute("jahiaProfileInUse", "Profile name : " + jahiaGAprofile + " is already in use");
                request.setAttribute("gaError", "an empty field is detected");
                try {
                    displayAdd(request, response, session, "new");
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (ServletException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }

    private boolean profileISavailable(HttpServletRequest request, String jahiaGAprofile) {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        boolean available = true;

        // todo limit the number the enabled profiles
        // forbid the usage of some names such as jahiaGAprofile
        if (jData != null) {
            jParams = jData.getProcessingContext();
            site = jParams.getSite();
            Iterator it = ((site.getSettings()).keySet()).iterator();
            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.startsWith("jahiaGAprofile")) {
                    if (site.getSettings().getProperty(key).equals(jahiaGAprofile)) {
                        available = false;
                        break;
                    }
                }
            }
        }
        return available;  //To change body of created methods use File | Settings | File Templates.
    }

    private Map<String, String> gaSettingsValidity() {
        // todo check whether the account corrsponds to the site url
        // todo check if the useracccount is already in use

        Map<String, String> validity = new HashMap<String, String>();

        boolean emptyField = (gaLogin.length() == 0) || (gaPassword.length() == 0) || (gaUserAccount.length() == 0) || (gaProfile.length() == 0);
        boolean credentialsOK = false;
        boolean validAccount = false;
        boolean validProfile = false;

        JAnalytics ja = new JAnalytics();
        if (!emptyField) {
            try {
                ja.login(gaLogin, gaPassword);
                credentialsOK = ja.gmailCredentialsAreValids();
                if (credentialsOK) {
                    LinkedHashMap<String, String> accounts = ja.getAccounts();
                    validAccount = (gaUserAccount.startsWith("UA-")) && (accounts.containsKey(gaUserAccount.split("-")[1]));
                    if (validAccount) {
                        validProfile = ja.getProfilesForAccountWithId((gaUserAccount.trim().split("-"))[1]).containsValue(gaProfile);
                    }
                }
            } catch (JAnalyticsException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        validity.put("credentialsOK", String.valueOf(credentialsOK));
        validity.put("validAccount", String.valueOf(validAccount));
        validity.put("validProfile", String.valueOf(validProfile));
        validity.put("emptyField", String.valueOf(emptyField));
        return validity;
    }

    private boolean accountInUse(HttpServletRequest request, String userAccount) {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        Iterator iterateOnSettings = ((jData.getProcessingContext().getSite()).getSettings()).keySet().iterator();
        while (iterateOnSettings.hasNext()) {
            String prp = (String) iterateOnSettings.next();
            if (prp.endsWith("gaUserAccount")) {
                if (jData.getProcessingContext().getSite().getSettings().get(prp).equals(userAccount)) {
                    return true;
                }
            }
        }

        return false;
    }

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
                    site.getSettings().setProperty(profile + "_" + site.getSiteKey() + "_trackingEnabled", String.valueOf(newTrackingEnabled));
                    site.getSettings().setProperty(profile + "_" + site.getSiteKey() + "_trackedUrls", newTrackedUrls);
                }
            }
            sMgr = sReg.getJahiaSitesService();
            try {
                sMgr.updateSite(site);
                //sReg.getCacheService().flushAllCaches();
                sReg.getInstance().getCacheService().getSkeletonCacheInstance().flushSkeletonsForSite(site.getID());
                displayAnalyticsParams(request, response, session);
            } catch (JahiaException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ServletException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }

    private void displayAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session, String operation) throws IOException, ServletException {
        JahiaAdministration.doRedirect(request,
                response,
                session,
                JSP_PATH + "manage_analytics_form.jsp");
    }

    private void commitDelete(HttpServletRequest request, HttpServletResponse response, HttpSession session, String profile) throws IOException, ServletException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;

        if (jData != null) {
            jParams = jData.getProcessingContext();
            site = jParams.getSite();

            List<String> propertiesToBeRemoved = new ArrayList<String>(8);
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_gaUserAccount");
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_gaProfile");
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_gaLogin");
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_gaPassword");
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_trackingEnabled");
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_trackedUrls");

            int id = -1;
            if (site.getSettings().getProperty(profile + "_" + site.getSiteKey() + "_profileId") != null) {
                id = Integer.parseInt(site.getSettings().getProperty(profile + "_" + site.getSiteKey() + "_profileId"));
            }
            propertiesToBeRemoved.add(profile + "_" + site.getSiteKey() + "_profileId");
            propertiesToBeRemoved.add("jahiaGAprofile_" + id + "_" + site.getSiteKey());


            try {
                sReg.getJahiaSitesService().removeSiteProperties(site, propertiesToBeRemoved);
                sReg.getCacheService().flushAllCaches();
            } catch (JahiaException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "manage_analytics.jsp");
        }
    }

    private void displayAnalyticsParams(HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws IOException, ServletException {
        JahiaAdministration.doRedirect(request,
                response,
                session,
                JSP_PATH + "manage_analytics.jsp");
    }

    private void diplayErrorMessage(HttpServletRequest request, HttpServletResponse response, HttpSession session, String msg, ProcessingContext jParams, String mode) throws IOException, ServletException {
        String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.request" + msg + "Error.label",
                jParams.getLocale());
        request.setAttribute("gaError", dspMsg);
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_analytics_form.jsp");
    }


    private void saveSite(String mode, HttpServletRequest request,
                          HttpServletResponse response,
                          HttpSession session) throws IOException, ServletException {
        if (sReg != null) {
            if (mode.equals("new")) {
                if (site.getSettings().get("profileCnt_" + site.getSiteKey()) != null) {
                    profileCnt = Integer.parseInt((String) site.getSettings().get("profileCnt_" + site.getSiteKey()));
                }
                profileCnt++;
                site.getSettings().setProperty("profileCnt_" + site.getSiteKey(), String.valueOf(profileCnt));
                site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_profileId", String.valueOf(profileCnt));
                site.getSettings().setProperty("jahiaGAprofile_" + profileCnt + "_" + site.getSiteKey(), jahiaGAprofile);
            }
            site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_gaUserAccount", gaUserAccount);
            site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_gaProfile", gaProfile);
            site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_gaLogin", gaLogin);
            site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_gaPassword", gaPassword);
            site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_trackingEnabled", String.valueOf(trackingEnabled));
            site.getSettings().setProperty(jahiaGAprofile + "_" + site.getSiteKey() + "_trackedUrls", trackedUrls);

            sMgr = sReg.getJahiaSitesService();
            try {
                sMgr.updateSite(site);
                //sReg.getCacheService().flushAllCaches();
                sReg.getInstance().getCacheService().getSkeletonCacheInstance().flushSkeletonsForSite(site.getID());// todo may  be not necessary
                displayAnalyticsParams(request, response, session);
            } catch (JahiaException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ServletException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

    }
}

