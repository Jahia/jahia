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
// $Id$
//
//  ManageSites
//
//  02.04.2001  MJ  added in jahia.
//  19.04.2001  AK  multisite first steps.
//  20.04.2001  AK  change selection way for re-use existant administrator.
//  21.04.2001  AK  complete add a site capability.
//  22.04.2001  AK  edit a site and delete a site capabilities.
//  27.04.2001  NK  added default site handling.
//  02.05.2001  NK	added templates, applications, users, files repository
//					full deletion from disk and db storage when deleting a whole site
//  04.05.2001  NK	Can delete all sites, no server site anymore
//  16.05.2001  NK  Handle special case when the current site is deleted -> need session synchronisation.
//  16.05.2001  NK  Integrate License Check.
//  23.05.2001  NK  Add Site reminder previous data by storing temporaty new site in session allowing
//					GO BACK to the previous screen.
//  27.09.2001  NK  Add Site , added template set choice step
//

package org.jahia.admin.sites;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.*;
import org.jahia.services.files.JahiaTextFileService;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.ImportAction;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.*;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.admin.AbstractAdministrationModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.jcr.RepositoryException;

import java.io.*;
import java.security.Principal;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * This class provides the business methods for sites
 * management, from the JahiaAdministration servlet.
 *
 * @author Khue Nguyen
 *         <p/>
 *         Copyright:    Copyright (c) 2002
 *         Company:      Jahia Ltd
 * @version 1.0
 */
public class ManageSites extends AbstractAdministrationModule {
// ------------------------------ FIELDS ------------------------------

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageSites.class);

    private static final String CLASS_NAME = JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private static JahiaSitesService sMgr;

    private License coreLicense;
    private ProcessingContext jParams;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                       HttpServletResponse response)
            throws Exception {

        ServicesRegistry sReg = ServicesRegistry.getInstance();
        if (sReg != null) {
            sMgr = sReg.getJahiaSitesService();
        }

        this.jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");

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

    /**
     * This method serves as a dispatcher for user requests.
     *
     * @param req  Servlet request.
     * @param res  Servlet response.
     * @param sess Servlet session for the current user.
     */
    private void userRequestDispatcher(HttpServletRequest req,
                                       HttpServletResponse res,
                                       HttpSession sess)
            throws IOException, ServletException {
        String op = req.getParameter("sub");
        if (op == null) {
            return;
        }
        if (op.equals("list")) {
            displayList(req, res, sess);
        } else if (op.equals("add")) {
            displayAdd(req, res, sess);
        } else if (op.equals("processadd")) {
            processAdd(req, res, sess);
        } else if (op.equals("displaycreateadmin")) {
            displayCreateAdmin(req, res, sess);
        } else if (op.equals("displayselectexistantadmin")) {
            displaySelectExistantAdmin(req, res, sess);
        } else if (op.equals("processcreateadmin")) {
            processCreateAdmin(req, res, sess);
        } else if (op.equals("processexistantadmin")) {
            processExistantAdmin(req, res, sess);
        } else if (op.equals("processadminselectsite")) {
            processExistantAdminSelectSite(req, res, sess);
        } else if (op.equals("processtemplatesetchoice")) {
            processTemplateSetChoice(req, res, sess);
        } else if (op.equals("edit")) {
            displayEdit(req, res, sess);
        } else if (op.equals("processedit")) {
            processEdit(req, res, sess);
        } else if (op.equals("delete")) {
            displayDelete(req, res, sess);
        } else if (op.equals("processdelete")) {
            processDelete(req, res, sess);
        } else if (op.equals("multipledelete")) {
            displayMultipleDelete(req, res, sess);
        } else if (op.equals("processmultipledelete")) {
            processMultipleDelete(req, res, sess);
        } else if (op.equals("prepareimport")) {
            prepareMultipleImport(req, res, sess);
        } else if (op.equals("processimport")) {
            processFileImport(req, res, sess);
        } else if (op.equals("createsite")) {
            createSite(req, res, sess);
        }
    } // userRequestDispatcher

    /**
     * Display the list of sites.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayList(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpSession session)
            throws IOException, ServletException {
        logger.debug(" display sites list started ");

        // retrieve previous form values...
        String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        String warningMsg = (String) request.getAttribute("warningMsg");
        if (warningMsg == null) {
            warningMsg = "";
        }

        // set default values...
        if (jahiaDisplayMessage == null) {
            jahiaDisplayMessage = Jahia.COPYRIGHT;
        }

        try {
            Iterator<JahiaSite> siteEnum = sMgr.getSites();
            List<JahiaSite> sortedSites = new ArrayList<JahiaSite>();
            while (siteEnum.hasNext()) {
                JahiaSite curSite = (JahiaSite) siteEnum.next();
                sortedSites.add(curSite);
            }
            Locale defaultLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
            if (defaultLocale != null) {
                Collections.sort(sortedSites, JahiaSite.getTitleComparator(defaultLocale));
            } else {
                Collections.sort(sortedSites, JahiaSite.getTitleComparator());
            }
            request.setAttribute("sitesList", sortedSites.iterator());
            if (sortedSites.size() == 0) {
                JahiaSite newJahiaSite = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");

                if (newJahiaSite == null) {
                    logger.debug("site not found in session, assuming new site ");

                    // it's the first time this screen is displayed, so create an empty one

                    newJahiaSite = new JahiaSite(-1,
                            "My Site",        // site title
                            "servername",        // site server name
                            "mySite",     // site key
                            // is active
                            // default page (homepage id)
                            "",        // description
                            null);
                }

                session.setAttribute(CLASS_NAME + "newJahiaSite", newJahiaSite);
                session.setAttribute(CLASS_NAME + "noSites", Boolean.TRUE);
            } else {
                session.removeAttribute(CLASS_NAME + "noSites");
            }
        } catch (JahiaException ex) {
            logger.error("Error while retrieving site list", ex);
            jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
        }
        // set request attributes...
        request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
        request.setAttribute("warningMsg", warningMsg);

        // redirect...
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "sites_management.jsp");
    } // end displayList

    /**
     * Display Add new site form
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayAdd(HttpServletRequest request,
                            HttpServletResponse response,
                            HttpSession session)
            throws IOException, ServletException {
        logger.debug(" display add site started ");

        // retrieve previous form values...
        String jahiaDisplayMessage = "";
        String warningMsg = (String) request.getAttribute("warningMsg");
        if (warningMsg == null) {
            warningMsg = "";
        }

        JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);

        // check it there is a temporary new site in session
        JahiaSite newJahiaSite = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");

        if (newJahiaSite == null) {
            logger.debug("site not found in session, assuming new site ");

            // it's the first time this screen is displayed, so create an empty one

            newJahiaSite = new JahiaSite(-1,
                    "mySite",        // site title
                    "servername",        // site server name
                    "mySite",     // site key
                    // is active
                    // default page (homepage id)
                    "",        // description
                    null);
        }


        Boolean defaultSite = (Boolean) session.getAttribute(CLASS_NAME + "defaultSite");
        if (defaultSite == null) {
            try {
                if (ServicesRegistry.getInstance().getJahiaSitesService().getNbSites() > 0 ) {
                    defaultSite = Boolean.FALSE;
                }
                else {
                    defaultSite = Boolean.TRUE;
                }
            } catch (JahiaException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                defaultSite = Boolean.TRUE;
            }
        }

        // store in session
        session.setAttribute(CLASS_NAME + "newJahiaSite", newJahiaSite);
        session.setAttribute(CLASS_NAME + "defaultSite", defaultSite);

        session.setAttribute(CLASS_NAME + "adminPassword", "");

        List<JahiaSite> grantedSites = new ArrayList<JahiaSite>();
        if (theUser != null) {
            try {
                grantedSites = ServicesRegistry.getInstance()
                        .getJahiaGroupManagerService()
                        .getAdminGrantedSites(theUser);
            } catch (JahiaException je) {
            }
            if (grantedSites == null) {
                grantedSites = new ArrayList<JahiaSite>();
            } else {
                Locale defaultLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
                if (defaultLocale != null) {
                    Collections.sort(grantedSites, JahiaSite.getTitleComparator(defaultLocale));
                } else {
                    Collections.sort(grantedSites, JahiaSite.getTitleComparator());
                }
            }
        }

        // set request attributes...
        request.setAttribute("newJahiaSite", newJahiaSite);
        request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
        request.setAttribute("warningMsg", warningMsg);
        request.setAttribute("defaultSite", defaultSite);
        request.setAttribute("newAdminOnly", Boolean.valueOf(grantedSites.size() <= 0));
        String enforcePasswordPolicy = newJahiaSite.getSettings().getProperty(
                JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY);
        request.setAttribute(JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY,
                enforcePasswordPolicy != null ? enforcePasswordPolicy
                        : "true");

        try {
            // logger.debug(" license check ");

            // get the number of sites in db
            int nbSites = ServicesRegistry.getInstance().getJahiaSitesService().getNbSites();

            request.setAttribute("nbSites", new Integer(nbSites));

            // set license info
            request.setAttribute("siteLimit", new Integer(Jahia.getSiteLimit()));

            //logger.debug(" let go in ");

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_add.jsp");
        } catch (JahiaException je) {
            logger.error("Error while retrieving number of site in database", je);
            // set request attributes...
            jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "sites_management.jsp");
        }
    } // end displayAdd

    /**
     * Process Add new site form
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processAdd(HttpServletRequest request,
                            HttpServletResponse response,
                            HttpSession session)
            throws IOException, ServletException {
        logger.debug("started");

        boolean processError = true;

        // get form values...
        String siteTitle = StringUtils.left(JahiaTools.getStrParameter(request, "siteTitle", "").trim(), 100);
        String siteServerName = StringUtils.left(JahiaTools.getStrParameter(request, "siteServerName", "").trim(), 200);
        String siteKey = StringUtils.left(JahiaTools.getStrParameter(request, "siteKey", "").trim(), 50);
        String siteDescr = StringUtils.left(JahiaTools.getStrParameter(request, "siteDescr", "").trim(), 250);
        String siteAdmin = JahiaTools.getStrParameter(request, "siteAdmin", "").trim();
        Boolean defaultSite = Boolean.valueOf(request.getParameter("defaultSite") != null);

        request.getSession().setAttribute("siteAdminOption", siteAdmin);
        session.setAttribute("siteAdminOption", siteAdmin);
        String warningMsg = "";
        boolean enforcePasswordPolicy = (request
                .getParameter(JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY) != null);
        session.setAttribute(CLASS_NAME + "defaultSite", defaultSite);

        // check license limitation again
        // get the number of sites in db
        if (!Jahia.checkSiteLimit()) {
            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "sites_management.jsp");
            return;
        }

        // create jahia site object if checks are in green light...
        try {
            // check validity...
            if (siteTitle != null && (siteTitle.length() > 0)
                    && siteServerName != null && (siteServerName.length() > 0)
                    && siteKey != null && (siteKey.length() > 0)) {
                if (!JahiaTools.isAlphaValid(siteKey)) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label",
                            jParams.getLocale());
                } else if (siteKey.equals("site")) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label",
                            jParams.getLocale());
                } else if (!isServerNameValid(siteServerName)) { 
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.invalidServerName.label",
                            jParams.getLocale());
                } else if (siteServerName.equals("default")) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.chooseAnotherServerName.label",
                            jParams.getLocale());
                } else if (siteServerName.equals("localhost")) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.chooseAnotherServerName.label",
                            jParams.getLocale());
                } else if (sMgr.getSite(siteServerName) != null) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.chooseAnotherServerName.label",
                            jParams.getLocale());
                } else if (sMgr.getSiteByKey(siteKey) != null) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label",
                            jParams.getLocale());
                } else {
                    processError = false;
                }
            } else {
                warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.completeRequestInfo.label",
                        jParams.getLocale());
            }

            if (!processError) {
                // save new jahia site...
                JahiaSite site = new JahiaSite(-1,
                        siteTitle,
                        siteServerName,
                        siteKey,
                        // is active
                        // default page (homepage id)... subject to update in terminateAdd().
                        siteDescr,
                        null);
//                site.setTemplatesAutoDeployMode(true);
//                site.setWebAppsAutoDeployMode(true);

                site.getSettings().setProperty(
                        JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY,
                        enforcePasswordPolicy ? "true" : "false");


                // set in session...
                session.setAttribute(CLASS_NAME + "newJahiaSite", site);

                // all is okay, go to add admin or use existent admin...
                if (siteAdmin.trim().equals("0")) {
                    displayCreateAdmin(request, response, session);
                } else if (siteAdmin.trim().equals("1")) {
                    displaySelectExistantAdmin(request, response, session);
                } else {
                    displayTemplateSetChoice(request, response, session);
                }

                site = null;
            } else {
                JahiaSite site = new JahiaSite(-1,
                        siteTitle,
                        siteServerName,
                        siteKey,
                        siteDescr,
                        null);


                site.getSettings().setProperty(
                        JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY,
                        enforcePasswordPolicy ? "true" : "false");
                session.setAttribute(CLASS_NAME + "newJahiaSite", site);
                request.setAttribute("newJahiaSite", site);
                request.setAttribute("warningMsg", warningMsg);

                if (session.getAttribute(CLASS_NAME + "noSites") != null) {
                    displayList(request, response, session);
                } else {
                    displayAdd(request, response, session);
                }
            }
        } catch (JahiaException ex) {
            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                    jParams.getLocale());
            request.setAttribute("warningMsg", warningMsg);
            displayAdd(request, response, session);
        }
        finally {
            siteTitle = null;
            siteServerName = null;
            siteKey = null;
            siteDescr = null;
            siteAdmin = null;
            defaultSite = null;
            warningMsg = null;
            request.getSession().setAttribute("lastPage", "processadd");
        }
    } // end processAdd

    private boolean isServerNameValid(String serverName) {
        return !serverName.contains(" ");
    }

    /**
     * Display page to create an administrator for the new site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayCreateAdmin(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display create administrator started ");

        request.getSession().setAttribute("admin", "create");

        String warningMsg = (String) request.getAttribute("warningMsg");
        if (warningMsg == null) {
            warningMsg = "";
        }
        // retrieve previous form values...
        String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        String adminUsername = (String) request.getAttribute("adminUsername");
        String adminPassword = (String) request.getAttribute("adminPassword");
        String adminConfirm = (String) request.getAttribute("adminConfirm");
        String adminFirstName = (String) request.getAttribute("adminFirstName");
        String adminLastName = (String) request.getAttribute("adminLastName");
        String adminOrganization = (String) request.getAttribute("adminOrganization");
        String adminEmail = (String) request.getAttribute("adminEmail");

        // set default values...
        if (jahiaDisplayMessage == null) {
            jahiaDisplayMessage = Jahia.COPYRIGHT;
        }
        if (adminUsername == null) {
            adminUsername = (String) session.getAttribute(CLASS_NAME + "adminUsername");
        }
        if (adminUsername == null) {
            JahiaUserManagerService userManager = ServicesRegistry.getInstance()
                    .getJahiaUserManagerService();
            for (int i = 1; adminUsername == null; i++) {
                String testUserName = "siteadmin";
                if (i > 1) {
                    testUserName += i;
                }
                if (userManager.lookupUser(testUserName) == null) {
                    adminUsername = testUserName;
                }
            }
        }
        if (adminPassword == null) {
            adminPassword = (String) session.getAttribute(CLASS_NAME + "adminPassword");
            adminConfirm = adminPassword;
        }
        if (adminPassword == null) {
            adminPassword = "";
            adminConfirm = "";
        }

        Properties userProps = (Properties) session.getAttribute(CLASS_NAME + "adminProps");

        if (adminFirstName == null && userProps != null) {
            adminFirstName = userProps.getProperty("firstname");
        }
        if (adminFirstName == null) {
            adminFirstName = "Site administrator";
        }
        if (adminLastName == null && userProps != null) {
            adminLastName = userProps.getProperty("lastname");
        }
        if (adminLastName == null) {
            adminLastName = "";
        }
        if (adminOrganization == null && userProps != null) {
            adminOrganization = userProps.getProperty("organization");
        }
        if (adminOrganization == null) {
            adminOrganization = "";
        }
        if (adminEmail == null && userProps != null) {
            adminEmail = userProps.getProperty("email");
        }
        if (adminEmail == null) {
            adminEmail = "";
        }

        // set display message...
        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);

        // set request attributes...
        request.setAttribute("adminUsername", adminUsername);
        request.setAttribute("adminPassword", adminPassword);
        request.setAttribute("adminConfirm", adminConfirm);
        request.setAttribute("adminFirstName", adminFirstName);
        request.setAttribute("adminLastName", adminLastName);
        request.setAttribute("adminOrganization", adminOrganization);
        request.setAttribute("adminEmail", adminEmail);
        request.setAttribute("warningMsg", warningMsg);

        // redirect...
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_create_admin.jsp");
        request.getSession().setAttribute("lastPage", "processcreateadmin");
    } // end displayCreateAdmin

    /**
     * Display page to select an existant administrator for the new site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displaySelectExistantAdmin(HttpServletRequest request,
                                            HttpServletResponse response,
                                            HttpSession session)
            throws IOException, ServletException {
        //logger.debug("display select existant administrator started ");

        request.getSession().setAttribute("admin", "create");

        // retrieve previous form values...
        String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        String selectedSite = (String) request.getAttribute("selectedSite");

        // set default values...
        if (jahiaDisplayMessage == null) {
            jahiaDisplayMessage = Jahia.COPYRIGHT;
        }
        if (selectedSite == null) {
            if (request.getParameter("site") == null) {
                selectedSite = "0";
            } else {
                selectedSite = request.getParameter("site");
            }
        }

        try {
            // get admins list...
            JahiaGroupManagerService groupManager = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            Iterator<JahiaSite> allSites = sMgr.getSites();
            List<JahiaSite> sitesList = new ArrayList<JahiaSite>();
            Integer siteIDInteger = new Integer(selectedSite);

            // clean sites...
            while (allSites.hasNext()) {
                JahiaSite site = (JahiaSite) allSites.next();
                if (site.getID() > 0) {
                    sitesList.add(site);
                }
            }

            // set the Iterator to null if the List is empty...
            if (sitesList.size() == 0) {
                allSites = null;
            } else {
                Locale defaultLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
                if (defaultLocale != null) {
                    Collections.sort(sitesList, JahiaSite.getTitleComparator(defaultLocale));
                } else {
                    Collections.sort(sitesList, JahiaSite.getTitleComparator());
                }
                allSites = sitesList.iterator();
            }

            // get users... only if allSites is not null...
            if ((allSites != null) && (!selectedSite.equals("0"))) {
                List<Map<String, String>> allAdministrators = new ArrayList<Map<String, String>>();

                JahiaGroup adminGroup = groupManager.getAdministratorGroup(siteIDInteger.intValue());
                Enumeration<Principal> admins = adminGroup.members();

                while (admins.hasMoreElements()) {
                    try {
                        JahiaUser user = (JahiaUser) admins.nextElement();
                        if (!user.isRoot()) {
                            Map<String, String> adminHash = new HashMap<String, String>();
                            adminHash.put("key", user.getUserKey());
                            adminHash.put("username", user.getUsername());
                            allAdministrators.add(adminHash);
                        }
                    } catch (Exception e) {
                        // do nothing...
                    }
                }

                request.setAttribute("adminsList", allAdministrators.iterator());
            }

            // set attributes...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            request.setAttribute("allSites", allSites);
            request.setAttribute("allSitesJS", sitesList.iterator());
            request.setAttribute("selectedSite", new Integer(selectedSite));

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_existant_admin.jsp");
        } catch (JahiaException je) {
            logger.error("Error while displaying existing administrator selection UI", je);
            displayList(request, response, session);
        }

        // set default values...
        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
    } // end displaySelectExistantAdmin

    /**
     * Process create new administrator for this new site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processCreateAdmin(HttpServletRequest request,
                                    HttpServletResponse response,
                                    HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" process create administrator started ");
        boolean processError = true;

        // get form values...
        String adminUsername = JahiaTools.getStrParameter(request, "adminUsername", "").trim();
        String adminPassword = JahiaTools.getStrParameter(request, "adminPassword", "").trim();
        String adminConfirm = JahiaTools.getStrParameter(request, "adminConfirm", "").trim();
        String adminFirstName = JahiaTools.getStrParameter(request, "adminFirstName", "").trim();
        String adminLastName = JahiaTools.getStrParameter(request, "adminLastName", "").trim();
        String adminOrganization = JahiaTools.getStrParameter(request, "adminOrganization", "").trim();
        String adminEmail = JahiaTools.getStrParameter(request, "adminEmail", "").trim();
        String warningMsg = "";
        // set request attributes...
        request.setAttribute("adminUsername", adminUsername);
        request.setAttribute("adminPassword", adminPassword);
        request.setAttribute("adminConfirm", adminConfirm);
        request.setAttribute("adminFirstName", adminFirstName);
        request.setAttribute("adminLastName", adminLastName);
        request.setAttribute("adminOrganization", adminOrganization);
        request.setAttribute("adminEmail", adminEmail);

        // get current user...
        JahiaUserManagerService userManager = ServicesRegistry.getInstance()
                .getJahiaUserManagerService();
        JahiaUser currentUser = userManager.
                lookupUser(adminUsername);

        if (currentUser != null) {
            warningMsg = JahiaResourceBundle.getJahiaInternalResource("label.nextStep",
                    jParams.getLocale());
            warningMsg += " [" + adminUsername + "] ";
            warningMsg += JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.alreadyExist.label",
                    jParams.getLocale()) + " ";
            warningMsg += JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.onAnotherSite.label", jParams.getLocale()) + ".";
        } else if (adminUsername.length() == 0) {
            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.specifyUserName.label",
                    jParams.getLocale());
        } else if (!adminPassword.equals(adminConfirm)) {
            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.confirmPasswdBeSame.label",
                    jParams.getLocale());
        } else if (adminPassword.length() == 0) {
            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.userMessage.specifyPassword.label",
                    jParams.getLocale());
        } else if (!userManager.isUsernameSyntaxCorrect(adminUsername)) {
            warningMsg = StringUtils.capitalize(JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.users.ManageUsers.onlyCharacters.label",
                    jParams.getLocale()));
        } else {
            JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry
                    .getInstance().getJahiaPasswordPolicyService();
            JahiaSite newSite = (JahiaSite) session.getAttribute(CLASS_NAME
                    + "newJahiaSite");
            if (newSite != null
                    && "true".equals(newSite.getSettings().get(
                            JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY))) {
                PolicyEnforcementResult evalResult = pwdPolicyService
                        .enforcePolicyOnUserCreate(new JahiaDBUser(-1,
                                adminUsername, adminPassword, null, null),
                                adminPassword);
                if (!evalResult.isSuccess()) {
                    EngineMessages policyMsgs = evalResult.getEngineMessages();
                    policyMsgs.saveMessages(((ParamBean) jParams).getRequest());
                } else {
                    processError = false;
                }
            } else {
                processError = false;
            }
        }

        if (!processError) {
            // compose user properties...
            Properties userProps = new Properties();
            userProps.setProperty("firstname", adminFirstName);
            userProps.setProperty("lastname", adminLastName);
            userProps.setProperty("email", adminEmail);
            userProps.setProperty("organization", adminOrganization);

            session.setAttribute(CLASS_NAME + "adminUsername", adminUsername);
            session.setAttribute(CLASS_NAME + "adminPassword", adminPassword);
            session.setAttribute(CLASS_NAME + "adminProps", userProps);
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);

            // reset session
            session.setAttribute(CLASS_NAME + "existantAdminUser", null);

            JahiaTemplateManagerService templateManager = ServicesRegistry
                    .getInstance().getJahiaTemplateManagerService();
            if (templateManager != null && (templateManager.getAvailableTemplatePackagesCount() > 0)) {
                displayTemplateSetChoice(request, response, session);
            } else {
                // redirection to full sites list...
                if (!terminateAdd(request, session)) {
                    displayAdd(request, response, session);
                }
                redirectAfterAdd(request, response, session);
            }
        } else {
            request.setAttribute("warningMsg", warningMsg);
            displayCreateAdmin(request, response, session);
        }
    } // end processCreateAdmin

    /**
     * Display page to let user choose a set of templates.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayTemplateSetChoice(HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpSession session)
            throws IOException, ServletException {
        logger.debug("Display template set choice started ");

        // retrieve previous form values...
        String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
        // set default values...
        if (jahiaDisplayMessage == null) {
            jahiaDisplayMessage = Jahia.COPYRIGHT;
        }

        String selectedTmplSet = (String) request.getAttribute("selectedTmplSet");

        JahiaTemplateManagerService templateService = ServicesRegistry
                .getInstance().getJahiaTemplateManagerService();
        List<JahiaTemplatesPackage> templateSets = templateService.getAvailableTemplatePackages();

        TreeMap<String, JahiaTemplatesPackage> orderedTemplateSets = new TreeMap<String, JahiaTemplatesPackage>();
        for (JahiaTemplatesPackage tmp : templateSets) {
            orderedTemplateSets.put(tmp.getName(), tmp);
        }

        // try to select the default set if not selected
        if (selectedTmplSet == null) {
            selectedTmplSet = templateService.getDefaultTemplatePackage()
                    .getName();
        }

        JahiaTemplatesPackage selectedPackage = selectedTmplSet != null ? templateService
                .getTemplatePackage(selectedTmplSet)
                : null;
        request.setAttribute("selectedTmplSet", selectedTmplSet);
        request.setAttribute("tmplSets", orderedTemplateSets.values());
        request.setAttribute("selectedPackage", selectedPackage);
        Locale currentLocale = (Locale) session
                .getAttribute(ProcessingContext.SESSION_LOCALE);
        if (currentLocale == null) {
            currentLocale = request.getLocale();
        }
        Locale selectedLocale = (Locale) session.getAttribute(CLASS_NAME
                + "selectedLocale");
        if (selectedLocale == null) {
            selectedLocale = LanguageCodeConverters.languageCodeToLocale(Jahia
                    .getSettings().getDefaultLanguageCode());
        }
        session.setAttribute(CLASS_NAME + "selectedLocale", selectedLocale);
        request.setAttribute("selectedLocale", selectedLocale);
        request.setAttribute("currentLocale", currentLocale);

        logger.debug("Nb template set found " + templateSets.size());

        // redirect...
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_choose_template_set.jsp");

        // set default values...
        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
    }

    /**
     * Terminate the creation of a new site. Create the site and dependancies.
     *
     * @param request Servlet request.
     * @param session HttpSession object.
     */
    public boolean terminateAdd(HttpServletRequest request,
                                HttpSession session)
            throws IOException, ServletException {

        String warningMsg = "";

        JahiaSite currentSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        // get objects temporary stocked in session...
        JahiaSite site = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");
        Boolean defaultSite = (Boolean) session.getAttribute(CLASS_NAME + "defaultSite");
        JahiaUser existantAdminUser = (JahiaUser) session.getAttribute(CLASS_NAME + "existantAdminUser");
        Locale selectedLocale = (Locale) session.getAttribute(CLASS_NAME + "selectedLocale");

        try {
            JahiaUser adminSiteUser = null;

            // get services...
            JahiaUserManagerService jums = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaSitesService jsms = ServicesRegistry.getInstance().getJahiaSitesService();

            JahiaUser currentUser = jums.lookupUser((String) session.getAttribute(CLASS_NAME + "jahiaLoginUsername"));
            // add the site in siteManager...
            site = jsms.addSite(currentUser, site.getTitle(), site.getServerName(), site.getSiteKey(), site.getDescr(),
                    site.getSettings(), selectedLocale, (String) request.getAttribute("selectedTmplSet"), (String) request.getAttribute("firstImport"),
                    (File) request.getAttribute("fileImport"),
                    (String) request.getAttribute("fileImportName"), (Boolean) request.getAttribute("asAJob"),
                    (Boolean) request.getAttribute("doImportServerPermissions"), jParams);
            if (site != null) {
                // set as default site
                if (defaultSite.booleanValue()) {
                    changeDefaultSite(site);
                }

                // create administrator user if requested...
                if (existantAdminUser == null) {
                    // get session administrator properties...
                    String adminUsername = (String) session.getAttribute(CLASS_NAME + "adminUsername");
                    String adminPassword = (String) session.getAttribute(CLASS_NAME + "adminPassword");
                    Properties adminProps = (Properties) session.getAttribute(CLASS_NAME + "adminProps");
                    if (adminUsername != null) {
                        // create user...
                        adminSiteUser = jums.createUser(adminUsername, adminPassword, adminProps);
                    }
                } else {
                    adminSiteUser = existantAdminUser;         // the administrator already exists... use this.
                }

                if (adminSiteUser != null) {
                    // attach admin to administrators group...
                    JahiaSiteTools.getAdminGroup(site).addMember(adminSiteUser);

                    // create admin membership for this site...
                    JahiaSiteTools.addMember(adminSiteUser, site);
                }

                // set as current site if the session site is null
                if (session.getAttribute(ProcessingContext.SESSION_SITE) == null) {
                    session.setAttribute(ProcessingContext.SESSION_SITE, site);
                    session.setAttribute(JahiaAdministration.CLASS_NAME + "manageSiteID", new Integer(site.getID()));
                }

                // set new site in session
                session.setAttribute(CLASS_NAME + "newJahiaSite", site);

                if (currentSite != null) {
                    jParams.setSite(currentSite);
                    jParams.setSiteID(currentSite.getID());
                }
                sMgr.updateSite(site);
            } else {
                warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                        jParams.getLocale());
                request.setAttribute("warningMsg", warningMsg);

                if (currentSite != null) {
                    jParams.setSite(currentSite);
                    jParams.setSiteID(currentSite.getID());
                }
                return false;
            }
        } catch (Exception ex) {
            // clean site
            try {
                JahiaUserManagerService userServ = ServicesRegistry.getInstance().getJahiaUserManagerService();
                Object[] src = userServ.getProviderList().toArray();
                String[] provs = new String[src.length];
                System.arraycopy(src, 0, provs, 0, src.length);
                delete(site, jParams.getUser(), true);
                sMgr.removeSite(site);
            } catch (Exception t) {
                logger.error("Error while cleaning site", t);
            }

            logger.error("Error while adding site", ex);

            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                    jParams.getLocale());
            request.setAttribute("warningMsg", warningMsg);
            return false;
        }
        return true;
    } // end terminateAdd

    /**
     * Set the site as default site in storage, if site == null , set default site to undefined
     *
     * @param site the site
     */
    private void changeDefaultSite(JahiaSite site) {
        //Site changes flush the ESI cache
        ServicesRegistry.getInstance().getJahiaSitesService().setDefaultSite(site);        
    }

    /**
     * Process select existant administrator for this new site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processExistantAdmin(HttpServletRequest request,
                                      HttpServletResponse response,
                                      HttpSession session)
            throws IOException, ServletException {

        // get form values...
        String adminSelected = request.getParameter("adminSelected");
        String siteID = request.getParameter("site");

        if (adminSelected == null) {     // it's only the choice of site... display user list.
            request.setAttribute("selectedSite", siteID);
            String jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.chooseUserInList.label",
                    jParams.getLocale());
            request.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            displaySelectExistantAdmin(request, response, session);
        } else {
            // get the user...
            JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaUser theUser = userManager.lookupUserByKey(adminSelected);

            session.setAttribute(CLASS_NAME + "existantAdminUser", theUser);

            JahiaTemplateManagerService templateManager = ServicesRegistry
                    .getInstance().getJahiaTemplateManagerService();
            if (templateManager != null && (templateManager.getAvailableTemplatePackagesCount() > 0)) {
                displayTemplateSetChoice(request, response, session);
            } else {
                if (!terminateAdd(request, session)) {
                    displayAdd(request, response, session);
                }
                // redirection to full sites list...
                displayList(request, response, session);
            }
        }
    }

    /**
     * Process select existant administrator for this new site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processExistantAdminSelectSite(HttpServletRequest request,
                                                HttpServletResponse response,
                                                HttpSession session)
            throws IOException, ServletException {

        request.setAttribute("selectedSite", request.getParameter("site"));
        displaySelectExistantAdmin(request, response, session);
    } // end processExistantAdminSelectSite

    /**
     * Process template set choice.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processTemplateSetChoice(HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpSession session)
            throws IOException, ServletException {
        logger.debug("Process template set choice started ");

        // get form values...
        String operation = jParams.getParameter("operation");
        String selectTmplSet = jParams.getParameter("selectTmplSet");
        String siteAdminOption = (String) session.getAttribute("siteAdminOption");
        if (selectTmplSet == null || selectTmplSet.trim().equals("")) {
            selectTmplSet = "0";
        }

        logger.debug("operation = " + operation);
        logger.debug("selected template = " + selectTmplSet);

        request.setAttribute("siteAdminOption", siteAdminOption);
        request.setAttribute("selectedTmplSet", selectTmplSet);

        String firstImport = jParams.getParameter("firstImport");
        request.setAttribute("firstImport", firstImport);

        if (jParams.getParameter("importpath") != null) {
            File file = new File(jParams.getParameter("importpath"));
            if (file.exists()) {
                request.setAttribute("fileImport", file);
            }
        }

        String selectedLanguage = JahiaTools.getStrParameter(jParams, "languageList", "").trim();
        if (!selectedLanguage.equals("")) {
            session.setAttribute(CLASS_NAME + "selectedLocale",
                    LanguageCodeConverters.languageCodeToLocale(selectedLanguage));
        }
        request.setAttribute("selectedlanguage", selectedLanguage);

        if (operation == null || !operation.trim().equals("save")
                || selectTmplSet.equals("0")) {
            displayTemplateSetChoice(request, response, session);
        } else {
            displayNewSiteValues(request, response, session);
        }
    }

    private void createSite(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, ServletException {
        // selected template
        String selectTmplSet = jParams.getParameter("selectTmplSet");
        if (selectTmplSet == null || selectTmplSet.trim().equals("")) {
            selectTmplSet = "0";
        }
        logger.debug("selected template = " + selectTmplSet);
        request.setAttribute("selectedTmplSet", selectTmplSet);

        // first import
        String firstImport = jParams.getParameter("firstImport");
        request.setAttribute("firstImport", firstImport);

        // import path
        if (jParams.getParameter("importpath") != null) {
            File file = new File(jParams.getParameter("importpath"));
            if (file.exists()) {
                request.setAttribute("fileImport", file);
            }
        }

        // language
        String selectedLanguage = JahiaTools.getStrParameter(jParams, "languageList", "").trim();
        if (!selectedLanguage.equals("")) {
            session.setAttribute(CLASS_NAME + "selectedLocale",
                    LanguageCodeConverters.languageCodeToLocale(selectedLanguage));
        }
        request.setAttribute("selectedlanguage", selectedLanguage);

        if (!terminateAdd(request, session)) {
            displayAdd(request, response, session);
        }

        JahiaSite site = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");
        logger.debug("Site = " + site.getID());

        // reset session
        session.setAttribute(CLASS_NAME + "newJahiaSite", null);

        redirectAfterAdd(request, response, session);
    }

    /**
     * Display Edit Site form
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayEdit(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display edit site started ");

        try {
            // get site...
            String site_id = JahiaTools.getStrParameter(request, "siteid", "").trim();
            Integer siteID = new Integer(site_id);
            JahiaSite site = sMgr.getSite(siteID.intValue());

            // retrieve previous form values...
            String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
            String siteTitle = (String) request.getAttribute("siteTitle");
            String siteServerName = (String) request.getAttribute("siteServerName");
            String siteKey = (String) request.getAttribute("siteKey");
            String siteDescr = (String) request.getAttribute("siteDescr");
            String warningMsg = (String) request.getAttribute("warningMsg");

            Boolean defaultSite = Boolean.FALSE;

            String enforcePasswordPolicy = site.getSettings().getProperty(
                    JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY);
            request.setAttribute(JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY,
                    enforcePasswordPolicy != null ? enforcePasswordPolicy
                            : "false");


            if (request.getAttribute("defaultSite") == null) {
                JahiaSite defSite = getDefaultSite();
                if (defSite != null && defSite.getSiteKey().equals(site.getSiteKey())) {
                    defaultSite = Boolean.TRUE;
                }
            } else {
                defaultSite = (Boolean) request.getAttribute("defaultSite");
            }

            // set default values...
            if (jahiaDisplayMessage == null) {
                jahiaDisplayMessage = Jahia.COPYRIGHT;
            }
            if (siteTitle == null) {
                siteTitle = site.getTitle();
            }
            if (siteServerName == null) {
                siteServerName = site.getServerName();
            }
            if (siteKey == null) {
                siteKey = site.getSiteKey();
            }
            if (siteDescr == null) {
                siteDescr = site.getDescr();
            }
            if (warningMsg == null) {
                warningMsg = "";
            }

            // set request attributes...
            request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
            request.setAttribute("siteTitle", siteTitle);
            request.setAttribute("siteServerName", siteServerName);
            request.setAttribute("siteKey", siteKey);
            request.setAttribute("siteDescr", siteDescr);
            request.setAttribute("warningMsg", warningMsg);
            request.setAttribute("siteID", siteID);
            request.setAttribute("defaultSite", defaultSite);
            request.setAttribute("siteTemplatePackageName", site.getTemplatePackageName());

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_edit.jsp");
        } catch (Exception e) {
            logger.error("Error while dislaying site edition UI", e);
            // redirect to list...
            String jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.processingError.label",
                    jParams.getLocale());
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            displayList(request, response, session);

            // reset display message...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
        }
    } // end displayEdit

    /**
     * Display Edit Site form
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayNewSiteValues(HttpServletRequest request,
                                      HttpServletResponse response,
                                      HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display edit site started ");

        try {
            // get site...
            JahiaSite site = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");

            // retrieve previous form values...
            String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
            String siteTitle = site.getTitle();
            String siteServerName = site.getServerName();
            String siteKey = site.getSiteKey();
            String siteDescr = site.getDescr();
           

            String selectedTmplSet = (String) request.getAttribute("selectedTmplSet");
            // get tmplPackage list...
            JahiaTemplateManagerService templateMgr = ServicesRegistry
                    .getInstance().getJahiaTemplateManagerService();

            JahiaTemplatesPackage tmplPack = templateMgr
                    .getTemplatePackage(selectedTmplSet);

            String enforcePasswordPolicy = site.getSettings().getProperty(JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY);
            request.setAttribute(JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY, enforcePasswordPolicy != null ? enforcePasswordPolicy : "false");

            Boolean defaultSite = Boolean.FALSE;
            if (request.getAttribute("defaultSite") == null) {
                JahiaSite defSite = getDefaultSite();
                if (defSite != null && defSite.getSiteKey().equals(site.getSiteKey())) {
                    defaultSite = Boolean.TRUE;
                }
            } else {
                defaultSite = (Boolean) request.getAttribute("defaultSite");
            }

            Locale selectedLocale = (Locale) session.getAttribute(CLASS_NAME + "selectedLocale");
            if (selectedLocale == null) {
                selectedLocale = LanguageCodeConverters.languageCodeToLocale(org.jahia.settings.SettingsBean.getInstance().getDefaultLanguageCode());
            }


            // set request site attributes...
            request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
            request.setAttribute("siteTitle", siteTitle);
            request.setAttribute("siteServerName", siteServerName);
            request.setAttribute("siteKey", siteKey);
            request.setAttribute("siteDescr", siteDescr);
            request.setAttribute("siteID", site.getID());
            request.setAttribute("defaultSite", defaultSite);

            if (tmplPack != null) {
                request.setAttribute("templateName", tmplPack.getName());
            }
            request.setAttribute("selectedTmplSet", selectedTmplSet);
            request.setAttribute("selectedLocale", selectedLocale);
            // set display message...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);

            String adminUsername = (String) session.getAttribute(CLASS_NAME + "adminUsername");
            if (adminUsername == null) {
                adminUsername = "siteadmin";
            }
            request.setAttribute("adminUsername", adminUsername);

            // user properties
            Properties userProps;
            JahiaUser existantAdminUser = (JahiaUser) session.getAttribute(CLASS_NAME + "existantAdminUser");
            if (existantAdminUser == null) {
                userProps = (Properties) session.getAttribute(CLASS_NAME + "adminProps");
            } else {
                userProps = existantAdminUser.getProperties();
            }
            if (userProps != null) {
                request.setAttribute("adminFirstName", userProps.getProperty("firstname"));
                request.setAttribute("adminLastName", userProps.getProperty("lastname"));
                request.setAttribute("adminOrganization", userProps.getProperty("organization"));
                request.setAttribute("adminEmail", userProps.getProperty("email"));
            }

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_create_display_values.jsp");
        } catch (Exception e) {
            logger.error("Error while displaying new site values",e);
            // redirect to list...
            String jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.processingError.label",
                    jParams.getLocale());
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            displayList(request, response, session);

            // reset display message...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
        }
    } // end displayEdit


    /**
     * Return the default site or null if not found or undefined
     *
     * @return JahiaSite the default site
     */
    private JahiaSite getDefaultSite() {
        return ServicesRegistry.getInstance().getJahiaSitesService().getDefaultSite();
    }

    /**
     * Process Edit Site form
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processEdit(HttpServletRequest request,
                             HttpServletResponse response,
                             HttpSession session)
            throws IOException, ServletException {
        logger.debug(" process edit site started ");

        boolean processError = false;

        // get form values...
        String siteTitle = StringUtils.left(JahiaTools.getStrParameter(request, "siteTitle", "").trim(), 100);
        String siteServerName = StringUtils.left(JahiaTools.getStrParameter(request, "siteServerName", "").trim(), 200);
//		String  siteKey			    = JahiaTools.getStrParameter(request,"siteKey","").toLowerCase().trim();
        String siteDescr = StringUtils.left(JahiaTools.getStrParameter(request, "siteDescr", "").trim(), 250);

        String warningMsg = "";
        boolean defaultSite = (request.getParameter("defaultSite") != null);
        boolean enforcePasswordPolicy = (request
                .getParameter(JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY) != null);

        boolean versioningEnabled = (request.getParameter("versioningEnabled") != null);
        boolean stagingEnabled = (request.getParameter("stagingEnabled") != null);

        // set request attributes...
        request.setAttribute("siteTitle", siteTitle);
        request.setAttribute("siteServerName", siteServerName);
        request.setAttribute("siteDescr", siteDescr);
        request.setAttribute("defaultSite", Boolean.valueOf(defaultSite));
        request.setAttribute("versioningEnabled", Boolean.valueOf(versioningEnabled));
        request.setAttribute("stagingEnabled", Boolean.valueOf(stagingEnabled));

        try {
            // get site...
            String site_id = JahiaTools.getStrParameter(request, "siteid", "").trim();
            Integer siteID = new Integer(site_id);
            JahiaSite site = sMgr.getSite(siteID.intValue());

            // check validity...
            if (siteTitle != null && (siteTitle.trim().length() > 0)
                    && siteServerName != null && (siteServerName.trim().length() > 0)
                    ) {
                if (!isServerNameValid(siteServerName)) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.invalidServerName.label",
                            jParams.getLocale());
                    processError = true;
                } else if (!site.getServerName().equals(siteServerName)) {
                    if (sMgr.getSite(siteServerName) != null) {
                        warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.chooseAnotherServerName.label",
                                jParams.getLocale());
                        processError = true;
                    }
                }
            } else {
                warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.completeRequestInfo.label",
                        jParams.getLocale());
            }

            if (!processError) {
                // save modified informations...
                site.setTitle(siteTitle);
                site.setServerName(siteServerName);
                site.setDescr(siteDescr);

                site.getSettings().setProperty(
                        JahiaSite.PROPERTY_ENFORCE_PASSWORD_POLICY,
                        enforcePasswordPolicy ? "true" : "false");
                sMgr.updateSite(site);

                JahiaSite defSite = getDefaultSite();
                if (defaultSite) {
                    if (defSite == null) {
                        changeDefaultSite(site);
                    } else if (!defSite.getSiteKey().equals(site.getSiteKey())) {
                        changeDefaultSite(site);
                    }
                } else {
                    if (defSite != null && defSite.getSiteKey().equals(site.getSiteKey())) {
                        changeDefaultSite(null);
                    }
                }

                // set message default values...
                session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);

                // redirect...
                displayList(request, response, session);
            } else {
                request.setAttribute("warningMsg", warningMsg);
                displayEdit(request, response, session);
            }
        } catch (JahiaException ex) {
            logger.warn("Error while processing site edition", ex);
            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                    jParams.getLocale());
            request.setAttribute("warningMsg", warningMsg);
            displayEdit(request, response, session);
        }
    }

    /**
     * Display Delete Site confirmation.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayDelete(HttpServletRequest request,
                               HttpServletResponse response,
                               HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display delete site started ");

        // change session time out to 1 hour ( the extraction can be very long !)
        int timeOut = session.getMaxInactiveInterval();

        try {
            session.setMaxInactiveInterval(7200);

            // get site...
            String site_id = JahiaTools.getStrParameter(request, "siteid", "").trim();
            Integer siteID = new Integer(site_id);

            JahiaSite site = sMgr.getSite(siteID.intValue());

            // retrieve previous form values...
            String jahiaDisplayMessage = Jahia.COPYRIGHT;
            String siteTitle = (String) request.getAttribute("siteTitle");
            String siteServerName = (String) request.getAttribute("siteServerName");
            String siteKey = (String) request.getAttribute("siteKey");
            String siteDescr = (String) request.getAttribute("siteDescr");

            // set default values...
            if (siteTitle == null) {
                siteTitle = site.getTitle();
            }
            if (siteServerName == null) {
                siteServerName = site.getServerName();
            }
            if (siteKey == null) {
                siteKey = site.getSiteKey();
            }
            if (siteDescr == null) {
                siteDescr = site.getDescr();
            }

            // set request attributes...
            request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
            request.setAttribute("siteTitle", siteTitle);
            request.setAttribute("siteServerName", siteServerName);
            request.setAttribute("siteKey", siteKey);
            request.setAttribute("siteDescr", siteDescr);
            request.setAttribute("siteID", siteID);

            // list of user providers
            JahiaUserManagerService userServ = ServicesRegistry.getInstance().getJahiaUserManagerService();
            List<JahiaUserManagerProvider> usrProviders = new ArrayList<JahiaUserManagerProvider>();
            for (JahiaUserManagerProvider usrProviderBean : userServ.getProviderList()) {
                if (!usrProviderBean.isReadOnly()) {
                    usrProviders.add(usrProviderBean);
                }
            }
            request.setAttribute("usrProviders", usrProviders);

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_delete.jsp");
        } catch (Exception e) {
            logger.error("Error while display site delete UI", e);
            // redirect to list...
            String jahiaDisplayMessage = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg..processingError.label",
                    jParams.getLocale());
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            displayList(request, response, session);
        } finally {
            // restore time out
            session.setMaxInactiveInterval(timeOut);
        }

        // reset display message...
        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
    } // end displayEdit

    /**
     * Process Delete Site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processDelete(HttpServletRequest request,
                               HttpServletResponse response,
                               HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" process delete site started ");

        JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);

        try {
            // get site...
            String site_id = JahiaTools.getStrParameter(request, "siteid", "").trim();
            Integer siteID = new Integer(site_id);
            JahiaSite site = sMgr.getSite(siteID.intValue());

            boolean deleteFiles = request.getParameter("deleteFileRepository") != null;

            delete(site, theUser, deleteFiles);

            changeSiteIfCurrent(session, site);

            // redirect...
            displayList(request, response, session);
        } catch (JahiaException ex) {
            logger.error("Error while processing site deletion", ex);
            String warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                    jParams.getLocale());
            request.setAttribute("warningMsg", warningMsg);
            displayEdit(request, response, session);
        }
    }

    /**
     * Display Delete Site confirmation.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayMultipleDelete(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display delete site started ");

        List<JahiaSite> sites = new ArrayList<JahiaSite>();
        String[] sitekeys = request.getParameterValues("sitebox");
        if (sitekeys != null) {
            for (int i = 0; i < sitekeys.length; i++) {
                final String sitekey = sitekeys[i];
                try {
                    final JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                    sites.add(site);
                } catch (JahiaException e) {
                    logger.error("Error getting site: " + sitekey);
                }
            }
        }
        request.setAttribute("sites", sites);

        // list of user providers
        JahiaUserManagerService userServ = ServicesRegistry.getInstance().getJahiaUserManagerService();
        List<JahiaUserManagerProvider> usrProviders = new ArrayList<JahiaUserManagerProvider>();
        for (JahiaUserManagerProvider usrProviderBean : userServ.getProviderList()) {
            if (!usrProviderBean.isReadOnly()) {
                usrProviders.add(usrProviderBean);
            }
        }
        request.setAttribute("usrProviders", usrProviders);

        // redirect...
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_multiple_delete.jsp");

        // reset display message...
        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
    } // end displayEdit

    /**
     * Process Delete Site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void processMultipleDelete(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" process delete site started ");

        JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);

        try {
            // get site...
            List<JahiaSite> sites = new ArrayList<JahiaSite>();
            String[] sitekeys = request.getParameterValues("sitebox");
            for (int i = 0; i < sitekeys.length; i++) {
                String sitekey = sitekeys[i];
                try {
                    JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                    sites.add(site);
                } catch (JahiaException e) {

                }
            }

            boolean deleteFiles = request.getParameter("deleteFileRepository") != null;

            for (JahiaSite site : sites) {
                delete(site, theUser, deleteFiles);
                changeSiteIfCurrent(session, site);
            }

            // redirect...
            displayList(request, response, session);
        } catch (JahiaException ex) {
            logger.error("Error while deleting multiple sites", ex);
            String warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                    jParams.getLocale());
            request.setAttribute("warningMsg", warningMsg);
            displayEdit(request, response, session);
        }
    }

    private void changeSiteIfCurrent(HttpSession session, JahiaSite site) {
        JahiaSite sessSite = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (sessSite != null && sessSite.getSiteKey().equals(site.getSiteKey())) {
            session.setAttribute(ProcessingContext.SESSION_SITE, null);
        }
        if (jParams.getSite() != null && jParams.getSite().getSiteKey().equals(site.getSiteKey())) {
            JahiaSite siteToUseNow = sMgr.getDefaultSite() != null ? sMgr
                    .getDefaultSite()
                    : new JahiaSite(-1, "", "", "", "",
                    new Properties());
            jParams.setSite(siteToUseNow);
            jParams.setContentPage(siteToUseNow.getHomeContentPage());
        }
    }

    private void delete(JahiaSite site, JahiaUser theUser, boolean deleteFiles) throws JahiaException, IOException {
        // now let's check if this site is the default site, in which case
        // we need to change the default site to another one.
        JahiaSite defSite = getDefaultSite();

        // first let's build a list of the all the sites except the
        // current one.
        List<JahiaSite> otherSites = new ArrayList<JahiaSite>();
        for (Iterator<JahiaSite> siteIt = ServicesRegistry.getInstance().getJahiaSitesService().getSites(); siteIt.hasNext();) {
            JahiaSite curSite = siteIt.next();
            if (!curSite.getSiteKey().equals(site.getSiteKey())) {
                otherSites.add(curSite);
            }
        }
        if (defSite == null) {
            // no default site, let's assign once that isn't the current
            // one being deleted.
            if (otherSites.size() > 0) {
                changeDefaultSite((JahiaSite) otherSites.get(0));
            }
        } else if (defSite.getSiteKey().equals(site.getSiteKey())) {
            // the default site IS the site being deleted, let's set
            // another site as a default site.
            if (otherSites.size() > 0) {
                changeDefaultSite((JahiaSite) otherSites.get(0));
            } else {
                changeDefaultSite(null);
            }
        }

        // purge options
//        if (deleteTemplates) {
//            //logger.debug(" process delete templates ");
//            JahiaSiteTools.deleteTemplates(theUser, site);
//        }

        // delete big text
        JahiaTextFileService textFileServ = ServicesRegistry.getInstance()
                .getJahiaTextFileService();
        textFileServ.deleteSiteBigText(site.getID(), theUser);

        // switch staging and versioning to false.
        sMgr.updateSite(site);

        //remove site definition
        sMgr.removeSite(site);

        if (deleteFiles) {
            List<JCRNodeWrapper> list = JCRStoreService.getInstance().getSiteFolders(site.getSiteKey());
            for (JCRNodeWrapper jcrNodeWrapper : list) {
                try {
                    jcrNodeWrapper.remove();
                    jcrNodeWrapper.saveSession();
                } catch (RepositoryException e) {
					logger.error("Error removing site folders for site '" + site.getTitle() + " (" + site.getSiteKey()
					        + ")'. Cause: " + e.getMessage(), e);
                }
            }
        }
    }


    private void prepareMultipleImport(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session) throws IOException, ServletException {
        FileUpload fileUpload = ((ParamBean) jParams).getFileUpload();
        if (fileUpload != null) {
            Set<String> filesName = fileUpload.getFileNames();
            Iterator<String> iterator = filesName.iterator();
            if (iterator.hasNext()) {
                String n = iterator.next();
                File f = fileUpload.getFile(n);
                prepareFileImports(f, fileUpload.getFileSystemName(n), request);
            }

        }
        String importPath = jParams.getParameter("importpath");
        if (StringUtils.isNotBlank(importPath)) {
            File f = new File(importPath);

            if (f.exists()) {
                prepareFileImports(f, f.getName(), request);
            }
        }
        if (jParams.getSessionState().getAttribute("importsInfos") != null) {
            if (!((List<?>) jParams.getSessionState().getAttribute("importsInfos")).isEmpty()) {
                request.setAttribute("tmplSets", ServicesRegistry.getInstance()
                        .getJahiaTemplateManagerService()
                        .getAvailableTemplatePackages());
                JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "import_choose.jsp");
                return;
            } else {
                request.setAttribute("warningMsg", JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.site.ManageSites.multipleimport.noValidSite",
                        jParams.getLocale()));
            }
        } else {
            final String msg = fileUpload.getFileNames().size() == 0
                    && StringUtils.isBlank(importPath) ? "org.jahia.admin.site.ManageSites.multipleimport.noFile"
                    : "org.jahia.admin.site.ManageSites.multipleimport.noValidSite";
            request.setAttribute("warningMsg", JahiaResourceBundle
                    .getJahiaInternalResource(msg, jParams.getLocale()));
        }

        displayList(request, response, session);
    }

    private void prepareFileImports(File f, String name, HttpServletRequest request) {
        if (f != null && f.exists()) {
            try {
                Properties exportProps = new Properties();
                ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
                ZipEntry z;
                Map<File, String> imports = new HashMap<File, String>();
                List<File> importList = new ArrayList<File>();
                while ((z = zis.getNextEntry()) != null) {
                    File i = File.createTempFile("import", ".zip");
                    OutputStream os = new FileOutputStream(i);
                    byte[] buf = new byte[4096];
                    int r;
                    while ((r = zis.read(buf)) > 0) {
                        os.write(buf, 0, r);
                    }
                    os.close();

                    String n = z.getName();
                    if (n.equals("export.properties")) {
                        exportProps.load(new FileInputStream(i));
                        jParams.setAttribute("exportProps", exportProps);
                        i.delete();

                    } else if (n.equals("classes.jar")) {
                        i.delete();
                    } else if (n.equals("site.properties") || ((n.startsWith("export_") && n.endsWith(".xml")))) {
                        // this is a single site import, stop everything and import
                        i.delete();
                        for (File file : imports.keySet()) {
                            file.delete();
                        }
                        imports.clear();
                        importList.clear();
                        File tempFile = File.createTempFile("import", ".zip");
                        FileUtils.copyFile(f, tempFile);
                        imports.put(tempFile, name);
                        importList.add(tempFile);
                        break;
                    } else {
                        imports.put(i, n);
                        importList.add(i);
                    }
                }

                List<Map<Object, Object>> importsInfos = new ArrayList<Map<Object, Object>>();
                Map<String, File> importsInfosSorted = new TreeMap<String, File>();
                File users = null;
                File serverPermissions = null;
                for (Iterator<File> iterator = importList.iterator(); iterator.hasNext();) {
                    File i = iterator.next();
                    String fileName = imports.get(i);
                    Map<Object, Object> value = prepareSiteImport(i, imports.get(i));
                    if (value != null) {
                        importsInfos.add(value);
                        if ("users.xml".equals(fileName)) {
                            users = i;
                        } else if ("serverPermissions.xml".equals(fileName)) {
                            serverPermissions = i;
                        } else {
                            importsInfosSorted.put(fileName, i);
                        }
                    }
                }

                List<File> sorted = new LinkedList<File>(importsInfosSorted.values());
                if (serverPermissions != null) {
                    sorted.add(0, serverPermissions);
                }
                if (users != null) {
                    sorted.add(0, users);
                }
                jParams.getSessionState().setAttribute("importsInfos", importsInfos);
                jParams.getSessionState().setAttribute("importsInfosSorted", sorted);
            } catch (IOException e) {
                logger.error("Cannot read import file :" + e.getMessage());
            }
        }
//        SharedTemplatePackagesRegistry tmplSetReg = SharedTemplatePackagesRegistry.getInstance();
//        Iterator en = tmplSetReg.getAllTemplatePackages();
//        List list = new ArrayList();
//        while (en.hasNext()) {
//            list.add(en.next());
//        }
        // TODO properly pol�late templates list
        request.setAttribute("tmplSets", Collections.emptyList());
    }

    private Map<Object, Object> prepareSiteImport(File i, String filename) throws IOException {
        Map<Object, Object> importInfos = new HashMap<Object, Object>();
        importInfos.put("importFile", i);
        importInfos.put("importFileName", filename);
        importInfos.put("selected", Boolean.TRUE);
        if (filename.endsWith(".xml")) {
            importInfos.put("type", "xml");
        } else {
            ZipEntry z;
            ZipInputStream zis2 = new ZipInputStream(new FileInputStream(i));
            boolean isSite = false;
            boolean isLegacySite = false;
            while ((z = zis2.getNextEntry()) != null) {
                if ("site.properties".equals(z.getName())) {
                    Properties p = new Properties();
                    p.load(zis2);
                    zis2.closeEntry();
                    importInfos.putAll(p);
                    importInfos.put("templates", importInfos
                            .containsKey("templatePackageName") ? importInfos
                            .get("templatePackageName") : "");
                    importInfos.put("oldsitekey", importInfos.get("sitekey"));
                    isSite = true;
                } else if (z.getName().startsWith("export_")) {
                    isLegacySite = true;
                }
            }
            importInfos.put("isSite", Boolean.valueOf(isSite));
            // todo import ga parameters
            if (isSite || isLegacySite) {
                importInfos.put("type", "site");
                if (!importInfos.containsKey("sitekey")) {
                    importInfos.put("sitekey", "");
                    importInfos.put("siteservername", "");
                    importInfos.put("sitetitle", "");
                    importInfos.put("description", "");
                    importInfos.put("mixLanguage", "false");
                    importInfos.put("templates", "");
                    importInfos.put("siteKeyExists", Boolean.TRUE);
                    importInfos.put("siteServerNameExists", Boolean.TRUE);
                } else {
                    try {
                        importInfos.put("siteKeyExists", Boolean.valueOf(ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) importInfos.get("sitekey")) != null || "".equals(importInfos.get("sitekey"))));
                        importInfos.put("siteServerNameExists", Boolean.valueOf(ServicesRegistry.getInstance().getJahiaSitesService().getSite((String) importInfos.get("siteservername")) != null || "".equals(importInfos.get("siteservername"))));
                    } catch (JahiaException e) {
                        logger.error("Error while preparing site import", e);
                    }
                }
            } else {
                importInfos.put("type", "files");
            }

        }
        return importInfos;
    }

    private void processFileImport(HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session) throws IOException, ServletException {
        List<Map<Object, Object>> importsInfos = (List<Map<Object, Object>>) session.getAttribute("importsInfos");
        Map<Object, Object> siteKeyMapping = new HashMap<Object, Object>();
        boolean stillBad = false;
        for (Map<Object, Object> infos : importsInfos) {
            File file = (File) infos.get("importFile");
            infos.put("sitekey", StringUtils.left(request.getParameter(file.getName() + "siteKey") == null ? null : request.getParameter(file.getName() + "siteKey").trim(), 50));
            infos.put("oldsitekey", request.getParameter(file.getName() + "oldSiteKey") == null ? null : request.getParameter(file.getName() + "oldSiteKey").trim());
            if (infos.get("sitekey") != null && !infos.get("sitekey").equals(infos.get("oldsitekey"))) {
                siteKeyMapping.put(infos.get("oldsitekey"), infos.get("sitekey"));
            }
            infos.put("siteservername", StringUtils.left(request.getParameter(file.getName() + "siteServerName") == null ? null : request.getParameter(file.getName() + "siteServerName").trim(), 200));
            infos.put("sitetitle", StringUtils.left(request.getParameter(file.getName() + "siteTitle") == null ? null : request.getParameter(file.getName() + "siteTitle").trim(), 100));
            infos.put("selected", request.getParameter(file.getName() + "selected"));
            infos.put("templates", request.getParameter(file.getName() + "templates"));

            if (request.getParameter(file.getName() + "selected") != null) {
                try {
                    if (infos.get("importFileName").equals("serverPermissions.xml") || infos.get("importFileName").equals("users.xml")) {

                    } else {
                        infos.put("siteKeyExists", Boolean.valueOf(ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) infos.get("sitekey")) != null || "".equals(infos.get("sitekey"))));
                        infos.put("siteServerNameExists", Boolean.valueOf(ServicesRegistry.getInstance().getJahiaSitesService().getSite((String) infos.get("siteservername")) != null || "".equals(infos.get("siteservername"))));

                        if ("".equals(infos.get("sitekey")) || "".equals(infos.get("siteservername")) || "".equals(infos.get("sitetitle"))) {
                            // todo display an error message
                            stillBad = true;
                        }

                        if (Boolean.TRUE.equals(infos.get("siteKeyExists")) ||
                                Boolean.TRUE.equals(infos.get("siteServerNameExists"))) {
                            stillBad = true;
                        }
                    }
                } catch (JahiaException e) {
                    logger.error("Error while processing file import", e);
                }
            } else {
                infos.put("siteKeyExists", Boolean.FALSE);
                infos.put("siteServerNameExists", Boolean.FALSE);

            }
        }
        if (stillBad) {
            request.setAttribute("tmplSets", ServicesRegistry.getInstance()
                    .getJahiaTemplateManagerService()
                    .getAvailableTemplatePackages());

            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "import_choose.jsp");
        } else {
            boolean license = LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.server.admin.sites.ManageSites", 0);
            boolean noMoreSite = false;

            boolean authorizedForServerPermissions = LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.server.admin.permissions.ManageServerPermissions", 0);

            boolean doImportServerPermissions = false;
            if (authorizedForServerPermissions) {
                for (Map<Object, Object> infos : importsInfos) {
                    File file = (File) infos.get("importFile");
                    if (request.getParameter(file.getName() + "selected") != null
                            && infos.get("importFileName").equals(
                            "serverPermissions.xml")) {
                        doImportServerPermissions = true;
                        break;
                    }
                }
            }
            request.setAttribute("doImportServerPermissions", Boolean.valueOf(doImportServerPermissions));

            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (request.getParameter(file.getName() + "selected") != null
                        && infos.get("importFileName").equals("users.xml")) {
                    ImportExportBaseService.getInstance().importUsers(file);
                    break;
                }
            }

            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (request.getParameter(file.getName() + "selected") != null) {
                    if (infos.get("type").equals("files")) {
                        try {
                            ImportExportBaseService.getInstance().importSiteZip(file, new ArrayList<ImportAction>(), null, jParams.getSite());
                        } catch (RepositoryException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                    } else if (infos.get("type").equals("xml") &&
                            (infos.get("importFileName").equals("serverPermissions.xml") || infos.get("importFileName").equals("users.xml"))) {

                    } else if (infos.get("type").equals("site")) {
                        // site import
                        String tpl = (String) infos.get("templates");
                        if ("".equals(tpl)) tpl = null;
                        try {
                            if (!noMoreSite) {
                                ServicesRegistry.getInstance().getJahiaSitesService().addSite(jParams.getUser(), (String) infos.get("sitetitle"),
                                        (String) infos.get("siteservername"), (String) infos.get("sitekey"), "", null, jParams.getLocale(), tpl, "fileImport", file, (String) infos.get("importFileName"), true, false, jParams);

//                                createSite(jParams.getUser(), (String) infos.get("sitetitle"),
//                                        (String) infos.get("siteservername"), (String) infos.get("sitekey"), "", false, jParams.getLocale(), tpl, "fileImport", file, (String) infos.get("importFileName"),true);
                                noMoreSite = !license;
                            }
                        } catch (Exception e) {
                            logger.error("Cannot create site " + infos.get("sitetitle"), e);
                        }
                    }
                }
            }

            // import serverPermissions.xml
            if (doImportServerPermissions) {
                for (Map<Object, Object> infos : importsInfos) {
                    File file = (File) infos.get("importFile");
                    if (request.getParameter(file.getName() + "selected") != null) {
                        if (infos.get("importFileName").equals(
                                "serverPermissions.xml")) {
                            // pass the old-new site key information for server permissions
                            jParams.setAttribute("sitePermissions_siteKeyMapping", siteKeyMapping);
                            ImportExportBaseService.getInstance()
                                    .importServerPermissions(jParams,
                                            new FileInputStream(file));
                        }
                    }
                }
            }

            redirectAfterAdd(request, response, session);
        }
    }

    private void redirectAfterAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException, ServletException {
//        JahiaSite site = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");
//        ContentPage page = site.getHomeContentPage();
//
//        try {
//            JCRNodeWrapper source = page.getJCRNode(Jahia.getThreadParamBean());
//            source.copyFile("/sites/"+site.getSiteKey());
//        } catch (JahiaException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (RepositoryException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
//
        if (session.getAttribute(CLASS_NAME + "redirectToJahia") != null) {
            session.removeAttribute(CLASS_NAME + "redirectToJahia");
            try {
                setAllowedDayRequestAttr(request);
                List<?> l = ServicesRegistry.getInstance().getSchedulerService().getAllActiveJobsDetails();
                if (!l.isEmpty()) {
                    JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "import_wait.jsp");
                } else {
                    JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_created.jsp");
                    //response.sendRedirect(request.getContextPath());
                }
            } catch (JahiaException e) {
                logger.error("Cannot get jobs", e);
            }
        } else {
            displayList(request, response, session);
        }
    }
// -------------------------- OTHER METHODS --------------------------

    /**
     * set allowed day attribute of the request
     *
     * @param request
     */
    private void setAllowedDayRequestAttr(HttpServletRequest request) {
        try {
            int maxDays = LicenseManager.getInstance().getJahiaMaxUsageDays();
            if (maxDays > 0) {
                Integer limit = Integer.valueOf(maxDays);
                request.setAttribute("allowedDays", limit);
                request.setAttribute("allowedDaysMsg", new EngineMessage("org.jahia.bin.JahiaConfigurationWizard.congratulations.daysLeftInLicense.label", limit));
            }
        } catch (Exception e) {
            logger.error("Enable to compute allowed days.");
        }
    }
}
