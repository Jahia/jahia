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

//
//  JahiaSitesService
//
//  NK      12.03.2001
//
package org.jahia.services.sites;

import org.jahia.exceptions.JahiaException;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.sites.JahiaSite;

import java.util.Iterator;
import java.util.Locale;
import java.io.File;
import java.io.IOException;

/**
 * Jahia Multi Sites Management Service
 *
 * @author Khue ng
 */
public abstract class JahiaSitesService extends JahiaService {

    /**
     * return the list of all sites
     *
     * @return Iterator an Iterator of JahiaSite bean
     */
    public abstract Iterator<JahiaSite> getSites()
            throws JahiaException;

    /**
     * return the site bean looking at it id
     *
     * @param id the JahiaSite id
     *
     * @return JahiaSite the JahiaSite bean
     */
    public abstract JahiaSite getSite (int id)
            throws JahiaException;


    /**
     * return a site looking at its key
     *
     * @param siteKey site key
     *
     * @return JahiaSite the JahiaSite bean or null
     */
    public abstract JahiaSite getSiteByKey (String siteKey)
            throws JahiaException;

    /**
     * Find a site by it's server name value
     *
     * @param serverName the server name to look for
     *
     * @return if found, returns the site with the corresponding serverName,
     *         or the first one if there are multiple, or null if there are none.
     *
     * @throws JahiaException thrown if there was a problem communicating with
     *                        the database.
     */
    public abstract JahiaSite getSiteByServerName (String serverName)
            throws JahiaException;

    /**
     * return a site looking at its server name
     *
     * @param String site name
     *
     * @return JahiaSite the JahiaSite bean or null
     */
    public abstract JahiaSite getSite (String name)
            throws JahiaException;


    /**
     * Add a new site only if there is no other site with same server name
     *
     * @return boolean false if there is another site using same server name
     *
     * @auhtor NK
     */
    public abstract JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                                      Locale selectedLocale, String selectTmplSet, String firstImport, File fileImport, String fileImportName,
                                      Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException;

    /**
     * Add a new site
     * @param currentUser
     * @param title
     * @param serverName
     * @param siteKey
     * @param descr
     * @param selectedLocale
     * @param selectTmplSet
     * @param modulesToDeploy
     * @param firstImport
     * @param fileImport
     * @param fileImportName
     * @param asAJob
     * @param doImportServerPermissions
     * @param originatingJahiaRelease
     * @return
     * @throws JahiaException
     * @throws IOException
     * @since Jahia 6.6
     */
    public abstract JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                                      Locale selectedLocale, String selectTmplSet, String[] modulesToDeploy, String firstImport, File fileImport, String fileImportName,
                                      Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException;
    /**
     * Add a new site
     * @param currentUser
     * @param title
     * @param serverName
     * @param siteKey
     * @param descr
     * @param selectedLocale
     * @param selectTmplSet
     * @param modulesToDeploy
     * @param firstImport
     * @param fileImport
     * @param fileImportName
     * @param asAJob
     * @param doImportServerPermissions
     * @param originatingJahiaRelease
     * @param legacyMappingFilePath
     * @return
     * @throws JahiaException
     * @throws IOException
     * @since Jahia 6.6
     */
    public abstract JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                                      Locale selectedLocale, String selectTmplSet, String[] modulesToDeploy, String firstImport, File fileImport, String fileImportName,
                                      Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease,
                                      String legacyMappingFilePath, String legacyDefinitionsFilePath) throws JahiaException, IOException;



    /**
     * remove a site
     *
     * @param JahiaSite the JahiaSite bean
     */
    public abstract void removeSite (JahiaSite site)
            throws JahiaException;


    /**
     * Update a JahiaSite definition
     *
     * @param JahiaSite the site bean object
     */
    public abstract void updateSite (JahiaSite site)
            throws JahiaException;


    /**
     * Return the amount of sites in the system.
     *
     * @return Amount of sites in the system.
     */
    public abstract int getNbSites ()
            throws JahiaException;

    public abstract JahiaSite getDefaultSite();

    public abstract void setDefaultSite(JahiaSite site);

}