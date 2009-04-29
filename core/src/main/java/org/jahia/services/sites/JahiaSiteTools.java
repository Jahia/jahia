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
//
//  JahiaSite
//
//  NK      11.07.2001
//
//

package org.jahia.services.sites;

import java.io.File;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.deamons.filewatcher.JahiaFileWatcherService;
import org.jahia.services.deamons.filewatcher.webappobserver.WebAppsObserver;
import org.jahia.services.templates_deployer.JahiaTemplatesDeployerService;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteUserManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService;
import org.jahia.settings.SettingsBean;


/**
 * Class JahiaSiteTools.<br>
 *
 * @author Khue ng
 * @version 1.0
 */
public final class JahiaSiteTools {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaSiteTools.class);

    /**
     * Create a new membership for a user
     *
     * @param user  the user to add as member
     * @param site  the site
     */
    public static boolean addMember (JahiaUser user, JahiaSite site)
            throws JahiaException {

        if (site == null || user == null) {
            return false;
        }
        JahiaSiteUserManagerService jsums = ServicesRegistry.getInstance ()
                .getJahiaSiteUserManagerService ();

        if (jsums == null) {
            return false;
        }

        return jsums.addMember (site.getID (), user);
    }


    /**
     * Add a group membership on this site ( in a group sharing context )
     *
     * @param grp   the group reference
     * @param site  the site reference
     */
    public static boolean addGroup (JahiaGroup grp, JahiaSite site)
            throws JahiaException {

        if (site == null || grp == null) {
            return false;
        }

        JahiaSiteGroupManagerService jsgms = ServicesRegistry.getInstance ()
                .getJahiaSiteGroupManagerService ();
        if (jsgms == null) {
            return false;
        }

        return jsgms.addGroup (site.getID (), grp);
    }


    /**
     * return the admin group of this site
     *
     * @param site  the site reference
     */
    public static JahiaGroup getAdminGroup (JahiaSite site)
            throws JahiaException {

        if (site == null) {
            return null;
        }

        JahiaGroupManagerService jgms = ServicesRegistry.getInstance ()
                .getJahiaGroupManagerService ();
        if (jgms == null) {
            return null;
        }

        return jgms.getAdministratorGroup (site.getID ());
    }


    /**
     * Create the template directory for a gived site
     *
     * @param site  the site reference
     * @deprecated due to changes in the template deployment
     */
    public static boolean createTemplateDir (JahiaSite site) {

        if (site == null) {
            return false;
        }

        JahiaTemplatesDeployerService jtds =
                ServicesRegistry.getInstance ()
                .getJahiaTemplatesDeployerService ();
        if (jtds == null) {
            return false;
        }

        // get the root folder for all templates
        String jahiaTemplatesRootPath = jtds.getTemplateRootPath ();
        if (jahiaTemplatesRootPath == null) {
            return false;
        }

        // compose the full template path for this site
        StringBuffer buff = new StringBuffer (jahiaTemplatesRootPath);
        buff.append (File.separator);
        buff.append (site.getSiteKey ());

        logger.debug (" start creating the template path " + buff.toString ());

        // create the folder
        File f = new File (buff.toString ());
        if (!f.isDirectory ()) {
            return f.mkdirs ();
        } else {
            return true;
        }

    }


    /**
     * Return the full path to a site's template root folder
     *
     * @param site  the site reference
     *
     * @return String
     * @deprecated due to changes in the template deployment
     */
    public static String getSiteTemplateFolder (JahiaSite site) throws JahiaException {

        if (site == null) {
            return null;
        }

        String templateRootPath = ServicesRegistry.getInstance ()
                .getJahiaTemplatesDeployerService ()
                .getTemplateRootPath ();

        String templateContext = ServicesRegistry.getInstance ()
                .getJahiaTemplatesDeployerService ()
                .getTemplatesContext ();

        if (templateRootPath == null || templateContext == null) {
            return null;
        }


        String path = templateRootPath + File.separator + site.getSiteKey ();

        return path;

    }

    /**
     * Create a web apps repository used to store new webapps for a site.
     *
     * @return boolean false on error
     */
    private static boolean createSiteNewWebAppsFolder(JahiaWebAppsDeployerService wads) {

        if (wads == null) {
            return false;
        }

        String path = JahiaWebAppsDeployerService.getNewWebAppsPath ();
        if (path == null) {
            return false;
        }

        StringBuffer buff = new StringBuffer (path);

        File f = new File (buff.toString ());

        if (!f.isDirectory ()) {
            return f.mkdirs ();
        }
        return true;
    }


    /**
     * Start a file watcher to watch a site's new web apps folder
     *
     * @return boolean false on error
     *
     */
    public static boolean startWebAppsObserver(SettingsBean settingsBean,
            JahiaSitesService ss,
            JahiaWebAppsDeployerService wads,
            JahiaFileWatcherService fileWatcherService) {

        if (wads == null) {
            return false;
        }

        StringBuffer buff = new StringBuffer (JahiaWebAppsDeployerService.getNewWebAppsPath ());
        File f = new File (buff.toString ());

        boolean success = true;

        if (!f.isDirectory ()) {
            // try to create it
            success = createSiteNewWebAppsFolder (wads);
        }

        if (success) {
            long interval = settingsBean.getWebAppsObserverInterval();
            if (interval == -1) {
                return false;
            }

            boolean fileOnly = true;
            boolean checkDate = false;

            try {
                new WebAppsObserver (JahiaWebAppsDeployerService.getNewWebAppsPath (),
                        checkDate,
                        interval,
                        fileOnly, ss, fileWatcherService, wads);
                return true;
            } catch (JahiaException je) {
                logger.error ("exception with FilesObserver", je);
            }
        }
        return false;
    }



}
