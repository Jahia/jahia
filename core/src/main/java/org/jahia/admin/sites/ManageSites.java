/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.importexport.NoCloseZipInputStream;
import org.jahia.services.importexport.validation.ValidationResults;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.search.spell.CompositeSpellChecker;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSiteTools;
import org.jahia.services.sites.JahiaSitesBaseService;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.*;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Url;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.security.Principal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

	// authorized chars
    private static final String AUTHORIZED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789.-";

    private static final HashSet<String> NON_SITE_IMPORTS = new HashSet<String>(Arrays.asList("serverPermissions.xml", "users.xml", "users.zip", JahiaSitesBaseService.SYSTEM_SITE_KEY + ".zip", "references.zip", "roles.zip"));

    private static final Map<String, Integer> RANK;
    static {
        RANK = new HashMap<String, Integer>(3);
        RANK.put("users.xml", 10);
        RANK.put("serverPermissions.xml", 20);
        RANK.put("shared.zip", 30);
        RANK.put(JahiaSitesBaseService.SYSTEM_SITE_KEY+".zip", 40);
    }
    private static final Comparator<Map<Object, Object>> IMPORTS_COMPARATOR = new Comparator<Map<Object,Object>>() {
        public int compare(Map<Object, Object> o1, Map<Object, Object> o2) {
            Integer rank1 = RANK.get((String) o1.get("importFileName"));
            Integer rank2 = RANK.get((String) o2.get("importFileName"));
            rank1 = rank1 != null ? rank1 : 100;
            rank2 = rank2 != null ? rank2 : 100;
            return rank1.compareTo(rank2);
        }
    };

// ------------------------------ FIELDS ------------------------------

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManageSites.class);

    private static final String CLASS_NAME = JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private static final Pattern LANGUAGE_RANK_PATTERN = Pattern.compile("(?:language.)(\\w+)(?:.rank)");

    private static JahiaSitesService sMgr;

    private ProcessingContext jParams;

// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request, HttpServletResponse response) throws Exception {

        ServicesRegistry sReg = ServicesRegistry.getInstance();
        if (sReg != null) {
            sMgr = sReg.getJahiaSitesService();
        }

        this.jParams = (ProcessingContext) request.getAttribute("org.jahia.params.ParamBean");

        userRequestDispatcher(request, response, request.getSession());
    } // end constructor

    /**
     * This method serves as a dispatcher for user requests.
     *
     * @param req  Servlet request.
     * @param res  Servlet response.
     * @param sess Servlet session for the current user.
     */
    private void userRequestDispatcher(HttpServletRequest req, HttpServletResponse res, HttpSession sess)
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
    private void displayList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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
                if (!curSite.getSiteKey().equals(JahiaSitesBaseService.SYSTEM_SITE_KEY)) {
                    sortedSites.add(curSite);
                }
            }
            Locale defaultLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
            if (defaultLocale != null) {
                Collections.sort(sortedSites, JahiaSite.getTitleComparator(defaultLocale));
            } else {
                Collections.sort(sortedSites, JahiaSite.getTitleComparator());
            }
            request.setAttribute("sitesList", sortedSites.iterator());
            request.setAttribute("sitesListSize", Integer.valueOf(sortedSites.size()));
            request.setAttribute("systemSite", sMgr.getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY));
            if (sortedSites.size() == 0) {
                JahiaSite newJahiaSite = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");

                if (newJahiaSite == null) {
                    logger.debug("site not found in session, assuming new site ");

                    // it's the first time this screen is displayed, so create an empty one

                    newJahiaSite = new JahiaSite(-1, "My Site",        // site title
                            "localhost",        // site server name
                            "mySite",     // site key
                            // is active
                            // default page (homepage id)
                            "",        // description
                            null, null);
                }

                session.setAttribute(CLASS_NAME + "newJahiaSite", newJahiaSite);
                session.setAttribute(CLASS_NAME + "noSites", Boolean.TRUE);
            } else {
                session.removeAttribute(CLASS_NAME + "noSites");
            }
        } catch (JahiaException ex) {
            logger.error("Error while retrieving site list", ex);
            jahiaDisplayMessage =
                    getMessage("message.generalError");
        }
        // set request attributes...
        request.setAttribute("jahiaDisplayMessage", jahiaDisplayMessage);
        request.setAttribute("warningMsg", warningMsg);
        try {
            request.setAttribute("hasTemplateSets",getTemplatesSets().size() > 0);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
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
    private void displayAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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

            newJahiaSite = new JahiaSite(-1, "My Site",        // site title
                    "localhost",        // site server name
                    "mySite",     // site key
                    // is active
                    // default page (homepage id)
                    "",        // description
                    null, null);
        }


        Boolean defaultSite = (Boolean) session.getAttribute(CLASS_NAME + "defaultSite");
        if (defaultSite == null) {
            try {
                if (ServicesRegistry.getInstance().getJahiaSitesService().getNbSites() > 1) {
                    defaultSite = Boolean.FALSE;
                } else {
                    defaultSite = Boolean.TRUE;
                }
            } catch (JahiaException e) {
                logger.error(e.getMessage(), e);
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
                grantedSites =
                        ServicesRegistry.getInstance().getJahiaGroupManagerService().getAdminGrantedSites(theUser);
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

        try {
            // logger.debug(" license check ");

            // get the number of sites in db
            int nbSites = ServicesRegistry.getInstance().getJahiaSitesService().getNbSites();

            request.setAttribute("nbSites", new Integer(nbSites));

            // set license info
            request.setAttribute("siteLimit", Integer.valueOf(-1));

            //logger.debug(" let go in ");

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_add.jsp");
        } catch (JahiaException je) {
            logger.error("Error while retrieving number of site in database", je);
            // set request attributes...
            jahiaDisplayMessage =
                    getMessage("message.generalError");
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
    private void processAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        logger.debug("started");

        boolean processError = true;

        // get form values...
        String siteTitle = StringUtils.left(StringUtils.defaultString(request.getParameter("siteTitle")).trim(), 100);
        String siteServerName = StringUtils.left(StringUtils.defaultString(request.getParameter("siteServerName")).trim(), 200);
        String siteKey = StringUtils.left(StringUtils.defaultString(request.getParameter("siteKey")).trim(), 50);
        String siteDescr = StringUtils.left(StringUtils.defaultString(request.getParameter("siteDescr")).trim(), 250);
        String siteAdmin = StringUtils.defaultString(request.getParameter("siteAdmin")).trim();
        Boolean defaultSite = Boolean.valueOf(request.getParameter("defaultSite") != null);

        request.getSession().setAttribute("siteAdminOption", siteAdmin);
        session.setAttribute("siteAdminOption", siteAdmin);
        String warningMsg = "";
        session.setAttribute(CLASS_NAME + "defaultSite", defaultSite);

        // create jahia site object if checks are in green light...
        try {
            // check validity...
            if (siteTitle != null && (siteTitle.length() > 0) && siteServerName != null &&
                    (siteServerName.length() > 0) && siteKey != null && (siteKey.length() > 0)) {
                if (!isSiteKeyValid(siteKey)) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.onlyLettersDigitsUnderscore.label");
                } else if (siteKey.equals("site")) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label");
                } else if (!isServerNameValid(siteServerName)) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.invalidServerName.label");
                } else if (siteServerName.equals("default")) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.chooseAnotherServerName.label");
                } else if (!Url.isLocalhost(siteServerName) && sMgr.getSite(siteServerName) != null) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.chooseAnotherServerName.label");
                } else if (sMgr.getSiteByKey(siteKey) != null) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.chooseAnotherSiteKey.label");
                } else {
                    processError = false;
                }
            } else {
                warningMsg =
                        getMessage("org.jahia.admin.warningMsg.completeRequestInfo.label");
            }

            if (!processError) {
                // save new jahia site...
                JahiaSite site = new JahiaSite(-1, siteTitle, siteServerName, siteKey,
                        // is active
                        // default page (homepage id)... subject to update in terminateAdd().
                        siteDescr, null, null);
//                site.setTemplatesAutoDeployMode(true);
//                site.setWebAppsAutoDeployMode(true);


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
                JahiaSite site = new JahiaSite(-1, siteTitle, siteServerName, siteKey, siteDescr, null, null);


                session.setAttribute(CLASS_NAME + "newJahiaSite", site);
                request.setAttribute("newJahiaSite", site);
                request.setAttribute("warningMsg", warningMsg);
                displayAdd(request, response, session);
            }
        } catch (JahiaException ex) {
            warningMsg =
                    getMessage("label.error.processingRequestError");
            request.setAttribute("warningMsg", warningMsg);
            displayAdd(request, response, session);
        } finally {
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

    public static boolean isServerNameValid(String serverName) {
        return StringUtils.isNotEmpty(serverName) && !serverName.contains(" ") && !serverName.contains(":");
    }

    /**
     * Display page to create an administrator for the new site.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayCreateAdmin(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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
            JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
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
    private void displaySelectExistantAdmin(HttpServletRequest request, HttpServletResponse response,
                                            HttpSession session) throws IOException, ServletException {
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
    private void processCreateAdmin(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" process create administrator started ");
        boolean processError = true;

        // get form values...
        String adminUsername = StringUtils.defaultString(request.getParameter("adminUsername")).trim();
        String adminPassword = StringUtils.defaultString(request.getParameter("adminPassword")).trim();
        String adminConfirm = StringUtils.defaultString(request.getParameter("adminConfirm")).trim();
        String adminFirstName = StringUtils.defaultString(request.getParameter("adminFirstName")).trim();
        String adminLastName = StringUtils.defaultString(request.getParameter("adminLastName")).trim();
        String adminOrganization = StringUtils.defaultString(request.getParameter("adminOrganization")).trim();
        String adminEmail = StringUtils.defaultString(request.getParameter("adminEmail")).trim();
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
        JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();

         if (adminUsername.length() == 0) {
            warningMsg =
                    getMessage("org.jahia.admin.userMessage.specifyUserName.label");
        } else if (!adminPassword.equals(adminConfirm)) {
            warningMsg =
                    getMessage("org.jahia.admin.JahiaDisplayMessage.confirmPasswdBeSame.label");
        } else if (adminPassword.length() == 0) {
            warningMsg =
                    getMessage("org.jahia.admin.userMessage.specifyPassword.label");
        } else if (!userManager.isUsernameSyntaxCorrect(adminUsername)) {
            warningMsg = StringUtils.capitalize(getMessage(
                    "org.jahia.admin.users.ManageUsers.onlyCharacters.label"));
        } else if ( userManager.lookupUser(adminUsername) != null) {
            warningMsg = getMessage("label.user") + " [" + adminUsername + "] " +
                    getMessage("org.jahia.admin.userMessage.alreadyExist.label") +
                    " ";
        } else {
            JahiaPasswordPolicyService pwdPolicyService =
                    ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
            JahiaSite newSite = (JahiaSite) session.getAttribute(CLASS_NAME + "newJahiaSite");
            if (newSite != null) {
                PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(adminUsername, adminPassword);
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

            JahiaTemplateManagerService templateManager =
                    ServicesRegistry.getInstance().getJahiaTemplateManagerService();
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
    private void displayTemplateSetChoice(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        try {
            logger.debug("Display template set choice started ");

            // retrieve previous form values...
            String jahiaDisplayMessage = (String) request.getAttribute(CLASS_NAME + "jahiaDisplayMessage");
            // set default values...
            if (jahiaDisplayMessage == null) {
                jahiaDisplayMessage = Jahia.COPYRIGHT;
            }

            String selectedTmplSet = (String) request.getAttribute("selectedTmplSet");

            TreeMap<String, JCRNodeWrapper> orderedTemplateSets = getTemplatesSets();

            // try to select the default set if not selected
            if (selectedTmplSet == null) {
                selectedTmplSet = SettingsBean.getInstance().getPropertiesFile().getProperty("default_templates_set", orderedTemplateSets.firstKey());
            }

            JCRNodeWrapper selectedPackage = selectedTmplSet != null && orderedTemplateSets.containsKey(selectedTmplSet) ? orderedTemplateSets.get(selectedTmplSet) : orderedTemplateSets.get(orderedTemplateSets.firstKey());

            request.setAttribute("selectedTmplSet", selectedTmplSet);
            request.setAttribute("tmplSets", orderedTemplateSets.values());
            request.setAttribute("modules", getModulesOfType(JahiaTemplateManagerService.MODULE_TYPE_MODULE).values());
            request.setAttribute("jahiApps", getModulesOfType(JahiaTemplateManagerService.MODULE_TYPE_JAHIAPP).values());
            request.setAttribute("selectedModules", jParams.getParameterValues("selectedModules"));
            request.setAttribute("selectedPackage", selectedPackage);
            Locale currentLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
            if (currentLocale == null) {
                currentLocale = request.getLocale();
            }
            Locale selectedLocale = (Locale) session.getAttribute(CLASS_NAME + "selectedLocale");
            if (selectedLocale == null) {
                selectedLocale =
                        LanguageCodeConverters.languageCodeToLocale(Jahia.getSettings().getDefaultLanguageCode());
            }
            session.setAttribute(CLASS_NAME + "selectedLocale", selectedLocale);
            request.setAttribute("selectedLocale", selectedLocale);
            request.setAttribute("currentLocale", currentLocale);

            logger.debug("Nb template set found " + orderedTemplateSets.size());

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_choose_template_set.jsp");

            // set default values...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", Jahia.COPYRIGHT);
        } catch (RepositoryException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Terminate the creation of a new site. Create the site and dependancies.
     *
     * @param request Servlet request.
     * @param session HttpSession object.
     */
    public boolean terminateAdd(HttpServletRequest request, HttpSession session) throws IOException, ServletException {

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
                    selectedLocale, (String) request.getAttribute("selectedTmplSet"),
                    jParams.getParameterValues("selectedModules"),
                    (String) request.getAttribute("firstImport"), (File) request.getAttribute("fileImport"),
                    (String) request.getAttribute("fileImportName"), (Boolean) request.getAttribute("asAJob"),
                    (Boolean) request.getAttribute("doImportServerPermissions"), (String) request.getAttribute("originatingJahiaRelease"));
            if (site != null) {
                jParams.setSite(site);
                jParams.setSiteID(site.getID());
                jParams.setSiteKey(site.getSiteKey());
                jParams.setCurrentLocale(selectedLocale);

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

                JahiaSite jahiaSite = sMgr.getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY);
                jahiaSite.getLanguages().addAll(site.getLanguages());
                sMgr.updateSite(jahiaSite);
            } else {
                warningMsg =
                        getMessage("label.error.processingRequestError");
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

            warningMsg =
                    getMessage("label.error.processingRequestError");
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
    private void processExistantAdmin(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {

        // get form values...
        String adminSelected = request.getParameter("adminSelected");
        String siteID = request.getParameter("site");

        if (adminSelected == null) {     // it's only the choice of site... display user list.
            request.setAttribute("selectedSite", siteID);
            String jahiaDisplayMessage =
                    getMessage("org.jahia.admin.JahiaDisplayMessage.chooseUserInList.label");
            request.setAttribute(CLASS_NAME + "jahiaDisplayMessage", jahiaDisplayMessage);
            displaySelectExistantAdmin(request, response, session);
        } else {
            // get the user...
            JahiaUserManagerService userManager = ServicesRegistry.getInstance().getJahiaUserManagerService();
            JahiaUser theUser = userManager.lookupUserByKey(adminSelected);

            session.setAttribute(CLASS_NAME + "existantAdminUser", theUser);

            JahiaTemplateManagerService templateManager =
                    ServicesRegistry.getInstance().getJahiaTemplateManagerService();
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
    private void processExistantAdminSelectSite(HttpServletRequest request, HttpServletResponse response,
                                                HttpSession session) throws IOException, ServletException {

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
    private void processTemplateSetChoice(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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

        request.setAttribute("selectedModules", jParams.getParameterValues("selectedModules"));

        String firstImport = jParams.getParameter("firstImport");
        request.setAttribute("firstImport", firstImport);

        if (jParams.getParameter("importpath") != null) {
            File file = new File(jParams.getParameter("importpath"));
            if (file.exists()) {
                request.setAttribute("fileImport", file);
            }
        }

        String selectedLanguage = getStrParameter(jParams, "languageList", "").trim();
        if (!selectedLanguage.equals("")) {
            session.setAttribute(CLASS_NAME + "selectedLocale",
                    LanguageCodeConverters.languageCodeToLocale(selectedLanguage));
        }
        request.setAttribute("selectedlanguage", selectedLanguage);

        if (operation == null || !operation.trim().equals("save") || selectTmplSet.equals("0")) {
            displayTemplateSetChoice(request, response, session);
        } else {
            displayNewSiteValues(request, response, session);
        }
    }

    private void createSite(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
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
        String selectedLanguage = getStrParameter(jParams, "languageList", "").trim();
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
    private void displayEdit(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display edit site started ");

        try {
            // get site...
            String site_id = StringUtils.defaultString(request.getParameter("siteid")).trim();
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
            request.setAttribute("site", site);

            // redirect...
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "site_edit.jsp");
        } catch (Exception e) {
            logger.error("Error while displaying site edition UI", e);
            // redirect to list...
            String jahiaDisplayMessage =
                    getMessage("org.jahia.admin.JahiaDisplayMessage.processingError.label");
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
    private void displayNewSiteValues(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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
            final JCRNodeWrapper tmplPack = JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession()
                    .getNode("/templateSets/" + selectedTmplSet);


            Boolean defaultSite = Boolean.FALSE;
            if (request.getAttribute("defaultSite") == null) {
                defaultSite = Boolean.valueOf((Boolean) session.getAttribute(CLASS_NAME + "defaultSite"));
            } else {
                defaultSite = (Boolean) request.getAttribute("defaultSite");
            }

            Locale selectedLocale = (Locale) session.getAttribute(CLASS_NAME + "selectedLocale");
            if (selectedLocale == null) {
                selectedLocale = LanguageCodeConverters
                        .languageCodeToLocale(org.jahia.settings.SettingsBean.getInstance().getDefaultLanguageCode());
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
            request.setAttribute("selectedModules", request.getAttribute("selectedModules"));
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
            logger.error("Error while displaying new site values", e);
            // redirect to list...
            String jahiaDisplayMessage =
                    getMessage("org.jahia.admin.JahiaDisplayMessage.processingError.label");
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
    private void processEdit(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        logger.debug(" process edit site started ");

        boolean processError = false;

        // get form values...
        String siteTitle = StringUtils.left(StringUtils.defaultString(request.getParameter("siteTitle")).trim(), 100);
        String siteServerName = StringUtils.left(StringUtils.defaultString(request.getParameter("siteServerName")).trim(), 200);
//		String  siteKey			    = StringUtils.defaultIfEmpty(request.getParameter("siteKey"),"").toLowerCase().trim();
        String siteDescr = StringUtils.left(StringUtils.defaultString(request.getParameter("siteDescr")).trim(), 250);

        String warningMsg = "";
        boolean defaultSite = (request.getParameter("defaultSite") != null);

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
            String site_id = StringUtils.defaultString(request.getParameter("siteid")).trim();
            Integer siteID = new Integer(site_id);
            JahiaSite site = sMgr.getSite(siteID.intValue());

            // check validity...
            if (siteTitle != null && (siteTitle.trim().length() > 0) && siteServerName != null &&
                    (siteServerName.trim().length() > 0)) {
                if (!isServerNameValid(siteServerName)) {
                    warningMsg =
                            getMessage("org.jahia.admin.warningMsg.invalidServerName.label");
                    processError = true;
                } else if (!site.getServerName().equals(siteServerName)) {
                    if (!Url.isLocalhost(siteServerName) && sMgr.getSite(siteServerName) != null) {
                        warningMsg =
                                getMessage("org.jahia.admin.warningMsg.chooseAnotherServerName.label");
                        processError = true;
                    }
                }
            } else {
                warningMsg =
                        getMessage("org.jahia.admin.warningMsg.completeRequestInfo.label");
            }

            if (!processError) {
                // save modified informations...
                site.setTitle(siteTitle);
                site.setServerName(siteServerName);
                site.setDescr(siteDescr);

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
            warningMsg =
                    getMessage("label.error.processingRequestError");
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
    private void displayDelete(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" display delete site started ");

        // change session time out to 1 hour ( the extraction can be very long !)
        int timeOut = session.getMaxInactiveInterval();

        try {
            session.setMaxInactiveInterval(7200);

            // get site...
            String site_id = StringUtils.defaultString(request.getParameter("siteid")).trim();
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
            String jahiaDisplayMessage =
                    getMessage("org.jahia.admin.warningMsg..processingError.label");
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
    private void processDelete(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        //logger.debug(" process delete site started ");

        JahiaUser theUser = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);

        try {
            // get site...
            String site_id = StringUtils.defaultString(request.getParameter("siteid")).trim();
            Integer siteID = new Integer(site_id);
            JahiaSite site = sMgr.getSite(siteID.intValue());

            boolean deleteFiles = request.getParameter("deleteFileRepository") != null;

            delete(site, theUser, deleteFiles);

            changeSiteIfCurrent(session, site);

            // redirect...
            displayList(request, response, session);
        } catch (JahiaException ex) {
            logger.error("Error while processing site deletion", ex);
            String warningMsg =
                    getMessage("label.error.processingRequestError");
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
    private void displayMultipleDelete(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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
    private void processMultipleDelete(HttpServletRequest request, HttpServletResponse response, HttpSession session)
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
            String warningMsg =
                    getMessage("label.error.processingRequestError");
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
            JahiaSite siteToUseNow = sMgr.getDefaultSite() != null ? sMgr.getDefaultSite() :
                    new JahiaSite(-1, "", "", "", "", new Properties(), null);
            jParams.setSite(siteToUseNow);
            jParams.setSiteKey(siteToUseNow.getSiteKey());
            session.setAttribute(ProcessingContext.SESSION_SITE, siteToUseNow);
        }
    }

    private void delete(JahiaSite site, JahiaUser theUser, boolean deleteFiles) throws JahiaException, IOException {
        // now let's check if this site is the default site, in which case
        // we need to change the default site to another one.
        JahiaSite defSite = getDefaultSite();

        // first let's build a list of the all the sites except the
        // current one.
        List<JahiaSite> otherSites = new ArrayList<JahiaSite>();
        for (Iterator<JahiaSite> siteIt = ServicesRegistry.getInstance().getJahiaSitesService().getSites();
             siteIt.hasNext();) {
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

        // switch staging and versioning to false.
        sMgr.updateSite(site);

        //remove site definition
        sMgr.removeSite(site);

    }


    private void prepareMultipleImport(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
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
                try {
                    TreeMap<String, JCRNodeWrapper> orderedTemplateSets = getTemplatesSets();

                    request.setAttribute("tmplSets", new ArrayList<JCRNodeWrapper>(orderedTemplateSets.values()));
                } catch (RepositoryException e) {
                    logger.error("Error when getting templates", e);
                }
                JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "import_choose.jsp");
                return;
            } else {
                request.setAttribute("warningMsg", getMessage(
                        "org.jahia.admin.site.ManageSites.multipleimport.noValidSite"));
            }
        } else {
            final String msg = fileUpload.getFileNames().size() == 0 && StringUtils.isBlank(importPath) ?
                    "org.jahia.admin.site.ManageSites.multipleimport.noFile" :
                    "org.jahia.admin.site.ManageSites.multipleimport.noValidSite";
            request.setAttribute("warningMsg", getMessage(msg));
        }

        displayList(request, response, session);
    }

    private void prepareFileImports(File f, String name, HttpServletRequest request) {
        if (f != null && f.exists()) {
            ZipInputStream zis = null;
            try {
                Properties exportProps = new Properties();
                zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(f)));
                ZipEntry z;
                Map<File, String> imports = new HashMap<File, String>();
                List<File> importList = new ArrayList<File>();
                while ((z = zis.getNextEntry()) != null) {
                    File i = File.createTempFile("import", ".zip");
                    OutputStream os = new BufferedOutputStream(new FileOutputStream(i));
                    try {
                        IOUtils.copy(zis, os);
                    } finally {
                        IOUtils.closeQuietly(os);
                    }

                    String n = z.getName();
                    if (n.equals("export.properties")) {
                        InputStream is = new BufferedInputStream(new FileInputStream(i));
                        try {
                            exportProps.load(is);
                        } finally {
                            IOUtils.closeQuietly(is);
                            FileUtils.deleteQuietly(i);
                        }
                        jParams.setAttribute("exportProps", exportProps);
                    } else if (n.equals("classes.jar")) {
                        FileUtils.deleteQuietly(i);
                    } else if (n.equals("site.properties") || ((n.startsWith("export_") && n.endsWith(".xml")))) {
                        // this is a single site import, stop everything and import
                        FileUtils.deleteQuietly(i);
                        for (File file : imports.keySet()) {
                            FileUtils.deleteQuietly(file);
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
                for (Iterator<File> iterator = importList.iterator(); iterator.hasNext();) {
                    File i = iterator.next();
                    Map<Object, Object> value = prepareSiteImport(i, imports.get(i));
                    if (value != null) {
                        Object legacyImport = value.get("legacyImport");
                        if (legacyImport!=null && (Boolean) legacyImport && jParams instanceof ParamBean) {
                            final String defaultMappingsFolderPath = ((ParamBean)jParams).getContext().getRealPath("/WEB-INF/var/legacyMappings");
                            final File defaultMappingsFolder = defaultMappingsFolderPath != null ? new File(defaultMappingsFolderPath) : null;
                            Collection<File> legacyMappings = null;
                            Collection<File> legacyDefinitions = null;
                            if (defaultMappingsFolder != null && defaultMappingsFolder.exists()) {
                                try {
                                    legacyMappings = FileUtils.listFiles(defaultMappingsFolder, new String[]{"map"}, false);
                                } catch (Exception e) {
                                    logger.debug("Legacy mappings not found", e);
                                }
                                try {
                                    legacyDefinitions = FileUtils.listFiles(defaultMappingsFolder, new String[]{"cnd"}, false);
                                } catch (Exception e) {
                                    logger.debug("Legacy definitions not found", e);
                                }
                            }

                            org.springframework.core.io.Resource[] modulesLegacyMappings = SpringContextSingleton.getInstance().getResources("/modules/**/META-INF/legacyMappings/*.map");
                            if (legacyMappings == null && modulesLegacyMappings.length > 0) {
                                legacyMappings = new ArrayList<File>();
                            }
                            for (int j=0; j<modulesLegacyMappings.length; j++) {
                                legacyMappings.add(modulesLegacyMappings[j].getFile());
                            }

                            org.springframework.core.io.Resource[] modulesLegacyDefinitions = SpringContextSingleton.getInstance().getResources("/modules/**/META-INF/legacyMappings/*.cnd");
                            if (legacyDefinitions == null && modulesLegacyDefinitions.length > 0) {
                                legacyDefinitions = new ArrayList<File>();
                            }
                            for (int j=0; j<modulesLegacyDefinitions.length; j++) {
                                legacyDefinitions.add(modulesLegacyDefinitions[j].getFile());
                            }

                            if (legacyMappings != null && !legacyMappings.isEmpty()) {
                                value.put("legacyMappings", legacyMappings);
                            }
                            if (legacyDefinitions != null && !legacyDefinitions.isEmpty()) {
                                value.put("legacyDefinitions", legacyDefinitions);
                            }
                        }
                        importsInfos.add(value);
                    }
                }

                Collections.sort(importsInfos, IMPORTS_COMPARATOR);

                List<File> sorted = new LinkedList<File>();
                for (Map<Object, Object> info : importsInfos) {
                    sorted.add((File) info.get("importFile"));
                }

                jParams.getSessionState().setAttribute("importsInfos", importsInfos);
                jParams.getSessionState().setAttribute("importsInfosSorted", sorted);
            } catch (IOException e) {
                logger.error("Cannot read import file :" + e.getMessage());
            } finally {
                IOUtils.closeQuietly(zis);
            }
        }
        request.setAttribute("tmplSets", Collections.emptyList());
    }

    private Map<Object, Object> prepareSiteImport(File i, String filename) throws IOException {
        Map<Object, Object> importInfos = new HashMap<Object, Object>();
        importInfos.put("importFile", i);
        importInfos.put("importFileName", filename);
        importInfos.put("selected", Boolean.TRUE);
        boolean doValidate = Boolean.valueOf(jParams.getParameter("validityCheckOnImport"));
        final Properties exportProps = (Properties) jParams.getAttribute("exportProps");
        if (exportProps != null) {
            importInfos.put("originatingJahiaRelease", exportProps.getProperty("JahiaRelease"));
        }
        if (filename.endsWith(".xml")) {
            importInfos.put("type", "xml");
        } else if (filename.endsWith("systemsite.zip")) {
            importInfos.put("type", "files");
        } else {
            List<String> installedModules = readInstalledModules(i);
            org.jahia.utils.zip.ZipEntry z;
            NoCloseZipInputStream zis2 = new NoCloseZipInputStream(
                    new BufferedInputStream(new FileInputStream(i)));

            boolean isSite = false;
            boolean isLegacySite = false;
            try {
                while ((z = zis2.getNextEntry()) != null) {
                    if ("site.properties".equals(z.getName())) {
                        Properties p = new Properties();
                        p.load(zis2);
                        importInfos.putAll(p);

                        importInfos.put("templates", "");
                        if (importInfos.containsKey("templatePackageName")) {
                            JahiaTemplateManagerService templateManager = ServicesRegistry
                                    .getInstance()
                                    .getJahiaTemplateManagerService();
                            JahiaTemplatesPackage pack = templateManager
                                    .getTemplatePackageByFileName((String) importInfos
                                            .get("templatePackageName"));
                            if (pack == null) {
                                pack = templateManager
                                        .getTemplatePackage((String) importInfos
                                                .get("templatePackageName"));
                            }
                            if (pack != null) {
                                importInfos
                                        .put("templates", pack.getFileName());
                            }
                        }
                        importInfos.put("oldsitekey",
                                importInfos.get("sitekey"));
                        isSite = true;
                    } else if (z.getName().startsWith("export_")) {
                        isLegacySite = true;
                    } else if (doValidate && z.getName().contains("repository.xml")) {
                        try {
                            long timer = System.currentTimeMillis();
                            ValidationResults validationResults = ImportExportBaseService
                                    .getInstance()
                                    .validateImportFile(
                                            JCRSessionFactory.getInstance().getCurrentUserSession(),
                                            zis2, "application/xml", installedModules);
                            logger.info(
                                    "Import {}/{} validated in {} ms: {}",
                                    new String[] { filename, z.getName(),
                                            String.valueOf((System.currentTimeMillis() - timer)),
                                            validationResults.toString() });
                            if (!validationResults.isSuccessful()) {
                                if (importInfos.containsKey("validationResult")) {
                                    // merge results
                                    importInfos.put("validationResult",
                                            ((ValidationResults) importInfos
                                                    .get("validationResult"))
                                                    .merge(validationResults));
                                } else {
                                    importInfos.put("validationResult", validationResults);
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Error when validating import file", e);
                        }
                    }
                    zis2.closeEntry();
                }
            } finally {
                zis2.reallyClose();
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
                    importInfos.put("siteKeyInvalid", Boolean.TRUE);
                    importInfos.put("siteKeyExists", Boolean.TRUE);
                    importInfos.put("siteServerNameInvalid", Boolean.TRUE);
                    importInfos.put("siteServerNameExists", Boolean.TRUE);
                } else {
                    try {
                        String siteKey = (String) importInfos.get("sitekey");
                        boolean valid = isSiteKeyValid(siteKey);
                        importInfos.put("siteKeyInvalid", !valid);
                        importInfos.put("siteKeyExists",
                                valid
                                        && ServicesRegistry.getInstance()
                                                .getJahiaSitesService()
                                                .getSiteByKey(siteKey) != null);
                        String serverName = (String) importInfos
                                .get("siteservername");
                        valid = isServerNameValid(serverName);
                        importInfos.put("siteServerNameInvalid", !valid);
                        importInfos.put(
                                "siteServerNameExists",
                                valid
                                        && !Url
                                                .isLocalhost(serverName)
                                        && ServicesRegistry.getInstance()
                                                .getJahiaSitesService()
                                                .getSite(serverName) != null);
                    } catch (JahiaException e) {
                        logger.error("Error while preparing site import", e);
                    }
                }
                importInfos.put("legacyImport",isLegacySite);
            } else {
                importInfos.put("type", "files");
            }

        }
        return importInfos;
    }

    private List<String> readInstalledModules(File i) throws IOException {
        List<String> modules = new LinkedList<String>();
        org.jahia.utils.zip.ZipEntry z;
        NoCloseZipInputStream zis2 = new NoCloseZipInputStream(new BufferedInputStream(
                new FileInputStream(i)));

        try {
            while ((z = zis2.getNextEntry()) != null) {
                try {
                    if ("site.properties".equals(z.getName())) {
                        Properties p = new Properties();
                        p.load(zis2);
                        Map<Integer, String> im = new TreeMap<Integer, String>();
                        for (Object k : p.keySet()) {
                            String key = String.valueOf(k);
                            if (key.startsWith("installedModules.")) {
                                try {
                                    im.put(Integer.valueOf(StringUtils.substringAfter(key, ".")),
                                            p.getProperty(key));
                                } catch (NumberFormatException e) {
                                    logger.warn("Unable to parse installed module from key {}", key);
                                }
                            }
                        }
                        modules.addAll(im.values());
                    }
                } finally {
                    zis2.closeEntry();
                }
            }
        } finally {
            zis2.reallyClose();
        }
        return modules;
    }

    private void processFileImport(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        @SuppressWarnings("unchecked")
        List<Map<Object, Object>> importsInfos = (List<Map<Object, Object>>) session.getAttribute("importsInfos");
        Map<Object, Object> siteKeyMapping = new HashMap<Object, Object>();
        boolean stillBad = false;
        JahiaSitesService jahiaSitesService = ServicesRegistry.getInstance().getJahiaSitesService();
        for (Map<Object, Object> infos : importsInfos) {
            File file = (File) infos.get("importFile");
            infos.put("sitekey", StringUtils.left(request.getParameter(file.getName() + "siteKey") == null ? null :
                    request.getParameter(file.getName() + "siteKey").trim(), 50));
            infos.put("oldsitekey", request.getParameter(file.getName() + "oldSiteKey") == null ? null :
                    request.getParameter(file.getName() + "oldSiteKey").trim());
            if (infos.get("sitekey") != null && !infos.get("sitekey").equals(infos.get("oldsitekey"))) {
                siteKeyMapping.put(infos.get("oldsitekey"), infos.get("sitekey"));
            }
            infos.put("siteservername", StringUtils.left(
                    request.getParameter(file.getName() + "siteServerName") == null ? null :
                            request.getParameter(file.getName() + "siteServerName").trim(), 200));
            infos.put("sitetitle", StringUtils.left(request.getParameter(file.getName() + "siteTitle") == null ? null :
                    request.getParameter(file.getName() + "siteTitle").trim(), 100));
            infos.put("selected", request.getParameter(file.getName() + "selected"));
            infos.put("templates", request.getParameter(file.getName() + "templates"));
            infos.put("legacyMapping", request.getParameter(file.getName() + "legacyMapping"));
            infos.put("legacyDefinitions", request.getParameter(file.getName() + "legacyDefinitions"));
            if (request.getParameter(file.getName() + "selected") != null) {
                try {
                    if (NON_SITE_IMPORTS.contains(infos.get("importFileName"))) {

                    } else {
                       	infos.put("siteTitleInvalid", StringUtils.isEmpty((String) infos.get("sitetitle")));

                        String siteKey = (String) infos.get("sitekey");
	                    boolean valid = isSiteKeyValid(siteKey);
	                    infos.put("siteKeyInvalid", !valid);
                       	infos.put("siteKeyExists", valid && jahiaSitesService.getSiteByKey(siteKey) != null);

                       	String serverName = (String) infos.get("siteservername");
	                    valid = isServerNameValid(serverName);
	                    infos.put("siteServerNameInvalid", !valid);
                       	infos.put("siteServerNameExists", valid && !Url.isLocalhost(serverName) && jahiaSitesService.getSite(serverName) != null);

						stillBad = (Boolean) infos.get("siteKeyInvalid")
						        || (Boolean) infos.get("siteKeyExists")
						        || (Boolean) infos.get("siteServerNameInvalid")
						        || (Boolean) infos.get("siteServerNameExists");
                    }
                } catch (JahiaException e) {
                    logger.error("Error while processing file import", e);
                }
            } else {
                infos.put("siteKeyInvalid", Boolean.FALSE);
                infos.put("siteKeyExists", Boolean.FALSE);
                infos.put("siteServerNameInvalid", Boolean.FALSE);
                infos.put("siteServerNameExists", Boolean.FALSE);

            }
        }
        if (stillBad) {
            try {
                TreeMap<String, JCRNodeWrapper> orderedTemplateSets = getTemplatesSets();

                request.setAttribute("tmplSets", new ArrayList<JCRNodeWrapper>(orderedTemplateSets.values()));
            } catch (RepositoryException e) {
            	logger.error(e.getMessage(), e);
            }

            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "import_choose.jsp");
        } else {

            boolean doImportServerPermissions = false;
            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (request.getParameter(file.getName() + "selected") != null
                        && infos.get("importFileName").equals("serverPermissions.xml")) {
                    doImportServerPermissions = true;
                    break;
                }
            }

            request.setAttribute("doImportServerPermissions", Boolean.valueOf(doImportServerPermissions));

            for (Map<Object, Object> infos : importsInfos) {
                File file = (File) infos.get("importFile");
                if (request.getParameter(file.getName() + "selected") != null &&
                        infos.get("importFileName").equals("users.xml")) {
                    try {
                        ImportExportBaseService.getInstance().importUsers(file);
                    } finally {
                        file.delete();
                    }
                    break;
                }
            }

            try {
                for (Map<Object, Object> infos : importsInfos) {
                    File file = (File) infos.get("importFile");
                    if (request.getParameter(file.getName() + "selected") != null) {
                        if (infos.get("type").equals("files")) {
                            try {
                                JahiaSite system = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(JahiaSitesBaseService.SYSTEM_SITE_KEY);

                                Map<String,String> pathMapping = JCRSessionFactory.getInstance().getCurrentUserSession().getPathMapping();
                                pathMapping.put("/shared/files/", "/sites/" + system.getSiteKey() + "/files/");
                                pathMapping.put("/shared/mashups/", "/sites/" + system.getSiteKey() + "/portlets/");

                                ImportExportBaseService.getInstance().importSiteZip(file, system, infos);
                            } catch (Exception e) {
                                logger.error("Error when getting templates", e);
                            }
                        } else if (infos.get("type").equals("xml") &&
                                (infos.get("importFileName").equals("serverPermissions.xml") ||
                                        infos.get("importFileName").equals("users.xml"))) {

                        } else if (infos.get("type").equals("site")) {
                            // site import
                            String tpl = (String) infos.get("templates");
                            if ("".equals(tpl)) {
                                tpl = null;
                            }
                            Object legacyImport = infos.get("legacyImport");
                            String legacyImportFilePath = null;
                            String legacyDefinitionsFilePath = null;
                            if(legacyImport != null && (Boolean) legacyImport) {
                                legacyImportFilePath = (String) infos.get("legacyMapping");
                                if (legacyImportFilePath != null && "".equals(legacyImportFilePath.trim())){
                                    legacyImportFilePath = null;
                                }
                                legacyDefinitionsFilePath = (String) infos.get("legacyDefinitions");
                                if (legacyDefinitionsFilePath != null && "".equals(legacyDefinitionsFilePath.trim())){
                                    legacyDefinitionsFilePath = null;
                                }
                            }
                            Locale defaultLocale = determineDefaultLocale(jParams, infos);
                            try {
                                JahiaSite site = jahiaSitesService
                                        .addSite(jParams.getUser(), (String) infos.get("sitetitle"),
                                                (String) infos.get("siteservername"), (String) infos.get("sitekey"), "",
                                                defaultLocale, tpl, null, "fileImport", file,
                                                (String) infos.get("importFileName"), false, false, (String) infos.get("originatingJahiaRelease"),legacyImportFilePath,legacyDefinitionsFilePath);
                                session.setAttribute(ProcessingContext.SESSION_SITE, site);
                                jParams.setSite(site);
                                jParams.setSiteID(site.getID());
                                jParams.setSiteKey(site.getSiteKey());
                                jParams.setCurrentLocale(defaultLocale);


                            } catch (Exception e) {
                                logger.error("Cannot create site " + infos.get("sitetitle"), e);
                            }
                        }
                    }
                }
            } finally {
                for (Map<Object, Object> infos : importsInfos) {
                    FileUtils.deleteQuietly((File) infos.get("importFile"));
                }
            }

            CompositeSpellChecker.updateSpellCheckerIndex();

            redirectAfterAdd(request, response, session);
        }
    }

    private TreeMap<String, JCRNodeWrapper> getTemplatesSets() throws RepositoryException {
        return getModulesOfType(JahiaTemplateManagerService.MODULE_TYPE_TEMPLATES_SET);
    }

    private TreeMap<String, JCRNodeWrapper> getModulesOfType(String... moduleTypes) throws RepositoryException {
        Set<String> types = new HashSet<String>(Arrays.asList(moduleTypes));
        JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        TreeMap<String, JCRNodeWrapper> orderedTemplateSets = new TreeMap<String, JCRNodeWrapper>();
        final JCRNodeWrapper templatesSet =
                JCRStoreService.getInstance().getSessionFactory().getCurrentUserSession().getNode("/templateSets");
        NodeIterator templates = templatesSet.getNodes();
        while (templates.hasNext()) {
            JCRNodeWrapper node = (JCRNodeWrapper) templates.next();
            if (!node.getName().equals("templates-system") && node.hasProperty("j:siteType") &&
                    types.contains(node.getProperty("j:siteType").getString())) {
                if (managerService.getTemplatePackageByFileName(node.getName()) != null) {
                    orderedTemplateSets.put(node.getName(), node);
                }
            }
        }
        return orderedTemplateSets;
    }

    private Locale determineDefaultLocale(ProcessingContext jParams, Map<Object, Object> infos) {
        Locale defaultLocale = jParams.getLocale();
        SortedMap<Integer, String> activeLanguageCodesByRank = new TreeMap<Integer, String>();
        for (Map.Entry<Object, Object> info : infos.entrySet()) {
            if (info.getKey() instanceof String) {
                Matcher m = LANGUAGE_RANK_PATTERN.matcher((String) info.getKey());
                if (m.find()) {
                    String languageCode = m.group(1);
                    boolean activated =
                            Boolean.parseBoolean((String) infos.get("language." + languageCode + ".activated"));

                    if (activated) {
                        if ("1".equals(info.getValue())) {
                            return LanguageCodeConverters.languageCodeToLocale(languageCode);
                        } else {
                            activeLanguageCodesByRank.put(new Integer((String) info.getValue()), languageCode);
                        }
                    }
                }
            }
        }
        if (!activeLanguageCodesByRank.isEmpty()) {
            defaultLocale = LanguageCodeConverters
                    .languageCodeToLocale(activeLanguageCodesByRank.get(activeLanguageCodesByRank.firstKey()));
        }
        return defaultLocale;
    }

    private void redirectAfterAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        displayList(request, response, session);
    }

    /**
     * return a parameter of String type if not null or return the subsitute value
     *
     * @param processingContext the request object
     * @param paramName         the param name
     * @param nullVal           the subsitute value to return if the parameter is null
     * @return String the parameter value
     * @author NK
     */
    public static String getStrParameter(ProcessingContext processingContext, String paramName, String nullVal) {

        String val = (String) processingContext.getParameter(paramName);
        if (val == null) {
            return nullVal;
        }
        return val;
    }

    public static boolean isSiteKeyValid(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }

        if (JahiaSitesBaseService.SYSTEM_SITE_KEY.equals(name)) {
        	return false;
        }

        boolean valid = true;
        for (char toBeTested : name.toCharArray()) {
        	if (AUTHORIZED_CHARS.indexOf(toBeTested) == -1) {
        		valid = false;
        		break;
        	}
        }

        return valid;
    }
}
