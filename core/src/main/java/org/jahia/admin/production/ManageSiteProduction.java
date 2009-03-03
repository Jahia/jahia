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

package org.jahia.admin.production;

import org.apache.axis.encoding.Base64;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.importexport.ImportExportBaseService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.admin.AbstractAdministrationModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <p>Title: Manage site production</p>
 * <p>Description: Administration web user interface to manage the production
 * settings of a Jahia site.</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Jahia </p>
 *
 * @author Cédric Mailleux
 * @version 1.0
 */

public class ManageSiteProduction extends AbstractAdministrationModule {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageSiteProduction.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;

    /**
     * Default constructor.
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
        License coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = "Invalid license.";
            if (jParams != null) {
                dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                                                              jParams.getLocale());
            }
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
     */
    private void userRequestDispatcher (HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws Exception {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        String operation = request.getParameter("sub");

        ServicesRegistry sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        JahiaUser user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && sReg != null) {

            // set the new site id to administrate...
            request.setAttribute("site", site);

            if (operation.equals("display")) {
                displayProductionsInfos(request, response, session);
            } else if (operation.equals("commit")) {
                commitChanges(request, response, session);
            } else {
                displayProductionsInfos(request, response, session);
            }

        } else {
            String dspMsg = "Error during request processing.";
            if (jParams != null) {
                dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                                              jParams.getLocale());
            }
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "menu.jsp");
        }
    } // userRequestDispatcher


    //-------------------------------------------------------------------------
    private void displayProductionsInfos (HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_production.jsp");
    }


    //-------------------------------------------------------------------------
    private void commitChanges (HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        String[] targetSites = request.getParameter("targetSites").split(",");
        String[] sitenames = request.getParameter("sitenames").split(",");
        String[] usernames = request.getParameter("usernames").split(",");
        String[] passwords = request.getParameter("passwords").split(",");
        String[] crons = request.getParameter("crons").split(",");
        String[] profiles = request.getParameter("profiles").split(",");
        String[] aliases = request.getParameter("aliases").split(",");
        String[] metadatas = request.getParameter("metadatas").split(",");
        String[] workflows = request.getParameter("workflows").split(",");
        String[] acls = request.getParameter("acls").split(",");
        String[] publishes = request.getParameter("publishes").split(",");
        Properties settings = site.getSettings();
        // remove old properties to avoid non usable properties in the db
        try {
            String[] toRemove = settings.getProperty(ImportExportBaseService.PRODUCTION_TARGET_LIST_PROPERTY, "").split(",");
            List<String> propertiesToBeRemoved = new ArrayList<String>(toRemove.length * 3 + 1);
            propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_TARGET_LIST_PROPERTY);
            settings.remove(ImportExportBaseService.PRODUCTION_TARGET_LIST_PROPERTY);
            for (String s : toRemove) {
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_USERNAME_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_PASSWORD_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_CRON_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_PROFILE_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_SITE_NAME_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_ALIAS_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_METADATA_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_WORKFLOW_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_ACL_PROPERTY + s);
                propertiesToBeRemoved.add(ImportExportBaseService.PRODUCTION_AUTO_PUBLISH_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_USERNAME_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_PASSWORD_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_CRON_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_PROFILE_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_SITE_NAME_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_ALIAS_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_METADATA_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_WORKFLOW_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_ACL_PROPERTY + s);
                settings.remove(ImportExportBaseService.PRODUCTION_AUTO_PUBLISH_PROPERTY + s);
            }
            ServicesRegistry registry = ServicesRegistry.getInstance();
            registry.getJahiaSitesService().removeSiteProperties(site, propertiesToBeRemoved);
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        }
        StringBuffer targetSitesProperty = new StringBuffer();
        boolean errors = false;
        if (targetSites.length == usernames.length && targetSites.length == passwords.length &&
            targetSites.length == crons.length && targetSites.length == profiles.length) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            for (int i = 0; i < targetSites.length; i++) {
                String targetSite = targetSites[i].trim();
                if (!"".equals(targetSite)) {
                    String sitename = sitenames[i];
                    String username = usernames[i];
                    String password = passwords[i];
                    String cron = crons[i];
                    String profile = profiles[i];
                    String alias = aliases[i];
                    String metadata = metadatas[i];
                    String worfklow = workflows[i];
                    String acl = acls[i];
                    String publish = publishes[i];
                    try {
                        Date date = simpleDateFormat.parse(cron);
                        // We have an hour:minutes specified instaed of a true cron expression so let's convert it
                        // to a cron expression to ensure job scheduled on time every day of the year
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);
                        cron = "0 " + calendar.get(Calendar.MINUTE) + " " + calendar.get(Calendar.HOUR_OF_DAY) + " ? * *";
                    } catch (ParseException e) {
                        // Let's mange this expression as a standrad cron expression
                    }

                    JahiaUser p;
                    p = ServicesRegistry.getInstance()
                            .getJahiaSiteUserManagerService()
                            .getMember(site.getID(), profile);
                    if (p == null) {
                        logger.error("Invalid profile " + profile);
                        String warningMsg = "Invalid profile " + profile;
                        if (jParams != null) {
                            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.productionManager.error.profile",
                                                                                 jParams.getLocale());
                        }
                        request.setAttribute("warningMsg", warningMsg);
                        errors = true;
                        continue;
                    }

                    if (i > 0) {
                        targetSitesProperty.append(",");
                    }
                    try {
                        new URL(targetSite);
                    } catch (MalformedURLException e) {
                        String warningMsg = "Target Site URL is Malformed.";
                        if (jParams != null) {
                            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.productionManager.error.targetSite",
                                                                                 jParams.getLocale());
                        }
                        request.setAttribute("warningMsg", warningMsg);
                        errors = true;
                        continue;
                    }
                    targetSitesProperty.append(i);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_TARGET + i, targetSite);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_USERNAME_PROPERTY + i, username);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_PASSWORD_PROPERTY + i, Base64.encode(password.getBytes()));
                    settings.setProperty(ImportExportBaseService.PRODUCTION_CRON_PROPERTY + i, cron);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_PROFILE_PROPERTY + i, profile);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_SITE_NAME_PROPERTY + i, sitename);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_ALIAS_PROPERTY + i, alias);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_METADATA_PROPERTY + i, metadata);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_WORKFLOW_PROPERTY + i, worfklow);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_ACL_PROPERTY + i, acl);
                    settings.setProperty(ImportExportBaseService.PRODUCTION_AUTO_PUBLISH_PROPERTY + i, publish);
                } else {
                    String warningMsg = "Target site doesn't exist";
                    if (jParams != null) {
                        warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.productionManager.error.targetSite",
                                                                             jParams.getLocale());
                    }
                    request.setAttribute("warningMsg", warningMsg);
                    errors = true;
                }
            }
        } else {
            String warningMsg = "Some Fields Are Empty";
            if (jParams != null) {
                warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.productionManager.error.SomeFieldsAreEmpty",
                                                                     jParams.getLocale());
            }
            request.setAttribute("warningMsg", warningMsg);
            errors = true;
        }
        if (!errors) {
            settings.setProperty(ImportExportBaseService.PRODUCTION_TARGET_LIST_PROPERTY, targetSitesProperty.toString());
            try {
                ServicesRegistry registry = ServicesRegistry.getInstance();
                registry.getJahiaSitesService().updateSite(site);
                registry.getImportExportService().startProductionJob(site, jParams);
            } catch (JahiaException e) {
                String warningMsg = "processingRequestError";
                if (jParams != null) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                                                                         jParams.getLocale());
                }
                request.setAttribute("warningMsg", warningMsg);
            } catch (ParseException e) {
                String warningMsg = "processingRequestError";
                if (jParams != null) {
                    warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.processingRequestError.label",
                                                                         jParams.getLocale());
                }
                request.setAttribute("warningMsg", warningMsg);
            }
        }
        displayProductionsInfos(request, response, session);
    } // end addComponent
}