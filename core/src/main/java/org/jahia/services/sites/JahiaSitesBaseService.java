/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

//
//  JahiaSitesBaseService
//
//  NK      12.03.2001
//
package org.jahia.services.sites;

import org.jahia.api.Constants;
import org.jahia.services.JahiaAfterInitializationService;
import org.jahia.services.content.*;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.jcr.JCRSitesProvider;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Jahia Multi Sites Management Service
 *
 * @author Khue ng
 */
public class JahiaSitesBaseService extends JahiaSitesService implements JahiaAfterInitializationService {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaSitesBaseService.class);

    protected static JahiaSitesBaseService instance = null;

    public static final String SITE_CACHE_BYID = "JahiaSiteByIDCache";
    public static final String SITE_CACHE_BYNAME = "JahiaSiteByNameCache";
    public static final String SITE_CACHE_BYKEY = "JahiaSiteByKeyCache";
    public static final String SYSTEM_SITE_KEY = "systemsite";
    /**
     * The cache in memory
     */
    private Cache<Comparable<?>, JahiaSite> siteCacheByID = null;
    private Cache<String, Serializable> siteCacheByName = null;
    private Cache<String, JahiaSite> siteCacheByKey = null;

    protected JCRSitesProvider siteProvider = null;

    private CacheService cacheService;

    protected JahiaGroupManagerService groupService;
    protected JCRSessionFactory sessionFactory;
    private String systemSiteDefaultLanguage = "en";
    private String systemSiteTitle = "System Site";
    private String systemSiteServername = "localhost";
    private String systemSiteTemplateSetName = "templates-system";

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setSiteProvider(JCRSitesProvider siteProvider) {
        this.siteProvider = siteProvider;
    }

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public void setSessionFactory(JCRSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    /**
     * Default constructor, creates a new <code>JahiaSitesBaseService</code> instance.
     *
     * @throws JahiaException
     */
    protected JahiaSitesBaseService() throws JahiaException {
    }


    /**
     * Retrieves the unique instance of this singleton class.
     *
     * @return the unique instance of this class
     * @throws JahiaException
     */
    public static synchronized JahiaSitesBaseService getInstance()
            throws JahiaException {
        if (instance == null) {
            instance = new JahiaSitesBaseService();
        }
        return instance;
    }


    /**
     * return the list of all sites
     *
     * @return Iterator an Iterator of JahiaSite bean
     * @todo this only returns the entries that are in the cache !! If the
     * cache was flushed in the meantime, this method is FALSE !
     */
    public Iterator<JahiaSite> getSites() throws JahiaException {
        return new ArrayList<JahiaSite>(siteProvider.getSites()).iterator();
    }

    public void start()
            throws JahiaInitializationException {

        siteCacheByID = cacheService.createCacheInstance(SITE_CACHE_BYID);
        siteCacheByName = cacheService.createCacheInstance(SITE_CACHE_BYNAME);
        siteCacheByKey = cacheService.createCacheInstance(SITE_CACHE_BYKEY);
    }

    public void stop() {
    }

    /**
     * return a site bean looking at it id
     *
     * @param id the JahiaSite id
     * @return JahiaSite the JahiaSite bean
     */
    public JahiaSite getSite(int id) throws JahiaException {
        JahiaSite site = siteCacheByID.get(new Integer(id));
        if (site != null) {
            return site;
        }
        // try to load from db

        site = siteProvider.getSiteById(id);
        // if the site could be loaded, add it into the cache
        if (site != null) {
            addToCache(site);
        }

        return site;
    }


    private synchronized void addToCache(JahiaSite site) {
        siteCacheByID.put(site.getID(), site);
        if (site.getServerName() != null) {
            siteCacheByName.put(site.getServerName(), site);
        }
        siteCacheByKey.put(site.getSiteKey(), site);
    }

    /**
     * return a site bean looking at it key
     *
     * @param siteKey the site key
     * @return JahiaSite the JahiaSite bean
     */
    public JahiaSite getSiteByKey(String siteKey) throws JahiaException {
        if (siteKey == null) {
            return null;
        }

        if (siteCacheByKey.containsKey(siteKey))
            return siteCacheByKey.get(siteKey);
        JahiaSite site = siteProvider.getSiteByKey(siteKey);
        // if the site could be loaded from the database, add it into the cache.
        if (site != null) {
            addToCache(site);
        } else {
            siteCacheByKey.put(siteKey, null);
        }

        return site;
    }

    /**
     * Find a site by it's server name value
     *
     * @param serverName the server name to look for
     * @return if found, returns the site with the corresponding serverName, or
     *         the first one if there are multiple, or null if there are none.
     * @throws JahiaException thrown if there was a problem communicating with the
     *                        database.
     */
    public JahiaSite getSiteByServerName(String serverName)
            throws JahiaException {
        if (serverName == null) {
            return null;
        }
        Object cachedObj = siteCacheByName.get(serverName);

        if (cachedObj != null) {
            return cachedObj.equals("") ? null : (JahiaSite) cachedObj;
        }

        // the site was not found in the cache, try to load it from the
        // database.
        JahiaSite site = siteProvider.getSiteByName(serverName);
        // if the site could be loaded from the database, add it into the cache.
        if (site != null) {
            addToCache(site);
        } else {
            siteCacheByName.put(serverName, "");
        }

        return site;
    }


    // --------------------------------------------------------------------------
    // FH 10 May 2001 Cache storing improvments for performance issues.

    /**
     * return a site looking at it's name
     *
     * @param name the site name
     * @return JahiaSite the JahiaSite bean or null
     */
    public JahiaSite getSite(String name) throws JahiaException {
        return getSiteByServerName(name);
    }


    // --------------------------------------------------------------------------

    /**
     * Add a new site only if there is no other site with same server name
     *
     * @return boolean false if there is another site using same server name
     */

//    public synchronized boolean addSite (JahiaSite site)
//            throws JahiaException {
//
    public JahiaSite addSite(JahiaUser currentUser, String title, String serverName, String siteKey, String descr,
                             Locale selectedLocale, String selectTmplSet, String firstImport, File fileImport, String fileImportName,
                             Boolean asAJob, Boolean doImportServerPermissions, String originatingJahiaRelease) throws JahiaException, IOException {
        JahiaSite site = new JahiaSite(-1, title, serverName, siteKey, descr, null, null);

        if (selectTmplSet != null) {
            site.setTemplatePackageName(selectTmplSet);
        }
        // create site language
        site.setDefaultLanguage(selectedLocale.toString());
        site.setLanguages(new LinkedHashSet<String>(Arrays.asList(selectedLocale.toString())));
        site.setMandatoryLanguages(site.getLanguages());
        site.setMixLanguagesActive(false);
        // check there is no site with same server name before adding
        boolean importingSystemSite = false;
        if (getSiteByKey(site.getSiteKey()) == null) {
            siteProvider.addSite(site, site.getSiteKey(), site.getTemplatePackageName(), site.getTitle(),
                    site.getDescr(), site.getServerName(), site.getDefaultLanguage(), site.isMixLanguagesActive(), site.getLanguages(),
                    site.getMandatoryLanguages());
            if (site.getID() == -1) {
                return null;
            }

            addToCache(site);

        } else if (siteKey.equals(SYSTEM_SITE_KEY)) {
            site = getSiteByKey(SYSTEM_SITE_KEY);
            importingSystemSite = true;
        } else {
            throw new IOException("site already exists");
        }


        // continue if the site is added correctly...
        if (site.getID() != -1) {
            if (getNbSites() == 1 && !site.isDefault() && !site.getSiteKey().equals(SYSTEM_SITE_KEY)) {
                setDefaultSite(site);
            }
            if (!importingSystemSite) {
                JahiaGroupManagerService jgms = ServicesRegistry.getInstance().getJahiaGroupManagerService();

                updateSite(site);

                site.setMixLanguagesActive(false);

                // create default groups...
                JahiaGroup usersGroup = jgms.lookupGroup(0, JahiaGroupManagerService.USERS_GROUPNAME);
                if (usersGroup == null) {
                    usersGroup = jgms.createGroup(0, JahiaGroupManagerService.USERS_GROUPNAME, null, false);
                }

                JahiaGroup guestGroup = jgms.lookupGroup(0, JahiaGroupManagerService.GUEST_GROUPNAME);
                if (guestGroup == null) {
                    guestGroup = jgms.createGroup(0, JahiaGroupManagerService.GUEST_GROUPNAME, null, false);
                }

                JahiaGroup privGroup = jgms.lookupGroup(0, JahiaGroupManagerService.PRIVILEGED_GROUPNAME);
                if (privGroup == null) {
                    privGroup = jgms.createGroup(0, JahiaGroupManagerService.PRIVILEGED_GROUPNAME, null, false);
                }

                JahiaGroup adminGroup = jgms.lookupGroup(site.getID(),
                        JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);
                if (adminGroup == null) {
                    adminGroup = jgms.createGroup(site.getID(), JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME, null,
                            false);
                }

                JahiaGroup sitePrivGroup = jgms.lookupGroup(site.getID(),
                        JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME);
                if (sitePrivGroup == null) {
                    sitePrivGroup = jgms.createGroup(site.getID(), JahiaGroupManagerService.SITE_PRIVILEGED_GROUPNAME, null,
                            false);
                }

                // attach superadmin user (current) to administrators group...
                adminGroup.addMember(currentUser);

                // atach site privileged group to server privileged
                privGroup.addMember(sitePrivGroup);
            }
            File initialZip = null;
            String initialZipName = null;
            if ("fileImport".equals(firstImport)) {
                initialZip = fileImport;
                initialZipName = fileImportName;
            }

            // create the default homepage...
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(selectedLocale);

            if ("importRepositoryFile".equals(firstImport) || (initialZip != null && initialZip.exists() && !"noImport".equals(firstImport))) {
                // enable admin group to admin the page
//                if (asAJob == null || asAJob.booleanValue()) {
//
//                    // check if we need to import users synchronously
//                    if (doImportServerPermissions != null
//                            && doImportServerPermissions.booleanValue()) {
//                        // import users and groups
//                        try {
//                            ImportExportBaseService.getInstance()
//                                    .importUsersFromZip(initialZip, jParams.getSite());
//                        } catch (Exception e) {
//                            logger.error(
//                                    "Unable to import users for the site '"
//                                            + site.getTitle() + "'["
//                                            + site.getSiteKey() + "]", e);
//                        }
//                    }
//
//                    JobDetail jobDetail = BackgroundJob.createJahiaJob("Initial import for site " + site.getSiteKey(), ImportJob.class);
//                    JobDataMap jobDataMap;
//                    jobDataMap = jobDetail.getJobDataMap();
//
//                    if ("importRepositoryFile".equals(firstImport)) {
//                        jobDataMap.put(ImportJob.URI, fileImportName);
//                        jobDataMap.put(ImportJob.FILENAME, fileImportName.substring(fileImportName.lastIndexOf('/') + 1));
//                    } else {
//                        String dateOfExport = ImportExportBaseService.DATE_FORMAT.format(new Date());
//                        String uploadname = "initialImport_" + dateOfExport.replaceAll(":", "-") + ".zip";
//                        List<JCRNodeWrapper> l = ServicesRegistry.getInstance().getJCRStoreService().getImportDropBoxes(null, jParams.getUser());
//                        JCRNodeWrapper importFolder = l.iterator().next();
//                        try {
//                            importFolder.uploadFile(uploadname, new FileInputStream(
//                                    initialZip),
//                                    "application/zip");
//                            importFolder.save();
//                        } catch (RepositoryException e) {
//                            logger.error("cannot upload file", e);
//                        }
//                        jobDataMap.put(ImportJob.URI, importFolder.getPath() + "/" + uploadname);
//                        if (initialZipName == null) {
//                            initialZipName = initialZip.getName();
//                        }
//                        jobDataMap.put(ImportJob.FILENAME, initialZipName);
//                    }
//                    jobDataMap.put(ImportJob.JOB_SITEKEY, site.getSiteKey());
//                    jobDataMap.put(ImportJob.CONTENT_TYPE, "application/zip");
//                    jobDataMap.put(BackgroundJob.JOB_DESTINATION_SITE, site.getID());
//                    jobDataMap.put(ImportJob.DELETE_FILE, Boolean.TRUE);
//                    jobDataMap.put(ImportJob.COPY_TO_JCR, Boolean.TRUE);
//                    jobDataMap.put(ImportJob.ORIGINATING_JAHIA_RELEASE, originatingJahiaRelease);
//
//                    ServicesRegistry.getInstance().getSchedulerService().scheduleJobNow(jobDetail);
//                } else {
                try {
                    Map<Object, Object> importInfos = new HashMap<Object, Object>();
                    importInfos.put("originatingJahiaRelease", originatingJahiaRelease);
                    ServicesRegistry.getInstance().getImportExportService().importSiteZip(initialZip, site, importInfos);
                } catch (RepositoryException e) {
                    logger.warn("Error importing site ZIP", e);
                }
//                }
            } else {
                try {
                    JCRSessionWrapper session = sessionFactory.getCurrentUserSession(null, selectedLocale);
                    JCRNodeWrapper nodeWrapper = session.getNode("/sites/" + site.getSiteKey());
                    if (!nodeWrapper.hasNode("home")) {
                        session.checkout(nodeWrapper);
                        JCRNodeWrapper page = nodeWrapper.addNode("home", "jnt:page");
                        page.setProperty("jcr:title", "Welcome to " + site.getServerName());
                        session.save();
                    }
                } catch (RepositoryException e) {
                    logger.warn("Error adding home node", e);
                }
            }

            try {
                JCRSessionWrapper session = sessionFactory.getCurrentUserSession(null, selectedLocale);
                JCRNodeWrapper nodeWrapper = session.getNode("/sites/" + site.getSiteKey());
                if (nodeWrapper.hasNode("templates")) {
                    JCRPublicationService.getInstance().publishByMainId(nodeWrapper.getNode("templates").getIdentifier(),"default","live",null,true, null);
                }
            } catch (RepositoryException e) {
                logger.warn("Error adding home node", e);
            }

            logger.debug("Site updated with Home Page");
        } else {
            removeSite(site);      // remove site because the process generate error(s)...
            return null;
        }
        return site;
    }


    /**
     * remove a site
     *
     * @param site the JahiaSite bean
     */
    public synchronized void removeSite(JahiaSite site) throws JahiaException {
        List<String> groups = groupService.getGroupList(site.getID());
        for (String group : groups) {
            groupService.deleteGroup(groupService.lookupGroup(group.replace("{jcr}","")));
        }
        siteProvider.deleteSite(site.getSiteKey());

        invalidateCache(site);
    }


    /**
     * Update a JahiaSite definition
     *
     * @param site the site bean object
     */
    public synchronized void updateSite(JahiaSite site) throws JahiaException {
        siteProvider.updateSite(site);
        siteCacheByName.flush();
        siteCacheByID.flush();
        siteCacheByKey.flush();
    }


    // javadocs automaticaly imported from the JahiaSitesService class.
    //

    public int getNbSites()
            throws JahiaException {
        return siteProvider.getNbSites();
    }

    public JahiaSite getDefaultSite() {
        JahiaSite site = siteCacheByID.get("default");
        if (site != null) {
            return site;
        }
        site = siteProvider.getDefaultSite();
        siteCacheByID.put("default", site);
        return site;
    }

    public void setDefaultSite(JahiaSite site) {
        siteProvider.setDefaultSite(site);
        siteCacheByID.put("default", site);
    }

    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        try {
            JahiaUser jahiaUser = JCRSessionFactory.getInstance().getCurrentUser();
            if (getNbSites() == 0) {
                Locale selectedLocale = LanguageCodeConverters.languageCodeToLocale(systemSiteDefaultLanguage);
                JahiaSite site = addSite(jahiaUser, systemSiteTitle, systemSiteServername, SYSTEM_SITE_KEY, "", selectedLocale,
                        systemSiteTemplateSetName, "noImport", null, null, false, false, null);
                site.setMixLanguagesActive(true);
                updateSite(site);
                final LinkedHashSet<String> languages = new LinkedHashSet<String>();
                languages.add(selectedLocale.toString());
                final List<String> uuids = new ArrayList<String>();
                 try {
                     JCRNodeWrapper node =(JCRNodeWrapper) sessionFactory.getCurrentUserSession().getNode("/sites").getNodes().nextNode();
                     node.checkout();
//                     node.changeRoles("g:users","re---");
                     uuids.add(node.getIdentifier());
                    List<PublicationInfo> publicationInfos = JCRPublicationService.getInstance().getPublicationInfo(node.getIdentifier(),languages,true,true,true, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE);
                    for (PublicationInfo publicationInfo : publicationInfos) {
                        if (publicationInfo.needPublication(null)) {
                            uuids.addAll(publicationInfo.getAllUuids());
                        }
                    }
                    JCRPublicationService.getInstance().publish(uuids, Constants.EDIT_WORKSPACE, Constants.LIVE_WORKSPACE, null);
                } catch (RepositoryException e) {
                    e.printStackTrace();
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setSystemSiteDefaultLanguage(String systemSiteDefaultLanguage) {
        this.systemSiteDefaultLanguage = systemSiteDefaultLanguage;
    }

    public void setSystemSiteServername(String systemSiteServername) {
        this.systemSiteServername = systemSiteServername;
    }

    public void setSystemSiteTemplateSetName(String systemSiteTemplateSetName) {
        this.systemSiteTemplateSetName = systemSiteTemplateSetName;
    }

    public void setSystemSiteTitle(String systemSiteTitle) {
        this.systemSiteTitle = systemSiteTitle;
    }

    /**
     * Invalidates the cache for the specified site.
     *
     * @param site the site bean object
     */
    public void invalidateCache(JahiaSite site) throws JahiaException {
        siteCacheByID.remove(site.getID());
        siteCacheByName.remove(site.getServerName());
        siteCacheByKey.remove(site.getSiteKey());
    }

}
